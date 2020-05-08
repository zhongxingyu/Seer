 package hg.histo;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.border.Border;
 
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.swing.mxGraphOutline;
 import com.mxgraph.view.mxGraph;
 
 public class FramCell extends JFrame implements ActionListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2707712944901661771L;
 
 
 	private String img_default = "src/ressources/image0046.jpg";
 	private String path_image = img_default;
 	private String path_excel_default = "src/ressources/image0046.csv";
 	private String path_current = path_excel_default;
 
 	private JCheckBox checkAll;
 	private JCheckBox checkBox ;
 	private String color;
 	private String celselected;
 	/*
 	private JCheckBox check_Tumor;
 	private JCheckBox check_Granulocyte_nucleus;
 	private JCheckBox check_Lymphocyte;
 	private JCheckBox check_NucleusDAB;
 	private JCheckBox check_NucleusDAB_PRB;
 	 */
 	private JTextField request;
 	private JLabel label;
 	private JButton validate;
 	private JButton cancel;
 	private JPanel containerequest;
     private JMenuItem newCell;
 	//private Hashtable<String,String> tableCell = new Hashtable< String,String>();
 	private HashMap<String, String> m = new HashMap<String,String>();
 	//private enum enumColorCell {black,blue,gray,green,white,orange,red,yellow,pink};
 
 	Menu menu=new Menu();
 	JPanel down = new JPanel(new GridLayout(0,1));
 	JPanel optionBox = new JPanel();
 	JPanel buttonBar = new JPanel();
 	JButton btZoomToFit = new JButton("Zoom Off");
 	JButton btDisplay=new JButton("Display");
 
 	mxGraph graph = new mxGraph();
 	Object parent = graph.getDefaultParent();
 	mxGraphComponent graphComponent ;
 
 	SearchFile searchFile ;
 
 	List<Cell> listCells;
 
 	ImageIcon img = new ImageIcon(img_default);
 	HashMap<String,JCheckBox> checkBoxes = new HashMap<String,JCheckBox>();
 
 
 	
 	public FramCell() {
 
 		super("Frame Cell!");
 		listCells = new ArrayList<Cell>();
 
 		graphComponent = new mxGraphComponent(graph);
 		graphComponent.setBounds(0, 0, (int)(img.getIconWidth()*0.4), (int)(img.getIconHeight()*0.4));
 
 		graphComponent.setPreferredSize(new Dimension( (int)(img.getIconWidth()*0.4),(int)(img.getIconHeight()*0.4)));
 
 		graphComponent.setGridVisible(true);
 		graphComponent.setGridColor(Color.BLACK);
 		graphComponent.setConnectable(false);
 		graphComponent.getViewport().setOpaque(true);
 
 		graph.setCellsCloneable(false);
 		graph.setCellsDeletable(false);
 		graph.setCellsMovable(false);
 		graph.setCellsSelectable(false);
 		graph.setCellsResizable(false);
 
 		listCells = setListCell(path_current);
 
 		//create color of cell
 		MapColorCell();
 
 		optionBox.setLayout(new BorderLayout());   	
 		//buttonBar.setLayout(new FlowLayout());
 
 		buttonBar.add(btZoomToFit);
 
 
 
 		//Zoom out graph
 		final mxGraphOutline graphOutline = new mxGraphOutline(graphComponent);
 		graphOutline.setPreferredSize(new Dimension(150, 150));
 		optionBox.add(graphOutline, BorderLayout.NORTH);
 
 
 
 		//Check Box 
 		Border border = BorderFactory.createTitledBorder("Selected Cell");
 		down.setBorder(border);
 		down.setBackground(Color.BLUE);
 		down.setBounds(0, 200, 150, 200);
 		down.setOpaque(true);
 
 		//creation des checkbox
 
 		checkAll = new JCheckBox("All cells");
 		checkAll.setSelected(true);
 		down.add(checkAll);
 
 		for(String key : m.keySet()){
 
 			checkBox = new JCheckBox(key.toString());
 			checkBox.setName("CheckBox_" + key.toString());
 			checkBox.setSelected(false);
 			checkBoxes.put(key.toString(), checkBox);
 			down.add(checkBox);
 			//System.out.println(checkBox.getName());
 		}
 		//Ada ButtonDisplay in  down  JPanel
 		down.add(btDisplay,BorderLayout.CENTER);
 
 		//Ada JPanel down ie check box in OptionBox JPanel
 		optionBox.add(down);
 		optionBox.add(buttonBar,BorderLayout.CENTER);//because Zoom is on NORTH
 		containerequest = new JPanel();
 
 		Font police = new Font("Arial", Font.BOLD, 14);
 		request =new JTextField("Enter Users Request");
 		request.setPreferredSize(new Dimension(950, 30));
 		request.setForeground(Color.BLUE);
 		request.setFont(police);
 		label = new JLabel("Request");
 		validate=new JButton("Validate");
 		cancel=new JButton("Cancel");
 
 
 		containerequest.add(label);
 		containerequest.add(request);
 		containerequest.add(validate);
 		containerequest.add(cancel);
 
 		getContentPane().add(containerequest,BorderLayout.PAGE_END);
 
 
 		getContentPane().add(optionBox, BorderLayout.EAST);
 
 		setJMenuBar(menu.buildMenu());
 
 		//Add ActionListener elements				
 		menu.getExit().addActionListener(this);
 		//menu.getImage_hidden().addActionListener(this);
 		//menu.getImage().addActionListener(this);
 		menu.getOpen().addActionListener(this);
 		//menu.getAddCell().addActionListener(this);
 		//menu.getChangeColor().addActionListener(this);
 		menu.getCb1().addActionListener(this);
 		menu.getCb2().addActionListener(this);
 		btZoomToFit.addActionListener(this);
 		btDisplay.addActionListener(this);
 
 		getContentPane().add(graphComponent);
 
 	}
 
 
 	public void initFrame(){
 		graph.getModel().beginUpdate();
 		try
 		{
 			for (Cell c : listCells) {
 
 				Object v1 = graph
 						.insertVertex(parent, null, c.getClass_name(),
 								c.getInner_x()*0.4, c.getInner_y()*0.4 , 10, 10,
 								"shape=ellipse;per=ellipsePerimeter;fillColor="
 										+ m.get(c.getClass_name()));
 			}
 		} finally {
 			graph.getModel().endUpdate();
 		}
 		//display background
 		graphComponent.setBackgroundImage(new ImageIcon("src/ressources/image0046.jpg"));			
 		ImageIcon img = new ImageIcon(img_default);
 		img = scale(img_default, (int)(img.getIconWidth()*0.4),(int)(img.getIconHeight()*0.4));
 		graphComponent.setBackgroundImage(img);			
 
 		getContentPane().add(graphComponent, BorderLayout.CENTER);
 	}
 	public static ImageIcon scale(String source, int width, int height) {
 
 		ImageIcon icon = new ImageIcon(source);
 		Image imag = icon.getImage();
 		BufferedImage bi = new BufferedImage(imag.getWidth(null), imag.getHeight(null), BufferedImage.TYPE_INT_ARGB);
 		Graphics g = bi.createGraphics();
 		g.drawImage(imag, 0, 0, width, height, null);
 		ImageIcon newIcon = new ImageIcon(bi);
 		return newIcon;
 	}
 	public void changeFrame(String path){
 
 
 		graph.setCellsDeletable(true);
 		this.graph.removeCells(this.graph.getChildVertices(this.parent));
 		graph.refresh();
 		graph.setCellsDeletable(false);
 		listCells.clear();
 		listCells = setListCell(path);
 
 		
 		graph.getModel().beginUpdate();
 		try
 		{
 			for (Cell c : listCells) {
 
 				Object v2 = graph
 						.insertVertex(parent, null, c.getClass_name(),
 								c.getInner_x()*0.4, c.getInner_y()*0.4 , 10, 10,
 								"shape=ellipse;per=ellipsePerimeter;fillColor="
 										+ m.get(c.getClass_name()));
 			}
 		}
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 
 		//display background
 
 		ImageIcon img = new ImageIcon(path_image);
 		img = scale(path_image, (int)(img.getIconWidth()*0.4),(int)(img.getIconHeight()*0.4));
 		graphComponent.setBackgroundImage(img);	
 		getContentPane().add(graphComponent, BorderLayout.CENTER);
 
 	}
 	public void displaySelectedCells(String nameSelected){
 		graph.getModel().beginUpdate();
 		try
 		{
 			for (Cell c : listCells) {
 				if(c.getClass_name().equals(nameSelected)){
 
 					Object v2 = graph
 							.insertVertex(parent, null, c.getClass_name(),
 									c.getInner_x()*0.4, c.getInner_y()*0.4 , 10, 10,
 									"shape=ellipse;per=ellipsePerimeter;fillColor="
 											+ m.get(c.getClass_name()));
 				}
 			}
 		}
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 
 	}
 	public void actionPerformed(ActionEvent e){
 
 		if(e.getSource() == menu.getExit() ){
 			FramCell.this.setVisible(false);
 		}
 		
 		if( e.getSource() == menu.getCb2()){
 			graphComponent.setBackgroundImage(new ImageIcon("src/ressources/Back_White.png"));			
 			getContentPane().add(graphComponent);
 			graphComponent.refresh();
 			}
 		if(e.getSource() == menu.getCb1() ){
 			//display or remove image 
 			
 			ImageIcon img = new ImageIcon(path_image);
 			img = scale(path_image, (int)(img.getIconWidth()*0.4),(int)(img.getIconHeight()*0.4));
 			graphComponent.setBackgroundImage(img);			
 			getContentPane().add(graphComponent);
 			graphComponent.refresh();
 		}
 		if(e.getSource() == menu.getOpen()){
 
 			JFileChooser chooser = new JFileChooser();
 
 			chooser.setApproveButtonText("Choose File..."); // intitul du
 			// bouton
 			chooser.showOpenDialog(null); // affiche la boite de dialogue
 			path_current = chooser.getSelectedFile().getAbsolutePath();
 			System.out.println("Path selected current : " + path_current);
 
 			StringTokenizer st = new StringTokenizer(path_current, "."); 
 
 			while (st.hasMoreTokens()) { 
 
 				//System.out.println("token:"+st.nextToken()); 
 
 				String path_initial = st.nextToken();
 				System.out.println("path_initial : " + path_initial); 
 
 				String ext=st.nextToken();
 				System.out.println("ext : " + ext);
 
 				if(ext.equals("csv")){
 					System.out.println("Ceci est un bon fichier ");
 					System.out.println(ext);
 
 					//remplace .cvs to jpg
 					String newPath1=ext.replace(ext.charAt(0), 'j');
 					String newPath2=newPath1.replace(ext.charAt(1), 'p');
 					String newPath3=newPath2.replace(ext.charAt(2), 'g');
 
 					// creation de path.jpg
 					path_image = path_initial +"."+ newPath3;
 
 					System.out.println("path_image " + path_image);
 
 					// aller chercher path.jpg dans le dossier
 
 					searchFile = new SearchFile(path_image);
 					boolean found = searchFile.searchFileImage(searchFile.name, searchFile.filePath);
 					if (found ){
 						System.out.println("ok found file .jpg ");
 						JOptionPane.showMessageDialog(graphComponent, "File .jpn  found ! Name is :"+path_image,"avertissement",
 								JOptionPane.WARNING_MESSAGE);
 						FramCell.this.setVisible(true);
 					}
 					else {
 						JOptionPane.showMessageDialog(graphComponent, "File .jpn not found ! Default image is image0046 ",
 								"avertissement",
 								JOptionPane.ERROR_MESSAGE);
 						System.out.println("Ko not found .jpg ");
 						path_image = img_default;
 						FramCell.this.setVisible(true);
 					}
 
 					//if .cvs is found then display new graph
 					System.out.println("path using with change Frame"+ path_current);
 					changeFrame(path_current);
 
 				}
 
 				else if (!ext.equals("csv")){
 					System.out.println("Ceci n'est pas un bon fichier ");
 
 					JOptionPane.showMessageDialog(graphComponent, "File choosen is not expected",
 							"avertissement",
 							JOptionPane.ERROR_MESSAGE);
 					FramCell.this.setVisible(false);
 
 
 				}
 
 			} 
 
 		}
 		if(e.getSource() == btZoomToFit){
 
 			double newScale = 1;
 
 			Dimension graphSize = graphComponent.getGraphControl().getSize();
 
 			Dimension viewPortSize = graphComponent.getViewport().getSize();
 
 			int gw = (int) graphSize.getWidth();
 			int gh = (int) graphSize.getHeight();
 			System.out.println("size graphSize : " + gw);
 			System.out.println("size graphSize : " + gh);
 
 			System.out.println("size viewPortSize : " + (int) viewPortSize.getWidth());
 			System.out.println("size viewPortSiee : " + (int) viewPortSize.getHeight());
 
 			if (gw > 0 && gh > 0) {
 				int w = (int) viewPortSize.getWidth();
 				int h = (int) viewPortSize.getHeight();
 				newScale = Math.min((double) w / gw, (double) h / gh);
 			}
 			graphComponent.zoom(newScale);
 			graphComponent.getGraphControl().scrollRectToVisible(new Rectangle(0,0,0,0));
 		}
 		if(e.getSource() == btDisplay){
 			graph.setCellsDeletable(true);
 			this.graph.removeCells(this.graph.getChildVertices(this.parent));
 			graph.refresh();
 			graph.setCellsDeletable(false);
 			if(checkAll.isSelected()){
 				changeFrame(path_current);
 			}
 			for(String p : checkBoxes.keySet()){
 				if(checkBoxes.get(p).isSelected()){
 					displaySelectedCells(p);
 				}}}
 			
 		}
 
 
 	
 	public List<Cell> setListCell(String path){
 		File myFile = new File(path);
 		FillCellWithCSV f;
 		try {
 			f = new FillCellWithCSV(myFile);
 			List<Cell> listcells = f.allCells();
 			return listcells;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			setListCell(path_excel_default);
 		}
 		return null;
 
 
 
 	}
 	
 
 	public void  addCellWithMap(){
 		HashMap<String,String> tmp = new HashMap< String,String>();
 		
 		for (Cell c : listCells) {
 			tmp.put(c.getClass_name(),color);
 		}
 		Set<String> k=tmp.keySet();
 		/*Set<String> v=(Set<String>) tmp.values();
 		for(String col : tmp.values()){
 			System.out.println(col);
 		}*/
 			
 		//while(k.hasMoreElements())
 			for(String j : tmp.keySet() ){
 				System.out.println(j.replaceAll(" ", "_"));
 			    newCell = new JMenuItem(j.replaceAll(" ", "_"));
 				//menu.getAddCell().add(newCell);
 				menu.getPropertyCells().add(newCell);
 				//celselected=newCell.getSelectedObjects().toString();
 				//System.out.println("selectedCell:" +celselected);
 				newCell.addActionListener(new ActionListener() {
 				
 					
 					@Override
 					public void actionPerformed(ActionEvent evt) {
 						String[] list = {"RED", "GREEN", "BLUE","YELLOW", "BLACK", "WHITE","ORANGE", "PURPLE"};
 						JComboBox jcb = new JComboBox(list);
 						jcb.setEditable(false);
 						jcb.getSelectedItem();
 						JOptionPane.showMessageDialog( null, jcb,evt.getActionCommand(),JOptionPane.QUESTION_MESSAGE);
 						celselected=evt.getActionCommand();
 						System.out.println("chosenCell:" + celselected);
 						color=jcb.getSelectedItem().toString();
 						System.out.println("chosenColor:" + color);
 					
 						
 					}
 					
 				});
 				
 			
 				
 			}
 		
 		}
//Commit
 	public void MapColorCell(){
 		
 		
 		
 	     m.put("Tumor nucleus","red");
 		
 		m.put("Granulocyte nucleus", "yellow");
 		m.put("Lymphocyte Nucleus", "green");
 		m.put("Nucleus DAB+ PRD+", "black");
 		//m.put(celselected, color);
 		//System.out.println(m.keySet());
 
 
 
 	}
 }
