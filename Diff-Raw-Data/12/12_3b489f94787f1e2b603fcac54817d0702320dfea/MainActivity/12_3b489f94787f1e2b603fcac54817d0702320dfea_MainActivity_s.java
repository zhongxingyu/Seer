 package se.chalmers.pd.playlistmanager;
 
 import java.util.ArrayList;
 
 import android.app.SearchManager;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.widget.SearchView;
 
 public class MainActivity extends FragmentActivity implements ApplicationController.Callback, QueryTextListener.Callback {
 
 	private SectionsPagerAdapter sectionsPagerAdapter;
 	private ViewPager viewPager;
 	private ApplicationController controller;
 	private LoadingDialogFragment loadingDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
 		viewPager = (ViewPager) findViewById(R.id.pager);
 		viewPager.setAdapter(sectionsPagerAdapter);
 		controller = new ApplicationController(this, this);
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.search_menu, menu);
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
 		searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
 		searchView.setOnQueryTextListener(new QueryTextListener(this));
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public void onSearchBegin() {
 		loadingDialog = DialogFactory.buildLoadingDialog();
 		loadingDialog.show(getFragmentManager(), "loadingDialog");
 	}
 
 	@Override
 	public void onSearchResult(ArrayList<Track> tracks) {
 		loadingDialog.dismiss();
 		viewPager.setCurrentItem(SectionsPagerAdapter.FIRST_PAGE, true);
 		sectionsPagerAdapter.updateResults(tracks);
 	}
 	
 	public void onTrackSelected(Track track) {
 		switch (viewPager.getCurrentItem()) {
 		case SectionsPagerAdapter.FIRST_PAGE:
 			controller.addTrack(track);
 			break;
 		case SectionsPagerAdapter.SECOND_PAGE:
			
 			break;
 		}
 	}
 	
 	@Override
 	public void onUpdatePlaylist(Track track) {
 		sectionsPagerAdapter.updatePlaylist(track);
 	}
 
 	public void onPlayerAction(Action action) {
 		controller.performAction(action);
 	}
 }
