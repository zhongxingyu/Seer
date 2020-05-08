 package com.dbstar.app;
 
 import java.util.Calendar;
 import java.util.Locale;
 
 import com.dbstar.R;
 import com.dbstar.model.GDCalendarGB;
 
 import android.app.Activity;
 import android.util.Log;
 import android.widget.TextView;
 
 public class GDCelanderThread extends Thread {
 	private static final String TAG = "GDCelanderThread";
 	private static int INTERVAL_IN_SECONDS = 1000;
 	private static int INTERVAL_IN_MINUTES = 60000;
 
 	int mInterval = INTERVAL_IN_SECONDS;
 
 	TextView mTimeView;
 	TextView mDateView;
 	TextView mWeekView;
 
 	Object mUpdateLock = new Object();
 	boolean mUpdate = false;
 	Object mExitLock = new Object();
 	boolean mExit = false;
//	boolean mLunarDateIsSet = false;
 	String mTextNongLi;
 	Activity mParentActivity;
 	GDCalendarGB mLunar;
 	
 	public GDCelanderThread(Activity parent, TextView timeView, TextView dateView,
 			TextView weekView) {
 		mParentActivity = parent;
 		mTimeView = timeView;
 		mDateView = dateView;
 		mWeekView = weekView;

//		mLunarDateIsSet = false;
 		
 		mTextNongLi = mParentActivity.getResources().getString(R.string.celander_text_nongli);
 		mLunar = new GDCalendarGB(mParentActivity);
 	}
 	
 	public void setExit(boolean exit) {
 		synchronized (mUpdateLock) {
 			mExit = exit;
 		}
 	}
 	
 	private boolean checkExit() {
 		synchronized (mUpdateLock) {
 			return mExit;
 		}
 	}
 
 	public void setUpdate(boolean update) {
 
 		synchronized (mUpdateLock) {
 			mUpdate = update;
 
 			if (mUpdate) {
 				mUpdateLock.notify();
 			}
 		}
 	}
 
 	private boolean upDate() {
 		synchronized (mUpdateLock) {
 			return mUpdate;
 		}
 	}
 
 	public void run() {
 		
 //		Log.d(TAG, "Begin Run!");
 		
 		while (!checkExit()) {
 			try {
 				if (upDate()) {
 					doWork();
 					Thread.sleep(INTERVAL_IN_MINUTES);
 				} else {
 					synchronized (mUpdateLock) {
 						mUpdateLock.wait();
 					}
 
 					Log.d(TAG, "CelanderThread wait");
 				}
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 //		Log.d(TAG, "Exit!");
 	}
 
 	public void doWork() {
 
 //		Log.d(TAG, "doWork get focus = " + mParentActivity.hasWindowFocus());
 
 		mParentActivity.runOnUiThread(new Runnable() {
 			public void run() {
 				try {
 					Calendar c = Calendar.getInstance();
 					int hours = c.get(Calendar.HOUR_OF_DAY);
 					int minutes = c.get(Calendar.MINUTE);
 					String curTime = "";
 					if (hours < 10) {
 						curTime = "0" + hours + ":";
 					} else {
 						curTime = hours + ":";
 					}
 					if (minutes < 10) {
 						curTime = curTime + "0" + minutes;
 					} else {
 						curTime = curTime + minutes;
 					}
 
 					mTimeView.setText(curTime);
 
 					int year = c.get(Calendar.YEAR);
 					int month = getMonthNumber(c.get(Calendar.MONTH));
 					int numDay = c.get(Calendar.DAY_OF_MONTH);
 					String numDate = year + "/" + month + "/" + numDay;
					mDateView.setText(numDate);
					
 					Locale locale = Locale.getDefault();
 					if (!locale.equals(Locale.CHINA)) {
 						locale = Locale.CHINA;
 					}
 					String strWeek = c.getDisplayName(Calendar.DAY_OF_WEEK,
 							Calendar.LONG, locale);
 
//					mWeekView.setText(strWeek);
 
 					// if (!mLunarDateIsSet || hours % 12 == 0)
 					{
 						// we only calculate the lunar date every 12
 						// hours
 //						mLunarDateIsSet = true;
 
 						mLunar.setGregorian(year, month, numDay);
 						mLunar.computeChineseFields();
 						mLunar.computeSolarTerms();
						
//						String strDate = strWeek + " " + mTextNongLi + mLunar.getLunarDate();
//						mDateView.setText(strDate);
						strWeek = mTextNongLi + mLunar.getLunarDate() + " " + strWeek;
						mWeekView.setText(strWeek);
 					}
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 	}
 
 	private int getMonthNumber(int cMonth) {
 		int month = 0;
 		switch (cMonth) {
 		case Calendar.JANUARY:
 			month = 1;
 			break;
 		case Calendar.FEBRUARY:
 			month = 2;
 			break;
 		case Calendar.MARCH:
 			month = 3;
 			break;
 		case Calendar.APRIL:
 			month = 4;
 			break;
 		case Calendar.MAY:
 			month = 5;
 			break;
 		case Calendar.JUNE:
 			month = 6;
 			break;
 		case Calendar.JULY:
 			month = 7;
 			break;
 		case Calendar.AUGUST:
 			month = 8;
 			break;
 		case Calendar.SEPTEMBER:
 			month = 9;
 			break;
 		case Calendar.OCTOBER:
 			month = 10;
 			break;
 		case Calendar.NOVEMBER:
 			month = 11;
 			break;
 		case Calendar.DECEMBER:
 			month = 12;
 			break;
 		default:
 			break;
 		}
 
 		return month;
 	}
 }
