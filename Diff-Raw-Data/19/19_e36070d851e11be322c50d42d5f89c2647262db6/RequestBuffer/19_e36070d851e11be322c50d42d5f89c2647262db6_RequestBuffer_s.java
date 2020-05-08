 package net.cpbgroup.Saucier;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 public class RequestBuffer extends Vector<Request> {
 
 
 	/**
 	 * Filters the RequestBuffer by the given statusCode
 	 * @param statusCode
 	 * @return
 	 */
 	public RequestBuffer filterByHttpStatusCode(int statusCode) {
 		RequestBuffer filteredList = new RequestBuffer();
 		for(Request r : this) {
 			if(r.statusCode == statusCode) filteredList.add(r);
 		}
 		return filteredList;
 	}
 	
 	/**
 	 * Filters the RequestBuffer by the given hostname
 	 * @param String hostname
 	 * @return
 	 */
 	public RequestBuffer filterByHost(String hostname) {
 		RequestBuffer filteredList = new RequestBuffer();
 		for(Request r : this) {
 			if(r.uri.getHost().contains(hostname)) {
 				filteredList.add(r);
 			}
 		}
 		return filteredList;
 	}
 	
 	/**
 	 * Filters the RequestBuffer by the given ArrayList of hostnames
 	 * @param ArrayList<String> hostnames
 	 * @return
 	 */
 	public RequestBuffer filterByHostnames(ArrayList<String> hostnames) {
 		RequestBuffer filteredList = new RequestBuffer();
 		for(Request r : this) {
 			for(String host : hostnames) {
 				if(r.uri.getHost().contains(host)) filteredList.add(r);
 			}
 		}
 		return filteredList;
 	}
 }
