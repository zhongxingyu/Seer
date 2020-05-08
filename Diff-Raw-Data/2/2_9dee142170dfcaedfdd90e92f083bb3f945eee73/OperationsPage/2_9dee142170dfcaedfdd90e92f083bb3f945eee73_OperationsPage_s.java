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
  * @date 24th September 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.server.pages;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import au.edu.uts.eng.remotelabs.rigclient.main.RigClient;
 import au.edu.uts.eng.remotelabs.rigclient.util.BackupCleaner;
 
 /**
  * Page to provide confirmations for the operations:
  * <ul>
  *  <li>Restart</li>
  *  <li>Shutdown</li>
  * </ul>
  */
 public class OperationsPage extends AbstractPage
 {
 
     @Override
     public void preService(HttpServletRequest req)
     {
         this.framing = false;
     }
     
     @Override
     public void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException
     {
         String uri = req.getRequestURI();
         if (uri.endsWith("shutdown") && "POST".equals(req.getMethod()))
         {
             /* Shutdown the rig client. */
             RigClient.blockingStop();
         }
         else if (uri.endsWith("shutdown"))
         {
             /* Display a confirmation page for shutting down the rig client. */
             this.displayConfirmation(false);
         }
         else if (uri.endsWith("restart") && "POST".equals(req.getMethod()))
         {
             /* Restart the rig client. */
             RigClient.restart();
         }
         else if (uri.endsWith("restart"))
         {
             /* Display a confirmation page for restarting the rig client. */
             this.displayConfirmation(true);
         }
         else if (uri.endsWith("clear") && "POST".equals(req.getMethod()))
         {
             /* Clear the maintenance state. */
             this.rig.setMaintenance(false, null, true);
         }
         else if (uri.endsWith("clear"))
         {
             /* Display a confirmation page for clearing maintenance. */
             this.displayMaintenanceConfirmation();
         }
         else if (uri.endsWith("clean") && "POST".equals(req.getMethod()))
         {
             new BackupCleaner().clean();
         }
         else if (uri.endsWith("clean"))
         {
             /* Display a confirmation page for cleaning backups. */
             this.displayCleanConfirmation();
         }
         else if (uri.endsWith("gc"))
         {
             System.gc();
             this.redirect(resp, "/");
         }
         else
         {
             /* Unknown operation so go back to index. */
             new IndexPage().service(req, resp);
         }
     }
     
     /**
      * Displays a confirmation page.
      * 
      * @param isRestart whether the confirmation is to restart or stop.
      */
     private void displayConfirmation(boolean isRestart)
     {
         this.pageBeginning();
         this.confirmPanelStart();
         
         if (isRestart)
         {
             this.println("      <div class='confirmimg'><img src='/img/restart_huge.png' alt='restart' /></div>");
             this.println("       <div class='confirmtext'>Are you sure you want to restart the rig client?</div>");
         }
         else
         {
             this.println("      <div class='confirmimg'><img src='/img/shutdown_huge.png' alt='shutdown' /></div>");
             this.println("      <div class='confirmtext'>Are you sure you want to shutdown the rig client?</div>");
         }
         
         this.println("<div style='clear:both'> </div>");
         
         if (this.rig.isSessionActive())
         {
             this.println("<div id='confirmsession' class='ui-state ui-state-highlight ui-corner-all'>");
             this.println("  <span class='detailspanelicon ui-icon ui-icon-info'></span>");
            this.println("  A session is currently active and will be termianted.");
             this.println("</div>");
         }
         
         /* Buttons to make a choice. */
         this.println("<div id='confirmbuttons'>");
         this.addButton("noconfirm", "No", "window.location.replace(\"/\")");
         if (isRestart)
         {
             this.addButton("confirmres", "Yes", "restartRigClient()");
         }
         else
         {
             this.addButton("confirmres", "Yes", "shutdownRigClient()");
         }
         this.println("</div>");
         
         this.confirmPanelEnd();
         this.pageEnd();
     }
     
     /**
      * Displays a confirmation page for the maintenance function.
      */
     private void displayMaintenanceConfirmation()
     {
         this.pageBeginning();
         this.confirmPanelStart();
 
         this.println("      <div class='confirmimg'><img src='/img/clearmain_huge.png' alt='clear' /></div>");
         this.println("      <div class='confirmtext'>Are you sure you clear any maintenance states of the rig " +
         		"client? This will allow users to be assigned to the rig (provided no exerciser tests are " +
         		"failing).</div>");
         
         this.println("<div style='clear:both'> </div>");
         
         /* Buttons to make a choice. */
         this.println("<div id='confirmbuttons'>");
         this.addButton("noconfirm", "No", "window.location.replace(\"/\")");
         this.addButton("clearmain", "Yes", "clearMaintenance()");
         this.println("</div>");
         
         this.confirmPanelEnd();
         this.pageEnd();
     }
     
     /**
      * Displays a confirmation page for the maintenance function.
      */
     private void displayCleanConfirmation()
     {
         this.pageBeginning();
         this.confirmPanelStart();
 
         this.println("      <div class='confirmimg'><img src='/img/clean_huge.png' alt='clean' /></div>");
         this.println("      <div class='confirmtext'>Are you sure you want to clean configuration and log backup " +
         		"files?</div>");
         
         this.println("<div style='clear:both'> </div>");
         
         /* Buttons to make a choice. */
         this.println("<div id='confirmbuttons'>");
         this.addButton("noconfirm", "No", "window.location.replace(\"/\")");
         this.addButton("clearmain", "Yes", "cleanBackups()");
         this.println("</div>");
         
         this.confirmPanelEnd();
         this.pageEnd();
     }
 
     /**
      * Confirmation panel start content.
      */
     private void confirmPanelStart()
     {
         this.println("<div id='confirmationcontainer' class='detailspanel ui-corner-all'>");
         this.println("   <div class='detailspaneltitle'>");
         this.println("      <p>");
         this.println("          <span class='detailspanelicon ui-icon ui-icon-notice'> </span>");
         this.println("          Confirm:");
         this.println("      </p>");
         this.println("   </div>");
         this.println("   <div class='detailspanelcontents'>");
     }
 
     /**
      * Confirmation panel end content.
      */
     private void confirmPanelEnd()
     {
         this.println("   </div>");
         this.println("</div>");
         
         /* Script to put the confirmation dialog in the center of the page. */
         this.println("<script type='text/javascript'>");
         this.println(
                 "$(document).ready(function() {\n" + 
                 "   var leftpos = Math.floor($(window).width() / 2) - 175;\n" +
                 "   var toppos = Math.floor($(window).height() / 2) - ($(window).height() > 600 ? 200 : 75);\n" +
                 "   $('#confirmationcontainer').css('left', leftpos);\n" +
                 "   $('#confirmationcontainer').css('top', toppos)\n" +
                 "});");
         this.println("</script>");
     }
 
     /**
      * Page beginning.
      */
     private void pageBeginning()
     {
         this.println("<!DOCTYPE html>");
         this.println("<html>");
         this.addHead();
         this.println("<body>");
     }
     
     /**
      * Page end.
      */
     private void pageEnd()
     {
         this.println("</body>");
         this.println("</html>");
     }
 
     @Override
     protected String getPageType()
     {
         return "Operations";
     }
 
 }
