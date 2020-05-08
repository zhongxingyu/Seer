 package bv;
 
 public class Op3 extends Expression {
     public enum OpName {
         if0, fold, tfold, total
     }
 
     public Op3 () {
         super(false, false, false, 0);
        throw new Error("Op3.eval()!");
     }
 
     @Override
     public long eval () {
        throw new Error("Op3.eval()!");
     }
 
     @Override
     public long weight () {
        throw new Error("Op3.eval()!");
     }
 
     @Override
     public Expression filter (long output) {
        throw new Error("Op3.eval()!");
     }
 
     @Override
     public String toString () {
        throw new Error("Op3.eval()!");
     }
 }
