 package net.udrunk;
 
 import net.udrunk.adapters.PlaceAdapater;
 import net.udrunk.domain.Place;
 import net.udrunk.model.Model;
 import android.content.Intent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.Bean;
 import com.googlecode.androidannotations.annotations.Click;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.googlecode.androidannotations.annotations.ItemClick;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.ViewById;
 
 @EFragment(R.layout.places)
 public class PlacesFragment extends SherlockFragment {
 
 	@ViewById
 	ListView listView;
 
 	@Bean
 	public Model model;
 
 	@ViewById
 	public ProgressBar empty;
 
 	@ViewById(R.id.bnt_getplaces)
 	public Button getPlacesButton;
 
 	@AfterViews
 	public void afterViews() {
 		setHasOptionsMenu(true);
 		updatePlaces();
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.places_menu, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.map_item_menu:
 			Intent intent = new Intent(getActivity(), LocationActivity_.class);
 			startActivity(intent);
 			return true;
 		}
 		return (super.onOptionsItemSelected(item));
 	}
 
 	@UiThread
 	void updatePlaces() {
 		if (model != null) {
 			PlaceAdapater adapter = new PlaceAdapater(getActivity(),
 					R.layout.list_place_item, model.getPlaces());
 			listView.setAdapter(adapter);
 
 			updateProgress();
 		}
 	}
 
 	@ItemClick
 	public void listViewItemClicked(Place place) {
 		if (getActivity() instanceof ShareActivity) {
 			((ShareActivity) getActivity()).showShareFragment(place);
 		} else {
 			Intent intent = new Intent(getActivity(),
 					PlaceDetailsActivity_.class);
 			intent.putExtra("place_extra", place);
 			startActivity(intent);
 		}
 	}
 
 	@Click(R.id.bnt_getplaces)
 	protected void retryClicked() {
		model.getPlaces();
 	}
 
 	public void updateProgress() {
 		if (model != null) {
 			if (model.placesLoading) {
 				getPlacesButton.setVisibility(View.GONE);
 				empty.setVisibility(View.VISIBLE);
 			} else if (model.getPlaces() == null
 					|| model.getPlaces().size() == 0) {
 				getPlacesButton.setVisibility(View.VISIBLE);
 				empty.setVisibility(View.GONE);
 			} else {
 				getPlacesButton.setVisibility(View.GONE);
 				empty.setVisibility(View.GONE);
 			}
 		}
 	}
 }
