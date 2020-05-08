 /******************************************************************************
  * 
  * Route Visualiser class. Outputs the arrays provided as a graph.
  * 
  * Shows the path for each truck and total length for each path
  * 
  *****************************************************************************/
 
 package com.github.daentech.graphics;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import javax.imageio.ImageIO;
 
 import com.github.daentech.CVRPData;
 
 public class RouteVisualiser {
 	
 	private BufferedImage b;
 	private Graphics2D g;
 	
 	public RouteVisualiser(){
 		b = new BufferedImage(770,770,BufferedImage.TYPE_INT_RGB); /* change sizes of course */
 		g = b.createGraphics();
 	}
 	
 	public void getNodeMap(){
 		
 		char[] s = String.valueOf(CVRPData.getDemand(1)).toCharArray();
 		g.setColor(Color.green);
 		g.drawOval(CVRPData.getCoords()[1][CVRPData.X_COORDINATE] * 10, CVRPData.getCoords()[1][CVRPData.Y_COORDINATE] * 10, 2, 2);
 		g.setColor(Color.green);
 		g.drawChars( s, 0, s.length, CVRPData.getCoords()[1][CVRPData.X_COORDINATE] * 10 + 5, CVRPData.getCoords()[1][CVRPData.Y_COORDINATE] * 10 - 5);
 		System.out.println("(" + CVRPData.getCoords()[1][CVRPData.X_COORDINATE] + "," + CVRPData.getCoords()[1][CVRPData.Y_COORDINATE] + ")");
 		for(int i = 2; i <= CVRPData.NUM_NODES; i++){
 			s = String.valueOf(CVRPData.getDemand(i)).toCharArray();
 			g.setColor(Color.red);
 			g.drawOval(CVRPData.getCoords()[i][CVRPData.X_COORDINATE] * 10, CVRPData.getCoords()[i][CVRPData.Y_COORDINATE] * 10, 2, 2);
 			g.setColor(Color.white);
 			g.drawChars( s, 0, s.length, CVRPData.getCoords()[i][CVRPData.X_COORDINATE] * 10 + 10, CVRPData.getCoords()[i][CVRPData.Y_COORDINATE] * 10 - 10);
 		}
 	}
 	
 	public void drawPaths(int[][] paths){
 		for (int i = 0; i < paths.length; i++ ){
			g.setColor(Color.getHSBColor((float)((0.17 * i + 1.0) % 1.0), 0.8f, 1.0f));
 			for(int j = 0; j < paths[i].length - 1; j++){
				g.drawLine(CVRPData.getCoords()[paths[i][j]][CVRPData.X_COORDINATE] * 10, CVRPData.getCoords()[paths[i][j]][CVRPData.Y_COORDINATE] * 10,
						CVRPData.getCoords()[paths[i][j+1]][CVRPData.X_COORDINATE] * 10 , CVRPData.getCoords()[paths[i][j+1]][CVRPData.Y_COORDINATE] * 10 );
 			}
 		}
 	}
 	
 	public void saveImage(){
 		try{ImageIO.write(b,"png",new File("test.png"));}catch (Exception e) {}
 	}
 
 }
