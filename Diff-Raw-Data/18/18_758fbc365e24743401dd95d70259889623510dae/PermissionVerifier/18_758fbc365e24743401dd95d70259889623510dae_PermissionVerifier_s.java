 package br.com.caelum.vraptor.vaas.authorization;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.event.Event;
 import javax.enterprise.inject.Instance;
 import javax.enterprise.inject.spi.CDI;
 import javax.enterprise.util.AnnotationLiteral;
 import javax.inject.Inject;
 
 import br.com.caelum.vraptor.vaas.AccessConfiguration;
 import br.com.caelum.vraptor.vaas.RolesConfigMethod;
 import br.com.caelum.vraptor.vaas.Rule;
 import br.com.caelum.vraptor.vaas.event.AuthorizationFailedEvent;
 import br.com.caelum.vraptor.vaas.event.RefreshUserEvent;
 
 
 public class PermissionVerifier {
 	@Inject
 	private Event<AuthorizationFailedEvent> authorizationFailedEvent;
 
 	@Inject
 	private Event<RefreshUserEvent> refreshUserEvent;
 
 	private RolesConfigMethod rulesConfigMethod;
 
 	private Object accessConfiguration;
 
 	
 	@SuppressWarnings({ "rawtypes", "serial" })
 	@PostConstruct
 	public void config() {
 		Instance possibleConfigurations = CDI.current().select(new AnnotationLiteral<AccessConfiguration>() {});
 		if (possibleConfigurations.isAmbiguous()) {
 			throw new RuntimeException(
 					"You should use just one AccessConfiguration class");
 		}
 		this.accessConfiguration = possibleConfigurations.get();
 		this.rulesConfigMethod = new RolesConfigMethod(this.accessConfiguration);
 	}
 
 	
 	public boolean verifyAccessFor(String uri){
 		List<Rule> rulesNotAllowed = getNotAllowedRules(rulesConfigMethod.rulesFor(uri));
 		if (!rulesNotAllowed.isEmpty()) {
 			authorizationFailedEvent.fire(new AuthorizationFailedEvent(rulesNotAllowed));
 			return false;
 		}
 		refreshUserEvent.fire(new RefreshUserEvent());
 		return true;
 
 	}
 
 	private List<Rule> getNotAllowedRules(List<Rule> roles) {
 		List<Rule> rulesNotAllowed = new ArrayList<Rule>();
 		for (Rule rule : roles) {
 			if(!rule.isAuthorized()){
 				rulesNotAllowed.add(rule);
 			}
 		}
 		return rulesNotAllowed;
 	}
 
 	
 }
