 /**
  * 
  */
 package com.vendsy.bartsy.venue.view;
 
 
 import java.util.ArrayList;
 
 import com.vendsy.bartsy.venue.R;
 import com.vendsy.bartsy.venue.BartsyApplication;
 import com.vendsy.bartsy.venue.MainActivity;
 import com.vendsy.bartsy.venue.model.Order;
 import com.vendsy.bartsy.venue.utils.Constants;
 import com.vendsy.bartsy.venue.utils.Utilities;
 
 import android.support.v4.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * @author peterkellis
  * 
  */
 public class BartenderSectionFragment extends Fragment implements OnClickListener {
 
 	static final String TAG = "BartenderSectionFragment";
 	
 	private View mRootView = null;
 	LinearLayout mNewOrdersView = null;
 	LinearLayout mAcceptedOrdersView = null;
 	LinearLayout mCompletedOrdersView = null;
 	LayoutInflater mInflater = null;
 	ViewGroup mContainer = null;
 	public BartsyApplication mApp = null;
 	
 
 	/*
 	 * Creates a map view, which is for now a mock image. Listen for clicks on the image
 	 * and toggle the bar details image
 	 */ 
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		Log.v("Bartsy", "OrdersSectionFragment.onCreateView()");
 
 		mInflater = inflater;
 		mContainer = container;
 		mRootView = mInflater.inflate(R.layout.bartender_main, mContainer, false);
 		mNewOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_new_order_list);
 		mAcceptedOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_accepted_order_list);
 		mCompletedOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_completed_order_list);
 		
 		// Make sure the fragment pointed to by the activity is accurate
 		mApp = (BartsyApplication) getActivity().getApplication();
 		((MainActivity) getActivity()).mBartenderFragment = this;	
 		
 		// Update the view
 		updateOrdersView();
 		
 		// Check and set development environment display
 		if (Constants.DOMAIN_NAME.equalsIgnoreCase("http://54.235.76.180:8080/") && 
 				Utilities.SENDER_ID.equalsIgnoreCase("605229245886")) 
 			((TextView) mRootView.findViewById(R.id.view_main_deployment_environment)).setText("Server: DEV");
 		else if (Constants.DOMAIN_NAME.equalsIgnoreCase("http://app.bartsy.vendsy.com/") && 
 				Utilities.SENDER_ID.equalsIgnoreCase("560663323691")) 
 			((TextView) mRootView.findViewById(R.id.view_main_deployment_environment)).setText("Server: PROD");
 		else 
 			((TextView) mRootView.findViewById(R.id.view_main_deployment_environment)).setText("** INCONSISTENT DEPLOYMENT **");
 		
 		return mRootView;
 
 	}
 	
 	
 	/***
 	 * Updates the orders view
 	 */
 	
 	synchronized public void updateOrdersView() {
 		
 		Log.v(TAG, "updateOrdersView()");
 		
 		if (mRootView == null) return;
 		
 		mNewOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_new_order_list);
 		mAcceptedOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_accepted_order_list);
 		mCompletedOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_completed_order_list);
 		
 		if (mNewOrdersView == null || mAcceptedOrdersView == null || mCompletedOrdersView == null)
 			return;
 		
 		// Make sure the list views are all empty
 		
 		mNewOrdersView.removeAllViews();
 		mAcceptedOrdersView.removeAllViews();
 		mCompletedOrdersView.removeAllViews();
 		
 		// Add any existing orders in the layout, one by one
 		
 		Log.v("Bartsy", "mApp.mOrders list size = " + mApp.getOrderCount());
 
 		for (Order order : mApp.mOrders) {
 			
 			Log.v("Bartsy", "Adding order " + order.serverID + " with status " + order.status + " and last status " + order.last_status + " to the layout");
 			
 			// Update the view's main layout 
 			order.view = mInflater.inflate(R.layout.bartender_order, mContainer, false);
 			order.updateView();
 			
 			switch (order.status) {
 			case Order.ORDER_STATUS_NEW:
 				// add order to the top of the accepted orders list view
 				insertOrderInLayout(order,mNewOrdersView);
 				break;
 			case Order.ORDER_STATUS_IN_PROGRESS:
 				// add order to the top of the accepted orders list view
 				insertOrderInLayout(order, mAcceptedOrdersView);
 				break;
 			case Order.ORDER_STATUS_READY:
 				// add order to the bottom of the completed orders list view 
 				insertOrderInLayout(order, mCompletedOrdersView);
 				break;
 			case Order.ORDER_STATUS_CANCELLED:
 			case Order.ORDER_STATUS_TIMEOUT:
 				// add cancelled order in the right layout based on its last state
 				switch (order.last_status) {
 				case Order.ORDER_STATUS_NEW:
 					insertOrderInLayout(order, mNewOrdersView);
 					break;
 				case Order.ORDER_STATUS_IN_PROGRESS:
 					insertOrderInLayout(order, mAcceptedOrdersView);
 					break;
 				case Order.ORDER_STATUS_READY:
 					insertOrderInLayout(order, mCompletedOrdersView);
 					break;
 				default:
 					// We should not have gotten there. Show the order regardless but warn the user...
 					order.errorReason = "This order is cancelled, but in the wrong state. Please let the Bartsy team know.";
 					insertOrderInLayout(order, mCompletedOrdersView);
 					
 				}
 				break;
 			}
 		}
 	}
 
 	
 	/**
 	 * Bundles orders of a user together using the same order number for convenience
 	 */
 	
 	void insertOrderInLayout(Order order, LinearLayout layout) {
 		
 		// Never bundle expired or cancelled orders 
 		if (order.status != Order.ORDER_STATUS_CANCELLED && order.status != Order.ORDER_STATUS_TIMEOUT) {
 		
 			// Try to insert the order in a previous order from the same user
 
 			for (int i=0 ; i < layout.getChildCount() ; i++) {
 				
 				View view = layout.getChildAt(i);
 				Order layoutOrder = (Order) view.getTag();
 				
 				if (layoutOrder.status != Order.ORDER_STATUS_CANCELLED && // Don't insert in expired orders
 						layoutOrder.status != Order.ORDER_STATUS_TIMEOUT &&
 						layoutOrder.orderSender.userID.equalsIgnoreCase(order.orderSender.userID)) {
 					// Found an existing order from the same user. Insert a mini-view of the order
 					
 					LinearLayout miniLayout = (LinearLayout) view.findViewById(R.id.view_order_mini);
 					View miniView = order.getMiniView(mInflater, mContainer);
 					miniView.findViewById(R.id.view_order_button_remove).setOnClickListener(this);
 					miniLayout.addView(miniView);
 					return;
 				}
 			}
 		}
 		
 		
 		// No previous order was found, insert the order at the top level
 		
 		layout.addView(order.view);
 		order.view.findViewById(R.id.view_order_button_positive).setOnClickListener(this);
 		order.view.findViewById(R.id.view_order_button_negative).setOnClickListener(this);
 		order.view.findViewById(R.id.view_order_button_remove).setOnClickListener(this);
 		order.view.findViewById(R.id.view_order_button_remove).setOnClickListener(this);
 		order.view.findViewById(R.id.view_order_button_expired).setOnClickListener(this);
 	}
 	
 
 	/**
 	 * 
 	 * Handle clicks coming from an item in the order list. These change the state of the orders and notify the
 	 * other sides. We bundle orders together by sender and pressing a postivie or negative button processes all
 	 * the orders in the bundle. Individual items in an order can also be rejected by pressing the button on the 
 	 * left of the time.
 	 * 
 	 */
 	
 	@Override
 	public synchronized void onClick(View v) {
 
 		Log.v(TAG, "onClick()");
 
 		Order order = (Order) v.getTag();
 		ArrayList<Order> orders = (ArrayList<Order>) mApp.mOrders.clone();
 		int status = order.status;
 		String userID = order.orderSender.userID;
 
 		// Update the order status locally 
 		
 		Log.v(TAG, "---- Master order: " + order.serverID + " from " + userID + " with status " + status);
 
 		switch (v.getId()) {
 		
 		case R.id.view_order_button_positive:
 			
 			// Process all orders for that user that are currently in this state
 
 			Log.v(TAG, "Clicked on order positive button");
 
 			for (Order orderItem : orders) {
 				
 				Log.v(TAG, "Processing child order " + orderItem.serverID + " from " + orderItem.orderSender.userID + " with status " + orderItem.status);
 				
 				if (orderItem.status == status && orderItem.orderSender.userID.equalsIgnoreCase(userID)) {
 
 					orderItem.nextPositiveState();	
 					Log.v(TAG, "Child matches parent - update status to " + orderItem.status);
 
 					if (orderItem.status == Order.ORDER_STATUS_COMPLETE) {
 						Log.v(TAG, "Removing child with status COMPLETE");
 						orderItem.view = null;
 						mApp.removeOrder(orderItem);
 					}
 					// Send updated order status to the remote
 					((MainActivity) getActivity()).sendOrderStatusChanged(orderItem);
 				}
 			}
 
 			break;
 			
 		case R.id.view_order_button_negative:
 			
 			// Process all orders for that user that are currently in this state
 
 			Log.v(TAG, "Clicked on order negative button");
 
 			for (Order orderItem : orders) {
 
 				Log.v(TAG, "Processing order " + orderItem.serverID);
 				
 				if (orderItem.status == status && orderItem.orderSender.userID.equalsIgnoreCase(userID)) {
 					orderItem.nextNegativeState("Order rejected by the bartender");	
 					orderItem.view = null;
 					mApp.removeOrder(orderItem);
 					// Send updated order status to the remote
 					((MainActivity) getActivity()).sendOrderStatusChanged(orderItem);
 				}
 			}
 			
 			break;
 			
 		case R.id.view_order_button_remove:
 			Log.v(TAG, "Clicked on order remove button");
 			order.nextNegativeState("Individual order rejected by the bartender");
 			order.view = null;
 			mApp.removeOrder(order);
 			// Send updated order status to the remote
 			((MainActivity) getActivity()).sendOrderStatusChanged(order);
 			break;
 
 		case R.id.view_order_button_expired:
 			Log.v(TAG, "Clicked on order expired button");
 			order.view = null;
 			mApp.removeOrder(order);
 			break;
 
 		default:
 			break;
 		}
 		
 		// Update the orders view
 		updateOrdersView();
 	}
 	
 	
 	@Override 
 	public void onDestroyView() {
 		super.onDestroyView();
 
 		Log.v(TAG, "onDestroyView()");
 	}
 	
 	
 	@Override 
 	public void onDestroy() {
 		super.onDestroy();
 
 		Log.v(TAG, "onDestroy()");
 		
 		mRootView = null;
 		mNewOrdersView = null;
 		mInflater = null;
 		mContainer = null;
 
 		// Because the fragment may be destroyed while the activity persists, remove pointer from activity
 		((MainActivity) getActivity()).mBartenderFragment = null;
 	}
 }
