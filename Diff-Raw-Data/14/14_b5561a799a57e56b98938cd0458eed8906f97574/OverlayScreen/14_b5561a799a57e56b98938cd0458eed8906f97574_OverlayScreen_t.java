 package edu.rit.se.sse.rapdevx.gui.screens;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import edu.rit.se.sse.rapdevx.clientstate.AttackState;
 import edu.rit.se.sse.rapdevx.clientstate.DoneState;
 import edu.rit.se.sse.rapdevx.clientstate.GameSession;
 import edu.rit.se.sse.rapdevx.clientstate.MoveState;
 import edu.rit.se.sse.rapdevx.clientstate.StateBase;
 import edu.rit.se.sse.rapdevx.clientstate.UnitPlacementState;
 import edu.rit.se.sse.rapdevx.events.StateEvent;
 import edu.rit.se.sse.rapdevx.events.StateListener;
 import edu.rit.se.sse.rapdevx.gui.Screen;
 import edu.rit.se.sse.rapdevx.gui.drawable.Text;
 import edu.rit.se.sse.rapdevx.gui.images.HoverableImage;
 import edu.rit.se.sse.rapdevx.gui.images.IGrayableImage;
 
 public class OverlayScreen extends Screen implements StateListener
 {
 	private static final int UNDO_MODIFIER_X = -1;
 	private static final int REDO_MODIFIER_X = 35;
 	private static final int PHASE_MODIFIER_X = 64;
 	private static final int READY_MODIFIER_X = 283;
 	private static final int PLAY_CONTROLS_MODIFIER_X = 23;
 
 	private static final int TURN_CONTROL_Y = 0;
 	private static final int TURN_CONTROL_SIZE_X = 40;
 	private static final int TURN_CONTROL_END_SIZE_X = 69;
 
 	private IGrayableImage undo;
 	private IGrayableImage redo;
 	private IGrayableImage phase;
 	private IGrayableImage readyEnabled;
 	private IGrayableImage readyDisabled;
 
 	private IGrayableImage[] turnControlImages = new IGrayableImage[7];
 	private String[] turnControlImageStr = { "beginning", "backfast", "back",
 			"pause", "forward", "forwardfast", "skip" };
 
 	private IGrayableImage selectedImage;
 
 	private Text readyText;
 	private Text stateText;
 
 	private boolean isReady;
 	private boolean displayPlayback = false;
 
 	private Rectangle mainBounds;
 	private Rectangle playbackBounds;
 	
 	private int turn = 0;
 	
 	private ConcurrentLinkedQueue<ActionListener> listeners = new ConcurrentLinkedQueue<ActionListener>();
 	
 	public OverlayScreen(int width, int height)
 	{
 		super(width, height);
 
 		int STARTING_X = width / 2 - 128 - 64;
 		int STARTING_Y = 1;
 
 		isReady = false;
 
 		mainBounds = new Rectangle(382, 32);
 		mainBounds.x = STARTING_X;
 		mainBounds.y = 0;
 
 		playbackBounds = new Rectangle(280, 64);
 		playbackBounds.x = STARTING_X + 52;
 		playbackBounds.y = 32;
 
 		int curX = STARTING_X + PLAY_CONTROLS_MODIFIER_X;
 		for (int x = 0; x < 7; ++x)
 		{
 			turnControlImageStr[x] = "assets/Play-" + turnControlImageStr[x];
 			turnControlImages[x] = new HoverableImage(turnControlImageStr[x],
 					curX, STARTING_Y + TURN_CONTROL_Y);
 			
 			curX += (x == 0) ? TURN_CONTROL_END_SIZE_X : TURN_CONTROL_SIZE_X;
 		}
 
 		undo = new HoverableImage("assets/Undo", STARTING_X
 				+ UNDO_MODIFIER_X, STARTING_Y);
 		redo = new HoverableImage("assets/Redo", STARTING_X
 				+ REDO_MODIFIER_X, STARTING_Y);
 		phase = new HoverableImage("assets/StatusBar", STARTING_X
 				+ PHASE_MODIFIER_X, STARTING_Y);
 		readyEnabled = new HoverableImage("assets/Ready-enabled", STARTING_X
 				+ READY_MODIFIER_X, STARTING_Y);
 		readyDisabled = new HoverableImage("assets/Ready-disabled",
 				STARTING_X + READY_MODIFIER_X, STARTING_Y);
 
 		readyText = new Text("Ready", STARTING_X + READY_MODIFIER_X + 28,
 				STARTING_Y + 9, Color.BLACK);
 		stateText = new Text("Placing : " + turn, STARTING_X + 100, 15, 2.5,
 				Color.BLACK);
 		
 		GameSession.get().addStateListener(this);
 	}
 
 	public void update(boolean hasFocus, boolean isVisible)
 	{
 
 	}
 
 	public void draw(Graphics2D gPen)
 	{
 
 		if (displayPlayback)
 		{
 			// Bottom part, conditional, playback buttons
 			for (IGrayableImage img : turnControlImages)
 			{
 				img.draw(gPen);
 			}
 		}
 
 		// Top part, under phase bar
 		undo.draw(gPen);
 		redo.draw(gPen);
 		if (!isReady)
 			readyEnabled.draw(gPen);
 		else
 			readyDisabled.draw(gPen);
 
 		// Top part, phase bar
 		phase.draw(gPen);
 
 		readyText.draw(gPen);
 		stateText.draw(gPen);
 	}
 	
 	public void mouseReleased(MouseEvent e)
 	{
 		if (mainBounds.contains(e.getPoint()))
 		{
 			if (undo.containsPoint(e.getPoint()))
 			{
 				
 			}
 			else if (redo.containsPoint(e.getPoint()))
 			{
 				//TODO temp for testing purposes
 				displayPlayback = !displayPlayback;
 			}
 			else if (readyEnabled.containsPoint(e.getPoint()))
 			{
 				// TODO more shit
 				isReady = true;
 				readyText.setColor(Color.DARK_GRAY);
 				
				notifyActionListeners("ready");
 			}
 			e.consume();
 		}
 		else if (displayPlayback && playbackBounds.contains(e.getPoint()))
 		{
 			for (IGrayableImage img : turnControlImages)
 			{
 				if (img.containsPoint(e.getPoint()))
 				{
 					//GameSession.get().advanceState();
 				}
 			}
 			e.consume();
 		}
 		
 		undo.setPressed(false);
 		redo.setPressed(false);
 		readyEnabled.setPressed(false);
 		for (IGrayableImage img : turnControlImages) {
 			img.setPressed(false);
 		}
 	}
 	
 	public void mousePressed(MouseEvent e)
 	{
 		IGrayableImage currentlyOver = null;
 
 		if (mainBounds.contains(e.getPoint()))
 		{
 			if (undo.containsPoint(e.getPoint()))
 			{
 				currentlyOver = undo;
 			}
 			else if (redo.containsPoint(e.getPoint()))
 			{
 				currentlyOver = redo;
 			}
 			else if (readyEnabled.containsPoint(e.getPoint()))
 			{
 				currentlyOver = readyEnabled;
 			}
 			e.consume();
 		}
 		else if (displayPlayback && playbackBounds.contains(e.getPoint()))
 		{
 			for (IGrayableImage img : turnControlImages)
 			{
 				if (img.containsPoint(e.getPoint()))
 				{
 					currentlyOver = img;
 				}
 			}
 			e.consume();
 		}
 
 		if (selectedImage != null)
 		{
 			selectedImage.setPressed(false);
 		}
 		if (currentlyOver != null)
 		{
 			currentlyOver.setPressed(true);
 			selectedImage = currentlyOver;
 			e.consume();
 		}
 	}
 
 	public void mouseMoved(MouseEvent e)
 	{
 		IGrayableImage currentlyOver = null;
 
 		if (mainBounds.contains(e.getPoint()))
 		{
 			if (undo.containsPoint(e.getPoint()))
 			{
 				currentlyOver = undo;
 			}
 			else if (redo.containsPoint(e.getPoint()))
 			{
 				currentlyOver = redo;
 			}
 			else if (readyEnabled.containsPoint(e.getPoint()))
 			{
 				currentlyOver = readyEnabled;
 			}
 			e.consume();
 		}
 		else if (displayPlayback && playbackBounds.contains(e.getPoint()))
 		{
 			for (IGrayableImage img : turnControlImages)
 			{
 				if (img.containsPoint(e.getPoint()))
 				{
 					currentlyOver = img;
 				}
 			}
 			e.consume();
 		}
 
 		if (selectedImage != null)
 		{
 			selectedImage.setHovering(false);
 		}
 		if (currentlyOver != null)
 		{
 			currentlyOver.setHovering(true);
 			selectedImage = currentlyOver;
 			e.consume();
 		}
 	}
 
 	public void stateChanged(StateEvent e)
 	{
 		isReady = false;
 
 		if (e.getNewState() instanceof UnitPlacementState)
 		{
 			stateText.setText("Placing :" + turn);
 		}
 		else if (e.getNewState() instanceof MoveState)
 		{
 			stateText.setText("Moving :" + turn);
 		}
 		else if (e.getNewState() instanceof AttackState)
 		{
 			stateText.setText("Attack :" + turn);
 		}
 		else if (e.getNewState() instanceof DoneState)
 		{
 			stateText.setText("Victory :" + turn);
 		}
 	}
 	
 	
 	/**
 	 * @param listener
 	 *              the listener to add
 	 */
 	public synchronized void addActionListener(ActionListener listener) {
 		listeners.add(listener);
 	}
 
 	/**
 	 * @param observer
 	 *              the observer to remove
 	 */
 	public synchronized void removeActionListener(ActionListener listener) {
 		listeners.remove(listener);
 	}
 
 	private synchronized void notifyActionListeners(String command) {
 		for (ActionListener listener : listeners) {
 			ActionEvent event = new ActionEvent(this, 0, command);
 			listener.actionPerformed(event);
 		}
 	}
 
 }
