 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.sourceforge.frcsimulator.internals;
 
 /**
  * Used by the simulator to represent a hardware module.
  * @author benjamin
  */
 public class CRIOModule implements FrcBotSimComponent{
     private FrcBotSimProperties m_simProperties;
     /**
      * Creates a module of the given type to be used by the CRIO class.
      * @param type The type of module to be created: Analog, Digital, or Solenoid.
      * @throws ModuleException 
      */
     public CRIOModule(int type) throws ModuleException{
         if(type<0x1||type>0x3){throw new InvalidModuleException();}
         m_simProperties = new FrcBotSimProperties();
         m_simProperties.put("type", new FrcBotSimProperty<Integer>(type));
     }
     /**
      * Gets the type of this module as specified in the constructor.
      * @return The type of this module.
      */
     public int getType(){
         return (Integer)m_simProperties.get("type").get();
     }
     /**
      * Gets the properties of this module that can be parsed by the simulator or an external program.
      * @return The FrcBotSimProperties of this module.
      */
     @Override
     public FrcBotSimProperties getSimProperties() {
         return m_simProperties;
     }
     
 }
