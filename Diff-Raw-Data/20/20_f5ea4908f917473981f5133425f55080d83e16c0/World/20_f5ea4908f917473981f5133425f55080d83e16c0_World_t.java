 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.util.HashSet;
import java.util.Set;
 
 public class World {
 	
	private Set<GraphicsObject> graphicsobjects = new HashSet<GraphicsObject>(0); // Setting initial capacity to 0 to prevent NULL's
 	private Background background;
 	private Dimension worldDimension;
 	private Katt katten;
 	private Camera camera;
 	private Ramp rampen;
 	private Pool poolen;
 
 	
 	public World(String bgPath) {
 		background = new Background(bgPath);
 		worldDimension = new Dimension((int)background.getSpriteWidth(), (int)background.getSpriteHeight());
 		camera = new Camera(new Dimension(1200, 600));
 		camera.setLimits(worldDimension);
 		
 		katten = new Katt("katt.png");
 		katten.setLimits(worldDimension);
 		katten.moveTo(0, worldDimension.getHeight());
 		
 		rampen = new Ramp(200, 100);
 		rampen.setPosX(800);
 		rampen.setPosY(worldDimension.getHeight());
 		
		poolen = new Pool("pool.png");
		poolen.moveTo(worldDimension.getWidth(), worldDimension.getHeight());
		
 		camera.setTarget(rampen);
 		camera.animateTo(katten, 300);
 		
 		graphicsobjects.add(background);
 		graphicsobjects.add(katten);
 		graphicsobjects.add(rampen);
 		graphicsobjects.add(poolen);
 	}
 	
 	public void render(Graphics2D g) {
 		// Looping thru all GOs
 		for(GraphicsObject go : graphicsobjects) {
 			g.translate(getScreenCoords(go)[0], getScreenCoords(go)[1]);
 				go.render(g);
 			g.translate(-getScreenCoords(go)[0], -getScreenCoords(go)[1]);
 		}
 	}
 	
 	public void update(Controller controller) {
 		katten.setControllable(camera.isAnimationDone());
 		katten.update(controller);
 		camera.update();
 		background.update(controller);
 	}
 	
 	public Dimension getCameraDimension() {
 		return camera.getDimension();
 	}
 	
 	/**
 	 * Returns the actual coordinates on screen.
 	 * By subtracting the camera position from given GOs position
 	 * @param go
 	 * @return
 	 */
 	public double[] getScreenCoords(GraphicsObject go) {
 		double[] screenCoords = new double[2];
 		screenCoords[0]	= go.getPosX() - camera.getPosX();
 		screenCoords[1] = go.getPosY() - camera.getPosY();
 		
 		return screenCoords;
 	}
 }
