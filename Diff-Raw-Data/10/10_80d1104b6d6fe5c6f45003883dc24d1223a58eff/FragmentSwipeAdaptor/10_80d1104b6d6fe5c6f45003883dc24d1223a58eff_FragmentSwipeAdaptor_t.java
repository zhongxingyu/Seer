 package directi.androidteam.training.chatclient.Chat;
 
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.util.Log;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 17/9/12
  * Time: 6:10 PM
  * To change this template use File | Settings | File Templates.
  */
 public  class FragmentSwipeAdaptor extends FragmentStatePagerAdapter {
     static FragmentManager fragmentManager;
     private int position = POSITION_UNCHANGED;
     public FragmentSwipeAdaptor(FragmentManager fm) {
         super(fm);
         fragmentManager = fm;
     }
 
     @Override
     public Fragment getItem(int i) {
         Log.d("adad","get item called");
         MyFragmentManager manager = MyFragmentManager.getInstance();
         String from = manager.getJidByFragId(i);
         Fragment fragment = manager.getFragByJID(from);
         //fragmentManager.beginTransaction().addSubscriber(fragment, from).commit();
         Fragment oldFragment = fragmentManager.findFragmentByTag(from);
         if(oldFragment!=null)
             fragmentManager.beginTransaction().remove(oldFragment).commitAllowingStateLoss();
         fragmentManager.beginTransaction().add(fragment, from).commitAllowingStateLoss();
         return fragment;
     }
 
     @Override
     public int getCount() {
        return MyFragmentManager.getInstance().getSizeofActiveChats();
     }
 
 
 
     @Override
     public void destroyItem(android.view.ViewGroup container, int position, java.lang.Object object) {
         super.destroyItem(container,position,object);
         Log.d("xcxc", "destroy swiper : position = "+position);
     }
 
 
     public static Fragment getFragment(String jid) {
         Fragment fragment = fragmentManager.findFragmentByTag(jid);
         if(fragment==null){
             MyFragmentManager manager = MyFragmentManager.getInstance();
             fragment = manager.getFragByJID(jid);
            if(fragment!=null) {
                try{
                    fragmentManager.beginTransaction().add(fragment, jid).commitAllowingStateLoss();
                }
                catch (Exception e) {
                   e.printStackTrace();
                }
            }
         }
         return fragment;
     }
 
     public void setPosition(Boolean Changed) {
         if(Changed)
         this.position = POSITION_NONE;
     }
 
     @Override
     public int getItemPosition(Object object) {
         if(position==POSITION_NONE) {
             position = POSITION_UNCHANGED;
             return POSITION_NONE;
         }
         else
         return POSITION_UNCHANGED;
     }
 }
