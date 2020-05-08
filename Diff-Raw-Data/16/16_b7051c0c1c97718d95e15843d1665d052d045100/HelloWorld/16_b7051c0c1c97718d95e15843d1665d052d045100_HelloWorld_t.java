 package sorcer.ex0;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import sorcer.schema.Schema;
 import sorcer.service.Context;
 
 public interface HelloWorld extends Remote {
 
    //@Schema(HelloWorldContext.class, HelloWorld2Context.class)
 	Context sayHelloWorld(Context context) throws RemoteException;
 
 }
