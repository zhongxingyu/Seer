 package system;
 
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import api.Result;
 import api.Task;
 
 /**
  * Defines a computer that can execute a {@link  api.Task  Task}.
  * 
  * @author Manasa Chandrasekhar
  * @author Kowshik Prakasam
  */
 public interface Computer extends Remote {
     /**
      * @param t Task to be executed
      * @return Returns the object returned by the task's execute method
      * @throws java.rmi.RemoteException
      */
    Result<?> execute(Task<?> t) throws RemoteException;
 }
 
 
