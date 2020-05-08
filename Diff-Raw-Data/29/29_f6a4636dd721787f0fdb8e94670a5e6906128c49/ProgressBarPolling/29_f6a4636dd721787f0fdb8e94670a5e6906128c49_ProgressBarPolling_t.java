  /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 
 package org.icefaces.samples.showcase.example.ace.progressbar; 
 
 import org.icefaces.samples.showcase.metadata.annotation.*;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 
 @ComponentExample(
         parent = ProgressBarBean.BEAN_NAME,
         title = "example.ace.progressBarPoling.title",
         description = "example.ace.progressBarPoling.description",
         example = "/resources/examples/ace/progressbar/progressBarPolling.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="progressBarPolling.xhtml",
                     resource = "/resources/examples/ace/progressbar/progressBarPolling.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="ProgressBarPolling.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase/example/ace/progressbar/ProgressBarPolling.java")
         }
 )
 @ManagedBean(name= ProgressBarPolling.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class ProgressBarPolling extends ComponentExampleImpl<ProgressBarPolling> implements Serializable {
 
     public static final String BEAN_NAME = "progressBarPolling";
     private ArrayList<UploadObject> pendingUploads;
     private ArrayList<UploadObject> uploads;
     private String selection;
     private boolean selectorIsDisabled;
    private UploadObject currentSelection; //pointer to the selected UploadObject
     
     public ProgressBarPolling() {
         super(ProgressBarPolling.class);
         initializeBean();
     }
     
     public void processSelection(ValueChangeEvent e) {
         String newValue = (String)e.getNewValue();
         //find selected item
         UploadObject selectedItem = findUploadObject(newValue);
         if(selectedItem != null) {
             //set selected item as active and disable upload selector
             selectedItem.setReady(true);
             selectorIsDisabled = true;
             //add to the upload manager list
             uploads.add(selectedItem);
            //save current selection
            currentSelection = selectedItem;
         }
     }
     
    public void startUpload()
    {
        currentSelection.setReady(false);
        currentSelection.setActive(true);
        currentSelection.setProgressValue(0);
    }
    
     public void processFormReset(ActionEvent event)
     {
         initializeBean();
     }
     
     public void completeListener() {
         //find completed object
         UploadObject completedUpload = findUploadObject(selection);
         if(completedUpload != null) {
             //mark as inactive and completed
             completedUpload.setActive(false);
             completedUpload.setCompleted(true);
             completedUpload.setReady(false);
             //set description
             String completedUploadDescription = completedUpload.getImageDescription() + " (completed)";
             completedUpload.setImageDescription(completedUploadDescription);
             //activate upload selector
             if(!pendingUploads.isEmpty()) {
                 pendingUploads.remove(completedUpload);
                 selectorIsDisabled = false;
                 selection = "none";
             }
         }
     }
     ////////////////////////////////////////////BEAN PRIVATE METHODS////////////////////////////////////////////////////////
     private void initializeBean() {
         uploads = new ArrayList<UploadObject>();
         pendingUploads = populatePendingUploads();
         selectorIsDisabled = false;
         selection = "none";
        currentSelection = null;
     }
     private ArrayList<UploadObject> populatePendingUploads() {
         //note: UploadObject.widgetVarName MUST BE UNIQUE
         ArrayList<UploadObject> uploadList = new ArrayList<UploadObject>();
         uploadList.add( new UploadObject(0, "Laptop", "/resources/css/images/dragdrop/laptop.png", "Laptop",false,false,false) );
         uploadList.add( new UploadObject(0, "PDA", "/resources/css/images/dragdrop/pda.png", "PDA",false,false,false));
         uploadList.add( new UploadObject(0, "Monitor", "/resources/css/images/dragdrop/monitor.png", "Monitor",false,false,false) );
         uploadList.add( new UploadObject(0, "Desktop", "/resources/css/images/dragdrop/desktop.png", "Desktop",false,false,false) );
         Collections.shuffle(uploadList);
         return uploadList;
     }
     private UploadObject findUploadObject(String matchingCriteria) {
         UploadObject matchingUpload = null;
         boolean matchFound = false;
         //search in uploads list
         if(! uploads.isEmpty()) {
             for (UploadObject object : uploads) {
                 if(object.getWidgetVarName().equals(matchingCriteria)) {
                       matchingUpload = object;
                       matchFound = true;
                       break;
                 }
             }
         }
         //search in pending uploads list if nothing found at this point and this list is not empty
         if(!matchFound) {
             if(! pendingUploads.isEmpty()) {
                 for (UploadObject object : pendingUploads)  {
                     if(object.getWidgetVarName().equals(matchingCriteria)) {
                         matchingUpload = object;
                         break;
                     }
                 }
             }
         }
         return matchingUpload;
     }
     ///////////////////////////////////////////INNER CLASS BEGIN///////////////////////////////////////////////////////////////
     public class UploadObject implements Serializable {
         private int progressValue;
         private String imageDescription;
         private String imagePath;
         private String widgetVarName;
        private boolean ready; // is true when upload has been selected, but not started
         private boolean active; // is true when upload is in progress
         private boolean completed; // is true when upload has been completed
 
         public UploadObject(int progressValue, String imageDescription, String imagePath, String widgetVarName,boolean ready, boolean active, boolean completed) {
             this.progressValue = progressValue;
             this.imageDescription = imageDescription;
             this.imagePath = imagePath;
             this.widgetVarName = widgetVarName;
             this.active = active;
             this.completed = completed;
             this.ready = ready;
         }
         //////////////////////////////MODIFIED GETTER OF THE INNER CLASS BEGIN////////////////////////////////////////
         public int getProgressValue() {
              if(active && progressValue >=0 && progressValue <100) {
                 progressValue += 20;
              }
              else if(active && progressValue >= 100) {
                 progressValue = 100;
             }
             return progressValue;
         }
         ///////////////REGULAR GETTERS AND SETTERS OF THE INNER CLASS BEGIN///////////////////////////////////
         public void setProgressValue(int progressValue) { this.progressValue = progressValue;}
         public boolean isActive()  { return active;}
         public void setActive(boolean active) { this.active = active;}
         public boolean isCompleted() { return completed;}
         public void setCompleted(boolean completed) { this.completed = completed;}
         public String getImageDescription() { return imageDescription;}
         public void setImageDescription(String imageDescription) { this.imageDescription = imageDescription;}
         public String getImagePath() { return imagePath;}
         public void setImagePath(String imagePath) { this.imagePath = imagePath;}
         public boolean isReady() { return ready;}
         public void setReady(boolean ready) { this.ready = ready;}
         public String getWidgetVarName() { return widgetVarName;}
         public void setWidgetVarName(String widgetVarName) { this.widgetVarName = widgetVarName;}
     }
     ///////////////////////////////////////////INNER CLASS END///////////////////////////////////////////////////////////////
     
     ///////////////////////////////////////////BEAN GETTERS&SETTERS BEGIN////////////////////////////////////////////////
     public String getSelection() {
         return selection;
     }
     public void setSelection(String selection) {
         this.selection = selection;
     }
     public ArrayList<UploadObject> getUploads() {
         return uploads;
     }
     public void setUploads(ArrayList<UploadObject> uploads) {
         this.uploads = uploads;
     }
     public ArrayList<UploadObject> getPendingUploads() {
         return pendingUploads;
     }
     public void setPendingUploads(ArrayList<UploadObject> pendingUploads) {
         this.pendingUploads = pendingUploads;
     }
 
     public boolean isSelectorIsDisabled() {
         return selectorIsDisabled;
     }
     public void setSelectorIsDisabled(boolean selectorIsDisabled) {
         this.selectorIsDisabled = selectorIsDisabled;
     }
     ////////////////////////////////////////////BEAN GETTERS&SETTERS END/////////////////////////////////////////////////
 }//end of managed bean
