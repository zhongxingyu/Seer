 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.feedback.extension.html;
 
//~--- non-JDK imports --------------------------------------------------------

 import org.openmrs.module.web.extension.HeaderIncludeExt;
 
//~--- JDK imports ------------------------------------------------------------

 import java.util.ArrayList;
 import java.util.List;
 
 public class HeaderInclude extends HeaderIncludeExt {
     public List<String> getHeaderFiles() {
         List<String> myHeaderFiles = new ArrayList<String>();
 
        myHeaderFiles.add("/scripts/jquery/jquery.min.js");
        myHeaderFiles.add("/scripts/jquery-ui/js/jquery-ui.custom.min.js");

        myHeaderFiles.add("/moduleResources/feedback/fdbk.js");
        myHeaderFiles.add("/moduleResources/feedback/wizard/jquery.ba-bbq.min.js");
        myHeaderFiles.add("/moduleResources/feedback/wizard/jquery.form.js");
        myHeaderFiles.add("/moduleResources/feedback/wizard/jquery.form.wizard-min.js");
        myHeaderFiles.add("/moduleResources/feedback/wizard/jquery.validate.js");
        myHeaderFiles.add("/moduleResources/feedback/wizard/BrowserDetect.js");

        myHeaderFiles.add("/moduleResources/feedback/jsFeedback/feedback.css");
        myHeaderFiles.add("/moduleResources/feedback/jsFeedback/html2canvas.js");
        myHeaderFiles.add("/moduleResources/feedback/jsFeedback/jsfeedback.js");
        myHeaderFiles.add("/moduleResources/feedback/jsFeedback/jquery.blockUI.js");

         return myHeaderFiles;
     }
 }
 
//~ Formatted by Jindent --- http://www.jindent.com
