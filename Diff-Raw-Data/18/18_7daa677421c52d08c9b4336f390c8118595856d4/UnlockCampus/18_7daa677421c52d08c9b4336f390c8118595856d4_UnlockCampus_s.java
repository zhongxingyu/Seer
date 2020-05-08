 package org.alt60m.ministry.servlet;
 
 import java.util.Collection;
 import java.util.Vector;
 import java.util.Arrays;
 import java.util.Hashtable;
 import org.alt60m.ministry.model.dbio.Activity;
 import org.alt60m.ministry.model.dbio.ActivityContact;
 
 import org.alt60m.ministry.model.dbio.LocalLevel;
 import org.alt60m.ministry.model.dbio.Staff;
 
 import org.alt60m.staffSite.model.dbio.StaffSiteProfile;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 public class UnlockCampus  {
 	
 	
 	public static Vector<String>keys(String userName){
 		
 		org.alt60m.security.dbio.model.User user=new org.alt60m.security.dbio.model.User();
 		user.setUsername(userName);
 		user.select();
 		org.alt60m.ministry.model.dbio.Person person=new org.alt60m.ministry.model.dbio.Person();
 		person.setFk_ssmUserID(user.getUserID());
 		person.select();
 		String personID=person.getPersonID()+"";
 		
 		Vector<String> result=new Vector<String>();
 		
 		Vector<Hashtable<String,String>> missionalTeams=new Vector<Hashtable<String,String>> (InfoBaseTool.listTeamsForPerson(personID));
 		for (Hashtable<String,String> h:missionalTeams){//we get campuses by team membership according to the person record's missional team assoc.s
 			Activity act=new Activity();
 			LocalLevel team=new LocalLevel(h.get("teamID"));
 			act.setTeam(team);
 			Collection<Activity> acts=act.selectList();
 			for (Activity a : acts) result.add(a.getTargetAreaId());
 		}
 		StaffSiteProfile profile=new StaffSiteProfile();
 		profile.setUserName(userName);
 		profile.select();
 		Staff staff=new Staff(profile.getAccountNo());
 		
 		LocalLevel team=staff.getLocalLevel();//we get campuses by team membership according to the staff table
 		Activity act=new Activity();
 		act.setTeam(team);
 		Collection<Activity> acts=act.selectList();
 		for (Activity a : acts) result.add(a.getTargetAreaId());
 
		ActivityContact contactRelationship=new ActivityContact();//we get campuses by contact relationship
		contactRelationship.setAccountNo(staff.getAccountNo());
		Collection<ActivityContact> contactRelationshipS=contactRelationship.selectList();
		for (ActivityContact a : contactRelationshipS) 	result.add(a.getActivity().getTargetAreaId());
 		
 		if (staff.getPosition().equals("Regional Director"))	result.add(staff.getRegion()); //regional directors see all in their domain
 		if (staff.getPosition().equals("National Director"))	result.add("ALL"); //national directors see all, period.
 		result.add("ALL"); //comment this out when we get approval for the security system and it is fully realized
 		return (Vector<String>)result;
 		
 
 	}
 
 }
