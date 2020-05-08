 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xsqconvertergit;
 
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import xsqconvertergit.interfaces.CSFastQEntryInterface;
 import xsqconvertergit.interfaces.FastaQualEntryInterface;
 
 /**
 * CSFasta entries
  * @author Wim Spee
  */
 public class CSFastaQualEntry implements FastaQualEntryInterface {
 
     
     private HashMap<Integer, Character> intToCharMap = null;   
     
     private String seqName = null;
     private String description = "+";
       
     
     private StringBuilder readCSSb = null;
     private List<String> readQvalues = null;   
     
     private Integer readStartPosition = null;
     private Integer readLengthCutoff = null;
     
     
     public CSFastaQualEntry()    
     {        
         intToCharMap = new HashMap<Integer,Character>();
         intToCharMap.put(0, '0');
         intToCharMap.put(1, '1');
         intToCharMap.put(2, '2');
         intToCharMap.put(3, '3');  
         
         readCSSb = new StringBuilder(50);
         readQvalues = new ArrayList<String>();
         
     }
     
     public void setSeqName(String seqName)
     {
         this.seqName = seqName;
     }  
 
     public String getSeq() {
       return readCSSb.toString();
     }
     
     public void setReadStartPosition(int readStartPosition)
     {
         this.readStartPosition = readStartPosition;
     }
     
      public void setReadLengthCutoff(int readLengthCutoff) {
         this.readLengthCutoff = readLengthCutoff;
      }
     
     
     
     
     public void addFastQEntryPosition(int csInt, int qInt )
     {
         char csValue = convertCSIntToCSChar(csInt);
        String qValue = convertQual(qInt); 
         
         if(qInt == 63)
         {
             csValue = '.';
         }
         
         readCSSb.append(csValue);
         readQvalues.add(qValue);
     }    
     
     
     private char convertCSIntToCSChar(int csInt)
     {
         return intToCharMap.get(csInt);
     }
     
    private String convertQual(int qInt)
     {
         
         if(qInt ==63){qInt=0;}
         if(qInt ==1){qInt=0;}
         if(qInt ==2){qInt=0;}
        if(qInt ==3){qInt=0;}                    
         
         String qualValue = Integer.toString(qInt);
         
         
         return qualValue;
     }
 
     @Override
     public String toString() {
         
         int readEndPosition = readCSSb.length();
         if(( readStartPosition + readLengthCutoff) < readEndPosition)
         {
             readEndPosition =  readStartPosition + readLengthCutoff;
         }        
         
         StringBuilder sb = new StringBuilder(105);
         sb.append(">");
         sb.append(seqName);
         sb.append("\n");
         sb.append(readCSSb.substring(readStartPosition, readEndPosition));
         sb.append("\n");
         
         return sb.toString();    
     }     
 
     public String getSeqName() {
         return seqName;
     }
 
 	@Override
 	public String toQualString() {
 		int readEndPosition = readCSSb.length();
         if(( readStartPosition + readLengthCutoff) < readEndPosition)
         {
             readEndPosition =  readStartPosition + readLengthCutoff;
         }        
         
         StringBuilder sb = new StringBuilder(105);
         sb.append(">");
         sb.append(seqName);
         sb.append("\n");
 
         readQvalues = readQvalues.subList(readStartPosition, readEndPosition);
 
         sb.append(StringUtils.join(readQvalues, " "));
         sb.append("\n");
 		return sb.toString();
 	}
     
     
     
     
     
 }
