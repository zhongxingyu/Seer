 package ca.idrc.tecla;
 
 import android.accessibilityservice.AccessibilityService;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.accessibility.AccessibilityEvent;
 import android.view.accessibility.AccessibilityNodeInfo;
 
 public class TeclaAccessibilityService extends AccessibilityService {
 
 	private final static boolean DEBUG = true;
 	
 	private AccessibilityNodeInfo original, parent;
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
 		Log.w("TeclaA11y", "I was here.  ");
 		Log.d("TeclaA11y", AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());
 		
 		AccessibilityNodeInfo node = event.getSource();
 		if (node != null) {
 			//if (event_type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
 				Log.w("TeclaA11y", "Updating node!");
 				updateTeclaASNodeInfo(node);
 				if(DEBUG) logNode(node, true);
 				
 				TeclaAccessibilityOverlay.updateNodes(parent, null);
 			//}
 		} else {
 			Log.e("TeclaA11y", "Node is null!");
 		}
 	}
 
 	private void updateTeclaASNodeInfo(AccessibilityNodeInfo node) {
 		original = node;
 		parent = findMultipleChildAncestor(node);
 		Log.d("TeclaA11y", "New parent window ID " + parent.getWindowId()); // + " with " + child_count + " children");
 	}
 
 	private AccessibilityNodeInfo findMultipleChildAncestor(AccessibilityNodeInfo node) {
 		AccessibilityNodeInfo multiple_child_ancestor = node;
 		if (multiple_child_ancestor != null) {
 			if (getActiveChildCount(multiple_child_ancestor) == 1) {
 				multiple_child_ancestor = findFirstActiveChild(multiple_child_ancestor);
 				multiple_child_ancestor = findMultipleChildAncestor(multiple_child_ancestor);
 			}
 		}
 		return multiple_child_ancestor;
 	}
 
 	private AccessibilityNodeInfo findFirstActiveChild(AccessibilityNodeInfo node) {
 		AccessibilityNodeInfo active_child = null;
 		if (node != null && node.getChildCount() > 0) {
 			AccessibilityNodeInfo child;
 			int i = 0;
 			while(active_child == null) {
 				child = node.getChild(i);
 				if (hasVisibleClickableNode(child)) {
 					active_child = child;
 				}
 				i++;
 			}
 		}
 		return active_child;
 	}
 	
 	private int getActiveChildCount(AccessibilityNodeInfo node) {
 		int active_child_count = 0;
 		if (node != null) {
 			int child_count = node.getChildCount();
 			AccessibilityNodeInfo child;
 			for (int i=0;i < child_count; i++) {
 				child = node.getChild(i);
 				if (hasVisibleClickableNode(child)) {
 					active_child_count++;
 				}
 			}
 		}
 		return active_child_count;
 	}
 
 	private boolean hasVisibleClickableNode(AccessibilityNodeInfo node) {
 		boolean is_active = false;
 		if (node != null) {
 			if (node.isVisibleToUser()){
 				is_active = node.isClickable();
 				if (!is_active) {
 					AccessibilityNodeInfo child = null;
 					int i = 0;
 					while ((i < node.getChildCount()) && !is_active) {
 						child = node.getChild(i);
 						if (child != null) {
 							if (child.isClickable()) {
 								is_active = true;
 							} else {
 								is_active = hasVisibleClickableNode(child);
 							}
 						}
 						i++;
 					}
 				}
 			}
 		}
 		return is_active;
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
 					// it's a left! 
 					Log.w("TeclaA11y", "6-switch access: LEFT");
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPRIGHT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
 					// it's an up!  
 					Log.w("TeclaA11y", "6-switch access: UP");
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT) {
 					// it's a down!  
 					Log.w("TeclaA11y", "6-switch access: DOWN");
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
 					// it's a right!  
 					Log.w("TeclaA11y", "6-switch access: RIGHT");
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
 					// it's a send!
 					Log.w("TeclaA11y", "6-switch access: SEND");
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
 					// it's a cancel!  
 					Log.w("TeclaA11y", "6-switch access: CANCEL");
 				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
 					// shut down   
 					Log.w("TeclaA11y", "6-switch access: SHUTDOWN");
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
 	
 	private static void logNode(AccessibilityNodeInfo node, boolean lookForChildren) {
 
		String s;
		
 		Log.w("TeclaA11y", "toString " + node.toString());
 		Log.w("TeclaA11y", "getActions " + Integer.toString(node.getActions()));
		CharSequence c = node.getContentDescription();
		if(c != null) {
			s = c.toString();
			Log.w("TeclaA11y", "getContentDescription " + s);
		}
 		Log.w("TeclaA11y", "getMovementGranularities " + Integer.toString(node.getMovementGranularities()));
 		Log.w("TeclaA11y", "getText " + node.getText().toString());
 		Log.w("TeclaA11y", "isAccessibilityFocused " + Boolean.toString(node.isAccessibilityFocused()));
 		Log.w("TeclaA11y", "isCheckable " + Boolean.toString(node.isCheckable()));
 		Log.w("TeclaA11y", "isChecked " + Boolean.toString(node.isChecked()));
 		Log.w("TeclaA11y", "isEnabled " + Boolean.toString(node.isEnabled()));
 		Log.w("TeclaA11y", "isFocusable " + Boolean.toString(node.isFocusable()));
 		Log.w("TeclaA11y", "isFocused " + Boolean.toString(node.isFocused()));
 		Log.w("TeclaA11y", "isLongClickable " + Boolean.toString(node.isLongClickable()));
 		Log.w("TeclaA11y", "isPassword " + Boolean.toString(node.isPassword()));
 		Log.w("TeclaA11y", "isScrollable " + Boolean.toString(node.isScrollable()));
 		Log.w("TeclaA11y", "isSelected " + Boolean.toString(node.isSelected()));
 		Log.w("TeclaA11y", "isVisibleToUser " + Boolean.toString(node.isVisibleToUser()));		
 		Log.w("TeclaA11y", "getChildCount " + Integer.toString(node.getChildCount()));
 		if(lookForChildren) {
 			for (int i=0; i<node.getChildCount(); ++i) {
 				AccessibilityNodeInfo node1 = node.getChild(i);
 				Log.w("TeclaA11y", "Child Node #" + Integer.toString(i+1) + ": ");
				if(node1 == null) continue; 
 				logNode(node1, false);
 			}
 		}
 		
 	}
 }
