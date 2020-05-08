 package carnero.me.fragment;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import carnero.me.R;
 import carnero.me.data._NetworksList;
 import carnero.me.model.Network;
 
 public class NetworksFragment extends Fragment {
 
 	private Context mContext;
 	private LayoutInflater mInflater;
 	private PackageManager mPM;
 	private LinearLayout mLayoutOn;
 	private LinearLayout mLayoutOff;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
 		mInflater = inflater;
 
 		final View view = inflater.inflate(R.layout.networks, container, false);
 
 		mLayoutOn = (LinearLayout) view.findViewById(R.id.networks_layout);
 		mLayoutOff = (LinearLayout) view.findViewById(R.id.networks_missing_layout);
 
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedState) {
 		super.onActivityCreated(savedState);
 
 		mContext = getActivity().getBaseContext();
 
 		int networksOn = 0; // user have installed official client...
 		int networksOff = 0; // ...user has not
 
 		ViewGroup view;
 		ImageView icon;
 		TextView title;
 		TextView description;
 
 		for (Network network : _NetworksList.ENTRIES) {
			view = (ViewGroup) mInflater.inflate(R.layout.item_network, null);
			icon = (ImageView) view.findViewById(R.id.network_icon);
			title = (TextView) view.findViewById(R.id.network_title);
			description = (TextView) view.findViewById(R.id.network_description);

			view.setOnClickListener(new EntryAction(network.tapAction));
			title.setText(network.title);
			description.setText(network.description);

 			if (isPackageInstalled(network.packageName)) {
 				icon.setImageResource(network.iconOn);
 
 				mLayoutOn.addView(view);
 				networksOn++;
 			} else {
				title.setTextAppearance(mContext, R.style.Text_Network_Title_Off);
				description.setTextAppearance(mContext, R.style.Text_Network_Description_Off);
 
 				icon.setImageResource(network.iconOff);
 
 				mLayoutOff.addView(view);
 				networksOff++;
 			}
 		}
 
 		if (networksOn == 0) {
 			mLayoutOn.setVisibility(View.GONE);
 		}
 		if (networksOff == 0) {
 			mLayoutOff.setVisibility(View.GONE);
 		}
 	}
 
 	private boolean isPackageInstalled(String clazz) {
 		if (TextUtils.isEmpty(clazz)) {
 			return false;
 		}
 
 		if (mPM == null) {
 			mPM = getActivity().getPackageManager();
 		}
 
 		try {
 			mPM.getPackageInfo(clazz, 0);
 
 			return true;
 		} catch (NameNotFoundException e) {
 			return false;
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
 		}
 	}
 }
