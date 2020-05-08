 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.ibpworkbench.comp.ibtools.breedingview.select;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.generationcp.commons.breedingview.xml.DesignType;
 import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.ibpworkbench.Message;
 import org.generationcp.ibpworkbench.actions.BreedingViewDesignTypeValueChangeListener;
 import org.generationcp.ibpworkbench.actions.BreedingViewEnvFactorValueChangeListener;
 import org.generationcp.ibpworkbench.actions.BreedingViewEnvNameForAnalysisValueChangeListener;
 import org.generationcp.ibpworkbench.actions.BreedingViewReplicatesValueChangeListener;
 import org.generationcp.ibpworkbench.actions.CancelDetailsAsInputForBreedingViewAction;
 import org.generationcp.ibpworkbench.actions.RunBreedingViewAction;
 import org.generationcp.ibpworkbench.util.BreedingViewInput;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.manager.api.ManagerFactoryProvider;
 import org.generationcp.middleware.manager.api.StudyDataManager;
 import org.generationcp.middleware.manager.api.TraitDataManager;
 import org.generationcp.middleware.pojos.CharacterLevel;
 import org.generationcp.middleware.pojos.Factor;
 import org.generationcp.middleware.pojos.NumericLevel;
 import org.generationcp.middleware.pojos.Trait;
 import org.generationcp.middleware.pojos.workbench.Project;
 import org.generationcp.middleware.pojos.workbench.Tool;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.mysql.jdbc.StringUtils;
 import com.vaadin.ui.AbsoluteLayout;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Select;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.Window;
 
 /**
  * 
  * @author Jeffrey Morales
  *
  */
 @Configurable
 public class SelectDetailsForBreedingViewWindow extends Window implements InitializingBean, InternationalizableComponent {
 
     private static final long serialVersionUID = 1L;
     
     private static final String GERMPLASM_ID = "germplasm id";
     private static final String GERMPLASM_IDENTIFICATION = "germplasm identification";
     private static final String GERMPLASM_ENTRY = "germplasm entry";
     private static final String TRIAL_INSTANCE = "trial instance";
     private static final String FIELD_PLOT = "field plot";
     private static final String REPLICATION = "replication";
     private static final String BLOCK = "block";
     private static final String ROW_IN_LAYOUT = "row in layout";
     private static final String COLUMN_IN_LAYOUT = "column in layout";
     private static final String RCBD_DESIGN = "RCBD";
     private static final String ALPHA_DESIGN = "ALPHA";
     private static final String ROWCOL_DESIGN = "ROWCOL";
     private static final String EXPERIMENTAL_DESIGN = "experimental design";
     private static final String STUDY_TRAIT_NAME = "study";
     
     private Label lblVersion;
     private Label lblProjectType;
     private Label lblAnalysisName;
     private Label lblSiteEnvironment;
     private Label lblSpecifyEnvFactor;
     private Label lblSelectEnvironmentForAnalysis;
     private Label lblSpecifyNameForAnalysisEnv;
     private Label lblDesign;
     private Label lblDesignType;
     private Label lblReplicates;
     private Label lblBlocks;
     private Label lblSpecifyRowFactor;
     private Label lblSpecifyColumnFactor;
     private Label lblGenotypes;
     private Button btnRun;
     private Button btnCancel;
     private TextField txtVersion;
     private TextField txtProjectType;
     private TextField txtAnalysisName;
     private TextField txtNameForAnalysisEnv;
     private Select selDesignType;
     private Select selEnvFactor;
     private Select selEnvForAnalysis;
     private Select selReplicates;
     private Select selBlocks;
     private Select selRowFactor;
     private Select selColumnFactor;
     private Select selGenotypes;
     
     private BreedingViewInput breedingViewInput;
     private Tool tool;
     private List<Factor> factorsInDataset;
     //for mapping factor ids with the key being the trait name of the factors identified by the ids
     private Map<String, Integer> factorIdsMap;
     //for mapping label ids with the key being the trait name of the factors identified by the ids
     private Map<String, Integer> labelIdsMap;
     
     private StudyDataManager studyDataManager;
     private TraitDataManager traitDataManager;
     private Project project;
     
     private AbsoluteLayout mainLayout;
     
     @Autowired 
     private ManagerFactoryProvider managerFactoryProvider;
     
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
 
     public SelectDetailsForBreedingViewWindow(Tool tool, BreedingViewInput breedingViewInput, List<Factor> factorsInDataset
             ,Project project) {
 
         this.tool = tool;
         this.breedingViewInput = breedingViewInput;
         this.factorsInDataset = factorsInDataset;
         this.project = project;
         
         setModal(true);
         
        /* Make the sub window 50% the size of the browser window */
        setWidth("60%");
         setHeight("70%");
         /*
          * Center the window both horizontally and vertically in the browser
          * window
          */
         center();
         
         setScrollable(true);
 
         setCaption("Breeding View Analysis Specifications: ");
         
     }
     
 
     public Tool getTool() {
         return tool;
     }
 
     public TextField getTxtVersion() {
         return txtVersion;
     }
 
     public Select getSelDesignType() {
         return selDesignType;
     }
     
     public BreedingViewInput getBreedingViewInput() {
         return breedingViewInput;
     }
     
     public TextField getTxtProjectType() {
         return txtProjectType;
     }
 
     public TextField getTxtAnalysisName() {
         return txtAnalysisName;
     }
     
     public TextField getTxtNameForAnalysisEnv() {
         return txtNameForAnalysisEnv;
     }
     
     public Select getSelEnvFactor() {
         return selEnvFactor;
     }
     
     public Select getSelEnvForAnalysis() {
         return selEnvForAnalysis;
     }
 
     public Select getSelReplicates() {
         return selReplicates;
     }
     
     public Select getSelBlocks() {
         return selBlocks;
     }
     
     public Select getSelRowFactor() {
         return selRowFactor;
     }
     
     public Select getSelColumnFactor() {
         return selColumnFactor;
     }
     
     public Select getSelGenotypes() {
         return selGenotypes;
     }
 
     protected void initialize() {
     }
 
     protected void initializeComponents() {
         
         mainLayout = new AbsoluteLayout();
         
         lblVersion = new Label();
         lblProjectType = new Label();
         lblAnalysisName = new Label();
         lblSiteEnvironment = new Label();
         lblSpecifyEnvFactor = new Label();
         lblSelectEnvironmentForAnalysis = new Label();
         lblSpecifyNameForAnalysisEnv = new Label();
         lblDesign = new Label();
         lblDesignType = new Label();
         lblReplicates = new Label();
         lblBlocks = new Label();
         lblSpecifyRowFactor = new Label();
         lblSpecifyColumnFactor = new Label();
         lblGenotypes = new Label();
         
         txtVersion = new TextField();
         txtVersion.setNullRepresentation("");
         
         if (!StringUtils.isNullOrEmpty(breedingViewInput.getVersion())) {
             
             txtVersion.setValue(breedingViewInput.getVersion());
             txtVersion.setReadOnly(true);
             txtVersion.setRequired(false);
             
         } else {
             
             txtVersion.setNullSettingAllowed(false);
             txtVersion.setRequired(false);
             
         }
         
         txtProjectType = new TextField();
         txtProjectType.setNullRepresentation("");
         txtProjectType.setValue("Field Trial");
         txtProjectType.setReadOnly(true);
         txtProjectType.setRequired(false);
         
         txtAnalysisName = new TextField();
         txtAnalysisName.setNullRepresentation("");
         if (!StringUtils.isNullOrEmpty(breedingViewInput.getBreedingViewProjectName())) {
             txtAnalysisName.setValue(breedingViewInput.getBreedingViewProjectName());
         }
         txtAnalysisName.setRequired(false);
         txtAnalysisName.setWidth("95%");
         
         factorIdsMap = new HashMap<String, Integer>();
         labelIdsMap = new HashMap<String, Integer>();
         populateFactorAndLabelIdsMap();
         
         selEnvFactor = new Select();
         selEnvFactor.setImmediate(true); 
         populateChoicesForEnvironmentFactor();
         selEnvFactor.setNullSelectionAllowed(true);
         selEnvFactor.setNewItemsAllowed(false);
         
         selEnvForAnalysis = new Select();
         selEnvForAnalysis.setImmediate(true); 
         populateChoicesForEnvForAnalysis();
         selEnvForAnalysis.setNullSelectionAllowed(false);
         selEnvForAnalysis.setNewItemsAllowed(false);
         
         txtNameForAnalysisEnv = new TextField();
         txtNameForAnalysisEnv.setNullRepresentation("");
         txtNameForAnalysisEnv.setRequired(false);
         
         selDesignType = new Select();
         selDesignType.setImmediate(true); 
         selDesignType.addItem(DesignType.INCOMPLETE_BLOCK_DESIGN.getName());
         selDesignType.addItem(DesignType.RANDOMIZED_BLOCK_DESIGN.getName());
         selDesignType.addItem(DesignType.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN.getName());
         selDesignType.addItem(DesignType.ROW_COLUMN_DESIGN.getName());
         selDesignType.addItem(DesignType.RESOLVABLE_ROW_COLUMN_DESIGN.getName());
         checkDesignFactor();
         selDesignType.setNullSelectionAllowed(false);
         selDesignType.setNewItemsAllowed(false);
         
         selReplicates = new Select();
         selReplicates.setImmediate(true); 
         populateChoicesForReplicates();
         selReplicates.setNullSelectionAllowed(true);
         selReplicates.setNewItemsAllowed(false);
         
         selBlocks = new Select();
         selBlocks.setImmediate(true); 
         populateChoicesForBlocks();
         selBlocks.setNullSelectionAllowed(false);
         selBlocks.setNewItemsAllowed(false);
         
         selRowFactor = new Select();
         selRowFactor.setImmediate(true); 
         populateChoicesForRowFactor();
         selRowFactor.setNullSelectionAllowed(false);
         selRowFactor.setNewItemsAllowed(false);
         
         selColumnFactor = new Select();
         selColumnFactor.setImmediate(true); 
         populateChoicesForColumnFactor();
         selColumnFactor.setNullSelectionAllowed(false);
         selColumnFactor.setNewItemsAllowed(false);
         
         refineChoicesForBlocksReplicationRowAndColumnFactos();
         
         selGenotypes = new Select();
         selGenotypes.setImmediate(true); 
         populateChoicesForGenotypes();
         selGenotypes.setNullSelectionAllowed(false);
         selGenotypes.setNewItemsAllowed(false);
         
         btnRun = new Button();
         btnCancel = new Button();
     }
 
     private void populateFactorAndLabelIdsMap(){
         for(Factor factor : this.factorsInDataset){
             try{
                 Trait trait = this.traitDataManager.getTraitById(factor.getTraitId());
                 if(trait != null){
                     String traitName = trait.getName().trim().toLowerCase();
                     
                     if(traitName.equals(TRIAL_INSTANCE)){
                         this.factorIdsMap.put(TRIAL_INSTANCE, factor.getFactorId());
                         this.labelIdsMap.put(TRIAL_INSTANCE, factor.getId());
                     } else if(traitName.equals(REPLICATION)){
                         this.factorIdsMap.put(REPLICATION, factor.getFactorId());
                         this.labelIdsMap.put(REPLICATION, factor.getId());
                     } else if(traitName.equals(BLOCK)){
                         this.factorIdsMap.put(BLOCK, factor.getFactorId());
                         this.labelIdsMap.put(BLOCK, factor.getId());
                     } else if(traitName.equals(ROW_IN_LAYOUT)){
                         this.factorIdsMap.put(ROW_IN_LAYOUT, factor.getFactorId());
                         this.labelIdsMap.put(ROW_IN_LAYOUT, factor.getId());
                     } else if(traitName.equals(COLUMN_IN_LAYOUT)){
                         this.factorIdsMap.put(COLUMN_IN_LAYOUT, factor.getFactorId());
                         this.labelIdsMap.put(COLUMN_IN_LAYOUT, factor.getId());
                     } else if(traitName.equals(GERMPLASM_ENTRY)){
                         if(factor.getDataType().equals("N")){
                             this.factorIdsMap.put(GERMPLASM_ENTRY, factor.getFactorId());
                             this.labelIdsMap.put(GERMPLASM_ENTRY, factor.getId());
                         }
                     } else if(traitName.equals(GERMPLASM_IDENTIFICATION)){
                         if(factor.getDataType().equals("N")){
                             this.factorIdsMap.put(GERMPLASM_IDENTIFICATION, factor.getFactorId());
                             this.labelIdsMap.put(GERMPLASM_IDENTIFICATION, factor.getId());
                         }
                     } else if(traitName.equals(GERMPLASM_ID)){
                         if(factor.getDataType().equals("N")){
                             this.factorIdsMap.put(GERMPLASM_ID, factor.getFactorId());
                             this.labelIdsMap.put(GERMPLASM_ID, factor.getId());
                         }
                     } else if(traitName.equals(STUDY_TRAIT_NAME)){
                         this.factorIdsMap.put(STUDY_TRAIT_NAME, factor.getFactorId());
                         this.labelIdsMap.put(STUDY_TRAIT_NAME, factor.getId());
                     } else if(traitName.equals(FIELD_PLOT)){
                         this.factorIdsMap.put(FIELD_PLOT, factor.getFactorId());
                         this.labelIdsMap.put(FIELD_PLOT, factor.getId());
                     } else if(traitName.equals(EXPERIMENTAL_DESIGN)){
                         this.factorIdsMap.put(EXPERIMENTAL_DESIGN, factor.getFactorId());
                         this.labelIdsMap.put(EXPERIMENTAL_DESIGN, factor.getId());
                     } 
                 }
                 
             }catch(MiddlewareQueryException ex){
                 continue;
             }        
         }
     }
     
     private Factor getFactorByLabelId(Integer labelId){
         for(Factor factor : this.factorsInDataset){
             if(factor.getId().equals(labelId)){
                 return factor;
             }
         }
         return null;
     }
     
     private void populateChoicesForEnvironmentFactor(){
         //try finding a factor with trait trial instance
         if(this.labelIdsMap.get(TRIAL_INSTANCE) != null){
             Factor trialInstanceFactor = getFactorByLabelId(this.labelIdsMap.get(TRIAL_INSTANCE));
             this.selEnvFactor.addItem(trialInstanceFactor.getName());
             this.selEnvFactor.setValue(trialInstanceFactor.getName());
         }
         
         //add other factors that can be selected for this
         for(Factor factor : this.factorsInDataset){
             if(!factor.getFactorId().equals(this.factorIdsMap.get(BLOCK))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(REPLICATION))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(ROW_IN_LAYOUT))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(COLUMN_IN_LAYOUT))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(EXPERIMENTAL_DESIGN))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_IDENTIFICATION))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_ID))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_ENTRY))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(STUDY_TRAIT_NAME))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(FIELD_PLOT))){
                 this.selEnvFactor.addItem(factor.getName());
             }
         }        
     }
     
     public void populateChoicesForEnvForAnalysis(){
         this.selEnvForAnalysis.removeAllItems();
         String envFactorName = (String) this.selEnvFactor.getValue();
         
         if(envFactorName != null){
             Factor envFactor = null;
             for(Factor factor : this.factorsInDataset){
                 if(factor.getName().equals(envFactorName)){
                     envFactor = factor;
                     break;
                 }
             }
             
             if(envFactor != null){
                 try{
                     if(envFactor.getDataType().equals("C")){
                         List<CharacterLevel> levelsOfEnvFactor = this.studyDataManager.getCharacterLevelsByFactorAndDatasetId(envFactor
                                 , this.breedingViewInput.getDatasetId());
                         for(CharacterLevel level : levelsOfEnvFactor){
                             this.selEnvForAnalysis.addItem(level.getValue());
                         }
                     } else if(envFactor.getDataType().equals("N")){
                         List<NumericLevel> levelsOfEnvFactor = this.studyDataManager.getNumericLevelsByFactorAndDatasetId(envFactor
                                 , this.breedingViewInput.getDatasetId());
                         for(NumericLevel level : levelsOfEnvFactor){
                             this.selEnvForAnalysis.addItem("" + level.getValue().intValue());
                         }
                     }
                 } catch(MiddlewareQueryException ex){
                     //do nothing for now
                 }
             }
         } else {
             this.selEnvForAnalysis.removeAllItems();
         }
     }
     
     private void populateChoicesForGenotypes(){
         Integer germplasmEntryFactorId = null;
         //try getting factor with trait germplasm entry
         if(this.labelIdsMap.get(GERMPLASM_ENTRY) != null){
             Factor germplasmEntryFactor = getFactorByLabelId(this.labelIdsMap.get(GERMPLASM_ENTRY));
             this.selGenotypes.addItem(germplasmEntryFactor.getName());
             this.selGenotypes.setValue(germplasmEntryFactor.getName());
             germplasmEntryFactorId = germplasmEntryFactor.getFactorId();
         } else if(this.labelIdsMap.get(GERMPLASM_ID) != null){
             //next try getting factor with trait germplasm id
             Factor germplasmEntryFactor = getFactorByLabelId(this.labelIdsMap.get(GERMPLASM_ID));
             this.selGenotypes.addItem(germplasmEntryFactor.getName());
             this.selGenotypes.setValue(germplasmEntryFactor.getName());
             germplasmEntryFactorId = germplasmEntryFactor.getFactorId();
         } else if(this.labelIdsMap.get(GERMPLASM_IDENTIFICATION) != null){
             //next try getting factor with the trait germplasm identification
             Factor germplasmEntryFactor = getFactorByLabelId(this.labelIdsMap.get(GERMPLASM_IDENTIFICATION));
             this.selGenotypes.addItem(germplasmEntryFactor.getName());
             this.selGenotypes.setValue(germplasmEntryFactor.getName());
             germplasmEntryFactorId = germplasmEntryFactor.getFactorId();
         }
         
         //and then add all factors which are labels of germplasm entry factor
         if(germplasmEntryFactorId != null){
             for(Factor factor : this.factorsInDataset){
                 if(germplasmEntryFactorId.equals(factor.getFactorId())){
                     this.selGenotypes.addItem(factor.getName());
                 } 
             }
         } else {
             //if there is no germplasm entry factor add any factor which is a possible choice
             for(Factor factor : this.factorsInDataset){
                 if(!factor.getFactorId().equals(this.factorIdsMap.get(BLOCK))
                     && !factor.getFactorId().equals(this.factorIdsMap.get(REPLICATION))
                     && !factor.getFactorId().equals(this.factorIdsMap.get(ROW_IN_LAYOUT))
                     && !factor.getFactorId().equals(this.factorIdsMap.get(COLUMN_IN_LAYOUT))
                     && !factor.getFactorId().equals(this.factorIdsMap.get(EXPERIMENTAL_DESIGN))
                     && !factor.getFactorId().equals(this.factorIdsMap.get(TRIAL_INSTANCE))
                     && !factor.getFactorId().equals(this.factorIdsMap.get(STUDY_TRAIT_NAME))
                     && !factor.getFactorId().equals(this.factorIdsMap.get(FIELD_PLOT))){
                     this.selGenotypes.addItem(factor.getName());
                 }
             }
         }
     }
     
     private void populateChoicesForReplicates(){
         //try getting a factor with trait = replication
         if(this.labelIdsMap.get(REPLICATION) != null){
             Factor replicatesFactor = getFactorByLabelId(this.labelIdsMap.get(REPLICATION));
             this.selReplicates.addItem(replicatesFactor.getName());
             this.selReplicates.setValue(replicatesFactor.getName());
         }
         
         //add other factors that can be selected for this
         for(Factor factor : this.factorsInDataset){
             if(!factor.getFactorId().equals(this.factorIdsMap.get(TRIAL_INSTANCE))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_IDENTIFICATION))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_ID))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(STUDY_TRAIT_NAME))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(EXPERIMENTAL_DESIGN))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_ENTRY))){
                 this.selReplicates.addItem(factor.getName());
             }
         } 
     }
     
     private void populateChoicesForBlocks(){
         //try getting a factor with trait = block
         if(this.labelIdsMap.get(BLOCK) != null){
             Factor blocksFactor = getFactorByLabelId(this.labelIdsMap.get(BLOCK));
             this.selBlocks.addItem(blocksFactor.getName());
             this.selBlocks.setValue(blocksFactor.getName());
         }
         
         //add other factors that can be selected for this
         for(Factor factor : this.factorsInDataset){
             if(!factor.getFactorId().equals(this.factorIdsMap.get(TRIAL_INSTANCE))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_IDENTIFICATION))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_ID))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(STUDY_TRAIT_NAME))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(EXPERIMENTAL_DESIGN))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_ENTRY))){
                 this.selBlocks.addItem(factor.getName());
             }
         }
     }
     
     private void populateChoicesForRowFactor(){
         //try getting a factor with trait = row in layout
         if(this.labelIdsMap.get(ROW_IN_LAYOUT) != null){
             Factor rowFactor = getFactorByLabelId(this.labelIdsMap.get(ROW_IN_LAYOUT));
             this.selRowFactor.addItem(rowFactor.getName());
             this.selRowFactor.setValue(rowFactor.getName());
         }
         
         //add other factors that can be selected for this
         for(Factor factor : this.factorsInDataset){
             if(!factor.getFactorId().equals(this.factorIdsMap.get(TRIAL_INSTANCE))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_IDENTIFICATION))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_ID))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(STUDY_TRAIT_NAME))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(EXPERIMENTAL_DESIGN))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_ENTRY))){
                 this.selRowFactor.addItem(factor.getName());
             }
         }
     }
     
     private void populateChoicesForColumnFactor(){
       //try getting a factor with trait = column in layout
         if(this.labelIdsMap.get(COLUMN_IN_LAYOUT) != null){
             Factor columnFactor = getFactorByLabelId(this.labelIdsMap.get(COLUMN_IN_LAYOUT));
             this.selColumnFactor.addItem(columnFactor.getName());
             this.selColumnFactor.setValue(columnFactor.getName());
         }
         
         //add other factors that can be selected for this
         for(Factor factor : this.factorsInDataset){
             if(!factor.getFactorId().equals(this.factorIdsMap.get(TRIAL_INSTANCE))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_IDENTIFICATION))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_ID))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(STUDY_TRAIT_NAME))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(EXPERIMENTAL_DESIGN))
                 && !factor.getFactorId().equals(this.factorIdsMap.get(GERMPLASM_ENTRY))){
                 this.selColumnFactor.addItem(factor.getName());
             }
         }
     }
     
     private void refineChoicesForBlocksReplicationRowAndColumnFactos(){
         if(this.selReplicates.getValue() != null){
             this.selBlocks.removeItem(this.selReplicates.getValue());
             this.selRowFactor.removeItem(this.selReplicates.getValue());
             this.selColumnFactor.removeItem(this.selReplicates.getValue());
         }
         
         if(this.selBlocks.getValue() != null){
             this.selReplicates.removeItem(this.selBlocks.getValue());
             this.selRowFactor.removeItem(this.selBlocks.getValue());
             this.selColumnFactor.removeItem(this.selBlocks.getValue());
         }
         
         if(this.selRowFactor.getValue() != null){
             this.selReplicates.removeItem(this.selRowFactor.getValue());
             this.selBlocks.removeItem(this.selRowFactor.getValue());
             this.selColumnFactor.removeItem(this.selRowFactor.getValue());
         }
         
         if(this.selColumnFactor.getValue() != null){
             this.selReplicates.removeItem(this.selColumnFactor.getValue());
             this.selBlocks.removeItem(this.selColumnFactor.getValue());
             this.selRowFactor.removeItem(this.selColumnFactor.getValue());
         }
     }
     
     private void checkDesignFactor(){
         //try getting a factor with trait = experimental design
         try{
             if(this.labelIdsMap.get(EXPERIMENTAL_DESIGN) != null){
                 Factor designFactor = getFactorByLabelId(this.labelIdsMap.get(EXPERIMENTAL_DESIGN));
                 
                 List<CharacterLevel> levelsForDesign = this.studyDataManager.getCharacterLevelsByFactorAndDatasetId(designFactor
                         , this.breedingViewInput.getDatasetId());
                 
                 if(!levelsForDesign.isEmpty()){
                     CharacterLevel level = levelsForDesign.get(0);
                     
                     if(level.getValue().equals(RCBD_DESIGN)){
                         this.selDesignType.setValue(DesignType.RANDOMIZED_BLOCK_DESIGN.getName());
                     } else if(level.getValue().equals(ALPHA_DESIGN)){
                         this.selDesignType.setValue(DesignType.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN.getName());
                     } else if(level.getValue().equals(ROWCOL_DESIGN)){
                         this.selDesignType.setValue(DesignType.RESOLVABLE_ROW_COLUMN_DESIGN.getName());
                     }
                 }
             }
         } catch(MiddlewareQueryException ex){
             //do nothing for now
         }
     }
     
     protected void initializeLayout() {
         mainLayout.setWidth("570");
         mainLayout.setHeight("500");
         
         mainLayout.addComponent(lblVersion, "left: 35px; top: 30px;");
         mainLayout.addComponent(txtVersion, "left: 135px; top: 30px;");
         mainLayout.addComponent(lblProjectType, "left: 35px; top: 60px;");
         mainLayout.addComponent(txtProjectType, "left: 135px; top: 60px;");
         mainLayout.addComponent(lblAnalysisName, "left: 35px; top: 90px;");
         mainLayout.addComponent(txtAnalysisName, "left: 135px; top: 90px;");
         mainLayout.addComponent(lblSiteEnvironment, "left: 35px; top: 120px;");
         mainLayout.addComponent(lblSpecifyEnvFactor, "left: 85px; top: 150px;");
         mainLayout.addComponent(selEnvFactor, "left: 305px; top: 150px;");
         mainLayout.addComponent(lblSelectEnvironmentForAnalysis, "left: 85px; top: 180px;");
         mainLayout.addComponent(selEnvForAnalysis, "left: 305px; top: 180px;");
         mainLayout.addComponent(lblSpecifyNameForAnalysisEnv, "left: 85px; top: 210px;");
         mainLayout.addComponent(txtNameForAnalysisEnv, "left: 305px; top: 210px;");
         mainLayout.addComponent(lblDesign, "left: 35px; top: 240px;");
         mainLayout.addComponent(lblDesignType, "left: 85px; top: 270px;");
         mainLayout.addComponent(selDesignType, "left: 305px; top: 270px;");
         mainLayout.addComponent(lblReplicates, "left: 85px; top: 300px;");
         mainLayout.addComponent(selReplicates, "left: 305px; top: 300px;");
         mainLayout.addComponent(lblBlocks, "left: 85px; top: 330px;");
         mainLayout.addComponent(selBlocks, "left: 305px; top: 330px;");
         mainLayout.addComponent(lblSpecifyRowFactor, "left: 85px; top: 360px;");
         mainLayout.addComponent(selRowFactor, "left: 305px; top: 360px;");
         mainLayout.addComponent(lblSpecifyColumnFactor, "left: 85px; top: 390px;");
         mainLayout.addComponent(selColumnFactor, "left: 305px; top: 390px;");
         mainLayout.addComponent(lblGenotypes, "left: 35px; top: 430px;");
         mainLayout.addComponent(selGenotypes, "left: 135px; top: 430px;");
         mainLayout.addComponent(btnCancel, "left: 35px; top: 460px;");
         mainLayout.addComponent(btnRun, "left: 105px; top: 460px;");
         
         mainLayout.setMargin(true);
         
        
         
         setContent(mainLayout);
     }
 
     protected void initializeActions() {
        btnCancel.addListener(new CancelDetailsAsInputForBreedingViewAction(this));
        btnRun.addListener(new RunBreedingViewAction(this));
        
        selDesignType.addListener(new BreedingViewDesignTypeValueChangeListener(this));
        selReplicates.addListener(new BreedingViewReplicatesValueChangeListener(this));
        selEnvFactor.addListener(new BreedingViewEnvFactorValueChangeListener(this));
        selEnvForAnalysis.addListener(new BreedingViewEnvNameForAnalysisValueChangeListener(this));
     }
 
     protected void assemble() {
         initialize();
         initializeComponents();
         initializeLayout();
         initializeActions();
     }
     
     @Override
     public void afterPropertiesSet() {
         ManagerFactory managerFactory = managerFactoryProvider.getManagerFactoryForProject(this.project);
         
         this.studyDataManager = managerFactory.getStudyDataManager();
         this.traitDataManager = managerFactory.getTraitDataManager();
         
         assemble();
     }
     
     @Override
     public void attach() {
         super.attach();
         
         updateLabels();
     }
 
     @Override
     public void updateLabels() {
         messageSource.setValue(lblVersion, Message.BV_VERSION);
         messageSource.setValue(lblProjectType, Message.BV_PROJECT_TYPE);
         messageSource.setValue(lblAnalysisName, Message.BV_ANALYSIS_NAME);
         messageSource.setValue(lblSiteEnvironment, Message.BV_SITE_ENVIRONMENT);
         messageSource.setValue(lblSpecifyEnvFactor, Message.BV_SPECIFY_ENV_FACTOR);
         messageSource.setValue(lblSelectEnvironmentForAnalysis, Message.BV_SELECT_ENV_FOR_ANALYSIS);
         messageSource.setValue(lblSpecifyNameForAnalysisEnv, Message.BV_SPECIFY_NAME_FOR_ANALYSIS_ENV);
         messageSource.setValue(lblDesign, Message.BV_DESIGN);
         messageSource.setValue(lblDesignType, Message.DESIGN_TYPE);
         messageSource.setValue(lblReplicates, Message.BV_SPECIFY_REPLICATES);
         messageSource.setValue(lblBlocks, Message.BV_SPECIFY_BLOCKS);
         messageSource.setValue(lblSpecifyRowFactor, Message.BV_SPECIFY_ROW_FACTOR);
         messageSource.setValue(lblSpecifyColumnFactor, Message.BV_SPECIFY_COLUMN_FACTOR);
         messageSource.setValue(lblGenotypes, Message.BV_GENOTYPES);
         messageSource.setCaption(btnRun, Message.RUN_BREEDING_VIEW);
         messageSource.setCaption(btnCancel, Message.CANCEL);
     }
 
 }
