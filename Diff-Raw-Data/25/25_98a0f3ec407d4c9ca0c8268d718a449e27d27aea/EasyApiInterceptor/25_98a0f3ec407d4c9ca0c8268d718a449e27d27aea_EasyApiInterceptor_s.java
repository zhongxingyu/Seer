 package net.unit8.sastruts.easyapi;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.unit8.sastruts.easyapi.dto.ErrorDto;
 import net.unit8.sastruts.easyapi.dto.FailureDto;
 import net.unit8.sastruts.easyapi.dto.RequestDto;
 import net.unit8.sastruts.easyapi.dto.ResponseDto;
 
 import org.aopalliance.intercept.MethodInvocation;
 import org.seasar.framework.aop.interceptors.AbstractInterceptor;
 import org.seasar.framework.beans.BeanDesc;
 import org.seasar.framework.beans.PropertyDesc;
 import org.seasar.framework.beans.factory.BeanDescFactory;
 import org.seasar.framework.container.annotation.tiger.Binding;
 import org.seasar.framework.container.annotation.tiger.BindingType;
 import org.seasar.framework.log.Logger;
 import org.seasar.framework.util.StringUtil;
 import org.seasar.struts.util.RequestUtil;
 import org.seasar.struts.util.ResponseUtil;
 
 import com.thoughtworks.xstream.XStream;
 
 @SuppressWarnings("serial")
 public class EasyApiInterceptor extends AbstractInterceptor {
 	private static final Logger logger = Logger.getLogger(EasyApiInterceptor.class);
 
 	@Binding(bindingType=BindingType.MAY)
 	public String systemErrorCode = "9999";
 
 	@Binding(bindingType=BindingType.MAY)
 	public String transactionIdName = "X-Transaction-Id";
 
 	@Binding(bindingType=BindingType.MAY)
 	public String encoding = "UTF-8";
 
 	@Binding(bindingType=BindingType.MAY)
 	public String contentType = "text/xml";
 
 	public Object invoke(MethodInvocation invocation) throws Throwable {
 		EasyApi easyApiAnno = invocation.getMethod().getAnnotation(EasyApi.class);
 		Object action = invocation.getThis();
 		String transactionId = null;
 
 		Object ret = null;
 
 		if (easyApiAnno != null) {
 			BeanDesc beanDesc = BeanDescFactory.getBeanDesc(action.getClass());
 
 			String requestDtoName = easyApiAnno.requestDto();
 			ResponseDto responseDto = new ResponseDto();
 			HttpServletResponse response = ResponseUtil.getResponse();
 			try {
 				if (StringUtil.isNotEmpty(requestDtoName)) {
 					PropertyDesc requestPropDesc = beanDesc.getPropertyDesc(requestDtoName);
 
 					HttpServletRequest request = RequestUtil.getRequest();
 					transactionId = request.getHeader(transactionIdName);
 					InputStream in = request.getInputStream();
 					XStream xstream = XStreamFactory.getInstance();
 					XStreamFactory.setBodyDto(requestPropDesc.getPropertyType());
 					RequestDto requestDto = (RequestDto)xstream.fromXML(in);
 					requestPropDesc.setValue(action, requestDto.body);
 				}
 
 				response.setCharacterEncoding(encoding);
 				response.setContentType(contentType);
 
 				ret = invocation.proceed();
 				response.setStatus(HttpServletResponse.SC_OK);
 			} catch (EasyApiException cause) {
				logger.log(transactionId == null ? "WSEA0007" : "WSEA0008",
						transactionId == null ? new Object[]{cause.getMessageCode()} : new Object[]{cause.getMessageCode(), transactionId},
 						cause);
 				Iterator<EasyApiException> iter = cause.iterator();
 				responseDto.header.failures = new ArrayList<FailureDto>();
 				while(iter.hasNext()) {
 					EasyApiException e = iter.next();
 					responseDto.header.failures.add(new FailureDto(e.getMessageCode(), e.getMessage()));
 				}
 				response.setStatus(HttpServletResponse.SC_OK);
 			} catch (Throwable e) {
				logger.log(transactionId == null ? "ESEA0005" : "ESEA0006",
						transactionId == null ? new Object[]{} : new Object[]{transactionId}, e);
 				responseDto.header.errors = new ArrayList<ErrorDto>();
 				responseDto.header.errors.add(new ErrorDto(systemErrorCode, e.getMessage()));
 				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			}
 
 			String responseBodyName = easyApiAnno.responseDto();
 			if (StringUtil.isNotEmpty(responseBodyName)) {
 				PropertyDesc responseBodyPropDesc = beanDesc.getPropertyDesc(responseBodyName);
 				responseDto.body = responseBodyPropDesc.getValue(action);
 			}
 			if (transactionId != null)
 				response.setHeader(transactionIdName, transactionId);
 
 			String responseXml = XStreamFactory.getInstance().toXML(responseDto);
 			response.getWriter().write(responseXml);
 		} else {
 			ret = invocation.proceed();
 		}
 
 		return ret;
 	}
 
 }
