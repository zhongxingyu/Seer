 package net.rptools.maptool.model;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class CampaignProperties {
 
 	public static final String DEFAULT_TOKEN_PROPERTY_TYPE = "Basic";
 
 	private Map<String, List<TokenProperty>> tokenTypeMap;
     private List<String> remoteRepositoryList;
 
     private Map<String, Map<GUID, LightSource>> lightSourcesMap;
     
     private Map<String, LookupTable> lookupTableMap;
 
     private Map<String, SightType> sightTypeMap;
     
     private String defaultSightType;
     
     public CampaignProperties() {
     	init();
     }
     
     public CampaignProperties(CampaignProperties properties) {
     	tokenTypeMap = new HashMap<String, List<TokenProperty>>(); 
     	for (Entry<String, List<TokenProperty>> entry : properties.tokenTypeMap.entrySet()) {
 
     		List<TokenProperty> typeList = new ArrayList<TokenProperty>();
     		typeList.addAll(properties.tokenTypeMap.get(entry.getKey()));
 
     		tokenTypeMap.put(entry.getKey(), typeList);
     	}
 
     	remoteRepositoryList = new ArrayList<String>(properties.remoteRepositoryList);
     	
 		lookupTableMap = new HashMap<String, LookupTable>();
 		if (properties.lookupTableMap != null) {
 			lookupTableMap.putAll(properties.lookupTableMap);
 		}
 
 		defaultSightType = properties.defaultSightType;
 		sightTypeMap = new HashMap<String, SightType>();
 		if (properties.sightTypeMap != null) {
 			sightTypeMap.putAll(properties.sightTypeMap);
 		}
 
 		// TODO: This doesn't feel right, should we deep copy, or does this do that automatically ?
 		lightSourcesMap = new HashMap<String, Map<GUID, LightSource>>(properties.lightSourcesMap);
     }
     
     public void mergeInto(CampaignProperties properties) {
     	
     	if (tokenTypeMap != null) {
     		// This will replace any dups
     		properties.tokenTypeMap.putAll(tokenTypeMap);
     	}
     	
     	if (remoteRepositoryList != null) {
     		// Need to cull out dups
     		for (String repo : properties.remoteRepositoryList) {
     			if (!remoteRepositoryList.contains(repo)) {
     				properties.remoteRepositoryList.add(repo);
     			}
     		}
     	}
     	
     	if (lightSourcesMap != null) {
     		properties.lightSourcesMap.putAll(lightSourcesMap);
     	}
     	
     	if (lookupTableMap != null) {
     		properties.lookupTableMap.putAll(lookupTableMap);
     	}
     	
     	if (sightTypeMap != null) {
     		properties.sightTypeMap.putAll(sightTypeMap);
     	}
     }
     
     public Map<String, List<TokenProperty>> getTokenTypeMap() {
     	if (tokenTypeMap == null) {
     		initTokenTypeMap();
     	}
     	return tokenTypeMap;
     }
 
     public Map<String, SightType> getSightTypeMap() {
     	if (sightTypeMap == null) {
     		initSightTypeMap();
     	}
     	return sightTypeMap;
     }
 
     // TODO: This is for conversion from 1.3b19-1.3b20
     public void setTokenTypeMap(Map<String, List<TokenProperty>> map) {
     	tokenTypeMap = map;
     }
     
     public List<TokenProperty> getTokenPropertyList(String tokenType) {
     	return getTokenTypeMap().get(tokenType);
     }
 
     public List<String> getRemoteRepositoryList() {
     	if (remoteRepositoryList == null) {
     		initRemoteRepositoryList();
     	}
     	return remoteRepositoryList;
     }
 
     // TODO: This is for conversion from 1.3b19-1.3b20
     public void setRemoteRepositoryList(List<String> list) {
     	remoteRepositoryList = list;
     }
     
     public Map<String, Map<GUID, LightSource>> getLightSourcesMap() {
     	if (lightSourcesMap == null) {
     		initLightSourcesMap();
     	}
     	return lightSourcesMap;
     }
     
     // TODO: This is for conversion from 1.3b19-1.3b20
     public void setLightSourcesMap(Map<String, Map<GUID, LightSource>> map) {
     	lightSourcesMap = map;
     }
 
     public Map<String, LookupTable> getLookupTableMap() {
     	if (lookupTableMap == null) {
     		initLookupTableMap();
     	}
     	return lookupTableMap;
     }
 
     // TODO: This is for conversion from 1.3b19-1.3b20
     public void setLookupTableMap(Map<String, LookupTable> map) {
     	lookupTableMap = map;
     }
     
     private void init() {
     	initLookupTableMap();
     	initLightSourcesMap();
     	initRemoteRepositoryList();
     	initTokenTypeMap();
     	initSightTypeMap();
     }
 
     private void initLookupTableMap() {
     	if (lookupTableMap != null) {
     		return;
     	}
 		lookupTableMap = new HashMap<String, LookupTable>();
     }
 
     private void initLightSourcesMap() {
     	if (lightSourcesMap != null) {
     		return;
     	}
 		lightSourcesMap = new HashMap<String, Map<GUID, LightSource>>();
 
 		try {
 			Map<String, List<LightSource>> map = LightSource.getDefaultLightSources();
 			for (String key : map.keySet()) {
 				
 	    		Map<GUID, LightSource> lightSourceMap = new LinkedHashMap<GUID, LightSource>();
 	    		for (LightSource source : map.get(key)) {
 	    			lightSourceMap.put(source.getId(), source);
 	    		}
 	    		lightSourcesMap.put(key, lightSourceMap);
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace(); 
 		}
     		
     }
 
     private void initRemoteRepositoryList() {
     	if (remoteRepositoryList != null) {
     		return;
     	}
 		remoteRepositoryList = new ArrayList<String>();
 		remoteRepositoryList.add("http://rptools.net/image-indexes/gallery.rpax.gz");
     }
     
     public String getDefaultSightType() {
     	return defaultSightType;
     }
     
     private void initSightTypeMap() {
     	
     	sightTypeMap = new HashMap<String, SightType>();
     	
     	defaultSightType = "Normal";
     	sightTypeMap.put("Normal", new SightType("Normal", 1, null));
     	sightTypeMap.put("Lowlight", new SightType("Lowlight", 2, null));
     	
     	try {
     		sightTypeMap.put("Darkvision", new SightType("Darkvision", 1, LightSource.getDefaultLightSources().get("Generic").get(5)));
    		sightTypeMap.put("Darkvision & Lowlight", new SightType("Darkvision", 2, LightSource.getDefaultLightSources().get("Generic").get(4)));
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
     }
     
     private void initTokenTypeMap() {
     	if (tokenTypeMap != null) {
     		return;
     	}
     	
 		tokenTypeMap = new HashMap<String, List<TokenProperty>>();
 
 		List<TokenProperty> list = new ArrayList<TokenProperty>();
     	list.add(new TokenProperty("Strength", "Str"));
     	list.add(new TokenProperty("Dexterity", "Dex"));
     	list.add(new TokenProperty("Constitution", "Con"));
     	list.add(new TokenProperty("Intelligence", "Int"));
     	list.add(new TokenProperty("Wisdom", "Wis"));
     	list.add(new TokenProperty("Charisma", "Char"));
     	list.add(new TokenProperty("HP", true, true, false));
     	list.add(new TokenProperty("AC", true, true, false));
     	list.add(new TokenProperty("Defense", "Def"));
     	list.add(new TokenProperty("Movement", "Mov"));
     	list.add(new TokenProperty("Elevation", "Elv", true, false, false));
     	list.add(new TokenProperty("Description", "Des"));
 
     	tokenTypeMap.put(DEFAULT_TOKEN_PROPERTY_TYPE, list);
     }
 }
