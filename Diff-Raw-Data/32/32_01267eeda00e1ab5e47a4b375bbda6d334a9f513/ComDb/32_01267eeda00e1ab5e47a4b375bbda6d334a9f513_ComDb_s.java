 package db;
 
 import java.io.InputStreamReader;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 import comm.ComResources.CommType;
 
 import models.BugzillaTO;
 import models.Change;
 import models.Item;
 import models.Link;
 import models.Person;
 import models.Item;
 import models.Reply;
 
 public class ComDb extends DbConnection
 {
 	public ComDb() {
 		super();
 	}
 	
 	/**
 	 * Creates a db on the current connection.
 	 * @param dbName
 	 * @return true for success
 	 */
 	public boolean createDb(String dbName) {
 		PreparedStatement s;
 		try {
 			// Drop the DB if it already exists
 			s = conn.prepareStatement("DROP DATABASE IF EXISTS " + dbName + ";");
 			s.execute();
 			
 			// First create the DB.
 			s = conn.prepareStatement("CREATE DATABASE " + dbName + ";");
 			s.execute();
 			
 			// Reconnect to our new database.
 			connect(dbName.toLowerCase());
 			
 			// Now load our default schema in.
			sr.runScript(new InputStreamReader(this.getClass().getResourceAsStream("createdb.sql")));
 			return true;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public int insertItem(Item item) {
 		try 
 		{
 			PreparedStatement s = conn.prepareStatement(
 					"INSERT INTO items (p_id, item_date, item_id, body, title, type) VALUES " +
 					"(?, ?, default, ?, ?, ?)");
 			s.setString(1, Integer.toString(item.getPId()));
 			s.setString(2, item.getItemDate().toString());
 			s.setString(3, item.getBody());
 			s.setString(4, item.getTitle());
 			s.setString(5, item.getCommunicationType().toString());
 			s.execute();
 			
 			return getSequenceValue("items_id_seq"); 
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	public int insertPerson(Person person) {
 		try 
 		{
 			String sql = "SELECT * FROM people WHERE " +
 					"name=? AND email=?"; 
 			String[] parms = {person.getName(), person.getEmail()};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if(!rs.next()) {
 				// Insert
 				PreparedStatement s = conn.prepareStatement(
 						"INSERT INTO people (p_id, name, email) VALUES " +
 						"(default, ?, ?)");
 				s.setString(1, person.getName());
 				s.setString(2, person.getEmail());
 				s.execute();
 
 				return getSequenceValue("people_id_seq");
 			}
 			else {
 				return rs.getInt("p_id");
 			}
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	public int insertThreadUnknownThread(models.Thread thread) {
 		try 
 		{
 			// Insert
 			PreparedStatement s = conn.prepareStatement(
 					"INSERT INTO threads (item_id, thread_id) VALUES " +
 					"(? default)");
 			s.setString(1, Integer.toString(thread.getItemID()));
 			s.execute();
 			
 			return getSequenceValue("threads_id_seq");
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	public boolean insertThreadKnownThread(models.Thread thread) {
 		try 
 		{
 			PreparedStatement s = conn.prepareStatement(
 					"INSERT INTO threads (item_id, thread_id) VALUES " +
 					"(? ?)");
 			s.setString(1, Integer.toString(thread.getItemID()));
 			s.setString(2, Integer.toString(thread.getThreadID()));
 			s.execute();
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean insertReply(Reply reply) {
 		try 
 		{
 			PreparedStatement s = conn.prepareStatement(
 					"INSERT INTO replies (from_item_id, to_item_id) VALUES " +
 					"(?, ?)");
 			s.setString(1, Integer.toString(reply.getFromItemID()));
 			s.setString(2, Integer.toString(reply.getToItemID()));
 			s.execute();
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean insertLink(Link link) {
 		try 
 		{
 			PreparedStatement s = conn.prepareStatement(
 					"INSERT INTO replies (item_id, commit_id, confidence) VALUES " +
 					"(?, ?, ?)");
 			s.setString(1, Integer.toString(link.getItemID()));
 			s.setString(2, link.getCommitID());
 			s.setString(1, Float.toString(link.getConfidence()));
 			s.execute();
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public List<Item> getItemsInThread(int ThreadID) {
 		try 
 		{
 			LinkedList<Item> items = new LinkedList<Item>();
 			String sql = "SELECT p_id, item_date, item_id, body, title, type" +
 					" FROM threads NATURAL JOIN items" +
 					" WHERE thread_id=?"; 
 			String[] parms = {Integer.toString(ThreadID)};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			while(rs.next())
 			{
 				items.add(new Item(rs.getInt("p_id"), rs.getTimestamp("item_date"), rs.getInt("item_id"), 
 						rs.getString("body"), rs.getString("title"), CommType.valueOf(rs.getString("type"))));
 			}
 			return items;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	private int getSequenceValue(String sequence) {
 		try 
 		{
 			// Get the ID
 			String sql = "SELECT currval(?)"; 
 			String[] parms = {sequence};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if(rs.next())
 				return rs.getInt("currval");
 			return -1;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return -1;
 		}
 	}
 }
