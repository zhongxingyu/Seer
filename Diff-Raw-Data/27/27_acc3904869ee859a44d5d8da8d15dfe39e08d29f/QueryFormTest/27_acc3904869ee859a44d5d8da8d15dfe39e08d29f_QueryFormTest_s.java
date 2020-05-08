 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.web.action.query.form;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import gov.nih.nci.caintegrator.application.arraydata.PlatformDataTypeEnum;
 import gov.nih.nci.caintegrator.application.study.AnnotationFieldDescriptor;
 import gov.nih.nci.caintegrator.application.study.AnnotationGroup;
 import gov.nih.nci.caintegrator.application.study.AnnotationTypeEnum;
 import gov.nih.nci.caintegrator.application.study.DnaAnalysisDataConfiguration;
 import gov.nih.nci.caintegrator.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator.application.study.ImageDataSourceConfiguration;
 import gov.nih.nci.caintegrator.application.study.Status;
 import gov.nih.nci.caintegrator.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator.application.study.StudyManagementServiceStub;
 import gov.nih.nci.caintegrator.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator.domain.annotation.PermissibleValue;
 import gov.nih.nci.caintegrator.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator.domain.application.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator.domain.application.CopyNumberAlterationCriterion;
 import gov.nih.nci.caintegrator.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.FoldChangeCriterion;
 import gov.nih.nci.caintegrator.domain.application.GeneNameCriterion;
 import gov.nih.nci.caintegrator.domain.application.GenomicIntervalTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.IdentifierCriterion;
 import gov.nih.nci.caintegrator.domain.application.NumericComparisonCriterion;
 import gov.nih.nci.caintegrator.domain.application.NumericComparisonOperatorEnum;
 import gov.nih.nci.caintegrator.domain.application.Query;
 import gov.nih.nci.caintegrator.domain.application.RegulationTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator.domain.application.ResultTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.ResultsOrientationEnum;
 import gov.nih.nci.caintegrator.domain.application.SelectedValueCriterion;
 import gov.nih.nci.caintegrator.domain.application.SortTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.StringComparisonCriterion;
 import gov.nih.nci.caintegrator.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator.domain.application.SubjectIdentifier;
 import gov.nih.nci.caintegrator.domain.application.SubjectList;
 import gov.nih.nci.caintegrator.domain.application.SubjectListCriterion;
 import gov.nih.nci.caintegrator.domain.application.WildCardTypeEnum;
 import gov.nih.nci.caintegrator.domain.genomic.Sample;
 import gov.nih.nci.caintegrator.domain.genomic.SampleSet;
 import gov.nih.nci.caintegrator.domain.translational.Study;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.opensymphony.xwork2.ValidationAwareSupport;
 
 public class QueryFormTest {
 
     private long nextId = 0;
     private AnnotationGroup subjectGroup;
     private AnnotationGroup imagingGroup;
     private QueryForm queryForm = new QueryForm();
     private StudySubscription subscription;
     private AnnotationDefinition stringClinicalAnnotation1;
     private AnnotationDefinition stringClinicalAnnotation2;
     private AnnotationDefinition numericClinicalAnnotation;
     private AnnotationDefinition selectClinicalAnnotation1;
     private AnnotationDefinition selectClinicalAnnotation2;
     private AnnotationDefinition testImageSeriesAnnotation;
     private PermissibleValue value1 = new PermissibleValue();
     private PermissibleValue value2 = new PermissibleValue();
     private PermissibleValue value3 = new PermissibleValue();
 
     @Before
     public void setUp() {
         queryForm.setStudyManagementService(new StudyManagementServiceStub());
 
         subscription = new StudySubscription();
         subscription.setId(1L);
         Study study = new Study();
         subjectGroup = new AnnotationGroup();
         imagingGroup = new AnnotationGroup();
         subjectGroup.setName("subjects");
         imagingGroup.setName("images");
         study.getAnnotationGroups().add(subjectGroup);
         study.getAnnotationGroups().add(imagingGroup);
 
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyConfiguration.setStatus(Status.DEPLOYED);
         study.setStudyConfiguration(studyConfiguration);
         GenomicDataSourceConfiguration genomicSource = new GenomicDataSourceConfiguration();
         studyConfiguration.getGenomicDataSources().add(genomicSource);
 
         subscription.setStudy(study);
         stringClinicalAnnotation1 = createDefinition("stringClinicalAnnotation1", AnnotationTypeEnum.STRING);
         stringClinicalAnnotation2 = createDefinition("stringClinicalAnnotation2", AnnotationTypeEnum.STRING);
         numericClinicalAnnotation = createDefinition("numericClinicalAnnotation", AnnotationTypeEnum.NUMERIC);
         selectClinicalAnnotation1 = createDefinition("selectClinicalAnnotation1", AnnotationTypeEnum.STRING);
         value1.setValue("value1");
         value3.setValue("value3");
         value2.setValue("value2");
         selectClinicalAnnotation1.getPermissibleValueCollection().add(value1);
         selectClinicalAnnotation1.getPermissibleValueCollection().add(value2);
         selectClinicalAnnotation1.getPermissibleValueCollection().add(value3);
 
         selectClinicalAnnotation2 = createDefinition("selectClinicalAnnotation2", AnnotationTypeEnum.STRING);
         PermissibleValue value2_1 = new PermissibleValue();
         value2_1.setValue("value2_1");
         selectClinicalAnnotation2.getPermissibleValueCollection().add(value2_1);
         testImageSeriesAnnotation = createDefinition("testImageSeriesAnnotation", AnnotationTypeEnum.STRING);
 
         createAfd(stringClinicalAnnotation1, subjectGroup);
         createAfd(stringClinicalAnnotation2, subjectGroup);
         createAfd(numericClinicalAnnotation, subjectGroup);
         createAfd(selectClinicalAnnotation1, subjectGroup);
         createAfd(selectClinicalAnnotation2, subjectGroup);
 
         createAfd(testImageSeriesAnnotation, imagingGroup);
 
         SubjectList subjectList = new SubjectList();
         SubjectIdentifier identifier1 = new SubjectIdentifier();
         identifier1.setIdentifier("subject1");
         subjectList.getSubjectIdentifiers().add(identifier1);
         SubjectIdentifier identifier2 = new SubjectIdentifier();
         identifier2.setIdentifier("subject2");
         subjectList.getSubjectIdentifiers().add(identifier2);
         SubjectIdentifier identifier3 = new SubjectIdentifier();
         identifier3.setIdentifier("subject3");
         subjectList.getSubjectIdentifiers().add(identifier3);
         subjectList.setName("subjectList");
         subscription.getListCollection().add(subjectList);
     }
 
     private void createAfd(AnnotationDefinition subjectDef, AnnotationGroup group) {
         AnnotationFieldDescriptor fieldDescriptor = new AnnotationFieldDescriptor();
         fieldDescriptor.setShownInBrowse(true);
         fieldDescriptor.setDefinition(subjectDef);
         fieldDescriptor.setAnnotationGroup(group);
         group.getAnnotationFieldDescriptors().add(fieldDescriptor);
     }
 
     private AnnotationDefinition createDefinition(String name, AnnotationTypeEnum type) {
         AnnotationDefinition definition = new AnnotationDefinition();
         definition.setDisplayName(name);
         definition.setId(nextId++);
         definition.setDataType(type);
         return definition;
     }
 
     @Test
     public void testCreateQuery() {
         queryForm.createQuery(subscription, null, null, null);
         assertNotNull(queryForm.getQuery());
         assertNotNull(queryForm.getCriteriaGroup());
         assertEquals(subscription, queryForm.getQuery().getSubscription());
         assertEquals(5, queryForm.getAnnotations("subjects").getNames().size());
         assertEquals("selectClinicalAnnotation1", queryForm.getAnnotations("subjects").getNames().get(1));
         assertEquals(stringClinicalAnnotation1, queryForm.getAnnotations("subjects").getDefinition("stringClinicalAnnotation1").getDefinition());
 
         assertEquals(5, queryForm.getCriteriaTypeOptions().size());
         queryForm.getQuery().getSubscription().getStudy().getStudyConfiguration().getGenomicDataSources().clear();
         assertEquals(4, queryForm.getCriteriaTypeOptions().size());
 
         testImageSeriesAnnotation = createDefinition("testImageSeriesAnnotation", AnnotationTypeEnum.STRING);
 
         queryForm.getQuery().setId(1L);
         assertTrue(queryForm.isSavedQuery());
         queryForm.getQuery().setId(null);
         assertFalse(queryForm.isSavedQuery());
 
         queryForm.getResultConfiguration().setOrientation(null);
         assertEquals("", queryForm.getResultConfiguration().getOrientation());
         assertEquals(null, queryForm.getQuery().getOrientation());
         queryForm.getResultConfiguration().setOrientation(ResultsOrientationEnum.SUBJECTS_AS_COLUMNS.getValue());
         assertEquals(ResultsOrientationEnum.SUBJECTS_AS_COLUMNS.getValue(), queryForm.getResultConfiguration().getOrientation());
         assertEquals(ResultsOrientationEnum.SUBJECTS_AS_COLUMNS, queryForm.getQuery().getOrientation());
     }
 
     @Test
     public void testValidation() {
         queryForm.createQuery(subscription, null, null, null);
         CriteriaGroup group = queryForm.getCriteriaGroup();
         group.setCriterionTypeName("subjects");
         group.addCriterion(subscription.getStudy());
         AnnotationCriterionRow criterionRow = (AnnotationCriterionRow) group.getRows().get(0);
         setFieldName(criterionRow, "numericClinicalAnnotation");
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         parameter.setValue("2.0");
         ValidationAwareSupport validationAware = new ValidationAwareSupport();
         queryForm.validate(validationAware);
         assertFalse(validationAware.hasFieldErrors());
         parameter.setValue("not a number");
         assertEquals("not a number", parameter.getValue());
         queryForm.validate(validationAware);
         assertTrue(validationAware.hasActionErrors());
     }
 
     @Test
     public void testCriteriaGroup() {
         queryForm.createQuery(subscription, null, null, null);
         CriteriaGroup group = queryForm.getCriteriaGroup();
         assertEquals(BooleanOperatorEnum.AND, group.getBooleanOperator());
         group.setBooleanOperator(BooleanOperatorEnum.OR);
         assertEquals(BooleanOperatorEnum.OR, group.getBooleanOperator());
         group.setCriterionTypeName("");
         assertEquals("", group.getCriterionTypeName());
         group.setCriterionTypeName("subjects");
         assertEquals("subjects", group.getCriterionTypeName());
         assertEquals(0, group.getRows().size());
         group.addCriterion(subscription.getStudy());
         assertEquals(1, group.getRows().size());
     }
 
     @Test
     public void testCriterionRow() {
         SampleSet sampleSet1 = new SampleSet();
         sampleSet1.setName("ControlSampleSet1");
         sampleSet1.getSamples().add(new Sample());
         getFirstGenomicSource(subscription).getControlSampleSetCollection().add(sampleSet1);
         queryForm.createQuery(subscription, null, null, null);
         CriteriaGroup group = queryForm.getCriteriaGroup();
         group.setCriterionTypeName("subjects");
         group.addCriterion(subscription.getStudy());
         AnnotationCriterionRow criterionRow = (AnnotationCriterionRow) group.getRows().get(0);
         checkNewCriterionRow(group, criterionRow);
 
         checkSetNewRowToStringField(criterionRow);
         checkSetToEqualsOperator(criterionRow);
         checkSetStringOperandValue(criterionRow);
         checkChangeStringFieldOperator(criterionRow);
         checkChangeToDifferentStringField(criterionRow);
         checkChangeToSelectField(criterionRow);
         checkChangeToNumericField(criterionRow);
         checkChangeNumericOperator(criterionRow);
         checkAddImageSeriesCriterion(group);
         checkAddGeneExpressionCriterion(group);
         checkAddCopyNumberCriterion(group);
         checkRemoveRow(group);
 
     }
 
     @Test
     public void testSubjectListRow() {
         queryForm.createQuery(subscription, null, null, null);
         CriteriaGroup group = queryForm.getCriteriaGroup();
         group.setCriterionTypeName(CriterionRowTypeEnum.SAVED_LIST.getValue());
         group.addCriterion(subscription.getStudy());
         SavedListCriterionRow criterionRow = (SavedListCriterionRow) group.getRows().get(0);
         checkChangeToSubjectListField(criterionRow);
     }
 
 
     @Test
     public void testIdentifierRow() {
         queryForm.createQuery(subscription, null, null, null);
         CriteriaGroup group = queryForm.getCriteriaGroup();
         group.setCriterionTypeName(CriterionRowTypeEnum.UNIQUE_IDENTIIFER.getValue());
         group.addCriterion(subscription.getStudy());
         IdentifierCriterionRow criterionRow = (IdentifierCriterionRow) group.getRows().get(0);
         assertEquals(1, criterionRow.getAvailableFieldNames().size());
         subscription.getStudy().getStudyConfiguration().getImageDataSources().add(new ImageDataSourceConfiguration());
         assertEquals(2, criterionRow.getAvailableFieldNames().size());
         setFieldName(criterionRow, IdentifierCriterionWrapper.SUBJECT_IDENTIFIER_FIELD_NAME);
         IdentifierCriterion criterion = (IdentifierCriterion) criterionRow.getCriterion();
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         setOperator(parameter, CriterionOperatorEnum.BEGINS_WITH.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_AFTER_STRING, criterion.getWildCardType());
         setOperator(parameter, CriterionOperatorEnum.ENDS_WITH.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_BEFORE_STRING, criterion.getWildCardType());
         setOperator(parameter, CriterionOperatorEnum.EQUALS.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_OFF, criterion.getWildCardType());
         setOperator(parameter, CriterionOperatorEnum.CONTAINS.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_BEFORE_AND_AFTER_STRING, criterion.getWildCardType());
 
     }
 
     @Test
     public void testCriterionRowNoSubjectLists() {
         subscription.getListCollection().clear();
         queryForm.createQuery(subscription, null, null, null);
         CriteriaGroup group = queryForm.getCriteriaGroup();
         group.setCriterionTypeName("subjects");
         group.addCriterion(subscription.getStudy());
         AnnotationCriterionRow criterionRow = (AnnotationCriterionRow) group.getRows().get(0);
         checkNewCriterionNoSubjectLists(group, criterionRow);
     }
 
     private GenomicDataSourceConfiguration getFirstGenomicSource(StudySubscription subscription) {
         return subscription.getStudy().getStudyConfiguration().getGenomicDataSources().get(0);
     }
 
     @Test
     public void testCriterionRowNoControlSamples() {
         getFirstGenomicSource(subscription).getControlSampleSetCollection().clear();
         queryForm.createQuery(subscription, null, null, null);
         CriteriaGroup group = queryForm.getCriteriaGroup();
         group.setCriterionTypeName(CriterionRowTypeEnum.GENE_EXPRESSION.getValue());
         group.addCriterion(subscription.getStudy());
         GeneExpressionCriterionRow criterionRow = (GeneExpressionCriterionRow) group.getRows().get(0);
         assertEquals(2, criterionRow.getAvailableFieldNames().size());
         assertTrue(criterionRow.getAvailableFieldNames().contains("Gene Name"));
         assertFalse(criterionRow.getAvailableFieldNames().contains("Fold Change"));
     }
 
     /**
      * @param group
      */
     private void checkRemoveRow(CriteriaGroup group) {
         assertEquals(4, group.getRows().size());
         assertEquals(4, queryForm.getQuery().getCompoundCriterion().getCriterionCollection().size());
         group.removeRow(3);
         assertEquals(3, group.getRows().size());
         assertEquals(3, queryForm.getQuery().getCompoundCriterion().getCriterionCollection().size());
         group.removeRow(2);
         assertEquals(2, group.getRows().size());
         assertEquals(2, queryForm.getQuery().getCompoundCriterion().getCriterionCollection().size());
         group.removeRow(0);
         assertEquals(1, group.getRows().size());
         assertEquals(1, queryForm.getQuery().getCompoundCriterion().getCriterionCollection().size());
         group.removeRow(0);
         assertEquals(0, group.getRows().size());
         assertEquals(0, queryForm.getQuery().getCompoundCriterion().getCriterionCollection().size());
     }
 
     @SuppressWarnings("unchecked")
     private void checkChangeToSelectField(AnnotationCriterionRow criterionRow) {
         setFieldName(criterionRow, "selectClinicalAnnotation1");
         SelectedValueCriterion criterion = (SelectedValueCriterion) criterionRow.getCriterion();
         assertEquals(selectClinicalAnnotation1, criterion.getAnnotationFieldDescriptor().getDefinition());
         assertTrue(criterion.getValueCollection().isEmpty());
         assertEquals(1, criterionRow.getParameters().size());
         SelectListParameter<PermissibleValue> select = (SelectListParameter<PermissibleValue>) criterionRow.getParameters().get(0);
         assertEquals(CriterionOperatorEnum.EQUALS.getValue(), select.getOperator());
         assertTrue(select.getAvailableOperators().contains(CriterionOperatorEnum.EQUALS.getValue()));
         assertTrue(select.getAvailableOperators().contains(CriterionOperatorEnum.IN.getValue()));
         assertEquals("value1", select.getOptions().get(0).getDisplayValue());
         assertEquals("value2", select.getOptions().get(1).getDisplayValue());
         assertEquals("value3", select.getOptions().get(2).getDisplayValue());
         select.setValue("value2");
         assertEquals(1, criterion.getValueCollection().size());
         assertTrue(criterion.getValueCollection().contains(value2));
         setOperator(select, CriterionOperatorEnum.IN.getValue());
         assertEquals(1, criterionRow.getParameters().size());
         MultiSelectParameter<PermissibleValue> multiSelect = (MultiSelectParameter<PermissibleValue>) criterionRow.getParameters().get(0);
         assertEquals(CriterionOperatorEnum.IN.getValue(), multiSelect.getOperator());
         assertTrue(multiSelect.getAvailableOperators().contains(CriterionOperatorEnum.EQUALS.getValue()));
         assertTrue(multiSelect.getAvailableOperators().contains(CriterionOperatorEnum.IN.getValue()));
         assertEquals("value1", multiSelect.getOptions().get(0).getDisplayValue());
         assertEquals("value2", multiSelect.getOptions().get(1).getDisplayValue());
         assertEquals("value3", multiSelect.getOptions().get(2).getDisplayValue());
         assertTrue(multiSelect.getValues().length == 1);
         assertTrue(multiSelect.getValues()[0] == "value2");
         multiSelect.setValues(new String[] {"value1", "value3"});
         assertTrue(multiSelect.getValues().length == 2);
         assertTrue(multiSelect.getValues()[0] == "value1");
         assertTrue(multiSelect.getValues()[1] == "value3");
         assertEquals(2, criterion.getValueCollection().size());
         assertTrue(criterion.getValueCollection().contains(value1));
         assertTrue(criterion.getValueCollection().contains(value3));
         multiSelect.setValues(new String[] {"value1", "value3"});
         assertEquals(2, multiSelect.getValues().length);
         assertTrue(multiSelect.getValues()[0] == "value1");
         assertTrue(multiSelect.getValues()[1] == "value3");
         setFieldName(criterionRow, "selectClinicalAnnotation2");
         assertEquals(CriterionOperatorEnum.IN.getValue(), select.getOperator());
         multiSelect = (MultiSelectParameter<PermissibleValue>) criterionRow.getParameters().get(0);
         assertEquals(1, multiSelect.getOptions().size());
         assertEquals("value2_1", multiSelect.getOptions().get(0).getDisplayValue());
         assertEquals(0, multiSelect.getValues().length);
         multiSelect.setValues(new String[] {"invalid value"});
         assertEquals(0, multiSelect.getValues().length);
     }
 
     @SuppressWarnings("unchecked")
     private void checkChangeToSubjectListField(SavedListCriterionRow criterionRow) {
         setFieldName(criterionRow, SubjectListCriterionWrapper.SUBJECT_LIST_FIELD_NAME);
         SubjectListCriterion criterion = (SubjectListCriterion) criterionRow.getCriterion();
         MultiSelectParameter<PermissibleValue> multiSelect = (MultiSelectParameter<PermissibleValue>) criterionRow.getParameters().get(0);
         assertEquals("subjectList", multiSelect.getOptions().get(0).getDisplayValue());
         multiSelect.setValues(new String[] {"subjectList"});
         assertEquals(3, criterion.getSubjectIdentifiers().size());
     }
 
     private void checkChangeNumericOperator(AnnotationCriterionRow criterionRow) {
         NumericComparisonCriterion criterion = (NumericComparisonCriterion) criterionRow.getCriterion();
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         setOperator(parameter, CriterionOperatorEnum.GREATER_THAN.getValue());
         assertEquals(NumericComparisonOperatorEnum.GREATER, criterion.getNumericComparisonOperator());
         setOperator(parameter, CriterionOperatorEnum.GREATER_THAN_OR_EQUAL_TO.getValue());
         assertEquals(NumericComparisonOperatorEnum.GREATEROREQUAL, criterion.getNumericComparisonOperator());
         setOperator(parameter, CriterionOperatorEnum.LESS_THAN.getValue());
         assertEquals(NumericComparisonOperatorEnum.LESS, criterion.getNumericComparisonOperator());
         setOperator(parameter, CriterionOperatorEnum.LESS_THAN_OR_EQUAL_TO.getValue());
         assertEquals(NumericComparisonOperatorEnum.LESSOREQUAL, criterion.getNumericComparisonOperator());
         setOperator(parameter, CriterionOperatorEnum.EQUALS.getValue());
         assertEquals(NumericComparisonOperatorEnum.EQUAL, criterion.getNumericComparisonOperator());
     }
 
     @SuppressWarnings("unchecked")
     private void checkAddCopyNumberCriterion(CriteriaGroup group) {
         group.setCriterionTypeName(CriterionRowTypeEnum.COPY_NUMBER.getValue());
         group.addCriterion(subscription.getStudy());
         assertEquals(4, group.getRows().size());
         assertEquals(3, group.getCompoundCriterion().getCriterionCollection().size());
         CopyNumberCriterionRow criterionRow = (CopyNumberCriterionRow) group.getRows().get(3);
 
         assertEquals(2, criterionRow.getAvailableFieldNames().size());
         assertTrue(criterionRow.getAvailableFieldNames().contains("Gene Name"));
         assertTrue(criterionRow.getAvailableFieldNames().contains("Segmentation"));
 
         getFirstGenomicSource(subscription).setDataType(PlatformDataTypeEnum.COPY_NUMBER);
         getFirstGenomicSource(subscription).setDnaAnalysisDataConfiguration(new DnaAnalysisDataConfiguration());
         getFirstGenomicSource(subscription).getDnaAnalysisDataConfiguration().setUseCghCall(true);
 
         assertEquals(3, criterionRow.getAvailableFieldNames().size());
         assertTrue(criterionRow.getAvailableFieldNames().contains("Gene Name"));
         assertTrue(criterionRow.getAvailableFieldNames().contains("Calls"));
 
         assertEquals(group, criterionRow.getGroup());
         setFieldName(criterionRow, "Gene Name");
         assertEquals(4, group.getCompoundCriterion().getCriterionCollection().size());
         assertEquals("Gene Name", criterionRow.getFieldName());
         assertTrue(criterionRow.getCriterion() instanceof GeneNameCriterion);
         ((TextFieldParameter) criterionRow.getParameters().get(0)).setValue("EGFR");
         assertEquals("EGFR", ((GeneNameCriterion) criterionRow.getCriterion()).getGeneSymbol());
 
         setFieldName(criterionRow, "Segmentation");
         assertEquals(4, group.getCompoundCriterion().getCriterionCollection().size());
         assertEquals("Segmentation", criterionRow.getFieldName());
         assertTrue(criterionRow.getCriterion() instanceof CopyNumberAlterationCriterion);
         ((TextFieldParameter) criterionRow.getParameters().get(3)).setValue("EGFR");
         assertEquals("EGFR", ((CopyNumberAlterationCriterion) criterionRow.getCriterion()).getGeneSymbol());
 
         setFieldName(criterionRow, "Segmentation");
         ((SelectListParameter<GenomicIntervalTypeEnum>) criterionRow.getParameters().get(2)).setValue(GenomicIntervalTypeEnum.CHROMOSOME_COORDINATES.getValue());
         queryForm.processCriteriaChanges();
         assertEquals(4, group.getCompoundCriterion().getCriterionCollection().size());
         assertEquals("Segmentation", criterionRow.getFieldName());
         assertTrue(criterionRow.getCriterion() instanceof CopyNumberAlterationCriterion);
         ((TextFieldParameter) criterionRow.getParameters().get(4)).setValue("1");
         assertEquals("1", ((CopyNumberAlterationCriterion) criterionRow.getCriterion()).getChromosomeNumber().toString());
         ((TextFieldParameter) criterionRow.getParameters().get(5)).setValue("2");
         assertEquals("2", ((CopyNumberAlterationCriterion) criterionRow.getCriterion()).getChromosomeCoordinateHigh().toString());
 
         setFieldName(criterionRow, "Calls");
         assertEquals(4, group.getCompoundCriterion().getCriterionCollection().size());
         assertEquals("Calls", criterionRow.getFieldName());
         assertTrue(criterionRow.getCriterion() instanceof CopyNumberAlterationCriterion);
         ((TextFieldParameter) criterionRow.getParameters().get(2)).setValue("EGFR");
         assertEquals("EGFR", ((CopyNumberAlterationCriterion) criterionRow.getCriterion()).getGeneSymbol());
 
         setFieldName(criterionRow, "Calls");
         ((SelectListParameter<GenomicIntervalTypeEnum>) criterionRow.getParameters().get(1)).setValue(GenomicIntervalTypeEnum.CHROMOSOME_COORDINATES.getValue());
         queryForm.processCriteriaChanges();
         assertEquals(4, group.getCompoundCriterion().getCriterionCollection().size());
         assertEquals("Calls", criterionRow.getFieldName());
         assertTrue(criterionRow.getCriterion() instanceof CopyNumberAlterationCriterion);
         ((TextFieldParameter) criterionRow.getParameters().get(3)).setValue("1");
         assertEquals("1", ((CopyNumberAlterationCriterion) criterionRow.getCriterion()).getChromosomeNumber().toString());
         ((TextFieldParameter) criterionRow.getParameters().get(4)).setValue("2");
         assertEquals("2", ((CopyNumberAlterationCriterion) criterionRow.getCriterion()).getChromosomeCoordinateHigh().toString());
 
     }
 
     @SuppressWarnings("unchecked")
     private void checkAddGeneExpressionCriterion(CriteriaGroup group) {
         group.setCriterionTypeName(CriterionRowTypeEnum.GENE_EXPRESSION.getValue());
         group.addCriterion(subscription.getStudy());
         assertEquals(3, group.getRows().size());
         assertEquals(2, group.getCompoundCriterion().getCriterionCollection().size());
         GeneExpressionCriterionRow criterionRow = (GeneExpressionCriterionRow) group.getRows().get(2);
 
         assertEquals(3, criterionRow.getAvailableFieldNames().size());
         assertTrue(criterionRow.getAvailableFieldNames().contains("Gene Name"));
         assertTrue(criterionRow.getAvailableFieldNames().contains("Fold Change"));
 
         assertEquals(group, criterionRow.getGroup());
         setFieldName(criterionRow, "Gene Name");
         assertEquals(3, group.getCompoundCriterion().getCriterionCollection().size());
         assertEquals("Gene Name", criterionRow.getFieldName());
         assertTrue(criterionRow.getCriterion() instanceof GeneNameCriterion);
         ((TextFieldParameter) criterionRow.getParameters().get(0)).setValue("EGFR");
         assertEquals("EGFR", ((GeneNameCriterion) criterionRow.getCriterion()).getGeneSymbol());
 
         setFieldName(criterionRow, "Fold Change");
         assertEquals(0, criterionRow.getParameters().get(0).getAvailableOperators().size());
         assertTrue(criterionRow.getCriterion() instanceof FoldChangeCriterion);
         assertEquals(4, criterionRow.getParameters().size());
         TextFieldParameter geneSymbolParameter = ((TextFieldParameter) criterionRow.getParameters().get(0));
         SelectListParameter<String> controlSampleSetNameParameter = ((SelectListParameter<String>) criterionRow.getParameters().get(1));
         SelectListParameter<RegulationTypeEnum> regulationParameter = ((SelectListParameter<RegulationTypeEnum>) criterionRow.getParameters().get(2));
         TextFieldParameter foldsUpParameter = ((TextFieldParameter) criterionRow.getParameters().get(3));
         geneSymbolParameter.setValue("EGFR");
         controlSampleSetNameParameter.setValue("ControlSampleSet1");
         regulationParameter.setValue(RegulationTypeEnum.UP.getValue());
         foldsUpParameter.setValue("2.0");
         FoldChangeCriterion foldChangeCriterion = (FoldChangeCriterion) criterionRow.getCriterion();
         assertEquals("EGFR", foldChangeCriterion.getGeneSymbol());
         assertEquals("ControlSampleSet1", foldChangeCriterion.getControlSampleSetName());
         assertEquals(RegulationTypeEnum.UP, foldChangeCriterion.getRegulationType());
         assertEquals(2.0, foldChangeCriterion.getFoldsUp(), 0.0);
         assertEquals(3, group.getCompoundCriterion().getCriterionCollection().size());
 
         regulationParameter.setValue(RegulationTypeEnum.UP_OR_DOWN.getValue());
         TextFieldParameter foldsDownParameter = ((TextFieldParameter) criterionRow.getParameters().get(3));
         foldsUpParameter = ((TextFieldParameter) criterionRow.getParameters().get(4));
         ValidationAwareSupport validationAware = new ValidationAwareSupport();
         queryForm.validate(validationAware);
         assertFalse(validationAware.hasFieldErrors());
         foldsUpParameter.setValue("not a number");
         assertEquals("not a number", foldsUpParameter.getValue());
         assertEquals(2.0, foldChangeCriterion.getFoldsUp(), 0.0);
         foldsDownParameter.setValue("not a number");
         assertEquals("not a number", foldsDownParameter.getValue());
         assertEquals(2.0, foldChangeCriterion.getFoldsDown(), 0.0);
         queryForm.validate(validationAware);
         assertTrue(validationAware.hasActionErrors());
 
         regulationParameter.setValue(RegulationTypeEnum.UP.getValue());
         assertEquals(3, group.getCompoundCriterion().getCriterionCollection().size());
         regulationParameter.setValue(RegulationTypeEnum.UP_OR_DOWN.getValue());
         assertEquals(5, criterionRow.getParameters().size());
         regulationParameter.setValue(RegulationTypeEnum.DOWN.getValue());
         assertEquals(4, criterionRow.getParameters().size());
         regulationParameter.setValue(RegulationTypeEnum.UNCHANGED.getValue());
         assertEquals(5, criterionRow.getParameters().size());
     }
 
 
     private void checkAddImageSeriesCriterion(CriteriaGroup group) {
         group.setCriterionTypeName("images");
         group.addCriterion(subscription.getStudy());
         AnnotationCriterionRow imageSeriesCriterionRow = (AnnotationCriterionRow) group.getRows().get(1);
         assertEquals(1, imageSeriesCriterionRow.getAvailableFieldNames().size());
         assertTrue(imageSeriesCriterionRow.getAvailableFieldNames().contains("testImageSeriesAnnotation"));
         setFieldName(imageSeriesCriterionRow, "testImageSeriesAnnotation");
         assertEquals(group, imageSeriesCriterionRow.getGroup());
         TextFieldParameter textField = (TextFieldParameter) imageSeriesCriterionRow.getParameters().get(0);
         setOperator(textField, CriterionOperatorEnum.EQUALS.getValue());
         textField.setValue("value");
         assertEquals(2, group.getRows().size());
         assertEquals(2, group.getCompoundCriterion().getCriterionCollection().size());
     }
 
     private void checkChangeToNumericField(AnnotationCriterionRow criterionRow) {
         setFieldName(criterionRow, "numericClinicalAnnotation");
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         assertEquals(5 , parameter.getAvailableOperators().size());
         assertEquals("equals", parameter.getOperator());
         assertEquals(CriterionOperatorEnum.EQUALS.getValue(), parameter.getOperator());
         assertTrue(criterionRow.getCriterion() instanceof NumericComparisonCriterion);
         assertEquals(1, criterionRow.getGroup().getCompoundCriterion().getCriterionCollection().size());
         assertTrue(criterionRow.getGroup().getCompoundCriterion().getCriterionCollection().iterator().next() instanceof NumericComparisonCriterion);
     }
 
 
     private void checkChangeToDifferentStringField(AnnotationCriterionRow criterionRow) {
         setFieldName(criterionRow, "stringClinicalAnnotation2");
         assertEquals("stringClinicalAnnotation2", criterionRow.getFieldName());
         StringComparisonCriterion criterion = (StringComparisonCriterion) criterionRow.getCriterion();
         assertEquals(WildCardTypeEnum.WILDCARD_BEFORE_AND_AFTER_STRING, criterion.getWildCardType());
         assertEquals("value", ((TextFieldParameter) criterionRow.getParameters().get(0)).getValue());
         assertEquals("value", criterion.getStringValue());
         assertEquals(stringClinicalAnnotation2, criterion.getAnnotationFieldDescriptor().getDefinition());
     }
 
     private void checkChangeStringFieldOperator(AnnotationCriterionRow criterionRow) {
         StringComparisonCriterion criterion = (StringComparisonCriterion) criterionRow.getCriterion();
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         setOperator(parameter, CriterionOperatorEnum.BEGINS_WITH.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_AFTER_STRING, criterion.getWildCardType());
         setOperator(parameter, CriterionOperatorEnum.ENDS_WITH.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_BEFORE_STRING, criterion.getWildCardType());
         setOperator(parameter, CriterionOperatorEnum.EQUALS.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_OFF, criterion.getWildCardType());
         setOperator(parameter, CriterionOperatorEnum.CONTAINS.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_BEFORE_AND_AFTER_STRING, criterion.getWildCardType());
         assertEquals("value", ((TextFieldParameter) criterionRow.getParameters().get(0)).getValue());
         assertEquals("value", criterion.getStringValue());
     }
 
     private void setOperator(AbstractCriterionParameter parameter, String value) {
         parameter.setOperator(value);
         queryForm.processCriteriaChanges();
     }
 
     private void setFieldName(AbstractCriterionRow row, String name) {
         row.setFieldName(name);
         queryForm.processCriteriaChanges();
     }
 
     private void checkSetStringOperandValue(AnnotationCriterionRow criterionRow) {
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         parameter.setValue("value");
         assertEquals("value", parameter.getValue());
         StringComparisonCriterion criterion = (StringComparisonCriterion) criterionRow.getCriterion();
         assertEquals("value", criterion.getStringValue());
     }
 
     private void checkNewCriterionRow(CriteriaGroup group, AnnotationCriterionRow criterionRow) {
         assertEquals(5, criterionRow.getAvailableFieldNames().size());
         assertTrue(criterionRow.getAvailableFieldNames().contains("stringClinicalAnnotation1"));
         assertEquals(group, criterionRow.getGroup());
         assertEquals(0 , criterionRow.getParameters().size());
         assertEquals("", criterionRow.getFieldName());
     }
 
     private void checkNewCriterionNoSubjectLists(CriteriaGroup group, AnnotationCriterionRow criterionRow) {
         assertEquals(5, criterionRow.getAvailableFieldNames().size());
         assertTrue(criterionRow.getAvailableFieldNames().contains("stringClinicalAnnotation1"));
         assertFalse(criterionRow.getAvailableFieldNames().contains(SubjectListCriterionWrapper.SUBJECT_LIST_FIELD_NAME));
         assertEquals(group, criterionRow.getGroup());
         assertEquals(0 , criterionRow.getParameters().size());
         assertEquals("", criterionRow.getFieldName());
     }
 
     private void checkSetToEqualsOperator(AnnotationCriterionRow criterionRow) {
         assertEquals(1, criterionRow.getParameters().size());
         assertTrue(criterionRow.getParameters().get(0) instanceof TextFieldParameter);
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         setOperator(parameter, CriterionOperatorEnum.EQUALS.getValue());
         assertEquals(CriterionOperatorEnum.EQUALS.getValue(), parameter.getOperator());
         StringComparisonCriterion criterion = (StringComparisonCriterion) criterionRow.getCriterion();
         assertNotNull(criterion);
         assertEquals(WildCardTypeEnum.WILDCARD_OFF, criterion.getWildCardType());
         assertEquals(stringClinicalAnnotation1, criterion.getAnnotationFieldDescriptor().getDefinition());
     }
 
     private void checkSetNewRowToStringField(AnnotationCriterionRow criterionRow) {
         setFieldName(criterionRow, "stringClinicalAnnotation1");
         assertEquals("stringClinicalAnnotation1", criterionRow.getFieldName());
         assertEquals(5 , criterionRow.getParameters().get(0).getAvailableOperators().size());
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testSetQuery() {
         Query query = new Query();
         query.setSubscription(subscription);
         CompoundCriterion compoundCriterion = new CompoundCriterion();
         compoundCriterion.setId(1L);
         compoundCriterion.setBooleanOperator(BooleanOperatorEnum.OR);
         query.setCompoundCriterion(compoundCriterion);
         compoundCriterion.setCriterionCollection(new ArrayList<AbstractCriterion>());
         query.setColumnCollection(new HashSet<ResultColumn>());
 
         StringComparisonCriterion stringCriterion = new StringComparisonCriterion();
         stringCriterion.setId(1L);
         stringCriterion.setAnnotationFieldDescriptor(createAFDForDefinition(stringClinicalAnnotation1, subjectGroup));
         stringCriterion.setEntityType(EntityTypeEnum.SUBJECT);
         stringCriterion.setStringValue("value");
         stringCriterion.setWildCardType(WildCardTypeEnum.WILDCARD_BEFORE_AND_AFTER_STRING);
         compoundCriterion.getCriterionCollection().add(stringCriterion);
 
         FoldChangeCriterion foldChangeCriterion = new FoldChangeCriterion();
         foldChangeCriterion.setGeneSymbol("EGFR");
         foldChangeCriterion.setControlSampleSetName("controlSampleSet1");
         foldChangeCriterion.setFoldsDown(3.0f);
         foldChangeCriterion.setRegulationType(RegulationTypeEnum.DOWN);
         foldChangeCriterion.setId(2L);
         compoundCriterion.getCriterionCollection().add(foldChangeCriterion);
 
         GeneNameCriterion geneNameCriterion = new GeneNameCriterion();
         geneNameCriterion.setGeneSymbol("EGFR");
         geneNameCriterion.setId(3L);
         compoundCriterion.getCriterionCollection().add(geneNameCriterion);
 
         ResultColumn subjectColumn1 = new ResultColumn();
         subjectColumn1.setEntityType(EntityTypeEnum.SUBJECT);
         subjectColumn1.setAnnotationFieldDescriptor(createAFDForDefinition(stringClinicalAnnotation1, subjectGroup));
         subjectColumn1.setColumnIndex(2);
         query.getColumnCollection().add(subjectColumn1);
 
         ResultColumn subjectColumn2 = new ResultColumn();
         subjectColumn2.setEntityType(EntityTypeEnum.SUBJECT);
         subjectColumn2.setAnnotationFieldDescriptor(createAFDForDefinition(numericClinicalAnnotation, subjectGroup));
         subjectColumn2.setColumnIndex(1);
         query.getColumnCollection().add(subjectColumn2);
 
         ResultColumn imageSeriesColumn = new ResultColumn();
         imageSeriesColumn.setEntityType(EntityTypeEnum.IMAGESERIES);
         imageSeriesColumn.setAnnotationFieldDescriptor(createAFDForDefinition(testImageSeriesAnnotation, imagingGroup));
         imageSeriesColumn.setColumnIndex(0);
         query.getColumnCollection().add(imageSeriesColumn);
 
         queryForm.setQuery(query, null, null, null);
         assertNotNull(queryForm.getQuery());
         CriteriaGroup group = queryForm.getCriteriaGroup();
         assertNotNull(group);
         assertEquals(compoundCriterion, group.getCompoundCriterion());
         assertEquals(BooleanOperatorEnum.OR, group.getBooleanOperator());
         assertEquals(3, group.getRows().size());
 
         AnnotationCriterionRow criterionRow = (AnnotationCriterionRow) group.getRows().get(0);
         assertEquals(stringCriterion, criterionRow.getCriterion());
         assertEquals(stringClinicalAnnotation1.getDisplayName(), criterionRow.getFieldName());
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         assertEquals(CriterionOperatorEnum.CONTAINS.getValue(), parameter.getOperator());
         assertEquals("value", parameter.getValue());
         assertEquals("queryForm.criteriaGroup.rows[0].parameters[0]", parameter.getFormFieldName());
 
         GeneExpressionCriterionRow foldChangeRow = (GeneExpressionCriterionRow) group.getRows().get(1);
         assertEquals(foldChangeCriterion, foldChangeRow.getCriterion());
         assertEquals(FoldChangeCriterionWrapper.FOLD_CHANGE, foldChangeRow.getFieldName());
         TextFieldParameter geneNameParameter = (TextFieldParameter) foldChangeRow.getParameters().get(0);
         assertEquals("EGFR", geneNameParameter.getValue());
         SelectListParameter<String> controlSampleSetNameParameter = (SelectListParameter<String>) foldChangeRow.getParameters().get(1);
         SelectListParameter<String> regulationParameter = (SelectListParameter<String>) foldChangeRow.getParameters().get(2);
         TextFieldParameter foldsParameter = (TextFieldParameter) foldChangeRow.getParameters().get(3);
         assertEquals(null, controlSampleSetNameParameter.getValue());
         assertEquals("Down", regulationParameter.getValue());
         assertEquals("3.0", foldsParameter.getValue());
 
         GeneExpressionCriterionRow geneNameRow = (GeneExpressionCriterionRow) group.getRows().get(2);
         assertEquals(geneNameCriterion, geneNameRow.getCriterion());
         assertEquals(GeneNameCriterionWrapper.FIELD_NAME, geneNameRow.getFieldName());
         geneNameParameter = (TextFieldParameter) geneNameRow.getParameters().get(0);
         assertEquals("EGFR", geneNameParameter.getValue());
 
         assertEquals(3, queryForm.getResultConfiguration().getSelectedColumns().size());
         assertEquals(testImageSeriesAnnotation, queryForm.getResultConfiguration().getSelectedColumns().get(0).getAnnotationDefinition());
         assertEquals(numericClinicalAnnotation, queryForm.getResultConfiguration().getSelectedColumns().get(1).getAnnotationDefinition());
         assertEquals(stringClinicalAnnotation1, queryForm.getResultConfiguration().getSelectedColumns().get(2).getAnnotationDefinition());
 
         assertEquals(8, queryForm.getResultConfiguration().getColumnSelectionLists().get(1).getColumnList().getOptions().size());
         List<String> columnNames = Arrays.asList(queryForm.getResultConfiguration().getColumnSelectionLists().get(1).getValues());
         assertEquals(2, columnNames.size());
         assertTrue(columnNames.contains("stringClinicalAnnotation1"));
         assertTrue(columnNames.contains("numericClinicalAnnotation"));
         assertEquals(2, queryForm.getResultConfiguration().getColumnSelectionLists().get(0).getColumnList().getOptions().size());
         assertEquals(1, queryForm.getResultConfiguration().getColumnSelectionLists().get(0).getValues().length);
 
         queryForm.getResultConfiguration().getColumnSelectionLists().get(0).setValues(new String[0]);
         queryForm.getResultConfiguration().getColumnSelectionLists().get(1)
             .setValues(new String[] {"stringClinicalAnnotation2"});
         assertEquals(1, queryForm.getResultConfiguration().getColumnSelectionLists().get(1).getValues().length);
         assertEquals(1, queryForm.getResultConfiguration().getSelectedColumns().size());
         ResultColumn column = queryForm.getResultConfiguration().getSelectedColumns().iterator().next();
         assertEquals(stringClinicalAnnotation2, column.getAnnotationDefinition());
         assertEquals(0, (int) column.getColumnIndex());
         assertEquals(1, queryForm.getResultConfiguration().getColumnIndex("stringClinicalAnnotation2"));
         assertEquals(1, queryForm.getResultConfiguration().getSelectedColumns().size());
         assertEquals(stringClinicalAnnotation2,
                 queryForm.getResultConfiguration().getSelectedColumns().get(0).getAnnotationDefinition());
 
         ValidationAwareSupport validationAware = new ValidationAwareSupport();
         queryForm.validate(validationAware);
         assertFalse(validationAware.hasFieldErrors());
 
        queryForm.getResultConfiguration().setSortType("invalidColumn", SortTypeEnum.ASCENDING);
         assertEquals("", queryForm.getResultConfiguration().getSortType("invalidColumn"));
        queryForm.getResultConfiguration().setSortType("stringClinicalAnnotation2", SortTypeEnum.ASCENDING);
        assertEquals(SortTypeEnum.ASCENDING.getValue(), queryForm.getResultConfiguration()
                 .getSortType("stringClinicalAnnotation2"));
         queryForm.getResultConfiguration().setSortType("stringClinicalAnnotation2", null);
         assertEquals("", queryForm.getResultConfiguration().getSortType("stringClinicalAnnotation2"));
 
     }
 
     private AnnotationFieldDescriptor createAFDForDefinition(AnnotationDefinition ad, AnnotationGroup ag) {
         AnnotationFieldDescriptor afd = new AnnotationFieldDescriptor();
         afd.setDefinition(ad);
         afd.setAnnotationGroup(ag);
         ag.getAnnotationFieldDescriptors().add(afd);
         return afd;
     }
 
     @Test
     public void testIsPotentialLargeQuery() {
         assertFalse(queryForm.isPotentiallyLargeQuery());
         queryForm.createQuery(subscription, null, null, null);
         assertFalse(queryForm.isPotentiallyLargeQuery());
         queryForm.getQuery().setResultType(ResultTypeEnum.GENE_EXPRESSION);
         assertTrue(queryForm.isPotentiallyLargeQuery());
         queryForm.getCriteriaGroup().setCriterionTypeName("Gene Expression");
         queryForm.getCriteriaGroup().addCriterion(subscription.getStudy());
         queryForm.getCriteriaGroup().getRows().get(0).setCriterion(new GeneNameCriterion());
         assertFalse(queryForm.isPotentiallyLargeQuery());
         queryForm.getCriteriaGroup().getRows().get(0).setCriterion(new FoldChangeCriterion());
         assertFalse(queryForm.isPotentiallyLargeQuery());
     }
 
     @Test
     public void testResultTypes() {
         queryForm.createQuery(subscription, null, null, null);
         assertEquals(3, queryForm.getResultTypes().size());
         GenomicDataSourceConfiguration genomicSource = new GenomicDataSourceConfiguration();
         DnaAnalysisDataConfiguration dnaAnalysisConfiguration = new DnaAnalysisDataConfiguration();
         genomicSource.setDnaAnalysisDataConfiguration(dnaAnalysisConfiguration);
         dnaAnalysisConfiguration.setUseCghCall(false);
         subscription.getStudy().getStudyConfiguration().getGenomicDataSources().add(genomicSource);
         genomicSource.setDataType(PlatformDataTypeEnum.COPY_NUMBER);
         assertEquals(5, queryForm.getResultTypes().size());
         dnaAnalysisConfiguration.setUseCghCall(true);
         assertEquals(4, queryForm.getResultTypes().size());
         subscription.getStudy().getStudyConfiguration().getGenomicDataSources().clear();
         assertEquals(1, queryForm.getResultTypes().size());
     }
 
     @Test
     public void testHasGenomicDataSource() {
         queryForm.createQuery(subscription, null, null, null);
         assertTrue(queryForm.hasGenomicDataSources());
         queryForm.getQuery().getSubscription().getStudy().getStudyConfiguration().getGenomicDataSources().clear();
         assertFalse(queryForm.hasGenomicDataSources());
     }
 }
