 package uk.ac.nott.mrl.homework.server.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gson.annotations.Expose;
 
 public class Catalogue
 {
 	private class InnerCatalogue
 	{
 		@Expose
 		private Dynamic dynamic = new Dynamic(); 
 	}
 	
	private class Meta
	{
		@Expose
		private Map<String, Map<String, String>> devices = new HashMap<String, Map<String,String>>();		
	}
	
 	private class Dynamic
 	{
 		@Expose
 		private Map<String,List<String>> subjects = new HashMap<String, List<String>>();
 		@Expose	
 		private Map<String,String> bundlelookup = new HashMap<String, String>();
 		@Expose	
		private Meta metadata = new Meta();		
 	}
 	
 	@Expose
 	private InnerCatalogue catalogue = new InnerCatalogue();
 		
 	public void addSubject(String subject, String ip)
 	{
 		List<String> ips = catalogue.dynamic.subjects.get(subject);
 		if(ips == null)
 		{
 			ips = new ArrayList<String>();
 			catalogue.dynamic.subjects.put(subject, ips);
 		}
 		ips.add(ip);
 	}
 	
 	public void addBundle(String id, String bundle)
 	{
 		catalogue.dynamic.bundlelookup.put(id, bundle);
 	}
 	
 	public void addName(String ip, String name)
 	{
		Map<String, String> metadata = catalogue.dynamic.metadata.devices.get(ip);
 		if(metadata == null)
 		{
 			metadata = new HashMap<String, String>();
			catalogue.dynamic.metadata.devices.put(ip, metadata);
 		}
 		metadata.put("name", name);
 	}
 }
