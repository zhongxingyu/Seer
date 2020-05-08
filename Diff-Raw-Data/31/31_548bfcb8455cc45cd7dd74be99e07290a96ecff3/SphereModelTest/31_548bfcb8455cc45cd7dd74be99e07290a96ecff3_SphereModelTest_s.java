 package com.tihiy.reo;
 
 import junit.framework.TestCase;
 
 public class SphereModelTest extends TestCase {
     public void testSphereModel(){
         ElectrodeSystem eSystem = new ElectrodeSystem(0.06, 0.03, 0, 0);
         BodyGeometry bodyGeometry = new BodyGeometry(0.040, 0.020);
         SphereModel model = new SphereModel();
         Measurement measurement = new Measurement(model , eSystem, bodyGeometry);
         double value[] = getPotentials(model, eSystem);
         double expValue[] = {-1.646, -0.23, -0.23, -1.646};
         for(int i = 0; i < 4; i++){
             assertEquals(expValue[i], value[i], 0.001);
         }
 
         eSystem.setPosition(0, 0.01);
         expValue = new double[]{-2.212, -0.315, -0.184, -1.18};
         value = getPotentials(model, eSystem);
         for(int i = 0; i < 4; i++){
             assertEquals(expValue[i], value[i], 0.001);
         }
 
         eSystem.setPosition(0, -0.01);
         expValue = new double[]{-1.18, -0.184, -0.315, -2.212};
         value = getPotentials(model, eSystem);
         for(int i = 0; i < 4; i++){
             assertEquals(expValue[i], value[i], 0.001);
         }
 
         eSystem.setPosition(0.01, 0);
         expValue = new double[]{-1.58, -0.243, -0.243, -1.58};
         value = getPotentials(model, eSystem);
         for(int i = 0; i < 4; i++){
             assertEquals(expValue[i], value[i], 0.001);
         }
 
         eSystem.setPosition(-0.01, 0);
         expValue = new double[]{-1.58, -0.243, -0.243, -1.58};
         value = getPotentials(model, eSystem);
         for(int i = 0; i < 4; i++){
             assertEquals(expValue[i], value[i], 0.001);
         }
     }
 
     public void testMovingESystem(){
         ElectrodeSystem eSystem = new ElectrodeSystem(0.06, 0.03, 0, 0.01);
         BodyGeometry bodyGeometry = new BodyGeometry(0.050, 0.020);
         SphereModel model = new SphereModel();
         model.setRo(5, 1.5);
         Measurement measurement = new Measurement(model , eSystem, bodyGeometry);
         double value = measurement.getMeasurement();
         assertEquals(-2.058, value, 0.001);
         eSystem.setPosition(0, 0);
         value = measurement.getMeasurement();
         assertEquals(-2.032, value, 0.001);
         eSystem.setPosition(0, -0.01);
         value = measurement.getMeasurement();
         assertEquals(-2.058, value, 0.001);
 
         eSystem.setPosition(0.02, 0.01);
         value = measurement.getMeasurement();
         assertEquals(-1.7, value, 0.001);
         eSystem.setPosition(0.02, 0);
         value = measurement.getMeasurement();
         assertEquals(-1.681, value, 0.001);
         eSystem.setPosition(0.02, -0.01);
         value = measurement.getMeasurement();
         assertEquals(-1.7, value, 0.001);
 
         eSystem.setPosition(-0.02, 0.01);
         value = measurement.getMeasurement();
         assertEquals(-1.7, value, 0.001);
         eSystem.setPosition(-0.02, 0);
         value = measurement.getMeasurement();
         assertEquals(-1.681, value, 0.001);
         eSystem.setPosition(-0.02, -0.01);
         value = measurement.getMeasurement();
         assertEquals(-1.7, value, 0.001);
     }
 
 
     private double[] getPotentials(SphereModel model, ElectrodeSystem eSystem){
         double value[] = new double[4];
         value[0] = model.potentialInPointFromElectrode(eSystem.getUElectrode(ElectrodeSystem.El.MINUS), eSystem.getIElectrode(ElectrodeSystem.El.MINUS));
         value[1] = model.potentialInPointFromElectrode(eSystem.getUElectrode(ElectrodeSystem.El.MINUS), eSystem.getIElectrode(ElectrodeSystem.El.PLUS));
         value[2] = model.potentialInPointFromElectrode(eSystem.getUElectrode(ElectrodeSystem.El.PLUS), eSystem.getIElectrode(ElectrodeSystem.El.MINUS));
         value[3] = model.potentialInPointFromElectrode(eSystem.getUElectrode(ElectrodeSystem.El.PLUS), eSystem.getIElectrode(ElectrodeSystem.El.PLUS));
         return  value;
     }
 }
