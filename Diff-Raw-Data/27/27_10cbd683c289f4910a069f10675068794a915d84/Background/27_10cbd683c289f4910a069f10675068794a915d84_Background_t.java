 package tilemap;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import javax.imageio.ImageIO;
 
 import main.GamePanel;
 
 public class Background {
 	private BufferedImage image;
 	private double x;
 	private double y;
 	private double dx;
 	private double dy;
 
 	private double moveScale;
 
 	public Background(String path, double moveScale) {
 		try {
 			image = ImageIO.read(getClass().getResourceAsStream(path));
 			this.moveScale = moveScale;
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setPosition(double x, double y) {
 		this.x = (x * moveScale) % GamePanel.WIDTH;
 		this.y = (y * moveScale) % GamePanel.HEIGHT;
 	}
 
 	public void setScroll(double dx, double dy) {
 		this.dx = dx;
 		this.dy = dy;
 	}
 
 	public void update() {
 		x += dx;
 		y += dy;
 	}
 
 	public void draw(Graphics2D g) {
 		g.drawImage(image, (int) x, (int) y, null);
 
 		if (x < 0) {
 			g.drawImage(image, (int) x + GamePanel.WIDTH, (int) y, null);
 		}
 		
 		if (x > 0) {
 			g.drawImage(image, (int) x - GamePanel.WIDTH, (int) y, null);
 		}
 		
		if ((x + image.getWidth()) < 0 || (x - image.getWidth() > 0)) {
			x = 0;
		}
 
 	}
 
 }
