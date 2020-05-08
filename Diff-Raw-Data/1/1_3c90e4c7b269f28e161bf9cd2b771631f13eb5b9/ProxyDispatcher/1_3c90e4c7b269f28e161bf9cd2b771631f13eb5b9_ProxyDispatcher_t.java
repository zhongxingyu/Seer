 import java.util.*;
 import java.lang.*;
 import java.lang.reflect.*;
 import java.io.*;
 import java.net.*;
 
 public class ProxyDispatcher {
     public HashMap<String,Object> objList;
     public ObjectInputStream in;
     public ObjectOutputStream out;
     
 
     public ProxyDispatcher(int p, InetAddress a) {
 	try {
 	    Socket soc = new Socket(a, p);
 
 	    InputStream inStream = soc.getInputStream();
 	    in = new ObjectInputStream(inStream);
 	
 	    OutputStream outStream = soc.getOutputStream();
 	    out = new ObjectOutputStream(outStream);
         } catch(Exception e) {
 	    e.printStackTrace();
 	}
 
 	objList = new HashMap<String,Object>();
     }
 
     public void addObj(String name,Object o) {
 	objList.put(name,o);
     }
     
     public void executeMessage(){
 	Object o = null;
 	RMIMessage msg;
 	Method m;
 	Object callee; 
 	Object returnValue = null;
 	try {
 	    o = in.readObject();
 	    if(o.getClass().getName().equals("RMIMessage")) {
 		msg = (RMIMessage)o;
 		m = msg.getMethod();
 		callee = objList.get(msg.remoteObject.name);
 		
         // Handles any exceptions thrown by this object and fowards
         // them to the client
         try{
             returnValue = m.invoke(callee, msg.args);
 		} catch (Exception e)
         {
             Throwable cause = e;
             if (e.getClass() == InvocationTargetException.class)
                 cause = e.getCause();
 
             //Returns a wrapped Throwable to clientside
             returnValue = new RMIException(cause);
         }
         
         out.writeObject(returnValue);
 		out.flush();
 	    }
 	} catch(Exception e) {
 	    e.printStackTrace();
 	}
 	
     }		
 }	
