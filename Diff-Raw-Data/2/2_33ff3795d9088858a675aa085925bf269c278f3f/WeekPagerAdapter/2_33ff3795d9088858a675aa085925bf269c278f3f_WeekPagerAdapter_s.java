 package by.fksis.schedule.adapters;
 
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.view.ViewGroup;
 import by.fksis.schedule.R;
 import by.fksis.schedule.Util;
 import by.fksis.schedule.app.DayScheduleFragment;
 
 import java.util.Calendar;
 
 public class WeekPagerAdapter extends FragmentPagerAdapter {
     public static final int TOTAL_PAGES = 30;
     public static final int NOW_PAGE = 15;
 
     private final Context context;
     private Calendar[] dates;
 
     @Override
     public CharSequence getPageTitle(int position) {
         if (position == NOW_PAGE)
             return context.getString(R.string.today);
         if (position == NOW_PAGE + 1)
             return context.getString(R.string.tomorrow);
         if (position == NOW_PAGE - 1)
             return context.getString(R.string.yesterday);
         if (Math.abs(position - NOW_PAGE) < 4)
             return context.getResources().getStringArray(R.array.weekdays)[Util.getDayOfWeekIndex(dates[position])].toUpperCase();
 
         int day = dates[position].get(Calendar.DAY_OF_MONTH);
         int month = dates[position].get(Calendar.MONTH) + 1;
        return day + "." + (month < 9 ? "0" : "") + month;
     }
 
     public WeekPagerAdapter(FragmentManager fm, Context context) {
         super(fm);
         this.context = context;
 
         dates = new Calendar[TOTAL_PAGES];
         Calendar now = Calendar.getInstance();
 
         for (int i = 0; i < TOTAL_PAGES; i++) {
             dates[i] = (Calendar) now.clone();
             dates[i].add(Calendar.DAY_OF_MONTH, i - NOW_PAGE);
         }
     }
 
     @Override
     public Fragment getItem(int i) {
         return new DayScheduleFragment(dates[i]);
     }
 
     @Override
     public int getCount() {
         return TOTAL_PAGES;
     }
 
     @Override
     public void destroyItem(ViewGroup container, int position, Object object) {
 
     }
 }
