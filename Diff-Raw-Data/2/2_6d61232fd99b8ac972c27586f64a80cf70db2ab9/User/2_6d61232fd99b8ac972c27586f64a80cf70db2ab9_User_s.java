 package hygeia;
 
 import java.sql.*;
 
 /* User class used for getting user information and creating users */
 public class User {
     
     private Database db;
     private int uid;
     private String username;
     private String email;
     private char gender;
     private short activity;
     private int blocks;
     private double height;
     private double weight;
     private double hips;
     private double waist;
     private double wrist;
     private double leanBodyMass;
         
     /*Use this to create User object in a page after the user has started a session */
     public User(Database db, int uid) {
         this.db = db;
         this.uid = uid;
     }
     
     /* Returns user id if login is successful, zero otherwise */
     public static int login(Database db, String email, String pwd) {
         if ((db == null) || (email == null) || (pwd == null)) {
             return -1;
        }
        
        /* Clean inputs */
        email = Algorithm.Clean(email);
        pwd = Algorithm.MD5(Algorithm.Clean(pwd));
        
        ResultSet rs = db.execute("select uid from users where email = '" +
               email + "' and hpwd = '" + pwd + "';");
       
       int uid = 0;
     
          /* Try to get uid from result */
          try {
             /* Select first (should be only) record */
             if (rs == null) {
                 return -2;
             }
             if (rs.next()) {
                 uid = rs.getInt("uid");
             }
         
             /* Free db resources */
             db.free();
         } catch (SQLException e) {
             return -3;
         }
         /* System.out.print(uid); */
         return uid;
     }
 
     
     /* Get database object for other classes. Returns the actual object--not a
        clone! */
     public Database getDb() {
         return this.db;
     }
     
     /* Create a new user. Returns uid; negative if unsuccessful  */
     public static int createUser(Database db, String uname, String pwd, 
         String email, double ht, double wt, char gender) {
         
         if ((db == null) || (uname == null) || (pwd == null) || (email == null)
              || (ht == 0) || (wt == 0)) {
             return -1;
         }
         
         /* Clean */
         uname = Algorithm.Clean(uname);
         pwd = Algorithm.Clean(pwd);
         String hpwd = Algorithm.MD5(pwd);
         email = Algorithm.Clean(email);
         gender = Character.toUpperCase(gender);
         
         /* check if the account already exists. */
         if (User.accountExists(db, email)) {
             return -4;
         }
         
         /* Insert new record */
         int success = db.update("insert into users (username, hpwd, email, " +
             "height, weight, gender) values ('" + uname + "', '" + hpwd + "', '"
              + email + "', " + ht + ", " + wt +", '" + gender + "');");
         /* Return error if somethign strange happened */
         if (success != 1) {
             return -2;
         }
         
         /* Now get the uid.. there might be a better way of doing this */
         int uid = User.login(db, email, pwd);
         
         /* If there was an error from User.login, return an error code */
         if (uid < 1) {
             return -3;
         }
         
         return uid;
     }
 
     /* Returns true if an account exists with the given email */
     public static boolean accountExists(Database db, String email) {
         
         if ((db == null) || (email == null)) {
             return false;
         }
         
         email = Algorithm.Clean(email);
         
         ResultSet rs = db.execute("select uid from users where email = '" +
             email + "';");
         
         try {
             if (rs == null) {
                 return false;
             }
             if (rs.next()) {
                 return true;
             } else {
                 return false;
             }
         } catch (SQLException e) {
             return false;
         }
         
     }
 
     
     /* Delete user. */
     public static boolean deleteUser(Database db, int uid) {
         
         if ( uid < 1) {
             return false;
         }
 
         int  r = db.update("DELETE * FROM users WHERE uid = '" +
               uid + "';");
 
 
         if (r < 1) {
             return false;
         }
         
         return true;
     }
     
     /* Sets instance variables for all properties from the database */
     public boolean getAllInfo() {
         
         ResultSet rs = this.db.execute("select username, email, height, weight"
             + ", gender, activity, blocks, hips, waist, wrist, leanBodyMass" +
             " from users where uid = " + this.uid + ";");
         
         /* Set variables */
         try {
             if (rs == null) {
                 return false;
             }
             if (rs.next()) {
                 this.username = rs.getString("username");
                 this.email = rs.getString("email");
                 this.height = rs.getDouble("height");
                 this.weight = rs.getDouble("weight");
                 this.wrist = rs.getDouble("wrist");
                 this.gender = rs.getString("gender").charAt(0);
                 this.activity = (short)rs.getInt("activity");
                 this.blocks = rs.getInt("blocks");
                 this.hips = rs.getDouble("hips");
                 this.waist = rs.getDouble("waist");
                 this.leanBodyMass = rs.getDouble("leanBodyMass");
             } else {
                 return false;
             }
             this.db.free();
         } catch (SQLException e) {
             return false;
         }
         
         return true;
         
     }
     
     public String getUsername() {
         /* Return pre-loaded copy, if available */
         if (this.username != null) {
             return this.username;
         }
     
         /* Get it */
         ResultSet rs = this.db.execute("select username from users where uid = "
             + this.uid + ";");
         
         String username;
     
         /* Try to find it */
         try {
             if (rs == null) {
                 return null;
             }
             if (rs.next()) {
                 username = rs.getString("username");
                 this.username = username;
             }
         
             /* Free db resources */
             this.db.free();
         } catch (SQLException e) {
             /* Some kind of error reporting */
         }
     
         return this.username;    
     }
 
     
     public String getEmail() {
         if (this.email != null) {
             return this.email;
         }
         
         ResultSet rs = this.db.execute("select email from users where uid = " +
             this.uid + ";");
         
         String email = null;
             
         try {
             if (rs == null) {
                 return null;
             }
             if (rs.next()) {
                 email = rs.getString("email");
             }
             this.db.free();
         } catch (SQLException e) {
             return null;
         }
         return email;
     }
     
     /* Do not access db. Use instance variable */
     public int getUid() {
         return this.uid;
     }
 
     public char getGender() {
         return this.gender;
     }
     
     public int getActivity() {
         return this.activity;
     }
     
     public int getBlocks() {
         return this.blocks;
     }
     
     public double getHips() {
         return this.hips;
     }
     
     public double getWaist() {
         return this.waist;
     }
     
     public double getWrist() {
         return this.wrist;
     }
     
     public double getLeanBodyMass() {
         return this.leanBodyMass;
     }
     
     public double getHeight() {
         if (this.height != 0) {
             return this.height;
         }
     
         ResultSet rs = this.db.execute("select height from users where uid = " +
             this.uid + ";");
         
         double height = 0;
             
         try {
             if (rs == null) {
                 return -1;
             }
             if (rs.next()) {
                 height = rs.getDouble("height");
             }
             this.db.free();
         } catch (SQLException e) {
             return 0;
         }
         return height;
     }
     
     public double getWeight() {
         if (this.weight != 0) {
             return this.weight;
         }
         
         ResultSet rs = this.db.execute("select weight from users  where uid = " +
             this.uid + ";");
         
         double weight = 0;
             
         try {
             if (rs == null) {
                 return -1;
             }
             if (rs.next()) {
                 weight = rs.getDouble("weight");
             }
             this.db.free();
         } catch (SQLException e) {
             return 0;
         }
         return weight;
     }
     
     /* Updates the database and instance variables with new information */
     public boolean updateAllInfo(String username, String email, char gender, 
         short activity, int blocks, double ht, double wt, double hips, 
         double waist, double wrist, double lbm) {
         if ((username == null) || (email == null) || (ht < 1) || (wt < 1) || 
             (hips < 1) || (waist < 1) || (wrist < 1) || (lbm < 1)) {
             return false;
         }
 
         this.username = Algorithm.Clean(username);
         this.email = Algorithm.Clean(email);
        this.gender = gender;
         this.activity = activity;
         this.blocks = blocks;
         this.height = ht;
         this.weight = wt;
         this.hips = hips;
         this.waist = waist;
         this.wrist = wrist;
         
         
         if (!User.accountExists(this.db, email)) {
             return false;
         }
         
         int up;
         
         up = this.db.update("update users set username='" + this.username +
             "', email='" + this.email + "', gender='" + gender + "', activity=" 
             + activity + ", blocks=" + blocks + ", height=" + ht + ", weight="
             + wt + ", hips=" + hips + ", waist=" + waist + ", wrist=" + wrist +
             ", leanBodyMass=" + lbm + " where uid = " + this.uid + ";");
 
         
         if (up < 1) {
             return false;
         }
         return true;
     }
     
     public boolean updateUsername(String uname) {
         this.username = Algorithm.Clean(uname);
  
         int up;
         
 
             up = this.db.update("update users set username='" + this.username +
              "' where uid = " + this.uid + ";");
 
         
         if (up < 1) {
             return false;
         }
         return true;
     }
     /* This shouldn't be used.  
     public boolean updatePwd(String pwd) {
         return false;
     } */
     
     public boolean updateEmail(String email) {
         this.email = Algorithm.Clean(email);
  
         if (User.accountExists(this.db, email)) {
             return false; 
         }
  
         int up;
         
 
             up = this.db.update("update users set email='" + this.email +
              "' where uid = " + this.uid + ";");
 
         
         if (up < 1) {
             return false;
         }
         return true;
     }
     
     public boolean updateHeight(double height) {
         this.height = height;
  
         int up;
         
             up = this.db.update("update users set height='" + this.height +
              "' where uid = " + this.uid + ";");
 
         
         if (up < 1) {
             return false;
         }
         return true;
     
     }
     
     public boolean updateWeight(double weight) {
         this.weight = weight;
  
         int up;
         
             up = this.db.update("update users set weight='" + this.weight +
              "' where uid = " + this.uid + ";");
         
         if (up < 1) {
             return false;
         }
         return true;
     }
     
     /* Changes the user's password provided that old is the user's old 
        password. */
     public boolean resetPassword(String old, String pwd) {
         old = Algorithm.MD5(Algorithm.Clean(old));
         pwd = Algorithm.MD5(Algorithm.Clean(pwd));
        
         System.out.print(old + " " + pwd + "\n");
  
         int up;
         
         String s = "update users set hpwd='" + pwd +
              "' where uid=" + this.uid + " and hpwd='" + old + "';";
         
         System.out.print(s + "\n");
         this.db.update("insert into users (username) values ('poop');");
         up = this.db.update(s);
 
         if (up < 1) {
             return false;
         }
         return true;
     }
     
 }
