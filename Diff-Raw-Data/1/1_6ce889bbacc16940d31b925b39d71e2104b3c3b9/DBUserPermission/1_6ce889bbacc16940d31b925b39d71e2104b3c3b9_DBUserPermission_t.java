 /**
  * filename    : DBUserPermission.java
  * created     : Dec 11, 2012 (5:05:06 PM)
  * description :
  * -------------------------------------------------------
  * @version    : 0.1
  * @changes    :
  */
 
 package db;
 
 import models.UserPermission;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class DBUserPermission implements IFDBUserPermission
 {
 	private DataAccess _da;
 	public DBUserPermission()
 	{
 		_da = DataAccess.getInstance();
 	}
 	
 	/**
 	 * Retrieve all roles from database
 	 * 
 	 * @return ArrayList<UserPermission>
 	 */
 	public ArrayList<UserPermission> getAllRoles() throws Exception
 	{
 		ArrayList<UserPermission> returnList = new ArrayList<UserPermission>();
 		
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM UserPermissions");
 		_da.setSqlCommandText(query);
 		ResultSet roleResult = _da.callCommandGetResultSet();
 		
 		while(roleResult.next())
 		{
 			UserPermission userPermission = buildRoles(roleResult);
 			returnList.add(userPermission);
 		}
 		
 		return returnList;
 	}
 	
 	/**
 	 * Retrieve a specific role by it's id
 	 * 
 	 * @param value				the value of the id you need returned
 	 * @return UserPermission
 	 */
 	public UserPermission getRoleById(int value) throws Exception
 	{
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM UserPermissions WHERE permissionId = ?");
 		query.setInt(1, value);
 		_da.setSqlCommandText(query);
 		ResultSet roleResult = _da.callCommandGetRow();
 		if(roleResult.next())
 			return buildRoles(roleResult);
 		
 		return null;
 	}
 	
 	/**
 	 * Insert a role into the database
 	 * 
 	 * @param userPermission	the object that contains the data you want stored
 	 * @return int				returns the number of rows affected
 	 */
 	public int insertRole(UserPermission userPermission) throws Exception
 	{
 		if (userPermission == null)
 			return 0;
 		
 		PreparedStatement query = _da.getCon().prepareStatement("INSERT INTO UserPermissions (userRole, creationDate, editedDate " +
 																"VALUES(?, ?, ?)");
 		
 		query.setString(1, userPermission.getUserRole());
 		query.setDate(2, (java.sql.Date)userPermission.getCreatedDate());
 		query.setDate(3, (java.sql.Date)userPermission.getEditedDate());
 		_da.setSqlCommandText(query);
 		
 		return _da.callCommand();
 	}
 	
 	/**
 	 * Updates an existing role in the database
 	 * 
 	 * @param userPermission	the object containing the data you want to update
 	 * @return int				returns the number of rows affected
 	 */
 	public int updateRole(UserPermission userPermission) throws Exception
 	{
 		if (userPermission == null)
 			return 0;
 		
 		if (getRoleById(userPermission.getPermissionId()) == null)
 			return 0;
 		
 		PreparedStatement query = _da.getCon().prepareStatement("UPDATE UserRoles SET userRole = ?, creationDate = ?, " + 
 																"editedDate = ? WHERE permissionId = ?");
 		
 		query.setString(1, userPermission.getUserRole());
 		query.setDate(2, (java.sql.Date)userPermission.getCreatedDate());
 		query.setDate(3, (java.sql.Date)userPermission.getEditedDate());
 		query.setInt(4, userPermission.getPermissionId());
 		_da.setSqlCommandText(query);
 		
 		return _da.callCommand();
 	}
 	
 	/**
 	 * Delete an existing role from the database
 	 * 
 	 * @param userPermission	the object containing the role which is going to be deleted
 	 * @return int				returns the number of rows affected
 	 */
 	public int deleteRole(UserPermission userPermission) throws Exception
 	{
 		if(userPermission == null)
 			return 0;
 		
 		PreparedStatement query = _da.getCon().prepareStatement("DELETE FROM UserPermissions WHERE permissionId = ?");
 		
 		query.setInt(1, userPermission.getPermissionId());
 		_da.setSqlCommandText(query);
 		
 		return _da.callCommand();
 	}
 	
 	/**
 	 * Delete an existing role from the database
 	 * 
 	 * @param value				the value of the id which is going to be deleted
 	 * @return int				returns the number of rows affected
 	 */
 	public int deleteRole(int value) throws Exception
 	{
 		PreparedStatement query = _da.getCon().prepareStatement("DELETE FROM UserPermissions WHERE permissionId = ?");
 		
 		query.setInt(1, value);
 		_da.setSqlCommandText(query);
 		
 		return _da.callCommand();
 	}
 	
 	private UserPermission buildRoles(ResultSet row) throws Exception
 	{
 		if(row == null)
 			return null;
 		
 		int permissionId = row.getInt("permissionId");
 		String userRole = row.getString("userRole");
 		Date creationDate = row.getDate("creationDate");
 		Date editedDate = row.getDate("editedDate");
 		
 		return new UserPermission(permissionId, userRole, creationDate, editedDate);
 	}
 }
