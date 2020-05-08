 package client;
 
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import org.apache.log4j.Logger;
 
 import data.IComment;
 import data.IIdea;
 import data.IStorable;
 import functions.IGame;
 import functions.IGameDescription;
 import functions.IGameServer;
 
 /**
  * <p>
  * Functional core of a client (player).</br>
  * Receives requests from a GUI or another interface. </br>
  * also receives updates (events) from a server.</br>
  * </p>
  * <p>if the UI wants so, the core can track remaining tokens via the use of spendTokens().</p>
  * <p>
  * On connection, creates a LocalCopyOfGame from server, to serv as frontend.
  * </p>
  * <p>When disconnected, it keeps a reference to the game, for possible later use</p>
  * @author Samuel Thiriot, Pierre Marques
  *
  */
 public abstract class ClientCore implements IClientCore {
 
 	private Logger logger = Logger.getLogger("client.core");
 	
 	private IGame game = null;
 	private boolean connected = false;
 
 	private int playerId=IStorable.notAnId;
 //TODO: definir playerId pour les clients autre que GUI
 	
 	private int nbRemainingTokens = 0;
 	
 	public ClientCore() {
 		logger.debug("client core created.");
 	}
 	
 	public int getPlayerId()
 	{
 		return playerId;
 	}
 	
 	public void setPlayerId(int id)
 	{
 		playerId=id;
 	}
 	
 	public Map<Integer, Integer> getCurrentIdeasTokens() {
 		
 		Map<Integer, Integer> res = new HashMap<Integer, Integer>();
 		
 		try {
 			for (IIdea idea: game.getAllIdeas()) {
 							res.put(idea.getUniqueId(),0);						
 			}
 			for (IComment comment: game.getAllComments()) {
 				if ((comment.getPlayerId()==this.getPlayerId())&
 						(comment.getTokensCount()!=0))
 				{
 					if (comment.getTokensCount()>0)
 					{
 						int ideaId=game.findIdeaFromComment(comment.getUniqueId());
 						res.put(ideaId, res.get(ideaId)+comment.getTokensCount());
 					}
 					if (comment.getTokensCount()<0)
 					{
 						int ideaId=game.findIdeaFromComment(comment.getUniqueId());
 						res.put(ideaId, res.get(ideaId)+comment.getTokensCount());
 					}
 				}
 			}
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return res;
 		
 	}
 	
 
 	
 	public int getRemainingTokens(){
 		return nbRemainingTokens;
 	}
 
 	public void spendTokens(int tokens) throws IllegalStateException{
 		if(nbRemainingTokens<tokens) throw new IllegalStateException("not enough tokens available");
 		nbRemainingTokens -= tokens;
 	}
 	
 	/* (non-Javadoc)
 	 * @see client.IClientCore#isConnected()
 	 */
 	@Override
 	public boolean isConnected(){
 		return connected;
 	}
 	
 	/* (non-Javadoc)
 	 * @see client.IClientCore#fetchServer(java.lang.String)
 	 */
 	@Override
 	public final Collection<IGameDescription> fetchServer(String serverBindName){
 		try {
 			logger.debug("lookup for fetched server "+serverBindName+"...");
 			IGameServer server = (IGameServer) Naming.lookup(serverBindName);
 
 			logger.debug("listing open games...");
 			return server.getOpenGames();
 
 		} catch (MalformedURLException e) {
 			throw new IllegalArgumentException("invalid server url", e);
 		} catch (RemoteException e) {
 			throw new IllegalArgumentException("unable to lookup server", e);
 		} catch (NotBoundException e) {
 			throw new IllegalArgumentException("no server there", e);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see client.IClientCore#connect(java.lang.String)
 	 */
 	@Override
 	public final void connectToGame(String gameBindName) throws MalformedURLException, RemoteException, NotBoundException, UnknownHostException {
 		logger.info("client connecting to "+gameBindName+"...");
 		
 		IGame remoteGame = (IGame) Naming.lookup(gameBindName);
 
 		logger.debug("getting local copy of the game...");
 		this.game = LocalCopyOfGame.getOrCreateLocalCopy(remoteGame);
		logger.debug("correction of the link bug...");
 
 		logger.debug("registring as listener...");
 		
 		this.game.addListener(this);
 		
 		nbRemainingTokens = this.game.getMaxTokensByPlayer();
 		
 		connected=true;
 		logger.info("client connected.");
 	}
 	
 	/* (non-Javadoc)
 	 * @see client.IClientCore#disconnectFromGame()
 	 */
 	@Override
 	public final void disconnectFromGame() {
 		if (connected) {
 			try {
 				logger.info("disconnecting from game "+game.getDescription().getName()+"...");
 				game.removeListener(this);
 				logger.info("done");
 			} catch (RemoteException e) {
 				logger.error("Unable to disconnect from game",e);
 			} finally {
 				connected=false;
 			}
 		}
 	}
 
 	@Override
 	public IGame getGame(){
 		return game;
 	}
 
 	/* (non-Javadoc)
 	 * @see client.IClientCore#displayIdeaComments(int, client.IClientCore.TreeExplorator)
 	 */
 	@Override
 	public void displayIdeaComments(int ideaId, TreeExplorator worker) throws RemoteException {
 		if( !isConnected() || worker==null ) return;
 		
 		DefaultMutableTreeNode root = game.getIdeaComments(ideaId);
 		if(root==null) return;
 		
 		worker.start( (String) ((IStorable)root.getUserObject()).getShortName());
 		worker.work(root, 0);
 //		worker.end( (String) root.getUserObject());
 		worker.end( (String) ((IStorable)root.getUserObject()).getShortName());
 
 	}
 
 }
