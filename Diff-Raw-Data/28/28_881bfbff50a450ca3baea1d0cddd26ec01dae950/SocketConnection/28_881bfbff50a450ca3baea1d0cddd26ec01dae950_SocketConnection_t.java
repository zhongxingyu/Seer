 package uk.org.smithfamily.mslogger.comms;
 
 import java.io.*;
 import java.net.*;
 
 import uk.org.smithfamily.mslogger.ecu.simulated.MS1Simulator;
 import uk.org.smithfamily.mslogger.ecu.simulated.MSSimulator;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 
 /**
  * Implements a connection to a megasquirt ECU over a network connection
  * As no ECUs currently support WiFi or Ethernet, spawn off a simulator 
  * instead.  Right now, this is hardwired to MS1
  * @author dgs
  *
  */
 public enum SocketConnection implements Connection
 {
     INSTANCE;
 
     private MSSimulator sim = new MS1Simulator();
     private Socket sock;
     private OutputStream os;
     private InputStream is;
 
     @Override
     public void init(String addr)
     {
         sim.init();
         try
         {
             sim.startRunning();
        }
        catch (IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
     }
 
     @Override
     public boolean isInitialised()
     {
         return sim.isRunning();
     }
 
     @Override
     public void connect() throws IOException
     {
        if (!sim.isRunning())
         {
             init("");
         }
         InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
         sock = new Socket(serverAddr, MSSimulator.SERVERPORT);
         is = sock.getInputStream();
         os = sock.getOutputStream();
     }
 
     @Override
     public void disconnect() throws IOException
     {
         sim.stopRunning();
 
     }
 
     @Override
     public void switchSettings()
     {
 
     }
 
     @Override
     public InputStream getInputStream() throws IOException
     {
         return is;
     }
 
     @Override
     public OutputStream getOutputStream() throws IOException
     {
         return os;
     }
 
     @Override
     public void tearDown()
     {
         if (is != null)
         {
             try
             {
                 is.close();
            }
            catch (IOException e)
             {
                 DebugLogManager.INSTANCE.logException(e);
             }
             is = null;
         }
         if (os != null)
         {
             try
             {
                 os.close();
            }
            catch (IOException e)
             {
                 DebugLogManager.INSTANCE.logException(e);
             }
             os = null;
         }
         if (sock != null)
         {
             try
             {
                 sock.close();
            }
            catch (IOException e)
             {
                 DebugLogManager.INSTANCE.logException(e);
             }
         }
         sock = null;
     }
 
     @Override
     public boolean isConnected()
     {
         return is != null;
     }
 
     @Override
     public boolean connectionPossible()
     {
         return true;
     }
 
     @Override
     public boolean connectionEnabled()
     {
         return true;
     }
 }
