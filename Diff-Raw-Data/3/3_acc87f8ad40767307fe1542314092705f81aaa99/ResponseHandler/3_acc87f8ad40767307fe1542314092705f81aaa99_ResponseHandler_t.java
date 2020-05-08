 package client;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.concurrent.BlockingQueue;
 
 import networking.packages.AuthenticationResponse;
 import networking.packages.AuthenticationResponse.AuthenticationResponseType;
 import networking.packages.DataResponse;
 import networking.packages.Response;
 import networking.packages.*;
 import data.*;
 
 public class ResponseHandler implements Runnable{
 	private BlockingQueue<Response> responseQueue;
 	
 	
 	public ResponseHandler(BlockingQueue<Response> responseQueue) {
 		super();
 		System.out.println("responseHandler created");
 		this.responseQueue = responseQueue;
 	}
 	
 	private void receivedAlarm(List<Alarm> data){
 		List<Alarm> alarms = new ArrayList<Alarm>();
 		for (Object object : data) {
 			alarms.add((Alarm) object);
 		}
 		//TODO send to program
 	}
 	
 	private void receivedNotification(List<Notification> data){
 		List<Notification> notifications = new ArrayList<Notification>();
 		for (Object object : data) {
 			notifications.add((Notification) object);
 		}
 		//TODO send to program
 	}
 	
 	private void receivedMeeting(List<Meeting> data){
 		List<Meeting> meetings = new ArrayList<Meeting>();
 		for (Object object : data) {
 			meetings.add((Meeting) object);
 		}
 		//TODO send to program
 	}
 	
 	private void receivedPeople(List<Person> data){
 		List<Person> people = new ArrayList<Person>();
 		for (Object object : data) {
 			people.add((Person) object);
 		}
 		Program.calendarModel.setAllPersons(data);
 	}
 	
 	private void receivedTeam(List<Team> data){
 		List<Team> team = new ArrayList<Team>();
 		for (Object object : data) {
 			team.add((Team) object);
 		}
 		//TODO send to program
 	}
 
 	private void handleResponse(Response response){
 		switch(response.getResponseType()){
 		case AUTHENTICATION_RESPONSE:
 			//TODO
 			break;
 		case DATA_RESPONSE:
 			DataResponse dataResponse = (DataResponse) response;
 			switch (dataResponse.getQueryResponseType()){
 			case ALARM_RESPONSE:
 				receivedAlarm(dataResponse.getData());
 				break;
 			case NOTIFICATION_RESPONSE:
 				receivedNotification(dataResponse.getData());
 				break;
 			case MEETING_RESPONSE:
 				receivedMeeting(dataResponse.getData());
 				break;
 			case PERSON_RESPONSE:
 				receivedPeople(dataResponse.getData());
 				break;
 			case TEAM_RESPONSE:
 				receivedTeam(dataResponse.getData());
 				break;
 			}
 			break;
 		}
 	}
 
 	
 	@Override
 	public void run() {
 		while(true){
 			try {
 				handleResponse(responseQueue.take());
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 }
 
