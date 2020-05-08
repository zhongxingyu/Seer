 package org.vaadin.addons.springsecurityviewprovider;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.EnableAspectJAutoProxy;
 import org.springframework.context.annotation.EnableLoadTimeWeaving;
 import org.springframework.context.annotation.EnableLoadTimeWeaving.AspectJWeaving;
 import org.springframework.context.annotation.ImportResource;
 import org.springframework.context.annotation.Profile;
 import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
 import org.springframework.security.authentication.AnonymousAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.authority.AuthorityUtils;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.AnnotationConfigContextLoader;
 import org.vaadin.addons.springsecurityviewprovider.SpringSecurityViewProvider;
 import org.vaadin.addons.springsecurityviewprovider.views.CacheableTestView;
 import org.vaadin.addons.springsecurityviewprovider.views.UncacheableTestView;
 
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewProvider;
 
 /**
  * @author Michael J. Simons, 2013-04-03
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(loader=AnnotationConfigContextLoader.class)
 @ActiveProfiles({"test"})
 public class SpringSecurityViewProviderTest {
 	@Configuration
 	@Profile("test")
 	@ComponentScan("org.vaadin.addons.springsecurityviewprovider.views")
 	@ImportResource({
 		"classpath:/org/vaadin/addons/springsecurityviewprovider/config/security.xml"
 	})	
 	@EnableLoadTimeWeaving(aspectjWeaving=AspectJWeaving.ENABLED)
 	@EnableSpringConfigured
 	@EnableAspectJAutoProxy
 	public static class TestConfiguration {	
 	}
 
 	final static Authentication anonymousAuthentication = new AnonymousAuthenticationToken("anonymousUser", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
 	
 	@Autowired
 	private ApplicationContext applicationContext;
 
 	@Test
 	public void shouldCacheCacheableViews() {		
 		final ViewProvider viewProvider = SpringSecurityViewProvider.createViewProvider(anonymousAuthentication, true);
 		final View v1 = viewProvider.getView(CacheableTestView.VIEW_NAME);
 		final View v2 = viewProvider.getView(CacheableTestView.VIEW_NAME);		
 		Assert.assertEquals(v1, v2);
 	}
 
 	@Test
 	public void shouldNotCacheUncacheableViews() {
 		final ViewProvider viewProvider = SpringSecurityViewProvider.createViewProvider(anonymousAuthentication, true);
 		final View v1 = viewProvider.getView(UncacheableTestView.VIEW_NAME);
 		final View v2 = viewProvider.getView(UncacheableTestView.VIEW_NAME);		
 		Assert.assertNotEquals(v1, v2);
 	}
 }
