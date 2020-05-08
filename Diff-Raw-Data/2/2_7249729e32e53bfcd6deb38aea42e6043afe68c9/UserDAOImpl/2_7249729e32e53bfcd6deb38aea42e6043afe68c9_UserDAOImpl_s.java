 package com.nearme;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 
 public class UserDAOImpl implements UserDAO {
 	
 	private static Logger logger = Logger.getLogger(UserDAOImpl.class);
 
 	/* In the real world I'd be using an ORM here, but that feels like one too many things
 	 * to shove at the team during this project. 
 	 * 
 	 * Actually I'd probably write the thing in Groovy or Perl, but that'd be even crueller.
 	 * 
 	 * TODO: wrap these big sets of queries in transactions.
 	 * And check the database is set up to use them...
 	 */
 	private static final String READ_ID_SQL = "SELECT user.*, idHash.hash FROM user,idHash WHERE user.id = ? AND idHash.id = user.hashId";
 	private static final String READ_HASH_SQL = "SELECT user.*, idHash.hash FROM user,idHash WHERE idHash.hash = ? AND idHash.id = user.hashId";
 	private static final String READ_DEVICE_SQL = "SELECT user.*, idHash.hash FROM user,idHash WHERE user.deviceId = ? AND idHash.id = user.hashId";
 	private static final String READ_AB_SQL = "SELECT ab.id, ab.name, ab.permission, ih.id, ih.hash FROM addressBook ab, addressBookHashMatcher abhm, idHash ih WHERE ab.ownerId = ? AND abhm.addressBookId = ab.id AND abhm.hashId = ih.id ORDER BY ab.name";
 	private static final String NEAREST_SQL = "SELECT u2.*, ab.name, ( 6371 * acos( cos( radians(?) ) * cos( radians( u2.latitude ) ) * cos( radians( u2.longitude ) - radians(?) ) + sin( radians(?) ) * sin( radians( u2.latitude ) ) ) ) AS distance FROM user u, addressBook ab, addressBookHashMatcher abhm, user u2 WHERE u.id = ? AND u.id = ab.ownerId AND abhm.addressBookId = ab.id AND abhm.hashId = u2.hashId  HAVING distance < ? ORDER BY distance";
 
 	private static final String USER_INSERT_SQL = "INSERT INTO user (deviceId, hashId, latitude, longitude, lastReport) VALUES (?,?,?,?,?)";
 	private static final String USER_UPDATE_POSITION_SQL = "UPDATE user SET latitude = ?, longitude = ?, lastReport = ? WHERE id = ?";
 	private static final String USER_UPDATE_IDHASH_SQL = "UPDATE user SET hashId = ? WHERE id = ?";
 	
 	private static final String IDHASH_FIND_SQL = "SELECT id FROM idHash WHERE hash = ?";
 	private static final String IDHASH_INSERT_SQL = "INSERT INTO idHash (hash) VALUES (?)";
 	
 	private static final String BOOK_DELETE_SQL = "DELETE FROM addressBook WHERE ownerId = ?";
 	private static final String HASHMATCH_DELETE_SQL = "DELETE ah.* from addressBookHashMatcher ah, addressBook ab WHERE ab.ownerId = ? AND ah.addressBookId = ab.id";
 	private static final String BOOK_INSERT_SQL = "INSERT INTO addressBook (ownerId, name, permission) VALUES (?,?,?)";
 	private static final String MATCH_INSERT_SQL = "INSERT INTO addressBookHashMatcher (hashId, addressBookId) VALUES (?,?)";
 
 	private static final String IDHASH_LIST_SQL = "SELECT ih.id, ih.hash from addressBook ab, addressBookHashMatcher hm, idHash ih WHERE ih.id = hm.hashId AND hm.addressBookId = ab.id and ab.ownerId = ? AND ab.permission = ?";
 
 	private static final String PERMS_RESET_SQL = "UPDATE addressBook SET permission = ? WHERE ownerId = ?";
	private static final String PERMS_UPDATE_SQL = "UPDATE addressBook ab SET permission = ? WHERE ownerID = ? AND id IN (SELECT abHM.addressBookId FROM addressBookHashMatcher abhm, idHash h WHERE abhm.hashId = h.id AND h.hash IN (?,?,?,?,?,?,?,?,?,?))";
 	
 	private static final String USER_DELETE_SQL = "DELETE FROM user WHERE id = ?";
 	
 	/* See the comment for setPermissions() to understand what this is, and why it needs to match the 
 	 * number of question-marks in the subquery of PERMS_UPDATE_SQL
 	 */
 	private static final int PERM_UPDATE_COUNT = 10;
 	
 	private DataSource dataSource = null;
 	
 	public UserDAOImpl(DataSource d) {
 		this.dataSource = d;
 	}
 
 	/**
 	 * Read a User from the database, identified by their unique ID
 	 */
 	
 	@Override
 	public User read(int i) throws SQLException {
 		Connection c = null;
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 		try {
 			c = dataSource.getConnection();
 			pst = c.prepareStatement(READ_ID_SQL);
 			pst.setInt(1, i);
 			rs = pst.executeQuery();
 			if (rs.next()) return userFrom(rs);
 			else return null;
 		} finally {
 			if (rs!=null) rs.close();
 			if (pst!=null) pst.close();
 			if (c!=null) c.close();
 		}
 	}
 	
 	/**
 	 * Helper method, takes a ResultSet and returns the first User from it
 	 * 
 	 * @param rs
 	 * @return
 	 * @throws SQLException
 	 */
 	
 	private User userFrom(ResultSet rs) throws SQLException {
 		User u = new User(rs.getInt("user.id"),
 					rs.getString("user.deviceId"),
 					rs.getString("idHash.hash"));
 		
 		if (rs.getTimestamp("user.lastReport")!=null)
 			u.setLastPosition(new Position(rs.getDouble("user.latitude"), rs.getDouble("user.longitude"), new java.util.Date(rs.getTimestamp("user.lastReport").getTime())));
 		
 		return u;
 	}
 
 	/**
 	 * Read a User from the database, identified by the Hash of their MSISDN
 	 */
 
 	public User readByHash(String string) throws SQLException {
 		Connection c = null;
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 		try {
 			c = dataSource.getConnection();
 			pst = c.prepareStatement(READ_HASH_SQL);
 			pst.setString(1, string);
 			rs = pst.executeQuery();
 			if (rs.next()) return userFrom(rs);
 			else return null;
 		} finally {
 			if (rs!=null) rs.close();
 			if (pst!=null) pst.close();
 			if (c!=null) c.close();
 		}
 	}
 
 	/**
 	 * Read a User from the database, identified by their unique device ID
 	 */
 
 	@Override
 	public User readByDeviceId(String string) throws SQLException {
 		Connection c = null;
 		PreparedStatement pst = null;
 		try {
 			c = dataSource.getConnection();
 			pst = c.prepareStatement(READ_DEVICE_SQL);
 			pst.setString(1, string);
 			ResultSet rs = pst.executeQuery();
 			if (rs.next()) return userFrom(rs);
 			else return null;
 		} finally {
 			if (pst!=null) pst.close();
 			if (c!=null) c.close();
 		}
 	}
 
 	/**
 	 * Read the address book for a user from the database, identified by their unique ID
 	 */
 
 	@Override
 	public List<AddressBookEntry> getAddressBook(int i) throws SQLException {
 		
 		User u = read(i);
 		if (u==null) return null;
 		
 		Connection c = null;
 		PreparedStatement pst = null;
 		ArrayList<AddressBookEntry> ret = new ArrayList<AddressBookEntry>();
 		try {
 			c = dataSource.getConnection();
 			pst = c.prepareStatement(READ_AB_SQL);
 			pst.setInt(1, i);
 			ResultSet rs = pst.executeQuery();
 			int lastId = -1;
 			AddressBookEntry abe = null;
 			ArrayList<IdentityHash> hashes = null;
 			
 			while (rs.next()) {
 				// rows of ab.id, ab.name, ab.permission, ih.id, ih.hash
 				
 				IdentityHash ih = new IdentityHash(rs.getInt("ih.id"), rs.getString("ih.hash"));
 				if (rs.getInt("ab.id")!=lastId) {
 					hashes = new ArrayList<IdentityHash>();
 					abe = new AddressBookEntry(rs.getInt("ab.id"), u, rs.getString("ab.name"), rs.getInt("ab.permission"), hashes);
 					ret.add(abe);
 				}
 				hashes.add(ih);
 				lastId = rs.getInt("ab.id");
 			}
 		} finally {
 			if (pst!=null) pst.close();
 			if (c!=null) c.close();
 		}
 		return ret;
 	}
 
 	@Override
 	public List<Poi> getNearestUsers(User u, int radius) throws SQLException {
 		List<Poi> ret = new ArrayList<Poi>();
 		if (u==null) {
 			logger.debug("getNearestUsers() u=null,radius="+radius);
 			return ret; /* By definition we have no nearby friends for users we don't know! */
 		}
 		if (u.getLastPosition()==null) {
 			logger.debug("getNearestUsers() u="+u.getId()+",lastPosition=null,radius="+radius);
 			return ret; /* By definition we have no nearby friends for users we don't know a last position for */
 			
 		}
 		logger.debug("getNearestUsers() lat="+u.getLastPosition().getLatitude()+",long="+u.getLastPosition().getLongitude()+",id="+u.getId()+",radius="+radius);
 		Connection c = null;
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 		
 		try {
 			c = dataSource.getConnection();
 			pst = c.prepareStatement(NEAREST_SQL);
 			pst.setDouble(1, u.getLastPosition().getLatitude());
 			pst.setDouble(2, u.getLastPosition().getLongitude());
 			pst.setDouble(3, u.getLastPosition().getLatitude());
 			pst.setInt(4, u.getId());
 			pst.setInt(5, radius);
 
 			rs = pst.executeQuery();
 			while (rs.next()) {
 				Poi p = new Poi(rs.getString("ab.name"), rs.getDouble("u2.latitude"), rs.getDouble("u2.longitude"), PoiType.FRIEND, 0);
 				ret.add(p);
 			}
 		} finally {
 			if (rs!=null) rs.close();
 			if (pst!=null) pst.close();
 			if (c!=null) c.close();
 		}
 		return ret;
 	}
 	
 	/**
 	 * Writes the given User into the database, either inserting or updating, depending on whether a
 	 * record for them already exists.
 	 */
 
 	@Override
 	public User write(User u) throws SQLException {
 
 		User exists = readByDeviceId(u.getDeviceId());
 
 		Connection c = null;
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 		
 		try {
 			c = dataSource.getConnection();
 
 			/* If the User exists, we must:
 			 * 1. If there's a lat and long in the User passed in, update the lat, long and time of their last position report, otherwise do nothing.
 			 * 2. If their MSISDN IdentityHash has changed,
 			 * 2a. Either look to see if it's changed to something in our database, and get the ID of that something, or
 			 * 2b. If it's completely new, add it and get its ID, then
 			 * 2c. Update the hashId of their user record to point to this new IdentityHash
 			 * 
 			 * It's meaningless to update their deviceId, as it's the way we found them.
 			 */
 
 			if (exists!=null) {
 				logger.debug("write() user already exists");
 				if ((u.getLastPosition()!=null) && (!u.getLastPosition().equals(exists.getLastPosition()))) {
 					logger.debug("write() updating last known position");
 					pst = c.prepareStatement(USER_UPDATE_POSITION_SQL);
 					pst.setDouble(1, u.getLastPosition().getLatitude());
 					pst.setDouble(2, u.getLastPosition().getLongitude());
 					pst.setTimestamp(3, new Timestamp(u.getLastPosition().getWhen().getTime()));
 					pst.setInt(4, exists.getId());
 					pst.executeUpdate();
 					pst.close();
 				} else
 					logger.debug("write() no position updated; null or unchanged, old="+exists.getLastPosition()+",current="+u.getLastPosition());
 
 				
 				if (!u.getMsisdnHash().equals(exists.getMsisdnHash())) {
 					
 					/* update the ID hash to this new value */
 					int idHash = findOrCreateIdHash(c, u.getMsisdnHash());
 					pst = c.prepareStatement(USER_UPDATE_IDHASH_SQL);
 					pst.setInt(1, idHash);
 					pst.setInt(2, exists.getId());
 					pst.executeUpdate();
 					pst.close();
 					
 				}
 				
 				u.setId(exists.getId());
 			} else {
 				/* Otherwise, this is a new user, so
 				 * 
 				 * 1. Add their idHash if necessary, getting its ID
 				 * 2. Add a User record for them
 				 * 3. Get the ID of this User record, and update the User passed in with it
 				 */
 				
 				int idHash = findOrCreateIdHash(c, u.getMsisdnHash());
 				pst = c.prepareStatement(USER_INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
 				pst.setString(1, u.getDeviceId());
 				pst.setInt(2, idHash);
 				if (u.getLastPosition()!=null) {
 					pst.setDouble(3, u.getLastPosition().getLatitude());
 					pst.setDouble(4, u.getLastPosition().getLongitude());
 					pst.setTimestamp(5, new Timestamp(u.getLastPosition().getWhen().getTime()));
 				} else {
 					pst.setNull(3, Types.DOUBLE);
 					pst.setNull(4, Types.DOUBLE);
 					pst.setNull(5, Types.TIMESTAMP);
 				}
 				pst.executeUpdate();
 
 				/* If they've just been added to the database for the first time, it will
 				 * have allocated them an ID number automatically. Fetch this and set
 				 * the ID class variable to it
 				 */
 				rs = pst.getGeneratedKeys();
 				if (rs.next()) u.setId(rs.getInt(1));
 				
 			}
 			
 		} finally {
 			if (rs!=null) rs.close();
 			if (pst!=null) pst.close();
 			if (c!=null) c.close();
 		}
 	
 		return u;
 	}
 	
 	/**
 	 * Give an idHash and a Connection, finds the hash in the database and returns its ID,
 	 * or if it isn't there, adds it and returns its ID.
 	 * 
 	 * @param c
 	 * @param hash
 	 * @return
 	 * @throws SQLException
 	 */
 	
 	private int findOrCreateIdHash(Connection c, String hash) throws SQLException {
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 		try {
 			pst = c.prepareStatement(IDHASH_FIND_SQL);
 			pst.setString(1, hash);
 			rs = pst.executeQuery();
 			if (rs.next()) return rs.getInt(1);
 			
 			rs.close();
 			pst.close();
 			
 			pst = c.prepareStatement(IDHASH_INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
 			pst.setString(1, hash);
 			pst.executeUpdate();
 			rs = pst.getGeneratedKeys();
 			if (rs.next()) return rs.getInt(1);
 		} finally {
 			if (rs!=null) rs.close();
 			if (pst!=null) pst.close();
 		}
 		
 		// It should be impossible to reach this point
 		throw new RuntimeException("couldn't add an IdentityHash");
 	}
 
 	private void deleteAddressBook(Connection c, int userId) throws SQLException {
 		PreparedStatement pst = null;
 		try {
 			/* Delete all entries for this addressBook from the addressBookHashMatcher table */
 	
 			pst = c.prepareStatement(HASHMATCH_DELETE_SQL);
 			pst.setInt(1, userId);
 			pst.executeUpdate();
 	
 			/* Delete all entries for this user from the addressBook table */
 			
 			pst = c.prepareStatement(BOOK_DELETE_SQL);
 			pst.setInt(1, userId);
 			pst.executeUpdate();
 		} finally {
 			if (pst!=null) pst.close();
 		}
 
 	}
 	
 	@Override
 	public boolean setAddressBook(int id, List<AddressBookEntry> book) throws SQLException {
 		
 		/* No such user? Not a valid request, so return false */
 		User u = read(id);
 		if (u==null) return false;
 
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 		Connection c = null;
 
 		/* If they exist, delete their old address book and any orphaned hashes;
 		 * If this were a real-world app I'd consider this kind of approach kludgy;
 		 * a proper syncing mechanism would be quicker for migrating small changes
 		 * from client to server, and would minimise load on the database. But I
 		 * think that would be overcomplicating the app, in this case. 
 		 * 
 		 * This will be a *very* expensive operation - oodles of queries generated.
 		 * The good news is that it should be done only infrequently, but even so
 		 * I would definitely look to optimise this heavily "in the wild".
 		 */
 		
 		try {
 			c = dataSource.getConnection();
 			deleteAddressBook(c, u.getId());
 
 			/* For each new AddressBookEntry to be added
 			 * - create an entry in the AddressBook table
 			 * - for each hash for this entry
 			 * -- create it in the idHash table if necessary
 			 * -- create an entry linking it to the addressBook row in the addressBookHashMatcher table
 			 */
 			
 			for (AddressBookEntry entry: book) {
 				
 				/* Add an entry into the AddressBook table */
 				
 				pst = c.prepareStatement(BOOK_INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
 				pst.setInt(1, u.getId());
 				pst.setString(2, entry.getName());
 				pst.setInt(3, entry.getPermission());
 				pst.executeUpdate();
 
 				/* Look up the unique ID for the entry we just added - we need this */
 				
 				rs = pst.getGeneratedKeys();
 				int addressBookId = -1;
 				if (rs.next()) addressBookId = rs.getInt(1);
 				else throw new RuntimeException("Never received insert_id when adding book");
 				rs.close();
 				pst.close();
 				
 				for (IdentityHash hash: entry.getHashes()) {
 					if (hash.getHash()==null) {
 						logger.warn("Received null hash");
 					} else {
 						int hashId = findOrCreateIdHash(c, hash.getHash());
 						
 						pst = c.prepareStatement(MATCH_INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
 						pst.setInt(1, hashId);
 						pst.setInt(2, addressBookId);
 						pst.executeUpdate();
 						pst.close();
 					}
 				}
 
 			}
 			
 			/* Finally, there may be hashes which used to be used, but no longer are
 			 * (if, for instance, the user has deleted a contact from their address
 			 * book). Remove these from the database.
 			 */
 			
 			//TODO Add this bit in. It's a tidy-up rather than essential, mind
 			/*
 			 * 
 			 * 
 			pst = c.prepareStatement(IDHASH_DELETE_ORPHAN_SQL);
 			pst.setInt(1, u.getId());
 			pst.executeUpdate();
 			pst.close();
 			*/
 			return true;
 		} finally {
 			if (rs!=null) rs.close();
 			if (pst!=null) pst.close();
 			if (c!=null) c.close();
 		}
 	}
 
 	@Override
 	public List<IdentityHash> getPermissions(User u) throws SQLException {
 		Connection c = null;
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 		try {
 			c = dataSource.getConnection();
 			pst = c.prepareStatement(IDHASH_LIST_SQL);
 			pst.setInt(1, u.getId());
 			pst.setInt(2, AddressBookEntry.PERM_SHOWN);
 			rs = pst.executeQuery();
 			List<IdentityHash> ret = new ArrayList<IdentityHash>();
 			while (rs.next()) {
 				ret.add(new IdentityHash(rs.getInt(1), rs.getString(2)));
 			}
 			return ret;
 		} finally {
 			if (rs!=null) rs.close();
 			if (pst!=null) pst.close();
 			if (c!=null) c.close();
 		}
 	}
 	
 	public boolean setPermissions(User u, String[] perms) throws SQLException {
 		
 		
 		Connection c = null;
 		PreparedStatement pst = null;
 		ResultSet rs = null;
 		try {
 			c = dataSource.getConnection();
 			
 			/* We wrap this whole operation in a transaction. This means that we can roll back the "resetting of permissions"
 			 * if it turns out that we're not making any changes further down the line
 			 */
 			
 			c.setAutoCommit(false);
 			/* First, set all permissions for this user to HIDDEN */
 			
 			pst = c.prepareStatement(PERMS_RESET_SQL); 
 			pst.setInt(1, AddressBookEntry.PERM_HIDDEN);
 			pst.setInt(2, u.getId());
 			int rows = pst.executeUpdate();
 			pst.close();
 			logger.debug("setPermissions reset " + rows);
 
 			/* Then, mark the ones we're interested in. This is more involved than it ought to be,
 			 * because the MySQL JDBC drivers don't let use use the setArray() method on a PreparedStatement
 			 * to pass in an array of stuff.
 			 * 
 			 * Instead, we have to have a query with an IN(?,?,?,?,?) clause and set each one of those 
 			 * substitutions to be null, or one of our values from our array... and then loop around
 			 * because, of course, we may have more friends to share with than are allowed for in that clause.
 			 * I kid you not.
 			 * */
 
 			pst = c.prepareStatement(PERMS_UPDATE_SQL);
 			pst.setInt(1, AddressBookEntry.PERM_SHOWN);
 			pst.setInt(2, u.getId());
 			rows = 0;
 			
 			for (int i=0; i<perms.length; i+= PERM_UPDATE_COUNT) {
 				for (int j=0; j<PERM_UPDATE_COUNT; j++) {
 					if (i+j<perms.length) {
 						logger.debug("set parameter " + (j+3) + " to hash " + (i+j));
 						pst.setString(j+3, perms[i+j]);
 					} else {
 						logger.debug("set parameter " + (j+3) + " to null");
 						pst.setString(j+3, null);
 					}
 				}
 				rows += pst.executeUpdate();
 				logger.debug("setPermissions updated " + rows);
 			}
 			if ((perms.length>0) && (rows==0)) return false; // the IdentityHash we were given wasn't linked to this user
 
 			c.commit();
 			c.setAutoCommit(true); // reset this to its usual setting
 			return true;
 		} finally {
 			if (rs!=null) rs.close();
 			if (pst!=null) pst.close();
 			if (c!=null) c.close();
 		}
 
 		
 		
 	}
 
 	@Override
 	public void deleteUser(User u) throws SQLException {
 		Connection c = dataSource.getConnection();
 		PreparedStatement pst = null;
 		
 		try {
 			deleteAddressBook(c, u.getId());
 			
 			pst = c.prepareStatement(USER_DELETE_SQL);
 			pst.setInt(1, u.getId());
 			pst.executeUpdate();
 		} finally {
 			if (pst!=null) pst.close();
 		}
 	}
 
 }
