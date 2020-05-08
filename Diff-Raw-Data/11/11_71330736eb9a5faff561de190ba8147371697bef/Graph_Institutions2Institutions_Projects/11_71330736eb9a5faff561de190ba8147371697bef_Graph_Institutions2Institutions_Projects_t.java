 package edu.utep.cybershare.vlc.visualization.transformers.graphBuilders.graphs;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 public class Graph_Institutions2Institutions_Projects {
 		
 	private ArrayList<InstitutionNode> nodes;
 	private Hashtable<String,Integer> nodesMap;
 	private int nodeCounter;
 	
 	private Hashtable<String,InstitutionLink> linksMap;
 	
 	public Graph_Institutions2Institutions_Projects(){
 		nodeCounter = 0;
 		linksMap = new Hashtable<String,InstitutionLink>();
 		nodesMap = new Hashtable<String,Integer>();
 	}
 		
 	public void addLink(InstitutionLink link){
 		String key = link.getKey();
 		
 		InstitutionLink existingLink = linksMap.get(key);
 		if(existingLink == null)
 			linksMap.put(key, link);
 		else{
 			for(String projectName : link.getCommonProjectNames())
 				existingLink.addCommonProjectName(projectName);
 		}
 	}
 	
 	private void populateNodes(){
 		List<InstitutionLink> links = new ArrayList<InstitutionLink>(linksMap.values());
 		nodes = new ArrayList<InstitutionNode>();
 		
 		InstitutionLink aLink;
 		InstitutionNode sourceInstitution;
 		InstitutionNode targetInstitution;
 				
 		for(int i = 0; i < links.size(); i ++){
 			aLink = links.get(i);
 			sourceInstitution = aLink.getSourceInstitution();
 			targetInstitution = aLink.getTargetInstitution();
 			
 			sourceInstitution.setIndex(getIndex(aLink.getSourceInstitution()));
 			targetInstitution.setIndex(getIndex(aLink.getTargetInstitution()));		
 		}
 	}
 	
 	private int getIndex(InstitutionNode node){
 		Integer index = nodesMap.get(node.getKey());
 		if(index == null){
 			nodesMap.put(node.getKey(), new Integer(nodeCounter));
 			nodes.add(node);
 			return nodeCounter ++;
 		}
 		
 		return index;
 	}
 	
 	private JSONArray getJSONArrayNodes(){
 		JSONArray jsonNodes = new JSONArray();
 		JSONObject jsonNode;
 		InstitutionNode node;
 		for(int i = 0; i < nodes.size(); i ++){
 			node = nodes.get(i);
 			jsonNode = new JSONObject();
 			try{
 				jsonNode.put("location", node.getInstitutionName());
 				jsonNode.put("lat", node.getLatitude());
 				jsonNode.put("lon", node.getLongitude());
 				//TODO Need to add list of projects associated with institutions!!!
 				
 				jsonNodes.put(i, jsonNode);
 			}catch(Exception e){e.printStackTrace();}
 		}
 		return jsonNodes;
 	}
 	
 	private JSONArray getJSONArrayLinks(){
 		ArrayList<InstitutionLink> links = new ArrayList<InstitutionLink>(linksMap.values());
 		InstitutionLink institutionLink;
 		JSONArray jsonLinks = new JSONArray();
 		JSONObject jsonLink;
 		for(int i = 0; i < links.size(); i ++){
 			institutionLink = links.get(i);
 			jsonLink = new JSONObject();
 			try{				
 				jsonLink.put("source", institutionLink.getSourceInstitution().getIndex());
 				jsonLink.put("target", institutionLink.getTargetInstitution().getIndex());
 				jsonLink.put("location", "");
 				jsonLink.put("projects", new JSONArray(institutionLink.getCommonProjectNames()));
 
 				jsonLinks.put(i, jsonLink);
 			}catch(Exception e){e.printStackTrace();}
 		}
 		return jsonLinks; 
 	}
 	
 	public JSONObject getJSONObjectGraph(){
 		populateNodes();
 		
 		JSONObject graph = new JSONObject();
 		JSONArray links = getJSONArrayLinks();
 		JSONArray nodes = getJSONArrayNodes();
 		
 		try{
 			graph.put("links", links);
 			graph.put("nodes", nodes);
 			return graph;
 		}
 		catch(Exception e){e.printStackTrace();}
 		return graph;
 	}
 	
 	public static class InstitutionNode{
 		private String institutionName;
 		private double lat;
 		private double lon;
 		private ArrayList<String> projectNames;
 		private int index;
 		
 		public InstitutionNode(String institutionName, double latitude, double longitude){
 			this.institutionName = institutionName;
 			this.projectNames = new ArrayList<String>();
 			index = -1;
 			
 			lat = latitude;
 			lon = longitude;
 		}
 		
 		private double getLatitude(){
 			return lat;
 		}
 		
 		private double getLongitude(){
 			return lon;
 		}
 		
 		private String getInstitutionName(){
 			return institutionName;
 		}
 		
 		public void addProjectName(String projectName){
 			projectNames.add(projectName);
 		}
 		
 		private void setIndex(int index){
 			this.index = index;
 		}
 		
 		private int getIndex(){
 			return index;
 		}
 		
 		private String getKey(){
 			return institutionName;
 		}
 	}
 	
 	public static class InstitutionLink{
 		private InstitutionNode sourceInstitution;
 		private InstitutionNode targetInstitution;
 				
 		private ArrayList<String> commonProjectNames;
 		
 		public InstitutionLink(InstitutionNode sourceInstitution, InstitutionNode targetInstitution){
 			this.sourceInstitution = sourceInstitution;
 			this.targetInstitution = targetInstitution;
 			
 			commonProjectNames = new ArrayList<String>();
 		}
 		
 		public void addCommonProjectName(String projectName){
 			if(!commonProjectNames.contains(projectName))
 				commonProjectNames.add(projectName);
 		}
 		
 		private List<String> getCommonProjectNames(){
 			return commonProjectNames;
 		}
 		
 		public InstitutionNode getSourceInstitution(){
 			return sourceInstitution;
 		}
 		
 		public InstitutionNode getTargetInstitution(){
 			return targetInstitution;
 		}
 					
 		//since the graph is undirected, this will help us eliminate x->y and y->x redundant links
 		//but we actually want redundancies due to Matt's current code for hiding links
 		private String getKey(){
 			if(sourceInstitution.getKey().hashCode() > targetInstitution.getKey().hashCode())
 				return sourceInstitution.getKey() + targetInstitution.getKey();
 			else
 				return targetInstitution.getKey() + sourceInstitution.getKey();
 			//return sourceInstitution.getKey() + targetInstitution.getKey();
 		}
 	}
 }
