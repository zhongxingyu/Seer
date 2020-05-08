 package com.android.tecla.keyboard;
 
 import android.content.Context;
 import android.view.View;
 import android.view.WindowManager;
 import ca.idrc.tecla.framework.SimpleOverlay;
 import ca.idrc.tecla.framework.TeclaStatic;
 
 public class SingleSwitchTouchInterface extends SimpleOverlay {
 
 	/**
 	 * Tag used for logging in the whole framework
 	 */
 	public static final String CLASS_TAG = "SingleSwitchTouchInterface";
 	private static SingleSwitchTouchInterface sInstance;
 
 	public SingleSwitchTouchInterface(Context context) {
 		super(context);
 
 		final WindowManager.LayoutParams params = getParams();
 		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
 		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
 		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 		setParams(params);
 
 		View rView = getRootView();
 		rView.setOnLongClickListener(mOverlayLongClickListener);
 		rView.setOnClickListener(mOverlayClickListener);
 	}
 
 	@Override
 	protected void onShow() {
 		sInstance = this;
 	}
 
 	@Override
 	protected void onHide() {
 		sInstance = null;
 	}
 
 	private View.OnClickListener mOverlayClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if(IMEAdapter.isShowingKeyboard()) IMEAdapter.selectScanHighlighted();
 			else TeclaHUDOverlay.selectScanHighlighted();
 				
 		}
 	};	
 
 	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {
 
 		@Override
 		public boolean onLongClick(View v) {
 			TeclaStatic.logV(CLASS_TAG, "Long clicked.  ");
 			TeclaAccessibilityService.getInstance().shutdownInfrastructure();
 			return true;
 		}
 	};
 
 }
