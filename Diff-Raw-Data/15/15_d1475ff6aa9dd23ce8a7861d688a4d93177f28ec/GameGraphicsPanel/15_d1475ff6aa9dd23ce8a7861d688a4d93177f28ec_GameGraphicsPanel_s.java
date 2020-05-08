 package crescendo.game;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.swing.JPanel;
 import crescendo.base.AudioPlayer;
 import crescendo.base.NoteAction;
 import crescendo.base.NoteEvent;
 import crescendo.base.NoteEventListener;
 import crescendo.base.ProcessedNoteEvent;
 import crescendo.base.ProcessedNoteEventListener;
 import crescendo.base.SongPlayer;
 import crescendo.base.Updatable;
 import crescendo.base.UpdateTimer;
 import crescendo.base.EventDispatcher.ActionType;
 import crescendo.base.EventDispatcher.MidiEvent;
 import crescendo.base.EventDispatcher.MidiEventListener;
 import crescendo.base.song.Note;
 import crescendo.base.song.SongModel;
 import crescendo.base.song.Track;
 
 /**
  * GameGraphicsPanel
  * 
  * This JPanel extension represents the graphical part of the Game Module, it
  * is responsible for all of the vector drawing required to render the game
  * screen.
  * 
  * @author groszc
  *
  */
 public class GameGraphicsPanel extends JPanel implements NoteEventListener, ProcessedNoteEventListener, MidiEventListener, Updatable{
 	private static final long serialVersionUID=1L;
 
 	/** The AudioPlayer for the background tracks **/
 	private AudioPlayer audioPlayer;
 
 	/** The SongModel for the song to display **/
 	private SongModel songModel;
 
 	/** The Track of the song the user is to play **/
 	private List<Track> activeTracks;
 
 
 
 	//*** graphics variables ***//
 	/** The top margin **/
 	int yOffset=10;
 
 	/** The dimensions of the intended screen **/
 	int height;
 	int width;
 
 	/** A Map of the notes that scroll down the screen **/
 	HashMap<Note,Long> fallingNotes;
 
 	/** A Map of the "Correct!" and "Miss!" response messages **/
 	HashMap<ProcessedNoteEvent,Integer> fallingResponse;
 
 	/** A List of the currently held piano keys **/
 	ArrayList<Integer> midiNotesPressed;
 
 	/**The lowest and highest piano key on the user's keyboard **/
 	int lowestKey;
 	int highestKey;
 
 	/** The time since the last note was expected **/
 	int timeOfNote=0;
 
 	// The Thread that keep this JPanel repainting
 	UpdateTimer timer;
 	Thread timerThread;
 
 
 	/**
 	 * GameGraphicsPanel
 	 * 
 	 * Basic Constructor
 	 * 
 	 * @param model - The songmodel of the song to display
 	 * @param activeTrack - The active tracks (to display)
 	 * @param songPlayer - a songplayer reference to use
 	 */
 	public GameGraphicsPanel(SongModel model,List<Track> activeTracks,List<Track> audioTracks,SongPlayer songPlayer) {
 
 		//default values
 		lowestKey = 21;
 		highestKey = 108;
 
 		this.setPreferredSize(new Dimension(1024,768));
 
 		width = this.getWidth();
 		height = this.getHeight();
 
 		this.setBackground(Color.WHITE);
 
 
 
 		//initialize member variables
 		midiNotesPressed = new ArrayList<Integer>();	
 		fallingNotes = new HashMap<Note,Long>();
 		fallingResponse = new HashMap<ProcessedNoteEvent,Integer>();
 
 		this.activeTracks = activeTracks;
 		songModel = model;
 
 		//set up the AudioPlayer
 		audioPlayer = new AudioPlayer(model,audioTracks);
 
 		songPlayer.attach(this, 2000);
 		songPlayer.attach(audioPlayer,(int)audioPlayer.getLatency());
 
 		//Start repainting
 		timer = new UpdateTimer(this);
 		timerThread = new Thread(timer);
 		timerThread.start();
 
 		//Play the song
 		songPlayer.play();
 	}
 
 	/**
 	 * nX
 	 * 
 	 * Normalizes a value based off the width of the screen
 	 * 
 	 * @param x - The X value to normalize
 	 * @return - normalized x value
 	 */
 	int nX(double x)
 	{
 		return (int) (double)((width*x/1024));
 	}
 
 	/**
 	 * nXY
 	 * 
 	 * Normalizes a value based off the height of the screen
 	 * 
 	 * @param y - The Y value to normalize
 	 * @return - normalized y value
 	 */
 	int nY(double y)
 	{
 		return (int) (double)((height*y/768));
 	}
 
 	/**
 	 * isSharp
 	 * 
 	 * Returns true if the given midiNote value is sharp/flat
 	 * 
 	 * @param n - the midiNote value
 	 * @return
 	 */
 	public boolean isSharp(int n)
 	{
 		boolean toRet = false;
 
 		if(n==22 || n==25 || n==27 || n==30 || n==32 || n==34 || 
 				n==37 || n==39 || n==42 || n==44 || n==46 || 
 				n==49 || n==51 || n==54 || n==56 || n==58 ||
 				n==61 || n==63 || n==66 || n==68 || n==70 ||
 				n==73 || n==75 || n==78 || n==80 || n==82 ||
 				n==85 || n==87 || n==90 || n==92 || n==94 ||
 				n==97 || n==99 || n==102 || n==104 || n==106)toRet = true;
 
 
 
 		return toRet;
 	}
 
 
 	/**
 	 * whichKey
 	 * 
 	 * Returns the note letter that corresponds with a midi note value
 	 * 
 	 * @param n - the midiNote value to find the note letter of
 	 * @return
 	 */
 	public char whichKey(int n)
 	{
 		//21='a'
 
 		char toRet='a';
 
 		for(int i=21;i<n;i++)
 		{
 			if(!isSharp(i))
 			{
 				if(toRet<'g')toRet++;
 				else toRet='a';
 			}
 		}
 		return toRet;
 	}
 
 	/**
 	 * findXForKey
 	 * 
 	 * Returns the un-normalized X position for a given midi note value
 	 * 
 	 * @param n - the midi note value to find the x - position of
 	 * @return
 	 */
 	public int findXForKey(int n)
 	{
 		double toRet = 10;
 
 		//count the white keys
 		int numWhiteKeys=0;
 		for(int i=lowestKey;i<=highestKey;i++)if(!isSharp(i))numWhiteKeys++;
 
 
 		for(int i=lowestKey;i<n;i++)
 		{
 			if(!isSharp(i))
 			{
 				toRet+=(1004.0/numWhiteKeys);
 			}
 		}
 
 		return (int)toRet;
 	}
 
 
 
 
 
 	@Override
 	public void paint(Graphics g){
 		super.paint(g);
 
 
 		//check to see if our dimensions have changed
 		width = this.getWidth();
 		height = this.getHeight();
 
 
 		//count the white keys
 		int numWhiteKeys=0;
 		for(int i=lowestKey;i<=highestKey;i++)if(!isSharp(i))numWhiteKeys++;
 
 
 		//draw a gray background
 		g.setColor(new Color(240,240,240));
 		g.fillRect(0, 0, width, height);
 
 		//draw the blue lane background
 		g.setColor(new Color(200,200,255));
 		g.fillRect(nX(10), nY(yOffset), nX(1004), nY(500));
 
 
 		//draw a box of white inder the piano keys
 		g.setColor(Color.white);
 		g.fillRect(nX(10), nY(yOffset+500), nX(1004), nY(100));
 
 
 		//if any lanes are highlighted, draw them in
 		synchronized (GameGraphicsPanel.class) 
 		{
 			//draw the highlighted lanes 
 			g.setColor(new Color((int) (170-(40.0*(1.0/((((int)System.currentTimeMillis())-timeOfNote)/500.0+1.0)))),150,255));
 			for(Integer i: midiNotesPressed)
 			{
 				if(isSharp(i))
 				{
 					g.fillRect(nX(findXForKey(i)-(1004/numWhiteKeys/4)), nY(yOffset), nX((1004/numWhiteKeys/2)), nY(600));
 				}
 				else
 				{
 					g.fillRect(nX(findXForKey(i)), nY(yOffset), nX(1004/numWhiteKeys), nY(600));
 				}
 			}
 		}
 
 
 		//Draw the black outline for the piano keys
 		g.setColor(Color.black);
 		g.drawLine(nX(10), nY(yOffset), nX(1014), nY(yOffset));
 		g.drawLine(nX(10), nY(yOffset+500), nX(1014), nY(yOffset+500));
 		g.drawLine(nX(10), nY(yOffset+600), nX(1014), nY(yOffset+600));
 
 
 		//Draw the lines in between the white keys
 		char keyName=whichKey(lowestKey);
 		for(int i=0;i<=numWhiteKeys;i++)
 		{
 			g.drawLine(nX(10+i*1004/numWhiteKeys), nY(yOffset), nX(10+i*1004/numWhiteKeys), nY(yOffset+600));
 
 
 
 			g.drawString(keyName+"", nX(10+i*1004/numWhiteKeys+(1004/numWhiteKeys*.5)), nY(yOffset+590));
 
 			if(keyName<'g')keyName++;
 			else keyName='a';
 		}
 
 
 		//Draw the black keys
 		int x=0;
 		for(int i=0;i<highestKey-lowestKey;i++)
 		{
 			if(isSharp(i+lowestKey))
 			{
 				g.fillRect(nX(10+x*1004/numWhiteKeys-(1004/numWhiteKeys/4)), nY(yOffset+500), nX((1004/numWhiteKeys/2)), nY(50));
 			}
 			else x++;
 		}
 
 
 		//Draw falling Notes
 		g.setClip(nX(10),nY(yOffset),nX(1014),nY(500));
 		synchronized (GameGraphicsPanel.class) 
 		{
 			for(Note n: fallingNotes.keySet())
 			{
 				//calculate where this falling note starts
 				long y = System.currentTimeMillis()-fallingNotes.get(n);
 				y = (y*500)/2000;
 				y += yOffset;
 
 				//calculate the y-axis length of this falling note (in pixels)
 				long duration = (long) (n.getDuration()/(songModel.getBPM()/60.0/1000.0/500.0*2000.0));
 
 				//if its a black key
 				if(isSharp(n.getPitch()))
 				{
 					double color =  70+(70*(1.0/((((int)System.currentTimeMillis())-timeOfNote)/500.0+1.0)));
 					g.setColor(new Color((int)color,(int)color,(int)color));
 					g.fillRect(nX(findXForKey(n.getPitch())-(1004/numWhiteKeys/4)), nY(y-duration), nX((1004/numWhiteKeys/2)), nY(duration));
 				}
 				//if its a white key
 				else
 				{
 					double color =  255.0-(20.0*(1.0/((((int)System.currentTimeMillis())-timeOfNote)/500.0+1.0)));
 					g.setColor(new Color((int)color,(int)color,(int)color));
 					g.fillRect(nX(findXForKey(n.getPitch())), nY(y-duration), nX(1004/numWhiteKeys), nY(duration));
 					g.setColor(new Color(0,0,0));
 					g.drawRect(nX(findXForKey(n.getPitch())), nY(y-duration), nX(1004/numWhiteKeys), nY(duration));
 				}
 			}
 
 
 
 			//Draw the "Correct!" and "Miss!" messages
 			ArrayList<ProcessedNoteEvent> toRemove = new ArrayList<ProcessedNoteEvent>();
 			g.setClip(-1,-1,-1,-1);
 			for(ProcessedNoteEvent k: fallingResponse.keySet())
 			{
 				int deltaTime = (int)(System.currentTimeMillis()) - fallingResponse.get(k);
 				if(deltaTime > 500)toRemove.add(k);
 				else
 				{
 					g.setFont(new Font("Georgia", Font.PLAIN, nY(12+deltaTime/50)));
 					if(k.isCorrect())
 					{
 						g.setColor(Color.green);
 						g.drawString("Correct!", nX(findXForKey(k.getExpectedNote().getNote().getPitch())), nY(500+deltaTime/25));
 					}	
 					else
 					{
 						g.setColor(Color.red);
 						g.drawString("Miss!", nX(findXForKey(k.getExpectedNote().getNote().getPitch())), nY(500+deltaTime/25));
 					}
 				}
 			}
 			for(ProcessedNoteEvent e: toRemove)fallingResponse.remove(e);
 		}
 	}
 
 
 
 
 	@Override
 	public void update() {
 		repaint();
 	}
 
 	@Override
 	public void handleNoteEvent(NoteEvent e) {
 		if(e.getAction()==NoteAction.BEGIN)
 		{
 			synchronized (GameGraphicsPanel.class) 
 			{
 				for(int i=0;i<activeTracks.size();i++)
 				{
 					if(e.getNote().getTrack().equals(activeTracks.get(i)))
 					{
 						fallingNotes.put(e.getNote(), System.currentTimeMillis());
 					}
 				}
 			}
 		}
 
 
 	}
 
 	@Override
 	public void handleProcessedNoteEvent(ProcessedNoteEvent e) {
 		if(e.getExpectedNote().getAction()==NoteAction.BEGIN)
 		{
 			synchronized (GameGraphicsPanel.class) 
 			{
 				fallingResponse.put(e, (int)System.currentTimeMillis());
 				timeOfNote = (int)System.currentTimeMillis();
 			}
 		}
 
 
 	}
 
 	@Override
 	public void handleMidiEvent(MidiEvent midiEvent) {
 
 		synchronized (GameGraphicsPanel.class) 
 		{
 			if(midiEvent.getAction()==ActionType.PRESS)
 			{
				midiNotesPressed.add(midiEvent.getNote());
 			}
 			else
 			{
				midiNotesPressed.remove(midiEvent.getNote());
 			}
 		}
 
 	}
 
 
 }
 
