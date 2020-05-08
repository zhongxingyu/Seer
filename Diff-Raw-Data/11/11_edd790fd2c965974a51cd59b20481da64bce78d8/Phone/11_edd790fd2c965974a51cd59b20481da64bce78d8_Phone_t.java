 package com.android.tencere.activity;
 
 import android.telephony.TelephonyManager;
 
 
 public class Phone {
 	public String number;
 	
 	TelephonyManager mTelephonyMgr;
 	   
 	public Phone(TelephonyManager mTelephonyMgr){
 		this.mTelephonyMgr = mTelephonyMgr;
		this.number = getMyPhoneNumber();
 	}	
 	public String getMyPhoneNumber(){
 	    return this.mTelephonyMgr.getLine1Number();
 	}
 
 }
 	
