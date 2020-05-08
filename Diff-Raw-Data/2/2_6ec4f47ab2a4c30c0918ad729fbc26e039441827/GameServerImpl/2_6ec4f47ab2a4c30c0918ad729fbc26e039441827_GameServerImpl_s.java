 package Server.gameModule;
 
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 
 import Remote.GameServer;
 import Remote.IGameTable;
 
 public class GameServerImpl extends UnicastRemoteObject  implements GameServer  {
 	
 	private ArrayList<IGameTable> pokerTables;
 	
 	public GameServerImpl()  throws RemoteException{
 		super();
 		pokerTables = new ArrayList<IGameTable>();
 	}
 	
 	@Override
 	public ArrayList<IGameTable> getAllTables() throws RemoteException{
 		ArrayList<IGameTable> tables = new ArrayList<IGameTable>();
 		for(IGameTable t: pokerTables){
 			if(t.getPlayers().size() < 1){
 				continue;
 			}
 			
 			tables.add(t);
 			
 		}
		return tables;
 		this.pokerTables=tables;
 		return pokerTables;
 	}
 	
 
 	@Override
 	public IGameTable createTable(int ante, int hostId,
 			ArrayList<Integer> playersId, double bringIn, String suggestedName)
 			throws RemoteException {
 		IGameTable table = new GameTable(ante, hostId, playersId,  bringIn, suggestedName);
 		pokerTables.add(table);
 		return table;
 	}
 
 	@Override
 	public IGameTable getTable(String name) throws RemoteException {
 		for (IGameTable t: pokerTables){
 			try {
 				if(t.getName().equals(name)){
 					return t;
 				}
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 }
