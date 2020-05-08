 /**
  * JBoss, Home of Professional Open Source. Copyright 2011, Red Hat, Inc., and individual
  * contributors as indicated by the
  *
  * @author tags. See the copyright.txt file in the distribution for a full listing of individual
  * contributors.
  *
  * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
  * General Public License as published by the Free Software Foundation; either version 2.1 of the
  * License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this
  * software; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
  * Boston, MA 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.nio2.client;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.URL;
 import java.security.KeyStore;
 import java.util.Random;
 import javax.net.ssl.*;
 
 /**
  * {@code JioClient}
  *
  * Created on Nov 11, 2011 at 3:38:26 PM
  *
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 public class SSLJioClient extends JioClient {
 
     private static final TrustManager[] trustAllCerts = new TrustManager[]{
         new X509TrustManager() {
 
             @Override
             public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                 return null;
             }
 
             @Override
             public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
             }
 
             @Override
             public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
             }
         }
     };
 
     /**
      * Create a new instance of {@code JioClient}
      *
      * @param d_max
      * @param delay
      */
     public SSLJioClient(int d_max, int delay) {
         super(d_max, delay);
     }
 
     /**
      * Create a new instance of {@code JioClient}
      *
      * @param url
      * @param d_max
      * @param delay
      */
     public SSLJioClient(URL url, int d_max, int delay) {
         super(url, d_max, delay);
     }
 
     /**
      * Create a new instance of {@code JioClient}
      *
      * @param url
      * @param delay
      */
     public SSLJioClient(URL url, int delay) {
         super(url, delay);
     }
 
     /**
      * Create a new instance of {@code JioClient}
      *
      * @param delay
      */
     public SSLJioClient(int delay) {
         super(delay);
     }
 
     /**
      *
      * @throws Exception
      */
     @Override
     protected void connect() throws Exception {
         try {
             SSLContext sslCtx = SSLContext.getInstance("TLS");
             KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
             KeyStore ks = KeyStore.getInstance("JKS");
             String fileName = System.getProperty("javax.net.ssl.keyStore");
             char[] passphrase = System.getProperty("javax.net.ssl.keyStorePassword").toCharArray();
             ks.load(new FileInputStream(fileName), passphrase);
             kmf.init(ks, passphrase);
             sslCtx.init(kmf.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
             SSLSocketFactory socketFactory = sslCtx.getSocketFactory();
             Thread.sleep(new Random().nextInt(5 * NB_CLIENTS));
             // Open connection with server
             System.out.println("Connecting to server on " + this.url.getHost() + ":" + this.url.getPort());
             SSLSocket sock = (SSLSocket) socketFactory.createSocket(this.url.getHost(), this.url.getPort());
             setInOut(sock);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      *
      * @param args
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
 
         if (args.length < 1) {
             System.err.println("Usage: java " + SSLJioClient.class.getName() + " URL [n] [delay]");
             System.err.println("\tURL: The url of the service to test.");
             System.err.println("\tn: The number of clients. (default is " + NB_CLIENTS + ")");
             System.err.println("\tdelay: The delay between writes. (default is " + DEFAULT_DELAY + "ms)");
             System.exit(1);
         }
 
         URL strURL = new URL(args[0]);
         int delay = DEFAULT_DELAY;
 
         if (args.length > 1) {
             try {
                 NB_CLIENTS = Integer.parseInt(args[1]);
                 if (args.length > 2) {
                     delay = Integer.parseInt(args[2]);
                     if (delay < 1) {
                         throw new IllegalArgumentException("Negative number: delay");
                     }
                 }
             } catch (Exception exp) {
                 System.err.println("Error: " + exp.getMessage());
                 System.exit(1);
             }
         }
 
         System.out.println("\nRunning test with parameters:");
         System.out.println("\tURL: " + strURL);
         System.out.println("\tn: " + NB_CLIENTS);
         System.out.println("\tdelay: " + delay);
 
 
         String home = System.getProperty("user.home") + File.separatorChar;
         System.setProperty("javax.net.ssl.trustStore", home + "cacerts.jks");
         System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
         System.setProperty("javax.net.ssl.keyStore", home + ".keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "bismillah");
 
 
         Thread clients[] = new Thread[NB_CLIENTS];
 
         for (int i = 0; i < clients.length; i++) {
             clients[i] = new SSLJioClient(strURL, delay);
         }
 
         for (int i = 0; i < clients.length; i++) {
             clients[i].start();
         }
 
         for (int i = 0; i < clients.length; i++) {
             clients[i].join();
         }
     }
 }
