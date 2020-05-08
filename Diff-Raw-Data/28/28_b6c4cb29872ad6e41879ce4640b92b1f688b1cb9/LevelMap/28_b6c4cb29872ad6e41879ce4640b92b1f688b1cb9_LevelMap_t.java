 package devhead.ru.jlaby;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 
 @SuppressWarnings("serial")
 public class LevelMap extends JComponent implements Ant {
 
 	private HashMap<Point, Field> levelMap;
 	private Dimension dimension;
 	private Point antPosition;
 
	public LevelMap(String levelPath) {
	    LoadLevel(levelPath);
	    this.setMinimumSize(new Dimension(600,400));
	}
 
 	public void LoadLevel(String levelPath) {
 		levelMap = new HashMap<Point, Field>();
 		dimension = new Dimension(0, 0);
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(levelPath));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		String line;
 		String flag = "";
 		int y = 0;
 		try {
 			while ((line = reader.readLine()) != null) {
 				if (line.contains("#")) {
 					continue;
 				} else if (line.equalsIgnoreCase("Map:")) {
 					flag = "map";
 					continue;
 				} else if (line.equals("")) {
 					flag = "";
 					continue;
 				}
 				if (flag.equals("map")) {
 					String mapLine[] = line.split(" ");
 					int x;
 					for (x = 0; x < mapLine.length; x++) {
						Point point = new Point(x, dimension.height);
 						levelMap.put(point, new Field(mapLine[x]));
 						if (levelMap.get(point).isAnt()) {
 							antPosition = point;
 						}
 					}
 					if (dimension.width < mapLine.length) {
						dimension.width = mapLine.length;
 					}
 					dimension.height++;
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	
 	public void paintComponent(Graphics g) {
 		for (int y = 0; y < dimension.height; y++) {
 			for (int x = 0; x < dimension.width; x++) {
 				ImageIcon imageIcon = levelMap.get(new Point(x, y)).getImageIcon();
 				imageIcon.paintIcon(this, g, x * imageIcon.getIconWidth(),
 						y * imageIcon.getIconHeight());
 			}
 		}
 	}
 
 	@Override
 	public void right() {
 		Field antField = levelMap.get(antPosition);
 		if (antField.getType().equals("↑")) {
 			antField.setType("→");
 		} else if (antField.getType().equals("↓")) {
 			antField.setType("←");
 		} else if (antField.getType().equals("←")) {
 			antField.setType("↑");
 		} else if (antField.getType().equals("→")) {
 			antField.setType("↓");
 		}
 		this.repaint();
 	}
 
 	@Override
 	public void left() {
 		Field antField = levelMap.get(antPosition);
 		if (antField.getType().equals("↑")) {
 			antField.setType("←");
 		} else if (antField.getType().equals("↓")) {
 			antField.setType("→");
 		} else if (antField.getType().equals("←")) {
 			antField.setType("↓");
 		} else if (antField.getType().equals("→")) {
 			antField.setType("↑");
 		}
 		this.repaint();
 	}
 	
 /**
  * Calculate next position and is doing step if possible.
  */
 	@Override
 	public void forward() {
 		Field antField = levelMap.get(antPosition);
 		Point nextPoint = null;
 /*      ------------> x
  *      | 
  *      |    O_o
  *      |
  *     \/ y
  */
 		if (antField.getType().equals("→")) {
 			nextPoint = new Point(antPosition.x + 1, antPosition.y);
 		} else if (antField.getType().equals("←")) {
 			nextPoint = new Point(antPosition.x - 1, antPosition.y);
 		} else if (antField.getType().equals("↑")) {
 			nextPoint = new Point(antPosition.x, antPosition.y - 1);
 		} else if (antField.getType().equals("↓")) {
 			nextPoint = new Point(antPosition.x, antPosition.y + 1);
 		}
 		Field nextField = levelMap.get(nextPoint);
 		if (nextField.isObstacle()) {
 			/* need it say */
 		} else {
 			levelMap.remove(nextPoint);
 			levelMap.remove(antPosition);
 			levelMap.put(antPosition, nextField);
 			levelMap.put(nextPoint, antField);
 			antPosition = nextPoint;
 		}
 		this.repaint();
 	}
 
 	@Override
 	public void take() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void drop() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
