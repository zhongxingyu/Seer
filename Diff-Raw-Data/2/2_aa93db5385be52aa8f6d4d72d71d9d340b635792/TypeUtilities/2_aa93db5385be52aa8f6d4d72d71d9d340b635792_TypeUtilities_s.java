 /**
  * (c) 2013 FAO / UN (project: virtual-repository-service)
  */
 package org.virtualrepository.service.utils;
 
 import org.virtualrepository.AssetType;
 
 /**
  * Place your class / interface description here.
  *
  * History:
  *
  * ------------- --------------- -----------------------
  * Date			 Author			 Comment
  * ------------- --------------- -----------------------
  * 27 Aug 2013   Fiorellato     Creation.
  *
  * @version 1.0
  * @since 27 Aug 2013
  */
 final public class TypeUtilities {
 	private TypeUtilities() { }
 	
 	static public AssetType forName(AssetType[] types, String name) throws RuntimeException {
 		if(types == null || types.length == 0)
 			throw new RuntimeException("Please provide a non-NULL and non-empty list of available asset types");
 		
 		if(name == null)
 			throw new RuntimeException("Please provide a non-NULL asset type name");
 		
 		for(AssetType type : types)
 			if(type.name().equals(name))
 				return type;
 		
		throw new RuntimeException("Unknown / unavailable asset type name " + name);
 	}
 }
