 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package ibeans.client;
 
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.Style;
 
 /**
  * TODO
  */
 public class AboutPanel extends LayoutContainer
 {
 
     public AboutPanel() {
         final Dialog simple = new Dialog();
         simple.setHeading("About...");
         simple.setButtons(Dialog.OK);
         simple.setBodyStyleName("pad-text");
         simple.add(getAboutText());
         simple.setScrollMode(Style.Scroll.AUTO);
         simple.setHideOnButtonClick(true);
         simple.setWidth(500);
         simple.show();
     }
 
     // FIXME:
     // we need version info, license info, support info, etc...
     public Html getAboutText() {
         Html html = new Html();
         html.setHtml("<div style='padding: 6px;'> <h1 class=\"welcome-h1\">Mule iBeans</h1>\n" +
                 "\n" +
                 "    Web Application integration made easy. Copyright 2009, MuleSoft, Inc. All rights reserved\n" +
                 "<p>&nbsp;</p>\n" +
                 "    <p>\n" +
 
                "        <ul><li>Getting start with iBean <a href='http://www.mulesoft.org/display/IBEANS/Home' target='_blank'>go here</a></li>" +
                 "        <ul><li>For the Mule and iBeans community forums <a href='http://mulesoft.org' target='_blank'>go here</a></li>" +
                 "        <ul><li>To get an iBeans community account <a href='http://www.mulesoft.org/register/newuser.mule' target='_blank'>go here</a></li>" +
                 "<li>For bug reports and feature requests <a href='http://www.mulesoft.org/jira/browse/IBEANS' target='_blank'>go here</a></li>" +
                 "<li>For MuleSoft commercial support <a href='http://www.mulesoft.com/subscriptions-tcat-server-and-apache-tomcat' target='_blank'>go here</a></li>" +
                 "    </ul>\n" +
                 "\n" +
                 "</div>");
 
         return html;
     }
 
 }
