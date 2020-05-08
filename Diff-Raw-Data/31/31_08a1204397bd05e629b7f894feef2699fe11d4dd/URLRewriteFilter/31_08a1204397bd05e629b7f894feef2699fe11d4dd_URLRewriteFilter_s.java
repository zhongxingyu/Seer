 package burst.reader.web.route;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.context.ApplicationContext;
 
 import burst.web.util.SpringLocator;
 import burst.web.util.WebUtil;
 
 public class URLRewriteFilter implements Filter {
 
 	@Override
 	public void destroy() { }
 	
 	private String getRewritedURL(Matcher m, String pattern, HttpServletRequest hsRequest) {
 		String res = pattern;
 		for (int i = 1, l = m.groupCount(); i <= l; ++i) {
 			res = res.replaceAll("\\$" + i, m.group(i));
 		}
 		return hsRequest.getContextPath() + res;
 	}
 	
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		
 		HttpServletRequest hsRequest = (HttpServletRequest)request;
         String userAgent = hsRequest.getHeader(WebUtil.HEAD_USERAGENT);
 
        if(userAgent.matches(userAgentFilter)) {
            for(Entry<String, String> entry : userAgentFilterUrlRewriteRules) {
 
                Pattern p = Pattern.compile(entry.getKey());
                Matcher m = p.matcher(WebUtil.getContextURL(hsRequest));
                if(m.matches()) {
                    RequestDispatcher dispatcher = request.getRequestDispatcher(getRewritedURL(m, entry.getValue(), hsRequest));
                    dispatcher.forward(request, response);
                    return;
                 }
             }
         }
 		
 		for(Entry<String, String> entry : rewriteRules) {
 			Pattern p = Pattern.compile(entry.getKey());
 			Matcher m = p.matcher(WebUtil.getContextURL(hsRequest));
 			if(m.matches()) {
 				RequestDispatcher dispatcher = request.getRequestDispatcher(getRewritedURL(m, entry.getValue(), hsRequest));
 				dispatcher.forward(request, response);
 				return;
 			}
 		}
 		
 		chain.doFilter(request, response);
 	}
 
     private String userAgentFilter;
     private Set<Entry<String, String>> userAgentFilterUrlRewriteRules;
 	private Set<Entry<String, String>> rewriteRules;
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void init(FilterConfig arg0) throws ServletException {
 		ApplicationContext ac = SpringLocator.getApplicationContext();
 		rewriteRules = ((Map<String, String>)ac.getBean("urlRewriteRules")).entrySet();
         userAgentFilterUrlRewriteRules = ((Map<String, String>)ac.getBean("userAgentFilterUrlRewriteRules")).entrySet();
         userAgentFilter = (String)ac.getBean("userAgentFilterHolder");
 	}
 	
 }
