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
 
 package edu.wisc.my.portlets.bookmarks.domain.validation;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 
 import edu.wisc.my.portlets.bookmarks.domain.Bookmark;
 
 /**
  * Validates a Bookmark, requires a valid URL, determined by calling new URL on the URL String. Also
  * appends a default protocol of http:// if it isn't already there. The default protocol the validator
  * uses may be set via the defaultProtocol property.
  * 
  * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
  * @version $Revision$
  */
 public class BookmarkValidator extends EntryValidator {
     private static final String PROTOCOL_SEPERATOR = "://";
 
 
     private String defaultProtocol = "http://";
     
     /**
      * @return Returns the defaultProtocol.
      */
     public String getDefaultProtocol() {
         return this.defaultProtocol;
     }
 
     /**
      * @param defaultProtocol The defaultProtocol to set.
      */
     public void setDefaultProtocol(String defaultProtocol) {
         if (defaultProtocol == null) {
             throw new IllegalArgumentException("defaultProtocol may not be null.");
         }
         if (!defaultProtocol.endsWith(PROTOCOL_SEPERATOR)) {
             throw new IllegalArgumentException("defaultProtocol must end with a the protocol seperator='" + PROTOCOL_SEPERATOR + "'.");
         }
         
         this.defaultProtocol = defaultProtocol;
     }
 
     /**
      * @see edu.wisc.my.portlets.bookmarks.domain.validation.EntryValidator#supports(java.lang.Class)
      */
     @Override
     public boolean supports(Class clazz) {
         return Bookmark.class.isAssignableFrom(clazz) && super.supports(clazz);
     }
 
     /**
      * @see edu.wisc.my.portlets.bookmarks.domain.validation.EntryValidator#validate(java.lang.Object, org.springframework.validation.Errors)
      */
     @Override
     public void validate(Object obj, Errors errors) {
         super.validate(obj, errors);
         final Bookmark bookmark = (Bookmark)obj;
         this.validateUrl(bookmark, errors);
     }
 
     private void validateUrl(Bookmark bookmark, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "url", "portlet.bookmark.error.url.required");
 
         String url = bookmark.getUrl();
         if (!url.contains(PROTOCOL_SEPERATOR)) {
             url = this.defaultProtocol + url;
         }
         
         bookmark.setUrl(url);
 
         try {
             new URL(url);
         }
         catch (MalformedURLException mue) {
            errors.rejectValue("url", "portlet.bookmark.error.url.malformed");
         }
     }
 }
