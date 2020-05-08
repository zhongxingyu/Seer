 /*
  * Copyright (C) 2012 Marius Volkhart
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.volkhart.selenium.report;
 
 import com.google.common.io.ByteStreams;
 import com.google.common.io.Closeables;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Provides a way to report Selenium test events in an HTML format.
  * 
  * @author Marius Volkhart
  */
 public class Reporter {
 
     private static final String CSS = "hololike.css";
 
     private String mErrorIconUrl;
     private String mWarningIconUrl;
     private String mPassIconUrl;
     private String mDoneIconUrl;
     private String mTitle = "Automation Report";
     private Map<String, File> mNameToFile = new HashMap<String, File>();
     private Script mScript;
     private ArrayList<Script> mScripts = new ArrayList<Script>();
     private ArrayList<Info> mInfos = new ArrayList<Info>();
     private Writer mWriter;
     private File mOutput;
     private String mOutputPath;
     private String mBrowser;
 
     // Ensure that each thread only has a single reporter.
     private static ThreadLocal<Reporter> sReporter = new ThreadLocal<Reporter>() {
 
         @Override
         protected Reporter initialValue() {
             return new Reporter();
         }
 
     };
 
     // Need private constructor to avoid instantiation
     private Reporter() {
     }
 
     /**
      * Returns the thread-local instance of Reporter. If a Reporter has not yet
      * been instantiated it is created by this method call.
      * 
      * @return Thread-local Reporter instance
      */
     public static Reporter get() {
         return sReporter.get();
     }
 
     /**
      * Creates the HTML report to the location previously set.
      * 
      * @throws IOException If the report cannot be written to the specified
      *             path.
      */
     public void generateReport() throws IOException {
 
         // TODO add check for invalid path & nulls
         formatOutputPath();
         Screenshot.get().generateReport(mOutputPath);
         mOutput = new File(mOutputPath + ".html");
        mOutput.createNewFile();
         mWriter = new BufferedWriter(new FileWriter(mOutput));
 
         mErrorIconUrl = addLocalResources(Reporter.class.getResource("error.png"));
         mWarningIconUrl = addLocalResources(Reporter.class.getResource("warning.png"));
         mPassIconUrl = addLocalResources(Reporter.class.getResource("success.png"));
         mDoneIconUrl = addLocalResources(Reporter.class.getResource("info.png"));
 
         startReport();
 
         writeOverview();
 
         if (mScripts.size() > 0) {
 
             mWriter.write("\n<br/>\n\n");
             mainReporting();
 
         } else {
             mWriter.write("Doh! You need to write a test first!");
         }
 
         finishReport();
     }
 
     /**
      * Provides a way of segmenting tests at a high-level. When used in
      * conjunction with {@link #setFunction} this can be considered the parent
      * and {@code setFunction} the child.
      * 
      * @param name The name given to this high-level test segment.
      */
     public void setScript(String name) {
         mScript = new Script(name);
         mScripts.add(mScript);
     }
 
     /**
      * Provides a way of segmenting tests at a low-level. When used in
      * conjunction with {@link #setScript} this can be considered the child and
      * {@code setScript} the parent.
      * 
      * @param name The name given to this low-level test segment.
      */
     public void setFunction(String name) {
         mScript.add(new Function(name));
     }
 
     /**
      * Sets the title of the test. The title is used both in the report and in
      * generating the results directory hierarchy.
      * 
      * @param title The title of the test suite
      */
     public void setTitle(String title) {
         mTitle = title.trim();
         mTitle = mTitle.replace(' ', '_');
     }
 
     /**
      * Sets the location of where to create the directory hierarchy for test
      * results
      * 
      * @param path The location where test results should be written to.
      */
     public void setOutputPath(String path) {
         if (mOutputPath == null) {
             mOutputPath = path;
         }
     }
 
     /**
      * Sets the browser information used in generating the results directory
      * hierarchy.
      * 
      * @param browser The name of the browser being used.
      */
     public void setBrowser(String browser) {
         if (mBrowser == null) {
             mBrowser = browser.trim();
             mBrowser = mBrowser.replace(' ', '_');
         }
     }
 
     public void putInfo(String key, String value) {
         key = key == null ? "" : key;
         value = value == null ? "" : value;
         mInfos.add(new Info(key, value));
     }
 
     /**
      * Provides a way to add custom events to the Reporter for the results
      * generated at the end of the test.
      * 
      * @param e The Event that should be reported. The Function and Script are
      *            automatically applied.
      * @return true if this Reporter was changed as a result of this add.
      * @see #setFunction(String)
      * @see #setScript(String)
      */
     public boolean add(Event e) {
         if (mScript == null) {
             setScript(Script.DEFAULT_TITLE);
         }
         return mScript.add(e);
     }
 
     private void startReport() throws IOException {
 
         // Title
         mWriter.write("<!DOCTYPE html>\n<html>\n<head>\n<title>" + mTitle + "</title>");
 
         // Include resources
         mWriter.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://fonts.googleapis.com/css?family=Roboto\">");
 
         // Link to the CSS. No need to inline since we include images also.
         URL cssUrl = Reporter.class.getResource(CSS);
         String ref = addLocalResources(cssUrl);
         if (ref != null) {
             mWriter.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + ref + "\">\n");
         }
 
         // Inject JavaScript to collapse/expand sections & to enter/exit
         // fullscreen mode
         mWriter.write("<script type=\"text/javascript\"> \n" +
                 "function reveal(id) {\n" +
                 "if (document.getElementById(id).style.display == 'none') {\n" +
                 "document.getElementById(id).style.display = 'block';\n" +
                 "document.getElementById(id+'Button').value = 'Hide details...';\n" +
                 "} else {\n" +
                 "document.getElementById(id).style.display = 'none';\n" +
                 "document.getElementById(id + 'Button').value = 'Show details...';\n" +
                 "}\n}\n\n\n" +
                 "function viewFullScreen(id) {\n" +
                 "var docElm = document.getElementById(id);\n" +
                 "if (docElm) {\n" +
                 "if (docElm.requestFullscreen) {\n" +
                 "docElm.requestFullscreen();\n" +
                 "} else if (docElm.mozRequestFullScreen) {\n" +
                 "docElm.mozRequestFullScreen();\n" +
                 "} else if (docElm.webkitRequestFullScreen) {\n" +
                 "docElm.webkitRequestFullScreen();\n}\n\n" +
                 "document.addEventListener(\"click\", function() {\n" +
                 "if (document.exitFullscreen) {\n" +
                 "document.exitFullscreen();\n" +
                 "} else if (document.mozCancelFullScreen) {\n" +
                 "document.mozCancelFullScreen();\n" +
                 "} else if (document.webkitCancelFullScreen) {\n" +
                 "document.webkitCancelFullScreen();\n" +
                 "}\n}, false);\n}\n}\n</script>\n");
 
         // The header and timestamp
         mWriter.write("</head>\n<body>\n<h1>" + mTitle
                 + "<div class=\"titleSeparator\"></div></h1>\n");
         mWriter.write(String.format("Test performed at %1$s.", new Date().toString()));
         mWriter.write("<br/>");
 
         // Get the failure & warning counts
         int failCount = 0;
         int warningCount = 0;
         for (Script script : mScripts) {
             failCount += script.getFailureCount();
             warningCount += script.getWarningCount();
         }
 
         mWriter.write(String.format("%1$d failures and %2$d warnings found.", failCount,
                 warningCount));
 
         // Print all the info thats been added for the test
         if (mInfos.size() > 0) {
             mWriter.write("\n<br/>\n<br/>");
             mWriter.write("<table class=\"overview\">\n");
             for (Info i : mInfos) {
                 mWriter.write("<tr>\n<td>");
                 mWriter.write(i.getKey());
                 mWriter.write("</td><td>");
                 mWriter.write(i.getValue());
                 mWriter.write("</td>\n</tr>\n");
             }
             mWriter.write("</table>");
         }
 
         mWriter.write("\n<br/>\n");
     }
 
     private void writeOverview() throws IOException {
         // Write issue id summary
         mWriter.write("<div class=\"category\">\nOverview<div class=\"categorySeparator\"></div>\n</div>\n<table class=\"overview\">");
 
         for (Script script : mScripts) {
             mWriter.write("\n<tr>\n<td class=\"scriptColumn\"><a href=\"#");
             mWriter.write(script.getName());
             mWriter.write("\">");
             mWriter.write(script.getName());
             mWriter.write("</a></td>\n</tr>");
 
             for (Function function : script.getFunctions()) {
                 mWriter.write("\n<tr>\n<td class=\"functionColumn\">");
                 mWriter.write(formatOverviewInt(function.getFailureCount()));
                 mWriter.write(" <img border=\"0\" align=\"top\" src=\"");
                 mWriter.write(mErrorIconUrl);
                 mWriter.write("\"/> ");
                 mWriter.write(formatOverviewInt(function.getWarningCount()));
                 mWriter.write(" <img border=\"0\" align=\"top\" src=\"");
                 mWriter.write(mWarningIconUrl);
                 mWriter.write("\"/> ");
                 mWriter.write(formatOverviewInt(function.getPassCount()));
                 mWriter.write(" <img border=\"0\" align=\"top\" src=\"");
                 mWriter.write(mPassIconUrl);
                 mWriter.write("\"/>");
 
                 mWriter.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"#");
                 mWriter.write(function.getName());
                 mWriter.write("\">");
                 mWriter.write(function.getName());
                 mWriter.write("</a></td>\n</tr>");
             }
         }
         mWriter.write("\n</table>");
     }
 
     private void mainReporting() throws IOException {
 
         for (Script script : mScripts) {
 
             // Print the Script name
             mWriter.write("<div class=\"category\">\n");
             mWriter.write("<a name=\"");
             mWriter.write(script.getName());
             mWriter.write("\" href=\"#\">");
             mWriter.write(script.getName());
             mWriter.write("</a><div class=\"categorySeparator\"></div></div>");
 
             for (Function function : script.getFunctions()) {
                 // Write the Function data
                 mWriter.write("\n\n<div class=\"issue\">\n<a name=\"");
                 mWriter.write(function.getName());
                 mWriter.write("\" class=\"id\" href=\"#\">");
                 mWriter.write(function.getName());
                 mWriter.write("</a><div class=\"issueSeparator\"></div>");
 
                 for (Event event : function.getEvents()) {
                     mWriter.write("<br/>\n<div class=\"eventTitle\"><img border=\"0\" align=\"top\" src=\"");
                     mWriter.write(extractImageUrl(event));
                     mWriter.write("\"/> ");
                     String temp = event.getTitle();
                     mWriter.write(temp.charAt(0) + temp.substring(1).toLowerCase());
                     mWriter.write("</div>\n<div class=\"eventExplanation\">");
                     mWriter.write(event.getMessage());
                     mWriter.write("</div>");
 
                     if (event.getStatus() == Event.Status.FAIL
                             || event.getStatus() == Event.Status.WARNING) {
                         long UID = System.nanoTime();
                         mWriter.write("<br/><input type=\"button\" id=\"");
                         mWriter.write(Long.toString(UID));
                         mWriter.write("Button\" onclick=\"reveal('");
                         mWriter.write(Long.toString(UID));
                         mWriter.write("');\" value=\"Show details...\"/>\n<br/>\n<div id=\"");
                         mWriter.write(Long.toString(UID));
                         mWriter.write("\" style=\"display: none\">\n<br/>\n");
                         writeScreenshot(event);
                         writeCode(event);
                         mWriter.write("</div>");
                     }
                 }
                 mWriter.write("</div>");
             }
         }
     }
 
     private void finishReport() throws IOException {
         mWriter.write("\n</body>\n</html>");
         mWriter.close();
 
         String path = mOutput.getAbsolutePath();
         System.out.println(String.format("Wrote HTML report to %1$s", path));
     }
 
     private void writeCode(Event event) throws IOException {
         mWriter.write("\n<div class=\"warningslist\"><br/>\n<pre class=\"errorlines\">");
 
         String[] traces = event.getStackTrace();
 
         // Start at 1 because first element is the stackTrace call itself
         for (int i = 1, max = traces.length; i < max; i++) {
             mWriter.write("\n<span class=\"lineno\"> ");
             mWriter.write(Integer.toString(i));
             mWriter.write("</span>\t");
             appendEscapedText(traces[i]);
         }
         mWriter.write("</pre></div>");
     }
 
     /**
      * Returns a URL to a local copy of the given resource, or null. There is no
      * filename conflict resolution.
      */
     private String addLocalResources(URL url) {
         // Attempt to make local copy
         File resourceDir = computeResourceDir();
         if (resourceDir != null) {
             String base = url.getFile();
             base = base.substring(base.lastIndexOf('/') + 1);
             mNameToFile.put(base, new File(url.toExternalForm()));
 
             File target = new File(resourceDir, base);
             try {
                 FileOutputStream output = new FileOutputStream(target);
                 InputStream input = url.openStream();
                 ByteStreams.copy(input, output);
                 Closeables.closeQuietly(output);
                 Closeables.closeQuietly(input);
             } catch (IOException e) {
                 return null;
             }
             return "../" + resourceDir.getName() + '/' + encodeUrl(base);
         }
         return null;
     }
 
     /** Finds/creates the local resource directory, if possible */
     private File computeResourceDir() {
         File resources = new File(mOutput.getParentFile().getParentFile(), "HTML_resources");
         if (!resources.exists() && !resources.mkdir()) {
             resources = null;
         }
 
         return resources;
     }
 
     /** Encodes the given String as a safe URL substring, escaping spaces etc */
     private static String encodeUrl(String url) {
         try {
             return URLEncoder.encode(url, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             // This shouldn't happen for UTF-8
             System.err.println("Invalid string " + e.getLocalizedMessage());
             return url;
         }
     }
 
     private String extractImageUrl(Event event) {
         switch (event.getStatus()) {
             case FAIL:
                 return mErrorIconUrl;
             case WARNING:
                 return mWarningIconUrl;
             case DONE:
                 return mDoneIconUrl;
             case PASS:
                 return mPassIconUrl;
             default:
                 return "";
         }
     }
 
     private void appendEscapedText(String textValue) throws IOException {
         for (int i = 0, n = textValue.length(); i < n; i++) {
             char c = textValue.charAt(i);
             if (c == '<') {
                 mWriter.write("&lt;");
             } else if (c == '&') {
                 mWriter.write("&amp;");
             } else if (c == '\n') {
                 mWriter.write("<br/>");
             } else {
                 if (c > 255) {
                     mWriter.write("&#");
                     mWriter.write(Integer.toString(c));
                     mWriter.write(';');
                 } else {
                     mWriter.write(c);
                 }
             }
         }
     }
 
     private void formatOutputPath() {
         mOutputPath = mOutputPath + File.separator + mTitle + File.separator + mBrowser;
     }
 
     private void writeScreenshot(Event event) throws IOException {
         String id = mBrowser + event.getScreenshotFilePath();
         String path = "./" + id;
 
         mWriter.write("<a title=\"Larger image\" href=\"");
         mWriter.write(path);
         mWriter.write("\" target=\"_blank\">Permalink</a>\n\n");
         mWriter.write("<a title=\"Fullscreen\" href=\"#");
         mWriter.write(id);
         mWriter.write("\" onclick=\"viewFullScreen('");
         mWriter.write(id);
         mWriter.write("')\"><img width=\"100%\" src=\"");
         mWriter.write(path);
         mWriter.write("\" id=\"");
         mWriter.write(id);
         mWriter.write("\"/></a>\n\n");
     }
     
     private String formatOverviewInt(int i) {
         String toReturn = Integer.toString(i);
         if (toReturn.length() == 1) {
             toReturn = "&nbsp;&nbsp;" + toReturn;
         } else if (toReturn.length() == 2) {
             toReturn = "&nbsp;" + toReturn;
         }
         
         // Add an extra space to give some space
         return "&nbsp;" + toReturn;
     }
 
     private class Info {
 
         private String mKey;
         private String mValue;
 
         private Info(String key, String value) {
             mKey = key;
             mValue = value;
         }
 
         private String getKey() {
             return mKey;
         }
 
         private String getValue() {
             return mValue;
         }
     }
 
 }
