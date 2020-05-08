 package ca.idrc.tecla;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.WindowManager;
 import android.view.accessibility.AccessibilityNodeInfo;
 
 public class TeclaHighlighter extends SimpleOverlay {
 
     private static TeclaHighlighter sInstance;
 
     private final HighlightBoundsView mInnerBounds;
     private final HighlightBoundsView mOuterBounds;
     
 	public TeclaHighlighter(Context context) {
 		super(context);
 		final WindowManager.LayoutParams params = getParams();
 		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
 		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
 		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 		params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;		
 		setParams(params);
 		
 		setContentView(R.layout.tecla_highlighter);
 
 		mInnerBounds = (HighlightBoundsView) findViewById(R.id.announce_bounds);
 //		mAnnounceBounds.setHighlightColor(Color.argb(0xff, 0x21, 0xad, 0xe3));
 		mInnerBounds.setHighlightColor(Color.WHITE);
 		
 		
 		mOuterBounds = (HighlightBoundsView) findViewById(R.id.bounds);
 		mOuterBounds.setHighlightColor(Color.argb(0xdd, 0x38, 0x38, 0x38));
 	}
 
 	@Override
 	protected void onShow() {
 		sInstance = this;
 	}
 
 	@Override
 	protected void onHide() {
         sInstance = null;
         mOuterBounds.clear();
         mInnerBounds.clear();
 	}
 	
 
 	public static void clearHighlight() {
         sInstance.mOuterBounds.clear();
         sInstance.mInnerBounds.clear();
        sInstance.mOuterBounds.postInvalidate();
        sInstance.mInnerBounds.postInvalidate();
 	}
 	
     public static void removeInvalidNodes() {
         if (sInstance == null) {
             return;
         }
 
         sInstance.mOuterBounds.removeInvalidNodes();
         sInstance.mOuterBounds.postInvalidate();
 
         sInstance.mInnerBounds.removeInvalidNodes();
         sInstance.mInnerBounds.postInvalidate();
     }
 
     public static void highlightNode(AccessibilityNodeInfo announced) {
         if (sInstance == null) {
             return;
         }
 
         clearHighlight();
         if(announced != null) {
             sInstance.mOuterBounds.setStrokeWidth(20);
             sInstance.mOuterBounds.add(announced);
             sInstance.mOuterBounds.postInvalidate();        	
             sInstance.mInnerBounds.setStrokeWidth(6);
             sInstance.mInnerBounds.add(announced);
             sInstance.mInnerBounds.postInvalidate();
         	
         }
     }
     
 }
