 package com.remirran.digitalmenu;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Vector;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.HorizontalPager.dynamicPaging.HorizontalPager;
 import com.HorizontalPager.dynamicPaging.HorizontalPager.OnScreenSwitchListener;
 import com.remirran.digitalmenu.data.Dish;
 import com.remirran.digitalmenu.data.ExtData;
 import com.remirran.digitalmenu.data.Order;
 import com.remirran.digitalmenu.data.Section;
 import com.remirran.digitalmenu.data.Tools;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnLayoutChangeListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class CafeMenuActivity extends Activity {
 	/* Constants */
 	private final int REQ_CODE_TABLE_ID = 1;
 	private final String LOG_TAG = "CafeMenuActivity";
 	public static final int DIALOG_ORDER_REMOVE = 1;
 	public static final int DIALOG_ORDER_ADD = 2;
 	private static final int DISHES_PER_SCREEN = 4;
 	private static final int ROWS_PER_SCREEN = 2;
 	/* Tools */
 	private static LayoutInflater ltInflatter;
 	/* Order */
 	private static final Order order = Order.getOrder();
 	private static OrderAdapter orderAdapter;
 	private static OrderDialog orderDialog;
 	private Object dialogObject = null;
 	/*Main window state save*/
 	private static View save = null;
 	public static int MAIN_TABLE_IMAGE_WIDTH;
 	public static int MAIN_TABLE_IMAGE_HEIGHT;
 
 	private List<Dish> mCurrentSectionDishes;
 	private List<Integer> mSectionChildrenCount;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         ExtData.setContext(this);
         
         /* Show adv window */
         /* TODO: move init screen there */
         Intent tAdvScreen = new Intent(this, TitleScreenActivity.class);
         startActivity(tAdvScreen);
         
         mSectionChildrenCount = new Vector<Integer>();
         
         /* Init with the table number */
         /* TODO: skip this in locked state */
         //Intent tInitScreen = new Intent(this,InitScreenActivity.class);
         //startActivityForResult(tInitScreen, REQ_CODE_TABLE_ID);
         
         ltInflatter = getLayoutInflater();
         
         orderAdapter = new OrderAdapter(this);
         ListView tLstLayout = (ListView) findViewById(R.id.main_order_layout);
         tLstLayout.setAdapter(orderAdapter);
         tLstLayout.setOnItemClickListener(orderListener);
         
         LinearLayout tTableParent = (LinearLayout) findViewById(R.id.main_data);
         tTableParent.addOnLayoutChangeListener(mainDataLayoutChangeListener);
         
     }
 	
 	private OnLayoutChangeListener mainDataLayoutChangeListener = new OnLayoutChangeListener() {
 		
 		@Override
 		public void onLayoutChange(View v, int left, int top, int right,
 				int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
 			if (left == 0 && top == 0 && right == 0 && bottom == 0) {
 				return;
 			}
 			MAIN_TABLE_IMAGE_WIDTH = (right - left)/2;
 			MAIN_TABLE_IMAGE_HEIGHT = (bottom - top)/2;
 		}
 	};
 	
 	public void applyDownloadedInfo() {
         /* Fill titles*/
         LinearLayout tTtlLayout = (LinearLayout) findViewById(R.id.main_title_layout);
         Vector<Section> sects = ExtData.getInstance().getSections();
         synchronized (sects) {
 			Collections.sort(sects);
 			Iterator<Section> itr = sects.iterator();
 			while (itr.hasNext()) {
 				Section item = itr.next();
 				View tView = ltInflatter.inflate(R.layout.main_title_button, tTtlLayout, false);
 				Button tButton = (Button) tView.findViewById(R.id.main_title_button);
 				tButton.setText(item.getName());
 				tButton.setTag(item);
 				tTtlLayout.addView(tView);
 			}
 		}
         
         
     	/* Fill items for 1st section */
         ListView tLstLayout = (ListView) findViewById(R.id.main_list_layout);
         tLstLayout.setOnItemClickListener(subsListener);
         
         /* Init side menu with first section data */
         titleButtonOnClick(tTtlLayout.getChildAt(0));
         /* TODO: remove this */
         //((TextView) v).setBackgroundColor(Color.parseColor("#cccccc"));
 	}
 
 	private OnItemClickListener subsListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			mCurrentSectionDishes = ExtData.getInstance().getDishesBySection((Section)parent.getAdapter().getItem(position));
 
 			/*TODO no idea what to show in case of empty set*/
 			if (mCurrentSectionDishes == null || mCurrentSectionDishes.isEmpty()) return;
 
         	mainDataClear();
         	
         	/* Start drawing */
         	LinearLayout tTableLayout = (LinearLayout) findViewById(R.id.main_data);
             View tTable = ltInflatter.inflate(R.layout.main_table, tTableLayout, true);
             
             
             try {
             	HorizontalPager tPager = (HorizontalPager) tTable.findViewById(R.id.table_pager); 
                 tPager.removeAllViews();
                 tPager.setOnScreenSwitchListener(screenSwitch);
                 
                 /* Add screens */
                 for (int i = 0; i < mCurrentSectionDishes.size(); i++) {
                 	if ( i % DISHES_PER_SCREEN == 0 ) {
                 		View tScreen = ltInflatter.inflate(R.layout.main_table_screen, tPager, false);
                 		tPager.addView(tScreen);
                 		mSectionChildrenCount.add(0);
                 	}
                 }
                 updateScreens(0);
             }catch (NullPointerException e) {
             	/*TODO: show something else, case of no dishes*/
             }
             
             ((SubAdapter)parent.getAdapter()).setActiveItem(view);
             parent.invalidate();
 		}
 	};
 	
 	private void updateScreens(int currentScreen) {
 		HorizontalPager tPager = (HorizontalPager) findViewById(R.id.table_pager); 
 		for (int i = 0; i < tPager.getChildCount(); i++) {
 			View screen = tPager.getChildAt(i);
 			if ( i >= currentScreen - 1 && i <= currentScreen + 1) {
 				if (mSectionChildrenCount.get(i) == 0) {
 					fillScreen(screen, i);
 				}
 			} else {
 				if (mSectionChildrenCount.get(i) > 0) {
 					clearScreen(screen);
 					mSectionChildrenCount.set(i, 0);
 				}
 			}
 		}
 	}
 	
 	private void clearScreen(View screen) {
 		for (int i = 0; i < ((LinearLayout)screen).getChildCount(); i++) {
 			unbindDrawables( ((LinearLayout)screen).getChildAt(i) );
 		}
 	}
 	
 	private void fillScreen(View screen, int pos) {
 		LinearLayout tTableRows[] = new LinearLayout[ROWS_PER_SCREEN];
 		for ( int i = 0; i < ROWS_PER_SCREEN; i++ ) {
 			tTableRows[i] = (LinearLayout) ((LinearLayout)screen).getChildAt(i);
 		}
 		mSectionChildrenCount.set(pos, Math.min(DISHES_PER_SCREEN * (pos + 1), mCurrentSectionDishes.size()));
 		for ( int i = DISHES_PER_SCREEN * pos; i < mSectionChildrenCount.get(pos); i++ ) {
 			
 			View tElem = ltInflatter.inflate(R.layout.main_table_item, tTableRows[i%2], false);
 			DishLayout dishLayout = (DishLayout) tElem.findViewById(R.id.main_table_item);
 			((LinearLayout.LayoutParams)dishLayout.getLayoutParams()).width = MAIN_TABLE_IMAGE_WIDTH; 
 			
 			dishLayout.fillDishValues(mCurrentSectionDishes.get(i));
 
 			tTableRows[ i % ROWS_PER_SCREEN ].addView(tElem);
 		}
 	}
 	
 	private OnScreenSwitchListener screenSwitch = new OnScreenSwitchListener() {
 		
 		@Override
 		public void onScreenSwitched(int screen) {
 			updateScreens(screen);
 		}
 	};
 	
 	private OnItemClickListener orderListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			dialogObject = orderAdapter.getItem(position);
 			showDialog(DIALOG_ORDER_ADD);
 			
 		}
 	};
 
 	private void titleButtonSwitch(View v) {
 		try {
 			LinearLayout tTtlLayout = (LinearLayout) findViewById(R.id.main_title_layout);
 			((Button)tTtlLayout.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
 		    ((Button)tTtlLayout.getChildAt(0)).setBackgroundResource(R.drawable.button_title);
 		    tTtlLayout.removeView(v);
 		    ((Button)v).setTextColor(Color.parseColor("#000000"));
 		    ((Button)v).setBackgroundResource(R.drawable.button_title_pressed);
 		    tTtlLayout.addView(v, 0);
 		    tTtlLayout.requestLayout();
 		} catch (NullPointerException e) {
 			Log.w(LOG_TAG, "No menu buttons", e);
 		}
 	}
 	
 	private void updateSubsList(View v) {
 		try {
 			ListView tLstLayout = (ListView) findViewById(R.id.main_list_layout);
 	        SubAdapter tLstAdapter = new SubAdapter(this, ExtData.getInstance().getSubs((Section) ((Button)v).getTag()));
 	        tLstLayout.setAdapter(tLstAdapter);
 		} catch (NullPointerException e) {
         	/* TODO: case of no subs*/
         }
 	}
 	
 	private void mainDataClear() {
 		LinearLayout tTableLayout = (LinearLayout) findViewById(R.id.main_data);
     	unbindDrawables(tTableLayout);
     	mSectionChildrenCount.clear();
     	System.gc();
 	}
 	
     public void titleButtonOnClick(View v) {
     	/*Restore main view if req*/
     	if (save != null) {
     		LinearLayout tll = (LinearLayout) findViewById(R.id.main);
     		tll.removeViewAt(1);
     		tll.addView(save);
     		save = null;
     		
     		/*Assign adapter*/
     		ListView tLstLayout = (ListView) tll.findViewById(R.id.main_order_layout);
             tLstLayout.setAdapter(orderAdapter);
             
             /*Reset order if req*/
         	if (Order.getSize() == 0) {
         		tll = (LinearLayout) findViewById(R.id.main_text_confirm);
         		if (tll != null) {
         			tll.removeAllViews();
         			ltInflatter.inflate(R.layout.main_order_empty, tll);
         		}
         	}
     	}
     	titleButtonSwitch(v);
         mainDataClear();
     	
     	updateSubsList(v);
         try {
         	LinearLayout tTableLayout = (LinearLayout) findViewById(R.id.main_data);
         	View tView = ltInflatter.inflate(R.layout.main_table_splash, tTableLayout, false);
 	        ImageView tImgView = (ImageView) tView.findViewById(R.id.main_table_splash);
 	        Section sect = (Section) ((Button) v).getTag();
 	        ExtData.getInstance().fillImg(sect.getImgUri(), tImgView);
 	        tTableLayout.addView(tView);
 	        
 	        mCurrentSectionDishes = null;
         } catch (NoSuchElementException e) {
         	Log.w(LOG_TAG, "Can't get section: ", e);
         } catch (NullPointerException e) {
         	Log.w(LOG_TAG, "Title bar is empty", e);
         }
     }
     
     private void unbindDrawables (View v) {
     	if (v == null) return;
     	if (v.getBackground() != null){
     		if (v instanceof ImageView) {
     			BitmapDrawable content = (BitmapDrawable) ((ImageView) v).getDrawable();
     			if (content != null) {
     				Bitmap contentImg = content.getBitmap();
     				contentImg.recycle();
     				contentImg = null;
     			}
     		}
     		v.getBackground().setCallback(null);
     	}
     	if (v instanceof ViewGroup) {
     		for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++){
     			unbindDrawables(((ViewGroup) v).getChildAt(i));
     		}
     		((ViewGroup) v).removeAllViews();
     	}
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
     	if (data == null) {
     		return;
     	}
     	if (resultCode == RESULT_OK) {
     		switch(requestCode) {
     		case REQ_CODE_TABLE_ID:
     			String tableId = data.getStringExtra("tableId");
     			Log.d("hz", tableId);
     			break;
     		default:
     			Log.d("hz", "requestCode: "+ requestCode);
     		}
     	}
     }
     
     public void onTableImageClick(View v) {
     	dialogObject = ((MenuImage)v).getProduct();
     	showDialog(DIALOG_ORDER_ADD);
     }
     
     public void onTableImageButtonClick (View v) {
     	order.inc((Dish) ((Button)v).getTag());
     	updateOrderDetails();
     }
   
     public void onOrderCleanButtonClick (View v) {
     	order.clear();
     	updateOrderDetails();
     }
     
     public void updateOrderDetails () {
     	orderAdapter.notifyDataSetChanged();
 
     	TextView tv = (TextView) findViewById(R.id.order_total_sum);
     	if (Order.getSize() == 0) {
     		LinearLayout tll = (LinearLayout) findViewById(R.id.main_text_confirm);
     		if (tll != null) {
     			tll.removeAllViews();
     			ltInflatter.inflate(R.layout.main_order_empty, tll);
     		}
     	} else if (Order.getSize() > 0 && tv == null) {
     		LinearLayout tll = (LinearLayout) findViewById(R.id.main_text_confirm);
     		tll.removeAllViews();
     		View w = ltInflatter.inflate(R.layout.main_order_complete, tll, false);
     		tv = (TextView) w.findViewById(R.id.order_total_sum);
     		tll.addView(w);
     	}
     	
     	tv = (TextView) findViewById(R.id.order_total_sum);
     	if (tv != null) tv.setText(Tools.formatCurrency(this, Order.getSum()));
     	
     	
     }
     
     public void onOrderCompleteClick(View v) {
     	/*Replace main screen*/
     	LinearLayout tll = (LinearLayout) findViewById(R.id.main);
     	save = tll.getChildAt(1);
     	tll.removeViewAt(1);
     	ltInflatter.inflate(R.layout.order_second_screen, tll);
     	
     	/*Remove title button highlight*/
 		try {
 			LinearLayout tTtlLayout = (LinearLayout) findViewById(R.id.main_title_layout);
 			((Button)tTtlLayout.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
 		    ((Button)tTtlLayout.getChildAt(0)).setBackgroundResource(R.drawable.button_title);
 		    tTtlLayout.requestLayout();
 		} catch (NullPointerException e) {
 			Log.w(LOG_TAG, "No menu buttons", e);
 		}
 		
 		/*Set sum*/
 		TextView tv = (TextView) findViewById(R.id.order_total_sum);
 		tv.setText(Tools.formatCurrency(this, Order.getSum()));
 		
 		/*Assign adapter*/
 		ListView tLstLayout = (ListView) tll.findViewById(R.id.main_order_layout);
         tLstLayout.setAdapter(orderAdapter);
         tLstLayout.setOnItemClickListener(orderListener);
     	
     }
     
     public void onOrderSendClick(View v) {
     	/*TODO: Check connectivity and server answer */
     	try {
 			new SendDataToServer().execute(Order.toJSON());
 		} catch (JSONException e) {
 			Log.e(LOG_TAG, "Can't generate JSON", e);
 		}
     }
     
     private class SendDataToServer extends AsyncTask<JSONObject, Void, Void> {
 
 		@Override
 		protected Void doInBackground(JSONObject... params) {
 	    	try {
 	    		JSONObject data = params[0];
 	    		DefaultHttpClient http = new DefaultHttpClient();
 	    		HttpPost post = new HttpPost(getString(R.string.xml_uri) + getString(R.string.restaurant));
 	    		ByteArrayEntity se = new ByteArrayEntity(data.toString().getBytes("UTF-8"));
 	    		se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
 	    		post.setEntity(se);
 	    		HttpResponse response = http.execute(post);
 	    	} catch (UnsupportedEncodingException e) {
 	    		Log.e(LOG_TAG, "Unsupported encoding", e);
 			} catch (ClientProtocolException e) {
 				Log.e(LOG_TAG, "Can't send data", e);
 			} catch (IOException e) {
 				Log.e(LOG_TAG, "IO exception", e);
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			RelativeLayout tll = (RelativeLayout) findViewById(R.id.order_second_screen);
 	    	tll.removeAllViews();
 	    	ltInflatter.inflate(R.layout.order_thanks, tll);
 		}
     	
     }
     
     @Override
     protected Dialog onCreateDialog(int id, Bundle params) {
     	switch(id) {
     	case DIALOG_ORDER_REMOVE:
     		return null;
     	case DIALOG_ORDER_ADD:
     		orderDialog = new OrderDialog(this, (Dish)dialogObject);
     		return orderDialog.draw();
     	default:
     		return null;
     	}
     }
     
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
     	switch(id) {
     	case DIALOG_ORDER_ADD:
     		orderDialog.setDish((Dish)dialogObject);
     		orderDialog.updateDialog(dialog);
     	default:
     		break;
     	}
     }
 
 }
