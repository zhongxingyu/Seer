 package game.gui;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import org.hibernate.Session;
 import org.hibernate.cfg.Configuration;
 
 import game.gui.GameModel.GamePhase;
 import game.gui.Interaction.Type;
 
 import game.network.GameService;
 import game.network.IGameClient;
 import game.network.IGameService;
 import game.network.messages.GameStatus;
 import game.network.messages.NetPlayer;
 
 /*
  * Filtre les données provenant du GameSerice et actualise le modele.
  * 
  * 
  */
 
 public class GameHandler implements IGameClient {
 	protected GameModel model;
 	
 	protected IGameService service;
 	Gui view;
 
 	public GameHandler(GameModel model) {
 		this.model = model;
 		this.service = new GameService();
 		this.service.setGameClient(this);
 		
 	}
 	public void setView(Gui v) {
 		view = v;
 	}
 
 	
 
 
 
 	@Override
 	public void handleInvit(NetPlayer creator, NetPlayer guest) {
 		if(model.getMe() == null) return;
 		if (model.isInGame())
 			return;
 		if (!model.getMe().equals(guest))
 			return;
 
 		if (model.getGamePhase() != GamePhase.WAITING)
 			return;
 		
 		model.setCreator(creator);
 		service.joinGame(creator, guest);
 		view.slidePlayerList();
 		
 	}
 
 	@Override
 	public void handleJoinAnswer(NetPlayer creator, NetPlayer guest) {
 		if(model.getCreator() == null)
 			return;
 		if (!model.getCreator().equals(creator))
 			return;
 		if (!model.getMe().equals(creator))
 			return;
 
 		if(model.getPlayersModel().isLimitReached()){
 			service.sendKick(model.getMe().toNet(), guest);
 			return;
 		}
 		model.addPlayer(guest);
 		service.sendGameStatus(model.getGameStatus());
 		
 
 	}
 
 	@Override
 	public void handleStatus(GameStatus status) {
 		if(model.getCreator() == null) return;
 		if (!model.getCreator().equals(status.getCreator()))
 			return;
 
 		if (this.model != null) {
 
 			updatePlayerList(status.getPlayerList());
 
 			if (!model.getGamePhase().equals(status.getPhase())) {
 				model.setPhase(status.getPhase());
 				if (status.getPhase() == GamePhase.ONEDICE) {
 					model.setDices(status.getDices());
 					// model.setPhase(GamePhase.ONEDICE);
 
 				}
 				if (status.getPhase() == GamePhase.CHECKDICE) {
 					checkDices(status);
 
 				}
 
 			}
 
 		}
 		System.err.println("+++ status received : " + status.getPhase());
 
 	}
 
 	private void updatePlayerList(ArrayList<NetPlayer> newlist) {
 		// TODO Modify for remove player
 		//model.getPlayersModel().reset();
 		ArrayList<PlayerModel> TheNewOrder = new ArrayList<PlayerModel>();
 		ArrayList<NetPlayer> toAdd = new ArrayList<NetPlayer>();
 		for (NetPlayer p : newlist) {
 			if (model.getPlayersModel().getPlayers().contains(p))
 				TheNewOrder.add(model.getPlayersModel().getFromNet(p));
 			else
 				toAdd.add(p);
 
 		}
 		model.getPlayersModel().setPlayers(TheNewOrder);
 		for (NetPlayer netPlayer : toAdd) {
 			model.addPlayer(netPlayer);
 		}
 		
 	}
 
 	public void checkDices(GameStatus status) {
 		System.err.println("checking dices ...");
 		model.setDices(status.getDices());
 		if (model.getDices().isSuite()) {
 			model.getInteraction().expectSuite();
 			model.setPhase(GamePhase.INTERACTION);
 			return;
 		} else if (model.getDices().isChouetteVelute()) {
 			model.getInteraction().expectChouetteVeloute();
 			model.setPhase(GamePhase.INTERACTION);
 			return;
 		} else {
 			calculScores();
 			checkScores();
 		}
 
 	}
 
 	private void checkScores() {
 		for (PlayerModel p : model.getPlayersModel().getPlayers()) {
 			if (p.getPlayerScore() >= 343) {
 				model.getPlayersModel().setWinner(p);
 				model.setPhase(GamePhase.FINISH);
 				model.getPlayersModel().reset();
				model.unsetCreator();
 				
 				if(this.model.getMe().equals(this.model.getPlayersModel().getWinner())) {
 					Session session = (new Configuration().configure().buildSessionFactory()).openSession();	
 					session.beginTransaction();
 					Games game = new Games(this.model.getPlayersModel());
 					session.persist(game);
 					session.getTransaction().commit();
 					for (PlayerModel player : game.getGamePlayersInfos().getPlayers()) {
 						System.out.println("player : "+player.getPlayerLogin());
 						session.beginTransaction();
 						History history = new History(game.getID(), player.getPlayerID(), player.getPlayerScore());
 						session.persist(history);
 						session.getTransaction().commit();
 					}
 //					session.beginTransaction();
 //					History history = new History(game.getID(), game.getWinnerID(), game.getWinnerID());
 //					session.persist(history);
 //					session.getTransaction().commit();
 //					session.close();
 				}
 				return;
 			}
 
 		}
 		// model.nextTurn();
 		model.setPhase(GamePhase.TWODICES);
 
 	}
 
 	private void calculScores() {
 		model.setOneGain(null, 0);
 		System.err.println("calcul score");
 		model.setPhase(GamePhase.SCORING);
 		if (model.getDices().isChouetteVelute()) {
 			model.getInteraction()
 					.getPlayer()
 					.setPlayerScore(
 							model.getInteraction().getPlayer().getPlayerScore()
 									+ model.getDices().getScore());
 			model.setOneGain(model.getInteraction().getPlayer(),model.getDices().getScore());
 			model.getInteraction().reset();
 			
 			
 			return;
 		}
 		if (model.getDices().isSuite()) {
 			model.getInteraction()
 					.getPlayer()
 					.setPlayerScore(
 							model.getInteraction().getPlayer().getPlayerScore()
 									+ model.getDices().getScore());
 			model.setOneGain(model.getInteraction().getPlayer(),model.getDices().getScore());
 			model.getInteraction().reset();
 			
 			return;
 		}
 		if (model.getDices().isCulDeChouette()) {
 			model.getTurn().setPlayerScore(model.getTurn().getPlayerScore()
 							+ model.getDices().getScore());
 			model.setOneGain(model.getTurn(),model.getDices().getScore());
 			
 			return;
 		}
 		if (model.getDices().isChouette()) {
 			model.getTurn().setPlayerScore(
 					model.getTurn().getPlayerScore()
 							+ model.getDices().getScore());
 			model.setOneGain(model.getTurn(),model.getDices().getScore());
 			return;
 		}
 		if (model.getDices().isVeloute()) {
 			model.getTurn().setPlayerScore(
 					model.getTurn().getPlayerScore()
 							+ model.getDices().getScore());
 			model.setOneGain(model.getTurn(),model.getDices().getScore());
 			return;
 		}
 
 	}
 
 	@Override
 	public void handleStart(GameStatus status) {
 		handleStatus(status);
 
 	}
 
 	@Override
 	public void handleRefresh() {
 		if (model.getGamePhase() == GamePhase.WAITING
 				&& model.getCreator() == null) {
 			service.sendWaiting(model.getMe().toNet());
 		}
 
 	}
 
 	@Override
 	public void handleInteraction(NetPlayer creator, NetPlayer player, Type type) {
 		System.err.println("Handle interacte, phase=" + model.getGamePhase());
 		
 		System.err.println("Player is in my game ?");
 		System.err.println(model.getCreator().toNet() + " " + creator);
 		if (!model.getCreator().toNet().equals(creator))
 			return;
 		System.err.println("expected interact ? ?");
 		interact(player, type);
 
 	}
 
 	public void interact(NetPlayer player, Type type) {
 		if (!model.getGamePhase().equals(GamePhase.INTERACTION))
 			return;
 		if (type.equals(model.getInteraction().getExpected())) {
 			System.err.println("it s an expected interact ");
 			model.getInteraction().addPlayer(
 					model.getPlayersModel().getFromNet(player));
 
 			System.err.println(model.getInteraction().interacCount() + " >= " + model
 					.getPlayersModel().size());
 			if (model.getInteraction().interacCount() >= model
 					.getPlayersModel().size()) {
 				
 				calculScores();
 				checkScores();
 			}
 
 		}
 		
 	}
 
 
 
 
 
 	public void refresh() {
 
 		service.sendRefresh();
 
 	}
 
 	@Override
 	public void handleWaitingNotification(NetPlayer player) {
 
 		if (model.getCreator() == null)
 			return;
 		if (model.getGamePhase() == GamePhase.WAITING
 				&& model.getCreator().equals(model.getMe())) {
 
 			if (!model.getAvailableModel().getPlayers().contains(player)) {
 				PlayerModel pm = new PlayerModel(player, Color.black);
 				model.getAvailableModel().add(pm);
 
 			}
 		}
 
 	}
 
 	public IGameService getService() {
 		return service;
 	}
 
 	public void lauchGame() {
 		model.getPlayersModel().shuffle();
 
 		model.setPhase(GamePhase.TWODICES);
 
 		service.startGame(model.getGameStatus());
 
 	}
 	@Override
 	public void handleKick(NetPlayer c, NetPlayer p) {
 		if(model.getCreator() == null) return;
 		if(model.getCreator().equals(c) && model.getMe().equals(p)){
 			model.unsetCreator();
 			model.getPlayersModel().reset();
 			model.setPhase(GamePhase.WAITING);
 		}
 		
 	}
 	@Override
 	public void handleCancelGame(NetPlayer c) {
 		if(model.getCreator() == null) return;
 		if(model.getCreator().equals(c)){
 			model.unsetCreator();
 			model.getPlayersModel().reset();
 			model.setPhase(GamePhase.WAITING);
 		}
 		
 	}
 	
 	@Override
 	public void handleLeaveGame(NetPlayer c, NetPlayer p) {
 		if(model.getCreator() == null) return;
 		if(model.getCreator().equals(c)){
 			model.getPlayersModel().remove(p);
 		}
 		
 	}
 }
