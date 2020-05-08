 package net.flatball.stub.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 
 public class StubFilter implements Filter {
 	private Set<String> paths = new HashSet<String>();
 	private String defaultPath = "/index.html";
 
 	public void destroy() {
 
 	}
 
 	public void doFilter(ServletRequest servletRequest,
 			ServletResponse servletResponse, FilterChain filterChain)
 			throws IOException, ServletException {
 		HttpServletRequest request = (HttpServletRequest) servletRequest;
 		String servletPath = request.getServletPath();
 		// System.out.println("SP is " + servletPath);
 		// System.out.println("PI is " + request.getPathInfo());
 		String[] parts = servletPath.split("/");
 		ServletRequest filterRequest = servletRequest;
 		// System.out.println("PART1 is " + parts[1]);
 		// System.out.println("REQ URI is " + request.getRequestURI());
		if ((parts.length > 0) && paths.contains(parts[1])) {
 			filterRequest = new HttpServletRequestWrapper(request) {
 				@Override
 				public String getRequestURI() {
 					return defaultPath;
 				}
 
 				@Override
 				public String getServletPath() {
 					return defaultPath;
 				}
 			};
 		}
 		filterChain.doFilter(filterRequest, servletResponse);
 	}
 
 	public void init(FilterConfig filterConfig) throws ServletException {
 		Properties config = new Properties(System.getProperties());
 		ServletContext context = filterConfig.getServletContext();
 		String configKey = context.getContextPath().substring(1);
 		String configResource = configKey + ".properties";
 		context.log("Trying config resource: " + configResource);
 		InputStream in = this.getClass().getResourceAsStream("/" + configResource);
 		if (in != null) {	
 			try {
 				try {
 					config.load(in);
 				} finally {
 					in.close();
 				}
 			} catch (IOException ioe) {
 				context.log("Error processing configuration", ioe);
 			}
 		} else {
 			context.log("no configuration properties found, only using System properties");
 		}
 		String value = config.getProperty("stub." + configKey + ".defaultPath");
 		if (value != null) {
 			defaultPath = value;
 		}
 		value = config.getProperty("stub." + configKey + ".paths");
 		if (value != null) {
 			String parts[] = value.split(",");
 			for (String part : parts) {
 				paths.add(part);
 				context.log("Mapping path '" + part + "' to '" + defaultPath + "'");
 			}
 		}
 	}
 }
