 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil.misc;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.Filter;
 
 import org.apache.shiro.config.Ini;
 import org.apache.shiro.util.CollectionUtils;
 import org.apache.shiro.util.StringUtils;
 import org.apache.shiro.web.config.IniFilterChainResolverFactory;
 import org.apache.shiro.web.filter.AccessControlFilter;
 import org.apache.shiro.web.filter.authc.AuthenticationFilter;
 import org.apache.shiro.web.filter.authc.LogoutFilter;
 import org.apache.shiro.web.filter.authz.AuthorizationFilter;
 import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
 import org.apache.shiro.web.filter.mgt.FilterChainResolver;
 import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
 import org.apache.shiro.web.mgt.WebSecurityManager;
 import org.apache.shiro.mgt.SecurityManager;
 import org.apache.shiro.web.servlet.AbstractShiroFilter;
 
 import zcu.xutil.Objutil;
 import zcu.xutil.cfg.CFG;
 import zcu.xutil.cfg.Provider;
 
 public class ShiroFilterFactory {
 	private SecurityManager securityManager;
 	private Map<String, Provider> filters = new HashMap<String, Provider>();
 	private String definitions, loginUrl, successUrl, unauthorizedUrl, logoutUrl;
 
 	public AbstractShiroFilter getShiroFilter() {
 		DefaultFilterChainManager manager = new DefaultFilterChainManager();
 		for (Filter filter : manager.getFilters().values())
 			applyGlobalPropertiesIfNecessary(filter);
 		for (Map.Entry<String, Provider> entry : filters.entrySet()) {
 			Filter filter = (Filter) entry.getValue().instance();
 			applyGlobalPropertiesIfNecessary(filter);
 			manager.addFilter(entry.getKey(), filter);
 		}
 		Ini ini = new Ini();
 		ini.load(definitions);
 		Ini.Section section = ini.getSection(IniFilterChainResolverFactory.URLS);
 		if (CollectionUtils.isEmpty(section))
 			section = ini.getSection(Ini.DEFAULT_SECTION_NAME);
 		for (Map.Entry<String, String> entry : section.entrySet())
 			manager.createChain(entry.getKey(), entry.getValue());
 		PathMatchingFilterChainResolver chainResolver = new PathMatchingFilterChainResolver();
 		chainResolver.setFilterChainManager(manager);
 		return new XSFilter((WebSecurityManager) securityManager, chainResolver);
 	}
 
 	public ShiroPlugin getShiroPlugin() {
 		ShiroPlugin ret = new ShiroPlugin();
 		if (StringUtils.hasText(unauthorizedUrl))
 			ret.setUnauthorizedUrl(unauthorizedUrl);
 		return ret;
 	}
 
 	public void setSecurityManager(SecurityManager securitymanager) {
 		this.securityManager = securitymanager;
 	}
 
 	public void setLoginUrl(String loginURL) {
 		this.loginUrl = loginURL;
 	}
 
 	public void setLogoutUrl(String logoutURL) {
 		this.logoutUrl = logoutURL;
 	}
 
 	public void setSuccessUrl(String successURL) {
 		this.successUrl = successURL;
 	}
 
 	public void setUnauthorizedUrl(String unauthorizedURL) {
 		this.unauthorizedUrl = unauthorizedURL;
 	}
 
 	public void addFilter(String name, String filterClass) {
 		addFilter(name, CFG.typ(Objutil.loadclass(filterClass)));
 	}
 
 	public void addFilter(String name, Provider filter) {
 		Class<?> type = filter.getType();
 		Objutil.validate(Filter.class.isAssignableFrom(type), "{} is not a Filter.", type);
 		filters.put(name, filter);
 	}
 
 	public void setFilterChainDefinitions(String definition) {
 		definitions = definition;
 
 	}
 
 	private void applyGlobalPropertiesIfNecessary(Filter filter) {
 		if (StringUtils.hasText(logoutUrl) && filter instanceof LogoutFilter) {
 			LogoutFilter logout = (LogoutFilter) filter;
 			if (LogoutFilter.DEFAULT_REDIRECT_URL.equals(logout.getRedirectUrl()))
 				logout.setRedirectUrl(logoutUrl);
 		}
 		if (StringUtils.hasText(loginUrl) && (filter instanceof AccessControlFilter)) {
 			AccessControlFilter access = (AccessControlFilter) filter;
 			if (AccessControlFilter.DEFAULT_LOGIN_URL.equals(access.getLoginUrl()))
 				access.setLoginUrl(loginUrl);
 		}
 		if (StringUtils.hasText(successUrl) && (filter instanceof AuthenticationFilter)) {
 			AuthenticationFilter authen = ((AuthenticationFilter) filter);
			if (AuthenticationFilter.DEFAULT_SUCCESS_URL.equals(authen.getSuccessUrl()))
 				authen.setSuccessUrl(successUrl);
 		}
 		if (StringUtils.hasText(unauthorizedUrl) && (filter instanceof AuthorizationFilter)) {
 			AuthorizationFilter author = ((AuthorizationFilter) filter);
 			if (!StringUtils.hasText(author.getUnauthorizedUrl()))
 				author.setUnauthorizedUrl(unauthorizedUrl);
 		}
 	}
 
 	private static final class XSFilter extends AbstractShiroFilter {
 		XSFilter(WebSecurityManager webSecurityManager, FilterChainResolver resolver) {
 			setSecurityManager(Objutil.notNull(webSecurityManager, "WebSecurityManager property cannot be null."));
 			if (resolver != null)
 				setFilterChainResolver(resolver);
 		}
 	}
 }
