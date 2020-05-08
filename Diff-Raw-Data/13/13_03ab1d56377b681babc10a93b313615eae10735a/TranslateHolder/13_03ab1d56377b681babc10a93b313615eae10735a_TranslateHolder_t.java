 package com.johndaniel.glosar;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuInflater;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 
 
 public class TranslateHolder extends Fragment {
 	boolean showingBack;
 	View thisView;
 	public static final String FRAGMENT_WORD = "com.johndaniel.glosar.FRAGMENT_WORD";
 	public static final String FRAGMENT_TRANSLATION = "com.johndaniel.glosar.FRAGMENT_TRANLATION";
 	
 	private String word, translation;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		String[] raw = getArguments().getString(TranslateActivity.TRANSLATION).split("=");
		if (raw[0] != null){
			word = raw[0];
		} else {
			word = "";
		}
		
		if (raw.length != 1){
			translation = raw[1];
		} else {
			translation = "";
		}
 		
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		
 		thisView = inflater.inflate(R.layout.fragment_translate_holder, container, false);
 		
 		
 		//Nsta steg: Skapa TranslateActivity igen och lt den innehlla en viewpager
 		if (savedInstanceState == null) {
 			
 			Fragment wordFragment = new TranslateFragment1();
 			Bundle wordBundle = new Bundle();
 			wordBundle.putString(FRAGMENT_WORD, word);
 			wordFragment.setArguments(wordBundle);
 			
 			
 			Fragment translateFragment = new TranslateFragment2();
 			Bundle translateBundle = new Bundle();
 			translateBundle.putString(FRAGMENT_TRANSLATION, translation);
 			translateFragment.setArguments(translateBundle);
 			
 			
             getChildFragmentManager()
                     .beginTransaction()
                     .add(R.id.main_activity_card_face, wordFragment)
                     .commit();
             
             getChildFragmentManager()
             	.beginTransaction()
             	.add(R.id.main_activity_card_back, translateFragment)
             	.commit();
         }
 		
 		RelativeLayout cont = (RelativeLayout) thisView.findViewById(R.id.main_activity_root);
 		cont.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				//Snurra
 				flip();
 				
 			}
 		});
 		
 		return thisView;
 		
 	}
 
 	private void flip(){
 		RelativeLayout rootLayout = (RelativeLayout) thisView.findViewById(R.id.main_activity_root);
 		RelativeLayout cardFace = (RelativeLayout) thisView.findViewById(R.id.main_activity_card_face);
 		RelativeLayout cardBack = (RelativeLayout) thisView.findViewById(R.id.main_activity_card_back);
 
 	    FlipAnimation flipAnimation = new FlipAnimation(cardFace, cardBack);
 
 	    if (cardFace.getVisibility() == View.GONE)
 	    {
 	        flipAnimation.reverse();
 	    }
 	    rootLayout.startAnimation(flipAnimation);
 
 	}
 	
 }
