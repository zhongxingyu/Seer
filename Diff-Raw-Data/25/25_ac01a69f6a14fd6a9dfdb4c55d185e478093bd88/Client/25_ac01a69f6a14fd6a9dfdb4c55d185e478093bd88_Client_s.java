 import java.rmi.*;
 import java.rmi.server.UnicastRemoteObject;
 import java.rmi.registry.*;
 import java.net.*;
 
 public class Client extends UnicastRemoteObject implements Client_itf {
 
     private static Client localptr;
 
     private static Server_itf serv;
 
     private HashMap<int,SharedObject> objects;
 
     public Client() throws RemoteException {
         super();
        serv=null;
         objects = new HashMap<int,SharedObject>();
        
        try {
            serv=(Server_itf)Naming.lookup("Server");
        } catch(Exception e) {
            System.err.println("Client init exception : " + e);
            e.printStackTrace();
        }

        if(serv==null) {
            System.out.println("Server RMI object not found");
        }
     }
 
 
 ///////////////////////////////////////////////////
 //         Interface to be used by applications
 ///////////////////////////////////////////////////
 
     // initialization of the client layer
     public static void init() {
         localptr = new Client();
     }
 	
     // lookup in the name server
     public static SharedObject lookup(String name) {
         int obj_id = serv.lookup(name);
         if(obj_id==-1) {
             return null;
         } else {n
             SharedObject obj = new SharedObject(null,obj_id);
             objects.put(obj_id,obj);
             return obj;
         }
     }		
 	
     // binding in the name server
     public static void register(String name, SharedObject_itf so) {
         serv.register(name,new_id);
     }
 
     // creation of a shared object
     public static SharedObject create(Object o) {
         int new_id = serv.create(o);        
         SharedObject obj = new SharedObject(o,new_id);
         return obj;
     }
 	
 /////////////////////////////////////////////////////////////
 //    Interface to be used by the consistency protocol
 ////////////////////////////////////////////////////////////
 
     // request a read lock from the server
     public static Object lock_read(int id) {
         return serv.lock_read(id,localptr);
     }
 
     // request a write lock from the server
     public static Object lock_write (int id) {
         return serv.lock_write(id,localptr);
     }
 
     // receive a lock reduction request from the server
     public Object reduce_lock(int id) throws java.rmi.RemoteException {
         SharedObject obj = objects.get(id);
         if(obj!=null) {
             obj.reduce_lock();
         }
     }
 
 
     // receive a reader invalidation request from the server
     public void invalidate_reader(int id) throws java.rmi.RemoteException {
         SharedObject obj = objects.get(id);
         if(obj!=null) {
             obj.invalidate_reader();
         }
     }
 
 
     // receive a writer invalidation request from the server
     public Object invalidate_writer(int id) throws java.rmi.RemoteException {
         SharedObject obj = objects.get(id);
         if(obj!=null) {
             obj.invalidate_writer();
         }
     }
 }
