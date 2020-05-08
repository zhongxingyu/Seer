 package com.xone.service.security;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.access.SecurityMetadataSource;
 import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
 import org.springframework.security.access.intercept.InterceptorStatusToken;
 import org.springframework.security.web.FilterInvocation;
 import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
 
 public class MySecurityFilter extends AbstractSecurityInterceptor implements
 		Filter {
 
 	// 与applicationContext-security.xml里的myFilter的属性securityMetadataSource对应，
 	// 其他的两个组件，已经在AbstractSecurityInterceptor定义
 	private FilterInvocationSecurityMetadataSource securityMetadataSource;
 
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		FilterInvocation fi = new FilterInvocation(request, response, chain);
 		try {
 			invoke(fi);
 		} catch (AccessDeniedException e) {
 			HttpServletResponse r = (HttpServletResponse) response;
 //			r.setStatus(HttpServletResponse.SC_FORBIDDEN);
 			HttpServletRequest req = (HttpServletRequest) request;
			// fix bug, in web, redirect loop
			r.sendRedirect(req.getContextPath() + "/login.jsp");
 		}
 
 	}
 
 	private void invoke(FilterInvocation fi) throws IOException,
 			ServletException {
 		// object为FilterInvocation对象
 		// super.beforeInvocation(fi);源码
 		// 1.获取请求资源的权限
 		// 执行Collection<ConfigAttribute> attributes =
 		// SecurityMetadataSource.getAttributes(object);
 		// 2.是否拥有权限
 		// this.accessDecisionManager.decide(authenticated, object, attributes);
 		InterceptorStatusToken token = super.beforeInvocation(fi);
 		// 核心的InterceptorStatusToken token = super.beforeInvocation(fi);
 		// 会调用我们定义的accessDecisionManager:decide(Object
 		// object)和securityMetadataSource:getAttributes(Object object)方法。
 		try {
 			fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
 		} finally {
 			super.afterInvocation(token, null);
 		}
 	}
 
 	@Override
 	public void destroy() {
 
 	}
 
 	@Override
 	public Class<?> getSecureObjectClass() {
 		// 下面的MyAccessDecisionManager的supports方面必须放回true,否则会提醒类型错误
 		return FilterInvocation.class;
 	}
 
 	@Override
 	public SecurityMetadataSource obtainSecurityMetadataSource() {
 		return this.securityMetadataSource;
 	}
 
 	public FilterInvocationSecurityMetadataSource getSecurityMetadataSource() {
 		return securityMetadataSource;
 	}
 
 	public void setSecurityMetadataSource(
 			FilterInvocationSecurityMetadataSource securityMetadataSource) {
 		this.securityMetadataSource = securityMetadataSource;
 	}
 
 }
