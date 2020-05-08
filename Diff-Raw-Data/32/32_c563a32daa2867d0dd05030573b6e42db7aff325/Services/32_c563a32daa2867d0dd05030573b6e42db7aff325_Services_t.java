 package org.eclipse.acceleo.module.model2play.soaws.services;
 
 
 import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
 import java.util.TreeMap;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.obeonetwork.dsl.soa.Operation;
 import org.eclipse.acceleo.module.model2play.soaws.*;
 
 public class Services {
 	
 	public final String DEFAULT_PROTOCOL = "GET";
 
	Map<String,String> _protocols = new HashMap<String, String>() {/**
 		 * 
 		 */
 		private static final long serialVersionUID = -7504100133787271366L;
 
		{
 		   put("constructor", "POST");
 		   put("destructor", "DELETE");
 		   put("setter", "PUT");
 		   put("getter","GET");
 		}};
		
	Set<String> _datatypes = new HashSet<String>() {
		
		{
			add("Double");
			add("Long");
			add("Float");
			add("Integer");
		}
		
	};
 
 	
 	public String get_protocol_method( String operation_type ) {
 		
 		if ( _protocols.containsKey( operation_type ) ) {
 			return _protocols.get(operation_type);
 		}
 		else {
 			return DEFAULT_PROTOCOL;
 		}
 		
 	}
 	
	public boolean is_default_datatype( String typename ) {
		
		return _datatypes.contains(typename);
		
	}
	
	public Set<String> get_default_datatypes() {
		return _datatypes;
	}
	/* Unused */
 	public String entityNameToVarName( String entity_name ) {
 		
 		String var_name = entity_name;
 		
 		String str = "";
 		
 		for ( int i=1; i<var_name.length(); i++  ) {
 			
 			//if ( entity_name. )
 				//CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "SomeInput");
 		}
 		
 		
 		return var_name;
 		
 	}
 	
 };
