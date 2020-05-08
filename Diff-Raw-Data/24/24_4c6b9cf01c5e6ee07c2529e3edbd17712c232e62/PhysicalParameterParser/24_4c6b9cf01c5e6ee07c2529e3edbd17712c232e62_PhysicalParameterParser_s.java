 package de.hswt.hrm.report.latex;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 
 import de.hswt.hrm.inspection.model.Inspection;
 
 public class PhysicalParameterParser {
 
     private final String COMMENT = "%";
 
     /*
      * keys of physicalparameterrow.tex
      */
     private final String PROPERTY = ":physicalParameterProperty:";
     private final String VALUE = ":physicalParameterValue:";
     private final String PARAM_GRADE = ":physicalParameterGrade:";
     private final String PARAM_WHEIGHTING = ":physicalParameterWheighting:";
     private final String PARAM_RATING = ":physicalParameterRating:";
     private final String PARAM_COMMENT = ":physicalParameterComment:";
 
     /*
      * keys of physicalparametertable.tex
      */
     private final String ROWS = ":rows";
     private final String GRADE_SUM = ":physicalParameterGradeSum:";
     private final String WHEIGHTING_SUM = ":physicalParameterWheightingSum:";
     private final String RATING_AV = ":physicalParameterRatingAverage:";
 
     /*
      * path specifications
      */
     private final String FILE_DIR = "templates";
     private final String FILE_NAME_TABLE = "physicalparametertable.tex";
     private final String FILE_NAME_ROW = "physicalparameterrow.tex";
 
     private Inspection inspection;
     private String path;
 
     private String endTarget;
     private String rows;
 
     private float sumQuantifier;
     private float sumRatings;
     private float totalGrade;
 
     private StringBuffer buffer = new StringBuffer();
 
     public PhysicalParameterParser(String path, Inspection inspection) {
         this.inspection = inspection;
         this.path = path;
     }
 
     public String parse() throws IOException {
         if (!(inspection.getTemperature().isPresent() && inspection.getHumidity().isPresent())) {
             return "";
         }
         this.parseRow();
         this.parseTable();
         return this.endTarget;
     }
 
     private void parseTable() throws IOException {
         Path pathTable = FileSystems.getDefault().getPath(this.path, FILE_DIR, FILE_NAME_TABLE);
         buffer.setLength(0);
         BufferedReader reader = Files.newBufferedReader(pathTable, Charset.defaultCharset());
         String line = null;
         while ((line = reader.readLine()) != null) {
             line = line.trim();
             if (!line.startsWith(COMMENT)) {
                 buffer.append(line);
                 appendNewLine();
             }
         }
 
         String target = buffer.toString();
         this.totalGrade = this.sumRatings / this.sumQuantifier;
         target = target.replace(ROWS, this.rows);
         target = target.replace(GRADE_SUM, String.valueOf(sumRatings));
         target = target.replace(GRADE_SUM, String.valueOf(sumRatings));
         target = target.replace(WHEIGHTING_SUM, String.valueOf(sumQuantifier));
         target = target.replace(RATING_AV, String.valueOf(this.totalGrade));
 
         this.endTarget = target;
     }
 
     private void parseRow() throws IOException {
         Path pathRow = FileSystems.getDefault().getPath(this.path, FILE_DIR, FILE_NAME_ROW);
         buffer.setLength(0);
         BufferedReader reader = Files.newBufferedReader(pathRow, Charset.defaultCharset());
         String line = null;
         while ((line = reader.readLine()) != null) {
             line = line.trim();
             if (!line.startsWith(COMMENT)) {
                 buffer.append(line);
                 appendNewLine();
             }
         }
 
         String preTarget;
 
         StringBuffer target = new StringBuffer();
 
         this.sumRatings += inspection.getTemperatureRating().or(0);
         this.sumRatings += inspection.getHumidityRating().or(0);
         this.sumQuantifier += inspection.getTemperatureQuantifier().or(0);
         this.sumQuantifier += inspection.getHumidityQuantifier().or(0);
 
        // - un-String the calls below..
         preTarget = buffer.toString();
         preTarget = preTarget.replace(PROPERTY, "Temperatur");
         // TODO no Integer!! ==> FLOAT! && what if none?!?
        preTarget = preTarget.replace(VALUE,
                "String.valueOf(inspection.getTemperature().orFloat())");
         preTarget = preTarget.replace(PARAM_GRADE,
                 String.valueOf(inspection.getTemperatureRating().or(0)));
         preTarget = preTarget.replace(PARAM_WHEIGHTING,
                 String.valueOf(inspection.getTemperatureQuantifier().or(0)));
         preTarget = preTarget.replace(
                 PARAM_RATING,
                 String.valueOf(inspection.getTemperatureRating().or(0)
                         * inspection.getTemperatureQuantifier().or(0)));
         // TODO comment!?!
        preTarget = preTarget.replace(PARAM_COMMENT, "inspection.getTempComment");
         target.append(preTarget);
 
         preTarget = buffer.toString();
         preTarget = preTarget.replace(PROPERTY, "relative Luftfeuchtigkeit");
        // TODO no Integer!! ==> FLOAT! && what if none?!?
        preTarget = preTarget.replace(VALUE, "String.valueOf(inspection.getHumidity().orFloat())");
         preTarget = preTarget.replace(VALUE, String.valueOf(inspection.getHumidity().or(-1F)));
         preTarget = preTarget.replace(PARAM_GRADE, String.valueOf(inspection.getHumidityRating()));
         preTarget = preTarget.replace(PARAM_WHEIGHTING,
                 String.valueOf(inspection.getHumidityQuantifier()));
         preTarget = preTarget.replace(
                 PARAM_RATING,
                 String.valueOf(inspection.getHumidityRating().or(0)
                         * inspection.getHumidityQuantifier().or(0)));
        // TODO comment!?!
        preTarget = preTarget.replace(PARAM_COMMENT, "inspection.getHumidityComment");
         target.append(preTarget);
 
         this.rows = target.toString();
 
     }
 
     /*
      * returns the totalGrade, calculated from the components.
      */
     public float getTotalGrade() throws IOException {
         if (!(inspection.getTemperature().isPresent() && inspection.getHumidity().isPresent())) {
             return -1F;
         }
         this.parseRow();
         this.parseTable();
         return this.totalGrade;
 
     }
 
     private void appendNewLine() {
         buffer.append("\n");
     }
 
 }
