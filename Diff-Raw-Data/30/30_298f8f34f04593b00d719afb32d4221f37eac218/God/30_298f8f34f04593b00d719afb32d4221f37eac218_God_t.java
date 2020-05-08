 package main;
 
 import javax.swing.JPanel;
 
 import data.MoveVertex;
 import data.ShapeData;
 import fileio.FileIOPanel;
 
 public class God {
 	
 	public static MainWindow mainWindow;
 	public static ImagePanel imagePanel; 
 	public static Toolbox toolBox;
 	public static FileIOPanel fileIOPanel;
 	public static ShapeData shapeData;
 	public static boolean moveMode = false;
 	public static MoveVertex moveVertex;
 	public static boolean release;
 	public static VertexPanel vertexPanel;
 	public static JPanel layeredPanel;
 	public static int[] image_dimension = new int[2];

 	public void resetMoveVertex()
 	{
 		moveVertex = null;
 	}
 
 
 }
