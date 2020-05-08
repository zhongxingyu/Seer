 package at.fakeroot.sepm.server;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.xml.crypto.dsig.keyinfo.PGPData;
 
 import org.postgresql.geometric.PGpoint;
 import at.fakeroot.sepm.client.serialize.BoundingBox;
 import at.fakeroot.sepm.client.serialize.SearchResult;
 
 /**
  * @author Anca Cismasiu
  * Class representing the DAO for the GeoObjects, that reads from and writes to the database
  * Based on the Singleton pattern.
  */
 public class GeoObjectManager 
 {
 	private static GeoObjectManager geoObjManager = null;
 	private DBConnection dbConn;
 
 	/**
 	 * Constructor that establishes the DB Connection
 	 * */
 	private GeoObjectManager()
 	{
 		try
 		{
 			dbConn = new DBConnection();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Method used for obtaining an instance of this Singleton class
 	 * */	
 	public static GeoObjectManager getInstance()
 	{
 		if(geoObjManager == null)
 			geoObjManager = new GeoObjectManager();
 		return geoObjManager;
 	}
 
 
 	
 	/**
 	 * Method used to check if the DBGeoObject already exists in the database
 	 * @param obj DBGeoObject object we are searching for
 	 * @return DBGeoObject the existing object, or null if there is no previous record in the database with these values
 	 * */
 
 	public DBGeoObject select(DBGeoObject obj)
 	{	ResultSet rs1=null;
 	ResultSet rs2=null;
 	ResultSet rs3=null;
 	DBGeoObject rc=null;
 
 	try {	
 		PreparedStatement pstmt1 = dbConn.prepareStatement("SELECT * FROM geoObject WHERE obj_id= ? AND svc_id = ? AND uid = '?' ");
 		pstmt1.setLong(1, obj.getId());
 		pstmt1.setInt(2, obj.getSvc_id());
 		pstmt1.setString(3, obj.getUid());
 		rs1=pstmt1.executeQuery();
 		pstmt1.close();
 		dbConn.disconnect();
 
 		PreparedStatement pstmt2 = dbConn.prepareStatement("SELECT name, value FROM objectProperty WHERE obj_id = ?");
 		pstmt2.setLong(1, obj.getId());
 		rs2=pstmt2.executeQuery();
 		pstmt2.close();
 		dbConn.disconnect();
 		rs2.last();
 		int propRowCount = rs2.getRow();
 		rs2.first();
 		
 
 		PreparedStatement pstmt3 = dbConn.prepareStatement("SELECT tag FROM objectTag WHERE obj_id=?");
 		pstmt3.setLong(1, obj.getId());
 		rs3=pstmt3.executeQuery();
 		rs3=pstmt3.executeQuery();
 		rs3.last();
 		int tagRowCount = rs3.getRow();
 		rs3.first();
 		pstmt3.close();
 		dbConn.disconnect();
 
 		if(rs1.next()){
 			PGpoint location = (PGpoint)rs1.getObject(6);
 
 			Property[] properties= new Property[propRowCount];
 			int i=0;
 			while(rs2.next()){
 				properties[i]=new Property(rs2.getString(2), rs2.getString(3));
 				i++;
 			}
 
 			String[] tags = new String[tagRowCount];
 			int j=0;
 			while(rs3.next()){
 				tags[j]=rs3.getString(2);
 				j++;
 			}
 
 			rc= new DBGeoObject(rs1.getInt(1), rs1.getString(4), location.x ,location.y, rs1.getInt(2), rs1.getString(3), rs1.getString(5),rs1.getString(7), properties, tags);
 		}
 
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
 
 	return rc;
 	}
 
 	
 	
 	/**
 	 * Method used to retrieve a limited number of ClientGeoObjects having a set of tags and lying in a particular BoundingBox
 	 * @param tags String[] the desired tags
 	 * @param box BoundingBox the search area
 	 * @param limit int the number of retrieved results 
 	 * */
 	public SearchResult select(String[] tags, BoundingBox box, int limit)
 	{	SearchResult searchResult = new SearchResult();
 		
 	
 	
 		///TODO 
 	
 	
 	
 	
 		return searchResult;
 	}
 
 	
 	
 	/**
 	 * Insert a new object in the Database
 	 * @param obj DBGeoObject object to be inserted
 	 * */
 	public void insert (DBGeoObject obj)
 	{
 		PreparedStatement pstmt = dbConn.prepareStatement("INSERT INTO geoObject(svc_id, uid, title, link, pos) VALUES (?, '?', '?', '?', ?)");
 		pstmt.setInt(1, obj.getSvc_id());
 		pstmt.setString(2, obj.getUid());
 		pstmt.setString(3, obj.getTitle());
 		pstmt.setString(4, obj.getLink());
 		pstmt.setObject(5, new PGpoint(obj.getXPos(), obj.getYPos()));
 		pstmt.executeUpdate();
 		pstmt.close();
 		dbConn.disconnect();
 	}
 
 	
 	
 	/**
 	 * Update an object in the database
 	 * @param the new object, that will overwrite the old one with the same object id (obj_id)
 	 * */
 	public void update (DBGeoObject obj)
 	{
 		PreparedStatement pstmt = dbConn.prepareStatement("UPDATE geoObject SET svc_id =?, uid='?', title = '?', link='?', pos='(?,?)' WHERE ID = ?");
 		pstmt.setInt(1, obj.getSvc_id());
 		pstmt.setString(2, obj.getUid());
 		pstmt.setString(3, obj.getTitle());
 		pstmt.setString(4, obj.getLink());
		pstmt.setDouble(5, obj.getXPos())
 		pstmt.setDouble(6, obj.getYPos());
 		pstmt.setLong(7, obj.getId());
 		pstmt.executeUpdate();
 		pstmt.close();
 		dbConn.disconnect();
 	}
 }
