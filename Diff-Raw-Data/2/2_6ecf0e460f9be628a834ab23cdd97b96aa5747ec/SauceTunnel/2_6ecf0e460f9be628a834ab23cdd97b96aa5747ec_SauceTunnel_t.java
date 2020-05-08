 /*
  * The MIT License
  *
  * Copyright (c) 2010, InfraDNA, Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.saucelabs.rest;
 
 import com.trilead.ssh2.Connection;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Represents a sauce tunnel server on the cloud.
  *
  * <p>
  * This object internally holds the status information that it retrieved from the server,
  * and getter methods will work against this state information. To obtain the up-to-date
  * status information from the server, use the {@link #refresh()} method. 
  *
  * @author Kohsuke Kawaguchi
  */
 public final class SauceTunnel {
     private final SauceTunnelFactory factory;
     private final String id;
     /**
      * The status information of this tunnel, if it's already obtained. Can be null.
      */
     private StatusResponse status;
     /**
      * SSH connection.
      */
     private Connection ssh;
 
     SauceTunnel(SauceTunnelFactory factory, String id) {
         this.factory = factory;
         this.id = id;
     }
 
     SauceTunnel(SauceTunnelFactory factory, StatusResponse status) {
         this(factory,status.id);
         this.status = status;
     }
 
     /**
      * Gets the unique ID of this tunnel.
      */
     public String getId() {
         return id;
     }
 
     /**
      * Returns the timestamp when this tunnel was created.
      */
     public Date getCreationTime() throws IOException {
         return new Date(status().CreationTime);
     }
 
     /**
      * Returns the timestamp when this tunnel was destroyed.
      *
      * @return null
      *      If the tunnel is not yet destroyed.
      */
     public Date getShutDownTime() throws IOException {
         long t = status().ShutDownTime;
         if (t==0)   return null;
         return new Date(t);
     }
 
     /**
      * Gets the tunnel host name inside the Sauce OnDemand cloud.
      */
     public String getHost() throws IOException {
         return status().Host;
     }
 
     /**
      * Gets the list of the domain names that this tunnel maps to.
      */
     public List<String> getDomainNames() throws IOException {
         return Collections.unmodifiableList(status().DomainNames);
     }
 
     /**
      * Is this tunnel actively running?
      *
      * <p>
      * While it's unclear if all the possible states of the tunnel is a committed part of the API,
      * the tunnel appears to transition from "booting" -> "running" -> "halting". This method
      * returns true iff the status is "running".
      *
      * <p>
      * Note that the status information isn't updated every time you call this method, so do not
      * call this method in a loop without calling {@link #refresh()}.
      */
     public boolean isRunning() throws IOException {
         return "running".equals(status().Status);
     }
 
     /**
      * Waits until the tunnel transitions into the running state, or until the specified timeout expires.
      *
      * <p>
      * The method returns normally both in case of time out and successful tunnel start up.
      * Use {@link #isRunning()} to verify the result.
      *
      * @param timeout
      *      number of milli-seconds to wait, or -1 to wait forever. Timeout is approximation and not
      *      necessarily accurately honored.
      */
     public void waitUntilRunning(long timeout) throws IOException, InterruptedException {
         long start = System.currentTimeMillis();
        while (!isRunning() && (timeout>=0 && System.currentTimeMillis()<start+timeout))
             Thread.sleep(3000);
     }
 
     /**
      * Destroys the tunnel server. This will eliminate all the existing SSH tunnels to this server
      * anywhere, not just ones from this JVM.
      *
      * This is not to be confused with {@link #disconnectAll()}.
      */
     public void destroy() throws IOException {
         factory.credential.call("tunnels/"+id).delete();
     }
 
     /**
      * Lazily retrieves the status if necessary.
      */
     private StatusResponse status() throws IOException {
         if (status==null)
             refresh();
         return status;
     }
 
     /**
      * Establishes the remote-to-local port forwarding.
      */
     public synchronized void connect(int remotePort, String localHost, int localPort) throws IOException {
         if (ssh==null) {
             ssh = new Connection(getHost());
             ssh.connect();
         }
         factory.credential.authenticate(ssh);
         ssh.requestRemotePortForwarding("0.0.0.0",remotePort,localHost,localPort);
     }
 
     /**
      * Cancels the remote-to-local port forwarding.
      */
     public synchronized void disconnect(int remotePort) throws IOException {
         if (ssh!=null)
             ssh.cancelRemotePortForwarding(remotePort);
     }
 
     /**
      * Shuts down all the SSH tunnels opened between the sauce tunnel server and
      * this JVM.
      *
      * This is not to be confused with {@link #destroy()}.
      */
     public synchronized void disconnectAll() {
         if (ssh!=null) {
             ssh.close();
             ssh = null;
         }
     }
 
 
     /**
      * Retrieves the up-to-date status information by contacting the server.
      *
      * @throws IOException
      *      If the communication fails, or if the tunnel no longer exists.
      */
     public void refresh() throws IOException {
         status = factory.credential.call("tunnels/"+id).get(StatusResponse.class);
     }
 
 }
