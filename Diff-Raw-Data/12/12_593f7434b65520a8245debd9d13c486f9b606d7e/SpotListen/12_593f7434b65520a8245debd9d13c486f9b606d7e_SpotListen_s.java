 package org.sunspotworld;
 
 public class SpotListen extends PeriodicTask {
     private Connection rconn = new Connection(1); // conecção em mode de leitura
     private LuminosityReader lr = null;
     private TemperatureReader tr = null;
     private MovementReader mr = null;
     public static int READ_PERIOD = 500;          // periodo para verificar input
 
     // verifica 
     public void doTask() {
        rconn.spotRecive(lr, tr, mr);
     }
 
     public SpotListen (LuminosityReader l, TemperatureReader t, MovementReader m) {
         super(1, READ_PERIOD);
         lr = l;
         tr = t;
         mr = m;
     }
 
 }
