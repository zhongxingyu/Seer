 package org.CommunityService.Services;
 
 import javax.servlet.ServletContext;
 
 import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
 import org.ocpsoft.rewrite.config.Configuration;
 import org.ocpsoft.rewrite.config.ConfigurationBuilder;
 import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
 import org.ocpsoft.rewrite.servlet.config.rule.Join;
 
 @RewriteConfiguration
 public class FacesConfigurationProvider extends HttpConfigurationProvider{
 	
 	@Override
 	public Configuration getConfiguration(final ServletContext context) {
 		return ConfigurationBuilder.begin()
 				.addRule(Join.path("/").to("/Home.xhtml"))
				.addRule(Join.path("/event/{eventId}").to("/Event.xhtml"))
				.addRule(Join.path("/organization/{orgId}").to("/Organization.xhtml"))
				.addRule(Join.path("/group/{groupId}").to("/Group.xhtml"))
 				.addRule(Join.path("/login").to("/Login.xhtml"))
 				.addRule(Join.path("/search").to("/Search.xhtml"))
				.addRule(Join.path("/register").to("/Register.xhtml"));
 	}
 	
 	@Override
 	public int priority() {
 		// Using 10 so that if we want more configuration providers, we can add above or below this one
 		return 10;
 	}
 
 }
