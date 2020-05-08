 package uq.deco7381.runspyrun.model;
 
 import java.util.ArrayList;
 
 import uq.deco7381.runspyrun.R;
 import android.R.integer;
 import android.content.Context;
 import android.location.Location;
 import android.support.v4.widget.SlidingPaneLayout;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.parse.ParseUser;
 
 public class ListAdapter_defence extends BaseAdapter {
 
 	private LayoutInflater mInflater;
 	private ArrayList<Equipment> mAppList;
 	private ArrayList<Obstacle> newObstaclesOnCourse;	// Save the obstacle when user create new.
 	private Context mContext;
 	private Location mLocation;
 	private GoogleMap mGmap;
 	private double distanceToStream;
 	private int userEnergy;
 	private SlidingPaneLayout mPaneLayout;
 	private TextView energy;
 	private TextView obstacleTextView;
 	private int maxObstacle;
 	private int currentObstacle;
 
 	
 	public ListAdapter_defence(Context c, ArrayList<Equipment> list, GoogleMap map, SlidingPaneLayout mPaneLayout, TextView energy, TextView obstacleTextView, int maxObstacle, int currentObstacle) {
 		// TODO Auto-generated constructor stub
 		mAppList = list;
 		mContext = c;
 		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		mGmap = map;
 		userEnergy = ParseUser.getCurrentUser().getInt("energyLevel");
 		this.newObstaclesOnCourse = new ArrayList<Obstacle>();
 		this.mPaneLayout = mPaneLayout;
 		this.energy = energy;
 		this.obstacleTextView = obstacleTextView;
 		this.maxObstacle = maxObstacle;
 		this.currentObstacle = currentObstacle;
 	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return mAppList.size();
 	}
 
 	public void setCurrentLocation(Location currenLocation, double distanceToStream){
 		this.mLocation = currenLocation;
 		this.distanceToStream = distanceToStream;
 		notifyDataSetChanged();
 	}
 	/**
      * Override all the data set in this adapter.
      * @param ArrayList<Equipment>
      */
     public void overrideDataset(ArrayList<Equipment> equipment){
 		this.mAppList = equipment;
 		notifyDataSetChanged();
 	}
     /**
      * Override the max number of obstacle of this course.
      * @param integer
      */
     public void overrideMaxObstacle(int maxObstacle){
     	this.maxObstacle = maxObstacle;
     }
     /**
      * Override the current number of obstacle of this course.
      * @param integer
      */
     public void overrideCurrentObstacle(int currentObstacle){
     	this.currentObstacle = currentObstacle;
     }
     public ArrayList<Equipment> getList(){
     	return this.mAppList;
     }
 	public ArrayList<Obstacle> getNewObstaclesOnCourse(){
 		return this.newObstaclesOnCourse;
 	}
 	public int getUserEnergy(){
 		return userEnergy;
 	}
 	
 	@Override
 	public Object getItem(int position) {
 		// TODO Auto-generated method stub
 		return mAppList.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// TODO Auto-generated method stub
 		
 		/*
 		 * Set up view holder
 		 */
 		ViewHolder holder = null;
 		if (convertView != null){
 			holder = (ViewHolder) convertView.getTag();
 		}else {
 			convertView = mInflater.inflate(R.layout.list_tag_defence, null);
 			holder = new ViewHolder();
 			holder.type = (TextView) convertView.findViewById(R.id.missionNum);
 			holder.number = (TextView)convertView.findViewById(R.id.textView2);
 			convertView.setTag(holder);
 		}
 		final Equipment equipment = mAppList.get(position);
 		final int currentNum = equipment.getNumber();
 		holder.type.setText(equipment.getType());
 		holder.number.setText(String.valueOf(currentNum));
 		
 		if(mLocation != null && currentNum > 0){
 			if(ParseUser.getCurrentUser().getInt("level") >= equipment.getLevelLimit()){
 				holder.type.setTextColor(mContext.getResources().getColor(R.color.androidBlue));
 				holder.number.setTextColor(mContext.getResources().getColor(R.color.androidBlue));
 				convertView.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						// TODO Auto-generated method stub
 						if(distanceToStream <= 400){
 							
 							int COST = equipment.getBaseCost(); //Cost 40 energy to set a guard in lv 1
 							int userLevel = ParseUser.getCurrentUser().getInt("level");
 							int userSpend = COST * userLevel;
 							if(userSpend > equipment.getMaxCost()){
 								userSpend  = equipment.getMaxCost(); 
 							}
 							if(userSpend > userEnergy){
 								Toast.makeText(mContext.getApplicationContext(), "You don't have enough energy.", Toast.LENGTH_LONG).show();
 							}else if(currentObstacle >= maxObstacle){
 								System.out.println("current:" + currentObstacle);
 								System.out.println("max:"+maxObstacle);
 								Toast.makeText(mContext.getApplicationContext(), "This course is full.", Toast.LENGTH_LONG).show();
 							}else{
 								ParseUser currentUser = ParseUser.getCurrentUser();
 								
 								Obstacle obstacle = null;
 								if(equipment.getType().equals("Guard")){
 									obstacle = new Guard(mLocation.getLatitude(),mLocation.getLongitude(),mLocation.getAltitude(), currentUser, currentUser.getInt("level"),null);
 								} else if(equipment.getType().equals("Dog")){
 									obstacle = new Dog(mLocation.getLatitude(),mLocation.getLongitude(),mLocation.getAltitude(), currentUser, currentUser.getInt("level"),null);
 								} else if(equipment.getType().equals("MotionDetector")){
 									obstacle = new MotionDetector(mLocation.getLatitude(),mLocation.getLongitude(),mLocation.getAltitude(), currentUser, currentUser.getInt("level"),null);
 								}
 								mGmap.addMarker(obstacle.getMarkerOptions());
 								mPaneLayout.closePane();
 								
 								currentObstacle += 1;
 								String obstaclesString = String.valueOf(currentObstacle) + " / " + String.valueOf(maxObstacle);
 								obstacleTextView.setText(obstaclesString);
 								
 								/*
 								 *  Add to the list of new obstacle
 								 */
 								newObstaclesOnCourse.add(obstacle);
 								userEnergy -= userSpend;
 								equipment.setNumber(currentNum-1);
 								
 								String energyString = String.valueOf(userEnergy);
 								energy.setText(energyString);
 								
 								
 								notifyDataSetChanged();
 							}
 						}else{
 							Toast.makeText(mContext.getApplicationContext(), "You can't create obstacle outside of zone.", Toast.LENGTH_LONG).show();
 						}
 					}
 				});
 			}else{
 				holder.type.setTextColor(mContext.getResources().getColor(R.color.gray));
 				holder.number.setTextColor(mContext.getResources().getColor(R.color.gray));
 			}
 		}else{
 			holder.type.setTextColor(mContext.getResources().getColor(R.color.gray));
 			holder.number.setTextColor(mContext.getResources().getColor(R.color.gray));
 		}
 		
 		
 		return convertView;
 	}
 
 	/*
 	 * private view holder class
 	 */
 	private class ViewHolder {
 	    TextView type;
 	    TextView number;
 	}
 }
