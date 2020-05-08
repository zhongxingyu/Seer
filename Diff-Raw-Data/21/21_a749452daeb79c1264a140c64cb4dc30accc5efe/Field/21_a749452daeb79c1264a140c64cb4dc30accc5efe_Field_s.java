 package model.gameoflife;
 
 import java.awt.Point;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author pierre
  */
 public class Field {
 
 	private HashMap<Point, Cell> _cells;
 	private HashMap<Point, Integer> _emergingPlaces; // coord d'une case vide -> nb voisin
 	private ArrayList<HashMap<Point, Cell>> _cellsFragments;
 	private ArrayList<HashMap<Point, Integer>> _emergingPlacesFragments;
 	private Point _size;
 	private int _threadNumber;
 	private int _currentThreadNumber;
 	
 	public static final int NB_FRAGMENT = 10;
 	//private Rule _rule;
 
 	/**
 	 * Constructeur par défaut
 	 *
 	 * @param size La taille du terrain.
 	 * @param r Les règle du jeu.
 	 */
 	public Field(Point size) {
 		this._size = size;
 
 		this._cells = new HashMap<>();
 		this._emergingPlaces = new HashMap<>();
 
 		_cellsFragments = new ArrayList<>();
 		_emergingPlacesFragments = new ArrayList<>();
 		
 		_threadNumber = 5;
 		_currentThreadNumber = -1;
 
 		for (int i = 0 ; i < _threadNumber ; ++i) {
 			_cellsFragments.add(new HashMap<Point, Cell>());
 			_emergingPlacesFragments.add(new HashMap<Point, Integer>());
 		}
 
 		//this._rule = r;
 
 		//this._rule.randomlyFill(this._size, this._cells, this._nighCases);
 	}
 
 	/*public void addCell(Point place, Cell cell) {
 		_cells.put(place, cell);
 
 		int index = this.getFragmentIndex(place);
 
 		_cellsFragments.get(index).put(place, cell);
 	}
 
 	public void removeCell(Point place) {
 		_cells.remove(place);
 
 		int index = this.getFragmentIndex(place);
 
 //		_cellsFragments.get(index).remove(place);
 	}
 
 	public void addEmergingPlace(Point place, int neighborhood) {
 		_emergingPlaces.put(place, neighborhood);
 
 		int index = this.getFragmentIndex(place);
 
 		_emergingPlacesFragments.get(index).put(place, neighborhood);
 	}
 
 	public int getFragmentIndex(Point place) {
 		return place.y / Field.NB_FRAGMENT * (int) Math.ceil(_size.x / (double) Field.NB_FRAGMENT)
 			   + place.x / Field.NB_FRAGMENT;
 	}*/
 
 	public HashMap<Point, Cell> getCells() {
 		return _cells;
 	}
 
 	public void setCells(HashMap<Point, Cell> cells) {
 		this._cells = cells;
 	}
 
 	public HashMap<Point, Integer> getEmergingPlaces() {
 		return _emergingPlaces;
 	}
 
 	public void setEmergingPlaces(HashMap<Point, Integer> emergingPlaces) {
 		this._emergingPlaces = emergingPlaces;
 
 	}
 
 	public Point getSize() {
 		return _size;
 	}
 
 	public void setSize(Point size) {
 		_size = size;
 
 		Iterator<Map.Entry<Point, Cell>> it = _cells.entrySet().iterator();
 
 		while (it.hasNext()) {
 
 			Cell cell = it.next().getValue();
 
 			Point pos = cell.getCoordinate();
 
 			if (pos.x < 0 || pos.y < 0 || pos.x >= _size.x || pos.y >= _size.y) {
 				it.remove();
 				this.removeCell(pos);
 			}
 		}
 	}
 
 	/*public String toString() {
 	
 	String str = new String();
 	
 	String[][] cells = new String[this._size.y][this._size.x];
 	String[][] emergingPlaces = new String[this._size.y][this._size.x];
 	
 	for (int y = 0; y < this._size.y; ++y) {
 	for (int x = 0; x < this._size.x; ++x) {
 	
 	cells[y][x] = ".";
 	emergingPlaces[y][x] = ".";
 	}
 	}
 	
 	
 	for (Map.Entry<Point, Cell> entry : this._cells.entrySet()) {
 	
 	Point c = entry.getKey();
 	
 	cells[c.y][c.x] = "O";
 	}
 	
 	for (Map.Entry<Point, Integer> entry : this._emergingPlaces.entrySet()) {
 	
 	Point ep = entry.getKey();
 	
 	emergingPlaces[ep.y][ep.x] = Integer.toString(entry.getValue());
 	}
 	
 	/*for(String[] row : cells) {
 	str += "[ ";
 	for(String place : row) {
 	
 	str += place + " ";
 	}
 	str += "]\n";
 	}
 	
 	str += "\n";
 	
 	for(String[] row : emergingPlaces) {
 	str += "[ ";
 	for(String place : row) {
 	
 	str += place + " ";
 	}
 	str += "]\n";
 	}*/
 
 	/*for (int y = 0; y < this._size.y; ++y) {
 	str += "[ ";
 	for (int x = 0; x < this._size.x; ++x) {
 	
 	str += cells[y][x] + " ";
 	}
 	str += "]\t [";
 	
 	for (int x = 0; x < this._size.x; ++x) {
 	
 	str += emergingPlaces[y][x] + " ";
 	}
 	str += "]\n";
 	}
 	
 	return str;
 	}*/
 	public int save(String name) {
 		File f = new java.io.File(name);
 		try {
 			f.createNewFile();
 		}
 		catch (IOException e) {
 			System.err.println("Could not create file");
 			return 2;
 		}
 		try {
 			FileOutputStream FOS = new java.io.FileOutputStream(f);
 			Iterator<Map.Entry<Point, Cell>> it = _cells.entrySet().iterator();
 
 			String title = "<cells>";
 			String begin = "\n\t<cell>\n\t\t<x>";
 			String end = "</y>\n\t</cell>";
 			String middle = "</x>\n\t\t<y>";
 			String titleend = "\n</cells>";
 
 			byte[] bbegin = begin.getBytes();
 			byte[] bmiddle = middle.getBytes();
 			byte[] bend = end.getBytes();
 
 			FOS.write(title.getBytes());
 			while (it.hasNext()) {
 				FOS.write(bbegin);
 				Point cell = it.next().getKey();
 				String x = Integer.toString(cell.x);
 				FOS.write(x.getBytes());
 				FOS.write(bmiddle);
 				String y = Integer.toString(cell.y);
 				FOS.write(y.getBytes());
 				FOS.write(bend);
 			}
 			FOS.write(titleend.getBytes());
 			return 1;
 		}
 		catch (FileNotFoundException e) {
 			System.err.println("Can not find the file");
 			return 3;
 		}
 		catch (IOException ex) {
 			ex.printStackTrace();
 			return 4;
 		}
 	}
 
 	public int load(String name) {
 		try {
 			// création d'une fabrique de documents
 			DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
 
 			// création d'un constructeur de documents
 			DocumentBuilder DB = DBF.newDocumentBuilder();
 
 			// lecture du contenu d'un fichier XML avec DOM
 			File xml = new File(name);
 			Document document = DB.parse(xml);
 
 			//traitement du document
 			Element root = document.getDocumentElement();
 			String tag = "cell";
 			NodeList liste = root.getElementsByTagName(tag);
 			HashMap<Point, Cell> hm = new HashMap();
 			for (int i = 0 ; i < liste.getLength() ; i++) {
 				Element e = (Element) liste.item(i);
 				NodeList coordXElement = e.getElementsByTagName("x");
 				NodeList coordYElement = e.getElementsByTagName("y");
 				int x = Integer.parseInt(coordXElement.item(0).getTextContent());
 				int y = Integer.parseInt(coordYElement.item(0).getTextContent());
 				Point coord = new Point(x, y);
 				this.addCell(coord);
 				//hm.put(coord, new Cell(coord));
 			}
 			//this.setCells(hm);
 			return 1;
 		}
 		catch (ParserConfigurationException pce) {
 			System.err.println("Configuration error DOM parser");
 			System.err.println("when calling to DBF.newDocumentBuilder();");
 			return 2;
 		}
 		catch (SAXException se) {
 			System.err.println("Error while parsing the document");
 			System.err.println("when calling to DB.parse(xml)");
 			return 3;
 		}
 		catch (IOException ioe) {
 			System.err.println("Error I/O");
 			System.err.println("when calling to DB.parse(xml)");
 			return 4;
 		}
 	}
 
 	public ArrayList patternList() {
 		String[] list;
 		ArrayList result = new ArrayList();
 		int i;
 		File f = new File("src/resources/");
 
 		list = f.list();
 		for (i = 0 ; i < list.length ; i++) {
 			if (list[i].endsWith(".cells")) {
 				result.add(list[i].substring(0, list[i].length() - 6));
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public String toString() {
 
 		String str = new String();
 
 		String[][] cells = new String[this._size.y][this._size.x];
 		String[][] emergingPlaces = new String[this._size.y][this._size.x];
 
 		for (int y = 0 ; y < this._size.y ; ++y) {
 			for (int x = 0 ; x < this._size.x ; ++x) {
 
 				cells[y][x] = ".";
 				emergingPlaces[y][x] = ".";
 			}
 		}
 
 
 		for (Map.Entry<Point, Cell> entry : this._cells.entrySet()) {
 
 			Point c = entry.getKey();
 
 			if (entry.getValue().getState().isAlive()) {
 				cells[c.y][c.x] = "0";
 			}
 			else {
 				cells[c.y][c.x] = "X";
 			}
 		}
 
 		for (Map.Entry<Point, Integer> entry : this._emergingPlaces.entrySet()) {
 
 			Point ep = entry.getKey();
 
 			emergingPlaces[ep.y][ep.x] = Integer.toString(entry.getValue());
 		}
 
 		for (int y = 0 ; y < this._size.y ; ++y) {
 			str += "[ ";
 			for (int x = 0 ; x < this._size.x ; ++x) {
 
 				str += cells[y][x] + " ";
 			}
 			str += "]\t [";
 
 			for (int x = 0 ; x < this._size.x ; ++x) {
 
 				str += emergingPlaces[y][x] + " ";
 			}
 			str += "]\n";
 		}
 
 		return str;
 
 	}
 	
 	private int getCurrentThreadNumber() {
 		
 		_currentThreadNumber++;
 		if(_currentThreadNumber >= _cellsFragments.size()) {
 			_currentThreadNumber = 0;
 		}
 		
 		return _currentThreadNumber;
 	}
 	
 	
 	
 	public void addCell(Point coord){
 		
 		coord = new Point(coord);
 		
 		int thread = this.getCurrentThreadNumber();
 		
 		Cell c =  new Cell(coord, thread);
 		
 		this.add(c);
 	}
 	
 	public void addEmergingCell(Point coord){
 		coord = new Point(coord);
 		
 		int thread = this.getCurrentThreadNumber();
 		
 		Cell c =  Cell.getEmergingCell(coord, thread);
 		
 		this.add(c);
 	}
 	
 	private void add(Cell c) {
 		
 		_cells.put(c.getCoordinate(), c);
 		_cellsFragments.get(c.getCurrentThreadNumber()).put(c.getCoordinate(), c);
 	}
 	
 	public void removeCell(Point coord) {
 		
 		//Cell c = _cells.get(coord);
 		
 		//_cellsFragments.get(c.getCurrentThreadNumber()).remove(coord);
 		_cells.remove(coord);
 	}
 	
 	public void clearEmergingPlaces() {
 		_emergingPlaces.clear();
 		int fragmentNumber = _emergingPlacesFragments.size();
 		for(int i = 0; i < fragmentNumber; ++i) {
 			_emergingPlacesFragments.get(i).clear();
 		}
 	}
 	
 	public void addEmergingPlace(Point coord, Integer neighborNumber){
 		coord = new Point(coord);
 		
 		_emergingPlaces.put(coord, neighborNumber);
 		
 		int thread = (coord.x + coord.y) % _threadNumber;
 		
 		_emergingPlacesFragments.get(thread).put(coord, neighborNumber);
 	}
 }
