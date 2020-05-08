 
 package nz.net.ultraq.web.lesscss;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.annotation.WebFilter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Filter to process .css requests which are actually LESS files, doing on the
  * server side what less.js normally does on the client side.
  * 
  * @author Emanuel Rabina
  */
 @WebFilter(
 	filterName = "LessCSSFilter",
 	urlPatterns = "*.css"	
 )
 public class LessCSSFilter implements Filter {
 
 	private static final HashMap<String,LessCSSFile> lesscache = new HashMap<>();
 
 	private LessCSSProcessor processor;
 
 	/**
 	 * Does nothing.
 	 */
 	@Override
 	public void destroy() {
 	}
 
 	/**
	 * Process a request for a LESS file, processing the result and
 	 * returning the compiled CSS file.
 	 * 
 	 * @param req
 	 * @param res
 	 * @param chain
 	 * @throws IOException
 	 * @throws ServletException
 	 */
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
 		throws IOException, ServletException {
 
 		HttpServletRequest request = (HttpServletRequest)req;
 		HttpServletResponse response = (HttpServletResponse)res;
 
 		// Capture the LESS file
 		LessCSSResponseWrapper lessresponse = new LessCSSResponseWrapper(response);
 		chain.doFilter(request, lessresponse);
 
 		// Do nothing if file not modified
 		if (lessresponse.getStatus() == HttpServletResponse.SC_NOT_MODIFIED) {
 			return;
 		}
 
 		// Use URL as the cache key
 		StringBuffer urlbuilder = request.getRequestURL();
 		if (request.getQueryString() != null) {
 			urlbuilder.append(request.getQueryString());
 		}
 		String url = urlbuilder.toString();
 
 		File lessfile = new File(request.getServletContext().getRealPath(request.getServletPath()));
 
 		// Create a new less processing result
 		LessCSSFile result;
 		if (!lesscache.containsKey(url) || lesscache.get(url).sourcesModified()) {
 			result = lesscache.containsKey(url) ? lesscache.get(url) : new LessCSSFile(lessfile, lessresponse);
 			lesscache.put(url, processor.process(result));
 		}
 		// Use the one already in cache
 		else {
 			result = lesscache.get(url);
 		}
 
 		// Write result to response
 		response.setContentLength(result.getProcessedContent().getBytes().length);
 		response.getOutputStream().write(result.getProcessedContent().getBytes());
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	/**
 	 * Initialze the LessCSS processor.
 	 * 
 	 * @param filterConfig
 	 */
 	@Override
 	public void init(FilterConfig filterConfig) {
 
 		processor = new LessCSSProcessor();
 	}
 }
