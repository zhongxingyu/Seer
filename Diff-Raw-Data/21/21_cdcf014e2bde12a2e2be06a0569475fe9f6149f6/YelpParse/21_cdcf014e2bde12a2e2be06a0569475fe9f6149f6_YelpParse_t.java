 package me.walkable.yelp;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
import me.walkable.db.*;
 
 /**
  * 
  */
 
 /**
  * @author Christopher Butera
  *
  */
 public class YelpParse {
 
 	public static YelpDealObject parse(String json){
 		Gson gson = new GsonBuilder().create();
 		YelpDealObject object = gson.fromJson(json, YelpDealObject.class);
 		InsertYelpData(object);
 		return object;
 	}
 
 	public static void InsertYelpData(YelpDealObject obj){
 		Connection conn = null;
 		YelpDealData[] deals = obj.businesses;
 
 		try {
 			conn = DatabaseUtil.getConnection();
 
 			for (YelpDealData deal : deals){
 //				Merchant merchant = new Merchant();
 //				merchant.setName(deal.name);
 ////				merchant.setUrl(deal.url);  NO URL Provided by Yelp
 //				merchant.setYelp_id(deal.id);
 //
 //				int mid = merchant.insertMerchant(conn);
 
 				YelpDealData.YelpLocation yLoc = deal.location;
 				Location location = new Location();
 				//Parse Yelp Address ToDo
 				if (yLoc.display_address.length > 0) {
 					location.setStreet(yLoc.display_address[0]);
 					if (yLoc.display_address.length > 1){
 						location.setStreet2(yLoc.display_address[1]);
 						if (yLoc.display_address.length > 2){
 							System.out.println("More than 2 lines of display address");
 						}
 					}
 				}
 				//				location.setNeighborhood(yLoc);  //Is there Neighborhood data?
 				//				location.setZip(yLoc.); //Zip unfortunately is encoded with display address
 				location.setLat(yLoc.coordinate.latitude);
 				location.setLng(yLoc.coordinate.longitude);
 				location.setName(deal.name);
 				//No URL Provided by Yelp
 				location.insertLocation(conn);
 			}
 
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) { /*ignored*/ }
 			}
 		}
 	}
 }
 
 
 
 
