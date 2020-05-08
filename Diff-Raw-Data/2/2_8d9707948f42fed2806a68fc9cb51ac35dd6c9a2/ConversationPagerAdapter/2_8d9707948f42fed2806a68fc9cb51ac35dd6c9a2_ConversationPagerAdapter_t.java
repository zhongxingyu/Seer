 package no.whg.whirc.adapters;
 
 import java.util.ArrayList;
 
 import jerklib.Channel;
 import jerklib.Session;
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
 	private ArrayList<Fragment> fragments;
 	private FragmentManager manager;
 	private Session session;
 	
 
     public ConversationPagerAdapter(FragmentManager fm, Session s) {
         super(fm);
         this.session = s;
         this.manager = fm;
         this.fragments = new ArrayList<Fragment>();
         
         for (Channel c : session.getChannels()) {
         	fragments.add(new ConversationFragment()); // derp..
         }
     }
 
 	@Override
     public Fragment getItem(int index) {	
 
         // getItem is called to instantiate the fragment for the given page.
         // Return a ConversationFragment (defined as a static inner class
         // below) with the page number as its lone argument.
         if (fragments != null && fragments.size() >= index) {
         	return fragments.get(index);
         } else {
         	return null;
         }
     }
 
     @Override
     public int getCount() {
         // Show 3 total pages.
         return fragments.size();
     }
 
     @Override
     public CharSequence getPageTitle(int position) {
         return null;
     }
     
 
 	@Override
 	public int getItemPosition(Object object) {
 		int position = fragments.indexOf(object);
 		if (position >= 0) {
 			return position;
 		} else {
 			return POSITION_NONE;
 		}
 	}
 
 	public void addFragment(ConversationFragment f) {
     	fragments.add(f);
     	notifyDataSetChanged();
     }
     
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
 }
