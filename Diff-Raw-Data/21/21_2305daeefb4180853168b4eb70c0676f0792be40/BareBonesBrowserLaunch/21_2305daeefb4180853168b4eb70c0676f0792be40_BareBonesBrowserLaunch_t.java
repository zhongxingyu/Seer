 /**
  * $RCSfile: ,v $
  * $Revision: $
  * $Date: $
  *
  * Copyright (C) 2004-2010 Jive Software. All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
  /**
   * <b>Bare Bones Browser Launch for Java</b><br>
   * Utility class to open a web page from a Swing application
   * in the user's default browser.<br>
   * Supports: Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7<br>
   * Example Usage:<code><br> &nbsp; &nbsp;
   *    String url = "http://www.google.com/";<br> &nbsp; &nbsp;
   *    BareBonesBrowserLaunch.openURL(int width, int height, String url, String title);<br></code>
   * Latest Version: <a href="http://www.centerkey.com/java/browser/">www.centerkey.com/java/browser</a><br>
   * Author: Dem Pilafian<br>
   * Public Domain Software -- Free to Use as You Like
   * @version 3.1, June 6, 2010
  */
 
 
 package org.jivesoftware.spark.plugin.red5;
 
 import java.awt.BorderLayout;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 
 import org.jivesoftware.spark.SparkManager;
 import org.jivesoftware.spark.component.browser.*;
 import org.jivesoftware.spark.util.log.*;
 
 public class BareBonesBrowserLaunch {
 
    static final String[] browsers = { "google-chrome", "firefox", "opera","epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla" };
    static final String errMsg = "Error attempting to launch web browser";
 
    static final Map<String, JFrame> windows = new HashMap<String, JFrame>();
    static final Map<String, BrowserViewer> viewers = new HashMap<String, BrowserViewer>();
 
 
    /**
     * Opens the specified web page in the user's default browser
     * @param url A web address (URL) of a web page (ex: "http://www.google.com/")
     */
 
    public static void openURL(int width, int height, String url, String title)
    {
       try {
 
 		String OS = System.getProperty("os.name").toLowerCase();
 
 	 	Log.warning("BareBonesBrowserLaunch.openURL: " + url + " " + title + " " + OS);
 
 		if (OS.indexOf("windows") > -1)
 		{
 			openBrowserURL(width, height, url, title);
 
 		} else {
 
 		  	//attempt to use Desktop library from JDK 1.6+
 
          	Class<?> d = Class.forName("java.awt.Desktop");
          	d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(d.getDeclaredMethod("getDesktop").invoke(null), new Object[] {java.net.URI.create(url)});
 
          	//above code mimicks:  java.awt.Desktop.getDesktop().browse()
 	 	}
 
      } catch (Exception ignore) {  //library not available or failed
 
 	 	 Log.warning("BareBonesBrowserLaunch.openURL: ignore " + ignore);
 
          String osName = System.getProperty("os.name");
 
          try {
 
             if (osName.startsWith("Mac OS"))
             {
                Class.forName("com.apple.eio.FileManager").getDeclaredMethod("openURL", new Class[] {String.class}).invoke(null, new Object[] {url});
 
             }  else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
 
 
             } else { //assume Unix or Linux
                String browser = null;
 
                for (String b : browsers)
                {
                   if (browser == null && Runtime.getRuntime().exec(new String[] {"which", b}).getInputStream().read() != -1)
                   {
                      Runtime.getRuntime().exec(new String[] {browser = b, url});
 				  }
 			   }
 
                if (browser == null)
                {
                   throw new Exception(Arrays.toString(browsers));
                }
             }
 
          } catch (Exception e) {
 			Log.warning("BareBonesBrowserLaunch.openURL: error " + e);
             JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString());
          }
       }
 	}
 
    private static void openBrowserURL(int width, int height, String url, String title)
    {
 	   	if (windows.containsKey(title))
 	   	{
 			JFrame frame = windows.get(title);
 			BrowserViewer viewer = viewers.get(title);
 
 			frame.setSize(width, height);
 			viewer.loadURL(url);
 
 		} else {
 
 			BrowserViewer viewer = new NativeBrowserViewer();
 			viewer.initializeBrowser();
 
 			JFrame frame = new JFrame(title);
 
 			frame.addWindowListener(new java.awt.event.WindowAdapter() {
 
 				public void windowClosing(WindowEvent winEvt)
 				{
 					JFrame frame = (JFrame) winEvt.getWindow();
 					String title = frame.getTitle();
 
 					frame.dispose();
 					windows.remove(title);
 
 					BrowserViewer viewer = viewers.get(title);
 					viewers.remove(title);
 				}
 			});
 
 			frame.setIconImage(SparkManager.getMainWindow().getIconImage());
 			frame.getContentPane().setLayout(new BorderLayout());
 			frame.getContentPane().add(viewer, BorderLayout.CENTER);
 			frame.setVisible(true);
 			frame.pack();
 			frame.setSize(width, height);
 			viewer.loadURL(url);
 
 			windows.put(title, frame);
 			viewers.put(title, viewer);
 		}
    }
 
 }
