 package eel.seprphase2.Simulator;
 
 import com.fasterxml.jackson.annotation.JsonAutoDetect;
 import com.fasterxml.jackson.annotation.JsonIdentityInfo;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.annotation.ObjectIdGenerators;
 import eel.seprphase2.GameOverException;
 import eel.seprphase2.Utilities.Energy;
 import eel.seprphase2.Utilities.Mass;
 import eel.seprphase2.Utilities.Percentage;
 import eel.seprphase2.Utilities.Pressure;
 import eel.seprphase2.Utilities.Temperature;
 import static eel.seprphase2.Utilities.Units.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import lamprey.seprphase3.DynSimulator.PlantModel;
 
 /**
  *
  * @author Marius
  */
 public class PhysicalModel implements PlantStatus {
 
     private PlantModel plant;
 
     public PhysicalModel(PlantModel plant) {
         this.plant = plant;
     }
 
     @Override
     public String[] listFailedComponents() {
         ArrayList<String> out = new ArrayList<String>();
 
         /*
          * Iterate through all pumps to get their IDs
          */
         Iterator pumpIterator = plant.pumps().entrySet().iterator();
         while (pumpIterator.hasNext()) {
             Map.Entry pump = (Map.Entry)pumpIterator.next();
 
             if (((Pump)pump.getValue()).hasFailed()) {
                 out.add("Pump " + pump.getKey());
             }
         }
 
         /*
          * Check if reactor failed
          */
         if (plant.reactor().hasFailed()) {
             out.add("Reactor");
         }
 
         /*
          * Check if turbine failed
          */
         if (plant.turbine().hasFailed()) {
             out.add("Turbine");
         }
 
         /*
          * Check if condenser failed
          */
         if (plant.condenser().hasFailed()) {
             out.add("Condenser");
         }
 
         return out.toArray(new String[out.size()]);
     }
 
     @Override
     public Temperature reactorTemperature() {
         return plant.reactor().temperature();
     }
 
     @Override
     public Percentage reactorMinimumWaterLevel() {
         return plant.reactor().minimumWaterLevel();
     }
 
     @Override
     public Energy energyGenerated() {
         return plant.energyGenerated();
     }
 
     @Override
     public Percentage controlRodPosition() {
         return plant.reactor().controlRodPosition();
     }
 
     @Override
     public Pressure reactorPressure() {
         return plant.reactor().pressure();
     }
 
     @Override
     public Percentage reactorWaterLevel() {
         return plant.reactor().waterLevel();
     }
     @Override
     public Percentage reactorWear() {
         return plant.reactor().wear();
     }
 
     @Override
     public Boolean getValveState(int valveID) throws KeyNotFoundException {
         if (plant.valves().containsKey(valveID)) {
             return plant.valves().get(valveID).getOpen();
         } else
         {
             throw new KeyNotFoundException("No valve with ID (" + valveID + ") exists!");
 	}
     }
     
     @Override
     public Boolean getPumpState(int pumpID) throws KeyNotFoundException {
         if (plant.pumps().containsKey(pumpID)) {
             return plant.pumps().get(pumpID).getStatus();
         } else
         {
             throw new KeyNotFoundException("No pump with ID (" + pumpID + ") exists!");
         }
         
     }
 
     @Override
     public ArrayList<FailableComponent> failableComponents() {
         ArrayList<FailableComponent> c = new ArrayList<FailableComponent>();
         c.add(0, plant.turbine());
         c.add(1, plant.reactor());
         c.add(2, plant.condenser());
         c.addAll(plant.pumps().values());
         return c;
     }
 
     @Override
     public Percentage turbineWear() {
         return plant.turbine().wear();
     }
 
     @Override
     public Temperature condenserTemperature() {
         return plant.condenser().getTemperature();
     }
     
 
     @Override
     public Pressure condenserPressure() {
         return plant.condenser().getPressure();
     }
     
     
 
     @Override
     public Percentage condenserWaterLevel() {
         return plant.condenser().getWaterLevel();
     }
 
     @Override
     public Percentage condenserWear() {
         return plant.condenser().wear();
     }
     
     @Override
     public String wornComponent() {
         return plant.getCurrentWornComponent();
     }
     
     @Override
     public Percentage getPumpWear(int pumpID) throws KeyNotFoundException {
         if (plant.pumps().containsKey(pumpID)) {
             return plant.pumps().get(pumpID).wear();
         } else
         {
             throw new KeyNotFoundException("No pump with ID (" + pumpID + ") exists!");
         }
     }
     
     @Override
     public double getOutputPower() {
         return plant.turbine().outputPower();
     }
 
     @Override
     public boolean turbineHasFailed() {
         return plant.turbine().hasFailed();
     }
     
     @Override
     public int getSoftwareFailureTimeRemaining() {
        return plant.getSoftwareFailureTimeRemaining();
     }
     
     @Override
     public Pressure reactorMaximumPressure() {
         return plant.reactor().maximumPressure();
     }
 
     @Override
     public Temperature reactorMaximumTemperature() {
         return plant.reactor().maximumTemperature();
     }
 
     
 }
