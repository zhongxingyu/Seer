 package taskSimulate;
 
 import java.awt.*;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 import order.Location;
 import order.Product;
 
 public class OrderPickingTaskSimulation extends JPanel {
 	private ArrayList<TravelingSalesmanProblem> problems;
 	private int currentProblem = 0;
 
 	public OrderPickingTaskSimulation(
 			ArrayList<TravelingSalesmanProblem> problems) {
 		super();
 
 		setPreferredSize(new Dimension(400, 300));
 
 		this.problems = problems;
 	}
 
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		g.setColor(Color.BLACK);
 
 		// Hier wordt het magazijn getekend in 20x10
 		for (int y = 0; y <= 19; y++) {
 			for (int x = 0; x <= 9; x++) {
 				g.drawRect((x * 20), (y * 20), 20, 20);
 			}
 		}
 
 		Location lastLocation = null;
 		g.setColor(Color.BLUE);
 		Graphics2D g2D = (Graphics2D) g;
 		g2D.setStroke(new BasicStroke(3));
		
 			for (ArrayList<Product> robot : problems.get(currentProblem)
 					.getProblem()) {
 				for (Product product : robot) {
 					Location productLocation = product.getLocation();
 					g.fillRect(productLocation.x * 20 + 5,
 							productLocation.y * 20 + 5, 10, 10);
 					if (lastLocation != null)
 						g2D.drawLine(lastLocation.x * 20 + 10,
 								lastLocation.y + 10 * 20 + 10,
 								productLocation.x * 20 + 10,
 								productLocation.y * 20 + 10);
 					lastLocation = productLocation;
 				}
 				g.setColor(Color.RED);
 			}
 	}
 
 	public void nextProblem() {
 		currentProblem++;
 		repaint();
 	}
 
 	public void previousProblem() {
 		currentProblem--;
 		repaint();
 	}
 }
