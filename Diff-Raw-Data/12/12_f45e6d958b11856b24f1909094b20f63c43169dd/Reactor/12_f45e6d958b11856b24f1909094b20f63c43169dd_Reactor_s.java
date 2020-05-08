 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eel.seprphase2.Simulator.PhysicalModel;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import eel.seprphase2.Simulator.FailureModel.FailableComponent;
 import eel.seprphase2.Simulator.FailureModel.FailureState;
 import static eel.seprphase2.Simulator.PhysicalModel.PhysicalConstants.*;
 import eel.seprphase2.Utilities.Density;
 import eel.seprphase2.Utilities.Mass;
 import eel.seprphase2.Utilities.Percentage;
 import eel.seprphase2.Utilities.Pressure;
 import eel.seprphase2.Utilities.Temperature;
 import static eel.seprphase2.Utilities.Units.*;
 import eel.seprphase2.Utilities.Velocity;
 import eel.seprphase2.Utilities.Volume;
 import java.lang.Math.*;
 
 /**
  *
  * @author Yazidi
  */
 public class Reactor extends FailableComponent {
 
     private final Mass maximumWaterMass = kilograms(1000);
     private final Mass minimumWaterMass = kilograms(800);
     private final Volume reactorVolume = cubicMetres(2);
     @JsonProperty
     private FuelPile fuelPile = new FuelPile();
     @JsonProperty
     private Mass waterMass;
     @JsonProperty
     private Mass steamMass;
     @JsonProperty
     private Temperature temperature;
     @JsonProperty
     private Pressure pressure;
     @JsonProperty
     private Density steamDensity;
     @JsonProperty
     private Port outputPort = new Port();
     @JsonProperty
     private Port inputPort = new Port();
     @JsonProperty
     private double boilingPtAtPressure;
     @JsonProperty
     private double neededEnergy;
 
     /**
      *
      */
     public Reactor() {
         super();
         fuelPile.moveControlRods(new Percentage(0));
         waterMass = maximumWaterMass;
         steamMass = kilograms(0);
         temperature = kelvin(350);
         pressure = pascals(101325);
     }
 
     /**
      *
      * @param controlRodPosition
      * @param waterLevel
      * @param temperature
      * @param pressure
      */
     public Reactor(Percentage controlRodPosition, Percentage waterLevel,
                    Temperature temperature, Pressure pressure) {
         super();
         fuelPile.moveControlRods(controlRodPosition);
         waterMass = kilograms(maximumWaterMass.inKilograms() * waterLevel.ratio());
         steamMass = kilograms(0);
         this.temperature = temperature;
         this.pressure = pressure;
     }
 
     /**
      *
      * @param extracted
      */
     public void moveControlRods(Percentage extracted) {
         fuelPile.moveControlRods(extracted);
     }
 
     /**
      *
      * @return
      */
     public Percentage controlRodPosition() {
         return fuelPile.controlRodPosition();
     }
 
     /**
      *
      * @return
      */
     public Percentage waterLevel() {
         return new Percentage((waterMass.inKilograms() / maximumWaterMass.inKilograms())*100);
     }
 
     /**
      *
      * @return
      */
     public Temperature temperature() {
         return this.temperature;
     }
 
     /**
      *
      * @return
      */
     public Pressure pressure() {
         return this.pressure;
     }
 
     /**
      *
      */
     public void step() {
 
         if (steamMass.inKilograms() > inputPort.mass.inKilograms()) {
             steamMass = steamMass.minus(inputPort.mass);
             waterMass = waterMass.plus(inputPort.mass);
             calculateNewTemperature(inputPort);
         }
         else {
            waterMass = waterMass.plus(steamMass);
             steamMass = kilograms(0);
             calculateNewTemperature(inputPort);
             
         }
         
         
         if (hasFailed()) {
             System.exit(0);
         }
         
         /*
          * Calculates the boiling point at the current pressure,
          * then it calculates the needed enegry to reach that boiling point
          */
         
         boilingPtAtPressure = boilingPointOfWater + 10*Math.log(pressure.inPascals()/atmosphericPressure);
         
         neededEnergy = (boilingPtAtPressure - temperature.inKelvin()) * waterMass.inKilograms() * specificHeatOfWater;
         
         
        if (neededEnergy >= fuelPile.output(1)) {
 
             /*
              * Calculates how much the water heats if it's not at boiling point
              */
                      
             temperature = kelvin(temperature.inKelvin() +
                                  fuelPile.output(1) / waterMass.inKilograms() /
                                  specificHeatOfWater);
             outputPort.mass = kilograms(0);
         } else {
 
             /*
              * Sets temperature to boiling point
              * If any energy is left from the fuelpile after heating up:
              * Calculates how much water turns to steam in one timestep at boiling point using remaining energy
              */
             temperature = kelvin(boilingPtAtPressure);
             Mass deltaMass = kilograms((fuelPile.output(1) - neededEnergy) / latentHeatOfWater);
             steamMass = steamMass.plus(deltaMass);
             waterMass = waterMass.minus(deltaMass);
             outputPort.mass = deltaMass;
         }
 
         /*
          * Calculates volume of steam in this particular timestep
          * Calculates pressure of said steam
          */
 
         Volume steamVolume = reactorVolume.minus(waterMass.volumeAt(Density.ofLiquidWater()));
         pressure = IdealGas.pressure(steamVolume, steamMass, temperature);
         if (pressure.inPascals() < atmosphericPressure) {
             pressure = pascals(atmosphericPressure);
         }
         steamDensity = steamMass.densityAt(steamVolume);
 
         /*
          * Sends information to output port
          */
         
         outputPort.flow = steamMass;
         outputPort.density = steamDensity;
         outputPort.pressure = pressure;
         outputPort.temperature = temperature;
 
         //System.out.println("Reactor Water Mass " + waterMass);
         //System.out.println("Reactor Steam Mass " + outputPort.flow);
 
 
 
         /*
          * Calculates component wear after a time step
          */
 
         Percentage wearDelta = calculateWearDelta();
         setWear(wearDelta);
     }
 
     /**
      *
      * @return
      */
     public Velocity outputFlowVelocity() {
         return metresPerSecond(pressure().inPascals() / 100);
     }
 
     /**
      *
      * @return
      */
     public Port outputPort() {
         return outputPort;
     }
     
     public Port inputPort() {
         return inputPort;
     }
     
     public Mass maximumWaterMass() {
         return maximumWaterMass;
     }
 
     public Mass minimumWaterMass() { 
         return minimumWaterMass;
     }
     public void calculateNewTemperature(Port in) {
         temperature = kelvin((temperature.inKelvin() * waterMass.inKilograms() + in.temperature.inKelvin() * in.mass
                               .inKilograms()) / (waterMass.inKilograms() + in.mass.inKilograms()));
     }
     
     @Override
     public Percentage calculateWearDelta() {
         return new Percentage(0);
     }
 
     Percentage minimumWaterLevel() {
         return new Percentage((this.minimumWaterMass.inKilograms()/this.maximumWaterMass.inKilograms())*100);
     }
     
     
 }
