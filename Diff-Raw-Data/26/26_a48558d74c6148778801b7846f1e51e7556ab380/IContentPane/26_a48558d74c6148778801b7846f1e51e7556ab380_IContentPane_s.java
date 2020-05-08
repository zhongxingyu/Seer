 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.component;
 
 public interface IContentPane extends IMobiComponent{
     
    public static final String CONTENT_BASE_CLASS = "mobi-contentpane ui-body-c ";
     public static final String CONTENT_HIDDEN_CLASS = "mobi-contentpane-hidden ";
    public static final String CONTENT_SINGLE_BASE_CLASS = "mobi-contentpane-single ui-body-c";
     public static final String CONTENT_SINGLE_HIDDEN_CLASS = "mobi-contentpane-single-hidden";
     public static final String CONTENT_SINGLE_MENUPANE_CLASS = "mobi-contentpane-single-menu-hidden";
         
     public boolean isAccordionPane();
     public IAccordion getAccordionParent();
     public boolean isStackPane();
     public boolean isTabPane();
     public boolean isClient();
     public String getId();
     public String getTitle();
     public void setTitle(String title);
   //  public String getJSSrc();
 }
