 /*
  * BuildMonitor.java
  *
  * Copyright (C) 2010 Mario Sanchez Prada
  * Authors: Mario Sanchez Prada <msanchez@igalia.com>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of version 2 of the GNU General Public
  * License as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this program; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  *
  */
 
 package com.igalia.msanchez.webkitwatcher;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.Toast;
 
 import com.igalia.msanchez.webkitwatcher.Builder.BuildResult;
 
 public class BuildBotMonitor implements Runnable {
 
         private static final int N_BUILDERS = 21;
 
         private WebKitWatcher watcher;
         private String url;
         private Map<String, Builder> builders;
         private ProgressDialog progressDialog;
 
         private void initializeBuilders() {
                 this.builders = new HashMap<String, Builder>(N_BUILDERS);
 
                 // FIXME It would be nice not to have this hard-coded
                 Builder builder;
                 builder = new Builder("builders/Leopard%20Intel%20Release%20%28Build%29");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Leopard%20Intel%20Release%20%28Tests%29");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Leopard%20Intel%20Debug%20%28Build%29");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Leopard%20Intel%20Debug%20%28Tests%29");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/SnowLeopard%20Intel%20Release%20%28Build%29");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/SnowLeopard%20Intel%20Release%20%28Tests%29");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Windows%20Release%20%28Build%29");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Windows%20Debug%20%28Build%29");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/GTK%20Linux%2032-bit%20Release");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/GTK%20Linux%2032-bit%20Debug");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/GTK%20Linux%2064-bit%20Debug");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Qt%20Linux%20Release");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Qt%20Linux%20Release%20minimal");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Qt%20Linux%20ARMv5%20Release");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Qt%20Linux%20ARMv7%20Release");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Qt%20Windows%2032-bit%20Release");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Qt%20Windows%2032-bit%20Debug");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Chromium%20Win%20Release");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Chromium%20Mac%20Release");
                 builders.put(builder.getPath(), builder);
                 builder = new Builder("builders/Chromium%20Linux%20Release");
                 builders.put(builder.getPath(), builder);
         }
 
         public BuildBotMonitor (WebKitWatcher watcher) {
                 this.watcher = watcher;
                 this.url = "http://build.webkit.org";
                 this.progressDialog = null;
                 initializeBuilders();
         }
 
         public String getURL() {
                 return this.url;
         }
 
         public Map<String, Builder> getBuilders() {
                 return this.builders;
         }
 
         public void refreshState() {
                 // First check whether there's a valid network connection
                 ConnectivityManager connMan = (ConnectivityManager) this.watcher.getSystemService(Context.CONNECTIVITY_SERVICE);
                 NetworkInfo networkInfo = connMan.getActiveNetworkInfo();
 
                 // Check network connection before doing anything
                 if (networkInfo != null && networkInfo.isConnected()) {
                         this.progressDialog = ProgressDialog.show(this.watcher,
                                         this.watcher.getString(R.string.refreshing_title),
                                         this.watcher.getString(R.string.refreshing_message),
                                         true, true);
 
                         Thread thread = new Thread(this);
                         thread.start();
                 } else {
                         Toast.makeText(this.watcher.getApplicationContext(),
                                         this.watcher.getString(R.string.error_no_network),
                                         Toast.LENGTH_SHORT).show();
                 }
         }
 
         @Override
         public void run() {
                 try {
                         // Read the HTML
                         String htmlContent = "";
                         URL url = new URL(this.url + "/builders");
                        InputStream is = url.openStream();
                         InputStreamReader isr = new InputStreamReader(is);
                         BufferedReader reader = new BufferedReader(isr);
                         String line = reader.readLine();
                         while (line != null) {
                                 htmlContent += line;
                                 line = reader.readLine();
                         }
                         reader.close();
                         isr.close();
                         is.close();
 
                         // Process it
 
                         Pattern rootPattern = Pattern.compile("<?.tr>");
                         Pattern builderPattern = Pattern.compile(".*<td.*>.*<a href=.*>.*</a>.*</td>.*<td.*>.*<a href=.*>.*</a>.*<br/>.*</td>.*<td.*>.*</td>.*", Pattern.DOTALL);
                         Pattern builderCellPattern = Pattern.compile("</td>");
                         String[] buildersList = rootPattern.split(htmlContent);
                         String builderString = null;
                         Builder currentBuilder = null;
                         int so, eo;
 
                         // For each builder, update its information in the map
                         for (String s : buildersList) {
 
                                 // Sanity check
                                 Matcher m = builderPattern.matcher(s);
                                 if (!m.matches())
                                         continue;
 
                                 String[] builderCellsList = builderCellPattern.split(s);
 
                                 // Identify builder
                                 builderString = builderCellsList[0];
                                 so = builderString.indexOf("<a href=\"") + "<a href=\"".length();
                                 eo = builderString.lastIndexOf("\">");
                                 currentBuilder = builders.get(builderString.substring(so, eo));
 
                                 // Builder found. Fill remaining data
                                 if (currentBuilder != null) {
 
                                         // Name
                                         so = builderString.lastIndexOf("\">") + "\">".length();
                                         eo = builderString.indexOf("</a>");
                                         currentBuilder.setName(builderString.substring(so, eo));
 
                                         // Build result
                                         builderString = builderCellsList[1];
                                         if (builderString.contains("success"))
                                                 currentBuilder.setBuildResult(BuildResult.PASSED);
                                         else
                                                 currentBuilder.setBuildResult(BuildResult.FAILED);
 
                                         // Build number
                                         so = builderString.indexOf("<a href=\"") + "<a href=\"".length();
                                         eo = builderString.lastIndexOf("\">");
                                         String buildPath = builderString.substring(so, eo);
                                         String buildNumber = buildPath.substring(buildPath.lastIndexOf("/") + 1);
                                         currentBuilder.setBuildNumber(buildNumber);
 
                                         // SVN revision number
                                         so = eo + "\">".length();
                                         eo = builderString.indexOf("</a>");
                                         currentBuilder.setRevisionFromString(builderString.substring(so, eo));
 
                                         // Summary
                                         so = builderString.indexOf("<br/>") + "<br/>".length();
                                         currentBuilder.setSummary(builderString.substring(so));
 
                                         // Current status
                                         builderString = builderCellsList[2];
                                         so = builderString.indexOf("\">") + "\">".length();
                                         currentBuilder.setStatus(builderString.substring(so));
                                 }
                         }
                 } catch (Exception e) {
                         Toast.makeText(this.watcher.getApplicationContext(),
                                         "An error has occurred: " + e, Toast.LENGTH_SHORT).show();
                 }
 
                 handler.sendEmptyMessage(0);
         }
 
         private Handler handler = new Handler() {
                 @Override
                 public void handleMessage(Message msg) {
                         progressDialog.dismiss();
                         watcher.updateView();
                 }
         };
 }
