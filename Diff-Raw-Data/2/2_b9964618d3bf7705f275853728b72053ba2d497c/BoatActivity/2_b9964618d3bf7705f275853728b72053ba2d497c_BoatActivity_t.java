 package de.htwg.seapal.aview.gui.activity;
 
 import java.util.UUID;
 
 import roboguice.activity.RoboActivity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.view.View;
 
 import com.google.inject.Inject;
 
 import de.htwg.seapal.R;
 import de.htwg.seapal.aview.gui.fragment.BoatDetailFragment;
 import de.htwg.seapal.aview.gui.fragment.BoatListFragment;
 import de.htwg.seapal.controller.impl.BoatController;
 import de.htwg.seapal.utils.observer.Event;
 import de.htwg.seapal.utils.observer.IObserver;
 
 public class BoatActivity extends RoboActivity implements IObserver,
 		BoatListFragment.ListSelectedCallback {
 
 	@Inject
 	private BoatController controller;
 	private BoatListFragment fragmentList;
 	private BoatDetailFragment fragmentDetail;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.boat);
 
 		controller.addObserver(this);
 
 		if (savedInstanceState == null) {
 			fragmentList = new BoatListFragment();
 			fragmentList.setController(controller);
 			FragmentTransaction transaction = getFragmentManager()
 					.beginTransaction();
 			transaction
 					.add(R.id.frame_list, fragmentList, BoatListFragment.TAG);
 
 			View v = this.findViewById(R.id.linearLayout_xlarge);
 
 			if (v != null) { // tablet -> FragmentDetail
 				fragmentDetail = new BoatDetailFragment();
 				fragmentDetail.setController(controller);
 				transaction.add(R.id.frame_detail, fragmentDetail,
 						BoatDetailFragment.TAG);
 			}
 			transaction.commit();
 
 		}
 
 	}
 
 	@Override
 	public void selected(UUID boat) {
 		// Clickes an ListItem
 
 		View v = this.findViewById(R.id.linearLayout_xlarge);
 
 		if (v != null) { // Tablet scenario
 //			fragmentDetail = (BoatDetailFragment) getFragmentManager()
 //					.findFragmentByTag(BoatDetailFragment.TAG);
 			fragmentDetail.setController(controller);
 			fragmentDetail.refresh(boat);
 		} else {
 			// Smartphone
 			fragmentDetail = new BoatDetailFragment();
 			fragmentDetail.setController(controller);
 			fragmentDetail.setBoat(boat);
 			FragmentTransaction transaction = getFragmentManager()
 					.beginTransaction();
 			transaction.replace(R.id.frame_list, fragmentDetail);
 			transaction.addToBackStack(null);
 			transaction.commit();
 		}
 	}
 
 	@Override
 	public void update(Event event) {
		if (this.findViewById(R.id.boatlistheader) != null) {
 			if (this.findViewById(R.id.linearLayout_xlarge) != null
 					&& controller.getAllBoats().size() < fragmentList
 							.getBoatListSize()) {
 				// tablet -> delete button called -> clear FragmentDetailView
 				FragmentTransaction tr = getFragmentManager()
 						.beginTransaction();
 				fragmentDetail = new BoatDetailFragment();
 				fragmentDetail.setController(controller);
 				tr.replace(R.id.frame_detail, fragmentDetail);
 				tr.commit();
 			}
 			// new Element
 			fragmentList.onConfigurationChanged(null);
 		} else {
 
 			if (controller.getAllBoats().size() != fragmentList
 					.getBoatListSize()) {
 
 				FragmentTransaction transaction = getFragmentManager()
 						.beginTransaction();
 				transaction.remove(fragmentDetail);
 				transaction.remove(fragmentList);
 				transaction.commit();
 
 				FragmentManager manager = getFragmentManager();
 				manager.popBackStack();
 			}
 
 		}
 
 	}
 
 }
