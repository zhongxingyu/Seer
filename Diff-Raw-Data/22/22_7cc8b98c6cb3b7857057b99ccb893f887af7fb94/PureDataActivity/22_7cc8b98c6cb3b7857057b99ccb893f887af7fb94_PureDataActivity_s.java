 package com.example.pdp5test;
 
import java.io.File;
import java.io.IOException;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;

 import android.app.Activity;
import android.os.Bundle;
 import android.util.Log;
 
 
 public class PureDataActivity extends Activity {
 	
 	 /* ---- GLOBAL VARS ----*/ 
 	 
	private PdUiDispatcher dispatcher;
 	private static final String TAG = "PDP5";
 
 	 // Constructor
 	PureDataActivity() {
 		 Log.e(TAG, "Contructed");
 		 
 	 }
 }
