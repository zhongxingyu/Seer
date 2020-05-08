 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.SpinnerAdapter;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.domain.Domain;
 import no.hials.muldvarp.v2.fragments.MuldvarpFragment;
 import no.hials.muldvarp.v2.utility.FragmentUtils;
 
 /**
  * This class defines a top-level activity for a given level. This activity-class
  * contains an ArrayList of Fragment classes named fragmentList which contains the
  * types and various fragments that comprise the main content of the Activity.
  *
  * @author johan
  */
 public class TopActivity extends MuldvarpActivity{
     private Activity thisActivity = this;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
        getApplicationContext().deleteDatabase("muldvarp.db");
         //See if the Activity was started with an Intent that included a Domain object
         if(getIntent().hasExtra("Domain")) {
             domain = (Domain) getIntent().getExtras().get("Domain");
             activityName = domain.getName();
         } else {
             activityName = getResources().getString(R.string.app_logo_top);
             domain = new Domain();
         }
 
         //Add fragments to list if not empty:
         if(fragmentList.isEmpty()) {
             domain.populateList(fragmentList, this);
         }
 
         //Get dropdown menu
         SpinnerAdapter mSpinnerAdapter = new ArrayAdapter(this,
                 android.R.layout.simple_spinner_dropdown_item,
                 getDropDownMenuOptions(fragmentList));
 
         ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
 
             @Override
             public boolean onNavigationItemSelected(int position, long itemId) {
 
                 return FragmentUtils.changeFragment(thisActivity, fragmentList, position);
             }
         };
         getActionBar().setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
     }
 
     /**
      * This function generates a List of String values based on the titles of
      * a List of MuldvapFragments.
      *
      * @param fragmentList List of MuldvarpFragments
      * @return List
      */
     public List getDropDownMenuOptions(List<MuldvarpFragment> fragmentList){
         List retVal = new ArrayList();
         for (int i = 0; i < fragmentList.size(); i++) {
             retVal.add(fragmentList.get(i).getFragmentTitle());
         }
         return retVal;
     }
 
     public Domain getDomain() {
         return domain;
     }
 
 }
