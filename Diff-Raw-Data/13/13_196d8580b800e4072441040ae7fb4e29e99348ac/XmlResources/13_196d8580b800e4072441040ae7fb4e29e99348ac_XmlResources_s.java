 package org.jbundle.res.base.screen;
 /**
  * @(#)XmlResources.java  0.00 1-Jan-10 Don Corley
  *
 * Copyright © 2010 jbundle.org. All Rights Reserved.
 *   don@tourgeek.com
  */
 import java.util.*;
 
 /**
  * XmlResources - Resources.
 XML Ajax XSLT strings
  */
 public class XmlResources extends ListResourceBundle
 {
     public Object[][] getContents() {
         return contents;
     }
    static final Object[][] contents = {
      // LOCALIZE THIS
     {"Help", "Help"},
     {"Home", "Home"},
     {"Login", "Sign in"},
     {"Logout", "Sign out"},
     {"My home", "My home"},
     {"Settings", "Settings"},
     {"xmlHeader", "<top-menu>" + "\n" +
         "   <title>{title}</title>" + "\n" +
         "   <menu-item>" + "\n" +
         "       <name>{Home}</name>" + "\n" +
         "       <description>Home page</description>" + "\n" +
         "       <link>?menu=Main</link>" + "\n" +
         "       <image>Home</image>" + "\n" +
         "   </menu-item>" + "\n" +
         "   <menu-item>" + "\n" +
         "       <name>{My home}</name>" + "\n" +
         "       <description>My home</description>" + "\n" +
         "       <link>?menu=</link>" + "\n" +
         "       <image>MyHome</image>" + "\n" +
         "   </menu-item>" + "\n" +
         "   <help-item>" + "\n" +
         "       <menu-item>" + "\n" +
         "           <name>{loginDesc}</name>" + "\n" +
         "           <description>{loginDesc}</description>" + "\n" +
         "           <link>{loginLink}</link>" + "\n" +
         "           <image>{loginIcon}</image>" + "\n" +
         "       </menu-item>" + "\n" +
         "       <menu-item>" + "\n" +
         "           <name>{Settings}</name>" + "\n" +
         "           <description>View or change settings</description>" + "\n" +
         "           <link>?screen=.main.user.screen.UserPreferenceScreen&amp;java=no</link>" + "\n" +
         "           <image>Settings</image>" + "\n" +
         "       </menu-item>" + "\n" +
         "       <menu-item>" + "\n" +
         "           <name>{Help}</name>" + "\n" +
         "           <description>Help for the current screen</description>" + "\n" +
         "           <link>?help=&amp;{url}&amp;class={helpclass}</link>" + "\n" +
         "           <image>Help</image>" + "\n" +
         "       </menu-item>" + "\n" +
         "   </help-item>" + "\n" +
         "</top-menu>"},
     {"xmlNavMenu", " <navigation-menu>" + "\n" +
         "   <navigation-item>" + "\n" +
         "       <name>Home</name>" + "\n" +
         "       <description>Home</description>" + "\n" +
         "       <link>?menu=Main</link>" + "\n" +
         "       <image>Home</image>" + "\n" +
         "   </navigation-item>" + "\n" +
         "   <navigation-item>" + "\n" +
         "       <name>My home</name>" + "\n" +
         "       <description>My home</description>" + "\n" +
         "       <link>?menu=</link>" + "\n" +
         "       <image>MyHome</image>" + "\n" +
         "   </navigation-item>" + "\n" +
         "   <navigation-item>" + "\n" +
         "       <name>Settings</name>" + "\n" +
         "       <description>View or change settings</description>" + "\n" +
         "       <link>?screen=.main.user.screen.UserPreferenceScreen&amp;java=no</link>" + "\n" +
         "     <image>Settings</image>" + "\n" +
         "   </navigation-item>" + "\n" +
         "   <navigation-item>" + "\n" +
         "       <name>Sign in</name>" + "\n" +
         "       <id>Login</id>" + "\n" +
         "       <description>Display sign in screen</description>" + "\n" +
         "       <link>?screen=.main.user.screen.UserLoginScreen&amp;java=no</link>" + "\n" +
         "       <image>Login</image>" + "\n" +
         "   </navigation-item>" + "\n" +
         "   <navigation-item>" + "\n" +
         "       <name>Sign out</name>" + "\n" +
         "       <id>Logout</id>" + "\n" +
         "       <description>Sign out</description>" + "\n" +
         "       <link>?user=</link>" + "\n" +
         "       <image>Logoff</image>" + "\n" +
         "   </navigation-item>" + "\n" +
         "   <navigation-item>" + "\n" +
         "       <name>Help</name>" + "\n" +
         "       <description>Help for the current screen</description>" + "\n" +
         "       <link>?help=&amp;class=.base.screen.model.report.HelpScreen</link>" + "\n" +
         "       <image>Help</image>" + "\n" +
         "   </navigation-item>" + "\n" +
         "</navigation-menu>"}        // END OF MATERIAL TO LOCALIZE
     };
 }
