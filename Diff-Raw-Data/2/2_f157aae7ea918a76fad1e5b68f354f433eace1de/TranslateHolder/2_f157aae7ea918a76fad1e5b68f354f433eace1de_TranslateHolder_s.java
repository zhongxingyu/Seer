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
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		thisView = inflater.inflate(R.layout.activity_translate, container, false);
 		
 		
 		//Nsta steg: Skapa TranslateActivity igen och lt den innehlla en viewpager
 		if (savedInstanceState == null) {
             getChildFragmentManager()
                     .beginTransaction()
                     .add(R.id.main_activity_card_face, new TranslateFragment1())
                     .commit();
             
             getChildFragmentManager().beginTransaction()
             	.add(R.id.main_activity_card_back, new TranslateFragment2())
             	.commit();
                     
             showingBack = false;
         }
 		
 		RelativeLayout cont = (RelativeLayout) thisView.findViewById(R.id.main_activity_root);
		container.setOnClickListener(new View.OnClickListener() {
 			
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
