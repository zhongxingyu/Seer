 package steppers;
 
 import java.io.File;
 

 import java.io.FileWriter;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
 import org.apache.batik.util.XMLResourceDescriptor;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 public class step {
 
 	private static ArrayList<GElement> list = new ArrayList<GElement>();
 
 	private static Properties properties = new Properties();
 	private static java.util.Properties drawingProperties;
 	public static step instance = new step();
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		String fileName = "properties.conf";
 
 		// читаем свойства из conf файла
 		drawingProperties = instance.readProperties(fileName);
 
 		// берем файл для рисования
 		String svgFileName = getFilename();
 
 		// разбираем файл на элементы (линия, прямоугольник и т.д.)
 		ArrayList<GElement> elements = parseSVGwrapper(svgFileName);
 		//elements = list;
 		// инициализация, выставление в начальную точку
 		Point initialPoint = initialize(properties.initialXTicks,
 				properties.initialYTicks);
 		listTrace(list);
 		// добавляем переходы между элементами
 		ArrayList<GElement> moreElements = addMoveTo(elements, initialPoint);
 
 		// listTrace(moreElements);
 
 		// делим элементы на небольшие линейные сегменты
 		ArrayList<Segment> segments = split(moreElements,
 				properties.maxSegmentLength);
 
 		// добавляем обходные пути для более плавного движения
 		ArrayList<Segment> allLines = addPathToLines(segments);
 
 		// получение состояний длин ремней для рисования
 		ArrayList<State> states = makeStates(initialPoint, allLines);
 
 		String outputFileName = "G-code ";
 		// запись G-кода в файл
 		outputFileName = instance.makeNGCfile(states, outputFileName);
 
 		// создание графического файла
 		makeSVGfile(outputFileName);
 
 		p("-----finish");
 	}
 
 	private static void makeSVGfile(String fileName) {
 		if (fileName.isEmpty()) {
 			return;
 		}
 		ArrayList<String> fileContent;
 		try {
 			fileContent = FileUtil.getFileContent(fileName);
 		} catch (IOException e) {
 
 			e.printStackTrace();
 			return;
 		}
 
 		instance.writeFile(fileContent, fileName);
 	}
 
 	private void writeFile(ArrayList<String> fileContent, String outputFileName) {
 
 		File f = new File(outputFileName + ".svg");
 		try {
 			f.createNewFile();
 			FileWriter fw = new FileWriter(f);
 
 			fw.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
 			fw.append('\n');
 
 			fw.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
 			fw.append('\n');
 
 			fw.append("<svg version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" ");
 			fw.append('\n');
 			fw.append("\t ");
 
 			fw.append("width=\"" + Double.toString(properties.canvasSizeX)
 					+ "px\" ");
 			fw.append("height=\"" + Double.toString(properties.canvasSizeY)
 					+ "px\" ");
 
 			fw.append("viewBox=\"0 0 "
 					+ Double.toString(properties.canvasSizeX) + " "
 					+ Double.toString(properties.canvasSizeY) + "\""
 					+ " enable-background=\"new 0 0 "
 					+ Double.toString(properties.canvasSizeX) + " "
 					+ Double.toString(properties.canvasSizeY) + "\" "
 					+ "xml:space=\"preserve\">");
 			fw.append('\n');
 
 			fw.append("<marker id = \"pointMarker\" viewBox = \"0 0 12 12\" refX = \"12\" refY = \"6\" markerWidth = \"3\" markerHeight = \"3\" stroke = \"green\" stroke-width = \"2\" fill = \"none\" orient = \"auto\"> "
 					+ "\n"
 					+ "<circle cx = \"6\" cy = \"6\" r = \"5\"/>"
 					+ "\n"
 					+ "</marker>");
 			fw.append('\n');
 
 			double x = (properties.canvasSizeX * properties.canvasSizeX
 					- properties.initialYTicks * properties.dl
 					* properties.initialYTicks * properties.dl + properties.initialXTicks
 					* properties.dl * properties.initialXTicks * properties.dl)
 					/ (2 * properties.canvasSizeX);
 			double y = Math.sqrt(properties.initialXTicks * properties.dl
 					* properties.initialXTicks * properties.dl - x * x);
 
 			double ll = properties.initialXTicks * properties.dl;
 			double lr = properties.initialYTicks * properties.dl;
 
 			for (int i = 0; i < fileContent.size(); i++) {
 				if (fileContent.get(i).contains("G00")) {
 					fw.append("<line fill=\"none\" stroke=\"#FF0000\" stroke-miterlimit=\"5\" x1=\""
 							+ x + "\" y1=\"" + y + "\" ");
 
 					ll = Double.parseDouble(fileContent.get(i).substring(
 							fileContent.get(i).indexOf('X') + 1,
 							fileContent.get(i).indexOf('Y') - 1));
 					lr = Double.parseDouble(fileContent.get(i).substring(
 							fileContent.get(i).indexOf('Y') + 1,
 							fileContent.get(i).indexOf('Z') - 1));
 
 					x = (properties.canvasSizeX * properties.canvasSizeX - lr
 							* lr + ll * ll)
 							/ (2 * properties.canvasSizeX);
 					y = Math.sqrt(ll * ll - x * x);
 
 					fw.append("x2=\"" + x + "\" y2=\"" + y + "\" />");
 					fw.append('\n');
 
 				} else if (fileContent.get(i).contains("G01")) {
 					fw.append("<line fill=\"none\" stroke=\"#000000\" marker-start = \"url(#pointMarker)\" x1=\""
 							+ x + "\" y1=\"" + y + "\" ");
 
 					ll = Double.parseDouble(fileContent.get(i).substring(
 							fileContent.get(i).indexOf('X') + 1,
 							fileContent.get(i).indexOf('Y') - 1));
 					lr = Double.parseDouble(fileContent.get(i).substring(
 							fileContent.get(i).indexOf('Y') + 1,
 							fileContent.get(i).indexOf('Z') - 1));
 
 					x = (properties.canvasSizeX * properties.canvasSizeX - lr
 							* lr + ll * ll)
 							/ (2 * properties.canvasSizeX);
 					y = Math.sqrt(ll * ll - x * x);
 
 					fw.append("x2=\"" + x + "\" y2=\"" + y + "\" />");
 					fw.append('\n');
 
 				}
 
 			}
 
 			fw.append("</svg>");
 			fw.append('\n');
 			fw.flush();
 			fw.close();
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}
 
 	}
 
 	private static String getFilename() {
 
 		// TODO Auto-generated method stub
 		//return "file:/Users/Mikhail/Documents/workspace/steppers/bin/Trifold_Brochure.svg";
 		return "file:/Users/Mikhail/Documents/workspace/steppers/bin/Domik.svg";
 	}
 
 	private String makeNGCfile(ArrayList<State> states, String outputFileName) {
 		try {
 			File f = new File(outputFileName
 					+ new Date(System.currentTimeMillis()).toString() + ".ngc");
 			f.createNewFile();
 			FileWriter fw = new FileWriter(f);
 
 			fw.append("%");
 			fw.append('\n');
 			// fw.append("G90 G40 G17 G21");
 			// fw.append('\n');
 			fw.append("G51Y-1");
 			fw.append('\n');
 
 			for (int i = 0; i < states.size(); i++) {
 
 				if (!states.get(i).isMoveTo) { // прорисовка элементов
 					fw.append("G01 X");
 					fw.append(Double.toString(states.get(i).ll));
 					fw.append(" Y");
 					fw.append(Double.toString(states.get(i).lr));
 					fw.append(" Z100");
 					fw.append(" F");
 					fw.append(Double.toString(properties.linearVelocity
 							* states.get(i).rate));
 					fw.append(" " + states.get(i).comment);
 					fw.append('\n');
 				} else { // быстрое перемещение между элементами
 					fw.append("G00 X");
 					fw.append(Double.toString(states.get(i).ll));
 					fw.append(" Y");
 					fw.append(Double.toString(states.get(i).lr));
 					fw.append(" Z0");
 					fw.append('\n');
 				}
 			}
 			fw.append("M30");
 			fw.append('\n');
 			fw.append("%");
 			fw.append('\n');
 
 			drawingProperties.store(fw, "drawing propreties");
 
 			fw.flush();
 			fw.close();
 			outputFileName = f.getName();
 		} catch (Exception ex) {
 			p(ex.toString() + " makeDrawFile");
 		}
 		return outputFileName;
 
 	}
 
 	/**
 	 * makeStates
 	 * 
 	 * @param initialPoint
 	 *            - начальная точка
 	 * @param allLines
 	 *            - список сегментов
 	 * @return список состояний
 	 */
 	private static ArrayList<State> makeStates(Point initialPoint,
 			ArrayList<Segment> allLines) {
 		ArrayList<State> _states = new ArrayList<State>();
 		Iterator<Segment> iterator;
 		iterator = allLines.iterator();
 
 		Segment s;
 
 		while (iterator.hasNext()) {
 			s = iterator.next();
 			State st = new State(s, properties);
 
 			if (s.isMoveToSegment)
 				st.isMoveTo = true;
 			st.comment = s.comment;
 			_states.add(st);
 
 		}
 
 		return _states;
 	}
 
 	private static Point initialize(double initialXTicks, double initialYTicks) {
 		// TODO Auto-generated method stub
 		return new Point(500, 500);
 	}
 
 	private static ArrayList<Segment> addPathToLines(
 			ArrayList<Segment> moreLines) {
 
 		for (int i = 0; i < moreLines.size() - 1; i++) {
 			// moreLines.get(i).
 		}
 
 		return moreLines;
 	}
 
 	private static ArrayList<GElement> addMoveTo(ArrayList<GElement> elements,
 			Point initialPoint) {
 		for (int i = 0; i < elements.size() - 1; i++) {
 			switch (elements.get(i).type) {
 			case bezier:
 				break;
 			case ellipse:
 				break;
 			case line:
 				switch (elements.get(i + 1).type) {
 				case bezier:
 					break;
 				case ellipse:
 					break;
 				case line:
 					elements.add(i + 1, new GElement(EType.moveTo, elements
 							.get(i).getP3(), elements.get(i).getP4(), elements
 							.get(i + 1).getP1(), elements.get(i + 1).getP2()));
 					break;
 				case moveTo:
 					break;
 				case paintTo:
 					break;
 				case path:
 					break;
 				case rectangle:
 					elements.add(i + 1, new GElement(EType.moveTo, elements
 							.get(i).getP3(), elements.get(i).getP4(), elements
 							.get(i + 1).getP1(), elements.get(i + 1).getP2()));
 					break;
 				default:
 					break;
 
 				}
 				break;
 			case moveTo:
 				break;
 			case paintTo:
 				break;
 			case path:
 				break;
 			case rectangle:
 				switch (elements.get(i + 1).type) {
 				case bezier:
 					break;
 				case ellipse:
 					break;
 				case line:
 					elements.add(i + 1, new GElement(EType.moveTo, elements
 							.get(i).getP1(), elements.get(i).getP2(), elements
 							.get(i + 1).getP1(), elements.get(i + 1).getP2()));
 					break;
 				case moveTo:
 					break;
 				case paintTo:
 					break;
 				case path:
 					break;
 				case rectangle:
 					elements.add(i + 1, new GElement(EType.moveTo, elements
 							.get(i).getP1(), elements.get(i).getP2(), elements
 							.get(i + 1).getP1(), elements.get(i + 1).getP2()));
 					break;
 				default:
 					break;
 
 				}
 				break;
 			default:
 				break;
 
 			}
 		}
 
 		elements.add(0, new GElement(EType.moveTo, initialPoint.x,
 				initialPoint.y, elements.get(0).getP1(), elements.get(0)
 						.getP2())); // добавление перехода в начало рисования
 
 		for (int i = 0; i < elements.size(); i++) { // удаление нулевых
 													// переходов (начало и конец
 													// совпадают)
 			if ((elements.get(i).type == EType.moveTo)
 					&& (elements.get(i).getP1() == elements.get(i).getP3())
 					&& (elements.get(i).getP2() == elements.get(i).getP4())) {
 				elements.remove(i);
 			}
 		}
 		return elements;
 	}
 
 	private static ArrayList<Segment> split(ArrayList<GElement> elements,
 			double _maxSegmentLength) {
 		ArrayList<Segment> s = new ArrayList<Segment>();
 
 		GElement e;
 		Iterator<GElement> eIt = elements.iterator();
 
 		while (eIt.hasNext()) {
 			e = eIt.next();
 
 			switch (e.type) {
 			case line:
 				s.addAll(splitLine(e, _maxSegmentLength));
 				break;
 			case bezier:
 				break;
 			case ellipse:
 				break;
 			case moveTo:
 				Segment moveToSegment = new Segment(e.getP1(), e.getP2(),
 						e.getP3(), e.getP4());
 				moveToSegment.isMoveToSegment = true;
 				s.add(moveToSegment);
 				break;
 			case paintTo:
 				break;
 			case path:
 				break;
 			case rectangle:
 				s.addAll(splitLine(
 						new GElement(EType.line, e.getP(1), e.getP(2), e
 								.getP(1) + e.getP(3), e.getP(2)),
 						_maxSegmentLength));
 				s.addAll(splitLine(
 						new GElement(EType.line, e.getP(1) + e.getP(3), e
 								.getP(2), e.getP(1) + e.getP(3), e.getP(2)
 								+ e.getP(4)), _maxSegmentLength));
 				s.addAll(splitLine(
 						new GElement(EType.line, e.getP(1) + e.getP(3), e
 								.getP(2) + e.getP(4), e.getP(1), e.getP(2)
 								+ e.getP(4)), _maxSegmentLength));
 				s.addAll(splitLine(
 						new GElement(EType.line, e.getP(1), e.getP(2)
 								+ e.getP(4), e.getP(1), e.getP(2)),
 						_maxSegmentLength));
 				break;
 			default:
 				break;
 			}
 
 		}
 
 		// TODO Auto-generated method stub
 		return s;
 	}
 
 	private static ArrayList<Segment> splitLine(GElement e,
 			double _maxSegmentLength) {
 
 		ArrayList<Segment> s = new ArrayList<Segment>();
 
 		double dx = e.getP(3) - e.getP(1);
 		double dy = e.getP(4) - e.getP(2);
 		double l = Math.sqrt(dx * dx + dy * dy);
 		int n = (int) Math.round(l / _maxSegmentLength) + 1;
 
 		double x1 = e.getP(1);
 		double x2;
 		double y1 = e.getP(2);
 		double y2;
 		for (int i = 1; i <= n; i++) {
 			x2 = x1 + dx / n;
 			y2 = y1 + dy / n;
 			Segment segment = new Segment(x1, y1, x2, y2);
 			segment.comment = new String("(Line " + e.getP1() + " " + e.getP2()
 					+ " " + e.getP3() + " " + e.getP4() + " " + "Segment #" + i
 					+ ")");
 			segment.segmentLength = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1)
 					* (y2 - y1));
 			s.add(segment);
 			x1 = x2;
 			y1 = y2;
 		}
 		return s;
 
 	}
 
 	private java.util.Properties readProperties(String fileName) {
 
 		java.util.Properties prop = new java.util.Properties();
 
 		try {
 			prop.load(getClass().getResourceAsStream(fileName));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		properties.initialXTicks = Double.parseDouble(prop
 				.getProperty("initialXTicks"));// 1000
 		properties.initialYTicks = Double.parseDouble(prop
 				.getProperty("initialYTicks"));// 1000;
 		properties.a = Double.parseDouble(prop.getProperty("a"));// 10;
 		properties.canvasSizeX = Double.parseDouble(prop
 				.getProperty("canvasSizeX"));// 846;
 		properties.canvasSizeY = Double.parseDouble(prop
 				.getProperty("canvasSizeY"));// 1200;
 		properties.linearVelocity = Double.parseDouble(prop
 				.getProperty("linearVelocity"));// 15000;
 		properties.maxV = Double.parseDouble(prop.getProperty("maxV"));// 250;
 		properties.radius = Double.parseDouble(prop.getProperty("radius"));// 15.9;
 		properties.stepsPerRound = Double.parseDouble(prop
 				.getProperty("stepsPerRound"));// 200;
 		properties.tickSize = Double.parseDouble(prop.getProperty("tickSize"));// 0.000250;
 		properties.maxSegmentLength = Double.parseDouble(prop
 				.getProperty("maxSegmentLength"));// 10;
 		properties.calculate();
 
 		return prop;
 	}
 
 	public static void listTrace(ArrayList<GElement> _list) {
 		Iterator<GElement> iterator = _list.iterator();
 		while (iterator.hasNext()) {
 			GElement ge = (GElement) iterator.next();
 			ge.trace();
 		}
 		return;
 	}
 
 	private static ArrayList<GElement> parseSVGwrapper(String svgFileName) {
 		
 		try {
 			p(svgFileName);
 			String parser = XMLResourceDescriptor.getXMLParserClassName();
 			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
 			String uri = svgFileName;
 			Document doc = f.createDocument(uri);
 
 			Element svg = doc.getDocumentElement();
 			// Remove the xml-stylesheet PI.
 			for (Node n = svg.getPreviousSibling(); n != null; n = n
 					.getPreviousSibling()) {
 				if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
 					doc.removeChild(n);
 					break;
 				}
 			}
 			p(svg.getChildNodes().getLength());
 			// Поиск элементов дерева
 			for (int i = 0; i < svg.getChildNodes().getLength(); i++) {
 				p(svg.getChildNodes().item(i).getChildNodes().getLength());
 
 			}
 
 			parseSVGbody(svg);
 
 		} catch (Exception ex) {
 			p(ex.toString() + " Parse SVG");
 		}
 		return list;
 	}
 
 	private static void parseSVGbody(Node n) {
 		parseSVGNodeAnalysis(n);
 		for (int i = 0; i < n.getChildNodes().getLength(); i++) {
 			Node k = n.getChildNodes().item(i);
 			parseSVGbody(k);
 		}
 	}
 
 	private static void parseSVGNodeAnalysis(Node k) {
 		if (k.getLocalName() != null) {
 			p(k.getLocalName());
 
 			// ///// BODY
 			if (k.getLocalName().equals("rect")) {
 				Element e = (Element) k;
 
 				GElement el = new GElement(EType.rectangle,
 						e.getAttribute("x"), e.getAttribute("y"),
 						e.getAttribute("width"), e.getAttribute("height"));
 
 				list.add(el);
 			}
 
 			else if (k.getLocalName().equals("line")) {
 				Element e = (Element) k;
 
 				GElement el = new GElement(EType.line, e.getAttribute("x1"),
 						e.getAttribute("y1"), e.getAttribute("x2"),
 						e.getAttribute("y2"));
 
 				list.add(el);
 			} else if (k.getLocalName().equals("ellipse")) {
 				Element e = (Element) k;
 
 				GElement el = new GElement(EType.ellipse, e.getAttribute("cx"),
 						e.getAttribute("cy"), e.getAttribute("rx"),
 						e.getAttribute("ry"));
 
 				list.add(el);
 			}
 
 			// ///// BODY
 		}
 	}
 
 	public static void p(String str) {
 		System.out.println(str);
 	}
 
 	public static void p(int str) {
 		System.out.println(Integer.toString(str));
 	}
 
 }
