 package iotc.test;
 
 import java.util.Random;
 import java.util.UUID;
 import org.itolab.morihit.clinkx.*;
 
 /**
  * テスト用ダミーUPnPデバイス
  * @author atsushi-o
  */
 public class DummyUPnPDevice implements Runnable, UPnPActionListener {
     private final Thread th;
     private final UPnPDevice device;
     private boolean isStop = false;
 
    private UPnPFloatStateVariable    illum;
     private UPnPFloatStateVariable      temp;
     private UPnPFloatStateVariable      hum;
 
     public DummyUPnPDevice() {
         this("Dummy-"+UUID.randomUUID().toString());
     }
     public DummyUPnPDevice(String deviceName) {
         device = new UPnPDevice(deviceName);
 
         temp = new UPnPFloatStateVariable("Temperature");
        illum = new UPnPFloatStateVariable("Illuminance");
         hum = new UPnPFloatStateVariable("Humidity");
 
         UPnPService service = new UPnPService();
         addVariable(service, temp, illum, hum);
         device.addService(service);
 
         th = new Thread(this);
     }
 
     private void addVariable(UPnPService service, UPnPStateVariable ...variables) {
         for (UPnPStateVariable val : variables) {
             service.addStateVariable(val);
             val.publish();
             UPnPAction action = new UPnPSetFloatAction(val.getName());
             action.setActionListener(this);
             service.addAction(action);
             action = new UPnPGetFloatAction(val.getName());
             action.setActionListener(this);
             service.addAction(action);
         }
     }
 
     public synchronized void start() {
         temp.setValue(22.0f);
         illum.setValue(1000);
         hum.setValue(50.0f);
 
         this.isStop = false;
         th.start();
     }
 
     public synchronized void stop() {
         this.isStop = true;
     }
 
     @Override
     public void run() {
         device.start();
 
         while (!isStop) {
             /* Update variable value at random */
             Random r = new Random();
 
             float ttemp = temp.getValue();
             ttemp += r.nextInt(4) + r.nextFloat() - 2.0f;
             temp.setValue(ttemp);
 
            float tillum = illum.getValue();
             tillum += r.nextInt(200) - 100;
             illum.setValue(tillum);
 
             float thum = hum.getValue();
             thum += r.nextInt(6) + r.nextFloat() - 3.0f;
             hum.setValue(thum);
 
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException ex) {}
         }
 
         device.stop();
     }

    public float getTemperature() {
        return temp.getValue();
    }
    public void setTemperature(float f) {}
    public float getIlluminance() {
        return illum.getValue();
    }
    public void setIlluminance(float i) {}
    public float getHumidity() {
        return hum.getValue();
    }
    public void setHumidity(float f) {}
 }
