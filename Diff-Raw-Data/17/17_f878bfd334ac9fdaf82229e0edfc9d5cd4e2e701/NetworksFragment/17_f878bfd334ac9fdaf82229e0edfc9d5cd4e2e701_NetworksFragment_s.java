 package carnero.me.fragment;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import carnero.me.R;
 import carnero.me.Utils;
 import carnero.me.data._NetworksList;
 import carnero.me.model.Network;
 import carnero.me.view.TransparentScrollView;
 import com.google.analytics.tracking.android.GAServiceManager;
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Tracker;
 
 public class NetworksFragment extends Fragment {
 
 	private LayoutInflater mInflater;
 	private TransparentScrollView mView;
 	private LinearLayout mLayoutOn;
 	private LinearLayout mLayoutOff;
 	private Tracker mTracker;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
 		mInflater = inflater;
 
 		mView = (TransparentScrollView) inflater.inflate(R.layout.networks, container, false);
 
 		mLayoutOn = (LinearLayout) mView.findViewById(R.id.networks_layout);
 		mLayoutOff = (LinearLayout) mView.findViewById(R.id.networks_missing_layout);
 
 		return mView;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedState) {
 		super.onActivityCreated(savedState);
 
 		final GoogleAnalytics analytics = GoogleAnalytics.getInstance(getActivity());
 		mTracker = analytics.getTracker(getString(R.string.ga_trackingId));
 
 		int networksOn = 0; // user have installed official client...
 		int networksOff = 0; // ...user has not
 
 		ViewGroup view;
 		ViewGroup container;
 		ImageView icon;
 		TextView title;
 		TextView description;
 		View separator = null;
 
 		for (Network network : _NetworksList.ENTRIES) {
 			if (Utils.isPackageInstalled(getActivity(), network.packageName)) {
 				view = (ViewGroup) mInflater.inflate(R.layout.item_network_on, mLayoutOn, false);
 				container = (ViewGroup) view.findViewById(R.id.network_container);
 				icon = (ImageView) view.findViewById(R.id.network_icon);
 				title = (TextView) view.findViewById(R.id.network_title);
 				description = (TextView) view.findViewById(R.id.network_description);
 				separator = view.findViewById(R.id.separator);
 
				container.setOnClickListener(new EntryAction(network.tapAction.getIntent(getActivity())));
 				icon.setImageResource(network.iconOn);
 				title.setText(network.title);
 				description.setText(network.description);
 
 				mLayoutOn.addView(view);
 				networksOn++;
 			} else {
 				view = (ViewGroup) mInflater.inflate(R.layout.item_network_off, mLayoutOff, false);
				container = (ViewGroup) view.findViewById(R.id.network_container);
 				icon = (ImageView) view.findViewById(R.id.network_icon);
 				title = (TextView) view.findViewById(R.id.network_title);
 				description = (TextView) view.findViewById(R.id.network_description);
 
				container.setOnClickListener(new EntryAction(network.tapAction.getIntent(getActivity())));
 				icon.setImageResource(network.iconOff);
 				title.setText(network.title);
 				description.setText(network.description);
 
 				mLayoutOff.addView(view);
 				networksOff++;
 			}
 
 			mView.registerView(view);
 		}
 
 		if (separator != null) {
 			separator.setVisibility(View.GONE); // hide separator in last ON network
 		}
 
 		if (networksOn == 0) {
 			mLayoutOn.setVisibility(View.GONE);
 		}
 		if (networksOff == 0) {
 			mLayoutOff.setVisibility(View.GONE);
 		}
 	}
 
 	// classes
 	private class EntryAction implements View.OnClickListener {
 
 		private Intent mIntent;
 
 		public EntryAction(Intent intent) {
 			mIntent = intent;
 		}
 
 		@Override
 		public void onClick(View v) {
 			getActivity().startActivity(mIntent);
 
 			if (mTracker != null) {
 				mTracker.sendEvent("network", "tap", mIntent.getData().toString(), 0l);
 
 				GAServiceManager.getInstance().dispatch();
 			}
 		}
 	}
 }
