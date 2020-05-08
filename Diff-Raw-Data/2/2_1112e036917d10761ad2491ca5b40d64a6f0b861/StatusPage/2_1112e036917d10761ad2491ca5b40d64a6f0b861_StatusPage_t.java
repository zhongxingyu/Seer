 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 20th September 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.server.pages;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.AbstractRig;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigSession.Session;
 import au.edu.uts.eng.remotelabs.rigclient.rig.ITestAction;
 import au.edu.uts.eng.remotelabs.rigclient.status.StatusUpdater;
 
 /**
  * Rig status page.
  */
 public class StatusPage extends AbstractPage
 {
     /** Request URI. */
     private String uri;
     
     @Override
     public void preService(HttpServletRequest req)
     {
         this.uri = req.getRequestURI();
         
         if (this.uri.endsWith("update") || this.uri.endsWith("current"))
         {
             /* This is just a page update, not complete page generation. */
             this.framing = false;
         }
 
     }
     
     @Override
     public void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException
     {
         if (this.uri.endsWith("current"))
         {
             if (!StatusUpdater.isRegistered())
             {
                 this.println("  <img src='/img/blue_small.gif' alt='blue' />");
             }
             else if (this.rig.isSessionActive())
             {
                 this.println("  <img src='/img/yellow_small.gif' alt='red' />");
             }
             else if (this.rig.isMonitorStatusGood())
             {
                 this.println("  <img src='/img/green_small.gif' alt='green' />");
             }
             else
             {
                 this.println("  <img src='/img/red_anime_small.gif' alt='red' />");
             }
         }
         else if (this.uri.endsWith("update"))
         {
             this.generateStatusContents();
         }
         else
         {
             this.addLegend();
             this.println("<div id='statuscontents'>");
             this.generateStatusContents();
             this.println("</div>");
             
            this.println("<div style='clear:both; margin-bottom:20px'> </div>");
                         
             /* Page auto-update. */
             this.println(
                 "<script type='text/javascript'>\n" +
                 "$(document).ready(function() {\n" +
                 "   setTimeout(updateStatus, 10000);\n" +
                 "});" +
                 "</script>");
         }
     }
 
     /**
      * Generates the main contents of the page.
      */
     private void generateStatusContents()
     {
         /* Main status. */
         this.println("<div id='bigstatus'>");
         if (!StatusUpdater.isRegistered())
         {
             this.println("  <img src='/img/blue.gif' alt='Not registered' />");
             this.println("  <h3>Not registered</h3>");
         }
         else if (this.rig.isSessionActive())
         {
             this.println("  <img src='/img/yellow.gif' alt='In use' />");
             this.println("  <h3>In Use</h3>");
         }
         else if (this.rig.isMonitorStatusGood())
         {
             this.println("  <img src='/img/green.gif' alt='Online' />");
             this.println("  <h3>Online</h3>");
         }
         else
         {
             this.println("  <img src='/img/red_anime.gif' alt='Offline' />");
             this.println("  <h3>Offline</h3>");
             this.println("  <div class='ui-state ui-state-error errdialog ui-corner-all'>");
             this.println("      <p>");
             this.println("          <span class=' erricon ui-icon ui-icon-alert'></span>");
             this.println("          " + this.rig.getMonitorReason());
             this.println("      </p>");
             this.println("  </div>");
         }
         this.println("</div>");
         
         /* Push div. */
         this.println("<div style='height:20px'> </div>");
         
         this.addExerciserDetails();
         this.addSessionDetails();
     }
     
     /**
      * Adds the legend to the page.
      */
     private void addLegend()
     {
         this.println("<div id='statelegend'>");
         this.println("  <p>Legend:</p>");
         this.println("  <ul>");
         this.println("      <li id='leg1'>");
         this.println("          <img class='legicon' src='/img/blue_tiny.gif' alt='blue' />Not Registered");
         this.println("          <div id='leghov1' class='tooltiphov ui-corner-all'>");
         this.println("              <div class='tooltipimg'><img src='/img/blue.gif' alt='blue' /></div>");
         this.println("              <div class='tooltipdesc'>The rig is not registered to a scheduling server so cannot " +
         		"have users assigned to it.</div>");
         this.println("          </div>");
         this.println("      </li>");
         this.println("      <li id='leg2'>");
         this.println("          <img class='legicon' src='/img/green_tiny.gif' alt='green' />Online");
         this.println("          <div id='leghov2' class='tooltiphov ui-corner-all'>");
         this.println("              <div class='tooltipimg'><img src='/img/green.gif' alt='green' /></div>");
         this.println("              <div class='tooltipdesc'>The rig is online and ready for use.</div>");
         this.println("          </div>");
         this.println("      </li>");
         this.println("      <li id='leg3'>");
         this.println("          <img class='legicon' src='/img/red_tiny.gif' alt='red' />Offline");
         this.println("          <div id='leghov3' class='tooltiphov ui-corner-all'>");
         this.println("              <div class='tooltipimg'><img src='/img/red.gif' alt='red' /></div>");
         this.println("              <div class='tooltipdesc'>The rig is offline and cannot be used. The reason for being " +
         		"offline is that an exerciser test has failed or a session action has failed more than the failure " +
         		"threshold.</div>");
         this.println("          </div>");
         this.println("      </li>");
         this.println("      <li id='leg4'>");
         this.println("          <img class='legicon' src='/img/yellow_tiny.gif' alt='yellow' />In Use");
         this.println("          <div id='leghov4' class='tooltiphov ui-corner-all'>");
         this.println("              <div class='tooltipimg'><img src='/img/yellow.gif' alt='yellow' /></div>");
         this.println("              <div class='tooltipdesc'>The rig is currently being used.</div>");
         this.println("          </div>");
         this.println("      </li>");
         this.println("  </ul>");
         this.println("</div>");
         
         /* Click events. */
         this.println("<script type='text/javascript'>");
         this.println("var ttStates = [false, false, false, false];");
         
         this.println("$(document).ready(function() {");
         for (int i = 1; i <= 4; i++)
         {
             this.println(
                     "$('#leg" + i + "').hover(\n" + 
             		"        function () {\n" + 
             		"            ttStates['" + i + "'] = true;\n" + 
             		"            setTimeout('loadToolTip(\"#leg\", " + i + ", ttStates)', 1200);\n" + 
             		"        },\n" + 
             		"        function () {\n" + 
             		"            if (ttStates['" + i + "'])\n" + 
             		"            {\n" + 
             		"                $('#leg' + " + i + ").css('font-weight','normal');\n" + 
             		"                $('#leghov' + " + i + ").fadeOut();\n" + 
             		"            }\n" + 
             		"            ttStates['" + i + "'] = false;\n" + 
             		"        }\n" + 
             		");");
         }
 
         this.println("});");
         this.println("</script>");
     }
 
 
     /**
      *  Adds the exerciser details to the page.
      */
     private void addExerciserDetails()
     {
         this.println("<div id='exerciserdetails' class='ui-corner-all detailspanel'>");
         this.println("  <div class='detailspaneltitle'>");
         this.println("  <p><span class='ui-icon ui-icon-wrench detailspanelicon'></span>Exerciser details</p>");
         this.println("  </div>");
         this.println("  <div class='detailpanelcontents'>");
         
         /* Overall exerciser state. */
         this.println("<div id='exerciserstate'>");
         this.println("  Exerciser State:");
         if (this.rig.isMonitorStatusGood())
         {
            this.println("<img src='/img/green_small.gif' />");
         }
         else
         {
             this.println("  <img src='/img/red_small.gif' />");
             this.println("  <div id='exercisereason'>");
             this.println("      <strong>Reason:</strong> " + this.rig.getMonitorReason());
             this.println("  </div>");
         }
         this.println("</div>");
         
         /* Specific test displays. */
         if (this.rig instanceof AbstractRig)
         {
             List<ITestAction> tests = ((AbstractRig)this.rig).getTests();
             if (tests.size() == 0)
             {
                 this.println("No tests configured.");
             }
             else
             {
                 this.println("<ul id='exercisertestlist'>");
                 for (ITestAction t : tests)
                 {
                     this.println("<li class='exercisertest'>");
                     this.println("  <div class='teststatus'>");
                     if (t.getStatus())
                     {
                         this.println("<img src='/img/green_tiny.gif' alt='green' />");
                     }
                     else
                     {
                         this.println("<img src='/img/red_tiny.gif' alt='red' />");
                     }
                     this.println(t.getActionType());
                     this.println("  </div>");
                     
                     if (!t.getStatus())
                     {
                         this.println("  <div class='testreason'>");
                         this.println("      <strong>Reason: </strong> " + t.getReason());
                         this.println("  </div>");
                     }
                     
                     this.println("</li>");
                 }
                 this.println("</ul>");
             }
             
         }
         
         this.println("</div>"); /* Panel contents. */
         this.println("</div>"); /* Panel. */
     }
 
     /**
      * Adds the session details to the page.
      */
     private void addSessionDetails()
     {
         this.println("<div id='sessiondetails' class='ui-corner-all detailspanel'>");
         this.println("  <div class='detailspaneltitle'>");
         this.println("  <p><span class='ui-icon ui-icon-person detailspanelicon'></span>Session details</p>");
         this.println("  </div>");
         this.println("  <div class='detailpanelcontents'>");
         if (this.rig.isSessionActive())
         {
             /* Activity detection. */
             this.println("<div id='activitydect'>");
             this.println("  Activity:");
             if (this.rig.isActivityDetected())
             {
                this.println("<img src='/img/green_small.gif' />");
             }
             else
             {
                 this.println("<img src='/img/red_small.gif' />");
             }
             this.println("</div>");
             
             /* User list. */
             String master = "";
             List<String> activeSlaves = new ArrayList<String>();
             List<String> passiveSlaves = new ArrayList<String>();
             
             for (Entry<String, Session> e : this.rig.getSessionUsers().entrySet())
             {
                 switch (e.getValue())
                 {
                     case MASTER:
                         master = e.getKey();
                         break;
                     case SLAVE_ACTIVE:
                         activeSlaves.add(e.getKey());
                         break;
                     case SLAVE_PASSIVE:
                         passiveSlaves.add(e.getKey());
                         break;
                 }
             }
             
             this.println("Master user: <strong>" + master + "</strong><br />");
             this.println("<strong>" + activeSlaves.size() + "</strong> active slave users.<br />");
             if (activeSlaves.size() > 0)
             {
                 this.println("Active slave list:");
                 this.println("<ul class='slavelist'>");
                 for (String u : activeSlaves)
                 {
                     this.println("<li>" + u + "</li>");
                 }
                 this.println("</ul>");
             }
             
             this.println("<strong>" + passiveSlaves.size() + "</strong> passive slave users.<br />");
             if (passiveSlaves.size() > 0)
             {
                 this.println("Passive slave list:");
                 this.println("<ul class='slavelist'>");
                 for (String u : passiveSlaves)
                 {
                     this.println("<li>" + u + "</li>");
                 }
                 this.println("</ul>");
             }
         }
         else
         {
             this.println("No session is active.");
         }
         
         this.println("  </div>");
         this.println("</div>");
     }
 
     @Override
     protected String getPageType()
     {
         return "Status";
     }
     
     @Override
     protected String getPageHeader()
     {
         return this.stringTransform(this.config.getProperty("Rig_Name")) + " Status";
     }
 
 }
