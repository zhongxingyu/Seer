 package au.org.intersect.samifier.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class GffOutputter implements Outputter {
    public String genomeFileName;
    public Integer start;
    public Integer end;
     private String glimmerScore;
     private String directionFlag;
     private String frame;
     private String glimmerName;
     private String virtualProteinName;
 
     public GffOutputter(ProteinLocation location,
             String genomeFileNameWithExtension) {
         this.genomeFileName = genomeFileNameNoExtension(genomeFileNameWithExtension);
         this.start = location.getStartIndex();
         this.end = location.getStop();
         if (location.getConfidenceScore() != null) {
             this.glimmerScore = location.getConfidenceScore().toString();
         } else {
             this.glimmerScore = "0";
         }
         this.directionFlag = location.getDirection();
         this.frame = location.getFrame();
         this.glimmerName = location.getName();
         this.virtualProteinName = location.getVirtualProteinName();
     }
 
     @Override
     public String getOutput() {
         StringBuilder output = new StringBuilder();
 
         output.append(getOutputLine("gene", false));
         output.append(System.getProperty("line.separator"));
 
         output.append(getOutputLine("CDS", true));
         output.append(System.getProperty("line.separator"));
 
         return output.toString();
 
     }
 
     private String getOutputLine(String type, boolean addParent) {
         StringBuilder output = new StringBuilder();
         output.append(genomeFileName);
         column(output, "Glimmer");
         column(output, type);
         column(output, Integer.toString(start));
         column(output, Integer.toString(end));
         column(output, glimmerScore);
         column(output, directionFlag);
        column(output, frame);
         ArrayList<String> attributes = new ArrayList<String>();
         attributes.add("Name=" + glimmerName);
         if (addParent) {
             attributes.add("Parent=" + glimmerName);
         } else {
             attributes.add("ID=" + glimmerName);
         }
         if (virtualProteinName != null && virtualProteinName.length() > 0) {
             attributes.add("Virtual_protein=" + virtualProteinName);
         }
         column(output, attributes);
         return output.toString();
     }
 
     private void column(StringBuilder buff, String field) {
         buff.append('\t');
         buff.append(field);
     }
 
     private void column(StringBuilder buff, List<String> attributes) {
         buff.append('\t');
         for (String attribute : attributes) {
             buff.append(attribute + ";");
         }
     }
 
     private String genomeFileNameNoExtension(String genomeFileName) {
         int indexOfDot = genomeFileName.lastIndexOf('.');
         if (indexOfDot > 0) {
             return genomeFileName.substring(0, indexOfDot);
         }
         return genomeFileName;
     }
 }
