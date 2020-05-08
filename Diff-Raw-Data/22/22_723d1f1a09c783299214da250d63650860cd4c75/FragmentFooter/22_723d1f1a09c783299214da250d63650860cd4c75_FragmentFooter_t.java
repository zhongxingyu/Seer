 package com.csc591.view;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 
 public class FragmentFooter extends Fragment implements OnClickListener {
 	
 	private static final String TAG = null;
 	private boolean amusementFlag,landmarksFlag,commerceFlag,openspaceFlag;
 	
 	ArrayList<Integer> newFlags = new ArrayList<Integer>();
 	public interface FragmentFooterInterface{
 		public void onCategoryButtonClickHandler(ArrayList<Integer> newFlags);
 	}
 	
 	public FragmentFooterInterface homeActivityInterface;
 	
 	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
 	{
 		View view = inflater.inflate(R.layout.fragment_footer,container,false);
 		
 		amusementFlag=landmarksFlag=commerceFlag=openspaceFlag=false;
 		
 		Button amusementButton = (Button) view.findViewById(R.id.amusement_button);
 		amusementButton.setOnClickListener(this);
 		
 		Button landmarksButton = (Button) view.findViewById(R.id.landmarks_button);
 		landmarksButton.setOnClickListener(this);
 		
 		Button commerceButton = (Button) view.findViewById(R.id.commerce_button);
 		commerceButton.setOnClickListener(this);
 		
 		Button openspaceButton = (Button) view.findViewById(R.id.openspace_button);
 		openspaceButton.setOnClickListener(this);
 		
 		return view;
 		
 	}
 	
 	public void onAttach(Activity activity)
 	{
 		super.onAttach(activity);
 		
 		if(activity instanceof FragmentFooterInterface)
 		{
 			homeActivityInterface = (FragmentFooterInterface)activity;
 		}
 	}
 	
 	public void onDetach()
 	{
 		super.onDetach();
 		homeActivityInterface = null;
 	}
 	
 	public void onClick(View v)
 	 {
 	    if(v.getId()==R.id.amusement_button) 
 	        {
 	    	  if(amusementFlag==false)
 	    	  	{
 	    		  Log.e(TAG,"Clicked on Amusement");
 	    		  amusementFlag=true;
 	    	  	}
 	    	  else
 	    	    {
 	    		  Log.e(TAG,"Amusement de-selected");
 	    		  amusementFlag=false;
 	    	    }
 	        }
 	    
 	    if(v.getId()==R.id.landmarks_button) 
         {
 	    	if(landmarksFlag==false)
     	  	{
     		  Log.e(TAG,"Clicked on Landmarks");
     		  landmarksFlag=true;
     	  	}
     	  else
     	    {
     		  Log.e(TAG,"Landmarks de-selected");
     		  landmarksFlag=false;
     	    }     
         }
 	    
 	    if(v.getId()==R.id.commerce_button) 
         {
 	    	if(commerceFlag==false)
     	  	{
     		  Log.e(TAG,"Clicked on Commerce");
     		  commerceFlag=true;
     	  	}
     	  else
     	    {
     		  Log.e(TAG,"Commerce de-selected");
     		  commerceFlag=false;
     	    }     
         }
 	    
 	    if(v.getId()==R.id.openspace_button) 
         {
 	    	if(openspaceFlag==false)
     	  	{
     		  Log.e(TAG,"Clicked on Open Space");
     		  openspaceFlag=true;
     	  	}
     	  else
     	    {
     		  Log.e(TAG,"Open space de-selected");
     		  openspaceFlag=false;
     	    }     
         }
 	    // amusementFlag - 0, landmark - 1, commerce - 2, open space - 3
 	    newFlags.clear();
 	    if(amusementFlag)
 	    	newFlags.add(0);

 	    if(landmarksFlag)
 	    	newFlags.add(1);

 	    if(commerceFlag)
 	    	newFlags.add(2);

 	    if(openspaceFlag)
 	    	newFlags.add(3);
	    
	    if(!amusementFlag && !landmarksFlag && !commerceFlag && !openspaceFlag)
	    	for(int i=0;i<=3;i++)
	    		newFlags.add(i);
 	    homeActivityInterface.onCategoryButtonClickHandler(newFlags);
 	 }		
 }
