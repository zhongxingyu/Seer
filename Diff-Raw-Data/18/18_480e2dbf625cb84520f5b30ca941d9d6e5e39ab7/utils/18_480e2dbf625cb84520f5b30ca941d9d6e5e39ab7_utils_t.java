 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.utility;
 
import no.hials.muldvarp.v2.fragments.MuldvarpFragment;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.util.Log;
 import java.util.List;
 import no.hials.muldvarp.R;
 
 /**
  *
  * @author kristoffer
  */
public class testUtils {
     
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
     
    public static boolean changeFragmentWithoutList(Activity activity, Fragment fragment) {
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
