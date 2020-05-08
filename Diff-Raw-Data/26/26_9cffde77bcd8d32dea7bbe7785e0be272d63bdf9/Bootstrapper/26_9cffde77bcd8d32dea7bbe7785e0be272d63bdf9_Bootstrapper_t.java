 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.setup.debian;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import net.wgr.settings.Settings;
 import net.wgr.utility.Network;
 import net.wgr.xenmaster.connectivity.tftp.TFTPServer;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.net.tftp.TFTPOptionReadRequestPacket;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Oct 27, 2011
  * @author double-u
  */
 public class Bootstrapper {
 
     protected TFTPServer tftpd;
     protected HashMap<String, String> preseedValues;
 
     public Bootstrapper() {
         tftpd = new TFTPServer();
         preseedValues = new HashMap<>();
     }
 
     public void boot() {
         tftpd.addListener(new PXEListener());
         tftpd.boot();
 
         preseedValues.put("menuItemLabel", "^Install Debian");
     }
     
     public void waitForServerToQuit() throws InterruptedException {
         tftpd.waitTillQuit();
     }
 
     protected class PXEListener implements TFTPServer.ActivityListener {
 
         @Override
         public InputStream pathRequest(TFTPOptionReadRequestPacket packet) {
             try {
                 File path = new File(Settings.getInstance().getString("StorePath") + "/netboot/" + packet.getFilename());
                 if (!path.exists()) {
                     return null;
                 }
                 File f = path.getAbsoluteFile();
                 if (f.exists()) {
                     FileInputStream fis = new FileInputStream(f);
                     if (f.getName().equals("txt.cfg")) {
                         preseedValues.put("preseedUrl", "http://" + Network.getHostAddressInSubnet(packet.getAddress().getHostAddress(), "255.255.0.0").getHostAddress() + ":" + Settings.getInstance().get("WebApplicationPort") + "/setup/xapi");
 
                         String txt = IOUtils.toString(fis);
                         for (Map.Entry<String, String> entry : preseedValues.entrySet()) {
                             // todo regex
                             txt = txt.replace("#{" + entry.getKey() + "}", entry.getValue());
                         }
                        
                        return new ByteArrayInputStream(txt.getBytes("UTF-8"));
                     }
                     return fis;
                 }
             } catch (IOException ex) {
                 Logger.getLogger(getClass()).error("File not found", ex);
             }
             return null;
         }
     }
 }
