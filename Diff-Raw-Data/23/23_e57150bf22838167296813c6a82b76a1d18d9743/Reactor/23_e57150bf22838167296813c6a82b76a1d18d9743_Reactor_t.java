 package eel.seprphase2.Simulator;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import eel.seprphase2.GameOverException;
 import static eel.seprphase2.Simulator.PhysicalConstants.*;
 import eel.seprphase2.Utilities.Density;
 import eel.seprphase2.Utilities.Mass;
 import eel.seprphase2.Utilities.Percentage;
 import eel.seprphase2.Utilities.Pressure;
 import eel.seprphase2.Utilities.Temperature;
 import static eel.seprphase2.Utilities.Units.*;
 import eel.seprphase2.Utilities.Velocity;
 import eel.seprphase2.Utilities.Volume;
 import lamprey.seprphase3.DynSimulator.FlowThroughComponent;
 import lamprey.seprphase3.DynSimulator.OutputPort;
 import static lamprey.seprphase3.DynSimulator.GameConfig.*;
 
 /**
  *
  * @author Marius
  */
 public class Reactor extends FailableComponent {

     @JsonProperty
     private final Mass maximumWaterMass = REACTOR_VOLUME.massAt(Density.ofLiquidWater());
     @JsonProperty
     private final Mass minimumWaterMass = kilograms(maximumWaterMass.inKilograms() * REACTOR_MINIMUMSAFEWATERLEVEL.ratio());
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
     private double boilingPtAtPressure;
     @JsonProperty
     private double neededEnergy;
     @JsonProperty
     private double deltaSeconds;
 
     /**
      *
      */
     public Reactor() {
         super();
         fuelPile.moveControlRods(new Percentage(0));
         waterMass = REACTOR_INITIALWATERMASS;
         steamMass = kilograms(0);
         steamDensity = kilogramsPerCubicMetre(0);
         temperature = ROOMTEMPERATURE.plus(kelvin(60));
         pressure = pascals(atmosphericPressure);
         this.pressurised = true;
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
         correctWaterMass();
     }
 
     /**
      *
      */
     public void step(double seconds) throws GameOverException {
         System.out.println("Reactor: ");
         System.out.println("\t steam mass: " + steamMass);
         System.out.println("\t water mass: " + waterMass);
         System.out.println("\t max water mass: " + maximumWaterMass);
         System.out.println("\t min water mass: " + minimumWaterMass);
 
         deltaSeconds = seconds;
         updateSteamAndWater(seconds);
         if (hasFailed()) {
             throw new GameOverException();
         }
         updateTemperatureAndEvaporateAccordingly(seconds);
         recalcPressure();
         stepWear();
     }
 
     private void updateTemperatureAndEvaporateAccordingly(double seconds) {
         /*
          * Calculates the boiling point at the current pressure,
          * then it calculates the needed enegry to reach that boiling point
          */
         boilingPtAtPressure = boilingPointOfWater + 10 * Math.log(pressure.inPascals() / atmosphericPressure);
         neededEnergy = (boilingPtAtPressure - temperature.inKelvin()) * waterMass.inKilograms() * specificHeatOfWater;
         if (neededEnergy >= fuelPile.output(seconds)) {
             /*
              * Calculates how much the water heats if it's not at boiling point
              */
             temperature = kelvin(temperature.inKelvin() +
                                  fuelPile.output(seconds) / waterMass.inKilograms() /
                                  specificHeatOfWater);
         } else {
             /*
              * Sets temperature to boiling point
              * If any energy is left from the fuelpile after heating up:
              * Calculates how much water turns to steam in one timestep at boiling point using remaining energy
              */
             temperature = kelvin(boilingPtAtPressure);
             Mass deltaMass = kilograms((fuelPile.output(seconds) - neededEnergy) / latentHeatOfWater);
             steamMass = steamMass.plus(deltaMass);
             waterMass = waterMass.minus(deltaMass);
             correctWaterMass();
         }
     }
 
     public void updateSteamAndWater(double seconds) {
         if (steamMass.inKilograms() > massOfSteamLeavingOverTime(seconds).inKilograms()) {
             steamMass = steamMass.minus(massOfSteamLeavingOverTime(seconds));
             waterMass = waterMass.plus(getMassComingInOverTime(seconds));
             correctWaterMass();
             temperature = temperature.plus(calculateNewTemperature(input.outputPort(this)));
         } else {
             waterMass = waterMass.plus(steamMass);
             waterMass = waterMass.plus(getMassComingInOverTime(seconds));
             correctWaterMass();
             steamMass = kilograms(0);
             temperature = temperature.plus(calculateNewTemperature(input.outputPort(this)));
         }
     }
 
     /**
      * Calculates pressure due to steam. Then also calculates the density.
      */
     public void recalcPressure() {
         Volume steamVolume = REACTOR_VOLUME.minus(waterMass.volumeAt(Density.ofLiquidWater()));
         pressure = IdealGas.pressure(steamVolume, steamMass, temperature);
         if (pressure.inPascals() < atmosphericPressure) {
             pressure = pascals(atmosphericPressure);
         }
         steamDensity = steamMass.densityAt(steamVolume);
     }
 
     /**
      * Calculates the change in temperature due to the flow in from an OutputPort.
      * @param in OutputPort to calculate the temperature difference from.
      *           It will usually be the OutputPort connected to the input of this component.
      * @return 
      */
     private Temperature calculateNewTemperature(OutputPort in) {
         Temperature deltaTemp = in.temperature.minus(this.temperature);
         Mass totalMass = this.steamMass.plus(this.waterMass);
         Mass massIn = in.flownThroughInTime(deltaSeconds);
         
         deltaTemp = kelvin(deltaTemp.inKelvin() * (massIn.inKilograms() / totalMass.inKilograms()));
         System.out.println("\t water mass in: " + massIn);
         return deltaTemp;
     }
 
     // avoid issues with floating-point error
     private void correctWaterMass() {
         if (waterMass.inKilograms() > maximumWaterMass.inKilograms()) {
             waterMass = maximumWaterMass;
         }
         if (waterMass.inKilograms() < 0) {
             waterMass = kilograms(0);
         }
     }
     
     @Override
     public Percentage calculateWearDelta() {
         return new Percentage(0);
     }
 
     public Percentage minimumWaterLevel() {
         return REACTOR_MINIMUMSAFEWATERLEVEL;
     }
     
     public Temperature maximumTemperature() {
         return REACTOR_MAXIMUMTEMPERATURE;
     }
     
     public Pressure maximumPressure() {
         return REACTOR_MAXIMUMPRESSURE;
     }
     
     public Mass maximumWaterMass() {
         return maximumWaterMass;
     }
 
     public Mass minimumWaterMass() {
         return minimumWaterMass;
     }
 
     public void moveControlRods(Percentage extracted) {
         fuelPile.moveControlRods(extracted);
     }
 
     public Percentage controlRodPosition() {
         return fuelPile.controlRodPosition();
     }
 
     /**
      * Returns the water level in the reactor as a percentage of it's capacity.
      * @return the water level in the reactor as a percentage of it's capacity. 
      */
     public Percentage waterLevel() {
         return new Percentage((waterMass.inKilograms() / maximumWaterMass.inKilograms()) * 100);
     }
 
     public Temperature temperature() {
         return this.temperature;
     }
 
     public Pressure pressure() {
         return this.pressure;
     }
 
     public Density steamDensity() {
         return this.steamDensity;
     }
 
     private Mass getMassComingInOverTime(double seconds) {
         return this.input.outputPort(this).flownThroughInTime(seconds);
     }
 
     private Mass massOfSteamLeavingOverTime(double seconds) {
         return this.outputPort(null).flownThroughInTime(seconds);
     }
 }
