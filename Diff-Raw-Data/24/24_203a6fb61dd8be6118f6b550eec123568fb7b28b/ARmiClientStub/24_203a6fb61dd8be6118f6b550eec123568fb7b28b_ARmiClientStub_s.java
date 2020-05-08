 /*
 Copyright 2008 Flaptor (flaptor.com) 
 
 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 
 
     http://www.apache.org/licenses/LICENSE-2.0 
 
 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
 */
 
 package com.flaptor.util.remote;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import org.apache.log4j.Logger;
 
 import com.flaptor.util.Execute;
 
 
 
 public abstract class ARmiClientStub extends AClientStub {
    
     private static final Logger logger = Logger.getLogger(Execute.whoAmI());
 
     private final String serviceName;
     private final boolean waitForPolicy;
     private boolean remoteInitialized;
 
     public ARmiClientStub(int port, String host, String context, IRetryPolicy policy) {
         this(port,host,context,policy,false);
     }
 
     public ARmiClientStub(int port, String host, IRetryPolicy policy) {
         this(port,host,policy,false);
     }
 
     public ARmiClientStub(int port, String host, IRetryPolicy policy, boolean wait) {
         super(port,host,policy);
         this.waitForPolicy = wait;
         serviceName = RmiServer.DEFAULT_SERVICE_NAME;
     }
 
     public ARmiClientStub(int port, String host, String context, IRetryPolicy policy, boolean wait) {
         super(port,host,policy);
         this.waitForPolicy = wait;
         serviceName = context != null ? context : RmiServer.DEFAULT_SERVICE_NAME;
     }
 
     private void connect() throws RemoteException{
    
         logger.debug("connecting to " + host + ":" + port);
     	Remote remote = RmiUtil.getRemoteService(host, port, serviceName);
     	if (null != remote) {
    	    remoteInitialized = true;
     		this.setRemote(remote);
     	} else {
     		logger.error("Could not get remote service: " + serviceName + "@" + host + ":" + port);
     		throw new RemoteException("Could not get remote service: " + serviceName + "@" + host + ":" + port);
     	}
     };
 
 
     public void checkConnection() throws RemoteException{
        if (policy.reconnect()) {
             try {
                 connect();
             } catch (RemoteException e) {
                 connectionFailure();
                 throw e;
             }
         }

         // If policy does not allow to connect
         if (!policy.callServer())  {
             // if we have to wait for the policy to allow us
             if (waitForPolicy) {
                 //wait
                 while (!policy.callServer()) {
                     Execute.sleep(100);
                 }
             // otherwise, just fail
             } else {
                 throw new RemoteException();
             }
         }
 
         if (!remoteInitialized) {
             throw new RemoteException("Remote server never reached.");
         }
     }
 
     public void connectionSuccess() {
         policy.markSuccess();
     }
     public void connectionFailure() {
         policy.markFailure();
     }
 
 
     protected abstract void setRemote(Remote stub);
 
 
     public String toString() {
         return host + ":" + port;
     }
 }
