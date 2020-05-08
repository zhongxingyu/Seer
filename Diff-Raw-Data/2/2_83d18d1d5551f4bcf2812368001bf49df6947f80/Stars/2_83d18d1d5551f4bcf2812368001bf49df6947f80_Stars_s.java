 package stars;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import javax.imageio.ImageIO;
 
 public class Stars {
 
 	private static int minIntensity = 64;
 	private static String inputFileName = "stars.jpg";
 
 	public static void main(String[] args) {
 		//Variables
 		int width, height, pixelTotal;
 		BufferedImage constellationImg;
 
 		////////Reading Input Files//////// 
 		File in = new File(inputFileName);
 		BufferedImage img = null;
 		try {
 			img = ImageIO.read(in);
 		} catch (IOException ioException) {
 			System.out.println("Input File Could Not Be Found.");
 			System.exit(0);
 		}
 		//Gather basic file information
 		width = img.getWidth();
 		height = img.getHeight();
 		pixelTotal = width * height;
 
 		Map<Long, Node> nodes = new HashMap<Long, Node>();
 		Queue<Edge> edges = new PriorityQueue<Edge>();
 		//Iterate through buffered image
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {				
 				//The first node is created on its current iteration otherwise all nodes are created in the node above them.
 				Node n = null;
 				
 				//Create the first node
 				if(x == 0 && y == 0){
 					n = new Node(x, y, img.getRGB(x, y));
 					//Hashmap (16 bits: x, 16 bits: y)
 					nodes.put((((long) x) << 16 + ((long) y)), n);
 
 
 					Node right = new Node(x+1, y, img.getRGB(x+1, y));
 					nodes.put((((long) x) << 16 + ((long) y)), n);
 					Edge e = new Edge(n, right, n.intensity + right.intensity);
 					edges.add(e);
 
 					Node down = new Node(x, y+1, img.getRGB(x, y+1));
 					nodes.put((((long) x) << 16 + ((long) y)), n);
 					e = new Edge(n, right, n.intensity + down.intensity);
 					edges.add(e);
 
 				}
 				//Checks the first row
 				else if(y==0){
 					//Check if we have anything on the right
 					if(x+1 != width){
 						
 					
 					//This could be moved in the previous if block
 					//The first row will not have been created vs all other rows would have been.
 
 					//This will be used to make the edge nodes
 					n = nodes.get((x << 16) + y);
 
 					Node down = new Node(x, y+1, img.getRGB(x, y+1));
 					nodes.put((((long) x) << 16 + ((long) y+1)), n);
 					Edge e = new Edge(n, down, n.intensity + down.intensity);
 					edges.add(e);
 
 					Node right = new Node(x+1, y, img.getRGB(x, y));
 					nodes.put((((long) x+1) << 16 + ((long) y)), n);
 					e = new Edge(n, right, n.intensity + right.intensity);
 					edges.add(e);
 					}
 					//If we are at the edge, don't add the right edge, duh.
 					else{
 						Node down = new Node(x, y+1, img.getRGB(x, y+1));
 						nodes.put((((long) x) << 16 + ((long) y+1)), n);
 						Edge e = new Edge(n, down, n.intensity + down.intensity);
 						edges.add(e);
 					}
 						
 
 
 				}
 				//Covers the bottom row
 				else if(y + 1 == height){
 					if(x+1 != width){
					Node n  = nodes.get((x << 16) + y);
 					Node right = nodes.get(((x+1) << 16) + y);
 					Edge e = new Edge(n, right, n.intensity + right.intensity);
 					edges.add(e);
 					}
 				}
 				//Covers the right most column
 				else if(x + 1 == width){
 					n  = nodes.get((x << 16) + y);
 					Node down = new Node(x, y+1, img.getRGB(x, y+1));
 					nodes.put((((long) x) << 16 + ((long) y+1)), n);
 					Edge e = new Edge(n, down, n.intensity + down.intensity);
 					edges.add(e);
 
 				}
 				//Creates all the non first, bottom, or right nodes and relationships
 				else{
 					n  = nodes.get((x << 16) + y);
 
 					Node right = new Node(x+1, y, img.getRGB(x, y));
 					nodes.put((((long) x+1) << 16 + ((long) y)), n);
 					Edge e = new Edge(n, right, n.intensity + right.intensity);
 					edges.add(e);
 					
 					
 					Node down = new Node(x, y+1, img.getRGB(x, y+1));
 					nodes.put((((long) x) << 16 + ((long) y+1)), n);
 					e = new Edge(n, down, n.intensity + down.intensity);
 					edges.add(e);
 				}
 
 
 
 			}
 		}
 		
 		Iterator<Edge> itr = edges.iterator();
 		while (itr.hasNext()){
 			Edge e = itr.next();
 			
 		}
 	}
 }
