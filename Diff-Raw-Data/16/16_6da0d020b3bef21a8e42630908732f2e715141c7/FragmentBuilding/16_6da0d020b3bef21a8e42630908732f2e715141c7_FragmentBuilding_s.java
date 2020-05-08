 package se.mah.kd330a.project.find.view;
 
 import java.util.Arrays;
 import se.mah.kd330a.project.R;
 import se.mah.kd330a.project.find.data.GetImage;
 import se.mah.kd330a.project.find.data.ImageLoader;
 import se.mah.kd330a.project.find.data.ImageLoader.OnImageLoaderListener;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class FragmentBuilding extends Fragment implements OnImageLoaderListener {
 	public static final String ARG_BUILDING = "building";
 	private String buildingCode;
 	
 	private ImageView imgNav;
 	private ProgressBar prgBar;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		
 		ViewGroup rootView = (ViewGroup) inflater
 				.inflate(R.layout.fragment_screen_find_building, container, false);
 		imgNav = (ImageView) rootView.findViewById(R.id.img_find_navigation);
 		imgNav.setVisibility(View.INVISIBLE);
 		prgBar = (ProgressBar) rootView.findViewById(R.id.pb_find_loading);
 		prgBar.setVisibility(View.VISIBLE);
 		
 		return rootView;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		Bundle args = getArguments();
 		
 		buildingCode = args.getString(ARG_BUILDING);
 		String[] buildings = getResources().getStringArray(R.array.find_building_code_array);
 		final int pos = Arrays.asList(buildings).indexOf(buildingCode);
 		
 		buildings = getResources().getStringArray(R.array.find_building_array);
 		((TextView) getView().findViewById(R.id.text_find_startTitle)).setText(buildings[pos]);
 		
 		Log.i("project", "FragmentBuilding " + buildingCode);
 		
 		new ImageLoader(getActivity(), this).execute("b" + buildingCode + ".jpg");
 		
 		LinearLayout llFloor = (LinearLayout) getView().findViewById(R.id.ll_find_floormap);
 		llFloor.setClickable(true);
 		llFloor.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Fragment fragment = new FragmentFloorMap();
 				Bundle args = new Bundle();
 				args.putString(FragmentBuilding.ARG_BUILDING, buildingCode);
 				fragment.setArguments(args);
 
 				FragmentManager	 fragmentManager = getActivity().getSupportFragmentManager();
 
 				FragmentTransaction fragmentTrans = fragmentManager.beginTransaction();	
 				fragmentTrans.replace(R.id.content_frame, fragment);
 				fragmentTrans.addToBackStack(null);
 				fragmentTrans.commit();
 			}
 		});
 	
 		llFloor = (LinearLayout) getView().findViewById(R.id.ll_find_google);
 		llFloor.setClickable(true);
 		llFloor.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String[] buildingNames = getResources().getStringArray(R.array.find_building_array);
 				String location = buildingNames[pos];
 
 				if(location.equals("Klerken (Kl)"))
 					location = "Jan Waldenstroms gata 25";
				else if(location.equals("University Hospital (Hs)"))
 					location = "Carl Gustafs vag 34";
 
 				//getting the google map
 				Intent i = new Intent(android.content.Intent.ACTION_VIEW,
 						Uri.parse("geo:0,0?q="+location+"+Malmo+Sweden"));
 
 				startActivity(i);
 			}});
 	}
 
 	@Override
 	public void onImageReceived(String fileName) {
 		prgBar.setVisibility(View.INVISIBLE);
 		imgNav.setImageBitmap(GetImage.getImageFromLocalStorage(fileName, getActivity()));
 		imgNav.setVisibility(View.VISIBLE);	
 	}
 }
