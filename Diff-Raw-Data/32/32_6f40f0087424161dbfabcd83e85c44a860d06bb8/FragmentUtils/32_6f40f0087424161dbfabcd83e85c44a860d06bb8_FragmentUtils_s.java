 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.utility;
 
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.util.Log;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.fragments.MuldvarpFragment;
 
 /**
  *
  * @author kristoffer
  */
 public class FragmentUtils {
     /**
      * Swaps the fragment in position with specified fragment from list
      * 
      * @param activity
      * @param fragmentList
      * @param position
      * @return 
      */
     public static boolean changeFragment(Activity activity, List<MuldvarpFragment> fragmentList, int position) {
         try {
             FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
             if(fragmentList.get(position) == null)
                 position = 0;
             ft.replace(R.id.desktop, fragmentList.get(position));
             ft.commit();
         } catch(IndexOutOfBoundsException e) {
             Log.e(activity.getLocalClassName(), e.getMessage());
             return false;
         }
         return true;
     }
     
     /**
      * Swaps the fragment in position with specified fragment
      * 
      * @param activity
      * @param fragment
      * @return 
      */
     public static boolean changeFragmentWithoutList(Activity activity, MuldvarpFragment fragment) {
         try {
             FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
             ft.replace(R.id.desktop, fragment);
            ft.addToBackStack(null);
             ft.commit();
         } catch(IndexOutOfBoundsException e) {
             Log.e(activity.getLocalClassName(), e.getMessage());
             return false;
         }
         return true;
     }
     
 
 }
