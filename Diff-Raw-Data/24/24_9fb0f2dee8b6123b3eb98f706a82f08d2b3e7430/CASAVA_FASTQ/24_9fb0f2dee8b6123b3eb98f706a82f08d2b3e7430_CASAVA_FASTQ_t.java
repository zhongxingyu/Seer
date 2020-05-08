 import java.util.*;
 import java.io.*;
 
 public class CASAVA_FASTQ extends FASTQParse {
 
     String labels[] = {
 	"Instrument Name",
 	"Run ID",
 	"Flowcell ID",
 	"Lane number",
 	"Tile number",
 	"X-pos",
 	"Y-pos",
 	"Read Number",
 	"Is Filtered?",
 	"Control Number",
 	"Barcode sequence",
 	"Sequence",
 	"Quality"
     };
 
     public CASAVA_FASTQ(final String filename){
 	super(filename);
     }
 
     public String getColumnName(int col){
         if(col >= labels.length){
             return "Unknown";
         }
 	if(col < 0) return "Unknown";
         return labels[col];
     }
 
     public int getNumColumnLabels() {
         if(labels == null) return 0;
         return labels.length;
     }
 
     public String[] getNextRecord(){
         try {
             String ans[] = new String[13];
 	    for(int i = 0; i < ans.length; i++){
 		ans[i] = "";
 	    }
             String line;
 
             while((line=filein.readLine()) != null){
                 
                if(!line.substring(0,1).equals("@")) return null;
                 String header = line.substring(1);
 		String [] result = header.split(" ");
 		if(result.length > 0){
 		    String[] header1 = result[0].split(":");
 		    for(int i = 0; i < Math.min(header1.length, 7); i++){
 			ans[i] = header1[i];
 		    }
 		}
 		if(result.length > 1){
 		    String[] header2 = result[1].split(":");
 		    for(int i = 0; i < Math.min(header2.length,4); i++){
 			ans[i+7] = header2[i];
                     }
 		}
 
 		String seq = "";
                 String qual = "";
                 while((line = filein.readLine()) != null){
                     if(line.matches("[ACTGNacgtnURYSWKMBDHVN.-]*")){
                         seq += line;
                     }else{
                        if(!line.substring(0,1).equals("+")) return null;
                         
                         while((line = filein.readLine()) != null){
                             qual += line;
                             if(seq.length() <= qual.length()){
                                 ans[11] = seq;
                                 ans[12] = qual;
                                 return ans;
                             }
                         }
                     }
                 }
 
             }
             return null;
         } catch (Exception e){
             return null;
         }
     }
 
     public String getToolTip(final String value, int row, int col, final String[] other_values){
         if(col == 11 && other_values.length > 12){
             String qual = other_values[12];
             return prettyPrintBaseQual(value, qual);
         }
 
         return value;
     }
 
 }
