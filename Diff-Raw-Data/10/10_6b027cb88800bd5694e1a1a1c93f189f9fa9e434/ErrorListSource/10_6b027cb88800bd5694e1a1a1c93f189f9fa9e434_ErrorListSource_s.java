 package com.xaf.value;
 
 /**
  * Title:        The Extensible Application Platform
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      Netspective Communications Corporation
  * @author Shahid N. Shah
  * @version 1.0
  */
 
 import com.xaf.form.*;
 import com.xaf.form.field.*;
 
 public class ErrorListSource extends ListSource
 {
     String errorMessage;
 
     public ErrorListSource(Exception e)
     {
         errorMessage = e.toString();
     }
 
     public ErrorListSource(String msg)
     {
         errorMessage = msg;
     }
 
     public void initializeSource(String srcParams)
     {
 		super.initializeSource(srcParams);
         SelectChoicesList choices = new SelectChoicesList();
         choices.add(new SelectChoice("Error creating ListSource: " + errorMessage + " (" + srcParams + ")"));
 		setChoices(choices);
     }
 }
