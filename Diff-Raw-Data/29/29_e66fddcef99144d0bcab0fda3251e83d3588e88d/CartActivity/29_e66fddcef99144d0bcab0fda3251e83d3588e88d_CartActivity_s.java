 package com.lossboys.customerapp.dashboard;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.lossboys.customerapp.CustomHTTP;
 import com.lossboys.customerapp.R;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.RelativeLayout;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CartActivity extends ListActivity {
 	TextView totalView;
 	ArrayList<HashMap<String, String>> itemList;
 	Button checkout;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.cart_layout);
 
 		totalView = (TextView) findViewById(R.id.cartTotal);
 		checkout = (Button) findViewById(R.id.btnCartCheckout);
 
 		checkout.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				Intent i = new Intent(getApplicationContext(), com.lossboys.customerapp.dashboard.CheckoutActivity.class);
 
 				startActivity(i);
 				finish();
 			}
 		});
 
 		itemList = new ArrayList<HashMap<String, String>>();
 
 		updateCart();
 
 		getListView().setItemsCanFocus(true);
 	}
 
 	private void updateCart() {
 		itemList.clear();
 
 		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(1);
 
 		nameValuePair.add(new BasicNameValuePair("function", "check_cart"));
 
 		JSONObject cartJSON = CustomHTTP.makePOST("http://23.21.158.161:4912/cart.php", nameValuePair);
 
 		float total = 0;
 		DecimalFormat df = new DecimalFormat("#0.00");
 
 		try {
 			JSONArray items = cartJSON.getJSONArray("items");
 
 			for (int i = 0; i < items.length(); i++) {
 				JSONObject item = items.getJSONObject(i);
 
 				String name = item.getString("Name");
 				String quantity = item.getString("Quantity");
 				Float price = Float.parseFloat(item.getString("Price"));
 
 				HashMap<String, String> map = new HashMap<String, String>();
 				map.put("Name", name);
 				map.put("Quantity", quantity);
 				map.put("Price", "$" + df.format(price));
 				map.put("ItemID", item.getString("ItemID"));
 				total += price * Float.parseFloat(quantity);
 
 				itemList.add(map);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		totalView.setText("Total: $" + df.format(total));
 
 		ListAdapter adapter = new SimpleAdapter(this, itemList, R.layout.cart_item_layout, new String[] { "Name", "Quantity", "Price",
 				"ItemID" }, new int[] { R.id.cartName, R.id.cartQuantity, R.id.cartPrice, R.id.cartItemID });
 
 		setListAdapter(adapter);
 	}
 
 	public void removeHandler(View view) {
 		RelativeLayout layout = (RelativeLayout) view.getParent().getParent();
 
 		final String iid = ((TextView) layout.findViewById(R.id.cartItemID)).getText().toString();
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CartActivity.this);
 
 		alertDialogBuilder.setTitle("Confirm Remove");
 
 		alertDialogBuilder.setMessage("Are you sure you want to remove this item?").setCancelable(false)
 				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
 						nameValuePair.add(new BasicNameValuePair("function", "remove_cart"));
 						nameValuePair.add(new BasicNameValuePair("itemid", iid));
 
 						JSONObject resultJSON = CustomHTTP.makePOST("http://23.21.158.161:4912/cart.php", nameValuePair);
 
 						Context context = CartActivity.this;
 						CharSequence text;
 						int duration = Toast.LENGTH_SHORT;
 
 						if (resultJSON != null) {
 							try {
 								String jsonResult = resultJSON.getString("error");
 								if (jsonResult.equals("true"))
 									text = "Remove failed.";
 								else
 									text = "Remove succeeded.";
 							} catch (Exception e) {
 								e.printStackTrace();
 								text = "Remove failed.";
 							}
 						} else
 							text = "Remove failed.";
 
 						Toast toast = Toast.makeText(context, text, duration);
 						LinearLayout toastLayout = (LinearLayout) toast.getView();
 						TextView toastTV = (TextView) toastLayout.getChildAt(0);
 						toastTV.setTextSize(20);
 						toast.setGravity(Gravity.CENTER, 0, 0);
 						toast.show();
 						updateCart();
 						dialog.dismiss();
 					}
 				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		alertDialog.show();
 	}
 
 	public void itemHandler(View view) {
 		RelativeLayout layout = (RelativeLayout) view.getParent();
 
 		Intent i = new Intent(getApplicationContext(), com.lossboys.customerapp.dashboard.ProductActivity.class);
 
 		i.putExtra("ItemID", ((TextView) layout.findViewById(R.id.cartItemID)).getText().toString());
 
 		startActivityForResult(i, 1);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == 1) {
 			if (resultCode == RESULT_OK) {
 				updateCart();
 			}
 		}
 	}
 
 	public void editHandler(View view) {
 		RelativeLayout layout = (RelativeLayout) view.getParent().getParent();
 
 		String iid = ((TextView) layout.findViewById(R.id.cartItemID)).getText().toString();
 		String qty = ((TextView) layout.findViewById(R.id.cartQuantity)).getText().toString();
 
 		final Dialog itemDialog = new Dialog(CartActivity.this);
 		itemDialog.setContentView(R.layout.cartdialog_layout);
 		itemDialog.setTitle("Update Quantity");
 
 		((TextView) itemDialog.findViewById(R.id.dialogItemID)).setText(iid);
 		((TextView) itemDialog.findViewById(R.id.dialogQuantity)).setText(qty);
 
 		Button update = (Button) itemDialog.findViewById(R.id.dialogButtonUpdate);
 		Button cancel = (Button) itemDialog.findViewById(R.id.dialogButtonCancel);
 		ImageButton add = (ImageButton) itemDialog.findViewById(R.id.dialogButtonAdd);
 		ImageButton subtract = (ImageButton) itemDialog.findViewById(R.id.dialogButtonSubtract);
 
 		update.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
 				nameValuePair.add(new BasicNameValuePair("function", "modify_cart"));
 				nameValuePair.add(new BasicNameValuePair("itemid", ((TextView) itemDialog.findViewById(R.id.dialogItemID)).getText()
 						.toString()));
 				nameValuePair.add(new BasicNameValuePair("quantity", ((TextView) itemDialog.findViewById(R.id.dialogQuantity)).getText()
 						.toString()));
 
 				JSONObject resultJSON = CustomHTTP.makePOST("http://23.21.158.161:4912/cart.php", nameValuePair);
 
 				Context context = CartActivity.this;
 				CharSequence text;
 				int duration = Toast.LENGTH_SHORT;
 
 				if (resultJSON != null) {
 					try {
 						String jsonResult = resultJSON.getString("error");
 						if (jsonResult.equals("true"))
 							text = "Update failed.";
 						else
 							text = "Update succeeded.";
 					} catch (Exception e) {
 						e.printStackTrace();
 						text = "Update failed.";
 					}
 				} else
 					text = "Update failed.";
 
 				Toast toast = Toast.makeText(context, text, duration);
 				LinearLayout toastLayout = (LinearLayout) toast.getView();
 				TextView toastTV = (TextView) toastLayout.getChildAt(0);
 				toastTV.setTextSize(20);
 				toast.setGravity(Gravity.CENTER, 0, 0);
 				toast.show();
 				updateCart();
 				itemDialog.dismiss();
 			}
 		});
 		cancel.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				itemDialog.dismiss();
 			}
 		});
 		add.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				TextView qty = (TextView) itemDialog.findViewById(R.id.dialogQuantity);
 				int num = Integer.parseInt(qty.getText().toString()) + 1;
 				qty.setText(Integer.toString(num));
 			}
 		});
 		subtract.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				TextView qty = (TextView) itemDialog.findViewById(R.id.dialogQuantity);
 				int num = Integer.parseInt(qty.getText().toString()) - 1;
 				if (num > 0)
 					qty.setText(Integer.toString(num));
 			}
 		});
 		itemDialog.show();
 	}
 }
