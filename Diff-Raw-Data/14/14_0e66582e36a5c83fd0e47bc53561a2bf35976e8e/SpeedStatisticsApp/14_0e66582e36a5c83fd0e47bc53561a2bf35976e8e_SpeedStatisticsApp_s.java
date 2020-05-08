 package xml.eventbroker.test;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import xml.eventbroker.shared.MultiXMLRootFilter;
 import xml.eventbroker.test.Statistics.DataPoint;
 
 public class SpeedStatisticsApp extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 	private static final Logger logger = Logger.getAnonymousLogger();
 
 	private DocumentBuilder docBuilder;
 	Transformer transformer;
 
 	List<DataPoint> events;
 	XMLInputFactory f;
 
 	@Override
 	public void init() throws ServletException {
 		super.init();
 		f = XMLInputFactory.newInstance();
 		f.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
 
 		try {
 			docBuilder = DocumentBuilderFactory.newInstance()
 					.newDocumentBuilder();
 			transformer = TransformerFactory.newInstance().newTransformer();
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		transformer.setOutputProperty("omit-xml-declaration", "yes");
 		events = new LinkedList<DataPoint>();
 	}
 
 	boolean changed = true;
 	Statistics statistics;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		if (changed)
 			synchronized (events) {
 				statistics = new Statistics(events);
 				changed = false;
 			}
 
 		String[] paths = null;
 		if (req.getPathInfo() != null)
 			paths = req.getPathInfo().split("/", 2);
 
 		if (paths != null && paths.length == 2
 				&& "statistics.svg".equals(paths[1])) {
 			req.setAttribute("statistic", statistics);
 			javax.servlet.RequestDispatcher rd = this.getServletContext()
 					.getRequestDispatcher("/time_diff_line.jsp");
 			rd.forward(req, resp);
 
 		} else if (paths != null && paths.length == 2
 				&& "out_of_order.svg".equals(paths[1])) {
 			req.setAttribute("statistic", statistics);
 			javax.servlet.RequestDispatcher rd = this.getServletContext()
 					.getRequestDispatcher("/out_of_order_line.jsp");
 			rd.forward(req, resp);
 		} else if (paths != null && paths.length == 2
 				&& "start_statistics".equals(paths[1])) {
 			String cnt = req.getParameter("event_count");
 			String throughput = req.getParameter("throughput");
 			String deliverer = req.getParameter("deliverer");
 			boolean reset = "on".equals(req.getParameter("reset_statistics"));
 
 			if (reset)
 				synchronized (events) {
 					events = new LinkedList<DataPoint>();
 				}
 
 			StringBuilder params = new StringBuilder();
			params.append(
					"http://localhost:8080/broker/SpeedTest?test.eventcount=")
 					.append(cnt);
 			params.append("&test.throughput=").append(throughput);
 			params.append("&test.type=").append(deliverer);
 
 			URL broker = new URL(params.toString());
 			HttpURLConnection con = (HttpURLConnection) broker.openConnection();
 			con.setRequestMethod("POST");
 			con.connect();
 			int response = con.getResponseCode();
 			resp.setStatus(response);
 
 		} else if (paths != null && paths.length == 2
 				&& "get_statistics".equals(paths[1])) {
 
			URL broker = new URL("http://localhost:8080/broker/SpeedTest");
 			HttpURLConnection con = (HttpURLConnection) broker.openConnection();
 			con.setRequestMethod("GET");
 			con.setDoInput(true);
 			con.connect();
 
 			// parse stats
 			Document doc;
 			try {
 				if (con.getContentLength() > 0) {
 
 					doc = docBuilder.parse(con.getInputStream());
 					Element root = doc.getDocumentElement();
 
 					// add own stats
 					Element rEvents = doc.createElement("recieved-events");
 					String recievedEvents;
 					synchronized (events) {
 						recievedEvents = Integer.toString(events.size());
 					}
 					rEvents.setTextContent(recievedEvents);
 					root.appendChild(rEvents);
 
 					// send answer =)
 					resp.setContentType("text/xml");
 					resp.setStatus(HttpServletResponse.SC_OK);
 					transformer.transform(new DOMSource(doc), new StreamResult(
 							resp.getOutputStream()));
 				}
 			} catch (SAXException e) {
 				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
 				e.printStackTrace();
 			} catch (TransformerException e) {
 				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
 				e.printStackTrace();
 			}
 
 			// } else if (paths == null || paths.length == 1) {
 			// req.setAttribute("statistic", statistics);
 			// javax.servlet.RequestDispatcher rd = this.getServletContext()
 			// .getRequestDispatcher("/statistics.jsp");
 			// rd.forward(req, resp);
 		} else
 			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
 	}
 
 	private void registerEvent(long timeDiff, int id) {
 		synchronized (events) {
 			changed = true;
 			events.add(new DataPoint(Long.valueOf(timeDiff), id));
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		readXMLEvents(req.getReader());
 	}
 
 	private void parseStream(Reader in) throws XMLStreamException {
 		XMLStreamReader r;
 
 		r = f.createXMLStreamReader(in);
 
 		long timeDiff;
 		int id;
 		int level = 0;
 
 		while (r.hasNext()) {
 			switch (r.next()) {
 
 			case XMLStreamConstants.START_ELEMENT:
 				if (level == 0) {
 					String tStr = r.getAttributeValue(null, "send-time");
 					// store diff in milliseconds
 					timeDiff = (System.nanoTime() - Long.valueOf(tStr)) / 1000000;
 
 					tStr = r.getAttributeValue(null, "id");
 					id = Integer.valueOf(tStr);
 					registerEvent(timeDiff, id);
 				}
 				level++;
 				break;
 			case XMLStreamConstants.END_ELEMENT:
 				level--;
 				break;
 			default:
 			}
 		}
 
 	}
 
 	private void readXMLEvents(Reader reader) throws IOException {
 		MultiXMLRootFilter filter = new MultiXMLRootFilter(reader, 0x100);
 		try {
 			while (filter.hasNext()) {
 				parseStream(filter);
 			}
 		} catch (SocketTimeoutException e) {
 			logger.log(Level.WARNING, "Connection timed out");
 		} catch (SocketException e) {
 			logger.log(Level.WARNING, "SockerError during parse", e);
 		} catch (IOException e) {
 			logger.log(Level.WARNING, "IOError during parse", e);
 		} catch (XMLStreamException e) {
 			logger.log(Level.WARNING, "Error during parse", e);
 		} finally {
 			filter.forceClose();
 		}
 	}
 }
