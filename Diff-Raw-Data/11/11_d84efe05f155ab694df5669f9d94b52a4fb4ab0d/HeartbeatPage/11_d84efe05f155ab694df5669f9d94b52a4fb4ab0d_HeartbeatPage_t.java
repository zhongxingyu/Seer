 package com.google.jstestdriver.server.handlers.pages;
 
 import java.io.IOException;
 
 import com.google.jstestdriver.util.HtmlWriter;
 
 public class HeartbeatPage implements Page {
   public void render(HtmlWriter writer, SlavePageRequest request) throws IOException {
     writer.startHead()
       .writeTitle("Heartbeat")
       .writeStyleSheet("/static/heartbeatclient.css")
       .writeExternalScript("/static/lib/json2.js")
      .writeExternalScript("/static/jstestdrivernamespace.js")
       .writeExternalScript("/static/lib/jquery-min.js")
       .writeExternalScript("/static/heartbeatclient.js")
       .writeScript("jstestdriver.heartbeat = jstestdriver.createHeartbeat(\"" +
           request.createCaptureUrl() + "\");" +
           "jstestdriver.jQuery(document).ready(" +
           "function() {jstestdriver.heartbeat.start();});")
       .finishHead()
       .startBody()
       .finishBody()
       .flush();
   }
 }
