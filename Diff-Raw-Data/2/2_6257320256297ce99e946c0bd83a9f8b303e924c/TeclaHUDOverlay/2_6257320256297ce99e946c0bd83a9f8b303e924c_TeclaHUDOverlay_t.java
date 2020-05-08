 package com.android.tecla.addon;
 
 import java.util.ArrayList;
 
 import ca.idrc.tecla.R;
 import ca.idrc.tecla.framework.SimpleOverlay;
 import ca.idrc.tecla.framework.TeclaStatic;
 import ca.idrc.tecla.hud.TeclaHUDButtonView;
 
 import android.animation.AnimatorInflater;
 import android.animation.AnimatorSet;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Point;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Display;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.accessibility.AccessibilityNodeInfo;
 
 import com.android.tecla.addon.TeclaApp;
 
 public class TeclaHUDOverlay extends SimpleOverlay {
 
 	/**
 	 * Tag used for logging in the whole framework
 	 */
 	public static final String CLASS_TAG = "TeclaHUDOverlay";
 
 	private final static byte HUD_BTN_TOP = 0;
 	private final static byte HUD_BTN_TOPRIGHT = 1;
 	private final static byte HUD_BTN_RIGHT = 2;
 	private final static byte HUD_BTN_BOTTOMRIGHT = 3;
 	private final static byte HUD_BTN_BOTTOM = 4;
 	private final static byte HUD_BTN_BOTTOMLEFT = 5;
 	private final static byte HUD_BTN_LEFT = 6;
 	private final static byte HUD_BTN_TOPLEFT = 7;
 
 	private Context mContext;
 	private Resources mResources;
 	private float side_width_proportion;
 	private float stroke_width_proportion;
 	private float scan_alpha_max;
 
 	private final WindowManager mWindowManager;
 	private static TeclaHUDOverlay sInstance;
 
 	private ArrayList<TeclaHUDButtonView> mHUDPad;
 	private ArrayList<AnimatorSet> mHUDAnimators;
 	private byte mScanIndex;
 
 	public TeclaHUDOverlay(Context context) {
 		super(context);
 
 		mContext = context;
 		mResources = mContext.getResources();
 		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
 
 		scan_alpha_max = Float.parseFloat(mResources.getString(R.string.scan_alpha_max));
 		side_width_proportion = Float.parseFloat(mResources.getString(R.string.side_width_proportion));
 		stroke_width_proportion = Float.parseFloat(mResources.getString(R.string.stroke_width_proportion));
 
 		final WindowManager.LayoutParams params = getParams();
 		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
 		params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;	
 		setParams(params);
 
 		setContentView(R.layout.tecla_hud);
 
 		/*View rView = getRootView();
 
 		rView.setOnLongClickListener(mOverlayLongClickListener);
 		rView.setOnClickListener(mOverlayClickListener);*/
 
 		findAllButtons();        
 		fixHUDLayout();
 
 		mScanIndex = 0;
 
 		for (int i = 0; i < mHUDPad.size(); i++) {
 			mHUDPad.get(i).setAlpha(scan_alpha_max);
 		}
 
 		mHUDAnimators = new ArrayList<AnimatorSet>();
 		for (int i = 0; i < mHUDPad.size(); i++) {
 			mHUDAnimators.add((AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.hud_alpha_animator));
 			mHUDAnimators.get(i).setTarget(mHUDPad.get(i));
 		}
 
 		if(TeclaApp.persistence.isSelfScanningEnabled())
 			mAutoScanHandler.sleep(TeclaApp.persistence.getScanDelay());
 	}
 
 	// memory storage for HUD values during preview
 	private boolean[] mHUDPadHighlightVal;
 	private float[] mHUDPadAlphaVal;
 	
 	public void setPreviewHUD(boolean preview) {
 		if(preview) {
 			for(int i=0; i<mHUDPad.size(); ++i) {
 				mHUDPadHighlightVal[i] = mHUDPad.get(i).isHighlighted();
 				mHUDPadAlphaVal[i] = mHUDPad.get(i).getAlpha();
 				mHUDPad.get(i).setHighlighted(true);
 				mHUDPad.get(i).setAlpha(0.5f);
 			}
 		} else {
 			for(int i=0; i<mHUDPad.size(); ++i) {
 				mHUDPad.get(i).setHighlighted(mHUDPadHighlightVal[i]);
 				mHUDPad.get(i).setAlpha(mHUDPadAlphaVal[i]);
 			}
 		}
 	}
 	
 	@Override
 	protected void onShow() {
 		sInstance = this;
 		TeclaApp.persistence.setHUDShowing(true);
 	}
 
 	@Override
 	protected void onHide() {
 		sInstance = null;
 		TeclaApp.persistence.setHUDShowing(false);
 	}
 
 	protected BroadcastReceiver mConfigChangeReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Configuration conf = context.getResources().getConfiguration();
 
 			fixHUDLayout(); // resizes properly for both orientations
 			//			if(conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			//				fixHUDLayout();
 			//			} else {
 			//				fixHUDLayout();
 			//			}
 		}		
 	};
 
 	private View.OnClickListener mOverlayClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if(IMEAdapter.isShowingKeyboard()) IMEAdapter.selectScanHighlighted();
 			else scanTrigger();
 		}
 	};	
 
 	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {
 
 		@Override
 		public boolean onLongClick(View v) {
 			TeclaStatic.logV(CLASS_TAG, "Long clicked.  ");
 			TeclaApp.persistence.setHUDCancelled(true);
 			TeclaApp.a11yservice.shutdownInfrastructure();
 			return true;
 		}
 	};
 
 	public static void selectScanHighlighted() {
 		TeclaHUDOverlay.sInstance.scanTrigger();
 	}
 
 	protected void scanTrigger() {
 
 		AccessibilityNodeInfo node = TeclaApp.a11yservice.mSelectedNode;
 		AccessibilityNodeInfo parent = null;
 		if(node != null) parent = node.getParent();
 		int actions = 0;
 		if(parent != null) actions = node.getParent().getActions();
 				
 		switch (mScanIndex){
 		case HUD_BTN_TOP:
 			if(TeclaAccessibilityService.isFirstScrollNode(node) 
 					&& (actions & AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) 
 					== AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
 				parent.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
 			} else
 				TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_UP);
 			break;
 		case HUD_BTN_BOTTOM:
 			if(TeclaAccessibilityService.isLastScrollNode(node)
 					&& (actions & AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) 
 					== AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
 				node.getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
 			} else 
 				TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_DOWN);
 			break;
 		case HUD_BTN_LEFT:
 			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_LEFT);
 			break;
 		case HUD_BTN_RIGHT:
 			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_RIGHT);
 			break;
 		case HUD_BTN_TOPRIGHT:
 			TeclaAccessibilityService.clickActiveNode();
 			break;
 		case HUD_BTN_BOTTOMLEFT:
 			TeclaAccessibilityService.sendGlobalBackAction();
 			/*if(Persistence.isDefaultIME(mContext) && TeclaApp.persistence.isIMERunning()) {
 				TeclaStatic.logI(CLASS_TAG, "LatinIME is active");
 				TeclaApp.ime.pressBackKey();
 			} else TeclaStatic.logW(CLASS_TAG, "LatinIME is not active!");*/
 			break;
 		case HUD_BTN_TOPLEFT:
 			TeclaAccessibilityService.sendGlobalHomeAction();
 			/*if(Persistence.isDefaultIME(mContext) && TeclaApp.persistence.isIMERunning()) {
 				TeclaStatic.logI(CLASS_TAG, "LatinIME is active");
 				TeclaApp.ime.pressHomeKey();
 			} else TeclaStatic.logW(CLASS_TAG, "LatinIME is not active!");*/
 			break;
 		}
 		
 		if(TeclaApp.persistence.isSelfScanningEnabled())
 			mAutoScanHandler.sleep(TeclaApp.persistence.getScanDelay());
 	}
 
 	protected void scanPrevious() {
 
 		// Move highlight out of previous button
 		if (mHUDAnimators.get(mScanIndex).isRunning()) {
 			mHUDAnimators.get(mScanIndex).cancel();
 		}
 		mHUDPad.get(mScanIndex).setHighlighted(false);
 		AnimatorSet hud_animator = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.hud_alpha_animator);
 		long duration = TeclaApp.persistence.getScanDelay() * mHUDPad.size();
 		hud_animator.getChildAnimations().get(0).setDuration(Math.round(0.1 * duration));
 		hud_animator.getChildAnimations().get(1).setDuration(Math.round(0.9 * duration));
 		hud_animator.setTarget(mHUDPad.get(mScanIndex));
 		mHUDAnimators.set(mScanIndex, hud_animator);
 		mHUDAnimators.get(mScanIndex).start();
 		// Proceed to highlight previous button
 		if (mScanIndex == 0) {
 			mScanIndex = (byte) (mHUDPad.size()-1);
 		} else {
 			--mScanIndex;
 		}
 		if (mHUDAnimators.get(mScanIndex).isRunning()) {
 			mHUDAnimators.get(mScanIndex).cancel();
 		}
 		mHUDPad.get(mScanIndex).setHighlighted(true);
 		mHUDPad.get(mScanIndex).setAlpha(1.0f);
 	}
 
 	protected void scanNext() {
 
 		// Move highlight out of previous button
 		if (mHUDAnimators.get(mScanIndex).isRunning()) {
 			mHUDAnimators.get(mScanIndex).cancel();
 		}
 		mHUDPad.get(mScanIndex).setHighlighted(false);
 		AnimatorSet hud_animator = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.hud_alpha_animator);
 		long duration = TeclaApp.persistence.getScanDelay() * mHUDPad.size();
 		hud_animator.getChildAnimations().get(0).setDuration(Math.round(0.1 * duration));
 		hud_animator.getChildAnimations().get(1).setDuration(Math.round(0.9 * duration));
 		hud_animator.setTarget(mHUDPad.get(mScanIndex));
 		mHUDAnimators.set(mScanIndex, hud_animator);
 		mHUDAnimators.get(mScanIndex).start();
 		// Proceed to highlight next button
 		if (mScanIndex == mHUDPad.size()-1) {
 			mScanIndex = 0;
 		} else {
 			mScanIndex++;
 		}
 		if (mHUDAnimators.get(mScanIndex).isRunning()) {
 			mHUDAnimators.get(mScanIndex).cancel();
 		}
 		mHUDPad.get(mScanIndex).setHighlighted(true);
 		mHUDPad.get(mScanIndex).setAlpha(1.0f);
 	}
 
 	private void findAllButtons() {
 
 		mHUDPad = new ArrayList<TeclaHUDButtonView>();
 		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_top));
 		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_topright));
 		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_right));
 		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_bottomright));
 		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_bottom));
 		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_bottomleft));
 		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_left));
 		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_topleft));
 
 		mHUDPadHighlightVal = new boolean[mHUDPad.size()];
 		mHUDPadAlphaVal = new float[mHUDPad.size()];
 	}
 
 	private void fixHUDLayout () {
 		Display display = mWindowManager.getDefaultDisplay();
 		Point display_size = new Point();
 		display.getSize(display_size);
 		int display_width = display_size.x;
 		int display_height = display_size.y;
 
 		int size_reference = 0;
 		if (display_width <= display_height) { // Portrait (use width)
 			size_reference = Math.round(display_width * 0.24f);
 		} else { // Landscape (use height)
 			size_reference = Math.round(display_height * 0.24f);
 		}
 
 		ArrayList<ViewGroup.LayoutParams> hudParams = new ArrayList<ViewGroup.LayoutParams>();
 		for(TeclaHUDButtonView button : mHUDPad) {
 			hudParams.add(button.getLayoutParams());
 		}
 
 		hudParams.get(HUD_BTN_TOPLEFT).width = size_reference;
 		hudParams.get(HUD_BTN_TOPLEFT).height = size_reference;
 		hudParams.get(HUD_BTN_TOPRIGHT).width = size_reference;
 		hudParams.get(HUD_BTN_TOPRIGHT).height = size_reference;
 		hudParams.get(HUD_BTN_BOTTOMLEFT).width = size_reference;
 		hudParams.get(HUD_BTN_BOTTOMLEFT).height = size_reference;
 		hudParams.get(HUD_BTN_BOTTOMRIGHT).width = size_reference;
 		hudParams.get(HUD_BTN_BOTTOMRIGHT).height = size_reference;
 		hudParams.get(HUD_BTN_LEFT).width = Math.round(side_width_proportion * size_reference);
 		hudParams.get(HUD_BTN_LEFT).height = display_height - (2 * size_reference);
 		hudParams.get(HUD_BTN_TOP).width = display_width - (2 * size_reference);
 		hudParams.get(HUD_BTN_TOP).height = Math.round(side_width_proportion * size_reference);
 		hudParams.get(HUD_BTN_RIGHT).width = Math.round(side_width_proportion * size_reference);
 		hudParams.get(HUD_BTN_RIGHT).height = display_height - (2 * size_reference);
 		hudParams.get(HUD_BTN_BOTTOM).width = display_width - (2 * size_reference);
 		hudParams.get(HUD_BTN_BOTTOM).height = Math.round(side_width_proportion * size_reference);
 
 		for (int i = 0; i < mHUDPad.size(); i++) {
 			mHUDPad.get(i).setLayoutParams(hudParams.get(i));
 		}
 
 		int stroke_width = Math.round(stroke_width_proportion * size_reference);
 
 		mHUDPad.get(HUD_BTN_TOPLEFT).setProperties(TeclaHUDButtonView.POSITION_TOPLEFT, stroke_width, false);
 		mHUDPad.get(HUD_BTN_TOPRIGHT).setProperties(TeclaHUDButtonView.POSITION_TOPRIGHT, stroke_width, false);
 		mHUDPad.get(HUD_BTN_BOTTOMLEFT).setProperties(TeclaHUDButtonView.POSITION_BOTTOMLEFT, stroke_width, false);
 		mHUDPad.get(HUD_BTN_BOTTOMRIGHT).setProperties(TeclaHUDButtonView.POSITION_BOTTOMRIGHT, stroke_width, false);
 		mHUDPad.get(HUD_BTN_LEFT).setProperties(TeclaHUDButtonView.POSITION_LEFT, stroke_width, false);
 		mHUDPad.get(HUD_BTN_TOP).setProperties(TeclaHUDButtonView.POSITION_TOP, stroke_width, true);
 		mHUDPad.get(HUD_BTN_RIGHT).setProperties(TeclaHUDButtonView.POSITION_RIGHT, stroke_width, false);
 		mHUDPad.get(HUD_BTN_BOTTOM).setProperties(TeclaHUDButtonView.POSITION_BOTTOM, stroke_width, false);
 
 		mHUDPad.get(HUD_BTN_TOPLEFT).setDrawables(mResources.getDrawable(R.drawable.hud_icon_home_normal), mResources.getDrawable(R.drawable.hud_icon_home_focused));
 		mHUDPad.get(HUD_BTN_TOPRIGHT).setDrawables(mResources.getDrawable(R.drawable.hud_icon_select_normal), mResources.getDrawable(R.drawable.hud_icon_select_focused));
 		mHUDPad.get(HUD_BTN_BOTTOMLEFT).setDrawables(mResources.getDrawable(R.drawable.hud_icon_undo_normal), mResources.getDrawable(R.drawable.hud_icon_undo_focused));
 		mHUDPad.get(HUD_BTN_BOTTOMRIGHT).setDrawables(mResources.getDrawable(R.drawable.hud_icon_page2_normal), mResources.getDrawable(R.drawable.hud_icon_page2_focused));
 		mHUDPad.get(HUD_BTN_LEFT).setDrawables(mResources.getDrawable(R.drawable.hud_icon_left_normal), mResources.getDrawable(R.drawable.hud_icon_left_focused));
 		mHUDPad.get(HUD_BTN_TOP).setDrawables(mResources.getDrawable(R.drawable.hud_icon_up_normal), mResources.getDrawable(R.drawable.hud_icon_up_focused));
 		mHUDPad.get(HUD_BTN_RIGHT).setDrawables(mResources.getDrawable(R.drawable.hud_icon_right_normal), mResources.getDrawable(R.drawable.hud_icon_right_focused));
 		mHUDPad.get(HUD_BTN_BOTTOM).setDrawables(mResources.getDrawable(R.drawable.hud_icon_down_normal), mResources.getDrawable(R.drawable.hud_icon_down_focused));
 	}
 
 	class AutoScanHandler extends Handler {
 		public AutoScanHandler() {
 
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			TeclaHUDOverlay.this.scanNext();
 			sleep(TeclaApp.persistence.getScanDelay());
 		}
 
 		public void sleep(long delayMillis) {
 			removeMessages(0);
 			sendMessageDelayed(obtainMessage(0), delayMillis);
 		}
 	}
 	AutoScanHandler mAutoScanHandler = new AutoScanHandler();
 
 }
