 package edu.thu.cslab.footwith.server;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bxl
  * Date: 3/15/13
  * Time: 10:24 AM
  * To change this template use File | Settings | File Templates.
  */
 public class SiteManager {
     public SiteManager() {
     }
     public int addSite(Site site) throws SQLException {
 //        assert site.getSiteName().length()<40;
 //        assert !Mediator.getAllLocations().contains(site.getLocation());
 
         String SQLCommand = null;
         DBUtil du = DBUtil.getDBUtil();
         SQLCommand = " insert into " + tableName + " ( siteName, rate, location, brief, picture) " +
                 " values ( '"+ site.getSiteName()+"' , "+ site.getRate()+ " ,'"+ site.getLocation()+ "' , " + site.getBrief()+ " , " + site.getPicture() + " ) ";
         ResultSet rs=du.executeUpdate(SQLCommand);
         rs.next();
         return rs.getInt(1);
     }
 
     /**
      * get all site information
      * @param
      * @return site information vector
      * @throws SQLException
      */
     public Vector<Site> getAllSite() throws SQLException {
         Vector<Site> sites=new Vector<Site>();
         String SQLCommand="select * from "+tableName+";";
         ResultSet rs=DBUtil.getDBUtil().executeQuery(SQLCommand);
         while(rs.next()){
             sites.add(new Site(rs.getInt("siteID"), rs.getString("siteName"), rs.getInt("rate"),rs.getString("location"), rs.getString("brief"), rs.getInt("picture")));
         }
         return sites;
     }
 
     /**
      *
      * @param siteName
      * @return
      * @throws TextFormatException
      * @throws SQLException
      */
     public Site seleteSite(String siteName) throws TextFormatException, SQLException {
        assert siteName.length()>40;
         Site site;
         DBUtil du = DBUtil.getDBUtil();
         String SQLCommand = null;
         ResultSet rs;
         if(siteName == null)
             throw new TextFormatException("siteName is null");
         SQLCommand  = " select * from " + tableName + " where siteName = '" + siteName + "';";
         System.out.println(SQLCommand);
         rs=du.executeQuery(SQLCommand);
         //while(rs.next()){
         rs.next();
         site = new Site();
         site.setSiteID(rs.getInt("siteID"));
         site.setSiteName(rs.getString("siteName"));
         site.setRate(rs.getInt("rate"));
         site.setLocation(rs.getString("location"));
         site.setBrief(rs.getString("brief"));
         site.setPicture(rs.getInt("picture"));
         //}
         return site;
     }
     public Site seleteSite(int siteID) throws TextFormatException, SQLException {
         Site site;
         DBUtil du = DBUtil.getDBUtil();
         String SQLCommand = null;
         ResultSet rs;
         if(siteID < 0)
             throw new TextFormatException("siteID is null");
         SQLCommand  = " select * from " + tableName + " where siteID = " + siteID;
         rs=du.executeQuery(SQLCommand);
         //while(rs.next()){
         rs.next();
         site =new Site();
         site.setSiteID(rs.getInt("siteID"));
         site.setSiteName(rs.getString("siteName"));
         site.setRate(rs.getInt("rate"));
         site.setLocation(rs.getString("location"));
         site.setBrief(rs.getString("brief"));
         site.setPicture(rs.getInt("picture"));
         //}
         return site;
     }
     public Vector<Site> selectSite(Site site) throws TextFormatException, SQLException {
         DBUtil du = DBUtil.getDBUtil();
         String SQLCommand = null;
         ResultSet rs;
         Vector <Site> sites=new Vector<Site>();
         boolean isAnd = false;
         if(site==null)
             throw new TextFormatException();
         SQLCommand  = " select * from " + tableName + " where ";
         if(site.getLocation() != null){
             if(isAnd)
                 SQLCommand += " and ";
             SQLCommand += " location = '" + site.getLocation() + "'";
             isAnd = true;
         }
         if(site.getBrief() != null){
             if(isAnd)
                 SQLCommand += " and ";
             SQLCommand += " brief = '" + site.getBrief() + "'";
             isAnd = true;
 
         }
         if(site.getRate()!=-1){
             if(isAnd)
                 SQLCommand += " and ";
             SQLCommand += " rate = " + site.getRate();
             isAnd = true;
         }
         System.out.println(SQLCommand);
         rs=du.executeQuery(SQLCommand);
         while (rs.next()){
              sites.add(new Site(rs.getInt("siteID"), rs.getString("siteName"), rs.getInt("rate"),rs.getString("location"), rs.getString("brief"), rs.getInt("picture")));
         }
         return sites;
     }
     public void deleteSite(String siteName) throws TextFormatException, SQLException {
         Site site=new Site();
         DBUtil du = DBUtil.getDBUtil();
         String SQLCommand = null;
         if(siteName == null)
             throw new TextFormatException("siteName is null");
         SQLCommand  = " delete from " + tableName + " where siteName = '" + siteName + "'";
         du.executeUpdate(SQLCommand);
 
     }
     public void deleteSite(int siteID) throws TextFormatException, SQLException {
         Site site=new Site();
         DBUtil du = DBUtil.getDBUtil();
         String SQLCommand = null;
         if(siteID < 0)
             throw new TextFormatException("siteID is null");
         SQLCommand  = " delete from " + tableName + " where siteID = " + siteID;
         du.executeUpdate(SQLCommand);
     }
 
     public void editSite(String siteName, Site new_site) throws TextFormatException, SQLException {
         DBUtil du = DBUtil.getDBUtil();
         String SQLCommand = null;
         boolean isComma = false;
         if(siteName == null)
             throw new TextFormatException("siteName is null");
         SQLCommand  = " update " + tableName + " set ";
         if(new_site.getLocation() != null){
             SQLCommand += " location = '" + new_site.getLocation()+"'";
             isComma = true;
         }
         if(new_site.getBrief() != null){
             if(isComma)
                 SQLCommand += " , ";
             SQLCommand += " brief = '" + new_site.getBrief() + "'";
             isComma = true;
 
         }
         if(new_site.getRate()!=-1){
             if(isComma)
                 SQLCommand += " , ";
             SQLCommand += " rate = " + new_site.getRate();
             isComma = true;
         }
         if(new_site.getPicture() != -1){
             if(isComma)
                 SQLCommand += " , ";
             SQLCommand += " picture = '" + new_site.getPicture() +"'";
             isComma = true;
         }
 
         SQLCommand += " where siteName = '" + siteName +"'";
         du.executeUpdate(SQLCommand);
 
     }
     public void editSite(int siteID, Site new_site) throws TextFormatException, SQLException {
         DBUtil du = DBUtil.getDBUtil();
         String SQLCommand = null;
         boolean isComma = false;
         if(siteID < 0)
             throw new TextFormatException("siteName is null");
         SQLCommand  = " update " + tableName + " set ";
         if(new_site.getLocation() != null){
             SQLCommand += " location = '" + new_site.getLocation() + "'";
             isComma = true;
         }
         if(new_site.getBrief() != null){
             if(isComma)
                 SQLCommand += " , ";
             SQLCommand += " brief = '" + new_site.getBrief() + "'";
             isComma = true;
 
         }
         if(new_site.getRate()!=-1){
             if(isComma)
                 SQLCommand += " , ";
             SQLCommand += " rate = " + new_site.getRate();
             isComma = true;
         }
         if(new_site.getPicture() != -1){
             if(isComma)
                 SQLCommand += " , ";
             SQLCommand += " picture = '" + new_site.getPicture() + "'";
             isComma = true;
         }
 
         SQLCommand += " where siteID = " + siteID;
         du.executeUpdate(SQLCommand);
 
     }
 
     private final String tableName ="site";
 }
