 package sd.server;
 
 import java.rmi.Naming;
 
 import sd.interfaces.InterfaceAcesso;
 import sd.interfaces.InterfaceReplicacao;
 
 /**
  * 
  * @author Anderson de França Queiroz <contato (at) andersonq (dot) eti (dot) br
  * @author André Xavier Martinez
  * @author Tiago de França Queiroz <contato (at) tiago (dot) eti (dot) br
  *
  */
 
 public class RunServer
 {
     public static void main(String[] args)
     {
         InterfaceAcesso ia;
         InterfaceReplicacao ir;
         Server s = new Server();
         
         try
         {
             ia = s;
             ir = s;
             
            Naming.rebind("rmi://localhostAcesso", ia);
            Naming.rebind("rmi://localhostReplica", ir);
         }
         catch(Exception e)
         {
             e.printStackTrace();
            
         }
     }

 }
