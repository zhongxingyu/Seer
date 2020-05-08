 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.salaboy.rolo.wedo.impl;
 
 import com.salaboy.rolo.wedo.api.BlockManager;
 import com.salaboy.rolo.wedo.api.Motor;
 import com.salaboy.rolo.wedo.api.Motor.DIRECTION;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.inject.Inject;
 
 /**
  *
  * @author salaboy
  */
 public class SimpleMotorImpl implements Motor {
 
     private String name;
     @Inject
     BlockManager manager;
 
     public synchronized void forward(int speed, long millisec) {
         try {
             byte[] buff = new byte[8];
             int n = manager.read(buff);
             if (n != 8) {
                 throw new IllegalStateException(" Wrong data");
             }
             byte[] data = new byte[]{0, 64, 60, (byte) -speed, buff[3], buff[4], buff[5], buff[6], buff[7]};
             manager.write(data);
             Thread.sleep(millisec);
             //data = new byte[]{0, 64, -60, 0, 0, 0, 1, 0, 0};
             data = new byte[]{0, 64, 0, 0, buff[3], buff[4], buff[5], buff[6], buff[7]};
             manager.write(data);
         } catch (InterruptedException ex) {
             Logger.getLogger(SimpleMotorImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public synchronized void backward(int speed, long millisec) {
         try {
             byte[] buff = new byte[8];
             int n = manager.read(buff);
             byte[] data = new byte[]{0, 64, -60, (byte) speed, 0, 0, 0, 0, 0};
             if (n == 8) {
                 data = new byte[]{0, 64, -60, (byte) speed, buff[3], buff[4], buff[5], buff[6], buff[7]};
             }
             
             manager.write(data);
             Thread.sleep(millisec);
             data = new byte[]{0, 64, 0, 0, buff[3], buff[4], buff[5], buff[6], buff[7]};
             manager.write(data);
         } catch (InterruptedException ex) {
             Logger.getLogger(SimpleMotorImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public synchronized void stop() {
         byte[] buff = new byte[8];
         int n = manager.read(buff);
         byte[] data = new byte[]{0, 64, 0, 0, 0, 0, 0, 0, 0};
         if (n == 8) {
             data = new byte[]{0, 64, 0, 0, buff[3], buff[4], buff[5], buff[6], buff[7]};
         }
         manager.write(data);
     }
 
     public synchronized void start(int speed, DIRECTION dir) {
         byte[] data = null;
         byte[] buff = new byte[8];
         int n = manager.read(buff);

        if (n != 8) {
            throw new IllegalStateException(" Wrong data");
        }
         System.out.println("## " + (buff[0] & 0xff) + "," + (buff[1] & 0xff) + "," + (buff[2] & 0xff) + "," + (buff[3] & 0xff) + "," + (buff[4] & 0xff) + "," + (buff[5] & 0xff) + "," + (buff[6] & 0xff) + "," + (buff[7] & 0xff));
         switch (dir) {
             case BACKWARD:

                 data = new byte[]{0, 64, (byte)speed, 0, 0, 0, 0, 0, 0};
                 System.out.println("## " + (data[0] & 0xff) + "," + (data[1] & 0xff) + "," + (data[2] & 0xff) + "," + (data[3] & 0xff) + "," + (data[4] & 0xff) + "," + (data[5] & 0xff) + "," + (data[6] & 0xff) + "," + (data[7] & 0xff));
                 manager.write(data);
                 break;
             case FORWARD:

                 data = new byte[]{0, 64, (byte)-speed, 0, 0, 0, 0, 0, 0};
                 System.out.println("## " + (data[0] & 0xff) + "," + (data[1] & 0xff) + "," + (data[2] & 0xff) + "," + (data[3] & 0xff) + "," + (data[4] & 0xff) + "," + (data[5] & 0xff) + "," + (data[6] & 0xff) + "," + (data[7] & 0xff));
                 manager.write(data);
                 break;
         }
 
     }
 
     @Override
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public String getName() {
         return this.name;
     }
 }
