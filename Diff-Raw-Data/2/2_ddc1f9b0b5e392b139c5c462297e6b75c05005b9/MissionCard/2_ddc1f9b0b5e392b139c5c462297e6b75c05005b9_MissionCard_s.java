 package jcavern.ui;
 
 import jcavern.*;
 import jcavern.ui.*;
 import jcavern.thing.*;
 
 import java.awt.*;
 import java.util.*;
 import java.applet.Applet;
 
 /**
  * Displays an AppletCard where all the action of performing a Mission takes place,
  * including a view of the Player's statistics and equipment, a view of the World and
  * its inhabitants, a view of the status of the mission, and a view of the log messages.
  */
 public class MissionCard extends AppletCard implements Observer
 {
 	/** * The official PLATO orange color, as used in jcavern. */
 	public static final Color		CavernOrange = new Color(0xFF, 0x66, 0x00);
 	
 	/** * A view of the game world */
 	private WorldView				mWorldView;
 	
 	/** * A view of the player statistics */
 	private PlayerView				mPlayerView;
 	
 	/** * A view of the player statistics */
 	private MissionView				mMissionView;
 	
 	/** * A model of the game world */
 	private World					mWorld;
 	
 	/** * A model of the game world */
 	private LogView					mLogView;
 	
 	/** * The representation of the player */
 	private Player					mPlayer;
 	
 	/**
 	 * Receives update notification that the World being viewed has changed.
 	 * In this particular case, the MissionCard is looking for a MISSION_END event.
 	 * Upon finding one, it creates and displays an EndMissionCard.
 	 *
 	 * @param	a	the object sending the event
 	 * @param	b	details about the event
 	 */
 	public void update(Observable a, Object b)
 	{
 		// System.out.println("MissionCard.update(" + a + ", " + b + ")");
 		
 		WorldEvent anEvent = (WorldEvent) b;
 
 		if (anEvent.getEventCode() == WorldEvent.MISSION_END)
 		{
			AppletCard anAlert = new NewEndMissionCard(mApplet, anEvent, mWorld);
 			anAlert.show();
 		}
 	}
 
     /**
      * Creates a new MissionCard for the given Applet and Player.
      *
      * @param	inApplet	a non-null Applet in which to display the message
      * @param	inPlayer	a non-null Player who will do a Mission
      */
 	public MissionCard(JCavernApplet inApplet, Player inPlayer)
 	{
 		super(inApplet);
 		
 		mPlayer = inPlayer;
 		
 		mPlayerView = new PlayerView(inApplet, mPlayer);
 		mPlayer.setMission(MonsterFactory.createMission(mPlayer));
 		mMissionView = new MissionView(inApplet, mPlayer.getMission());
 
 		// Create a world  and a view of the world
 		mWorld = new World();
 		mWorldView = new WorldView(inApplet, mWorld);
 
 		mLogView = new LogView(inApplet, mPlayer);
 		mLogView.setSize(300, 200);
 		
 		mWorldView.setSize(300, 300);		
 		mWorld.addObserver(mWorldView);
 		mWorld.addObserver(mLogView);
 		mWorld.addObserver(this);
 		
 		mPlayerView.setSize(150, 300);
 		
 		mMissionView.setSize(150, 200);		
 		mPlayer.getMission().addObserver(mMissionView);
 	}
 	
 	/**
 	 * Notifies this card that it was removed from view by the Applet.
 	 */
 	public void cardRemoved()
 	{
 		mWorld.deleteObserver(mWorldView);
 		mWorld.deleteObserver(mLogView);
 		mWorld.deleteObserver(this);
 		mPlayer.deleteObserver(mPlayerView);
 		mPlayer.getMission().deleteObserver(mMissionView);
 		
 		mWorldView = null;
 		mPlayerView = null;
 		mMissionView = null;
 		mWorld = null;
 		mLogView = null;
 		mPlayer = null;
 	}
 	
 	/**
 	 * Displays this AppletCard in its Applet.
 	 */
 	public void show()
 	{
 		super.show();
 		
 		mApplet.setBackground(Color.black);
 		mApplet.setForeground(CavernOrange);
 		
 		mApplet.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
 		
 		
 		Panel leftPanel = new Panel();
 		leftPanel.setLayout(new BorderLayout());
 		leftPanel.setSize(300, 500);
 		leftPanel.add(mWorldView, BorderLayout.NORTH);
 		leftPanel.add(mLogView, BorderLayout.SOUTH);
 		
 		Panel rightPanel = new Panel();
 		rightPanel.setLayout(new BorderLayout());
 		rightPanel.setSize(150, 500);
 		rightPanel.add(mPlayerView, BorderLayout.NORTH);
 		rightPanel.add(mMissionView, BorderLayout.SOUTH);
 		
 		mApplet.add(leftPanel);
 		mApplet.add(rightPanel);
 		
 		mApplet.validate();
 		
 		try
 		{
 			mWorld.populateFor(mPlayer);
 		
 			mWorldView.requestFocus();
         	mWorldView.addKeyListener(new KeyboardCommandListener(mWorld, mPlayer));
 			mWorld.eventHappened(new WorldEvent(mPlayer, WorldEvent.TURN_STOP));
 		}
 		catch (JCavernInternalError jcie)
 		{
 			System.out.println("UHOH, internal error " + jcie);
 		}
 	}
 }
