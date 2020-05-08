 package com.jcertif.android.fragments;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.jcertif.android.MainActivity;
 import com.jcertif.android.R;
 import com.jcertif.android.model.Participant;
 import com.squareup.picasso.Picasso;
 
 public class ProfileFragment extends RESTResponderFragment {
 
 	private Participant user;
 	TextView tv_name, tv_entreprise, tv_bio;
 	ImageView avatar;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		setRetainInstance(true);
 		View rootView = inflater.inflate(R.layout.fragment_profile, container,
 				false);
 		getActivity().setTitle(R.string.profile);
 
 		tv_name = (TextView) rootView.findViewById(R.id.tv_fullname);
 		tv_entreprise = (TextView) rootView.findViewById(R.id.tv_entreprise);
 		tv_bio = (TextView) rootView.findViewById(R.id.tv_bio);
 		avatar = (ImageView) rootView.findViewById(R.id.imgavatar);
 		return rootView;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onActivityCreated(savedInstanceState);
 
 		getSherlockActivity().getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 		user = MainActivity.user;
 		tv_name.setText(user.getFirstname()+" "+user.getLastname());
 		tv_entreprise.setText(user.getCompany());
 		tv_bio.setText(user.getBiography().toString());
		Picasso.with(getActivity().getApplicationContext()).load(user.getPhoto()).resize(200, 200).placeholder(R.drawable.ic_action_profile).into(avatar);
 	}
 
 	@Override
 	public void onRESTResult(int code, String result) {
 
 	}
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 
 	}
 
 }
