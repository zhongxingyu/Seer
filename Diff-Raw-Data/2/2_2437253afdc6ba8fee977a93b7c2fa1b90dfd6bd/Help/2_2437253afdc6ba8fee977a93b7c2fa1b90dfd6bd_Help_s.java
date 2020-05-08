 /*
  *      HarshJelly Tweaker - An app to Tweak HarshJelly ROM
  *      Author : Harsh Panchal <panchal.harsh18@gmail.com, mr.harsh@xda-developers.com>
  */
 package com.harsh.romtool;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Html;
 import android.widget.TextView;
 
 public class Help extends Activity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.help);
         TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setText(Html.fromHtml("<b>1. CRT Animation</b><br>"+"This will Enable CRT effect while turniing screen off.Beware that there are some bugs which wake up device sometime after turning screen off.In that case just wait for 1-2 second and then try to turn it off<br>"+"<b>2. Long Press Back to kill app</b><br>"+"This will simply kill the foreground activity on Holding back key.<br>"+"<b>3. Ascending Ringtone</b><br>"+"When enabled,Ringtone volume will be increase in Ascending manner<br>"+"<b>4. Wake on Unplug</b><br>"+"Turns screen on upon disconnecting device from USB cable, Charger etc<br>"+"<b>5. Allow all Rotations</b><br>"+"Allows Screen to rotate in any direction (90,180,270,360)<br>"+"<b>6. Show Navigationbar</b><br>"+"Shows on screen virtual Hardware Keys.This is only for Fun.Some stock apps will be messed up after enabling this option<br>"+"<b>7. IME Switcher Notification</b><br>"+"Shows notification in Status bar while typing to switch IME (keyboard).Useful while using Multiple keyboards<br>"+"<b>8. Scrolling Cache</b><br>"+"When disabled, some apps may work more smoothly and also reduces CPU overhead.But some apps have negative effect.So test and set it to whatever you like.By Default:ENABLED<br>"+"<b>9. Lockscreen Vibration</b><br>"+"Enables Haptic Feedback in AOSP Lockscreen<br>"+"<b>10. Lockscreen Rotation</b><br>"+"Allows rotation in lockscreen.Due to bug in Pattern Lock screen, it is mandatory to Enable this option if you're using Pattern Lock.<br>"+"<b>11. Logger</b><br>"+"Enables Android Logger.This will reduce system performance.But if you're going to report a bug, enable this option then take log and post it.This Option works only for cocore Kernel<br>"+"<b>12. Sysctl Tweaks</b><br>"+"This Tweaks are EXPERIMENTAL, so their effects are unknown.Enable/disable them according to whatever you feel.By Default:DISABLED<br>"+"<b>13. Disable FSYNC</b><br>"+"When disabled, FSYNC can dramatically increase IO Performance but at the cost of data loss in case of Power Failure/Random Restart.Note that if you've disabled FSYNC then Reboot/Recovery Option from Power menu won't work<br>"+"<b>14. Safe Volume Warning</b><br>"+"Shows/hides warning Toast when listening to High level sound.It is better to keep it disabled while watching Video.<br>CAUTION: Listening to loud music for longer periods can damage your ear."));
     }
 }
