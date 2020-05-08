 package edu.csufresno.mycsufi;
 
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ScheduleListView extends Activity{
 
 	public int dayofweek = 0;
 	public static final String[] days = { "Mo","Tu", "We","Th", "Fr","Sa"};
 	public static String[] place;
 	public static String[] time;
 	public static String[] cord;
 	StudentClassSchedule studentClassSchedule=new StudentClassSchedule();
 
 	public static class EfficientAdapter extends BaseAdapter {
 		private LayoutInflater mInflater;
 
 		public EfficientAdapter(Context context) {
 			mInflater = LayoutInflater.from(context);
 
 		}
 
 		public int getCount() {
 			return time.length;
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder holder;
 			if (convertView == null) {
 				convertView = mInflater.inflate(R.layout.scheduleview, null);
 				holder = new ViewHolder();
 				holder.text = (TextView) convertView
 						.findViewById(R.id.Time);
 				holder.text2 = (TextView) convertView
 						.findViewById(R.id.Place);
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			holder.text.setText(time[position]);
 			holder.text2.setText(place[position]);
 			return convertView;
 		}
 
 		static class ViewHolder {
 			TextView text;
 			TextView text2;
 		}
 
 	}
 
 	GestureDetector mGestureDetector = null;
 	View.OnTouchListener mGestureListener = null;
 
 	class MyGestureDetector extends SimpleOnGestureListener {
 		private static final int SWIPE_MIN_DISTANCE = 150;
 		private static final int SWIPE_MAX_OFF_PATH = 100;
 		private static final int SWIPE_THRESHOLD_VELOCITY = 100;
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			float dX = e2.getX() - e1.getX();
 			float dY = e1.getY() - e2.getY();
 			if (Math.abs(dY) < SWIPE_MAX_OFF_PATH
 					&& Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY
 					&& Math.abs(dX) >= SWIPE_MIN_DISTANCE) {
 				if (dX > 0) {
 					dayofweek = (dayofweek - 1) % 7;
 					screen();
 				} else {
 					dayofweek = (dayofweek + 1) % 7;
 					screen();
 				}
 				return true;
 			}
 			return false;
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.schedulemain);
 //		TextView myText = (TextView) findViewById(R.id.TextView04);
 //		myText.setText("Monday");
 		screen();
 	}// end off on create
 
 	private void screen()
 	{
 		String day=days[dayofweek];
 		ArrayList<StudentClass> classes= studentClassSchedule.getClassesByDayOfWeek(day);
 		place = new String[classes.size()];
 		time = new String[classes.size()];
 		for(int i=0;i<classes.size();i++)
 		{
 			StudentClass sclass=classes.get(i);
 			//String ClassName=sclass.getName();
 			place[i]=sclass.getRoom()+" " + sclass.getBuilding();
 			time[i]=sclass.getStarttime() +" " + sclass.getName();
 			
 		}
 
 		updateview();
 		
 	};
 
 	private void updateview() {
 		ListView l1 = (ListView) findViewById(R.id.ListView01);
 		l1.setAdapter(new EfficientAdapter(this));
 		l1.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				Toast.makeText(getBaseContext(), "You clciked " + cord[arg2],
 						Toast.LENGTH_LONG).show();
 			}
 		});
 
 		mGestureDetector = new GestureDetector(new MyGestureDetector());
 		mGestureListener = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent aEvent) {
 				if (mGestureDetector.onTouchEvent(aEvent))
 					return true;
 				else
 					return false;
 			}
 		};
 		l1.setOnTouchListener(mGestureListener);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			Toast.makeText(this, "You pressed the icon and text!",
 					Toast.LENGTH_LONG).show();
 			this.finish();
 
 			break;
 		}
 		return true;
 	}
 
 }// end of everything
