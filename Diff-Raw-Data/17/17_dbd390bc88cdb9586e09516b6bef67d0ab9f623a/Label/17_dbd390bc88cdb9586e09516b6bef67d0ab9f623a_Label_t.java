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
     labelMap = new HashMap<String, Integer>();
    storeAllLabels(p);
   }
 
   private void storeAllLabels(Program p) {
     RISCInstruction rs = p.getNextCommand();
     while (rs != null) {
      if (rs.isHasLabel()) {
         int pos = (int) (p.getFilePointer() - startPos);
         labelMap.put(rs.getLabel(), pos);
        System.out.println(rs.getLabel() + "   added with  " + pos);
       }
       rs = p.getNextCommand();
     }
   }
 
   public long findLabel(String label) {
    try {
      return labelMap.get(label);
    } catch (Exception e) {
      Debug.forceQuit("The label " + label + " is not defined");
      e.printStackTrace();
    }
    return 0;
   }
 }
