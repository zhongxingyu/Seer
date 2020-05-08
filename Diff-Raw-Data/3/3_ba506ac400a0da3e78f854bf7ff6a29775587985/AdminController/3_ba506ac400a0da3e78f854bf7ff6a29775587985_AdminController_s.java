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
 package br.octahedron.figgo.modules.admin.controller;
 
 import static br.octahedron.figgo.Utils.getDomainURL;
 import br.octahedron.cotopaxi.auth.AuthenticationRequired;
 import br.octahedron.cotopaxi.auth.AuthorizationRequired;
 import br.octahedron.cotopaxi.controller.Controller;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.cotopaxi.validation.Validator;
 import br.octahedron.figgo.modules.admin.controller.validation.AdminValidators;
 import br.octahedron.figgo.modules.admin.data.ApplicationConfiguration;
 import br.octahedron.figgo.modules.admin.manager.AdminManager;
 
 import static br.octahedron.figgo.modules.admin.controller.validation.AdminValidators.*;
 /**
  * @author Vítor Avelino
  */
 @AuthorizationRequired
 @AuthenticationRequired
 public class AdminController extends Controller {
 
 	private static final String BASE_DIR_TPL = "admin/";
 	private static final String INDEX_TPL = BASE_DIR_TPL + "index.vm";
 	private static final String CONFIG_APP_TPL = BASE_DIR_TPL + "config.vm";
 	private static final String NEW_DOMAIN_TPL = BASE_DIR_TPL + "domain/new.vm";
 	private static final String NEW_DOMAIN_URL = "/admin/domain/new";
 	
 	@Inject
 	private AdminManager adminManager;
 	
 	public void setAdminManager(AdminManager adminManager) {
 		this.adminManager = adminManager;
 	}
 	
 	
 	/**
 	 * Shows admin index page.
 	 */
 	public void getAdminIndex() {
 		success(INDEX_TPL);
 	}
 	
 	/**
 	 *  Shows app config form
 	 */
 	public void getAppConfig() {
 		if (adminManager.hasApplicationConfiguration()) {
 			ApplicationConfiguration applicationConfiguration = this.adminManager.getApplicationConfiguration();
 			out("accessKey", applicationConfiguration.getRoute53AccessKeyID());
 			out("keySecret", applicationConfiguration.getRoute53AccessKeySecret());
 			out("zone", applicationConfiguration.getRoute53ZoneID());
 		}
 		success(CONFIG_APP_TPL);
 	}
 	
 	/**
 	 * Process app config form 
 	 */
 	public void postAppConfig() {
 		if ( getConfigValidator().isValid() ) {
 			this.adminManager.configureApplication(in("accessKey"), in("keySecret"), in("zone"));
 			redirect(NEW_DOMAIN_URL);
 		} else {
 			echo();
 			invalid(CONFIG_APP_TPL);
 		}
 	}
 	
 	/**
 	 * Shows new domain form
 	 */
 	public void getCreateDomain() {
 		success(NEW_DOMAIN_TPL);
 	}
 	
 	/**
 	 * Process new domain form
 	 */
 	public void postCreateDomain() {
 		Validator validator = AdminValidators.getDomainValidator();
 		if (validator.isValid()) {
 			String domain = in("name");
 			this.adminManager.createDomain(domain, in("userId"));
 			redirect(getDomainURL(domain));
 		} else {
 			echo();
 			invalid(NEW_DOMAIN_TPL);
 		}
 	}
 }
