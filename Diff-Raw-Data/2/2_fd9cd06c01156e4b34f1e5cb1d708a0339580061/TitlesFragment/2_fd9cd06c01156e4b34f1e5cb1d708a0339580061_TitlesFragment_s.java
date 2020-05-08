 package ru.neverdark.phototools.fragments;
 
 import ru.neverdark.phototools.Constants;
 import ru.neverdark.phototools.DetailsActivity;
 import ru.neverdark.phototools.R;
 import ru.neverdark.phototools.log.Log;
 import android.support.v4.app.Fragment;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockListFragment;
 
 /**
  * Fragment for navigation list
  */
 public class TitlesFragment extends SherlockListFragment {
     private DofFragment mDofFragment;
     private AboutFragment mAboutFragment;
     private EvpairsFragment mEvFragment;
     private boolean mDualPane;
     private int mCurrentCheckPosition = 0;
     
     /* (non-Javadoc)
      * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
      */
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         Log.message("Enter");
         super.onActivityCreated(savedInstanceState);
         final String[] TITLES = getResources().getStringArray(R.array.main_menuTitles);
 
         setListAdapter(new ArrayAdapter<String>(getActivity(),
                 R.layout.menu_item,
                 R.id.menuItem_label_title,
                 TITLES));
         
         View detailsFrame = getActivity().findViewById(R.id.main_detailFragment);
         mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
         
         if (savedInstanceState != null) {
             mCurrentCheckPosition = savedInstanceState.getInt(Constants.CURRENT_CHOICE, 0);
         } 
         
         if (mDualPane) {
             getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
             showDetails(mCurrentCheckPosition);
         }
     }
     
     
     /* (non-Javadoc)
      * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
      */
     @Override
     public void onSaveInstanceState(Bundle outState) {
         Log.message("Enter");
         super.onSaveInstanceState(outState);
         
         outState.putInt(Constants.CURRENT_CHOICE, mCurrentCheckPosition);
     }
     
     /* (non-Javadoc)
      * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
      */
     @Override
     public void onListItemClick(ListView listView, View view, int position, long id) {
         Log.message("Enter");
         showDetails(position);
     }
     
     /**
      * Shows fragment if application runned on the Tablet or activity for other case
      */
     private void showDetails(int index) {
         Log.message("Enter");
 
         if (index == Constants.RATE_CHOICE) {
             gotoMarket();
         } else if (index == Constants.FEEDBACK_CHOICE) {
             sendEmail();
         } else {
             if (mDualPane == true) {
                 getListView().setItemChecked(index, true);
                 showFragment(index);
             } else {
                 showActivity(index);
             }
 
             setCurentCheckPosition(index);
         }
     }
     
     /**
      * Sends email to application author
      */
     private void sendEmail() {
         Intent mailIntent = new Intent(Intent.ACTION_SEND);
         mailIntent.setType("plain/text");
         mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.common_emailAuthor)});
         mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
         startActivity(Intent.createChooser(mailIntent,
                 getString(R.string.common_selectEmailApplication)));
     }
 
 
     /**
      * Opens market detail application page
      */
     private void gotoMarket() {
         Intent marketIntent = new Intent(Intent.ACTION_VIEW);
        marketIntent.setData(Uri.parse("market://details?id=ru.neverdark.photools"));
         startActivity(marketIntent);
     }
 
 
     /**
      * Sets current position for navigation list
      * @param index
      */
     private void setCurentCheckPosition(int index) {
         mCurrentCheckPosition = index;
     }
     
     /**
      * Shows fragment by index
      * @param index fragment index for shown
      */
     private void showFragment(int index) {
         Log.message("Enter");
 
         switch (index) {
         case Constants.DOF_CHOICE:
             replaceFragment(mDofFragment, index);
             break;
         case Constants.EV_CHOICE:
             replaceFragment(mEvFragment, index);
         case Constants.ABOUT_CHOICE:
             replaceFragment(mAboutFragment, index);
             break;
         }
     }
     
     
     /**
      * Replace current fragment to other
      * @param details new fragment object
      * @param index index fragment
      */
     private void replaceFragment(Fragment details, int index) {
         Log.message("Enter");
         boolean isOperationNeed = false;
         
         switch (index) {
         case Constants.DOF_CHOICE:
             try {
                 details = (DofFragment) getFragmentManager().findFragmentById(
                         R.id.main_detailFragment);
             } catch (ClassCastException e) {
                 Log.message("Exception: " + e.getMessage());
             }
 
             if (details == null) {
                 details = new DofFragment();
                 isOperationNeed = true;
             }
             break;
         case Constants.EV_CHOICE:
             try {
                 details = (EvpairsFragment) getFragmentManager().findFragmentById(
                         R.id.main_detailFragment);
             } catch (ClassCastException e) {
                 Log.message("Exception: " + e.getMessage());
             }
 
             if (details == null) {
                 details = new EvpairsFragment();
                 isOperationNeed = true;
             }
             break;
         case Constants.ABOUT_CHOICE:
             
             try {
                 details = (AboutFragment) getFragmentManager()
                         .findFragmentById(R.id.main_detailFragment);
             } catch (ClassCastException e) {
                 Log.message("Exception: " + e.getMessage());
             }
 
             if (details == null) {
                 details = new AboutFragment();
                 isOperationNeed = true;
             }
             break;
         }
 
         if (isOperationNeed == true) {
             FragmentTransaction ft = getFragmentManager().beginTransaction();
             ft.replace(R.id.main_detailFragment, details);
             ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
             ft.commit();
         }
     }
     
     /**
      * Shows activity by index
      * @param index activity index for shown
      */
     private void showActivity(int index) {
         Log.message("Enter");
         Intent intent = new Intent();
         intent.setClass(getActivity(), DetailsActivity.class);
         intent.putExtra(Constants.SHOWN_INDEX, index);
         startActivity(intent);
     }
     
 }
