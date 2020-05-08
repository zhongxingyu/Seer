 package fit.main;
 
import android.sup
 
 public static class horarios extends FragmentPagerAdapter {
     public MyAdapter(FragmentManager fm) {
         super(fm);
     }
 
     @Override
     public int getCount() {
         return NUM_ITEMS;
     }
 
     @Override
     public Fragment getItem(int position) {
         return ArrayListFragment.newInstance(position);
     }
}
