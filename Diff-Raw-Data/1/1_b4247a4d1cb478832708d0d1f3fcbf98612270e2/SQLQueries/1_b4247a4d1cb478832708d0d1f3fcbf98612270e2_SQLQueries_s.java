 package server;
 
 
 public class SQLQueries {
 	
 	/**
 	 * MESSAGE : query for  oppdatere hvem som har lest et Message-objekt.
 	 * trenger parametrene userId og msgId
 	 * RETURN content
 	 */
 	
 //	Appointment sprringer
 	/**
 	 * Query for  legge tl en appointment
 	 * brukes til funksjonen save()
 	 * Hvilke users som blir invitert, blir evt lagt til med 'queryAddUserToAppointment'
 	 * som ligger lengre nede i dokumentet.
 	 * rekkeflge--> id (settes selv), place, title, descr, start, end, 
 	 * daysAppearing, endOfRe, isPrivate, creatorId, participants, room_name 
 	 */
 
 	String querySaveAppointment = 
 			"INSERT INTO Appointment VALUES(%s, %s, %s, " +
 			"date, date, set, Date,'%s' ,%b, %i)";
 	/**
 	 * UPDATE a meeting
 	 * Oppdaterer et allerede opprettet mte (appointment), 
 	 * med alle attributter. 
 	 */
 	String queryUpdateAppointment =
 			"UPDATE Appointment SET" +
 			" place='%s'" +
 			" title='%s'" +
 			" description='%s'" +
 			" startTime='date'" +
 			" endTime='date'" +
 			" daysAppearing='set'" +
 			" endOfRepeatDate='date'" +
 			" roomName='%s'" +
 			" isPrivate='%b'" +
 			" creatorId='%i'" +
 			" WHERE appId='%i'";
 	/**
 	 * getAppointment()
 	 */
 	String queryGetAppointment =
 			"SELECT * FROM Appointment WHERE appId='%i'";
 	/**
 	 * getTitle()
 	 */
 	String queryGetTitleAppointment =
 			"SELECT title FROM Appointment WHERE appId='%i'";
 	/**
 	 * getPlace()
 	 */
 	String queryGetPlaceAppointment =
 			"SELECT place FROM Appointment WHERE appId='%i'";
 	/**
 	 * getRoom()
 	 */
 	String queryGetRoomAppointment =
 			"SELECT roomName FROM Appointment WHERE appId='%i'";
 	/**
 	 * getStart()
 	 */
 	String queryGetStartAppointment =
 			"SELECT startTime FROM Appointment WHERE appId='%i'";
 	/**
 	 * getEnd()
 	 */
 	String queryGetEndAppointment =
 			"SELECT endTime FROM Appointment WHERE appId='%i'";
 	/**
 	 * deleteAppointment()
 	 */
 	String queryDeleteAppointment =
 			"DELETE FROM Appointment WHERE appId='%i'";
 	/**
 	 * acceptInvite(user, boolean)
 	 */
 	String queryUpdateUserAppointment = 
 			"UPDATE UserAppointment SET" +
 			" hasAccepted='%b'" +
 			" WHERE userId='%i' AND msgId='%i'";
 	/**
 	 * getParticipants()
 	 */
 	String queryGetParticipants =
 			"SELECT userId, hasAccepted FROM UserAppointment" +
 			" WHERE appId='%i'";
 	/**
 	 * updateParticipants(HashMap)
 	 * en funksjon for  legge til, og en for  slette
 	 * rekkeflge --> userId, appId, hasAccepted
 	 */
 	String queryAddUserToAppointment =
 			"INSERT INTO UserAppointment VALUES(%i, %i, %b)";
 	String queryDeleteUserFromAppointment = 
 			"DELETE FROM UserAppoinment WHERE userId='%i' AND msgId='%i'";
 	
 //	Sprringer til klasse Person:
 
 	// lagrer en ny bruker i systemet:
 	String querySavePerson = 
 				"INSERT INTO Person VALUES(%i, %s, %s, %s, %s, %s)";
 
 	// oppdaterer bruker
 	String queryUpdatePerson =
 				"UPDATE Person SET" +
 				" userId='%i'" +
 				" email='%s'" +
 				" passwordHash='%s'" +
 				" firstname='%s'" +
 				" lastname='%s'" +
 				" department='%s'";
 
 	// slette bruker
 	String queryDeletePerson =
 				"DELETE FROM Person WHERE userId='%i'";
 
 	// hente userId
 	String queryGetuserId = 
 				"SELECT userId FROM Person WHERE userId='%i'";
 
 	// hente email
 	String queryGetemail = 
 				"SELECT email FROM Person WHERE userId='%i'";
 
 	// hente firstname
 	String queryGetfirstname = 
 				"SELECT firstname FROM Person WHERE userId ='%i'";
 
 	// hente lastname
 	String queryGetlastname = 
 				"SELECT lastname FROM Person WHERE userId ='%i'";
 
 	// hente department
 	String queryGetdepartment = 
 				"SELECT department FROM Person WHERE userId ='%i'";
 
 //	Sprringer til klasse Calendar:
 
 	// lagrer en ny kalender i systemet:
 	String querySaveCal = 
 			"INSERT INTO Calendar";
 
 	//queryGetAppointments = 
 	String queryGetAppointments = 
 				"SELECT appId, place, startTime, endTime, description, daysAppearing, endOfRepeatDate," +  
 				"roomName, isPrivate, creatorId FROM Appointment WHERE startTime==start && endTime==end";
 
 
 //	Sprringer til klasse UserCalendars:
 	String queryAddUserToCalendar =
 				"INSERT INTO UserCalendars VALUES(%i, %i)";
 	
 	String queryDeleteUserFromCalendar = 
 				"DELETE FROM UserCalendar WHERE userId='%i' AND calendarId='%i'";
				
 
 //	Sprringer til klasse Room:
 
 	// oppretter nytt rom med navn og kapasitet:
 	String querySaveRoom = 
 				"INSERT INTO Room VALUES(%s, %i)";
 
 	// oppdaterer rom
 	String queryUpdateRoom =
 				"UPDATE Room SET" +
 				" name='%s'" +
 				" capacity='%s'";
 
 	// hente romnavn
 	String queryGetroomName = 
 				"SELECT name FROM Room WHERE name='%s'";
 
 	// hente romkapasitet
 	String queryGetroomCapacity = 
 				"SELECT capacity FROM Room WHERE name='%i'";
 	
 	// sprring for isAvailable()
 	String queryIsAvalable =
 			"SELECT name, capacity FROM Room";
 
 
 //	Sprringer til klasse Message:
 	/**
 	 * MESSAGE : query for  oppdatere hvem som har lest et Message-objekt.
 	 * trenger parametrene userId og msgId
 	 * RETURN content
 	 */
 	// Registrere at melding er lest
 	String queryReadMessage = 
 				"UPDATE UserMessages SET hasBeenRead='true' WHERE userId='%i' AND msgId='%i'";
 
 	// Hente ut innholdet/lese en melding
 	String queryGetContentMsg = 
 				"SELECT content FROM Message WHERE msgId='%i'";
 
 	// Opprette en ny melding
 	String querySaveMessage = 
 				"INSERT INTO Message VALUES(%i, Date, %s, %s)";
 
 	// Hente ut innholdet meldingstittel
 	String queryGetTitle = 
 				"SELECT title FROM Message WHERE msgId='%i'";
 	
 }
