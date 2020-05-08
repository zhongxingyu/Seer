 package me.walkable.groupon;
 
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
 public class GrouponParse {
 
 
 	public static GrouponObject parse(String json){
 		Gson gson = new GsonBuilder().create();
 		GrouponObject object = gson.fromJson(json, GrouponObject.class);
 		return object;
 	}
 
 	public static void InsertGrouponData(GrouponObject obj){
 		Connection conn = null;
 		GrouponData[] deals = obj.deals;
 
 		try {
 			conn = DatabaseUtil.getConnection();
 
 			for (GrouponData deal : deals){
 //				Merchant merchant = new Merchant();
 //				merchant.setName(deal.merchant.name);
 //				merchant.setUrl(deal.merchant.websiteUrl);
 //				merchant.setGroupon_id(deal.id);
 
 				//This is sometimes a yelp ID
 //				if (deal.merchant.ratings.length > 0) {
 //					if (deal.merchant.ratings[0].linkText.equals("Yelp")){
 //						String url = deal.merchant.ratings[0].url;
 //						int lastSlash = url.lastIndexOf("/");
 //						if (lastSlash <= 0){
 //							System.out.println("Error getting Yelp ID");
 //						}
 //						else {
 //							String yelpID = url.substring(lastSlash+1, url.length());
 //							merchant.setYelp_id(yelpID);
 //						}
 //					}
 //					else {
 //						System.out.println("Ratings from " + deal.merchant.ratings[0].linkText);
 //					}
 //				}
 //				else {
 //					//System.out.println("No Ratings Data in Groupon Deal");
 //				}
 //
 //				int mid = merchant.insertMerchant(conn);
 
 				for (GrouponData.GrouponOptions opt : deal.options){
 					for (GrouponData.GrouponLocation gLoc : opt.redemptionLocations){
 						//Pass mid into location
 						Location location = new Location();
 						location.setStreet(gLoc.streetAddress1);
 						location.setStreet2(gLoc.streetAddress2);
 						location.setNeighborhood(gLoc.neighborhood);
 						location.setZip(gLoc.postalCode);
 						location.setLat(gLoc.lat);
 						location.setLng(gLoc.lng);
 						location.setName(deal.merchant.name);
 						location.setUrl(deal.merchant.websiteUrl);
 						location.insertLocation(conn);
 					}
 
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) { /*ignored*/ }
 			}
 
 			//			String insert2 = "INSERT INTO deals "
 			//					+ "VALUES()";
 			//			String insert3 = "INSERT INTO groupon "
 			//					+ "VALUES('did', 'small_image', 'med_image', 'grid_image', 'pitch', 'highlights', 'placement_priority', 'announcement', 'location_note', 'ship_req', 'is_now_deal', 'fine_print')";
 
 		}
 
 
 	}
 
 }
