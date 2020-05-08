 package com.example.homepage;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.TextView;
 
 import com.example.firstapp.R;
 import com.example.firstapp.UserInfo;
 
 public class HomePageFragment extends Fragment {
 	private UserInfo user;
 
 	String[] qs = { "What's the definition of a Group?",
 			"What's the meaning of life?", "Is 1 + 1 actually 2?" };
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View homePageView = inflater.inflate(R.layout.homepage_structure,
 				container, false);
 		user = (UserInfo) getActivity().getIntent().getExtras().get("User");
 		// select a picture
 		homePageView.findViewById(R.id.personal_profile).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						Intent intent = new Intent(getActivity(),
 								PersonalProfile.class);
 						intent.putExtra("User", user);
 						startActivity(intent);
 					}
 				});
 
 		System.out.println(user.getUsername() + " " + user.getPoints());
 		TextView username = (TextView) homePageView.findViewById(R.id.username);
 		username.setText("Username:   " + user.getUsername());
 		
 		TextView points = (TextView) homePageView.findViewById(R.id.points);
 		points.setText("Points:       " + String.valueOf(user.getPoints()));
 	
 		return homePageView;
 	}
 	
 }
