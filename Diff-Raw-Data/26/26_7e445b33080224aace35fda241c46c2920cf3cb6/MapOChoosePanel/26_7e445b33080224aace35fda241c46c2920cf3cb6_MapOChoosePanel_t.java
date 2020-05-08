 package mapeditor.panels;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import mapeditor.CData;
 import mapeditor.MapData;
 import mapeditor.LImage;
 import mapeditor.MapManager;
 import mapeditor.MapObject;
 
 public class MapOChoosePanel extends JPanel  {
 	public JTextField xField;
 	public JTextField yField;
 	
 	private JList list;
 	private JButton save;
 	private JButton load;
 	private JButton saveMapOSet;
 	private JButton loadMapOSet;
 	private JButton addMapObject;
 	private JButton removeMapObject;
 	private JButton imageView;
 
 	public MapOChoosePanel(){
 
 		//Initialize objects
 		list = new JList();	
 		xField = new JTextField(""+CData.getArraySizeX());
 		yField = new JTextField(""+CData.getArraySizeX());
 		save = new JButton("Save Map");
 		load = new JButton("Load Map");
 		addMapObject = new JButton("Add MapO");
 		removeMapObject = new JButton("Remove MapO");
 		imageView = new JButton("View images");
 		saveMapOSet = new JButton("Save Tileset");
 		loadMapOSet = new JButton("Load Tileset");
 		
 		
 		xField.setColumns(3);
 		yField.setColumns(3);
 		list.setPreferredSize(new Dimension(80, 20));
 		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		addActionListeners();
 		addComponents();
 		createFirstMapO();
 
 	}
 
 	private void createFirstMapO() {
 		MapObject clearMapO = new MapObject((short)0, "Clear"); 
 		CData.mapObjects.put(0, clearMapO);
 	}
 
 	private void addComponents(){
 		add(xField);
 		
 		add(new JLabel("x"));
 		
 		add(yField);
 		
 		add(addMapObject);
 		
 		add(removeMapObject);
 		
 		JScrollPane listScrollPane = new JScrollPane(list);
 		add(listScrollPane);
 		
 		add(imageView);
 		
 		add(load);
 		
 		add(save);
 		
 		add(saveMapOSet);
 		
 		add(loadMapOSet);
 	}
 
 	private void addActionListeners(){
 		list.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if(e.getValueIsAdjusting() == false){
 					JList list = (JList)e.getSource();
 					MapObject mapO = (MapObject) list.getSelectedValue();
 					if(mapO == mapO) return;
 					CData.curMapObj = mapO;
 					CData.mainFrame.repaint();
 				}
 			}
 		});
 		
 		addMapObject.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MapManager.addMapO();
				updateList();
				updateUI();
 			}
 		});
 		
 		removeMapObject.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MapObject mapO = (MapObject) list.getSelectedValue();
				if(mapO.getID() == 0) return; // this is the clear object. Should not
				//remove this.
 				MapManager.removeMapO(mapO);
				updateList();
				updateUI();
 			}
 		});
 
 
 		xField.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try{
 					CData.changeLevelSize(Integer.parseInt(xField.getText()), 
 										  MapData.mapHeight);
 				}catch (Exception e) {
 					xField.setText(""+CData.getArraySizeX());
 				}
 
 			}
 		});
 
 		yField.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try{
 					CData.changeLevelSize(MapData.mapWidth, 
 										  Integer.parseInt(yField.getText()));
 				}catch (Exception e) {
 					yField.setText(""+CData.getArraySizeY());
 				}
 
 			}
 		});
 		
 		imageView.addActionListener(new ActionListener() {
 			
 			private ImagePopupViewer imageViewWindow;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(imageViewWindow == null){
 					imageViewWindow = new ImagePopupViewer();
 				}
 				imageViewWindow.pack();
 				imageViewWindow.setVisible(true);
 				imageViewWindow.requestFocus();
 			}
 		});
 
 		save.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveMapFile();
 			}
 		});
 
 		load.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				loadMapFile();
 			}
 		});
 
 		saveMapOSet.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MapManager.writeTileSet("tileset.xml");
 
 			}
 		});
 
 		loadMapOSet.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 //				MapManager.loadTileSet(new File("tileset.xml"));
 
 			}
 		});
 	}
 
 	private void loadMapFile(){
 		MapManager.loadTestMap();
 //		JFileChooser fileChooser = new JFileChooser();
 //		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
 //		fileChooser.setCurrentDirectory(new File("."));
 //		FileNameExtensionFilter filter = 
 //			new FileNameExtensionFilter("Infectosaurus map file *.ism","ism");
 //		fileChooser.setFileFilter(filter);
 //		fileChooser.setApproveButtonText("Load");
 //		fileChooser.setDialogTitle("Load map");
 //
 //
 //		int returnVal = fileChooser.showOpenDialog(CData.mainFrame);
 //		if(returnVal == JFileChooser.APPROVE_OPTION){
 //			File file = fileChooser.getSelectedFile();
 //
 //			MapManager.loadMap(file);
 //		}
 	}
 	
 	private void saveMapFile(){
 		
 		JFileChooser fileChooser = new JFileChooser();
 		fileChooser.setCurrentDirectory(new File("."));
 		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
 		FileNameExtensionFilter filter = 
 			new FileNameExtensionFilter("Infectosaurus map file *.ism)","ism");
 		fileChooser.setFileFilter(filter);
 		fileChooser.setApproveButtonText("Save");
 		fileChooser.setDialogTitle("Save map");
 
 
 		int returnVal = fileChooser.showSaveDialog(CData.mainFrame);
 		if(returnVal == JFileChooser.APPROVE_OPTION){
 			File file = fileChooser.getSelectedFile();
 
 			//Append .ism if it`s not there
 			String fName = file.getName();
 			if(fName.indexOf('.') == -1){
 				fName += "."+ filter.getExtensions()[0];
 				file = new File(file.getParentFile(),fName);
 			}
 
 			MapManager.writeMap(file);
 		}
 	}
 	
 	public void updateList(){
 		list.setListData(CData.mapObjects.values().toArray());
 		updateUI();
 	}
 }
