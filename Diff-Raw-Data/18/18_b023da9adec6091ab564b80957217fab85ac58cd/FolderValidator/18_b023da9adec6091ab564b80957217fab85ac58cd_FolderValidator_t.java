 /* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */
 
 package edu.wisc.my.portlets.bookmarks.domain.validation;
 
 import org.springframework.validation.Errors;
 
 import edu.wisc.my.portlets.bookmarks.domain.Folder;
 
 /**
  * Validates a Folder, currently there is nothing to validate beyond what the EntryValidator provides.
  * 
  * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
  * @version $Revision$
  */
 public class FolderValidator extends EntryValidator {
 
     /**
      * @see edu.wisc.my.portlets.bookmarks.domain.validation.EntryValidator#supports(java.lang.Class)
      */
     @Override
     public boolean supports(Class clazz) {
         return Folder.class.isAssignableFrom(clazz) && super.supports(clazz);
     }
 
     /**
      * @see edu.wisc.my.portlets.bookmarks.domain.validation.EntryValidator#validate(java.lang.Object, org.springframework.validation.Errors)
      */
     @Override
     public void validate(Object obj, Errors errors) {
        //Currently folders have no Folder specific fields to validate
         super.validate(obj, errors);
     }
 }
