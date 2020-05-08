 /*
  * Copyright (C) 2014 RolTekk
  * Application: DragonChess
  * Description: Android platform wrapper
  */
 
 package com.roltekk.game.dragonchess_android;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
 import com.roltekk.game.dragonchess_core.DragonChessCore;
 
 public class DragonChessAndroid extends AndroidApplication {
   public void onCreate(android.os.Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);t
     initialize(new DragonChessCore(), false);
   }
 }
