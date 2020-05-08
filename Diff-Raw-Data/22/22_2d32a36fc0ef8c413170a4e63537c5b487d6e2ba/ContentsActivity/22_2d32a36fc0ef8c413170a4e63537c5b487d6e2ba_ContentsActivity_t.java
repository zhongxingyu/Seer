 package edu.dartmouth.mhb;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ListView;
 
 
 public class ContentsActivity extends FragmentActivity implements ActionBar.TabListener{
     // When requested, this adapter returns a DemoObjectFragment,
     // representing an object in the collection.
     ContentsPagerAdapter mContentsPagerAdapter;
     ViewPager mViewPager;
     List<Fragment> fList;
     static ArrayList<Hymn> hymns;
     
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		//TODO Change theme to allow for visible tabs
        setTheme(R.style.ContentTheme);
         setContentView(R.layout.activity_contents);
     
       final ActionBar actionBar = getActionBar();
       actionBar.setDisplayHomeAsUpEnabled(true);        
         
       
     // Specify that tabs should be displayed in the action bar.
       	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
       
         hymns = MainActivity.hymns;
 //        
         //Create tab fragment lists
         // ViewPager and its adapters use support library
         // fragments, so use getSupportFragmentManager.
         fList = getContentFragments();
         mContentsPagerAdapter = new ContentsPagerAdapter(getSupportFragmentManager(),
                 fList);
 
 
 
         // Set up the ViewPager, attaching the adapter and setting up a listener for when the
         // user swipes between sections.
         mViewPager = (ViewPager) findViewById(R.id.tab_pager);
         mViewPager.setAdapter(mContentsPagerAdapter);
         mViewPager.setOnPageChangeListener(
             new ViewPager.SimpleOnPageChangeListener() {
                 @Override
                 public void onPageSelected(int position) {
                     getActionBar().setSelectedNavigationItem(position);
                 }
         });  
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mContentsPagerAdapter.getCount(); i++) {
             actionBar.addTab(actionBar.newTab()
                             .setText(mContentsPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
 
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#55000000")));
    
         
     }
     
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) 
     {
         // Handle item selection
         switch (item.getItemId()) 
         {
             case android.R.id.home:
                 onBackPressed();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     
     
     
     public ArrayList<Fragment> getContentFragments(){
     	ArrayList<Fragment> frags = new ArrayList<Fragment>();
         Fragment f = new ContentsTitlesFragment();
         frags.add(f);
         
         f = new ContentsNumbersFragment();
         frags.add(f);
         
         f = new ContentsAuthorsFragment();
         frags.add(f);
         
         return frags;
     }
 
 	
     // Since this is an object collection, use a FragmentStatePagerAdapter,
     // and NOT a FragmentPagerAdapter.
     private class ContentsPagerAdapter extends FragmentStatePagerAdapter {
         private List<Fragment> fragments;
     	
     	public ContentsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
             super(fm);
             this.fragments = fragments;
         }
 
         @Override
         public Fragment getItem(int position) {     	
             return this.fragments.get(position);
         }
 
         @Override
         public int getCount() {
             return this.fragments.size();
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
         	String str_val="";
         	switch(position){
         	case 0:
         		str_val = "Titles";
         		break;
         	case 1:
         		str_val = "MHB Nos.";
         		break;
         	case 2:
         		str_val = "Authors";
         		break;
         	}
         	
             return str_val;
         }
     }
   
     
     public static class ContentsTitlesFragment extends Fragment{
     	
     	@Override
     	public View onCreateView(LayoutInflater inflater,
     			ViewGroup container, Bundle savedInstanceState) {
     		View rootView = inflater.inflate(
     				R.layout.fragment_contents_list,container,false);
     		
     		//Setup layout here.
     		final ListView listview = (ListView) rootView.findViewById(R.id.contents_list);
     	    ArrayList<String> titles = new ArrayList<String>();
     	    for (int i=0; i<hymns.size();i++){
     	    	titles.add(hymns.get(i).getTitle());
     	    }
     	    
 	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
 	            android.R.layout.simple_list_item_1, titles);
 	        listview.setAdapter(adapter);    		
     		
     		return rootView;
     	}
     }
     
     public static class ContentsNumbersFragment extends Fragment{
     	@Override
     	public View onCreateView(LayoutInflater inflater,
     			ViewGroup container, Bundle savedInstanceState) {
     		View rootView = inflater.inflate(
     				R.layout.fragment_contents_grid,container,false);
     		
     		//Setup layout here.
     		final GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
     	    ArrayList<String> titles = new ArrayList<String>();
     	    for (int i=0; i<hymns.size();i++){
     	    	titles.add(String.valueOf(hymns.get(i).getId()));
     	    }
     	    
 	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
 	            android.R.layout.simple_list_item_1, titles);
 	        gridview.setAdapter(adapter);    		
     		
     		return rootView;
     	}    	
     }    
    
     public static class ContentsAuthorsFragment extends Fragment{
     	@Override
     	public View onCreateView(LayoutInflater inflater,
     			ViewGroup container, Bundle savedInstanceState) {
     		View rootView = inflater.inflate(
     				R.layout.fragment_contents_list,container,false);
     		
     		//Setup layout here.
     		final ListView listview = (ListView) rootView.findViewById(R.id.contents_list);
     	    ArrayList<String> authors = new ArrayList<String>();
     	    for (int i=0; i<hymns.size();i++){
     	    	authors.add(hymns.get(i).getAuthor());
     	    }
     	    
 	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
 	            android.R.layout.simple_list_item_1, authors);
 	        listview.setAdapter(adapter);    		
     		
     		return rootView;
     	}    	
     }
 
 	@Override
 	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
 		// TODO Auto-generated method stub
 		mViewPager.setCurrentItem(arg0.getPosition());
 	}
 
 
 	@Override
 	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
 		// TODO Auto-generated method stub
 		
 	}
     
 
 }
