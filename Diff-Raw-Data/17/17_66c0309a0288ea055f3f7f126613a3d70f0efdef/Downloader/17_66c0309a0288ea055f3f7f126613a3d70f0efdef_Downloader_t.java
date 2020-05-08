 /*
  * JBoss, Home of Professional Open Source.
  * Copyright (c) 2011, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.noxius.plugins.download;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
  */
 public class Downloader {
     private static Logger log = Logger.getLogger(Downloader.class.getPackage().getName());
 
     private static void copy(final InputStream in, final OutputStream out) throws IOException {
         final byte[] buf = new byte[8192];
         int l;
         while ((l = in.read(buf)) > 0) {
             out.write(buf, 0, l);
         }
     }
 
     public static void download(final String spec, final String targetFileName) throws IOException {
        download(spec, targetFileName, true);
    }

    public static void download(final String spec, final String targetFileName, final boolean useExisting) throws IOException {
         final File target = new File(targetFileName);
        if (useExisting && target.exists()) {
            log.log(Level.INFO, targetFileName + " exists, skipping download");
            return;
        }
        final URL url = new URL(spec);
         final long targetLastModified = target.lastModified();
         final URLConnection conn = url.openConnection();
         conn.setIfModifiedSince(targetLastModified);
         conn.connect();
         final long lastModified = conn.getLastModified();
         // TODO: check size
         if (lastModified != 0 && lastModified <= targetLastModified) {
             log.log(Level.INFO, spec + " not modified, skipping download");
             return;
         }
         final InputStream in = new BufferedInputStream(conn.getInputStream(), 8192);
         try {
             final OutputStream out = new BufferedOutputStream(new FileOutputStream(target), 8192);
             try {
                 copy(in, out);
             } finally {
                 out.close();
             }
         } finally {
             in.close();
         }
         log.log(Level.INFO, spec + " downloaded");
     }
 }
