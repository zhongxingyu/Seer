 package com.android.tecla.keyboard;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.concurrent.locks.ReentrantLock;
 
 
 import ca.idi.tecla.sdk.SwitchEvent;
 import ca.idi.tecla.sdk.SEPManager;
 import ca.idrc.tecla.framework.Persistence;
 import ca.idrc.tecla.framework.TeclaStatic;
 import ca.idrc.tecla.highlighter.TeclaHighlighter;
 
 import android.accessibilityservice.AccessibilityService;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Rect;
 import android.os.Bundle;
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
 	
 	protected static TeclaAccessibilityService sInstance;
 
 	private AccessibilityNodeInfo mOriginalNode, mPreviousOriginalNode;
 	protected AccessibilityNodeInfo mSelectedNode;
 
 	private ArrayList<AccessibilityNodeInfo> mActiveNodes;
 	private int mNodeIndex;
 
 	private TeclaHighlighter mTeclaHighlighter;
 	protected TeclaHUDOverlay mTeclaHUDController;
 	private SingleSwitchTouchInterface mTouchInterface;
 
 	public static TeclaAccessibilityService getInstance() {
 		return sInstance;
 	}
 
 	protected static ReentrantLock mActionLock;
 	
 	@Override
 	protected void onServiceConnected() {
 		super.onServiceConnected();
 		TeclaStatic.logD(CLASS_TAG, "Tecla Accessibility Service Connected!");
 
 		sInstance = this;
 
 		mOriginalNode = null;
 		mActiveNodes = new ArrayList<AccessibilityNodeInfo>();
 		mActionLock = new ReentrantLock();
 		
 		if (mTeclaHighlighter == null) {
 			mTeclaHighlighter = new TeclaHighlighter(this);
 			mTeclaHighlighter.show();
 		}
 
 		if (mTeclaHUDController == null) {
 			mTeclaHUDController = new TeclaHUDOverlay(this);
 			mTeclaHUDController.show();
 			registerReceiver(mTeclaHUDController.mConfigChangeReceiver, 
 					new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
 		}
 
 		if (mTouchInterface == null) {
 			mTouchInterface = new SingleSwitchTouchInterface(this);
 			mTouchInterface.show();
 		}
 
		performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
		
 		//registerReceiver(mReceiver, new IntentFilter(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED));
 		//SEPManager.start(this);
 	}
 
 	@Override
 	public void onAccessibilityEvent(AccessibilityEvent event) {
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
 					//searchAndUpdateNodes();
 				} else if (event_type == AccessibilityEvent.TYPE_VIEW_SELECTED) {
 					//searchAndUpdateNodes();
 				} else if(event_type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
 					mPreviousOriginalNode = mOriginalNode;
 					mOriginalNode = node;				
 					mNodeIndex = 0;
 					searchAndUpdateNodes();
 				}
 			} else {
 				TeclaStatic.logD(CLASS_TAG, "Node is null!");
 			}
 		}
 	}
 
 	private void searchAndUpdateNodes() {
 //		TeclaHighlighter.clearHighlight();
 		searchActiveNodesBFS(mOriginalNode);
 		if (mActiveNodes.size() > 0 ) {
 			mSelectedNode = findNeighbourNode(mSelectedNode, DIRECTION_ANY);
 			if(mSelectedNode == null) mSelectedNode = mActiveNodes.get(0);
 			TeclaHighlighter.highlightNode(mSelectedNode);	
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
 		selectNode(getInstance().mSelectedNode,  direction );
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
 		for (AccessibilityNodeInfo node: TeclaAccessibilityService.sInstance.mActiveNodes ) {
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
 		TeclaHighlighter.clearHighlight();
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
 	
 //	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 //
 //		@Override
 //		public void onReceive(Context context, Intent intent) {
 //			String action = intent.getAction();
 //
 //			if (action.equals(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED)) {
 //				handleSwitchEvent(intent.getExtras());
 //			}
 //		}
 //	};
 
 	private void handleSwitchEvent(Bundle extras) {
 		SwitchEvent event = new SwitchEvent(extras);
 		if (event.isAnyPressed()) {
 			String[] actions = (String[]) extras.get(SwitchEvent.EXTRA_SWITCH_ACTIONS);
 			String action_tecla = actions[0];
 			int max_node_index = mActiveNodes.size() - 1;
 			switch(Integer.parseInt(action_tecla)) {
 
 			case SwitchEvent.ACTION_NEXT:
 				if (max_node_index > mNodeIndex) {
 					mNodeIndex++;
 					TeclaHighlighter.highlightNode(mActiveNodes.get(mNodeIndex));
 				}
 				break;
 			case SwitchEvent.ACTION_PREV:
 				if (mNodeIndex > 0) {
 					mNodeIndex--;
 					TeclaHighlighter.highlightNode(mActiveNodes.get(mNodeIndex));
 				}
 				break;
 			case SwitchEvent.ACTION_SELECT:
 				mActiveNodes.get(mNodeIndex).performAction(AccessibilityNodeInfo.ACTION_CLICK);
 				break;
 			case SwitchEvent.ACTION_CANCEL:
 				//TODO: Programmatic back key?
 			default:
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void onInterrupt() {
 
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		SEPManager.stop(this);
 		shutdownInfrastructure();
 		if(mTeclaHUDController != null) unregisterReceiver(mTeclaHUDController.mConfigChangeReceiver);
 //		unregisterReceiver(mReceiver);
 	}
 
 	/**
 	 * Shuts down the infrastructure in case it has been initialized.
 	 */
 	public void shutdownInfrastructure() {	
 		TeclaStatic.logD(CLASS_TAG, "Shutting down infrastructure...");
 		unregisterReceiver(mTeclaHUDController.mConfigChangeReceiver);
 		if(mTeclaHUDController != null) {
 			mTeclaHUDController.hide();
 //			mTeclaHUDController = null;
 		}
 		if (mTeclaHighlighter != null) {
 			mTeclaHighlighter.hide();
 //			mTeclaHighlighter = null;
 		}
 		if(mTouchInterface != null) {
 			mTouchInterface.hide();
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
 				getInstance().mSelectedNode = node;
 			}
 			mActionLock.unlock();   
 			TeclaHighlighter.highlightNode(getInstance().mSelectedNode);
 	    }
 	}
 }
