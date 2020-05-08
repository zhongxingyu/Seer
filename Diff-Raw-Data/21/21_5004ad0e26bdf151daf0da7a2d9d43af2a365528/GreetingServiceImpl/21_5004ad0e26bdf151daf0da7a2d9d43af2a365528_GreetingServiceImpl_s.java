 package miro.server;
 
 import com.googlecode.objectify.Query;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 import miro.shared.*;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class GreetingServiceImpl extends RemoteServiceServlet implements
 		GreetingService {
 
 	String error = "";
 	Calendar calendar = new GregorianCalendar();
 
 	public void setLocked(boolean locked) throws IllegalArgumentException,
 			NullPointerException {
 		ObjectifyService.register(Lock.class);
 		Objectify ofy = ObjectifyService.begin();
		Lock lock = (Lock) ofy.find(new Key(Lock.class, 58002));
 		//ofy.getTxn().
 		if (lock.isLocked()) {
 			if (locked)
 				throw new IllegalArgumentException(
 						"Table utilise par un autre utilisateur.\nRessayez plus tard!");
 			else
 				lock.setLocked(false);
 		} else {
 			lock.setLocked(true);
 		}
 		ofy.put(lock);
 	}
 
 	public void putData() {
 		ObjectifyService.register(Assignment.class);
 		Objectify ofy = null;
 
 		Person person = new Person("bagno", "laurent");
 		Person person2 = new Person("alfred", "dupond");
 		Person person3 = new Person("Jean", "dujardin");
 
 		// person.setElementOfHolidaysList(1, new Record(5, new Time(2, 2010)));
 		// person.setElementForTrainingList(2, new Record(6, new Time(3,
 		// 2010)));
 
 		Project project = new Project("nova");
 		Project project2 = new Project("chancellerie");
 
 		Assignment assignment1 = new Assignment(project, person);
 		Assignment assignment2 = new Assignment(project, person2);
 		Assignment assignment3 = new Assignment(project, person3);
 		Assignment assignment4 = new Assignment(project2, person3);
 
 		try {
 			ofy = ObjectifyService.beginTransaction();
 			ofy.put(assignment1);
 			ofy.getTxn().commit();
 			ofy = ObjectifyService.beginTransaction();
 			ofy.put(assignment2);
 			ofy.getTxn().commit();
 			ofy = ObjectifyService.beginTransaction();
 			ofy.put(assignment3);
 			ofy.getTxn().commit();
 			ofy = ObjectifyService.beginTransaction();
 			ofy.put(assignment4);
 			ofy.getTxn().commit();
 		} catch (Exception e) {
 			error = e.getMessage();
 		} finally {
 			if (ofy.getTxn().isActive())
 				ofy.getTxn().rollback();
 		}
 	}
 
 	public List<Assignment> getAssignments() {
 		ObjectifyService.register(Assignment.class);
 		List<Assignment> assignmentList = new ArrayList<Assignment>();
 
 		//Les six lignes suivantes sont des tests
 		Objectify ofy2 = ObjectifyService.beginTransaction();
		ofy2.put(new Assignment(new Project("nova"),new Person("bart","simpson")));
 		ofy2.getTxn().commit();
 		
 		try {
 			Objectify ofy = ObjectifyService.begin();
 			
 			Query<Assignment> query = ofy.query(Assignment.class);
 
 			for (Assignment assignmentFromList : query) {
 				assignmentList.add(assignmentFromList);
 			}
 		} catch (Exception e) {
 		}
 		return assignmentList;
 	}
 
 	public String updateAssignments(List<Person> personList,
 			List<Assignment> assignmentList) {
 		List<Assignment> assignmentList2 = getAssignments();
 		Logger.getLogger(this.getClass()).error("put data");
 		
 		for (Assignment assignment : assignmentList) {
 
 			try {
 				if (assignmentList2.contains(assignment)) {
 					int indexOf = assignmentList2.indexOf(assignment);
 					Assignment assignment2 = assignmentList2.get(indexOf);
 
 					for (int i = 0; i < 12; i++) {
 						assignment2.setPrestation(i, assignment
 								.getPrestation(i));
 					}
 				} else {
 					assignmentList2.add(assignment);
 				}
 			} catch (Exception e) {
 			}
 		}
 		updateInfosPersonInAssignmentList(personList, assignmentList2);
 		
 		putData(assignmentList2);
 		Logger.getLogger(this.getClass()).error("put data");
 		return "" + assignmentList2.size();
 	}
 
 	private void updateInfosPersonInAssignmentList(List<Person> personList,
 			List<Assignment> assignmentList2) {
 		
 		for (int i = 0; i < personList.size(); i++) {
 			Person person = personList.get(i);
 			for (int j = 0; j < assignmentList2.size(); j++) {
 				if (assignmentList2.get(j).getPerson().equals(person)) {
 					assignmentList2.get(j).setPerson(person);
 				}
 			}
 		}
 	}
 
 	void putData(List<Assignment> assignmentList) {
 		ObjectifyService.register(Assignment.class);
 		Objectify ofy = null;
 		
 		for (Assignment assignment : assignmentList) {
 			try {
 				ofy = ObjectifyService.beginTransaction();
 				ofy.put(assignment);
 				ofy.getTxn().commit();
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	public String getMonthOfDate() {
 		return "" + calendar.get(GregorianCalendar.MONTH);
 	}
 	
 	public String getYearOfDate(){
 		return ""+calendar.get(GregorianCalendar.YEAR);
 	}
 }
