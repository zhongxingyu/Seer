 package com.envsocial.android.features.order;
 
 import java.io.Serializable;
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.envsocial.android.R;
 import com.envsocial.android.api.ActionHandler;
 import com.envsocial.android.api.Location;
 import com.envsocial.android.features.Feature;
 import com.facebook.FacebookException;
 import com.facebook.FacebookRequestError;
 import com.facebook.HttpMethod;
 import com.facebook.LoggingBehavior;
 import com.facebook.Request;
 import com.facebook.RequestAsyncTask;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.SessionDefaultAudience;
 import com.facebook.SessionState;
 import com.facebook.Settings;
 import com.facebook.UiLifecycleHelper;
 
 
 public class OrderTabFragment extends SherlockFragment implements OnClickListener {
 	private static final String TAG = "OrderTabFragment"; 
 	
 	static OrderTabFragment newInstance(Location location, List<Map<String, Object>> tabOrderSelections) {
 		OrderTabFragment f = new OrderTabFragment();
 		
 		Bundle args = new Bundle();
 		args.putSerializable("selections", (Serializable)tabOrderSelections);
 		args.putSerializable(ActionHandler.CHECKIN, location);
 		
 		f.setArguments(args);
 		return f;
 	}
 	
 	// internal data
 	private Location mLocation;
 	private OrderFeature mOrderFeature;
 	private List<Map<String, Object>> mOrderSelections;
 	
 	// views and adapters
 	private TextView mTotalOrderPrice;
 	private Button mBackButton;
 	private OrderTabListAdapter mAdapter;
 	
 	// -------- facebook session and actions --------
 	private static final String PUBLISH_ORDER = "publish_order";
 	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
 	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
 	private static final int REAUTH_ACTIVITY_CODE = 100;
 
 	private Session.StatusCallback statusCallback = new SessionStatusCallback();
 	private String mPublishOrderMessage;
 	private boolean pendingPublishReauthorization = false;
 	private UiLifecycleHelper uiHelper;
 	
 	
 	protected Location getLocation() {
 		return mLocation;
 	}
 	
 	
 	protected OrderFeature getOrderFeature() {
 		return mOrderFeature;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	
 		mOrderSelections = (List<Map<String, Object>>) getArguments().get("selections");
 		mLocation = (Location) getArguments().get(ActionHandler.CHECKIN);
 		mOrderFeature = (OrderFeature)mLocation.getFeature(Feature.ORDER);
 		
 		mAdapter = new OrderTabListAdapter(this, mOrderSelections);
 		
 		// get the publish order if there was one saved
 		if (savedInstanceState != null) {
 			mPublishOrderMessage = savedInstanceState.getString(PUBLISH_ORDER);
 			pendingPublishReauthorization = savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
 		}
 		
 		Context context = getActivity();
 		
 		
 		
 		// retrieve any existing facebook session
 		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
 //		Session session = Session.getActiveSession();
 //		if (session == null) {
 //			if (savedInstanceState != null) {
 //				session = Session.restoreSession(context, null, statusCallback, savedInstanceState);
 //			}
 //
 //			if (session == null) {
 //				session = new Session(context);
 //			}
 //
 //			Session.setActiveSession(session);
 //			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
 //				session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
 //			}
 //		}
 		
 		uiHelper = new UiLifecycleHelper(getActivity(), statusCallback);
 		uiHelper.onCreate(savedInstanceState);
 	}
 	
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		
 		View v = inflater.inflate(R.layout.order_tab, container, false);
 		ExpandableListView list = (ExpandableListView) v.findViewById(R.id.order_tab_summary_list);
 		
 		View footer = inflater.inflate(R.layout.order_tab_footer, null, false);
 		View header = inflater.inflate(R.layout.order_tab_header, null, false);
 		list.addFooterView(footer);
 		list.addHeaderView(header);
 		list.setAdapter(mAdapter);
 		
 		for (int i = 0; i < mAdapter.getGroupCount(); i++) {
 			list.expandGroup(i);
 		}
 		
 		double totalTabPrice = getTabTotalPrice();
 		
 		mTotalOrderPrice = (TextView) footer.findViewById(R.id.order_tab_total_price);
 		mTotalOrderPrice.setText(new DecimalFormat("#.##").format(totalTabPrice) + " RON");
 		mBackButton = (Button) footer.findViewById(R.id.order_tab_back_to_orders_button);
 		mBackButton.setOnClickListener(this);
 		
 		return v;
 	}
 	
 	
 	@Override
     public void onResume() {
         super.onResume();
         //Session.getActiveSession().addCallback(statusCallback);
         uiHelper.onResume();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         //Session.getActiveSession().removeCallback(statusCallback);
         uiHelper.onPause();
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	uiHelper.onDestroy();
     }
 	
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         
         // since we only deal with the facebook loop here we can just pass the 
         // results to the facebook session handler
         Log.d(TAG, "Received activity request for code: " + requestCode + ", result: " + resultCode);
         
         uiHelper.onActivityResult(requestCode, resultCode, data);
         
 //        Activity activity = getActivity();
 //        Session.getActiveSession().onActivityResult(activity, requestCode, resultCode, data);
     }
     
     
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         
         // save publishOrder and pendingPublishReauthorization
         if (mPublishOrderMessage != null) {
         	outState.putString(PUBLISH_ORDER, mPublishOrderMessage);
         }
         outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
         
         uiHelper.onSaveInstanceState(outState);
         
         // save facebook session
 //        Session session = Session.getActiveSession();
 //        Session.saveSession(session, outState);
     }
     
     
     @Override
 	public void onClick(View v) {
 		if (v == mBackButton) {
 			// pop current fragment from back stack - return to the original 
 			getActivity().getSupportFragmentManager().popBackStackImmediate();
 		}
 	}
     
     
     protected void setPublishOrderMessage(String orderMessage) {
 		mPublishOrderMessage = orderMessage;
 	}
     
     
     protected void publishOrderOnFB() {
 		if (mPublishOrderMessage != null) {
 			// check facebook session
 			Session session = Session.getActiveSession();
 
 			if (session != null) {
 				Log.d(TAG, "## Facebook session state: " + session.getState());
 
 				if (session.isOpened()) {
 
 					// Check for publish permissions
 					List<String> permissions = session.getPermissions();
 					if (!isSubsetOf(PERMISSIONS, permissions)) {
 						pendingPublishReauthorization = true;
 						Session.NewPermissionsRequest reauthRequest = new Session.NewPermissionsRequest(
 								this, PERMISSIONS)
 								.setDefaultAudience(SessionDefaultAudience.ONLY_ME)
 								.setRequestCode(REAUTH_ACTIVITY_CODE);
 						session.requestNewPublishPermissions(reauthRequest);
 
 						Log.d(TAG, "## Checking for write permissions: " + session.getState());
 
 						return;
 					}
 
 					Log.d(TAG, "## Composing message and sending: " + session.getState());
 
 					Bundle postParams = new Bundle();
 					postParams.putString("message", mPublishOrderMessage);
 					postParams.putString("description",
 							"Test of order publish from Facebook integrated Android app.");
 
 					Request.Callback callback = new Request.Callback() {
 						public void onCompleted(Response response) {
 //							if (response == null) {
 //								Log.d(TAG, "response is null");
 //								return;
 //							}
 //							if (response.getGraphObject() == null) {
 //								Log.d(TAG, "getGraphObject is null");
 //								return;
 //							}
 							
 							JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
 
 							String postId = null;
 							try {
 								postId = graphResponse.getString("id");
 							} catch (JSONException e) {
 								Log.i(TAG, "JSON error " + e.getMessage());
 							}
 

 							FacebookRequestError error = response.getError();
 

 							if (error != null) {
 								Log.d(TAG, error.getErrorMessage(), error.getException());
 								Toast.makeText(getActivity().getApplicationContext(),
 										R.string.msg_share_order_fb_err, Toast.LENGTH_SHORT).show();
 							} else {
 								Toast.makeText(getActivity().getApplicationContext(),
 										R.string.msg_share_order_fb, Toast.LENGTH_LONG).show();
 							}
 							
 							// after post - clear the publishOrder
 							mPublishOrderMessage = null;
 						}
 					};
 
 					Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);
 
 					RequestAsyncTask task = new RequestAsyncTask(request);
 					task.execute();
 				} else if (!session.isOpened() && !session.isClosed()) {
 					Log.d(TAG, "## Need to open a new session because state is: " + session.getState());
 					session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
 				} else {
 					Log.d(TAG, "## Intermediary step: " + session.getState());
 					Session.openActiveSession(getActivity(), this, true, statusCallback);
 				}
 			}
 		}
 		else {
 			Log.d(TAG, "Nothing to publish, order is null.");
 		}
     }
     
     
     private double getTabTotalPrice() {
 		double totalPrice = 0;
 		
 		int orderLen = mOrderSelections.size();
 		for (int idx = 0; idx < orderLen; idx++ ) {
 			// the keys have no importance; we just want access to the elements
 			Map<String, Object> selection = mOrderSelections.get(idx);
 			totalPrice += (Integer) selection.get("quantity") * (Double) selection.get(OrderFeature.ITEM_PRICE);
 		}
 		
 		return totalPrice;
 	}
     
     
     private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
 	    for (String string : subset) {
 	        if (!superset.contains(string)) {
 	            return false;
 	        }
 	    }
 	    return true;
 	}
 	
 	
 	private class SessionStatusCallback implements Session.StatusCallback {
         @Override
         public void call(Session session, SessionState state, Exception exception) {
         	
         	// normally one would check for session state and take actions acordingly - but
         	// in this case we only have the one action we want to do: publish order to the wall
         	// so we just call that
         	
         	Log.d(TAG, "Calling FB onSessionChanged with state value: " + state);
         	
         	if (pendingPublishReauthorization && state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
         	    pendingPublishReauthorization = false;
         	    publishOrderOnFB();
         	}
         	
         	else if (state.equals(SessionState.OPENED)) {
         		publishOrderOnFB();
         	}
         	
         	
         }
     }
 }
