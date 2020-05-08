 package com.combustiblelemons.thrillingtales;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class AboutFragment extends SherlockFragment implements OnTouchListener, OnClickListener {
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		getSherlockActivity().getSupportActionBar().hide();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View _r = inflater.inflate(R.layout.fragment_about, null);
 		_r.setOnTouchListener(this);
 		_r.findViewById(R.id.iv_about_cover).setOnClickListener(this);
 		setHasOptionsMenu(true);
 		return _r;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		menu.clear();
 		inflater.inflate(R.menu.about_menu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.oi_send:
 			Resources r = getResources();
 			Intent send = new Intent(Intent.ACTION_SEND);
 			send.putExtra(Intent.EXTRA_EMAIL, new String[] { r.getString(R.string.email_recepient_address) });
 			send.putExtra(Intent.EXTRA_SUBJECT, r.getString(R.string.email_subject));
 			send.putExtra(Intent.EXTRA_TEXT, r.getString(R.string.email_body_prefix));
 			send.setType("message/rfc822");
 			try {
 				startActivity(send);
 			} catch (ActivityNotFoundException e) {
 				Toast.makeText(getActivity().getApplicationContext(), r.getString(R.string.noemailapp),
 						Toast.LENGTH_LONG).show();
 			}
 			return true;
 		case R.id.oi_settings:
 			Intent intent = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
		case R.id.iv_about_cover:
			ActionBar bar = getSherlockActivity().getSupportActionBar();
 			if (bar.isShowing()) {
 				bar.hide();
 			} else {
 				bar.show();
 			}
 			break;
 		default:
 			break;
 		}
 	}
 }
