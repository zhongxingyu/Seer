 package no.whg.whirc.adapters;
 
 import java.util.ArrayList;
 
 import no.whg.whirc.R;
 import no.whg.whirc.activities.MainActivity;
 import no.whg.whirc.fragments.ConnectionFragment;
 import no.whg.whirc.fragments.ConversationFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 
 /**
  * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
  * one of the sections/tabs/pages.
  */
 public class ConversationPagerAdapter extends FragmentPagerAdapter {
 	private static final String TAG = "ConversationFragmentAdapter";
 	private ArrayList<ConversationFragment> fragments;
 	private FragmentManager manager;
 	
 	/**
 	 * 
 	 * @param fm
 	 */
     public ConversationPagerAdapter(FragmentManager fm) {
         super(fm);
         this.manager = fm;
         this.fragments = new ArrayList<ConversationFragment>();
     }
 
 	@Override
 	/**
 	 * @param index
 	 */
     public Fragment getItem(int index) {	
 
         // getItem is called to instantiate the fragment for the given page.
         // Return a ConversationFragment (defined as a static inner class
         // below) with the page number as its lone argument.
         if (fragments != null && fragments.size() > 0) {
         	Log.d(TAG, "FRAGMENTS ARRAY: " + fragments.toString());
         	Log.d("ConversationPagerAdapter", "fragments != null && fragments.size() >= index has run");
         	return fragments.get(index);
         } else {
         	Log.d("ConversationPagerAdapter", "new ConnectionFragment has been created");
         	//return new ConnectionFragment();
         	return null;
         }
     }
 
     @Override
     /**
      * 
      */
     public int getCount() {
//        if (fragments.size() == 0) {
//        	return 1;
//        } else {
//        	return fragments.size();
//        }
    	return fragments.size();
 
     }
 
     @Override
     /**
      * @param position
      */
     public CharSequence getPageTitle(int position) {
         if (fragments.size() == 0) {
         	return "Networks"; // TODO: internationalize
         } else {
         	return fragments.get(position).getName();
         }
     }
     
 
 	@Override
 	/**
 	 * @param object
 	 */
 	public int getItemPosition(Object object) {
 		Log.d("ConversationPagerAdapter", "getItemPosition ran");
 		int position = fragments.indexOf(object);
 		if (position >= 0) {
 			return position;
 		} else {
 			return POSITION_NONE;
 		}
 	}
 	/**
 	 * 
 	 * @param f
 	 */
 	public void addFragment(ConversationFragment f) {
     	fragments.add(f);
     	//notifyDataSetChanged();
     }
 	/**
 	 * 
 	 */
 	public void removeFragments(){
 		fragments.clear();
 	}
     /**
      * 
      * @param position
      */
     public void removeFragment(int position) {
     	FragmentTransaction t = manager.beginTransaction();
     	
     	try {
     		t.remove(getItem(position));
     	} catch (Exception e) {
     		Log.e("ConversationPagerAdapter", "Error when deleting fragment: " + e);
     	}
     	t.commit();
     	fragments.remove(position);
     	notifyDataSetChanged();
     }
     /**
      * finds a fragment by title and removes it
      * @param title
      */
     public void removeFragment(String title){
     	for (ConversationFragment f : fragments){
     		if (getPageTitle(getItemPosition(f)).toString().equals(title)){
     			removeFragment(fragments.indexOf(f));
     		}
     	}
     }
 }
