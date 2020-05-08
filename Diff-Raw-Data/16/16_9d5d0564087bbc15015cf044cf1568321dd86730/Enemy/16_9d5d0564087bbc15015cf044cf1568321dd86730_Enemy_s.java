 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.Stack;
 
 
 public class Enemy extends GameCharacter {
 	private Stack<Node> path;
 	private Node goal;
 
 	public Enemy(float x, float y, Color color) {
 		super(x, y, 0, color);
 		Model model = SpectroPolaris.frame().gamePanel().model();
 		path = model.findPath(d_x, d_y, model.hill().x, model.hill().y);
 		goal = path.pop();
		System.out.println(d_x + ", " + d_y + " to " + goal.x() + ", " + goal.y());
 	}
 
 	
 	public void step() {
 		Model model = SpectroPolaris.frame().gamePanel().model();
 		
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
 		// Debug draw, DNE
 		Node array[] = new Node[path.size()];
 		path.copyInto(array);
 		for(Node node: array) {
 			g2d.setColor(Color.GREEN);
 			g2d.drawRect(node.x() * 10, node.y() * 10, 10, 10);
 		}
 		
 		super.draw(g2d);
 	}
 }
