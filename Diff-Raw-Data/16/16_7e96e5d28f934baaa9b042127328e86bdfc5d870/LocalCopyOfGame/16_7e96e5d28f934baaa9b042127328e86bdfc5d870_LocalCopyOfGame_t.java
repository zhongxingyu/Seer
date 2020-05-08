 package client;
 
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.apache.log4j.Logger;
 
 import util.Pair;
 import data.CommentValence;
 import data.IComment;
 import data.IIdea;
 import data.IItem;
 import data.IPlayer;
 import errors.AlreadyExistsException;
 import errors.TooLateException;
 import events.GameObjectEvent;
 import events.IEventListener;
 import events.LinkEvent;
 import events.PlayerEvent;
 import events.RemoteListener;
 import events.UncoupledListener;
 import functions.AbstractGame;
 import functions.IGame;
 
 /**
  * Stores locally a copy of a distant whiteboard.
  * It simply listens for the server copy and updates data here.
  *  
  * TODO enable full sync
  * 
  * @author Samuel Thiriot
  *
  */
 public final class LocalCopyOfGame extends AbstractGame implements IEventListener {
 
 	private static final long serialVersionUID = 1L;
 
 	private IGame distantGame;
 	private IEventListener myListener;
 	
 	private int listenerCount=0;
 	private static Logger logger = Logger.getLogger("innovnation.client.LocalCopyOfGame");
 
 	private LocalCopyOfGame(IGame game) throws RemoteException, UnknownHostException {
 		super(game.getDescription());
 		logger.debug("connecting copy...");
 		this.distantGame = game;
 		logger.debug("listening...");
 		myListener=new RemoteListener(new UncoupledListener(this));
 		this.distantGame.addListener(myListener);
 		logger.debug("done");
 	}
 
 	//IGame implementation
 	@Override
 	protected void addListenerAction() {
 		listenerCount++;
 	}
 	
 	@Override
 	protected void removeListenerAction() {
 		listenerCount--;
 		if(listenerCount==0){
 			//gonna die
 			logger.debug("disconnecting from server...");
 			try {
 				distantGame.removeListener(myListener);
 				logger.debug("done");
 			} catch (RemoteException e) {
 				logger.error("failed");
 			}
 		}
 	}
 	
 	
 	@Override
 	public int addItem(int authorId, String itemName, String itemDescription)
 	throws RemoteException {
 		return distantGame.addItem(authorId, itemName, itemDescription);
 	}
 
 	@Override
 	public int addIdea(int playerId, String ideaName, String ideaDesc, Collection<Integer> itemsIds,
 			Collection<Integer> parentIdeasIds)
 	throws AlreadyExistsException, TooLateException, RemoteException {
 		return distantGame.addIdea(playerId, ideaName, ideaDesc, itemsIds, parentIdeasIds);
 	}
 
 	@Override
 	public void makeIdeaParentOf(int authorId, int parentId,
 			Collection<Integer> ideasIds)
 	throws TooLateException, RemoteException {
 		distantGame.makeIdeaParentOf(authorId, parentId, ideasIds);
 	}
 
 	@Override
 	public void makeIdeaChildOf(int authorId, int childId,
 			Collection<Integer> ideasIds)
 	throws TooLateException, RemoteException {
 		distantGame.makeIdeaChildOf(authorId, childId, ideasIds);
 	}
 
 	@Override
 	public int commentIdea(int playerId, int ideaId, String text, int tokens, CommentValence valence)
 			throws RemoteException {
 		return distantGame.commentIdea(playerId, ideaId, text, tokens, valence);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see functions.IGame#commentItem(int, int, java.lang.String)
 	 */
 	@Override
 	public int commentItem(int playerId, int itemId, String text)
 			throws RemoteException {
 		return distantGame.commentItem(playerId, itemId, text);
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see functions.IGame#answerComment(int, int, java.lang.String)
 	 */
 	@Override
 	public int answerComment(int playerId, int commentId, String text, int tokens)
 			throws RemoteException {
 		return distantGame.answerComment(playerId, commentId, text, tokens);
 	}
 	
 
 	@Override
 	public int addPlayer(String playerName) throws RemoteException {
 		return distantGame.addPlayer(playerName);
 	}
 	
 	@Override
 	public int addPlayer(String playerName, String avatar)
 			throws RemoteException {
 		return distantGame.addPlayer(playerName, avatar);
 	}
 
 	@Override
 	public void removePlayer(int playerId) throws RemoteException {
 		distantGame.removePlayer(playerId);
 
 	}
 
 	/*
 	 * listening
 	 * when an event comes from the server, local data should change
 	 */
 
 	@Override
 	public void ItemCreated(GameObjectEvent e) throws RemoteException {
 
 		int idItem = e.getObjectId();
 		IItem retrievedItem = distantGame.getItem(idItem);
 
 		logger.debug("novel item created on the server ("+retrievedItem+"), updating local info...");
 
 		int idNovelItem = injectItem(retrievedItem);
 		IItem novelItem = getItem(idNovelItem);
 		logger.debug("local copy of this item created  ("+novelItem+")");
 
 		//transmission de l'événement aux enfants
 		logger.debug("sending event...");
 		fireItemCreatedEvent(e);
 	}
 
 	@Override
 	public void IdeaCreated(GameObjectEvent e) throws RemoteException {
 
 
 		int idIdea = e.getObjectId();
 		IIdea retrievedIdea = distantGame.getIdea(idIdea);
 
 		logger.debug("novel idea created on the server ("+retrievedIdea+"), updating local info...");
 
 		int idNovelIdea = injectIdea(retrievedIdea);
 
 		LinkedList<Integer> parents = distantGame.getIdeaParentIds(idIdea);
 		for(int id : parents){
 			linkIdeas(id, idIdea);
 		}
 		
 		IIdea novelIdea = getIdea(idNovelIdea);
 		logger.debug("novel idea created on local server ("+novelIdea+")");
 
 		//transmission de l'événement aux enfants
 		logger.debug("sending event...");
 		fireIdeaCreatedEvent(e);
 	}
 
 	@Override
 	public void IdeaLinkCreated(LinkEvent e) throws RemoteException {
 
 		logger.debug("novel links between idea created on the server, updating local info...");
 
 		for(Pair<Integer, Integer> i : e){
 			linkIdeas(i.a, i.b);
 		}
 		
 		logger.debug("novel links created on local server");
 		
 		logger.debug("sending event...");
 		fireIdeaLinkCreatedEvent(e);
 	}
 	
 
 	@Override
 	public void ideaCommentCreated(GameObjectEvent e) throws RemoteException {
 		int commentId = e.getObjectId();
 		//find what is commented
 		
 		IComment retrievedComment = distantGame.getComment(commentId);
 
 		logger.debug("novel comment created on the server ("+retrievedComment+"), updating local info...");
 
 		int idNovelComment = injectIdeaComment(retrievedComment);
 		IComment novelComment = getComment(idNovelComment);
 
 		logger.debug("local copy of this comment created  ("+novelComment+")");
 
 		//transmission de l'événement aux enfants
 		logger.debug("sending event...");
 		fireCommentCreatedEvent(e);
 	}
 
 	@Override
 	public void playerJoined(PlayerEvent e) throws RemoteException {
 		IPlayer p = distantGame.getPlayer(e.getPlayerId());
 		logger.info("player joined: "+p);
 		injectPlayer(p);
 		logger.debug("sending event...");
 		firePlayerJoinedEvent(e);
 	}
 
 	@Override
 	public void playerLeft(PlayerEvent e) throws RemoteException {
 		IPlayer p = getPlayer(e.getPlayerId());
 		logger.info("player left: "+p);
 		ejectPlayer(e.getPlayerId());
 		logger.debug("sending event...");
 		firePlayerLeftEvent(e);
 	}
 
 	/* (non-Javadoc)
 	 * @see events.IEventListener#endOfGame()
 	 */
 	@Override
 	public void endOfGame() throws RemoteException {
 		logger.info("server has decided to end this game!");
 		//endGame();
 		logger.debug("sending event...");
 		fireEndOfGame();
 	}
 
 	
 	//synchro
 
 	/**
 	 * synchronize with the remote server, so that actual content is reflected.
 	 */
 	public void resync() {
 		// TODO !!!
 		try {
 			LinkedList<Integer> localIds;
 			
 			// synchro players
 			logger.debug("synchronizing players...");
 			localIds = getAllPlayersIds();
 			
 			for (IPlayer current : distantGame.getAllPlayers()) {
 
 				logger.debug("player retrieved from the server : "+current);
 
 				int novelId = current.getUniqueId();
 
 				if ( !localIds.contains(novelId) ) {
 					novelId = injectPlayer(current);
 					logger.debug("player injected as : "+getPlayer(novelId));
 				} else {
 					logger.debug("player already exists as : "+getPlayer(novelId));
 				}
 
 				localIds.removeFirstOccurrence(novelId);
 				
 			}
 
 			logger.debug("killing misexisting players...");
 			for (Integer currentId : localIds) {
 				IPlayer current = ejectPlayer(currentId);
 				logger.debug("player removed from local server: "+current);
 				
 				//TODO how do I release the potential observer?
 				//firePlayerLeftEvent(new PlayerEvent(currentId));
 			}		
 
 			// synchro items
 			logger.debug("synchronizing items...");
 
 			localIds = getAllItemsIds();
 
 			for (IItem current : distantGame.getAllItems()) {
 
 
 				logger.debug("item retrieved from the server : "+current);
 
 				int idNovelItem = current.getUniqueId();
 
 				if (!itemExists(idNovelItem)) {
 					idNovelItem = injectItem(current);	
 				}
 
 				localIds.removeFirstOccurrence(idNovelItem);
 
 
 			}
 
 			for (Integer currentItemId : localIds) {
 				IItem currentItem = ejectItem(currentItemId);
 				logger.debug("item removed from local server: "+currentItem);
 			}			
 
 			// synchro ideas
 			logger.debug("synchronizing ideas...");
 			for (IIdea current: distantGame.getAllIdeas()) {
 				
 				logger.debug("idea retrieved from the server : "+current);
 
 				int idNovel= current.getUniqueId();
 
 				if (!ideaExists(idNovel)) {
 					idNovel = injectIdea(current);
 					logger.debug("idea injected as : "+getIdea(idNovel));
 				} else {
 					logger.debug("idea already present as : "+getIdea(idNovel));
 				}
 			}
 			
			// synchro links
			logger.debug("synchronizing links...");
			for (IIdea current: distantGame.getAllIdeas()) {
				
				logger.debug("idea retrieved from the server : "+current);

//				current.recreateLinks();
				int idNovel= current.getUniqueId();

				if (!ideaExists(idNovel)) {
					idNovel = injectIdea(current);
					logger.debug("idea injected as : "+getIdea(idNovel));
				} else {
					logger.debug("idea already present as : "+getIdea(idNovel));
				}
			}
 
 			// synchro comments
 			logger.debug("synchronizing comments...");
 			for (IComment current: distantGame.getAllComments()) {
 				
 				logger.debug("comment retrieved from the server : "+current);
 
 				int idNovel= current.getUniqueId();
 
 				if (!commentExists(idNovel)) {
 					idNovel = injectIdeaComment(current);
 					for(int i : distantGame.getIdeaParentIds(idNovel)){
 						linkIdeas(i, idNovel);
 					}
 					logger.debug("comment injected as : "+getComment(idNovel));
 				} else {
 					logger.debug("comment already present as : "+getComment(idNovel));
 				}
 			}
 			
 			//finished... ouf!
 			logger.debug("end of synchro.");
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 
 	}
 
 	private static LocalCopyOfGame singletonInstance = null;
 
 	/**
 	 * Returns the local singleton for accessing to the game.
 	 * Warning, this has many drawbacks, notably the problem of 
 	 * limiting the total number of games by machine to one.
 	 * @return
 	 */
 	public static LocalCopyOfGame getLocalCopy() {
 
 		if (singletonInstance == null)
 			logger.error("someone asked for the local singleton of whiteboard, but there is no local copy");
 
 		return singletonInstance;
 	}
 
 	/**
 	 * A déporter sur un client whiteboard uniquement
 	 * @return
 	 */
 	public static LocalCopyOfGame getOrCreateLocalCopy(IGame game) throws RemoteException, UnknownHostException {
 
 		if (singletonInstance == null){
 			singletonInstance = new LocalCopyOfGame(game);
 
 			logger.debug("resync-ing...");
 			singletonInstance.resync();
 		}
 
 		return singletonInstance;
 	}
 
 	
 	
 }
