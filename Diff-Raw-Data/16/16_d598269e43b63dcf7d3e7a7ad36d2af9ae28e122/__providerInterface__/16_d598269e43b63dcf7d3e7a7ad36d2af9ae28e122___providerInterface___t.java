package ${package};
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import sorcer.service.Context;
 
 public interface ${providerInterface} extends Remote {
 	
 	Context sayHelloWorld(Context context) throws RemoteException;
 
 }
