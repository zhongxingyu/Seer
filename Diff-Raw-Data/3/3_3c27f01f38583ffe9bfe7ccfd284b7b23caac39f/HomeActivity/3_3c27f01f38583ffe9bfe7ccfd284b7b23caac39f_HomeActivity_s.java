 package com.hackathon.photohunt;
 
 import java.io.IOException;
 
 import com.hackathon.photohunt.utility.SmsUtility;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.telephony.TelephonyManager;
 
 public class HomeActivity extends Activity {
 	
 	private static final String TAG = HomeActivity.class.getName();
 	public static final int CONTACT_RESULT = 0;
 	
 	private HomeModel m_model;
 	private boolean m_isRetainingNonConfig;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.home_layout);
         
         m_model = (HomeModel) getLastNonConfigurationInstance();
         if(m_model != null)
         {
         	m_model.resetActivity(this);
         	m_model.attachViewsToActivity();
         }
         else
         {
         	m_model = new HomeModel(this);
         }
     }
 
     @Override
     public HomeModel onRetainNonConfigurationInstance() 
     {
     	m_isRetainingNonConfig = true;
         m_model.releaseViewsFromActivity();
         return m_model;
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
     	if(requestCode == CONTACT_RESULT)
     		if(resultCode == Activity.RESULT_OK)
     		{
     			String phoneNumber = null;
     			// get contact data, create SMS and send
     			Uri uri = data.getData();
     			ContentResolver cr = getContentResolver();
     			Cursor cur = cr.query(uri,
     	                null, null, null, null);
     	        if (cur.getCount() > 0) {
     		    while (cur.moveToNext()) 
     		    {
     		        String id = cur.getString(
     	                        cur.getColumnIndex(ContactsContract.Contacts._ID));
     		        String name = cur.getString(
     	                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 	    	 		if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
 	    	 		{
 	    	 			Cursor pCur = cr.query(
 	    	 		 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
 	    	 		 		    null, 
 	    	 		 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
 	    	 		 		    new String[]{id}, null);
 	    	 		 	        while (pCur.moveToNext()) {
 	    	 		 		       phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
 	    	 		 	        } 
 	    	 		 	        pCur.close();
 	    	 	    }
     	        }
     		    String location = m_model.getCurrentLocation();
     		    if(location != null)
     		    {
     		    	 TelephonyManager mTelephonyMgr;
     		         mTelephonyMgr = (TelephonyManager)
     		                 getSystemService(Context.TELEPHONY_SERVICE); 
     		         String number = mTelephonyMgr.getLine1Number();
     		    	SmsUtility.sendLocationText(number, m_model.getCurrentLocation());
     		    }
     		    else
     		    {
     		    	m_model.createErrorToast(R.string.no_location);
     		    }
     		    
     	 	}
     		}
     		else
     		{
     			// error, make error toast
     			m_model.createErrorToast(R.string.contact_retrieval_error);
     		}
     }
     
     @Override
 	protected void onDestroy()
     {
     	if(!m_isRetainingNonConfig)
     	{
     		try
 			{
 				m_model.close();
 			}
 			catch (IOException e)
 			{
 			}
     	}
    	super.onDestroy();
     }
 }
