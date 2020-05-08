 package client;
 
 import java.io.IOException;
 
 import networking.packages.AuthenticationRequest;
 import networking.packages.QueryRequest;
 import networking.packages.QueryRequest.QueryType;
 import networking.packages.UpdateRequest;
 import networking.packages.UpdateRequest.UpdateType;
 
 import data.Alarm;
 import data.Notification;
 import data.Person;
 import data.Meeting;
 
 public class RequestHandler {
 	
 	
 	//Authentication requests:
 	public void sendAuthenticationRequest(String username, String password) throws IOException {
 		AuthenticationRequest aReq = new AuthenticationRequest(username, password);
 		Program.client.sendRequest(aReq);
 	}
 	
 	//data requests:
 	public void sendGetAllPersonsRequest() throws IOException {
 		QueryRequest qReqGetAllPersons = new QueryRequest(null, null, QueryType.GET_ALL_PERSONS);
 		Program.client.sendRequest(qReqGetAllPersons);
 	}
 	
 	public void sendGetAllMeetingroomsRequest() throws IOException {
 		QueryRequest qReqGetAllMeetingrooms = new QueryRequest(null, null, QueryType.GET_ALL_MEETINGROOMS);
 		Program.client.sendRequest(qReqGetAllMeetingrooms);
 	}
 	
 	public void sendGetAllNotificationsRequest() throws IOException {
 		QueryRequest qReqGetAllNotifications = new QueryRequest(null, null, QueryType.GET_ALL_NOTIFICATIONS);
 		Program.client.sendRequest(qReqGetAllNotifications);
 	}
 	
 	public void sendGetEvryMeetingRequest() throws IOException{
 		QueryRequest qReqGetEveryMeeting =
 				new QueryRequest(null, null, QueryType.GET_EVERY_MEETING);
 		Program.client.sendRequest(qReqGetEveryMeeting);
 	}
 	
 	public void sendGetAlarmsByPersonRequest(Person person) throws IOException{
 		QueryRequest qReqGetAlarmsByPerson =
 				new QueryRequest(person, null, QueryType.GET_ALARMS_BY_PERSON);
 		Program.client.sendRequest(qReqGetAlarmsByPerson);
 	}
 	
 	public void sendGetNotificationsByMeetingRequest(Meeting meeting) throws IOException{
 		QueryRequest qReqGetNotificationsByMeeting =
 				new QueryRequest(null, meeting, QueryType.GET_NOTIFICATIONS_BY_MEETING);
 		Program.client.sendRequest(qReqGetNotificationsByMeeting);
 	}
 	
 	public void sendGetNotificationsByPersonRequest(Person person) throws IOException{
 		QueryRequest qReqGetNotificationsByPerson =
 				new QueryRequest(person, null, QueryType.GET_NOTIFICATIONS_BY_PERSON);
 		Program.client.sendRequest(qReqGetNotificationsByPerson);
 	}
 	
 	public void sendGetTeamsByMeetingRequest(Meeting meeting) throws IOException{
 		QueryRequest qReqGetTeamsByMeeting =
 				new QueryRequest(null, meeting, QueryType.GET_TEAMS_BY_MEETING);
 		Program.client.sendRequest(qReqGetTeamsByMeeting);
 	}
 	
 	//Update requests:
 	public void sendCreateMeetingRequest(Meeting m) throws IOException{
 		UpdateRequest uReqCreateMeeting =
 				new UpdateRequest(m, null, null, UpdateType.CREATE_MEETING);
 		Program.client.sendRequest(uReqCreateMeeting);
 	}
 	
 	public void sendUpdateMeetingRequest(Meeting m) throws IOException{
 		UpdateRequest uReqUpdateMeeting =
 				new UpdateRequest(m, null, null, UpdateType.UPDATE_MEETING);
 		Program.client.sendRequest(uReqUpdateMeeting);
 	}
 	
 	public void sendDeleteMeetingRequest(Meeting m) throws IOException{
 		UpdateRequest uReqDeleteMeeting =
 				new UpdateRequest(m, null, null, UpdateType.DELETE_MEETING);
 		Program.client.sendRequest(uReqDeleteMeeting);
 	}
 	
 	public void sendCreateAlarmRequest(Alarm a) throws IOException{
 		UpdateRequest uReqCreateAlarm =
 				new UpdateRequest(null, a, null, UpdateType.CREATE_ALARM);
 		Program.client.sendRequest(uReqCreateAlarm);
 	}
 
 	public void sendUpdateNotificationRequest(Notification n)throws IOException{
 		UpdateRequest uReqUpdateNotification =
 				new UpdateRequest(null, null, n, UpdateType.UPDATE_NOTIFICATION);
 		Program.client.sendRequest(uReqUpdateNotification);
 	}
 	
 }
 
