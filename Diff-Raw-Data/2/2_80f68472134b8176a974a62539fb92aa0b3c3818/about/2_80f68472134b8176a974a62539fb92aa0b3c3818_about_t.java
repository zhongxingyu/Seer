 package nz.gen.wellington.penguin;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Window;
 import android.webkit.WebView;
 
 public class about extends Activity {
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE); 
         setContentView(R.layout.about);       
         
         StringBuilder text = new StringBuilder();
        text.append("<p>The <a href=\"http://tinyurl.com/3osqgtl/\">lost emperor penguin 'Happy Feet'</a> is currently making his way home after recovering at Wellington Zoo. ");
         text.append("<p>Happy Feet is wearing a <a href=\"http://blog.tepapa.govt.nz/2011/08/29/happy-feet-gets-technological/\">Sirtrack KiwiSat 202 Satellite Transmitter</a> which will track his progress after he is released into the Southern Ocean. ");
         text.append("This tracking data is been published on the <a href=\"http://www.nzemperor.com/\">@NZEmperor</a> website.</p>");
         
         text.append("<p>This application periodically polls the tracking feed, raising an Android notification when a new position fix is published.</p>");
         
         text.append("<p>Application developed by Tony McCrae.</p>");
         text.append("<p>This program is free software: you can redistribute it and/or modify " +
         		"it under the terms of the GNU General Public License as published by " +
         		"the Free Software Foundation, either version 3 of the License, or (at your option) any later version.</p>");
         
         text.append("<p>Full source code is available on Github: <a href=\"https://github.com/tonytw1/penguin-tracker\">https://github.com/tonytw1/penguin-tracker</a></p>");
 
         text.append("<p>Penguin image obtained from a Creative Commons licensed photograph by <a href=\"http://www.flickr.com/photos/elisfanclub/5955801117\">Eli Duke</a>.");
         
         WebView about = (WebView) findViewById(R.id.about);
         about.loadData(text.toString(), "text/html", "UTF-8");
     }
 
 	// TODO Credit:
 	// sirtrack
 	// @nzemperor	
 	// github link
 }
