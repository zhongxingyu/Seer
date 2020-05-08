 package org.cotrix.domain.user;
 
 import static org.cotrix.action.Action.*;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.cotrix.action.Action;
 import org.cotrix.action.ResourceType;
 
 public class FingerPrint {
 
 	private Map<ResourceType,Map<String,Rights>> fp = new HashMap<ResourceType,Map<String,Rights>>();
 	
 	public FingerPrint(User user) {
 	
 		for (Action p : user.permissions())
 			buildTarget(p.resource(),p.type()).permissions.add(p);
 		
 		for (Role r : user.roles())
 			buildTarget(r.resource(),r.type()).roles.add(r.name());
 		
 	}
 	
 	public Collection<ResourceType> types() {
 		return fp.keySet();
 	}
 	
 	public Collection<String> resources(ResourceType type) {
 		
 		Collection<String> resources = fp.containsKey(type)?fp.get(type).keySet():new HashSet<String>();
 		
 		return resources;
 	}
 	
 	public Collection<String> rolesOver(String resource, ResourceType type) {
 		
 		Rights rights = target(resource,type);
 		
		Collection<String> roles =  (rights== null)? Collections.<String>emptySet() : rights.roles;
 		
 		//add roles over all resources
 		if (!resource.equals(any))
 			roles.addAll(rolesOver(any,type));
 		
 		return roles;
 	}
 	
 	
 	public Collection<Action> permissionsOver(String resource, ResourceType type) {
 		
 		Rights rights = target(resource,type);
 		
 		Collection<Action> permissions = rights == null? Collections.<Action>emptySet() : rights.permissions;
 		
 		//add permission over all resources
 		if (!resource.equals(any))
 			permissions.addAll(permissionsOver(any, type));
 		
 		return permissions;
 	}
 
 
 	//helper
 	public Rights target(String resource, ResourceType type) {
 		
 		Map<String,Rights> roleMap = fp.get(type);
 		
 		return roleMap!=null? roleMap.get(resource):null;
 	}
 	
 	private Rights buildTarget(String resource,ResourceType type) {
 		
 		Map<String,Rights> resourceMap = fp.get(type);
 		
 		if (resourceMap==null) {
 			resourceMap = new HashMap<String,Rights>();
 			fp.put(type,resourceMap);
 		}
 		
 		Rights randp = resourceMap.get(resource);
 		
 		if (randp == null) {
 			randp = new Rights();
 			resourceMap.put(resource,randp);
 		}
 		
 		return randp;
 	}
 	
 	
 	class Rights {
 
 		private Collection<String> roles = new HashSet<String>();
 		private Collection<Action> permissions = new HashSet<Action>();
 		
 		
 		@Override
 		public String toString() {
 			return "[roles=" + (roles != null ? roles.toString() : null) + ", permissions="
 					+ (permissions != null ? permissions.toString() : null) + "]";
 		}
 	}
 }
