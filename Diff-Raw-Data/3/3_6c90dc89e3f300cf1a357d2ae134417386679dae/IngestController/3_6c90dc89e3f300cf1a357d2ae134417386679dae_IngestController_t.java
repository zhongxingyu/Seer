 package com.drexelexp.ingest;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.drexelexp.subject.JdbcSubjectDAO;
 import com.drexelexp.subject.Subject;
 
 @Controller
 public class IngestController {
 	private static final Logger logger = LoggerFactory
 			.getLogger(IngestController.class);
 
 	private JdbcSubjectDAO _subjectDAO;
 
 	private JdbcSubjectDAO getSubjectDAO() {
 		if (_subjectDAO != null)
 			return _subjectDAO;
 
 		ApplicationContext context = new ClassPathXmlApplicationContext(
 				"Spring-Module.xml");
 		_subjectDAO = (JdbcSubjectDAO) context.getBean("subjectDAO");
 
 		return _subjectDAO;
 	}
 
 	@RequestMapping(value = "/ingest", method = RequestMethod.GET)
 	public String list(Model model) {
 		String rootUrl = "http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/index.html";
 
 		ArrayList<CollegeListing> colleges = new ArrayList<CollegeListing>();
 
 		try {
 			// Parse listing page into a Document
 			Document document = getDocument(new URL(rootUrl));
 
 			// Navigate to College Listings
 			Element content = document.getElementById("content");
 			NodeList divs = content.getElementsByTagName("div");
 			int divCount = divs.getLength();
 			for (int i = 0; i < divCount; i++) {
 				Element div = (Element) divs.item(i);
 				if (div.getAttribute("class").equals("colfloat")) {
 					// For each College parse the listing
 					colleges.add(new CollegeListing(div));
 				}
 			}
 		} catch (IllegalArgumentException ex) {
 			logger.error(ex.getMessage());
 		} catch (MalformedURLException ex) {
 			logger.error(ex.getMessage());
 		} finally {
 			model.addAttribute("colleges", colleges);
 		}
 
 		return "ingest/list";
 	}
 
 	@RequestMapping(value = "/ingest/courses/{collegeCode}/{subjectCode}", method = RequestMethod.GET)
 	public String courses(@PathVariable String collegeCode,
 			@PathVariable String subjectCode, Model model) {
 		String url = "http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/"
 				+ subjectCode.toLowerCase();
 		if (subjectCode.equals("UNIV"))
 			url += collegeCode.toLowerCase();
 		url += "/";
 
 		Subject subject = getSubjectDAO().getByCode(subjectCode);
 
 		ArrayList<CourseListing> courses = new ArrayList<CourseListing>();
 
 		try {
 			// Parse listing page into a Document
 			Document document = getDocument(new URL(url));
 
 			// Navigate to Course Listings
 			Element content = document
 					.getElementById("courseinventorycontainer");
 			NodeList divs = content.getElementsByTagName("div");
 			int divCount = divs.getLength();
 			for (int i = 0; i < divCount; i++) {
 				Element div = (Element) divs.item(i);
 				if (div.getAttribute("class").equals("courseblock")) {
 					// For each Course parse listing
 					courses.add(new CourseListing(div, subject));
 				}
 			}
 		} catch (IllegalArgumentException ex) {
 			logger.error(ex.getMessage());
 		} catch (MalformedURLException ex) {
 			logger.error(ex.getMessage());
 		} finally {
 			model.addAttribute("courses", courses);
 		}
		
		model.addAttribute("collegeCode",collegeCode);
		model.addAttribute("subjectCode",subjectCode);
 
 		return "ingest/courses";
 	}
 
 	@RequestMapping(value = "/ingest/professors/{collegeCode}/{subjectCode}", method = RequestMethod.GET)
 	public String professors(@PathVariable String collegeCode,
 			@PathVariable String subjectCode, Model model)
 			throws MalformedURLException, IOException, InterruptedException {
 		URL url;
 		URLConnection connection;
 
 		int term = 201215;
 		
 		// Connect to root page and receive session cookie
 		url = new URL("https://duapp3.drexel.edu/webtms_du/");
 		connection = url.openConnection();
 		connection.connect();
 		Map<String, List<String>> headers = connection.getHeaderFields();
 		String cookie = headers.get("Set-Cookie").get(0);		
 
 		// Connect to term page to
 		url = new URL("https://duapp3.drexel.edu/webtms_du/Colleges.asp?Term="+term+"&univ=DREX");
 		connection = url.openConnection();
 		connection.setRequestProperty("Cookie", cookie);
 		connection.connect();
 		connection.getContent();
 		
 		url = new URL(
 				"https://duapp3.drexel.edu/webtms_du/Courses.asp?"+
 						"SubjCode="	+ subjectCode +
 						"&CollCode=" + collegeCode +
 						"&univ=DREX");
 		connection = url.openConnection();
 		connection.setRequestProperty("Cookie", cookie);
 		connection.connect();
 		connection.getContent();
 
 		ArrayList<SectionListing> sections = new ArrayList<SectionListing>();
 
 		try {
 			BufferedReader input = new BufferedReader(
 					new InputStreamReader(connection.getInputStream()));
 			String line;
 
 			boolean consume = false;
 			String contents = "";
 			while ((line = input.readLine()) != null) {
 				if (line.contains("<TABLE")) {
 					consume = true;
 				}
 				if (consume) {
 					line = line.replaceAll("<BR>", "")
 							.replaceAll("<TD[^>]*>", "<TD>")
 							.replaceAll("<A[^>]*>", "<A>")
 							.replaceAll("<FONT[^>]*>", "")
 							.replaceAll("</FONT>", "")
 							.replaceAll("&nbsp;", "")
 							.replaceAll("&", "&amp;");
 
 					contents += line;
 				}
 				if (line.contains("</TABLE>")) {
 					break;
 				}
 			}
 			contents = "<html>" + contents + "</html>";
 			
 			Document document = getDocument(contents);
 
 			Element table = (Element) document
 					.getElementsByTagName("TABLE").item(0);
 
 			NodeList rows = table.getElementsByTagName("TR");
 			int courseNumber = 0;
 			int rowCount = rows.getLength();
 			for (int i = 1; i < rowCount; i++) {
 				Element row = (Element) rows.item(i);
 
 				NodeList cells = row.getElementsByTagName("TD");
 				if (cells.getLength() == 9) {
 					try{
 						int number = Integer.parseInt(((Element)cells.item(1)).getTextContent());
 						courseNumber=number;
 					}
 					catch(NumberFormatException ex){}
 					
 					sections.add(new SectionListing(subjectCode,courseNumber,row));
 				}
 			}
 		} catch (IllegalArgumentException ex) {
 			logger.error(ex.getMessage());
 		} finally {
 			model.addAttribute("sections", sections);
 		}
 
 		return "ingest/professors";
 	}
 
 	private String getFilteredContents(URL url) throws IOException {
 		return getFilteredContents(url.openStream());
 	}
 
 	private String getFilteredContents(InputStream stream) throws IOException {
 		BufferedReader input = new BufferedReader(new InputStreamReader(stream));
 		String contents = "";
 		String line;
 
 		int divs = 0;
 		while ((line = input.readLine()) != null) {
 			// Ensure divs are properly nested in parsed document
 			if (line.contains("<div")) {
 				divs++;
 			}
 			if (line.contains("</div")) {
 				if (divs == 0)
 					line = line.replace("</div>", "");
 				else
 					divs--;
 			}
 
 			// Remove broken tags from markup
 			line = line.replaceAll(" < ", " &lt; ")
 					.replaceAll(" & ", " &amp; ").replaceAll("<img[^>]*>", "")
 					.replaceAll("<li[^>]*>", "").replaceAll("</li>", "")
 					.replaceAll("<ul[^>]*>", "").replaceAll("</ul>", "")
 					.replaceAll("<br/>", "");
 
 			contents += line;
 		}
 
 		return contents;
 	}
 
 	private Document getDocument(String contents) {
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 
 			Reader reader = new StringReader(contents);
 			InputSource source = new InputSource(reader);
 			Document document = builder.parse(source);
 
 			return document;
 
 		} catch (ParserConfigurationException ex) {
 			logger.error(ex.getMessage());
 		} catch (IOException ex) {
 			logger.error(ex.getMessage());
 		} catch (SAXException ex) {
 			logger.error(ex.getMessage());
 		}
 
 		throw new IllegalArgumentException(
 				"Getting document from string failed.");
 	}
 
 	private Document getDocument(URL url) {
 		try {
 
 			return getDocument(getFilteredContents(url));
 
 		} catch (IOException ex) {
 			logger.error(ex.getMessage());
 		}
 
 		throw new IllegalArgumentException("Getting document at " + url
 				+ " failed.");
 	}
 }
