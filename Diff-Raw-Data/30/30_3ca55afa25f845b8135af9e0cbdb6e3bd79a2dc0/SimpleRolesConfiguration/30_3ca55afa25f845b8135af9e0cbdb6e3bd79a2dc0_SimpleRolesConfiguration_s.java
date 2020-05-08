 package br.com.caelum.vraptor.vaas.configurations;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.annotation.Priority;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.inject.Alternative;
 import javax.inject.Inject;
 
 import br.com.caelum.vraptor.vaas.AccessConfiguration;
 import br.com.caelum.vraptor.vaas.Rule;
 
 @AccessConfiguration
 @RequestScoped
 @Alternative
 @Priority(value=1000)
public class SimpleRolesConfiguration {
 	
 	@Inject
 	private SimpleRoleRule rule;
 	
 	public List<Rule> getRules(String uri) {
 		return Arrays.<Rule>asList(rule);
 	}
 
 }
