 /*
  * Copyright 2009 Andrew Pietsch 
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you 
  * may not use this file except in compliance with the License. You may 
  * obtain a copy of the License at 
  *      
  *      http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing permissions 
  * and limitations under the License. 
  */
 
 package com.pietschy.gwt.pectin.demo.client.activity;
 
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.pietschy.gwt.pectin.demo.client.domain.Person;
 import com.pietschy.gwt.pectin.demo.client.misc.AbstractDemo;
 
 /**
  * Created by IntelliJ IDEA.
  * User: andrew
  * Date: Jun 19, 2008
  * Time: 1:42:26 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ActivityDemo extends AbstractDemo
 {
    // just a dummy for the demo..
    private SaveServiceAsync saveService = new SaveServiceAsync();
 
    public ActivityDemo()
    {
       // create a person to edit ...
       Person person = new Person();
 
       // set the model.
       EditPersonController editController = new EditPersonController(person, saveService);
 
       // build a panel the form and add a save button
       FlowPanel panel = new FlowPanel();
       editController.go(panel);
 
       addBlurbParagraph("This is a \"putting it all together\" example using Activities with validated FormModels " +
                         "calling an async service.");
 
       addLinkToSource("Show Controller Source", EditPersonController.class);
      addLinkToSource("Show Save Activity Source", EditPersonController.class);
       addLinkToModel(EditPersonModel.class);
       addLinkToView(EditPersonForm.class);
 
       setMainContent(panel);
    }
 
 }
