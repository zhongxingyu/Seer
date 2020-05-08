 package com.RoboMobo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Роман
  * Date: 06.08.13
  * Time: 10:17
  */
 public class ActivityMainMenu extends Activity
 {
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.menu);
         RMR.init();
     }
 
     public void mp(View view)
     {
        Intent intent = new Intent(this, ActivityConnectMenu.class);
         startActivity(intent);
     }
 
     public void sp(View view)
     {
         RMR.currentMap = new Map(RMR.mapSideLength, RMR.mapSideLength);
         RMR.currentMap.p0 = new Player(0, 0, "Me", true);
         RMR.state = RMR.GameState.Singleplayer;
         RMR.currentMap.state = Map.MapState.Game;
         Intent intent = new Intent(this, ActivityMain.class);
         startActivity(intent);
     }
 }
