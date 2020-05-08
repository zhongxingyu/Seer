 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Map.Entry;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 
 import util.Constantes.Pion;
 
 public class Monopoly extends JFrame implements ActionListener,PropertyChangeListener {
 	private static final long serialVersionUID = 1L;
 	
 	private Plateau p;
 	
 	private JMenuBar menuBar = new JMenuBar();
 	private JMenu gameMenu = new JMenu("Jeu");
 	private JMenuItem newMItem = new JMenuItem("Nouveau");
 	private JMenuItem exitMItem = new JMenuItem("Quitter");
 	
 	private GridLayout northGLayout = new GridLayout(0, 11);
 	private GridLayout eastGLayout = new GridLayout(9, 0);
 	private GridLayout southGLayout = new GridLayout(0, 11);
 	private GridLayout westGLayout = new GridLayout(9, 0);
 	
 	private Vector<CasePanel> cases = new Vector<CasePanel>();
 	
 	public Monopoly(Plateau p){
 		super();
 		this.p = p;
 		createGUI();
 	}
 	
 	private void createGUI(){
 		// Set up the frame
 		this.setTitle("Monopoly");
 		this.setSize(new Dimension(660, 660));
 		this.setLocation(510, 5);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		// Set up the menu bar
 		newMItem.addActionListener(this);
 		gameMenu.add(newMItem);
 		exitMItem.addActionListener(this);
 		gameMenu.add(exitMItem);
 		menuBar.add(gameMenu);
 		this.setJMenuBar(menuBar);
 		
 		// Main layout & panels
 		JPanel panelMain = new JPanel();
 		panelMain.setSize(new Dimension(660, 660));
 		
 		JPanel panelNorth = new JPanel();
 		JPanel panelEast = new JPanel();
 		JPanel panelSouth = new JPanel();
 		JPanel panelWest = new JPanel();
 		JPanel panelCenter = new JPanel();
 		panelNorth.setLayout(northGLayout);
 		panelNorth.setSize(new Dimension(60, 660));
 		panelEast.setLayout(eastGLayout);
 		panelEast.setSize(new Dimension(660, 60));
 		panelSouth.setLayout(southGLayout);
 		panelSouth.setSize(new Dimension(60, 660));
 		panelWest.setLayout(westGLayout);
 		panelWest.setSize(new Dimension(660, 60));
 		panelCenter.setSize(new Dimension(540, 540));
 		panelCenter.add(new JLabel(new ImageIcon("../Monopoly/res/center2.png")));
 		
 		this.getContentPane().add(panelNorth, BorderLayout.NORTH);
 		this.getContentPane().add(panelEast, BorderLayout.EAST);
 		this.getContentPane().add(panelSouth, BorderLayout.SOUTH);
 		this.getContentPane().add(panelWest, BorderLayout.WEST);
 		this.getContentPane().add(panelCenter, BorderLayout.CENTER);
 		
 		// Cases panels
 		for(int i = 0; i < p.getPlateau().size(); i++){
 			cases.add(new CasePanel(i));
 		}
 		
 		// Ajout des 40 cases  l'interface graphique
 		for(int i = 10; i >= 0; i--)
 			panelSouth.add(cases.get(i));
 		
 		for(int i = 19; i >= 11; i--)
 			panelWest.add(cases.get(i));
 		
 		for(int i = 20; i <= 30; i++)
 			panelNorth.add(cases.get(i));
 		
 		for(int i = 31; i <= 39; i++)
 			panelEast.add(cases.get(i));
 		
 		this.setVisible(true);
 	}
 	
 	// Reset the frame with houses and tokens
 	public void redraw(){
		for(int i = 0; i < p.getPlateau().size(); i++){
			if(p.getPlateau().get(i).getClass().equals(CaseTerrain.class))
				cases.get(p.getPlateau().get(i).getPosition()).checkMaisons(((CaseTerrain)p.getPlateau().get(i)).getNbMaisons());
		}
 		for(int i = 0; i < cases.size(); i++){
 			for(Entry<Pion, Integer> entry : p.getPositionJoueurs().entrySet()) {
 				if(entry.getValue() == i)
 					cases.get(i).addPion(entry.getKey());
 			}
			//if(p.getPlateau().get(i).getClass().equals(CaseTerrain.class))
			//	cases.get(i).checkMaisons(((CaseTerrain)p.getPlateau().get(i)).getNbMaisons());
 			cases.get(i).repaint();
 		}
 	}
 
 	public void propertyChange(PropertyChangeEvent arg0) {
 	}
 
 	public void actionPerformed(ActionEvent arg0) {
 		if(arg0.getSource() == newMItem){
 			// Nouvelle partie
 		}
 		else if(arg0.getSource() == exitMItem){
 			System.exit(0);
 		}
 	}
 
 }
