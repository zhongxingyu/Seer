 package com.xingyun.activity;
 
 import java.util.Calendar;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.params.HttpParams;
 import com.xingyun.persistence.CartManager;
 import com.xingyun.persistence.UserManager;
 import com.xingyun.setting.Configuration;
 import com.xingyun.utility.DBManager;
 import com.xingyun.utility.StringUtility;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class OrderInfoActivity extends Activity {
 
 	Activity activity;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_orderinfo);
 		activity = this;
 
 		((EditText) activity.findViewById(R.id.txt_name)).setText(UserManager
 				.getContactName(activity));
 		((EditText) activity.findViewById(R.id.txt_telephone))
 				.setText(UserManager.getContactPhone(activity));
 
 		Button btnBack = (Button) findViewById(R.id.btn_back);
 		btnBack.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				finish();
 			}
 
 		});
 
 		Button btnNext = (Button) findViewById(R.id.btn_next);
 		btnNext.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				String name = ((EditText) activity.findViewById(R.id.txt_name))
 						.getText().toString();
 				String telephone = ((EditText) activity
 						.findViewById(R.id.txt_telephone)).getText().toString();
 				String guestNumber = ((EditText) activity
 						.findViewById(R.id.txt_guestnumber)).getText()
 						.toString();
				try{
					int n = Integer.parseInt(guestNumber);
					if(n<1) {
						Toast toast = Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.bad_guest_number),
								Toast.LENGTH_LONG);
						toast.setGravity(Gravity.BOTTOM, 0, 0);
						toast.show();
						return;
					}
				}catch(Exception ex) {
					Toast toast = Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.bad_guest_number),
							Toast.LENGTH_LONG);
					toast.setGravity(Gravity.BOTTOM, 0, 0);
					toast.show();
					return;
				}
 				String isVip = ((CheckBox) activity.findViewById(R.id.cb_isvip))
 						.isChecked() ? "true" : "false";
 				String arrivalDateTime = ((EditText) activity
 						.findViewById(R.id.txt_arrivaldate)).getText()
 						.toString()
 						+ "T"
 						+ ((EditText) activity
 								.findViewById(R.id.txt_arrivaltime)).getText()
 								.toString();
 				String requirements = ((EditText) activity
 						.findViewById(R.id.txt_requirements)).getText()
 						.toString();
 
 				// String data =
 				// "{    \"customer_id\" : 1,    \"contact_name\" : \"邵明岐\",    \"contact_phone\" : \"13811021667\",    \"people_number\" : 2,    \"box_required\" : true,    \"order_price\": 68,    \"dishes_count\": 8,    \"reserved_time\": \"2012-01-19T19:05:01\",    \"other_requirements\" : \"不要放香菜\",    \"order_dishes\" : [        {   \"menu_item_id\": 1,            \"quantity\" : 1        },        {   \"menu_item_id\": 5,            \"quantity\" : 3        }    ]}";
 				String data = "{\"customer_id\" : "
 						+ UserManager.getId(activity)
 						+ ", \"contact_name\" : \""
 						+ StringUtility.string2Json(name)
 						+ "\", \"contact_phone\" : \""
 						+ StringUtility.string2Json(telephone)
 						+ "\", \"people_number\" : "
 						+ StringUtility.string2Json(guestNumber)
 						+ ", \"box_required\" : "
 						+ StringUtility.string2Json(isVip)
 						+ ", \"order_price\": " + CartManager.getTotalPrice()
 						+ ", \"dishes_count\": "
 						+ CartManager.getOrderedDishes().size()
 						+ ", \"reserved_time\": \"" + arrivalDateTime
 						+ "\", \"other_requirements\" : \""
 						+ StringUtility.string2Json(requirements)
 						+ "\", \"order_dishes\" : " + CartManager.getJsonStr()
 						+ "}";
 				// post data
 				HttpPut httpPut = new HttpPut(Configuration.WS_PLACEORDER);
 
 				HttpParams params = new BasicHttpParams();
 
 				params.setParameter(CoreConnectionPNames.SO_TIMEOUT,
 						Configuration.WS_SOCKETTIMEOUT);
 				params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
 						Configuration.WS_CONNTIMEOUT);
 
 				try {
 					StringEntity se = new StringEntity(data, "UTF-8");
 					httpPut.setEntity(se);
 					HttpResponse httpResponse = new DefaultHttpClient()
 							.execute(httpPut);
 					// HTTP/1.0 201 CREATED
 					Log.d(OrderInfoActivity.class.getName(), httpResponse
 							.getStatusLine().toString());
 					if (httpResponse.getStatusLine().getStatusCode() == 201) {
 						CartManager.clearCart();
 						
 						UserManager.setContactName(activity, name);
 						UserManager.setContactPhone(activity, telephone);
 						
 						Intent i = new Intent();
 						i.setClass(OrderInfoActivity.this,
 								OrderDishesResultActivity.class);
 						startActivity(i);
 					} else {
 						Toast toast = Toast.makeText(
 								getApplicationContext(),
 								getResources().getString(
 										R.string.plz_check_input),
 								Toast.LENGTH_LONG);
 						toast.setGravity(Gravity.BOTTOM, 0, 0);
 						toast.show();
 					}
 				} catch (Exception e) {
 					Toast toast = Toast.makeText(
 							getApplicationContext(),
 							getResources().getString(
 									R.string.failed_to_get_data),
 							Toast.LENGTH_LONG);
 					toast.setGravity(Gravity.BOTTOM, 0, 0);
 					toast.show();
 				}
 			}
 
 		});
 		TextView txtDishCount = (TextView) this
 				.findViewById(R.id.txt_dishcount);
 		txtDishCount.setText("共点菜" + CartManager.getDishCount() + "道");
 		TextView txtTotalPrice = (TextView) this
 				.findViewById(R.id.txt_totalprice);
 		txtTotalPrice.setText("一共" + CartManager.getTotalPrice() + "元");
 
 		final EditText txtArrivalDate = (EditText) this
 				.findViewById(R.id.txt_arrivaldate);
 		txtArrivalDate.setOnClickListener(new OnClickListener() {
 			Calendar calendar = Calendar.getInstance();
 
 			@Override
 			public void onClick(View arg0) {
 				DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
 
 					@Override
 					public void onDateSet(DatePicker view, int year,
 							int monthOfYear, int dayOfMonth) {
 						txtArrivalDate.setText(year + "-" + (monthOfYear + 1)
 								+ "-" + dayOfMonth);
 					}
 				};
 				Dialog dialog = new DatePickerDialog(activity, dateListener,
 						calendar.get(Calendar.YEAR), calendar
 								.get(Calendar.MONTH), calendar
 								.get(Calendar.DAY_OF_MONTH));
 
 				dialog.show();
 			}
 
 		});
 
 		final EditText txtArrivalTime = (EditText) this
 				.findViewById(R.id.txt_arrivaltime);
 		txtArrivalTime.setOnClickListener(new OnClickListener() {
 			Calendar calendar = Calendar.getInstance();
 
 			@Override
 			public void onClick(View arg0) {
 				OnTimeSetListener timeListener = new OnTimeSetListener() {
 
 					@Override
 					public void onTimeSet(TimePicker view, int hourOfDay,
 							int minute) {
 						txtArrivalTime.setText(hourOfDay + ":" + minute);
 					}
 
 				};
 				TimePickerDialog dialog = new TimePickerDialog(activity,
 						timeListener, calendar.get(Calendar.HOUR_OF_DAY),
 						calendar.get(Calendar.MINUTE), true);
 				dialog.show();
 			}
 
 		});
 
 	}
 }
