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
 import mapeditor.MapManager;
 import mapeditor.MapObject;
 
 public class MapSetupPanel extends JPanel  {
 	public JTextField xField;
 	public JTextField yField;
 	
 	private JList mapOList;
 	private JButton save;
 	private JButton load;
 	private JButton saveMapOSet;
 	private JButton loadMapOSet;
 	private JButton addMapObject;
 	private JButton removeMapObject;
 	private JButton imageView;
 	
 	// Vector components of the isometric coordinate axes. 
 	private JTextField xHorisontal;
 	private JTextField xVertical;
 	private JTextField yHorisontal;
 	private JTextField yVertical;
 
 	public MapSetupPanel(){
 
 		//Initialize objects
 		mapOList = new JList();	
 		xField = new JTextField(""+CData.getArraySizeX());
 		yField = new JTextField(""+CData.getArraySizeX());
 		xHorisontal = new JTextField(""+MapData.getRotationIndex(0, 0));
 		xVertical = new JTextField(""+MapData.getRotationIndex(1, 0));
 		yHorisontal = new JTextField(""+MapData.getRotationIndex(0, 1));
 		yVertical = new JTextField(""+MapData.getRotationIndex(1, 1));
 		save = new JButton("Save Map");
 		load = new JButton("Load Map");
 		addMapObject = new JButton("Add MapO");
 		removeMapObject = new JButton("Remove MapO");
 		imageView = new JButton("View images");
 		saveMapOSet = new JButton("Save Tileset");
 		loadMapOSet = new JButton("Load Tileset");
 		
 		
 		xField.setColumns(3);
 		yField.setColumns(3);
 		xHorisontal.setColumns(3);
 		xVertical.setColumns(3);
 		yHorisontal.setColumns(3);
 		yVertical.setColumns(3);
 		mapOList.setPreferredSize(new Dimension(80, 20));
 		mapOList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		addActionListeners();
 		addComponents();
 
 	}
 
 	private void addComponents(){
 		add(xField);
 		
 		add(new JLabel("x"));
 		
 		add(yField);
 		
 		add(addMapObject);
 		
 		add(removeMapObject);
 		
 		JScrollPane listScrollPane = new JScrollPane(mapOList);
 		add(listScrollPane);
 		
 		add(imageView);
 		
 		add(load);
 		
 		add(save);
 		
 		add(saveMapOSet);
 		
 		add(loadMapOSet);
 		
 		add(new JLabel("x-axis:          "));
 		
 		add(xHorisontal);
		
		add(xVertical);
 
 		add(new JLabel("y-axis:          "));
 		
 		add(yHorisontal);

		add(yVertical);
 	}
 
 	private void addActionListeners(){
 		mapOList.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if(e.getValueIsAdjusting() == false){
 					JList list = (JList)e.getSource();
 					MapObject mapO = (MapObject) list.getSelectedValue();
 					if(mapO == CData.curMapO) return;
 					CData.curMapO = mapO;
 					CData.mainFrame.repaint();
 					if(mapO.getID() == 0) CData.propertiesPanel.disableAllProperties();
 					else CData.propertiesPanel.enableAllProperties();
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
 				MapObject mapO = (MapObject) mapOList.getSelectedValue();
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
 		
 		class AxisListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try{
 					CData.loadLevel(MapData.mapWidth, MapData.mapHeight, 
 							Integer.parseInt(xHorisontal.getText()),
 							Integer.parseInt(xVertical.getText()),
 							Integer.parseInt(yHorisontal.getText()),
 							Integer.parseInt(yVertical.getText()));
 				}catch (Exception e) {
 					xHorisontal.setText(""+MapData.getRotationIndex(0, 0));
 					xVertical.setText(""+MapData.getRotationIndex(1, 0));
 					yHorisontal.setText(""+MapData.getRotationIndex(0, 1));
 					yVertical.setText(""+MapData.getRotationIndex(1, 1));
 				}
 			}
 		}
 		
 		xHorisontal.addActionListener(new AxisListener());
 		xVertical.addActionListener(new AxisListener());
 		yHorisontal.addActionListener(new AxisListener());
 		yVertical.addActionListener(new AxisListener());
 		
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
 			@Override
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
 		mapOList.setListData(CData.mapObjects.values().toArray());
 		updateUI();
 	}
 
 	public void updateAxes() {
 		xHorisontal.setText(""+MapData.getRotationIndex(0, 0));
 		xVertical.setText(""+MapData.getRotationIndex(1, 0));
 		yHorisontal.setText(""+MapData.getRotationIndex(0, 1));
 		yVertical.setText(""+MapData.getRotationIndex(1, 1));
 	}
 }
