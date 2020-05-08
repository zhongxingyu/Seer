 package com.example.swp_ucd_2013_eule.model;
 
 import java.util.List;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.example.swp_ucd_2013_eule.data.SettingsWrapper;
 import com.example.swp_ucd_2013_eule.net.APIException;
 
 public class MyForest {
 
 	private static MyForest INSTANCE = new MyForest();
 	private Forest mForest;
 	private volatile OnItemBoughtListener mListener;
 
 	private APIModel<Forest, Forest> mForestAPI;
 	private APIModel<UserForestItem, Forest> mUserItemAPI;
 	private APIModel<Statistic, Forest> mStatAPI;
 
 	public final static Integer FOREST_ID = 1;
 
 	private MyForest() {
 	}
 
 	public static MyForest getInstance() {
 		return INSTANCE;
 	}
 
 	public Forest loadForest() {
 		mForestAPI = new APIModel<Forest, Forest>(Forest.class);
 		mUserItemAPI = new APIModel<UserForestItem, Forest>(
 				UserForestItem.class);
 		mStatAPI = new APIModel<Statistic, Forest>(Statistic.class);
 		try {
 
 			Forest forest = new Forest(SettingsWrapper.getInstance()
 					.getCurrentForestId());
 			mForest = mForestAPI.get(forest);
 
 			List<UserForestItem> userItems = mUserItemAPI.getAllByParent(
 					mForest, new UserForestItem(), "userforestitems");
 
 			List<Statistic> stats = mStatAPI.getAllByParent(mForest,
 					new Statistic(), "statistics");
 
 			mForest.setUserforestitems(userItems);
 			mForest.setStatistics(stats);
 			MyMarket.getInstance(); // call is important for initialization!
 		} catch (APIException e) {
 			Log.e("MyUser", "API failure. Failed to load forest!");
 		}
 
 		return mForest;
 	}
 
 	public Forest getForest() {
 		return mForest;
 	}
 
 	public boolean isObtainable(Item item) {
		if (item.isStandardItem() && item.getPrice() <= mForest.getPoints()
				&& item.getLevel() <= mForest.getLevel()) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean buyItem(Item item) {
 
 		item.incAmount();
 
 		int points = mForest.getPoints() - item.getPrice();
 		mForest.setPoints(points);
 		UserForestItem uItem = new UserForestItem(item);
 		uItem.setTile(-1, -1);
 		uItem.setOffset(0.5f, 0.5f);
 
 		mForest.addItem(uItem);
 
 		if (mListener != null) {
 			mListener.onNewItemBought(uItem);
 		}
 
 		// mUserItemAPI.save(uItem);
 		// mUserItemAPI.addToParent(uItem, mForest, "userforestitems");
 		addUserItem(uItem);
 
 		return true;
 
 	}
 
 	public void addOnItemBoughtListener(OnItemBoughtListener listener) {
 		mListener = listener;
 	}
 
	public void saveForest() {
 		new AsyncTask<Void, Void, Boolean>() {
 
 			@Override
 			protected Boolean doInBackground(Void... voids) {
 				try {
 					Log.d("MyForest", "Trying to save THE forest...");
 					mForestAPI.save(mForest);
 					return true;
 				} catch (APIException e) {
 					return false;
 				}
 			}
 
 			@Override
 			protected void onPostExecute(Boolean success) {
 				super.onPostExecute(success);
 
 				if (!success) {
 					// CharSequence text = "Failed to save UserForestItem!";
 					// Context context = getApplicationContext();
 					// Toast toast = Toast.makeText(context, text,
 					// Toast.LENGTH_LONG);
 					// toast.show();
 					Log.e("MyForest", "Failed to save THE Forest!");
 				}
 				// finish();
 			}
 		}.execute(null, null);
 
 	}
 
 	public void addStatistic(Statistic stat) {
 		new AsyncTask<Statistic, Void, Boolean>() {
 
 			@Override
 			protected Boolean doInBackground(Statistic... stats) {
 				try {
 					mStatAPI.save(stats[0]);
 					mStatAPI.addToParent(stats[0], mForest, "statistics");
 					return true;
 				} catch (APIException e) {
 					Log.e("MyForest", "API failure. Could not add statistic!");
 					return false;
 				}
 			}
 
 			@Override
 			protected void onPostExecute(Boolean success) {
 				super.onPostExecute(success);
 
 				if (!success) {
 					// CharSequence text = "Failed to save UserForestItem!";
 					// Context context = getApplicationContext();
 					// Toast toast = Toast.makeText(context, text,
 					// Toast.LENGTH_LONG);
 					// toast.show();
 					Log.e("MyForest", "Failed to add Statistic!");
 				}
 				// finish();
 			}
 		}.execute(stat);
 
 	}
 
 	public void addUserItem(UserForestItem item) {
 		new AsyncTask<UserForestItem, Void, Boolean>() {
 
 			@Override
 			protected Boolean doInBackground(UserForestItem... items) {
 				try {
 					mUserItemAPI.save(items[0]);
 					mUserItemAPI.addToParent(items[0], mForest,
 							"userforestitems");
 					return true;
 				} catch (APIException e) {
 					Log.e("MyForest", "API failure. Could not add useritem!");
 					return false;
 				}
 			}
 
 			@Override
 			protected void onPostExecute(Boolean success) {
 				super.onPostExecute(success);
 
 				if (!success) {
 					// CharSequence text = "Failed to save UserForestItem!";
 					// Context context = getApplicationContext();
 					// Toast toast = Toast.makeText(context, text,
 					// Toast.LENGTH_LONG);
 					// toast.show();
 					Log.e("MyForest", "Failed to add UserForestItem!");
 				}
 				// finish();
 			}
 		}.execute(item);
 
 	}
 
 	public void updateUserForestItemPosition(UserForestItem item) {
 		new AsyncTask<UserForestItem, Void, Boolean>() {
 
 			@Override
 			protected Boolean doInBackground(UserForestItem... items) {
 				try {
 					mUserItemAPI.save(items[0]);
 					return true;
 				} catch (APIException e) {
 					Log.e("MyForest",
 							"API failure. Could not update useritem position!");
 					return false;
 				}
 			}
 
 			@Override
 			protected void onPostExecute(Boolean success) {
 				super.onPostExecute(success);
 
 				if (!success) {
 					// CharSequence text = "Failed to save UserForestItem!";
 					// Context context = getApplicationContext();
 					// Toast toast = Toast.makeText(context, text,
 					// Toast.LENGTH_LONG);
 					// toast.show();
 					Log.e("MyForest", "Failed to save UserForestItem!");
 				}
 				// finish();
 			}
 		}.execute(item);
 	}
 
 }
