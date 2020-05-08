 /**
  * *****************************************************************************
  *
  * Copyright (c) 2012 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  * Winston Prakash
  *
  *****************************************************************************
  */
 package org.eclipse.hudson.plugins;
 
 import hudson.ProxyConfiguration;
 import hudson.Util;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.input.CountingInputStream;
 import org.eclipse.hudson.plugins.UpdateSiteManager.AvailablePluginInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Background Job to install or update a Job
  *
  * @author Winston Prakash
  */
 public final class PluginInstallationJob implements Runnable {
 
     private Logger logger = LoggerFactory.getLogger(PluginInstallationJob.class);
     private final AvailablePluginInfo plugin;
     private File pluginsDir;
     private boolean success;
     private String errorMsg;
     private ProxyConfiguration proxyConfig;
 
     public PluginInstallationJob(AvailablePluginInfo plugin, File dir, ProxyConfiguration proxyConfig) {
         this.plugin = plugin;
         pluginsDir = dir;
         this.proxyConfig = proxyConfig;
     }
 
     public String getName() {
         return plugin.getDisplayName();
     }
 
     @Override
     public void run() {
         URL src;
         try {
            logger.info("Installing the plugin " + getName());
             src = getDownloadURL();
             File dst = getDestination();
             File tmp = download(src);
             replace(dst, tmp);
            logger.info(getName() + " installation successful");
         } catch (Exception exc) {
             logger.error(getName() + " installation unsuccessful", exc);
             success = false;
             errorMsg = getStackTrace(exc);
             return;
         } catch (Error err) {
             logger.error(getName() + " installation unsuccessful", err);
             success = false;
             errorMsg = getStackTrace(err);
             return;
         }
         success = true;
     }
 
     @Override
     public String toString() {
         return super.toString() + "[plugin=" + plugin.getDisplayName() + "]";
     }
 
     public boolean getStatus() {
         return success;
     }
 
     public String getErrorMsg() {
         return errorMsg;
     }
 
     public static String getStackTrace(Throwable throwable) {
         Writer result = new StringWriter();
         PrintWriter printWriter = new PrintWriter(result);
         throwable.printStackTrace(printWriter);
         return result.toString();
     }
 
     private File download(URL src) throws IOException {
         URLConnection con;
         if ((proxyConfig != null) && (proxyConfig.name != null)) {
             con = proxyConfig.openUrl(src);
         } else {
             con = src.openConnection();
         }
         int total = con.getContentLength();
         CountingInputStream in = new CountingInputStream(con.getInputStream());
         byte[] buf = new byte[8192];
         int len;
 
         File dst = getDestination();
         File tmp = new File(dst.getPath() + ".tmp");
         OutputStream out = new FileOutputStream(tmp);
 
         try {
             while ((len = in.read(buf)) >= 0) {
                 out.write(buf, 0, len);
                 //job.status = job.new Installing(total == -1 ? -1 : in.getCount() * 100 / total);
             }
         } catch (IOException e) {
             throw new IOException("Failed to load " + src + " to " + tmp, e);
         } finally {
             IOUtils.closeQuietly(out);
             IOUtils.closeQuietly(in);
         }
 
         if (total != -1 && total != tmp.length()) {
             throw new IOException("Inconsistent file length: expected " + total + " but only got " + tmp.length());
         }
 
         return tmp;
     }
 
     /**
      * Called when the download is completed to overwrite the old file with the
      * new file.
      */
     private void replace(File dst, File src) throws IOException {
         File bak = Util.changeExtension(dst, ".bak");
         bak.delete();
         dst.renameTo(bak);
         dst.delete(); // any failure up to here is no big deal
         if (!src.renameTo(dst)) {
             throw new IOException("Failed to rename " + src + " to " + dst);
         }
     }
 
     private URL getDownloadURL() throws MalformedURLException {
         return new URL(plugin.getDownloadUrl());
     }
 
     private File getDestination() {
         return new File(pluginsDir, plugin.getName() + ".hpi");
     }
 }
