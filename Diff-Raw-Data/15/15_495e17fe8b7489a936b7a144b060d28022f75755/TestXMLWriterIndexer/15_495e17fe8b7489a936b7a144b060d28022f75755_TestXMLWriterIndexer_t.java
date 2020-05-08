 package edu.iit.swyne.crawler.test;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.Date;
 import java.util.Properties;
 
 import edu.iit.swyne.crawler.NewsDocument;
 import edu.iit.swyne.crawler.XMLWriterIndexer;
 
 import junit.framework.TestCase;
 
 public class TestXMLWriterIndexer extends TestCase {
 	
 	private static final String COLLECTION = "IIT Tech";
 	private static final String TITLE = "An Awesome new RSS Crawler Developed at IIT";
 	private static final String ARTICLE = "Today at IIT, a undergraduate computer science major participating in IPRO327 developed a web feed crawler. Ori Rawlings implemented the web feed listener to gather news articles for the teams 'Swyne' system. Swyne, being a semantically aware news search engine. After failures to implement a proper article gathering system in previous semester of the project, Rawlings stepped up to see the task accomplished. Originally planned to be first deployed on February 18th, 2009, some minor hang-ups accumulated during development pushing the inital deployment to February 23rd, 2009.";
 	private static final Date DATE = new Date();
 	private static final String SOURCE = "http://omega.cs.iit.edu/~orawling/IPROTesting/article.html";
 	
 	private NewsDocument doc = new NewsDocument();
 	private XMLWriterIndexer indexer;
	private String outputFile = "tmp/testData.xml";
 	private String expectedXML;
 	
 	public TestXMLWriterIndexer(String name) {
 		super(name);
 		
 		expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
 		expectedXML += "<news_documents>\n";
 		expectedXML += "  <document>\n";
 		expectedXML += "    <collection>" + COLLECTION + "</collection>\n";
 		expectedXML += "    <title>" + TITLE + "</title>\n";
 		expectedXML += "    <article>" + ARTICLE + "</article>\n";
 		expectedXML += "    <date>" + DATE.toString() + "</date>\n";
 		expectedXML += "    <source>" + SOURCE + "</source>\n";
 		expectedXML += "  </document>\n";
 		expectedXML += "</news_documents>\n\n";
 		
 		doc.setCollection(COLLECTION);
 		doc.setTitle(TITLE);
 		doc.setArticle(ARTICLE);
 		doc.setPublishedDate(DATE);
 		doc.setSource(SOURCE);
 		
 		Properties props = new Properties();
 		props.setProperty("crawler.indexer.xml.outputFile", outputFile);
 		
 		indexer = new XMLWriterIndexer(props);
 	}
 
 	public void testWritesToXML() throws Exception {
 		indexer.sendDocument(doc);
 		
 		String line, fileContents = "";
 		BufferedReader in = new BufferedReader(new FileReader("testData/newsDocs.xml"));
 		while((line = in.readLine()) != null) {
 			fileContents += line+"\n";
 		}
 		in.close();
 		
 		assertEquals(expectedXML , fileContents);
 	}
 }
