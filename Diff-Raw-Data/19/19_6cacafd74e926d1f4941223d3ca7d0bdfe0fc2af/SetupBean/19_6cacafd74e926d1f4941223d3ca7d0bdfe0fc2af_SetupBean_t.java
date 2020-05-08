 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uq.ilabs.labclient;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.logging.Level;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import uq.ilabs.library.lab.types.ClientSubmissionReport;
 import uq.ilabs.library.lab.types.ValidationReport;
 import uq.ilabs.library.lab.utilities.Logfile;
 import uq.ilabs.library.lab.utilities.XmlUtilities;
 import uq.ilabs.library.labclient.Consts;
 import uq.ilabs.library.labclient.ExperimentSpecification;
 import uq.ilabs.library.labclient.engine.LabClientSession;
 import uq.ilabs.library.labclient.engine.types.SetupInfo;
 
 /**
  *
  * @author uqlpayne
  */
 @ManagedBean
 @SessionScoped
 public class SetupBean implements Serializable {
 
     //<editor-fold defaultstate="collapsed" desc="Constants">
     private static final String STR_ClassName = SetupBean.class.getName();
     private static final Level logLevel = Level.FINE;
     /*
      * String constants
      */
     private static final String STR_SpecificationValid_arg = "Specification is valid. Execution time will be %s.";
     private static final String STR_SubmissionSuccessful_arg2 = "Submission was successful. Experiment # is %d and execution time is %s.";
     private static final String STR_MinutesAnd_arg2 = "%d minute%s and ";
     private static final String STR_Seconds_arg2 = "%d second%s";
     private static final String STR_ExperimentNumberHasBeenSubmitted_arg = "Experiment %d has been submitted.";
     private static final String STR_ExperimentNumbersHaveBeenSubmitted_arg = "Experiments %s have been submitted.";
     /*
      * String constants for XML elements
      */
     public static final String STRXML_SomeParameter = "someParameter";
     /*
      * String constants for exception messages
      */
     private static final String STRERR_ValidationFailed = "Validation Failed!";
     private static final String STRERR_SubmissionFailed = "Submission Failed!";
     //</editor-fold>
     //<editor-fold defaultstate="collapsed" desc="Variables">
     private LabClientSession labClientSession;
     private String setupId;
     private String oldSetupName;
     private String[] allSources;
     private String[] allAbsorbers;
     private String[] allDistances;
     private String defaultSource;
     private String defaultAbsorber;
     private ArrayList<String> availableAbsorberList;
     private ArrayList<String> selectedAbsorberList;
     private ArrayList<String> availableDistanceList;
     private ArrayList<String> selectedDistanceList;
     //</editor-fold>
     //<editor-fold defaultstate="collapsed" desc="Properties">
     private String[] setupNames;
     private String hsomSetupName;
     private String hotSetupDescription;
     private boolean hcbSubmitDisabled;
     private String holMessage;
     private String holMessageClass;
 
     public String[] getSetupNames() {
         return setupNames;
     }
 
     public String getHsomSetupName() {
         return hsomSetupName;
     }
 
     public void setHsomSetupName(String hsomSetupName) {
         this.hsomSetupName = hsomSetupName;
     }
 
     public String getHotSetupDescription() {
         return hotSetupDescription;
     }
 
     public boolean isHcbSubmitDisabled() {
         return hcbSubmitDisabled;
     }
 
     public String getHolMessage() {
         return holMessage;
     }
 
     public String getHolMessageClass() {
         return holMessageClass;
     }
 
     /*
      * Getter and Setter
      */
     private String hsomSource;
     private String hsomAbsorber;
     private String hsomDistance;
     private String hitDuration;
     private String hitTrials;
     /*
      * Getter only
      */
     private String selectedAbsorbersRendered;
     private boolean hcbAddAbsorberDisabled;
     private String selectedDistancesRendered;
     private boolean hcbAddDistanceDisabled;
 
     public String getHsomSource() {
         return hsomSource;
     }
 
     public void setHsomSource(String hsomSource) {
         this.hsomSource = hsomSource;
     }
 
     public String getHsomAbsorber() {
         return hsomAbsorber;
     }
 
     public void setHsomAbsorber(String hsomAbsorber) {
         this.hsomAbsorber = hsomAbsorber;
     }
 
     public String getHsomDistance() {
         return hsomDistance;
     }
 
     public void setHsomDistance(String hsomDistance) {
         this.hsomDistance = hsomDistance;
     }
 
     public String getHitDuration() {
         return hitDuration;
     }
 
     public void setHitDuration(String hitDuration) {
         this.hitDuration = hitDuration;
     }
 
     public String getHitTrials() {
         return hitTrials;
     }
 
     public void setHitTrials(String hitTrials) {
         this.hitTrials = hitTrials;
     }
 
     public String[] getAvailableSources() {
         return this.allSources;
     }
 
     public String[] getAvailableAbsorbers() {
         return (String[]) this.availableAbsorberList.toArray(new String[this.availableAbsorberList.size()]);
     }
 
     public String[] getSelectedAbsorbers() {
         return (String[]) this.selectedAbsorberList.toArray(new String[this.selectedAbsorberList.size()]);
     }
 
     public String[] getAvailableDistances() {
         return (String[]) this.availableDistanceList.toArray(new String[this.availableDistanceList.size()]);
     }
 
     public String[] getSelectedDistances() {
         return (String[]) this.selectedDistanceList.toArray(new String[this.selectedDistanceList.size()]);
     }
 
     public String getSelectedAbsorbersRendered() {
         return selectedAbsorbersRendered;
     }
 
     public boolean isHcbAddAbsorberDisabled() {
         return hcbAddAbsorberDisabled;
     }
 
     public String getSelectedDistancesRendered() {
         return selectedDistancesRendered;
     }
 
     public boolean isHcbAddDistanceDisabled() {
         return hcbAddDistanceDisabled;
     }
 
     //</editor-fold>
     /**
      * Creates a new instance of SetupBean
      */
     public SetupBean() {
         final String methodName = "SetupBean";
         Logfile.WriteCalled(logLevel, STR_ClassName, methodName);
 
         this.labClientSession = (LabClientSession) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(Consts.STRSSN_LabClient);
         this.setupNames = this.labClientSession.getSetupNames();
 
         try {
             /*
              * Get all sources
              */
             Document document = XmlUtilities.GetDocumentFromString(this.labClientSession.getXmlConfiguration());
             Node nodeConfiguration = XmlUtilities.GetRootNode(document, Consts.STRXML_Configuration);
             Node node = XmlUtilities.GetChildNode(nodeConfiguration, Consts.STRXML_Sources);
             ArrayList nodeList = XmlUtilities.GetChildNodeList(node, Consts.STRXML_Source);
             this.allSources = new String[nodeList.size()];
             for (int i = 0; i < nodeList.size(); i++) {
                 /*
                  * Get the source name
                  */
                 Node nodeSource = (Node) nodeList.get(i);
                 this.allSources[i] = XmlUtilities.GetChildValue(nodeSource, Consts.STRXML_Name);
             }
             this.defaultSource = XmlUtilities.GetAttributeValue(node, Consts.STRXML_ATTR_Default, false);
 
             /*
              * Get all absorbers
              */
             node = XmlUtilities.GetChildNode(nodeConfiguration, Consts.STRXML_Absorbers);
             nodeList = XmlUtilities.GetChildNodeList(node, Consts.STRXML_Absorber);
             this.allAbsorbers = new String[nodeList.size()];
             for (int i = 0; i < nodeList.size(); i++) {
                 /*
                  * Get the absorber name
                  */
                 Node nodeAbsorber = (Node) nodeList.get(i);
                 this.allAbsorbers[i] = XmlUtilities.GetChildValue(nodeAbsorber, Consts.STRXML_Name);
             }
             this.defaultAbsorber = XmlUtilities.GetAttributeValue(node, Consts.STRXML_ATTR_Default, false);
 
             /*
              * Get all distances
              */
             node = XmlUtilities.GetChildNode(nodeConfiguration, Consts.STRXML_Distances);
             int minimum = XmlUtilities.GetChildValueAsInt(node, Consts.STRXML_Minimum);
             int maximum = XmlUtilities.GetChildValueAsInt(node, Consts.STRXML_Maximum);
             int stepsize = XmlUtilities.GetChildValueAsInt(node, Consts.STRXML_Stepsize);
             ArrayList<String> arrayList = new ArrayList<>();
             for (int distance = minimum; distance <= maximum; distance += stepsize) {
                 arrayList.add(Integer.toString(distance));
             }
             this.allDistances = (String[]) arrayList.toArray(new String[arrayList.size()]);
 
             /*
              * Initialise local variables
              */
             this.availableAbsorberList = new ArrayList<>();
             this.selectedAbsorberList = new ArrayList<>();
             this.availableDistanceList = new ArrayList<>();
             this.selectedDistanceList = new ArrayList<>();
 
         } catch (Exception ex) {
         }
 
         Logfile.WriteCompleted(logLevel, STR_ClassName, methodName);
     }
 
     /**
      *
      */
     public void pageLoad() {
         final String methodName = "pageLoad";
         Logfile.WriteCalled(logLevel, STR_ClassName, methodName);
 
         if (FacesContext.getCurrentInstance().isPostback() == false) {
             /*
              * Not a postback, initialise page controls
              */
             this.ShowMessageInfo(null);
             this.hsomSetupName = this.setupNames[0];
 
             /*
              * Check if an experiment has been submitted
              */
             int[] submittedIds = this.labClientSession.getSubmittedIds();
             if (submittedIds != null) {
                 if (submittedIds.length == 1) {
                     /*
                      * Show the one that has been submitted
                      */
                     this.ShowMessageInfo(String.format(STR_ExperimentNumberHasBeenSubmitted_arg, submittedIds[0]));
                 } else if (submittedIds.length > 1) {
                     /*
                      * More than one has been submitted, show them all
                      */
                     String arg = "";
                     for (int i = 0; i < submittedIds.length; i++) {
                         if (i > 0) {
                             arg += ", ";
                         }
                         arg += Integer.toString(submittedIds[i]);
                     }
                     this.ShowMessageInfo(String.format(STR_ExperimentNumbersHaveBeenSubmitted_arg, arg));
                 }
             } else {
                 this.hcbSubmitDisabled = false;
             }
         }
 
         /*
          * Check if the setup has changed
          */
         if (this.hsomSetupName.equals(this.oldSetupName) == false) {
             this.PopulatePage();
             this.oldSetupName = this.hsomSetupName;
         }
 
         Logfile.WriteCompleted(logLevel, STR_ClassName, methodName);
     }
 
     public String actionAddAbsorber() {
         /*
          * Add absorber to the selected list and remove from available list
          */
         this.selectedAbsorberList.add(this.hsomAbsorber);
         this.availableAbsorberList.remove(this.hsomAbsorber);
 
         /*
          * Disable Add button if no more absorbers to select
          */
         this.hcbAddAbsorberDisabled = this.availableAbsorberList.isEmpty();
 
         /* Navigate to the current page */
         return null;
     }
 
     public String actionClearAbsorberList() {
         /*
          * Clear the list of selected absorbers and enable the Add button
          */
         this.selectedAbsorberList.clear();
         this.availableAbsorberList.clear();
         this.availableAbsorberList.addAll(Arrays.asList(this.allAbsorbers));
         this.hcbAddAbsorberDisabled = false;
 
         /* Navigate to the current page */
         return null;
     }
 
     public String actionAddDistance() {
         /*
          * Add distance to the selected list and remove from available list
          */
         this.selectedDistanceList.add(this.hsomDistance);
         this.availableDistanceList.remove(this.hsomDistance);
 
         /*
          * Disable Add button if no more distances to select
          */
         this.hcbAddDistanceDisabled = this.availableDistanceList.isEmpty();
 
         /* Navigate to the current page */
         return null;
     }
 
     public String actionClearDistanceList() {
         /*
          * Clear the list of selected distances, enable the Add button and repopulate the list of available distances
          */
         this.selectedDistanceList.clear();
         this.availableDistanceList.clear();
         this.availableDistanceList.addAll(Arrays.asList(this.allDistances));
         this.hcbAddDistanceDisabled = false;
 
         /* Navigate to the current page */
         return null;
     }
 
     /**
      *
      * @return
      */
     public String actionValidate() {
         final String methodName = "actionValidate";
         Logfile.WriteCalled(logLevel, STR_ClassName, methodName);
 
         try {
             /*
              * Create the XML experiment specification string
              */
             String xmlSpecification = this.CreateXmlSpecification();
 
             /*
              * Validate the experiment specification
              */
             ValidationReport validationReport = this.labClientSession.getServiceBrokerAPI().Validate(xmlSpecification);
             if (validationReport == null) {
                 throw new RuntimeException(STRERR_ValidationFailed);
             }
 
             if (validationReport.isAccepted() == false) {
                 throw new RuntimeException(validationReport.getErrorMessage());
             }
 
             /*
              * Validation was accepted
              */
             String message = String.format(STR_SpecificationValid_arg, FormatTimeMessage((int) validationReport.getEstRuntime()));
             ShowMessageInfo(message);
 
         } catch (Exception ex) {
             Logfile.WriteError(ex.toString());
             ShowMessageError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(logLevel, STR_ClassName, methodName);
 
         /* Navigate to the current page */
         return null;
     }
 
     /**
      *
      * @return
      */
     public String actionSubmit() {
         final String methodName = "actionSubmit";
         Logfile.WriteCalled(logLevel, STR_ClassName, methodName);
 
         try {
             /*
              * Create the XML experiment specification string
              */
             String xmlSpecification = this.CreateXmlSpecification();
 
             /*
              * Submit the experiment specification
              */
             ClientSubmissionReport clientSubmissionReport = this.labClientSession.getServiceBrokerAPI().Submit(xmlSpecification);
             if (clientSubmissionReport == null) {
                 throw new RuntimeException(STRERR_SubmissionFailed);
             }
 
             /*
              * Check if submission was successful
              */
             ValidationReport validationReport = clientSubmissionReport.getValidationReport();
             if (validationReport.isAccepted() == false) {
                 throw new RuntimeException(validationReport.getErrorMessage());
             }
 
             /*
              * Submission was accepted
              */
             String message = String.format(STR_SubmissionSuccessful_arg2,
                     clientSubmissionReport.getExperimentId(), FormatTimeMessage((int) validationReport.getEstRuntime()));
             ShowMessageInfo(message);
 
             /*
              * Update session with submitted id
              */
             this.labClientSession.AddSubmittedId(clientSubmissionReport.getExperimentId());
             if (this.labClientSession.isMultiSubmit() == false) {
                 /*
                  * Disable futher submission
                  */
                 this.hcbSubmitDisabled = true;
             }
         } catch (Exception ex) {
             Logfile.WriteError(ex.toString());
             ShowMessageError(ex.getMessage());
         }
 
         Logfile.WriteCompleted(logLevel, STR_ClassName, methodName);
 
         /* Navigate to the current page */
         return null;
     }
 
     /**
      *
      */
     private void PopulatePage() {
         /*
          * Get the setup Id and update the description for the selected setup
          */
         SetupInfo setupInfo = this.labClientSession.getSetupInfoMap().get(this.hsomSetupName);
         this.setupId = setupInfo.getId();
         this.hotSetupDescription = setupInfo.getDescription();
 
         /*
          * Get the selecected and default values for the specified setup
          */
         String[] selectedAbsorbers = null;
         String[] selectedDistances = null;
         try {
             Document document = XmlUtilities.GetDocumentFromString(setupInfo.getXmlSetup());
             Node nodeRoot = XmlUtilities.GetRootNode(document, Consts.STRXML_Setup);
 
             this.hitDuration = XmlUtilities.GetChildValue(nodeRoot, Consts.STRXML_Duration);
             this.hitTrials = XmlUtilities.GetChildValue(nodeRoot, Consts.STRXML_Repeat);
 
             String csvDistances = XmlUtilities.GetChildValue(nodeRoot, Consts.STRXML_Distance);
             selectedDistances = csvDistances.split(Consts.STR_CsvSplitter);
 
             String csvAbsorbers = XmlUtilities.GetChildValue(nodeRoot, Consts.STRXML_AbsorberName);
             selectedAbsorbers = csvAbsorbers.split(Consts.STR_CsvSplitter);
         } catch (Exception ex) {
         }
 
         /*
          * Clear selected lists and populate available lists
          */
         this.selectedAbsorberList.clear();
         this.selectedDistanceList.clear();
         this.availableAbsorberList.clear();
         this.availableDistanceList.clear();
         this.availableAbsorberList.addAll(Arrays.asList(this.allAbsorbers));
         this.availableDistanceList.addAll(Arrays.asList(this.allDistances));
 
         /*
          * Show/hide the page controls for the specified setup
          */
         switch (this.setupId) {
             case Consts.STRXML_SetupId_RadioactivityVsTime:
             case Consts.STRXML_SetupId_SimActivityVsTime:
             case Consts.STRXML_SetupId_SimActivityVsTimeNoDelay:
                 /*
                  * Hide selected absorber list and hide selected distance list
                  */
                 this.selectedAbsorbersRendered = Boolean.toString(false);
                 this.selectedDistancesRendered = Boolean.toString(false);

                /*
                 * Select distance
                 */
                this.hsomDistance = selectedDistances[0];
                 break;
 
             case Consts.STRXML_SetupId_RadioactivityVsDistance:
             case Consts.STRXML_SetupId_SimActivityVsDistance:
             case Consts.STRXML_SetupId_SimActivityVsDistanceNoDelay:
                 /*
                  * Hide selected absorber list and show selected distance list
                  */
                 this.selectedAbsorbersRendered = Boolean.toString(false);
                 this.selectedDistancesRendered = Boolean.toString(true);
 
                 /*
                  * Populate selected distances
                  */
                 this.availableDistanceList.removeAll(Arrays.asList(selectedDistances));
                 this.selectedDistanceList.addAll(Arrays.asList(selectedDistances));

                /*
                 * Select distance
                 */
                this.hsomDistance = this.availableDistanceList.get(0);
                 break;
 
             case Consts.STRXML_SetupId_RadioactivityVsAbsorber:
             case Consts.STRXML_SetupId_SimActivityVsAbsorber:
             case Consts.STRXML_SetupId_SimActivityVsAbsorberNoDelay:
                 /*
                  * Show selected absorber list and hide selected distance list
                  */
                 this.selectedAbsorbersRendered = Boolean.toString(true);
                 this.selectedDistancesRendered = Boolean.toString(false);
 
                 /*
                  * Populate selected absorbers
                  */
                 this.availableAbsorberList.removeAll(Arrays.asList(selectedAbsorbers));
                 this.selectedAbsorberList.addAll(Arrays.asList(selectedAbsorbers));

                /*
                 * Select distance
                 */
                this.hsomDistance = selectedDistances[0];
                 break;
         }
 
         /*
          * Update defaults
          */
         this.hsomSource = this.defaultSource;
         this.hsomAbsorber = this.defaultAbsorber;
 
         /*
          * Hide the information message
          */
         this.ShowMessageInfo(null);
     }
 
     /**
      *
      * @return String
      * @throws Exception
      */
     private String CreateXmlSpecification() throws Exception {
         /*
          * Create an instance of the experiment specification
          */
         ExperimentSpecification experimentSpecification = new ExperimentSpecification(this.labClientSession.getXmlSpecification());
 
         /*
          * Add specification information
          */
         experimentSpecification.setSetupName(this.hsomSetupName);
         experimentSpecification.setSetupId(this.setupId);
         experimentSpecification.setSource(this.hsomSource);
         experimentSpecification.setDuration(this.hitDuration);
         experimentSpecification.setTrials(this.hitTrials);
 
         /*
          * Create a CSV string of absorbers
          */
         String csvAbsorbers = "";
         for (String absorber : this.selectedAbsorberList) {
             csvAbsorbers += String.format("%s%s", (!csvAbsorbers.isEmpty()) ? Consts.STR_CsvSplitter : "", absorber);
         }
 
         /*
          * Create a CSV string of distances
          */
         String csvDistances = "";
         for (String distance : this.selectedDistanceList) {
             csvDistances += String.format("%s%s", (!csvDistances.isEmpty()) ? Consts.STR_CsvSplitter : "", distance);
         }
 
         switch (this.setupId) {
             case Consts.STRXML_SetupId_RadioactivityVsTime:
             case Consts.STRXML_SetupId_SimActivityVsTime:
             case Consts.STRXML_SetupId_SimActivityVsTimeNoDelay:
                 experimentSpecification.setAbsorbers(this.hsomAbsorber);
                 experimentSpecification.setDistances(this.hsomDistance);
                 break;
 
             case Consts.STRXML_SetupId_RadioactivityVsDistance:
             case Consts.STRXML_SetupId_SimActivityVsDistance:
             case Consts.STRXML_SetupId_SimActivityVsDistanceNoDelay:
                 experimentSpecification.setAbsorbers(this.hsomAbsorber);
                 experimentSpecification.setDistances(csvDistances);
                 break;
 
             case Consts.STRXML_SetupId_RadioactivityVsAbsorber:
             case Consts.STRXML_SetupId_SimActivityVsAbsorber:
             case Consts.STRXML_SetupId_SimActivityVsAbsorberNoDelay:
                 experimentSpecification.setAbsorbers(csvAbsorbers);
                 experimentSpecification.setDistances(this.hsomDistance);
                 break;
         }
 
         /*
          * Convert specification information to an XML string
          */
         String xmlSpecification = experimentSpecification.ToXmlString();
 
         return xmlSpecification;
     }
 
     /**
      *
      * @param seconds
      * @return
      */
     private String FormatTimeMessage(int seconds) {
         /*
          * Convert to minutes and seconds
          */
         int minutes = seconds / 60;
         seconds -= minutes * 60;
 
         String message = "";
         try {
             if (minutes > 0) {
                 /* Display minutes */
                 message += String.format(STR_MinutesAnd_arg2, minutes, FormatPlural(minutes));
             }
             /* Display seconds */
             message += String.format(STR_Seconds_arg2, seconds, FormatPlural(seconds));
         } catch (Exception ex) {
             Logfile.WriteError(ex.toString());
             message = ex.getMessage();
         }
 
         return message;
     }
 
     /**
      *
      * @param value
      * @return
      */
     private String FormatPlural(int value) {
         return (value == 1) ? "" : "s";
     }
 
     /**
      *
      * @param message
      */
     private void ShowMessageInfo(String message) {
         this.holMessage = message;
         this.holMessageClass = Consts.STRSTL_InfoMessage;
     }
 
     /**
      *
      * @param message
      */
     private void ShowMessageError(String message) {
         this.holMessage = message;
         this.holMessageClass = Consts.STRSTL_ErrorMessage;
     }
 }
