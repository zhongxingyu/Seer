 package com.android.tecla.addon;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.concurrent.locks.ReentrantLock;
 
 import com.android.tecla.addon.SwitchEventProvider.LocalBinder;
 
 import ca.idi.tecla.sdk.SwitchEvent;
 import ca.idi.tecla.sdk.SEPManager;
 import ca.idrc.tecla.framework.TeclaStatic;
 import ca.idrc.tecla.highlighter.TeclaHighlighter;
 
 import android.accessibilityservice.AccessibilityService;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.accessibility.AccessibilityEvent;
 import android.view.accessibility.AccessibilityNodeInfo;
 
 public class TeclaAccessibilityService extends AccessibilityService {
 
 	private final static String CLASS_TAG = "TeclaA11yService";
 
 	public final static int DIRECTION_UP = 0;
 	public final static int DIRECTION_LEFT = 1;
 	public final static int DIRECTION_RIGHT = 2;
 	public final static int DIRECTION_DOWN = 3;
 	private final static int DIRECTION_UP_NORATIOCONSTRAINT = 4;
 	private final static int DIRECTION_LEFT_NORATIOCONSTRAINT = 5;
 	private final static int DIRECTION_RIGHT_NORATIOCONSTRAINT = 6;
 	private final static int DIRECTION_DOWN_NORATIOCONSTRAINT = 7;
 	private final static int DIRECTION_ANY = 8;
 
 	private static TeclaAccessibilityService sInstance;
 
 	private Boolean register_receiver_called;
 
 	private AccessibilityNodeInfo mOriginalNode, mPreviousOriginalNode;
 	protected AccessibilityNodeInfo mSelectedNode;
 
 	private ArrayList<AccessibilityNodeInfo> mActiveNodes;
 	private int mNodeIndex;
 
 	private TeclaHighlighter mTeclaHighlighter;
 	private TeclaHUDOverlay mTeclaHUDController;
 	private SingleSwitchTouchInterface mFullscreenSwitch;
 
 	protected static ReentrantLock mActionLock;
 
 	@Override
 	protected void onServiceConnected() {
 		super.onServiceConnected();
 
 		TeclaStatic.logD(CLASS_TAG, "Service " + TeclaAccessibilityService.class.getName() + " connected");
 
 		init();
 
 	}
 
 	private void init() {
 		register_receiver_called = false;
 
 		mOriginalNode = null;
 		mActiveNodes = new ArrayList<AccessibilityNodeInfo>();
 		mActionLock = new ReentrantLock();
 
 		if(mTeclaHighlighter == null) {
 			mTeclaHighlighter = new TeclaHighlighter(this);
 			TeclaApp.setHighlighter(mTeclaHighlighter);			
 		}
 
 		if (mTeclaHUDController == null) 
 			mTeclaHUDController = new TeclaHUDOverlay(this);
 
 		if (mFullscreenSwitch == null)
 			mFullscreenSwitch = new SingleSwitchTouchInterface(this);
 
 //		if (TeclaApp.persistence.isHUDRunning()) {
 //			mTeclaHighlighter.show();
 //			mTeclaHUDController.show();
 //			registerReceiver(mTeclaHUDController.mConfigChangeReceiver, 
 //					new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
 //			performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
 //		}
 //
 //		if(TeclaApp.persistence.isSingleSwitchOverlayEnabled())
 //			mTouchInterface.show();
 
 		// Bind to SwitchEventProvider
 		Intent intent = new Intent(this, SwitchEventProvider.class);
 		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 
 		registerReceiver(mReceiver, new IntentFilter(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED));
 		register_receiver_called = true;
 		SEPManager.start(this);
 
 		sInstance = this;
 		TeclaApp.setA11yserviceInstance(this);
 }
 	
 	public boolean isHUDVisible() {
 		if (mTeclaHUDController != null) {
 			if (mTeclaHUDController.isVisible()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void hideHUD() {
 		if (mTeclaHUDController != null) {
 			if (mTeclaHUDController.isVisible()) {
 				//FIXME: Abstract into unregisterConfigReceiver() method on mTeclaHUDController
 				unregisterReceiver(mTeclaHUDController.mConfigChangeReceiver);
 				mTeclaHUDController.hide();
 			}
 		}
 		if (mTeclaHighlighter != null) {
 			if (mTeclaHighlighter.isVisible()) {
 				mTeclaHighlighter.hide();
 			}
 		}
 	}
 
 	public void showHUD() {
 		if (mTeclaHighlighter != null) {
 			if (!mTeclaHighlighter.isVisible()) {
 				mTeclaHighlighter.show();
 			}
 		}
 		if (mTeclaHUDController != null) {
 			if (!mTeclaHUDController.isVisible()) {
 				mTeclaHUDController.show();
 			}
 		}
 		//FIXME: Abstract into registerConfigReceiver() method on mTeclaHUDController
 		registerReceiver(mTeclaHUDController.mConfigChangeReceiver, 
 				new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
 	}
 
 	public void hideFullscreenSwitch() {
 		if (mFullscreenSwitch != null) {
 			if (mFullscreenSwitch.isVisible()) {
 				mFullscreenSwitch.hide();
 			}
 		}
 	}
 
 	public void showFullscreenSwitch() {
 		if (mFullscreenSwitch != null) {
 			if (!mFullscreenSwitch.isVisible()) {
 				mFullscreenSwitch.show();
 			}
 		}
 	}
 
 	public void scanNextHUDButton() {
 		mTeclaHUDController.scanNext();
 	}
 	
 	public void showPreviewHUD() {
 		mTeclaHUDController.setPreviewHUD(true);
 		showHUD();
 	}
 	
 	public void hidePreviewHUD() {
 		mTeclaHUDController.setPreviewHUD(false);
 		hideHUD();
 	}
 	
 	public boolean isPreviewHUD() {
 		return mTeclaHUDController.isPreview();
 	}
 	
 	@Override
 	public void onAccessibilityEvent(AccessibilityEvent event) {
 		if (TeclaApp.getInstance().isSupportedIMERunning()) {
 			if (mTeclaHUDController.isVisible() && mTeclaHighlighter.isVisible()) {
 				int event_type = event.getEventType();
 				TeclaStatic.logD(CLASS_TAG, AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());
 
 				AccessibilityNodeInfo node = event.getSource();
 				if (node != null) {
 					if (event_type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
 						mPreviousOriginalNode = mOriginalNode;
 						mOriginalNode = node;				
 						mNodeIndex = 0;
 						searchAndUpdateNodes();
 					} else if (event_type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {	
 						mPreviousOriginalNode = mOriginalNode;
 						mOriginalNode = node;				
 						mNodeIndex = 0;
 						searchAndUpdateNodes();
 					} else if (event_type == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
 						if(mSelectedNode.getClassName().toString().contains("EditText"))
 								TeclaApp.ime.showWindow(true);
 						//searchAndUpdateNodes();
 					} else if (event_type == AccessibilityEvent.TYPE_VIEW_SELECTED) {
 						//searchAndUpdateNodes();
 					} else if(event_type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
 						mPreviousOriginalNode = mOriginalNode;
 						mOriginalNode = node;				
 						mNodeIndex = 0;
 						searchAndUpdateNodes();
 					} else if (event_type == AccessibilityEvent.TYPE_VIEW_CLICKED) {
 						//searchAndUpdateNodes();
 					}
 				} else {
 					TeclaStatic.logD(CLASS_TAG, "Node is null!");
 				}
 			}
 		}
 	}
 
 	private void searchAndUpdateNodes() {
 		//		TeclaHighlighter.clearHighlight();
 		searchActiveNodesBFS(mOriginalNode);
 		if (mActiveNodes.size() > 0 ) {
 			mSelectedNode = findNeighbourNode(mSelectedNode, DIRECTION_ANY);
 			if(mSelectedNode == null) mSelectedNode = mActiveNodes.get(0);
 			TeclaApp.highlighter.highlightNode(mSelectedNode);	
 			if(mPreviousOriginalNode != null) mPreviousOriginalNode.recycle();
 		}
 		//		TeclaHighlighter.highlightNode(mActiveNodes.get(0));
 	}
 
 	private void searchActiveNodesBFS(AccessibilityNodeInfo node) {
 		mActiveNodes.clear();
 		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
 		q.add(node);
 		while (!q.isEmpty()) {
 			AccessibilityNodeInfo thisnode = q.poll();
 			if(thisnode == null) continue;
 			if(thisnode.isVisibleToUser() && thisnode.isClickable() 
 					&& !thisnode.isScrollable()) {
 				//if(thisnode.isFocused() || thisnode.isSelected()) {
 				mActiveNodes.add(thisnode);
 				//}
 			}
 			for (int i=0; i<thisnode.getChildCount(); ++i) q.add(thisnode.getChild(i));
 		}
 	}
 
 	private void sortAccessibilityNodes(ArrayList<AccessibilityNodeInfo> nodes) {
 		ArrayList<AccessibilityNodeInfo> sorted = new ArrayList<AccessibilityNodeInfo>();
 		Rect bounds_unsorted_node = new Rect();
 		Rect bounds_sorted_node = new Rect();
 		boolean inserted = false; 
 		for(AccessibilityNodeInfo node: nodes) {
 			if(sorted.size() == 0) sorted.add(node);
 			else {
 				node.getBoundsInScreen(bounds_unsorted_node);
 				inserted = false; 
 				for (int i=0; i<sorted.size() && !inserted; ++i) {
 					sorted.get(i).getBoundsInScreen(bounds_sorted_node);
 					if(bounds_sorted_node.centerY() > bounds_unsorted_node.centerY()) {
 						sorted.add(i, node);
 						inserted = true;
 					} else if (bounds_sorted_node.centerY() == bounds_unsorted_node.centerY()) {
 						if(bounds_sorted_node.centerX() > bounds_unsorted_node.centerX()) {
 							sorted.add(i, node);
 							inserted = true;
 						}
 					}
 				}
 				if(!inserted) sorted.add(node);
 			}
 		}
 		nodes.clear();
 		nodes = sorted; 
 	}
 
 	public static void selectNode(int direction ) {
 		selectNode(sInstance.mSelectedNode,  direction );
 	}
 
 	public static void selectNode(AccessibilityNodeInfo refnode, int direction ) {
 		NodeSelectionThread thread = new NodeSelectionThread(refnode, direction);
 		thread.start();	
 	}
 
 	private static AccessibilityNodeInfo findNeighbourNode(AccessibilityNodeInfo refnode, int direction) {
 		int r2_min = Integer.MAX_VALUE;
 		int r2;
 		double ratio;
 		Rect refOutBounds = new Rect();
 		if(refnode == null) return null;
 		refnode.getBoundsInScreen(refOutBounds);
 		int x = refOutBounds.centerX();
 		int y = refOutBounds.centerY();
 		Rect outBounds = new Rect();
 		AccessibilityNodeInfo result = null; 
 		for (AccessibilityNodeInfo node: sInstance.mActiveNodes ) {
 			if(refnode.equals(node) && direction != DIRECTION_ANY) continue; 
 			node.getBoundsInScreen(outBounds);
 			r2 = (x - outBounds.centerX())*(x - outBounds.centerX()) 
 					+ (y - outBounds.centerY())*(y - outBounds.centerY());
 			switch (direction ) {
 			case DIRECTION_UP:
 				ratio =(y - outBounds.centerY())/Math.sqrt(r2);
 				if(ratio < Math.PI/4) continue; 
 				break; 
 			case DIRECTION_DOWN:
 				ratio =(outBounds.centerY() - y)/Math.sqrt(r2);
 				if(ratio < Math.PI/4) continue;
 				break; 
 			case DIRECTION_LEFT:
 				ratio =(x - outBounds.centerX())/Math.sqrt(r2);
 				if(ratio <= Math.PI/4) continue;
 				break; 
 			case DIRECTION_RIGHT:
 				ratio =(outBounds.centerX() - x)/Math.sqrt(r2);
 				if(ratio <= Math.PI/4) continue;
 				break; 
 			case DIRECTION_UP_NORATIOCONSTRAINT:
 				if(y - outBounds.centerY() <= 0) continue; 
 				break; 
 			case DIRECTION_DOWN_NORATIOCONSTRAINT:
 				if(outBounds.centerY() - y <= 0) continue;
 				break; 
 			case DIRECTION_LEFT_NORATIOCONSTRAINT:
 				if(x - outBounds.centerX() <= 0) continue;
 				break; 
 			case DIRECTION_RIGHT_NORATIOCONSTRAINT:
 				if(outBounds.centerX() - x <= 0) continue;
 				break; 
 			case DIRECTION_ANY:
 				break; 
 			default: 
 				break; 
 			}
 			if(r2 < r2_min) {
 				r2_min = r2;
 				result = node; 
 			}
 		}
 		return result;		
 	}
 
 	public static void clickActiveNode() {
 		if(sInstance.mActiveNodes.size() == 0) return;
 		if(sInstance.mSelectedNode == null) sInstance.mSelectedNode = sInstance.mActiveNodes.get(0); 
 		sInstance.mSelectedNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
 		if(sInstance.isHUDVisible()) 
 			TeclaApp.highlighter.clearHighlight();
 	}
 
 	//	public static void selectActiveNode(int index) {
 	//		if(sInstance.mActiveNodes.size()==0) return; 
 	//		sInstance.mNodeIndex = index;
 	//		sInstance.mNodeIndex = Math.min(sInstance.mActiveNodes.size() - 1, sInstance.mNodeIndex + 1);
 	//		sInstance.mNodeIndex = Math.max(0, sInstance.mNodeIndex - 1);
 	//		TeclaHighlighter.updateNodes(sInstance.mActiveNodes.get(sInstance.mNodeIndex));
 	//	}
 	//
 	//	public static void selectPreviousActiveNode() {
 	//		if(sInstance.mActiveNodes.size()==0) return; 
 	//		sInstance.mNodeIndex = Math.max(0, sInstance.mNodeIndex - 1);
 	//		TeclaHighlighter.updateNodes(sInstance.mOriginalNode, sInstance.mActiveNodes.get(sInstance.mNodeIndex));
 	//
 	//	}
 	//
 	//	public static void selectNextActiveNode() {
 	//		if(sInstance.mActiveNodes.size()==0) return;
 	//		sInstance.mNodeIndex = Math.min(sInstance.mActiveNodes.size() - 1, sInstance.mNodeIndex + 1);
 	//		TeclaHighlighter.updateNodes(sInstance.mOriginalNode, sInstance.mActiveNodes.get(sInstance.mNodeIndex));
 	//	}
 	//
 
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			if (action.equals(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED)) {
 				handleSwitchEvent(intent.getExtras());
 			}
 		}
 	};
 
 	private void handleSwitchEvent(Bundle extras) {
 		TeclaStatic.logD(CLASS_TAG, "Received switch event.");
 		SwitchEvent event = new SwitchEvent(extras);
 		if (event.isAnyPressed()) {
 			String[] actions = (String[]) extras.get(SwitchEvent.EXTRA_SWITCH_ACTIONS);
 			String action_tecla = actions[0];
 			int max_node_index = mActiveNodes.size() - 1;
 			switch(Integer.parseInt(action_tecla)) {
 
 			case SwitchEvent.ACTION_NEXT:
 				if(IMEAdapter.isShowingKeyboard()) IMEAdapter.scanNext();
 				else mTeclaHUDController.scanNext();
 				break;
 			case SwitchEvent.ACTION_PREV:
 				if(IMEAdapter.isShowingKeyboard()) IMEAdapter.scanPrevious();
 				else mTeclaHUDController.scanPrevious();
 				break;
 			case SwitchEvent.ACTION_SELECT:
 				if(IMEAdapter.isShowingKeyboard()) IMEAdapter.selectScanHighlighted();
 				else TeclaHUDOverlay.selectScanHighlighted();				
 				break;
 			case SwitchEvent.ACTION_CANCEL:
 				//TODO: Programmatic back key?
 			default:
 				break;
 			}
 			if(TeclaApp.persistence.isSelfScanningEnabled())
 				AutomaticScan.setExtendedTimer();
 		}
 	}
 
 	@Override
 	public void onInterrupt() {
 
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		shutdownInfrastructure();
 	}
 
 	/**
 	 * Shuts down the infrastructure in case it has been initialized.
 	 */
 	public void shutdownInfrastructure() {	
 		TeclaStatic.logD(CLASS_TAG, "Shutting down infrastructure...");
 		if (mBound) unbindService(mConnection);
 		SEPManager.stop(getApplicationContext());
 		if (mTeclaHUDController != null) {
 			if(mTeclaHUDController.isVisible()) {
 				unregisterReceiver(mTeclaHUDController.mConfigChangeReceiver);
 				mTeclaHUDController.hide();
 			}
 		}
 		if (mTeclaHighlighter != null) {
 			if (mTeclaHighlighter.isVisible()) {
 				mTeclaHighlighter.hide();
 			}
 		}
 		if (mFullscreenSwitch != null) {
 			if(mFullscreenSwitch.isVisible()) {
 				mFullscreenSwitch.hide();
 			}
 		}
 		if (register_receiver_called) {
 			unregisterReceiver(mReceiver);
 			register_receiver_called = false;
 		}
 	}
 
 	protected static class NodeSelectionThread extends Thread {
 		AccessibilityNodeInfo current_node;
 		int direction; 
 		public NodeSelectionThread(AccessibilityNodeInfo node, int dir) {
 			current_node = node;
 			direction = dir;
 		}
 		public void run() {
 			AccessibilityNodeInfo node;
 			if(direction == DIRECTION_UP
 					&& isFirstScrollNode(current_node) 
 					&& !isInsideParent(current_node)) {
 				current_node.getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
 				return;
 			} 
 			if(direction == DIRECTION_DOWN	
 					&& isLastScrollNode(current_node) 
 					&& !isInsideParent(current_node)) {
 				current_node.getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
 				return;
 			} 
 			mActionLock.lock();
 			node = findNeighbourNode(current_node, direction );
 			if(node == null) {
 				switch (direction ) {
 				case DIRECTION_UP:
 					node = findNeighbourNode(current_node, DIRECTION_UP_NORATIOCONSTRAINT);
 					break; 
 				case DIRECTION_DOWN:
 					node = findNeighbourNode(current_node, DIRECTION_DOWN_NORATIOCONSTRAINT);
 					break; 
 				case DIRECTION_LEFT:
 					node = findNeighbourNode(current_node, DIRECTION_LEFT_NORATIOCONSTRAINT);
 					break; 
 				case DIRECTION_RIGHT:
 					node = findNeighbourNode(current_node, DIRECTION_RIGHT_NORATIOCONSTRAINT);
 					break; 
 				default: 
 					break; 
 				}
 			}			
 			if(node != null) {
 				sInstance.mSelectedNode = node;
 				if(node.getClassName().toString().contains("EditText")) {
 					node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
 				}
 			}
 			mActionLock.unlock(); 			
 			
 			TeclaApp.highlighter.highlightNode(sInstance.mSelectedNode);
 		}
 	}
 
 	public static boolean hasScrollableParent(AccessibilityNodeInfo node) {
 		if(node == null) return false;
 		AccessibilityNodeInfo parent = node.getParent();
 		if (parent != null) {
 			if(!parent.isScrollable()) return false;
 		}
 		return true;
 	}
 
 	public static boolean isFirstScrollNode(AccessibilityNodeInfo node) {
 		if(!hasScrollableParent(node)) return false;
 		AccessibilityNodeInfo parent = node.getParent();
 		AccessibilityNodeInfo  firstScrollNode = null;
 		for(int i=0; i<parent.getChildCount(); ++i) {
 			AccessibilityNodeInfo  aNode = parent.getChild(i);
 			if(aNode.isVisibleToUser() && aNode.isClickable()) {
 				firstScrollNode = aNode;
 				break;
 			}
 		}
 
 		return isSameNode(node, firstScrollNode);
 	}
 
 	public static boolean isLastScrollNode(AccessibilityNodeInfo node) {
 		if(!hasScrollableParent(node)) return false;
 		AccessibilityNodeInfo parent = node.getParent();
 		AccessibilityNodeInfo  lastScrollNode = null;
 		for(int i=parent.getChildCount()-1; i>=0; --i) {
 			AccessibilityNodeInfo aNode = parent.getChild(i);
 			if(aNode.isVisibleToUser() && aNode.isClickable()) {
 				lastScrollNode = aNode;
 				break;
 			}
 		}	
 		return isSameNode(node, lastScrollNode);
 	}
 
 	public static boolean isSameNode(AccessibilityNodeInfo node1, AccessibilityNodeInfo node2) {
 		if(node1 == null || node2 == null) return false;
 		Rect node1_rect = new Rect(); 
 		node1.getBoundsInScreen(node1_rect);	
 		Rect node2_rect = new Rect(); 
		node2.getBoundsInScreen(node2_rect);	
 		if(node1_rect.left == node2_rect.left
 				&& node1_rect.right == node2_rect.right
 				&& node1_rect.top == node2_rect.top
 				&& node1_rect.bottom == node2_rect.bottom) 
 			return true;
 		return false;
 	}
 	
 	public static boolean isInsideParent(AccessibilityNodeInfo node) {
 		if(node == null) return false;
 		AccessibilityNodeInfo parent = node.getParent();
 		if(parent == null) return false;
 		Rect node_rect = new Rect();
 		Rect parent_rect = new Rect();
 		node.getBoundsInScreen(node_rect);
 		parent.getBoundsInScreen(parent_rect);
 		if(node_rect.top >= parent_rect.top
 				&& node_rect.bottom <= parent_rect.bottom
 				&& node_rect.left >= parent_rect.left
 				&& node_rect.right <= parent_rect.right) 
 			return true;
 		return false;
 	}
 
 	public void sendGlobalBackAction() {
 		sInstance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
 	}
 
 	public void sendGlobalHomeAction() {
 		sInstance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);		
 	}	
 
 	public void sendGlobalNotificationAction() {
 		sInstance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);		
 	}	
 
 	public void injectSwitchEvent(SwitchEvent event) {
 		switch_event_provider.injectSwitchEvent(event);
 	}
 
 	public void injectSwitchEvent(int switchChanges, int switchStates) {
 		switch_event_provider.injectSwitchEvent(switchChanges, switchStates);
 	}
 
 	SwitchEventProvider switch_event_provider;
 	boolean mBound = false;
 
 	/** Defines callbacks for service binding, passed to bindService() */
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName arg0, IBinder service) {
 			// We've bound to LocalService, cast the IBinder and get LocalService instance
 			LocalBinder binder = (LocalBinder) service;
 			switch_event_provider = binder.getService();
 			mBound = true;
 			TeclaStatic.logD(CLASS_TAG, "IME bound to SEP");
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 
 		}
 	};
 }
