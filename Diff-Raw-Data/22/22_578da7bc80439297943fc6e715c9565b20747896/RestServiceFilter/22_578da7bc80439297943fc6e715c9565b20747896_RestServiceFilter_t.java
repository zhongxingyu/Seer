 package org.inigma.shared.webapp;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
import org.inigma.shared.tools.CollectionsUtil;

 /**
  * <p>This servlet filter caches the request data and allows its reading via reader/stream an unlimited number of times.
  * Warning, this does mean that the request data coming in is being cached and thus consuming a larger then normal
  * amount of resources but this should be trivial for RESTful web service calls. It may however be an issue if such
  * things as a file upload happen via the same RESTful path so do use with caution. It is not reliable to check the
  * upload size prior to reading the bytes and thus once caching has started it must be cached and so there is not a
  * safe manner in which to not cache the data intelligently.</p>
  * <p>As an emergency safety measure, passing in a <pre>X-CACHE-IGNORE</pre> as a header will cause the caching to be
  * ignored.</p>
  *
  * @author <a href="mailto:sejal@inigma.org">Sejal Patel</a>
  */
 public class RestServiceFilter implements Filter {
     @Override
     public void destroy() {
         // Do nothing
     }
 
     @Override
     public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws IOException, ServletException {
        req.setAttribute(RestService.ERRORS, CollectionsUtil.appendOnlyList());
        req.setAttribute(RestService.REQUEST_AS_STRING, "");

         HttpServletRequest request = (HttpServletRequest) req;
         if (request.getHeader("X-CACHE-IGNORE") != null) { // emergency fallback in case of file upload or something.
             fc.doFilter(req, resp);
             return;
         }
 
         ServletRequestBodyWrapper wrapper = new ServletRequestBodyWrapper(request);
         wrapper.setAttribute(RestService.REQUEST_AS_STRING, wrapper.getBody().trim());
         fc.doFilter(wrapper, resp);
     }
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
     }
 }
