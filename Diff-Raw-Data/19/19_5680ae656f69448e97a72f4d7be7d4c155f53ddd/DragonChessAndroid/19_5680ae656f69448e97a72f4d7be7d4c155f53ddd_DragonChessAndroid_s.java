 package com.roltekk.game.dragonchess_android;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
 
 public class DragonChessAndroid extends AndroidApplication {
        public void onCreate (android.os.Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                initialize(new Game(), false);
        }
 }
