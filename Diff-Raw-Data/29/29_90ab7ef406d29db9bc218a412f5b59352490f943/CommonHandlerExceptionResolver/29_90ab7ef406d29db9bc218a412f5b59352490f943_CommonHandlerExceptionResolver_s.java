 package my.commons.framework.springmvc.exception;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import my.commons.Constants;
 import my.commons.Result;
 import my.commons.exception.AppException;
 import my.commons.framework.springmvc.MessageUtils;
 import my.commons.framework.springmvc.annotation.ResponseMapping;
 import my.commons.framework.springmvc.annotation.ResponseMappings;
 import my.commons.lang.collections.Pair;
 import my.commons.web.util.ServletUtils;
 
 import org.springframework.beans.TypeMismatchException;
 import org.springframework.core.Ordered;
 import org.springframework.http.converter.HttpMessageNotReadableException;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.MethodArgumentNotValidException;
 import org.springframework.web.bind.MissingServletRequestParameterException;
 import org.springframework.web.bind.ServletRequestBindingException;
 import org.springframework.web.method.HandlerMethod;
 import org.springframework.web.multipart.support.MissingServletRequestPartException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
 
 /**
  * Enhanced default implementation of the {@link org.springframework.web.servlet.HandlerExceptionResolver
  * HandlerExceptionResolver} interface that resolves standard Spring exceptions
  * <br/>
  * 默认优先级最高
  * 
  * @author xiegang
  * @since 2013-5-2
  * 
  * @see org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver
  * @see AppException
  * @see TypeMismatchException
  * @see HttpMessageNotReadableException
  * @see MissingServletRequestPartException
  * @see MethodArgumentNotValidException
  * @see MissingServletRequestParameterException
  * @see ServletRequestBindingException
  * @see BindException
  */
 public class CommonHandlerExceptionResolver extends AbstractHandlerExceptionResolver {
 	private final static String CONTENTTYPE_JSON = "application/json";
 	
 	/** 编码 */
 	private String encoding = Constants.ENCODING;
 	
 	private int codeExceptionBase = 99000; // 99_000
 	private boolean sameCode = true;
	private int codeBindException = codeExceptionBase + 1; // BindException result code
	private int codeHttpMessageNotReadableException = codeExceptionBase + 2; // HttpMessageNotReadableException result code
	private int codeMissingServletRequestPartException = codeExceptionBase + 3; // MissingServletRequestPartException result code
	private int codeMissingServletRequestParameterException = codeExceptionBase + 4; // MissingServletRequestParameterException result code
	private int codeTypeMismatchException = codeExceptionBase + 5; // TypeMismatchException result code
	private int codeMethodArgumentNotValidException = codeExceptionBase + 6; // MethodArgumentNotValidException result code
 	
 	public CommonHandlerExceptionResolver() {
 		setOrder(Ordered.HIGHEST_PRECEDENCE);
 	}
 	
 	@Override
 	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
 		try {
 			if (ex instanceof AppException) {
 				// AppException
 				return handleApp((AppException) ex, request, response, handler);
 			} else if (ex instanceof TypeMismatchException) {
 				// 参数类型不匹配
 				return handleTypeMismatch((TypeMismatchException) ex, request, response, handler);
 			} else if (ex instanceof MethodArgumentNotValidException) {
 				// 参数校验失败
 				return handleMethodArgumentNotValidException((MethodArgumentNotValidException) ex, request, response, handler);
 			} else if (ex instanceof MissingServletRequestParameterException) {
 				// 缺少必须的参数
 				return handleMissingServletRequestParameter((MissingServletRequestParameterException) ex, request, response, handler);
 			} else if (ex instanceof HttpMessageNotReadableException) {
 				// 未知的请求体
 				return handleHttpMessageNotReadable((HttpMessageNotReadableException) ex, request, response, handler);
 			} else if (ex instanceof MissingServletRequestPartException) {
 				// 缺少部分请求内容
 				return handleMissingServletRequestPartException((MissingServletRequestPartException) ex, request, response, handler);
 			} else if (ex instanceof ServletRequestBindingException || ex instanceof BindException) {
 				// 请求不合法
 				return handleBindException((BindException) ex, request, response, handler);
 			}
 		}
 		catch (Exception handlerException) {
 			logger.warn("Handling of [" + ex.getClass().getName() + "] resulted in Exception", handlerException);
 		}
 		// for default processing
 		return null;
 	}
 	
 	/**
 	 * ajax to json<br/>
 	 * from to ResponseMapping ERROR
 	 * 
 	 * @param result
 	 * @param httpStatus
 	 * @param ex
 	 * @param request
 	 * @param response
 	 * @param handler
 	 * @return
 	 * @throws IOException
 	 */
 	private ModelAndView handleException(Result result, int httpStatus, Exception ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
 		if (ServletUtils.isAjaxRequest(request)) {
 			response.setStatus(httpStatus);
 			response.setContentType(CONTENTTYPE_JSON);
 			response.setCharacterEncoding(encoding);
 			
 			PrintWriter out = response.getWriter();
 			out.print(result.toJsonString()); // output Ajax String
 			if (logger.isDebugEnabled()) {
 				logger.debug(new StringBuffer("Catch a ").append(ex.getClass().getSimpleName()).append(" and write to response json errors = ").append(result.toJsonString()).toString());
 			}
 			out.flush();
 			return new ModelAndView();
 		} else {
 			Pair<String, String> view = resolveErrorViewName(handler);
 			if (view != null) {
 				if (ResponseMapping.REDIRECT.equalsIgnoreCase(view.getFirst())) { // type REDIRECT
 					if (logger.isDebugEnabled()) {
 						logger.debug(new StringBuffer("Catch a ").append(ex.getClass().getSimpleName()).append(" and redirect and write error to request = ").append(result.toJsonString()).toString());
 					}
 					ModelAndView mav = new ModelAndView("redirect:" + view.getSecond());
 					mav.addObject(Result.RET, result.getRet());
 					//不需要编码，RedirectView.class会用request.charactorEncoding()的编码去编码modelmap中的属性，然后放到重定向url后面
 					mav.addObject(Result.MSG, result.getMsg());
 					return mav;
 				} else { // type FORWARD
 					request.setAttribute(Result.RET, result.getRet());
 					request.setAttribute(Result.MSG, result.getMsg());
 					if (logger.isDebugEnabled()) {
 						logger.debug(new StringBuffer("Catch a ").append(ex.getClass().getSimpleName()).append(" and forward and write error to request = ").append(result.toJsonString()).toString());
 					}
 					return new ModelAndView(view.getSecond());
 				}
 				
 			}
 			// 不处理
 			return null;
 		}
 	}
 	
 	/**
 	 * 获取ResponseMapping name error
 	 * @param handler
 	 * @return <type, viewname> or null
 	 */
 	private Pair<String, String> resolveErrorViewName(Object handler) {
 		if (handler instanceof HandlerMethod) {
 			HandlerMethod hm = (HandlerMethod) handler;
 			ResponseMappings responseMappings = hm.getMethodAnnotation(ResponseMappings.class);
 			if (responseMappings != null) {
 				ResponseMapping[] rms = responseMappings.value();
 				if (rms != null && rms.length > 0) {
 					for (ResponseMapping rm : rms) {
 						if (ResponseMapping.ERROR.equalsIgnoreCase(rm.name())) {
 							return new Pair<String, String>(rm.type(), rm.value());
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	// handle area
 	/**
 	 * 500 {ex.code, message{ex.code}}
 	 * 
 	 * @param ex
 	 * @param request
 	 * @param response
 	 * @param handler
 	 * @return
 	 * @throws IOException
 	 */
 	private ModelAndView handleApp(AppException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
 		Result result = new Result(ex.getExCode(), MessageUtils.getMessage(request, "ex." + ex.getExCode()));
 		int httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR; // 500
 		return handleException(result, httpStatus, ex, request, response, handler);
 	}
 
 	private ModelAndView handleMissingServletRequestPartException(MissingServletRequestPartException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
 		int code = sameCode ? codeExceptionBase : codeMissingServletRequestPartException;
 		Result result = new Result(code, MessageUtils.getMessage(request, "ex." + code));
 		int httpStatus = HttpServletResponse.SC_BAD_REQUEST; // 400
 		return handleException(result, httpStatus, ex, request, response, handler);
 	}
 
 	private ModelAndView handleBindException(BindException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
 		int code = sameCode ? codeExceptionBase : codeBindException;
 		Result result = new Result(code, MessageUtils.getMessage(request, "ex." + code));
 		int httpStatus = HttpServletResponse.SC_BAD_REQUEST; // 400
 		return handleException(result, httpStatus, ex, request, response, handler);
 	}
 
 	private ModelAndView handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
 		int code = sameCode ? codeExceptionBase : codeMethodArgumentNotValidException;
 		Result result = new Result(code, MessageUtils.getMessage(request, "ex." + code));
 		int httpStatus = HttpServletResponse.SC_BAD_REQUEST; // 400
 		return handleException(result, httpStatus, ex, request, response, handler);
 	}
 
 	private ModelAndView handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
 		int code = sameCode ? codeExceptionBase : codeHttpMessageNotReadableException;
 		Result result = new Result(code, MessageUtils.getMessage(request, "ex." + code));
 		int httpStatus = HttpServletResponse.SC_BAD_REQUEST; // 400
 		return handleException(result, httpStatus, ex, request, response, handler);
 	}
 
 	private ModelAndView handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
 		int code = sameCode ? codeExceptionBase : codeTypeMismatchException;
 		Result result = new Result(code, MessageUtils.getMessage(request, "ex." + code));
 		int httpStatus = HttpServletResponse.SC_BAD_REQUEST; // 400
 		return handleException(result, httpStatus, ex, request, response, handler);
 	}
 	
 	private ModelAndView handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
 		int code = sameCode ? codeExceptionBase : codeMissingServletRequestParameterException;
 		Result result = new Result(code, MessageUtils.getMessage(request, "ex." + code));
 		int httpStatus = HttpServletResponse.SC_BAD_REQUEST; // 400
 		return handleException(result, httpStatus, ex, request, response, handler);
 	}
 
 	// set property
 	public void setEncoding(String encoding) {
 		this.encoding = encoding;
 	}
 	public void setCodeExceptionBase(int codeExceptionBase) {
 		this.codeExceptionBase = codeExceptionBase;
 	}
 	public void setSameCode(boolean sameCode) {
 		this.sameCode = sameCode;
 	}
 	public void setCodeTypeMismatchException(int codeTypeMismatchException) {
 		this.codeTypeMismatchException = codeTypeMismatchException;
 	}
 	public void setCodeHttpMessageNotReadableException(int codeHttpMessageNotReadableException) {
 		this.codeHttpMessageNotReadableException = codeHttpMessageNotReadableException;
 	}
 	public void setCodeMissingServletRequestPartException(int codeMissingServletRequestPartException) {
 		this.codeMissingServletRequestPartException = codeMissingServletRequestPartException;
 	}
 	public void setCodeMethodArgumentNotValidException(int codeMethodArgumentNotValidException) {
 		this.codeMethodArgumentNotValidException = codeMethodArgumentNotValidException;
 	}
 	public void setCodeMissingServletRequestParameterException(int codeMissingServletRequestParameterException) {
 		this.codeMissingServletRequestParameterException = codeMissingServletRequestParameterException;
 	}
 	public void setCodeBindException(int codeBindException) {
 		this.codeBindException = codeBindException;
 	}
 }
