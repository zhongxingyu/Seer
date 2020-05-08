 /**
  * Unified Simulator for Self-Reconfigurable Robots (USSR)
  * (C) University of Southern Denmark 2008
  * This software is distributed under the BSD open-source license.
  * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
  */
 package rar;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import ussr.description.Robot;
 import ussr.description.geometry.VectorDescription;
 import ussr.description.setup.ModulePosition;
 import ussr.description.setup.WorldDescription;
 import ussr.model.Controller;
 import ussr.model.Module;
 import ussr.physics.PhysicsFactory;
 import ussr.physics.PhysicsLogger;
 import ussr.physics.PhysicsObserver;
 import ussr.physics.PhysicsParameters;
 import ussr.physics.PhysicsSimulation;
 import ussr.physics.TimedPhysicsObserver;
 import ussr.remote.SimulationClient;
 import ussr.remote.facade.ParameterHolder;
 import ussr.samples.GenericSimulation;
 import ussr.samples.atron.ATRON;
 import ussr.samples.atron.ATRONController;
 import ussr.samples.atron.GenericATRONSimulation;
 import ussr.visualization.VisualizationParameters;
 
 /**
  * Port of the eight-to-car simulation to Java.  Classical ATRON self-reconfiguration example.
  * 
  * @author ups
  */ 
 public abstract class EightToCarRobustnessExperiment extends GenericATRONSimulation {
 
     public static final boolean USE_BLOCKING_ROTATE = true;
     public static final boolean CORRECT_CAR_WHEELS = false;
     public static final boolean VERIFY_OPERATIONS = false;
     public static final int CACHE_SIZE = 64;
 
     @Override
     protected void simulationHook(PhysicsSimulation simulation) {
         float maxTime = ((Parameters)Parameters.get()).maxTime;
         simulation.waitForPhysicsTimestep(new TimedPhysicsObserver(maxTime) {
             public void physicsTimeStepHook(PhysicsSimulation simulation) {
                 reportResult(false);
             }
         });
     }
     
     public static class Parameters extends ParameterHolder {
         public int number;
         public float minR, maxR, completeR, maxTime;
         public Parameters(Class<?> mainClass, int number, float minR, float maxR, float completeR, float maxTime) {
             super(mainClass);
             this.number = number;
             this.minR = minR;
             this.maxR = maxR;
             this.completeR = completeR;
             this.maxTime = maxTime;
         }
         public String toString() {
            return (super.mainClass==null?"_":super.mainClass.getName())+"#"+number+":minR="+minR+",maxR="+maxR+",comR="+completeR+",maxT="+maxTime;
         }
         /* (non-Javadoc)
          * @see java.lang.Object#hashCode()
          */
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = super.hashCode();
             result = prime * result + Float.floatToIntBits(completeR);
             result = prime * result + Float.floatToIntBits(maxR);
             result = prime * result + Float.floatToIntBits(maxTime);
             result = prime * result + Float.floatToIntBits(minR);
             result = prime * result + number;
             return result;
         }
         /* (non-Javadoc)
          * @see java.lang.Object#equals(java.lang.Object)
          */
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
             if (!super.equals(obj))
                 return false;
             if (getClass() != obj.getClass())
                 return false;
             Parameters other = (Parameters) obj;
             if (Float.floatToIntBits(completeR) != Float
                     .floatToIntBits(other.completeR))
                 return false;
             if (Float.floatToIntBits(maxR) != Float.floatToIntBits(other.maxR))
                 return false;
             if (Float.floatToIntBits(maxTime) != Float
                     .floatToIntBits(other.maxTime))
                 return false;
             if (Float.floatToIntBits(minR) != Float.floatToIntBits(other.minR))
                 return false;
             if (number != other.number)
                 return false;
             return true;
         }
     }
     
     @Override
     protected void setupPhysicsHook() {
         PhysicsLogger.setDisplayInfo(true);
         PhysicsParameters.get().setMaintainRotationalJointPositions(true);
         PhysicsParameters.get().setRealisticCollision(true);
         PhysicsParameters.get().setPhysicsSimulationStepSize(0.001f); // before: 0.0005f
         PhysicsParameters.get().setWorldDampingLinearVelocity(0.9f);
         PhysicsParameters.get().setUseModuleEventQueue(true);
         PhysicsParameters.get().setRealtime(false);
         PhysicsFactory.getOptions().setStartPaused(false);
        PhysicsFactory.getOptions().setHeadless(false);
         //VisualizationParameters.get().setAlwaysShowConnectors(true);
         PhysicsParameters.get().setGravity(0);
     }
     
     protected void changeWorldHook(WorldDescription world) {
         world.setPlaneTexture(WorldDescription.WHITE_GRID_TEXTURE);
         world.setHasBackgroundScenery(false);
     }
 
     protected ArrayList<ModulePosition> buildRobot() {
         float Yoffset = 0.25f+2*ATRON.UNIT;
         ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>();
         mPos.add(new ModulePosition("#0", new VectorDescription(0*ATRON.UNIT,0*ATRON.UNIT-Yoffset,0*ATRON.UNIT), ATRON.ROTATION_NS_BROKEN));
         mPos.add(new ModulePosition("#1", new VectorDescription(1*ATRON.UNIT,0*ATRON.UNIT-Yoffset,1*ATRON.UNIT), ATRON.ROTATION_EW));
         mPos.add(new ModulePosition("#2", new VectorDescription(1*ATRON.UNIT,0*ATRON.UNIT-Yoffset,-1*ATRON.UNIT), ATRON.ROTATION_EW));
         mPos.add(new ModulePosition("#3", new VectorDescription(2*ATRON.UNIT,0*ATRON.UNIT-Yoffset,0*ATRON.UNIT), ATRON.ROTATION_NS_BROKEN));
         if(CORRECT_CAR_WHEELS) {
             mPos.add(new ModulePosition("#4", new VectorDescription(3*ATRON.UNIT,0*ATRON.UNIT-Yoffset,1*ATRON.UNIT), ATRON.ROTATION_WE));
             mPos.add(new ModulePosition("#5", new VectorDescription(3*ATRON.UNIT,0*ATRON.UNIT-Yoffset,-1*ATRON.UNIT), ATRON.ROTATION_WE));
         } else {
             mPos.add(new ModulePosition("#4", new VectorDescription(3*ATRON.UNIT,0*ATRON.UNIT-Yoffset,1*ATRON.UNIT), ATRON.ROTATION_EW));
             mPos.add(new ModulePosition("#5", new VectorDescription(3*ATRON.UNIT,0*ATRON.UNIT-Yoffset,-1*ATRON.UNIT), ATRON.ROTATION_EW));
         }
         mPos.add(new ModulePosition("#6", new VectorDescription(4*ATRON.UNIT,0*ATRON.UNIT-Yoffset,0*ATRON.UNIT), ATRON.ROTATION_NS_BROKEN));
         return mPos;
     }
 
     public void reportResult(boolean success) throws Error {
         PhysicsSimulation sim = GenericSimulation.getPhysicsSimulation();
         String experiment = ParameterHolder.get().toString();
         try {
             if(SimulationClient.getReturnHandler()==null) {
                 System.err.println("No return handler specified; time taken = "+sim.getTime());
                 System.exit(0);
             }
             if(success)
                 SimulationClient.getReturnHandler().provideReturnValue(experiment,"success",sim.getTime());
             else
                 SimulationClient.getReturnHandler().provideReturnValue(experiment,"timeout", null);
             System.exit(0);
         } catch(RemoteException exn) {
             throw new Error("Unable to report return value");
         }
     }
     
 
 }
