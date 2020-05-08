 package com.android.tecla.addon;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.view.accessibility.AccessibilityNodeInfo;
 import ca.idrc.tecla.highlighter.TeclaHighlighter;
 
 public class TeclaVisualOverlay {
 
 	private Context mContext;
 	private TeclaHighlighter mHighlighter;
 	private TeclaHUDOverlay mHUDController;
 	
 	public TeclaVisualOverlay(Context context) {
 		mContext = context;
 		mHighlighter = new TeclaHighlighter(context);
 		mHUDController = new TeclaHUDOverlay(context);
 	}
 	
 	public void show() {
 		mHighlighter.show();
 		mHUDController.show();
 		//FIXME: Abstract into registerConfigReceiver() method on mTeclaHUDController
 		mContext.registerReceiver(mHUDController.mConfigChangeReceiver, 
 			new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
 	}
 	
 	public void hide() {
 		//FIXME: Abstract into unregisterConfigReceiver() method on mTeclaHUDController
 		mContext.unregisterReceiver(mHUDController.mConfigChangeReceiver);
 		mHighlighter.hide();
 		mHUDController.hide();
 	}
 	
 	public boolean isVisible() {
 		return mHighlighter.isVisible() && mHUDController.isVisible();
 	}
 	
 	public boolean isPreview() {
 		return mHUDController.isPreview();
 	}
 	
 	public void scanNextHUDButton() {
 		mHUDController.scanNext();
 	}
 
 	public void showPreviewHUD() {
 		mHUDController.setPreviewHUD(true);
 		show();
 	}
 
 	public void hidePreviewHUD() {
 		mHUDController.setPreviewHUD(false);
 		hide();
 	}
 
 	public void highlightNode(AccessibilityNodeInfo selectedNode) {
 		mHighlighter.highlightNode(selectedNode);	
 	}
 	
 	public void clearHighlight() {
 		mHighlighter.clearHighlight();
 	}
 	
 	public void scanNext() {
 		mHUDController.scanNext();
 	}
 	
 	public void scanPrevious() {
 		mHUDController.scanPrevious();
 	}

	public void checkAndUpdateHUDHeight() {
		if(mHUDController.isVisible())
			mHUDController.updateHUDHeight();
	}
 }
