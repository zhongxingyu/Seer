 package com.beautifulpromise.application;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.beautifulpromise.R;
 import com.beautifulpromise.common.alarm.Alarm;
 import com.beautifulpromise.common.dto.AddPromiseDTO;
 import com.beautifulpromise.database.CheckDAO;
 import com.beautifulpromise.database.CheckDBHelper;
 import com.beautifulpromise.database.DatabaseHelper;
 import com.beautifulpromise.database.GoalsDAO;
 
 public class HomeActivity extends BeautifulPromiseActivity {
 	/** Called when the activity is first created. */
 
 	ListView PromiseListView;
 	MyListAdapter MyAdapter;
 	ArrayList<AddPromiseDTO> promisedto;
 	int flag = 0;
 	AnimationDrawable mAni;
 	AlphaAnimation animation1;
 	AlphaAnimation animation2;
 	ImageView img;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		LinearLayout layout = (LinearLayout) View.inflate(this, R.layout.homeactivity, null);
 		setActivityLayout(layout);
 
 		PromiseListView = (ListView) findViewById(R.id.home_list);
 		
 		// 알람
 		Alarm alarm = new Alarm();
 		alarm.SetAlarm(this);
 		
 		if(flag == 0) {
 			img = (ImageView) findViewById(R.id.home_test);
 			
 			mAni = new AnimationDrawable();
 			
 //			animation1 = new AlphaAnimation(1.0f, 0.6f); 
 //			animation1.setDuration(1950); 
 //		    animation1.setStartOffset(50); 
 //			
 //		    animation2 = new AlphaAnimation(0.6f, 1.0f); 
 //		    animation2.setStartOffset(50);
 //		    animation2.setDuration(1950); 
 			
 			mAni.addFrame((BitmapDrawable) getResources().getDrawable(
 					R.drawable.home_banner1), 2000);
 			mAni.addFrame((BitmapDrawable) getResources().getDrawable(
 					R.drawable.home_banner2), 2000);
 			mAni.addFrame((BitmapDrawable) getResources().getDrawable(
 					R.drawable.home_banner3), 2000);
 			mAni.addFrame((BitmapDrawable) getResources().getDrawable(
 					R.drawable.home_banner4), 2000);
 			
 			mAni.setOneShot(false);
 			img.setImageDrawable(mAni);
 
 			mAni.start();
 //			img.startAnimation(animation1);
 			
 			flag++;
 		}
 //		
		img.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("TEST","TEST");
			}
		});
 //	    //animation1 AnimationListener 
 //	    animation1.setAnimationListener(new AnimationListener(){ 
 //	 
 //	        @Override 
 //	        public void onAnimationEnd(Animation arg0) { 
 //	            // start animation2 when animation1 ends (continue) 
 //	        	img.startAnimation(animation2); 
 //	        } 
 //	 
 //	        @Override 
 //	        public void onAnimationRepeat(Animation arg0) { 
 //	            // TODO Auto-generated method stub 
 //	 
 //	        } 
 //	 
 //	        @Override 
 //	        public void onAnimationStart(Animation arg0) { 
 //	            // TODO Auto-generated method stub 
 //	 
 //	        } 
 //	 
 //	    }); 
 //	    
 //	    //animation2 AnimationListener 
 //	    animation2.setAnimationListener(new AnimationListener(){ 
 //	 
 //	        @Override 
 //	        public void onAnimationEnd(Animation arg0) { 
 //	            // start animation1 when animation2 ends (repeat) 
 //	        	img.startAnimation(animation1); 
 //	        } 
 //	 
 //	        @Override 
 //	        public void onAnimationRepeat(Animation arg0) { 
 //	            // TODO Auto-generated method stub 
 //	 
 //	        } 
 //	 
 //	        @Override 
 //	        public void onAnimationStart(Animation arg0) { 
 //	            // TODO Auto-generated method stub 
 //	 
 //	        } 
 //	 
 //	    }); 
 	    
 	}
 
 
 	class MyListAdapter extends BaseAdapter implements OnClickListener {
 		Context maincon;
 		LayoutInflater Inflater;
 		ArrayList<AddPromiseDTO> arSrc;
 		int layout;
 
 		public MyListAdapter(Context context, int alayout,
 				ArrayList<AddPromiseDTO> aarSrc) {
 			maincon = context;
 			Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			arSrc = aarSrc;
 			layout = alayout;
 		}
 
 		public int getCount() {
 			return arSrc.size();
 		}
 
 		public AddPromiseDTO getItem(int position) {
 			return arSrc.get(position);
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			if (convertView == null) {
 				convertView = Inflater.inflate(layout, parent, false);
 			}
 
 			TextView promisenametxt = (TextView) convertView
 					.findViewById(R.id.promisename);
 			TextView d_daytxt = (TextView) convertView.findViewById(R.id.d_day);
 			ImageView checkimg = (ImageView) convertView.findViewById(R.id.home_check);
 
 			promisenametxt.setText(arSrc.get(position).getTitle());
 			int check = 0;
 			
 			CheckDBHelper checkDBHelper = new CheckDBHelper(HomeActivity.this);
 			CheckDAO checkDAO = new CheckDAO(checkDBHelper);
 			
 			check = checkDAO.feedcheckdo(arSrc.get(position).getPostId());
 			checkDAO.close();
 			
 			// D-Day, D-day가 지나서 평가를 해야되는 약속들
 			if (arSrc.get(position).getResult() == 0 && arSrc.get(position).getD_day() < 1) {
 				d_daytxt.setText("D-Day");
 				d_daytxt.setTextColor(Color.RED);
 				checkimg.setImageResource(R.drawable.ico_finished);
 			}
 			// 오늘 피드를 올린 약속
 			else if(check == 1)
 			{
 				d_daytxt.setText("D-"+ String.valueOf(arSrc.get(position).getD_day()));
 				checkimg.setImageResource(R.drawable.ico_assessment);
 			}
 			//오늘 피드를 올리지 않은 약속
 			else
 			{
 				d_daytxt.setText("D-" + String.valueOf(arSrc.get(position).getD_day()));
 				checkimg.setImageResource(R.drawable.ico_clear);
 			}
 
 			convertView.setTag(position);
 			convertView.setOnClickListener(this);
 
 			return convertView;
 		}
 
 		public void onClick(View v) {
 			int position = (Integer) v.getTag();
 
 			AddPromiseDTO promiseObject = getItem(position);
 			Intent intent = new Intent();
 
 			Bundle extras = new Bundle();
 			extras.putSerializable("PromiseDTO", promiseObject);
 
 			if(promiseObject.getResult() == 0 && promiseObject.getD_day() < 1)
 			{
 				intent.setAction("feedviewer.PromiseCheck");
 				intent.putExtra("feedId", "159564817509135");
 				startActivity(intent);
 			}
 			// 주기(GPS)
 			else if(promiseObject.getCategoryId() == 0) {
 //				intent = new Intent(HomeActivity.this, CycleCheckActivity.class);
 				intent.setAction("checkpromise.CycleCheckActivity");
 			}
 			// 운동/공부 (타이머)
 			else if (promiseObject.getCategoryId() == 1) {
 //				intent = new Intent(HomeActivity.this, WorkCheckActivity.class);
 				intent.setAction("checkpromise.WorkCheckActivity");
 			}
 			else {
 //				intent = new Intent(HomeActivity.this, EtcCheckActivity.class);
 				intent.setAction("checkpromise.EtcCheckActivity");
 			}
 			intent.putExtras(extras);
 			startActivityForResult(intent, 0);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		Calendar oCalendar = Calendar.getInstance();
 		DatabaseHelper databaseHelper = new DatabaseHelper(this);
 		GoalsDAO goalsDAO = new GoalsDAO(databaseHelper);
 		
 		promisedto = goalsDAO.getGoalList(oCalendar.get(Calendar.DAY_OF_WEEK));
 		
 		CheckDBHelper checkDBHelper = new CheckDBHelper(this);
 		CheckDAO checkDAO = new CheckDAO(checkDBHelper);
 //		checkDAO.feedcheckinit(promisedto);
 		checkDAO.feedtest();
 		checkDAO.close();
 		
 		// D-day계산해서 객체에 값넣음
 		for (int i = 0; i < promisedto.size(); i++) {
 			promisedto.get(i).setD_day(promisedto.get(i).getEndDate());
 		}
 
 		MyAdapter = new MyListAdapter(this, R.layout.homeactivity_list,
 				promisedto);
 
 		// PromiseListView = (ListView) findViewById(R.id.list);
 		PromiseListView.setAdapter(MyAdapter);
 		PromiseListView.setItemsCanFocus(false);
 		PromiseListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 	}
 
 }
 
