 package models;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 import org.jgraph.JGraph;
 import org.jgrapht.ext.JGraphModelAdapter;
 
 import tree.CustomTreeNode;
 
 /**
  * Plots form a given JGraphModelAdapter a JGraph in a Jframe and generates a
  * image file out of the Jgraph.
  */
 public class Plotter {
 
 	private static final Color DEFAULT_BG_COLOR = Color.decode("#ffffff");
 	private static JGraphModelAdapter<CustomTreeNode, CustomEdge> jgAdapter;
 	private static Dimension appletSize;
 	private static JFrame frame;
	private static Date now;
	private static SimpleDateFormat dateFormat;
 
 	public Plotter() {
 		frame = new JFrame();
 		frame.getContentPane();
 		frame.setTitle("JGraphT Adapter to JGraph Demo");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setVisible(false);
		now = new Date();
		dateFormat = new SimpleDateFormat("ddMMyyyy-HHmmssS");
 	}
 
 	public static void plot(Parser parser) {
 
 		// get calculated applet size from parser
		appletSize = new Dimension(parser.getSizeAppletx(),
				parser.getSizeApplety());
 
 		jgAdapter = parser.getJgAdapter();
 
 		// Add JgraphModelAdapter to Jgraph
 		JGraph jgraph = new JGraph(jgAdapter);
 
 		adjustImageSettings(jgraph);
 
 		frame.getContentPane().add(jgraph);
 		frame.setSize(appletSize);
 
 		// Write image to file
 		BufferedImage img = jgraph.getImage(jgraph.getBackground(), 10);
 
		String imagePath = "./pcg/public/images/graphs/"
				+ dateFormat.format(now).toString() + ".png";
 
 		try {
 			ImageIO.write(img, "png", new File(imagePath));
 		} catch (IOException e) {
 			System.out.println("Sorry image couldn't be generated!");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Will be used to set background color of generated image
 	 * 
 	 * @param jgraph
 	 *            the generated JGraph
 	 */
 	private static void adjustImageSettings(JGraph jgraph) {
 		jgraph.setPreferredSize(appletSize);
 		Color c = DEFAULT_BG_COLOR;
 		jgraph.setBackground(c);
 	}
 
 	public String getImageSource() {
		return "/public/images/graphs/" + dateFormat.format(now).toString()
				+ ".png";
 	}
 }
