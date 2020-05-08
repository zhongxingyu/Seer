 package com.example.swp_ucd_2013_eule.car_data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 import com.example.swp_ucd_2013_eule.data.SettingsWrapper;
 import com.example.swp_ucd_2013_eule.model.APIModel;
 import com.example.swp_ucd_2013_eule.model.Forest;
 import com.example.swp_ucd_2013_eule.model.MyForest;
 import com.example.swp_ucd_2013_eule.model.Statistic;
 
 /**
  * 
  * @author Marc
  * 
  *         CarDataLogic retrieves data from the CarData listener and calculates
  *         points according to the driving style. Therefore it can assign bad
  *         and good Points which will influence the amount of m² an user has.
  * 
  */
 public class CarDataLogic extends Handler {
 
 	private static CarDataLogic INSTANCE = new CarDataLogic();
 	private volatile int mShifts = 0; // positiv gut, negativ schlecht
 	private int mMaxRPM = 0; // innerhalb der ermittelten Zeit
 	private volatile int mShiftCount = 0;
 	private ArrayList<Float> mCurrentConsumptions = new ArrayList<Float>();
 	private ArrayList<Float> mCurrentSpeed = new ArrayList<Float>();
 	private volatile boolean mFastAcceleration = false;
 	private volatile boolean mHardBreaking = false;
 	private int mInterval = 75;
 	private HashMap<String, List<Handler>> mDataListeners = new HashMap<String, List<Handler>>();
 	private float mPointsScaleFactor = 2;
 	private float mCurrentRPM = 0;
 	private int[] mRPMExceeding = { 0, 0, 0, 0 };
 	private int mCurGear;
 	private int mGoodShifts = 0;
 	private float mMaxAcc = 0;
 	private float mMaxBreak = 0;
 	private int[] mAccExceeding = { 0, 0, 0 };
 	private Statistic mStatistics;
 	private APIModel<Statistic, Statistic> mAPI;
 	private int mProgressPointInterval = 100;
 	private boolean mRecordTrip = false;
 
 	private volatile float mCurPoints;
 
 	// for now static
 	private float mCity = 7.5f;
 	private float mCountry = 5.8f;
 	private float mMotorWay = 5.2f;
 	private float m5percent = 0.1f;
 	private float m10percent = 0.2f;
 	private float m15percent = 0.3f;
 	private float m20percent = 0.4f;
 
 	private Forest mForest;
 
 	private CarDataLogic() {
 		CarData carDataListener = CarData.getInstance();
 
 		carDataListener.subscribeHandler(this, "InstantaneousValuePerMilage");
 		carDataListener.subscribeHandler(this, "EngineSpeed");
 		carDataListener.subscribeHandler(this, "CurrentGear");
 		// instance.subscribeHandler(this, "RecommendedGear");
 		carDataListener.subscribeHandler(this, "VehicleSpeed");
 		carDataListener.subscribeHandler(this, "LongitudinalAcceleration");
 	}
 
 	public void setForest(Forest forest) {
 		mForest = forest;
 		// mStatistics = new Statistic(mForest.getId());
 		mCurPoints = mForest.getPointProgress() / mPointsScaleFactor;
 	}
 
 	public void setTripStartStop(boolean state) {
 		if (mRecordTrip && !state) {
 			mStatistics.calculateTripConsumption();
 			MyForest.getInstance().addStatistic(mStatistics);
 		} else if (!mRecordTrip && state) {
 			mStatistics = new Statistic(mForest.getId());
 		}
 		mRecordTrip = state;
 	}
 
 	public static CarDataLogic getInstance() {
 		return INSTANCE;
 	}
 
 	public void handleMessage(Message msg) {
 		Bundle data = msg.getData();
 		if (mRecordTrip) {
 			if (data.containsKey("InstantaneousValuePerMilage")) {
 				try {
 					mCurrentConsumptions.add(Float.valueOf(data
 							.getString("InstantaneousValuePerMilage")));
 					Log.d("CarDataLogic",
 							"Verbrauch: "
 									+ data.getString("InstantaneousValuePerMilage"));
 				} catch (NumberFormatException e) {
 					Log.w("CarDataLogic",
 							"InstantaneousValuePerMilage: " + e.getMessage());
 				}
 			} else if (data.containsKey("VehicleSpeed")) {
 				try {
 					mCurrentSpeed.add(Float.valueOf(data
 							.getString("VehicleSpeed")));
 					Log.d("CarDataLogic",
 							"Geschwindigkeit: "
 									+ data.getString("VehicleSpeed"));
 				} catch (NumberFormatException e) {
 					Log.w("CarDataLogic", "VehicleSpeed: " + e.getMessage());
 				}
 			} else if (data.containsKey("EngineSpeed")) {
 				Log.d("CarDataLogic",
 						"EngineSpeed: " + data.getString("EngineSpeed"));
 
 				try {
 					mCurrentRPM = Float.parseFloat(data
 							.getString("EngineSpeed"));
 				} catch (NumberFormatException e) {
 					Log.w("CarDataLogic", "EngineSpeed: " + e.getMessage());
 				}
 
 				if (mCurrentRPM > 4000) {
 					mRPMExceeding[3]++;
 				} else if (mCurrentRPM > 3000) {
 					mRPMExceeding[2]++;
 				} else if (mCurrentRPM > 2000) {
 					mRPMExceeding[1]++;
 				} else {
 					mRPMExceeding[0]++;
 				}
 
 			} else if (data.containsKey("CurrentGear")) {
 				Log.d("CarDataLogic", "Gear: " + data.getString("CurrentGear"));
 				int oldGear = mCurGear;
 				try {
 					mCurGear = Integer.parseInt(data.getString("CurrentGear"));
 				} catch (NumberFormatException e) {
 					Log.w("CarDataLogic", "Gear: " + e.getMessage());
 				}
 				if (oldGear < mCurGear) {
 					if (1600 < mCurrentRPM && mCurrentRPM < 2000) {
 						mGoodShifts++;
 					} else {
 						mGoodShifts--;
 					}
 				}
 
 			} else if (data.containsKey("LongitudinalAcceleration")) {
 				try {
 					// value can be -20 to +20,
 					// a normal car accelerates with 1.5 and max at 3
 					// a normal car breaks with -3 and max at -10
 					float acc = (Float.parseFloat(data
 							.getString("LongitudinalAcceleration")));
 					if (acc < 0) {
 						if (acc < mMaxBreak) {
 							mMaxBreak = acc;
 						}
 						if (acc < -4) {
 							mAccExceeding[1]++;
 						} else {
 							mAccExceeding[2]++;
 						}
 					} else {
 						if (mMaxAcc < acc) {
 							mMaxAcc = acc;
 						}
 						if (1.8 < acc) {
 							mAccExceeding[0]++;
 						} else {
 							mAccExceeding[2]++;
 						}
 					}
 				} catch (NumberFormatException e) {
 					Log.w("CarDataLogic", "Acceleration: " + e.getMessage());
 				}
 			}
 
 			if (mCurrentConsumptions.size() >= mInterval
 					&& mCurrentSpeed.size() >= mInterval) {
 				calculatePoints();
 			}
 		}
 
 	}
 
 	private void calculatePoints() {
 		/*
 		 * Bewertung anhand der angegebenen Durchschnittsverbrauchwerte z.b.
 		 * Golf 7 1.4TSI ACT S=5.8 /L=4.2 /A=4.7 6 Gang Schaltgetriebe
 		 * Zeiteinheit definieren: 30 Sekunden ~ 150 Messwerte (alle 200ms)
 		 * Durchschnitt des Momentanverbrauchs (ist schon auf l/100km normiert)
 		 * Durchschnitt der Fahrgeschwindigt => Berechnen ob Stadt/Land/Autobahn
 		 * Penaltyflag checken f�r Beschleunigung/Verz�gerung gr��er als GW
 		 * errechneten Durchschnitsverbrauch f�r diese Fahrt in Klasse einordnen
 		 * 20% drunter 10% drunter 0 10% dr�ber 20%dr�ber etc Schaltpunkte
 		 * bewerten Drehzahl �berschreitungen bewerten
 		 */
 		ArrayList<Float> listConsum = new ArrayList<Float>();
 		ArrayList<Float> listSpeed = new ArrayList<Float>();
 		synchronized (mCurrentConsumptions) {
 			listConsum.addAll(mCurrentConsumptions);
 		}
 		synchronized (mCurrentSpeed) {
 			listSpeed.addAll(mCurrentSpeed);
 		}
 		int[] rpm = new int[] { mRPMExceeding[0], mRPMExceeding[1],
 				mRPMExceeding[2], mRPMExceeding[3] };
 		int[] acc = new int[] { mAccExceeding[0], mAccExceeding[1],
 				mAccExceeding[2] };
 		Thread thread = new CalculationThread(listConsum, listSpeed, rpm,
 				mGoodShifts, mMaxAcc, mMaxBreak, acc);
 		thread.start();
 
 		resetVariables();
 
 	}
 
 	private void resetVariables() {
 		mGoodShifts = 0;
 		mRPMExceeding = new int[] { 0, 0, 0, 0 };
 		mCurrentConsumptions.clear();
 		mCurrentConsumptions.clear();
 		mMaxAcc = 0;
 		mMaxBreak = 0;
 		mAccExceeding = new int[] { 0, 0, 0 };
 	}
 
 	/**
 	 * 
 	 * @param handler
 	 *            which will be notified at a data event
 	 * @param identifier
 	 *            for the data to subscribe
 	 * @return false if the listener is allready known for the given identifier
 	 */
 	public boolean subscribeHandler(Handler handler, String identifier) {
 		List<Handler> list = mDataListeners.get(identifier);
 		if (list == null) {
 			list = new ArrayList<Handler>();
 		} else if (list.contains(handler)) {
 			return false;
 		}
 		list.add(handler);
 		mDataListeners.put(identifier, list);
 		return true;
 
 	}
 
 	private class CalculationThread extends Thread {
 		private List<Float> mConsumptions;
 		private List<Float> mSpeedList;
 		private float mConsumption = 0f;
 		private float mSpeed = 0f;
 		private float mReferenceConsumption;
 		private int[] mRPM;
 		private int mGShifts;
 		private float mMAcc;
 		private float mMBreak;
 		private int[] mAcc;
 
 		public CalculationThread(List<Float> consumps, List<Float> speed,
 				int[] rpm, int goodShifts, float maxAcc, float maxBreak,
 				int[] acc) {
 			mConsumptions = consumps;
 			mSpeedList = speed;
 			mRPM = rpm;
 			mGShifts = goodShifts;
 			mMAcc = maxAcc;
 			mMBreak = maxBreak;
 			mAcc = acc;
 		}
 
 		@Override
 		public void run() {
 
 			if (!mConsumptions.isEmpty() || !mSpeedList.isEmpty()) {
 				calcConsumption();
 			}
 			if (mRPM[0] != 0 || mRPM[1] != 0 || mRPM[2] != 0 || mRPM[3] != 0) {
 				calcRPM();
 			}
 			calcShift();
 
 			if (mAcc[0] != 0 || mAcc[1] != 0 || mAcc[2] != 0) {
 				calcAcc();
 			}
 
 			checkLevel();
 
 			float curPoints = mCurPoints * mPointsScaleFactor;
 			List<Handler> handlers = mDataListeners.get("pointProgress");
 			if (handlers != null) {
 				for (Handler handler : handlers) {
 					Message msg = handler.obtainMessage();
 					Bundle bundleData = new Bundle();
 					bundleData.putFloat("pointProgress", curPoints);
 					msg.setData(bundleData);
 					msg.sendToTarget();
 				}
 			}
 			mForest.setPointProgress(curPoints);
 			Log.d("CarDataLogic", "CurPoints: " + curPoints);
 
 		}
 
 		private void calcConsumption() {
 			// calculate average consumption and speed
 			for (int i = 0; i < mInterval; i++) {
 				mSpeed += mSpeedList.get(i);
 				mConsumption += mConsumptions.get(i);
 			}
 			mConsumption = mConsumption / mInterval;
 			mSpeed = mSpeed / mInterval;
 			// determine the driving conditions
 			if (mSpeed < 60) {
 				mReferenceConsumption = mCity;
 			} else if (mSpeed < 90) {
 				mReferenceConsumption = mCountry;
 			} else {
 				mReferenceConsumption = mMotorWay;
 			}
 			// according to the driving conditions determine if the consumption
 			// was above or below car average
 			float delta = mReferenceConsumption - mConsumption;
 			mStatistics.setConsumption(mConsumption);
 			float percent = Math.abs(delta) / mReferenceConsumption;
 			float factor = m20percent;
 			if (percent < 0.05f) {
 				factor = m5percent;
 			} else if (percent < 0.1f) {
 				factor = m10percent;
 			} else if (percent < 0.15f) {
 				factor = m15percent;
 			}
 
 			if (delta > 0f) {
 				// +Points
 				mCurPoints += 1 * factor;
 			} else {
 				// -Points
 				mCurPoints -= 1 * factor;
 			}
 
 		}
 
 		private void calcRPM() {
 			// calculate RPM exceeding penalty or bonus
 			int interval = mRPM[0] + mRPM[1] + mRPM[2] + mRPM[3];
 			// rpm under 2000
 			mCurPoints += ((float) mRPM[0] / interval) * 2;
 			// rpm under 3000
 			mCurPoints -= ((float) mRPM[1] / interval) * 1;
 			// rpm under 4000
 			mCurPoints -= ((float) mRPM[2] / interval) * 4;
 			// rpm above 4000
 			mCurPoints -= ((float) mRPM[3] / interval) * 8;
 
 		}
 
 		private void calcShift() {
 			// calculate bad shift penalty and good shift bonus
 			if (mGShifts < 0) {
 				mCurPoints += (mGShifts * 0.5);
 			} else {
 				mCurPoints += (mGShifts * 0.4);
 			}
 
 		}
 
 		private void calcAcc() {
 			// calculate acceleration and breaking penalty/bonus
 			int interval = mAcc[0] + mAcc[1] + mAcc[2];
 			// fast acc
 			mCurPoints -= ((float) mAcc[0] / interval) * 6;
 			// hard breaking
 			mCurPoints -= ((float) mAcc[1] / interval) * 2;
 			// acc in range
 			mCurPoints += ((float) mAcc[2] / interval) * 4;
 			Log.d("CarDataLogic", "MaxAcc: " + mMAcc);
 			Log.d("CarDataLogic", "MaxBreak: " + mMBreak);
 
 		}
 
 		private void checkLevel() {
 			SettingsWrapper settings = SettingsWrapper.getInstance();
 			boolean viewChanged = false;
 			if (mCurPoints * mPointsScaleFactor > mProgressPointInterval) {
 				viewChanged = true;
 				mForest.setPoints(mForest.getPoints() + 5);
 				mCurPoints = mCurPoints * mPointsScaleFactor
 						- mProgressPointInterval;
 				int level = mForest.getLevel();
 				int lvlPrgPoints = mForest.getLevelProgessPoints() + 1;
 				// level up
 				if (lvlPrgPoints >= settings.getPointsToNextLevel(level + 1)
 						&& level <= 100) {
 					mStatistics.addGainedPoint();
 					// calculate new progresPoints = curPoints -
 					// levelNeededPoints
 					lvlPrgPoints = 0;
 					// increment level in Forest
 					mForest.setLevel(++level);
 				}
 				// update lvlPrgPoints in Forest
 				mForest.setLevelProgessPoints(lvlPrgPoints);
 
 			} else if (mCurPoints * mPointsScaleFactor < -mProgressPointInterval) {
 				viewChanged = true;
 				mCurPoints = mCurPoints * mPointsScaleFactor
 						+ mProgressPointInterval;
 				int level = mForest.getLevel();
 				int lvlPrgPoints = mForest.getLevelProgessPoints() - 1;
 				// level down
 				if (lvlPrgPoints < 0 && level > 1) {
 					mStatistics.removeGainedPoint();
 					// calculate new progressPoints = levelNeededpoints -
 					// curPoints
 					lvlPrgPoints = settings.getPointsToNextLevel(level) - 1;
 					mForest.setLevel(--level);
 				}
 				// update lvlPrgPoints in Forest
 				mForest.setLevelProgessPoints(lvlPrgPoints);
 			}
 			if (viewChanged) {
 				List<Handler> handlers = mDataListeners.get("viewChanged");
 				if (handlers != null) {
 					for (Handler handler : handlers) {
 						Message msg = handler.obtainMessage();
 						Bundle bundleData = new Bundle();
 						bundleData.putString("viewChanged", "");
 						msg.setData(bundleData);
 						msg.sendToTarget();
 					}
 				}
 
 			}
 
 		}
 
 	}
 
 	public boolean unSubscribeHandler(Handler handler, String key) {
 		List<Handler> handlerList = mDataListeners.get(key);
 		if (handlerList != null) {
 			return handlerList.remove(handler);
 		} else {
 			return false;
 		}
 
 	}
 
 }
