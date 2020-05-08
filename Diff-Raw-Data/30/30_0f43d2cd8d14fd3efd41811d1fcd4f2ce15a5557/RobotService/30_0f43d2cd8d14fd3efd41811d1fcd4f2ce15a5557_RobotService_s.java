 package com.foton.robot_controller;
 
 import android.app.Service;
 import android.bluetooth.BluetoothSocket;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Pair;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Stack;
 
 /**
  * Created by foton on 06.10.13.
  */
 public class RobotService extends Service {
     private final int cellSize = 1;
     private final int fieldChunkSize = 10;
     public final static String BROADCAST_REPAINT = "com.foton.robot_controller.BROADCAST_REPAINT";
 
     private final IBinder binder = new LocalBinder();
     private BluetoothClient bluetoothClient = new BluetoothClient();
     private AutopilotThread autopilotThread;
     private volatile boolean stopAutopilot = false;
     private Field field = new Field(cellSize, fieldChunkSize);
     boolean autopilotRunning = false;
 
     Field getField() {
         return field;
     }
 
     void setBluetoothClient(BluetoothClient bluetoothClient_) {
         bluetoothClient = bluetoothClient_;
     }
 
     public class LocalBinder extends Binder {
         RobotService getService() {
             return RobotService.this;
         }
     }
 
     @Override
     public void onCreate() {
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         return START_REDELIVER_INTENT;
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return binder;
     }
 
     public boolean sendCommand(ArrayList<Integer> command, ArrayList<Integer> result) {
         return bluetoothClient.sendCommand(command, result);
     }
 
     public void startAutopilot() {
         if (autopilotThread != null && autopilotThread.isAlive())
             return;
         stopAutopilot = false;
         autopilotThread = new AutopilotThread();
         autopilotThread.start();
         autopilotRunning = true;
     }
 
     public void stopAutopilot() {
         stopAutopilot = true;
         autopilotRunning = false;
     }
 
     public boolean getAutopilotRunning() {
         return autopilotRunning;
     }
 
     private class AutopilotThread extends Thread {
         Stack<Macros> macrosStack = new Stack<Macros>();
 
         AutopilotThread() {
             super();
             macrosStack.push(new MoveStraight(RobotService.this, macrosStack));
         }
         public void run() {
             while (!stopAutopilot) {
                 //Macros macros = macrosStack.peek();
                 //macros.run();
                /*Cell zeroCell1 = field.getCellByCoords(new Pair<Double, Double>(Double.valueOf(0), Double.valueOf(0)));
                 zeroCell1.wall = !zeroCell1.wall;
                Cell zeroCell2 = field.getCellByCoords(new Pair<Double, Double>(Double.valueOf(-2), Double.valueOf(-3)));
                 zeroCell2.wall = !zeroCell2.wall;
                 Cell zeroCell3 = field.getCellByCoords(new Pair<Double, Double>(Double.valueOf(2), Double.valueOf(-5)));
                zeroCell3.wall = !zeroCell3.wall;*/
                Cell zeroCell4 = field.getCellByCoords(new Pair<Double, Double>(Double.valueOf(6), Double.valueOf(-4)));
                 zeroCell4.wall = !zeroCell4.wall;
                 repaint();
 
                 try {
                     Thread.sleep(1000);
                 }
                 catch (InterruptedException e) {
                 }
             }
         }
     }
 
     public void repaint() {
         Intent intent = new Intent(BROADCAST_REPAINT);
         sendBroadcast(intent);
     }
 }
