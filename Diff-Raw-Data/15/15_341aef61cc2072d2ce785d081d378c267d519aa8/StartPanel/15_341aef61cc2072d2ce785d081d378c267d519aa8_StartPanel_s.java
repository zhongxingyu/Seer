 /*
  * .NAME SOFTWARE, one line about what it does
  * 
  * Copyright (C) 2010 Regents of the University of Colorado.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  *
  * Please contact Sarah Kreidler (sarah.kreidler@ucdenver.edu) for more information
  * about this software.  Or visit the website at <>
  */
 
 package edu.cudenver.bios.powercalculator.client.panels;
 
 import java.util.ArrayList;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
 import com.google.gwt.xml.client.DOMException;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Node;
 import com.google.gwt.xml.client.XMLParser;
 
 import edu.cudenver.bios.powercalculator.client.PowerCalculatorConstants;
 import edu.cudenver.bios.powercalculator.client.PowerCalculatorGUI;
 import edu.cudenver.bios.powercalculator.client.listener.InputWizardStepListener;
 import edu.cudenver.bios.powercalculator.client.listener.ModelSelectListener;
 import edu.cudenver.bios.powercalculator.client.listener.StudyUploadListener;
 
 /**
  * Welcome / start panel in the input wizard for the power calculator 
  * site.  Allows the user to select the method for study design input
  * and the statistical model
  * 
  * @author Owner
  *
  */
 public class StartPanel extends Composite implements SubmitCompleteHandler
 {
     // uri of file upload service
     private static final String UPLOAD_URI = "/restcall/file/upload";
     // form tag for file
     private static final String FORM_TAG_FILE = "file";
     
     protected ListBox modelList = new ListBox();
     
     protected ArrayList<ModelSelectListener> modelSelectListeners = new ArrayList<ModelSelectListener>();
     protected ArrayList<StudyUploadListener> studyUploadListeners = new ArrayList<StudyUploadListener>();
     
     public StartPanel(InputWizardStepListener wizard, int stepIndex)
     {
         VerticalPanel panel = new VerticalPanel();
 
         // layout the widgets        
         // add introductory text
         HTML header = new HTML(PowerCalculatorGUI.constants.textStartPanelHeader());
         HTML description = new HTML(PowerCalculatorGUI.constants.textStartPanelDescription());
         panel.add(header);
         panel.add(description);
         
         // create user input container
         VerticalPanel inputContainer = new VerticalPanel();
 
         // build model selection list
         HorizontalPanel modelPanel = new HorizontalPanel();
         modelPanel.add(new HTML(PowerCalculatorGUI.constants.listBoxModel()));
         modelList.addItem(PowerCalculatorGUI.constants.labelOneSampleStudentsT(), 
                 PowerCalculatorGUI.constants.modelOneSampleStudentsT());
         modelList.addItem(PowerCalculatorGUI.constants.labelGLMM(), PowerCalculatorGUI.constants.modelGLMM());
         modelList.setItemSelected(1, true); // select glmm as default
         modelList.addChangeHandler(new ChangeHandler() {
         	public void onChange(ChangeEvent e)
         	{
                 String value = modelList.getValue(modelList.getSelectedIndex());
                 if (value != null && !value.isEmpty())
                 {
                     notifyOnModelSelect(value);
                 }
         	}
         });
         modelPanel.add(modelList);
         inputContainer.add(modelPanel);
         
         inputContainer.add(new HTML(PowerCalculatorGUI.constants.textStartPanelOr()));
         
         // build the upload form
         final FormPanel formPanel = new FormPanel();
         // for file upload, we need to use the POST method, and multipart MIME encoding.
         formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
         formPanel.setMethod(FormPanel.METHOD_POST);
         formPanel.setAction(UPLOAD_URI);
         // panel to contain the contents of the submission form
         HorizontalPanel formContents = new HorizontalPanel();
         // create an upload widget
         final FileUpload uploader = new FileUpload();
         uploader.addChangeHandler(new ChangeHandler() {
         	public void onChange(ChangeEvent e)
         	{
                 String filename = uploader.getFilename();
                 if (filename == null || filename.isEmpty())
                 {
                     Window.alert("No filename specified.  Please click the 'Browse' button and select a file for upload.");
                 }
                 else
                 {
                     formPanel.submit();
                 }
         	}
         });
         uploader.setName(FORM_TAG_FILE);
         formContents.add(new HTML(PowerCalculatorGUI.constants.fileUpload()));
         formContents.add(uploader);
        formContents.add(new HTML("Discard"));
         formPanel.add(formContents);
         inputContainer.add(formPanel);
         
         panel.add(inputContainer);
         
         // add style
         header.setStyleName(PowerCalculatorConstants.STYLE_WIZARD_STEP_HEADER);
         description.setStyleName(PowerCalculatorConstants.STYLE_WIZARD_STEP_DESCRIPTION);
         panel.setStyleName(PowerCalculatorConstants.STYLE_WIZARD_STEP_PANEL);
         inputContainer.setStyleName(PowerCalculatorConstants.STYLE_WIZARD_STEP_INPUT_CONTAINER);
 
         initWidget(panel);
     }
     
     /**
      * Add a listener for "start events" which indicate if the user
      * wants to create a new study, or upload an existing design
      * 
      * @param listener 
      */
     public void addModelSelectListener(ModelSelectListener listener)
     {
     	modelSelectListeners.add(listener);
     }
     
     public void addStudyUploadListener(StudyUploadListener listener)
     {
     	studyUploadListeners.add(listener);
     }
     
     /**
      * Notify listeners about new study creation request
      */
     private void notifyOnStudyUpload(Document doc, String modelName)
     {
         for(StudyUploadListener listener: studyUploadListeners) listener.onStudyUpload(doc, modelName);
     }
     
     /**
      * Notify listeners about existing study request
      */
     private void notifyOnModelSelect(String modelName)
     {
         for(ModelSelectListener listener: modelSelectListeners) listener.onModelSelect(modelName);
     }
     
     
     public void onSubmitComplete(SubmitCompleteEvent event) 
     {
         String results = event.getResults();
         if (results != null)
         {
         	try
         	{
         	    // make sure we at least have a study tag and model name specified
         		Document doc = XMLParser.parse(results);
             	Node studyNode = doc.getElementsByTagName("study").item(0);
             	if (studyNode == null) throw new DOMException(DOMException.SYNTAX_ERR, "no study tag specified");
             	Node modelName = studyNode.getAttributes().getNamedItem("modelname");
             	if (modelName == null)  throw new DOMException(DOMException.SYNTAX_ERR, "no model name specified");
         		// notify listeners of the file upload
             	notifyOnStudyUpload(doc, modelName.getNodeValue());
         	}
         	catch (DOMException e)
         	{
                 Window.alert("Uploaded file does not contain a valid study description [error: "+ e.getMessage() +"].  Please try another file.");
         	}
         }
         else
         {
         	Window.alert("Failed to upload file.  Please try again");
         }
 
     }
     
 }
