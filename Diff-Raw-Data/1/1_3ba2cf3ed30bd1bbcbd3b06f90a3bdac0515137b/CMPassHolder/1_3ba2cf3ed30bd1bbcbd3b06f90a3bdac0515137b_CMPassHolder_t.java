 package com.monster.pocketsafe.main;
 
 import java.math.BigInteger;
 import java.util.Date;
 
 import android.util.Log;
 
 import com.monster.pocketsafe.sec.IMAes;
 import com.monster.pocketsafe.sec.IMBase64;
 import com.monster.pocketsafe.utils.IMLocator;
 import com.monster.pocketsafe.utils.IMTimer;
 import com.monster.pocketsafe.utils.IMTimerObserver;
 import com.monster.pocketsafe.utils.MyException;
 import com.monster.pocketsafe.utils.MyException.TTypMyException;
 
 public class CMPassHolder implements IMPassHolder, IMTimerObserver {
 
 	private IMLocator mLocator;
 	private IMPassHolderObserver mObserver;
 	private String mPass;
 	private String mKey;
 	private IMAes mAes;
 	private IMBase64 mBase64;
 	private long mInterval;
 	private IMTimer mTimer;
 	private Date mTimExpire;
 	
 	public CMPassHolder(IMLocator locator) {
 		mLocator = locator;
 		mAes = mLocator.createAes();
 		mBase64 = mLocator.createBase64();
 		mTimer = mLocator.createTimer();
 		mTimer.SetObserver(this);
 	}
 
 	private void restartTimer() throws MyException {
 		Date dat = new Date();
 		mTimExpire = new Date(dat.getTime()+mInterval);
 		
 		mTimer.cancelTimer();
 		mTimer.startTimer(mInterval);
 	}
 	
 	public void setPass(String pass) throws MyException {
 		if (pass==null || pass.length()==0)
 			throw new MyException(TTypMyException.EPassInvalid);
 		
 		
 		mPass = pass;
		mTimExpire = null; //for pass check in getKey();
 		try {
 			if (mKey!=null)
 				getKey();
 		} catch (MyException e) {
 			Log.e("!!!", "Invalid pass: "+e.getId());
 			mPass=null;
 			throw e;
 		}
 		
 		restartTimer();
 	}
 
 	public String getPass() throws MyException {
 		return mPass;
 	}
 
 	public void setKey(String _key) {
 		mKey = _key;
 	}
 
 	public BigInteger getKey() throws MyException {
 		Date dat = new Date();
 		if (mTimExpire!=null && dat.after(mTimExpire)) {
 			mPass=null;
 		}
 		
 		if (mPass==null)
 			throw new MyException(TTypMyException.EPassExpired);
 		
 		String key = mAes.decrypt(mPass, mKey);
 		byte[] b = mBase64.decode(key.getBytes());
 		BigInteger res = new BigInteger(b);
 		
 		return res;
 	}
 
 	public boolean isPassValid() {
 		return (mPass!=null);
 	}
 
 	public void setInterval(long _ms) throws MyException {
 		mInterval = _ms;
 		
 		if ( isPassValid() ) {
 			restartTimer();
 		}
 	}
 
 	public void timerEvent(IMTimer sender) throws Exception {
 		if (mPass!=null) {
 			mPass = null;
 			mTimExpire=null;
 			if (mObserver!=null) 
 				mObserver.passExpired(this);
 		}
 	}
 
 	public void setObserever(IMPassHolderObserver observer) {
 		mObserver = observer;
 	}
 
 	public void clearPass() {
 		if (mPass!=null) {
 			mPass=null;
 			mTimExpire=null;
 			mTimer.cancelTimer();
 		}
 	}
 
 }
