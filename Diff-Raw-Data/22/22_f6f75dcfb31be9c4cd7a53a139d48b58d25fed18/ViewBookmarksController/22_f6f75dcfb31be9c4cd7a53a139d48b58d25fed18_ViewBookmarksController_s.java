 /*******************************************************************************
  * Copyright 2006, The Board of Regents of the University of Wisconsin System.
  * All rights reserved.
  *
  * A non-exclusive worldwide royalty-free license is granted for this Software.
  * Permission to use, copy, modify, and distribute this Software and its
  * documentation, with or without modification, for any purpose is granted
  * provided that such redistribution and use in source and binary forms, with or
  * without modification meets the following conditions:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *c
  * 3. Redistributions of any form whatsoever must retain the following
  * acknowledgement:
  *
  * "This product includes software developed by The Board of Regents of
  * the University of Wisconsin System."
  *
  *THIS SOFTWARE IS PROVIDED BY THE BOARD OF REGENTS OF THE UNIVERSITY OF
  *WISCONSIN SYSTEM "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
  *BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  *PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE BOARD OF REGENTS OF
  *THE UNIVERSITY OF WISCONSIN SYSTEM BE LIABLE FOR ANY DIRECT, INDIRECT,
  *INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  *LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  *PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  *LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  *OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  *ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 /* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */
 
 package edu.wisc.my.portlets.bookmarks.web;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 
 import org.springframework.web.portlet.ModelAndView;
 import org.springframework.web.portlet.mvc.AbstractController;
 
 import edu.wisc.my.portlets.bookmarks.domain.Bookmark;
 import edu.wisc.my.portlets.bookmarks.domain.BookmarkSet;
 import edu.wisc.my.portlets.bookmarks.domain.Folder;
 import edu.wisc.my.portlets.bookmarks.domain.Preferences;
 import edu.wisc.my.portlets.bookmarks.web.support.ViewConstants;
 import edu.wisc.my.portlets.bookmarks.web.support.BookmarkSetRequestResolver;
 import edu.wisc.my.portlets.bookmarks.web.support.PreferencesRequestResolver;
 
 /**
  * Controller resolves the BookmarkSet owner and name for the request and displays it
  * as the model.
  * 
  * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
  * @version $Revision$
  */
 public class ViewBookmarksController extends AbstractController {
     private BookmarkSetRequestResolver bookmarkSetRequestResolver;
     private PreferencesRequestResolver preferencesRequestResolver;
     
     
     /**
      * @return Returns the bookmarkSetRequestResolver.
      */
     public BookmarkSetRequestResolver getBookmarkSetRequestResolver() {
         return this.bookmarkSetRequestResolver;
     }
 
     /**
      * @param bookmarkSetRequestResolver The bookmarkSetRequestResolver to set.
      */
     public void setBookmarkSetRequestResolver(BookmarkSetRequestResolver bookmarkSetRequestResolver) {
         this.bookmarkSetRequestResolver = bookmarkSetRequestResolver;
     }
 
     /**
      * @return Returns the preferencesRequestResolver.
      */
     public PreferencesRequestResolver getPreferencesRequestResolver() {
         return this.preferencesRequestResolver;
     }
 
     /**
      * @param preferencesRequestResolver The preferencesRequestResolver to set.
      */
     public void setPreferencesRequestResolver(PreferencesRequestResolver preferencesRequestResolver) {
         this.preferencesRequestResolver = preferencesRequestResolver;
     }
 
 
     /**
      * @see org.springframework.web.portlet.mvc.AbstractController#handleRenderRequestInternal(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
      */
     @Override
     protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
         final BookmarkSet bookmarkSet = this.bookmarkSetRequestResolver.getBookmarkSet(request, false);
         final Preferences preferences = this.preferencesRequestResolver.getPreferences(request, false);
 
         final Map<String, Object> refData = new HashMap<String, Object>();
         refData.put(ViewConstants.BOOKMARK_SET, bookmarkSet);
        refData.put(ViewConstants.OPTIONS, preferences);
 
         refData.put(ViewConstants.COMMAND_EMPTY_BOOKMARK, new Bookmark());
         refData.put(ViewConstants.COMMAND_EMPTY_FOLDER, new Folder());
 
         return new ModelAndView("viewBookmarks", refData);
     }
 }
