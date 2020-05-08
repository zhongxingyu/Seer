 package de.hswt.hrm.report.latex;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Collection;
 
 import de.hswt.hrm.inspection.model.PhysicalRating;
 
 public class PhysicalInspectionParser {
 
     private final String FILE_NAME_ROW = "physicalinspectionrow.tex";
     private final String FILE_DIR = "templates";
     private final String FILE_NAME_TABLE = "physicalinspectiontable.tex";
 
     private final String INSPECTOIN_SAMPLE_POINT = ":physicalInspectionSamplingPoint:";
     private final String GRADE = ":physicalInspectionGrade:";
     private final String WHEIGHTING = ":physicalInspectionWheighting:";
     private final String RATING = ":physicalInspectionRating";
     private final String PHYS_COMMENT = ":physicalInspectionComment:";
     private final String ROWS = ":rows:";
     private final String GRADE_SUM = ":physicalInspectionGradeSum:";
     private final String WHEIGHTED_SUM = ":physicalInspectionWheightingSum:";
     private final String RATING_AV = ":physicalInspectionRatingAverage:";
 
     private final String COMMENT = "%";
 
     private String preTarget;
     private String target;
     private StringBuffer targetRow = new StringBuffer();
 
     private float sumRatings;
     private float sumQuantifier;
     private float totalGrade;
 
     private String path;
 
     private Collection<PhysicalRating> ratings;
 
     private StringBuffer buffer = new StringBuffer();
 
     public PhysicalInspectionParser(String path, Collection<PhysicalRating> ratings) {
         this.ratings = ratings;
         this.path = path;
     }
 
     public String parse() throws IOException {
         this.parseRow();
         this.parseTable();
 
         return this.target;
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
 
         target = null;
         target = buffer.toString();
 
         this.totalGrade = Math.round(10F * this.sumRatings / this.sumQuantifier) / 10F;
         target.replace(ROWS, this.targetRow.toString());
         target.replace(GRADE_SUM, String.valueOf(this.sumRatings));
         target.replace(WHEIGHTED_SUM, String.valueOf(this.sumQuantifier));
         target.replace(RATING_AV, String.valueOf(this.totalGrade));
 
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
 
         preTarget = null;
         targetRow = null;
         for (PhysicalRating rating : this.ratings) {
             this.sumRatings += rating.getRating();
             this.sumQuantifier += rating.getQuantifier();
             preTarget = buffer.toString();
             preTarget = preTarget.replace(INSPECTOIN_SAMPLE_POINT, 
             		rating.getComponent().getComponent().getName());
             preTarget = preTarget.replace(GRADE, String.valueOf(rating.getRating()));
             preTarget = preTarget.replace(WHEIGHTING, String.valueOf(rating.getQuantifier()));
             preTarget = preTarget.replace(
                     RATING,
                     String.valueOf(Math.round(10F * Float.valueOf(rating.getRating())
                             * Float.valueOf(rating.getQuantifier())) / 10F));
            preTarget = preTarget.replace(PHYS_COMMENT, rating.getNote().or("-"));
             targetRow.append(preTarget);
 
         }
 
     }
 
     /*
      * returns the totalGrade, calculated from the components.
      */
     public float getTotalGrade(String path) throws IOException {
         this.path = path;
         this.parseRow();
         this.parseTable();
         return this.totalGrade;
 
     }
 
     private void appendNewLine() {
         buffer.append("\n");
     }
 
 }
