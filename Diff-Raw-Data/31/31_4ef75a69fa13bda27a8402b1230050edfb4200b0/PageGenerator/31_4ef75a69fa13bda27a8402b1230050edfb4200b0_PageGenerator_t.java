 package ucmsite.pagegeneration;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.jdom.Content;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import ucmsite.util.AppEngineLogger;
 import ucmsite.xmltransforming.BaseTransformer;
 import ucmsite.xmltransforming.HeaderTransformer;
 import ucmsite.xmltransforming.TransformMethodReflector;
 
 public class PageGenerator {
 
 	private static PageGenerator self = null;
 	private String rootURL;
 	private URL test;
 	
 	private SAXBuilder builder;
 	private XMLOutputter output;
 	
 	private PageGenerator(HttpServletRequest req) {
 		try {
 			this.rootURL = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), "/").toString();
 			
 			builder = new SAXBuilder();
 			output = new XMLOutputter();
 			Format outputFormat = Format.getPrettyFormat();
 			outputFormat.setExpandEmptyElements(true);
 			output.setFormat(outputFormat);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static PageGenerator getPageGenerator(HttpServletRequest req) {
 		if (self == null) {
 			self = new PageGenerator(req);
 		}
 		
 		return self;
 	}
 	
 	public static PageGenerator getPageGenerator() {
 		return self;
 	}
 	
 	//this could definitely be improved, but for now should hopefully not screw up most urls.
 	public String getAbsoluteURL(String url) {
 		if (url == null)
 			return null;
 		if (url.trim().startsWith("http://") || url.trim().startsWith("www.") || url.trim().startsWith("https://")) {
 			return url;
 		}
 		if (url.startsWith("/")) {
 			url = url.substring(1);
 		}
 		return rootURL + url;
 	}
 	
 	
 	public InputStream fixCharacters(InputStream stream) throws MalformedURLException {
 		
 		BufferedReader reader  = new BufferedReader(new InputStreamReader(stream));
 		StringBuilder stringBuild = new StringBuilder();
 		
 		String line = null;
 		String xmlString;
 		try {
 			while ((line = reader.readLine()) != null) {
 				stringBuild.append(line);
 			}
 		} catch (Exception e) {
 			throw new MalformedURLException();
 		} finally {
 			try {
 				stream.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		xmlString = stringBuild.toString();
 		xmlString = xmlString.replaceAll("&amp;", "&");
 		xmlString = xmlString.replaceAll("&", "&amp;");
 		AppEngineLogger.log("Fixing input: " + xmlString);
 				
 		InputStream out = new ByteArrayInputStream(xmlString.getBytes());
 		return out;
 	}
 	
 	public String[] urlToFolderDoc(String url) {
 		String output[] = {null, null};
 		
 		if (url.startsWith("/"))
 			url = url.substring(1);
 		
 		String splits[] = url.split("/", 2);
 		if (splits.length < 2) {
 			output[0] = null;
 			output[1] = splits[0].split("\\.", 2)[0];
 		}
 		else {
 			output[0] = splits[0];
 			output[1] = splits[1].split("\\.", 2)[0];
 		}
 		
 		return output;
 	}
 	
 	public synchronized CachedPage getPage(String url, String dateString) throws Exception {
 		//extract folder/docname from urls for accessing documents from Google Docs
 		String googleURL[] = urlToFolderDoc(url);
 		AppEngineLogger.log("Running getPage: " +  googleURL[0] + " -> " + googleURL[1]);
 		
 		//get the documents that compose a page, using the arguments from the PageServlet
 		Document contentDoc = null;
 		Document headerDocTrans = null;
 		Document headerDocVisibleTrans = null;
 		Document navDocTrans = null;
 		Document footerDocTrans = null;
 		try {
 			headerDocTrans = builder.build(PageCacher.getPageCacher().getCachedStream(PageCacher.getPageCacher().getHeaderURL()));
 			AppEngineLogger.log("Cached header is: " + PageCacher.getPageCacher().getCachedHTML(PageCacher.getPageCacher().getHeaderURL()));
 			headerDocVisibleTrans = builder.build(PageCacher.getPageCacher().getCachedStream(PageCacher.getPageCacher().getHeaderVisibleURL()));
 			navDocTrans = builder.build(PageCacher.getPageCacher().getCachedStream(PageCacher.getPageCacher().getNavURL()));
 			footerDocTrans = builder.build(PageCacher.getPageCacher().getCachedStream(PageCacher.getPageCacher().getFooterURL()));
 		} catch (JDOMException e2) {
 			e2.printStackTrace();
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		}
 		
 		AppEngineLogger.log("Got decorations");
 		
 		try {
			AppEngineLogger.log("GET DOCUMENT: args: " + googleURL[0] + " and " + googleURL[1] + " is: " + GoogleAccessor.getGoogleAccessor().getDocument(googleURL[0], googleURL[1]));
 			InputStream contentStream = fixCharacters(GoogleAccessor.getGoogleAccessor().getDocument(googleURL[0], googleURL[1]));
 			contentDoc = builder.build(contentStream);
 		} catch (Exception e) {
 			return null;
 		}
 
 		AppEngineLogger.log("Got contentDoc");
 		
 		//create the transformers to assemble the page
 		BaseTransformer contentTransformer = TransformMethodReflector.getTranformMethodReflector().getTransformer(contentDoc.getRootElement());
 		
 		//create some 'unchanging' page elements
 		Element htmlPage = new Element("html");
 		Element htmlHead = new Element("head");
 		htmlPage.addContent(htmlHead);
 				
 		Element htmlBody = new Element("body");
 		htmlPage.addContent(htmlBody);
 		
 		Element backDrop = new Element("div");
 		backDrop.setAttribute("id", "backdrop");
 		htmlBody.addContent(backDrop);
 		
 		Element whiteWrapper = new Element("div");
 		whiteWrapper.setAttribute("id", "whitewrapper");
 		backDrop.addContent(whiteWrapper);
 		
 		Element backCenter = new Element("div");
 		backCenter.setAttribute("id", "backcenter");
 		whiteWrapper.addContent(backCenter);
 		
 		Element content = new Element("div");
 		content.setAttribute("id", "ucmonlinecontent");
 		
 		//run the transformers and add the dynamic content to the unchanging elements
 		for (Object item : headerDocTrans.getRootElement().cloneContent()) {
 			htmlHead.addContent((Content)item);
 		}
 		for (Object item : headerDocVisibleTrans.getRootElement().cloneContent()) {
 			backCenter.addContent((Content)item);
 		}
 		for (Object item : navDocTrans.getRootElement().cloneContent()) {
 			backCenter.addContent((Content)item);
 		}
 		
 		backCenter.addContent(content);
 		
 		for (Content item : contentTransformer.transform(contentDoc.getRootElement()))
 			content.addContent(item);
 		for (Object item : footerDocTrans.getRootElement().cloneContent())
 			backCenter.addContent((Content)item);
 		
 		
 		String newPageHTML = output.outputString(htmlPage).replaceAll("&amp;", "&");
 
 		AppEngineLogger.log("PAGE HTML: \n" +  newPageHTML);
 		
 		CachedPage newPage = new CachedPage(url, newPageHTML, dateString);
 		return newPage;
 	}
 	
 	String convertStreamToString(java.io.InputStream is) {
 	    try {
 	        return new java.util.Scanner(is).useDelimiter("\\A").next();
 	    } catch (java.util.NoSuchElementException e) {
 	        return "";
 	    }
 	}
 	
 	public CachedPage getComponent(String url, String dateString) throws Exception {
 		String componentURL[] = urlToFolderDoc(url);
 		Document componentDoc = null;
 
 		try {
 			componentDoc = builder.build(fixCharacters(GoogleAccessor.getGoogleAccessor().getDocument(componentURL[0], componentURL[1])));
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		
 		BaseTransformer componentTransformer = TransformMethodReflector.getTranformMethodReflector().getTransformer(componentDoc.getRootElement());
 
 		Element wrapper = new Element("div");
 		
 		if (url.trim().equals(PageCacher.getPageCacher().getHeaderURL().trim())) {
 			AppEngineLogger.log("Header detected");
 			for (Content item : ((HeaderTransformer)componentTransformer).transformBackGround(componentDoc.getRootElement())) {
 				AppEngineLogger.log("Adding to header");
 				wrapper.addContent(item);
 			}
 		}
 		else if (url.trim().equals(PageCacher.getPageCacher().getHeaderVisibleURL().trim())) {
 			for (Content item : ((HeaderTransformer)componentTransformer).transformVisible(componentDoc.getRootElement())) {
 				wrapper.addContent(item);
 			}
 		}
 		else {
 			for (Content item : componentTransformer.transform(componentDoc.getRootElement())) {
 				wrapper.addContent(item);
 			}
 		}
 		
 		String newPageHTML= output.outputString(wrapper).replaceAll("&amp;", "&");		
 		
 		CachedPage newPage = new CachedPage(url, newPageHTML, dateString);
 		return newPage;
 	}
 	
 }
