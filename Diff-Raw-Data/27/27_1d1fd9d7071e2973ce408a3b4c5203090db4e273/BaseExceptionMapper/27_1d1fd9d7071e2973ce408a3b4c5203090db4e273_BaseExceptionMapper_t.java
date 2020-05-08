 package org.jboss.pressgang.ccms.server.rest.v1.mapper;
 
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.ExceptionMapper;
 import java.lang.reflect.Method;
import java.util.ArrayList;
 
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.jaxrsinterfaces.RESTInterfaceV1;
 import org.jboss.pressgang.ccms.server.rest.v1.RESTv1;
 import org.jboss.pressgang.ccms.utils.common.VersionUtilities;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
 import org.jboss.resteasy.spi.interception.AcceptedByMethod;
 
 public abstract class BaseExceptionMapper<T extends Throwable> implements ExceptionMapper<T>, AcceptedByMethod {
    @Context HttpServletResponse servletResponse;
 
     protected Response buildPlainTextResponse(final Response.Status status, final T exception) {
         return buildPlainTextResponse(status.getStatusCode(), exception);
     }
 
     protected Response buildPlainTextResponse(final int status, final T exception) {
        // Copy any headers over from the original response as resteasy creates a new response for errors.
        final Headers<Object> headers = new Headers<Object>();
        for (final String headerName : servletResponse.getHeaderNames()) {
            headers.put(headerName, new ArrayList<Object>(servletResponse.getHeaders(headerName)));
        }

        // Add the additional required headers
        headers.putSingle("Content-Type", MediaType.TEXT_PLAIN);
        headers.putSingle(RESTv1Constants.X_PRESSGANG_VERSION_HEADER, VersionUtilities.getAPIVersion(RESTInterfaceV1.class));

        // Create the response
        return new ServerResponse(exception.getMessage(), status, headers);
     }
 
     @Override
     public boolean accept(Class declaring, Method method) {
         // Only use this interceptor for v1 endpoints.
         return RESTv1.class.equals(declaring);
     }
 }
