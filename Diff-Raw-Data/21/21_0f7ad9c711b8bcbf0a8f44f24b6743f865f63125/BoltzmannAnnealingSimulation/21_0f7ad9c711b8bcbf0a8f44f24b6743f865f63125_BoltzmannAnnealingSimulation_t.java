 package com.example.second;
 
 
 public class BoltzmannAnnealingSimulation extends AnnealingSimulation {
 
     protected BoltzmannAnnealingSimulation(SingleVariableFunction function) {
         super(function);
     }
 
     @Override
     protected double nextState(double state, double temperature) {
         return normalRandom(state,temperature);
     }
 
     @Override
     protected double cooling(int iteration) {
        return startingTemperature/Math.log(2+iteration);
     }
 
     @Override
     protected double initState() {
         return normalRandom(0, startingTemperature);
     }
 
     private double normalRandom( double mean, double standardDeviation ){
         double value = 0;
         for (int i=0; i<12; i++){
             value += Math.random();
         }
 
         return ( value - 6 + mean )*standardDeviation;
     }
 
 }
