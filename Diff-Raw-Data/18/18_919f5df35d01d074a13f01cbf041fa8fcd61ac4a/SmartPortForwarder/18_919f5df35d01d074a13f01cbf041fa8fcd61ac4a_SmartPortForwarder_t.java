 package org.jenkinsci.plugins.slave_proxy;
 
 import hudson.model.Computer;
 import hudson.model.Label;
 import hudson.remoting.Callable;
 import hudson.remoting.Channel;
 import hudson.remoting.RemoteOutputStream;
 import hudson.remoting.SocketInputStream;
 import hudson.remoting.SocketOutputStream;
 import hudson.remoting.VirtualChannel;
 import hudson.remoting.forward.Forwarder;
 import hudson.remoting.forward.ListeningPort;
 import hudson.remoting.forward.PortForwarder;
 import hudson.util.StreamCopyThread;
 import jenkins.model.Jenkins;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.logging.Logger;
 
 import static java.util.logging.Level.*;
 
 /**
  * Listens on an TCP port (on the master), and upon a client connecting,
  * route it the HTTP proxy service running on a slave.
  *
  * @author Kohsuke Kawaguchi
  */
 public class SmartPortForwarder extends Thread implements Closeable, ListeningPort {
     private final ServerSocket socket;
 
     /**
      * Used to select the slave to forward the connection to.
      */
     private volatile Label slaveSelector;
 
     public SmartPortForwarder(SlaveProxyConfiguration sp) throws IOException {
         super(String.format("Port forwarder %d",sp.getMasterPort()));
         this.socket = new ServerSocket(sp.getMasterPort(), 50, sp.isLocalOnly() ? InetAddress.getLocalHost() : null);
         // mark as a daemon thread by default.
         // the caller can explicitly cancel this by doing "setDaemon(false)"
         setDaemon(true);
         this.slaveSelector = sp.getApplicableLabel();
     }
 
     public int getPort() {
         return socket.getLocalPort();
     }
 
     public void setSlaveSelector(Label slaveSelector) {
         this.slaveSelector = slaveSelector;
     }
 
     /**
      * Finds the forwarding service from one of the available slaves.
      */
     private Forwarder selectForwarder() throws IOException {
         for (Computer c : Jenkins.getInstance().getComputers()) {
             if (slaveSelector==null || !slaveSelector.matches(c.getNode()))
                 continue;   // not applicable
 
             VirtualChannel ch = c.getChannel();
             if (ch instanceof Channel) {
                 Channel channel = (Channel) ch;
                 Forwarder f = channel.getProperty(MasterToSlaveProxy.FORWARDER_SERVICE);
                 if (f!=null)
                     return f;
             }
         }
         throw new IOException("No slave with HTTP proxy service is connected");
     }
 
     @Override
     public void run() {
         try {
             try {
                 while(true) {
                     final Socket s = socket.accept();
                     new Thread("Port forwarding session from "+s.getRemoteSocketAddress()) {
                         public void run() {
                             try {
                                 final OutputStream out = selectForwarder().connect(new RemoteOutputStream(new SocketOutputStream(s)));
                                 new StreamCopyThread("Copier for "+s.getRemoteSocketAddress(),
                                     new SocketInputStream(s), out, true).start();
                             } catch (IOException e) {
                                LOGGER.log(WARNING, "Port forwarding failed", e);
                                close();
                            }
                        }

                        private void close() {
                            try {
                                s.close();
                            } catch (IOException _) {
                                // ignore
                             }
                         }
                     }.start();
                 }
             } finally {
                 socket.close();
             }
         } catch (IOException e) {
             LOGGER.log(FINE,"Port forwarding was shut down abnormally",e);
         }
     }
 
     /**
      * Shuts down this port forwarder.
      */
     public void close() throws IOException {
         interrupt();
         socket.close();
     }
 
     /**
      * Starts a {@link PortForwarder} accepting remotely at the given channel,
      * which connects by using the given connector.
      *
      * @return
      *      A {@link Closeable} that can be used to shut the port forwarding down.
      */
     public static ListeningPort create(VirtualChannel ch, final int acceptingPort, Forwarder forwarder) throws IOException, InterruptedException {
         // need a remotable reference
         final Forwarder proxy = ch.export(Forwarder.class, forwarder);
 
         return ch.call(new Callable<ListeningPort,IOException>() {
             public ListeningPort call() throws IOException {
                 PortForwarder t = new PortForwarder(acceptingPort, proxy);
                 t.start();
                 return Channel.current().export(ListeningPort.class,t);
             }
         });
     }
 
     private static final Logger LOGGER = Logger.getLogger(PortForwarder.class.getName());
 }
