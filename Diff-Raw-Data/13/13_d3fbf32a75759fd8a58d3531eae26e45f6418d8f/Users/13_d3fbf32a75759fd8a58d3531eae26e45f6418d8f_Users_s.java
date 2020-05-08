 package uk.ac.ebi.ae15.components;
 
 import uk.ac.ebi.ae15.app.Application;
 import uk.ac.ebi.ae15.app.ApplicationComponent;
 import uk.ac.ebi.ae15.utils.persistence.PersistableUserList;
 import uk.ac.ebi.ae15.utils.persistence.TextFilePersistence;
 import uk.ac.ebi.ae15.utils.users.UserList;
 import uk.ac.ebi.ae15.utils.users.UserRecord;
 
 import java.io.File;
 
 public class Users extends ApplicationComponent
 {
     private TextFilePersistence<PersistableUserList> userList;
 
     public Users( Application app )
     {
         super(app, "Users");
     }
 
     public void initialize()
     {
         userList = new TextFilePersistence<PersistableUserList>(
                 new PersistableUserList()
                 , new File(
                     System.getProperty("java.io.tmpdir")
                     , getPreferences().getString("ae.users.cache.filename")
                 )
         );
     }
 
     public void terminate()
     {
     }
 
     public void setUserList( UserList _userList )
     {
         userList.setObject(new PersistableUserList(_userList));
     }
 
    public String hashLogin( String username, String password, String suffix )
     {
        if ( userList.getObject().containsKey(username) ) {
             UserRecord user = userList.getObject().get(username);
             if ( user.getPassword().equals(password) ) {
                 return user.getPasswordHash(suffix);
             }
         }
         // otherwise
         return "";
     }
 
     public boolean verifyLogin( String username, String hash, String suffix )
     {
        if ( userList.getObject().containsKey(username) ) {
             UserRecord user = userList.getObject().get(username);
             return user.getPasswordHash(suffix).equals(hash);
         }
         return false;
     }
 
     public UserRecord getUserRecord( String username )
     {
        return userList.getObject().get(username);    
     }
 }
