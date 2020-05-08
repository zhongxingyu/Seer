 package org.pharmgkb;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.sun.javafx.beans.annotations.NonNull;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.poi.ss.usermodel.*;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.pharmgkb.enums.*;
 import org.pharmgkb.exception.PgkbException;
 import org.pharmgkb.util.ExcelUtils;
 import org.pharmgkb.util.ExtendedEnum;
 import org.pharmgkb.util.IcpcUtils;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by IntelliJ IDEA.
  * User: whaleyr
  * Date: 8/28/12
  */
 public class SubjectIterator implements Iterator {
   private static final Logger sf_logger = Logger.getLogger(SubjectIterator.class);
   private static final Integer sf_columnNameRowIdx = 1;
   private static final Pattern sf_dosingPattern = Pattern.compile("(\\d+).*mg");
   private static final Pattern sf_geneticBases = Pattern.compile("^[AaTtGgCc/]+$");
 
   private Sheet m_sheet = null;
   private Integer m_currentRow = 3;
   private FormulaEvaluator m_formulaEvaluator = null;
   private Map<Integer,String> m_columnIdxToNameMap = Maps.newHashMap();
   private Integer m_columnCount = 0;
 
   public SubjectIterator(Sheet sheet) throws Exception {
     if (sheet == null) {
       throw new Exception("No sheet specified");
     }
     setSheet(sheet);
 
     Workbook wb = sheet.getWorkbook();
     setFormulaEvaluator(wb.getCreationHelper().createFormulaEvaluator());
 
     Row headerRow = getSheet().getRow(sf_columnNameRowIdx);
     setColumnCount((int)headerRow.getLastCellNum());
 
     if (sf_logger.isDebugEnabled()) {
       sf_logger.debug("Sheet has "+sheet.getLastRowNum()+" rows");
     }
   }
 
   public void parseHeading(@NonNull Session session) throws Exception {
     Preconditions.checkNotNull(session);
     Map<String,String> unmappedColumnMap = Maps.newTreeMap();
 
     Query query = session.createQuery("from IcpcProperty ip where trim(ip.description)=:descrip");
 
     Row headerRow = getSheet().getRow(sf_columnNameRowIdx);
 
     if (!headerRow.getCell(0).getStringCellValue().equals("PharmGKB Subject ID")) {
       throw new Exception("Can't find proper header row at index "+sf_columnNameRowIdx);
     }
 
     int cellCrawlCount = 0;
     for (Cell cell : headerRow) {
       String cellContent = StringUtils.normalizeSpace(cell.getStringCellValue());
       if (!IcpcUtils.isBlank(cellContent)) {
         Object result = query.setParameter("descrip", cellContent).uniqueResult();
 
         if (result!=null) {
           IcpcProperty property = (IcpcProperty)result;
           getColumnIdxToNameMap().put(cell.getColumnIndex(), property.getName());
         }
         else {
           unmappedColumnMap.put(ExcelUtils.getAddress(cell), cellContent);
         }
       }
       cellCrawlCount++;
     }
 
     if (unmappedColumnMap.size()>0) {
       throw new PgkbException("No column definitions found for:\n"+unmappedColumnMap);
     }
 
     sf_logger.debug("Finished parsing header, coloumns read: "+cellCrawlCount+", columns matched: "+getColumnIdxToNameMap().size());
   }
 
   @Override
   public boolean hasNext() {
     boolean notPastLast = getCurrentRow()<=getSheet().getLastRowNum();
 
     boolean hasSubjectId = getSheet()!=null
         && getSheet().getRow(getCurrentRow())!=null
         && getSheet().getRow(getCurrentRow()).getCell(0)!=null
         && StringUtils.isNotBlank(getSheet().getRow(getCurrentRow()).getCell(0).getStringCellValue());
 
     return notPastLast && hasSubjectId;
   }
 
   @Override
   public Subject next() {
     Subject subject = new Subject();
 
     Row row = getSheet().getRow(getCurrentRow());
 
     if (sf_logger.isDebugEnabled()) {
       sf_logger.debug("row " + getCurrentRow() + " length: " + row.getLastCellNum());
     }
 
     try {
       Map<String,String> keyValueMap = parseKeyValues();
       subject.addProperties(keyValueMap);
     }
     catch (Exception ex) {
       sf_logger.error("Couldn't copy subject data for row "+(getCurrentRow()+1), ex);
       subject = null;
     }
 
     bumpCurrentRow();
     return subject;
   }
 
   protected Map<String,String> parseKeyValues() {
     Map<String,String> keyValues = Maps.newHashMap();
     Map<Integer,String> colIdxToKey = getColumnIdxToNameMap();
 
     Row row = getSheet().getRow(getCurrentRow());
 
     for (int colIdx=0; colIdx<getColumnCount(); colIdx++) {
       if (colIdxToKey.containsKey(colIdx)) {
         String key = colIdxToKey.get(colIdx);
         String value = ExcelUtils.getStringValue(row.getCell(colIdx), getFormulaEvaluator());
 
         String normalizedValue = null;
         try {
           normalizedValue = normalizeValue(key, value);
         } catch (PgkbException ex) {
           sf_logger.warn("Bad value at "+ExcelUtils.getAddress(row.getCell(colIdx))+": "+ex.getMessage());
         }
         keyValues.put(key, normalizedValue);
       }
     }
 
     return keyValues;
   }
 
   /**
    * Takes a key and value and normalizes the value depending on what key it is for. It also validates that the data is
    * in the right format and will throw a PgkbException if it's malformed.
    *
    * @param key the key of the property to normalize
    * @param value the value of the property normalize
    * @return a normalized String version of the value
    * @throws PgkbException can occur if the value is malformed for the given key
    */
   protected String normalizeValue(String key, String value) throws PgkbException {
     boolean valid = true;
     String normalizedValue;
     ExtendedEnum enumValue;
 
     if (IcpcUtils.isBlank(value)) {
       return IcpcUtils.NA;
     }
 
     String strippedValue = StringUtils.stripToNull(value);
     normalizedValue = strippedValue;
 
     switch (key) {
       // subject ID column is special
       case "Subject_ID":
         valid = (strippedValue.startsWith("PA") && strippedValue.length()>2);
         break;
 
       // columns that must be integers
       case "Project":
         Integer.valueOf(strippedValue);
         break;
 
       // enum columns
       case "Alcohol":
         enumValue = AlcoholStatus.lookupByName(strippedValue);
         if (enumValue != null) {
           if (enumValue==AlcoholStatus.UNKNOWN) {
             normalizedValue = IcpcUtils.NA;
           }
           else {
             normalizedValue = enumValue.getShortName();
           }
         }
         else {
           valid = false;
         }
         break;
       case "Diabetes":
         enumValue = DiabetesStatus.lookupByName(strippedValue);
         if (enumValue != null) {
           if (enumValue == DiabetesStatus.UNKNOWN) {
             normalizedValue = IcpcUtils.NA;
           }
           else {
             normalizedValue = enumValue.getShortName();
           }
         }
         else {
           valid = false;
         }
         break;
       case "Gender":
         enumValue = Gender.lookupByName(strippedValue);
         if (enumValue != null) {
           normalizedValue = enumValue.getShortName();
         }
         else {
           valid = false;
         }
         break;
       case "Sample_Source":
         List<String> normalizedTokens = Lists.newArrayList();
         for (String token : Splitter.on(";").split(strippedValue)) {
           SampleSource source = SampleSource.lookupByName(StringUtils.strip(token));
           if (source!=null) {
             normalizedTokens.add(source.getShortName());
           }
           else {
             valid = valid && (source!=null);
           }
           normalizedValue = Joiner.on(";").join(normalizedTokens);
         }
         break;
       case "PPI_Name":
         enumValue = DrugPpi.lookupByName(strippedValue);
         if (enumValue != null) {
           normalizedValue = enumValue.getShortName();
         }
         else {
           valid = false;
         }
         break;
 
       // columns that must be floats
       case "Age":
       case "Height":
       case "Weight":
       case "BMI":
       case "Diastolic_BP_Max":
       case "Diastolic_BP_Median":
       case "Systolic_BP_Max":
       case "Systolic_BP_Median":
       case "CRP":
       case "BUN":
       case "Left_Ventricle":
       case "Right_Ventricle":
       case "Dose_Clopidogrel_aspirin":
       case "Duration_Clopidogrel":
       case "Duration_Aspirin":
       case "Duration_therapy":
       case "Active_metabolite":
       case "Days_MajorBleeding":
       case "Days_MinorBleeding":
       case "Num_bleeding":
       case "PFA_mean_EPI_Collagen_closure_Baseline":
       case "PFA_mean_ADP_Collagen_closure_Baseline":
       case "PFA_mean_EPI_Collagen_closure_Post":
       case "PFA_mean_ADP_Collagen_closure_Post":
       case "PFA_mean_EPI_Collagen_closure_Standard":
       case "PFA_mean_ADP_Collagen_closure_Standard":
       case "Verify_Now_baseline_Base":
       case "Verify_Now_baseline_PRU":
       case "Verify_Now_baseline_percentinhibition":
       case "Verify_Now_post_Base":
       case "Verify_Now_post_PRU":
       case "Verify_Now_post_percentinhibition":
       case "Verify_Now_on_clopidogrel_Base":
       case "Verify_Now_on_clopidogrel_PRU":
       case "Verify_Now_on_clopidogrel_percentinhibition":
       case "PAP_8_baseline_max_ADP_2 ":
       case "PAP_8_baseline_max_ADP_5":
       case "PAP_8_baseline_max_ADP_10":
       case "PAP_8_baseline_max_ADP_20":
       case "PAP_8_baseline_max_collagen_1":
       case "PAP_8_baseline_max_collagen_2":
       case "PAP_8_baseline_max_collagen_10":
       case "PAP_8_baseline_max_collagen_6":
       case "PAP_8_baseline_max_epi":
       case "PAP_8_baseline_max_aa":
       case "PAP_8_baseline_lag_collagen_1":
       case "PAP_8_baseline_lag_collagen_2":
       case "PAP_8_baseline_lag_collagen_5":
       case "PAP_8_baseline_lag_collagen_10":
       case "PAP_8_post_max_ADP_2 ":
       case "PAP_8_post_max_ADP_5":
       case "PAP_8_post_max_ADP_10":
       case "PAP_8_post_max_ADP_20":
       case "PAP_8_post_max_collagen_1":
       case "PAP_8_post_max_collagen_2":
       case "PAP_8_post_max_collagen_5":
       case "PAP_8_post_max_collagen_10":
       case "PAP_8_post_max_epi_perc":
       case "PAP_8_post_max_aa_perc":
       case "PAP_8_post_lag_collagen_1":
       case "PAP_8_post_lag_collagen_2":
       case "PAP_8_post_lag_collagen_5":
       case "PAP_8_post_lag_collagen_10":
       case "PAP_8_standard_max_ADP_2":
       case "PAP_8_standard_max_ADP_5":
       case "PAP_8_standard_max_ADP_10":
       case "PAP_8_standard_max_ADP_20":
       case "PAP_8_standard_max_collagen_1":
       case "PAP_8_standard_max_collagen_2":
       case "PAP_8_standard_max_collagen_5":
       case "PAP_8_standard_max_collagen_10":
       case "PAP_8_standard_max_epi_pct":
       case "PAP_8_standard_max_aa_pct":
       case "PAP_8_standard_lag_collagen_1":
       case "PAP_8_standard_lag_collagen_2":
       case "5PAP_8_standard_lag_collagen_5":
       case "PAP_8_standard_lag_collagen_10":
       case "Chronolog_baseline_max_ADP_5":
       case "Chronolog_baseline_max_ADP_20":
       case "Chronolog_baseline_max_aa":
       case "Chronolog_baseline_max_collagen1":
       case "Chronolog_baseline_lag_ADP_5":
       case "Chronolog_baseline_lag_ADP_20":
       case "Chronolog_baseline_lag_aa":
       case "Chronolog_baseline_lag_collagen1":
       case "Chronolog_loading_max_ADP_5":
       case "Chronolog_loading_max_ADP_20":
       case "Chronolog_loading_max_aa":
       case "Chronolog_loading_max_collagen1":
       case "Chronolog_loading_lag_ADP_5":
       case "Chronolog_loading_lag_ADP_20":
       case "Chronolog_loading_lag_aa":
       case "Chronolog_loading_lag_collagen1":
       case "Chronolog_standard_max_ADP_5":
       case "Chronolog_standard_max_ADP_20":
       case "Chronolog_standard_max_aa":
       case "Chronolog_standard_max_collagen1":
       case "Chronolog_standard_lag_ADP_5":
       case "Chronolog_standard_lag_ADP_20":
       case "Chronolog_standard_lag_aa":
       case "Chronolog_standard_lag_collagen1":
       case "VASP":
       case "Duration_followup_clinical_outcomes":
       case "Time_STEMI":
       case "Time_NSTEMI":
       case "Time_Angina":
       case "Time_REVASC":
       case "Time_stroke":
       case "Time_heartFailure":
       case "Time_MechValve":
       case "Time_tissValve":
       case "Time_stent":
       case "Time_mortality":
       case "Time_death":
       case "Time_venHypertrophy":
       case "Time_PeriVascular":
       case "Time_AF":
       case "Time_Loading_PFA":
       case "Time_loading_VerifyNow":
       case "Time_loading_PAP8":
       case "Clopidogrel_loading_dose":
       case "White_cell_count":
       case "Red_cell_count":
       case "Platelet_count":
       case "Abs_white_on_plavix":
       case "Red_on_plavix":
       case "Platelet_on_plavix":
       case "MeanPlateletVol_on_plavix":
       case "Hematocrit_on_plavix":
       case "Mean_platelet_volume":
       case "Hematocrit ":
       case "LDL":
       case "HDL":
       case "Total_Cholesterol":
       case "Triglycerides":
       case "Intra_assay_variation":
       case "Optical_Platelet_Aggregometry":
       case "Time_MACE":
       case "hemoglobin":
       case "plasma_urea":
         try {
           Float.valueOf(strippedValue);
         }
         catch (Exception ex) {
           Matcher m = sf_dosingPattern.matcher(strippedValue);
           valid = m.find();
 
           if (valid) {
             normalizedValue = m.group(1);
           }
         }
         break;
 
       // columns with no/yes as 0/1
       case "Genotyping":
       case "Phenotyping":
       case "Ejection_fraction":
       case "Clopidogrel":
       case "Aspirn":
       case "Clopidogrel_alone":
       case "Verify_Now_base":
       case "Verify_Now_post_loading":
       case "Verify_Now_while_on_clopidogrel":
       case "Pre_clopidogrel_platelet_aggregometry_base":
       case "Post_clopidogrel_platelet_aggregometry":
         enumValue = Value.lookupByName(strippedValue);
         if (enumValue != null) {
           normalizedValue = enumValue.getShortName();
         }
         else {
           valid = false;
         }
         break;
 
       // columns with no/yes/unknown as 0/1/99
       case "Ever_Smoked":
       case "Current_smoker":
       case "Blood_Pressure":
       case "placebo_RCT":
       case "Aspirin_Less_100":
       case "Statins":
       case "PPI ":
       case "Calcium_blockers":
       case "Beta_blockers":
       case "ACE_Inh":
       case "Ang_inh_blockers":
       case "Ezetemib":
       case "Glycoprotein_IIaIIIb_inhibitor":
       case "CV_events":
       case "Bleeding":
       case "Major_Bleeding":
       case "Minor_Bleeding":
       case "STEMI":
       case "NSTEMI":
       case "Other_ischemic":
       case "Stroke":
       case "All_cause_mortality":
       case "Cardiovascular_death":
       case "Angina":
       case "Left_ventricular_hypertrophy":
       case "Peripheral_vascular_disease":
       case "Atrial_fibrillation":
       case "REVASC":
       case "Congestive_Heart_Failure":
       case "Tissue_Valve_Replacement":
       case "Blood_Cell":
       case "Chol":
         valid = (strippedValue.equals("0") || strippedValue.equals("1") || strippedValue.equals("99"));
         if (strippedValue.equals("99")) {
           normalizedValue = IcpcUtils.NA;
         }
         break;
 
       // columns with left/right/no/unknown as 0/1/2/99
       case "Stent_thromb":
       case "Mechanical_Valve_Replacement":
         valid = (strippedValue.equals("0") || strippedValue.equals("1") || strippedValue.equals("2") || strippedValue.equals("99"));
         if (strippedValue.equals("99")) {
           normalizedValue = IcpcUtils.NA;
         }
         break;
 
       // columns that are supposed to be genetic bases (eg. A/T or GC)
       case "rs4244285":
       case "rs4986893":
       case "rs28399504":
       case "rs56337013":
       case "rs72552267":
       case "rs72558186":
       case "rs41291556":
       case "rs6413438":
       case "rs12248560":
       case "rs662":
       case "rs854560":
       case "rs1045642":
       case "rs4803418":
       case "rs48034189":
       case "rs8192719":
       case "rs3745274":
       case "rs2279343":
       case "rs3745274_cyp2b6_9":
       case "rs2242480":
       case "rs3213619":
       case "rs2032582":
       case "rs1057910":
       case "rs71647871":
         Matcher m = sf_geneticBases.matcher(strippedValue);
         if (m.matches()) {
           char[] valueArray = StringUtils.remove(strippedValue.toUpperCase(), '/').toCharArray();
           Arrays.sort(valueArray);
 
           StringBuilder sb = new StringBuilder();
           for (char base : valueArray) {
             if (sb.length()!=0) {
               sb.append("/");
             }
             sb.append(base);
           }
           normalizedValue = sb.toString();
         }
         else {
           valid = false;
         }
         break;
 
       // columns that are stored as strings and can skip validation
       case "Creatinine":
       case "Inter_assay_variation":
       case "ADP":
       case "Arachadonic_acid":
       case "Collagen":
      case "Time_loading_Chronolog":
         break;
 
         // no validation
       default:
         if (sf_logger.isDebugEnabled()) {
           sf_logger.debug("no validation for "+key);
         }
     }
 
     if (!valid) {
       throw new PgkbException(key+" value is not valid: "+strippedValue);
     }
 
     return normalizedValue;
   }
 
   @Override
   public void remove() {
     throw new UnsupportedOperationException("remove() not implemented for SubjectIterator");
   }
 
   public Sheet getSheet() {
     return m_sheet;
   }
 
   public void setSheet(Sheet sheet) {
     m_sheet = sheet;
   }
 
   protected Integer getCurrentRow() {
     return m_currentRow;
   }
 
   protected void bumpCurrentRow() {
     m_currentRow++;
   }
 
   public FormulaEvaluator getFormulaEvaluator() {
     return m_formulaEvaluator;
   }
 
   public void setFormulaEvaluator(FormulaEvaluator formulaEvaluator) {
     m_formulaEvaluator = formulaEvaluator;
   }
 
   public Map<Integer, String> getColumnIdxToNameMap() {
     return m_columnIdxToNameMap;
   }
 
   public Integer getColumnCount() {
     return m_columnCount;
   }
 
   public void setColumnCount(Integer columnCount) {
     m_columnCount = columnCount;
   }
 }
