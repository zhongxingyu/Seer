 package com.virtual.market;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.GetDataCallback;
 import com.parse.ParseException;
 import com.parse.ParseFile;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class Shopping_cart extends Activity {
 	static ArrayList<ItemDetails>  itemDetails;
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_shopping_cart);
         
         itemDetails= new ArrayList<ItemDetails>(); 
     	final ParseUser current_user = ParseUser.getCurrentUser();
     	String userId= current_user.getObjectId();
     	System.out.println(userId);
     	   	
     	final ListView lv1 = (ListView) findViewById(R.id.listV_main);
     	
     	
     	ParseQuery getShoppingCart = new ParseQuery("Cart");
     	getShoppingCart.whereEqualTo("user_id", userId);
     	getShoppingCart.whereEqualTo("bill_id", null);
     	getShoppingCart.findInBackground(new FindCallback() {
 			
 			@Override
 			public void done(List<ParseObject> objectList, ParseException e) {
 				// TODO Auto-generated method stub
 				if (e == null){
 					for (int i = 0; i < objectList.size(); i++) {
 						final ItemDetails item = new ItemDetails();
 						String itemId= objectList.get(i).getString("item_id");
 						System.out.println(itemId);
 						item.setAmount(objectList.get(i).getNumber("item_amount").toString());
 						ParseQuery itemInfo = new ParseQuery("Item");
 						itemInfo.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
 						itemInfo.whereEqualTo("objectId", itemId);
 						itemInfo.getFirstInBackground(new GetCallback() {
 							public void done(ParseObject object,ParseException e) {
 								if (object == null) {
 									Log.d("item_info","The get First request failed.");
 								} else {
 									Log.d("Item Description", object.getString("item_desc"));
 									item.setItemDescription(object.getString("item_desc"));
 									item.setName(object.getString("item_name"));
 									item.setPrice(object.getString("item_price"));
 									ParseFile image = (ParseFile) object.get("item_image");
 									image.getDataInBackground(new GetDataCallback() {
 										@Override
 										public void done(byte[] imageInBytes,ParseException pEx) {
 											// TODO Auto-generated method stub
 											Bitmap bmp = BitmapFactory.decodeByteArray(
 											imageInBytes, 0,
 											imageInBytes.length);
 											item.setBmp(bmp);
 											itemDetails.add(item);
 											lv1.setAdapter(new ItemListBaseAdapter(Shopping_cart.this, itemDetails));
 											}
 										});
 									
 									System.out.println(item);
 									}
 								}
 							});
 							
 						}
 						
 					} else {
 						Log.d("score", "Error: " + e.getMessage());
 						System.out.println("error");
 					}
 				}
 				
 				
 			});
     	
               
         Button checkout= (Button) findViewById(R.id.check_out);
         OnClickListener checkout_lsn = new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				System.out.println(itemDetails.size());
 		    	Double totalSum = 0.0;
 		    	for (int i=0;i<itemDetails.size();i++){
 		        	
 		        	Double itemPrice = 0.0;
 		        	String itemName=itemDetails.get(i).getName();
 		        	final String itemAmount=itemDetails.get(i).getAmount();
 		        	ParseQuery updateItemQuantity = new ParseQuery("Item");
 		        	updateItemQuantity.whereEqualTo("item_name", itemName);
 		        	
 		        	updateItemQuantity.getFirstInBackground(new GetCallback(){
 						public void done(ParseObject object,ParseException e) {
 							if (object == null) {
 								Log.d("item_info","The get First request failed.");
 							} else {
 								String itemStock=object.get("item_amount").toString();
 								System.out.println(itemStock);
 								Number currentItemStock = Integer.valueOf(itemStock)-Integer.valueOf(itemAmount);
 								System.out.println(currentItemStock);
 								object.put("item_amount", currentItemStock);
 								object.saveInBackground();
 							}
 						}
 					});
 						// TODO Auto-generated catch block
 					
 		        	itemPrice=Double.valueOf(itemDetails.get(i).getPrice())*Integer.valueOf(itemDetails.get(i).getAmount());
 		        	totalSum += itemPrice;
 		        	System.out.println(totalSum);
 		        
 		        }
 		        final Double summation = totalSum;
 		        
 		        ParseObject bill = new ParseObject("Bill");
 		        bill.put("total_cost",totalSum.toString());
 		        Calendar c = new GregorianCalendar();
 		        c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
 		        c.set(Calendar.MINUTE, 0);
 		        c.set(Calendar.SECOND, 0);
 		        
 		        bill.put("user_id", current_user.getObjectId().toString());
 		        bill.put("bill_status","done");
 		        bill.put("payment_status","pending");
 		        bill.put("bill_date", c.getTime().toLocaleString());
 		        bill.saveInBackground();
 		        ParseQuery billID= new ParseQuery("Bill");
 		        billID.whereEqualTo("user_id", current_user.getObjectId().toString());
 		        //billID.whereEqualTo("bill_date", c.getTime().toLocaleString());
 		        billID.getFirstInBackground(new GetCallback() {
 					
 					@Override
 					public void done(final ParseObject object, ParseException e) {
 						// TODO Auto-generated method stub
 						if (object == null) {
 							Log.d("bill was not saved","The get First request failed");
 						} else {
 							
 							//getShoppingCart.put("createdAt", java.util.Date.parse("8/1/2013"));
 							
 							ParseQuery cart = new ParseQuery("Cart");
 							cart.whereEqualTo("user_id", current_user.getObjectId().toString());
 							cart.findInBackground(new FindCallback() {
 								
 								@Override
 								public void done(List<ParseObject> objects, ParseException e) {
 									for (int i = 0; i < objects.size(); i++) {
 										final ParseObject ShoppingCart = new ParseObject("Cart");
 										ShoppingCart.put("user_id", current_user.getObjectId().toString());
 										System.out.println(current_user.getObjectId().toString());
 										ShoppingCart.put("item_id",objects.get(i).getString("item_id"));
 										System.out.println(objects.get(i).getString("item_id"));
 										ShoppingCart.put("item_amount", objects.get(i).getNumber("item_amount"));
 										ShoppingCart.put("bill_id", "1");
 										System.out.println(objects.get(i).getNumber("item_amount"));
 										System.out.println(object.getObjectId().toString());
 										final String bill_id = object.getObjectId().toString();
 										ShoppingCart.saveInBackground(new SaveCallback() {
 										
 											@Override
 											public void done(ParseException e) {
 												// TODO Auto-generated method stub
 												//System.out.println(object.getObjectId().toString());
 												ShoppingCart.put("bill_id", bill_id);
 												
 												ShoppingCart.saveInBackground();
 											}
 										});
 										
 									}
 									AlertDialog diaBoxmessage = ShowDialogBox("Total sum "+ summation + " L.E."+"\n\n Bill ID: "+ object.getObjectId().toString());
 									
 									diaBoxmessage.show();
 									// TODO Auto-generated method stub
 									
 									
 								}
 							});
 							
 							
 						
 						}
 							
 						
 					}
 				});
 		       
 		        
 //		        System.out.println(bill.getObjectId().toString());
 				
 				
 			}
         };
         
         checkout.setOnClickListener(checkout_lsn);
     }
     
     private AlertDialog ShowDialogBox(String message) {
 
 		AlertDialog myQuittingDialogBox =
 
 		new AlertDialog.Builder(this)
 				.setTitle("Notification")
 				.setMessage(message)
 
 				.setPositiveButton("Okay",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 							}
 						})
 
 				.create();
 
 		return myQuittingDialogBox;
 	}
 }
