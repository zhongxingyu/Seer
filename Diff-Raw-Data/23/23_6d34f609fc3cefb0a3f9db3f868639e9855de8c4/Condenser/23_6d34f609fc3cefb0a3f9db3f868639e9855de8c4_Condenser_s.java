 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eel.seprphase2.Simulator;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import eel.seprphase2.Utilities.Density;
 import eel.seprphase2.Utilities.Mass;
 import eel.seprphase2.Utilities.Pressure;
 import eel.seprphase2.Utilities.Temperature;
 import eel.seprphase2.Utilities.Volume;
 import eel.seprphase2.Utilities.Units;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import eel.seprphase2.FailureModel.FailableComponent;
 import eel.seprphase2.FailureModel.FailureState;
 import static eel.seprphase2.Simulator.PhysicalConstants.*;
 import eel.seprphase2.Utilities.Density;
 import eel.seprphase2.Utilities.Mass;
 import eel.seprphase2.Utilities.Percentage;
 import eel.seprphase2.Utilities.Pressure;
 import eel.seprphase2.Utilities.Temperature;
 import static eel.seprphase2.Utilities.Units.*;
 import eel.seprphase2.Utilities.Velocity;
 import eel.seprphase2.Utilities.Volume;
 /**
  *
  * @author James
  */
 public class Condenser extends FailableComponent {
 
     private final Mass maximumWaterMass = kilograms(1000);
     private final Volume condenserVolume = cubicMetres(2);
 
     @JsonProperty
     private Mass waterMass;
     @JsonProperty
     private Mass steamMass;
     @JsonProperty
     private Temperature temperature;
     @JsonProperty
     private Pressure pressure;
     @JsonProperty
     private Port steamPort = new Port();
     @JsonProperty
     private Port waterPort = new Port();
     @JsonProperty 
     private Port coolantInputPort = new Port();
     @JsonProperty 
     private Port coolantOutputPort = new Port();
     
     public Condenser() {
         super();
         waterMass = kilograms(0);
         steamMass = kilograms(0);
         temperature = kelvin(350);
         pressure = pascals(101325);
         
     }
     
     public Percentage waterLevel() {
         return new Percentage(waterMass.inKilograms() / maximumWaterMass.inKilograms());
     }
 
     public Temperature temperature() {
         return this.temperature;
     }
 
     public Pressure pressure() {
         return this.pressure;
     }
     
     public Port steamPort(){
         return this.steamPort;
     }
     
     public Port waterPort(){
         return this.waterPort;
     }
     
     public Port coolantInputPort(){
         return this.coolantInputPort;
     }
     
     public void step()
     {
         if (getFailureState() == FailureState.Normal){
             steamMass = steamMass.plus(steamPort.mass);
             pressure = IdealGas.pressure(calculateSteamVolume(), steamMass, temperature);
             
             if(temperature.inKelvin()>373.15)
             {
                 
             }
             else
             {
                 waterMass = waterMass.plus(steamMass);
                 
                 /*
                  * 4.19 kJ/kg = c_w of water (specific heat of water)
                  * h_fg = c_w * (t_f - t_0) = enthalpy of water
                  * t_f = steam saturation temperature = 100C
                  * t_0 = reference temperature = 0C
                  * 
                  * m_s*h_fg = mass of steam * enthalpy = thermal energy
                  */
                 temperature = temperature.plus(new Temperature(((4.19*(steamPort.temperature.inCelsius()-temperature.inCelsius()))/1000)*steamMass.inKilograms()));         
                 temperature = temperature.plus(new Temperature(coolantInputPort.mass.inKilograms()*4.19*(coolantInputPort.temperature.inCelsius()-temperature.inCelsius())/1000));
                 
                 steamMass = kilograms(0);
                
                 pressure = IdealGas.pressure(calculateSteamVolume(), steamMass, temperature);
             
                 
                 waterPort.density = Density.ofLiquidWater();
                 waterPort.mass = waterMass;
                 waterPort.pressure = pressure;
                 waterPort.temperature = temperature;
             }
             
    
             
             Percentage wearDelta = calculateWearDelta();
             setWear(wearDelta);
         }
         
         
     }
     
     /*
      * @Todo subtract water volume
      */
     private Volume calculateSteamVolume()
     {
         return condenserVolume.minus(waterMass.volumeAt(Density.ofLiquidWater()));
     }
     
     @Override
     public Percentage calculateWearDelta()
     {
        return new Percentage(0);
     }
     
 }
