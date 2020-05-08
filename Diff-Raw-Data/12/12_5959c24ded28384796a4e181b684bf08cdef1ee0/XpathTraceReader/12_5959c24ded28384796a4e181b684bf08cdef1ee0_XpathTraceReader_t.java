 package ca.uqac.info.trace;
 
 import java.io.*;
 
 import javax.xml.parsers.*;
 import org.xml.sax.*;
 import org.w3c.dom.*;
 import org.w3c.dom.traversal.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
import com.sun.org.apache.xpath.internal.XPathAPI;
 
 public class XpathTraceReader extends TraceReader
 {
 	 /** Creates a new instance of XpathTraceReader */
     public XpathTraceReader() {
     
     	super();
     }
     public EventTrace parseEventTrace(File f)
     {
     	String [] args=null;
    	Document doc = null ;
     	if (args.length < 2) 
     	{
     	      System.out.println("Usage: ");
     	      System.out.println("java -classpath xerces.jar;.;xalan.jar "+ " XPathDemo file.xml xpath-string");
     	      }
 
     	    try {
     	    	System.out.println("Parsing XML file " + args[0] + " ...");
     	    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
     	    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
     	    	// Parse the XML file and build the Document object in RAM
    	    	 doc = docBuilder.parse(new File(args[0]));
     	    	
     	       // Normalize text representation.
     	       // Collapses adjacent text nodes into one node.
     	       doc.getDocumentElement().normalize();
     	     
     	       /****************************************************************
     	        * How to use xpath to extract info from document object in Java
     	        ****************************************************************/
     	       String xpath = args[1];
     	       System.out.println("\nQuerying DOM using xpath string:" + xpath);
 
     	      // Catches the first node that meets the criteria of xpath string
     	      String str = XPathAPI.eval(doc, xpath).toString();
     	      System.out.println("=>" + str + "<=\n");
     	      
 
     	      /****************************************************************
     	       * How to get root node of the document object
     	       ****************************************************************/
     	      Node root = doc.getDocumentElement();
     	      System.out.println("\nRoot element of the doc is =>"+ root.getNodeName() + "<=");
     	      
 
 
     	      /****************************************************************
     	       * How to print the parsed xml file right back to system out
     	       ****************************************************************/
     	      String xpathString = args[1];
     	      // Set up an identity transformer to use as serializer.
     	      // This one can write input to output stream
     	      Transformer serializer = TransformerFactory.newInstance().newTransformer();
     	      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 
     	      // Use the simple XPath API to select a nodeIterator.
     	      System.out.println("\nPrinting subtree under xpath =>" + xpathString + "<=");
     	      NodeIterator nl = XPathAPI.selectNodeIterator(doc, xpathString);
 
     	      Node n;
     	      while ((n = nl.nextNode()) != null) {
     	        // Serialize the found nodes to System.out
     	        serializer.transform(new DOMSource(n),new StreamResult(System.out));
     	      }
     	    
 
     	    }
     	    catch (SAXParseException err) {
     	      String msg ="** SAXParseException"+ ", line "+ err.getLineNumber()+ ", uri "+ err.getSystemId()+ "\n"+ "   "+ err.getMessage();
     	      System.out.println(msg);
     	      // print stack trace
     	      Exception x = err.getException();
     	      ((x == null) ? err : x).printStackTrace();
     	    }
     	    catch (SAXException e) {
     	      String msg = "SAXException";
     	      System.out.println(msg);
     	      Exception x = e.getException();
     	      ((x == null) ? e : x).printStackTrace();
     	    }
     	    catch (Exception e) {
     	      e.printStackTrace();
     	    }
     	    catch (Throwable t) {
     	      t.printStackTrace();
     	      String msg = "Some other exception while getting XML";
     	      System.out.println(msg);
     	    }
     	
    	    EventTrace even = new EventTrace() ;
    	    even.parse(doc);
    	     return even;
     }
 
   
 }
