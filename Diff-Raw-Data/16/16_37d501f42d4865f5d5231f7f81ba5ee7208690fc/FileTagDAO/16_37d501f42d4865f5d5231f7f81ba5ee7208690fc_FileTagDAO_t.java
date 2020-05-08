 package jfmi.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 import jfmi.control.FileTag;
 import jfmi.repo.SQLiteRepository;
 
 
 /** A FileTag provides data access for storing FileTag objects in an 
   underlying database.
   */
 public class FileTagDAO extends AbstractDAO<FileTag, String> {
 
 	// PUBLIC CLASS Fields
 	public static final String TABLE_NAME = "main.FileTag";
 
 	// PRIVATE CLASS Fields
 	private static final String CREATE_PSQL;
 	private static final String READ_BY_ID_PSQL;
 	private static final String READ_ALL_SQL;
 	private static final String UPDATE_PSQL;
 	private static final String DELETE_PSQL;
 	private static final String DELETE_ALL_SQL;
 
 	static {
 		CREATE_PSQL = "INSERT INTO " + TABLE_NAME + " VALUES(?)";
 		READ_BY_ID_PSQL = "SELECT * FROM " + TABLE_NAME + " WHERE tag = ? ";
 		READ_ALL_SQL = "SELECT * FROM " + TABLE_NAME;
 		UPDATE_PSQL = "UPDATE " + TABLE_NAME + " SET tag = ? WHERE tag = ? ";
 		DELETE_PSQL = "DELETE FROM " + TABLE_NAME + " WHERE tag = ? ";
 		DELETE_ALL_SQL = "DELETE FROM " + TABLE_NAME;
 	}
 		
 
 	//************************************************************
 	// PUBLIC INSTANCE Methods
 	//************************************************************
 
 	/** Creates a new FileTag record in the underlying database.
 	  @param createMe a FileTag instance containing the necessary information
 					  to replicate it in the database
	  @return true if the record was created successfully
 	  @throws SQLException if a problem occurs working with the database
 	  */
 	public boolean create(FileTag createMe) throws SQLException
 	{
 		Connection conn = SQLiteRepository.instance().getConnection();
 
 		try {
 			PreparedStatement ps = conn.prepareStatement(CREATE_PSQL);
 
 			try {
 				ps.setString(1, createMe.getTag());
 
 				return ps.executeUpdate() == 1;	// 1 row should be created
 				
 			} finally {
 				SQLiteRepository.closeQuietly(ps);				
 			}
 
 		} finally {
 			SQLiteRepository.closeQuietly(conn);
 		}
 	}
 
 
 	/** Retrieves the information necessary to create a FileTag object from the
 	  relevant database tables.
 	  @param id the tag value of the record to search for
 	  @return a new FileTag if the read was successful, null otherwise
 	  @throws SQLException if a problem occurs working with the database
 	  */
 	public FileTag readById(String id) throws SQLException
 	{
 		Connection conn = SQLiteRepository.instance().getConnection();
 
 		try {
 			PreparedStatement ps = conn.prepareStatement(READ_BY_ID_PSQL);
 
 			try {
 				ps.setString(1, id);
 				ResultSet rs = ps.executeQuery();
 
 				try {
 					FileTag result = null;
 
 					if (rs.next()) {
 						result = new FileTag();
 						result.setTag(id);
 					}
 
 					return result;
 
 				} finally {
 					SQLiteRepository.closeQuietly(rs);
 				}
 
 			} finally {
 				SQLiteRepository.closeQuietly(ps);				
 			}
 
 		} finally {
 			SQLiteRepository.closeQuietly(conn);
 		}
 	}
 
 	/** Reads all FileTag records from the database.
 	  @return a list of retrieved FileTag records
 	  @throws SQLException if a problem occurs working with the database
 	  */
 	public List<FileTag> readAll() throws SQLException
 	{
 		Connection conn = SQLiteRepository.instance().getConnection();
 
 		try {
 			Statement stmt = conn.createStatement();
 
 			try {
 				ResultSet rs = stmt.executeQuery(READ_ALL_SQL);
 
 				try {
 					LinkedList<FileTag> list = new LinkedList<FileTag>();
 					FileTag next = null;
 
 					while (rs.next()) {
 						next = new FileTag();
 						next.setTag(rs.getString("tag"));
 
 						list.add(next);
 					}
 
 					return list;
 
 				} finally {
 					SQLiteRepository.closeQuietly(rs);
 				}
 
 			} finally {
 				SQLiteRepository.closeQuietly(stmt);				
 			}
 
 		} finally {
 			SQLiteRepository.closeQuietly(conn);
 		}
 	}
 
 	/** Updates the specified FileTag's corresponding record in the database,
 	  if it exists.
 	  @param updateMe the FileTag which will be used to update the database
 	  @param id the id of the record to update 
 	  @return true if the record existed and was updated successfully
 	  @throws SQLException if a problem occurs working with the database
 	  */
 	public boolean update(FileTag updateMe, String id) throws SQLException
 	{
 		Connection conn = SQLiteRepository.instance().getConnection();
 
 		try {
 			PreparedStatement ps = conn.prepareStatement(UPDATE_PSQL);
 
 			try {
 				ps.setString(1, updateMe.getTag());
 				ps.setString(2, id);
 
 				return ps.executeUpdate() == 1;	// 1 row should be updated
 				
 			} finally {
 				SQLiteRepository.closeQuietly(ps);				
 			}
 
 		} finally {
 			SQLiteRepository.closeQuietly(conn);
 		}
 	}
 
 	/** Deletes the specified FileTag's corresponding record from the 
 	  database if it exists.
 	  @param deleteMe the FileTag whose record should be deleted
 	  @return true if the record was deleted, or did not exist
 	  @throws SQLException if a problem occurs working with the database
 	  */
 	public boolean delete(FileTag deleteMe) throws SQLException
 	{
 		Connection conn = SQLiteRepository.instance().getConnection();
 
 		try {
 			PreparedStatement ps = conn.prepareStatement(DELETE_PSQL);
 
 			try {
 				ps.setString(1, deleteMe.getTag());
 				int rowCount = ps.executeUpdate();
 
 				return rowCount == 0 || rowCount == 1;
 				
 			} finally {
 				SQLiteRepository.closeQuietly(ps);				
 			}
 
 		} finally {
 			SQLiteRepository.closeQuietly(conn);
 		}
 	}
 
 	/** Deletes all FileTag records from the database.
 	  @throws SQLException if a problem occurs working with the database
 	  */
 	public void deleteAll() throws SQLException
 	{
 		Connection conn = SQLiteRepository.instance().getConnection();
 
 		try {
 			Statement stmt = conn.createStatement();
 
 			try {
 				stmt.executeUpdate(DELETE_ALL_SQL);
 				
 			} finally {
 				SQLiteRepository.closeQuietly(stmt);				
 			}
 
 		} finally {
 			SQLiteRepository.closeQuietly(conn);
 		}
 	}
 
 }
