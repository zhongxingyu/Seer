 package com.combustiblelemons.thrillingtales;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.combustiblelemons.thrillingtales.DescriptionFragment.onItemReRandomized;
 import com.combustiblelemons.thrillingtales.SavedFragment.onScriptItemSelected;
 import com.combustiblelemons.thrillingtales.ScriptFragment.onItemReReandomized;
 import com.combustiblelemons.thrillingtales.ScriptFragment.onItemSelected;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ViewFlipper;
 
 import static com.combustiblelemons.thrillingtales.Values.TAG;
 import static com.combustiblelemons.thrillingtales.Values.DescriptionFlags.TAG_FLAG;
 import static com.combustiblelemons.thrillingtales.Values.DescriptionFlags.VALUE_FLAG;
 import static com.combustiblelemons.thrillingtales.Values.FragmentFalgs.*;
 
 public class ThrillingTales extends SherlockFragmentActivity implements onItemReRandomized, onItemReReandomized,
 		onItemSelected, onScriptItemSelected {
 	protected static Context context;
 	/**
 	 * @param vf_main
 	 *            ViewFlipper that holds views: About, Scenario and Description
 	 */
 	protected static ViewFlipper vf_main;
 	protected FragmentManager fmanager;
 	protected ScriptFragment scriptFragment;
 	protected DescriptionFragment descriptionFragment;
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Log.d(TAG, "On Start");
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		context = getApplicationContext();
 		vf_main = (ViewFlipper) LayoutInflater.from(context).inflate(R.layout.main, null);
 		setContentView(vf_main);
 		fmanager = getSupportFragmentManager();
 		scriptFragment = new ScriptFragment();
 		Bundle args = new Bundle();
 		scriptFragment.setArguments(args);
 		fmanager.beginTransaction().add(R.id.ll_main, scriptFragment, SCRIPT_VIEW_FLAG).commit();
 		Log.d(TAG, "Now pulp");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflate = getSupportMenuInflater();
 		inflate.inflate(R.menu.menu_main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.oi_generate:
			View _script_view = (ViewGroup) scriptFragment.getView();
 			PulpMachine.pulpAgain((ViewGroup) _script_view);
 			return true;
 		case R.id.oi_save_script:
 			SaveFragment savingFragment = (SaveFragment) fmanager.findFragmentByTag(SAVING_UI_FLAG);
 			if (savingFragment == null) {
 				savingFragment = new SaveFragment();
 				fmanager.beginTransaction()
 						.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_right,
 								R.anim.slide_out_left).addToBackStack(null)
 						.replace(android.R.id.content, savingFragment, SAVING_UI_FLAG).commit();
 			} else {
 				fmanager.beginTransaction().show(savingFragment);
 			}
 			return true;
 		case R.id.oi_show_saved:
 			SavedFragment savedFragment = (SavedFragment) fmanager.findFragmentByTag(SAVED_FLAG);
 			if (savedFragment == null) {
 				savedFragment = new SavedFragment();
 				fmanager.beginTransaction()
 						.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_right,
 								R.anim.slide_out_left).addToBackStack(null)
 						.replace(android.R.id.content, savedFragment, SAVED_FLAG).commit();
 			} else {
 				fmanager.beginTransaction().show(savedFragment);
 			}
 			break;
 		// TODO Rewrite settings
 		case R.id.oi_settings:
 			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
 			startActivity(intent);
 			return true;
 		case R.id.oi_about:
 			/**
 			 * TODO Rewrite
 			 */
 			AboutFragment about = (AboutFragment) fmanager.findFragmentByTag(ABOUT_FLAG);
 			if (about == null) {
 				about = new AboutFragment();
 				fmanager.beginTransaction()
 						.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_right,
 								R.anim.slide_out_left).addToBackStack(null)
 						.add(android.R.id.content, about, ABOUT_FLAG).commit();
 			} else {
 				fmanager.beginTransaction()
 						.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_right,
 								R.anim.slide_out_left).show(about);
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (DescriptionFragment.description_edit != null && DescriptionFragment.description_edit.hasFocus()) {
 				ViewUtils.hideEditControls();
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onDescriptionItemReRandomized(String value, String tag) {
 		scriptFragment.setTitle(value, tag);
 	}
 
 	@Override
 	public void onScriptItemReRandomized(String value, String tag) {
 		if (descriptionFragment != null) {
 			descriptionFragment.changeDescription(value, tag);
 		}
 	}
 
 	@Override
 	public void itemTouched(String value, String tag) {
 		descriptionFragment = new DescriptionFragment();
 		Bundle args = new Bundle();
 		args.putString(VALUE_FLAG, value);
 		args.putString(TAG_FLAG, tag);
 		descriptionFragment.setArguments(args);
 		fmanager.beginTransaction()
 				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_right,
 						R.anim.slide_out_left).addToBackStack(null)
 				.replace(android.R.id.content, descriptionFragment, DESCRIPTION_VIEW_FLAG).commit();
 	}
 
 	/**
 	 * Called from list of saved scripts
 	 */
 	@Override
 	public void scriptItemSelected(String forDate) {
		ViewGroup parent = (ViewGroup) vf_main.findViewById(R.id.ll_main);
		scriptFragment.loadScript(parent, forDate);
 	}
 }
