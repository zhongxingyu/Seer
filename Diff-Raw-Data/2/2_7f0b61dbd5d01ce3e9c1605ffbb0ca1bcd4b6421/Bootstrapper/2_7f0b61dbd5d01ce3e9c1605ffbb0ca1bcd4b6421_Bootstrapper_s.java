 /*
  * Bootstrapper.java
  * Copyright (C) 2011,2012 Wannes De Smet
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.xenmaster.setup.debian;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 
 import net.wgr.settings.Settings;
 import net.wgr.utility.Network;
 
 import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
 import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.net.tftp.TFTPOptionReadRequestPacket;
 import org.apache.log4j.Logger;
 import org.xenmaster.connectivity.tftp.TFTPServer;
 import org.xenmaster.site.XenMasterSite;
 
 /**
  * 
  * @created Oct 27, 2011
  * @author double-u
  */
 public class Bootstrapper {
 
     protected TFTPServer tftpd;
     protected HashMap<String, String> preseedValues;
     protected long lastChecked;
 
     public Bootstrapper() {
         tftpd = new TFTPServer();
         preseedValues = new HashMap<>();
         lastChecked = 0;
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
                 if (downloadNetbootFiles()) return null;
 
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
 
     protected boolean downloadNetbootFiles() {
         if (System.currentTimeMillis() - lastChecked < 50 * 60 * 1000) {
             return false;
         }
 
         File localVersionFile = new File(Settings.getInstance().getString("StorePath") + "/netboot/version");
         int localVersion = -1;
         try (FileInputStream fis = new FileInputStream(localVersionFile)) {
             localVersion = Integer.parseInt(IOUtils.toString(fis));
         } catch (IOException | NumberFormatException ex) {
             Logger.getLogger(getClass()).error("Failed to retrieve local version file", ex);
         }
 
         int remoteVersion = -1;
         try {
             remoteVersion = Integer.parseInt(IOUtils.toString(XenMasterSite.getFileAsStream("/netboot/version")));
         } catch (IOException | NumberFormatException ex) {
             Logger.getLogger(getClass()).error("Failed to retrieve remote version file", ex);
         }
 
         lastChecked = System.currentTimeMillis();
 
         if (localVersion < remoteVersion) {
             Logger.getLogger(getClass()).info("New version " + remoteVersion + " found. Please hold while downloading netboot data");
             try {
                 TarArchiveInputStream tais = new TarArchiveInputStream(new GZIPInputStream(XenMasterSite.getFileAsStream("/netboot/netboot.tar.gz")));
                 TarArchiveEntry tae = null;
                 FileOutputStream fos = null;
                 while ((tae = tais.getNextTarEntry()) != null) {
                     if (tais.canReadEntryData(tae)) {
                         String target = Settings.getInstance().getString("StorePath") + "/" + tae.getName();
 
                         if (tae.isSymbolicLink()) {
                             Path targetFile = FileSystems.getDefault().getPath(tae.getLinkName());
                             Path linkName = FileSystems.getDefault().getPath(target);
 
                             // Link might already have been written as null file
                             if (targetFile.toFile().exists()) {
                                 targetFile.toFile().delete();
                             }
 
                             Files.createSymbolicLink(linkName, targetFile);
                             Logger.getLogger(getClass()).info("Created sym link " + linkName.toString() + " -> " + targetFile.toString());
                         } else if (tae.isDirectory()) {
                             new File(target).mkdir();
 
                             Logger.getLogger(getClass()).info("Created dir " + target);
                         } else if (tae.isFile()) {
                             fos = new FileOutputStream(target);
                             byte[] b = new byte[1024];
                             int curPos = 0;
                             while (tais.available() > 0) {
                                 tais.read(b, curPos, 1024);
                                 fos.write(b, curPos, 1024);
                             }
 
                             fos.flush();
                             fos.close();
 
                             Logger.getLogger(getClass()).info("Wrote file " + target);
                         }
                     }
                 }
 
                 tais.close();
                 // Write local version
                 IOUtils.write("" + remoteVersion, new FileOutputStream(localVersionFile));
             } catch (IOException ex) {
                 Logger.getLogger(getClass()).error("Failed to download netboot files", ex);
             }
         }
         
         return true;
     }
 }
