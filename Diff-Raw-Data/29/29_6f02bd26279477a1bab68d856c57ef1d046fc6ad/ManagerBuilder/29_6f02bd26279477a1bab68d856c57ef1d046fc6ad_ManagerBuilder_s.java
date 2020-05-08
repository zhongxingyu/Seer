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
 	
	private static enum Types {USERS, BANK, SERVICES, AUTORIZATION, CONFIGURATION }
 	
	private static Map<Types, Object> typesMap = new HashMap<Types, Object>();
 	
 	public static UsersIF getUserManager(){
		if (!typesMap.containsKey(Types.USERS)){
 			createUserManager();
 		}
		return (UsersIF)typesMap.get(Types.USERS);
 	}
 	
 	private static UsersIF createUserManager(){
 		UsersManager manager = new UsersManager();
 		UsersDecorator decorator = new UsersDecorator(manager);
		typesMap.put(Types.USERS, decorator);
 		return decorator;
 	}
 	
 	public static ServicesIF getServicesManager(){
		if (!typesMap.containsKey(Types.SERVICES)){
 			createServiceManager();
 		}
		return (ServicesIF)typesMap.get(Types.SERVICES);
 	}
 
 	private static ServicesIF createServiceManager() {
 		ServiceManager manager = new ServiceManager();
 		ServiceDecorator decorator = new ServiceDecorator(manager);
		typesMap.put(Types.SERVICES, manager);
 		return decorator;
 	}
 	
 }
 
 
 
