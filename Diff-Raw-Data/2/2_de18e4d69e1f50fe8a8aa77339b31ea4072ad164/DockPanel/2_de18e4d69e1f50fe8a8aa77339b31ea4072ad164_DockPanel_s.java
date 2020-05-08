 package com.android.rackspace.CalEvent;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 import java.text.*;
 
 import android.webkit.WebView;
 import com.android.rackspace.CalEvent.RackerCalendar;
 import com.android.rackspace.CalEvent.RackerEvent;
 
 public class DockPanel extends Activity
 {
     private TextView meetingStatusText;
     private TextView currentTimeText;
     private TextView currentMeetingTitleText;
     private TextView currentDurationText;
 
     private WebView mWebView;
     private RackerCalendar rc;
 
     private int toggle = 0;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         toggle = 0;
         rc = new RackerCalendar("http://apps.rackspace.com/a/feeds/ical/starwars@mailtrust.com/3::personal::633811/public/");
         
     }
 
     public void alarmButtonHandler(View view) {
         if (toggle == 0) {
           meetingStatusText = (TextView) findViewById(R.id.meetingStatus);
           meetingStatusText.setText("FREE");
         
           currentTimeText = (TextView) findViewById(R.id.currentTime);
           currentTimeText.setText("15:09");
 
           currentMeetingTitleText = (TextView) findViewById(R.id.currentMeetingTitle);
           currentMeetingTitleText.setText("Room is empty");
 
           currentDurationText = (TextView) findViewById(R.id.currentDuration);
           currentDurationText.setText("14:30-15:30");
 
           toggle = 1;
         }
         else {
           setContentView(R.layout.main);
           toggle = 0;
         }
     }
 
     public void allEventsButtonHandler(View view) {
       setContentView(R.layout.all_events);
 
       mWebView = (WebView) findViewById(R.id.webview);
       mWebView.getSettings().setJavaScriptEnabled(true);
      mWebView.loadUrl("http://apps.rackspace.com/a/mobile/index.php?p=calendar&wsid=fWq2Xc3RhcndhcnNAbWFpbHRydXN0LmNvbSxwcnVpR1RiYg2yZ4kx");
     }
 
     public void otherCalendarsButtonHandler(View view) {
       //setContentView(R.layout.other_cals);
       return;
     }
 
     public void refreshButtonHandler(View view) {
       rc.getFeed();
       RackerEvent event = rc.getCurrent();
 
       SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
 
       if (event == null) {
           meetingStatusText = (TextView) findViewById(R.id.meetingStatus);
           meetingStatusText.setText("FREE");
 
           currentMeetingTitleText = (TextView) findViewById(R.id.currentMeetingTitle);
           currentMeetingTitleText.setText("Room is empty");
 
           currentDurationText = (TextView) findViewById(R.id.currentDuration);
           currentDurationText.setText("");
       }
       else {
           meetingStatusText = (TextView) findViewById(R.id.meetingStatus);
           meetingStatusText.setText("OCCUPIED");
 
           currentMeetingTitleText = (TextView) findViewById(R.id.currentMeetingTitle);
           String meetingTitle = event.getSummary();
           currentMeetingTitleText.setText(meetingTitle);
 
           currentDurationText = (TextView) findViewById(R.id.currentDuration);
           String startTime = sdf.format(event.getStartTime());
           String stopTime = sdf.format(event.getStopTime());
 
           currentDurationText.setText(startTime + " - " + stopTime);
       }
 
       return;
 
     }
 
 }
