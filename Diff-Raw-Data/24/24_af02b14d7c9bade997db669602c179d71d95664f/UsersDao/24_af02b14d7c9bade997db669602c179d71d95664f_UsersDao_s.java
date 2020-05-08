 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package restaurante.DaoPersistence;
 
 /**
  *
  * @author Matheus
  */
 import restaurante.Beans.Users;
 import java.sql.SQLException;
 import restaurante.Utilities.ContentValues;
 
 public class UsersDao extends AbstractDao<Users> {
     public static final String TABLE_NAME = "users";
     /**
      * "active", "accesslevel", "name", "password", "id_branch", "passwordexpires"
      */
     public static final String[] FIELDS = new String[] 
         { "active", "accesslevel", "name", "password", "id_branch", "passwordexpires" };
 
     public UsersDao() throws ClassNotFoundException, SQLException {
     }
 
     /*
      * Insert the current Bean
      */
     public int insert(Users users) throws SQLException {
         return insert(users.isActive(), users.getAccessLevel().ordinal(), users.getName(),users.getPassword(), users.getBranch().getId(), users.getPasswordExpires());
     }
 
     /*
      * Update the current Bean
      */
     public void update(Users users) throws SQLException {
         update(users.getId(), users.isActive(), users.getAccessLevel().ordinal(), users.getName(), users.getPassword(), users.getBranch().getId(), users.getPasswordExpires());
     }
     
     /*
      * Convert the ContentValues (database fields) to bean
      */
     @Override
     protected Users toBean(ContentValues values) throws Exception {
         Users users = new Users();        
 
         users.setId(values.getInt(FIELD_ID));
         users.setActive(values.getBoolean(FIELDS[0]));
         users.setAccessLevel(Users.AccessLevel.values()[values.getInt(FIELDS[1])]);
         users.setName(values.getString(FIELDS[2]));
         users.setPassword(values.getString(FIELDS[3]));
         users.setPasswordExpires(values.getInt(FIELDS[5]));
         users.setBranch(new BranchesDao().findById(values.getInt(FIELDS[4])));
         return users;
     }
 
     /*
      * get the name of the table relative this DAO
      */
     @Override
     protected String getTableName() {
         return TABLE_NAME;
     }
 
     /*
      * get the name of the fields relative this DAO
      */
     @Override
     protected String[] getFields() {
         return FIELDS;
     }
     
     public Users selectByUserName(String userName) throws SQLException, Exception {
        ContentValues values = this.select(UsersDao.FIELDS[2] + " = ?", userName)[0];
         return toBean(values);
     }
 }
