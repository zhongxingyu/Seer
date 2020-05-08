 package org.BB.interactive;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.cometd.bayeux.Message;
 import org.cometd.bayeux.server.BayeuxServer;
 import org.cometd.bayeux.server.ServerSession;
 import org.cometd.server.AbstractService;
 
 public class SimpleUserStatistics extends AbstractService {
 
 	long time;
 	
 	// channel => number of users
 	Map<String,Integer> messages;
 	
 	// page => language => user session
 	Map<String, Map<String, Set<ServerSession>>> pages;
 	
 	// user session => language, page
 	Map<ServerSession, String[]> users;
 	final long TIME_SPAN = 3*60000;
 	boolean log_messages;
 	
     public SimpleUserStatistics(BayeuxServer bayeux, boolean log_messages)
 	{
         super(bayeux, "statistics-service");
         addService("/**", "all");
         time = System.currentTimeMillis();
         messages = new TreeMap<String, Integer>();
         pages = new TreeMap<String, Map<String, Set<ServerSession>>>();
         users = new HashMap<ServerSession, String[]>();
         this.log_messages = log_messages;
 	}
     
     // TODO(kolman): Remove synchronized by writing data structure module.
     // This module has to store all user actions for statistics.
     // Different kinds of statistics can be derived later.
     // This synchronized is waste of CPU, make non blocking concurent 
     // queue instead and handling thread. 
     public synchronized void all(ServerSession remote, Message message) {
         if (message != null) {
         	
         	if (messages.containsKey(message.getChannel())) {
         		messages.put(message.getChannel(), messages.get(message.getChannel())+1);
         	} else {
         		messages.put(message.getChannel(), 1);
         	}
 
         	if (log_messages) {
         		System.err.println(message.getJSON());
         	}
 
         	Object pageObj = message.get("page");
     		if (pageObj instanceof String) {
     			String page = (String)pageObj;
     			
     			// Canonic URL
     			page = page.startsWith("http://") ? page.substring(7) : page;
     			page = page.startsWith("www.") ? page.substring(4) : page;
     			page = page.startsWith("kabbalahgroup.info/internet/") ? page.substring(28) : page;
     			page = page.startsWith("localhost:3000/") ? page.substring(15) : page;
     			
     			String language = page.substring(0, 2);
     			page = page.substring(2, page.length());
     			page = page.startsWith("#") ? page.substring(1) : page;
     			
     			Map<String, Set<ServerSession>> page_data = null;
     			Set<ServerSession> sessions = null;
     			page_data = pages.get(page);
     			if (page_data == null) {
     				page_data = new TreeMap<String, Set<ServerSession>>();
     			}
     			sessions = page_data.get(language);
     			if (sessions == null) {
     				sessions = new HashSet<ServerSession>();			
     			}
     			sessions.add(remote);
     			page_data.put(language, sessions);
     			pages.put(page, page_data);
 
     			// Remove existing user from previous page.
     			// Check use exists and check page is different.
         		if (users.containsKey(remote)) {
         			String[] lang_page = users.get(remote);
         			if ((lang_page != null && lang_page.length >= 2) &&
         				lang_page[0].compareTo(language) != 0 ||
         				lang_page[1].compareTo(page) != 0) {
         				
 		                Map<String, Set<ServerSession>> pages_page_data = pages.get(lang_page[1]);
 		                Set<ServerSession> pages_sessions = pages_page_data.get(lang_page[0]);
 		                
 		                if (pages_sessions != null) {
 		                	pages_sessions.remove(remote);
 			            	if (pages_sessions.size() == 0) {
 			            		pages_page_data.remove(lang_page[0]);
 			            		if (pages_page_data.size() == 0) {
 			            			pages.remove(lang_page[1]);
 			            		}
 			            	}
 		                }
         			}            		
         		}
 
     			users.put(remote, new String[] {language, page});
         	}
         }
         
         update();
     }
     
     public void update() {
         if (System.currentTimeMillis() - time > TIME_SPAN) {
             int connected = 0;
             for(ServerSession s : getBayeux().getSessions()) {
             	if (!s.isLocalSession()) {
             		connected++;
             	}
             }
             
             // Print statistics header
             System.err.println();
             for(Entry<String, Integer> e : messages.entrySet()) {
             	System.err.print(e.getKey() + ":" + e.getValue() + " ");
             }
             // print timestamp
             System.err.println("in timespan:" + String.valueOf(System.currentTimeMillis() - time));
             
             // Print number of users.
         	System.err.println("Users:" + connected);
         	
         	// Print users locations.
         	for(Entry<String, Map<String, Set<ServerSession>>> e : pages.entrySet()) {
         		
         		int total_count = 0;
         		System.err.print("page:\"" + e.getKey() + "\" languages:");
         		
         		for(Entry<String, Set<ServerSession>> f : e.getValue().entrySet()) {
        			System.err.print(f.getKey() + ":" + f.getValue().size() + " ");
         			total_count += f.getValue().size();
         		}
         		
         		System.err.println("total:" + total_count);
         	}
 
         	HashSet<ServerSession> liveSessions = 
         		new HashSet<ServerSession>(getBayeux().getSessions());
         	HashSet<ServerSession> toRemove = new HashSet<ServerSession>();
         	
             for(ServerSession s : users.keySet()) {
             	if (!liveSessions.contains(s)) {
             		toRemove.add(s);
             	}
             }
             for(ServerSession s : toRemove) {
             	String[] lang_page = users.get(s);
             	Map<String, Set<ServerSession>> page_data = pages.get(lang_page[1]);
             	if (page_data != null) {
 	            	Set<ServerSession> sessions = page_data.get(lang_page[0]);
 	            	if (sessions != null) {
 		        		sessions.remove(s);
 		        		if (sessions.size() == 0) {
 		        			page_data.remove(lang_page[0]);
 		        			if (page_data.size() == 0) {
 		        				pages.remove(lang_page[1]);
 		        			}
 		        		}
 	            	}
             	}
         		users.remove(s);
             }
 
             messages = new TreeMap<String, Integer>();
             time = System.currentTimeMillis();
         }
     }
 }
