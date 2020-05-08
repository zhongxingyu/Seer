 package de.rallye.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Savepoint;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 import de.rallye.exceptions.DataException;
 import de.rallye.exceptions.InputException;
 import de.rallye.model.structures.ChatEntry;
 import de.rallye.model.structures.Chatroom;
 import de.rallye.model.structures.Group;
 import de.rallye.model.structures.LoginInfo;
 import de.rallye.model.structures.Node;
 import de.rallye.model.structures.PrimitiveEdge;
 import de.rallye.model.structures.PushMode;
 import de.rallye.model.structures.PushConfig;
 import de.rallye.model.structures.ServerConfig;
 import de.rallye.model.structures.SimpleChatEntry;
 import de.rallye.model.structures.User;
 import de.rallye.model.structures.UserAuth;
 import de.rallye.model.structures.UserInternal;
 
 public class DataAdapter {
 	
 	private static Logger logger = LogManager.getLogger(DataAdapter.class.getName());
 	
 	private ComboPooledDataSource dataSource;
 
 	
 	public DataAdapter(ComboPooledDataSource dataSource) {
 		this.dataSource = dataSource;
 	}
 	
 	/**
 	 * appends a list of string separated by ", "
 	 * @param strings
 	 * @return
 	 */
 	private static String cols(String... strings) {
 		StringBuilder b = new StringBuilder();
 		
 		int l = strings.length-1;
 		for (int i=0; i<=l; i++) {
 			b.append(strings[i]);
 			if (i < l)
 				b.append(", ");
 		}
 		
 		return b.toString();
 	}
 	
 	public void closeConnection() {
 		dataSource.close();
 	}
 	
 	private static void close(Connection con, Statement st, ResultSet rs) {
 		try {
 			if (rs != null) {
 				rs.close();
 			}
 			if (st != null) {
 				st.close();
 			}
 			if (con != null) {
 				con.close();
 			}
 		} catch (Exception e) {
 			logger.catching(e);
 		}
 	}
 	
 	public List<Group> getGroups() throws DataException {
 
 		Statement st = null;
 		Connection con = null;
 		ResultSet rs = null;
 
 		try {
 			con = dataSource.getConnection();
 			st = con.createStatement();
 			rs = st.executeQuery("SELECT "+ cols(Ry.Groups.ID, Ry.Groups.NAME, Ry.Groups.DESCRIPTION) +" FROM "+ Ry.Groups.TABLE);
 
 			List<Group> groups = new ArrayList<Group>();
 			
 			while (rs.next()) {
 				groups.add(new Group(rs.getInt(1), rs.getString(2), rs.getString(3)));
 			}
 
 			return groups;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 	
 	public List<Node> getNodes() throws DataException {
 		Connection con = null;
 		Statement st = null;
 		ResultSet rs = null;
 
 		try {
 			con = dataSource.getConnection();
 			st = con.createStatement();
 			rs = st.executeQuery("SELECT "+ cols(Ry.Nodes.ID, Ry.Nodes.NAME, Ry.Nodes.LAT, Ry.Nodes.LON, Ry.Nodes.DESCRIPTION) +" FROM "+ Ry.Nodes.TABLE);
 
 			ArrayList<Node> nodes = new ArrayList<Node>();
 			
 			while (rs.next()) {
 				nodes.add(new Node(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), rs.getString(5)));
 			}
 
 			return nodes;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 
 	public List<PrimitiveEdge> getEdges() throws DataException {
 		Connection con = null;
 		Statement st = null;
 		ResultSet rs = null;
 
 		try {
 			con = dataSource.getConnection();
 			st = con.createStatement();
 			rs = st.executeQuery("SELECT "+ cols(Ry.Edges.A, Ry.Edges.B, Ry.Edges.TYPE) +" FROM "+ Ry.Edges.TABLE);
 
 			ArrayList<PrimitiveEdge> edges = new ArrayList<PrimitiveEdge>();
 			
 			while (rs.next()) {
 				edges.add(new PrimitiveEdge(rs.getInt(1), rs.getInt(2), rs.getString(3)));
 			}
 
 			return edges;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 	
 	public ServerConfig getServerConfig() throws DataException {
 		Connection con = null;
 		Statement st = null;
 		ResultSet rs = null;
 		
 		try {
 			con = dataSource.getConnection();
 			st = con.createStatement();
 			rs = st.executeQuery("SELECT "+
 					cols(Ry.Config.NAME, Ry.Config.LAT, Ry.Config.LON,
 							Ry.Config.ROUNDS, Ry.Config.ROUND_TIME, "UNIX_TIMESTAMP("+ Ry.Config.START_TIME +")") +" FROM "+ Ry.Config.TABLE);
 			
 			rs.next();
 			
 			ServerConfig res = new ServerConfig(rs.getString(1), rs.getDouble(2), rs.getDouble(3),
 					rs.getInt(4), rs.getInt(5), rs.getLong(6));
 			
 			return res;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 	
 	public int[] isKnownUserAuthorized(String[] login) throws SQLException {
 		Connection con = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		
 		try {
 			String[] usr = login[0].split("@");//0: userID, 1:groupID
 			
 			
 			con = dataSource.getConnection();
 			st = con.prepareStatement("SELECT "+ cols(Ry.Users.ID, Ry.Users.ID_GROUP)
 												+" FROM "+ Ry.Users.TABLE +" AS usr LEFT JOIN "+ Ry.Groups.TABLE +" as grp USING("+ Ry.Users.ID_GROUP +")"
 												+" WHERE "+ Ry.Users.ID +"=? AND "+ Ry.Groups.ID +"=? AND usr."+ Ry.Users.PASSWORD +"=?");
 			st.setString(1, usr[0]);
 			st.setString(2, usr[1]);
 			st.setString(3, login[1]);
 			
 			rs = st.executeQuery();
 			
 			if (rs.next()) {
 				return new int[] { Integer.valueOf(usr[0]), Integer.valueOf(usr[1]) };
 			} else {
 				return null;
 			}
 		} catch (SQLException e) {
 			throw e;
 		} finally {
 			close(con, st, rs);
 		}
 	}
 	
 	public int isNewUserAuthorized(String[] login) throws SQLException {
 		Connection con = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		
 		try {
 			
 			con = dataSource.getConnection();
 			st = con.prepareStatement("SELECT "+ cols(Ry.Users.ID_GROUP)
 												+" FROM "+ Ry.Groups.TABLE
 												+" WHERE "+ Ry.Groups.ID +"=? AND "+ Ry.Groups.PASSWORD +"=?");
 			st.setString(1, login[0]);
 			st.setString(2, login[1]);
 			
 			rs = st.executeQuery();
 			
 			if (rs.next()) {
 				return Integer.valueOf(login[0]);
 			} else {
 				return -1;
 			}
 		} catch (SQLException e) {
 			throw e;
 		} finally {
 			close(con, st, rs);
 		}
 	}
 	
 	@SuppressWarnings("resource")
 	public UserAuth login(int groupID, LoginInfo info) throws DataException, InputException {
 		Connection con = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		
 		try {
 			
 			con = dataSource.getConnection();
 			
 			String password = generateNewUserPassword(groupID);
 			
 			int pushModeID = 0;
 			
 			if (info.pushMode != null) {
				st = con.prepareStatement("SELECT "+ Ry.PushModes.ID +" FROM "+ Ry.PushModes.TABLE +" WHERE "+ Ry.PushModes.NAME +" LIKE ?");
 				st.setString(1, info.pushMode);
 				
 				rs = st.executeQuery();
 				
 				if (!rs.first())
 					throw new InputException("PushMode '"+ info.pushMode +"' not supported");
 				
 				pushModeID = rs.getInt(1);
 				
 				close(null, st, rs);
 			}
 			
 			int userID = -1;
 			
 			if (info.uniqueID != null && info.uniqueID.length() > 3) {
 				st = con.prepareStatement("SELECT "+ cols(Ry.Users.ID, Ry.Users.ID_GROUP, Ry.Users.ID_PUSH_MODE, Ry.Users.NAME, Ry.Users.PASSWORD, Ry.Users.PUSH_ID, Ry.Users.UNIQUE_ID) +
											" FROM "+ Ry.Users.TABLE +" WHERE "+ Ry.Users.UNIQUE_ID +" LIKE ?");
 				st.setString(1, info.uniqueID);
 				rs = st.executeQuery();
 				
 				if (rs.first()) {
 					userID = rs.getInt(1);
 					password = rs.getString(5);
 				}
 				close(null, st, rs);
 			}
 			
 			if (userID > 0) {
				st = con.prepareStatement("UPDATE "+ Ry.Users.TABLE +" SET "+
 						Ry.Users.ID_GROUP +"=?, "+ Ry.Users.ID_PUSH_MODE +"=?, "+ Ry.Users.NAME +"=?, "+ Ry.Users.PASSWORD +"=?, "+ Ry.Users.PUSH_ID +"=? "+
 						"WHERE "+ Ry.Users.ID +"=?");
 				st.setInt(1, groupID);
 				st.setInt(2, pushModeID);
 				st.setString(3, info.name);
 				st.setString(4, password);
 				st.setString(5, info.pushID);
 				st.setInt(6, userID);
 				
 				if (st.executeUpdate() > 0) {
 					return new UserAuth(userID, password);
 				} else {
 					throw new DataException("User could not be reused");
 				}
 			} else {
 				st = con.prepareStatement("INSERT INTO "+ Ry.Users.TABLE +" ("+ cols(Ry.Users.ID_GROUP, Ry.Users.PASSWORD, Ry.Users.NAME, Ry.Users.UNIQUE_ID, Ry.Users.ID_PUSH_MODE, Ry.Users.PUSH_ID) +")" +
 						" VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
 				
 				st.setInt(1, groupID);
 				st.setString(2, password);
 				st.setString(3, info.name);
 				st.setString(4, info.uniqueID);
 				st.setInt(5, pushModeID);
 				st.setString(6, info.pushID);
 				st.execute();
 				
 				rs = st.getGeneratedKeys();
 				
 				if (rs.first()) {
 					return new UserAuth(rs.getInt(1), password);
 				} else {
 					throw new DataException("User could not be created");
 				}
 			}
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 	
 	private String generateNewUserPassword(int groupID) {
 		long pw = System.currentTimeMillis() + groupID + new Random().nextInt();
 		
 		return Long.toHexString(pw);
 	}
 	
 	public boolean logout(int groupID, int userID) throws DataException {
 		Connection con = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		
 		try {
 			con = dataSource.getConnection();
 //			st = con.prepareStatement("DELETE FROM "+ Ry.Users.TABLE +
 //					" WHERE "+ Ry.Users.ID +"=?");
 			
 			st = con.prepareStatement("UPDATE "+ Ry.Users.TABLE +" SET "+ Ry.Users.ID_PUSH_MODE +"=? "+//TODO: set status=offline?
 										"WHERE "+ Ry.Users.ID +"=?");
 			
 			st.setInt(1, 0);
 			st.setInt(2, userID);
 			st.executeUpdate();
 			
 			return true;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 	
 	public boolean hasRightsForChatroom(int groupID, int roomID) throws DataException {
 		Connection con = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		
 		try {
 			con = dataSource.getConnection();
 			st = con.prepareStatement("SELECT *"
 					+" FROM "+ Ry.Groups_Chatrooms.TABLE
 					+" WHERE "+ Ry.Groups_Chatrooms.ID_GROUP +"=? AND "+ Ry.Groups_Chatrooms.ID_CHATROOM +"=?");
 			st.setInt(1, groupID);
 			st.setInt(2, roomID);
 			rs = st.executeQuery();
 			
 			rs = st.executeQuery();
 			
 			return rs.first();
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 
 	public List<Chatroom> getChatrooms(int groupID) throws DataException {
 		Connection con = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		
 		try {
 			con = dataSource.getConnection();
 			st = con.prepareStatement("SELECT "+ cols(Ry.Chatrooms.ID, Ry.Chatrooms.NAME)
 					+" FROM "+ Ry.Groups_Chatrooms.TABLE +" LEFT JOIN "+ Ry.Chatrooms.TABLE +" USING ("+ Ry.Chatrooms.ID +")"
 					+"WHERE "+ Ry.Groups_Chatrooms.ID_GROUP +"=?");
 			st.setInt(1, groupID);
 			rs = st.executeQuery();
 			
 			List<Chatroom> chatrooms = new ArrayList<Chatroom>();
 			
 			while (rs.next()) {
 				chatrooms.add(new Chatroom(rs.getInt(1), rs.getString(2)));
 			}
 			
 			return chatrooms;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 
 	public List<ChatEntry> getChats(int roomID, long timestamp, int groupID) throws DataException {
 		Connection con = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			con = dataSource.getConnection();
 	
 			//TODO: throw Unauthorized if no rights to access chatroom
 			st = con.prepareStatement("SELECT "+ cols(Ry.Chats.ID, Ry.Messages.MSG, "UNIX_TIMESTAMP("+Ry.Chats.TIMESTAMP+")", Ry.Chats.ID_USER, "chats."+Ry.Chats.ID_GROUP, Ry.Chats.ID_PICTURE)+
 										" FROM "+ Ry.Chats.TABLE +" AS chats"+
 										" LEFT JOIN "+ Ry.Messages.TABLE +" AS msg USING ("+ Ry.Chats.ID_MESSAGE +") "+
 										"LEFT JOIN "+ Ry.Groups_Chatrooms.TABLE +" AS gc USING ("+ Ry.Chats.ID_CHATROOM +") "+
 										"WHERE chats."+Ry.Chats.ID_CHATROOM +" =? "+
 										"AND gc."+Ry.Groups_Chatrooms.ID_GROUP +" =? "+
 										"AND "+ Ry.Chats.TIMESTAMP +" >=FROM_UNIXTIME(?) "+
 										"ORDER BY "+ Ry.Chats.TIMESTAMP);
 
 
 			st.setInt(1, roomID);
 			st.setInt(2, groupID);
 			st.setLong(3, timestamp);
 			rs = st.executeQuery();
 
 			List<ChatEntry> chats = new ArrayList<ChatEntry>();
 			
 			while (rs.next()) {
 				chats.add(new ChatEntry(rs.getInt(1), rs.getString(2), rs.getLong(3), rs.getInt(5), rs.getInt(4), rs.getInt(6)));
 			}
 
 			return chats;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 
 	public ChatEntry addChat(SimpleChatEntry chat, int roomID, int groupID, int userID) throws DataException {
 		Connection con = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		Savepoint transaction = null;
 		boolean success = false;
 		
 		try {
 			con = dataSource.getConnection();
 			con.setAutoCommit(false);
 			
 			transaction = con.setSavepoint();
 
 			//Insert message
 			st = con.prepareStatement("INSERT INTO "+ Ry.Messages.TABLE +" ("+ Ry.Messages.MSG +") VALUES (?)", Statement.RETURN_GENERATED_KEYS);
 			st.setString(1, chat.message);
 			st.execute();
 
 			rs = st.getGeneratedKeys();
 			rs.first();
 
 			int msgID = rs.getInt(1);
 
 			rs.close();
 			st.close();
 			
 			//TODO: check for existance of picture
 			//Insert Chat
 			st = con.prepareStatement("INSERT INTO "+ Ry.Chats.TABLE +" ("+
 					cols(Ry.Chats.ID_CHATROOM, Ry.Chats.ID_USER, Ry.Chats.ID_GROUP, Ry.Chats.ID_MESSAGE, Ry.Chats.ID_PICTURE)
 					+") VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
 			
 			
 			st.setInt(1, roomID);
 			st.setInt(2, userID);
 			st.setInt(3, groupID);
 			st.setInt(4, msgID);
 			if (chat.hasPicture()) {
 				st.setInt(5, chat.pictureID);
 			} else {
 				st.setNull(5, java.sql.Types.INTEGER);
 			}
 			
 			st.execute();
 			
 			rs = st.getGeneratedKeys();
 			rs.next();
 			int chatID = rs.getInt(1);
 			
 			rs.close();
 			st.close();
 			
 			st = con.prepareStatement("SELECT UNIX_TIMESTAMP("+ Ry.Chats.TIMESTAMP +") FROM "+ Ry.Chats.TABLE +" WHERE "+ Ry.Chats.ID +"=?");
 			st.setInt(1, chatID);
 			rs = st.executeQuery();
 			rs.first();
 			long timestamp = rs.getLong(1);
 			
 			ChatEntry newChat = new ChatEntry(chatID, chat.message, timestamp, groupID, userID, chat.pictureID);
 			
 			success = true;
 			//TODO: notifiyNewChatEntry()
 			
 			return newChat;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			try {
 				if (success) {
 					con.commit();
 				} else {
 					con.rollback(transaction);
 					logger.error("Rolling back new chat");
 				}
 			} catch (Exception e) {}
 			
 			close(con, st, rs);
 		}
 	}
 
 	public void setPushConfig(int groupID, int userID, PushConfig push) throws DataException {
 		Connection con = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		
 		try {
 			con = dataSource.getConnection();
 			st = con.prepareStatement("UPDATE "+ Ry.Users.TABLE +" SET "+ Ry.Users.PUSH_ID +"=?, " + Ry.Users.ID_PUSH_MODE +
										"=(SELECT "+ Ry.PushModes.ID +" FROM "+ Ry.PushModes.TABLE +" WHERE "+ Ry.PushModes.NAME +" LIKE ?)"+
 										" WHERE "+ Ry.Users.ID +"=?");
 			
 			st.setString(1, push.pushID);
 			st.setString(2, push.pushMode);
 			st.setInt(3, userID);
 			int count = st.executeUpdate();
 			
 			if (count > 1) {
 				throw new DataException("Impossible!!!!");
 			} else if (count < 1) {
 				throw new DataException("Push Configuration change failed");
 			}
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 	
 	public PushConfig getPushConfig(int groupID, int userID) throws DataException {
 		Connection con = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		
 		try {
 			con = dataSource.getConnection();
 			st = con.prepareStatement("SELECT "+ cols("push."+Ry.PushModes.NAME, "usr."+Ry.Users.PUSH_ID) +
 										" FROM "+ Ry.Users.TABLE +" AS usr LEFT JOIN "+ Ry.PushModes.TABLE +" AS push USING("+ Ry.Users.ID_PUSH_MODE +")"+
 										" WHERE "+ Ry.Users.ID +"=? "+ Ry.Users.ID_GROUP +"=?");
 			
 			st.setInt(1, userID);
 			st.setInt(2, groupID);
 			rs = st.executeQuery();
 			
 			if (!rs.first()) {
 				throw new DataException("User is not in group (should never have gotten here)");
 			}
 			
 			PushConfig cfg = new PushConfig(rs.getString(2), rs.getString(1));
 			return cfg;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 
 	public List<PushMode> getPushModes() throws DataException {
 		Statement st = null;
 		Connection con = null;
 		ResultSet rs = null;
 
 		try {
 			con = dataSource.getConnection();
 			st = con.createStatement();
 			rs = st.executeQuery("SELECT "+ cols(Ry.PushModes.ID, Ry.PushModes.NAME) +" FROM "+ Ry.PushModes.TABLE);
 
 			List<PushMode> modes = new ArrayList<>();
 			
 			while (rs.next()) {
 				modes.add(new PushMode(rs.getInt(1), rs.getString(2)));
 			}
 
 			return modes;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 
 	public List<UserInternal> getMembers(int groupID) throws DataException {
 		PreparedStatement st = null;
 		Connection con = null;
 		ResultSet rs = null;
 
 		try {
 			con = dataSource.getConnection();
 			st = con.prepareStatement("SELECT "+ cols(Ry.Users.ID, Ry.Users.NAME, Ry.Users.ID_PUSH_MODE, Ry.Users.PUSH_ID) +" FROM "+ Ry.Users.TABLE +" WHERE "+ Ry.Users.ID_GROUP +"=?");
 			st.setInt(1, groupID);
 			
 			rs = st.executeQuery();
 
 			List<UserInternal> users = new ArrayList<>();
 			
 			while (rs.next()) {
 				users.add(new UserInternal(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
 			}
 
 			return users;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 	
 	public List<UserInternal> getChatroomMembers(int roomID) throws DataException {
 		PreparedStatement st = null;
 		Connection con = null;
 		ResultSet rs = null;
 
 		try {
 			con = dataSource.getConnection();
 			st = con.prepareStatement("SELECT "+ cols(Ry.Users.ID, Ry.Users.NAME, Ry.Users.ID_PUSH_MODE, Ry.Users.PUSH_ID) +
 									" FROM "+ Ry.Users.TABLE +
 									" LEFT JOIN "+ Ry.Groups_Chatrooms.TABLE +" USING("+ Ry.Groups_Chatrooms.ID_GROUP +")"+
 									" WHERE "+ Ry.Groups_Chatrooms.ID_CHATROOM +"=?");
 			st.setInt(1, roomID);
 			
 			rs = st.executeQuery();
 
 			List<UserInternal> users = new ArrayList<>();
 			
 			while (rs.next()) {
 				users.add(new UserInternal(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
 			}
 
 			return users;
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 
 	public int assignNewPictureID(int userID) throws DataException {
 		PreparedStatement st = null;
 		Connection con = null;
 		ResultSet rs = null;
 
 		try {
 			con = dataSource.getConnection();
 			st = con.prepareStatement("INSERT INTO "+ Ry.Pictures.TABLE +" ("+ cols(Ry.Pictures.ID_USER) +") VALUES (?)", Statement.RETURN_GENERATED_KEYS);
 			st.setInt(1, userID);
 			
 			st.execute();
 			
 			rs = st.getGeneratedKeys();
 			
 			if (rs.first()) {
 				return rs.getInt(1);
 			} else {
 				throw new DataException("No new ID assigned by DB");
 			}
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 
 	public void editChatAddPicture(int chatID, int pictureID) throws DataException {
 		PreparedStatement st = null;
 		Connection con = null;
 		ResultSet rs = null;
 		
 		try {
 			con = dataSource.getConnection();
 			st = con.prepareStatement("UPDATE "+ Ry.Chats.TABLE +" SET "+ Ry.Chats.ID_PICTURE +"=? WHERE "+ Ry.Chats.ID +"=?");
 			st.setInt(1, pictureID);
 			st.setInt(2, chatID);
 			
 			if (st.executeUpdate() > 0)
 				return;
 			else
 				throw new DataException("Failed to add picture to Chat");
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 
 	public void updatePushIds(HashMap<String, String> changes) throws DataException {
 		PreparedStatement st = null;
 		Connection con = null;
 		ResultSet rs = null;
 		
 		try {
 			con = dataSource.getConnection();
 			st = con.prepareStatement("UPDATE "+ Ry.Users.TABLE +" SET "+ Ry.Users.PUSH_ID +"=? WHERE "+ Ry.Users.PUSH_ID +"=?");
 			
 			for (Entry<String, String> pair: changes.entrySet()) {
 				st.setString(1, pair.getValue());
 				st.setString(2, pair.getKey());
 				
 				st.executeUpdate();
 			}
 		} catch (SQLException e) {
 			throw new DataException(e);
 		} finally {
 			close(con, st, rs);
 		}
 	}
 }
