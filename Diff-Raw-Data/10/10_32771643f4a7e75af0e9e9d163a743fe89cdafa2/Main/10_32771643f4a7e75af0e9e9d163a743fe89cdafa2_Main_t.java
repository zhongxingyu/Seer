 
 package foss.jonasl.svtplayer;
 
 import java.util.Date;
 
 import foss.jonasl.svtplayer.pvr.PVRService;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class Main extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         Native.init(this);
         DB
                 .instance()
                 .pvrInsert(
                        "http://www0.c90910.dna.qbrick.com/90910/od/20110102/11141153-0102RAPPORT1930-PLAY-hts-a-v1/11141153-0102RAPPORT1930-PLAY-hts-a-v1_vod.m3u8",
                        "rapport", new Date().getTime(), 0, 0, 0, 0);
         Intent i = new Intent(this, PVRService.class);
         startService(i);
     }
 
 }
