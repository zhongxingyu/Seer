 package chord.rels;
 
 import chord.program.CFG;
 import chord.program.Method;
 import chord.program.insts.Inst;
 import chord.program.insts.InvkInst;
 import chord.project.Chord;
 import chord.project.ProgramRel;
 
 import chord.program.insts.InvkKind;
 import chord.doms.DomI;
 
 /**
  * Relation containing each tuple (i,m) such that m is the resolved
  * method of method invocation statement i of kind <tt>INVK_SPECIAL</tt>.
  */
 @Chord(
 	name = "specIM",
 	sign = "I0,M0:I0xM0"
 )
 public class RelSpecIM extends ProgramRel {
     public void fill() {
         DomI domI = (DomI) doms[0];
         for (InvkInst inst : domI) {
            if (inst.getKind() == InvkKind.INVK_SPECIAL) {
                 add(inst, inst.getRslvMethod());
             }
         }
     }
 }
