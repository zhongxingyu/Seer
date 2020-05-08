 package crescendo.sheetmusic;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JPanel;
 import javax.swing.Scrollable;
 
 import crescendo.base.ErrorHandler;
 import crescendo.base.FlowController;
 import crescendo.base.NoteAction;
 import crescendo.base.NoteEvent;
 import crescendo.base.NoteEventListener;
 import crescendo.base.ProcessedNoteEvent;
 import crescendo.base.ProcessedNoteEventListener;
 import crescendo.base.EventDispatcher.ActionType;
import crescendo.base.EventDispatcher.EventDispatcher;
 import crescendo.base.song.Creator;
 import crescendo.base.song.Note;
 import crescendo.base.song.SongModel;
 import crescendo.base.song.Track;
 import crescendo.base.song.modifier.NoteModifier;
 
 public class MusicEngine extends JPanel implements ProcessedNoteEventListener,ComponentListener,Scrollable,FlowController,NoteEventListener {
 	private static final long serialVersionUID=1L;
 	
 	private static final float CLEF_THRESHOLD=0.25f;
 	
 	private static final int MARGIN=10;
 	private static final int HEADER_HEIGHT=52;
 	public static final int CLEF_WIDTH=24;
 	private static final int TIME_SIGNATURE_WIDTH=14;
 	private static final int ACCIDENTAL_WIDTH=6;
 	public static final int STAFF_LINE_HEIGHT=8;
 	public static final int STAFF_HEIGHT=4*STAFF_LINE_HEIGHT;
 	private static final int STAFF_MARGIN=STAFF_HEIGHT;
 	private static final int STAFF_SPACING=0;
 	
 	private static final String CLEF_TREBLE_PATH="resources/images/clef.treble.28x64.png";
 	private static final String CLEF_BASS_PATH="resources/images/clef.bass.28x64.png";
 	
 	private static final DrawableNote[] NOTE_PROTOTYPES=new DrawableNote[]{
 		new WholeNote(),
 		new HalfNote(),
 		new QuarterNote(),
 		new EighthNote(),
 		new SixteenthNote()
 	};
 	
 	private static final DrawableNote[] REST_PROTOTYPES=new DrawableNote[]{
 		new WholeRest(),
 		new HalfRest(),
 		new QuarterRest(),
 		new EighthRest(),
 		new SixteenthRest()
 	};
 	
	private static final Map<Integer,int[]> KEY_SIGNATURE_MAP=new HashMap<Integer,int[]> ();
 	static {
 		// fill in the map
 		// according to http://en.wikipedia.org/wiki/File:Circle_of_fifths_deluxe_4.svg
 		KEY_SIGNATURE_MAP.put(0,new int[] {});
 		KEY_SIGNATURE_MAP.put(-1,new int[] {4});
 		KEY_SIGNATURE_MAP.put(-2,new int[] {4,1});
 		KEY_SIGNATURE_MAP.put(-3,new int[] {4,1,5});
 		KEY_SIGNATURE_MAP.put(-4,new int[] {4,1,5,2});
 		KEY_SIGNATURE_MAP.put(-5,new int[] {4,1,5,2,6});
 		KEY_SIGNATURE_MAP.put(-6,new int[] {4,1,5,2,6,3});
 		KEY_SIGNATURE_MAP.put(-7,new int[] {4,1,5,2,6,3,7});
 		KEY_SIGNATURE_MAP.put(1,new int[] {0});
 		KEY_SIGNATURE_MAP.put(2,new int[] {0,3});
 		KEY_SIGNATURE_MAP.put(3,new int[] {0,3,-1});
 		KEY_SIGNATURE_MAP.put(4,new int[] {0,3,-1,2});
 		KEY_SIGNATURE_MAP.put(5,new int[] {0,3,-1,2,5});
 		KEY_SIGNATURE_MAP.put(6,new int[] {0,3,-1,2,5,1});
 		KEY_SIGNATURE_MAP.put(7,new int[] {0,3,-1,2,5,1,4});
	};
 	
 	private static int[] PITCH_INDEXES=new int[] {3,3,2,2,1,0,0,6,6,5,5,4};
 	
 	// images (want to make sure we only load these once for the entire program);
 	private static Image TREBLE_CLEF=null;
 	private static Image BASS_CLEF=null;
 	
 	private static void loadImages()
 	{
 		Toolkit tk=Toolkit.getDefaultToolkit();
 		TREBLE_CLEF=tk.createImage(CLEF_TREBLE_PATH);
 		BASS_CLEF=tk.createImage(CLEF_BASS_PATH);
 	}
 	
 	//data items
 	private SongModel model;
 	private List<Track> activeTracks;
 	private double beatsPerMeasure;
 	private double beatNote;
 	private int measureCount;
 	private List<TimingEdge> trebleEdges;
 	private List<TimingEdge> bassEdges;
 	
 	//alignment items
 	private boolean built;
 	private Map<Note,List<DrawableNote>> noteMap;
 	private List<Drawable> drawables;
 	private boolean titleShowing;
 	private int measureWidth=200; // maybe someday this won't be hardcoded
 	private int decoratorWidth=0;
 	private int measuresPerLine;
 	private double beatWidth;
 	private int barX;
 	private int barY;
 	private boolean trebleNeeded;
 	private boolean bassNeeded;
 	
 	//state items
 	private boolean paused;
 	private BeatTimer timer;
 	
 	public MusicEngine(SongModel model,List<Track> activeTracks){
 		this(model,activeTracks,true);
 	}
 
 	public MusicEngine(SongModel model,List<Track> activeTracks,boolean showTitle){
 		this.model=model;
 		this.activeTracks=activeTracks;
 		this.titleShowing=showTitle;
 		this.addComponentListener(this);
 		
 		this.beatsPerMeasure=model.getTimeSignature().getBeatsPerMeasure();
 		this.beatNote=model.getTimeSignature().getBeatNote();
 		this.measureCount=(int)Math.ceil(model.getDuration()/beatsPerMeasure);
 		
 		this.built=false;
 		
 		this.barX=-1;
 		this.barY=-1;
 		
 		this.paused=true;
 		this.timer=null;
 		
 		// determine if treble and/or bass are needed, and calculate edges
 		int trebleCount=0;
 		int bassCount=0;
 		int total=0;
 		for (Track track : this.activeTracks)
 		{
 			for (Note note : track.getNotes())
 			{
 				if (note.isPlayable())
 				{
 					total++;
 					if (isBassNote(note.getPitch()))
 					{
 						bassCount++;
 					}
 					else
 					{
 						trebleCount++;
 					}
 				}
 			}
 		}
 		trebleNeeded=(trebleCount>total*CLEF_THRESHOLD);
 		bassNeeded=(bassCount>total*CLEF_THRESHOLD);
 		
 		// if zero notes are given, neither staff would be shown, which could be confusing
 		if (!trebleNeeded && !bassNeeded)
 		{
 			ErrorHandler.showNotification("Note","You've opened an empty song!");
 			trebleNeeded=true;
 		}
 		
 		// calculate edges
 		double beat;
 		this.trebleEdges=new LinkedList<TimingEdge>();
 		this.bassEdges=new LinkedList<TimingEdge>();
 		for (Track track : this.activeTracks)
 		{
 			beat=0;
 			for (Note note : track.getNotes())
 			{
 				if (note.isPlayable())
 				{
 					TimingEdge rising=new TimingEdge(beat,TimingEdge.RISING);
 					TimingEdge falling=new TimingEdge(beat+note.getDuration(),TimingEdge.FALLING);
 					
 					if ((isBassNote(note.getPitch()) && bassNeeded) || (!isBassNote(note.getPitch()) && !trebleNeeded))
 					{
 						bassEdges.add(rising);
 						bassEdges.add(falling);
 					}
 					else
 					{
 						trebleEdges.add(rising);
 						trebleEdges.add(falling);
 					}
 				}
 				beat+=note.getDuration();
 			}	
 		}
 		// sort edges
 		Collections.sort(trebleEdges);
 		Collections.sort(bassEdges);
 	}
 	
 	private void build()
 	{
 		if (this.getWidth()>0) // if the width is 0... don't even try
 		{
 			// set some base values
 			this.decoratorWidth=TIME_SIGNATURE_WIDTH+Math.abs(ACCIDENTAL_WIDTH*this.model.getKeySignature());
 			this.measuresPerLine=(int)Math.floor((this.getWidth()-2*MARGIN-CLEF_WIDTH-this.decoratorWidth)/this.measureWidth);
 			int height=this.titleShowing?HEADER_HEIGHT:0;
 			height+=2*MARGIN;
 			int lineHeight=STAFF_HEIGHT+2*STAFF_MARGIN+STAFF_SPACING;
 			if (this.trebleNeeded && this.bassNeeded)
 			{
 				lineHeight+=STAFF_HEIGHT+STAFF_MARGIN;
 			}
 			height+=lineHeight*(int)Math.ceil(1.0*this.measureCount/this.measuresPerLine);
 			height+=2*STAFF_HEIGHT; // for good "measure" (GET IT?)
 			this.setPreferredSize(new Dimension(this.getWidth(),height));
 			this.beatWidth=1/this.beatNote*this.measureWidth;
 			
 			// add notes to list
 			this.noteMap=new HashMap<Note,List<DrawableNote>>();
 			this.drawables=new LinkedList<Drawable>();
 			for (Track track : this.activeTracks)
 			{
 				double beatOffset=0;
 				double leftInMeasure=this.beatsPerMeasure;
 				for (Note note : track.getNotes())
 				{
 					if (note.isPlayable())
 					{
 						noteMap.put(note,calculateNotes(note,leftInMeasure,beatOffset));
 					}
 					beatOffset+=note.getDuration();
 					leftInMeasure-=note.getDuration();
 					while (leftInMeasure<0)
 					{
 						leftInMeasure+=this.beatsPerMeasure;
 					}
 				}
 			}
 			
 			// determine if an extra rest is needed on the end of the measure
 			double actualLength=this.model.getDuration();
 			double fullLength=Math.ceil(actualLength/this.beatsPerMeasure)*this.beatsPerMeasure;
 			double endNoteLength=fullLength-actualLength;
 			
 			// add rests for both staves
 			if (trebleNeeded)
 			{
 				int level=0;
 				double lastTime=0;
 				for (TimingEdge edge : this.trebleEdges)
 				{
 					double duration=edge.getTime()-lastTime;
 					if (level==0 && duration>0)
 					{
 						this.drawables.addAll(createRests(lastTime,duration,false));
 					}
 					level+=edge.getShift();
 					lastTime=edge.getTime();
 				}
 				
 				if (lastTime<model.getDuration())
 				{
 					double diff=model.getDuration()-lastTime;
 					this.drawables.addAll(createRests(lastTime,diff,false));
 				}
 				
 				if (endNoteLength>0)
 				{
 					this.drawables.addAll(createRests(lastTime,endNoteLength,false));
 				}
 			}
 			if (bassNeeded)
 			{
 				int level=0;
 				double lastTime=0;
 				for (TimingEdge edge : this.bassEdges)
 				{
 					double duration=edge.getTime()-lastTime;
 					if (level==0 && duration>0)
 					{
 						this.drawables.addAll(createRests(lastTime,duration,true));
 					}
 					level+=edge.getShift();
 					lastTime=edge.getTime();
 				}
 				
 				if (lastTime<model.getDuration())
 				{
 					double diff=model.getDuration()-lastTime;
 					this.drawables.addAll(createRests(lastTime,diff,true));
 				}
 				
 				if (endNoteLength>0)
 				{
 					this.drawables.addAll(createRests(lastTime,endNoteLength,true));
 				}
 			}
 			
 			// good to go
 			this.built=true;
 		}
 		this.repaint();
 	}
 	
 	private List<DrawableNote> createRests(double beatOffset,double length,boolean forceBass) {
 		Note dummyNote=new Note(0,length,0,null,false);
 		double beatsLeft=Math.ceil(beatOffset/this.beatsPerMeasure)*this.beatsPerMeasure-beatOffset;
 		return this.calculateNotes(dummyNote,beatsLeft,beatOffset,forceBass);
 	}
 	
 	private List<DrawableNote> calculateNotes(Note note,double beatsLeft,double beatOffset) {
 		return calculateNotes(note,beatsLeft,beatOffset,false);
 	}
 	
 	private List<DrawableNote> calculateNotes(Note note,double beatsLeft,double beatOffset,boolean forceBass) {
 		double originalBeatsLeft=beatsLeft;
 		double originalOffset=beatOffset;
 		List<DrawableNote> ret=new LinkedList<DrawableNote>();
 		double duration=note.getDuration();
 		while (duration>beatsLeft)
 		{
 			ret.addAll(splitNote(note,beatsLeft,beatOffset,forceBass));
 			beatOffset+=beatsLeft;
 			duration-=beatsLeft;
 			beatsLeft=this.beatsPerMeasure;
 		}
 		if (duration>0)
 		{
 			ret.addAll(splitNote(note,duration,beatOffset,forceBass));
 		}
 		
 		// add created notes to list of drawables, with modifiers added
 		Drawable last=null;
 		for (DrawableNote dn : ret)
 		{
 			Drawable d=dn;
 			if (isAccidental(note.getPitch()) && !pitchInKeySignature(note.getPitch()))
 			{
 				if (this.model.getKeySignature()<0)
 				{
 					d=new Flat((DrawableNote)d);
 				}
 				else
 				{
 					d=new Sharp((DrawableNote)d);
 				}
 			}
 			else if (!isAccidental(note.getPitch()) && pitchInKeySignature(note.getPitch()))
 			{
 				d=new Natural((DrawableNote)d);
 			}
 			if (last!=null)
 			{
 				if (note.isPlayable())
 				{
 					last=new Tie(last,d);
 				}
 				this.drawables.add(last);
 			}
 			last=d;
 		}
 		if (last!=null)
 		{
 			this.drawables.add(last);
 		}
 		
 		// add all chorded notes
 		for (NoteModifier mod : note.getModifiers())
 		{
 			for (Note otherNote : mod.getNotes())
 			{
 				ret.addAll(calculateNotes(otherNote,originalBeatsLeft,originalOffset));
 			}
 		}
 		return ret;
 	}
 	
 	private List<DrawableNote> splitNote(Note note,double duration,double beatOffset,boolean forceBass) {
 		List<DrawableNote> ret=new LinkedList<DrawableNote>();
 		for (int i=0; i<NOTE_PROTOTYPES.length;)
 		{
 			DrawableNote[] prototypeArray=note.isPlayable()?NOTE_PROTOTYPES:REST_PROTOTYPES;
 			if (prototypeArray[i].getBeatsCovered()>duration)
 			{
 				i++;
 			}
 			else
 			{
 				DrawableNote spawned=prototypeArray[i].spawn(note,
 					getX(beatOffset,prototypeArray[i].getBeatsCovered(),prototypeArray[i].getWidth()),
 					getY(note,beatOffset,forceBass));
 				// add dot if it comes out EXACTLY right, otherwise assume ties will be used
 				if (spawned.getBeatsCovered()*1.5==duration)
 				{
 					spawned=new DottedNote(spawned);
 				}
 				ret.add(spawned);
 				duration-=spawned.getBeatsCovered();
 				beatOffset+=spawned.getBeatsCovered();
 			}
 		}
 		return ret;
 	}
 	
 	private int getX(double beats,double count,double width) {
 		// place on proper line
 		int xb=MARGIN+CLEF_WIDTH+this.decoratorWidth;
 		while (beats>=this.beatsPerMeasure*this.measuresPerLine)
 		{
 			beats-=this.beatsPerMeasure*this.measuresPerLine;
 		}
 		// place in proper measure
 		xb+=(int)(beats*this.beatWidth);
 		// center note in its allotted area
 		xb+=(int)(count*this.beatWidth-width)/2;
 		return xb;
 	}
 	
 	private int getY(Note n,double beats,boolean forceBass) {
 		int yb=MARGIN+STAFF_MARGIN+(this.titleShowing?HEADER_HEIGHT:0);
 		if (n.isPlayable())
 		{
 			int pitch=n.getPitch();
 			boolean bass=this.bassNeeded && isBassNote(pitch);
 			// is it a bass note, and are we showing a bass clef?
 			if (bass)
 			{
 				pitch+=21; // calculate as if it were a different pitch
 			}
 			// middle C (60) is one full bar down
 			yb+=STAFF_LINE_HEIGHT;
 			int sign=(int)Math.signum(72-pitch);
 			for (int i=pitch; i!=72; i+=sign)
 			{
 				if (!isAccidental(i))
 				{
 					yb+=sign*STAFF_LINE_HEIGHT/2;
 				}
 			}
 			// if both bass and treble are showing and this is a bass note, adjust the height to show on the proper staff
 			// (or if bass note is being forced)
 			if (bass && this.trebleNeeded || forceBass)
 			{
 				yb+=STAFF_HEIGHT+STAFF_MARGIN;
 			}
 		}
 		else // it's a rest, just give it the middle of the staff
 		{
 			yb+=STAFF_HEIGHT/2;
 			if (forceBass)
 			{
 				yb+=STAFF_HEIGHT+STAFF_MARGIN;
 			}
 		}
 		int staffHeight=STAFF_HEIGHT+2*STAFF_MARGIN+STAFF_SPACING;
 		if (this.trebleNeeded && this.bassNeeded)
 		{
 			staffHeight+=STAFF_HEIGHT+STAFF_MARGIN;
 		}
 		while (beats>=this.beatsPerMeasure*this.measuresPerLine)
 		{
 			beats-=this.beatsPerMeasure*this.measuresPerLine;
 			yb+=staffHeight;
 		}
 		return yb;
 	}
 	
 	private boolean pitchInKeySignature(int pitch) {
 		int kt=PITCH_INDEXES[pitch%12]; // expected pitch note
 		int[] ks=KEY_SIGNATURE_MAP.get(this.model.getKeySignature());
 		
 		for (int i=0; i<ks.length; i++)
 		{
 			if (ks[i]%7==kt)
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public void setSection(Note startNote,Note endNote) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void paint(Graphics g){
 		synchronized(MusicEngine.class)
 		{
 			if (this.getWidth()>0 && this.built)
 			{
 				// wipe it
 				g.setColor(Color.WHITE);
 				g.fillRect(0,0,this.getWidth(),this.getHeight());
 				// draw header
 				g.setColor(Color.BLACK);
 				if (this.titleShowing)
 				{
 					Font headerFont=new Font(Font.SERIF,Font.BOLD,24);
 					g.setFont(headerFont);
 					String title=this.model.getTitle();
 					int width=this.getFontMetrics(headerFont).stringWidth(title);
 					String creator=null;
 					for (Creator c : this.model.getCreators())
 					{
 						if (creator==null)
 						{
 							creator="";
 						}
 						else
 						{
 							creator+=", ";
 						}
 						creator+=c.getType()+": "+c.getName();
 					}
 					g.drawString(title,(this.getWidth()-width)/2,24);
 					headerFont=headerFont.deriveFont(Font.PLAIN,16);
 					g.setFont(headerFont);
 					width=this.getFontMetrics(headerFont).stringWidth(creator);
 					g.drawString(creator,(this.getWidth()-width)/2,40);
 				}
 				// draw staff lines
 				g.setColor(Color.BLACK);
 				int xb=MARGIN;
 				int yb=MARGIN+(this.titleShowing?HEADER_HEIGHT:0);
 				for (int i=0; i<Math.ceil(1.0*this.measureCount/this.measuresPerLine); i++)
 				{
 					int measures=this.measuresPerLine;
 					boolean last=false;
 					if (i==this.measureCount/this.measuresPerLine)
 					{
 						measures=this.measureCount%this.measuresPerLine;
 						last=true;
 					}
 					if (TREBLE_CLEF==null) // if either one is null, neither is loaded; only checking one is necessary
 					{
 						loadImages();
 					}
 					if (this.trebleNeeded)
 					{
 						yb+=STAFF_MARGIN;
 						this.drawStaff(g,true,xb,yb,measures,last);
 						yb+=STAFF_HEIGHT;
 					}
 					if (this.bassNeeded)
 					{
 						yb+=STAFF_MARGIN;
 						this.drawStaff(g,false,xb,yb,measures,last);
 						yb+=STAFF_HEIGHT;
 					}
 					yb+=STAFF_MARGIN+STAFF_SPACING;
 				}
 				// draw notes
 				for (Drawable draw : this.drawables)
 				{
 					draw.draw(g);
 				}
 				// draw line
 				if (this.barX>=0)
 				{
 					int barHeight=STAFF_HEIGHT+2*STAFF_MARGIN;
 					if (bassNeeded && trebleNeeded)
 					{
 						barHeight+=STAFF_HEIGHT+STAFF_MARGIN;
 					}
 					g.setColor(Color.RED);
 					g.drawLine(this.barX,this.barY,this.barX,this.barY+barHeight);
 				}
 			}
 		}
 	}
 	
 	private void drawStaff(Graphics g,boolean isTreble,int xb,int yb,int measures,boolean lastLine)
 	{
 		// draw clef
 		g.drawImage(isTreble?TREBLE_CLEF:BASS_CLEF,MARGIN,yb-2*STAFF_LINE_HEIGHT,CLEF_WIDTH,2*STAFF_HEIGHT,this);
 		// draw time signature
 		g.setFont(new Font(Font.SERIF,Font.BOLD,STAFF_HEIGHT/2));
 		g.drawString(Integer.toString((int)this.model.getTimeSignature().getBeatsPerMeasure()),xb+CLEF_WIDTH,yb+STAFF_HEIGHT/2-2);
 		g.drawString(Integer.toString((int)this.model.getTimeSignature().getBeatNote()),xb+CLEF_WIDTH,yb+STAFF_HEIGHT-2);
 		// draw key signature
 		int[] positions=KEY_SIGNATURE_MAP.get(this.model.getKeySignature());
 		for (int j=0; j<positions.length; j++)
 		{
 			int x=xb+CLEF_WIDTH+TIME_SIGNATURE_WIDTH+j*ACCIDENTAL_WIDTH;
 			int y=yb+(int)(positions[j]*0.5*STAFF_LINE_HEIGHT);
 			if (!isTreble) // it's bass clef, move down a line
 			{
 				y+=STAFF_LINE_HEIGHT;
 			}
 			if (this.model.getKeySignature()<0)
 			{
 				Flat.render(g,x,y-STAFF_LINE_HEIGHT);
 			}
 			else
 			{
 				Sharp.render(g,x,y-STAFF_LINE_HEIGHT/2);
 			}
 		}
 		// draw vertical lines
 		for (int j=0; j<measures; j++)
 		{
 			g.drawLine(xb+CLEF_WIDTH+this.decoratorWidth+(j+1)*this.measureWidth,yb,
 				xb+CLEF_WIDTH+this.decoratorWidth+(j+1)*this.measureWidth,yb+STAFF_HEIGHT);
 		}
 		// draw double line at end
 		if (lastLine)
 		{
 			g.drawLine(xb+CLEF_WIDTH+this.decoratorWidth+measures*this.measureWidth-3,yb,
 				xb+CLEF_WIDTH+this.decoratorWidth+measures*this.measureWidth-3,yb+STAFF_HEIGHT);
 		}
 		g.drawLine(xb,yb,xb,yb+STAFF_HEIGHT);
 		// draw horizontal lines
 		for (int j=0; j<5; j++)
 		{
 			if (j!=0)
 			{
 				yb+=STAFF_LINE_HEIGHT;
 			}
 			g.drawLine(xb,yb,xb+CLEF_WIDTH+this.decoratorWidth+measures*this.measureWidth,yb);
 		}
 	}
 
 	public void play(){
 		for (List<DrawableNote> noteList : this.noteMap.values())
 		{
 			for (DrawableNote note : noteList)
 			{
 				note.reset();
 			}
 		}
 		this.barX=MARGIN+CLEF_WIDTH+this.decoratorWidth;
 		this.barY=MARGIN+(this.titleShowing?HEADER_HEIGHT:0);
 		this.paused=false;
 		this.timer=new BeatTimer();
 	}
 
 	public void pause(){
 		this.paused=true;
 	}
 
 	public void stop(){
 		this.paused=true;
 		if (this.timer!=null)
 		{
 			this.timer.destroy();
 		}
 		this.timer=null;
 	}
 
 	public void resume(){
 		this.paused=false;
 	}
 	
 	public void suspend()
 	{
 		this.paused=true;
 	}
 	
 	public void songEnd()
 	{
 		this.paused=true;
 		this.timer.destroy();
 		this.timer=null;
		EventDispatcher.getInstance().detachAllMidi();
 	}
 	
 	public void handleNoteEvent(NoteEvent e) {
 		if (this.activeTracks.contains(e.getNote().getTrack()) && e.getAction()==NoteAction.BEGIN)
 		{
 			List<DrawableNote> noteList=this.noteMap.get(e.getNote());
 			if (noteList!=null)
 			{
 				if (noteList.size()>0)
 				{
 					DrawableNote note=noteList.get(0);
 					this.barX=note.getX()+note.getWidth()/2;
 					int staffHeight=STAFF_HEIGHT+2*STAFF_MARGIN+STAFF_SPACING;
 					if (this.trebleNeeded && this.bassNeeded)
 					{
 						staffHeight+=STAFF_HEIGHT+STAFF_MARGIN;
 					}
 					this.barY=MARGIN+(this.titleShowing?HEADER_HEIGHT:0)+((note.getY()-MARGIN-(this.titleShowing?HEADER_HEIGHT:0))/staffHeight*staffHeight);
 					// scroll to bar in view
 					// get measure in which note happened
 					int measureX=(this.barX-MARGIN)/this.measureWidth*this.measureWidth+MARGIN;
 					Rectangle view=new Rectangle(measureX,this.barY-STAFF_MARGIN,this.measureWidth*2,2*staffHeight);
 					this.scrollRectToVisible(view);
 					this.repaint();
 				}
 			}
 		}
 	}
 
 	@Override
 	public void handleProcessedNoteEvent(ProcessedNoteEvent e) {
 		if (e.getExpectedNote()!=null && e.getExpectedNote().getAction()==NoteAction.BEGIN && e.getExpectedNote().getNote()!=null && this.noteMap.keySet().contains(e.getExpectedNote().getNote()))
 		{
 			for (DrawableNote note : this.noteMap.get(e.getExpectedNote().getNote()))
 			{
 				note.setCorrect(e.isCorrect());
 				this.repaint();
 			}
 		}
 		
 		if (e.getPlayedNote()!=null && !e.isCorrect() && this.timer!=null && !this.paused &&e.getPlayedNote().getAction()==ActionType.PRESS)
 		{
 			double offset=this.timer.getBeatOffset();
 			double beatsLeft=2;
 			Note dummyNote=new Note(e.getPlayedNote().getNote(),1,100,null);
 			DrawableNote note=this.calculateNotes(dummyNote,beatsLeft,offset).get(0);
 			note.setCorrect(false);
 			synchronized(MusicEngine.class)
 			{
 				this.drawables.add(note);
 			}
 			this.repaint();
 		}
 	}
 	
 	public static boolean isAccidental(int n)
 	{
 		return n%12==1 || n%12==3 || n%12==6 || n%12==8 || n%12==10;
 	}
 	
 	public static boolean isBassNote(int n)
 	{
 		return n<60;
 	}
 
 	public void componentHidden(ComponentEvent arg0) {}
 	public void componentMoved(ComponentEvent arg0) {}
 	public void componentShown(ComponentEvent arg0) {}
 	
 	public void componentResized(ComponentEvent arg0)
 	{
 		synchronized (MusicEngine.class)
 		{
 			this.build();
 			this.updateUI();
 		}
 	}
 
 	@Override
 	public Dimension getPreferredScrollableViewportSize() {
 		return getPreferredSize();
 	}
 
 	@Override
 	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
 		return 10;
 	}
 
 	@Override
 	public boolean getScrollableTracksViewportHeight() {
 		return false;
 	}
 
 	@Override
 	public boolean getScrollableTracksViewportWidth() {
 		return true;
 	}
 
 	@Override
 	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
 		return 1;
 	}
 	
 	/** A data object representing a shift at a given time on an arbitrary scale. **/
 	private class TimingEdge implements Comparable<TimingEdge>
 	{
 		public static final int RISING=1;
 		public static final int FALLING=-1;
 		
 		private double time;
 		private int shift;
 		
 		public TimingEdge(double time,int shift)
 		{
 			this.time=time;
 			this.shift=shift;
 		}
 		
 		public int compareTo(TimingEdge other)
 		{
 			return (int) Math.signum(this.time-other.time);
 		}
 		
 		public double getTime()
 		{
 			return this.time;
 		}
 		
 		public int getShift()
 		{
 			return this.shift;
 		}
 	}
 	
 	private class BeatTimer
 	{
 		private Thread thread;
 		private long elapsedTime;
 		private long lastRun;
 		
 		public BeatTimer()
 		{
 			this.elapsedTime=0;
 			this.lastRun=0;
 			this.thread=new Thread(new Runnable() {
 				public void run() {
 					while (!Thread.interrupted())
 					{
 						long time=System.currentTimeMillis();
 						if (lastRun!=0 && !paused)
 						{
 							elapsedTime+=time-lastRun;
 						}
 						lastRun=time;
 						try
 						{
 							Thread.sleep(5);
 						}
 						catch (InterruptedException e)
 						{
 						}
 					}
 				}
 			});
 			this.thread.start();
 		}
 		
 		public void destroy()
 		{
 			this.thread.interrupt();
 		}
 		
 		public double getBeatOffset()
 		{
 			double beatsPerMS=model.getBPM()/60000.0;
			return beatsPerMS*this.elapsedTime-0.5;
 		}
 	}
 }
