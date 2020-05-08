 import gui.GraphicGraph;
 import gui.GraphicVertex;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Line2D;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileFilter;
 
 import color.ColorationAlgorithm;
 import color.Dsatur;
 import color.ImplementedAlgorithms;
 import model.DynGraphModel;
 import model.StdDynGraphModel;
 import model.Vertex;
 
 
 public class GraphColor {
 
 	private static final int FRAME_WIDTH = 800;
     private static final int FRAME_HEIGHT = 600;
 	private JFrame frame = null;
 	private JPanel contentPane = null;
 	private JButton newButton = null;
 	private JButton randomButton = null;
 	private JButton saveButton = null;
 	private JButton loadButton = null;
 	private JToggleButton vertexButton = null;
 	private JToggleButton edgeButton = null;
 	private JComboBox<String> algoButton = null;
 	private JButton colorButton = null;
 	private JButton uncolorButton = null;
 	private DynGraphModel model = null;
 	private ColorationAlgorithm algo = null;
 	private GraphicGraph graphic = null;
 	private JLabel status = null;
 	private boolean dragging = false;
 	private GraphicVertex draggedVertex = null;
 	private enum Tools {
 		VERTEX,EDGE;
 	}
 	private Tools mode = Tools.VERTEX;
 	private ImplementedAlgorithms setAlgo = null;
 	private boolean colored = false;
 	private Line2D line = null;
 	
 	public GraphColor(){
 		createModel();
 		createView();
 		placeComponents();
 		createController();
 	}
 		
 	public void display() {
 	    frame.pack();
 	    frame.setLocationRelativeTo(null);
 	    frame.setVisible(true);
 	    model.notifyObservers();
 	}
 	
 	private void createModel() {
 		model = new StdDynGraphModel();
 		model.randomize(10);
     }
 	
 	private void createView() {
         frame = new JFrame("Graph Color");
         newButton = new JButton("Nouveau");
 		randomButton = new JButton("Aleatoire");
 		saveButton = new JButton("Sauvegarder");
 		loadButton = new JButton("Charger");
 		vertexButton = new JToggleButton("Sommet");
 		edgeButton = new JToggleButton("Arete");
		String[] algos = { "DSATUR", "Algo 2", "Algo 3", "Algo 4", "Algo 5" };
 		algoButton= new JComboBox<String>(algos);
 		algoButton.setSelectedIndex(0);
 		colorButton = new JButton("Colorier");
 		uncolorButton = new JButton("Decolorier");
         contentPane = new JPanel(new BorderLayout());
         graphic = new GraphicGraph(model);
         status = new JLabel("Yo");
         status.setBorder(BorderFactory.createEtchedBorder());
 		contentPane.setBackground(Color.WHITE);
     	frame.setPreferredSize(
                 new Dimension(FRAME_WIDTH, FRAME_HEIGHT)
         );
     }
 	
 	private void placeComponents() {
 		JToolBar toolBar = new JToolBar(); {
 			toolBar.setFloatable(false);
 			toolBar.setOrientation(JToolBar.HORIZONTAL);
 			toolBar.addSeparator();
 			toolBar.add(newButton);
 			toolBar.add(randomButton);
 			toolBar.add(saveButton);
 			toolBar.add(loadButton);
 			toolBar.addSeparator();
 			toolBar.addSeparator();
 			//toolBar.add(new JLabel("Outils : "));
 			toolBar.add(vertexButton);
 			toolBar.add(edgeButton);
 			toolBar.addSeparator();
 			toolBar.addSeparator();
 			toolBar.add(algoButton);
 			toolBar.add(colorButton);
 			toolBar.add(uncolorButton);
 			toolBar.addSeparator();
 		}
 		contentPane.add(toolBar, BorderLayout.NORTH);
 		contentPane.add(graphic,BorderLayout.CENTER);
 		frame.add(contentPane);
 		frame.add(status, BorderLayout.SOUTH);
 	}
 	
 	private void createController() {
 		model.addObserver(new Observer() {
             public void update(Observable o, Object arg) {
             	   if (model.getVerticesNb() == 0) {
             		   mode = Tools.VERTEX;
             		   saveButton.setEnabled(false);
             		   colorButton.setEnabled(false);
             		   uncolorButton.setEnabled(false);
             	   } else {
             		   saveButton.setEnabled(true);
             		   colorButton.setEnabled(true);
             		   uncolorButton.setEnabled(true);
             	   }
             }
         });
 		ButtonGroup bg = new ButtonGroup();
         bg.add(vertexButton);
         bg.add(edgeButton);
         vertexButton.setSelected(true);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         newButton.addActionListener( new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		graphic.reset();
         		model.notifyObservers();
         	}
         });
         newButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				status.setText("Cre un nouveau graphe (page vierge).");
 			}  	
         });
         randomButton.addActionListener( new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		graphic.randomize(10); 
         		model.notifyObservers();
         	}
         });
         randomButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				status.setText("Gnre un graphe alatoire.");
 			}      	
         });
         saveButton.addActionListener( new ActionListener(){
         	public void actionPerformed(ActionEvent e)  {
         			try {
 						saveState();
 					} catch (FileNotFoundException e1) {
 						e1.printStackTrace();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
         	}
         });
         saveButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				status.setText("Sauvegarde le graphe actuel.");
 			}      	
         });
         loadButton.addActionListener(new ActionListener(){
         	public void actionPerformed(ActionEvent e) {
         		JFileChooser fc = new JFileChooser();
         		fc.setFileFilter(new GraphFileFilter());
         		int returnVal = fc.showOpenDialog(null);
         		if (returnVal == JFileChooser.APPROVE_OPTION) {
                     File f = fc.getSelectedFile();
                     try {
 						load(f);
 					} catch (FileNotFoundException e1) {
 						e1.printStackTrace();
 					} catch (ClassNotFoundException e1) {
 						e1.printStackTrace();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
                     status.setText("Charg : " + f.getAbsolutePath());
                     model.notifyObservers();
         		}
         	}
         });
         loadButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				status.setText("Charge un graphe depuis un fichier compatible (.gra).");
 			}
         });
         vertexButton.addActionListener( new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		mode = Tools.VERTEX; 
         		model.notifyObservers();
         	}
         });
         vertexButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				status.setText("Selectionne l'outil Sommet (Ajout, Suppression et Dplacement).");
 			}      	
         });
         edgeButton.addActionListener( new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		mode = Tools.EDGE; 
         		model.notifyObservers();
         	}
         });
         edgeButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				status.setText("Selectionne l'outil Arte (Ajout et Suppression).");
 			}    	
         });
         algoButton.addActionListener( new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		//A FINIR 
         		model.notifyObservers();
         	}
         });
         algoButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				status.setText("Slectionne l'algorithme utilis pour colorier le graphe.");
 			}  	
         });
         colorButton.addActionListener( new ActionListener() { //A NETTOYER
         	public void actionPerformed(ActionEvent e) {
         		algo = new Dsatur(model);
         		algo.color();
         		colored = true;
         		model.notifyObservers();
         	}
         });
         colorButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				status.setText("Colorie le graphe suivant l'algorithme indiqu");
 			}    	
         });
         uncolorButton.addActionListener( new ActionListener() { //A NETTOYER
         	public void actionPerformed(ActionEvent e) {
         		algo = new Dsatur(model);
         		algo.uncolor();
         		colored = false;
         		model.notifyObservers();
         	}
         });
         uncolorButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				status.setText("Colorie tout le graphe en noir.");
 			}   	
         });
         graphic.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				switch (mode) {
 					case VERTEX:
 						Point p = arg0.getPoint();
 						if(SwingUtilities.isRightMouseButton(arg0)){
 							GraphicVertex gv = graphic.clickedVertex(p);
 							if (gv != null) {
 								graphic.removeVertex(gv);
 								if ((colored == true) && (gv.getVertex().getDegree() > 0)) {
 									algo = new Dsatur(model); //A CHANGER
 									algo.color();
 								}
 							}
 						} else {
 							graphic.addVertex(p);
 						}
 						break;
 					case EDGE:
 						break;
 					default:
 						break;
 				}
 				model.notifyObservers();
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				switch (mode) {
 					case VERTEX:
 						graphic.setCursor(new Cursor(Cursor.HAND_CURSOR));
 						status.setText("<Clic gauche> ajoute un sommet. <Clic droit> supprime un sommet. Maintenir pour dplacer.");
 						break;
 					case EDGE:
 						graphic.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
 						status.setText("<Clic gauche> sur un sommet puis glisser-dposer pour tracer un arc. <Clic droit> puis glisser-dposer pour supprimer un arc .");
 						break;
 					default:
 						break;
 				}
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				GraphicVertex gv = graphic.clickedVertex(arg0.getPoint());
 				switch (mode) {
 					case VERTEX:
 						if (gv != null) {
 							dragging = true;
 							draggedVertex = gv;
 						} else {
 							dragging = false;
 							draggedVertex = null;
 						}
 						break;
 					case EDGE:
 						if (gv != null) {
 							dragging = true;
 							draggedVertex = gv;
 						} else {
 							dragging = false;
 							draggedVertex = null;
 						}
 						break;
 					default:
 						break;
 				}
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				switch (mode) {
 					case VERTEX:
 						if (dragging == true){
 							graphic.moveVertex(draggedVertex.getVertex(), arg0.getPoint());
 						}
 						dragging = false;
 						draggedVertex = null;
 						break;
 					case EDGE:
 						if (dragging == true){
 							GraphicVertex gv = graphic.clickedVertex(arg0.getPoint());
 							if (gv != null) {
 								if (SwingUtilities.isRightMouseButton(arg0)) {
 									if ((gv.getVertex().isConnectedTo(draggedVertex.getVertex()) && (!gv.getVertex().equals(draggedVertex.getVertex()))) ) {
 										model.disconnect(gv.getVertex(), draggedVertex.getVertex());
 										if (colored == true) {
 											algo = new Dsatur(model); //A CHANGER
 											algo.color();
 										}
 									}
 								} else {
 									if ((!gv.getVertex().isConnectedTo(draggedVertex.getVertex()) && (!gv.getVertex().equals(draggedVertex.getVertex()))) ) {
 										model.connect(gv.getVertex(), draggedVertex.getVertex());
 										if (colored == true) {
 											algo = new Dsatur(model); //A CHANGER
 											algo.color();
 										}
 									}
 								}
 							}
 						}
 						dragging = false;
 						draggedVertex = null;
 						model.notifyObservers();
 						break;
 				}
 			}
         	
         });
         graphic.addMouseMotionListener(new MouseMotionListener() {
 			
 			@Override
 			public void mouseMoved(MouseEvent arg0) {
 
 			}
 			
 			@Override
 			public void mouseDragged(MouseEvent arg0) {
 				switch (mode) {
 					case VERTEX:
 						if (dragging) {
 							graphic.moveVertex(draggedVertex.getVertex(), arg0.getPoint());
 						}
 						model.notifyObservers();
 						break;
 					case EDGE:
 						if (dragging) {
 							graphic.getGraphics().drawLine(draggedVertex.getPoint().x + GraphicVertex.RADIUS
 									, draggedVertex.getPoint().y + GraphicVertex.RADIUS, arg0.getPoint().x, arg0.getPoint().y);
 						    graphic.repaint();
 						}
 						model.notifyObservers();
 						break;
 				}
 			}
 		});
         
 	}
 	
 	public void saveState() throws IOException, FileNotFoundException {
 		Date now = new Date();
 	    SimpleDateFormat sdf = new SimpleDateFormat ("dd-MM-yyyy.hhmmssa'.gra'");
 	    String s = sdf.format(now);
 		FileOutputStream fos = new FileOutputStream(s);
 		ObjectOutputStream oos =new ObjectOutputStream(fos);
 		oos.writeObject(model);
 		oos.writeObject(graphic.getCoords());
 		oos.close();
 		status.setText("Sauv : " + s);
 	}
 	
 	public void load(File f) throws IOException, FileNotFoundException, ClassNotFoundException{
 		if (f == null){
 			throw new IllegalArgumentException();
 		}
 		FileInputStream fis = new FileInputStream(f);
         ObjectInputStream ois = new ObjectInputStream(fis);
         model = (DynGraphModel) ois.readObject();
 		Map<Vertex, GraphicVertex> c = (Map<Vertex, GraphicVertex>) ois.readObject(); //???
         graphic.setModel(model, c);
         ois.close();
 	}
 	
 	public class GraphFileFilter extends FileFilter
 	{
 	     public boolean accept(File f)
 	    {
 	        if(f.isDirectory())
 	        {
 	            return true;
 	        }
 	        return f.getName().endsWith(".gra");
 	    }
 	 
 	    public String getDescription()
 	    {
 	        return "Fichiers Graphes (*.gra)";
 	    }
 	}
 	
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 new GraphColor().display();
             }
 		});
 	}
 }
