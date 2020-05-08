 package com.boh.flatmate;
 
 import com.boh.flatmate.R;
 import com.boh.flatmate.FlatMate.FlatDataExchanger;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class FlatFragment extends Fragment {
 
 	private ViewGroup c;
 	FlatListFragment flatList;
 
 	private int debtsOpen = 0;
 	private int settingsOpen = 0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v1 = inflater.inflate(R.layout.flat_mate_page, container, false);
 		c = container;
 		return v1;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		android.support.v4.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
 			flatList = new FlatListFragment();
 			//ft.setCustomAnimations(R.anim.slide_up_in, R.anim.slide_out_down);
 			ft.replace(R.id.list, flatList,"flat_fragment");
 			ft.commit();
 			if(settingsOpen == 1){
 				Fragment settings = new FlatSettingsFragment();
 				//ft.setCustomAnimations(R.anim.slide_up_in, R.anim.slide_out_down);
				ft.replace(R.id.list, settings,"Set_fragment");
				ft.addToBackStack(null);
				ft.commit();
 				((ImageView) c.findViewById(R.id.settingsButton)).setImageResource(R.drawable.flatmates_tab);
 				c.findViewById(R.id.map_button).setVisibility(View.GONE);
 			}
 		final Button button = (Button) c.findViewById(R.id.map_button);
 		button.setText("View Debts");
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if(debtsOpen == 0){
 					button.setText("View Contact Buttons");
 					debtsOpen = 1;
 					flatList.setDebtsVisible(1);
 				}else{
 					debtsOpen = 0;
 					button.setText("View Debts");
 					flatList.setDebtsVisible(0);
 				}
 			}
 		});
 		
 		getFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener(){
 			public void onBackStackChanged(){
 				if(settingsOpen == 1 && getFragmentManager().getBackStackEntryCount() == 0){
 					settingsOpen = 0;
 					button.setVisibility(View.VISIBLE);
 				}
 			}
 		});
 
 		final ImageButton settings = (ImageButton) c.findViewById(R.id.settingsButton);
 		settings.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if(settingsOpen == 0){
 					settingsOpen = 1;
 					settings.setImageResource(R.drawable.flatmates_tab);
 					android.support.v4.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
 					Fragment set = new FlatSettingsFragment();
 					ft.replace(R.id.list, set, "Set_fragment");
 					ft.addToBackStack(null);
 					ft.commit();
 					button.setVisibility(View.GONE);
 				}else{
 					settingsOpen = 0;
 					settings.setImageResource(R.drawable.settings);
 					getFragmentManager().popBackStack();
 					button.setVisibility(View.VISIBLE);
 				}
 			}
 		});
 
		TextView flatName = (TextView) c.findViewById(R.id.flatName);
 		flatName.setText(FlatDataExchanger.flatData.getNickname());
 
 		TextView atFlat = (TextView) c.findViewById(R.id.atFlatText);
 		atFlat.setText(FlatDataExchanger.flatData.getNoAtFlat()+" flat mates at home");
 
 		ImageButton messageButton = (ImageButton)c.findViewById(R.id.messageButton);
 		messageButton.setOnClickListener(FlatDataExchanger.flatData.messageListener);
 	}
 }
