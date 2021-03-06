 /*******************************************************************************
  * Copyright 2006 - 2012 Vienna University of Technology,
  * Department of Software Technology and Interactive Systems, IFS
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.scape_project.planning.plato.wfview.full;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import javax.enterprise.context.ConversationScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import eu.scape_project.planning.exception.PlanningException;
 import eu.scape_project.planning.manager.ByteStreamManager;
 import eu.scape_project.planning.manager.DigitalObjectManager;
 import eu.scape_project.planning.manager.StorageException;
 import eu.scape_project.planning.model.DigitalObject;
 import eu.scape_project.planning.model.PlanState;
 import eu.scape_project.planning.plato.wf.AbstractWorkflowStep;
 import eu.scape_project.planning.plato.wf.CreateExecutablePlan;
 import eu.scape_project.planning.plato.wfview.AbstractView;
 import eu.scape_project.planning.taverna.parser.TavernaParserException;
 import eu.scape_project.planning.utils.Downloader;
 import eu.scape_project.planning.utils.ParserException;
 import eu.scape_project.planning.xml.C3POProfileParser;
 
 import org.richfaces.event.FileUploadEvent;
 import org.richfaces.model.UploadedFile;
 import org.slf4j.Logger;
 
 /**
  * View bean for step Create Executable Plan.
  */
 @Named("createExecutablePlan")
 @ConversationScoped
 public class CreateExecutablePlanView extends AbstractView {
     private static final long serialVersionUID = 1L;
 
     @Inject
     private Logger log;
 
     @Inject
     private CreateExecutablePlan createExecutablePlan;
 
     @Inject
     private DigitalObjectManager digitalObjectManager;
 
     @Inject
     private Downloader downloader;
 
     @Inject
     private ByteStreamManager bytestreamManager;
 
    private List<String> collectionProfileElements;

     /**
      * Default constructor.
      */
     public CreateExecutablePlanView() {
         currentPlanState = PlanState.ANALYSED;
         name = "Create Executable Plan";
         viewUrl = "/plan/createexecutableplan.jsf";
         group = "menu.buildPreservationPlan";
     }
 
     @Override
     protected AbstractWorkflowStep getWfStep() {
         return createExecutablePlan;
     }
 
     public boolean isCollectionProfileDefined() {
         return plan.getSampleRecordsDefinition().getCollectionProfile() != null;
     }
 
     /**
      * Returns the list of objects specified in the collection profile.
      */
    public void loadCollectionProfileElements() {
 
         DigitalObject profile = plan.getSampleRecordsDefinition().getCollectionProfile().getProfile();
         if (profile != null && profile.isDataExistent()) {
             try {
                 DigitalObject datafilledProfile = digitalObjectManager.getCopyOfDataFilledDigitalObject(profile);
 
                 C3POProfileParser parser = new C3POProfileParser();
                 parser.read(new ByteArrayInputStream(datafilledProfile.getData().getRealByteStream().getData()), false);
 
                collectionProfileElements = parser.getObjectIdentifiers();
                
                parser = null;
                datafilledProfile = null;
             } catch (StorageException e) {
                 facesMessages.addError("Could not load collection profile.");
             } catch (ParserException e) {
                 facesMessages.addError("Could not parse collection profile.");
             }
         } else {
             log.debug("No profile defined so far.");
         }
     }
 
     /**
      * Uploads an executable plan and stores it.
      * 
      * @param event
      *            the event containing the upload data
      */
     public void uploadT2flowExecutablePlan(final FileUploadEvent event) {
         UploadedFile item = event.getUploadedFile();
         String fileName = item.getName();
         log.debug("Executable plan file [{}] uploaded", fileName);
 
         if (!fileName.endsWith(".t2flow")) {
             log.warn("The uploaded file [{}] is not an t2flow file", fileName);
             facesMessages.addError("The uploaded file is not an t2flow");
             return;
         }
 
         try {
             createExecutablePlan.readT2flowExecutablePlan(item.getInputStream());
         } catch (TavernaParserException e) {
             log.warn("An error occurred during parsing: {}", e.getMessage());
             facesMessages.addError("An error occurred, while reading in the uploaded executable plan: "
                 + e.getMessage());
         } catch (PlanningException e) {
             log.warn("An error occurred furing parsing: {}", e.getMessage());
             facesMessages.addError("An error occurred, while reading in the uploaded executable plan: "
                 + e.getMessage());
         } catch (IOException e) {
             log.warn("An error occurred while opening the input stream: {}", e.getMessage());
             facesMessages.addError("An error occurred, while reading the file. Please try again");
         }
     }
 
     /**
      * Starts a download for the given digital object. Uses
      * {@link eu.scape_project.planning.util.Downloader} to perform the
      * download.
      * 
      * @param object
      *            the object to download
      */
     public void download(final DigitalObject object) {
         File file = bytestreamManager.getTempFile(object.getPid());
         downloader.download(object, file);
     }
 
     /**
      * Initiates the generation of a preservation action plan.
      */
     public void generatePreservationActionPlan() {
 
         if (!isPapGenerationPossible()) {
             facesMessages.addError("An error occured while generating the preservation ation plan.");
             return;
         }
 
         try {
             createExecutablePlan.generatePreservationActionPlan();
         } catch (PlanningException e) {
             log.warn("An error occured while generating the preservation action plan: {}", e.getMessage());
             facesMessages.addError("An error occured while generating the preservation ation plan: " + e.getMessage());
         }
 
         facesMessages.addInfo("Preservation actino plan generated.");
     }
 
     /**
      * Checks if it is possible to generate a preservation action plan.
      * 
      * @return true if possible, false otherwise
      */
     public boolean isPapGenerationPossible() {
         return plan.getExecutablePlanDefinition() != null
             && plan.getExecutablePlanDefinition().getT2flowExecutablePlan() != null
             && plan.getExecutablePlanDefinition().getT2flowExecutablePlan().isDataExistent()
             && plan.getSampleRecordsDefinition() != null
             && plan.getSampleRecordsDefinition().getCollectionProfile() != null
             && plan.getSampleRecordsDefinition().getCollectionProfile().getProfile() != null
             && plan.getSampleRecordsDefinition().getCollectionProfile().getProfile().isDataExistent();
     }

    // ---------------------- getters/setters --------------------
    public List<String> getCollectionProfileElements() {
        return collectionProfileElements;
    }

 }
