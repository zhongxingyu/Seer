 package com.softmo.smssafe.main.notificator;
 
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 
 public class CMNotificatorSound extends CMSmsNotificator implements
 		IMNotificatorSound {
 
	private TTypNotification mTyp = TTypNotification.ESoundAndIcon;
 	private int mCntNewSms=0;
 	
 	
 	public void setType(TTypNotification typ) {
 		mTyp = typ;
 		switch (mTyp) {
 		case ESoundAndIcon:
 		case EIconOnly:
 			Update(mCntNewSms);
 			break;
 		case ESoundOnly:
 		case ENone:
 			Cancel();
 			break;
 		}
 	}
 
 	public void Cancel() {
 		super.Update(0);
 	}
 
 	private void playSound() {
 		try {
 			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 			Ringtone r = RingtoneManager.getRingtone(getCotntext(), notification);
         r.play();
     	} catch (Exception e) {}	
 	}
 	
 	@Override
 	public void Popup(int cnt_newsms) {
 		mCntNewSms = cnt_newsms;
 		
 		switch(mTyp)
 		{
 		case ESoundAndIcon:
 			super.Popup(mCntNewSms);
 			break;
 		case EIconOnly:
 			super.Update(mCntNewSms);
 			break;
 		case ESoundOnly:
 			playSound();
 		case ENone:
 			//do nothing
 			break;
 		}
 	}
 
 	@Override
 	public void Update(int cnt_newsms) {
 		mCntNewSms = cnt_newsms;
 		
 		switch(mTyp)
 		{
 		case ESoundAndIcon:
 		case EIconOnly:
 			super.Update(mCntNewSms);
 			break;
 		case ESoundOnly:
 		case ENone:
 			//do nothing
 			break;
 		}
 	}
 
 }
