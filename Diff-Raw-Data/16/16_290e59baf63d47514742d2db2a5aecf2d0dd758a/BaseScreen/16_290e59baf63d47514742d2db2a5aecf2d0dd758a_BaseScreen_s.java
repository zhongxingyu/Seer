 package net.fiive.kotoba.test.screen.base;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 
 public class BaseScreen<F extends Fragment, AT extends BaseScreenAutomator> {
 
 	private F fragment;
 	private AT automator;
 
 
 	public BaseScreen(F fragment, AT automator) {
 		this.fragment = fragment;
 		this.automator = automator;
 	}
 
 	public void onActivityCreated(Bundle bundle) {
 		this.fragment.onActivityCreated(bundle);
 	}
 
 	public void onSaveInstanceState(Bundle bundle) {
 		fragment.onSaveInstanceState(bundle);
 	}
 
 	public void onResume() {
 		fragment.onResume();
 	}
 
 	public void selectMenuItem(int itemId) {
 		automator.selectMenuItem(itemId);
 	}
 
 	protected F getFragment() {
 		return fragment;
 	}
 
 	protected AT getAutomator() {
 		return automator;
 	}
 }
