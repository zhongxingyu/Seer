 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.Stack;
 
 
 public class Enemy extends GameCharacter {
 	private Stack<Node> path;
 	private Node goal;
 	private int lastPlayerPath;
	private int lastShotFired;
 	private long d_timeSinceLastBullet = 0;
 
 	public Enemy(float x, float y, Color color) {
 		super(x, y, 0, color);
 		Model model = SpectroPolaris.frame().gamePanel().model();
 		path = model.findPath(d_x, d_y, model.hill().x, model.hill().y);
 		goal = path.pop();
 		lastPlayerPath = 0;
		lastShotFired = 0;
 	}
 
 	
 	public void step() {
 		Model model = SpectroPolaris.frame().gamePanel().model();
 		
 		d_speed = 1;
 		
 		Player player = model.closestPlayer(d_x, d_y, 100);
		if(player != null && lastPlayerPath < 0) {
			if(System.nanoTime() - d_timeSinceLastBullet > 250000000 && player.distanceFrom(d_x, d_y) < 80 && model.visible(d_x, d_y, player.d_x, player.d_y) == null) {
 				d_speed = 0;
				model.addBullet().instantiate(d_x, d_y, (float) Math.atan2(player.d_x - d_x, player.d_y - d_y), id());
				d_timeSinceLastBullet = System.nanoTime();
 				return;
			} 
			lastShotFired++;
 			
 			path = model.findPath(d_x, d_y, player.x(), player.y());
 			path.pop();
 			lastPlayerPath = 5;
 		} else {
 			lastPlayerPath--;
 		}
 		
 		if(model.inTile(d_x, d_y, goal.x(), goal.y())) {
 			if(path.isEmpty()) {
 				path = model.findPath(d_x, d_y, model.hill().x, model.hill().y);
 			}
 			goal = path.pop();
 			d_direction = (float) Math.atan2(goal.x() * model.tileSize() + 5 - d_x, goal.y() * model.tileSize() + 5 - d_y);
 			// System.out.println("New goal: from " + d_x + ", " + d_y + " to " + goal.x() * 10 + ", " + goal.y() * 10 + ". New direction: " + d_direction);
 		} 
 		
 		super.step();
 	}
 	
 	@Override
 	public void draw(Graphics2D g2d) {
 		if(SpectroPolaris.frame().gamePanel().model().isDrawPaths()) {
 			g2d.setColor(Color.GREEN);
 			Node array[] = new Node[path.size()];
 			path.copyInto(array);
 			for(Node node: array) {
 				g2d.drawRect(node.x() * 10, node.y() * 10, 10, 10);
 			}
 		}
 		
 		super.draw(g2d);
 	} 
 }
