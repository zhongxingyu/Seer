 package com.virtual.market;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.GetDataCallback;
 import com.parse.ParseException;
 import com.parse.ParseFile;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 
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
     	ParseUser current_user = ParseUser.getCurrentUser();
     	String userId= current_user.getObjectId();
     	System.out.println(userId);
     	
     	final ListView lv1 = (ListView) findViewById(R.id.listV_main);
     	
     	
     	ParseQuery getShoppingCart = new ParseQuery("Cart");
     	getShoppingCart.whereEqualTo("user_id", userId);
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
		        	itemPrice=Double.valueOf(itemDetails.get(i).getPrice())*Integer.valueOf(itemDetails.get(i).getAmount());
 		        	totalSum += itemPrice;
 		        	System.out.println(totalSum);
 		        
 		        }
		        final Double summation = totalSum;
 		        
 				// TODO Auto-generated method stub
 				AlertDialog diaBoxmessage = ShowDialogBox("Total sum "+ summation+" L.E."+"\n\norder shipment ID  023456");
 				diaBoxmessage.show();
 				
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
