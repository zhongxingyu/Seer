 package net.sf.beezle.ssass.scss;
 
 import net.sf.beezle.mork.misc.GenericException;
 
 public class MediaQuery implements Base {
     /** may be null */
     private final MediaOperator op;
 
     /** may be null */
     private final String type;
 
     private final MediaExpr[] exprs;
 
     public MediaQuery(MediaOperator op, String type, MediaExpr[] exprs) {
         this.op = op;
         this.type = type;
         this.exprs = exprs;
     }
 
     @Override
     public void toCss(Output output) throws GenericException {
         boolean first;
 
         if (op != null) {
             output.string(op.toString());
         }
         if (type != null) {
             output.string(type);
         }
        first = true;
         for (MediaExpr expr : exprs) {
             if (first) {
                 first = false;
             } else {
                output.string("and");
             }
             expr.toCss(output);
         }
     }
 }
