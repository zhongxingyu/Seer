 package sd.controler;
 
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 
 import sd.exceptions.NenhumServidorDisponivelException;
 import sd.exceptions.ObjetoNaoEncontradoException;
 import sd.interfaces.InterfaceControlador;
 import sd.interfaces.InterfaceReplicacao;
 import sd.types.Box;
 
 /**
  * 
  * @author Anderson de França Queiroz <contato (at) andersonq (dot) eti (dot) br
  * @author André Xavier Martinez
  * @author Tiago de França Queiroz <contato (at) tiago (dot) eti (dot) br
  *
  */
 
 @SuppressWarnings("serial")
public class Controler extends UnicastRemoteObject implements InterfaceControlador, InterfaceReplicacao
 {
 
     protected Controler() throws RemoteException
     {
         super();
     }
 
    //=======================Begin of interface Controller=======================
 	@Override
 	public void armazena(String nome, Box obj) throws RemoteException,
 			NenhumServidorDisponivelException {
		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public String procura(String nome) throws RemoteException,
 			NenhumServidorDisponivelException, ObjetoNaoEncontradoException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ArrayList<String> lista() throws RemoteException,
 			NenhumServidorDisponivelException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void apaga(String nome) throws RemoteException,
 			NenhumServidorDisponivelException, ObjetoNaoEncontradoException {
 		// TODO Auto-generated method stub
 		
 	}

	@Override
	public void apaga(Integer arg0) throws RemoteException,
			NenhumServidorDisponivelException, ObjetoNaoEncontradoException {
		// TODO Auto-generated method stub
		
	}
	
	//=======================End of interface Controller=======================

	//=======================Begin of interface Replication=======================
	@Override
	public void replica(Integer arg0, Box arg1) throws RemoteException,
			NenhumServidorDisponivelException {
		// TODO Auto-generated method stub
		
	}
	//=======================End of interface Replication=======================

 }
