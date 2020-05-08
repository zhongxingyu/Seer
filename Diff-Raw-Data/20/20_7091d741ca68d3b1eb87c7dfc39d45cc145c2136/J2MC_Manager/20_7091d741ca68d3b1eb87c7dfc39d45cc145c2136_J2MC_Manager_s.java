 package to.joe.j2mc.core;
 
 import to.joe.j2mc.core.MySQL.MySQL;
 import to.joe.j2mc.core.log.Log;
 import to.joe.j2mc.core.permissions.Permissions;
 import to.joe.j2mc.core.visibility.Visibility;
 
 public class J2MC_Manager {
     private static J2MC_Manager self = new J2MC_Manager();
 
     public static J2MC_Core getCore() {
         return J2MC_Manager.getInstance().core;
     }
 
     public static J2MC_Manager getInstance() {
         return J2MC_Manager.self;
     }
 
     public static Log getLog() {
         return J2MC_Manager.getInstance().log;
     }
 
     public static MySQL getMySQL() {
         return J2MC_Manager.getInstance().mySQL;
     }
 
     public static Permissions getPermissions() {
         return J2MC_Manager.getInstance().permissions;
     }
 
     public static int getServerID() {
         return J2MC_Manager.getInstance().serverID;
     }
 
     public static Visibility getVisibility() {
         return J2MC_Manager.getInstance().visibility;
     }
 
     private Log log;
 
     private MySQL mySQL;
 
     private Permissions permissions;
 
     private Visibility visibility;
 
     private J2MC_Core core;

     private int serverID;
 
     public void setCore(J2MC_Core core) {
         this.core = core;
     }
 
     public void setLog(Log log) {
         this.log = log;
     }
 
     public void setMySQL(MySQL mySQL) {
         this.mySQL = mySQL;
     }
 
     public void setPermissions(Permissions permissions) {
         this.permissions = permissions;
     }
 
     public void setServerID(int id) {
         this.serverID = id;
     }
 
     public void setVisibility(Visibility visibility) {
         this.visibility = visibility;
     }
 }
