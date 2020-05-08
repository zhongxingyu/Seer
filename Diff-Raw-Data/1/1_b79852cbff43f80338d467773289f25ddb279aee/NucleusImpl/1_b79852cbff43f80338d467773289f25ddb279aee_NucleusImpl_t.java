 package org.varnalab.organic.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 import org.varnalab.organic.api.Nucleus;
 import org.varnalab.organic.api.Organel;
 import org.varnalab.organic.api.Plasma;
 
 public class NucleusImpl implements Nucleus {
 
 	private DNAImpl dna;
 	private Plasma plasma;
 	private Map<Object, Organel> organelMap;
 	
 	
 	@SuppressWarnings("unchecked")
 	public NucleusImpl(Object dna, Plasma plasma) {
 		super();
 		this.dna =  ((dna instanceof DNAImpl)? 
 				new DNAImpl((Map<String, Object>) dna):
 				(DNAImpl) dna);
 		this.plasma = plasma;
 	}
 
 	
 	private static final String SOURCE = "source";
 	private static final String ADDRESS = "address";
 	
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<Organel> createNamespace(String namespace, Object parent) {
 		Collection<Organel> result = new ArrayList<>();
 		Map<String, Object> branch = dna.selectBranch(namespace);
 	    for(String key: branch.keySet()) {
 	      Object objectConfig = branch.get(key);
 	      if (objectConfig instanceof Map)
 	    	  throw new IllegalStateException("a map was expected at namespace" + namespace);
 	      
 	      Map<String, Object> config = (Map<String, Object>) objectConfig;
 	      
 	      if(!config.containsKey(SOURCE))
 	    	  throw new IllegalStateException("can not create object without source at "+namespace);
 	        
 	      String source = (String) config.get(SOURCE);
 	      Organel instance = null;
 	      
 	      try {
 			instance = (Organel) Class.forName(source)
 			  .getConstructor(Plasma.class, Map.class, Object.class)
 			  .newInstance(plasma, config, parent);
 		} catch (InstantiationException | IllegalAccessException
 				| IllegalArgumentException | InvocationTargetException
 				| NoSuchMethodException | SecurityException
 				| ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	      
 	      result.add(instance);
 	      
 	      Object address = createAddress(config, namespace, key);
 	      organelMap.put(address, instance);
 	    }
 	  	return result;
 	}
 	
 	protected Object createAddress(Map<String, Object> config, String namespace, String key) {
 		return config.containsKey(ADDRESS) ? config.get(ADDRESS) : (namespace + "." + key);
 	}
 
 }
