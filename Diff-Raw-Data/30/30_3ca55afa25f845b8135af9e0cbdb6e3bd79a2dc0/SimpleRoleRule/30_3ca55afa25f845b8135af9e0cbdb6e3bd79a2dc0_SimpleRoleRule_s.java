 package br.com.caelum.vraptor.vaas.configurations;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 
 import br.com.caelum.vraptor.vaas.Rule;
 
 @RequestScoped
 public class SimpleRoleRule implements Rule {
 	
 	@Inject
 	private HttpServletRequest request;
 	@Inject
 	private RolesByUriConfiguration config;
 
 	@Override
 	public boolean isAuthorized() {
 		Map<String, List<String>> mappings = config.getRolesByUri();
 		List<String> roles = mappings.get(request.getRequestURI());
		
 		boolean valid = true;
 		for (String role : roles) {
 			valid = valid && request.isUserInRole(role);
 		}
 		
 		return valid;
 	}
 
 }
