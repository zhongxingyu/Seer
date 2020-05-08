 package edu.cmu.square.server.business.implementation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.stereotype.Service;
 
 import edu.cmu.square.client.exceptions.ExceptionType;
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtUser;
 import edu.cmu.square.server.authorization.AllowedRoles;
 import edu.cmu.square.server.authorization.Roles;
 import edu.cmu.square.server.business.interfaces.ManageSiteBusiness;
 import edu.cmu.square.server.dao.interfaces.UserDao;
 import edu.cmu.square.server.dao.model.User;
 
 
 @Service
 @Scope("prototype")
 public class ManageSiteBusinessImpl extends BaseBusinessImpl implements ManageSiteBusiness
 {
 
 	
 	@Resource
 	private UserDao userDao;
 	@Resource(name="userCreatedMessage")
 	private SimpleMailMessage userCreatedMessage;
 
 	@Resource(name="resetPasswordTemplateEmail")
 	private SimpleMailMessage resetPasswordTemplateEmail;
 	
 	
 	@AllowedRoles(roles = {Roles.All})
 	public List<GwtUser> getAllUsers() throws SquareException
 	{
 		List<GwtUser> gwtUserList = new ArrayList<GwtUser>();
 		List<User> databaseUserList = userDao.fetchAll();
 
 		for (User user : databaseUserList)
 		{
 			GwtUser gwtUser = user.createGwtUser();
 			gwtUserList.add(gwtUser);
 		}
 
 		return gwtUserList;
 	}
 
 	/**
 	 * Deletes the user permanently from the database
 	 * 
 	 * @param user
 	 *            the user to be deleted
 	 * @return true if the user is deleted, false otherwise
 	 */
 	@AllowedRoles(roles = {Roles.Administrator})
 	public boolean removeUser(GwtUser user) throws SquareException
 	{
 		User dataUser = new User(user, "");
 
 		if (this.userDao.removeUserFromDatabase(dataUser) > 0)
 		{
 			return true;
 		}
 
 		return false;
 	}
 
 	
 	@AllowedRoles(roles = {Roles.All})
 	public GwtUser getUserInfo(Integer userID) throws SquareException
 	{
 		User dataUser = new User();
 		dataUser = userDao.fetch(userID);
 
 		GwtUser gwtUser = dataUser.createGwtUser();
 		return gwtUser;
 	}
 
 	
 	@AllowedRoles(roles = {Roles.All})
 	public void updateUserNameEmailRole(GwtUser gwtUser) throws SquareException
 	{
 		this.checkUser(gwtUser);
 
 		try
 		{
 			User updateUser = userDao.fetch(gwtUser.getUserId());
 			updateUser.setFullName(gwtUser.getFullName());
 			updateUser.setEmail(gwtUser.getEmailAddress());
 			updateUser.setisAdmin(gwtUser.getIsAdmin());
 			updateUser.setLocked(gwtUser.isLocked());
 			
 			if (gwtUser.getUserName() != "")
 			{
 				updateUser.setUserName(gwtUser.getUserName());
 			}
 
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException("updating user name, email, role failed", t);
 		}
 	}
 
 	
 	@AllowedRoles(roles = {Roles.Administrator})
 	public void updateUserPassword(GwtUser gwtUser, String password, boolean email) throws SquareException
 	{
 		this.checkUser(gwtUser);
 		
 		try
 		{
 			User updateUser = userDao.fetch(gwtUser.getUserId());
 			
 			if (password.trim().length() != 0)
 			{
 				updateUser.setPassword(password.trim());
 			}
 			
 			// send mail
 			if (email)
 			{
 				sendMail(updateUser, resetPasswordTemplateEmail, "resetPassword.vm");
 			}
 		}
 		catch (SquareException se)
 		{
 			throw se;
 		}
 		catch (Throwable t)
 		{
 			SquareException se = new SquareException("updating user password failed", t);
 			se.setType(ExceptionType.other);
 			throw se;
 		}
 
 	}
 
 
 	
 	@AllowedRoles(roles = {Roles.All})
 	public void updateUserProfile(GwtUser gwtUser, String password) throws SquareException
 	{
 		this.checkUser(gwtUser);
 		boolean isDuplicate = true;
 		
 		try
 		{
 			User updateUser = userDao.fetch(gwtUser.getUserId());
 			updateUser.setFullName(gwtUser.getFullName());
 			
 			if (password.trim().length() != 0)
 			{
 				updateUser.setPassword(password.trim());	
 			}
 			
 			updateUser.setisAdmin(gwtUser.getIsAdmin());
 			updateUser.setPhone(gwtUser.getPhone());
 			List<User> users = userDao.getUsersbyEmail(gwtUser.getEmailAddress());
 			
 			if (!users.isEmpty())
 			{
 				for(User u: users)
 				{
 					if(u.getId() == updateUser.getId())
 					{
 						isDuplicate = false;
 					}
 				}
 				if(isDuplicate)
 				{
 					SquareException se = new SquareException("User with same email address exists");
 					se.setType(ExceptionType.duplicateEmail);
 					throw se;
 				}
 			}
 			
 			updateUser.setEmail(gwtUser.getEmailAddress());
 			updateUser.setDepartment(gwtUser.getDepartment());
 			updateUser.setOrganization(gwtUser.getOrganization());
 			updateUser.setLocation(gwtUser.getLocation());
 			updateUser.setSkipTeachStep(gwtUser.isSkipTeachStep());
 		}
 		catch (SquareException se)
 		{
 			throw se;
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException("updating user failed", t);
 		}
 
 	}
 
 	
 	@AllowedRoles(roles = {Roles.Administrator})
 	public GwtUser createUser(GwtUser userToCreate, String password, boolean emailPassword) throws SquareException
 	{
 		if (userToCreate == null)
 		{
 			throw new SquareException("gwtUser should not be null.");
 		}
 		
 		try
 		{
 			User user = null;
 			
 
 			System.out.println(" server mngsite biz done0");
 			if (password.trim().length() != 0)
 			{
 				user = new User(userToCreate, password.trim());
 			}
 			else
 			{
 				throw new SquareException("user must have a password.");
 			}
 			System.out.println("done1");
 			userDao.create(user);
 
 			System.out.println("done2");
 			if (emailPassword)
 			{
 				sendMail(user, userCreatedMessage, "userCreated.vm");
 			}
 
 			System.out.println("done3");
 			return user.createGwtUser();
 		}
 		catch (Throwable t)
 		{
			System.out.println();
 			throw new SquareException("creating user failed", t);
 		}
 	}
 	
 	
 	private void checkUser(GwtUser usertoCheck) throws SquareException
 	{
 		if (usertoCheck == null)
 		{
 			throw new SquareException("gwtUser should not be null.");
 		}
 
 		if (usertoCheck.getUserId() <= 0)
 		{
 			throw new SquareException("gwtUser must have an id.");
 		}
 	}
 	
 	
 	
 
 }
