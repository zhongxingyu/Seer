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
 package br.octahedron.straight.modules.services.manager;
 
 import java.util.Collection;
 
 import br.octahedron.straight.modules.services.data.Service;
 import br.octahedron.straight.modules.services.data.ServiceDAO;
 import br.octahedron.straight.modules.users.data.User;
 
 /**
  * This entity is responsible by manage services
  * 
  * @author Erick Moreno
  */
 public class ServiceManager {
 
 	private ServiceDAO dao = new ServiceDAO();
 
 	/**
 	 * Creates and saves a new service
 	 * 
 	 * @return The {@link Service} created
 	 */
 	public Service createService(String name, String value, String description) {
 		Service service = new Service(name, value, description);
 		this.dao.save(service);
 		return service;
 	}
 
 	/**
 	 * Updates an service
 	 */
	public Service updateService(String name, String value, String description) {
 		Service serv = this.dao.get(name);
 
 		serv.setValue(value);
 		serv.setDescription(description);
 		// This object will be updated to the DB by JDO persistence manager
		
		return serv;
 	}
 
 	/**
 	 * Gets the {@link Service} with the given id
 	 * 
 	 * @return the {@link Service} with the given id, if exists, or <code>null</code>, if doesn't
 	 *         exists a user with the given id.
 	 */
 	public Service getService(String name) {
 		return this.dao.get(name);
 	}
 
 	/**
 	 * Checks if exists a {@link Service} with the given id.
 	 * 
 	 * @return <code>true</code> if exists, <code>false</code> otherwise.
 	 */
 	public boolean existsService(String name) {
 		return this.dao.exists(name);
 	}
 
 	/**
 	 * Adds a {@link User} that provides this service
 	 * 
 	 * @param serviceName
 	 * @param userId
 	 */
 	public void addProvider(String serviceName, String userId) {
 		Service serv = this.dao.get(serviceName);
 		serv.addProvider(userId);
 	}
 
 	/**
 	 * Removes a {@link User} from the providers list
 	 * 
 	 * @param serviceName
 	 * @param userId
 	 */
 	public void removeProvider(String serviceName, String userId) {
 		Service serv = this.dao.get(serviceName);
 		serv.removeProvider(userId);
 	}
 
 	/**
 	 * This method retrieves all {@link User} that provides this service
 	 * 
 	 * @param serviceName
 	 * @return An collection with userId
 	 */
 	public Collection<String> getServiceProviders(String serviceName) {
 		Service serv = this.dao.get(serviceName);
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
 		return this.dao.getUserServices(userId);
 	}
 
 }
