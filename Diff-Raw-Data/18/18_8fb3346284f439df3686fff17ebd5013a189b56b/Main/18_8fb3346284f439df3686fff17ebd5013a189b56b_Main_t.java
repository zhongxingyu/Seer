 package org.bandex;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.sax.SAXResult;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.SchemaFactory;
 
 import org.apache.fop.apps.FOUserAgent;
 import org.apache.fop.apps.Fop;
 import org.apache.fop.apps.FopFactory;
 import org.apache.fop.apps.MimeConstants;
 import org.xml.sax.XMLReader;
 
 
 /**
  * Servlet implementation class Main
  */
 @WebServlet("/Main")
 public class Main extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public Main() {
 		super();
 	}
 
 	protected static boolean isMobileBrowser(HttpServletRequest request) {
 		String userAgent = request.getHeader("user-agent");
 		if (userAgent.matches(".*Android.*") || userAgent.matches(".*iPhone.*")
 				|| userAgent.matches(".*iPad.*"))
 			return true;
 
 		return false;
 	}
 
 	protected boolean isValid(PrintWriter out) {
 		try {
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			factory.setValidating(false);
 			factory.setNamespaceAware(true);
 
 			SchemaFactory schemaFactory = SchemaFactory
 					.newInstance("http://www.w3.org/2001/XMLSchema");
 			schemaFactory.setErrorHandler(new SimpleErrorHandler(out));
 
 			// TODO: Ver como tratar isso aqui.
 			StreamSource source = new StreamSource(
					"file:///home/andre/workspace/BandexView/WebContent/restaurante.xsd");
 			factory.setSchema(schemaFactory.newSchema(new Source[] { source }));
 
 			SAXParser parser = factory.newSAXParser();
 
 			XMLReader reader = parser.getXMLReader();
 			// reader.setErrorHandler(null);
 
			reader.parse("http://www.pcasc.usp.br/restaurante.xml");
			//reader.parse("file:///home/andre/workspace/BandexView/WebContent/NVMonitorLog11012239.xml");
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace(out);
 			return false;
 		}
 	}
 
 	public void toHTML(PrintWriter out) {
 		try {
 
 			TransformerFactory tFactory = TransformerFactory.newInstance();
 			StreamSource XSLSource = new StreamSource(
					"file:///home/andre/workspace/BandexView/WebContent/restaurante.xsl");
 			Transformer transformer = tFactory.newTransformer(XSLSource);
 
 			StreamSource XMLSource = new StreamSource(
 					"http://www.pcasc.usp.br/restaurante.xml");
 			
 			StreamResult result = new StreamResult(out);
 			
 			transformer.transform(XMLSource, result);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected void toPDF () {
 		 try {
 	            System.out.println("FOP ExampleXML2PDF\n");
 	            System.out.println("Preparing...");
 
 	            // Setup directories
 	            File baseDir = new File("/home/andre");
 	            File outDir = new File(baseDir, "out");
 	            outDir.mkdirs();
 
 	            // Setup input and output files
 	            //File xmlfile = new File(baseDir, "xml/xml/projectteam.xml");
 	            URL xmlfile = new URL("http://www.pcasc.usp.br/restaurante.xml");
	            File xsltfile = new File(baseDir, "workspace/BandexView/WebContent/restaurante-fo.xsl");
 	            File pdffile = new File(outDir, "ResultXML2PDF.pdf");
 
 	            System.out.println("Input: XML (" + xmlfile + ")");
 	            System.out.println("Stylesheet: " + xsltfile);
 	            System.out.println("Output: PDF (" + pdffile + ")");
 	            System.out.println();
 	            System.out.println("Transforming...");
 
 	            // configure fopFactory as desired
 	            FopFactory fopFactory = FopFactory.newInstance();
 
 	            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
 	            // configure foUserAgent as desired
 
 	            // Setup output
 	            OutputStream out = new java.io.FileOutputStream(pdffile);
 	            out = new java.io.BufferedOutputStream(out);
 
 	            try {
 	                // Construct fop with desired output format
 	                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
 
 	                // Setup XSLT
 	                TransformerFactory factory = TransformerFactory.newInstance();
 	                Transformer transformer = factory.newTransformer(new StreamSource(xsltfile));
 
 	                // Set the value of a <param> in the stylesheet
 	                transformer.setParameter("versionParam", "2.0");
 
 	                // Setup input for XSLT transformation
 	                Source src = new StreamSource(xmlfile.openStream());
 
 	                // Resulting SAX events (the generated FO) must be piped through to FOP
 	                Result res = new SAXResult(fop.getDefaultHandler());
 
 	                // Start XSLT transformation and FOP processing
 	                transformer.transform(src, res);
 	            } finally {
 	                out.close();
 	            }
 
 	            System.out.println("Success!");
 	        } catch (Exception e) {
 	            e.printStackTrace(System.err);
 	            System.exit(-1);
 	        }
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		PrintWriter out = response.getWriter();
 		toHTML(out);
 		//toPDF();
 		out.close();
		String s = request.getParameter("all");
		System.out.println(s);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
