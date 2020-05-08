 package net.praqma.util.xml;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import net.praqma.util.debug.Logger;
 
 import org.w3c.dom.DOMError;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class XML {
     private Document doc;
     private Element root;
 
     protected Logger logger = Logger.getLogger();
     
     public XML() {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware( true );
         DocumentBuilder builder;
         try {
             builder = factory.newDocumentBuilder();
             doc = builder.newDocument();
         } catch( ParserConfigurationException e ) {
             e.printStackTrace();
         }
     }
 
     public XML( String roottag ) {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware( true );
         DocumentBuilder builder;
         try {
             builder = factory.newDocumentBuilder();
             doc = builder.newDocument();
         } catch( ParserConfigurationException e ) {
             e.printStackTrace();
         }
 
         /* Preparing the root note */
         root = (Element) doc.appendChild( doc.createElement( roottag ) );
     }
     
     public XML( File xmlfile ) throws IOException {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware( true );
         InputStream is = new FileInputStream( xmlfile );
         DocumentBuilder builder;
         try {
             builder = factory.newDocumentBuilder();
             doc = builder.parse( is );
         } catch( Exception e ) {
             e.printStackTrace();
         }
 
         root = doc.getDocumentElement();
     }
 
     public XML( File xmlfile, String roottag ) throws IOException {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware( true );
         InputStream is = new FileInputStream( xmlfile );
         DocumentBuilder builder;
         try {
             builder = factory.newDocumentBuilder();
             doc = builder.parse( is );
         } catch( Exception e ) {
         	logger.debug( "The file " + xmlfile + " does not exist" );
         	if( roottag != null ) {
 	            try {
 					builder = factory.newDocumentBuilder();
 					doc = builder.newDocument();
 					logger.debug( "The document was created" );
 				} catch (ParserConfigurationException e1) {
 					e1.printStackTrace();
 					logger.error( "Could not create document" );
 				}
         	}            
         }
 
        root = doc.getDocumentElement();
     }
 
     public XML( InputStream is ) {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware( true );
         DocumentBuilder builder;
         try {
             builder = factory.newDocumentBuilder();
             doc = builder.parse( is );
         } catch( Exception e ) {
             e.printStackTrace();
         }
 
         root = doc.getDocumentElement();
     }
 
     /* Setters */
 
     public Element addElement( String tag ) {
         return addElement( root, tag );
     }
 
     public Element addElement( Element root, String tag ) {
         return (Element) root.appendChild( doc.createElement( tag ) );
     }
 
     /* Getters */
 
     public Element getRoot() {
         return root;
     }
 
     public Element getFirstElement( String tag ) throws DOMException {
         return getFirstElement( root, tag );
     }
 
     public Element getFirstElement( Element e, String tag ) throws DOMException {
         NodeList list = e.getChildNodes();
 
         for( int i = 0; i < list.getLength(); i++ ) {
             Node node = list.item( i );
 
             if( node.getNodeType() == Node.ELEMENT_NODE ) {
                 if( node.getNodeName().equals( tag ) ) {
                     return (Element) node;
                 }
             }
         }
 
         throw new DOMException( DOMError.SEVERITY_WARNING, "Could not GetElement " + tag );
     }
 
     public List<Element> getElements() {
         return getElements( root );
     }
 
     public List<Element> getElements( Element e ) {
         NodeList list = e.getChildNodes();
 
         List<Element> result = new ArrayList<Element>();
 
         for( int i = 0; i < list.getLength(); i++ ) {
             Node node = list.item( i );
 
             if( node.getNodeType() == Node.ELEMENT_NODE ) {
                 Element e1 = (Element) node;
                 result.add( e1 );
             }
         }
 
         return result;
     }
 
     public List<Element> getElements( Element e, String tag ) {
         NodeList list = e.getChildNodes();
 
         List<Element> result = new ArrayList<Element>();
 
         for( int i = 0; i < list.getLength(); i++ ) {
             Node node = list.item( i );
 
             if( node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase( tag ) ) {
                 Element e1 = (Element) node;
                 result.add( e1 );
             }
         }
 
         return result;
     }
 
     public List<Element> getElementsWithAttribute( Element e, String attr, String name ) {
         NodeList list = e.getChildNodes();
 
         List<Element> result = new ArrayList<Element>();
 
         for( int i = 0; i < list.getLength(); i++ ) {
             Node node = list.item( i );
 
             if( node.getNodeType() == Node.ELEMENT_NODE ) {
                 Element e1 = (Element) node;
 
                 if( e1.getAttribute( attr ).equals( name ) ) {
                     result.add( e1 );
                 }
             }
         }
 
         return result;
     }
 
     public String getXML() {
         StringWriter out = new StringWriter();
 
         try {
             TransformerFactory factory = TransformerFactory.newInstance();
             // factory.setAttribute( "indent-number", new Integer( 4 ) );
 
             Transformer transformer = factory.newTransformer();
 
             transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
             transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
 
             // aTransformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION,
             // "yes" );
             transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
 
             Source src = new DOMSource( doc );
 
             Result dest = new StreamResult( out );
             transformer.transform( src, dest );
         } catch( Exception e ) {
             e.printStackTrace();
         }
 
         return out.toString();
     }
 
     public Source getXMLAsSource() {
         try {
             TransformerFactory factory = TransformerFactory.newInstance();
             // factory.setAttribute( "indent-number", new Integer( 4 ) );
 
             Transformer transformer = factory.newTransformer();
 
             transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
             transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
 
             // aTransformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION,
             // "yes" );
             transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
 
             return new DOMSource( doc );
         } catch( Exception e ) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     public String transform( File xml, String xsl, File output ) {
         StreamSource xmlSource = new StreamSource( xml );
         StreamSource xsltSource = new StreamSource( getClass().getResourceAsStream( xsl ) );
 
         return transform( xmlSource, xsltSource, output );
     }
 
     public String transform( String xsl, File output ) {
         StreamSource xsltSource = null;
 
         try {
             xsltSource = new StreamSource( getClass().getResourceAsStream( xsl ) );
             logger.log( "Using " + xsl + " as XSLT" );
         } catch( Exception e ) {
             logger.log( "Could not find the XSLT file, using SECRET XSLT" );
             /* Debugging only */
             xsltSource = new StreamSource( "C:\\projects\\PRAQMA\\VANS\\trunk\\src\\main\\resources\\junit.xsl" );
         }
 
         return transform( new DOMSource( doc ), xsltSource, output );
     }
 
     public String transform( Source xmlSource, StreamSource xsltSource, File output ) {
         TransformerFactory transFact = TransformerFactory.newInstance();
 
         try {
             Transformer transformer = transFact.newTransformer( xsltSource );
             transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
             transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
             transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
             transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
 
             /* Correct UTF-8 encoding!? */
             OutputStream os = new FileOutputStream( output );
             transformer.transform( xmlSource, new StreamResult( os ) );
 
             return os.toString();
         } catch( TransformerException e ) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch( FileNotFoundException e ) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return "";
     }
 
     public void saveState( File filename ) {
         String xml = getXML();
         try {
             FileWriter fw = new FileWriter( filename );
             BufferedWriter bw = new BufferedWriter( fw );
             bw.append( xml );
             bw.close();
             fw.close();
         } catch( IOException e ) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
