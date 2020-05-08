 package com.example.portfolioapp;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.squareup.picasso.Picasso;
 
 
 /**
  * A fragment representing a single project in the project-view. The fragment shows images/videos & text
  * about the given project.
  * 
  * https://developer.android.com/training/animation/screen-slide.html
  *
  */
 public class ScreenSlideProjectMediaFragment extends Fragment {
     /**
      * The argument key for the page number this fragment represents.
      */
     public static final String ARG_PAGE = "page";
 
     /**
      * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
      */
     private int mPageNumber;
     
     private String mMedia;
     
     /**
      * Factory method for this fragment class. Constructs a new fragment for the given page number.
      */
     public static ScreenSlideProjectMediaFragment create(int pageNumber, String media) {
         ScreenSlideProjectMediaFragment fragment = new ScreenSlideProjectMediaFragment();
         Bundle args = new Bundle();        
         
     	args.putString("media", media);
         args.putInt(ARG_PAGE, pageNumber);
         
         fragment.setArguments(args);
         return fragment;
     }
 
     public ScreenSlideProjectMediaFragment() {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mPageNumber = getArguments().getInt(ARG_PAGE);
         mMedia = getArguments().getString("media");
     }
 
     @Override    
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         // Inflate the layout containing a title and body text.
         ViewGroup rootView = (ViewGroup) inflater
                 .inflate(R.layout.fragment_screen_slide_media, container, false);
         
         if(mMedia.length()>0)
         {
         	Log.v("MediaFragment", mMedia);
         	((TextView) rootView.findViewById(R.id.text)).setVisibility(View.GONE);
         	int imageResource = getResources().getIdentifier(mMedia, null, rootView.getContext().getPackageName());
        	Picasso.with(rootView.getContext()).load(imageResource).into(((ImageView) rootView.findViewById(R.id.picture)));
         }
         else
         {
         	((ImageView) rootView.findViewById(R.id.picture)).setVisibility(View.GONE);
         }
 
         return rootView;
     }
 
     /**
      * Returns the page number represented by this fragment object.
      */
     public int getPageNumber() {
         return mPageNumber;
     }
 }
