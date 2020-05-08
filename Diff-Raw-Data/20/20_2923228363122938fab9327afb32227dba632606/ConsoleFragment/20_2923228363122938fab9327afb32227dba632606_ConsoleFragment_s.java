 package com.phunware.alerts.sample;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.phunware.alerts.sample.ConsoleOutput.ConsoleLogger;
 
 public class ConsoleFragment extends Fragment {
 	
 	public static final String TAG = "ConsoleFragment";
 	private TextView mTextConsole;
 	private ConsoleLogger mActivityLink;
 	private ScrollView mScrollView;
 	
 	/**
 	 * Use this when passing console text in a bundle
 	 */
 	public static final String ARG_CONSOLE_TEXT = "console_text";
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_console, container, false);
 		
 		mTextConsole = (TextView)view.findViewById(R.id.console_output);
 		
 		//scroll to bottom (most recent logs)
 		mScrollView = (ScrollView)view.findViewById(R.id.scroll_lyt);
 		scrollDown();
 		
 		setHasOptionsMenu(true);
 		
 		
 		return view;
 	}
 	
 	@Override
 	public void onAttach(Activity activity) {
 		if(!(activity instanceof ConsoleLogger))
 			throw new RuntimeException("ConsoleFragment's activity must implement ConsoleLogger");
 		mActivityLink = (ConsoleLogger)activity;
 		super.onAttach(activity);
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		Log.v(TAG, "onStart");
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		Log.v(TAG, "onActivityCreated");
 		if(savedInstanceState != null)
 		{
 			String text = savedInstanceState.getString("text");
 			if(text != null)
 				mTextConsole.setText(text);
 			else
 				Log.i(TAG, "text from saved state is null");
 		}
 		super.onActivityCreated(savedInstanceState);
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.main, menu);
 		menu.getItem(Utils.MENU_INDEX_REFRESH).setVisible(false);
 		menu.getItem(Utils.MENU_INDEX_UPDATE).setVisible(false);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int id = item.getItemId();
 		
 		if(id == R.id.menu_clear){
 			mTextConsole.setText("");
 			mActivityLink.clearConsole();
 			return true;
 		}
 		
 		else if(id == R.id.menu_email){
 			Utils.email(getActivity(), mTextConsole.getText().toString());
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		if(mTextConsole != null)
 			outState.putString("text", mTextConsole.getText().toString().trim());
 		super.onSaveInstanceState(outState);
 	}
 	
 	/**
 	 * Set the text of the console directly.
 	 * This allows for dynamic updates
 	 * @param text to set to the console
 	 */
 	public void setConsoleText(String text)
 	{
 		if(mTextConsole != null)
 			mTextConsole.setText(text);
 		//scroll to bottom (most recent logs)
 		scrollDown();
 	}
 	
 	private void scrollDown()
 	{
 		if(mScrollView != null)
 		{
 			mScrollView.post(new Runnable() {
 				@Override
 				public void run() {
 					mScrollView.fullScroll(View.FOCUS_DOWN);
 				}
 			});
 		}
 	}
 }
