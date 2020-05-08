 package de.cgarbs.apsynth.internal;
 
 import de.cgarbs.apsynth.signal.Signal;
 
 public abstract class ClassType {
 
     protected int paramCount = 0;
 
     protected void checkParams(Signal[] s) {
         if (s.length != this.paramCount ) {
             // TODO throw proper exception
             throw new RuntimeException(getType()+" "+getName()+" needs "+paramCount+" parameters, but got "+s.length);
         }
     }
     
     protected void checkMinParams(Signal[] s) {
         if (s.length < this.paramCount ) {
             // TODO throw proper exception
            throw new RuntimeException(getType()+" "+getName()+" needs at least "+paramCount+" parameters, but got "+s.length);
         }
     }
     
     public int getParamCount() {
         return paramCount;
     }
 
     abstract public String getName();
     abstract public String getType();
     
     public String toString() {
         return getType()+"["+getName()+", params="+getParamCount()+"]";
     }
 }
