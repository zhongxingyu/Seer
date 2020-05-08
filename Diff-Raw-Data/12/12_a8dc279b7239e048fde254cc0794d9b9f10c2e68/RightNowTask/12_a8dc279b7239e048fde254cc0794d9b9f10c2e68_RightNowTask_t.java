 /**
   * This file is part of SR Player for Android
   *
   * SR Player for Android is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License version 2 as published by
   * the Free Software Foundation.
   *
   * SR Player for Android is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU General Public License for more details.
   *
   * You should have received a copy of the GNU General Public License
   * along with SR Player for Android.  If not, see <http://www.gnu.org/licenses/>.
   */
 package sr.player;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
import java.text.ParsePosition;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimerTask;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.util.Log;
 
 public class RightNowTask extends TimerTask {
 	
 	private PlayerService service;
 	private static boolean allreadyRunning = false;
 
 	
 	public RightNowTask(PlayerService service) {
 		this.service = service;
 	}
 
 	@Override
 	public void run() {
 		Log.d(SRPlayer.TAG, "RightNowTask run");		
 		if ( allreadyRunning ) {
 			Log.d(SRPlayer.TAG, "RightNowTask allready running terminating.");		
 			return;
 		}
 		RightNowTask.allreadyRunning = true;
 		RightNowChannelInfo info = new RightNowChannelInfo();
         URL url;
         InputStream urlStream;
 		try {
 			url = new URL(this.service.getCurrentStation().getRightNowUrl());
 			urlStream = url.openStream();
 		} catch (MalformedURLException e) {
 			Log.e(SRPlayer.TAG, "Error getting RightNowInfo", e);
 			RightNowTask.allreadyRunning = false;
 			return;
 		} catch (IOException e) {
 			Log.e(SRPlayer.TAG, "Error getting RightNowInfo", e);
 			RightNowTask.allreadyRunning = false;
 			return;
 		}
         try {
         	
 			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 	         factory.setNamespaceAware(false);
 	         XmlPullParser xpp = factory.newPullParser();
 	         xpp.setInput(urlStream, null);
 	         int eventType = xpp.getEventType();
 	         while( eventType != XmlPullParser.END_DOCUMENT) {
 	        	 if (eventType == XmlPullParser.START_TAG) {
 	        		 if ( xpp.getName().equals("Channel")
 	        				 && xpp.getAttributeValue(null, "Id")
 	        				 	.equals(""+this.service.getCurrentStation().getChannelId()) ) {
 	        			 parseChannel(xpp, info);
 	        			 eventType = XmlPullParser.END_DOCUMENT;
 	        			 continue;
 	        		 }
 	        	 }
 	        	 eventType = xpp.next();
 	         }
 	         Log.d(SRPlayer.TAG, "RightNowTask Update: " + info.getProgramTitle());
 	 		 this.service.rightNowUpdate(info);
 		} catch (XmlPullParserException e) {
 			Log.e(SRPlayer.TAG, "Error getting RightNowInfo", e);
 		} catch (IOException e) {
 			Log.e(SRPlayer.TAG, "Error getting RightNowInfo", e);
 		} finally {
 			try {
 				if ( urlStream != null ) {
 					urlStream.close();
 				}
 			} catch (IOException e) { }
 		}
 		
 		
 		RightNowTask.allreadyRunning = false;
 	}
 
 	private void parseChannel(XmlPullParser xpp, RightNowChannelInfo info) throws XmlPullParserException, IOException {
 		Log.d(SRPlayer.TAG, "RightNowTask parseChannel");		
 		int eventType = xpp.next();
         while( eventType != XmlPullParser.END_DOCUMENT) {
         	if (eventType == XmlPullParser.END_TAG ) {
         		if (xpp.getName().equals("Channel")) {
         			Log.d(SRPlayer.TAG, "RightNowTask parseChannel end channel");
         			return;
         		}
         	} else if (eventType == XmlPullParser.START_TAG ) {
         		if (xpp.getName().equals("ProgramTitle")) {        			
         			xpp.next();
         			info.setProgramTitle(xpp.getText());
         		} else if (xpp.getName().equals("ProgramInfo")) {
         			xpp.next();
         			info.setProgramInfo(xpp.getText());
         		} else if (xpp.getName().equals("ProgramURL")) {
         			xpp.next();
         			info.setProgramURL(xpp.getText().trim());
         		} else if (xpp.getName().equals("IsidorTitle")) {
         			xpp.next();
         			info.setiSidorTitle(xpp.getText().trim());
         		} else if (xpp.getName().equals("IsidorInfo")) {
         			xpp.next();
         			info.setiSidorInfo(xpp.getText().trim());
         		} else if (xpp.getName().equals("IsidorURL")) {
         			xpp.next();
         			info.setiSidorUrl(xpp.getText().trim());
         		} else if (xpp.getName().equals("Song")) {
         			xpp.next();
         			info.setSong(xpp.getText().trim());
         		} else if (xpp.getName().equals("NextSong")) {
         			xpp.next();
         			info.setNextSong(xpp.getText().trim());
         		} else if (xpp.getName().equals("NextProgramTitle")) {
         			xpp.next();
         			info.setNextProgramTitle(xpp.getText());
         		} else if (xpp.getName().equals("NextProgramDescription")) {
         			xpp.next();
         			info.setNextProgramDescription(xpp.getText());
         		} else if (xpp.getName().equals("NextProgramURL")) {
         			xpp.next();
         			info.setNextProgramURL(xpp.getText());
         		} else if (xpp.getName().equals("NextProgramStartTime")) {
         			xpp.next();
        			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.GERMAN);        			
        			Date date;        			        		        			        			        		
         			try {
						date = df.parse(xpp.getText());        										
 					} catch (ParseException e) {
 						date = new Date();
 						e.printStackTrace();
					}					
 					info.setNextProgramStartTime(date);
         		} 
         	}
         	eventType = xpp.next();
         }
 		Log.d(SRPlayer.TAG, "RightNowTask parseChannel end");		
 	} // end parseChannel
 
 }
