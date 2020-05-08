 package ch.hszt.sp.views;
 import javax.swing.*;
 
 import com.sun.org.apache.xml.internal.utils.StopParseException;
 
 import ch.hszt.sp.controllers.SPController;
 import ch.hszt.sp.models.CEdge;
 import ch.hszt.sp.models.CNode;
 import ch.hszt.sp.models.CShortestPathModel;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 public class CShortestPathView implements IShortestPathListener, IShortestPathGui, Observer{
 	
 	/**
 	 * Die Klasse CShortestPathView sorgt fuer das GUI in unserer Applikation
 	 * Sie reagiert auf die Events die in der GUI ausgeloest werden.
 	 */
 	
 	private static final long serialVersionUID = 1L;
 	private JFrame frame;
 	private JLabel label;
 	private JPanel mapPanel;
 	private Observable observer;
 	private ArrayList<CNode> cnlist;
 	private ArrayList<CEdge> cEdge;
 	private LinkedList<CNode> uNode;
 	private Map<Integer, CNode> lnode;
 	private Map<Integer, CEdge> ledge;
 
 	
 	public CShortestPathView(Observable obs){
 		this.observer = obs;
 		observer.addObserver(this);
 		this.cnlist = new ArrayList<CNode>();
 		this.cEdge = new ArrayList<CEdge>();
 		this.uNode = new LinkedList<CNode>();
 		this.lnode = new HashMap<Integer, CNode>();
 		this.ledge = new HashMap<Integer, CEdge>();
 	}
 	
 	//Die Methode viewGUI verpasst dem Frame zwei Panels die dann alle GUI Komponenten enthalten.
 	public void viewGUI(){
 		frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JButton calcDistanceBtn = new JButton("Calc Distance");
 		JButton searchPathBtn = new JButton("Search Path");
 		//label = new JLabel("Ich bin hier..");
 		//Image img = new ImageIcon("img/testmap.jpg").getImage();
 		
 		JMenuBar menubar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenu help = new JMenu("Help");
 		file.setMnemonic(KeyEvent.VK_F);
 		help.setMnemonic(KeyEvent.VK_F1);
 		
 		//ImageIcon icon = new ImageIcon(getClass().getResource("exit.png"));
 		//JMenuItem eMenuItem = new JMenuItem("Exit", icon);
 		
 		/*eMenuItem.setMnemonic(KeyEvent.VK_C);
 		eMenuItem.setToolTipText("Exit application");
 		eMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event){
 				System.exit(0);
 			}
 		});
 		
 		file.add(eMenuItem);
 		*/menubar.add(file);
 		
 		calcDistanceBtn.setMaximumSize(new Dimension(150, 50));
 		searchPathBtn.setMaximumSize(new Dimension(150, 50));
 		//calcDistanceBtn.setSize(100, 50);
 		//searchPathBtn.setSize(100, 50);
 		
 		SPController spc = new SPController();
 		SPController.SearchPathListener spcl = spc.new SearchPathListener(); 
 		
 		calcDistanceBtn.addActionListener(spcl);
 		searchPathBtn.addActionListener(spcl);
 		
 		//System.out.println(uNode.get(1));
 		ShortestPathsPannel mapPanel = new ShortestPathsPannel(this.cnlist, this.cEdge, this.uNode, this.lnode, this.ledge);
 		this.mapPanel = mapPanel;
 		JPanel bPanel = new JPanel();
 		
 		//BoxLayout um die Buttons untereinander anzuordnen
 		bPanel.setLayout(new BoxLayout(bPanel, BoxLayout.PAGE_AXIS));
 		bPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 		bPanel.add(Box.createHorizontalGlue());
 		bPanel.add(calcDistanceBtn);
 		bPanel.add(Box.createRigidArea(new Dimension(10,0)));
 		bPanel.add(searchPathBtn);
 		
 		frame.add(BorderLayout.EAST, bPanel);
 		frame.add(BorderLayout.CENTER, mapPanel);
 		//mapPanel.add(BorderLayout.NORTH, label);
 		//bPanel.add(BorderLayout.NORTH, calcDistanceBtn);
 		//bPanel.add(BorderLayout.SOUTH, searchPathBtn);
 		
 		//int wide = 1200+img.getWidth(mapPanel);
 		//int height = img.getWidth(mapPanel);
 		frame.setSize(1024, 768);
 		frame.setVisible(true);
 		
 	}
 	
 	//Diese Klasse wird das Panel bieten welches die Buttons enthaelt die in der App benoetigt werden
 	class ButtonPanel extends JPanel{
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		
 		public void paintComponent(Graphics g){
 			g.setColor(Color.orange);
 			
 		}
 		
 	}
 	
 	class CalcDistListener implements ActionListener{
 
 		public void actionPerformed(ActionEvent arg0) {
 			frame.setBackground(Color.BLACK);
 		}
 		
 	}
 
 	@Override
 	public void getNodePosition() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void getEdge() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setNodePositions() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setEdges() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setShortestPath() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void calcDistanceBtn() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void viewPathBtn() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void selectNode() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void selectNodeBtn() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void update(Observable obs, Object arg) {
 		
 		if(obs instanceof CShortestPathModel){
 			CShortestPathModel csm = (CShortestPathModel) obs;
 			this.cnlist = csm.getNodes();
 			this.cEdge = csm.getEdges();
 			this.lnode = csm.getNodesAsMap();
 			this.ledge = csm.getEdgesAsMap();
			this.uNode = csm.getShortestPath(0,5 );
 		}
 		viewGUI();
 	}
 	
 }
