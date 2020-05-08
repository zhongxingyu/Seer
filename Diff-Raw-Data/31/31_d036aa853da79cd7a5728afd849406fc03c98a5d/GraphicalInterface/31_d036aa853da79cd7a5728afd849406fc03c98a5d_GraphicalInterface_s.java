 package tools.Interface;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.io.IOException;
 
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 
 import tools.XplodedIndexXplodedWeightFileSearcher;
 
 public class GraphicalInterface extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
	public static int NUMBER_RESULTS = 0;
 	private final JSplitPane split;
 	private final int WIDTH = 1400;
 	private final int HEIGHT = 800;
 	XplodedIndexXplodedWeightFileSearcher s;
 
 	public GraphicalInterface() {
 
 		// this.setLocationRelativeTo(null);
 		this.setTitle("Gérer vos conteneur");
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(this.WIDTH, this.HEIGHT);
 
 		final JTextField jtf = new JTextField();
 
 		final JEditorPane pan2 = new JEditorPane();
 		pan2.setText("Mon fichier et son contenu...");
 		pan2.setEditable(false);
 		try {
 			pan2.setPage("");
 		} catch (final IOException e) {
 			pan2.setContentType("text/html");
			pan2.setText("<html><p style='color:red'>Could not load</p></html>");
 		}
 		final JScrollPane scrollPane = new JScrollPane(pan2);
 		// pan2.setBackground(Color.red);
 
 		// On crée deux conteneurs de couleurs différentes
 		final P1 pan = new P1(jtf, pan2);
 		pan.setBackground(Color.white);
 
 		final MouseManager mouseManager = new MouseManager(pan, pan2);
 
 		pan.setMouseManager(mouseManager);
 		// pan.addMouseListener(mouseManager);
 
 		this.split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pan,
 				scrollPane);
 		this.split.setDividerLocation(this.WIDTH / 3);
 
 		this.getContentPane().add(this.split, BorderLayout.CENTER);
 		this.setVisible(true);
 	}
 
 	public static void main(final String[] args) {
 		final GraphicalInterface f = new GraphicalInterface();
 		// IOManager.returnFilePathFromPoid("01987639.txt.poid");
 	}
 }
