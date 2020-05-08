 package crescendo.sheetmusic;
 
 import java.awt.Canvas;
 import java.awt.Graphics;
 import java.awt.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import crescendo.base.ProcessedNoteEvent;
 import crescendo.base.ProcessedNoteEventListener;
 import crescendo.base.song.Note;
 import crescendo.base.song.SongModel;
 
 public class MusicEngine extends Canvas implements ProcessedNoteEventListener {
 	
 	private Thread timerThread;
 	private boolean isLooping;
 	private SongModel songModel;
 	private Map<Note,DrawableNote> notes;
 	private double currentPosition;
 	private Note sectionStartNote;
 	private Note sectionEndNote;
 	private ArrayList<Drawable> drawables;
 	
 	
 	
 	
 	
 	int measuresPerLine = 4;
 	int sheetMusicWidth = 860;
 	double measureWidth = sheetMusicWidth/measuresPerLine;
 	double xMargin=80;
 	double yMargin=80;
 	double yMeasureDistance=300;
 	double noteOffset = 60/measuresPerLine; //so the first note isnt on the measure line
 	
 
 	public MusicEngine(SongModel model){
 		this.setSize(1024, 8000);
 		timerThread = new Thread(new MusicEngineTimer());
 		isLooping = false;
 		songModel = model;
 		notes = new HashMap<Note,DrawableNote>();
 		drawables = new ArrayList<Drawable>();
 		currentPosition = 0.0;
 		sectionStartNote = null;
 		sectionEndNote = null;
 		
 		
 		
 
 		
 		
 		
 		
 		//Get our drawables ready
		LinkedList<Note> notes =(LinkedList<Note>) songModel.getTracks().get(0).getNotes();
 		
 		System.out.println("Notes : " + notes.size() + " \n");
 		
 		
 		
 		
 		
 		double beatsPerMeasure = songModel.getTimeSignature().getBeatsPerMeasure();
 		double beatNote = songModel.getTimeSignature().getBeatNote();
 		
 		
 		
 		double currentBeatCount = 0;
 		double currentMeasure=0;
 		
 		int x=0;
 		int y=0;
 		
 		for(int i=0;i<notes.size();i++)
 		{
 			double noteBeat = notes.get(i).getDuration();
 			
 //			if(notes.get(i).getDynamic()==0)pitch = 66;
 			
 			x = (int) ((currentMeasure%measuresPerLine * measureWidth) + (((currentBeatCount)/beatsPerMeasure)*measureWidth) + xMargin + noteOffset);
 			y = (int) (300*((currentMeasure-(currentMeasure%measuresPerLine))/measuresPerLine) + yMargin);
 			
 			//if a "beat" is a half note
 			if(beatNote==2)
 			{
 				if(notes.get(i).getDynamic()==0);//drawables.add(new Rest(notes.get(i),x));
 				else 
 				{
 					if(noteBeat==0.25)drawables.add(new EighthNote(notes.get(i),x,y));
 					if(noteBeat==0.5)drawables.add(new QuarterNote(notes.get(i),x,y));
 					if(noteBeat==1)drawables.add(new HalfNote(notes.get(i),x,y));
 					if(noteBeat==2)drawables.add(new WholeNote(notes.get(i),x,y));
 				}
 			}
 			//if a "beat" is a quarter note
 			else if(beatNote==4)
 			{
 				if(notes.get(i).getDynamic()==0);//drawables.add(new Rest(notes.get(i),x));
 				else 
 				{
 					if(noteBeat==0.5)drawables.add(new EighthNote(notes.get(i),x,y));
 					if(noteBeat==1)drawables.add(new QuarterNote(notes.get(i),x,y));
 					if(noteBeat==2)drawables.add(new HalfNote(notes.get(i),x,y));
 					if(noteBeat==4)drawables.add(new WholeNote(notes.get(i),x,y));
 				}
 			}
 			//if a "beat" is a eighth note
 			else if(beatNote==8)
 			{
 				if(notes.get(i).getDynamic()==0);//drawables.add(new Rest(notes.get(i),x));
 				else 
 				{
 					if(noteBeat==1)drawables.add(new EighthNote(notes.get(i),x,y));
 					if(noteBeat==2)drawables.add(new QuarterNote(notes.get(i),x,y));
 					if(noteBeat==4)drawables.add(new HalfNote(notes.get(i),x,y));
 					if(noteBeat==8)drawables.add(new WholeNote(notes.get(i),x,y));
 				}
 			}
 			
 			currentBeatCount+=noteBeat;
 			while(currentBeatCount>=beatsPerMeasure)
 			{
 				currentBeatCount-=beatsPerMeasure;
 				currentMeasure++;
 			}
 		}
 	}
 
 	public MusicEngine(SongModel model, double currentPosition){
 		this(model);
 		this.currentPosition = currentPosition;
 		//use logic for finding the number of beats in a song
 	}
 	
 	public MusicEngine(SongModel model, Note currentNote){
 		this(model);
 		this.sectionStartNote = currentNote;
 	}
 	
 	public MusicEngine(SongModel model, Note currentSectionBeginNote, Note currentSectionEndNote){
 		this(model);
 		sectionStartNote = currentSectionBeginNote;
 		sectionEndNote = currentSectionEndNote;
 	}
 	
 	public void setLooping(boolean looping){
 		isLooping = looping;
 	}
 	
 	public void setPosition(double position){
 		currentPosition = position;
 	}
 	
 	public void setPosition(Note position){
 		sectionStartNote = position;
 	}
 	
 	public void setSection(Note sectionStartNote, Note sectionEndNote){
 		this.sectionStartNote = sectionStartNote;
 		this.sectionEndNote = sectionEndNote;
 	}
 	
 	
 	
 	
 	@Override
 	public void paint(Graphics g){
 		super.paint(g);
 		
 		
 		//draw staffs
 		for(int j=0;j<20;j++)
 		{	
 			g.drawLine((int)xMargin,(int)(yMargin+(yMeasureDistance*j)),(int)(xMargin),(int)(yMargin+194+(yMeasureDistance*j)));
 			for(int k=1;k<=measuresPerLine;k++)
 				g.drawLine((int)(xMargin+measureWidth*k),(int)(yMargin+(yMeasureDistance*j)),(int)(xMargin+measureWidth*k),(int)(yMargin+194+(yMeasureDistance*j)));
 			
 			//draw top staff
 			for(int i=0;i<5;i++)
 				g.drawLine((int)(xMargin),(int)(yMargin+(16*i)+(yMeasureDistance*j)),(int)(xMargin+measureWidth*measuresPerLine),(int)(yMargin+(16*i)+(yMeasureDistance*j)));
 			
 			//draw bottom staff
 			for(int i=0;i<5;i++)
 				g.drawLine((int)(xMargin),(int)(yMargin+130+(16*i)+(yMeasureDistance*j)),(int)(xMargin+measureWidth*measuresPerLine),(int)(yMargin+130+(16*i)+(yMeasureDistance*j)));
 		}
 		
 		
 		
 		for(Drawable d: drawables)d.draw(g);
 		
 	}
 	
 	public void play(){
 		
 	}
 	
 	public void pause(){
 		
 	}
 	
 	public void stop(){
 		
 	}
 	
 	public void resume(){
 		
 	}
 	
 	@Override
 	public void handleProcessedNoteEvent(ProcessedNoteEvent e) {
 		if(e.getExpectedNote()==null){
 			//Create a new incorrect note
 		}else{
 			//notes.get(e.getExpectedNote()).setCorrect(e.isCorrect());
 		}
 	}
 	
 	private class MusicEngineTimer implements Runnable{
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 	
 }
