 package com.founderapp;
 
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.ShareActionProvider;
 
 import com.founderapp.domain.DomainHelper;
 import com.founderapp.domain.EditorValue;
 import com.founderapp.domain.Pitch;
 import com.viewpagerindicator.PageIndicator;
 
 
 public class PitchEditorActivity extends FragmentActivity implements OnPageChangeListener, OnNavigationListener {
 		
 	private static final String TAG = "PitchEditorActivity";
 	protected static Pitch pitch;
 	
 	ViewPager pager;
 	PitchEditorPagerAdapter pagerAdapter;
 	PageIndicator indicator;
 	
 	@Override
 	public void onCreate(Bundle bundleInstance) {
 		super.onCreate(bundleInstance);
 		setContentView(R.layout.pitch_editor_activity);
 		
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		ArrayAdapter<CharSequence> list = ArrayAdapter
 			    .createFromResource(this, R.array.sections, android.R.layout.simple_dropdown_item_1line);
 		list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		getActionBar().setListNavigationCallbacks(list, this);
 
 		
 		pitch = (Pitch) getIntent().getSerializableExtra("pitch"); 
 		
 		pagerAdapter = new PitchEditorPagerAdapter(this, pitch);
 		
 		pager = (ViewPager)findViewById(R.id.view_pager);
 		pager.setAdapter(pagerAdapter);
 		pager.setOnPageChangeListener(this);
 		
 		String title = "New Pitch";
 		if (pitch != null) title = pitch.getCompanyName();
 		setTitle( title );
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.note_menus, menu);
		
 		MenuItem menuItem = menu.findItem(R.id.share);
 		ShareActionProvider mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
 	    mShareActionProvider.setShareIntent(getShareIntentMethod());
 
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			break;
 
 		case R.id.archive:
 			delete();
 			break;
 			
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 	@Override
 	public void onPageSelected(int index) {
 		Log.d(TAG, " # onPageSelected: " + index);
 		getActionBar().setSelectedNavigationItem(index);
 	}
 
 	@Override
 	public void onPageScrollStateChanged(int arg0) {
 		//	Log.d(TAG, " XX onPageScrollStateChanged: " + arg0);
 		/* do nothing much here */
 	}
 
 	@Override
 	public void onPageScrolled(int arg0, float arg1, int arg2) {
 		//Log.d(TAG, " ** onPageScrolled: " + arg0 + ", float=" + arg1 + ", arg2=" + arg2);
 		/* do nothing */
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		Log.d(TAG, "  >>> item position: " + itemPosition);
 		pager.setCurrentItem(itemPosition, true);
 		return false;
 	}
 
 	/* Remove/archive the pitch */
 	private void delete() {
 		Log.d(TAG, " * Deleting " + pitch);
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Archive Pitch Content");
 		builder.setMessage("Really remove this pitch?");
 		builder.setPositiveButton("OK", new OnClickListener() {
             public void onClick(DialogInterface dialog, int arg1) {
             	pitch.setClosed(true);
         		DomainHelper.savePitch(PitchEditorActivity.this, pitch);
         		finish();
                 dialog.dismiss();
         }});
 		builder.setNegativeButton("Cancel", new OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
 			}
 		});
 		
 		builder.show();
 		return;
 	}
 	
 	private Intent getShareIntentMethod() {
		Intent intent=new Intent(android.content.Intent.ACTION_SEND);
 		intent.setType("text/plain");
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 		
 		StringBuffer sb = new StringBuffer();
		sb.append("#Founder Notepad - " + pitch.getCompanyName() + "#\n\n");
		sb.append(pitch.toShareTextContent());
 		sb.append("\n\n#Details#\n");
 		
 		List<EditorValue> values = DomainHelper.loadEditorValues(this, pitch.getId());
 		
 		for (EditorValue val : values) {
 			sb.append(val.toShareTextContent());
 		}
 		
		intent.putExtra(Intent.EXTRA_SUBJECT, pitch.getCompanyName());
 		intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
 		
 		return intent;
 	}
 
 }
