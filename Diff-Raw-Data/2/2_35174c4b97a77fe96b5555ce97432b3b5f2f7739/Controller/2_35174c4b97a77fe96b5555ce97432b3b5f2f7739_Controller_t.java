 package matachi.mapeditor.editor;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import matachi.mapeditor.grid.Camera;
 import matachi.mapeditor.grid.Grid;
 import matachi.mapeditor.grid.GridCamera;
 import matachi.mapeditor.grid.GridModel;
 import matachi.mapeditor.grid.GridView;
 
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 /**
  * Controller of the application.
  * 
  * @author Daniel "MaTachi" Jonsson
  * @version 1
  * @since v0.0.5
  * 
  */
 public class Controller implements ActionListener, GUIInformation {
 
 	/**
 	 * The model of the map editor.
 	 */
 	private Grid model;
 
 	private Tile selectedTile;
 	private Camera camera;
 
 	private List<Tile> tiles;
 
 	private GridView grid;
 	private View view;
 
 	private int gridWith = Constants.MAP_WIDTH;
 	private int gridHeight = Constants.MAP_HEIGHT;
 
 	/**
 	 * Construct the controller.
 	 */
 	public Controller() {
 		init(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
 
 	}
 
 	public void init(int width, int height) {
 		this.tiles = TileManager.getTilesFromFolder("data/");
 		this.model = new GridModel(width, height, tiles.get(0).getCharacter());
 		System.out.println(tiles.get(0).getCharacter());
 
 		this.camera = new GridCamera(model, Constants.GRID_WIDTH,
 				Constants.GRID_HEIGHT);
 
 		grid = new GridView(this, camera, tiles); // Every tile is
 													// 30x30 pixels
 
 		this.view = new View(this, camera, grid, tiles);
 	}
 
 	/**
 	 * Different commands that comes from the view.
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		for (Tile t : tiles) {
 			if (e.getActionCommand().equals(
 					Character.toString(t.getCharacter()))) {
 				selectedTile = t;
 				break;
 			}
 		}
 		if (e.getActionCommand().equals("flipGrid")) {
 			// view.flipGrid();
 		} else if (e.getActionCommand().equals("save")) {
 			saveFile();
 		} else if (e.getActionCommand().equals("load")) {
 			loadFile();
 		} else if (e.getActionCommand().equals("update")) {
 			updateGrid(gridWith, gridHeight);
 		}
 	}
 
 	public void updateGrid(int width, int height) {
 		view.close();
 		init(width, height);
 		view.setSize(width, height);
 	}
 
 	DocumentListener updateSizeFields = new DocumentListener() {
 
 		public void changedUpdate(DocumentEvent e) {
 		}
 
 		public void removeUpdate(DocumentEvent e) {
 			gridWith = view.getWidth();
 			gridHeight = view.getHeight();
 		}
 
 		public void insertUpdate(DocumentEvent e) {
 			gridWith = view.getWidth();
 			gridHeight = view.getHeight();
 		}
 	};
 
 	// OLD
 	private void saveTEXTFile() {
 		do {
 			JFileChooser chooser = new JFileChooser();
 			FileNameExtensionFilter filter = new FileNameExtensionFilter(
 					"Text/Java files", "txt", "java");
 			chooser.setFileFilter(filter);
 			int returnVal = chooser.showSaveDialog(null);
 			try {
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					PrintStream p;
 					p = new PrintStream(chooser.getSelectedFile());
 					p.print(model.getMapAsString());
 					break;
 				} else {
 					break;
 				}
 			} catch (FileNotFoundException e1) {
 				JOptionPane.showMessageDialog(null, "Invalid file!", "error",
 						JOptionPane.ERROR_MESSAGE);
 			}
 		} while (true);
 	}
 
 	private void saveFile() {
 
 		JFileChooser chooser = new JFileChooser();
 		FileNameExtensionFilter filter = new FileNameExtensionFilter(
 				"xml files", "xml");
 		chooser.setFileFilter(filter);
 		int returnVal = chooser.showSaveDialog(null);
 		try {
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 
 				Element level = new Element("level");
 				Document doc = new Document(level);
 				doc.setRootElement(level);
 
 				Element size = new Element("size");
 				int height = model.getHeight();
 				int width = model.getWidth();
 				size.addContent(new Element("width").setText(width + ""));
 				size.addContent(new Element("height").setText(height + ""));
 				doc.getRootElement().addContent(size);
 
 				for (int y = 0; y < height; y++) {
 					Element row = new Element("row");
 					for (int x = 0; x < width; x++) {
 						char tileChar = model.getTile(x,y);
 						System.out.println(model.getTile(x,y));
 						String item = "";
 						String enemy = ""; 
 						String type = "AirTile";
 						//if (tileChar == '0')
 						//	type = "AirTile";
 						if (tileChar == '1')
 							type = "GroundTile";
 						else if (tileChar == '2')
 							type = "SpawnTile";
 						else if (tileChar == '3')
 							type = "EndTile";
 						else if (tileChar == '4')
 							item = "healthPack";
 						else if(tileChar == '5')
 							item = "laserPistol";
 						else if(tileChar == '6')
							item = "upgradePoint";
 						else if(tileChar == '7')
 							enemy = "ballbot";
 						else if(tileChar == '8')
 							enemy = "bucketbot";
 						else if(tileChar == '9')
 							enemy = "tankbot";
 						else if(tileChar == ':')
 							item = "rocketLauncher";
 						else if(tileChar == ';')
 							item = "shotgun";
 						
 						Element e = new Element("cell");
 						if(!item.equals(""))
 							e.setAttribute(new Attribute("item",item));
 						if(!enemy.equals(""))
 							e.setAttribute(new Attribute("enemy",enemy));
 						
 						row.addContent(e.setText(type));
 					}
 					doc.getRootElement().addContent(row);
 				}
 				XMLOutputter xmlOutput = new XMLOutputter();
 				xmlOutput.setFormat(Format.getPrettyFormat());
 				xmlOutput
 						.output(doc, new FileWriter(chooser.getSelectedFile()));
 
 				System.out.println("File Saved!");
 
 			}
 		} catch (FileNotFoundException e1) {
 			JOptionPane.showMessageDialog(null, "Invalid file!", "error",
 					JOptionPane.ERROR_MESSAGE);
 		} catch (IOException e) {
 		}
 	}
 
 	public void loadFile() {
 		SAXBuilder builder = new SAXBuilder();
 		try {
 			JFileChooser chooser = new JFileChooser();
 			File selectedFile;
 			BufferedReader in;
 			FileReader reader = null;
 
 			int returnVal = chooser.showOpenDialog(null);
 			Document document;
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				selectedFile = chooser.getSelectedFile();
 				if (selectedFile.canRead() && selectedFile.exists()) {
 					document = (Document) builder.build(selectedFile);
 
 					Element rootNode = document.getRootElement();
 
 					List sizeList = rootNode.getChildren("size");
 					Element sizeElem = (Element) sizeList.get(0);
 					int height = Integer.parseInt(sizeElem
 							.getChildText("height"));
 					int width = Integer
 							.parseInt(sizeElem.getChildText("width"));
 					updateGrid(width, height);
 
 					List rows = rootNode.getChildren("row");
 					for (int y = 0; y < rows.size(); y++) {
 						Element cellsElem = (Element) rows.get(y);
 						List cells = cellsElem.getChildren("cell");
 
 						for (int x = 0; x < cells.size(); x++) {
 							Element cell = (Element) cells.get(x);
 							String cellValue = cell.getText();
 							
 							
 							String item = cell.getAttributeValue("item");
 							String enemy = cell.getAttributeValue("enemy");
 							char tileNr;
 							if(item != null){
 								if(item.equals("healthPack"))
 									tileNr = '4';
 								else if(item.equals("laserPistol"))
 									tileNr = '5';
 								else if(item.equals("rocketLauncher"))
 									tileNr = ':';
 								else if(item.equals("shotgun"))
 									tileNr = ';';
 								else
 									tileNr = '6';
 							}
 							else if(enemy != null){
 								if(enemy.equals("ballbot"))
 									tileNr = '7';
 								else if(enemy.equals("bucketbot"))
 									tileNr = '8';
 								else
 									tileNr = '9';
 							}
 							else{
 								if (cellValue.equals("AirTile"))
 									tileNr = '0';
 								else if (cellValue.equals("GroundTile"))
 									tileNr = '1';
 								else if (cellValue.equals("SpawnTile"))
 									tileNr = '2';
 								else if (cellValue.equals("EndTile"))
 									tileNr = '3';
 								else
 									tileNr = '0';
 							}
 							model.setTile(x, y, tileNr);
 							grid.redrawGrid();
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 		}
 	}
 
 	// OLD
 	private void loadTEXTFile() {
 		try {
 			JFileChooser chooser = new JFileChooser();
 			File selectedFile;
 			BufferedReader in;
 			FileReader reader = null;
 
 			int returnVal = chooser.showOpenDialog(null);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				selectedFile = chooser.getSelectedFile();
 				if (selectedFile.canRead() && selectedFile.exists()) {
 					reader = new FileReader(selectedFile);
 				}
 			}
 
 			in = new BufferedReader(reader);
 			String line;
 			int y = 0;
 			while ((line = in.readLine()) != null) {
 				System.out.println(line);
 				for (int x = 0; x < line.length(); x++) {
 					char c = line.charAt(x);
 					model.setTile(x, y, c);
 				}
 				y++;
 			}
 			in.close();
 			grid.redrawGrid();
 		} catch (Exception e) {
 			System.out.println(e.getStackTrace());
 		}
 
 		/**
 		 * //catches input/output exceptions and all subclasses exceptions
 		 * catch(IOException ex) { jTxtAMain.append("Error Processing File:\n" +
 		 * ex.getMessage()+"\n"); } //catches nullpointer exception, file not
 		 * found catch(NullPointerException ex) {
 		 * jTxtAMain.append("Open File Cancelled:\n" + ex.getMessage()+"\n"); }
 		 */
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Tile getSelectedTile() {
 		return selectedTile;
 	}
 }
