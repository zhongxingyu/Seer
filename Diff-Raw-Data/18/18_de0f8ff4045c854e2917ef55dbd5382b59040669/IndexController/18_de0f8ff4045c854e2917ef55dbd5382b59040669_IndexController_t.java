 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo;
 
 import static br.octahedron.cotopaxi.CotopaxiProperty.getProperty;
 import br.octahedron.cotopaxi.auth.AuthenticationRequired;
 import br.octahedron.cotopaxi.controller.Controller;
 import br.octahedron.cotopaxi.datastore.namespace.NamespaceManager;
 import br.octahedron.cotopaxi.datastore.namespace.NamespaceRequired;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.figgo.modules.authorization.manager.AuthorizationManager;
 import br.octahedron.figgo.modules.configuration.manager.ConfigurationManager;
 
 /**
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class IndexController extends Controller {
 
 	private static final String INDEX_TPL = "index.vm";
	private static final String DOMAIN_INDEX_TPL = "domain/index.vm";
	private static final String DOMAIN_PUBLIC_INDEX_TPL = "domain/public_index.vm";
 
 	@Inject
 	private ConfigurationManager configurationManager;
 	@Inject
 	private AuthorizationManager authorizationManager;
 	@Inject
 	private NamespaceManager namespaceManager;
 
 	public void setNamespaceManager(NamespaceManager namespaceManager) {
 		this.namespaceManager = namespaceManager;
 	}
 
 	public void setConfigurationManager(ConfigurationManager configurationManager) {
 		this.configurationManager = configurationManager;
 	}
 
 	public void setAuthorizationManager(AuthorizationManager authorizationManager) {
 		this.authorizationManager = authorizationManager;
 	}
 
 	/**
 	 * Shows initial land page or redirect user to dashboard/domain page
 	 */
 	@NamespaceRequired
 	public void getIndex() {
 		String username = this.currentUser();
 		boolean userLogged = (username != null);
 
 		if (fullRequestedUrl().equalsIgnoreCase(getProperty("APPLICATION_BASE_URL"))) {
 			// user accessing the raw url (www), redirects it to dash board
 			if (!userLogged) {
 				// user not logged, show the initial land page
 				success(INDEX_TPL);
 			} else {
 				redirect("/dashboard");
 			}
 		} else {
 			// user is accessing an specific domain page
 			out("domain", this.configurationManager.getDomainConfiguration());
 
 			boolean userExists = false;
 			try {
 				if (userLogged) {
 					namespaceManager.changeToGlobalNamespace();
 					userExists = this.authorizationManager.getUserDomains(username).contains(subDomain());
 				}
 			} finally {
 				namespaceManager.changeToPreviousNamespace();
 			}
 
 			if (userExists) {
				success(DOMAIN_INDEX_TPL);
 			} else {
				success(DOMAIN_PUBLIC_INDEX_TPL);
 			}
 		}
 	}
 
 	/**
 	 * Just to force user to login. If user already logged, redirect to main page
 	 */
 	@AuthenticationRequired
 	public void getLogin() {
 		redirect("/");
 	}
 }
