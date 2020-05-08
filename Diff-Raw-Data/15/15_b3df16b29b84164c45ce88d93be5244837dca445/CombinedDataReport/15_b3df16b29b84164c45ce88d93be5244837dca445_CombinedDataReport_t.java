 package org.pharmgkb;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 import com.sun.javafx.beans.annotations.NonNull;
 import org.apache.commons.io.IOUtils;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.xssf.streaming.SXSSFWorkbook;
 import org.hibernate.Session;
 import org.pharmgkb.enums.Property;
 import org.pharmgkb.exception.PgkbException;
 import org.pharmgkb.util.ExcelUtils;
 import org.pharmgkb.util.HibernateUtils;
 import org.pharmgkb.util.IcpcUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This report generator will dump all subjects and their properties to a single file.
  *
  * @author Ryan Whaley
  */
 public class CombinedDataReport {
   private static final Logger sf_logger = LoggerFactory.getLogger(CombinedDataReport.class);
   private File m_outputFile = null;
 
   /**
    * Constructor
    * @param file file to write the report to, required
    */
   public CombinedDataReport(@NonNull File file) {
     Preconditions.checkNotNull(file);
     setOutputFile(file);
   }
 
   /**
    * Generate the report and save it to the file specified at construction.
    *
    * @throws PgkbException can occur if output file is not specified or if I/O operations fail
    */
   public void generate() throws PgkbException {
     Preconditions.checkNotNull(getOutputFile());
     Session session = null;
     FileOutputStream fileOutputStream = null;
     int currentRowIdx = 0;
 
     try {
       session = HibernateUtils.getSession();
 
       SXSSFWorkbook workbook = new SXSSFWorkbook(5);
       Sheet sheet = workbook.createSheet("ICPC Data");
       Row descripRow = sheet.createRow(currentRowIdx++);
       Row nameRow = sheet.createRow(currentRowIdx++);
 
       Map<String,Integer> propertyIndexMap = Maps.newHashMap();
       Map<String,String> propertyTypeMap = Maps.newHashMap();
       List rez = session.createQuery("from IcpcProperty ip order by ip.index")
           .list();
       int columnIdx = 0;
 
       ExcelUtils.writeCell(descripRow, columnIdx, Property.SUBJECT_ID.getDisplayName());
       ExcelUtils.writeCell(nameRow, columnIdx, Property.SUBJECT_ID.getShortName());
       columnIdx++;
       ExcelUtils.writeCell(descripRow, columnIdx, Property.RACE_OMB.getDisplayName());
       ExcelUtils.writeCell(nameRow, columnIdx, Property.RACE_OMB.getShortName());
       columnIdx++;
 
       for (Object result :rez) {
         IcpcProperty property = (IcpcProperty)result;
         Property propertyAttributes = Property.lookupByName(property.getName());
 
         if (propertyAttributes==null || propertyAttributes.isShownInReport()) {
           propertyIndexMap.put(property.getName(), columnIdx);
           propertyTypeMap.put(property.getName(), property.getType());
 
           if (sf_logger.isDebugEnabled()) {
             sf_logger.debug(columnIdx+": "+property.getDescription());
           }
 
           ExcelUtils.writeCell(descripRow, columnIdx, property.getDescription());
           ExcelUtils.writeCell(nameRow, columnIdx, property.getName());
           columnIdx++;
         }
       }
 
       rez = session.createQuery("select s.subjectId from Subject s order by s.project,s.subjectId").list();
       for (Object result : rez) {
         Subject subject = (Subject)session.get(Subject.class, (String)result);
         Row row = sheet.createRow(currentRowIdx++);
 
         ExcelUtils.writeCell(row, 0, subject.getSubjectId());
         ExcelUtils.writeCell(row, 1, subject.calculateRace());
 
         for (String propertyName : propertyIndexMap.keySet()) {
           Integer valueColIdx= propertyIndexMap.get(propertyName);
           String propType = propertyTypeMap.get(propertyName);
           String propValue = subject.getProperties().get(propertyName);
 
           if (IcpcUtils.isBlank(propValue)) {
             ExcelUtils.writeCell(row, valueColIdx, IcpcUtils.NA);
           }
           else if (propType.equals("number")) {
             try {
              double numValue = Double.valueOf(propValue);
               ExcelUtils.writeCell(row, valueColIdx, numValue, null);
             } catch (NumberFormatException ex) {
               sf_logger.debug("Input string is not number: {}", propValue);
               ExcelUtils.writeCell(row, valueColIdx, IcpcUtils.NA);
             }
           }
           else {
             ExcelUtils.writeCell(row, valueColIdx, propValue);
           }
         }
       }
 
       sf_logger.info("writing to file " + getOutputFile());
       fileOutputStream = new FileOutputStream(getOutputFile());
       workbook.write(fileOutputStream);
     }
     catch (IOException ex) {
       throw new PgkbException("Error writing report to disk", ex);
     }
     finally {
       IOUtils.closeQuietly(fileOutputStream);
       HibernateUtils.close(session);
     }
   }
 
   /**
    * Gets the file to output the report to
    * @return the file to output the report to
    */
   public File getOutputFile() {
     return m_outputFile;
   }
 
   /**
    * Sets the file to output the report to
    * @param outputFile the file to output the report to
    */
   public void setOutputFile(File outputFile) {
     m_outputFile = outputFile;
   }
 }
