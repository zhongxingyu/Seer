 package com.example.portfolioapp;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.VideoView;
 
 /**
  * A fragment representing a single project in the project-view. The fragment shows images/videos & text
  * about the given project.
  * 
  * https://developer.android.com/training/animation/screen-slide.html
  *
  */
 public class ScreenSlideProjectFragment extends Fragment {
 	
     /**
      * The argument key for the page number this fragment represents.
      */
     public static final String ARG_PAGE = "page";
 
     /**
      * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
      */
     private int mPageNumber;
     private String mName;
     private String mDescription;
     private ArrayList<String> mMedia;
     
     protected static Project project;
 
     /**
      * Factory method for this fragment class. Constructs a new fragment for the given page number.
      */
     public static ScreenSlideProjectFragment create(int pageNumber, Project p) {
     	
     	ScreenSlideProjectFragment fragment = new ScreenSlideProjectFragment();
         Bundle args = new Bundle();
         args.putInt(ARG_PAGE, pageNumber);
         args.putString("name", p.name);
         args.putString("description", p.description);
         args.putStringArrayList("media", p.pictures);
         fragment.setArguments(args);
         return fragment;
     }
 
     public ScreenSlideProjectFragment() {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mPageNumber = getArguments().getInt(ARG_PAGE);
         mName = getArguments().getString("name");
         mDescription = getArguments().getString("description");
         mMedia = getArguments().getStringArrayList("media");
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         // Inflate the layout containing a title and body text.
         ViewGroup rootView = (ViewGroup) inflater
                 .inflate(R.layout.fragment_screen_slide_project, container, false);
         
         ((TextView) rootView.findViewById(R.id.ProjectName)).setText(mName);
         ((TextView) rootView.findViewById(R.id.ProjectDescription)).setText(mDescription);
 
         // Set the media and description.
         // Media, Instantiate a ViewPager and a PagerAdapter:
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
         ((ViewPager) rootView.findViewById(R.id.mediaPager)).setAdapter(mPagerAdapter);
         return rootView;
     }
 
     /**
      * Returns the page number represented by this fragment object.
      */
     public int getPageNumber() {
         return mPageNumber;
     }
     
     /**
      * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
      * sequence.
      */
    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter 
     {	
         public ScreenSlidePagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             return (Fragment) ScreenSlideProjectMediaFragment.create( position, mMedia.get(position) );
         }
 
         @Override
         public int getCount() {
             return mMedia.size();
         }
     }
 }
