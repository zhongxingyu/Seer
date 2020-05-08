 package com.vzw.hackathon.apihandler;
 
 import com.vzw.hackathon.GroupEvent;
 import com.vzw.hackathon.GroupEventManager;
 import com.vzw.hackathon.MemberStatus;
 
 public class RestClient2Test {
	//private static final String serverBaseURL = "http://hud.wcmad.com:8080";
	private static final String serverBaseURL = "http://localhost:8080";
 	
 	//ssh -nNT -R 8080:localhost:8080 -l hud hud.wcmad.com
 	
 	public static void main(String[] args) {
 
 		
 		
 		//testGroupEvent();
		testRsvp();
		//stestPostMessage();
 		
 
 		
 		
 	}
 
 	public static void testGroupEvent() {
 		GroupEvent ge = GroupEventManager.getInstance().loadGroupEventFromDb(3);
 		RestClient2.createGroupEvent(ge, serverBaseURL);
 	}
 	
 	public static void testRsvp() {
 		RestClient2.rsvp(4, "9258881234", MemberStatus.ACCEPTED, serverBaseURL);
 	}
 	
 	public static void testPostMessage() {
 		RestClient2.postMessage("9252000001", "92520000002,9252000003", "test message", serverBaseURL);
 	}
 }
