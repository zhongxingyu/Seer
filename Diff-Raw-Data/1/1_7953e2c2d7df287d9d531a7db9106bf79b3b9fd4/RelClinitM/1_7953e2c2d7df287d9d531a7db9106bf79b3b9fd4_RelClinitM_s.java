 package chord.rels;
 
 import chord.program.Method;
 import chord.project.Chord;
 import chord.project.ProgramRel;
 
 /**
  * Relation containing all class initializer methods, namely, methods
  * having signature <tt>&lt;clinit&gt;()</tt>.
  */
 @Chord(
 	name = "clinitM",
 	sign = "M0"
 )
 public class RelClinitM extends ProgramRel {
 	public void fill() {
 	           DomM domM = (DomM) doms[0];
 	                int numM = domM.size();
 	                for (int mIdx = 0; mIdx < numM; mIdx++) {
 	                        Method mVal = domM.get(mIdx);
 	                        if (mVal.isClinit())
 	                                add(mIdx);
 	                }
 	}
 }
