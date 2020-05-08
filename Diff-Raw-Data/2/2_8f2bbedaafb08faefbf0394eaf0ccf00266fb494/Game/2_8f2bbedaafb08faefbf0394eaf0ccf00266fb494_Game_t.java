 package com.example.seamerchant.game;
 
 import java.util.Random;
 
 /**
  * Controls game process, send events about change of the game state
  * @author alex
  *
  */
 public class Game {
 	private static final int LAST_DAY = 7;
	private int mCurrentDay = 1;
 	private Player mPlayer;
 	private Location mLocIsrael;
 	private Location mLocTurkey;
 	private Location mLocEgypt;
 	private Weather mWeather;
 	
 	private int mStormyWeather = 0;
 
 	private OnGameChangeListener mListener;
 	
 	public interface OnGameChangeListener {
 		void onPiratesAttack();
 		void onGameFinish();
 		//TODO: more
 		
 	}
 	
 	public Game() {
 		mPlayer = new Player(0, Location.ISRAEL);
 		mLocIsrael = new Location(Location.ISRAEL);
 		mLocTurkey = new Location(Location.TURKEY);
 		mLocEgypt = new Location(Location.EGYPT);
 		mWeather = new Weather();
 		init();
 	}
 	
 	private void init() {
 		setWeather();
 		setPrices();
 	}
 
 	public Player getPlayer() {
 		return mPlayer;
 	}
 	
 	public int getCurrentWeather(int locationID) {
 		switch (locationID) {
 			case Location.ISRAEL:
 				return mLocIsrael.getWeather();
 			case Location.EGYPT:
 				return mLocEgypt.getWeather();
 			case Location.TURKEY:
 				return mLocTurkey.getWeather();
 			default:
 				break;
 		}
 		return Weather.CALM;
 	}
 	
 	/**
 	 * TODO: Check what it means.
 	 */
 	private void setWeather()
 	{
 		mWeather.makeWeather();
 		mLocIsrael.setWeather(new Weather());
 		mLocEgypt.setWeather(new Weather());
 		mLocTurkey.setWeather(new Weather());
 		if (mWeather.getCurrentWeather() == Weather.STORM) {
 			Random rand = new Random();
 			switch (rand.nextInt(3)) {
 			case 0:
 				mLocIsrael.setWeather(mWeather);
 				setStormyWeather(Location.ISRAEL);
 				break;
 			case 1:
 				mLocEgypt.setWeather(mWeather);
 				setStormyWeather(Location.EGYPT);
 				break;
 			case 2:
 				mLocTurkey.setWeather(mWeather);
 				setStormyWeather(Location.TURKEY);
 				break;
 			default:
 				setStormyWeather(0);
 				break;
 			}
 		}else {
 			setStormyWeather(0);
 		}
 	}
 	public void setGameChangeListener(OnGameChangeListener listener) {
 		mListener = listener;
 	}
 
 	public int getCurrentDay() {
 		return mCurrentDay;
 	}
 	
 	public void nextDay() {
 		if (mCurrentDay == LAST_DAY) {
 			finish();
 			return;
 		}
 		mCurrentDay++;
 		setWeather();
 		setPrices();
 	}
 
 	private void setPrices() {
 		mLocIsrael.setPrices();
 		mLocEgypt.setPrices();
 		mLocTurkey.setPrices();
 	}
 
 	private void finish() {
 		mListener.onGameFinish();
 	}
 
 	public Location getLocation(int location) {
 		switch (location) {
 		case Location.ISRAEL:
 			return mLocIsrael;
 		case Location.EGYPT:
 			return mLocEgypt;
 		case Location.TURKEY:
 			return mLocTurkey;
 		default:
 			break;
 		}
 		return null;
 	}
 
 	public int getStormyWeather() {
 		return mStormyWeather;
 	}
 
 	public void setStormyWeather(int mStormyWeather) {
 		this.mStormyWeather = mStormyWeather;
 	}
 }
