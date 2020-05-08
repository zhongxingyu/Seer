 package de.enwida.web.dao.implementation;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.TypedQuery;
 
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import de.enwida.web.controller.AdminController;
 import de.enwida.web.dao.interfaces.AbstractBaseDao;
 import de.enwida.web.dao.interfaces.IUserDao;
 import de.enwida.web.db.model.UploadedFile;
 import de.enwida.web.model.User;
 import de.enwida.web.utils.Constants;
 
 @Repository
 public class UserDaoImpl extends AbstractBaseDao<User> implements IUserDao {
 	
     private static org.apache.log4j.Logger logger = Logger.getLogger(AdminController.class);
 
 	@Override
 	public void deleteUser(User user) {
 		delete(user);
 	}
 	
     @Override
 	public long save(User user)
 	{
 		User exisinguser = fetchByName(user.getUserName());
 		try {
 			if (exisinguser == null) {
 				// create the user and refresh the user object
 				create(user);
 			} else {
 				user = update(user);
 			}
 		} catch (Exception e) {
 			logger.error("Error saving user : " + user.getUserName(), e);
 		}
 
 		return user.getUserID();
 	}
   
 	@SuppressWarnings({ "unchecked", "rawtypes" })
     @Override
     public boolean checkUserActivationId(String username, String activationCode) {
         //TODO:Fix here
         String sql = "select activation_id from "+Constants.USERS_SCHEMA_NAME+Constants.USER_TABLE_NAME+" where users.user_name=?";
         String activationID = (String) this.jdbcTemplate.queryForObject(sql, new Object[] { username }, new RowMapper() {
             @Override
             public String mapRow(ResultSet rs, int arg1) throws SQLException {
                 return rs.getString("activation_id");
             }
         });
         return activationID.equalsIgnoreCase(activationCode);
     }
     
 
     @Override
 	public void activateUser(String username) {
         User user=fetchByName(username);
         user.setEnabled(true);
         save(user);
     }
 
     @Override
 	public void updateUser(User user) {
 		save(user);
 	}
 
     @Override
 	public void enableDisableUser(long userID, boolean enabled) {
        User user=fetchById(userID);
        user.setEnabled(false);
        update(user);
     }
     
     @Override
     public boolean usernameAvailablility(String userName) {
         return this.fetchByName(userName)!=null;
     }
 
     @Override
     public List<User> getUsersByGroupID(Long groupID) {
         TypedQuery<User> typedQuery = em.createQuery("from "
                                 + User.class.getName()
                                 + " INNER JOIN users.user_group ON users.users.user_id=users.user_group.user_id WHERE user_group.group_ID=:groupID",
                         User.class);
         return typedQuery.setParameter("groupID", groupID).getResultList();
     }
 
     @Override
 	public int getUploadedFileVersion(UploadedFile uplaodedfile, User user) {
 		int revision = 1;
 		User latestuser = fetchByName(user.getUserName());
 		Set<UploadedFile> uploadedFiles = latestuser.getUploadedFiles();
 		for (UploadedFile file : uploadedFiles) {
 			if (file.getDisplayFileName().equals(
 					uplaodedfile.getDisplayFileName())) {
 				revision += 1;
 			}
 		}
 		return revision;
 	}
 
 	@Override
 	public Long getNextSequence(String schema, String sequenceName) {
 		return super.getNextSequenceNumber(schema, sequenceName);
 	}
 }
