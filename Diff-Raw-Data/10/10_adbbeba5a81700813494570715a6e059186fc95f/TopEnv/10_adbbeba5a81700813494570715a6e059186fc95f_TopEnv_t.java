 package org.unbunt.sqlscript.support;
 
 import org.unbunt.sqlscript.lang.Null;
 import org.unbunt.sqlscript.lang.Obj;
 
 import java.util.ArrayList;
 
 /**
  * User: tweiss
  * Date: 23.04.2009
  * Time: 09:19:56
  * <p/>
  * Copyright: (c) 2007 marketoolz GmbH
  */
 public class TopEnv implements Env {
     protected ArrayList<Obj> vars = new ArrayList<Obj>();
 
     public TopEnv() {
     }
 
     public Obj get(Variable var) {
         return get(var, var.address);
     }
 
     public Obj get(Variable var, int addr) {
        addr &= 0xFFFF;
         resizeFor(addr);
         Obj value = vars.get(addr);
         if (value == null) {
             warn(var);
             value = Null.instance;
             vars.set(addr, value);
         }
         return value;
     }
 
     public void set(Variable var, Obj value) {
         set(var, var.address, value);
     }
 
     public void set(Variable var, int addr, Obj value) {
        addr &= 0xFFFF;
         resizeFor(addr);
         if (vars.get(addr) == null) {
             warn(var);
         }
         vars.set(addr, value);
     }
 
     protected void resizeFor(int addr) {
         vars.ensureCapacity(addr + 1);
        int size = vars.size();
        while (size++ <= addr) {
            vars.add(null);
        }
     }
 
     public void add(Obj value) {
         throw new RuntimeException("TopEnv.add() not implemented");
     }
 
     protected void warn(Variable var) {
         System.err.println("Undefined variable: " + var.name);
     }
 
     public void extend() {
         throw new RuntimeException("TopEnv.extend() should not be called");
     }
 
     public int getMaxAddress() {
         throw new RuntimeException("TopEnv.getMaxAddress() should not be called");
     }
 
     public Obj getThis() {
         throw new RuntimeException("TopEnv.getThis() should not be called");
     }
 
     public void setThis(Obj thisRef) {
         throw new RuntimeException("TopEnv.setThis() should not be called");
     }
 }
