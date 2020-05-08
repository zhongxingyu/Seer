 package server.analytics;
 
 import java.util.ArrayList;
 
 public class Subscription {
 	String[] a = {"AUCTION_STARTED", "AUCTION_ENDED", "BID_PLACED", "BID_OVERBID", "BID_WON", "USER_SESSIONTIME_MIN", "USER_SESSIONTIME_MAX", "USER_SESSIONTIME_AVG", "BID_PRICE_MAX", "BID_COUNT_PER_MINUTE", "AUCTION_TIME_AVG", "AUCTION_SUCESS_RATIO", "USER_LOGIN", "USER_LOGOUT", "USER_DISCONNECTED"};
 	final ArrayList<String> types = new ArrayList<String>();
 	int id;
 	ArrayList<String> filter;
 	Client c;
 
 	public Subscription(int id, String f, Client c) {
 		this.id = id;	
 		for(int i = 0;i<a.length;i++) {
 			types.add(a[i]);
 		}
 
 		this.c = c;
 		filter = new ArrayList<String>();
 		createFilter(f);
 	}
 
 	private void createFilter(String f) {
 		String[] filterParts = f.split("\\|");
 		for(int i = 0;i<filterParts.length;i++){
 			if(filterParts[i].charAt(0) == '\'') {
 				filterParts[i] = filterParts[i].substring(1, filterParts[i].length()-1);
 
 				//remove ( and )
 				if(filterParts[i].charAt(0) == '(') {
 					filterParts[i] = filterParts[i].substring(1, filterParts[i].length()-1);
 
 					//if last char of pattern is * change to .*
 					if(filterParts[i].charAt(filterParts[i].length()-1) == '*') {
 						filterParts[i]=filterParts[i].substring(0, filterParts[i].length()-1) + ".*";
 					}
 
 					//for each type of types check if matches with pattern
 					for(String type:types) {
 						if(type.matches(filterParts[i])) {
 							//add type to filter
 							if(!filter.contains(type)) {
 								boolean alreadyExists = false;
 								for(Subscription s:c.getSubscriptions().values()) {
 									if(s.getFilter().contains(type)) {
 										alreadyExists = true;
 									}
 								}
 								if(!alreadyExists) {
 									filter.add(type);
 								}
 							}
 						}
 					}
 
 				} else {
 					System.out.println("Filter muss in Runden Klammern stehen!");
 				}
			}else {
				System.out.println("Filter muss in '' stehen!");
 			}
 		}
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public ArrayList<String> getFilter() {
 		return filter;
 	}
 
 	public void setFilter(ArrayList<String> filter) {
 		this.filter = filter;
 	}
 
 	public ArrayList<String> getTypes() {
 		return types;
 	}
 
 	public String toString(){
 		return id + " " + filter.toString();
 	}
 }
