 package com.jsravn.mc;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.Before;
 
 public class StandardSimulatorTest {
     private double result;
     Simulation mockSim = new Simulation() {
 	    public double simulate() {
 		return result;
 	    }
 	};
     Simulator sim;
 
     @Before public void setUp() {
 	result = 0.0;
 	sim = new StandardSimulator(mockSim);
     }
 
     @Test public void testIterations() {
 	assertEquals(0, sim.iteration());
 	sim.run();
 	assertEquals(1, sim.iteration());
 	for (int i = 0; i < 100; i++) {
 	    sim.run();
 	}
 	assertEquals(101, sim.iteration());
     }
 
     double muValues[][] = { {1.0, 1.0, 0.0},
 			    {0.0, 0.5, 0.0},
 			    {1.0, 0.67, 0.01},
 			    {0.5, 0.625, 0.0},
 			    {-0.25, 0.45, 0.0} };
 
     @Test public void testMu() {
 	assertEquals(Double.NaN, sim.mu());
 	for (double[] muVal : muValues) {
 	    result = muVal[0];
 	    sim.run();
 	    assertEquals(muVal[1], sim.mu(), muVal[2]);
 	}
     }
 
     double varValues[][] = { {1.0, 0.0, 0.0},
 			     {0.0, 0.25, 0.0},
 			     {-0.5, 0.38, 0.01} };
 
     @Test public void testVariance() {
 	assertEquals(Double.NaN, sim.variance());
 	for (double[] varVal : varValues) {
 	    result = varVal[0];
 	    sim.run();
 	    assertEquals(varVal[1], sim.variance(), varVal[2]);
 	}
     }
 
     double errValues[][] = { {1.0, 0.0, 0.0},
			     {0.0, 0.35, 0.01},
			     {-0.5, 0.36, 0.01} };
 
     @Test public void testFastError() {
 	assertEquals(Double.NaN, sim.fastError());
 	for (double[] errVal : errValues) {
 	    result = errVal[0];
 	    sim.run();
 	    assertEquals(errVal[1], sim.fastError(), errVal[2]);
 	}
     }
 
     @Test public void testError() {
 	assertEquals(Double.NaN, sim.error(67), 0.0);
 	result = 1.0;
 	sim.run();
 	assertEquals(0.0, sim.error(67), 0.0);
     }
 }
