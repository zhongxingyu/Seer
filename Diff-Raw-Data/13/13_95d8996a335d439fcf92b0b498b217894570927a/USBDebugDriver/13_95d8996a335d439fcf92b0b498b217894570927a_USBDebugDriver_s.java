 package ru.kcode.service.drivers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
import org.apache.commons.lang.ArrayUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ru.kcode.service.protocol.Protocol;
 
 public class USBDebugDriver extends DeviceDriver implements Runnable {
     private static final String NAME = "USB Driver (debug)";
     private static final String DEVICE_NAME = "/dev/kcopter";
     private Logger log = LoggerFactory.getLogger(USBDebugDriver.class);
 
     private FileOutputStream writer;
     private FileInputStream reader;
     private boolean isRun = false;
 
     @Override
     public void sendData(Protocol p) {
         if (writer == null) {
             return;
         }
         try {
             byte[] mess = p.getMess();
             writer.write(mess, 0, p.getLen());
             writer.flush();
             log.debug("Send data: {}", p.toString());
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     @Override
     public String getName() {
         return NAME;
     }
 
     @Override
     public void start() {
         writer = getWriter();
         reader = getReader();
         isRun = true;
         Thread t = new Thread(this);
         t.start();
     }
 
     @Override
     public void stop() {
         try {
             isRun = false;
             writer.close();
             reader.close();
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     private FileOutputStream getWriter() {
         File device = new File(DEVICE_NAME);
         
         if ( !device.exists() ){
             System.out.printf("File %s not exists\n", device);
             return null;
         }
         else if ( !device.canWrite() ) {
             System.out.printf("Can not wrte to file %s\n", device);
             return null;
         }
         
         FileOutputStream stmWriter;
         try {
             stmWriter = new FileOutputStream(device);
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return null;
         }
         
         return stmWriter;
     }
     
     private FileInputStream getReader() {
         File device = new File(DEVICE_NAME);
         
         if ( !device.exists() ){
             System.out.printf("File %s not exists\n", device);
             return null;
         }
         else if ( !device.canRead() ) {
             System.out.printf("Can not wrte to file %s\n", device);
             return null;
         }
         
         FileInputStream stmReader;
         try {
             stmReader = new FileInputStream(device);
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return null;
         }
         
         return stmReader;
     }
 
     @Override
     public void run() {
         while (isRun) {
             byte buf[] = new byte[Protocol.MAX_LENGTH];
             try {
                 int available = reader.available();
 
                 //log.debug("Avaible bytes: {}", available);
                 if (available < 0) {
                     continue;
                 }
                 int len = reader.read(buf, 0, available);
                 if (len > 0) {
                    //Protocol p = new Protocol(buff);
                     String str = new String();
                     for (int i=0; i < len; i++) {
                         str = String.format("%s%02x ", str, (byte)buf[i]);
                     }
                    log.debug(str);
                    //log.debug(String.format("X: %d; Y: %d\n", p.getFrames().get(0).getData(), p.getFrames().get(1).getData()));
                     //RelationsController.getCopter3dView().setXAngle(buff[0]);
                     //RelationsController.getCopter3dView().setZAngle(buff[1]);
                 }
                 Thread.sleep(100);
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 }
