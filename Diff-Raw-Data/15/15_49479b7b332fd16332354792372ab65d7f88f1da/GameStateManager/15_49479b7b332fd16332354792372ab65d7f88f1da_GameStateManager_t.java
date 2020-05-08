 package com.PsichiX.JustIDS;
 
 import android.content.Context;
 import android.content.IntentSender.SendIntentException;
 import android.provider.Settings.Secure;
 import android.util.Log;
 
 public class GameStateManager {
 	
 	private boolean DEBUG_SELF_SENDING = false;
 	
 	private volatile double lifePointsOfMine;
 	private volatile double lifePointsOfOther;
 	
 	public double getLifePointsOfMine() {
 		return lifePointsOfMine;
 	}
 	
 	public synchronized boolean isLost() {
 		return lifePointsOfMine <= 0.01;
 	}
 
 	public synchronized boolean isWon() {
 		return lifePointsOfOther <= 0.01;
 	}
 
 	BroadCastManager bcm;
 	private String android_id;
 	private Context context;
 	private Thread thread;
 	private Runnable onSomethingChanged;
 	private Runnable hitListener;
 
 	private VibratorUtil vibratorUtil;
 
 	private Runnable wonListener;
 
 	private Runnable lostListener;

	private boolean inGame = false;
 	
 	public GameStateManager(Context context, BroadCastManager bcm) {
 		this.bcm = bcm;
 		this.android_id = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID); 
 		this.context = context;
 		this.vibratorUtil = new VibratorUtil(context);
 		resetGame();
 		this.thread = new Thread() {
 			@Override
 			public void run() {
 				Log.i("INFO", "Starting listening for messages");
 				while (true) {
 					final byte[] message = GameStateManager.this.bcm.receiveBroadCast(GameStateManager.this.context);
 					PlayerInfo pi = (PlayerInfo) Serializer.deserialize(message);
					if (!inGame) {
						continue;
					}
 					if (shouldICare(pi)) {
 						Log.i("MSG","Received message from: " + pi);
 						lifePointsOfOther = pi.lifePoints;
 						if (isAttackSuccessfull(pi)) {
 							decreaseLifePointsOfMine(pi.attackStrength);							
 							if (hitListener != null) {
 								hitListener.run();
 							}
 							vibrateOnHit();
 						}						
 					} else {
 						Log.i("MSG", "Skipping message : " + pi);
 					}
 					if (isWon()) {
						inGame = false;
 						vibrateOnWon();
 						if (wonListener != null) {
 							wonListener.run();
 						}
 					} else {
 						if (GameStateManager.this.onSomethingChanged != null){
 							GameStateManager.this.onSomethingChanged.run();
 						}
 						if (isLost()) {
							inGame = false;
 							iLostTheGame();								
 							vibrateOnLost();
 							if (lostListener != null) {
 								lostListener.run();
 							}
 						}
 					}
 				}
 			}
 
 			private boolean shouldICare(PlayerInfo pi) {
 				if (hasFinished()) {
 					return false;
 				}
 				if (DEBUG_SELF_SENDING) {
 					return true;
 				}
 				return !pi.id.equals(android_id);
 			}
 
 		};
 		thread.start();
 	}
 
 	private boolean isAttackSuccessfull(PlayerInfo pi) {
 		return pi.attackStrength > 0.01;
 	}
 
 	private void vibrateOnHit() {
 		vibratorUtil.vibrate(500);
 	}
 
 	private void vibrateOnWon() {
 		vibratorUtil.vibrate(1000);
 	}
 
 	private void vibrateOnLost() {
 		vibratorUtil.vibrate(1000);
 	}
 
 	private synchronized double decreaseInternally(double decreaseBy) {
 		lifePointsOfMine -= decreaseBy;
 		if (lifePointsOfMine < 0.01) {
 			lifePointsOfMine =0.0;
 		}
 		return lifePointsOfMine;
 	}
 	
 	public void decreaseLifePointsOfMine(double decreaseBy) {
 		double lp = decreaseInternally(decreaseBy);
 		PlayerInfo pi = new PlayerInfo();
 		pi.id = android_id; 
 		pi.lifePoints = lp;
 		pi.attackStrength = 0;
 		this.bcm.sendBroadcast(context, Serializer.serialize(pi));
 	}
 	
 	public boolean hasFinished() {
 		return (isLost() || isWon());
 	}
 	
 	public void attackWithStrength(double strength) {
 		if (hasFinished()) {
 			// do nothing
 			return;
 		}
 		double lp = decreaseInternally(0.0f);
 		PlayerInfo pi = new PlayerInfo();
 		pi.id = android_id; 
 		pi.lifePoints = lp;
 		pi.attackStrength = strength;
 		this.bcm.sendBroadcast(context, Serializer.serialize(pi));
 	}
 
 	public void iLostTheGame() {
 		PlayerInfo pi = new PlayerInfo();
 		pi.id = android_id; 
 		pi.lifePoints = 0.0;
 		pi.attackStrength = 0.0;
 		this.bcm.sendBroadcast(context, Serializer.serialize(pi));
 	}
 
 	
 	public void setSomethingChangedListener(Runnable runnable) {
 		this.onSomethingChanged = runnable;
 	}
 
 	public void setHitListener(Runnable runnable) {
 		this.hitListener = runnable;
 	}
 
 	
 	public void setWonListener(Runnable runnable) {
 		this.wonListener = runnable;
 	}
 
 	public void setLostListener(Runnable runnable) {
 		this.lostListener = runnable;
 	}
 
 	
 	public void resetGame(){
 		this.lifePointsOfMine = 100.0;
 		this.lifePointsOfOther = 100.0;
 		if (this.onSomethingChanged != null){
 			this.onSomethingChanged.run();
 		}
 		decreaseLifePointsOfMine(0);
		inGame = true;
	}	
 }
