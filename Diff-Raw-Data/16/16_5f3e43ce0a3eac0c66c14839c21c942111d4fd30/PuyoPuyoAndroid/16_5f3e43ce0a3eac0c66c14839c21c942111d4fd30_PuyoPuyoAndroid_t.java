 package com.tacoid.puyopuyo;
 
 import android.os.Bundle;
 import com.badlogic.gdx.backends.android.AndroidApplication;
 
 public class PuyoPuyoAndroid extends AndroidApplication
 {	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);


         initialize(PuyoPuyo.getInstance(), false);
     }
 }
