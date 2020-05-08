 /*
  *	Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  * 	Evan DeGraff
  *
  * 	Permission is hereby granted, free of charge, to any person obtaining a copy of
  * 	this software and associated documentation files (the "Software"), to deal in
  * 	the Software without restriction, including without limitation the rights to
  * 	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * 	the Software, and to permit persons to whom the Software is furnished to do so,
  * 	subject to the following conditions:
  *
  * 	The above copyright notice and this permission notice shall be included in all
  * 	copies or substantial portions of the Software.
  *
  * 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * 	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * 	FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * 	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * 	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * 	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.view;
 
 import ca.cmput301f13t03.adventure_datetime.R;
 import ca.cmput301f13t03.adventure_datetime.model.Story;
 import ca.cmput301f13t03.adventure_datetime.model.StoryFragment;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.ICurrentFragmentListener;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.ICurrentStoryListener;
 import ca.cmput301f13t03.adventure_datetime.serviceLocator.Locator;
 import ca.cmput301f13t03.adventure_datetime.view.treeView.TreeView;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 /**
  * 
  * View containing three fragments/tabs:
  *  - AuthorEdit_Preview
  *  - AuthorEdit_Overview
  *  - AuthorEdit_Edit
  * 
  * TODO: Load data from the model
  * TODO: Send changes to the controller
  * 
  * @author James Finlay
  *
  */
 public class AuthorEdit extends FragmentActivity 
 implements 	ICurrentFragmentListener, 
 ICurrentStoryListener,
 IFragmentSelected
 {
 
 	public static final int OVERVIEW_INDEX = 0;
 	public static final int EDIT_INDEX = 1;
 	public static final int PREVIEW_INDEX = 2;
 	// make sure to update this if we add a tab!
 	public static final int INDEX_COUNT = 3;
 
 	private static final String TAG = "AuthorEdit";
 
 	private ViewPager _viewPager;
 	private ViewPagerAdapter _adapter;
 	private Story _story;
 	private StoryFragment _fragment;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.viewpager);
 
 		/* Set up View Pager */
 		_adapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
 		_viewPager = (ViewPager) findViewById(R.id.pager);
 		_viewPager.setAdapter(_adapter);
 
 		/* Set up Tabs */
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
 			@Override
 			public void onTabReselected(Tab tab, FragmentTransaction ft) {}
 
 			@Override
 			public void onTabSelected(Tab tab, FragmentTransaction ft) 
 			{
				int index = tab.getPosition();
				if(index != OVERVIEW_INDEX)
				{
					_adapter.CancelActions();
				}
				
 				invalidateOptionsMenu();
				
				_viewPager.setCurrentItem(index);
 			}
 			@Override
 			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
 		};
 
 		actionBar.addTab(actionBar.newTab()
 				.setText("Overview")
 				.setTabListener(tabListener));
 		actionBar.addTab(actionBar.newTab()
 				.setText("Edit")
 				.setTabListener(tabListener));
 		actionBar.addTab(actionBar.newTab()
 				.setText("Preview")
 				.setTabListener(tabListener));
 
 		/* Change tabs when View Pager swiped */
 		_viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) 
 			{
 				getActionBar().setSelectedNavigationItem(position);
 				invalidateOptionsMenu();
				
				if(_viewPager.getCurrentItem() != OVERVIEW_INDEX)
				{
					_adapter.CancelActions();
				}
 			}
 		});
 
 		// Select 'Overview' at start
 		getActionBar().setSelectedNavigationItem(OVERVIEW_INDEX);
 
 	}
 	@Override
 	public void onResume() {
 		Locator.getPresenter().Subscribe((ICurrentFragmentListener)this);
 		Locator.getPresenter().Subscribe((ICurrentStoryListener)this);
 		super.onResume();
 	}
 	@Override
 	public void onPause() {
 		Locator.getPresenter().Unsubscribe((ICurrentFragmentListener)this);
 		Locator.getPresenter().Unsubscribe((ICurrentStoryListener)this);
 		super.onPause();
 	}
 	@Override
 	public void OnCurrentFragmentChange(StoryFragment newFragment) {
 		_fragment = newFragment;
 		_adapter.setFragment(newFragment);
 	}
 	@Override
 	public void OnCurrentStoryChange(Story newStory) {
 		_adapter.setStory(newStory);
 		_story = newStory;
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		if(_viewPager.getCurrentItem() == OVERVIEW_INDEX)
 		{
 			getMenuInflater().inflate(R.menu.authoroverview, menu);
 		}
 		else
 		{
 			getMenuInflater().inflate(R.menu.authoredit, menu);
 		}
 
 		return true;		
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.action_back:
 			finish();
 			break;
 		case R.id.action_save:
 			_adapter.saveFragment();
 			Toast.makeText(getApplicationContext(), "Saved Fragment!", Toast.LENGTH_SHORT).show();
 			break;
 		case R.id.action_discard:
 			DoDeleteFragment();
 			break;
 		case R.id.action_addFragment:
 			StoryFragment newFrag = Locator.getAuthorController().CreateFragment();
 			Locator.getAuthorController().selectFragment(newFrag.getFragmentID());
 			_viewPager.setCurrentItem(EDIT_INDEX);
 			break;
 		default:
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void DoDeleteFragment()
 	{
 		if(_viewPager.getCurrentItem() == OVERVIEW_INDEX)
 		{
 			// TODO::JT gotta handle selection
 		}
 		else
 		{
 			final int TXT_LIMIT = 10;
 			
 			String fragText = _fragment.getStoryText();
 			
 			if(fragText.length() > TXT_LIMIT)
 			{
 				fragText = fragText.substring(0, TXT_LIMIT) + "...";
 			}
 			
 			/* Ensure user is not retarded and actually wants to do this */
 			new AlertDialog.Builder(this)
 			.setTitle("Delete Story Fragment")
 			.setMessage("This will delete \"" + fragText + "\"\nYou cannot undo.")
 			.setCancelable(true)
 			.setPositiveButton("Delete", new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (_story.getHeadFragmentId().equals(_fragment.getFragmentID())) {
 						Toast.makeText(getApplicationContext(), 
 								"Cannot delete Head Fragment", Toast.LENGTH_LONG).show();
 					} else {
 						Locator.getAuthorController().deleteFragment(_fragment.getFragmentID());
 						Locator.getAuthorController().selectFragment(_story.getHeadFragmentId());
 					}
 				}
 			})
 			.setNegativeButton("Cancel", new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 				}
 			})
 			.create().show();
 		}
 	}
 
 	public void OnFragmentSelected(StoryFragment selectedFragment) 
 	{
 		getActionBar().setSelectedNavigationItem(EDIT_INDEX);
 		Locator.getAuthorController().selectFragment(selectedFragment.getFragmentID());
 	}
 
 	// fuck this is ugly
 	public interface OnCreatedCallback
 	{
 		public void OnCreated(TreeView newTreeView);
 	}
 
 	public class ViewPagerAdapter extends FragmentPagerAdapter implements OnCreatedCallback
 	{
 		private AuthorEdit_Edit _edit;
 		private AuthorEdit_Overview _overview;
 		private AuthorEdit_Preview _preview;
 
 		public ViewPagerAdapter(FragmentManager fm, IFragmentSelected callback) {
 			super(fm);
 
 			_overview = new AuthorEdit_Overview();
 			_overview.SetFragmentSelectionCallback(callback);
 			_overview.SetOnCreatedCallback(this);
 
 			_edit = new AuthorEdit_Edit();
 			_edit.SetActionBar(getActionBar());
 
 			_preview = new AuthorEdit_Preview();
 		}
 
 		@Override
 		public Fragment getItem(int i) {
 			switch (i) {
 			case OVERVIEW_INDEX:
 				return _overview;
 			case EDIT_INDEX:
 				return _edit;
 			case PREVIEW_INDEX:
 				return _preview;
 			default:
 				Log.e(TAG, "invalid item #");
 				return null;
 			}
 		}
 		public void setStory(Story st) {
 			_edit.setStory(st);
 			_preview.setStory(st);
 		}
 		public void setFragment(StoryFragment sf) {
 			_edit.setFragment(sf);
 			_preview.setFragment(sf);
 		}
 		public void saveFragment() {
 			_edit.saveFragment();
 			_preview.saveFragment();
 		}
 		
 		public void CancelActions()
 		{
 			_edit.CancelPendingActions();
 		}
 
 		@Override
 		public int getCount() {
 			return INDEX_COUNT;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case EDIT_INDEX: return "Edit";
 			case OVERVIEW_INDEX: return "Overview";
 			case PREVIEW_INDEX: return "Preview";
 			default: return "It be a Pirate!";
 			}
 		}
 
 		public void OnCreated(TreeView newTreeView) 
 		{
 			_edit.SetTreeView(newTreeView);
 		}
 	}
 }
