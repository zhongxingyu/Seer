 package org.dentleisen.appening2;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.json.simple.JSONObject;
 
 import twitter4j.Tweet;
 
 public class Message {
 	private static Logger log = Logger.getLogger(Message.class);
 
 	private String id;
 	private String user;
 	private Date created;
 	private String text;
 
 	public Message(String id, String user, Date created, String text) {
 		this.id = id;
 		this.user = user;
 		this.created = created;
 		this.text = text;
 	}
 
 	public static Message fromTwitterJson(JSONObject jMessage) {
 		String id = (String) jMessage.get("id_str");
 		Date created;
 		try {
 			created = Utils.getTwitterDate((String) jMessage.get("created_at"));
 		} catch (ParseException e) {
 			created = Calendar.getInstance().getTime();
 		}
 		String user = (String) jMessage.get("from_user");
 		String text = (String) jMessage.get("text");
 
 		return new Message(id, user, created, text);
 	}
 
 	public static Message fromTwitter4JObject(Tweet t) {
 		String id = Long.toString(t.getId());
 		return new Message(id, t.getFromUser(), t.getCreatedAt(), t.getText());
 	}
 
 	public void save() {
 		try {
 			Connection c = Utils.getConnection();
 			PreparedStatement s = c
					.prepareStatement("INSERT DELAYED IGNORE INTO `messages` (`id`,`created`,`user`,`text`) VALUES (?,?,?,?)");
 
 			s.setString(1, getId());
 			s.setString(2, Utils.sqlDateTimeFormat.format(getCreated()));
 			s.setString(3, getUser());
 			s.setString(4, getText());
 
 			s.executeUpdate();
 			s.close();
 			c.close();
 		} catch (SQLException e) {
 			log.warn("Failed to save mention " + toString() + " to db", e);
 		}
 	}
 
 	public static String getLastId() {
 		String id = null;
 		try {
 			Connection c = Utils.getConnection();
 			Statement s = c.createStatement();
 			ResultSet rs = s
 					.executeQuery("SELECT `id` FROM `messages` ORDER BY `created` DESC LIMIT 1;");
 			if (rs.next()) {
 				id = rs.getString("id");
 			}
 
 			rs.close();
 			s.close();
 			c.close();
 		} catch (SQLException e) {
 			log.warn("Failed to load last id from db", e);
 
 		}
 		return id;
 	}
 
 	public static List<Message> loadMessages(Date startDate) {
 		List<Message> messages = new ArrayList<Message>();
 		try {
 			Connection c = Utils.getConnection();
 			Statement s = c.createStatement();
 			ResultSet rs = s
 					.executeQuery("SELECT * FROM `messages` ORDER BY `created` DESC");
 			while (rs.next()) {
 				messages.add(Message.fromSqlResult(rs));
 			}
 
 			rs.close();
 			s.close();
 			c.close();
 		} catch (SQLException e) {
 			log.warn("Failed to load messages from db", e);
 
 		}
 		return messages;
 	}
 
 	public static long messagesCount(String key, double minScore) {
 		long ret = 0;
 		try {
 			Connection c = Utils.getConnection();
 			PreparedStatement s = c
 					.prepareStatement("SELECT COUNT(*) FROM (SELECT `id`, MATCH (`text`) AGAINST (? IN NATURAL LANGUAGE MODE) AS score FROM `messages` WHERE MATCH (`text`) AGAINST (? IN NATURAL LANGUAGE MODE) ) AS `scores` WHERE `score`> ?;");
 			s.setString(1, key);
 			s.setString(2, key);
 			s.setDouble(3, minScore);
 			// TODO: date ranges
 
 			ResultSet rs = s.executeQuery();
 
 			if (rs.next()) {
 				ret = rs.getLong(1);
 			}
 
 			rs.close();
 			s.close();
 			c.close();
 		} catch (SQLException e) {
 			log.warn("Failed to load message count for keyword '" + key
 					+ "' from db", e);
 
 		}
 		return ret;
 	}
 
 	public static Message fromSqlResult(ResultSet rs) throws SQLException {
 		Date d = Calendar.getInstance().getTime();
 		try {
 			d = Utils.sqlDateTimeFormat.parse(rs.getString("created"));
 		} catch (ParseException e) {
 			log.warn("Unable to parse date", e);
 		}
 
 		return new Message(rs.getString("id"), rs.getString("user"), d,
 				rs.getString("text"));
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public Date getCreated() {
 		return created;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	@Override
 	public String toString() {
 		return text.substring(0, Math.min(text.length(), 100)) + " - "
 				+ created;
 	}
 
 	public static void main(String[] args) {
 		log.info(Message.messagesCount("hard rock cafe", 12.0));
 	}
 
 	@SuppressWarnings("unchecked")
 	public Object toJSON() {
 		JSONObject msgObj = new JSONObject();
 		msgObj.put("id", getId());
 		msgObj.put("created",
 				Utils.jsonDateFormat.format(getCreated()));
 		msgObj.put("user", getUser());
 		msgObj.put("text", getText());
 		return msgObj;
 	}
 }
