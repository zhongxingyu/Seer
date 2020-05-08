 package net.sf.beezle.ssass.scss;
 
 import net.sf.beezle.mork.misc.GenericException;
 
 public class MediaExpr implements Base {
     private final String feature;
     private final Expr expr;
 
     public MediaExpr(String feature, Expr expr) {
         this.feature = feature;
         this.expr = expr;
     }
 
     @Override
     public void toCss(Output output) throws GenericException {
         output.string("(", feature);
         if (expr != null) {
            output.string(": ");
             output.object(expr);
         }
         output.string(")");
     }
 }
