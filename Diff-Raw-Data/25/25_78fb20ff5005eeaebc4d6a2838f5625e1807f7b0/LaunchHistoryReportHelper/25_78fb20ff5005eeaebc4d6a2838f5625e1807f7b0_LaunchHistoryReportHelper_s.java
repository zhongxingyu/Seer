 /*
  *   Copyright 2009-2010 Rustici Software. Licensed under the
  *   Educational Community License, Version 2.0 (the "License"); you may
  *   not use this file except in compliance with the License. You may
  *   obtain a copy of the License at
  *   
  *   http://www.osedu.org/licenses/ECL-2.0
  *
  *   Unless required by applicable law or agreed to in writing,
  *   software distributed under the License is distributed on an "AS IS"
  *   BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  *   or implied. See the License for the specific language governing
  *   permissions and limitations under the License.
  */
 
 
 package com.rusticisoftware.scormcloud.logic.helpers;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
import com.rusticisoftware.hostedengine.client.LaunchInfo;
 
 public class LaunchHistoryReportHelper {
     
     private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ");
     
     protected String getJavaScriptCallText(String function, Object... args)
     {
         StringBuilder output = new StringBuilder();
         output.append("<script> document.write(");
         output.append(function);
         output.append("(");
         for (Object o : args) {
             output.append(o.toString() + ", ");
         }
         if (args.length > 0) {
             output.delete(output.length() - 2, output.length());
         }
         output.append(")); </script>");
         return output.toString();
     }
     
     public String getLaunchLinks(String regId, List<LaunchInfo> launchHistory) throws Exception
     {
         StringBuilder output = new StringBuilder();
 
         if (launchHistory.size() == 0) {
             output.append("    <div class='noLaunchMessage'> There are no launches recorded for this registration.</div>\n");
         }
 
         int idx = 1;
         output.append("  <div class='launch_list'>\n");
         for (LaunchInfo launchInfo : launchHistory){    
             Date launchTime = formatter.parse(launchInfo.getLaunchTime());
             Date exitTime = new Date();
             if(!(launchInfo.getExitTime() == null || launchInfo.getExitTime() == "")){
                 exitTime = formatter.parse(launchInfo.getExitTime());
             }
             Long launchDurationMillis = exitTime.getTime() - launchTime.getTime();
 
             output.append("    <div class='LaunchPlaceHolder' id='launch_" + launchInfo.getId() + "' regid='" + regId + "'>\n");
             output.append(" <div class='hide_show_div' >\n");
             output.append(" <table><tr>\n");
             output.append("          <td class='launch_listPrefix'>+</td>\n");
             output.append("          <td class='launch_index'>" + idx + ".</td>\n");
             output.append("          <td class='launch_time'>" + launchTime.toString() + "</td>\n");
             output.append("          <td class='launch_duration'>" + getJavaScriptCallText("fmtDuration", launchDurationMillis) + " </td>\n");
             output.append(" </tr></table>\n");
 
             output.append(" </div>\n");
             output.append("       <div class='launch_activity_list'><div id='receiver' class='div_receiver'></div></div>\n");
             output.append("    </div>\n");
 
             idx++;
         }
         output.append("  </div>\n");
         return output.toString();
     }
     
     public String getLaunchInfoXml(String regId, LaunchInfo launchInfo, List<LaunchInfo> launchHistory) throws Exception
     {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         DOMImplementation impl = builder.getDOMImplementation();
 
         Document doc = impl.createDocument(null,null,null);
 
         Element rootEl = doc.createElement("LaunchInfo");
         rootEl.setAttribute("launch_history_id", launchInfo.getId());
 
 
         // If we don't have an exit_time then the launch terminated without sending a final
         // post (or it is still in progress ...)
         rootEl.setAttribute("clean_termination", 
                 (launchInfo.getExitTime() != null && !launchInfo.getExitTime().equals("")) ? 
                         "true" : "false");
 
 
         String log = launchInfo.getLog();
         if (log != null && !log.equals(""))
         {
             Document launchDoc = builder.parse(new InputSource(new StringReader(log)));
 
             rootEl.appendChild(doc.importNode(launchDoc.getDocumentElement(), true));
 
             Element statusEl = doc.createElement("RegistrationStatusOnExit");
             statusEl.setAttribute("completion_status", launchInfo.getCompletion());
             statusEl.setAttribute("success_status", launchInfo.getSatisfaction());
             statusEl.setAttribute("score", "1".equals(launchInfo.getMeasureStatus()) ? String.format("%1$.2f%%", Double.parseDouble(launchInfo.getNormalizedMeasure())*100) : "unknown");
             statusEl.setAttribute("total_time_tracked", launchInfo.getExperiencedDurationTracked());
             rootEl.appendChild(statusEl);
         }
         
         // Find previous launch (if any) so that we know the status at launch start
         ArrayList<LaunchInfo> prevLaunches = new ArrayList<LaunchInfo>(launchHistory);
         ArrayList<LaunchInfo> prevLaunchesCopy = new ArrayList<LaunchInfo>(launchHistory);
 
         for (LaunchInfo otherLaunch : prevLaunchesCopy){
             boolean sameLaunch = (otherLaunch.getId().equals(launchInfo.getId()));
             boolean laterLaunch = otherLaunch.getLaunchTime().compareTo(launchInfo.getLaunchTime()) > 0;
             boolean emptyCompletion = otherLaunch.getCompletion() == null || "".equals(otherLaunch.getCompletion());
             if(sameLaunch || laterLaunch || emptyCompletion){
                 prevLaunches.remove(otherLaunch);
             }
         }
 
         Collections.sort(prevLaunches,
                 new Comparator<LaunchInfo>() {
                     public int compare(LaunchInfo x, LaunchInfo y) {
                         return x.getLaunchTime().compareTo(y.getLaunchTime());
                     }
                 });
         
         Element prevStatusEl = doc.createElement("RegistrationStatusOnEntry");
         if (prevLaunches.size() == 0)
         {
             // This was the first launch - assume initial state 
             prevStatusEl.setAttribute("completion_status", "unknown");
             prevStatusEl.setAttribute("success_status", "unknown");
             prevStatusEl.setAttribute("score", "unknown");
             prevStatusEl.setAttribute("total_time_tracked", "0");
         }
         else
         {
             LaunchInfo prevLaunch = prevLaunches.get(0);
             prevStatusEl.setAttribute("completion_status", prevLaunch.getCompletion());
             prevStatusEl.setAttribute("success_status", prevLaunch.getSatisfaction());
             prevStatusEl.setAttribute("score", "1".equals(prevLaunch.getMeasureStatus()) ? String.format("%1$.2f%%", Double.parseDouble(prevLaunch.getNormalizedMeasure())*100) : "unknown");
             prevStatusEl.setAttribute("total_time_tracked", prevLaunch.getExperiencedDurationTracked());
         }
 
         rootEl.appendChild(prevStatusEl);
         doc.appendChild(rootEl);
         return getXmlString(doc);
     }
     
     private String getXmlString (Document xmlDoc) throws TransformerFactoryConfigurationError, TransformerException {
         Transformer transformer = TransformerFactory.newInstance().newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
         //initialize StreamResult with File object to save to file
         StreamResult result = new StreamResult(new StringWriter());
         DOMSource source = new DOMSource(xmlDoc);
         transformer.transform(source, result);
         
         return result.getWriter().toString();
     }
 }
