 package com.konreu.android.wagz;
 
 import android.content.Context;
 import android.util.Log;
 
 public class Dog {
 	private static String TAG = "Dog";
 	private static Dog instance;
 	
 	private int NUM_MINS_BEFORE_CLEARING_HAPPINESS = 720;	// give them 12 hours
 	
 	private double PREF_MIN_HAPPINESS_FOR_LOYALTY_GAIN;
 	private long PREF_DESIRED_WALK_DURATION;
 	
 	private int PREF_DESIRED_WALK_FREQUENCY;
 	private final double GRACE_MULTIPLIER = 1.5;
 	
 	private double mHappiness;
 	private int mLoyalty;	
 	
 	private long mLastWalkedDate;
 	
 	private boolean mHasUpdatedLoyaltyRecently;
 	
 	private AppState mAppState;
 	
 	private Dog(Context c) {
 		// Get all of our values from the AppState and PedometerSettings ....
 		mAppState = AppState.getInstance(c);
 		
 		PREF_MIN_HAPPINESS_FOR_LOYALTY_GAIN = (double) PedometerSettings.getInstance(c).getWalkPercentComplete() / 100.0;
 		PREF_DESIRED_WALK_FREQUENCY = PedometerSettings.getInstance(c).getWalkFrequency();
 		PREF_DESIRED_WALK_DURATION = PedometerSettings.getInstance(c).getWalkLength();
 		
 		NUM_MINS_BEFORE_CLEARING_HAPPINESS = PREF_DESIRED_WALK_FREQUENCY / 2;	// give them half as long as their frequency
 
 		mHappiness = mAppState.getElapsedTime();
 		mLoyalty = mAppState.getLoyalty();
 		mLastWalkedDate = mAppState.getLastWalkDate();
 		
 		mHasUpdatedLoyaltyRecently = false;
 	}
 	
 	public boolean lostHappinessOnStartup() {
 		boolean bLostHappiness = false;
 		String sTAG = TAG + ".updateHappinessOnStartup";
 		
 		if (getNumMinsSinceLastActivity() > NUM_MINS_BEFORE_CLEARING_HAPPINESS) {
 			Log.i(sTAG, "It has been too long since their last activity. Clearing happiness.");
 			updateHappiness(0);
 			bLostHappiness = true;
 		} else {
 			Log.i(sTAG, "It has not been too long since their last activity. Persisting their happiness.");
 		}
 		
 		return bLostHappiness;
 	}
 	
 	public boolean lostLoyaltyOnStartup() {
 		boolean bLostLoyalty = false;
 		String sTAG = TAG + ".updateLoyaltyOnStartup";	
 		
 		if (getNumMinsSinceLastActivity() > getMaxMinsSinceLastActivity()) {
 			Log.i(sTAG, "They are over their grace period. Going to lose loyalty points");
 			setLoyalty(mLoyalty - getNumHeartsToLose());
 			bLostLoyalty = true;
 		} else {
 			Log.i(sTAG, "They are still within their grace period. They will not lose any loyalty");
 		}
 		
 		return bLostLoyalty;
 	}
 	
 	private double getMaxMinsSinceLastActivity() {
 		String sTAG = TAG + "getMaxMinsSinceLastActivity";
 		// give them a grace window
 		double dblMaxMinsSinceLastActivity = (double) (PREF_DESIRED_WALK_FREQUENCY * GRACE_MULTIPLIER);
 		Log.v(sTAG, "PREF_DESIRED_WALK_FREQUENCY: " + PREF_DESIRED_WALK_FREQUENCY);
 		Log.v(sTAG, "GRACE_MULTIPLIER: " + GRACE_MULTIPLIER);
 		Log.v(sTAG, "dblMaxMinsSinceLastActivity: " + dblMaxMinsSinceLastActivity);
 		return dblMaxMinsSinceLastActivity;
 	}
 	
 	private void setLoyalty(int iNewLoyalty) {
 		String sTAG = TAG + "setLoyalty";
 		Log.v(sTAG, "before loyalty: " + mLoyalty);
 		if (iNewLoyalty < 1) {
 			// Can't have anything below 1!
 			Log.i(sTAG, "was going to lose more loyalty than they had... keeping at 1");
 			iNewLoyalty = 1;
 		}
 		mLoyalty = iNewLoyalty;
 		// Save this new loyalty value
 		mAppState.setLoyalty(mLoyalty);
 	}
 	
 	private int getNumHeartsToLose() {
 		// How long has it been since you last walked ?
 		// How often were you supposed to walk ?
 		// If it has been 3.5 times as long as it should have been since your last walk, you're going to lose 3 hearts !
 		return (int) Math.floor(getNumMinsSinceLastActivity() / PREF_DESIRED_WALK_FREQUENCY);
 	}
 
 	private double getNumMinsSinceLastActivity() {
 		long elapsedMS = System.currentTimeMillis() - mLastWalkedDate;
 		double dblNumMins = (double) (elapsedMS / 60000.0);
 		Log.i(TAG + "getNumMinsSinceLastActivity", Double.toString(dblNumMins));
 		return dblNumMins;
 	}	
 	
 	public static Dog getInstance(Context c) {
 		if (instance == null) {
 			instance = new Dog(c);
 		}
 		return instance;
 	}
 	
 	public static void resetInstance(Context c) {
 		instance = new Dog(c);
 	}
 	
 	public int getLoyalty() {
 		return mLoyalty;
 	}
 	
 	public int getHappiness() {
 		int iHappinessPercentage = 0;
 		
     	if (mHappiness > 1) {
     		iHappinessPercentage = 100;
     	} else if (mHappiness < 0) {
     		iHappinessPercentage = 0;
     	} else {
     		iHappinessPercentage = (int)(mHappiness * 100);
     	}
     	
     	Log.v(TAG + ".getHappiness", "percentage: " + iHappinessPercentage);
     	return iHappinessPercentage;
 	}
 	
 	public void updateHappiness(long elapsedTime) {
 		if (elapsedTime <= 0) { elapsedTime = 0; }
 		mHappiness = (float)(elapsedTime / (PREF_DESIRED_WALK_DURATION * 60000.0));
 		Log.v(TAG + ".updateHappiness", "new happiness: " + mHappiness);
 		updateLoyalty();
 	}
     	
 	public void updateLoyalty() {
 		String sTAG = TAG + ".updateLoyalty";
 		
     	Log.v(sTAG, mHappiness + " vs. " + PREF_MIN_HAPPINESS_FOR_LOYALTY_GAIN);
     	
     	if (mHappiness >= PREF_MIN_HAPPINESS_FOR_LOYALTY_GAIN && !mHasUpdatedLoyaltyRecently) {
     		Log.i(sTAG, mHappiness + "% >= " + PREF_MIN_HAPPINESS_FOR_LOYALTY_GAIN + "% and not updated since last activity: updating loyalty from " + mLoyalty + " to " + (mLoyalty+1));
 
     		// Add loyalty !
     		mLoyalty += 1;
     		
     		// We have made our dog happy -- this date will live in infamy !
     		mAppState.setLastWalkDate(System.currentTimeMillis());
     		setLoyalty(mLoyalty);
     		
     		// Make sure we don't keep updating loyalty all day .......
     		mHasUpdatedLoyaltyRecently = true;
     	}
 	}
 	
 }
