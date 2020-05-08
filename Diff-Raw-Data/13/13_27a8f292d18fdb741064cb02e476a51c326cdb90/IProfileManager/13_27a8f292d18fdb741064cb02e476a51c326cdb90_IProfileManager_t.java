 package com.mymed.controller.core.manager.profile;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.model.data.user.MUserBean;
 
 /**
  * Manage the profile of an user
  * 
  * @author lvanni
  * 
  */
 public interface IProfileManager {
 
 	/**
 	 * Setup a new user profile into the database
 	 * 
 	 * @param user
 	 *            the user to insert into the database
 	 * @return true if the user if the user is created, false otherwise
 	 */
 	MUserBean create(MUserBean user) throws InternalBackEndException, IOBackEndException;
 
 	/**
 	 * @param id
 	 *            the id of the user
 	 * @return the User corresponding to the id
 	 */
 	MUserBean read(String id) throws InternalBackEndException, IOBackEndException;
 
 	/**
 	 * Update the profile of an user into the database
 	 * 
 	 * @param user
 	 *            The profile updated to store
	 * @return
 	 */
	MUserBean update(MUserBean user) throws InternalBackEndException, IOBackEndException;
 
 	/**
 	 * Delete an existing user
 	 * 
 	 * @param user
 	 *            The user to delete
 	 */
 	void delete(String id) throws InternalBackEndException, IOBackEndException;
 }
