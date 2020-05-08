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
 package org.icefaces.mobi.component.pagepanel;
 
 import org.icefaces.mobi.utils.Attribute;
 
 /**
  * Model data for PagePane component which is currently stateless.
  */
 public class PagePanel extends PagePanelBase {
 
     // base styles for the three page sections
     public static final String HEADER_CLASS = "mobi-pagePanel-header ui-header ";
     public static final String FOOTER_CLASS = "mobi-pagePanel-footer ui-footer ";
    public static final String BODY_CLASS = "mobi-pagePanel-body ";
     public static final String CTR_CLASS = "mobi-pagePanel-ctr";
 
     // style classes to remove header footer margins
     public static final String BODY_NO_HEADER_CLASS = "mobi-pagePanel-body-noheader";
     public static final String BODY_NO_FOOTER_CLASS = "mobi-pagePanel-body-nofooter";
 
     // facet names that define the three parts of a page
     public static final String HEADER_FACET = "header";
     public static final String BODY_FACET = "body";
     public static final String FOOTER_FACET = "footer";
 
     // pass through attributes for style and styleClass attributes.
     private Attribute[] commonAttributeNames = {
             new Attribute("style", null),
             new Attribute("styleClass", null)
     };
 
     public PagePanel() {
         super();
     }
 
     public Attribute[] getCommonAttributeNames() {
         return commonAttributeNames;
     }
 }
