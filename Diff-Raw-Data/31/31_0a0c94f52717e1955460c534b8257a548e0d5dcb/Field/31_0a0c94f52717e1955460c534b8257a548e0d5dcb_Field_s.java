 package adsim.core;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 
 import lombok.*;
 
 public class Field {
    public static final int MAX_SEND_COUNT = 1000;
     private final @Getter
     SpaceMediator space = initSpace();
     private double size;
     private ArrayList<Device> devices;
 
     private long sentCount;
     private long disposedCount;
 
     public List<Device> getDevices() {
         return this.devices;
     }
 
     public Field(double fieldSize) {
         this.size = fieldSize;
         this.devices = new ArrayList<Device>();
     }
 
     private SpaceMediator initSpace() {
         return new SpaceMediator(this);
     }
 
     public void addDevice(Device dev) {
         if (dev == null)
             throw new IllegalArgumentException("dev is null");
         dev.setBound(size);
         this.devices.add(dev);
     }
 
     public void addDevices(Collection<Device> devs) {
         for (val dev : devs) {
             addDevice(dev);
         }
     }
 
     public void next() {
         this.dispathPackets();
     }
 
     private void dispathPackets() { // FIXME
         int sent = 0;
         while (sent < MAX_SEND_COUNT) {
             boolean anyoneSent = false;
             for (val dev : getDevices()) {
                 val request = dev.offerSend();
                 if (request != null) {
                     space.dispatch(dev, request);
                     sentCount += 1;
                     sent += 1;
                     anyoneSent = true;
                     if (sent >= MAX_SEND_COUNT) {
                         break;
                     }
                 }
             }
             if(!anyoneSent) {
                 break;
             }
         }
         for (val dev : getDevices()) {
             val remain = dev.tellOverflowed();
             disposedCount += remain;
         }
     }
 
     public void expandCapacity(int size) {
         devices.ensureCapacity(devices.size() + size);
     }
 
     public long getWholeDisposedCount() {
         return disposedCount;
     }
 
     public long getWholeSentCount() {
         return sentCount;
     }
 
 }
