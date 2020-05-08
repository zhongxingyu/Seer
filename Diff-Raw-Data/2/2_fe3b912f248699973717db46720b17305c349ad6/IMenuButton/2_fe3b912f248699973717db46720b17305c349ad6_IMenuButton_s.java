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
 
 /*
   MenuButton is a html select with javascript to reset after use on value change
   and for ajax submit
  */
 public interface IMenuButton extends IMobiComponent  {
     public static final String BASE_STYLE_CLASS = "mobi-menu-btn";
    public static final String BUTTON_STYLE_CLASS = "mobi-menu-btn-btn";
     public static final String DISABLED_STYLE_CLASS = "mobi-button-dis";
     public static final String MENU_SELECT_CLASS =  "mobi-menu-btn-menu";
 
     public void setButtonLabel(String label);
     public String getButtonLabel();
 
     public void setSelectTitle(String title);
     public String getSelectTitle();
 
     public String getName();
 
 
 }
