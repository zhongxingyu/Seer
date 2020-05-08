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
 package br.octahedron.figgo.modules.domain.controller;
 
 import static br.octahedron.figgo.modules.domain.controller.validation.DomainValidator.getDomainValidator;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import br.octahedron.cotopaxi.auth.AuthenticationRequired;
 import br.octahedron.cotopaxi.auth.AuthorizationRequired;
 import br.octahedron.cotopaxi.controller.Controller;
 import br.octahedron.cotopaxi.datastore.namespace.NamespaceRequired;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.figgo.OnlyForNamespaceControllerInterceptor.OnlyForNamespace;
 import br.octahedron.figgo.modules.Module;
 import br.octahedron.figgo.modules.domain.data.DomainConfiguration;
 import br.octahedron.figgo.modules.domain.manager.ConfigurationManager;
 
 /**
  * @author vitoravelino
  * 
  */
 @AuthenticationRequired
 @AuthorizationRequired
 @NamespaceRequired
 @OnlyForNamespace
 public class ConfigurationController extends Controller {
 
 	protected static final String BASE_DIR_TPL = "domain/";
 	private static final String EDIT_DOMAIN_TPL = BASE_DIR_TPL + "edit.vm";
 	private static final String MODULE_CONFIG_TPL = BASE_DIR_TPL + "module/config.vm";
 	private static final String ROOT_URL = "/";
 	private static final String BASE_URL = "/domain";
 	private static final String EDIT_DOMAIN_URL = BASE_URL + "/edit";
 
 	@Inject
 	private ConfigurationManager configurationManager;
 
 	public void setConfigurationManager(ConfigurationManager configurationManager) {
 		this.configurationManager = configurationManager;
 	}
 
 	/**
 	 * Get edit domain page
 	 */
 	public void getEditDomain() {
 		DomainConfiguration domainConfiguration = this.configurationManager.getDomainConfiguration();
 		this.out("name", domainConfiguration.getName());
 		this.out("url", domainConfiguration.getUrl());
 		this.out("maillist", domainConfiguration.getMailList());
		this.out("description", domainConfiguration.getDescription());
 		this.success(EDIT_DOMAIN_TPL);
 	}
 
 	/**
 	 * Process the edit domain form
 	 */
 	public void postEditDomain() {
 		if (getDomainValidator().isValid()) {
 			this.configurationManager.updateDomainConfiguration(this.in("name"), this.in("url"), this.in("maillist"), this.in("description"));
 			this.redirect(ROOT_URL);
 		} else {
 			this.echo();
 			this.invalid(EDIT_DOMAIN_TPL);
 		}
 	}
 
 	/**
 	 * Shows edit module form
 	 */
 	public void getModuleDomain() {
 		this.out("name", this.in("module"));
 		this.out("module", this.configurationManager.getModuleConfiguration(Module.valueOf(this.in("module").toUpperCase())));
 		this.success(MODULE_CONFIG_TPL);
 	}
 
 	/**
 	 * Process the edit module form
 	 */
 	public void postModuleDomain() {
 		// TODO validate
 		Module module = Module.valueOf(this.in("module").toUpperCase());
 		Map<String, String> properties = new HashMap<String, String>();
 		for (String key : this.input()) {
 			if (key.startsWith("__")) {
 				properties.put(key.substring(2), this.in(key));
 			}
 		}
 		this.configurationManager.setModuleProperties(module, properties);
 		this.redirect(EDIT_DOMAIN_URL);
 	}
 
 	public void postEnableModuleDomain() {
 		// TODO validate
 		this.configurationManager.enableModule(Module.valueOf(this.in("module").toUpperCase()));
 		this.jsonSuccess();
 	}
 
 	public void postDisableModuleDomain() {
 		// TODO validate
 		this.configurationManager.disableModule(this.in("module").toUpperCase());
 		this.jsonSuccess();
 	}
 }
