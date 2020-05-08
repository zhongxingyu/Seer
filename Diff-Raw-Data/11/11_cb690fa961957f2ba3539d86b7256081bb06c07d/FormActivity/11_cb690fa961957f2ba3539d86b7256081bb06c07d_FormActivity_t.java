 package com.externc.coexist.ui;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.externc.coexist.R;
 import com.externc.coexist.api.API;
 import com.externc.coexist.api.Form;
 import com.externc.coexist.base.BaseActivity;
 
 /**
  * This Activity is contains a view pager with one page for
  * browsing a table, and another page for adding to that table.
  * @author Anthony Naddeo
  *
  */
 public class FormActivity extends BaseActivity{
 
 	private ViewPager pager;
 	private FragmentStatePagerAdapter adapter;
 	
 	private Form form;
 	private SyncCreator creator;
 
 	
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 
 		form = getIntent().getParcelableExtra("form");
 		getSherlock().getActionBar().setTitle(form.getLabel());
 
 		setContentView(R.layout.pager);
 		adapter = new CrudPager(getSupportFragmentManager());
 		pager = (ViewPager) findViewById(R.id.pager);
 		pager.setAdapter(adapter);
//		pager.setCurrentItem(1);
 	}
 	
 	@Override
 	protected void refresh(){}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.activity_form, menu);
 		return true;
 	}
 
 	protected void setCreator(SyncCreator creator) {
 		this.creator = creator;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_create:
 			create();
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 	}
 
 	public void create() {
 		new API().create(this, creator.getSync());
 	}
 	
 
 
 }
