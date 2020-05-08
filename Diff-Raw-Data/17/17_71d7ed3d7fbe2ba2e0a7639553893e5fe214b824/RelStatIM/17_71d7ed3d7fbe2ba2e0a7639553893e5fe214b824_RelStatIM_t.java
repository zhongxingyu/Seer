 package chord.rels;
 
import chord.program.insts.InvkInst;
import chord.program.insts.InvkKind;
 import chord.project.Chord;
 import chord.project.ProgramRel;
 
import chord.doms.DomI;

 /**
  * Relation containing each tuple (i,m) such that m is the resolved
  * method of method invocation statement i of kind
  * <tt>INVK_STATIC</tt>.
  */
 @Chord(
 	name = "statIM",
 	sign = "I0,M0:I0xM0"
 )
 public class RelStatIM extends ProgramRel {
    public void fill() {
        DomI domI = (DomI) doms[0];
        for (InvkInst inst : domI) {
            if (inst.getInvkKind() == InvkKind.INVK_STATIC) {
                add(inst, inst.getRslvMethod());
            }
        }
    }
 }
