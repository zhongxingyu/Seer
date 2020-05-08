 package cs447.PuzzleFighter;
 
 import jig.engine.RenderingContext;
 import jig.engine.Sprite;
 import jig.engine.util.Vector2D;
 
 public class Gem extends Sprite {
 	private PlayField pf;
 
 	public GemType type;
 	public Vector2D pos;
 
 	public int gemWidth;
 	public int gemHeight;
 
 	public Gem(PlayField pf, Vector2D pos, GemType type) {
 		this(pf, pos, type, "redGem");
 	}
 
 	public Gem(PlayField pf, Vector2D pos, GemType type, String resource) {
 		super(resource);
 		this.pf = pf;
 		this.pos = pos;
 		this.type = type;
 		gemWidth = 1;
 		gemHeight = 1;
 	}
 
 	public void render(RenderingContext rc) {
 		for (int dx = 0; dx < gemWidth; dx++) {
 			for (int dy = 0; dy < gemHeight; dy++) {
 				position = pos.translate(new Vector2D(dx, dy)).scale(new Vector2D(25, 25));
 				super.render(rc);
 			}
 		}
 	}
 
 	public boolean move(Vector2D dv) {
 		Vector2D newPos = pos.translate(dv);
 		// TODO: Think about fixing for horizontal movement?
 		for (int dx = 0; dx < gemWidth; dx++) {
			if (pf.isFilled(newPos.translate(new Vector2D(dx, 0)))) {
 				return false;
 			}
 		}
 
 		for (int dy = 0; dy < gemHeight; dy++) {
 			for (int dx = 0; dx < gemWidth; dx++) {
 				pf.clear(pos.translate(new Vector2D(dx, dy)));
 			}
 		}
 
 		for (int dy = 0; dy < gemHeight; dy++) {
 			for (int dx = 0; dx < gemWidth; dx++) {
 				pf.set(newPos.translate(new Vector2D(dx, dy)), this);
 			}
 		}
 
 		pos = newPos;
 		return true;
 	}
 
 	public boolean rotateClockwise(Vector2D about) {
 		Vector2D newPos = pos.translate(about.scale(-1));
 		newPos = new Vector2D(-newPos.getY(), newPos.getX());
 		newPos = newPos.translate(about);
 		if (pf.isFilled(newPos)) {
 			return false;
 		}
 		pf.clear(pos);
 		pf.set(newPos, this);
 		pos = newPos;
 		return true;
 	}
 
 	public boolean rotateCounterClockwise(Vector2D about) {
 		Vector2D newPos = pos.translate(about.scale(-1));
 		newPos = new Vector2D(newPos.getY(), -newPos.getX());
 		newPos = newPos.translate(about);
 		if (pf.isFilled(newPos)) {
 			return false;
 		}
 		pf.clear(pos);
 		pf.set(newPos, this);
 		pos = newPos;
 		return true;
 	}
 }
