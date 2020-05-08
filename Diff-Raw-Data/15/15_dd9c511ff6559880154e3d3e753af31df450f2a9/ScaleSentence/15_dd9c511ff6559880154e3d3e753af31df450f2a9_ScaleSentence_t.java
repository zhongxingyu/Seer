 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.examples.language.graphdraw3d.sentence;
 
 import static org.lwjgl.opengl.GL11.*;
 import org.modelcc.*;
 import org.modelcc.examples.language.graphdraw3d.Literal;
 import org.modelcc.examples.language.graphdraw3d.RealLiteral;
 import org.modelcc.examples.language.graphdraw3d.resources.RunData;
 import org.modelcc.examples.language.graphdraw3d.Sentence;
 
 /**
  * Scale Sentence
  * @author elezeta
  */
 @Prefix("scale")
 @FreeOrder
 public final class ScaleSentence extends Sentence implements IModel {
     
     @Optional
     @Prefix("x")
     Literal x;
 
     @Optional
     @Prefix("y")
     Literal y;
 
     @Optional
     @Prefix("z")
     Literal z;
 
     @Optional
     Literal all;
 
     boolean considerAll = false;
 
     @Setup
     public void setup() {
     	if (x != null || y != null || z != null || all != null) {
 	        if (all != null)
 	            considerAll = true;
	        else {
		        if (x == null)
		            x = new RealLiteral(1);
		        if (y == null)
		            y = new RealLiteral(1);
		        if (z == null)
		            z = new RealLiteral(1);
	        }
     	}
     }
 
     @Constraint
     public boolean check() {
         if (x == null && y == null && z == null && all == null)
             return false;
         if ((x != null || y != null || z != null) && all != null)
             return false;
         return true;
     }
     
 
     @Override
     public void run(RunData rd) {
         if (considerAll)
             glScaled(all.doubleValue(),all.doubleValue(),all.doubleValue());
         else
             glScaled(x.doubleValue(),y.doubleValue(),z.doubleValue());
     }
     
 }
