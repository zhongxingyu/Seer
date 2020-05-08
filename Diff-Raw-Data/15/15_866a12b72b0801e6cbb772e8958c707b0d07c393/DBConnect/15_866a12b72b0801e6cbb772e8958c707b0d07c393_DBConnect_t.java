 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package trampoline;
 import java.sql.*;
 import java.util.*;
 
 /**
  *
  * @author Andreas
  */
 public class DBConnect {
     public Connection conn_;
     public Statement stat_;
     public ResultSet rs_;
     private MessageHandler messageHandler_; // Error Handler inherited from main class;
     
     DBConnect(MessageHandler errHandl) {
         this.messageHandler_ = errHandl;
         try {
             Class.forName("org.sqlite.JDBC");
             //connect to the database
             conn_ = DriverManager.getConnection("jdbc:sqlite:data/database");
             stat_ = conn_.createStatement();
         }
         catch (Exception e) {
             this.messageHandler_.setError(2);
             this.messageHandler_.setMoreDetails(e.toString());
         }
     }
     
     public int addTag(int gid, String tag){
         return executeUpdate("INSERT INTO tags (gymnastid, tname) "
                               + "VALUES ('"+gid+"' ,'"+tag+"')");
     }
     
     public int addClub(String name) {
         return executeUpdate("INSERT INTO clubs (name) VALUES ('"+name+"')");
     }
     
     public int addGymnast(String name, int dobDay, int dobMonth, int dobYear, String category, int cid) {
         String dobfull = dobYear+"-"+dobMonth+"-"+dobDay;
         
         return executeUpdate("INSERT INTO gymnasts (clubid, gname, dobday, dobmonth, dobyear, dobfull, category) "
                 + "VALUES ('"+cid+"', '"+name+"', '"+dobDay+"', '"+dobMonth+"', '"+dobYear+"', '"+dobfull+"', '"+category+"')");
     }
     
     public int addGymnast(String name, int dobDay, int dobMonth, int dobYear, String category) {
         //Default to Club 1 for a single-glub gym. 
         return addGymnast(name, dobDay, dobMonth, dobYear, category, 1);
     }
     
     private int addJump(int routineid, int jumpnumber, double b1, double en, double b2, double tof, double ton, double total, String location) {
         return executeUpdate("INSERT INTO jumps (routineid, jumpnumber, break1, engage, break2, tof, ton, total, location) "
                 + "VALUES ('"+routineid+"', '"+jumpnumber+"', '"+b1+"', '"+en+"', '"+b2+"', '"+tof+"', '"+ton+"', '"+total+"', '"+location+"')");
     }
     
     private int addJump(Jump j, int routineid, int jumpnumber) {
         return addJump(routineid, jumpnumber, j.getBreakStart(), j.getEngage(), j.getBreakEnd(), j.getTof(), j.getTon(), j.getTotal(), j.getLocation());
     }
     
     public int addRoutine(Routine r, int gid, String datetime) {
         int rid = executeUpdate("INSERT INTO routines (gymnastid, totaltof, totalton, totaltime, datetime, numberofjumps) "
             + "VALUES ('"+gid+"', '"+r.getTotalTof()+"', '"+r.getTotalTon()+"', '"+r.getTotalTime()+"', '"+datetime+"', '"+r.getNumberOfJumps()+"')");
         Jump[] jumpArray = r.getJumps();
         
         for (int i = 0; i < jumpArray.length; i++) {
             addJump(jumpArray[i], rid, i);
         }
         
         return rid;
     }
     
     public int addComments(int rid, String comments){
         return executeUpdate("UPDATE routines SET comments = '"+comments+"' WHERE rid = '"+rid+"'");
     }
     
     public void deleteGymnast(int gid) {
         executeQuery("DELETE FROM gymnasts WHERE gid = '"+gid+"'");
     }
     
     public int editGymnast(int gid, String name, int dobDay, int dobMonth, int dobYear, String category, int cid) {
         String dobfull = dobYear+"-"+dobMonth+"-"+dobDay;
         
         System.out.println("UPDATE gymnasts SET clubid = '"+cid+"', gname = '"+name+"', dobday = '"+dobDay+"', dobmonth = '"+dobMonth+"', "
                 + "dobyear = '"+dobYear+"', dobfull = '"+dobfull+"', category = '"+category+"' "
                 + "WHERE gid = '"+gid+"'");
         return executeUpdate("UPDATE gymnasts SET clubid = '"+cid+"', gname = '"+name+"', dobday = '"+dobDay+"', dobmonth = '"+dobMonth+"', "
                 + "dobyear = '"+dobYear+"', dobfull = '"+dobfull+"', category = '"+category+"' "
                 + "WHERE gid = '"+gid+"'");
     }
     
     public Map<Integer,String> getTags(int gid){
         executeQuery("SELECT * FROM tags WHERE gymnastid = '"+gid+"'");
         
         Map<Integer,String> tagMap = new HashMap<Integer,String>(10);
         
         try {
             while(rs_.next()){
                 tagMap.put(resultGetInt("tid"),resultGetString("tname"));
             }
             rs_.close();
         } catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
         
         return tagMap;
     }
     
     public Category[] getAllCategories() {
         executeQuery("SELECT * FROM categories");
         
         ArrayList<Category> categoryList = new ArrayList<Category>();
         
         try {
             while (rs_.next()) {
                 categoryList.add(new Category(resultGetInt("catid"), resultGetString("categoryname")));
             }
             rs_.close();
         }
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
         
         return categoryList.toArray(new Category[categoryList.size()]);
     }
     
    public int getCategoryID(String name) {
        Category c = getCategory(name);
        return c.getID();
    }
    
     public Category getCategory(String name) {
         executeQuery("SELECT * FROM categories WHERE categoryname = '"+name+"'");
         
         try {
             rs_.next();
             rs_.close();
         }
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
         
         return new Category(resultGetInt("catid"), resultGetString("categoryname"));
     }
     
     public Category getCategory(int id) {
         executeQuery("SELECT * FROM categories WHERE catid = '"+id+"'");
         
         try {
             rs_.next();
             rs_.close();
         }
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
         
         return new Category(resultGetInt("catid"), resultGetString("categoryname"));
     }
     
     public Gymnast getGymnast(int gid) {
         executeQuery("SELECT g.*, c.* FROM gymnasts g, clubs c WHERE g.gid = '"+gid+"' AND g.clubid = c.cid");
         
         return new Gymnast(resultGetInt("gid"), resultGetString("gname"), resultGetInt("cid"), resultGetString("cname"), resultGetInt("dobday"), resultGetInt("dobmonth"), resultGetInt("dobyear"), resultGetInt("category"));
     }
     
     public Gymnast[] getAllGymnasts() {
         executeQuery("SELECT g.*, c.* FROM gymnasts AS g, clubs AS c WHERE g.clubid = c.cid");
         
         ArrayList<Gymnast> gymnastList = new ArrayList<Gymnast>();
         
         try {
             while (rs_.next()) {
                 gymnastList.add(new Gymnast(resultGetInt("gid"), resultGetString("gname"), resultGetInt("cid"), resultGetString("cname"), resultGetInt("dobday"), resultGetInt("dobmonth"), resultGetInt("dobyear"), resultGetInt("category")));
             }
             rs_.close();
         }
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
         
         return gymnastList.toArray(new Gymnast[gymnastList.size()]);
     }
     
     public Club[] getAllClubs() {
         executeQuery("SELECT * FROM clubs");
         
         ArrayList<Club> clubList = new ArrayList<Club>();
         try {
             while (rs_.next()) {
                 clubList.add(new Club(resultGetInt("cid"), resultGetString("shortname"), resultGetString("longname"), resultGetString("addressline1"), resultGetString("addressline2"), resultGetString("town"), resultGetString("county"), resultGetString("postcode"),resultGetString("headcoach"),resultGetString("contactdetails")));
             }
             rs_.close();
         }
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
         return clubList.toArray(new Club[clubList.size()]);
     }
     
     public Jump getJump(int jid) {
         Jump j = new Jump();
         try {
             rs_ = stat_.executeQuery("SELECT * FROM jumps WHERE jid = '"+jid+"'");
             while (rs_.next()) {
                 System.out.println("new jump123");
                 j = new Jump(rs_.getInt("break1"), rs_.getInt("engage"), rs_.getInt("break2"), "A0");
             }
             rs_.close();
         }
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
         return j;
     }
     
     //If we know we've already got the jump row loaded into "rs_" then use this. 
     Jump getJump() {
         return new Jump(resultGetInt("break1"), resultGetInt("engage"), resultGetInt("break2"), "A0");
     }
     
     public Routine getRoutine(int rid) {
         executeQuery("SELECT * FROM routines WHERE rid = '"+rid+"'");
         int numberOfJumps = resultGetInt("numberofjumps");
 
         Routine r = new Routine(numberOfJumps, rid);
         //executeQuery("SELECT * FROM jumps WHERE routineid = '"+rid+"' ORDER BY jumpnumber ASC");
         
         try {
             rs_ = stat_.executeQuery("SELECT * FROM jumps WHERE routineid = '"+rid+"' ORDER BY jumpnumber ASC");
             while (rs_.next()) {
                 r.addJump(getJump());
             }
         }
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
         
         return r;
     }
     
     //Gets all a Gymnasts Routines
     public Routine[] getRoutinesForGymnast(int gid) {
         executeQuery("SELECT * FROM routines WHERE gymnastid = '"+gid+"'");
         
         ArrayList<String> idList = new ArrayList<String>();
         
         //First get the list of IDs.
         try {
             while (rs_.next()) {
                 idList.add(resultGetString("rid"));
             }
         }
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
         
         //Then, we have the list of IDs, so create an array of routines and return it. 
         Routine[] r = new Routine[idList.size()];
         for (int i = 0; i < idList.size(); i++) {
             r[i] = getRoutine(Integer.parseInt(idList.get(i)));
         }
         
         return r;
     }
     
     //We run this, then just use the class variable "rs_" to get our result set. 
     private boolean executeQuery(String s) {
         System.out.println("Execute: "+s);
         try {
             //conn_ = DriverManager.getConnection("jdbc:sqlite:data/database");
             //stat_ = conn_.createStatement();
             rs_ = stat_.executeQuery(s);
         }
 
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
 
         return true;
     }
     
     //We run this, then just use the class variable "rs_" to get our result set. 
     private int executeUpdate(String s) {
         int i = 0;
         System.out.println("Execute Update: "+s);
         try {
             stat_ = conn_.createStatement();
             i = stat_.executeUpdate(s);
         
             ResultSet keys = stat_.getGeneratedKeys();  
 
             keys.next();  
             i = keys.getInt(1);
         }
 
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
         
         System.out.println("Update:"+i);
         return i;
     }
     
     private int resultGetInt(String recordName) {
         int i = 0;
         
         try {
             i = rs_.getInt(recordName);
         }
 
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
 
         return i;
     }
     
     private String resultGetString(String recordName) {
         String s = "";
         
         try {
             s = rs_.getString(recordName);
         }
 
         catch (Exception e) {
             messageHandler_.setError(10);
             messageHandler_.setMoreDetails(e.toString());
         }
 
         return s;
     }
 }
