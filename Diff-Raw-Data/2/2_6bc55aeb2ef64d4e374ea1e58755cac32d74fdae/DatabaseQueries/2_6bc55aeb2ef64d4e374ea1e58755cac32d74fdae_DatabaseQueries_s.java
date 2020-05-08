 package server.database;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import chronos.Person;
 import chronos.Room;
 import chronos.Singleton;
 import events.AuthEvent;
 import events.CalEvent;
 
 /**
  * Handles all specific queries THIS IS THE ONLY CLASS WITH SQL IN IT!
  * 
  */
 public class DatabaseQueries {
 
 	private final DatabaseConnection db;
 
 	/**
 	 * 
 	 * @param db
 	 */
 	public DatabaseQueries(DatabaseConnection db) {
 		this.db = db;
 	}
 
 	/**
 	 * 
 	 * @param username
 	 * @param password
 	 * @param name
 	 * @param LastLoggedIn
 	 * @return
 	 */
 	public boolean addUser(String username, String password, String name, long LastLoggedIn) {
 		try {
 			db.execute(String.format("insert into person values (%s,%s,%s,%s)", processString(username), ("MD5(" + processString(password) + ")"), processString(name), LastLoggedIn));
 			Singleton.log("successfully added: " + username);
 			return true;
 		} catch (SQLException e) {
 			System.out.println(e);
 			Singleton.log("error adding: " + username);
 			return false;
 		}
 	}
 
 	/**
 	 * 
 	 * @param username
 	 * @return
 	 */
 	public boolean removeUser(String username) {
 		username = processString(username);
 		try {
 			db.execute(String.format("delete from person where username = %s", username));
 			Singleton.log("successfully deleted: " + username);
 			return true;
 		} catch (SQLException e) {
 			System.out.println(e);
 			Singleton.log("error deleting: " + username);
 			return false;
 		}
 	}
 
 	/**
 	 * 
 	 * @param username
 	 * @param fieldToUpdate
 	 * @param newValue
 	 * @return
 	 */
 	public boolean updateUser(String username, String fieldToUpdate, String newValue) {
 		username = processString(username);
 		newValue = processString(newValue);
 		if (fieldToUpdate.equals("password"))
 			newValue = "MD5(" + newValue + ")";
 		try {
 			db.execute(String.format("update person set %s=%s where username = %s", fieldToUpdate, newValue, username));
 			Singleton.log(String.format("successfully updated %s to %s in %s", fieldToUpdate, newValue, username));
 			return true;
 		} catch (SQLException e) {
 			System.out.println(e);
 			Singleton.log("error updating: " + username);
 			return false;
 
 		}
 	}
 
 	/**
 	 * 
 	 * @param users
 	 */
 	public void addMultipleUsers(ArrayList<Person> users) {
 
 		String insertQuery = "insert into person (username,password,name, lastLoggedIn) values (?,?,?,?)";
 		PreparedStatement ps;
 		try {
 			ps = db.makeBatchUpdate(insertQuery);
 
 			for (Person user : users) {
 				try {
 					ps.setString(1, user.getUsername());
 					ps.setString(2, "MD5(derp)");
 					ps.setString(3, user.getName());
 					ps.setString(4, "" + user.getLastLoggedIn());
 					ps.addBatch();
 					Singleton.log("successfully added: " + user.getUsername() + " with name" + user.getName());
 				} catch (SQLException e) {
 					Singleton.log("error adding: " + user.getUsername() + " with name" + user.getName());
 				}
 			}
 			ps.executeBatch();
 			ps.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * Changes lastLoggedIn of user in the database to the specified time.
 	 * 
 	 * @param time
 	 * @param username
 	 */
 	public void setTimestampOfUser(long time, String username) {
 		try {
 			db.execute(String.format("UPDATE Person SET lastLoggedIn=(%s) WHERE username=" + processString(username), time));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Checks if the username and password is correct.
 	 * 
 	 * @param evt
 	 * @return
 	 */
 	public boolean isUsernameAndPassword(AuthEvent evt) {
 		ResultSet rs;
 		String query = "select username, name from person" + " WHERE person.username = " + processString(evt.getUsername().toLowerCase()) + " AND person.password = " + ("MD5(" + processString(evt.getPassword()) + ")");
 		try {
 			rs = db.makeSingleQuery(query);
 			rs.beforeFirst();
 			return rs.next();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public Person getUserByUsername(String username) {
 		ResultSet rs;
 		String query = "SELECT name FROM person WHERE person.username = " + processString(username);
 		try {
 			rs = db.makeSingleQuery(query);
 			rs.first();
 			String name = rs.getString(1);
 			return new Person(username, name);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Returns an ArrayList of chronos.Person
 	 * 
 	 * @return ArrayList
 	 */
 	public ArrayList<Comparable> getUsers() {
 		ArrayList<Comparable> users = new ArrayList<Comparable>();
 		ResultSet rs;
 		String query = "select username,name,lastLoggedIn from person";
 		try {
 			rs = db.makeSingleQuery(query);
 			rs.beforeFirst();
 			while (rs.next()) {
 				String username = rs.getString(1);
 				String name = rs.getString(2);
 				long lastLoggedIn = rs.getLong(3);
 				users.add(new Person(username, name, lastLoggedIn));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return users;
 	}
 
 	/**
 	 * 
 	 * @param evt
 	 */
 	public void addEvent(CalEvent evt) {
 		String insertQuery = "insert into Events (event_ID,title,startTime,duration,description,owner) values (?,?,?,?,?,?);";
 		PreparedStatement ps;
 		boolean addedAvtale = false;
 		try {
 			ps = db.makeBatchUpdate(insertQuery);
 			try {
 				ps.setString(1, "" + evt.getTimestamp());
 				ps.setString(2, evt.getTitle());
 				ps.setString(3, "" + evt.getStart().getTime());
 				ps.setString(4, "" + evt.getDuration());
 				ps.setString(5, evt.getDescription());
 				ps.setString(6, evt.getCreator().getUsername());
 				ps.addBatch();
 				Singleton.log("successfully added: " + evt.getTitle() + " with fields " + evt.getStart().getTime() + " and " + evt.getDuration() + " and " + evt.getDescription());
 			} catch (SQLException e) {
 				Singleton.log("error adding: " + evt.getTitle() + " with fields " + evt.getStart().getTime() + " and " + evt.getDuration() + " and " + evt.getDescription());
 				e.printStackTrace();
 			}
 			ps.executeBatch();
 			ps.close();
 			addedAvtale = true;
 		} catch (SQLException e) {
 			Singleton.log("error executing: " + evt.getTitle() + " with fields " + evt.getStart().getTime() + " and " + evt.getDuration() + " and " + evt.getDescription());
 			addedAvtale = false;
 			e.printStackTrace();
 		}
 
 		/**
 		 * If the apointment is added sucessfully to the database, the
 		 * participants are added and connected.
 		 * */
 		if (addedAvtale) {
 			addParticipants(evt);
 		}
 	}
 
 	/**
 	 * Adds participants from a CalEvent to the DB.
 	 * 
 	 * @param evt
 	 */
 	public void addParticipants(CalEvent evt) {
 		String insertQuery = "insert into participants (username,event_ID,alarm,status) values (?,?,?,?);";
 		PreparedStatement ps;
 		try {
 			ps = db.makeBatchUpdate(insertQuery);
 			for (Person p : evt.getParticipants().values()) {
 				try {
 					ps.setString(1, p.getUsername());
 					ps.setString(2, "" + evt.getTimestamp());
 					ps.setString(3, null);
 					ps.setString(4, "" + p.getStatus().ordinal());
 					ps.addBatch();
 					Singleton.log("successfully added participant: " + p.getUsername());
 				} catch (SQLException e) {
 					Singleton.log("error participant: " + p.getUsername());
 					e.printStackTrace();
 				}
 			}
 			ps.executeBatch();
 			ps.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * @param evt
 	 */
 	public void updateParticipants(CalEvent evt) {
 		String insertQuery = "UPDATE Participants SET alarm=?, status=? WHERE username=? AND event_ID=?;";
 		PreparedStatement ps;
 		String eventID = "" + evt.getTimestamp();
 		try {
 			ps = db.makeBatchUpdate(insertQuery);
 			for (Person p : evt.getParticipants().values()) {
 				try {
 					ps.setString(1, null);
 					ps.setString(2, "" + p.getStatus().ordinal());
 					ps.setString(3, p.getUsername());
 					ps.setString(4, eventID);
 					ps.addBatch();
 					Singleton.log("successfully updated participant \"" + p.getUsername() + "\" to " + p.getStatus()); // TODO
 																														// alarm?
 				} catch (SQLException e) {
 					Singleton.log("error updating " + p.getUsername());
 					e.printStackTrace();
 				}
 			}
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * Returns an ArrayList of all events the person is a participant of, either
 	 * before or after last login.
 	 * 
 	 * @param per
 	 * @param afterLastLogin
 	 * @return ArrayList<Comparable>
 	 */
 	public ArrayList<Comparable> getEventsByParticipant(Person per, boolean afterLastLogin) {
 		ArrayList<Comparable> al = new ArrayList<Comparable>();
 		ResultSet rs;
 		String param;
 		if (afterLastLogin) {
 			param = "<";
 		} else {
			param = ">";
 		}
 		String query = "SELECT events.event_ID, events.title, events.startTime, events.duration, events.description, person.username, person.name, person.lastLoggedIn " + "FROM Events, Participants, Person " + "WHERE Events.event_ID = participants.event_ID AND participants.username = "
 				+ processString(per.getUsername()) + " AND person.username = events.owner AND " + per.getLastLoggedIn() + " " + param + " participants.event_ID ORDER BY events.startTime ASC;";
 		try {
 			rs = db.makeSingleQuery(query);
 			rs.beforeFirst();
 			while (rs.next()) {
 				long event_id = rs.getLong(1);
 				String title = rs.getString(2);
 				long start = rs.getLong(3);
 				int duration = rs.getInt(4);
 				String description = rs.getString(5);
 				String username = rs.getString(6);
 				String name = rs.getString(7);
 				long lastLoggedIn = rs.getLong(8);
 				CalEvent evt = new CalEvent(title, new Date(start), duration, new Person(username, name, lastLoggedIn), description, event_id);
 				evt.setParticipants(getParticipantsByEventId(event_id));
 				al.add(evt);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return al;
 
 	}
 
 	/**
 	 * 
 	 * @param id
 	 * @return HashMap<String, chronos.Person>
 	 */
 	public HashMap<String, Person> getParticipantsByEventId(long id) {
 		HashMap<String, Person> participants = new HashMap<String, Person>();
 		ResultSet rs;
 		String query = "SELECT person.username, name, lastLoggedIn " + "FROM person, participants " + "WHERE participants.event_ID = " + id + " AND participants.username = person.username";
 		try {
 			rs = db.makeSingleQuery(query);
 			rs.beforeFirst();
 			while (rs.next()) {
 				String username = rs.getString(1);
 				String name = rs.getString(2);
 				long lastLoggedIn = rs.getLong(3);
 				participants.put(username, new Person(username, name, lastLoggedIn));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return participants;
 	}
 
 	/**
 	 * 
 	 * @param event
 	 */
 	public void updateCalEvent(CalEvent event) {
 		String insertQuery = "UPDATE Events SET title=?, startTime=?, duration=?, description=?," + "room_ID=(SELECT room_ID FROM Rooms, Events WHERE Events.room_ID = Rooms.room_ID AND Rooms.name=?) " + "WHERE event_ID=?;";
 		PreparedStatement ps;
 		try {
 			ps = db.makeBatchUpdate(insertQuery);
 			try {
 				ps.setString(1, processString(event.getTitle()));
 				ps.setString(2, "" + event.getStart());
 				ps.setString(3, "" + event.getDuration());
 				ps.setString(4, event.getDescription());
 				ps.setString(5, processString(event.getRoom().getName()));
 				ps.setString(6, "" + event.getTimestamp());
 				ps.addBatch();
 				Singleton.log("successfully updated: " + event.getTitle() + " with fields " + event.getStart().getTime() + " and " + event.getDuration() + " and " + event.getDescription());
 			} catch (SQLException e) {
 				Singleton.log("error adding: " + event.getTitle() + " with fields " + event.getStart().getTime() + " and " + event.getDuration() + " and " + event.getDescription());
 				e.printStackTrace();
 			}
 			ps.executeBatch();
 			ps.close();
 		} catch (SQLException e) {
 			Singleton.log("error executing: " + event.getTitle() + " with fields " + event.getStart().getTime() + " and " + event.getDuration() + " and " + event.getDescription());
 			e.printStackTrace();
 		}
 		// deprecated?
 	}
 
 	/**
 	 * 
 	 * @param event
 	 */
 	public void removeCalEvent(CalEvent event) {
 		try {
 			db.execute(String.format("DELETE FROM Events WHERE event_id=%s", "" + event.getTimestamp()));
 			Singleton.log("successfully deleted event " + event.getTitle());
 		} catch (SQLException e) {
 			Singleton.log("error deleting event " + event.getTitle());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * @param person
 	 * @return java.util.Date
 	 */
 	public Date lastLoggedIn(Person person) {
 		ResultSet rs;
 		String query = "SELECT lastLoggedIn FROM Person WHERE username=" + processString(person.getUsername());
 		try {
 			rs = db.makeSingleQuery(query);
 			rs.first();
 			return new Date(rs.getLong(1));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public ArrayList<Comparable> getAvailableRooms(CalEvent event) {
 		ArrayList<Comparable> roomList = new ArrayList<Comparable>();
 		String query = "SELECT Rooms.name, Rooms.description, Rooms.capacity FROM Rooms, Events WHERE Events.room_ID=Rooms.room_ID" + " AND (startTime+duration <" + event.getStart().getTime() + " OR startTime >" + (event.getStart().getTime() + event.getDuration()) + ") GROUP BY events.room_id";
 		ResultSet rs;
 		try {
 			rs = db.makeSingleQuery(query);
 			rs.beforeFirst();
 			while (rs.next()) {
 				String name = rs.getString(1);
 				String desc = rs.getString(2);
 				int cap = rs.getInt(3);
 				roomList.add(new Room(name, cap, desc));
 				Singleton.log("Successfully retrieved all available rooms for \"" + event.getTitle() + "\"");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			Singleton.log("Error retrieving all available rooms for \"" + event.getTitle() + "\"");
 		}
 		return roomList;
 	}
 
 	/**
 	 * Makes the string SQL compatible.
 	 * 
 	 * @param str
 	 * @return String
 	 */
 	private String processString(String str) {
 		str = "'" + str + "'";
 		return str;
 	}
 }
