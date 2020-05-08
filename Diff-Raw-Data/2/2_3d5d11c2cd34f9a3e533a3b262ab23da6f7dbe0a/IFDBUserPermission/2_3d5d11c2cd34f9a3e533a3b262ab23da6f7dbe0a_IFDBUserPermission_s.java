 /**
  * filename    : IFDBUserPermission.java
  * created     : Dec 11, 2012 (4:44:31 PM)
  * description : Interface class for DBUserPermission
  * -------------------------------------------------------
  * @version    : 0.1
  * @changes    :
  */
 
 package db;
 
 import models.UserPermission;
 import java.util.ArrayList;
 
 public interface IFDBUserPermission
 {
 	/**
 	 * Retrieve all roles from database
 	 * 
 	 * @return ArrayList<UserPermission>
 	 */
 	public ArrayList<UserPermission> getAllRoles() throws Exception;
 	
 	/**
 	 * Get a specific role by its id
 	 * 
 	 * @param value					the value of the id you need returned
 	 * @return UserPermission
 	 */
 	public UserPermission getRoleById(int value) throws Exception;
 	
 	/**
 	 * Inserts a new role in the database
 	 * 
 	 * @param userPermission		the object containing the information you want stored
	 * @return int 					returns the number of rows affected
 	 */
 	public int insertRole(UserPermission userPermission) throws Exception;
 	
 	/**
 	 * Update an existing role in the database
 	 * 
 	 * @param userPermission		the object containing the updated information you want stored
 	 * @return int					returns the number of rows effected
 	 */
 	public int updateRole(UserPermission userPermission) throws Exception;
 	
 	/**
 	 * Delete an existing role from the database
 	 * 
 	 * @param userPermission		the object containing the role which is going to be delete from the database
 	 * @return int					returns the number of rows affected
 	 */
 	public int deleteRole(UserPermission userPermission) throws Exception;
 }
