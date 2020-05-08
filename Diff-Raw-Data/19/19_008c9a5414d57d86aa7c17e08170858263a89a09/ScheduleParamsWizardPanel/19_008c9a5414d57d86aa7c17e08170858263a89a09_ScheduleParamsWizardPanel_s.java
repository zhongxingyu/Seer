 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2008 Pentaho Corporation.  All rights reserved.
  *
  * @created Aug 8, 2008 
  * @author wseyler
  */
 package org.pentaho.gwt.widgets.client.wizards.panels;
 
 import org.pentaho.gwt.widgets.client.i18n.WidgetsLocalizedMessages;
 import org.pentaho.gwt.widgets.client.i18n.WidgetsLocalizedMessagesSingleton;
 import org.pentaho.gwt.widgets.client.wizards.AbstractWizardPanel;
 
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.CaptionPanel;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author wseyler
  *
  */
 public class ScheduleParamsWizardPanel extends AbstractWizardPanel {
 
   private static final WidgetsLocalizedMessages MSGS = WidgetsLocalizedMessagesSingleton.getInstance().getMessages();
   
   private static final String PENTAHO_SCHEDULE = "pentaho-schedule-create"; //$NON-NLS-1$
   
   boolean parametersComplete = true;
   CaptionPanel parametersCaptionPanel = new CaptionPanel(MSGS.parameters());
   Label scheduleDescription = new Label();
   Frame parametersFrame;
   String scheduledFilePath;  
 
   
   public ScheduleParamsWizardPanel(String scheduledFile) {
     super();
     scheduledFilePath = scheduledFile;
     layout();
     ScheduleParamsWizardPanel thisInstance = this;
     registerSchedulingCallbacks(thisInstance);
   }
 
   public void setScheduleDescription(String description) {
     scheduleDescription.setText(description);
   }
   
  /**
   * 
    */
  public native JsArray<JsSchedulingParameter> getParams() /*-{
    var params = $doc.getElementById('schedulerParamsFrame').contentWindow.getParams();
     var paramEntries = new Array();
     for (var key in params) {
       var type = null;
       var value = new Array();
       if (Object.prototype.toString.apply(params[key]) === '[object Array]') {
         var theArray = params[key];
         if (theArray.length > 0) {
            for(var i=0; i < theArray.length; i++) {
             if (typeof theArray[i] == 'number') {
               if (type == null) {
                 type = "number[]";
               }
               value.push('' + theArray[i]);
             } else if (typeof theArray[i] == 'boolean') {
               if (type == null) {
                 type = "boolean[]";
               }
               value.push(theArray[i] ? "true" : "false");
             } else if (typeof theArray[i] instanceof Date) {
               if (type == null) {
                 type = "date[]";
               }
               value.push(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(theArray[i]));
             } else if (typeof theArray[i] == 'string') {
               if (type == null) {
                 type = "string[]"
               }
               value.push(theArray[i]);
             } else if (theArray[i] == null) {
               value.push(null);
             }     
           }
         }
       } else if (typeof params[key] == 'number') {
         type = "number";
         value.push('' + params[key]);
       } else if (typeof params[key] == 'boolean') {
         type = "boolean";
         value.push(params[key] ? "true" : "false");
       } else if (params[key] instanceof Date) {
         type = "date";
         value.push(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(params[key]));
       } else if (typeof params[key] == 'string') {
         type = "string";
         value.push(params[key]);
       }
       if (type != null) {
         paramEntries.push({
           name: key,
           stringValue: value,
           type: type
         });
       }
     }
     return paramEntries;
   }-*/;
   
   public native void schedulerParamsLoadedCallback(String filePath) /*-{
     $doc.getElementById('schedulerParamsFrame').contentWindow.initSchedulingParams(filePath, $wnd.schedulerParamsCompleteCallback);
   }-*/;
   
   public void schedulerParamsCompleteCallback(boolean complete) {
     parametersComplete = complete;
     setCanContinue(complete);
     setCanFinish(complete);
   }
   
   private native void registerSchedulingCallbacks(ScheduleParamsWizardPanel thisInstance)/*-{
     $wnd.schedulerParamsLoadedCallback = function(filePath) {thisInstance.@org.pentaho.gwt.widgets.client.wizards.panels.ScheduleParamsWizardPanel::schedulerParamsLoadedCallback(Ljava/lang/String;)(filePath)};
     $wnd.schedulerParamsCompleteCallback = function(flag) {thisInstance.@org.pentaho.gwt.widgets.client.wizards.panels.ScheduleParamsWizardPanel::schedulerParamsCompleteCallback(Z)(flag)};
   }-*/;
   
   private native void addOnLoad(Element ele, String scheduledFilePath)
   /*-{
     ele.onload = function() {
       $wnd.schedulerParamsLoadedCallback(scheduledFilePath);
     };
   }-*/;  
   
   /**
    * 
    */
   private void layout() {
     this.addStyleName(PENTAHO_SCHEDULE);
     this.add(scheduleDescription, NORTH);
     this.add(parametersCaptionPanel, CENTER);
     this.setCellHeight(parametersCaptionPanel, "100%");
     parametersCaptionPanel.setHeight("100%");
   }
 
   /* (non-Javadoc)
    * @see org.pentaho.gwt.widgets.client.wizards.IWizardPanel#getName()
    */
   public String getName() {
     // TODO Auto-generated method stub
     return MSGS.scheduleEdit();
   }
   
   public void setParametersUrl(String url) {
     if (url == null) {
       if (parametersFrame != null) {
         parametersCaptionPanel.remove(parametersFrame);
         parametersFrame = null;
       }
     } else {
       if (parametersFrame == null) {
         parametersFrame = new Frame();
         parametersCaptionPanel.add(parametersFrame);
         parametersFrame.setHeight("94%"); //$NON-NLS-1$
         
         //DOM.setElementAttribute(parametersFrame.getElement(), "onload", "schedulerParamsLoadedCallback('" + scheduledFilePath + "')");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         addOnLoad(parametersFrame.getElement(), scheduledFilePath);
         DOM.setElementAttribute(parametersFrame.getElement(), "id", "schedulerParamsFrame"); //$NON-NLS-1$ //$NON-NLS-2$
         parametersFrame.setUrl(url);
       } else if (!url.equals(parametersFrame.getUrl())) {
         parametersFrame.setUrl(url);
       }
     }
   }
 }
