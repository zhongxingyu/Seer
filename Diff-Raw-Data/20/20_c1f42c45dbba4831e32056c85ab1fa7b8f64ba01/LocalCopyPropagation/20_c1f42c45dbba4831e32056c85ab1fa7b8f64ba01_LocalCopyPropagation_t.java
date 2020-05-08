 import java.util.*;
 
 public class LocalCopyPropagation {
 
    public void optimize(Node block) {
       Map<Register, Register> validCopies = new HashMap<Register, Register>();
       Instruction instr, temp;
       Register src, dest, betterSrc;
       ArrayList<Instruction> instructions = block.getInstr();
 
       for (int ndx = 0; ndx < instructions.size(); ndx++) {
          instr = instructions.get(ndx);
 
          dest = null;
          src = null;
 
          if (!instr.getSources().isEmpty()) {
             for (Register oldSource : instr.getSources()) {
                betterSrc = validCopies.get(oldSource);
 
                if (betterSrc != null) {
                  //System.out.println("found better source in " + instr);
                  //System.out.println("use " + betterSrc + " instead of " + oldSource);
                   instr.transformSources(oldSource, betterSrc);
                }
             }
          }
 
          if (!instr.getDestinations().isEmpty()) {
             dest = instr.getDestinations().get(0);
 
            // Don't need to kill on useless moves. (mov %r1 %r1)
             if (!(dest.equals(src) && instr instanceof MovInstruction)) {

               Set<Register> notValidAnymore = new HashSet<Register>();

               for (Register old : validCopies.keySet()) {
                  betterSrc = validCopies.get(old);
                  if (betterSrc.equals(dest)) {
                  }
               }

               // Remove mappings now just to not modify set while iterating.
               for (Register old : notValidAnymore) {
                  validCopies.remove(old);
               }
             }
          }
 
          if (instr instanceof MovInstruction && !(dest.equals(src))) {
             src = instr.getSources().get(0);
             validCopies.put(dest, src);
          }
       }
    }
 }
