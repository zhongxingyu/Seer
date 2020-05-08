 package com.jsravn.mc;
 
import java.lang.Math;

 class StandardSimulator implements Simulator {
     private Simulation sim;
     private BootstrapError bootstrap;
     private int N;
     private double sum;
     private double sumOfSquares;
     private double mu;
     private double variance;
     private double fastError;
 
     StandardSimulator(Simulation sim) {
 	this.sim = sim;
 	bootstrap = new BootstrapError();
     }
 
     public void run() {
 	N += 1;
 
 	final double result = sim.simulate();
 	sum += result;
 	sumOfSquares += result * result;	
 	mu = sum / N;
 	variance = sumOfSquares / N - mu * mu;
	fastError = Math.sqrt(variance / N);
 	bootstrap.addSample(result);
     }
 
     public int iteration() {
 	return N;
     }
 
     public double mu() {
 	return mu;
     }
 
     public double variance() {
 	return variance;
     }
 
     public double fastError() {
 	return fastError;
     }
 
     public double error(int confidence) {
 	return bootstrap.calcError(confidence);
     }
 }
