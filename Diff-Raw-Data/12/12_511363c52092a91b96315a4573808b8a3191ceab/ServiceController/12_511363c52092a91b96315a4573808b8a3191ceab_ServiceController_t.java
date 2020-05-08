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
 package br.octahedron.figgo.modules.service.controller;
 
 import static br.octahedron.cotopaxi.controller.Converter.Builder.bigDecimalNumber;
 import static br.octahedron.cotopaxi.controller.Converter.Builder.safeString;
 import br.octahedron.cotopaxi.auth.AuthenticationRequired;
 import br.octahedron.cotopaxi.auth.AuthorizationRequired;
 import br.octahedron.cotopaxi.controller.Controller;
 import br.octahedron.cotopaxi.datastore.namespace.NamespaceRequired;
 import br.octahedron.cotopaxi.i18n.ControllerI18nHelper;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.cotopaxi.validation.Validator;
 import br.octahedron.figgo.FiggoException;
 import br.octahedron.figgo.modules.service.controller.validation.ServiceValidators;
 import br.octahedron.figgo.modules.service.data.Service;
 import br.octahedron.figgo.modules.service.data.ServiceContract.ServiceContractStatus;
 import br.octahedron.figgo.modules.service.manager.InexistentServiceProviderException;
 import br.octahedron.figgo.modules.service.manager.OnlyServiceProviderException;
 import br.octahedron.figgo.modules.service.manager.ServiceContractNotFound;
 import br.octahedron.figgo.modules.service.manager.ServiceManager;
 import br.octahedron.figgo.modules.service.manager.ServiceNotFoundException;
 
 /**
  * @author Vítor Avelino
  * 
  */
 @AuthenticationRequired
 @NamespaceRequired
 public class ServiceController extends Controller {
 
 	private static final String BASE_DIR_TPL = "services/";
 	private static final String NEW_SERVICE_TPL = BASE_DIR_TPL + "new.vm";
 	private static final String LIST_SERVICE_TPL = BASE_DIR_TPL + "list.vm";
 	private static final String SHOW_SERVICE_TPL = BASE_DIR_TPL + "show.vm";
 	private static final String EDIT_SERVICE_TPL = BASE_DIR_TPL + "edit.vm";
 	private static final String USER_SERVICES_TPL = BASE_DIR_TPL + "user.vm";
 	private static final String CONTRACT_DIR_TPL = BASE_DIR_TPL + "contract/";
 	private static final String EDIT_CONTRACT_TPL = CONTRACT_DIR_TPL + "edit.vm";
 	private static final String LIST_CONTRACTS_TPL = CONTRACT_DIR_TPL + "list.vm";
 
 	private static final String BASE_URL = "/services";
 	private static final String SHOW_CONTRACTS_URL = BASE_URL + "/contracts";
 
 	private ControllerI18nHelper i18n = new ControllerI18nHelper(this);
 
 	@Inject
 	private ServiceManager servicesManager;
 
 	public void setServiceManager(ServiceManager serviceManager) {
 		this.servicesManager = serviceManager;
 	}
 
 	@AuthorizationRequired
 	public void getListServices() {
 		this.out("services", this.servicesManager.getServices());
 		this.out("categories", this.servicesManager.getServiceCategories());
 		this.success(LIST_SERVICE_TPL);
 	}
 
 	@AuthorizationRequired
 	public void getShowService() {
 		try {
 			Service service = this.servicesManager.getService(this.in("id"));
 			this.out("service", service);
 			this.success(SHOW_SERVICE_TPL);
 		} catch (ServiceNotFoundException e) {
 			this.notFound();
 		}
 	}
 
 	@AuthorizationRequired
 	public void getNewService() {
 		this.success(NEW_SERVICE_TPL);
 	}
 
 	@AuthorizationRequired
 	public void postNewService() {
 		Validator validator = ServiceValidators.getServiceValidator();
 		if (validator.isValid()) {
 			this.servicesManager.createService(this.in("name", safeString()), this.in("amount", bigDecimalNumber()),
 					this.in("category", safeString()), this.in("description", safeString()));
 			this.redirect(BASE_URL);
 		} else {
 			this.echo();
 			this.invalid(NEW_SERVICE_TPL);
 		}
 	}
 
 	@AuthorizationRequired
 	public void getEditService() {
 		try {
 			Service service = this.servicesManager.getService(this.in("id"));
 			this.out("id", service.getId());
 			this.out("name", service.getName());
 			this.out("amount", service.getAmount());
 			this.out("category", service.getCategory());
 			this.out("description", service.getDescription());
 			this.success(EDIT_SERVICE_TPL);
 		} catch (ServiceNotFoundException e) {
 			this.notFound();
 		}
 	}
 
 	@AuthorizationRequired
 	public void postEditService() {
 		Validator validator = ServiceValidators.getServiceValidator();
 		if (validator.isValid()) {
 			try {
 				this.servicesManager.updateService(this.in("id"), this.in("name", safeString()), this.in("amount", bigDecimalNumber()),
 						this.in("category", safeString()), this.in("description", safeString()));
 				this.redirect(BASE_URL);
 			} catch (ServiceNotFoundException e) {
 				this.notFound();
 			}
 		} else {
 			this.echo();
 			this.invalid(EDIT_SERVICE_TPL);
 		}
 	}
 
 	@AuthorizationRequired
 	public void postAddProvider() {
 		try {
 			Service service = this.servicesManager.getService(this.in("id"));
 			service.addProvider(this.currentUser());
 			this.out("userId", this.currentUser());
 			this.out("serviceId", this.in("id"));
 			this.jsonSuccess();
 		} catch (ServiceNotFoundException e) {
 			this.notFound();
 		}
 	}
 
 	@AuthorizationRequired
 	public void postRemoveProvider() {
 		try {
 			this.out("service", this.servicesManager.removeProvider(this.in("id"), this.currentUser()));
 			this.jsonSuccess();
 		} catch (ServiceNotFoundException e) {
 			this.notFound();
 		}
 	}
 
 	@AuthorizationRequired
 	public void postRemoveService() {
 		try {
 			this.servicesManager.removeService(this.in("id"));
			this.redirect(BASE_URL);
 		} catch (ServiceNotFoundException e) {
 			this.notFound();
 		}
 	}
 
 	public void getUserServices() {
 		this.out("services", this.servicesManager.getUserServices(this.currentUser()));
 		this.success(USER_SERVICES_TPL);
 	}
 
 	public void getShowContracts() {
 		this.out("providerContracts", this.servicesManager.getProviderContracts(this.currentUser()));
 		this.out("contractorContracts", this.servicesManager.getContractorContracts(this.currentUser()));
 		this.success(LIST_CONTRACTS_TPL);
 	}
 
 	public void postRequestContract() {
 		try {
 			this.servicesManager.requestContract(this.in("id"), this.currentUser(), this.in("provider"));
 			this.jsonSuccess();
 		} catch (InexistentServiceProviderException e) {
 			this.out("exception", i18n.get(this.locales(), e.getMessage()));
 			this.jsonInvalid();
 		} catch (ServiceNotFoundException e) {
 			this.notFound();
 		}
 	}
 
 	public void getShowHistory() {
 		this.out("contracts", this.servicesManager.getContractsHistory(this.currentUser()));
 		this.success(LIST_CONTRACTS_TPL);
 	}
 
 	public void getEditContract() {
 		try {
 			this.out("contract", this.servicesManager.getServiceContract(this.in("id")));
 			this.success(EDIT_CONTRACT_TPL);
 		} catch (ServiceContractNotFound e) {
 			this.notFound();
 		}
 	}
 
	public void postEditContract() {
 		Validator existentContractStatusValidator = ServiceValidators.getExistentContractStatusValidator();
 		if (existentContractStatusValidator.isValid()) {
 			try {
 				this.servicesManager.updateContractStatus(this.in("id"), ServiceContractStatus.valueOf(this.in("status")), this.currentUser());
 				this.redirect(SHOW_CONTRACTS_URL);
 			} catch (OnlyServiceProviderException e) {
 				this.out("warning", i18n.get(this.locales(), e.getMessage()));
 				this.jsonInvalid();
 			} catch (ServiceContractNotFound e) {
 				this.notFound();
 			}
 		} else {
 			this.jsonInvalid();
 		}
 	}
 
 	public void postPayContract() {
 		try {
 			this.servicesManager.makePayment(this.in("id"), this.currentUser());
 			this.jsonSuccess();
 		} catch (ServiceContractNotFound e) {
 			this.notFound();
 		} catch (FiggoException e) {
 			this.out("exception", i18n.get(this.locales(), e.getMessage()));
 			this.jsonInvalid();
 		}
 	}
 
 	public void getServicesByCategory() {
 		String category = this.in("category", safeString());
 		if (this.servicesManager.existsServiceCategory(category)) {
 			this.out("services", this.servicesManager.getServicesByCategory(category));
 			this.out("categories", this.servicesManager.getServiceCategories());
 			this.out("currentCategory", category);
 			this.success(LIST_SERVICE_TPL);
 		} else {
 			this.notFound();
 		}
 	}
 }
