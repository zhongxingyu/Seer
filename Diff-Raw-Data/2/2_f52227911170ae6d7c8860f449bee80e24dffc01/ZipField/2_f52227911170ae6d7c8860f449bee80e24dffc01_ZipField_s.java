 /*
  * Title:        The Extensible Application Platform
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      Netspective Communications Corporation
  * @author ThuA
  * @version 
  * Created on: Jul 26, 2001 3:59:10 PM
  */
 package com.xaf.form.field;
 
 public class ZipField extends TextField
 {
    static public final String PATTERN_MATCHZIP  = "^([\\d]{5})((-)([\\d]{4}))?";
     public ZipField()
     {
         super();
         // set the dafault regex pattern for the phone field
         setValidatePattern("/" + PATTERN_MATCHZIP + "/");
         setValidatePatternErrorMessage("Zip codes must be in the 12345 or 12345-1234 format.");
     }
 
 
 }
