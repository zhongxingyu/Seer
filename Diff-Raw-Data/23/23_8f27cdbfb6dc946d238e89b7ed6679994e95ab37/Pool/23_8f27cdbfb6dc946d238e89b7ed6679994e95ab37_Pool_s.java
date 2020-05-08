 import java.awt.Graphics2D;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 
 public class Pool extends GraphicsObject {
 	
 	private Hitbox hitbox;
 
 	public Pool(String spritePath) {		
 		try {
 			sprite = ImageIO.read(getClass().getResource(spritePath));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		velocX = velocY = 0;
		hitbox = new Hitbox(getReferencePoint()[0]+25, getReferencePoint()[1]+ 25, this.getWidth()-100, 5, sprite.getHeight(null));
 	}
 
 	@Override
 	public void render(Graphics2D g) {
 		g.drawImage(sprite, 0, -sprite.getHeight(null), null);
 		hitbox.render(g);
 
 	}
 	
 	@Override
 	public void moveTo(double x, double y) {
 		this.posX = x;
 		this.posY = y;
 
 		hitbox.setCord(this.posX, y-getHeight());
 	}
 	
 	@Override
 	public double[] getReferencePoint() {
 		double[] points = new double[2];
 		points[0] = this.getPosX();
 		points[1] = this.getPosY();
 		
 		return points;
 	}
 	
 	public GraphicsObject getHitbox(){
 		return hitbox;
 	}
 }
