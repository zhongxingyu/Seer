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
 package br.octahedron.figgo.modules.service.manager;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 
 import br.octahedron.cotopaxi.eventbus.EventBus;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.figgo.modules.service.data.Service;
 import br.octahedron.figgo.modules.service.data.ServiceCategory;
 import br.octahedron.figgo.modules.service.data.ServiceCategoryDAO;
 import br.octahedron.figgo.modules.service.data.ServiceContract;
 import br.octahedron.figgo.modules.service.data.ServiceContract.ServiceContractStatus;
 import br.octahedron.figgo.modules.service.data.ServiceContractDAO;
 import br.octahedron.figgo.modules.service.data.ServiceDAO;
 import br.octahedron.figgo.modules.user.data.User;
 
 /**
  * This entity is responsible by manage services
  * 
  * @author Erick Moreno
  * @author Vítor Avelino
  */
 public class ServiceManager {
 
 	private final ServiceDAO serviceDAO = new ServiceDAO();
 	private final ServiceCategoryDAO serviceCategoryDAO = new ServiceCategoryDAO();
 	private final ServiceContractDAO serviceContractDAO = new ServiceContractDAO();
 	
 	@Inject
 	private EventBus eventBus;
 	
 	public void setEventBus(EventBus eventBus) {
 		this.eventBus = eventBus;
 	}
 	/**
 	 * Creates and saves a new service
 	 * 
 	 * @return The {@link Service} created
 	 */
 	public Service createService(String name, BigDecimal value, String category, String description) {
 		Service service = new Service(name, value, category, description);
 		this.serviceDAO.save(service);
 		this.eventBus.publish(new ServiceCreatedEvent(service.getCategoryId(), service.getCategory()));
 		return service;
 	}
 
 	/**
 	 * Updates an service
 	 * @throws ServiceNotFoundException 
 	 */
 	public Service updateService(String serviceId, String name, BigDecimal value, String category, String description) throws ServiceNotFoundException {
 		Service service = this.getService(serviceId);
 		String oldCategoryId = service.getCategoryId();
 		service.setName(name);
 		service.setAmount(value);
 		service.setDescription(description);
 		service.setCategory(category);
 		this.eventBus.publish(new ServiceUpdatedEvent(service, oldCategoryId));
 		// This object will be updated to the DB by JDO persistence manager
 		return service;
 	}
 
 	/**
 	 * Gets the {@link Service} with the given id
 	 * 
 	 * @return the {@link Service} with the given id, if exists, or <code>null</code>, if doesn't
 	 *         exists a user with the given id.
 	 * @throws ServiceNotFoundException 
 	 */
 	public Service getService(String serviceId) throws ServiceNotFoundException {
 		Service service = this.serviceDAO.get(serviceId);
		if (service != null) {
 			return service;
 		} else {
 			throw new ServiceNotFoundException();
 		}
 	}
 
 	/**
 	 * Checks if exists a {@link Service} with the given id.
 	 * 
 	 * @return <code>true</code> if exists, <code>false</code> otherwise.
 	 */
 	public boolean existsService(String id) {
 		return this.serviceDAO.exists(id);
 	}
 
 	/**
 	 * Adds a {@link User} that provides this service
 	 * 
 	 * @param serviceName
 	 * @param userId
 	 * @return 
 	 */
 	public Service addProvider(String serviceId, String userId) {
 		Service service = this.serviceDAO.get(serviceId);
 		service.addProvider(userId);
 		return service;
 	}
 
 	/**
 	 * Removes a {@link User} from the providers list
 	 * 
 	 * @param serviceName
 	 * @param userId
 	 * @return 
 	 * @throws ServiceNotFoundException 
 	 */
 	public Service removeProvider(String serviceId, String userId) throws ServiceNotFoundException {
 		Service service = this.getService(serviceId);
 		service.removeProvider(userId);
 		return service;
 	}
 
 	/**
 	 * This method retrieves all {@link User} that provides this service
 	 * 
 	 * @param serviceName
 	 * @return An collection with userId
 	 */
 	public Collection<String> getServiceProviders(String serviceName) {
 		Service serv = this.serviceDAO.getServiceByName(serviceName);
 		return serv.getProviders();
 	}
 
 	/**
 	 * This method returns all {@link Service} that has the specified {@link User} (represented by
 	 * his userId) as a provider.
 	 * 
 	 * @param userId
 	 *            The key that identifies a {@link User}
 	 * @return A collection with all services that has the passed user as provider.
 	 */
 	public Collection<Service> getUserServices(String userId) {
 		return this.serviceDAO.getUserServices(userId);
 	}
 	
 	/**
 	 * Creates a new contract of a service between a contractor and a provider.
 	 * 
 	 * @param serviceName
 	 * @param contractor
 	 * @param provider
 	 * @throws InexistentServiceProviderException 
 	 * @throws ServiceNotFoundException 
 	 */
 	public void requestContract(String serviceId, String contractor, String provider) throws InexistentServiceProviderException, ServiceNotFoundException {
 		Service service = this.getService(serviceId);
 		if (service.hasProvider(provider)) {
 			ServiceContract serviceContract = new ServiceContract(service, contractor, provider);
 			this.serviceContractDAO.save(serviceContract);
 			this.eventBus.publish(new ServiceContractRequestedEvent(serviceContract));
 		} else {
 			throw new InexistentServiceProviderException(); 
 		}
 	}
 	
 	/**
 	 * Provider updates the service contract status.
 	 * 
 	 * @param contractId
 	 * @param status
 	 * @param providerId
 	 * @throws OnlyServiceProviderException 
 	 * @throws ServiceContractNotFound 
 	 */
 	public void updateContractStatus(String contractId, ServiceContractStatus status, String providerId) throws OnlyServiceProviderException, ServiceContractNotFound {
 		ServiceContract serviceContract = this.getServiceContract(contractId);
 		if (serviceContract.getProvider().equals(providerId)) {
 			serviceContract.setStatus(status);
 			this.eventBus.publish(new ServiceContractUpdatedEvent(serviceContract));
 		} else {
 			throw new OnlyServiceProviderException();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param contractId
 	 * @throws UncompletedServiceContractException
 	 * @throws OnlyServiceContractorException 
 	 * @throws ServiceContractNotFound 
 	 */
 	public void makePayment(String contractId, String contractorId) throws UncompletedServiceContractException, OnlyServiceContractorException, ServiceContractNotFound {
 		ServiceContract serviceContract = this.getServiceContract(contractId);
 		if (serviceContract.getStatus() != ServiceContractStatus.COMPLETED) {
 			throw new UncompletedServiceContractException();
 		} else if (!serviceContract.getContractor().equals(contractId)) {
 			throw new OnlyServiceContractorException();
 		}
 
 		serviceContract.markAsPaid();
 		this.eventBus.publish(new ServiceContractPaidEvent(serviceContract));
 	}
 	
 	/**
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public Collection<ServiceContract> getContractsHistory(String userId) {
 		return this.serviceContractDAO.getHistory(userId);
 	}
 	
 	/**
 	 * @return all services
 	 */
 	public Collection<Service> getServices() {
 		return this.serviceDAO.getAll();
 	}
 	
 	/**
 	 * @param serviceName
 	 * @return
 	 * @throws ServiceNotFoundException 
 	 */
 	public void removeService(String serviceId) throws ServiceNotFoundException {
 		Service service = this.getService(serviceId);
 		this.serviceDAO.delete(service);
 		this.eventBus.publish(new ServiceRemovedEvent(service));
 	}
 	
 	/**
 	 * @param currentUser
 	 * @return 
 	 */
 	public Collection<ServiceContract> getContracts(String userId) {
 		return this.serviceContractDAO.getContracts(userId);
 	}
 	
 	/**
 	 * @param currentUser
 	 * @return 
 	 */
 	public Collection<ServiceContract> getProviderContracts(String userId) {
 		return this.serviceContractDAO.getProviderContracts(userId);
 	}
 	
 	/**
 	 * @param currentUser
 	 * @return 
 	 */
 	public Collection<ServiceContract> getContractorContracts(String userId) {
 		return this.serviceContractDAO.getContractorContracts(userId);
 	}
 	
 	/**
 	 * @param in
 	 * @return
 	 * @throws ServiceContractNotFound 
 	 */
 	public ServiceContract getServiceContract(String contractId) throws ServiceContractNotFound {
 		ServiceContract serviceContract = this.serviceContractDAO.get(contractId);
 		if (serviceContract != null) {
 			return serviceContract;
 		} else {
 			throw new ServiceContractNotFound();
 		}
 	}
 	
 	/**
 	 * Checks if exists a contract with the given id
 	 * @param contractId the contract's id
 	 * @return <code>true</code> if exists, <code>false</code> otherwise.
 	 */
 	public boolean existsServiceContract(String contractId) {
 		return this.serviceContractDAO.exists(contractId);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Collection<ServiceCategory> getServiceCategories() {
 		return this.serviceCategoryDAO.getAll();
 	}
 	
 	/**
 	 * @param categoryId
 	 * @param categoryName
 	 */
 	public void createServiceCategory(String categoryId, String categoryName) {
 		if (!serviceCategoryDAO.exists(categoryId)) {
 			ServiceCategory serviceCategory = new ServiceCategory(categoryId, categoryName);
 			this.serviceCategoryDAO.save(serviceCategory);
 		}
 	}
 	
 	/**
 	 * @param category
 	 */
 	public void cleanCategory(String category) {
 		Collection<Service> services = this.serviceDAO.getServicesByCategory(category);
 		if (services.isEmpty()) {
 			this.serviceCategoryDAO.delete(category);
 		}
 	}
 	
 	/**
 	 * @param in
 	 * @return
 	 */
 	public Collection<Service> getServicesByCategory(String category) {
 		return this.serviceDAO.getServicesByCategory(category);
 	}
 	
 	/**
 	 * @param category
 	 * @return
 	 */
 	public boolean existsServiceCategory(String category) {
 		return this.serviceCategoryDAO.exists(category);
 	}
 	
 }
