 package chord.rels;
 
 import chord.project.Chord;
 import chord.project.ProgramRel;
 
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
        throw new RuntimeException("cs265: implement this method");
	}
 }
