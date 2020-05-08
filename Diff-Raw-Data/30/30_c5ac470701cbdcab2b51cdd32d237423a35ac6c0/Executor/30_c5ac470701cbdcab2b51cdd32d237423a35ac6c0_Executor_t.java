 package com.coffeeandpower.utils;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
import android.util.Log;
 
 import com.coffeeandpower.AppCAP;
 import com.coffeeandpower.cont.DataHolder;
 import com.coffeeandpower.cont.User;
 import com.coffeeandpower.cont.UserResume;
 import com.coffeeandpower.cont.Venue;
 import com.coffeeandpower.cont.VenueSmart;
 import com.coffeeandpower.datatiming.CounterData;
 import com.coffeeandpower.views.CustomDialog;
 import com.google.android.maps.GeoPoint;
 
 public class Executor {
 
 	public static final int HANDLE_GET_USER_RESUME = 1600;
 	public static final int HANDLE_SENDING_PROP = 1601;
 	public static final int HANDLE_GET_VENUES_AND_USERS_IN_BOUNDS = 1602;
 	public static final int HANDLE_GET_USER_DATA = 1603;
 	public static final int HANDLE_VENUES_CLOSE_TO_LOCATION = 1604;
 	public static final int HANDLE_SEND_FRIEND_REQUEST = 1605;
 	public static final int HANDLE_ADD_PLACE = 1606;
 	public static final int HANDLE_GET_CONTACTS_LIST = 1607;
 	public static final int HANDLE_ONE_ON_ONE_CHAT_HISTORY = 1608;
 	public static final int HANDLE_SEND_CHAT_MESSAGE = 1609;
 	public static final int HANDLE_CHECK_IN = 1610;
 	public static final int HANDLE_GET_CHECHED_USERS_IN_FOURSQUARE = 1611;
 	public static final int HANDLE_ENTER_INVITATION_CODE = 1612;
 	public static final int HANDLE_SAVE_USER_JOB_CATEGORY = 1613;
 	public static final int HANDLE_SET_USER_PROFILE_DATA = 1614;
 	public static final int HANDLE_UPLOAD_USER_PROFILE_PHOTO = 1615;
 	public static final int HANDLE_GET_USER_TRANSACTION_DATA = 1616;
 	public static final int HANDLE_VENUE_CHAT = 1617;
 	public static final int HANDLE_SEND_VENUE_CHAT = 1618;
 
 	private DataHolder result;
 
 	private ProgressDialog progress;
 
 	private Context context;
 
 	public interface ExecutorInterface {
 		public void onActionFinished(int action);
 
 		public void onErrorReceived();
 	};
 
 	ExecutorInterface exeInter = new ExecutorInterface() {
 		@Override
 		public void onActionFinished(int action) {
			Log.d("Executor","onActionFinished...");
 		}
 
 		@Override
 		public void onErrorReceived() {
 		}
 	};
 
 	public void setExecutorListener(ExecutorInterface exeInter) {
 		this.exeInter = exeInter;
 	}
 
 	public Executor(Context context) {
 		this.context = context;
 		progress = new ProgressDialog(context);
 	}
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 
			Log.d("Executor","handleMessage()");
			
 			progress.dismiss();
 
 			if (msg.what == AppCAP.HTTP_ERROR) {
 				exeInter.onErrorReceived();
 				new CustomDialog(context, "Error", result.getResponseMessage()).show();
 			} else {
 				exeInter.onActionFinished(msg.what);
 			}
 
 		}
 	};
 
 	public synchronized DataHolder getResult() {
 		return result;
 	}
 
 	public synchronized void getResumeForUserId(final int userId) {
 		progress.setMessage("Loading");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().getUserResume(userId);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void sendReview(final UserResume userResume, final String review) {
 		progress.setMessage("Sending...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().sendReview(userResume, review);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void getVenuesAndUsersWithCheckinsInBoundsDuringInterval(final double[] coords, boolean withProgress) {
 		if (withProgress) {
 			progress.setMessage("Loading...");
 			progress.show();
 		}
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().getVenuesAndUsersWithCheckinsInBoundsDuringInterval(coords, 7);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void getUserData() {
 		progress.setMessage("Loading...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().getUserData();
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void getVenuesCloseToLocation(final GeoPoint gp, final int number) {
 		progress.setMessage("Loading nearby places...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().getVenuesCloseToLocation(gp, number);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void sendFriendRequest(final int userId) {
 		progress.setMessage("Sending Request...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().sendFriendRequest(userId);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void addPlace(final String name) {
 		progress.setMessage("Saving new place...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().addPlace(name, AppCAP.getUserCoordinates());
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void getContactsList() {
 		progress.setMessage("Loading...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().getContactsList();
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void getOneOnOneChatHistory(final int userId) {
 		progress.setMessage("Loading chat...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().getOneOnOneChatHistory(userId);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void sendOneOnOneChatMessage(final int userId, final String mess) {
 		progress.setMessage("Loading...");
 		// progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().sendOneOnOneChatMessage(userId, mess);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void getUsersCheckedInAtFoursquareID(final String venueId) {
 		progress.setMessage("Loading...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().getUsersCheckedInAtFoursquareID(venueId);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void checkIn(final VenueSmart venue, final int checkInTime, final int checkOutTime, final String statusText) {
 		progress.setMessage("Checking in...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().checkIn(venue, checkInTime, checkOutTime, statusText);
		
 				//FIXME
 				//This assumes that the checkin is going to be successful, it doesn't look like there
 				//is currently any code to verify that the checkin was successful
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void enterInvitationCode(final String invitationCode, final double lat, final double lng) {
 		progress.setMessage("Checking...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().enterInvitationCode(invitationCode, lat, lng);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void saveUserJobCategory(final String selectedMajorJob, final String selectedMinorJob) {
 		progress.setMessage("Uploading...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().saveUserJobCategory(selectedMajorJob, selectedMinorJob);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void setUserProfileData(final User user, final boolean isEmailChanged) {
 		progress.setMessage("Uploading...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().setUserProfileData(user, isEmailChanged);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void uploadUserProfilePhoto() {
 		progress.setMessage("Uploading photo...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().uploadUserProfilePhoto();
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	public synchronized void getUserTransactionData() {
 		progress.setMessage("Loading...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().getUserTransactionData();
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 	/**
 	 * Get/send venue chat <br>
 	 * getObject() instance of ArrayList <br>
 	 * <br>
 	 * [0]String lastId [1]String firstDate [2]String lastDate
 	 * [3]ArrayList(VenueChatEntry)
 	 * 
 	 * @param venueId
 	 * @param lastChatIDString
 	 */
 	public synchronized void venueChat(final int venueId, final String lastChatIDString, final String message, final boolean isSend) {
 		progress.setMessage(isSend ? "Sending..." : "Loading...");
 		progress.show();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				result = AppCAP.getConnection().venueChatForVenueWithID(venueId, lastChatIDString, message, isSend);
 				handler.sendEmptyMessage(result.getHandlerCode());
 			}
 		}).start();
 	}
 
 }
