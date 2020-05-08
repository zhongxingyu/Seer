 //
 // Copyright (C) 2013, Maxim Osipov
 //
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without modification,
 // are permitted provided that the following conditions are met:
 //
 //  - Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 //  - Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 //  - Neither the name of the University of Oxford nor the names of its
 //    contributors may be used to endorse or promote products derived from this
 //    software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 // IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 // DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 // LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 // OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 
 //  <uses-permission android:name="android.permission.READ_PHONE_STATE" />
 //	<uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
 //	<uses-permission android:name="android.permission.RECEIVE_SMS" />
 //	<uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
 
 
 package com.ibme.android.actopsy;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Environment;
 import android.os.Handler;
 import android.provider.CallLog;
 
 public class ClassCallsTexts {
 
 	private static final String TAG = "ActopsyCallsTexts";
 
 	private Context mContext;
 	private File mFile;
 	private BufferedWriter mWriter;
 	private long mTsPrev;
 	private CallsObserver mCalls;
 	private TextsObserver mTexts;
 	private ContentResolver mResolver;
 
 	public ClassCallsTexts(Context context)
 	{
 		mContext = context;
 	}
 
 	// Open for write
 	public void init(long ts)
 	{
 		// TODO: Fix it to check and wait for storage
 		try {
 			File root = Environment.getExternalStorageDirectory();
 			if (root.canWrite()){
 				// Create folders
 				File folder = new File(root, ClassConsts.FILES_ROOT);
 				if (!folder.exists()) {
 					folder.mkdirs();
 				}
 				// Initialise the main calls & texts file
 				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
 				mFile = new File(folder, "calls-texts-" + fmt.format(new Date(ts)) + ".csv");
 				mWriter = new BufferedWriter(new FileWriter(mFile, true));
 			} else {
 				new ClassEvents(TAG, "ERROR", "SD card not writable");
 			}
 		} catch (Exception e) {
 			new ClassEvents(TAG, "ERROR", "Could not open file " + e.getMessage());
 		}
 		mTsPrev = ts;
 
 		mResolver = mContext.getContentResolver();
 		Handler handler = new Handler();
 		mCalls = new CallsObserver(handler);
 		mTexts = new TextsObserver(handler);
 		mResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, mCalls);
 		mResolver.registerContentObserver(Uri.parse("content://sms/"), true, mTexts);		
 	}
 
 	public void fini()
 	{
 		mResolver.unregisterContentObserver(mTexts);
 		mResolver.unregisterContentObserver(mCalls);
 		try {
 			if (mWriter != null) {
 				mWriter.close();
 				mWriter = null;
 			}
 			if (mFile != null) {
 				mFile = null;
 			}
 		} catch (Exception e) {
 			new ClassEvents(TAG, "ERROR", "Could not close file " + e.getMessage());
 		}
 	}
 
 	void update(long ts, String type, String dir, String num, String len)
 	{
 		long today = ts/ClassConsts.MILLIDAY;
 		long yesterday = mTsPrev/ClassConsts.MILLIDAY;
 		if (today > yesterday) {
			File f = mFile;
 			fini();
 			new TaskZipper().execute(f);
 			init(ts);
 		}
 		if (mWriter != null) {
 			try {
 				SimpleDateFormat fmt = new SimpleDateFormat(ClassConsts.DATEFMT);
 				String str = fmt.format(new Date(ts)) + 
 						"," + type + "," + dir +"," + num +
 						"," + len + "\n";
 				mWriter.write(str);
 				// TODO: We may not need to flush that often
 				mWriter.flush();
 			} catch(Exception e) {
 				new ClassEvents(TAG, "ERROR", "Could not write file " + e.getMessage());
 			}
 		}
 		mTsPrev = ts;
 	}
 
 	class CallsObserver extends ContentObserver
 	{       
 		public CallsObserver(Handler handler) 
 		{
 			super(handler);
 		}
 
 		@Override
 		public void onChange(boolean bSelfChange)
 		{
 			super.onChange(bSelfChange);
 
 			Cursor c = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
 			c.moveToLast();
 
 			long ts = System.currentTimeMillis();
 			String len = c.getString(c.getColumnIndex("duration"));
 			String type = c.getString(c.getColumnIndex("type"));
 			String num = c.getString(c.getColumnIndex("number"));
 
 			if (type.contentEquals("1")) {
 				update(ts, "Call", "I", md5(num), len);
 			} else if (type.contentEquals("2")) {
 				update(ts, "Call", "O", md5(num), len);
 			} else {
 				// don't care about missed
 			}
 			c.close();
 		}
 
 		@Override
 		public boolean deliverSelfNotifications() {
 		    return true;
 		}
 	}
 
 	class TextsObserver extends ContentObserver
 	{       
 		public TextsObserver(Handler handler) {
 			super(handler);
 		}
 
 		@Override
 		public void onChange(boolean bSelfChange)
 		{    
 			super.onChange(bSelfChange);
 
 			String strUriInbox = "content://sms/";
 			Uri uriSms = Uri.parse(strUriInbox); 
 			Cursor c = mContext.getContentResolver().query(uriSms, null, null, null, null);
 			c.moveToNext();
 
 			long ts = System.currentTimeMillis();
 			String len = String.valueOf(c.getString(c.getColumnIndex("body")).length());
 			String type = c.getString(c.getColumnIndex("type"));
 			String num = c.getString(c.getColumnIndex("address"));
 
 			if (type.contentEquals("1")) {
 				update(ts, "Text", "I", md5(num), len);
 			} else if (type.contentEquals("2")) {
 				update(ts, "Text", "O", md5(num), len);
 			} else {
 				// don't care about missed
 			}
 			c.close();
 		}
 
 		@Override
 		public boolean deliverSelfNotifications() {
 		    return true;
 		}
 	}
 
 	public String md5(String s) {
 	    try {
 	        // Create MD5 Hash
 	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
 	        digest.update(s.getBytes());
 	        byte messageDigest[] = digest.digest();
 
 	        // Create Hex String
 	        StringBuffer hexString = new StringBuffer();
 	        for (int i=0; i<messageDigest.length; i++)
 	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
 	        return hexString.toString();
 
 	    } catch (NoSuchAlgorithmException e) {
 			new ClassEvents(TAG, "ERROR", "No MD5");
 	    }
 	    return "";
 	}
 }
