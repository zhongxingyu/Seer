 package org.jboss.pressgang.ccms.server.rest.v1.mapper;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.ExceptionMapper;
 import java.lang.reflect.Method;
 
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.jaxrsinterfaces.RESTInterfaceV1;
 import org.jboss.pressgang.ccms.server.rest.v1.RESTv1;
 import org.jboss.pressgang.ccms.server.utils.Constants;
 import org.jboss.pressgang.ccms.utils.common.VersionUtilities;
 import org.jboss.resteasy.spi.interception.AcceptedByMethod;
 
 public abstract class BaseExceptionMapper<T extends Throwable> implements ExceptionMapper<T>, AcceptedByMethod {
 
     protected Response buildPlainTextResponse(final Response.Status status, final T exception) {
         return buildPlainTextResponse(status.getStatusCode(), exception);
     }
 
     protected Response buildPlainTextResponse(final int status, final T exception) {
         return javax.ws.rs.core.Response.status(status)
                 .entity(exception.getMessage() + "\n")
                 .header("Content-Type", MediaType.TEXT_PLAIN)
                 .header(RESTv1Constants.X_PRESSGANG_VERSION_HEADER, VersionUtilities.getAPIVersion(RESTInterfaceV1.class))
                 .header(RESTv1Constants.ACCESS_CONTROL_EXPOSE_HEADERS, RESTv1Constants.X_PRESSGANG_VERSION_HEADER)
                 .build();
     }
 
     @Override
     public boolean accept(Class declaring, Method method) {
         // Only use this interceptor for v1 endpoints.
         return RESTv1.class.equals(declaring);
     }
 }
