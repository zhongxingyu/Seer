 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.server.jetty;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.net.ServerSocket;
 import java.security.KeyStore;
 import java.security.SecureRandom;
 
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLServerSocket;
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.TrustManagerFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mortbay.http.JsseListener;
 import org.mortbay.util.InetAddrPort;
 
 import com.adito.boot.ContextHolder;
 import com.adito.boot.ContextKey;
 import com.adito.boot.KeyStoreManager;
 import com.adito.boot.PropertyList;
 import com.adito.server.CustomKeyManager;
 
 
 /**
  * Extension to Jettys {@link org.mortbay.http.JsseListener} that loads
  * its SSL keys from Aditos keystore using the password that has been
  * configured in the property database.
  */
 public class CustomJsseListener extends JsseListener {
     private String keyPassword;
     private TrustManager trustManager;
     private boolean initialised;
     private boolean require = false;
     private boolean configureContext = true;
     private static boolean createAvailableCipherSuitesList = true;
     final static Log log = LogFactory.getLog(CustomJsseListener.class);
 
     /**
      * Constructor
      *
      * @param keyPassword key password
      */
     public CustomJsseListener(String keyPassword) {
         super();
         init(keyPassword);
     }
 
     /**
      * Constructor
      *
      * @param address address and port on which to listen
      * @param keyPassword key password
      */
     public CustomJsseListener(InetAddrPort address, String keyPassword) {
         super(address);
         init(keyPassword);
     }
 
     private void init(String keyPassword) {
         this.keyPassword = keyPassword;
     }
 
     /**
      * Set the trust manager to use. This must be done <b>before</b> the
      * socket factory is initialised (i.e. when Jetty is started). If the
      * method is not called the default trust managers will be used.
      *
      * @param trustManager trustManager
      * @throws IllegalStateException if the Jetty has already been started
      *
      */
     public void setTrustManager(TrustManager trustManager, boolean require) {
         if(initialised) {
             throw new IllegalStateException("Socket factory already created. Cannot set trust manager.");
         }
         this.trustManager = trustManager;
         this.require = require;
 
     }
 
     protected ServerSocket newServerSocket( InetAddrPort p_address,
                                             int p_acceptQueueSize )
         throws IOException
     {
         SSLServerSocket serverSocket = (SSLServerSocket)super.newServerSocket(p_address, p_acceptQueueSize);
         if(serverSocket.getNeedClientAuth()) {
         	
                serverSocket.setNeedClientAuth(require);
                setNeedClientAuth(require);
                if(!require)
                   serverSocket.setWantClientAuth(true);
         }
         
         
         String[] ciphers = serverSocket.getSupportedCipherSuites();
         String[] protocols = serverSocket.getSupportedProtocols();
         
         if(log.isInfoEnabled()) {
         	log.info("The following protocols are supported:");
 	        for(int i=0;i<protocols.length;i++) {
 	        	log.info("     " + protocols[i]);
 	        }
         }
         
         if(createAvailableCipherSuitesList) {
         	File f = new File(ContextHolder.getContext().getTempDirectory(), "availableCipherSuites.txt");
         	BufferedWriter writer = null; 
         	
         	try {
             	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
             	if(log.isInfoEnabled())
             		log.info("The following cipher suites are supported:");
         		for(int i=0;i<ciphers.length;i++) {
 					if(log.isInfoEnabled())
 						log.info("     " + ciphers[i]);
 					writer.write(ciphers[i]);
 					writer.newLine();
 				}
 			} catch (Throwable e) {
 				log.error("Could not create cipher list!", e);
 				configureContext = false;
 			} finally {
 				if(writer!=null)
 					writer.close();
 			}
 			createAvailableCipherSuitesList = false;
         }
         
         if(configureContext) {
 	        
         	PropertyList list = ContextHolder.getContext().getConfig().retrievePropertyList(new ContextKey("ssl.supportedProtocols"));
         	
         	if(!list.isEmpty()) {
         		serverSocket.setEnabledProtocols(list.asArray());
         	}
         		
 	        list = ContextHolder.getContext().getConfig().retrievePropertyList(new ContextKey("ssl.supportedCiphers"));
 	        
 	        if(!list.isEmpty()) { 
 	        	serverSocket.setEnabledCipherSuites(list.asArray());
 	        }
         }
         
         protocols = serverSocket.getEnabledProtocols();
         
         if(log.isInfoEnabled()) {
     		log.info("The following protocols are enabled:");
 			for(int i=0;i<protocols.length;i++) {
 				log.info("     " + protocols[i]);
 			}
         }
 		
 		
         ciphers = serverSocket.getEnabledCipherSuites();
     	if(log.isInfoEnabled()) {
     		log.info("The following cipher suites are enabled:");
 			for(int i=0;i<ciphers.length;i++) {
 				log.info("     " + ciphers[i]);
 			}
     	}
 		
         return serverSocket;
     }
 
     protected SSLServerSocketFactory createFactory() throws Exception {
         if(KeyStoreManager.getInstance(KeyStoreManager.DEFAULT_KEY_STORE).isKeyStoreEmpty()) {
             throw new Exception("The keystore does not contain any certificates. Please run the installation wizard (--install).");
         }
         KeyStore ks = KeyStoreManager.getInstance(KeyStoreManager.DEFAULT_KEY_STORE).getKeyStore();
         String pw = ContextHolder.getContext().getConfig().retrieveProperty(new ContextKey("webServer.keystore.sslCertificate.password"));
         KeyManager[] kma = new KeyManager[] { new CustomKeyManager(pw) };
         TrustManager[] tma = null;
         if(trustManager == null) {
             TrustManagerFactory tm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
             tm.init(ks);
             tma = tm.getTrustManagers();
         }
         else {
 
             // LDP - Add the existing trust managers so that outgoing certificates are still trusted.
             TrustManagerFactory tm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
             tm.init(ks);
 
             tma = new TrustManager[tm.getTrustManagers().length + 1];
            for(int i=0;i<tm.getTrustManagers().length;i++) {
                 tma[i] = tm.getTrustManagers()[i];
             }
             tma[tma.length-1] = trustManager;
         }
         SSLContext sslc = SSLContext.getInstance("SSL");
         sslc.init(kma, tma, SecureRandom.getInstance("SHA1PRNG"));
         SSLServerSocketFactory ssfc = sslc.getServerSocketFactory();
         if (log.isInfoEnabled())
         	log.info("SSLServerSocketFactory=" + ssfc);
         initialised = true;
         return ssfc;
     }
 
 	public boolean isConfigureContext() {
 		return configureContext;
 	}
 
 	public void setConfigureContext(boolean configureContext) {
 		this.configureContext = configureContext;
 	}
 
 }
