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
 package br.octahedron.figgo.modules.services.controller;
 
 import java.math.BigDecimal;
 
 import br.octahedron.cotopaxi.auth.AuthenticationRequired;
 import br.octahedron.cotopaxi.controller.Controller;
import br.octahedron.cotopaxi.datastore.NamespaceRequired;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.cotopaxi.validation.Validator;
 import br.octahedron.figgo.modules.services.controller.validation.ServiceValidators;
 import br.octahedron.figgo.modules.services.data.Service;
 import br.octahedron.figgo.modules.services.data.ServiceContract.ServiceContractStatus;
 import br.octahedron.figgo.modules.services.manager.ServiceManager;
 
 /**
  * @author Vítor Avelino
  *
  */
 @AuthenticationRequired
 @NamespaceRequired
 public class ServicesController extends Controller {
 
 	private static final String BASE_DIR_TPL = "services/";
 	private static final String NEW_SERVICE_TPL = BASE_DIR_TPL + "new.vm";
 	private static final String LIST_SERVICE_TPL = BASE_DIR_TPL + "list.vm";
 	private static final String SHOW_SERVICE_TPL = BASE_DIR_TPL + "show.vm";
 	private static final String EDIT_SERVICE_TPL = BASE_DIR_TPL + "edit.vm";
 	private static final String CONTRACT_DIR_TPL = BASE_DIR_TPL + "contract/";
 	private static final String EDIT_CONTRACT_TPL = CONTRACT_DIR_TPL + "edit.vm";
 	private static final String LIST_CONTRACTS_TPL = CONTRACT_DIR_TPL + "list.vm";
 	private static final String BASE_URL = "/services";
 	private static final String SHOW_CONTRACTS_URL = BASE_URL + "/contracts";
 	
 	@Inject
 	private ServiceManager servicesManager;
 	
 	public void setServiceManager(ServiceManager serviceManager) {
 		this.servicesManager = serviceManager;
 	}
 	
 	public void getListServices() {
 		out("myServices", this.servicesManager.getUserServices(currentUser()));
 		out("services", this.servicesManager.getServices());
 		success(LIST_SERVICE_TPL);
 	}
 	
 	public void getShowService() {
 		out("service", this.servicesManager.getService(in("name")));
 		success(SHOW_SERVICE_TPL);
 	}
 	
 	public void getNewService() {
 		success(NEW_SERVICE_TPL);
 	}
 	
 	public void postCreateService() {
 		Validator inexistentValidator = ServiceValidators.getInexistentValidator();
 		Validator valueValidator = ServiceValidators.getValueValidator();
 		if (inexistentValidator.isValid() && valueValidator.isValid()) {
 			this.servicesManager.createService(in("name"), new BigDecimal(in("value")), in("category"), in("description"));
 			redirect(BASE_URL);
 		} else {
 			out("name", in("name"));
 			out("value", in("value"));
 			out("category", in("category"));
 			out("description", in("description"));
 			invalid(NEW_SERVICE_TPL);
 		}
 	}
 	
 	public void getEditService() {
 		Service service = this.servicesManager.getService(in("name"));
 		out("name", service.getName());
 		out("value", service.getAmount());
 		out("category", service.getCategory());
 		out("description", service.getDescription());
 		success(EDIT_SERVICE_TPL);
 	}
 	
 	public void postUpdateService() {
 		Validator inexistentValidator = ServiceValidators.getInexistentValidator();
 		Validator valueValidator = ServiceValidators.getValueValidator();
 		if (inexistentValidator.isValid() && valueValidator.isValid()) {
 			this.servicesManager.updateService(in("name"), new BigDecimal(in("value")), in("category"), in("description"));
 			redirect(BASE_URL);
 		} else {
 			out("name", in("name"));
 			out("value", in("value"));
 			out("category", in("category"));
 			out("description", in("description"));
 			invalid(EDIT_SERVICE_TPL);
 		}
 	}
 	
 	public void postAddProvider() {
 		Validator inexistentValidator = ServiceValidators.getInexistentValidator();
 		if (inexistentValidator.isValid()) {
 			out("service", this.servicesManager.addProvider(in("name"), currentUser()));
 			jsonSuccess();
 		} else {
 			jsonInvalid();
 		}
 	}
 	
 	public void postRemoveProvider() {
 		Validator inexistentValidator = ServiceValidators.getInexistentValidator();
 		if (inexistentValidator.isValid()) {
 			out("service", this.servicesManager.removeProvider(in("name"), currentUser()));
 			jsonSuccess();
 		} else {
 			jsonInvalid();
 		}
 	}
 	
 	// authorized only for services domain admin
 	public void postRemoveService() {
 		Validator inexistentValidator = ServiceValidators.getInexistentValidator();
 		if (inexistentValidator.isValid()) {
 			this.servicesManager.removeService(in("name"));
 			jsonSuccess();
 		} else {
 			jsonInvalid();
 		}
 	}
 	
 	public void postRequestContract() {
 		Validator inexistentValidator = ServiceValidators.getInexistentValidator();
 		Validator providerValidator = ServiceValidators.getProviderValidator();
 		if (inexistentValidator.isValid() && providerValidator.isValid()) {
 			this.servicesManager.requestContract(in("name"), currentUser(), in("provider"));
 			jsonSuccess();
 		} else {
 			jsonInvalid();
 		}
 	}
 	
 	public void getShowContracts() {
 		out("contracts", this.servicesManager.getContracts(currentUser()));
 		success(LIST_CONTRACTS_TPL);
 	}
 	
 	public void getShowHistory() {
 		out("contracts", this.servicesManager.getContractsHistory(currentUser()));
 		success(LIST_CONTRACTS_TPL);
 	}
 	
 	public void getEditContract() {
 		Validator inexistentValidator = ServiceValidators.getInexistentValidator();
 		if (inexistentValidator.isValid()) {
 			this.servicesManager.updateContractStatus(new Long(in("id")), ServiceContractStatus.valueOf(in("status")));
 			success(EDIT_CONTRACT_TPL);
 		} else {
 			invalid(EDIT_CONTRACT_TPL);
 		}
 	}
 	
 	public void postUpdateContract() {
 		Validator inexistentValidator = ServiceValidators.getInexistentValidator();
 		Validator providerValidator = ServiceValidators.getProviderValidator();
 		if (inexistentValidator.isValid() && providerValidator.isValid()) {
 			this.servicesManager.updateContractStatus(new Long(in("id")), ServiceContractStatus.valueOf(in("status")));
 			redirect(SHOW_CONTRACTS_URL);
 		} else {
 			jsonInvalid();
 		}
 	}
 	
 }
