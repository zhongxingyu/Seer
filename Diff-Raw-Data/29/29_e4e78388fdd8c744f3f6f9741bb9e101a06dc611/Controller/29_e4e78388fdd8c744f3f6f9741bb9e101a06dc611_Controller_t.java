 package sd.controler;
 
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.server.RemoteServer;
 import java.rmi.server.ServerNotActiveException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.Vector;
 
 import sd.exceptions.NenhumServidorDisponivelException;
 import sd.exceptions.ObjetoJaExisteException;
 import sd.exceptions.ObjetoNaoEncontradoException;
 import sd.interfaces.InterfaceControlador;
 import sd.interfaces.InterfaceControllerServer;
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
 public class Controller extends UnicastRemoteObject 
 						implements InterfaceControlador, InterfaceControllerServer
 {
 	/** The servers */
	private Hashtable<Integer, InterfaceReplicacao> sev;
 	/** Keep the pair service-server_position_in_vector_server.
 	 *  It will be used to remove inactive servers
 	 */
 	private Hashtable<String, Integer> services;
 	/** A 'list' of all stored objects */
 	private Hashtable<String, Integer> objects;
 	/** The next server to be used by clients */
 	int nextserver;
 	/** The next object ID */
 	private static int ID;
 	/** The next server ID */
 	private static int serverID;
 
     protected Controller() throws RemoteException
     {
     	//Calls super class
         super();
 
         //Initialising variables
        this.sev = new Hashtable<Integer, InterfaceReplicacao>();
         this.services = new Hashtable<String, Integer>();
         this.objects = new Hashtable<String, Integer>();
         this.nextserver = 0;
         Controller.ID = 0;
         Controller.serverID = 0;
     }
 
     //===================Begin interface Controller===================
 
 	/**
 	 * Stores a object in all servers
 	 * @param nome The name of the object
 	 * @param obj the object to be stored
 	 * @throws RemoteException
 	 * @throws NenhumServidorDisponivelException There are NOT any available server
 	 */
 	public void armazena(String nome, Box obj) throws RemoteException,
 			NenhumServidorDisponivelException, ObjetoJaExisteException
 	{
 		Integer id;
 		id = objects.get(nome);
 		if(id != null)
 		    throw new ObjetoJaExisteException("\"" + nome + "\""+ " already stored!");
 		id = new Integer(ID++);
 		//Put the object in the list
 		objects.put(nome, new Integer(id));
 
 		//Store object in all servers
 		//TODO TEST IT!!!!
		
		//Getting a way to iterate in servers
		Collection<InterfaceReplicacao> tmp = sev.values();
		for(InterfaceReplicacao ir: tmp)
 		{
 		    boolean alive = false;
 		    alive = ir.areYouAlive();
 		    if(!alive)
 		        removeDeadServer(ir);
 			ir.replica(id, obj);
 			//DEBUG
 			System.out.println("Stored object " + obj + ", id " + id + " in server " + ir.getId());
 		}
 	}
 
 	/**
 	 * Look for a object
 	 * @param nome name of the object
 	 * @return A string with the tuple {object_id, server, service}, the syntax is:
 	 *  ID@rmi://SERVER/SERVICE, example:
 	 *  13@rmi://computer1/ServerMirror
 	 * @throws RemoteException
 	 * @throws NenhumServidorDisponivelException There are NOT any available server
 	 * @throws ObjetoNaoEncontradoException Object not found
 	 */
 	public String procura(String nome) throws RemoteException,
 			NenhumServidorDisponivelException, ObjetoNaoEncontradoException 
 	{
 		//Looking for the object
 		Integer id = objects.get(nome);
 		//InterfaceReplicacao s = servers.get(id);
 
 		//If there is not a object named nome, it throws exception
 		if(id == null)
 		{
 			throw new ObjetoNaoEncontradoException(nome);
 		}
 		
 		InterfaceReplicacao nserver = nextServer();
 		
 		return String.format("%d@rmi://%s/%s", id.intValue(), nserver.getAddress(), String.format("Acesso%d", nserver.getId()));
 	}
 
 	/**
 	 * List all stored objects
 	 * TODO define syntax to string
 	 * @return a string with all stored objects
 	 * @throws RemoteException
 	 * @throws NenhumServidorDisponivelException There are NOT any available server
 	 */
 	public ArrayList<String> lista() throws RemoteException,
 			NenhumServidorDisponivelException
 	{
 		ArrayList<String> list = new ArrayList<String>();
 
 		Set<String> objs = objects.keySet();
 
 		//Add all object names into 'list'
 		for(String s: objs)
 		{
 			list.add(s);
 		}
 
 		return list;
 	}
 	
 	/**
 	 * Debug for client-controller connection
 	 * @return uma string com a mesma mensagem que foi impressa no controlador
 	 * @throws RemoteException
 	 */
 	public String Connection_cliente_OK() throws RemoteException
 	{
 		String s ="Conexão Cliente-Controlador OK";
 		System.out.println(s);
 		return s;
 	}
 	
 	/**
 	 * Delete an object
 	 * @param nome the name of the object to be deleted
 	 * @throws RemoteException
 	 * @throws NenhumServidorDisponivelException There are NOT any available server
 	 * @throws ObjetoNaoEncontradoException Object not found
 	 */
 	public void intControladorApaga(String nome) throws RemoteException,
 			NenhumServidorDisponivelException, ObjetoNaoEncontradoException
 	{
 		//Delete object to the object list
 		Integer id = objects.remove(nome);
 
 		//If there is not a object named nome, it throws exception
 		if(id == null)
 			throw new ObjetoNaoEncontradoException(nome);
 
		//Getting a way to iterate in servers
		Collection<InterfaceReplicacao> tmp = sev.values();
 		//Delete the object from each server
		for(InterfaceReplicacao s: tmp)
 		{
 			s.intReplicacaoApaga(id);
 		}
 	}
 	
 	public void reportFail(String service) throws RemoteException
 	{
 		int pos = services.get(service);
 
 		try
 		{
 			System.out.println("Client " + RemoteServer.getClientHost() + " reported down of service " + 
 								service + " removing it from servers list");
 		}
 		catch (ServerNotActiveException e)
 		{
 			System.err.println("Controller.reportFail catched ServerNotActiveException");
 			e.printStackTrace();
 		}
 		
 		//Remove the inactive server from servers list
 		servers.remove(pos);
 	}
 	//===================End interface Controller=====================
 
 
 	//===================Begin interface ControllerServer===================
 
 	/**
 	 * Ask to controller a ID to compose the service name, like it: serviceID
 	 * @return the ID of this server or -1 if its fail
 	 */
 	public int beforeBind() throws RemoteException
 	{
 		/*
 		 * The ID to this server, it will compose the serviceName, like this:
 		 * ServiceID
 		 */
 		int nid = nextServerID();
 		try
 		{
 			System.out.println("Server " + RemoteServer.getClientHost() + " asked a ID, sending " + nid);
 		}
 		catch (ServerNotActiveException e)
 		{
 			System.err.println("Controller.beforeBind() catch ServerNotActiveException");
 			e.printStackTrace();
 		}
 		
 		return nid;
 	}
 
 	/**
 	 * Send to controller the server address and service name
 	 * @param addres the server address
 	 * @param serviceAcesso name registered to service Acesso
 	 * @param serviceReplicacao name registered to service Replicacao
 	 * @return true if the controller added the server or false other wise
 	 * @throws RemoteException
 	 */
 	public boolean registryServer(String address, int id) throws RemoteException
 	{
 		/* Was the server registered?
 		 * If something fails it will be set to 'false'
 		 */
 		boolean registered = true;
 		//New server
 		InterfaceReplicacao newserver = null;
 		
 		//Looking for the new server
 		try
 		{
 			newserver = (InterfaceReplicacao) Naming.lookup(String.format("rmi://%s/Replica%d", address, id));
 		}
 		catch (MalformedURLException e)
 		{
 			registered = false;
 			System.err.println("MalformedURLException");
 			e.printStackTrace();
 		}
 		catch(NotBoundException e)
 		{
 			registered = false;
 			System.err.println("NotBoundException");
 			e.printStackTrace();
 		}
 		
 		/*
 		 * Get a server to provide all objects to the new server
 		 * If there is not any server, this new server is the first
 		 */
 		InterfaceReplicacao nserver = null;
 		try 
 		{
 			//Get a server
 			nserver = nextServer();
 			
 			String server =  String.format("rmi://%s/%s", nserver.getAddress(), String.format("Acesso%d", nserver.getId()));
 			
 			//Command the new server copy all objects from another server
 			registered = newserver.replicaAll(objects, server);
 		} 
 		catch (NenhumServidorDisponivelException e) 
 		{
 			System.out.println("First server connecting...");
 		}
 
 		//Putting the new server with all servers
		try {sev.put(new Integer(id), newserver);}
		//Some thing got wrong!
		catch(NullPointerException e) {registered = false;}
				
 		System.out.println("New server connected addres: " + 
 							address + ", id: " + id);
 		return registered;
 	}
 	
 	//===================End interface ControllerServer=====================
 
 	/**
 	 * Gets the server to be informed to the client
 	 * @return
 	 * @throws RemoteException 
 	 * @throws NenhumServidorDisponivelException 
 	 */
 	/**TODO I'm getting and ArrayOutOfBounds exception here when a Client needs to use it (indirectly, that is)
 	 * Needs a review*/
 	private InterfaceReplicacao nextServer() throws RemoteException, NenhumServidorDisponivelException
 	{
 		//It will be return the next server
 		InterfaceReplicacao ir = null;
 	    boolean alive = false;
 	    ArrayList<Integer> To_remove = new ArrayList<Integer>();
 		if(servers.size() == 0)
 		{
 			throw new NenhumServidorDisponivelException();
 		}
 
 		for(int i = 0; i < servers.size(); i++)
 		{
 		    try
 		    {
 		    	/*This part seens ok, it will eventually check every server on the list so, no change */
 		        alive = servers.get((nextserver + i) % servers.size()).areYouAlive();
 		        /*If you found an alive server, get it's index and break the search*/
 		        if(alive)
 		        {
 		        	nextserver=(nextserver + i) % servers.size();
 		        	ir = servers.get(nextserver-1);
 		        	break;
 		        }      
 		    }
 		    catch (RemoteException e)
 		    {
 		    	/*Add the index of the server that is down for later removal*/
 		    	To_remove.add((Integer)(nextserver + i) % servers.size());
 		    }
 		}
 
 		/*If there is someone to remove, try to safely remove servers with highest index first, so it won't screw indexes*/
 		if(!To_remove.isEmpty())
 			for(Integer ids: To_remove)
 				removeDeadServer(ids);
 		
 		/*If no one is alive, we got some serious problem*/
 		if(!alive)
 		    throw new NenhumServidorDisponivelException();
 		
 		/*This makes sure that on next search it will start at the next server*/
 		nextserver++;
 		
 		/*Finally, it returns the server it was found*/
 		return ir;
 	}
 	
 	/**
 	 * Returns the next server ID
 	 * @return the next server ID
 	 */
 	private int nextServerID()
 	{
 		//TODO verify if 'return Controller.serverID' works
 		int id = Controller.serverID;
 		Controller.serverID = Controller.serverID + 1;
 
 		return id;
 	}
 
 	private void removeDeadServer(int server)
 	{
 		
 	    services.remove(servers.get(server));
 	    servers.remove(server);
 	    System.err.printf("Server %d removed!\n", nextserver% servers.size());
 	}
 
 	private void removeDeadServer(InterfaceReplicacao server)
 	{
 	    services.remove(server);
 	    servers.remove(server);
 	    System.err.printf("Server %d removed!\n", nextserver% servers.size());
 	}
 }
