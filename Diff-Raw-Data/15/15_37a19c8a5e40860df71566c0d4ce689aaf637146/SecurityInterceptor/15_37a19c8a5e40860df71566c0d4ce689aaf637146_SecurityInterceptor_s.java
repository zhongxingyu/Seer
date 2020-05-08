 package id.co.bippo.security;
 
 import javax.inject.Inject;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.Interceptor;
 import javax.interceptor.InvocationContext;
 
 import org.apache.shiro.mgt.SecurityManager;
 import org.apache.shiro.subject.Subject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Secured @Interceptor
 public class SecurityInterceptor {
 
	String entityName;
 	@Inject
 	Subject subject;
 	@Inject
 	SecurityManager securityManager;
 	Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);
 	
 //	public SecurityInterceptor() {
 //		logger.info("Getting Subject from Shiro...");
 //		SecurityManager securityManager = SecurityUtils.getSecurityManager();
 //		subject = SecurityUtils.getSubject();
 //		logger.info("Security Subject is: {}", subject);		
 //	}
 	
 //	@PostConstruct
 //	public void init() {
 //		logger.info("Security Subject is: {}", subject);
 //	}
 	
 	@AroundInvoke
 	public Object interceptGet(InvocationContext ctx) throws Exception {
 		logger.info("Securing {} {}", new Object[] { ctx.getMethod(), ctx.getParameters() });
 		logger.debug("Principal is: {}", subject.getPrincipal());
 
 		final Class<? extends Object> runtimeClass = ctx.getTarget().getClass();
 		logger.debug("Runtime extended classes: {}", runtimeClass.getClasses());
 		logger.debug("Runtime implemented interfaces: {}", runtimeClass.getInterfaces());
 		logger.debug("Runtime annotations ({}): {}", runtimeClass.getAnnotations().length, runtimeClass.getAnnotations());
 		
 		final Class<?> declaringClass = ctx.getMethod().getDeclaringClass();
 		logger.debug("Declaring class: {}", declaringClass);
 		logger.debug("Declaring extended classes: {}", declaringClass.getClasses());
 		logger.debug("Declaring annotations ({}): {}", declaringClass.getAnnotations().length, declaringClass.getAnnotations());
 		
 		String entityName;
 		try {
 			NamedResource namedResource = runtimeClass.getAnnotation(NamedResource.class);
 			entityName = namedResource.value();
 			logger.debug("Got @NamedResource={}", entityName);
 		} catch (NullPointerException e) {
 			entityName = declaringClass.getSimpleName().toLowerCase(); // TODO: should be lowerFirst camelCase
 			logger.warn("@NamedResource not annotated, inferred from declaring classname: {}", entityName);
 		}
 		
 		String action = "admin";
 		if (ctx.getMethod().getName().matches("get[A-Z].*"))
 			action = "view";
 		if (ctx.getMethod().getName().matches("set[A-Z].*"))
 			action = "edit";
 		String permission = String.format("%s:%s", action, entityName);
 		logger.info("Checking permission '{}' for user '{}'", permission, subject.getPrincipal());
 		try {
 			subject.checkPermission(permission);
 		} catch (Exception e) {
 			logger.error("Access denied - {}: {}", e.getClass().getName(), e.getMessage());
 			throw e;
 		}
 		return ctx.proceed();
 	}
 }
