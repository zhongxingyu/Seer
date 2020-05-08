 package com.grandst.whiplash.api;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.client.ClientProtocolException;
 
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.grandst.whiplash.Whiplash;
 import com.grandst.whiplash.bean.Order;
 import com.grandst.whiplash.bean.OrderItem;
 
 public class OrderService {
 	
 	public static ArrayList<Order> getOrders(Whiplash w) throws ClientProtocolException, ParseException, IOException{
 		return parseOrderListJson(API.get("/orders.json", w));
 	}
 	
 	private static ArrayList<Order> parseOrderListJson(String apiJson) throws  ParseException{
		apiJson = cleanDateFormat(apiJson); // ugh! only Java 7+ supports date formats with Timezone X eg. yyyy-MM-dd'T'HH:mm:ssX so we need to change the format to yyyy-MM-dd'T'HH:mm:ssZ
 		ArrayList<Order> retList = new ArrayList<Order>();
 		JsonParser parser = new JsonParser();
		GsonBuilder gb = new GsonBuilder()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
 		Gson gson = gb.create();
 		JsonArray orderArray = parser.parse(apiJson).getAsJsonArray();
 		for(int i = 0; i < orderArray.size(); i++){
 			Order o = new Order();
 			JsonObject orderObj = orderArray.get(i).getAsJsonObject();
 			ArrayList<OrderItem> oiList = new ArrayList<OrderItem>();
 			JsonArray itemArray = orderObj.getAsJsonArray("order_items");
 			for(int j = 0; j<itemArray.size();j++){
 				OrderItem oi = new OrderItem();
 				oi = gson.fromJson(itemArray.get(j).getAsJsonObject(), OrderItem.class);
 				oiList.add(oi);
 			}
 			o = gson.fromJson(orderArray.get(i).getAsJsonObject(), Order.class);
 			o.setOrderItems(oiList);
 			retList.add(o);
 		}
 		return retList;	
 	}
 	
 	private static String cleanDateFormat(String json){
 		Pattern regex = Pattern.compile("\\d\\d:\\d\\d:\\d\\d[-\\+]\\d\\d:\\d\\d"); 
 		Matcher regexMatcher = regex.matcher(json);
 		StringBuffer buff = new StringBuffer();
 		while(regexMatcher.find()){
 			regexMatcher.appendReplacement(buff, getSubOfMatch(regexMatcher));
 		}
 		regexMatcher.appendTail(buff);
		return buff.toString();
 	}
 	
 	private static String getSubOfMatch(Matcher matcher){
 		StringBuilder sb = new StringBuilder(matcher.group(0));
 		sb.deleteCharAt(sb.length()-3);
 		return sb.toString();
 	}
 	
 	/* 
 	private static ArrayList<Order> parseOrderListJson(String apiJson) throws  ParseException{	
 		/*
 		JSONArray jar = new JSONArray(apiJson);
 		for(int i =0;i<jar.size();i++){
 			JSONObject orderObj = jar.getJSONObject(i);
 			Order o = new Order();
 			if(orderObj.has("shipping_city") && !orderObj.isNull("shipping_city"))
 				o.setShippingCity(orderObj.getString("shipping_city"));
 			if(orderObj.has("shipping_name") && !orderObj.isNull("shipping_name"))
 				o.setShippingName(orderObj.getString("shipping_name"));
 			if(orderObj.has("shipping_company")  && !orderObj.isNull("shipping_company"))
 				o.setShippingCompany(orderObj.getString("shipping_company"));
 			if(orderObj.has("shipping_address_1")  && !orderObj.isNull("shipping_address_1"))
 				o.setShippingAddress(orderObj.getString("shipping_address_1"));
 			if(orderObj.has("shipping_address_2")  && !orderObj.isNull("shipping_address_2"))
 				o.setShippingAddress2(orderObj.getString("shipping_address_2"));
 			if(orderObj.has("shipping_zip")  && !orderObj.isNull("shipping_zip"))
 				o.setShippingZip(orderObj.getString("shipping_zip"));
 			if(orderObj.has("shipping_country")  && !orderObj.isNull("shipping_country"))
 				o.setShippingCountry(orderObj.getString("shipping_country"));
 			if(orderObj.has("shipping_phone")  && !orderObj.isNull("shipping_phone"))
 				o.setShippingName(orderObj.getString("shipping_phone"));
 			if(orderObj.has("ship_3rdparty_account")  && !orderObj.isNull("ship_3rdparty_account"))
 				o.setShip3rdPartyAccount(orderObj.getString("ship_3rdparty_account"));
 			if(orderObj.has("ship_3rdparty_country") && !orderObj.isNull("ship_3rdparty_country"))
 				o.setShip3rdPartyCountry(orderObj.getString("ship_3rdparty_country"));
 			if(orderObj.has("ship_3rdparty_cost") && !orderObj.isNull("ship_3rdparty_cost"))
 				o.setShip3rdPartyCost(new BigDecimal(orderObj.getString("ship_3rdparty_cost")));
 			if(orderObj.has("ship_3rdparty_zip")  && !orderObj.isNull("ship_3rdparty_zip"))
 				o.setShip3rdPartyZip(orderObj.getString("ship_3rdparty_zip"));
 			if(orderObj.has("req_ship_method_code")  && !orderObj.isNull("req_ship_method_code"))
 				o.setReqShipMethodCode(orderObj.getString("req_ship_method_code"));
 			if(orderObj.has("req_ship_method_price") && !orderObj.isNull("req_ship_method_price"))
 				o.setReqShipMethodPrice(new BigDecimal(orderObj.getString("req_ship_method_price")));
 			if(orderObj.has("ship_notes")  && !orderObj.isNull("ship_notes"))
 				o.setShipNotes(orderObj.getString("ship_notes"));
 			if(orderObj.has("created_at")  && !orderObj.isNull("created_at")){
 				DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
 				DateTime dt = formatter.parseDateTime(orderObj.getString("created_at"));
 				o.setCreatedAt(dt.toDate());
 			}
 			if(orderObj.has("shipped_on")  && !orderObj.isNull("shipped_on")){
 				DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
 				DateTime dt = formatter.parseDateTime(orderObj.getString("shipped_on"));
 				o.setShippedOn(dt.toDate());
 			}
 			if(orderObj.has("updated_at")  && !orderObj.isNull("updated_at")){
 				DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
 				DateTime dt = formatter.parseDateTime(orderObj.getString("updated_at"));
 				o.setUpdatedAt(dt.toDate());
 			}
 			if(orderObj.has("tracking_sent"))
 				o.setTrackingSent(orderObj.getBoolean(("tracking_sent")));
 			if(orderObj.has("tracking")  && !orderObj.isNull("tracking"))
 				o.setTracking(orderObj.getString(("tracking")));
 			if(orderObj.has("ship_method")  && !orderObj.isNull("ship_method"))
 				o.setShipMethod(orderObj.getString(("ship_method")));
 			if(orderObj.has("originator")  && !orderObj.isNull("originator"))
 				o.setOriginiator(orderObj.getString(("originator")));
 			if(orderObj.has("billed"))
 				o.setBilled(orderObj.getBoolean(("billed")));
 			if(orderObj.has("ship_actual_cost") && !orderObj.isNull("ship_actual_cost"))
 				o.setShipActualCost(new BigDecimal(orderObj.getString(("ship_actual_cost"))));
 			if(orderObj.has("originator_notified"))
 				o.setOriginatorNotified(orderObj.getBoolean(("originator_notified")));
 			if(orderObj.has("originator_id")  && !orderObj.isNull("originator_id"))
 				o.setOriginatorId(orderObj.getString(("originator_id")));
 			if(orderObj.has("status"))
 				o.setStatus(orderObj.getLong(("status")));
 			if(orderObj.has("order_orig")  && !orderObj.isNull("order_orig"))
 				o.setOrderOrig(orderObj.getString(("order_orig")));
 			if(orderObj.has("gift"))
 				o.setGift(orderObj.getBoolean(("gift")));
 			if(orderObj.has("public_note")  && !orderObj.isNull("public_note"))
 				o.setPublicNote(orderObj.getString(("public_note")));
 			if(orderObj.has("email")  && !orderObj.isNull("email"))
 				o.setEmail(orderObj.getString(("email")));
 			if(orderObj.has("order_items") && !orderObj.isNull("order_items")){
 				ArrayList<OrderItem> itemList = new ArrayList<OrderItem>();
 				JSONArray itemArray = orderObj.getJSONArray("order_items");
 				for(int j = 0; j<itemArray.size();j++){
 					OrderItem oi = new OrderItem();
 					JSONObject itemObj = itemArray.getJSONObject(j);
 					if(itemObj !=null){
 						if(itemObj.has("price") && !itemObj.isNull("price"))
 							oi.setPrice(new BigDecimal(itemObj.getString("price")));
 						if(itemObj.has("unshippable"))
 							oi.setUnshippable(itemObj.getBoolean("unshippable"));
 						if(itemObj.has("available"))
 							oi.setAvailable(itemObj.getBoolean("available"));
 						if(itemObj.has("quantity"))
 							oi.setQuantity(itemObj.getInt("quantity"));
 						if(itemObj.has("order_id"))
 							oi.setOrderId(itemObj.optLong("order_id"));
 						if(itemObj.has("packed"))
 							oi.setPacked(itemObj.optInt("packed"));
 						if(itemObj.has("picked"))
 							oi.setPicked(itemObj.optInt("picked"));
 						if(itemObj.has("id"))
 							oi.setId(itemObj.optLong("id"));
 						if(itemObj.has("packaging"))
 							oi.setPackaging(itemObj.optBoolean("packaging"));
 						if(itemObj.has("sku") && !itemObj.isNull("sku"))
 							oi.setSku(itemObj.optString("sku"));
 						if(itemObj.has("description") && !itemObj.isNull("description"))
 							oi.setDescription(itemObj.optString("description"));
 						if(itemObj.has("originator_id"))
 							oi.setOriginatorId(itemObj.optLong("originator_id"));
 						if(itemObj.has("item_id"))
 							oi.setItemId(itemObj.optLong("item_id"));
 						if(itemObj.has("updated_at")  && !itemObj.isNull("updated_at")){
 							DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
 							DateTime dt = formatter.parseDateTime(itemObj.getString("updated_at"));
 							oi.setUpdatedAt(dt.toDate());
 						}
 						if(itemObj.has("created_at")  && !itemObj.isNull("created_at")){
 							DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
 							DateTime dt = formatter.parseDateTime(itemObj.getString("created_at"));
 							oi.setCreatedAt(dt.toDate());
 						}
 					}
 					itemList.add(oi);
 				}
 				o.setOrderItems(itemList);
 			}
 			retList.add(o);
 		}
 		return retList;
 	}
 	*/
 
 }
