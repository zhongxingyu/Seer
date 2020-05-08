 package server;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import data.Alarm;
 import data.Meeting;
 import data.MeetingRoom;
 import data.Notification;
 import data.Person;
 import data.Reservation;
 import data.Team;
 import data.TimeInterval;
 
 public class DBController {
 
 	/*
 	 * A class that deals with datatransfer with the database. Some methods
 	 * require testing Not final
 	 */
 
 	private DBConnection dBConn;
 
 	// Needs error handling
 	public DBController() {
 		dBConn = new DBConnection();
 		try {
 			dBConn.initialize();
 		} catch (ClassNotFoundException e) {
 		} catch (SQLException e) {
 			OutputController.output("No connection to database established!");
 		}
 	}
 
 	private String parseStringForDB(String string) {
 		if (string != null) {
 			string = "'" + string + "'";
 		}
 		return string;
 	}
 
 	public void updateNotification(Notification notification) throws SQLException {
 		String sql = String.format("UPDATE notification "
 				+ "SET approved = '%s' "
 				+ "WHERE notification.meetingID = %d AND username = '%s' ",
 				Character.toString(notification.getApproved()), notification
 						.getMeeting().getMeetingID(), notification.getPerson()
 						.getUsername());
 		dBConn.makeUpdate(sql);
 	}
 
 	public void updateMeeting(Meeting meeting) throws SQLException {
 		String sql = String
 				.format("UPDATE Meeting "
 						+ "SET title = %s, location = %s, startTime = %d, endTime = %d, description = %s, teamID = %d, username = %s "
 						+ "WHERE meetingID = %d", parseStringForDB(meeting
 						.getTitle()), parseStringForDB(meeting.getLocation()),
 						meeting.getStartTime(), meeting.getEndTime(),
 						parseStringForDB(meeting.getDescription()), meeting
 								.getTeam().getTeamID(), meeting.getCreator()
 								.getUsername(), meeting.getMeetingID());
 		dBConn.makeUpdate(sql);
 	}
 
 	public boolean authenticateUser(String username, String password)
 			throws SQLException {
 		String sql = String.format("SELECT * FROM Person "
 				+ "WHERE username = '%s' " + "AND passwd = '%s'", username,
 				password);
 		return dBConn.makeQuery(sql).next();
 	}
 
 	public List<Team> getTeamsByMeeting(int meetingID) throws SQLException {
 		List<Team> teams = new ArrayList<Team>();
 		String sql = String.format("SELECT * FROM Team "
 				+ "WHERE Team.meetingID = %d", meetingID);
 		ResultSet rs = dBConn.makeQuery(sql);
 		while (rs.next()) {
 			teams.add(getTeamFromResultSet(rs));
 		}
 		return teams;
 	}
 
 	public boolean personExists(String username) throws SQLException {
 		ResultSet rs = dBConn.makeQuery(String.format("SELECT * FROM Person "
 				+ "WHERE username = '%s'", username));
 		return rs.next();
 	}
 
 	public void addMemberOf(String username, int teamID) throws SQLException {
 		String sql = "INSERT INTO memberOF (teamID, username) ";
 		sql += "VALUES (";
 		sql += Integer.toString(teamID) + ", '" + username + "');";
 		dBConn.makeUpdate(sql);
 	}
 
 	public int addTeam(Team team) throws SQLException {
 		String sql = "INSERT INTO Team (email) ";
 		sql += "VALUES ('" + team.getEmail() + "');";
 		int teamID = dBConn.makeUpdateReturnID(sql);
 		for (Person person : team.getMembers()) {
 			addMemberOf(person.getUsername(), teamID);
 		}
 		return teamID;
 	}
 
 	public int addAlarm(Alarm alarm) throws SQLException {
 		String kind = "'" + Character.toString(alarm.getKind()) + "'";
 		String time = Long.toString(alarm.getTime());
 		String meetingID = Integer.toString(alarm.getMeeting().getMeetingID());
 		String username = "'" + alarm.getPerson().getUsername() + "'";
 		String sql = "INSERT INTO Alarm (kind, time, meetingID, username) ";
 		sql += "VALUES (";
 		sql += kind + ", " + time + ", " + meetingID + ", " + username + ");";
 
 		return dBConn.makeUpdateReturnID(sql);
 
 	}
 
 	public MeetingRoom getMeetingRoom(String roomName) throws SQLException {
 		String sql = String.format("SELECT * FROM MeetingRoom "
 				+ "WHERE MeetingRoom.roomName = %d", roomName);
 		ResultSet rs = dBConn.makeQuery(sql);
 		rs.next();
 		return new MeetingRoom(rs.getString("roomName"));
 	}
 
 	private Reservation getReservationFromResultSet(ResultSet rs)
 			throws SQLException {
 		Meeting meeting = getMeeting(rs.getInt("meetingID"));
 		MeetingRoom meetingRoom = getMeetingRoom(rs.getString("roomName"));
 		TimeInterval timeInterval = new TimeInterval(rs.getLong("startTime"),
 				rs.getLong("endTime"));
 		return new Reservation(timeInterval, meetingRoom, meeting);
 	}
 
 	public Reservation getReservationForMeeting(Meeting meeting)
 			throws SQLException {
 		String sql = String.format("SELECT * FROM reservation "
 				+ "WHERE reservation.meetingID = %d;", meeting.getMeetingID());
 		ResultSet rs = dBConn.makeQuery(sql);
 		rs.next();
 		return getReservationFromResultSet(rs);
 	}
 
 	/*
 	 * Returns every meeting owned by this person
 	 */
 	public List<Meeting> getEveryMeetingOwnedByPerson(Person person)
 			throws SQLException {
 		List<Meeting> meetings = new ArrayList<Meeting>();
 
 		String sql = "SELECT * FROM Meeting, Person "
				+ "WHERE Meeting.username = '" + person.getUsername()+"';";
 		ResultSet rs = dBConn.makeQuery(sql);
 
 		while (rs.next()) {
 			getMeetingFromResultSet(rs);
 		}
 		return meetings;
 	}
 
 	public List<Reservation> getReservationsForMeetingRoom(MeetingRoom room)
 			throws SQLException {
 		String sql = String.format("SELECT * FROM reservation "
 				+ "WHERE reservation.roomName = '%s';", room.getRoomName());
 		ResultSet rs = dBConn.makeQuery(sql);
 		List<Reservation> reservations = new ArrayList<Reservation>();
 		while (rs.next()) {
 			reservations.add(getReservationFromResultSet(rs));
 		}
 		return reservations;
 	}
 
 	public List<Meeting> getEveryMeeting() throws SQLException {
 		List<Meeting> meetings = new ArrayList<Meeting>();
 
 		String sql = "SELECT * FROM Meeting;";
 		ResultSet rs = dBConn.makeQuery(sql);
 
 		while (rs.next()) {
 			getMeetingFromResultSet(rs);
 		}
 		return meetings;
 	}
 
 	public void addNotification(Notification notification) throws SQLException {
 		String time = Long.toString(notification.getTime());
 		String approved = Character.toString(notification.getApproved());
 		String kind = Character.toString(notification.getKind());
 		String meetingID = Integer.toString(notification.getMeeting()
 				.getMeetingID());
 		String username = notification.getPerson().getUsername();
 		String sql = "INSERT INTO notification (time, approved, kind, meetingID, username) ";
 		sql += "VALUES (";
 		sql += time + ", '" + approved + "', '" + kind + "', " + meetingID
 				+ ", '" + username + "')";
 
 		dBConn.makeUpdate(sql);
 	}
 
 	public Meeting addMeeting(Meeting meeting) throws SQLException {
 		String teamID = "null";
 		// Checks if the team has been set or not. This is the main difference
 		// between appointments and meetings.
 		if (meeting.getTeam() != null) {
 			if (meeting.getTeam().getTeamID() == -1) {
 				teamID = Integer.toString(addTeam(meeting.getTeam()));
 			} else {
 				teamID = Integer.toString(meeting.getTeam().getTeamID());
 			}
 		}
 
 		String sql = "INSERT INTO Meeting (title, location, startTime, endTime, description, username, teamID) ";
 		sql += "VALUES ";
 		sql += "( '" + meeting.getTitle() + "', '" + meeting.getLocation()
 				+ "', " + meeting.getStartTime() + ", " + meeting.getEndTime()
 				+ ", '" + meeting.getDescription() + "', '";
 		sql += meeting.getCreator().getUsername() + "', ";
 		sql += teamID;
 		sql += ");";
 		System.out.println(sql);
 
 		int meetingID = dBConn.makeUpdateReturnID(sql);
 		// add notifications
 		System.out.println(meetingID);
 		Meeting newMeeting = getMeeting(meetingID);
 		if (meeting.getTeam() != null) {
 			for (Person p : meeting.getTeam().getMembers()) {
 				addNotification(new Notification(Calendar.getInstance()
 						.getTimeInMillis(), 'w', 'm', newMeeting, p));
 			}
 		}
 
 		return newMeeting;
 	}
 
 	private Alarm getAlarmFromResultSet(ResultSet alarmResultSet)
 			throws SQLException {
 		return new Alarm(alarmResultSet.getInt("alarmID"), alarmResultSet
 				.getString("type").charAt(0), alarmResultSet.getLong("time"),
 				getMeeting(alarmResultSet.getInt("meetingID")));
 	}
 
 	public List<Alarm> getAlarmsOfPerson(String username) throws SQLException {
 		List<Alarm> alarms = new ArrayList<Alarm>();
 		ResultSet alarmsOfPersonRS = dBConn
 				.makeQuery("SELECT * FROM Alarm, Person "
 						+ "WHERE Alarm.username = Person.username;");
 		while (alarmsOfPersonRS.next()) {
 			alarms.add(getAlarmFromResultSet(alarmsOfPersonRS));
 		}
 		return alarms;
 
 	}
 
 	public Meeting getMeeting(int meetingID) throws SQLException {
 		ResultSet rs = dBConn.makeQuery(String.format(""
 				+ "SELECT * FROM Meeting " + "WHERE meetingID = %s",
 				Integer.toString(meetingID)));
 		rs.next();
 		return getMeetingFromResultSet(rs);
 	}
 
 	private Meeting getMeetingFromResultSet(ResultSet rs) throws SQLException {
 		Team team = null;
 		try {
 			int teamID = rs.getInt("teamID");
 			team = getTeam(teamID);
 
 		} catch (Exception e) {
 			// Do nothing. The meeting does not exist, therefore this is
 			// technically an 'appointment'
 		}
 		return new Meeting(rs.getInt("meetingID"), rs.getString("title"),
 				rs.getString("location"), rs.getLong("startTime"),
 				rs.getLong("endTime"), rs.getString("description"), team,
 				new MeetingRoom("100"), getPerson(rs.getString("username")));
 	}
 
 	private Team getTeamFromResultSet(ResultSet rs) throws SQLException {
 		List<Person> members = new ArrayList<Person>();
 		ResultSet membersOf = dBConn
 				.makeQuery("SELECT * FROM Person, memberOF, Team "
 						+ "WHERE memberOF.username = Person.username "
 						+ "AND memberOF.teamID = Team.teamID;");
 		while (membersOf.next()) {
 			members.add(getPersonFromResultSet(membersOf));
 		}
 		return new Team(rs.getInt("teamID"), rs.getString("email"), members);
 	}
 
 	public Team getTeam(int teamID) throws SQLException {
 		ResultSet rs = dBConn.makeQuery(String.format(
 				"SELECT * FROM Team WHERE teamID = %s",
 				Integer.toString(teamID)));
 		rs.next();
 		return getTeamFromResultSet(rs);
 
 	}
 
 	// Needs error handling
 	public boolean addPerson(Person person) throws SQLException {
 		String sql = "INSERT INTO Person (username, passwd, firstname, lastname, email, telephone)";
 		sql += "VALUES ";
 		sql += " ('" + person.getUsername() + "', '" + person.getPassword()
 				+ "', '" + person.getFirstName() + "', '"
 				+ person.getLastName() + "', '" + person.getEmail() + "', "
 				+ Integer.toString(person.getPhone());
 		sql += ");";
 		dBConn.makeUpdate(sql);
 		return true;
 	}
 
 	private Person getPersonFromResultSet(ResultSet rs) throws SQLException {
 		// Remember to call resultset.next() before this method
 		return new Person(rs.getString("email"), rs.getInt("telephone"),
 				rs.getString("firstname"), rs.getString("lastname"),
 				rs.getString("username"), rs.getString("passwd"));
 	}
 
 	public Person getPerson(String username) throws SQLException {
 		ResultSet rs = dBConn.makeQuery(String.format(
 				"SELECT * FROM Person WHERE username = '%s'", username));
 		rs.next();
 		return getPersonFromResultSet(rs);
 	}
 
 	public void deletePerson(String username) throws SQLException {
 		dBConn.makeUpdate(String.format(
 				"DELETE FROM Person WHERE username = '%s'", username));
 	}
 
 	public List<Person> getEveryPerson() throws SQLException {
 		List<Person> persons = new ArrayList<Person>();
 		ResultSet rs = dBConn.makeQuery("SELECT * FROM Person");
 		while (rs.next()) {
 			persons.add(getPersonFromResultSet(rs));
 		}
 		return persons;
 	}
 
 	private Notification getNotificationFromResultSet(ResultSet rs)
 			throws SQLException {
 		long time = rs.getLong("time");
 		char approved = rs.getString("approved").charAt(0);
 		char kind = rs.getString("kind").charAt(0);
 		Meeting meeting = getMeeting(rs.getInt("meetingID"));
 		Person person = getPerson(rs.getString("username"));
 		return new Notification(time, approved, kind, meeting, person);
 	}
 
 	/*
 	 * Returns every notficiation corresponding to the Person.
 	 */
 	public List<Notification> getNotifications(String username)
 			throws SQLException {
 		String sql = String.format("SELECT * FROM notification "
 				+ "WHERE notification.username = %s", username);
 		ResultSet rs = dBConn.makeQuery(sql);
 		List<Notification> notificationList = new ArrayList<Notification>();
 		while (rs.next()) {
 			notificationList.add(getNotificationFromResultSet(rs));
 		}
 		return notificationList;
 	}
 
 	/*
 	 * Returns every notficiation corresponding to the meeting.
 	 */
 	public List<Notification> getNotifications(Meeting meeting)
 			throws SQLException {
 		String sql = String.format("SELECT * FROM notification "
 				+ "WHERE notification.meetingID = %d", meeting.getMeetingID());
 		ResultSet rs = dBConn.makeQuery(sql);
 		List<Notification> notificationList = new ArrayList<Notification>();
 		while (rs.next()) {
 			notificationList.add(getNotificationFromResultSet(rs));
 		}
 		return notificationList;
 	}
 
 	public static void main(String[] args) throws SQLException {
 		DBController dbc = new DBController();
 		Person hakon = dbc.getPerson("haakondi");
 		Person david = dbc.getPerson("davidhov");
 		Person stian = dbc.getPerson("stiven");
 		Meeting meeting = dbc.getMeeting(3);
 
 		List<Person> personList = new ArrayList<Person>();
 		personList.add(david);
 		personList.add(hakon);
 
 		Team team = new Team(-1, "anyone", personList);
 		dbc.addTeam(team);
 		Notification notification = new Notification(10000, 'n', 'n', meeting,
 				stian);
 		dbc.addNotification(notification);
 
 		for (Notification notificatio : dbc.getNotifications(meeting)) {
 			System.out.println(notificatio);
 		}
 	}
 }
