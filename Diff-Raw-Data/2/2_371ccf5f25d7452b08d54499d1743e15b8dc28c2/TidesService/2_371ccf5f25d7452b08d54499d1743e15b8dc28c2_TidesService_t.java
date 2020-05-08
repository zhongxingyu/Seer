 package com.galwaytidetimes.service;
 
 import com.galwaytidetimes.MainActivity;
 
 import org.androidannotations.annotations.Background;
 import org.androidannotations.annotations.EBean;
 import org.androidannotations.annotations.RootContext;
 import org.androidannotations.annotations.UiThread;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.androidannotations.annotations.EBean.Scope.Singleton;
 
@EBean
 public class TidesService {
 
     @RootContext
     MainActivity mainActivity;
 
     private InputStream getInputStream(URL url) {
         try {
             return url.openConnection().getInputStream();
         } catch (IOException e) {
             return null;
         }
     }
         @Background
         public void downloadTideTimes() {
             ArrayList<String> itemList = new ArrayList<String>();
             String next = null;
             try {
                 URL url = new URL(
                         "http://www.tidetimes.org.uk/galway-tide-times-7.rss");
                 XmlPullParserFactory factory = XmlPullParserFactory
                         .newInstance();
                 factory.setNamespaceAware(false);
                 XmlPullParser xpp = factory.newPullParser();
                 xpp.setInput(getInputStream(url), "UTF_8");
                 boolean insideItem = false;
 
                 // Returns the type of current event: START_TAG, END_TAG, etc..
                 int eventType = xpp.getEventType();
                 while (eventType != XmlPullParser.END_DOCUMENT) {
                     if (eventType == XmlPullParser.START_TAG) {
 
                         if (xpp.getName().equalsIgnoreCase("item")) {
                             insideItem = true;
                         } else if (xpp.getName()
                                 .equalsIgnoreCase("description")) {
                             if (insideItem) {
                                 next = xpp.nextText();
                                 Pattern ptrn = Pattern.compile("(\\d{2}:\\d{2}\\s-\\s)(Low|High)(\\sTide\\s\\(\\d.\\d{2}m\\))");
                                 Matcher mtchr = ptrn.matcher(next);
                                 StringBuilder timesStringBuilder = new StringBuilder();
                                 while (mtchr.find()) {
                                     String match = mtchr.group();
                                     if(timesStringBuilder.length()!=0) {
                                         timesStringBuilder.append("<br>");
                                     }
                                     timesStringBuilder.append(match);
                                 }
                                 String item = timesStringBuilder.toString();
                                 itemList.add(item);
                             }
                         } else if (eventType == XmlPullParser.END_TAG
                                 && xpp.getName().equalsIgnoreCase("item")) {
                             insideItem = false;
                         }
                     }
                     eventType = xpp.next(); // move to next element
                 }
             } catch (MalformedURLException e) {
                 e.printStackTrace();
             } catch (XmlPullParserException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (IllegalArgumentException e) {
                 e.printStackTrace();
                 return;
             }
             this.updateUI(itemList);
         }
 
     @UiThread
     void updateUI(ArrayList<String> items) {
         mainActivity.handleDownloadResults(items);
     }
 
 
 }
