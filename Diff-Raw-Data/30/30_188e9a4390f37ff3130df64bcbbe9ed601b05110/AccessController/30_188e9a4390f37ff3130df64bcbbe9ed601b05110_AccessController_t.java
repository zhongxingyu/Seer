 package safeguard;
 
import java.util.List;

 public class AccessController {
 
     private static AccessController accessControlInstance = null;
     private Crypto crypto;
     private DatabaseHelper databaseHelper;
 
     private ClassLevel userCurrentClassLevel = ClassLevel.NOT_CONFIDENTIAL;
 
     public static enum ClassLevel implements Comparable<ClassLevel> {
 
         NOT_CONFIDENTIAL(0),
         CONFIDENTIAL(1),
         STRICTLY_CONFIDENTIAL(2);
 
         ClassLevel(int level) {
             this.level = level;
         }
 
         public int value() {
             return level;
         }
 
         public static ClassLevel map(int val) {
             switch (val) {
                 case 0:
                     assert NOT_CONFIDENTIAL.value() == 0;
                     return NOT_CONFIDENTIAL;
                 case 1:
                     assert CONFIDENTIAL.value() == 1;
                     return CONFIDENTIAL;
                 case 2:
                     assert STRICTLY_CONFIDENTIAL.value() == 2;
                     return STRICTLY_CONFIDENTIAL;
                 default:
                     return null;
             }
         }
 
         private int level;
     }
 
     private AccessController() {
         crypto = Crypto.getInstance();
         databaseHelper = DatabaseHelper.getInstance();
     }
 
     public static AccessController getInstance() {
         if (accessControlInstance != null) {
             return accessControlInstance;
         } else {
             return accessControlInstance = new AccessController();
         }
     }
 
     private String getPathName(String filePath) {
         if (filePath.charAt(filePath.lastIndexOf('\\') - 1) == ':') {
             return filePath.substring(0, filePath.indexOf(':') + 1);
         } else {
             return filePath.substring(0, filePath.lastIndexOf('\\') + 1);
         }
     }
     
     public boolean checkClassLevel(ClassLevel users, ClassLevel files) {
         return users.compareTo(files) >= 0;
     }
 
     /**
      * Checks if the user can create file with given filename.
      *
      * @param uid user identifier
      * @param filePath full path with filename
      * @return true if user can create file
      */
     public boolean checkIfHaveAccessToParent(int uid, String filePath) {
    	boolean check=databaseHelper.accessTypeDirVolExe(uid, getPathName(filePath)) != Engine.AccessMode.CANNOT_OPEN;
        if (check==true){
        	int group=databaseHelper.getUserGroup(uid);
        	List<Integer> users=databaseHelper.getUsers();
        	for (int u: users){
        		if (databaseHelper.getUserGroup(u)==group){
        			if (isDirectory(filePath)){
        				databaseHelper.addAccessToOther(u, filePath);
        			} else {
        				databaseHelper.addAccessToSecretFile(u, filePath, Engine.AccessMode.FULL_ACCESS);
        			}
        		} else {
        			if (isDirectory(filePath)){
        				if (databaseHelper.getUserNewDirMark(uid)==Engine.AccessMode.FULL_ACCESS){
        					databaseHelper.addAccessToOther(u, filePath);
        				}        				
        			} else {
        				databaseHelper.addAccessToSecretFile(u, filePath, databaseHelper.getUserNewFileMark(uid));
        			}
        		}
        	}
        }
        return check;
     }
 
     /**
      * Checks if the user has access to file
      *
      * @param uid user identifier
      * @param filePath full path
      * @return true if user can execute program
      */
     public boolean checkIfHaveAccess(int uid, String filePath) {
        return databaseHelper.accessTypeDirVolExe(uid, filePath) != Engine.AccessMode.CANNOT_OPEN;
     }
 
     /**
      * Gets access type for given secret file and user
      *
      * @param uid user identifier
      * @param filePath full path
      * @return access mode for given file and user
      */
     public Engine.AccessMode checkHowCanOpenFile(int uid, String filePath) {
         if(!checkClassLevel(databaseHelper.getUserClass(uid), databaseHelper.getFileClass(filePath))) {
             return Engine.AccessMode.CANNOT_OPEN;
         } else {
             Engine.AccessMode access = databaseHelper.accessTypeSecretFile(uid, filePath);
             if(access != Engine.AccessMode.CANNOT_OPEN) {
                 updateUserCurrentClassLevel(filePath);
             }
             return access;
         }
     }
 
     private boolean isExecutable(String filePath) {
         return filePath.endsWith(".exe");
     }
 
     private boolean isDirectory(String filePath) {
         return filePath.endsWith("\\");
     }
 
     /**
      * Checks if user can delete the directory
      *
      * @param uid user identifier
      * @param filePath full path
      * @return true if user can delete directory
      */
     public boolean checkIfCanDeleteDir(int uid, String filePath) {
         for (String inFilePath : databaseHelper.getControlledDirsAndFilesIn(filePath)) {
             if (isExecutable(inFilePath)) {
                 return false;
             } else {
                 if (databaseHelper.accessTypeDirVolExe(uid, filePath) != Engine.AccessMode.FULL_ACCESS) {
                     return false;
                 }
                 if (isDirectory(inFilePath) && !checkIfCanDeleteDir(uid, inFilePath)) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * Updates the highest level of security user had worked with
      *
      * @param uid user identifier
      * @param filePath full path
      */
     private void updateUserCurrentClassLevel(String filePath) {
         ClassLevel fileLevel = databaseHelper.getFileClass(filePath);
         if (fileLevel.compareTo(userCurrentClassLevel) > 0) {
             userCurrentClassLevel = fileLevel;
         }
     }
 
     /**
      *
      * @return the highest level of security user had worked with
      */
     public ClassLevel getUserCurrentClassLevel() {
         return userCurrentClassLevel;
     }
 
     /**
      * Sets stored level of security to NOT_CONFIDENTIONAL
      */
     public void refreshUserCurrentClassLevel() {
         userCurrentClassLevel = ClassLevel.NOT_CONFIDENTIAL;
     }
 }
