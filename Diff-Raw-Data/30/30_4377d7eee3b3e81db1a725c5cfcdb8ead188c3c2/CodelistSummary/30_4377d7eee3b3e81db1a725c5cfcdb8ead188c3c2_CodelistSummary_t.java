 package org.cotrix.repository;
 
 import static java.util.Collections.*;
 import static org.cotrix.domain.utils.Constants.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.cotrix.domain.Attribute;
 
 /**
  * Summary data about a codelist
  * @author Fabio Simeoni
  *
  */
 public class CodelistSummary {
 
 	private final QName name;
 	private final int size;
 	private final Collection<String> allLanguages = new ArrayList<String>();
 	private final Collection<QName> allTypes = new ArrayList<QName>();
 	private final Collection<QName> allNames = new ArrayList<QName>();
 	
 	private final Map<QName,Map<QName,Set<String>>> fingerprint;
 	private final Map<QName,Map<QName,Set<String>>> globalFingerprint;
 	
 	
 	public CodelistSummary(QName name,int size,Collection<Attribute> attributes, Map<QName,Map<QName,Set<String>>> fingerprint) {
 		
 		this.name=name;
 		this.size=size;
 		this.fingerprint=fingerprint;
 		
 		this.globalFingerprint = new HashMap<QName, Map<QName,Set<String>>>();
 		
 		addToFingerprint(this.globalFingerprint,attributes);
 		
 
 		//build flat globals
 		for (QName n : fingerprint.keySet()) {
 			allNames.add(n);
 			for (Map<QName,Set<String>> typeMap : fingerprint.values()) {
 				allTypes.addAll(typeMap.keySet());
 				for (Set<String> ls : typeMap.values())
 					allLanguages.addAll(ls);
 			}
 		}
 		
 		for (QName n : globalFingerprint.keySet()) {
 			allNames.add(n);
 			for (Map<QName,Set<String>> typeMap : globalFingerprint.values()) {
 				allTypes.addAll(typeMap.keySet());
 				for (Set<String> ls : typeMap.values())
 					allLanguages.addAll(ls);
 			}
 		}
 		
 		
 	
 		
 	}
 	
 	public QName name() {
 		return name;
 	}
 	
 	public int size() {
 		return size;
 	}
 	
 	public Collection<String> allLanguages() {
 		
 		return allLanguages;
 	}
 	
 	public Collection<QName> allTypes() {
 		
 		return allTypes;
 	}
 
 	public Collection<QName> allNames() {
 		return allNames;
 	}
 	
 	public Collection<String> codeLanguagesFor(QName name,QName type) {
 		if (fingerprint.containsKey(name))
 			if (fingerprint.get(name).containsKey(type))
 					return fingerprint.get(name).get(type);
 			
 		return emptySet();
 	}
 	
 	public Collection<QName> allTypesFor(QName name) {
 		
		Collection<QName> values = new HashSet<QName>();
	
		Map<QName,Set<String>> typeMap = globalFingerprint.get(name);
		
		if (typeMap!=null)
			values.addAll(typeMap.keySet());
 		
		typeMap = fingerprint.get(name);
		
		if (typeMap!=null)
			values.addAll(typeMap.keySet());
 		
 		return values;
 	}
 	
 	public Collection<String> allLanguagesFor(QName name,QName type) {
		
 		Collection<String> values = new HashSet<String>();
 
 		if (globalFingerprint.containsKey(name))
 			if (globalFingerprint.get(name).containsKey(type))
 					values.addAll(globalFingerprint.get(name).get(type));
 
 		if (fingerprint.containsKey(name))
 			if (fingerprint.get(name).containsKey(type))
 					values.addAll(fingerprint.get(name).get(type));
 		
 
 		return values;
 	}
 
 	public Collection<QName> codeNames() {
 		return fingerprint.keySet();
 	}
 	
 	public Collection<QName> codeTypesFor(QName name) {
 		return fingerprint.get(name).keySet();
 	}
 	
 	
 	public static void addToFingerprint(Map<QName,Map<QName,Set<String>>> fingerprint, Iterable<? extends Attribute> attributes) {
 		
 		for (Attribute a : attributes)
 			addAttributeToFingerprint(fingerprint, a);
 		
 	}
 	
 	
 	//helper
 	private static void addAttributeToFingerprint(Map<QName,Map<QName,Set<String>>> fingerprint,Attribute a) {
 			
 			if (a.type().equals(SYSTEM_TYPE))
 				return;
 			
 			
 			Map<QName,Set<String>> typeForAttr = fingerprint.get(a.name());
 			
 			if (typeForAttr==null) {
 				
 				typeForAttr = new HashMap<QName, Set<String>>();
 				fingerprint.put(a.name(), typeForAttr);
 				
 			}
 			
 			Set<String> langForType = typeForAttr.get(a.type());
 				
 			if (langForType==null) {
 				
 				langForType = new HashSet<String>();
 				typeForAttr.put(a.type(),langForType);	
 				
 			}
 			
 			if (a.language() != null)
 				langForType.add(a.language());
 			
 		}
 }
