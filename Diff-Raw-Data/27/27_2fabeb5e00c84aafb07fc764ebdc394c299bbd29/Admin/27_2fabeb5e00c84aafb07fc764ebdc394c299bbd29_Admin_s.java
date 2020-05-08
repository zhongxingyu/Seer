 //For super admin only
 package sql;
 import db.DBAccess;
 import helper.Settings;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * For the parameters, please use DB column names
  * Example: setNickName(String bu_id, String bu_nick) 
  *          bu_id and bu_nick are both DB column names
  * <p>
  * SQL Method prefix Legend:<p>
  * 1. (get): simple query<p>
  * 2. (is): query that use SELECT 1 to check existence<p>
  * 3. (default): UPDATE statement that set targeted data back to default values<p>
  * 4. (set): normal UPDATE statement, single column<p>
  * 5. (setMul): UPDATE statement, multi column<p>
  * 6. (update): UPDATE multiple tables using MySQL Stored Procedure (SP) or complex SQL statements
  *    if the method needs to be changed, edit would like be done in SQL script: bbb_db_init.sql<p>
  * 7. (create): INSERT INTO<p>
  * 8. (remove): DELETE<p>
  *
  */
 public class Admin extends Sql {
 	
     public Admin(DBAccess source) {
         super(source);
     }
     /**
      * Get all predefined_role on system<p>
      * Fields:<p>
      * (0)pr_name (1)pr_defaultmask 
      * @param result
      * @return
      */
     public Boolean getPreDefinedRole(ArrayList<ArrayList<String>> result){
         _sql = "SELECT * "
                 + "FROM predefined_role ";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get all user_role on system<p>
      * Fields:<p>
      * (0)ur_id(1)pr_name (2)ur_rolemask
      * @param result
      * @return
      */
     public Boolean getAllUserRoleInfo(ArrayList<ArrayList<String>> result){
         _sql = "SELECT * "
                 + "FROM user_role ";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get system settings information<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getSystemInfo(ArrayList<ArrayList<String>> result){
         _sql = "SELECT * "
                 + "FROM bbb_admin ";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get system settings information<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getSystemInfoByKeyName(ArrayList<ArrayList<String>> result,String key_name){
         _sql = "SELECT * "
                 + "FROM bbb_admin "
         		+ "WHERE key_name='" + key_name + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get timeout setting on system<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getTimeOut(ArrayList<ArrayList<String>> result){
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
         		+ "WHERE key_name='timeout'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get welcome message setting on system<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getWelcomeMsg(ArrayList<ArrayList<String>> result){
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
         		+ "WHERE key_name='welcome_msg'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get recording message setting on system<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getRecordingMsg(ArrayList<ArrayList<String>> result){
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
         		+ "WHERE key_name='recording_msg'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get default class section setting on system<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getDefaultClass(ArrayList<ArrayList<String>> result){
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
         		+ "WHERE key_name='default_class'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get default class section setting in a readable format on system<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getDefaultClassHr(ArrayList<ArrayList<String>> result){
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
         		+ "WHERE key_name='default_class_hr'";
         return _dbAccess.queryDB(result, _sql);
     }
     /**
      * Get default meeting setting on system<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getDefaultMeeting(ArrayList<ArrayList<String>> result){
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
         		+ "WHERE key_name='default_meeting'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get default meeting setting in readable format on system<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getDefaultMeetingHr(ArrayList<ArrayList<String>> result){
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
         		+ "WHERE key_name='default_meeting_hr'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get default user setting on system<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getDefaultUser(ArrayList<ArrayList<String>> result){
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
         		+ "WHERE key_name='default_user'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get default user setting in readable format on system<p>
      * Fields:<p>
      * (0)key_name (1)key_title (2)key_value 
      * @param result
      * @return
      */
     public Boolean getDefaultUserHr(ArrayList<ArrayList<String>> result){
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
         		+ "WHERE key_name='default_user_hr'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)key_name (1)key_value
      * @param message
      * @return
      */
     public boolean setWelcomeMsg(String message) {
         _sql = "UPDATE bbb_admin "
                 + "SET key_value = '" + message + "' "
                 + "WHERE key_name = 'welcome_msg'";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * (0)key_name (1)key_value
      * @param timeSecond
      * @return
      */
     public boolean setTimeout(String timeSecond) {
         _sql = "UPDATE bbb_admin "
                 + "SET key_value = '" + timeSecond + "' "
                 + "WHERE key_name = 'timeout'";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * (0)key_name (1)key_value
      * @param message
      * @return
      */
     public boolean setRecordingMsg(String message) {
         _sql = "UPDATE bbb_admin "
                 + "SET key_value = '" + message + "' "
                 + "WHERE key_name = 'recording_msg'";
         return _dbAccess.updateDB(_sql);
     }
     
     
     /**
      * (0)key_name (1)key_value
      * @param pr_name
      * @return
      */
     public boolean setPredefinedDefaultMask(HashMap<String, Integer> map, String pr_name) {
         int value;
         value = (map.get(Settings.ur_rolemask[0]) << 1)
                 + (map.get(Settings.ur_rolemask[1]) );
         _sql = "UPDATE predefined_role "
                 + "SET pr_defaultmask = " + value + " "
                 + "WHERE pr_name='" + pr_name + "'";
         return _dbAccess.updateDB(_sql);
     }
     
 }
