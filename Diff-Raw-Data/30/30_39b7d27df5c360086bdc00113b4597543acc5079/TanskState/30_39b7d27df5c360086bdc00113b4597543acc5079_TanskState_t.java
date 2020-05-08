 package chalmers.TDA367.B17.states;
 
 import java.util.Calendar;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import chalmers.TDA367.B17.Tansk;
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.network.Network.Packet;
 
 public abstract class TanskState extends BasicGameState {
 	protected int state;
 	protected GameController controller;
 	protected ConcurrentLinkedQueue<Packet> packetQueue;
 	protected int frameCounter;
 	protected int updatesPerSecond;
 	protected int packetsSent;
 	protected int packetsSentPerSecond;
 	protected int packetsRecPerSecond;
 	protected int packetsReceived;
 	protected long deltaTime;
 	protected long oldTime;
 	protected int updates;
 	protected int seconds;
 	
 	protected TanskState(int state) {
 	    this.state = state;
     }
 		
 	@Override
 	public void init(GameContainer gc, StateBasedGame game) throws SlickException {
 		gc.setSmoothDeltas(true);
 		gc.setAlwaysRender(true);
 		gc.setMouseCursor(new Image(Tansk.IMAGES_FOLDER + "/crosshair.png"), 16, 16);
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame game) throws SlickException {
 		super.enter(gc, game);
 
 		controller = GameController.getInstance();
 		packetQueue = new ConcurrentLinkedQueue<Packet>();
 		
 		frameCounter = 0;
 		seconds = 0;
 	}	
 	
 	@Override
 	public void leave(GameContainer gc, StateBasedGame sbg) throws SlickException {
 
 	}
 		
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {				
		if(container.getInput().isKeyPressed(Input.KEY_ESCAPE)){
 			game.enterState(Tansk.MENU);
 		}
 		
 		frameCounter++;
 		long newTime = Calendar.getInstance().getTimeInMillis();
 		deltaTime = newTime - oldTime;
 		if(deltaTime >= 1000){			
 			packetsRecPerSecond = packetsReceived;
 			packetsReceived = 0;
 			packetsSentPerSecond = packetsSent;
 			packetsSent = 0;
 			updatesPerSecond = updates;
 			updates = 0;
 			seconds++;
 			oldTime = newTime;
 		}
 		updates++;
 	}
 	
 	public void renderGUI(GameContainer container, Graphics g){
 		controller.getConsole().renderMessages(g);
 		g.setColor(Color.black);
 		g.setLineWidth(1);
 		
 		g.setColor(Color.black);
 		g.drawString("Time: " + seconds, 18, 300);
 		g.drawString("Packet rec/sec: " + packetsRecPerSecond, 18, 320);
 		g.drawString("Packet sent/sec: " + packetsSentPerSecond, 18, 340);
 		g.drawString("Update/sec: " + updatesPerSecond, 18, 360);
 		g.drawString("Frame: " + frameCounter, 18, 400);
 		g.drawString("Entities: " + controller.getWorld().getEntities().size(), 18, 420);
 		g.setColor(Color.white);
 	}
 	
 	@Override
 	public int getID() {
 	    return this.state;
 	}
 }
