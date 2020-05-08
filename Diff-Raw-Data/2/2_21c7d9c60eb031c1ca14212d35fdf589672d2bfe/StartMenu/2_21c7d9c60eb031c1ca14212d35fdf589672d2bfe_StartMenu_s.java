 package edu.calpoly.csc.pulseman;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public class StartMenu implements GameInterface
 {
 	private Image menuButton;
 	private Image menuBackground;
 	private Image connectButton, connectingButton, connectedButton;
 	private volatile int countdown = 0;
 
 	private final float[] buttonLoc =
 	{ 200, 400 }, connectLoc =
 	{ 850, 30 };
 
 	@Override
 	public void render(GameContainer gc, Graphics g)
 	{
 		g.drawImage(menuBackground, 0, 0);
 		g.drawImage(menuButton, buttonLoc[0], buttonLoc[1]);
 
 		if(Main.getAndroidState() == Main.AndroidStates.NOT_CONNECTED)
 		{
 			g.drawImage(connectButton, connectLoc[0], connectLoc[1]);
 		}
 		else if(Main.getAndroidState() == Main.AndroidStates.CONNECTING)
 		{
 			g.drawImage(connectingButton, connectLoc[0], connectLoc[1]);
 
 			g.drawString(String.valueOf(countdown), connectLoc[0] + connectingButton.getWidth(), connectLoc[1] + connectingButton.getHeight() / 2);
 		}
 		else
 		{
 			g.drawImage(connectedButton, connectLoc[0], connectLoc[1]);
 		}
 
 		g.drawString("You are a meditating monk. Head towards the light.\n" + "Use the beat to control nature's speed.", Main.getScreenWidth() / 2, Main.getScreenHeight() / 2);
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException
 	{
 		menuButton = new Image("res/subtitle.png");
 		connectButton = new Image("res/connect.png");
 		connectingButton = new Image("res/connecting.png");
 		connectedButton = new Image("res/connected.png");
 		menuBackground = new Image("res/mainscreen.png");
 	}
 
 	@Override
 	public void update(GameContainer gc, int dt)
 	{
 		Input input = gc.getInput();
 		if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON))
 		{
 			int x = input.getMouseX();
 			int y = input.getMouseY();
 			if(x >= buttonLoc[0] && x <= buttonLoc[0] + menuButton.getWidth() && y >= buttonLoc[1] && y <= buttonLoc[1] + menuButton.getHeight())
 			{
 				Main.setState(Main.GameState.GAME);
 			}
 
			if(Main.getAndroidState() == Main.AndroidStates.NOT_CONNECTED || (x >= connectLoc[0] && x <= connectLoc[0] + connectButton.getWidth() && y >= connectLoc[1] && y <= connectLoc[1] + connectButton.getHeight()))
 			{
 				listenForConnection();
 
 				countdown = MessageHandler.SOCKET_TIMEOUT / 1000 + 1;
 				new Timer("Countdown Timer").schedule(new TimerTask()
 				{
 					@Override
 					public void run()
 					{
 						if(--countdown < 0)
 						{
 							cancel();
 						}
 					}
 				}, 0, 1000);
 			}
 		}
 	}
 
 	public static void listenForConnection()
 	{
 		new Thread(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				Main.setAndroidConnecting();
 				MessageHandler.listenForConnection();
 			}
 		}).start();
 	}
 }
