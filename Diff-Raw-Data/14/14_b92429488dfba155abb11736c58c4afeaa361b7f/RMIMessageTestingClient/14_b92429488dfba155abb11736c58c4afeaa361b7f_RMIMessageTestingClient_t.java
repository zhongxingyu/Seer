 import java.lang.reflect.*;
 import java.io.*;
 import java.net.*;
 
 class RMIMessageTestingClient
 {
     private static String hostname = "unix12.andrew.cmu.edu";
     private static String methodName = "printThis";
     private static int port = 15444;
 
     public static void main(String[] args)
     {
         try{
             String s = "The sent message is cool";
 
             Class theClass = Class.forName("PrintingObject");
 
            RemoteObjectReference ref = new RemoteObjectReference(InetAddress.getByName(hostname), port, "", theClass.toString());
            
             Class[] argClasses = {String.class, int.class};
 
             Method method = theClass.getMethod(methodName, argClasses);
 
             Object[] remoteArgs = {s, 5};
             RMIMessage theMessage = new RMIMessage(ref, method, remoteArgs);
             Object returnObj = theMessage.getReturn();
 
             if ((returnObj != null) && (returnObj.getClass() == String.class))
             {
                 System.out.println(returnObj);
                 if (returnObj == "This is the intended return value")
                 {
                     System.out.println("RMI Message normal case test passed");
                 }
             }
 
         } catch (Throwable e) {
             e.printStackTrace();
         }
     }
 }
