 /*-
  * Copyright (c) 2010, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.ui;
 
 import org.logicprobe.LogicMail.LogicMailResource;
 
 import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.component.StandardTitleBar;
 
 public class StandardScreenBB60 extends StandardScreen {
 
     public StandardScreenBB60(NavigationController navigationController, ScreenProvider screenProvider) {
         super(navigationController, screenProvider);
     }
 
     protected Field createTitleField() {
         StandardTitleBar titlebarField = new StandardTitleBar();
         titlebarField.addTitle(
                 resources.getString(LogicMailResource.APPNAME)
                 + " - "
                 + screenProvider.getTitle());
         titlebarField.addSignalIndicator();
         titlebarField.setPropertyValue(
                 StandardTitleBar.PROPERTY_BATTERY_VISIBILITY,
                 StandardTitleBar.BATTERY_VISIBLE_ALWAYS);
        titlebarField.setFont(Font.getDefault());
         return titlebarField;
     }
     
     protected void cleanupTitleField(Field titleField) {
         // System title field does not require cleanup
     }
 }
