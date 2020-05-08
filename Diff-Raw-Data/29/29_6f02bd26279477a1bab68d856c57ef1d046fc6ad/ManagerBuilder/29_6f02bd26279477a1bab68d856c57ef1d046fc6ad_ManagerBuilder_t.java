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
 package br.octahedron.straight.modules;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import br.octahedron.straight.modules.services.ServiceDecorator;
 import br.octahedron.straight.modules.services.ServicesIF;
 import br.octahedron.straight.modules.services.manager.ServiceManager;
 import br.octahedron.straight.modules.users.UsersDecorator;
 import br.octahedron.straight.modules.users.UsersIF;
 import br.octahedron.straight.modules.users.manager.UsersManager;
 
 /**
  * This Builder knows how to create managers for each module.
  * 
  * @author Erick Moreno
  *
  */
 public class ManagerBuilder {
 	
	private static enum ManagerTypeEnum {
		USERS, 
		BANK, 
		SERVICES, 
		AUTORIZATION, 
		CONFIGURATION
	}
 	
	private static Map<ManagerTypeEnum, Object> managersInstanceMap = new HashMap<ManagerTypeEnum, Object>();
 	
 	public static UsersIF getUserManager(){
		if (!managersInstanceMap.containsKey(ManagerTypeEnum.USERS)){
 			createUserManager();
 		}
		return (UsersIF)managersInstanceMap.get(ManagerTypeEnum.USERS);
 	}
 	
 	private static UsersIF createUserManager(){
 		UsersManager manager = new UsersManager();
 		UsersDecorator decorator = new UsersDecorator(manager);
		managersInstanceMap.put(ManagerTypeEnum.USERS, decorator);
 		return decorator;
 	}
 	
 	public static ServicesIF getServicesManager(){
		if (!managersInstanceMap.containsKey(ManagerTypeEnum.SERVICES)){
 			createServiceManager();
 		}
		return (ServicesIF)managersInstanceMap.get(ManagerTypeEnum.SERVICES);
 	}
 
 	private static ServicesIF createServiceManager() {
 		ServiceManager manager = new ServiceManager();
 		ServiceDecorator decorator = new ServiceDecorator(manager);
		managersInstanceMap.put(ManagerTypeEnum.SERVICES, manager);
 		return decorator;
 	}
 	
 }
 
 
 
