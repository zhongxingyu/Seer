 package beatbots.simulation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.tiled.TiledMap;
 
 public class Sequence implements Entity, MetronomeListener {
 	
 	private Metronome metronome;
 	
 	private List<NoteColor> sequence;
 	
 	private Queue<NoteColor> recording;
 	
 	private Sound soundRed;
 	private Sound soundBlue;
 	private Sound soundYellow;
 	private Sound soundMagenta;
 	private Sound soundOrange;
 	private Sound soundGreen;
 	
 	private int score;
 
 	@Override
 	public boolean isActive() {
 		
 		return true;
 	}
 	
 	public int getScore() {
 		
 		if ((this.metronome.getBeatCount() == 0) || (this.sequence.isEmpty())) {
 			
 			return 50;
 		}
 		else {
 			
 			return (this.score * 100) / Math.min(this.metronome.getBeatCount(), this.sequence.size());
 		}
 	}
 	
 	public Sequence(Metronome metronome) {
 		
 		super();
 		
 		this.metronome = metronome;
 		
 		this.sequence = new ArrayList<NoteColor>();
 		
 		this.recording = new ConcurrentLinkedQueue<NoteColor>();
 	}
 
 	@Override
 	public void init(GameContainer container) throws SlickException {
 		
 		this.score = 0;
 		
 		this.sequence.clear();
 		
		TiledMap tiledMap = new TiledMap("assets/maps/Song0.tmx");
 
 		for (int x = 0; x < tiledMap.getWidth(); x++) {
 			
 			for (int y = 0; y < tiledMap.getHeight(); y++) {
 				
 				for (int l = 0; l < tiledMap.getLayerCount(); l++) {
 					
 					int tileId = tiledMap.getTileId(x, y, l);
 					
 					int c = Integer.parseInt(tiledMap.getTileProperty(tileId, "Color", "-1"));
 					
 					this.sequence.add(Utils.getNoteColor(c));
 				}
 			}
 		}
 		
 		this.soundRed = new Sound("assets/sfx/Red.wav");
 		this.soundBlue = new Sound("assets/sfx/Blue.wav");
 		this.soundYellow = new Sound("assets/sfx/Yellow.wav");
 		this.soundMagenta = new Sound("assets/sfx/Magenta.wav");
 		this.soundOrange = new Sound("assets/sfx/Orange.wav");
 		this.soundGreen = new Sound("assets/sfx/Green.wav");
 		
 		this.metronome.addListener(this);
 	}
 
 	@Override
 	public void update(GameContainer container, int delta) throws SlickException {
 		
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics graphics) {
 		
 		int x = 0;
 		int y = 4;
 		
 		for (int i = this.metronome.getBeatCount(); i < Math.min(this.sequence.size(), this.metronome.getBeatCount() + 20); i++) {
 			
 			graphics.setColor(Utils.getColor(this.sequence.get(i)));
 			
 			graphics.drawRect(x, y, 28, 28);
 			
 			if (i == this.metronome.getBeatCount()) {
 				
 				graphics.fillRect(x + 4, y + 4, 20, 20);
 			}
 			
 			x += 32;
 		}
 		
 		graphics.drawString(Integer.toString(this.getScore()) + "%", 4, 32);
 	}
 
 	@Override
 	public void deactivate() {
 		
 	}
 
 	@Override
 	public void destroy(GameContainer gameContainer) {
 		
 		this.metronome.removeListener(this);
 	}
 
 	@Override
 	public void beat(int beatCount) {
 		
 		if (!this.recording.isEmpty()) {
 			
 			if (this.recording.peek() == this.sequence.get(beatCount)) {
 				
 				this.score++;
 			}
 			
 			while (!this.recording.isEmpty()) {
 				
 				switch (this.recording.remove()) {
 				
 				case Red: 
 					
 					this.soundRed.play();
 					
 					break;
 					
 				case Blue: 
 					
 					this.soundBlue.play();
 					
 					break;
 					
 				case Yellow: 
 					
 					this.soundYellow.play();
 					
 					break;
 					
 				case Magenta: 
 					
 					this.soundMagenta.play();
 					
 					break;
 					
 				case Orange:
 					
 					this.soundOrange.play();
 					
 					break;
 					
 				case Green:
 					
 					this.soundGreen.play();
 					
 					break;
 				}
 			}
 		}
 	}
 	
 	public void record(NoteColor noteColor) {
 		
 		this.recording.add(noteColor);
 	}
 }
