 /*
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
 
 package org.openmrs.module.emr;
 
 import org.openmrs.ui.framework.BasicUiUtils;
 import org.openmrs.ui.framework.FormatterImpl;
 
 /**
  * Implementation of UiUtils suitable for use in non-context-sensitive unit tests.
  * This doesn't have a MessageSource configured, so it won't do localization
  */
 public class TestUiUtils extends BasicUiUtils {
 
     public TestUiUtils() {
        this.formatter = new FormatterImpl(null, null);
     }
 
     @Override
     public String message(String code, Object... args) {
         String ret = code;
         if (args.length > 0) {
             ret += ":";
         }
         ret += TestUtils.join(args, ",");
         return ret;
     }
 
 }
