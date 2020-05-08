 /*
  * Helper for save nursery
  * 
  */
 package ibfb.nursery.utils;
 
 import ibfb.domain.core.Condition;
 import ibfb.domain.core.Constant;
 import ibfb.domain.core.Measurement;
 import ibfb.domain.core.MeasurementData;
 import ibfb.domain.core.Study;
 import ibfb.domain.core.Variate;
 import ibfb.domain.core.Workbook;
 import ibfb.nursery.core.NurseryEditorTopComponent;
 import ibfb.nursery.models.ExperimentalConditionsTableModel;
 import ibfb.nursery.models.GermplasmEntriesTableModel;
 import ibfb.nursery.models.NurseryConditionsTableModel;
 import ibfb.nursery.models.ObservationsTableModel;
 import ibfb.nursery.models.StudyConditionsTableModel;
 import java.util.ArrayList;
 import java.util.List;
 import org.cimmyt.cril.ibwb.api.AppServicesProxy;
 import org.apache.commons.beanutils.BeanUtils;
 
 /**
  * Helper Class to save Nursery
  *
  * @author gamaliel
  */
 public class WorkbookSavingHelper {
 
     /*
      *
      *
      */
     public static void saveFieldbook(NurseryEditorTopComponent nurseryEditor) {
         Workbook savingWorkbook = new Workbook();
         
         savingWorkbook.setStudy(getStudy(nurseryEditor));
 //        savingWorkbook.getStudy().setShierarchy(getStudy(nurseryEditor).getStudyid());
 
         // assign number of instances //TODO : recuperar el numero de instancia del nursery 
         Integer instanceNumber = 1;//Integer instanceNumber = Integer.parseInt(studyEditor.jLabelInstances.getText());
         savingWorkbook.setInstanceNumber(instanceNumber);
 
         // study conditions
         savingWorkbook.setStudyConditions(getStudyConditions(nurseryEditor));
         
         savingWorkbook.setConditions(getConditionTrialList());
 
         // assign studyConditions data
         savingWorkbook.setConditionsData(getConditionTrialList());
 
         // assign factors
         savingWorkbook.setFactors(nurseryEditor.getMyWorkbook().getFactors());
 
 
 
         //assign selected constants
         savingWorkbook.setConstants(getSelectedConstants(nurseryEditor));
 
         // assign constants data
         savingWorkbook.setConstantsData(getConstantData(nurseryEditor));
 
         // assign selected variates
         savingWorkbook.setVariates(getSelectedVariates(nurseryEditor));
 
         //savingWorkbook.setFactorsData(); 
 
         savingWorkbook.setGermplasmData(getGermplasmData(nurseryEditor));
         
         savingWorkbook.setMeasurements(getMeasurements(nurseryEditor));
         
         for (Measurement measurement : savingWorkbook.getMeasurements()) {
             measurement.setTrial(1);
         }
         
         savingWorkbook.getValuesForGroupingLabels();
 
 
         // if study already exists then update it
         // AppServicesProxy.getDefault().appServices().saveWorkbookFull(savingWorkbook);
         if (nurseryEditor.isStudyAlreadyExists()) {
             AppServicesProxy.getDefault().appServices().updateWorkbook(savingWorkbook);
         } else {
             // study does not exist!, then create it 
             AppServicesProxy.getDefault().appServices().saveWorkbookNurseryFull(savingWorkbook);
             // but if user press save study already exists
             nurseryEditor.setStudyAlreadyExists(true);
         }
     }
     
     private static Condition getConditionTrial() {
         Condition condition = new Condition();
         condition.setConditionName("OCC");
         condition.setDescription("TRIAL NUMBER");
         condition.setProperty("TRIAL INSTANCE");
         condition.setScale("NUMBER");
         condition.setMethod("ENUMERATED");
         condition.setDataType("N");
         condition.setValue(1);
         condition.setLabel("OCC");
         condition.setInstance(1);
         return condition;
     }
     
     private static List<Condition> getConditionTrialList() {
         List<Condition> conditionsTrial = new ArrayList<Condition>();
         conditionsTrial.add(getConditionTrial());
         return conditionsTrial;
     }
 
     /**
      * Get data for study
      *
      * @param studyEditor
      * @return
      */
     private static Study getStudy(NurseryEditorTopComponent nurseryEditor) {
         Study study = new Study();
         
         study.setStudy(nurseryEditor.getjTextFieldNurseryName().getText());
         study.setTitle(nurseryEditor.jTextFieldTitle.getText());
         study.setObjective(nurseryEditor.jTextFieldObjective.getText());
         study.setPmkey(nurseryEditor.jTextFieldPMKey.getText());
         if (!nurseryEditor.isStudyAlreadyExists()) {
             study.setShierarchy(nurseryEditor.getStudy().getStudyid());
         }
         study.setSstatus(1);
         study.setStarDate(nurseryEditor.jDateChooserStart.getDate());
         study.setEndDate(nurseryEditor.jDateChooserEnd.getDate());
         
         study.setStudyType(Study.S_TYPE_NURSERY);
 
 //        study = nurseryEditor.getStudy();
 
         
         if (nurseryEditor.isStudyAlreadyExists()) {
             
             org.cimmyt.cril.ibwb.domain.Study existingStudy = AppServicesProxy.getDefault().appServices().getStudyByName(nurseryEditor.getjTextFieldNurseryName().getText().trim());
             study.setStudyid(existingStudy.getStudyid());
             study.setStudy(existingStudy.getSname());
             study.setStudy(nurseryEditor.getjTextFieldNurseryName().getText());
             study.setTitle(nurseryEditor.jTextFieldTitle.getText());
             study.setObjective(nurseryEditor.jTextFieldObjective.getText());
             study.setPmkey(nurseryEditor.jTextFieldPMKey.getText());
             study.setStarDate(nurseryEditor.jDateChooserStart.getDate());
             study.setEndDate(nurseryEditor.jDateChooserEnd.getDate());
             study.setShierarchy(existingStudy.getShierarchy());
             
         } else {
             study.setStudyid(null);
         }
         return study;
     }
 
     /**
      * Get all conditions for study
      *
      * @return
      */
     private static List<Condition> getStudyConditions(NurseryEditorTopComponent nurseryEditor) {
         List<Condition> studyConditions = new ArrayList<Condition>();
         
         StudyConditionsTableModel conditionsTable = (StudyConditionsTableModel) nurseryEditor.jTableStudyConditions.getModel();
         
         studyConditions = conditionsTable.getStudyConditions();
         
         return studyConditions;
     }
 
     /**
      * Assign Conditions for study where label equals trial
      *
      * @param studyEditor
      * @return
      */
     private static List<Condition> getConditions(NurseryEditorTopComponent nurseryEditor) {
         List<Condition> conditionList = new ArrayList<Condition>();
         
         for (Condition condition : nurseryEditor.getMyWorkbook().getConditions()) {
 
 //            if (!condition.getConditionName().equals(condition.getLabel())) {
             conditionList.add(condition);
 //            }
         }
         
         return conditionList;
     }
 
     /*
      * Get selected variate from editor. It checks first selected variates in
      * listbox, then find it int variates list from template, if found then add
      * it to list
      */
     public static List<Variate> getSelectedVariates(NurseryEditorTopComponent nurseryEditor) {
         List<Variate> variates = new ArrayList<Variate>();
         variates = nurseryEditor.getDoubleListPanel().getTargetList();
         
         return variates;
     }
 
     /**
      *
      * @param workbook
      * @param traitName
      * @return
      */
     private static Variate getSelectedVariate(Workbook workbook, String trait) {
         Variate variate = null;
         
         int parenthesisPosition = trait.indexOf("(");
         String traitName = trait.substring(0, parenthesisPosition - 1).trim();
         
         for (Variate templateVariate : workbook.getVariates()) {
             if (templateVariate.getVariateName().equals(traitName)) {
                 variate = templateVariate;
                 break;
             }
         }
         
         return variate;
     }
 
     /**
      * Get the list of GERMPLASM from editor
      *
      * @param studyEditor
      * @return
      */
     private static List<List<Object>> getGermplasmData(NurseryEditorTopComponent nurseryEditor) {
         GermplasmEntriesTableModel tableModel = (GermplasmEntriesTableModel) nurseryEditor.jTableEntries.getModel();
         
         
         List<List<Object>> tempGermplasmData = tableModel.getGermplasmData();
         
         List<List<Object>> germplasmData = new ArrayList<List<Object>>();
         
         for (int row = 0; row < tempGermplasmData.size(); row++) {
             List<Object> originalColumns = tempGermplasmData.get(row);
             List<Object> germplasmColumns = new ArrayList<Object>();
             for (int col = 0; col < nurseryEditor.getMyWorkbook().getEntryFactors().size(); col++) {
                 if (col < originalColumns.size()) {
                     germplasmColumns.add(originalColumns.get(col));
                 }
             }
             germplasmData.add(germplasmColumns);
         }
         
         return germplasmData;
     }
 
     /**
      *
      * @param studyEditor
      * @return
      */
     public static List<Constant> getSelectedConstants(NurseryEditorTopComponent nurseryEditor) {
         
         List<Constant> selectedConstants = new ArrayList<Constant>();
         
         if (nurseryEditor.jTableConstants.getRowCount() != 0) {
             
             List<String> differentConstants = new ArrayList<String>();
             
             ExperimentalConditionsTableModel constantsTable = (ExperimentalConditionsTableModel) nurseryEditor.jTableConstants.getModel();
 
             // first get different constants from constants table, because it contains 
             // repeated constans for each Trial
             for (int row = 0; row < constantsTable.getRowCount(); row++) {
                 String constantName = (String) constantsTable.getValueAt(row, 1);
                 // put only different constants
                 if (!differentConstants.contains(constantName)) {
                     differentConstants.add(constantName);
                 }
             }
 
             // then, get selected constants from readed constants from template,
             // (this, because constants in constants grid does not contain property and method)
 
             for (String constantName : differentConstants) {
                 // now iterate constans to search constant name
                 for (Constant constant : nurseryEditor.getMyWorkbook().getConstants()) {
                     // Check if selected constant exists in constants from template
                     if (constantName.equals(constant.getConstantName())) {
                         selectedConstants.add(constant);
                         break;
                     }
                 }
             }
             
         }
         
         return selectedConstants;
     }
 
     /**
      * Get study conditions for each instance, this condition list contains
      * filled values for each factor
      *
      * @param studyEditor
      * @return
      */
     private static List<Condition> getConditionsData(NurseryEditorTopComponent nurseryEditor) {
         
         List<Condition> conditionsData = new ArrayList<Condition>();
         NurseryConditionsTableModel trialConditions = (NurseryConditionsTableModel) nurseryEditor.jTableNurseryConditions.getModel();
         //conditionsData = studyConditions.getTrialConditions();
 
         int totalConditions = nurseryEditor.getMyWorkbook().getConditions().size();
         int currentInstance = 0;
         int instanceCounter = 1;
         
         Condition trialInstanceCondition = nurseryEditor.getMyWorkbook().getTrialInstanceCondition();
         
         for (instanceCounter = 1; instanceCounter <= 1; instanceCounter++) {
             Condition instanceCondition = new Condition();
             try {
                 BeanUtils.copyProperties(instanceCondition, trialInstanceCondition);
                 
             } catch (Exception ex) {
                 //Exceptions.printStackTrace(ex);
             }
             instanceCondition.setInstance(instanceCounter);
             instanceCondition.setValue(instanceCounter);
             conditionsData.add(instanceCondition);
             addAllConstantsDataFromInstance(trialConditions.getTrialConditions(), instanceCounter, conditionsData);
             
         }
 
 //        for (Condition condition : studyConditions.getTrialConditions()) {
 //
 //            if (currentInstance > totalConditions) {
 //                currentInstance = 0;
 //            }
 //            if (currentInstance == 0) {
 //
 //                Condition instanceCondition =  new Condition();
 //                try {
 //                    BeanUtils.copyProperties(instanceCondition, trialInstanceCondition);
 //
 //                } catch (Exception ex) {
 //                    //Exceptions.printStackTrace(ex);
 //                }
 //                instanceCondition.setInstance(instanceCounter);                
 //                instanceCondition.setValue(instanceCounter);
 //                conditionsData.add(instanceCondition);
 //                conditionsData.
 //                instanceCounter++;
 //            }
 //            conditionsData.add(condition);
 //            currentInstance++;
 //        }
 
         return conditionsData;
 
         /*
          * *
          * List<Condition> conditionsData = new ArrayList<Condition>();
          *
          *
          * NurseryConditionsTableModel studyConditions =
          * (NurseryConditionsTableModel)
          * nurseryEditor.jTableNurseryConditions.getModel();
          *
          * conditionsData = studyConditions.getNurseryConditions();
          *
          * return conditionsData;
          */
     }
     
     private static void addAllConstantsDataFromInstance(List<Condition> trialConditions, int instanceCounter, List<Condition> conditionsData) {
         for (Condition condition : trialConditions) {
             if (condition.getInstance().intValue() == instanceCounter) {
                 conditionsData.add(condition);
             }
         }
     }
     
     private static List<Measurement> getMeasurements(NurseryEditorTopComponent nurseryEditor) {
         List<Measurement> measurements = new ArrayList<Measurement>();
         
         ObservationsTableModel model = (ObservationsTableModel) nurseryEditor.jTableObservations.getModel();
         
         int colNumber = 0;
         //---------------- Generate measurement
         for (int row = 0; row < model.getRowCount(); row++) {
             Measurement m = new Measurement();
             List<MeasurementData> mdList = new ArrayList<MeasurementData>();
             for (int col = 0; col < model.getColumnCount(); col++) {
                 
                 colNumber = model.getHeaderIndex(ObservationsTableModel.NURSERY);
                 if (colNumber != -1) {
                     m.setTrial(Integer.parseInt(model.getValueAt(row, colNumber).toString()));
                 }
                 colNumber = model.getHeaderIndex(ObservationsTableModel.ENTRY);
                 if (colNumber != -1) {
                     m.setEntry(Integer.parseInt(model.getValueAt(row, colNumber).toString()));
                 }
                 colNumber = model.getHeaderIndex(ObservationsTableModel.ENTRY_CODE);
                 if (colNumber != -1) {
                     //m.set(Integer.parseInt(model.getValueAt(row, colNumber).toString()));                    
                 }
                 colNumber = model.getHeaderIndex(ObservationsTableModel.DESIG);
                 if (colNumber != -1) {
                     m.setDesignation(model.getValueAt(row, colNumber).toString());
                 }
                 colNumber = model.getHeaderIndex(ObservationsTableModel.GID);
                 if (colNumber != -1) {
                     m.setGid(Integer.parseInt(model.getValueAt(row, colNumber).toString()));
                 }
                 colNumber = model.getHeaderIndex(ObservationsTableModel.PLOT);
                 if (colNumber != -1) {
                     m.setPlot(Integer.parseInt(model.getValueAt(row, colNumber).toString()));
                 }
                 colNumber = model.getHeaderIndex(ObservationsTableModel.REPLICATION);
                 if (colNumber != -1) {
                     if (model.getValueAt(row, colNumber) != null) {
                         m.setReplication(Integer.parseInt(model.getValueAt(row, colNumber).toString()));
                     }
                 }
                 colNumber = model.getHeaderIndex(ObservationsTableModel.BLOCK);
                 if (colNumber != -1) {
                     if (model.getValueAt(row, colNumber) != null) {
                         m.setBlock(Integer.parseInt(model.getValueAt(row, colNumber).toString()));
                     }
                 }
                 colNumber = model.getHeaderIndex(ObservationsTableModel.ROW);
                 if (colNumber != -1) {
                     if (model.getValueAt(row, colNumber) != null) {
                         m.setRow(Integer.parseInt(model.getValueAt(row, colNumber).toString()));
                     }
                 }
                 colNumber = model.getHeaderIndex(ObservationsTableModel.COL);
                 if (colNumber != -1) {
                     if (model.getValueAt(row, colNumber) != null) {
                         m.setColumn(Integer.parseInt(model.getValueAt(row, colNumber).toString()));
                     }
                 }
                 
                 
                 
                 if (model.getHeaders().get(col) instanceof Variate) {
                     Variate headerVariate = (Variate) model.getHeaders().get(col);
                     // create the measurement
                     MeasurementData md = new MeasurementData();
                     // assign variate from header
                     md.setVariate(headerVariate);
 
                     //md.setData("N", model.getValueAt(row, col));
                     md.setData(headerVariate.getDataType(), model.getValueAt(row, col));
                     mdList.add(md);
                 }
             }
             m.setMeasurementsData(mdList);
             measurements.add(m);
         }
         
         return measurements;
     }
     
     private static List<Constant> getConstantData(NurseryEditorTopComponent nurseryEditor) {
         List<Constant> constantDataList = new ArrayList<Constant>();
         if (nurseryEditor.jTableConstants.getRowCount() != 0) {
             ExperimentalConditionsTableModel model = (ExperimentalConditionsTableModel) nurseryEditor.jTableConstants.getModel();
             constantDataList = model.getConstantList();
         }
         return constantDataList;
     }
 }
