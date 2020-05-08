 package ie.sortons.gwtfbplus.server.fql;
 
 
 import com.google.gson.Gson;
 
 
 /**
  * 
  * {
  *  "data": [
  *    {
  *      "uid": 118467031573937,
  *      "eid": 290199141098468,
  *      "rsvp_status": "",
  *      "start_time": "2012-11-08T15:30:00+0000"
  *    }, 
  * 
  * @author brianhenry
  *
  */
 
public class FqlEventMember {
 
 	public FqlEventMember.FqlEventMemberItem[] getData() {
 		return data;
 	}
 
 	public FqlEventMember() {}
 
 	private FqlEventMember.FqlEventMemberItem[] data; 
 
 	public String toString(){
 		Gson gson = new Gson();
 		return gson.toJson(this);
 	}
 
 
 	public static class FqlEventMemberItem {
 	
 		public String getEid() {
 			return eid;
 		}
 
 		public String getInviter() {
 			return inviter;
 		}
 
 		public String getInviter_type() {
 			return inviter_type;
 		}
 
 		public String getRsvp_status() {
 			return rsvp_status;
 		}
 
 		public String getStart_time() {
 			return start_time;
 		}
 
 		public String getUid() {
 			return uid;
 		}
 
 		FqlEventMemberItem() {}
 		
 		private String eid;
 		private String inviter;
 		private String inviter_type;
 		private String rsvp_status;
 		private String start_time;
 		private String uid;
 		
 		//TODO
 		//Do a date convert in here.
 		
 		public String toString(){
 			Gson gson = new Gson();
 			return gson.toJson(this);
 		}
 		
 
 	}
 }
