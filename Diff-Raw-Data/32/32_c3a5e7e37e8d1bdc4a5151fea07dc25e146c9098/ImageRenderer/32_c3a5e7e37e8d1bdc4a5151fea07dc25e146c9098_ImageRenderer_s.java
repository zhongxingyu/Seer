 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.ActionMap;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 
 import components.Edge;
 import components.Polygon;
 import components.Vector3D;
 import pipline.SimpleRenderer;
 
 public class ImageRenderer {
 
 	private int imageWidth = 700;
 	private int imageHeight = 700;
 
 	private JFrame frame;
 	private BufferedImage image;
 	private JComponent drawing;
 	private JTextArea textOutput;
 
 	SimpleRenderer simple;
 
 	//HashSet<Polygon> mesh;
 	ArrayList<Polygon> mesh;
 	Vector3D lightSource;
 
 	// private float shift = 1.0f;
 
 	public static void main(String[] args) {
 		new ImageRenderer();
 	}
 
 	private ImageRenderer() {
 		setupFrame();
 		textOutput.setText("Click open to open a model");
 
 	}
 
 	/**
 	 * Create my bitmap in here!
 	 * 
 	 */
 	private void createImage() {
 		Color[][] bitmap = new Color[imageWidth][imageHeight];
 
 		// make the bitmap of smoothly changing colors;
 		// Your program should render a model
 		simple = new SimpleRenderer(mesh, lightSource, new float[] { 0.5f,
 				0.5f, 0.5f }, imageHeight);
 		textOutput.setText("Rotating....");
 
 		//simple.rotatePolygons("UP");
 		textOutput.setText("Okay done, translating and scaling....");
 		simple.transformPolygons();
 		textOutput.setText("Okay done, removing hidden polys....");
 		simple.removeHidden();
 		textOutput.setText("Okay done, computing shading...");
 		Color[][] render = simple.render(textOutput);
 		textOutput.setText("Okay done");
 
 		image = convertBitmapToImage(render);
 		// draw it.
 		drawing.repaint();
 	}
 
 	/**
 	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
 	 * indexed by column then row and has imageHeight rows and imageWidth
 	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
 	 * that order.
 	 */
 	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
 		image = new BufferedImage(imageWidth, imageHeight,
 				BufferedImage.TYPE_INT_RGB);
 		for (int x = 0; x < imageWidth - 1; x++) {
 			for (int y = 0; y < imageHeight - 1; y++) {
 				int rgb = bitmap[x][y].getRGB();
 				image.setRGB(x, y, rgb);
 				// image.setRGB(x, y, bitmap[x][y].getRGB());
 			}
 		}
 		return image;
 	}
 
 	private void openModel() {
 		// TODO Auto-generated method stub
 		final JFileChooser fc = new JFileChooser("res");
 		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		int returnedVal = fc.showDialog(drawing, "Select 3D model");
 		try {
 			if (returnedVal == 0) {
 				String model = fc.getSelectedFile().toString();
 				readModel(model);
 				textOutput.setText("Click Render to make an image");
 			} else {
 				textOutput.setText("No file selected D:");
 			}
 		} catch (FileNotFoundException e) {
 			textOutput.setText("Error could not find specified file");
 		} catch (IOException e) {
 			textOutput.setText("Input error occured");
 		}
 	}
 
 	private void readModel(String model) throws IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader(new File(model)));
 		// read in the the lightsource as a 3dVector
 		String[] light = br.readLine().split(" ");
 		lightSource = new Vector3D(Float.parseFloat(light[0]),
 				Float.parseFloat(light[1]), Float.parseFloat(light[2]));
 		// now while there is still things begin making a model
 		// Make an arrayList of polygons for now.. FIXME!
 		mesh = new ArrayList<Polygon>();
 		String line;
 		while ((line = br.readLine()) != null) {
 			String[] poly = line.split(" ");
 			
 			Vector3D v1 = new Vector3D(Float.parseFloat(poly[0]),
 					Float.parseFloat(poly[1]), Float.parseFloat(poly[2]));
 			Vector3D v2 = new Vector3D(Float.parseFloat(poly[3]),
 					Float.parseFloat(poly[4]), Float.parseFloat(poly[5]));
 			Vector3D v3 = new Vector3D(Float.parseFloat(poly[6]),
 					Float.parseFloat(poly[7]), Float.parseFloat(poly[8]));
 			
 			int r = Integer.parseInt(poly[9]);
 			int g = Integer.parseInt(poly[10]);
 			int b = Integer.parseInt(poly[11]);
 			mesh.add(new Polygon(v1, v2, v3, r, g, b));
 
 		}
 
 	}
 
 	/**
 	 * Creates a frame with a JComponent in it. Clicking in the frame will close
 	 * it.
 	 */
 	private void setupFrame() {
 
 		frame = new JFrame("3D Renderer");
 		frame.setSize(imageWidth + 10, imageHeight + 20);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		drawing = new JComponent() {
 			protected void paintComponent(Graphics g) {
 				 g.drawImage(image, 0, 0, null);
 				 g.setColor(Color.red);
				if (mesh != null && !mesh.isEmpty()) {
 					for (Polygon p : mesh) {
 						if (p != null) {
 							Edge[] edges = p.edges;
 							Vector3D[] verts = p.getVerts();
 							for (int i = 0; i < verts.length - 1; i++) {
 								g.fillOval(Math.round(verts[i].x),
 										Math.round(verts[i].y), 2, 2);
 								Vector3D v1 = p.getEdges()[i].v1;
 								Vector3D v2 = p.getEdges()[i].v2;
 								g.drawLine(Math.round(v1.x),Math.round(v1.y), Math.round(v2.x), Math.round(v2.y));
 							}
 						}
 					}
 				}
 				
 				g.setColor(Color.CYAN);
 				g.drawOval(350, 350, 2, 2);
				g.setColor(Color.black);
 			}
 		};
 		frame.add(drawing, BorderLayout.CENTER);
 
 		JPanel panel = new JPanel();
 		frame.add(panel, BorderLayout.NORTH);
 		addButton("Render", panel, new ActionListener() {
 			public void actionPerformed(ActionEvent ev) {
 				createImage();
 			}
 		});
 		addButton("Open", panel, new ActionListener() {
 			public void actionPerformed(ActionEvent ev) {
 				openModel();
 
 			}
 		});
 		addButton("Quit", panel, new ActionListener() {
 			public void actionPerformed(ActionEvent ev) {
 				System.exit(0);
 			}
 		});
 
 		addButton("Rotate", panel, new ActionListener() {
 			public void actionPerformed(ActionEvent ev) {
 				simple.rotatePolygons("UP");
 			}
 		});
 
 		textOutput = new JTextArea(1, 20);
 		textOutput.setEditable(false);
 		JScrollPane textSP = new JScrollPane(textOutput);
 		frame.add(textSP, BorderLayout.SOUTH);
 
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Adds a button to the Specified JComponent
 	 * 
 	 * @param name
 	 * @param comp
 	 * @param listener
 	 */
 	private void addButton(String name, JComponent comp, ActionListener listener) {
 		JButton button = new JButton(name);
 		comp.add(button);
 		button.addActionListener(listener);
 	}
 }
