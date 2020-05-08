 package de.metacoder.blog.services;
 
 import org.apache.shiro.realm.Realm;
 import org.apache.shiro.web.mgt.WebSecurityManager;
 import org.apache.tapestry5.SymbolConstants;
 import org.apache.tapestry5.ioc.Configuration;
 import org.apache.tapestry5.ioc.MappedConfiguration;
 import org.apache.tapestry5.ioc.ServiceBinder;
 import org.apache.tapestry5.ioc.annotations.Contribute;
 import org.apache.tapestry5.services.HttpServletRequestFilter;
 import org.tynamo.security.SecuritySymbols;
 import org.tynamo.security.services.SecurityFilterChainFactory;
 import org.tynamo.security.services.impl.SecurityFilterChain;
 import org.tynamo.security.shiro.authz.SslFilter;
 
 import de.metacoder.blog.security.BlogRoles;
 
 /**
  * This module is automatically included as part of the Tapestry IoC Registry,
  * it's a good place to configure and extend Tapestry, or to place your own
  * service definitions.
  */
 public class AppModule {
 
 	@Contribute(WebSecurityManager.class)
 	public static void addRealms(final Configuration<Realm> configuration,
 			final Realm configuredRealm) {
 		configuration.add(configuredRealm);
 	}
 	
 	@Contribute(HttpServletRequestFilter.class)
 	public static void setupSecurity(final Configuration<SecurityFilterChain> configuration, final SecurityFilterChainFactory factory, final WebSecurityManager securityManager) {
 		final SslFilter ssl = factory.ssl();
 		
 		if("development".equalsIgnoreCase(System.getProperty("tapestry.execution-mode"))){
 			ssl.setPort(8443); // TODO move to DevelopmentModule?
 		}
 		configuration.add(factory.createChain("/admin/**")
 				.add(factory.roles(), BlogRoles.ADMIN) // only admin role users are allowed to see the /admin/** area
 				.add(factory.authc()) // MUST be authenticated to avoid security flaws due to the remember me services
 				.add(ssl) //  require SSL
 				.build()); 
 		
 		configuration.add(factory.createChain("/login").add(ssl).build()); // SSL for the login page.
 		
 	}
 
     public void contributeIgnoredPathsFilter(Configuration<String> conf) {
         conf.add("/services/.*");
     }
 
 
     public static void bind(final ServiceBinder binder) {
 		// binder.bind(MyServiceInterface.class, MyServiceImpl.class);
 
 		// Make bind() calls on the binder object to define most IoC services.
 		// Use service builder methods (example below) when the implementation
 		// is provided inline, or requires more initialization than simply
 		// invoking the constructor.
 	}
 
 	public static void contributeFactoryDefaults(
 			final MappedConfiguration<String, Object> configuration) {
 		// The application version number is incorprated into URLs for some
 		// assets. Web browsers will cache assets because of the far future
 		// expires header. If existing assets are changed, the version number
 		// should also change, to force the browser to download new versions.
 		// This overrides Tapesty's default (a random hexadecimal number), but
 		// may be further overriden by DevelopmentModule or QaModule.
 		final String version = AppModule.class.getPackage().getImplementationVersion();
 		configuration.override(SymbolConstants.APPLICATION_VERSION,	version != null ? version : "0.0.1-SNAPSHOT");
        configuration.override(SymbolConstants.MINIFICATION_ENABLED, false);

 	}
 
 	public static void contributeApplicationDefaults(
 			final MappedConfiguration<String, Object> configuration) {
 		// Contributions to ApplicationDefaults will override any contributions
 		// to FactoryDefaults (with the same key). Here we're restricting the
 		// supported locales to just "en" (English). As you add localised
 		// message catalogs and other assets, you can extend this list of
 		// locales (it's a comma separated series of locale names;
 		// the first locale name is the default when there's no reasonable
 		// match).
 		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en");
 		configuration.add(SymbolConstants.SECURE_ENABLED, false);
 
 		/* custom shiro config */
 		configuration.add(SecuritySymbols.LOGIN_URL, "/login");
 		configuration.add(SecuritySymbols.UNAUTHORIZED_URL, "/unauthorized");
 	}
 }
