 package com.luzi82.rbmfx;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Properties;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import com.luzi82.libmbgwalpurgis.ICallback;
 import com.luzi82.libmbgwalpurgis.IMwClient;
 import com.luzi82.libmbgwalpurgis.IMwClient.State;
 import com.luzi82.libmbgwalpurgis.MwClient;
 import com.luzi82.libmbgwalpurgis.PlayerStatus;
 import com.luzi82.libmbgwalpurgis.RaidBossMatchingFeed;
 import com.luzi82.libmbgwalpurgis.RaidBossMatchingFeed.Unit;
 import com.luzi82.libmbgwalpurgis.Utils;
 import com.luzi82.libmbgwalpurgis.common.Prop;
 
 public class MwClientMgr {
 
 	final ScheduledThreadPoolExecutor mExecutor;
 	final IMwClient mMwClient;
 	final String mLoginId;
 	final String mPassword;
 	final LinkedList<RaidBossMatchingFeed.Unit> mHistory = new LinkedList<RaidBossMatchingFeed.Unit>();
 
 	long mLastNotify = 0;
 	boolean mLastFullLp = false;
 	boolean mLastFullBp = false;
 	boolean mLastExpUp = false;
 	boolean mLastFullCard = false;
 	public float mLp2Exp = 7.5f;
 	int mMaintainPeriod = 15;
 
 	LinkedList<Long> mPollTime = new LinkedList<Long>();
 
 	public MwClientMgr(ScheduledThreadPoolExecutor aExecutor, String aLoginId, String aPassword) {
 		mExecutor = aExecutor;
 		mMwClient = new MwClient(mExecutor);
 		mLoginId = aLoginId;
 		mPassword = aPassword;
 	}
 
 	ScheduledFuture<?> mMaintainScheduledFuture;
 
 	public synchronized void start() {
 		if (mMaintainScheduledFuture != null)
 			return;
		mMaintainScheduledFuture = mExecutor.scheduleAtFixedRate(new MaintainRunnable(), 0, 15, TimeUnit.SECONDS);
 	}
 
 	public synchronized void stop() {
 		if (mMaintainScheduledFuture == null)
 			return;
 		mMaintainScheduledFuture.cancel(false);
 		mMaintainScheduledFuture = null;
 	}
 
 	public synchronized void setMaintainPeriod(int aSeconds) {
 		mMaintainPeriod = aSeconds;
 		if (mMaintainScheduledFuture != null) {
 			stop();
 			start();
 		}
 	}
 	
 	public int getMaintainPeriod(){
 		return mMaintainPeriod;
 	}
 
 	long mLastRest = 0;
 
 	public synchronized void maintain() {
 		IMwClient.State state = mMwClient.getState();
 		boolean rest = restTime();
 		long now = System.currentTimeMillis();
 		if (rest) {
 			if (now < mLastRest + 60 * 60 * 1000)
 				return;
 			mLastRest = now;
 		} else {
 			mLastRest = 0;
 		}
 		if (state == State.OFFLINE) {
 			markPollTime();
 			mMwClient.connect(mLoginId, mPassword, new MaintainCallback<Void>(), new ExceptionCallback());
 		} else if (state == State.ONLINE) {
 			markPollTime();
 			mMwClient.getFeed(new ICallback<RaidBossMatchingFeed>() {
 				@Override
 				public void callback(RaidBossMatchingFeed aResult) {
 					Iterator<RaidBossMatchingFeed.Unit> itr = aResult.mUnitList.descendingIterator();
 					while (itr.hasNext()) {
 						RaidBossMatchingFeed.Unit u = itr.next();
 						if (!mHistory.contains(u)) {
 							if (mUnitCallback != null) {
 								mUnitCallback.callback(u);
 							}
 							mHistory.addFirst(u);
 						}
 						while (mHistory.size() > 10) {
 							mHistory.removeLast();
 						}
 					}
 				}
 			}, new ExceptionCallback());
 			if (!rest) {
 				markPollTime();
 				mMwClient.getStatus(new ICallback<PlayerStatus>() {
 					@Override
 					public void callback(PlayerStatus aResult) {
 						mLastPlayerStatus = aResult;
 
 						boolean fullLp = aResult.mLp >= aResult.mLpMax;
 						boolean fullBp = aResult.mBp >= aResult.mBpMax;
 						boolean expUp = aResult.mLp * mLp2Exp >= aResult.mExpToUp;
 						boolean fullCard = aResult.mCard >= aResult.mCardMax - 5;
 						long now = System.currentTimeMillis();
 
 						boolean notify = fullLp || fullBp || expUp || fullCard;
 						boolean notifyNow = false;
 						notifyNow = notifyNow || (fullLp && (!mLastFullLp));
 						notifyNow = notifyNow || (fullBp && (!mLastFullBp));
 						notifyNow = notifyNow || (expUp && (!mLastExpUp));
 						notifyNow = notifyNow || (fullCard && (!mLastFullCard));
 						notifyNow = notifyNow || (notify && (now >= mLastNotify + 5 * 60 * 1000));
 						// notifyNow = true;
 
 						mLastFullLp = fullLp;
 						mLastFullBp = fullBp;
 						mLastExpUp = expUp;
 						mLastFullCard = fullCard;
 
 						if (notifyNow) {
 							if (mPlayerStatusCallback != null) {
 								mLastNotify = now;
 								// mPlayerStatusCallback.callback(aResult);
 								Utils.startCallback(mPlayerStatusCallback, aResult, mExecutor);
 							}
 						}
 
 					}
 				}, new ExceptionCallback());
 			}
 		}
 	}
 
 	PlayerStatus mLastPlayerStatus;
 
 	public PlayerStatus getLastPlayerStatus() {
 		return mLastPlayerStatus;
 	}
 
 	ICallback<PlayerStatus> mPlayerStatusCallback;
 
 	public void setPlayerStatusCallback(ICallback<PlayerStatus> aCallback) {
 		mPlayerStatusCallback = aCallback;
 	}
 
 	ICallback<RaidBossMatchingFeed.Unit> mUnitCallback;
 
 	public void setUnitCallback(ICallback<RaidBossMatchingFeed.Unit> aCallback) {
 		mUnitCallback = aCallback;
 	}
 
 	public class MaintainRunnable implements Runnable {
 		@Override
 		public void run() {
 			maintain();
 		}
 	}
 
 	public class MaintainCallback<T> implements ICallback<T> {
 		@Override
 		public void callback(T aResult) {
 			maintain();
 		}
 	}
 
 	public class ExceptionCallback implements ICallback<Exception> {
 		@Override
 		public void callback(Exception aResult) {
 			aResult.printStackTrace();
 			if (mExceptionListener != null) {
 				mExceptionListener.callback(aResult);
 			}
 			maintain();
 		}
 	}
 
 	public boolean restTime() {
 		// GregorianCalendar gc = new GregorianCalendar();
 		// gc.setTimeZone(TimeZone.getTimeZone("GMT+09:00"));
 		// int t = gc.get(GregorianCalendar.HOUR_OF_DAY) * 100 +
 		// gc.get(GregorianCalendar.MINUTE);
 		// return ((t >= 0105) && (t <= 0455));
 		return false;
 	}
 
 	ICallback<Exception> mExceptionListener = null;
 
 	public void setExceptionListener(ICallback<Exception> aExceptionListener) {
 		mExceptionListener = aExceptionListener;
 	}
 
 	public int getPollFreq() {
 		synchronized (mPollTime) {
 			trimPollTime();
 			return mPollTime.size();
 		}
 	}
 
 	public void markPollTime() {
 		long now = System.currentTimeMillis();
 		synchronized (mPollTime) {
 			mPollTime.addLast(now);
 			trimPollTime();
 		}
 	}
 
 	public void trimPollTime() {
 		synchronized (mPollTime) {
 			LinkedList<Long> rmList = new LinkedList<Long>();
 			long now = System.currentTimeMillis();
 			long limit = now - 60 * 1000;
 			for (long l : mPollTime) {
 				if (l < limit) {
 					rmList.add(l);
 				}
 			}
 			mPollTime.removeAll(rmList);
 		}
 	}
 
 	public void burnBronze(ICallback<Void> aCallback, ICallback<Exception> aExceptionCallback) {
 		mMwClient.burnBronze(aCallback, aExceptionCallback);
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Properties props = Prop.getAuthProperties();
 
 			MwClientMgr main = new MwClientMgr(new ScheduledThreadPoolExecutor(10), props.getProperty("login_id"), props.getProperty("login_pw"));
 			main.setUnitCallback(new ICallback<RaidBossMatchingFeed.Unit>() {
 				@Override
 				public void callback(Unit aResult) {
 					aResult.trace();
 				}
 			});
 			main.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
