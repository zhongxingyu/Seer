 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.vis.gwt.inputdata.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.DOM;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.types.Encoding;
 import com.smartgwt.client.types.Side;
 import com.smartgwt.client.types.VerticalAlignment;
 import com.smartgwt.client.widgets.IButton;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.ButtonItem;
 import com.smartgwt.client.widgets.form.fields.FormItem;
 import com.smartgwt.client.widgets.form.fields.TextAreaItem;
 import com.smartgwt.client.widgets.form.fields.TextItem;
 import com.smartgwt.client.widgets.form.fields.UploadItem;
 import com.smartgwt.client.widgets.layout.VLayout;
 import com.smartgwt.client.widgets.tab.Tab;
 import com.smartgwt.client.widgets.tab.TabSet;
 import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
 import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;
 
 /**
  *
  * @author Boris Capitanu
  *
  */
 
 public class InputData implements EntryPoint {
 
     public void onModuleLoad() {
         final String contextPath = DOM.getElementById("__component_contextPath").getAttribute("content");
 
         final Tab[] selectedTabs = new Tab[1];
 
         final TabSet tabSet = new TabSet();
         tabSet.setTabBarPosition(Side.TOP);
         tabSet.addTabSelectedHandler(new TabSelectedHandler() {
             public void onTabSelected(TabSelectedEvent event) {
                 selectedTabs[0] = event.getTab();
             }
         });
 
         final VLayout vLayout = new VLayout();
         vLayout.setWidth100();
         vLayout.setHeight100();
         vLayout.setMargin(20);
         vLayout.setMembersMargin(10);
 
         final DynamicForm formUrls = new DynamicForm();
         formUrls.setCellSpacing(5);
         formUrls.setAction(contextPath + "?action=urls");
         final FormItem[] fiUrls = new FormItem[getMaxUrlCount() + 1];
         for (int i = 0, iMax = getMaxUrlCount(); i < iMax; i++) {
             TextItem tiUrl = new TextItem();
             tiUrl.setWidth(350);
             tiUrl.setName("url_" + (i+1));
             tiUrl.setTitle("URL");
             fiUrls[i] = tiUrl;
             if (i > 0)
                 tiUrl.setVisible(false);
         }
         final ButtonItem biAddUrl = new ButtonItem();
         biAddUrl.setTitle("Add URL");
         biAddUrl.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
             public void onClick(com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
                 for (FormItem fi : fiUrls)
                     if (Boolean.FALSE.equals(fi.getVisible())) {
                         fi.show();
                         break;
                     }
             }
         });
         fiUrls[fiUrls.length-1] = biAddUrl;
         formUrls.setFields(fiUrls);
 
         final DynamicForm formText = new DynamicForm();
         formText.setNumCols(2);
         formText.setHeight100();
         formText.setAction(contextPath + "?action=text");
         TextAreaItem taText = new TextAreaItem();
         taText.setName("text");
         if (getMaxTextLength() > 0)
             taText.setLength(getMaxTextLength());
         taText.setColSpan(2);
         taText.setWidth("*");
         taText.setHeight("*");
         taText.setShowTitle(false);
         formText.setFields(taText);
 
         final DynamicForm formFiles = new DynamicForm();
         formFiles.setHeight100();
         formFiles.setEncoding(Encoding.MULTIPART);
         formFiles.setAction(contextPath + "?action=upload");
         final FormItem[] fiFiles = new FormItem[getMaxFileCount() + 1];
         for (int i = 0, iMax = getMaxFileCount(); i < iMax; i++) {
             UploadItem uiFile = new UploadItem();
             uiFile.setWidth(350);
             uiFile.setName("file_" + (i+1));
             uiFile.setTitle("File");
             fiFiles[i] = uiFile;
             if (i > 0)
                 uiFile.setVisible(false);
         }
         final ButtonItem biAddFile = new ButtonItem();
         biAddFile.setTitle("Add File");
         biAddFile.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
             public void onClick(com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
                 for (FormItem fi : fiFiles)
                     if (Boolean.FALSE.equals(fi.getVisible())) {
                         fi.show();
                         break;
                     }
             }
         });
         fiFiles[fiFiles.length-1] = biAddFile;
         formFiles.setItems(fiFiles);
 
         IButton btnSubmit = new IButton("Submit");
         btnSubmit.setLayoutAlign(Alignment.CENTER);
         btnSubmit.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                DynamicForm selectedForm = (DynamicForm)((VLayout)selectedTabs[0].getPane()).getMember(1);
                 selectedForm.submitForm();
             }
         });
         
         Label lblUrls = new Label();  
         lblUrls.setPadding(5);  
         lblUrls.setValign(VerticalAlignment.CENTER);  
         lblUrls.setWrap(true);  
         lblUrls.setIcon("[SKIN]/Dialog/say.png"); 
         lblUrls.setShowEdges(true);  
         lblUrls.setContents("<b>Instructions</b>" +
         		"<p>Type a URL into the textbox below, then press <i>Submit</i> to proceed." +
         		" (press <i>Add URL</i> if you want to add more URLs)" +
         		"<br><u>Note:</u> You can only insert up to " + getMaxUrlCount() + " URLs (as set in the component properties).</p>");
         
         Label lblText = new Label();  
         lblText.setPadding(5);  
         lblText.setValign(VerticalAlignment.CENTER);  
         lblText.setWrap(true);  
         lblText.setIcon("[SKIN]/Dialog/say.png"); 
         lblText.setShowEdges(true);  
         lblText.setContents("<b>Instructions</b>" +
                 "<p>Type or paste some text into the textbox, then press <i>Submit</i> to proceed." +
                ((getMaxTextLength() > 0) ?
                        "<br><u>Note:</u> You can only enter up to " + getMaxTextLength() + " characters (as set in the component properties)." : 
                        "") + 
                "</p>");
 
         Label lblFiles = new Label();  
         lblFiles.setPadding(5);  
         lblFiles.setValign(VerticalAlignment.CENTER);  
         lblFiles.setWrap(true);  
         lblFiles.setIcon("[SKIN]/Dialog/say.png"); 
         lblFiles.setShowEdges(true);  
         lblFiles.setContents("<b>Instructions</b>" +
                 "<p>Click <i>Add File</i> as many times as the number of files you want to submit, then for " +
                 "each entry select the desired file. Finally press <i>Submit</i> to proceed." +
                 "<br><u>Note:</u> You can only select up to " + getMaxFileCount() + " files (as set in the component properties).</p>");
         
         VLayout vLayoutUrl = new VLayout();
         vLayoutUrl.setMembersMargin(10);
         vLayoutUrl.addMember(lblUrls);
         vLayoutUrl.addMember(formUrls);
         
         VLayout vLayoutText = new VLayout();
         vLayoutText.setHeight100();
         vLayoutText.setMembersMargin(10);
         vLayoutText.addMember(lblText);
         vLayoutText.addMember(formText);
         
         VLayout vLayoutFiles = new VLayout();
         vLayoutFiles.setHeight100();
         vLayoutFiles.setMembersMargin(10);
         vLayoutFiles.addMember(lblFiles);
         vLayoutFiles.addMember(formFiles);
         
         Tab tabUrls = new Tab("URLs");
         tabUrls.setPane(vLayoutUrl);
 
         Tab tabText = new Tab("Text");
         tabText.setPane(vLayoutText);
 
         Tab tabFiles = new Tab("Files");
         tabFiles.setPane(vLayoutFiles);
 
         tabSet.setTabs(tabUrls, tabText, tabFiles);
 
         vLayout.addMember(tabSet);
         vLayout.addMember(btnSubmit);
 
         vLayout.draw();
     }
 
     private final native int getMaxUrlCount() /*-{
         return window.parent.__maxUrlCount;
     }-*/;
 
     private final native int getMaxFileCount() /*-{
         return window.parent.__maxFileCount;
     }-*/;
 
     private final native int getMaxTextLength() /*-{
         return window.parent.__maxTextLength;
     }-*/;
 }
