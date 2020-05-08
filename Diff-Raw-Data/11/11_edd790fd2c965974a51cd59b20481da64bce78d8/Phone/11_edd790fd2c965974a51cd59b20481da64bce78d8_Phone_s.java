 package com.android.tencere.activity;
 
 import android.telephony.TelephonyManager;
 
 
 public class Phone {
 	public String number;
 	
 	TelephonyManager mTelephonyMgr;
 	   
 	public Phone(TelephonyManager mTelephonyMgr){
 		this.mTelephonyMgr = mTelephonyMgr;
		this.number = getMy10DigitPhoneNumber();
 	}	
 	public String getMyPhoneNumber(){
 	    return this.mTelephonyMgr.getLine1Number();
 	}
 
	public String getMy10DigitPhoneNumber(){
		String s = this.getMyPhoneNumber();
		return s;
	}
	
	
	
	
 }
 	
