 /**
  *   ResolverFilter
  *   Copyright(c) 2011 Sergio Gabriel Teves
  * 
  *   This file is part of UrlResolver.
  *
  *   UrlResolver is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   UrlResolver is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with UrlResolver. If not, see <http://www.gnu.org/licenses/>.
  */
 package ar.sgt.resolver.filter;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ar.sgt.resolver.config.ResolverConfig;
 import ar.sgt.resolver.config.RuleConstant;
 import ar.sgt.resolver.exception.HttpError;
 import ar.sgt.resolver.exception.ReverseException;
 import ar.sgt.resolver.exception.RuleNotFoundException;
 import ar.sgt.resolver.listener.ContextLoader;
 import ar.sgt.resolver.processor.PermanentRedirectProcessor;
 import ar.sgt.resolver.processor.Processor;
 import ar.sgt.resolver.processor.ProcessorContext;
 import ar.sgt.resolver.processor.ResolverContext;
 import ar.sgt.resolver.rule.Rule;
 import ar.sgt.resolver.utils.UrlReverse;
 
 public class ResolverFilter implements Filter {
 
 	private static final Logger log = LoggerFactory.getLogger(ResolverFilter.class);
 	
 	private FilterConfig filterConfig;
 	private ResolverConfig resolverConfig;
 	private boolean appendBackSlash;	
 	private Set<String> excludePath;
 	
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 		log.debug("Initializing filter");
 		this.resolverConfig = (ResolverConfig) filterConfig.getServletContext().getAttribute(ContextLoader.RESOLVER_CONFIG);
 		this.appendBackSlash = filterConfig.getInitParameter("append_backslash") != null ? Boolean.parseBoolean(filterConfig.getInitParameter("append_backslash")) : true;
 		this.filterConfig = filterConfig;
 		if (filterConfig.getInitParameter("exclude-path") != null) {
 			this.excludePath = new HashSet<String>(Arrays.asList(StringUtils.split(filterConfig.getInitParameter("exclude-path"),",")));
 		} else {
 			this.excludePath = null;
 		}
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		log.trace("Entering filter processing");
 		HttpServletRequest req = (HttpServletRequest) request;
 		HttpServletResponse resp = (HttpServletResponse) response;
 		String path = req.getRequestURI();
		if (!"/".equals(req.getContextPath())) {
 			path = StringUtils.removeStartIgnoreCase(path, req.getContextPath());
 		}
 		if (this.excludePath != null) {
 			String fp = StringUtils.left(path, path.indexOf("/",1));
 			if (this.excludePath.contains(fp)) {
 				log.trace("Skip path {}", path);
 				chain.doFilter(request, response);
 				return;
 			}
 		}
 		if (this.appendBackSlash) {
 			if (!path.endsWith("/")) path = path + "/";
 		}
 		log.debug("Resolve path: {}", path);
 		Rule rule = resolverConfig.findRule(path);
 		if (rule != null) {
 			log.debug("Found rule {} using processor {}", rule.getName() == null ? "Unnamed" : rule.getName(), rule.getProcessor());
 			if (rule.getName() != null) {
 				req.setAttribute(RuleConstant.CURRENT_RULE, rule.getName());	
 			}
 			ResolverContext context = new ResolverContext(filterConfig.getServletContext(), req, resp, rule.parseParams(), req.getMethod());
 			String redirect = null;
 			if (rule.getRedirect() != null) {
 				// check first if there is a named rule matching
 				if (rule.getProcessor().equals(PermanentRedirectProcessor.class.getName())) {
 					redirect = rule.getRedirect();
 				} else {
 					UrlReverse reverse = new UrlReverse(resolverConfig);
 					try {
 						redirect = req.getContextPath() + reverse.resolve(rule.getRedirect());
						log.debug("Using named rule {}", rule.getRedirect());
 					} catch (ReverseException e) {
 						log.error(e.getMessage());
 						redirect = rule.getRedirect();
 					} catch (RuleNotFoundException e) {
						log.debug("Rule with name {} not found. Simple url redirect", rule.getRedirect());
 						redirect = rule.getRedirect();
 					}
 				}
 			}
 			ProcessorContext processorContext = new ProcessorContext(rule, redirect);
 			Processor processor;
 			try {
 				processor = loadClass(rule.getProcessor());
 				processor.process(processorContext, context);
 			} catch (HttpError e) {
 				log.debug("Handling HTTP ERROR {}", e.getHttpErrorCode());
 				resp.sendError(e.getHttpErrorCode());
 			} catch (Exception e) {
 				log.error(e.getMessage());
 				throw new ServletException(e);
 			}
 		} else {
			log.debug("No matching rule found");
 			chain.doFilter(request, response);
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	private Processor loadClass(String clasz) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
 		Class impl = Class.forName(clasz);
 		return (Processor) impl.newInstance();
 	}
 	
 	@Override
 	public void destroy() {
 		log.debug("Filter destroyed");
 	}
 
 }
