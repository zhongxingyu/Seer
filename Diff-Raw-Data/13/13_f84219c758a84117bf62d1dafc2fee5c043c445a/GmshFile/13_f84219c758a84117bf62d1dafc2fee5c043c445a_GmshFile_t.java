 package ro.ddanciu.finev.io.gmsh;
 
 import static java.lang.String.format;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import ro.ddanciu.finite.elements.api.Point;
 import ro.ddanciu.finite.elements.api.PoliLine;
 
 /**
  * 
  * @author dan
  *
  * <pre>
      $MeshFormat
      VERSION-NUMBER FILE-TYPE DATA-SIZE
      $EndMeshFormat
      $Nodes
      NUMBER-OF-NODES
      NODE-NUMBER X-COORD Y-COORD Z-COORD
      ...
      $EndNodes
      $Elements
      NUMBER-OF-ELEMENTS
      ELM-NUMBER ELM-TYPE NUMBER-OF-TAGS < TAG > ... NODE-NUMBER-LIST
      ...
      $EndElements
  * </pre>
  */
 public class GmshFile {
 
 	private static final String SEPARATOR = "\\s+";
 	private static final String FORMAT = "2.2";
 	private static final int TYPE = 0;
 	private static final int DATA_SIZE = 8;
 
 	protected static final String  FORMAT_START = "$MeshFormat"; 
 	protected static final String  FORMAT_END = "$EndMeshFormat"; 
 	protected static final String  NODES_START = "$Nodes"; 
 	protected static final String  NODES_END = "$EndNodes"; 
 	protected static final String  ELEMENTS_START = "$Elements"; 
 	protected static final String  ELEMENTS_END = "$EndElements";
 
 	private Map<Point, Integer> points = new LinkedHashMap<Point, Integer>();
 	private Map<PoliLine, Integer> elements = new LinkedHashMap<PoliLine, Integer>();
 	
 	private String format = FORMAT;
 	private String type = String.valueOf(TYPE);
 	private String size = String.valueOf(DATA_SIZE);
 	
 	public String getFormat() {
 		return format;
 	}
 
 	public void setFormat(String format) {
 		this.format = format;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getSize() {
 		return size;
 	}
 
 	public void setSize(String size) {
 		this.size = size;
 	}
 
 	public void addElement(PoliLine pl) {
 		elements.put(pl, elements.size() + 1);
 		for (Point p : pl) {
 			if (! points.containsKey(p)) {
 				points.put(p, points.size() + 1);
 			}
 		}
 	}
 	
 	public Iterable<PoliLine> elements() {
 		return elements.keySet();
 	}
 
 	public void write(PrintStream out) {
 		writeFormat(out);
 		writeNodes(out);
 		writeElements(out);
 	}
 	
 	public void read(BufferedReader in) throws GmshFileParseException {
 		try {
 			boolean format = readFormat(in);
 			
 			if (! format) {
 				throw new GmshFileParseException("Unable to read format line!");
 			}
 			
 			Point[] nodes = readNodes(in);
 			for (int i = 0; i < nodes.length; i++) {
 				points.put(nodes[i], i);
 			}
 			
 			PoliLine[] elements = readElements(in, nodes);
 			for (int i = 0; i < elements.length; i++) {
 				this.elements.put(elements[i], i);
 			}
 			
 		} catch (IOException e) {
 			throw new GmshFileParseException("IO Problems reading Gmsh file!", e);
 		} catch (NumberFormatException e) {
 			throw new GmshFileParseException("Problems parsing Gmsh file", e);
 		}
 	}
 
 	private void writeFormat(PrintStream out) {
 		out.println(FORMAT_START);
		out.println(format("%s %s %s", format, type, size));
 		out.println(FORMAT_END);
 	} 
 	
 	/**
 	 * <pre>
 	 $Nodes
      NUMBER-OF-NODES
      NODE-NUMBER X-COORD Y-COORD Z-COORD
      ...
      $EndNodes
 	 * </pre>
 	 * @param out
 	 */
 	private void writeNodes(PrintStream out) {
 		out.println(NODES_START);
 		out.println(points.size());
 		System.out.println(points.size());
 		for (Entry<Point, Integer> entry : points.entrySet()) {
 			Integer pos = entry.getValue();
 			Point point = entry.getKey();
 			
 			String line = format("%d %.4f %.4f %.4f", pos, point.getX(), point.getY(), 0.0);
 			System.out.println(line);
 			out.println(line);
 		}
 		out.println(NODES_END);
 		
 	}
 
 	/**
 	 * <pre>
      $Elements
      NUMBER-OF-ELEMENTS
      ELM-NUMBER ELM-TYPE NUMBER-OF-TAGS < TAG > ... NODE-NUMBER-LIST
      ...
      $EndElements
  * </pre>
 	 * @param out
 	 */
 	private void writeElements(PrintStream out) {
 		out.println(ELEMENTS_START);
 		out.println(elements.size());
 		for (Entry<PoliLine, Integer> entry : elements.entrySet()) {
 			PoliLine element = entry.getKey();
 			Integer pos = entry.getValue();
 			String line;
 			switch (element.size()) {
 			case 2:
 				line = format("%d 1 2 99 2 %d %d");
 				break;
 			case 3:
 				line = format("%d 2 2 99 2 %d %d %d", pos, 
 						points.get(element.get(0)), points.get(element.get(1)), points.get(element.get(2)));
 				break;
 			default:
 				line = format("# %d: unknown element: %s", pos, element);
 			}
 			out.println(line);
 			
 		}
 		out.println(ELEMENTS_END);
 		
 	}
 
 	private boolean readFormat(BufferedReader in) throws IOException, GmshFileParseException {
 		String line;
 		boolean started = false;
 		boolean finished = false;
 		while ((line = in.readLine()) != null) {
 			if (FORMAT_START.equals(line)) {
 				started = true;
 			} else if (FORMAT_END.equals(line)) {
 				finished = true;
 				break;
 			} else if (started) {
 				parseFormatLine(line);
 			}
 		}
 		
 		return finished;
 	}
 
 	private void parseFormatLine(String line) throws GmshFileParseException {
 		String[] vars = line.split(SEPARATOR);
 		
 		if (vars.length != 3) {
 			throw new GmshFileParseException(format("Format line illegal format: %s", line));
 		}
 		
 		format = vars[0];
 		type = vars[1];
 		size = vars[2];
 	}
 
 	private Point[] readNodes(BufferedReader in) throws IOException, GmshFileParseException {
 		String line;
 		boolean started = false;
 		Point[] points = null;
 		while ((line = in.readLine()) != null) {
 			if (NODES_START.equals(line)) {
 				started = true;
 			} else if (NODES_END.equals(line)) {
 				break;
 			} else if (started && points == null) {
 				points = new Point[Integer.parseInt(line)];
 			} else if (started) {
 				parseNodeLine(line, points);
 			}
 		}
 		
 		return points;
 	}
 
 	private void parseNodeLine(String line, Point[] points) throws GmshFileParseException {
 		String[] vars = line.split(SEPARATOR);
 		
 		if (vars.length != 4) {
 			throw new GmshFileParseException(format("Format line illegal format: %s", line));
 		}
 		
 		int pos = Integer.parseInt(vars[0]);
 		BigDecimal x = new BigDecimal(vars[1]);
 		BigDecimal y = new BigDecimal(vars[2]);
 		points[pos - 1] = new Point(x, y);
 		
 	}
 
 	/**
 	 * @param in
 	 * @return 
 	 * @throws IOException 
 	 * @throws  
 	 */
 	private PoliLine[] readElements(BufferedReader in, Point[] nodes) throws IOException {
 		
 		String line;
 		boolean started = false;
 		PoliLine[] elements = null;
 		while ((line = in.readLine()) != null) {
 			if (ELEMENTS_START.equals(line)) {
 				started = true;
 			} else if (ELEMENTS_END.equals(line)) {
 				break;
 			} else if (started && elements == null) {
 				elements = new PoliLine[Integer.parseInt(line)];
 			} else if (started) {
 				parseElementLine(line, nodes, elements);
 			}
 		}
 		
 		return elements;
 		
 	}
 
 	/**
 	 * ELM-NUMBER ELM-TYPE NUMBER-OF-TAGS < TAG > ... NODE-NUMBER-LIST 
 	 * @param line
 	 * @param nodes
 	 * @param elements
 	 */
 	private void parseElementLine(String line, Point[] nodes, PoliLine[] elements) {
 		String[] vars = line.split(SEPARATOR);
 		
 		int pos = Integer.parseInt(vars[0]);
 		int tagsNo = Integer.parseInt(vars[2]);
 		
 		Collection<Point> vertices = new ArrayList<Point>();
 		for (int i = 2 + tagsNo + 1; i < vars.length; i++) {
 			vertices.add(nodes[Integer.parseInt(vars[i]) - 1]);
 		}
 		
 		elements[pos - 1] = new PoliLine(vertices.toArray(new Point[vertices.size()]));
 	}
 
 }
