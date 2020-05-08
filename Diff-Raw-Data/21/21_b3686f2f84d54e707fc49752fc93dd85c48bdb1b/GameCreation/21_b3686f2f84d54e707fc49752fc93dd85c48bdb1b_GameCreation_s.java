 package fr.frozen.iron.client.gameStates;
 
 import org.apache.log4j.Logger;
 
 import fr.frozen.game.GameState;
 import fr.frozen.game.IGameEngine;
 import fr.frozen.game.ISprite;
 import fr.frozen.game.SpriteManager;
 import fr.frozen.iron.client.IronClient;
 import fr.frozen.iron.client.IronPlayer;
 import fr.frozen.iron.client.IronTactics;
 import fr.frozen.iron.client.components.ActionEvent;
 import fr.frozen.iron.client.components.ActionListener;
 import fr.frozen.iron.client.components.Button;
 import fr.frozen.iron.client.components.DropList;
 import fr.frozen.iron.client.components.DropListItem;
 import fr.frozen.iron.client.components.GUI;
 import fr.frozen.iron.client.components.Label;
 import fr.frozen.iron.client.messageEvents.NewPlayerEvent;
 import fr.frozen.iron.client.messageEvents.PlayerListReceivedEvent;
 import fr.frozen.iron.client.messageEvents.PlayerLogoutEvent;
 import fr.frozen.iron.client.messageEvents.RaceChoiceEvent;
 import fr.frozen.iron.protocol.Protocol;
 import fr.frozen.iron.util.IronUtil;
 import fr.frozen.network.client.NetEvent;
 import fr.frozen.network.client.NetEventListener;
 
 public class GameCreation extends GameState implements NetEventListener, ActionListener {
 
 	protected static String TXT_WAITING = "Waiting ...";
 	protected static String TXT_RACE_NOT_CHOSEN = "Not chosen yet";
 	protected static String TXT_HOST = "Host";
 	
 	protected GUI gui;
 	protected IronClient netClient;
 	
 	protected ISprite backTex;
 	
 	protected IronPlayer host;
 	protected IronPlayer other;
 	
 	protected Label hostName;
 	protected Label otherName;
 	
 	protected DropList hostList;
 	protected DropList otherList;
 	
 	public GameCreation(IGameEngine engine) {
 		super(engine,"gameCreation",false,false);
 		
 		netClient = ((IronTactics)gameEngine).getNetClient();
 		gui = new GUI();
 		
 		int width = 200;
		int height = 25;
 		hostName = new Label(TXT_HOST, 100, 100, width, height);
 		otherName = new Label(TXT_WAITING, 100, 225, width, height);
 		
 		hostList = new DropList(TXT_RACE_NOT_CHOSEN, 350, 100, width, height);
 		otherList = new DropList(TXT_RACE_NOT_CHOSEN, 350, 225, width, height);
 		
 		hostList.setEditable(false);
 		otherList.setEditable(false);
 		
 		backTex = SpriteManager.getInstance().getSprite("backTex");
 		
 		Button button = new Button("Start", 600, 350, 0, 0);
 		ISprite spriteNormal = SpriteManager.getInstance().getSprite("buttonNormal");
 		ISprite spriteHover = SpriteManager.getInstance().getSprite("buttonHover");
 
 		button.setDim((int)spriteNormal.getWidth(),(int)spriteNormal.getHeight());
 		
 		button.setHoverSprite(spriteHover);
 		button.setNormalSprite(spriteNormal);
 		
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setReady();
 			}
 		});
 		
 		gui.addComponent(hostName);
 		gui.addComponent(otherName);
 		gui.addComponent(hostList);
 		gui.addComponent(otherList);
 		gui.addComponent(button);
 	}
 	
 	protected void reInit() {
 		host = null;
 		other = null;
 		
 		hostName.setLabel(TXT_HOST);
 		otherName.setLabel(TXT_WAITING);
 		
 		hostList.setLabel(TXT_RACE_NOT_CHOSEN);
 		otherList.setLabel(TXT_RACE_NOT_CHOSEN);
 		
 		hostList.reInit();
 		otherList.reInit();
 		
 		hostList.setEditable(false);
 		otherList.setEditable(false);
 	}
 	
 	protected void setReady() {
 		netClient.sendEmptyMessage(Protocol.GAME_CREATION_READY);
 	}
 	
 	@Override
 	public void setActive(boolean val) {
 		super.setActive(val);
 		if (!val) {
 			reInit();
 		} else {
 			netClient.sendEmptyMessage(Protocol.SESSION_PLAYER_LIST_REQUEST);
 		}
 	}
 	
 	@Override
 	public void onNetEvent(NetEvent ne) {
 		if (!active) return;
 		
 		if (ne instanceof PlayerListReceivedEvent) {
 			PlayerListReceivedEvent plre = (PlayerListReceivedEvent) ne;
 			
 			if (plre.getList().size() >= 1) {
 				host = plre.getList().get(0);
 				hostName.setLabel(host.getName());
 				if (host.getId() == netClient.getClientId()) {
 					setList(hostList);
 				}
 			}
 			
 			if (plre.getList().size() >= 2) { 
 				other = plre.getList().get(1);
 				otherName.setLabel(other.getName());
 				
 				if (other.getId() == netClient.getClientId()) {
 					setList(otherList);
 				}
 			}
 		}
 		
 		if (ne instanceof NewPlayerEvent) {
 			NewPlayerEvent npe = (NewPlayerEvent) ne;
 			other = npe.getPlayer();
 			otherName.setLabel(other.getName());
 		}
 		
 		if (ne instanceof PlayerLogoutEvent) {
 			PlayerLogoutEvent ploe = (PlayerLogoutEvent) ne;
 			//if I logged out, i dont wont see the list anymore so no need to update
 			//if the other logged out but was hosting, then i will be bumped to the lobby
 			if (ploe.getId() == other.getId() && ploe.getId() != netClient.getId()) {
 				otherList.setLabel(TXT_RACE_NOT_CHOSEN);
 				otherName.setLabel(TXT_WAITING);
 			}
 		}
 		
 		if (ne instanceof RaceChoiceEvent) {
 			RaceChoiceEvent rce = (RaceChoiceEvent) ne;
 			String race = IronUtil.getRaceStr(Protocol.get(rce.getChosenRace()));
 			
 			if (host == null || other == null) return;//in case we receive a raceEvent before we know who the players are
 			//TODO: check if this works properly
 			
 			if (rce.getClientId() == netClient.getClientId()) {
 				if (rce.getClientId() == host.getId()) {
 					if (rce.getChosenRace() != hostList.getSelectedItem().getValue()) {
 						Logger.getLogger(getClass()).error("chosen race not the one requested");
 					}	
 				} else if (rce.getClientId() == other.getId()) {
 					if (rce.getChosenRace() != otherList.getSelectedItem().getValue()) {
 						Logger.getLogger(getClass()).error("chosen race not the one requested");
 					}	
 				}
 			} else {
 				if (rce.getClientId() == host.getId()) {
 					hostList.setLabel(race);
 				} else {
 					otherList.setLabel(race);
 				}
 			}
 		}
 	}
 	
 	protected void setList(DropList list) {
 		list.setEditable(true);
 		list.setLabel("choose your race");
 		
 		list.addItem(new DropListItem("orc", Protocol.ORC_RACE.ordinal()));
 		list.addItem(new DropListItem("human", Protocol.HUMAN_RACE.ordinal()));
 		list.addActionListener(this);
 	}
 	
 	
 	@Override
 	public void update(float deltaTime) {
 		super.update(deltaTime);
 		gui.update(deltaTime);
 	}
 	
 	@Override
 	public void render(float deltaTime) {
 		backTex.fillIn(0, 0, (float)gameEngine.getScreenSize().getWidth(), (float)gameEngine.getScreenSize().getWidth());
 		super.render(deltaTime);
 		gui.render(deltaTime);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() instanceof DropList) {
 			DropList source = (DropList) e.getSource();
 			DropListItem selected = source.getSelectedItem();
 			if (selected == null) return;
 			netClient.sendMessage(Protocol.GAME_CREATION_RACE_REQUEST, IronUtil.intToByteArray(selected.getValue()));
 		}
 	}
 }
