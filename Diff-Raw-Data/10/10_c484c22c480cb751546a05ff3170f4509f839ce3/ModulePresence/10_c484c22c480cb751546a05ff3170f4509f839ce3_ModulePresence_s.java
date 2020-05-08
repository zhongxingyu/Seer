 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.communication;
 
 import net.sourceforge.frcsimulator.internals.CRIO;
 import net.sourceforge.frcsimulator.internals.ModuleException;
 
 /**
  * Class to check whether an emulated module is present.
  * @author wolf
  */
 public class ModulePresence {
 	public static final int kMaxModuleNumber = 2;
 
     public static class ModuleType {
 
         public static final ModuleType kUnknown = new ModuleType(0x00);
         public static final ModuleType kAnalog = new ModuleType(0x01);
         public static final ModuleType kDigital = new ModuleType(0x02);
         public static final ModuleType kSolenoid = new ModuleType(0x03);
         private final int m_intValue;
 
         private ModuleType(int value) {
             m_intValue = value;
         }
 
         public int getValue() {
             return m_intValue;
         }
         @Override
         public String toString(){
             return m_intValue==0x03?"Solenoid":m_intValue==0x02?"Digital":m_intValue==0x01?"Analog":"Unknown";
         }
     };
 	/**
      * Determines whether the module of the given type and number is present.
      * @TODO verify that this actually works how it does on the real robot, the javadoc is somewhat ambiguous
      * @param moduleType The type of the module to be check.
      * @param moduleNumber The ID for this type of module to check (usually 0 or 1).
      * @return Whether the given module is present.
      */
     public static boolean getModulePresence(ModuleType moduleType, int moduleNumber) {
         if(moduleType == ModuleType.kUnknown){
             try{
                 CRIO.getInstance().getModule(moduleNumber+1);
                 return true;
             } catch(ModuleException me){return false;}
         }
         if(moduleType == ModuleType.kAnalog){
             try{
                 if(moduleNumber < 2){
                     CRIO.getInstance().getModule(moduleNumber+1);
                     return true;
                 }
             } catch(ModuleException me){return false;}
         }
         if(moduleType == ModuleType.kDigital){
             try{
                if(moduleNumber == 1){
                     CRIO.getInstance().getModule(4);
                     return true;
                 }
                if(moduleNumber == 2){
                     CRIO.getInstance().getModule(6);
                 }
            } catch(ModuleException me){return false;}
         }
         if(moduleType == ModuleType.kSolenoid){
             try{
                 CRIO.getInstance().getModule(8);
             } catch(ModuleException me){return false;}
         }
         return false;
     }
 }
