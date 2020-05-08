 package cz.cvut.fit.mi_mpr_dip.admission.endpoint.mapper;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.lang3.ObjectUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.stereotype.Component;
 
 import cz.cvut.fit.mi_mpr_dip.admission.exception.BusinessException;
 
 @RooJavaBean
 @Component
 public class WebApplicationExceptionMapper extends AdmissionExceptionMapper<WebApplicationException> implements
 		ExceptionMapper<WebApplicationException> {
 
 	@Autowired
 	private JaxbExceptionMapper jaxbExceptionMapper;
 
 	@Override
 	public Response toResponse(WebApplicationException exception) {
 		JAXBException cause = findJAXBException(exception);
 		if (cause != null) {
 			return getJaxbExceptionMapper().toResponse(cause);
 		}
 		return super.toResponse(exception);
 	}
 
 	private JAXBException findJAXBException(WebApplicationException exception) {
		return (JAXBException) getCause(exception);
 	}
 
 	private Throwable getCause(Throwable cause) {
 		if (cause.getCause() == null || cause.getCause() instanceof JAXBException) {
 			return cause.getCause();
 		}
 		return getCause(cause.getCause());
 	}
 
 	@Override
 	protected void logErrorResponse(WebApplicationException exception) {
 		getLoggingService().logErrorResponse(
 				new BusinessException(new Integer(exception.getResponse().getStatus()), exception));
 	}
 
 	@Override
 	protected ResponseBuilder getResponseBuilder(WebApplicationException exception) {
 		return super.getResponseBuilder(exception).type(MediaType.APPLICATION_XML);
 	}
 
 	@Override
 	protected String createMessage(WebApplicationException exception) {
 		return ObjectUtils.toString(exception.getResponse().getEntity());
 	}
 
 	@Override
 	protected Integer getResponseCode(WebApplicationException exception) {
 		return exception.getResponse().getStatus();
 	}
 
 }
