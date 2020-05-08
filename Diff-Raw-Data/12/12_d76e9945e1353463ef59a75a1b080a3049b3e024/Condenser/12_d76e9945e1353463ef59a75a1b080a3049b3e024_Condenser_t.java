 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eel.seprphase2.Simulator.PhysicalModel;
 
 import eel.seprphase2.Simulator.FailureModel.CannotRepairException;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import eel.seprphase2.Simulator.FailureModel.FailableComponent;
 import eel.seprphase2.Simulator.FailureModel.FailureState;
 import eel.seprphase2.Utilities.Mass;
 import eel.seprphase2.Utilities.*;
 import static eel.seprphase2.Utilities.Units.*;
 
 /**
  *
  * @author Yazidi
  */
 public class Condenser extends FailableComponent {
 
     @JsonProperty
     private Mass steamMass;
     @JsonProperty
     private Mass waterMass;
     @JsonProperty
     private final Mass maximumWaterMass = kilograms(1000);
     @JsonProperty
     private Port outputPort = new Port();
     @JsonProperty
     private Port steamInputPort = new Port();
     @JsonProperty
     private Port coolantInputPort = new Port();
     @JsonProperty
     private Port reactorInputPort = new Port();
     @JsonProperty
     private Temperature temperature;
     @JsonProperty
     private Pressure pressure;
     @JsonProperty
     private Percentage waterLevel = percent(0);
     @JsonProperty
     private Mass buildUp = kilograms(0);
 
     public Condenser() {
         InitVariables();
     }
 
     public void InitVariables() {
         pressure = pascals(101325);
         waterMass = kilograms(0);
         steamMass = kilograms(0);
         /*
          steamInputPort.mass = kilograms(0);
          reactorInputPort.mass = kilograms(0);
          outputPort.mass = kilograms(0);
          steamInputPort.temperature = kelvin(0);
          reactorInputPort.temperature = kelvin(0);
          */
         temperature = kelvin(298.15);  //Start at room temp
     }
 
     public void step() {
 
        waterMass = outputPort.mass.plus(buildUp);
         steamMass = steamInputPort.flow;
         buildUp = kilograms(0);
         //Debug
         
         //System.out.println("Condenser Steam Mass: " + steamInputPort.mass);
         
         
         
         if (reactorInputPort.mass.inKilograms() > 0) {
             calculateNewTemperature(reactorInputPort);
         } else if (steamInputPort.mass.inKilograms() > 0) {
             calculateNewTemperature(steamInputPort);
         }
         if (!hasFailed()){
         calculateNewTemperature(coolantInputPort);
         }
 
 
 
         pressure = IdealGas.pressure(calculateSteamVolume(), steamMass, temperature);
 
         //System.out.println("Condenser Steam Input Mass: " + steamInputPort.mass);
 
         try {
 
             if (steamInputPort.mass.inKilograms() > 0) {
                 waterMass = waterMass.plus(steamInputPort.mass);
             }
 
             if (reactorInputPort.mass.inKilograms() > 0) {
                 waterMass = waterMass.plus(reactorInputPort.mass);
             }
             
             //Debug
             
             //System.out.println("Condenser Water Mass: " + waterMass);
             
             waterLevel = new Percentage((waterMass.inKilograms() / maximumWaterMass.inKilograms()) * 100);
 
         } catch (Exception e) {
             waterLevel = new Percentage(100);
             //This is over-pressure condition, however will be handeled by failure model and not needed to be done here
         }
         
         
         if (hasFailed()) {
             //Do nothing until the condenser has been repaired
 
             outputPort.mass = kilograms(0);
             outputPort.pressure = pascals(101325);
            buildUp = waterMass.plus(steamInputPort.mass);
 
         }
         else
         {
         outputPort.mass = waterMass;
         outputPort.temperature = temperature;
         outputPort.pressure = pressure;
         setWear(calculateWearDelta());
         }
        
 
     }
 
     /*
      * @Todo subtract water volume
      */
     private Volume calculateSteamVolume() {
         return maximumWaterMass.volumeAt(Density.ofLiquidWater()).minus(waterMass.volumeAt(Density.ofLiquidWater()));
     }
 
     public void calculateNewTemperature(Port in) {
         /*
          temperature = kelvin((temperature.inKelvin() * waterMass.inKilograms() + in.temperature.inKelvin() * in.mass
          .inKilograms()) / (waterMass.inKilograms() + in.mass.inKilograms()));
          */
 
 
         /*
          * 4.19 kJ/kg = c_w of water (specific heat of water)
          * h_fg = c_w * (t_f - t_0) = enthalpy of water
          * t_f = steam saturation temperature = 100C
          * t_0 = reference temperature = 0C
          * 
          * m_s*h_fg = mass of steam * enthalpy = thermal energy
          */
         temperature = temperature.plus(new Temperature(((4.19 * (in.temperature.inKelvin() - temperature.inKelvin())) /
                                                         1000) * in.mass.inKilograms()));
 
 
     }
 
     public void reduceTemperature(int delta) {
         temperature = temperature.minus(kelvin(delta));
     }
 
     public void setTemperature(Temperature newTemp) {
         temperature = newTemp;
     }
 
     public Temperature getTemperature() {
         return temperature;
     }
 
     public void setWaterMass(Mass newWater) {
         waterMass = newWater;
     }
 
     public Mass getWaterMass() {
         return waterMass;
     }
 
     public Pressure getPressure() {
         return pressure;
     }
 
     public Port inputPort() {
         return steamInputPort;
     }
 
     public Port outputPort() {
         return outputPort;
     }
 
     public Port coolantInputPort() {
         return coolantInputPort;
     }
 
     public Percentage getWaterLevel() {
         return waterLevel;
     }
 
     @Override
     public Percentage calculateWearDelta() {
         return percent(1);
     }
 
     @Override
     public void repair() throws CannotRepairException {
         super.repair();
         InitVariables();
     }
 }
