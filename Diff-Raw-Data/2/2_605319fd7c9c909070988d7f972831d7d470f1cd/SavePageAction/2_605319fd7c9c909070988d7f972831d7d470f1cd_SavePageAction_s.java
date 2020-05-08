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
  * Saves the WikiPage that the user modified.  If the page doesn't exist, it
  * is created.  Otherwise, the existing page is saved as <PageName>.<oldversion>
  * and this page is saved in its place as <PageName>.
  *
  * @author  e_ridge
  */
 public class SavePageAction implements PageAction {
 
     /**
      * can only save a page if the request is POST and "?save=<pagename>" is
      * in the request
      */
     public boolean accept(WikiSystem wiki, WebContext wc, WikiUser user) {
         // and then only accept if this is a get request
         return wc.getRequest().getMethod().equalsIgnoreCase("POST") 
             && wc.getForm ("save") != null;
     }
     
     /**
      * do the saving of the page.  When we're done, we redirect to the page
      * so the user can view his changes.
      */
     public void perform(WikiSystem wiki, WebContext wc, WikiUser user, WikiPage page) throws PageActionException {
         if (page != null && page.getIsModerated() && !user.getIsModerator())
             throw new PageActionException ("This page can only be saved by moderators");
         
         try {
             if (page == null)
                 page = createNewPage (wiki, wc, user);
             else
                 modifyExistingPage (wiki, wc, user, page);
         } catch (Exception e) {
             e.printStackTrace();
             throw new PageActionException (e.toString());
         }
         
         throw new RedirectException (page.getTitle());
     }
     
     protected WikiPage createNewPage (WikiSystem wiki, WebContext wc, WikiUser user) throws Exception {
         // get the page elements from the request
         String editor = user.getIdentifier();
         String text = wc.getForm ("TEXT");
        boolean moderated = wc.getForm ("MODERATED").equals ("true");
         String keywords = wc.getForm ("RELATED_TITLES");
         String pageName = wc.getForm ("save");
         
         // create the page
         System.err.println ("pageName: " + pageName);
         WikiPage newPage = wiki.createPage (pageName, editor, text);
         newPage.setRelatedTitles (keywordsToStringArray(keywords));
         newPage.setIsModerated(moderated);
         System.err.println ("pageName: " + newPage.getTitle());
         // make sure to save the page
         wiki.savePage (newPage);
         
         return newPage;        
     }
     
     protected void modifyExistingPage (WikiSystem wiki, WebContext wc, WikiUser user, WikiPage page) throws Exception {
         // get the page elements from the request
         String text = wc.getForm ("TEXT");
         String editor = user.getIdentifier();
         boolean moderated = wc.getForm ("MODERATED") != null && wc.getForm ("MODERATED").equals ("true");
         String keywords = wc.getForm ("RELATED_TITLES");
         String pageName = wc.getForm ("save");
         
         // make sure the page wasn't modified by somebody else
         long version = Long.parseLong (wc.getForm ("VERSION"));
         if (version != page.getVersion())
             throw new ConcurrentModificationException (text);
         
         page.addEditor(editor);
         page.setUnparsedData(text);
         page.setIsModerated(moderated);
         page.setRelatedTitles(keywordsToStringArray(keywords));
 
         // parse the page and save it
         wiki.parsePage(page);
         wiki.savePage (page);
     }
     
     /**
      * no template for saving
      */
     public String getTemplateName(WikiSystem wiki, WikiPage page) {
         return null;
     }
     
     /**
      * the page name is whatever is behind the "?save=<pagename>" request
      * parameter.
      */
     public String getWikiPageName(WikiSystem wiki, WebContext wc) {
         return wc.getForm ("save");
     }
 
     private String[] keywordsToStringArray (String input) {
         List titles = new ArrayList ();
         StringTokenizer st = new StringTokenizer(input);
         StringBuffer keyword = new StringBuffer();
         while (st.hasMoreElements()) {
             String tmp = (String) st.nextElement();
             int size = tmp.length();
             for (int x=0; x<size; x++) {
                 char ch = tmp.charAt(x);
                 if (Character.isLetterOrDigit(ch))
                     keyword.append(ch);
             }
             titles.add (keyword.toString());
             keyword.setLength(0);
         }
         
         return (String[]) titles.toArray(new String[0]);
     }
 }
