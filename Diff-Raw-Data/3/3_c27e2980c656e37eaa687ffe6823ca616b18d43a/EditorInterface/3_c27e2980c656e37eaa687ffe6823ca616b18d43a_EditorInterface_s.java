 package org.aimas.craftingquest.mapeditor;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.Scrollbar;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 import org.aimas.craftingquest.state.CellState.CellType;
 import org.aimas.craftingquest.state.resources.ResourceType;
 
 
 @SuppressWarnings("serial")
 public class EditorInterface extends JFrame implements ActionListener, ItemListener {
 	private static final String SMALL_MAP = "small_map";
 	private static final String MEDIUM_MAP = "medium_map";
 	private static final String BIG_MAP = "big_map";
 	
 	private static Map<String, Map<String, Integer>> mapSizes;
 	static {
 		mapSizes = new HashMap<String, Map<String,Integer>>();
 		Map<String, Integer> smallMap = new HashMap<String, Integer>();
 		smallMap.put("height", 60);
 		smallMap.put("width", 60);
 		
 		Map<String, Integer> mediumMap = new HashMap<String, Integer>();
 		mediumMap.put("height", 80);
 		mediumMap.put("width", 80);
 		
 		Map<String, Integer> bigMap = new HashMap<String, Integer>();
 		bigMap.put("height", 100);
 		bigMap.put("width", 100);
 		
 		mapSizes.put(SMALL_MAP, smallMap);
 		mapSizes.put(MEDIUM_MAP, mediumMap);
 		mapSizes.put(BIG_MAP, bigMap);
 	}
 	
 	private MapCell[][] terrain;
 	
 	private int mapHeight = 60;
 	private int mapWidth = 60;
 	
 	private JPanel mapPanel;
 	private JPanel rightPanel;
 	
 	private EditorCanvas editorCanvas;
 	private MiniEditorCanvas miniEditorCanvas;
 	private Scrollbar hs, vs;
 	private JButton mirrorLeftRightBtn, mirrorUpDownBtn, openMapBtn, saveMapBtn, terrainTypeInfoBtn, resourceTypeInfoBtn;
 	private JTextArea infoArea;
 	private JComboBox mapSizeSelector;
 	
 	private JComboBox terrainSelector;
 	private JComboBox resourceSelector;
 	private JTextField resourceQuantityText;
 	
 	private JFileChooser fileChooser = new JFileChooser("maps");
 	
 	private static final int WIDTH = 1200;
 	private static final int HEIGHT = 660;
 	
 	public EditorInterface() {
 		setLayout(new BorderLayout(10, 10));
 		setSize(WIDTH, HEIGHT);
 		
 		mapPanel = new JPanel(new BorderLayout());
 		hs = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 0, 1);
 		vs = new Scrollbar(Scrollbar.VERTICAL, 0, 1, 0, 1);
 		
 		terrain = new MapCell[mapHeight][mapWidth];
 		for (int i = 0; i < mapHeight; i++) {
 			for (int j = 0; j < mapWidth; j++) {
 				terrain[i][j] = new MapCell(CellType.Grass);
 			}
 		}
 		
 		JPanel mapSizePanel = new JPanel(new BorderLayout());
 		mapSizeSelector = new JComboBox(new String[] {SMALL_MAP, MEDIUM_MAP, BIG_MAP});
 		mapSizeSelector.addItemListener(this);
 		mapSizePanel.add(BorderLayout.CENTER, mapSizeSelector);
 		
 		editorCanvas = new EditorCanvas(terrain, hs, vs);
 		mapPanel.add(BorderLayout.CENTER, editorCanvas);
 		mapPanel.add(BorderLayout.EAST, vs);
 		mapPanel.add(BorderLayout.SOUTH, hs);
 
 		rightPanel = new JPanel(new BorderLayout(0, 10));
 		miniEditorCanvas = new MiniEditorCanvas(terrain, editorCanvas);
 		editorCanvas.setMiniMap(miniEditorCanvas);
 		rightPanel.add(BorderLayout.NORTH, miniEditorCanvas);
 		
 		
 		infoArea = new JTextArea();
 		infoArea.setEditable(false);
 		editorCanvas.setInfoArea(infoArea);
 		rightPanel.add(BorderLayout.CENTER, infoArea);
 		
 		
 		JPanel controlPanel = new JPanel(new GridLayout(4, 1));
 		
 		JPanel btnPnl = new JPanel();
 		openMapBtn = new JButton("Open Map");
 		openMapBtn.addActionListener(this);
 		btnPnl.add(openMapBtn);
 		saveMapBtn = new JButton("Save Map");
 		saveMapBtn.addActionListener(this);
 		btnPnl.add(saveMapBtn);
 		
 		mirrorLeftRightBtn = new JButton("Mir.(Oy)");
 		mirrorLeftRightBtn.addActionListener(this);
 		btnPnl.add(mirrorLeftRightBtn);
 		
 		mirrorUpDownBtn = new JButton("Mir.(Ox)");
 		mirrorUpDownBtn.addActionListener(this);
 		btnPnl.add(mirrorUpDownBtn);
 		
 		JPanel terrainSelectorPanel = new JPanel();
 		JPanel resourceSelectorPanel = new JPanel();
 		
 		terrainSelector = new JComboBox(CellType.values());
 		terrainSelector.addActionListener(this);
 		
 		resourceSelector = new JComboBox(ResourceType.values());
 		resourceSelector.addActionListener(this);
 		
 		resourceQuantityText = new JTextField("10", 4);
 		
 		terrainTypeInfoBtn = new JButton("Select terrain type");
 		terrainTypeInfoBtn.addActionListener(this);
 
 		resourceTypeInfoBtn = new JButton("Select resource type");
 		resourceTypeInfoBtn.addActionListener(this);
 		
 		
 		terrainSelectorPanel.add(terrainSelector);
 		terrainSelectorPanel.add(terrainTypeInfoBtn);
 		
 		resourceSelectorPanel.add(resourceSelector);
 		resourceSelectorPanel.add(resourceTypeInfoBtn);
 		resourceSelectorPanel.add(resourceQuantityText);
 		
 		controlPanel.add(mapSizePanel);
 		controlPanel.add(btnPnl);
 		controlPanel.add(terrainSelectorPanel);
 		controlPanel.add(resourceSelectorPanel);
 		
 		rightPanel.add(BorderLayout.SOUTH, controlPanel);
 		
 		add(BorderLayout.CENTER, mapPanel);
 		add(BorderLayout.EAST, rightPanel);
 		
         addWindowListener(new WindowAdapter() {
             public void windowClosing(final WindowEvent we) {
                 System.exit(0);
             }
         });
         
 		setTitle("Map editor");
 		//setVisible(true);
 		//printDebugInfo();
 	}
 	
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(saveMapBtn)) {
 			int returnVal = fileChooser.showSaveDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File file = fileChooser.getSelectedFile();
                 //This is where a real application would save the file.
                 
                 MapWriter mapWriter = new MapWriter(file, terrain);
                 mapWriter.writeMap();
             }
 		}
 		
 		if (e.getSource().equals(openMapBtn)) {
 			int returnVal = fileChooser.showOpenDialog(this);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				File file = fileChooser.getSelectedFile();
                 MapReader.readMap(file);
                 
                 mapWidth = MapReader.mapWidth;
                 mapHeight = MapReader.mapHeight;
                 terrain = MapReader.cells;
                 
                 editorCanvas.setTerrain(terrain);
                 editorCanvas.repaint();
     			miniEditorCanvas.repaint();
             } 
 		}
 		
 		if (e.getSource().equals(terrainTypeInfoBtn)) {
 			editorCanvas.setSelectedCellType((CellType)terrainSelector.getSelectedItem());
 		}
 		
 		if (e.getSource().equals(terrainSelector)) {
 			editorCanvas.setSelectedCellType((CellType)terrainSelector.getSelectedItem());
 		}
 		
 		if (e.getSource().equals(resourceTypeInfoBtn)) {
 			int resourceQuantity = Integer.parseInt(resourceQuantityText.getText());
 			editorCanvas.setSelectedResourceType((ResourceType)resourceSelector.getSelectedItem(), resourceQuantity);
 		}
 		
 		if (e.getSource().equals(resourceSelector)) {
 			int resourceQuantity = Integer.parseInt(resourceQuantityText.getText());
 			editorCanvas.setSelectedResourceType((ResourceType)resourceSelector.getSelectedItem(), resourceQuantity);
 		}
 		
 		if (e.getSource().equals(mirrorLeftRightBtn)) {
 			/*
 			int n = terrain.length;
 			
 			for (int i = 0; i < n; i++) {
 				for (int j = 0; j < n - i; j++) {
 					terrain[n - 1 - i][n - 1 - j].cellType = terrain[i][j].cellType;
 					terrain[n - 1 - i][n - 1 - j].cellResources = terrain[i][j].cellResources;
 				}
 			}
 			*/
 			
 			int height = terrain.length;
 			int width = terrain[0].length;
 			
 			for (int i = 0; i < height; i++) {
 				for (int j = 0; j < width / 2; j++) {
 					terrain[i][width - 1 - j].cellType = terrain[i][j].cellType;
 					terrain[i][width - 1 - j].cellResources.putAll(terrain[i][j].cellResources);
 				}
 			}
 			
 			editorCanvas.repaint();
 			miniEditorCanvas.repaint();
 		}
 		
 		
 		if (e.getSource().equals(mirrorUpDownBtn)) {
 			/*
 			int n = terrain.length;
 			
 			for (int i = 0; i < n; i++) {
 				for (int j = 0; j < n - i; j++) {
 					terrain[n - 1 - i][n - 1 - j].cellType = terrain[i][j].cellType;
 					terrain[n - 1 - i][n - 1 - j].cellResources = terrain[i][j].cellResources;
 				}
 			}
 			*/
 			
 			int height = terrain.length;
 			int width = terrain[0].length;
 			
 			for (int i = 0; i < height / 2; i++) {
 				for (int j = 0; j < width; j++) {
 					terrain[height - 1 - i][j].cellType = terrain[i][j].cellType;
 					terrain[height - 1 - i][j].cellResources.putAll(terrain[i][j].cellResources);
 				}
 			}
 			
 			editorCanvas.repaint();
 			miniEditorCanvas.repaint();
 		}
 	}
 
 	interface InfoProvider {
 		public String getInfo();
 	}
 
 	public int getMapHeight() {
 		return mapHeight;
 	}
 
 
 	public void setMapHeight(int mapHeight) {
 		this.mapHeight = mapHeight;
 	}
 
 
 	public int getMapWidth() {
 		return mapWidth;
 	}
 
 
 	public void setMapWidth(int mapWidth) {
 		this.mapWidth = mapWidth;
 	}
 	
 	public static void main(String args[]) {
 		final EditorInterface editInterface = new EditorInterface();
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				editInterface.setVisible(true);
 			}
 		});
 	}
 
 
 	@Override
 	public void itemStateChanged(ItemEvent event) {
 		if (event.getStateChange() == ItemEvent.SELECTED) {
 			String mapSize = (String)event.getItem();
 			
 			mapWidth = mapSizes.get(mapSize).get("width");
 			mapHeight = mapSizes.get(mapSize).get("height");
 			
 			// remove old components
 			mapPanel.remove(editorCanvas);
 			rightPanel.remove(miniEditorCanvas);
 			
 			terrain = new MapCell[mapHeight][mapWidth];
 			for (int i = 0; i < mapHeight; i++) {
 				for (int j = 0; j < mapWidth; j++) {
 					terrain[i][j] = new MapCell(CellType.Grass);
 				}
 			}
 			
 			editorCanvas = new EditorCanvas(terrain, hs, vs);
 			miniEditorCanvas = new MiniEditorCanvas(terrain, editorCanvas);
 			editorCanvas.setMiniMap(miniEditorCanvas);
 			editorCanvas.setInfoArea(infoArea);
 			
 			mapPanel.add(BorderLayout.CENTER, editorCanvas);
 			rightPanel.add(BorderLayout.NORTH, miniEditorCanvas);
 			
 			//repaint();
 			mapPanel.repaint();
 			rightPanel.repaint();
 		}
 	}
 }
