 package com.bytopia.oboobs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentManager;
 import android.widget.ArrayAdapter;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.bytopia.oboobs.adapters.ImageProviderAdapter;
 import com.bytopia.oboobs.fragments.BoobsListFragment;
 import com.bytopia.oboobs.fragments.MainStateFragment;
 import com.bytopia.oboobs.fragments.dialogs.NetworkErrorDialog;
 import com.bytopia.oboobs.providers.FavoritesProvider;
 import com.bytopia.oboobs.providers.IdBoobsProvider;
 import com.bytopia.oboobs.providers.ImageProvider;
 import com.bytopia.oboobs.providers.InterestBoobsProvider;
 import com.bytopia.oboobs.providers.NoiseBoobsProvider;
 import com.bytopia.oboobs.providers.RankBoobsProvider;
 import com.bytopia.oboobs.utils.Tuple;
 
 public class OboobsMaintActivity extends SherlockFragmentActivity implements
 		ActionBar.OnNavigationListener, BoobsListFragmentHolder {
 
 	private static final String STATE_TAG = "state";
 	private ActionBar bar;
 	private OboobsApp app;
 	private FragmentManager fragmentManager;
 
 	private List<Tuple<Integer, ImageProvider>> providers;
 
 	private BoobsListFragment boobsListFragment;
 
 	private MainStateFragment stateFragment;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		app = (OboobsApp) getApplication();
 
 		setTheme(R.style.Theme_Sherlock); // Used for theme switching in samples
 
 		setContentView(R.layout.main);
 
 		fragmentManager = getSupportFragmentManager();
 
 		boobsListFragment = (BoobsListFragment) fragmentManager
 				.findFragmentByTag("BoobsList");
 
 		stateFragment = (MainStateFragment) fragmentManager
 				.findFragmentByTag(STATE_TAG);
 		if (stateFragment == null) {
 			stateFragment = new MainStateFragment();
 			fragmentManager.beginTransaction().add(stateFragment, STATE_TAG)
 					.commit();
 			stateFragment.providers = initProviders();
 		}
 		providers = stateFragment.providers;
 
 		bar = getSupportActionBar();
 		Context barContext = bar.getThemedContext();
 
 		ArrayAdapter<String> list = new ImageProviderAdapter(barContext,
 				R.layout.sherlock_spinner_item, providers);
 
 		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		bar.setListNavigationCallbacks(list, this);
 		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
 
 		if (stateFragment.provider != null) {
 
 			for (int i = 0; i < providers.size(); i++) {
 				if (stateFragment.provider == providers.get(i).b) {
 					bar.setSelectedNavigationItem(i);
 					break;
 				}
 			}
 		}
		
		bar.setSelectedNavigationItem(stateFragment.selectedProviderPosition);
 
 	}
 
 	private List<Tuple<Integer, ImageProvider>> initProviders() {
 		List<Tuple<Integer, ImageProvider>> lProviders = new ArrayList<Tuple<Integer, ImageProvider>>();
 		lProviders.add(new Tuple<Integer, ImageProvider>(R.string.by_rank,
 				new RankBoobsProvider()));
 		lProviders.add(new Tuple<Integer, ImageProvider>(R.string.by_interest,
 				new InterestBoobsProvider()));
 		lProviders.add(new Tuple<Integer, ImageProvider>(R.string.by_date,
 				new IdBoobsProvider()));
 		lProviders.add(new Tuple<Integer, ImageProvider>(R.string.random,
 				new NoiseBoobsProvider()));
 		lProviders.add(new Tuple<Integer, ImageProvider>(R.string.favorites,
 				new FavoritesProvider()));
 		return lProviders;
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 
		stateFragment.selectedProviderPosition = itemPosition;
 		final ImageProvider provider = providers.get(itemPosition).b;
 
 		if (stateFragment.provider != provider) {
 			stateFragment.provider = provider;
 			provider.setOrder(stateFragment.desk ? ImageProvider.DESK
 					: ImageProvider.ASC);
 			boobsListFragment.getBoobsFrom(provider);
 
 			invalidateOptionsMenu();
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Used to put dark icons on light action bar
 
 		MenuInflater menuInflater = getSupportMenuInflater();
 		menuInflater.inflate(R.menu.main_items, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 
 		MenuItem order = menu.findItem(R.id.order);
 
 		boolean isDark = app.isDark;
 
 		if (stateFragment != null && stateFragment.provider != null) {
 
 			if (isDark) {
 				order.setIcon(stateFragment.desk ? R.drawable.desc_dark
 						: R.drawable.asc_dark);
 			} else {
 				order.setIcon(stateFragment.desk ? R.drawable.desc_light
 						: R.drawable.ask_light);
 			}
 
 			if (stateFragment.provider.hasOrder()) {
 				order.setVisible(true);
 			} else {
 				order.setVisible(false);
 			}
 
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int id = item.getItemId();
 
 		switch (id) {
 		case R.id.order:
 			stateFragment.desk = !stateFragment.desk;
 
 			if (stateFragment != null && stateFragment.provider != null) {
 				stateFragment.provider
 						.setOrder(stateFragment.desk ? ImageProvider.DESK
 								: ImageProvider.ASC);
 				boobsListFragment.getBoobsFrom(stateFragment.provider);
 			}
 
 			invalidateOptionsMenu();
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void handleErrorWhileLoadingProvider() {
 		createSwitchToFavoritesDialog();
 
 	}
 
 	private void createSwitchToFavoritesDialog() {
 		DialogFragment newFragment = NetworkErrorDialog.newInstance(providers
 				.size() - 1);
 		newFragment.show(getSupportFragmentManager(), "dialog");
 	}
 }
