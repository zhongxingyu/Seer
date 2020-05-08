 
 package com.liquid.settings.activities;
 
 import com.liquid.settings.R;
  
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.StatFs;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.MotionEvent;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import 	org.apache.http.util.ByteArrayBuffer;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
  
 public class BootAnims extends Activity {
 
     private static final String TAG = "LiquidSettings :BootAnims";
     private static final int SWIPE_MIN_DISTANCE = 120;
     private static final int SWIPE_MAX_OFF_PATH = 250;
     private static final int SWIPE_THRESHOLD_VELOCITY = 200;
     private String SU_CMDS = null;
     private String WEBPATH = null;
     private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().toString();
     private GestureDetector gestureDetector;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.boot_anims);
 
         //to improve speed of thread creation we only load listeners if the bundle
         //is null (so we don't load listeners on orientation changes)
         if (savedInstanceState == null) {
             setupListeners();
         } else {
             //our listeners should be active from the first load
             //this should only get called when there is an orientation change
             Log.d(TAG, "our bundle was !=null listeners should be active already");
         }
     }
 
     private void setupListeners() {

         ImageButton does_gingy_button = (ImageButton) findViewById(R.id.does_gingy_command);
         does_gingy_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("gingy");
             }
         });
 
         ImageButton does_green_button = (ImageButton) findViewById(R.id.does_green_command);
         does_green_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("green");
             }
         });
 
         ImageButton does_light_blue_button = (ImageButton) findViewById(R.id.does_light_blue_command);
         does_light_blue_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("light_blue");
             }
         });
 
         ImageButton does_med_blue_button = (ImageButton) findViewById(R.id.does_green_command);
         does_med_blue_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("med_blue");
             }
         });
 
         ImageButton does_orange_button = (ImageButton) findViewById(R.id.does_orange_command);
         does_orange_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("orange");
             }
         });
 
         ImageButton does_pink_button = (ImageButton) findViewById(R.id.does_pink_command);
         does_pink_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("pink");
             }
         });
 
         ImageButton does_purple_button = (ImageButton) findViewById(R.id.does_purple_command);
         does_purple_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("purple");
             }
         });
 
         ImageButton does_red_button = (ImageButton) findViewById(R.id.does_red_command);
         does_red_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("red");
             }
         });
 
         ImageButton does_smoked_button = (ImageButton) findViewById(R.id.does_smoked_command);
         does_smoked_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("smoked");
             }
         });
 
         ImageButton does_two_blue_button = (ImageButton) findViewById(R.id.does_two_blue_command);
         does_two_blue_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("two_blue");
             }
         });
 
         ImageButton does_white_button = (ImageButton) findViewById(R.id.does_white_command);
         does_white_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("white");
             }
         });
 
         ImageButton does_yellow_button = (ImageButton) findViewById(R.id.does_yellow_command);
         does_yellow_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("yellow");
             }
         });
 
         /* Droid eyes */
         ImageButton eyes_green_button = (ImageButton) findViewById(R.id.eyes_green_command);
         eyes_green_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidEyes("green");
             }
         });
 
         ImageButton eyes_light_blue_button = (ImageButton) findViewById(R.id.eyes_light_blue_command);
         eyes_light_blue_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidEyes("light_blue");
             }
         });
 
         ImageButton eyes_blue_green_button = (ImageButton) findViewById(R.id.eyes_blue_green_command);
         eyes_blue_green_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidEyes("blue_green");
             }
         });
 
         ImageButton eyes_cyan_button = (ImageButton) findViewById(R.id.eyes_cyan_command);
         eyes_cyan_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidEyes("cyan");
             }
         });
 
         ImageButton eyes_orange_button = (ImageButton) findViewById(R.id.eyes_orange_command);
         eyes_orange_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("orange");
             }
         });
 
         ImageButton eyes_pink_button = (ImageButton) findViewById(R.id.eyes_pink_command);
         eyes_pink_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("pink");
             }
         });
 
         ImageButton eyes_purple_button = (ImageButton) findViewById(R.id.eyes_purple_command);
         eyes_purple_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("purple");
             }
         });
 
         ImageButton eyes_red_button = (ImageButton) findViewById(R.id.eyes_red_command);
         eyes_red_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("red");
             }
         });
 
         ImageButton eyes_yellow_button = (ImageButton) findViewById(R.id.eyes_yellow_command);
         eyes_yellow_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 droidDoes("yellow");
             }
         });
 
         ImageButton gingy_faster_button = (ImageButton) findViewById(R.id.gingy_faster_command);
         gingy_faster_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("gingy_faster");
             }
         });
 
         ImageButton gingy_slower_button = (ImageButton) findViewById(R.id.gingy_slower_command);
         gingy_slower_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("gingy_slower");
             }
         });
 
         ImageButton hallow_scarry_button = (ImageButton) findViewById(R.id.hallow_scarry_command);
         hallow_scarry_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("hallow_scarry");
             }
         });
 
         ImageButton hallow_skull_button = (ImageButton) findViewById(R.id.hallow_skull_command);
         hallow_skull_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("hallow_skull");
             }
         });
 
         ImageButton hallow_spiral_button = (ImageButton) findViewById(R.id.hallow_spiral_command);
         hallow_spiral_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("hallow_spiral");
             }
         });
 
         ImageButton liquid_default_button = (ImageButton) findViewById(R.id.liquid_default_command);
         liquid_default_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("liquid_default");
             }
         });
 
         ImageButton liquid_earth_button = (ImageButton) findViewById(R.id.liquid_earth_command);
         liquid_earth_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("liquid_earth");
             }
         });
 
         ImageButton liquid_rotate_button = (ImageButton) findViewById(R.id.liquid_rotate_command);
         liquid_rotate_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("liquid_rotate");
             }
         });
 
         ImageButton liquid_smooth_button = (ImageButton) findViewById(R.id.liquid_smooth_command);
         liquid_smooth_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("liquid_smooth");
             }
         });
 
         ImageButton liquid_sparkle_button = (ImageButton) findViewById(R.id.liquid_sparkle_command);
         liquid_sparkle_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("liquid_sparkle");
             }
         });
 
         ImageButton liquid_splash_button = (ImageButton) findViewById(R.id.liquid_splash_command);
         liquid_splash_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("liquid_splash");
             }
         });
 
         ImageButton other_android_frog_button = (ImageButton) findViewById(R.id.other_android_frog_command);
         other_android_frog_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_android_frog");
             }
         });
 
         ImageButton other_angry_birds_button = (ImageButton) findViewById(R.id.other_angry_birds_command);
         other_angry_birds_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_angry_birds");
             }
         });
 
         ImageButton other_arc_reactor_button = (ImageButton) findViewById(R.id.other_arc_reactor_command);
         other_arc_reactor_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_arc_reactor");
             }
         });
 
         ImageButton other_color_tbolt_button = (ImageButton) findViewById(R.id.other_color_tbolt_command);
         other_color_tbolt_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_color_tbolt");
             }
         });
 
         ImageButton other_cup_water_button = (ImageButton) findViewById(R.id.other_cup_water_command);
         other_cup_water_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_cup_water");
             }
         });
 
         ImageButton other_dark_blue_lfy_button = (ImageButton) findViewById(R.id.other_dark_blue_lfy_command);
         other_dark_blue_lfy_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_dark_blue_lfy");
             }
         });
 
         ImageButton other_inmemoryof_button = (ImageButton) findViewById(R.id.other_inmemoryof_command);
         other_inmemoryof_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_inmemoryof");
             }
         });
 
         ImageButton other_merry_xmas_button = (ImageButton) findViewById(R.id.other_merry_xmas_command);
         other_merry_xmas_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_merry_xmas");
             }
         });
 
         ImageButton other_rose_bloom_button = (ImageButton) findViewById(R.id.other_rose_bloom_command);
         other_rose_bloom_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_rose_bloom");
             }
         });
 
         ImageButton other_simply_stunning_button = (ImageButton) findViewById(R.id.other_simply_stunning_command);
         other_simply_stunning_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_simply_stunning");
             }
         });
 
         ImageButton other_solar_system_button = (ImageButton) findViewById(R.id.other_solar_system_command);
         other_solar_system_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("other_solar_system");
             }
         });
 
         ImageButton radial_default_button = (ImageButton) findViewById(R.id.radial_default_command);
         radial_default_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_default");
             }
         });
 
         ImageButton radial_dark_blue_button = (ImageButton) findViewById(R.id.radial_dark_blue_command);
         radial_dark_blue_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_dark_blue");
             }
         });
 
         ImageButton radial_green_button = (ImageButton) findViewById(R.id.radial_green_command);
         radial_green_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_green");
             }
         });
 
         ImageButton radial_light_blue_button = (ImageButton) findViewById(R.id.radial_light_blue_command);
         radial_light_blue_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_light_blue");
             }
         });
 
         ImageButton radial_lime_green_button = (ImageButton) findViewById(R.id.radial_lime_green_command);
         radial_lime_green_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_lime_green");
             }
         });
 
         ImageButton radial_orange_button = (ImageButton) findViewById(R.id.radial_orange_command);
         radial_orange_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_orange");
             }
         });
 
         ImageButton radial_pink_button = (ImageButton) findViewById(R.id.radial_pink_command);
         radial_pink_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_pink");
             }
         });
 
         ImageButton radial_purple_button = (ImageButton) findViewById(R.id.radial_purple_command);
         radial_purple_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_purple");
             }
         });
 
         ImageButton radial_red_button = (ImageButton) findViewById(R.id.radial_red_command);
         radial_red_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_red");
             }
         });
 
         ImageButton radial_silver_button = (ImageButton) findViewById(R.id.radial_silver_command);
         radial_silver_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_silver");
             }
         });
 
         ImageButton radial_white_button = (ImageButton) findViewById(R.id.radial_white_command);
         radial_white_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_white");
             }
         });
 
         ImageButton radial_yellow_button = (ImageButton) findViewById(R.id.radial_yellow_command);
         radial_yellow_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("radial_yellow");
             }
         });
 
         ImageButton sexy_booty_button = (ImageButton) findViewById(R.id.sexy_booty_command);
         sexy_booty_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("sexy_booty");
             }
         });
 
         ImageButton sexy_booby_button = (ImageButton) findViewById(R.id.sexy_booby_command);
         sexy_booby_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 specialBranding("sexy_booby");
             }
         });
     }
 
     private void buildMoveScript() {
 
         String BB = "busybox";
         String MOUNT = "busybox mount -o %s,remount -t yaffs2 /dev/block/mtdblock1 /system";
         String TMP_PATH = SDCARD_PATH + "/.liquid/bootanimation.zip";
         String SPACE = " ; ";
         StringBuilder cmds = new StringBuilder();
 
         cmds.append(String.format(MOUNT, "rw"));
         cmds.append(SPACE);
         cmds.append(String.format(BB + " cp -R %s /data/local/", TMP_PATH));
         cmds.append(SPACE);
         cmds.append(String.format(BB + " rm %s", TMP_PATH));
         cmds.append(SPACE);
         cmds.append(String.format(MOUNT, "ro"));
         cmds.append(SPACE);
         cmds.append("sync");
         cmds.append(SPACE);
         cmds.append("exit");
 
         SU_CMDS = cmds.toString();
 
     }
 
     private void droidDoes(String color) {
         String WHAT = new String();
         String PROMPT = "Install droid does boot animation?";
 
         if (color.equals("gingy")) {
             WHAT = "doesginger.zip";
         } else if (color.equals("green")) {
             WHAT = "doesgreen.zip";
         } else if (color.equals("light_blue")) {
             WHAT = "doeslightblue.zip";
         } else if (color.equals("med_blue")) {
             WHAT = "doesmedblue.zip";
         } else if (color.equals("orange")) {
             WHAT = "doesorange.zip";
         } else if (color.equals("pink")) {
             WHAT = "doespink.zip";
         } else if (color.equals("purple")) {
             WHAT = "doespurple.zip";
         } else if (color.equals("red")) {
             WHAT = "doesred.zip";
         } else if (color.equals("smoked")) {
             WHAT = "doessmoked.zip";
         } else if (color.equals("two_blue")) {
             WHAT = "doestwoblue.zip";
         } else if (color.equals("white")) {
             WHAT = "doeswhite.zip";
         } else if (color.equals("yellow")) {
             WHAT = "doesyellow.zip";
         } else {
             Log.wtf(TAG, "Shit is all fucked up");
         }
 
         buildMoveScript();
         WEBPATH = String.format("http://android.markjohnston.us/DL/LGB/BOOTS/DROID/DOES/%s", WHAT);
  
         runSuCommand(PROMPT);
     }
 
     private void droidEyes(String color) {
 
         String PROMPT = "Install droid eyes boot animation?";
         String WHAT = new String();
 
         if (color.equals("green")) {
             WHAT = "eyesgreen.zip";
         } else if (color.equals("light_blue")) {
                 WHAT = "eyeslightblue.zip";
         } else if (color.equals("blue_green")) {
                 WHAT = "eyesbluegreen.zip";
         } else if (color.equals("cyan")) {
                 WHAT = "eyescyan.zip";
         } else if (color.equals("orange")) {
                 WHAT = "eyesorange.zip";
         } else if (color.equals("pink")) {
                 WHAT = "eyespink.zip";
         } else if (color.equals("purple")) {
                 WHAT = "eyespurple.zip";
         } else if (color.equals("red")) {
                 WHAT = "eyesred.zip";
         } else if (color.equals("yellow")) {
                 WHAT = "eyesyellow.zip";
         } else {
             Log.wtf(TAG, "Shit is all fucked up in the eyes");
         }
 
         buildMoveScript();
         WEBPATH = String.format("http://android.markjohnston.us/DL/LGB/BOOTS/DROID/EYES/%s", WHAT);
  
         runSuCommand(PROMPT);
     }
 
     private void specialBranding(String branding) {
 
         String WHAT = new String();
         String PROMPT = "Install a special boot animation?";
 
         if (branding.equals("gingy_faster")) {
             WHAT = "GINGY/gingyfaster.zip";
         } else if (branding.equals("gingy_slower")) {
             WHAT = "GINGY/gingyslower.zip";
         } else if (branding.equals("other_hallow_scarry")) {
             WHAT = "HALLOW/hallowscarry.zip";
         } else if (branding.equals("other_hallow_skull")) {
             WHAT = "HALLOW/hallowskull.zip";
         } else if (branding.equals("other_hallow_spiral")) {
             WHAT = "HALLOW/hallowspiral.zip";
         } else if (branding.equals("other_liquid_default")) {
             WHAT = "LIQUID/liquidearth.zip";
         } else if (branding.equals("other_liquid_earth")) {
             WHAT = "LIQUID/liquidearth.zip";
         } else if (branding.equals("other_liquid_rotate")) {
             WHAT = "LIQUID/liquidrotate.zip";
         } else if (branding.equals("other_liquid_smooth")) {
             WHAT = "LIQUID/liquidsmooth.zip";
         } else if (branding.equals("other_liquid_sparkle")) {
             WHAT = "LIQUID/liquidsparkle.zip";
         } else if (branding.equals("other_liquid_splash")) {
             WHAT = "LIQUID/liquidsplash.zip";
         } else if (branding.equals("other_android_frog")) {
             WHAT = "OTHER/androidfrog.zip";
         } else if (branding.equals("other_angry_birds")) {
             WHAT = "OTHER/angrybirds.zip";
         } else if (branding.equals("other_apple_ouch")) {
             WHAT = "OTHER/angrybirds.zip";
         } else if (branding.equals("other_arc_reactor")) {
             WHAT = "OTHER/archreactor.zip";
         } else if (branding.equals("other_color_tbolt")) {
             WHAT = "OTHER/colortbolt.zip";
         } else if (branding.equals("other_cup_water")) {
             WHAT = "OTHER/cupofwater.zip";
         } else if (branding.equals("other_dark_blue_lfy")) {
             WHAT = "OTHER/darkbluelfy.zip";
         } else if (branding.equals("other_inmemoryof")) {
             WHAT = "OTHER/inmemoryof.zip";
         } else if (branding.equals("other_merry_xmas")) {
             WHAT = "OTHER/merryxmas.zip";
         } else if (branding.equals("other_rose_bloom")) {
             WHAT = "OTHER/rosebloom.zip";
         } else if (branding.equals("other_simply_stunning")) {
             WHAT = "OTHER/simplystunning.zip";
         } else if (branding.equals("other_solar_system")) {
             WHAT = "OTHER/solarsystem.zip";
         } else if (branding.equals("radial_default")) {
             WHAT = "RADIAL/radialdefault.zip";
         } else if (branding.equals("radial_dark_blue")) {
             WHAT = "RADIAL/radialdarkblue.zip";
         } else if (branding.equals("radial_green")) {
             WHAT = "RADIAL/radialgreen.zip";
         } else if (branding.equals("radial_light_blue")) {
             WHAT = "RADIAL/radiallightblue.zip";
         } else if (branding.equals("radial_lime_green")) {
             WHAT = "RADIAL/radiallimegreen.zip";
         } else if (branding.equals("radial_orange")) {
             WHAT = "RADIAL/radialorange.zip";
         } else if (branding.equals("radial_pink")) {
             WHAT = "RADIAL/radialpink.zip";
         } else if (branding.equals("radial_purple")) {
             WHAT = "RADIAL/radialpurple";
         } else if (branding.equals("radial_red")) {
             WHAT = "RADIAL/radialred.zip";
         } else if (branding.equals("radial_silver")) {
             WHAT = "RADIAL/radialsilver.zip";
         } else if (branding.equals("radial_white")) {
             WHAT = "RADIAL/radialwhite.zip";
         } else if (branding.equals("radial_yellow")) {
             WHAT = "RADIAL/radialyellow.zip";
         } else if (branding.equals("sexy_booby")) {
             WHAT = "SEXY/sexybooby.zip";
         } else if (branding.equals("sexy_booty")) {
             WHAT = "SEXY/sexybooty.zip";
         } else {
             Log.wtf(TAG, String.format("Shit is all fucked up in other right now WHAT=%s", WHAT));
         }
 
         buildMoveScript();
         WEBPATH = String.format("http://android.markjohnston.us/DL/LGB/BOOTS/%s", WHAT);
 
         runSuCommand(PROMPT);
     }
     private void runSuCommand(final String message){
         //we recieve the message to display && the website
         //now we need to check to see if 
         //SU_CMDS is still null; and we will reset it to null in the async
         if (SU_CMDS !=null) {
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage(message)
                 .setCancelable(false)
                 .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         new SuServer().execute(SU_CMDS);
                         SU_CMDS = null;
                     }
                 })
                 .setNegativeButton("No", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                     }
                 });
 
             AlertDialog alertDialog = builder.create();
             builder.show();
         } else {
             Toast.makeText(this, "shit is all fucked up over here", Toast.LENGTH_SHORT).show();
         }
     }
 
     private class SuServer extends AsyncTask<String, String, Void> {
         private ProgressDialog pd;
 
         @Override
         protected void onPreExecute() {
             //pd = ProgressDialog.show(GoodiesActivity.this, "Working", "Running Command...", true, false);
         }
 
         @Override
         protected void onProgressUpdate(String... values) {
         }
 
         @Override
         protected void onPostExecute(Void result) {
             pd.dismiss();
 
         }
 
         @Override
         protected Void doInBackground(String... script) {
             final Process p;
 
             try {
                 File tmpDir = new File(SDCARD_PATH + "/.liquid");
                 if (!tmpDir.exists()) {
                     tmpDir.mkdir();
                 }
 
                 //must be full path http:// and all
                 URL url = new URL(WEBPATH);
                 String dl_path = SDCARD_PATH + "/.liquid/bootanimation.zip";
 
                 File dl_file = new File(dl_path);
 
                 //timer so we can moniter progress later
                 long startTime = System.currentTimeMillis();
 
                 //open connection and read inputStreams
                 URLConnection connecter = url.openConnection();
                 InputStream incomming_data = connecter.getInputStream();
                 BufferedInputStream input_buffer = new BufferedInputStream(incomming_data);
 
                 //TODO is 2048 better than 1024?
                 ByteArrayBuffer ba_buffer = new ByteArrayBuffer(2048);
                 int current = 0;
                 while ((current = input_buffer.read()) != -1) {
                     ba_buffer.append((byte) current);
                 }
 
                 FileOutputStream file_out = new FileOutputStream(dl_file);
                 file_out.write(ba_buffer.toByteArray());
                 file_out.close();
                 Log.d(TAG, "Buffer closed without event");
                 //TODO: handle exceptions better
             } catch (IOException ioe) {
                 Log.d(TAG, "Error: " + ioe);
                 ioe.printStackTrace();
                 //TODO:
                 //not sure what to do here dl has failed we can't proceed
                 //maybe we throw a runtimeExeption?
                 //for now we will just finish
                 finish();
             }
 
             //now we have our file local
             //lets move it around to apply
             try {
                 String EXIT = ";\nexit\n";
                 p = Runtime.getRuntime().exec("su -c sh");
                 BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                 //TODO:
                 //if we are going to watch the ErrorStream we should do something with it
                 BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                 BufferedWriter stdOutput = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
 
                 stdOutput.write(script[0]);
                 stdOutput.write(EXIT);
                 stdOutput.flush();
                 Thread t = new Thread() {
                     public void run() {
                         try {
                             p.waitFor();
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                     }
                 };
 
                 t.start();
 
                 while (t.isAlive()) {
                     String status = stdInput.readLine();
                     if (status != null) {
                         //TODO add in publishProgress
                         //publishProgress(status);
                     }
                     //TODO @Liquid: Why do we sleep here?
                     Thread.sleep(20);
                 }
 
                 stdInput.close();
                 stdError.close();
                 stdOutput.close();
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             } catch (InterruptedException ie) {
                 ie.printStackTrace();
             } catch (NullPointerException npe) {
                 npe.printStackTrace();
             }
         return null;
         }
     }
 }
