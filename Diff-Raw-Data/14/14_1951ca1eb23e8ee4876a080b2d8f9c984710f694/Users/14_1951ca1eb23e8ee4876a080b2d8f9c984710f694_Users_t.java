 package org.backmeup.keysrv.rest;
 
 import java.util.ArrayList;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 import org.backmeup.keyserver.dal.AuthInfoDao;
 import org.backmeup.keyserver.dal.UserDao;
 import org.backmeup.keysrv.dal.postgres.impl.AuthInfoDaoImpl;
 import org.backmeup.keysrv.dal.postgres.impl.UserDaoImpl;
 import org.backmeup.keysrv.rest.data.UserContainer;
 import org.backmeup.keysrv.rest.exceptions.RestUserNotValidException;
 import org.backmeup.keysrv.worker.AuthInfo;
 import org.backmeup.keysrv.worker.DBLogger;
 import org.backmeup.keysrv.worker.DataManager;
import org.backmeup.keysrv.worker.FileLogger;
 import org.backmeup.keysrv.worker.User;
 
 @Path ("/users")
 public class Users
 {
 	@GET
 	@Path ("{bmu_user_id}")
 	@Produces ("application/json")
 	public UserContainer getUser (@PathParam ("bmu_user_id") long bmu_user_id)
 	{
 		UserDao userdao = DataManager.getUserDao ();
 		User user = userdao.getUser (bmu_user_id);
 		
 		return new UserContainer (user);
 	}
 
 	@DELETE
 	@Path ("{bmu_user_id}")
 	@Produces ("application/json")
 	public void deleteUser (@PathParam ("bmu_user_id") long bmu_user_id)
 	{
 		UserDao userdao = DataManager.getUserDao ();
 		
 		User user = new User (bmu_user_id);
 		
 		userdao.deleteUser (user);
 		DBLogger.deleteAllUserLogs (user);
 	}
 
 	@POST
 	@Path ("{bmu_user_id}/{bmu_user_pwd}/register")
 	@Produces ("application/json")
 	public void registerUser (@PathParam ("bmu_user_id") long  bmu_user_id, @PathParam ("bmu_user_pwd") String  bmu_user_pwd)
 	{
 		UserDao userdao = DataManager.getUserDao ();
 		
 		User user = new User (bmu_user_id);
 		user.setPwd (bmu_user_pwd);
 		
 		userdao.insertUser (user);
 		
 		DBLogger.logUserCreated (user);
 	}
 	
 	@GET
 	@Path ("{bmu_user_id}/{bmu_user_pwd}/validate")
 	@Produces ("application/json")
 	public void validateUser (@PathParam ("bmu_user_id") long  bmu_user_id, @PathParam ("bmu_user_pwd") String  bmu_user_pwd)
 	{
 		UserDao userdao = DataManager.getUserDao ();
 		User user = userdao.getUser (bmu_user_id);
 		
 		if (user.validatePwd (bmu_user_pwd) == false)
 		{
 			DBLogger.logUserWrongLoginPassword (user);
 			throw new RestUserNotValidException (bmu_user_id);
 		}
 		
 		DBLogger.logUserLogin (user);
 	}
 	
 	@GET
 	@Path ("{bmu_user_id}/{old_bmu_user_pwd}/{new_bmu_user_pwd}/changeuserpwd")
 	@Produces ("application/json")
 	public void changeUserPwd (@PathParam ("bmu_user_id") long bmu_user_id, @PathParam ("new_bmu_user_pwd") String new_bmu_user_pwd, @PathParam ("old_bmu_user_pwd") String old_bmu_user_pwd)
 	{
 		UserDao userdao = DataManager.getUserDao ();
 		User user = userdao.getUser (bmu_user_id);
 		
 		if (user.validatePwd (old_bmu_user_pwd) == false)
 		{
 			throw new RestUserNotValidException (bmu_user_id);
 		}
 		
 		user.setPwd (new_bmu_user_pwd);
 		userdao.changeUser (user);
 		
 		DBLogger.logUserChangedPassword (user);
 	}
 	
 	@GET
 	@Path ("{bmu_user_id}/{old_bmu_user_keyring_pwd}/{new_bmu_user_keyring_pwd}/changeuserkeyringpwd")
 	@Produces ("application/json")
 	public void changeUserKeyringPwd (@PathParam ("bmu_user_id") long bmu_user_id, @PathParam ("new_bmu_user_keyring_pwd") String new_bmu_user_keyring_pwd, @PathParam ("old_bmu_user_keyring_pwd") String old_bmu_user_keyring_pwd)
 	{
 		UserDao userdao = DataManager.getUserDao ();
 		AuthInfoDao authinfodao = DataManager.getAuthInfoDao ();
 		User user = userdao.getUser (bmu_user_id);
 		
 		ArrayList<AuthInfo> authinfos = authinfodao.getUserAuthInfos (user);
 		
 		for (AuthInfo authinfo : authinfos)
 		{
 			authinfo.changePassword (old_bmu_user_keyring_pwd, new_bmu_user_keyring_pwd);
 			authinfodao.deleteAuthInfo (authinfo.getBmuAuthinfoId ());
 			authinfodao.insertAuthInfo (authinfo);
 		}
 		
 		DBLogger.logUserChangedKeyringPassword (user);
 	}
 }
