 package com.tihiy.reo;
 
 import junit.framework.TestCase;
 
 public class SphereModelTest extends TestCase {
     public void testSphereModel(){
         ElectrodeSystem electrodeSystem = new ElectrodeSystem(0.06, 0.03, 0, 0);
         BodyGeometry bodyGeometry = new BodyGeometry(0.040, 0.020);
         SphereModel model = new SphereModel();
         Measurement measurement = new Measurement(model , electrodeSystem, bodyGeometry);
         double value = model.getPotentialInPoint(new ReoPoint(0, -0.03));
         assertEquals(-1.646, value, 0.001);
     }



 }
