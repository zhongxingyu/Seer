 package vphshare.driservice.aop;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import vphshare.driservice.exceptions.AppException;
 import vphshare.driservice.exceptions.ErrorInfo;
 
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.UniformInterfaceException;
 
 public class MetadataRegistryExceptionHandler implements MethodInterceptor {
 
     private static final Logger logger = LogManager.getLogger(MetadataRegistryExceptionHandler.class);
 
 	@Override
 	public Object invoke(MethodInvocation invocation) throws Throwable {
 		try {
 			return invocation.proceed();
 		
 		} catch (ClientHandlerException che) {
             AppException  ae = handleException(che);
            logger.error("MetadataRegistry invocation exception", ae);
             throw ae;
 
         } catch (UniformInterfaceException uie) {
             AppException  ae = handleException(uie);
            logger.error("MetadataRegistry invocation exception", ae);
             throw ae;
 		}
 	}
 
 	private AppException handleException(RuntimeException re) throws AppException {
 		AppException exception = new AppException();
 		
 		ErrorInfo info = exception.addInfo();
 		info.setErrorId("MetadataRegistry invocation error");
 		info.setContextId("MetadataRegistry invocation");
 		info.setCause(re);
 		
 		info.setErrorDescription("Error while invoking AIR registry service");
 		info.setUserErrorDescription("Unable to fullfill the request due to MetadataRegistry service error");
 		info.setErrorCorrection("Check whether AIR interface or URL changed, check the response");
 		
 		info.setErrorType(ErrorInfo.ERROR_TYPE_SERVICE);
 		info.setSeverity(ErrorInfo.SEVERITY_ERROR);
 		
 		return exception;
 	}
 
 }
