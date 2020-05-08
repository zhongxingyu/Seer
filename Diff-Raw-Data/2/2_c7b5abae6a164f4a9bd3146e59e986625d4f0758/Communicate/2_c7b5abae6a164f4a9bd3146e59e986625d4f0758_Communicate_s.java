 import java.net.*;
 import java.io.*;
 import java.util.*;
 
 public class Communicate
 {
     private static String registryURL = "unix12.andrew.cmu.edu";
     private static int registryPort = 15044;
     private static int objectPort = 15880;
     public static ProxyDispatcher pd;
     public static ProxyThread p;
 
     static {
 	pd = new ProxyDispatcher(objectPort);
	p = new ProxyThread(pd);
	p.start();
     }
     
     public static class ProxyThread extends Thread {
 	ProxyDispatcher pd;
 	public ProxyThread(ProxyDispatcher p) {
 	    pd = p;
 	}
 	public void run() {
 	    try{
 		ServerSocket serverSock = new ServerSocket(pd.port,pd.BACKLOG,pd.adr);
 		Socket soc = serverSock.accept();
 		System.out.println("client connected");
 
 		InputStream inStream = soc.getInputStream();
 		pd.in = new ObjectInputStream(inStream);
 	
 		OutputStream outStream = soc.getOutputStream();
 		pd.out = new ObjectOutputStream(outStream);
 	    
 		pd.executeMessage();
 	    } catch(Exception e) {
 		e.printStackTrace();
 	    }
 	}
     }
 
     public static Remote440 lookup(String key)
     {
         //Open registry connection
         
         Object readObject = null;
         Class<?> readClass = null;
         try {
             
             try {
                 Socket sock = new Socket(registryURL, registryPort);
 
                 OutputStream out = sock.getOutputStream();
                 ObjectOutputStream objOut = new ObjectOutputStream(out);
 
                 InputStream in = sock.getInputStream();
                 ObjectInputStream objIn = new ObjectInputStream(in);
 
                 //Retrieve the ROR from registry
                 objOut.writeObject(key);
                 
                 readObject = objIn.readObject();
                 if (readObject == null)
                 {
                     System.out.println("Read in null");
                     return null;
                 }
                 readClass = readObject.getClass();
             
             } catch (IOException e)
             {
                 System.out.println("Failed to interface with registry server");
                 e.printStackTrace();
                 return null;
             }
 
             if (readClass == RemoteObjectReference.class)
             {
                 return ((RemoteObjectReference)readObject).localize();
             }
 
             else
             {
                 System.out.println("Returned object of incompatible class " + readClass);
                 return null;
             }
         } catch (Exception e)
         {
             e.printStackTrace();
             return null;
         }
     }
 
     public static void rebind(String key, Remote440 object)
     {
 	pd.addObj(key,object);
 	try{
             //Contact RMIRegistry and give it this remote object, returning a ROR
             InetAddress myAddress = InetAddress.getLocalHost();
             String className = object.getClass().getName();
             RemoteObjectReference ror = new 
                 RemoteObjectReference(myAddress, objectPort, key, className);
             
             Socket sock = new Socket(registryURL, registryPort);
 
             OutputStream out = sock.getOutputStream();
             ObjectOutputStream objOut = new ObjectOutputStream(out);
 
             //This isn't use for now, can be removed, maybe
             InputStream in = sock.getInputStream();
             ObjectInputStream objIn = new ObjectInputStream(in);
             //Above can be removed
 
             objOut.writeObject(ror);
             
             sock.close();
         } catch (Exception e)
         {
             e.printStackTrace();
         }
     }
 }
