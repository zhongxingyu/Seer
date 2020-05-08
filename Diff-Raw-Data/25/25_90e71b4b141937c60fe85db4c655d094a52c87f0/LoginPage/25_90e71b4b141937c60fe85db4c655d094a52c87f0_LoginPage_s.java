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
  * @date 27th September 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.server.pages;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Login form page.
  */
 public class LoginPage extends AbstractPage
 {
     private String message;
     
     public LoginPage()
     {
         super();
     }
     
     public LoginPage(String message)
     {
         super();
         this.message = message;
     }
     
     @Override
     public void preService(HttpServletRequest req)
     {
         this.framing = false;
     }
     
     @Override
     public void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException
     {
        this.pageBeginning();
        
        this.println("<div id='loginformoutercontainer'>");
        
        if (this.message != null)
        {
            this.println("<div id='loginfail' class='ui-state ui-state-error ui-corner-all'>");
            this.println("   <span class='ui-icon ui-icon-alert detailspanelicon'> </span>");
            this.println("   " + this.message);
            this.println("</div>");
        }
        
        
        this.println("<form id='loginform' method='POST'>");
        
        this.println("<div id='loginformcontainer' class='detailspanel ui-corner-all'>");
        this.println("   <div class='detailspaneltitle'>");
        this.println("      <p>");
        this.println("          <span class='detailspanelicon ui-icon ui-icon-key'> </span>");
        this.println("          Login:");
        this.println("      </p>");
        this.println("   </div>");
        this.println("   <div class='detailspanelcontents'>");
 
        this.println("  <div class='forminput'>\n" +
                     "    <div class='formlabel'>Username:</div>\n" +
                     "    <div class='forminut'><input id='usernamefield' class='validate[required]' type='text' name='username' /></div>\n" +
                     "  </div>");
        this.println("  <div class='forminput'>\n" +
                     "    <div class='formlabel'>Password:</div>\n" +
                     "    <div class='forminut'><input id='passwordfield' class='validate[required]' type='password' name='password' /></div>\n" +
                     "  </div>");
 
        this.println("   </div>");
        this.println("</div>");
        
        this.println("  <div id='loginsubmit'><input id='subbutton' type='submit' name='submit' value='Login' /></div>");
        
        this.println("</form>");
        this.println("</div>");
 
        this.println("<script type='text/javascript'>");
        this.println(
                "$(document).ready(function() {");
        
        /* Form positioning at center of screen. */
        this.println(
                "   var leftpos = Math.floor($(window).width() / 2) - 175;\n" +
               "   var toppos = Math.floor($(window).height() / 2) - 200;\n" +
                "   $('#loginformoutercontainer').css('left', leftpos);\n" +
                "   $('#loginformoutercontainer').css('top', toppos)\n");
        
        /* Form validation. */
        this.println("   $('#loginform').validationEngine();\n");
       
        /* Form styling. */
        this.println(
                "   $('#loginform').jqTransform();\n" + 
                "   $('.jqTransformInputWrapper').css('margin-top', '8px').css('width', '200px');\n" +
                "   $('#subbutton').removeClass('jqTransformButton').button();\n");
        
        this.println("});");
        this.println("</script>");
 
        this.pageEnd();
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
         return "Login page.";
     }
 
 }
