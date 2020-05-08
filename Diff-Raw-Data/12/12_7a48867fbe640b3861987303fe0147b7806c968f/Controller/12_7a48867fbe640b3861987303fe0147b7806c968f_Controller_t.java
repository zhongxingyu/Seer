 package hg.hist.MVC;
 
 import hg.histo.Cell;
 import hg.histo.FillCellWithCSV;
 import hg.histo.ImageToIcon;
 import hg.histo.Menu;
 import hg.histo.SearchFile;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.plaf.synth.ColorType;
 
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.view.mxGraph;
 
 public class Controller {
 
 
 	private String img_default = "src/ressources/image0046.jpg";
 	private String path_image = img_default;
 	private String path_excel_default = "src/ressources/image0046.csv";
 	private String path_current = path_excel_default;
 	private String path_initial;
 	private String color;
 
 	JMenuItem newCell;
 	
 	SearchFile searchFile;
 
 	String cellselected;
 	Object key;
 
 	private ImageIcon img = new ImageIcon(img_default);
 	private HashMap<String, String> m = new HashMap<String,String>();
 	private HashMap<String, JMenuItem> JMenuItems = new HashMap<String,JMenuItem>();
 
 	private List<Cell> listCells;
 
 	public Controller (){
 		listCells = new ArrayList<Cell>();
 		listCells = setListCell(path_current);
 		img = new ImageIcon(img_default);
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
 			setListCell(getPath_excel_default());
 		}
 		return null;
 	}
 	public mxGraphComponent changeFrame(String path,mxGraph graph,  mxGraphComponent graphComponent){
 		graph.setCellsDeletable(true);
 		graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
 		graph.refresh();
 		graph.setCellsDeletable(false);
 		this.listCells.clear();
 		this.listCells = setListCell(path);
 		
 		ChangeMapColor(this.listCells);
 		graph.getModel().beginUpdate();
 		try
 		{
 			for (Cell c : this.listCells) {
 				Object v2 = graph
 						.insertVertex(graph.getDefaultParent(), null, c.getClass_name(),
 								c.getInner_x()*0.4, c.getInner_y()*0.4 , 10, 10,
 								"shape=ellipse;per=ellipsePerimeter;fillColor="
 										+ getM().get(c.getClass_name()));
 			}
 		}
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 		//display background
 		ImageIcon img = new ImageIcon(getPath_image());
 		img = ImageToIcon.scale(path_image, (int)(img.getIconWidth()*0.4),(int)(img.getIconHeight()*0.4));
 		graphComponent.setBackgroundImage(img);	
 		return graphComponent;
 	}
	public void  ChangeColorOfCell(Menu menu){	
		getJMenuItems().clear();		
 		for(String j : getM().keySet() ){
 			newCell = new JMenuItem(j);
 			menu.getPropertyCells().add(newCell);
 			JMenuItems.put(j.toString(),newCell);
 			System.out.println("element de la JMenuItems:" + JMenuItems.keySet());
 		}
 	}
 	public void ChangeFile(JFileChooser chooser,mxGraphComponent graphComponent,mxGraph graph){	
 		path_current = chooser.getSelectedFile().getAbsolutePath();
 		System.out.println("Path selected current : " + path_current);
 		StringTokenizer st = new StringTokenizer(path_current, "."); 
 
 		while (st.hasMoreTokens()) { 
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
 				boolean found = searchFile.searchFileImage(searchFile.getName(), searchFile.getFilePath());
 				if (found ){
 					System.out.println("ok found file .jpg ");
 					JOptionPane.showMessageDialog(graphComponent, "File .jpn  found ! Name is :"+path_image,"avertissement",
 							JOptionPane.WARNING_MESSAGE);
 				}
 				else {
 					JOptionPane.showMessageDialog(graphComponent, "File .jpn not found ! Default image is image0046 ",
 							"avertissement",
 							JOptionPane.ERROR_MESSAGE);
 					System.out.println("Ko not found .jpg ");
 					path_image = img_default;
 				}
 				//if .cvs is found then display new graph
 				System.out.println("path using with change Frame"+ path_current);
 				changeFrame(path_current,graph,  graphComponent);
 			}
 			else if (!ext.equals("csv")){
 				System.out.println("Ceci n'est pas un bon fichier ");
 				JOptionPane.showMessageDialog(graphComponent, "File choosen is not expected",
 						"avertissement",
 						JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 	public double newScale(mxGraphComponent graphComponent){
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
 		return newScale;
 	}
 	public void MapColorCell(){
 		m.put("Tumor nucleus", "red");
 		m.put("Granulocyte nucleus", "yellow");
 		m.put("Lymphocyte Nucleus", "green");
 		m.put("Nucleus DAB+ PRD+", "black");
 		m.put("Nucleus DAB+", "blue");
 	}
 	public mxGraphComponent initFrame( mxGraph graph,  mxGraphComponent graphComponent){
 		graph.getModel().beginUpdate();
 		try
 		{
 			for (Cell c : this.listCells) {
 				Object v1 = graph
 						.insertVertex(graph.getDefaultParent(), null, c.getClass_name(),
 								c.getInner_x()*0.4, c.getInner_y()*0.4 , 10, 10,
 								"shape=ellipse;per=ellipsePerimeter;fillColor="
 										+ getM().get(c.getClass_name()));
 			}
 		} finally {
 			graph.getModel().endUpdate();
 		}
 		//display background
 		graphComponent.setBackgroundImage(new ImageIcon("src/ressources/image0046.jpg"));			
 		ImageIcon img = new ImageIcon(img_default);
 		img = ImageToIcon.scale(img_default, (int)(img.getIconWidth()*0.4),(int)(img.getIconHeight()*0.4));
 		graphComponent.setBackgroundImage(img);			
 
 		return graphComponent;
 		//getContentPane().add(graphComponent, BorderLayout.CENTER);
 	}
 	public void displaySelectedCells(String nameSelected,mxGraph graph){
 		graph.getModel().beginUpdate();
 		try
 		{
 			for (Cell c : this.listCells) {
 				if(c.getClass_name().equals(nameSelected)){
 
 					Object v2 = graph
 							.insertVertex(graph.getDefaultParent(), null, c.getClass_name(),
 									c.getInner_x()*0.4, c.getInner_y()*0.4 , 10, 10,
 									"shape=ellipse;per=ellipsePerimeter;fillColor="
 											+ getM().get(c.getClass_name()));
 				}
 			}
 		}
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 	}
 	public void changeMap(String typeCell,String typeColor){
 		for(String e:getM().keySet()){
 			if(typeCell.equals(e)){
 				getM().put(e, typeColor);
 			}
 		}
 	}
     public HashMap<String,String> ChangeMapColor(List<Cell> myNewList){
     	System.out.println("ChangeMapColor");
     	
 	HashMap<String,String> tmp = new HashMap<String, String>();
 	for(Cell c : myNewList){
 		tmp.put(c.getClass_name(), "orange");
 	}
 	HashMap<String,String> unionMap = new HashMap<String, String>();
 	unionMap.putAll(tmp);
 	unionMap.putAll(getM());
 	for(String e:unionMap.keySet()){	
 			System.out.println("key : "+ e);
 			System.out.println("object : "+ unionMap.get(e));
 		}
 	setM(unionMap);
 	return getM();
     }
 	public List<Cell> getListCells() {
 	return listCells;
 }
    public void setListCells(List<Cell> listCells) {
 	this.listCells = listCells;
    }
 	public String getColor() {
 		return color;
 	}
 	public void setColor(String color) {
 		this.color = color;
 	}
 	public JMenuItem getNewCell() {
 		return newCell;
 	}
 	public void setNewCell(JMenuItem newCell) {
 		this.newCell = newCell;
 	}
 	public String getImg_default() {
 		return img_default;
 	}
 	public void setImg_default(String img_default) {
 		this.img_default = img_default;
 	}
 	public String getPath_excel_default() {
 		return path_excel_default;
 	}
 	public void setPath_excel_default(String path_excel_default) {
 		this.path_excel_default = path_excel_default;
 	}
 	public String getPath_current() {
 		return path_current;
 	}
 	public void setPath_current(String path_current) {
 		this.path_current = path_current;
 	}
 	public String getPath_initial() {
 		return path_initial;
 	}
 	public void setPath_initial(String path_initial) {
 		this.path_initial = path_initial;
 	}
 	public ImageIcon getImg() {
 		return img;
 	}
 	public void setImg(ImageIcon img) {
 		this.img = img;
 	}
 
 	public String getPath_image() {
 		return path_image;
 	}
 	public void setPath_image(String path_image) {
 		this.path_image = path_image;
 	}
 
 	public HashMap<String, String> getM() {
 		return m;
 	}
 	public void setM(HashMap<String, String> m) {
 		this.m = m;
 	}
 	public Object getKey() {
 		return key;
 	}
 	public void setKey(Object key) {
 		this.key = key;
 	}
 	public String getCellselected() {
 		return cellselected;
 	}
 	public void setCellselected(String cellselected) {
 		this.cellselected = cellselected;
 	}
 	public HashMap<String, JMenuItem> getJMenuItems() {
 		return JMenuItems;
 	}
 	public void setJMenuItems(HashMap<String, JMenuItem> jMenuItems) {
 		JMenuItems = jMenuItems;
 	}
 }
 
 
 
 
