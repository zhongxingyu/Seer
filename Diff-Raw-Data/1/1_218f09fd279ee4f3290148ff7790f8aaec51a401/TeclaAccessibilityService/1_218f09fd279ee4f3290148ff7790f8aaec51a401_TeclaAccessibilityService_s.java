 package ca.idrc.tecla;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import android.accessibilityservice.AccessibilityService;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.accessibility.AccessibilityEvent;
 import android.view.accessibility.AccessibilityNodeInfo;
 
 public class TeclaAccessibilityService extends AccessibilityService {
 
 	private final static boolean DEBUG = true;
 	
 	private AccessibilityNodeInfo mOriginalNode, mScanNode, activeNode;
 	private ArrayList<AccessibilityNodeInfo> mScanNodes, mActiveNodes;
 	private int mScanNodeIndex, mTouchMode;
 	private TeclaAccessibilityOverlay mTeclaAccessibilityOverlay;
 	
 	// used for debugging 
 	private final static int TOUCHED_TOPLEFT = 0;
 	private final static int TOUCHED_TOPRIGHT = 1;
 	private final static int TOUCHED_BOTTOMLEFT = 2;
 	private final static int TOUCHED_BOTTOMRIGHT = 3;
 	private int touchdown;
 	private int touchup;
 	
 	@Override
 	protected void onServiceConnected() {
 		super.onServiceConnected();
 		Log.d("TeclaA11y", "Tecla Accessibility Service Connected!");
 		
 		mScanNodes = new ArrayList<AccessibilityNodeInfo>();
 		mActiveNodes = new ArrayList<AccessibilityNodeInfo>();
 		mTouchMode = 0;
 		
 		if (mTeclaAccessibilityOverlay == null) {
 			mTeclaAccessibilityOverlay = new TeclaAccessibilityOverlay(this);
 			mTeclaAccessibilityOverlay.show();
 		}
 		
 		if(DEBUG) {
 			mTeclaAccessibilityOverlay.getRootView().setOnTouchListener(mOverlayTouchListener);
 			mTeclaAccessibilityOverlay.getRootView().setOnLongClickListener(mOverlayLongClickListener);
 		}
 		
 	}
 
 	@Override
 	public void onAccessibilityEvent(AccessibilityEvent event) {
 		int event_type = event.getEventType();
 		Log.d("TeclaA11y", AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());
 		
 		AccessibilityNodeInfo node = event.getSource();
 		if (node != null) {
 			if (event_type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
 				mOriginalNode = node;
 				activeNode = searchActiveNodeBFS(mOriginalNode);
 				TeclaAccessibilityOverlay.updateNodes(mOriginalNode, activeNode);				
 			} else if (event_type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {	
 				
 			} else if (event_type == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
 				searchActiveNodesBFS(mOriginalNode);
 				TeclaAccessibilityOverlay.updateNodes(mOriginalNode, mActiveNodes.get(mActiveNodes.size()-1));
 			} else if (event_type == AccessibilityEvent.TYPE_VIEW_SELECTED) {
 				activeNode = searchActiveNodeBFS(mOriginalNode);
 				TeclaAccessibilityOverlay.updateNodes(mOriginalNode, activeNode);
 			} else if(event_type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
 				/*
 				activeNode = searchActiveNodeBFS(mOriginalNode);
 				TeclaAccessibilityOverlay.updateNodes(mOriginalNode, activeNode);
 				*/
 				/*
 				mOriginalNode = node;
 				populateRefNodesBFS(node);
 				for (int i=0; i<mScanNodes.size(); ++i) {
 					AccessibilityNodeInfo searchNode = mScanNodes.get(i);
 					if(searchNode == mScanNode) {
 						mScanNodeIndex = i;
 						mScanNode = mScanNodes.get(i);	
 						break; 
 					}
 				}	
 				TeclaAccessibilityOverlay.updateNodes(mOriginalNode, mScanNode);	
 				Log.w("TeclaA11y", "Scrolled ");
 				*/
 			}
 		} else {
 			Log.e("TeclaA11y", "Node is null!");
 		}
 	}
 
 	private AccessibilityNodeInfo searchActiveNodeBFS(AccessibilityNodeInfo node) {
 		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
 		q.add(node);
 		while (!q.isEmpty()) {
 			AccessibilityNodeInfo thisnode = q.poll();
 			if(thisnode.isVisibleToUser() && thisnode.isClickable() && !thisnode.isScrollable()) {
 				if(thisnode.isFocused() || thisnode.isSelected()) {
 					return thisnode;
 				}
 			}
 			for (int i=0; i<thisnode.getChildCount(); ++i) q.add(thisnode.getChild(i));
 		}
 		return null;
 	}
 	
 	private void searchActiveNodesBFS(AccessibilityNodeInfo node) {
 		mActiveNodes.clear();
 		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
 		q.add(node);
 		while (!q.isEmpty()) {
 			AccessibilityNodeInfo thisnode = q.poll();
 			if(thisnode.isVisibleToUser() && thisnode.isClickable() && !thisnode.isScrollable()) {
 				if(thisnode.isFocused() || thisnode.isSelected()) {
 					mActiveNodes.add(thisnode);
 				}
 			}
 			for (int i=0; i<thisnode.getChildCount(); ++i) q.add(thisnode.getChild(i));
 		}
 	}
 	
 	// find the scan nodes with breadth first search 
 	private void populateRefNodesBFS(AccessibilityNodeInfo node) {
 		mScanNodes.clear();
 		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
 		q.add(node);
 		while (!q.isEmpty()) {
 			AccessibilityNodeInfo thisnode = q.poll();
 			if(thisnode.isVisibleToUser() && thisnode.isClickable()) mScanNodes.add(thisnode);
 			for (int i=0; i<thisnode.getChildCount(); ++i) q.add(thisnode.getChild(i));
 		}
 		Log.w("TeclaA11y", "There are " + mScanNodes.size() + " elements in the scan node list.");
 	}
 	
 	private boolean checkScroll(AccessibilityNodeInfo scanNode, AccessibilityNodeInfo nextScanNode) {
 		AccessibilityNodeInfo parent1 = scanNode.getParent();
 		AccessibilityNodeInfo parent2 = nextScanNode.getParent();
 		if(!parent1.isScrollable()) return false;
 		Log.w("TeclaA11y", parent1.toString());
 		Log.w("TeclaA11y", parent2.toString());
 		Log.w("TeclaA11y", "Same parent = " + (parent1.toString().equals(parent2.toString())));
 		/*
 		if(parent1 != parent2) { // scroll (assumed backwards) 
 			parent1.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
 			return true; 
 		}
 		*/
 		return false;  
 	}
 	
 	@Override
 	public void onInterrupt() {
 		
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 	}
 
 	private View.OnTouchListener mOverlayTouchListener = new View.OnTouchListener() {
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			int width = v.getWidth();
 			int height = v.getHeight();
 			float x=event.getX();
 			float y=event.getY();
 			float logicalX=x/width;
 			float logicalY=y/height;
 			switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				Log.v("TeclaA11y", "Tecla Overlay Touch Down! " + Float.toString(logicalX) + " " + Float.toString(logicalY));
 				
 				if(logicalX<0.5 && logicalY<0.5) {
 					touchdown = TeclaAccessibilityService.TOUCHED_TOPLEFT; 
 				} else if(logicalX<0.5 && logicalY>=0.5) {
 					touchdown = TeclaAccessibilityService.TOUCHED_BOTTOMLEFT;
 				} else if(logicalX>=0.5 && logicalY<0.5) {
 					touchdown = TeclaAccessibilityService.TOUCHED_TOPRIGHT;
 				} else if(logicalX>=0.5 && logicalY>=0.5) {
 					touchdown = TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT;
 				}
 				break;
 			case MotionEvent.ACTION_UP:
 				Log.v("TeclaA11y", "Tecla Overlay Touch Up! " + Float.toString(logicalX) + " " + Float.toString(logicalY));
 
 				if(logicalX<0.5 && logicalY<0.5) {
 					touchup = TeclaAccessibilityService.TOUCHED_TOPLEFT; 
 				} else if(logicalX<0.5 && logicalY>=0.5) {
 					touchup = TeclaAccessibilityService.TOUCHED_BOTTOMLEFT;
 				} else if(logicalX>=0.5 && logicalY<0.5) {
 					touchup = TeclaAccessibilityService.TOUCHED_TOPRIGHT;
 				} else if(logicalX>=0.5 && logicalY>=0.5) {
 					touchup = TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT;
 				}
 				
 				if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPLEFT) {
 					
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPRIGHT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
 					
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT) {
 					if(mTouchMode == 1) {
 						break; 
 					}
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
 					if(mTouchMode == 1) {
 						break; 
 					}
 					Log.w("TeclaA11y", "Current node: " + activeNode.toString());
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
 					
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
 					
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
 					// shut down   
 					++mTouchMode;
 					mTouchMode = mTouchMode%2;
 					Log.w("TeclaA11y", "Touch mode = " + mTouchMode);
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
 					// shut down  
 					Log.w("TeclaA11y", "3-switch access: SHUTDOWN");
 					shutdownInfrastructure();
 				}
 				break;
 			default:
 				break;
 			}
 			return true;
 		}
 		
 	};
 
 	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {
 		
 		@Override
 		public boolean onLongClick(View v) {
 			shutdownInfrastructure();
 			return true;
 		}
 	};
 	
 	/**
 	 * Shuts down the infrastructure in case it has been initialized.
 	 */
 	private void shutdownInfrastructure() {		
 		if (mTeclaAccessibilityOverlay != null) {
 			mTeclaAccessibilityOverlay.hide();
 			mTeclaAccessibilityOverlay = null;
 		}
 	}
 }
