 /*
  * Waker project 2013
  * 
  * folks studio
  * 
  * by caifangmao8@gmail.com
  */
 package com.cfm.waker.adapter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.cfm.waker.R;
 import com.cfm.waker.entity.Alarm;
 import com.cfm.waker.view.RowBlock;
 
 import android.content.Context;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class AlarmListAdapter extends PagerAdapter{
 	
 	private static final String TAG = "AlarmListAdapter";
 	
 	private Context context;
 	
 	private List<View> viewList;
 	private List<Alarm> alarmList;
 	
 	public AlarmListAdapter(Context context, List<Alarm> alarmList){
 		this.context = context;
 		this.alarmList = alarmList;
 		viewList = new ArrayList<View>();
 	}
 	
 	@Override
 	public void destroyItem(ViewGroup viewPager, int position, Object object){
 		try{
 			((ViewPager) viewPager).removeView(viewList.get(position));
 		}catch(IndexOutOfBoundsException e){
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void finishUpdate(View view){
 		
 	}
 	
 	@Override
 	public Object instantiateItem(View view, int position){
 		if(alarmList.size() <= 0){
 			LayoutInflater inflater = LayoutInflater.from(context);
 			View v = inflater.inflate(R.layout.viewpager_page_no_saved_alarm, null, false);
 			((ViewPager) view).addView(v);
 			((ViewPager) view).setTag(v);
 			return v;
 		}else{
 			//remove the no saved alarm layout
 			if(position == 0){
 				try{
 					((ViewPager) view).removeView((View) ((ViewPager) view).getTag());
 				}catch(NullPointerException e){
 					e.printStackTrace();
 				}
 			}
 			
 			//add RowBlock into ViewPager and recalculate visible and invisible items;
 			RowBlock rowBlock;
 			try{
 				rowBlock = (RowBlock) viewList.get(position);
 			}catch(IndexOutOfBoundsException e){
 				rowBlock = new RowBlock(context);
 				viewList.add(position, rowBlock);
 			}
 			
 			int i = 0;
 			int bdg = 0;
 			
 			//calculate whether RowBlock should make all items visible or not;
 			//bdg is the count of how many Alarms left in alarmList in a single RowBlock
 			if(position == getCount() - 1 && alarmList.size() % 4 != 0){
 				bdg = alarmList.size() % 4;
 			}else{
 				bdg = 4;
 			}
 			
 			
 			do{
 				
 				rowBlock.setAlarmVisible(i);
 				rowBlock.getAlarmBlock(i).setAlarm(alarmList.get(position * 4 + i));
 				
 			}while(++i < bdg);
 			
 			((ViewPager) view).addView(rowBlock);
 			
 			return rowBlock;
 		}
 	}
 	
 	@Override
 	public int getItemPosition(Object object){
 		return PagerAdapter.POSITION_NONE;
 	}
 
 	@Override
 	public int getCount() {
 		//when alarmList.size() == 0, shall return 1 for the no alarm saved layout
 		//otherwise calculate the pagecount in ViewPager by using alarmList.size()
 		if(alarmList.size() == 0){
 			return 1;
 		}else{
 			int div = alarmList.size() / 4;
 			int r = alarmList.size() % 4;
 			
 			return r == 0 ? div : div + 1;
 		}
 	}
 
 	@Override
 	public boolean isViewFromObject(View view, Object object) {
 		// TODO Auto-generated method stub
 		return view == object;
 	}
 	
 	@Override
 	public void restoreState(Parcelable parcelable, ClassLoader classLoader){
 		
 	}
 	
 	@Override
 	public Parcelable saveState(){
 		return null;
 	}
 	
 	@Override
 	public void startUpdate(View view){
 		
 	}
 
 }
