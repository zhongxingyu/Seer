 package moleculeMaker;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 


 @SuppressWarnings("serial")
 public class MMView extends JFrame implements ComponentListener
 {
 	final int WINDOW_WIDTH = 700;
 	final int WINDOW_HEIGHT = 600;
 	
 	MMController mmc;
 	MoleculeGrid gridA;
 	MoleculeGrid gridB;
 	JMenuBar menuBar;
 	AttributesModifier mod;
 	JPanel center = new JPanel();
 	JPanel south = new JPanel();
 	
 	public MMView(MMController mmc)
 	{
 		this.mmc = mmc;
 		this.addComponentListener(this);
 		createGUI();
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 	
 	/**
 	 * Initializes the view's components, adds them to the frame, then shows the
 	 * window.
 	 */
 	private void createGUI()
 	{
 		this.setLayout(new BorderLayout());
 
 		createMenuBar();
 		
 		// Graph panel
 		gridA = new MoleculeGrid(mmc);
 		gridB = new MoleculeGrid(mmc);
 		mod = new AttributesModifier(null);
 
 		// Grid's grid layout
 		GridLayout layout = new GridLayout(1,2);
 		layout.setHgap(10);
 		JPanel gridLayout = new JPanel();
 		gridLayout.setLayout(layout);
 		gridLayout.add(gridA);
 		gridLayout.add(gridB);
 		
 		add(gridLayout, BorderLayout.CENTER);
 		
 		// Set up/add to SOUTH
 		south.add(mod);
 		add(south, BorderLayout.SOUTH);
 		
 		this.setTitle("Molecule Maker");
 		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
 		this.setVisible(true);
 	}
 	
 	private void createMenuBar()
 	{
 		// File menu (thanks to: http://www.java-tips.org/java-se-tips/javax.swing/how-to-create-menu-bar.html)
 		menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		JMenu fileMenu = new JMenu("File");
 		JMenu graphMenu = new JMenu("Graph");
 		
 		// File submenu
 		JMenuItem exportAction = new JMenuItem("Export to XML");
 		JMenuItem quitAction = new JMenuItem("Quit");
 		fileMenu.add(exportAction);
 		fileMenu.addSeparator();
 		fileMenu.add(quitAction);
 		menuBar.add(fileMenu);
 		
 		menuBar.add(graphMenu);
 		JMenuItem clearElementsAction = new JMenuItem("Clear Element");
 		JMenuItem clearBondsAction = new JMenuItem("Clear Bonds");
 		JMenuItem clearArrowsAction = new JMenuItem("Clear Arrows");
 		graphMenu.add(clearElementsAction);
 		graphMenu.add(clearBondsAction);
 		graphMenu.add(clearArrowsAction);
 		
 		// ActionListeners:
 		exportAction.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				mmc.exportToXML();
 			}
 		});
 		quitAction.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mmc.quit();
 			}
 		});
 		
 		clearElementsAction.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mmc.clearElements();
 			}
 		});
 		clearBondsAction.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mmc.clearBonds();
 			}
 		});
 		clearArrowsAction.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
				mmc.clearBonds();
 			}
 		});
 	}
 	
 //	public void displayElementAttributes(Element e)
 //	{	
 //		mmc.displayElementAttributes(e); // logic for this belongs in controller
 //	}
 
 	public void displayAttributes(MoleculeComponent e)
 	{	
 		mmc.displayAttributes(e); // logic for this belongs in controller
 	}
 
 	
 	@Override
 	public void componentHidden(ComponentEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void componentMoved(ComponentEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void componentResized(ComponentEvent e) {
 		// TODO Auto-generated method stub
 		System.out.println("GridA size: " + gridA.getSize());
 		MoleculeGrid.setGridSpacing(gridA.getSize().width, gridA.getSize().height);
 		repaint();
 	}
 
 	@Override
 	public void componentShown(ComponentEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 }
