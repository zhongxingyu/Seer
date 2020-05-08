 package id.co.bippo.security;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Named;
 import javax.inject.Singleton;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.config.IniSecurityManagerFactory;
 import org.apache.shiro.mgt.SecurityManager;
 import org.apache.shiro.subject.Subject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Singleton
 public class SecurityFacade {
 
 	Logger logger = LoggerFactory.getLogger(SecurityFacade.class);
 	private SecurityManager securityManager;
 	
 	@PostConstruct
 	public void init() {
 		final String iniFile = "classpath:shiro.ini";
 		logger.info("Initializing Shiro INI SecurityManager using " + iniFile);
 		securityManager = new IniSecurityManagerFactory(iniFile).getInstance();
 		SecurityUtils.setSecurityManager(securityManager);
 	}
 	
	@Produces @Named("securityManager")
 	public SecurityManager getSecurityManager() {
 		return securityManager;
 	}
 	
	@Produces
 	public Subject getSubject() {
 		return SecurityUtils.getSubject();
 	}
}
