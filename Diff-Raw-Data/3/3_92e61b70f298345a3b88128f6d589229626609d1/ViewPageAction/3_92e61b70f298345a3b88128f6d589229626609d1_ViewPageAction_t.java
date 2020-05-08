 /**
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Wiki.
  *
  * The Initial Developer of the Original Code is Technology Concepts
  * and Design, Inc.
  * Copyright (C) 2000 Technology Concepts and Design, Inc.  All
  * Rights Reserved.
  *
  * Contributor(s): Lane Sharman (OpenDoors Software)
  *                 Justin Wells (Semiotek Inc.)
  *                 Eric B. Ridge (Technology Concepts and Design, Inc.)
  *
  * Alternatively, the contents of this file may be used under the
  * terms of the GNU General Public License Version 2 or later (the
  * "GPL"), in which case the provisions of the GPL are applicable
  * instead of those above.  If you wish to allow use of your
  * version of this file only under the terms of the GPL and not to
  * allow others to use your version of this file under the MPL,
  * indicate your decision by deleting the provisions above and
  * replace them with the notice and other provisions required by
  * the GPL.  If you do not delete the provisions above, a recipient
  * may use your version of this file under either the MPL or the
  * GPL.
  *
  *
  * This product includes sofware developed by OpenDoors Software.
  *
  * This product includes software developed by Justin Wells and Semiotek Inc.
  * for use in the WebMacro ServletFramework (http://www.webmacro.org).
  */
 
 package org.tcdi.opensource.wiki.servlet;
 
 import java.util.*;
 
 import org.webmacro.servlet.WebContext;
 
 import org.tcdi.opensource.wiki.*;
 
 /**
  * This Action allows pages to be viewed.<p>
  *
  * If no page name can be determined <i>StartPage</i> property
  * of the specified WikiSystem is used.<p>
  *
  * By default, pages are viewed using the <i>ViewPageAction.Template</i>
  * specified by the WikiSystem, however, one can override the template
  * on a page-by-page basis by specifying properties in the form of:<pre>
  *   <page name>.Template
  * </pre>
  *
  * @author  e_ridge
  */
 public class ViewPageAction implements PageAction {
 
     /**
      * A ViewPage action can only happen if there are no http request parameters
      * and if the request is a GET request
      */
     public boolean accept(WikiSystem wiki, WebContext wc, WikiUser user) {
         Enumeration enum = wc.getRequest().getParameterNames();
 
         // don't accept if we have request parameters
         if (enum.hasMoreElements())
             return false;
         
         // and then only accept if this is a get request
         return wc.getRequest().getMethod().equalsIgnoreCase("GET");
     }
     
     /**
      * do whatever this action is supposed to do to the specified page
      */
     public void perform(WikiSystem wiki, WebContext wc, WikiUser user, WikiPage page) throws PageActionException {
         wc.put ("Page", page);
     }
     
     /**
      * which WebMacro template does this action use?<p>
      *
      * If it is determined that the WikiSystem has defined a unique template
      * for the specified page, that template is used instead of the default
      * <i>ViewPageAction.Template</i>.
      */
     public String getTemplateName(WikiSystem wiki, WikiPage page) {
         Properties props = wiki.getProperties();
 	String template = props.getProperty ("ViewPageAction.Template");
         if (page != null) {
            if (props.getProperty (page.getTitle()) != null)
               template = props.getProperty (page.getTitle());
         } 
 
 	return template;
     }
     
     /**
      * based on the specified WikiSystem and WebContext, which WikiPage is
      * this action going to be dealing with?
      */
     public String getWikiPageName(WikiSystem wiki, WebContext wc) {
         String uri = wc.getRequest().getRequestURI ();
         String name = WikiUtil.formatAsWikiTitle(uri.substring (uri.lastIndexOf('/')+1));
         if (name.length() == 0)
             return wiki.getProperties().getProperty ("StartPage");
         else
             return name;
     }
 }
