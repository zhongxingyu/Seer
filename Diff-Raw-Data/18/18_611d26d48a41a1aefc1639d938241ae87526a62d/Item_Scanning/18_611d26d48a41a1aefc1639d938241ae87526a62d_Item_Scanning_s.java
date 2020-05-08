 package com.virtual.market;
 
 import com.parse.GetCallback;
 import com.parse.GetDataCallback;
 import com.parse.ParseException;
 import com.parse.ParseFile;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 
 import android.R.integer;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Item_Scanning extends Activity {
 	String Product_ID;
 	String Product_price;
 	int Product_amount;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_item__scanning);
 
 		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 		startActivityForResult(intent, 0);
 
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		// String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
 		final TextView Item_ID = (TextView) findViewById(R.id.Item_ID);
 		final TextView Item_title = (TextView) findViewById(R.id.item_title);
 		final ImageView Item_Image = (ImageView) findViewById(R.id.item_image);
 		final TextView Item_Cat = (TextView) findViewById(R.id.item_cat);
 		final TextView Item_Price = (TextView) findViewById(R.id.item_price);
 		final TextView item_amount = (TextView) findViewById(R.id.item_amount);
 		final Button btn_AddtoCart = (Button) findViewById(R.id.addtoCart);
 		final Button btn_AddanotherPro = (Button) findViewById(R.id.addanthr);
 		final ParseUser currentuser = ParseUser.getCurrentUser();
 
 		if (requestCode == 0) {
 			if (resultCode == RESULT_OK) {
 				this.Product_ID = intent.getStringExtra("SCAN_RESULT");
 				final String format = intent
 						.getStringExtra("SCAN_RESULT_FORMAT");
 
 				ParseQuery query = new ParseQuery("Item");
 				query.getInBackground(Product_ID, new GetCallback() {
 					public void done(final ParseObject object, ParseException e) {
 						if (e == null) {
 
 							Product_price = object.getString("item_price");
 							Product_amount = Integer.valueOf(object.getNumber(
 									"item_amount").toString());
 							Item_ID.setText(" Product ID : " + Product_ID);
 							Item_title.setText(" Product : "
 									+ object.getString("item_name"));
 							Item_Cat.setText(" Category : "
 									+ object.getString("item_desc"));
 							Item_Price.setText(" Price : " + Product_price);
 							item_amount.setText("Available amount= "
 									+ Product_amount);
 							ParseFile image = (ParseFile) object
 									.get("item_image");
 							image.getDataInBackground(new GetDataCallback() {
 
 								@Override
 								public void done(byte[] imageInBytes,
 										ParseException pEx) {
 									// TODO Auto-generated method stub
 									Bitmap bmp = BitmapFactory.decodeByteArray(
 											imageInBytes, 0,
 											imageInBytes.length);
 									Item_Image.setImageBitmap(bmp);
 								}
 							});
 
 							setProduct_Price(Product_price);
 
 						} else {
 							// something went wrong
 						}
 					}
 				});
 				// Handle successful scan
 			} else if (resultCode == RESULT_CANCELED) {
 				// Handle cancel
 			}
 
 		}
 
 		OnClickListener lsn_anth = new OnClickListener() {
 
 			public void onClick(View v) {
 				finish();
 				startActivity(getIntent());
 			}
 		};
 		btn_AddanotherPro.setOnClickListener(lsn_anth);
 
 		OnClickListener lsn_add = new OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				EditText txt_amount = (EditText) findViewById(R.id.txt_amount);
 				
 				String amount = txt_amount.getText().toString();
 				if (!amount.isEmpty()) {
 					if (Integer.valueOf(amount) <= Product_amount) {
 						ParseObject Cart = new ParseObject("Cart");
 						Cart.put("item_amount", Integer.valueOf(amount));
 						Cart.put("item_id", Product_ID);
 						Cart.put("user_id", currentuser.getObjectId());
 						Cart.saveInBackground();
 						ShowDialogBox("Item has been added to your cart");
 					} else {
 						ShowDialogBox("the required amount is not available on stock");
 					}
 
 				} else {
 					ShowDialogBox("Please add item quantity");
 				}
 
 			}
 		};
 
 		btn_AddtoCart.setOnClickListener(lsn_add);
 
 		OnClickListener lsn_cart = new OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent cart_intent = new Intent();
				cart_intent.setClass(Item_Scanning.this, Historical_Data.class);
 				startActivity(cart_intent);
 
 			}
 		};
 		Button btn_cart = (Button) findViewById(R.id.btn_cart);
 		btn_cart.setOnClickListener(lsn_cart);
 
 	}
 
 	void setProduct_Price(String PP) {
 		this.Product_price = PP;
 	}
 
 	private void ShowDialogBox(String message) {
 
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
 
 		myQuittingDialogBox.show();
 	}
 
 }
