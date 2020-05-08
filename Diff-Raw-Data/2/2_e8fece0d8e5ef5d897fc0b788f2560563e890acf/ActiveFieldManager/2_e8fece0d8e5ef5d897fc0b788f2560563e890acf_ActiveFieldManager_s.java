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
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.component.CookieProvider;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 
 /**
  * Field manager to contain active UI fields, and to provide control over
  * the context menu items they offer.
  */
 public class ActiveFieldManager extends VerticalFieldManager {
     /**
      * Indicates that the user has highlighted an E-Mail address and
      * selected the menu option to compose a message to that address.
      * The address itself should be available via {@link #getSelectedToken()}.
      */
     public final static int ACTION_SEND_EMAIL = 0x00000001;
 
     private static String VERBMENUITEM_CLASS = "net.rim.device.apps.api.ui.VerbMenuItem";
     private static String EMAILCOOKIE_CLASS = "net.rim.device.apps.internal.blackberryemail.address.EmailAddressModelImpl";
     
     private String selectedToken;
 
     /**
      * Constructs a new active field manager.
      */
     public ActiveFieldManager() {
         super();
     }
     
     /**
      * Constructs a new active field manager with the provided style.
      *
      * @param style Styles(s) for this manager.
      */
    public ActiveFieldManager(int style) {
         super(style);
     }
 
     /**
      * Gets the selected token from contained field whose menu item was
      * just clicked on.  This method should only be called from within
      * {@link FieldChangeListener#fieldChanged(net.rim.device.api.ui.Field, int)}
      * to ensure that it contains the desired contents.
      * 
      * @return the selected token
      */
     public String getSelectedToken() {
         return selectedToken;
     }
 
     /**
      * Creates the menu for this field.
      * This implementation needs to resort to a lot of trickery so that
      * invalid or inapplicable menu items from the browser field are not
      * provided to the user.
      * 
      * @see net.rim.device.api.ui.Manager#makeMenu(net.rim.device.api.ui.component.Menu, int)
      */
     protected void makeMenu(Menu menu, int instance) {
         int size = menu.getSize();
         for(int i=0; i<size; i++) {
             MenuItem item = menu.getItem(i);
             if(item instanceof EmailMenuItem) { continue; }
 
             Field field = getFieldWithFocus();
             
             if(field instanceof CookieProvider
                     && VERBMENUITEM_CLASS.equals(item.getClass().getName())) {
                 
                 Object cookie = ((CookieProvider)field).getCookieWithFocus();
                 if(cookie == null) { continue; }
                 
                 String address = getAddressFromCookie(cookie);
                 
                 if(address != null) {
                     MenuItem emailMenuItem =
                         new EmailMenuItem(
                                 item.toString(),
                                 item.getOrdinal(),
                                 item.getPriority(),
                                 address);
                     menu.add(emailMenuItem);
 
                     menu.deleteItem(i);
                     i = 0;
                     size = menu.getSize();
 
                     menu.setDefault(emailMenuItem);
                 }
             }
         }
         super.makeMenu(menu, instance);
     }
 
     private static String getAddressFromCookie(Object cookie) {
         String address = null;
         if(cookie instanceof Object[]) {
             Object[] cookieArray = (Object[])cookie;
             for(int j=0; j<cookieArray.length; j++) {
                 address = getAddressFromCookieItem(cookieArray[j]);
                 if(address != null) { break; }
             }
         }
         else {
             address = getAddressFromCookieItem(cookie);
         }
         return address;
     }
     
     private static String getAddressFromCookieItem(Object cookie) {
         String result = null;
         if(EMAILCOOKIE_CLASS.equals(cookie.getClass().getName())) {
             String address = cookie.toString();
             if(address != null && address.indexOf('@') != -1) {
                 result = address;
             }
         }
         return result;
     }
     
     /**
      * Special menu item that acts as a direct substitute for the framework
      * provided menu item that launches the E-Mail composer.
      */
     private class EmailMenuItem extends MenuItem {
         private String address;
         
         public EmailMenuItem(String text, int ordinal, int priority, String address) {
             super(text, ordinal, priority);
             this.address = address;
         }
 
         public void run() {
             ActiveFieldManager.this.selectedToken = address;
             ActiveFieldManager.this.fieldChangeNotify(
                     FieldChangeListener.PROGRAMMATIC | ActiveFieldManager.ACTION_SEND_EMAIL);
         }
     }
 }
