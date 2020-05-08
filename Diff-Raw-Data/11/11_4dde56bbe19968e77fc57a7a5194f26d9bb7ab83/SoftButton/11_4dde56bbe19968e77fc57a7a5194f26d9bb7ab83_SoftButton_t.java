 /*
  * SoftButton.java
  *
  * Created on July 18, 2007, 7:16 PM
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package com.totsp.gwittir.client.ui;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FocusListener;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.KeyboardListener;
 import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.MouseListenerAdapter;
 import com.google.gwt.user.client.ui.Widget;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  *
  * @author cooper
  */
 public class SoftButton extends Button {
     
     private FocusPanel softBase;
     private Widget content;
     private ClickListener listener;
     private boolean enabled;
     private ArrayList styleNames = new ArrayList();
     
     public SoftButton(){
         super();
     }
     
     public SoftButton(String label){
         super(label);
     }
     
     protected void init() {
         this.softBase = new FocusPanel();
         DOM.setStyleAttribute( this.softBase.getElement(), "display", "inline");
         this.content = new Label();
         this.softBase.setWidget( content );
         final SoftButton instance = this;
         listener = new ClickListener() {
             public void onClick(Widget sender) {
                 GWT.log( "Clicked "+getAction(), null);
                 if(enabled && getAction() != null) {
                     getAction().execute(instance);
                 }
             }
         };
         this.addFocusListener( new FocusListener(){
             public void onLostFocus(Widget sender) {
                 removeStyleName("focussed");
             }
             
             public void onFocus(Widget sender) {
                 addStyleName("focussed");
             }
             
         });
         this.softBase.addClickListener(listener);
         this.softBase.addMouseListener( new MouseListenerAdapter() {
             public void onMouseUp(Widget sender, int x, int y) {
                 GWT.log( "Up", null);
                 removeStyleName("pressed");
             }
             
             public void onMouseDown(Widget sender, int x, int y) {
                 GWT.log("Press", null);
                 addStyleName("pressed");
             }
             
         });
         this.softBase.addKeyboardListener( new KeyboardListenerAdapter(){
             public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                 if( keyCode == ' ' || keyCode == KeyboardListener.KEY_ENTER ){
                     if(enabled && getAction() != null) {
                         getAction().execute(instance);
                     }
                 }
                 removeStyleName("pressed");
             }
 
             public void onKeyUp(Widget sender, char keyCode, int modifiers) {
                 removeStyleName("pressed");
             }
 
             public void onKeyDown(Widget sender, char keyCode, int modifiers) {
                 addStyleName("pressed");
             }
             
             
         });
         this.setRenderer(new ToStringRenderer());
         this.initWidget(this.softBase);
         this.setStyleName( "gwittir-SoftButton");
         this.setEnabled( true );
     }
     
     public void setWidth(String width) {
         this.softBase.setWidth(width);
     }
     
     public void setTitle(String title) {
         this.softBase.setTitle(title);
     }
     
     public void setText(String text) {
         GWT.log( "Setting text "+text, null);
         if( this.content instanceof Label ){
             GWT.log( "Label text "+text, null);
             ((Label) this.content).setText( text );
         } else {
             GWT.log( "New Label text "+text, null);
             this.setContent(new Label( text ));
         }
         
     }
     
     public void setHTML(String html) {
         if( this.content instanceof HTML ){
             ((HTML) this.content).setHTML( html );
         } else {
             this.setContent(new HTML( html ));
         }
     }
     
     public void addKeyboardListener(KeyboardListener listener) {
         this.softBase.addKeyboardListener(listener);
     }
     
     public void removeKeyboardListener(KeyboardListener listener) {
         this.softBase.removeKeyboardListener(listener);
     }
     
     public void addClickListener(ClickListener listener) {
         this.softBase.addClickListener(listener);
     }
     
     public void removeClickListener(ClickListener listener) {
         this.softBase.removeClickListener(listener);
     }
     
     public void setTabIndex(int index) {
         this.softBase.setTabIndex(index);
     }
     
     public void setFocus(boolean focused) {
         this.softBase.setFocus(focused);
     }
     
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
         if( !this.enabled ){
             this.content.addStyleName("disabled");
         } else {
             this.content.removeStyleName("disabled");
         }
     }
     
     public void setAccessKey(char key) {
         this.softBase.setAccessKey(key);
     }
     
     public void addFocusListener(FocusListener listener) {
         this.softBase.addFocusListener(listener);
     }
     
     public void removeFocusListener(FocusListener listener) {
         this.softBase.removeFocusListener(listener);
     }
     
     public void setPixelSize(int width, int height) {
         this.softBase.setPixelSize(width, height);
     }
     
     public void setSize(String width, String height) {
         this.softBase.setSize(width, height);
     }
     
     public String getHTML() {
         if( this.content instanceof HTML ){
             return ((HTML) this.content).getHTML();
         } else {
             return ((Label) this.content).getText();
         }
     }
     
     public int getTabIndex() {
         int retValue;
         
         retValue = this.softBase.getTabIndex();
         return retValue;
     }
     
     public String getText() {
         if( this.content instanceof HTML ){
             return ((HTML) this.content).getHTML();
         } else {
             return ((Label) this.content).getText();
         }
     }
     
     public String getTitle() {
         String retValue;
         
         retValue = this.softBase.getTitle();
         return retValue;
     }
     
     public int hashCode() {
         int retValue;
         
         retValue = this.softBase.hashCode();
         return retValue;
     }
     
     public boolean isEnabled() {
         return this.enabled;
     }
     
     public void removeStyleName(String style) {
         this.styleNames.remove( style );
         this.content.removeStyleName(style);
     }
     
     public void setStyleName(String style) {
         this.styleNames = new ArrayList();
         styleNames.add( style );
         this.content.setStyleName(style);
     }
     
     public String getStyleName() {
         String retValue;
         retValue = this.content.getStyleName();
         return retValue;
     }
     
     public void addStyleName(String style) {
         this.styleNames.add( style );
         this.content.addStyleName(style);
     }
     
     public Widget getContent(){
         return this.content;
     }
     
     public void setContent(Widget w){
         for(Iterator it = this.styleNames.iterator(); it.hasNext(); ){
             w.addStyleName( (String) it.next() );
         }
         this.content = w;
        this.softBase.clear();
        this.softBase.setWidget( this.content );
     }
     
 }
