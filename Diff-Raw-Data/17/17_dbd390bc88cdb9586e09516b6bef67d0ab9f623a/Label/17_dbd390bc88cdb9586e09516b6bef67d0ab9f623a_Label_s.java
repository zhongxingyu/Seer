 import java.util.HashMap;
 
 /**
  * User: moulikrishna.
  * Institute: IIT Delhi.
  * purpose: As part of course requirements CSL211 Computer Architecture.
  * Submission date: 15th September, 2013
  */
 public class Label {
 
   private long startPos;
   private HashMap<String, Integer> labelMap;
 
   public Label(Program p) {
     startPos = p.getFilePointer();
    storeAllLabels(p);
     labelMap = new HashMap<String, Integer>();
   }
 
   private void storeAllLabels(Program p) {
     RISCInstruction rs = p.getNextCommand();
     while (rs != null) {
      if (rs.getLabel() != null) {
         int pos = (int) (p.getFilePointer() - startPos);
         labelMap.put(rs.getLabel(), pos);
       }
       rs = p.getNextCommand();
     }
   }
 
   public long findLabel(String label) {
    return labelMap.get(label);
   }
 }
