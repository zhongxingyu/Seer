 package de.uni.leipzig.asv.zitationsgraph.preprocessing;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.apache.pdfbox.exceptions.CryptographyException;
 import org.apache.pdfbox.exceptions.InvalidPasswordException;
 import org.apache.pdfbox.pdfparser.PDFParser;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.util.PDFTextStripper;
 
 /**Central class of the <code>preprocessing</code> package.
  * Provides method to read supported file formats and split the scientific papers into
  * the parts HEAD, BODY and REFERENCES.
  * To read or process a file initialize an instance of this class with the path to the 
  * file and call the process method.
  * As of yet we only support the reading and splitting of single scientific papers.
  * For future releases we currently develop
  * <ul>	<li>a support for the DHQ XML format (version 0.3)
  * 		<li>a support for the entire proceedings of the Digital Humanities Conference (version 0.4),
  * 			for which an initial splitting into single papers is necessary.
  * </ul>
  * @version 0.2
  * @author Klaus Lyko
  *
  */
 public class BaseDoc {
 	public static final String HEAD = "head";
 	public static final String BODY = "body";
 	public static final String REFERENCES = "references";
 	public static final boolean debug = false;
 	Logger logger = Logger.getLogger("ZitGraph");
 	
 	private String fileName;
 	private String fullText;
 	private String head, body, references;
 	
 	public BaseDoc(String fileName) {
 		super();
 		setFileName(fileName);
 	}
 	
 	/**
 	 * Method to process the separation of a file.
 	 * @throws IOException 
 	 */
 	public void process() throws IOException {
 		String split[] = fileName.split("\\.");
 	
 		if(split[split.length-1].equalsIgnoreCase("pdf")) {
 			logger.info("Reading text from PDF file...");
 			process_pdf();
 		}
 		else if (split[split.length-1].equalsIgnoreCase("xml")) {
 			logger.warning("We support Parsing XML files of the DHQ. Use the DHQXMLParser.");
 			//process_plainTextFile();
 		} 
 		else {
 			// try to read plain text
 			logger.info("No PDF or XML file. Trying to read plain text from file...");
 			process_plainTextFile();
 		}
 		
 		if(fullText != null && fullText.length() > 0) {
 			splitFullText();
 		} else {
 			logger.warning("Not able to split the document.");
 		}
 	}
 // ------ ------ ------   PDF preprocessing ------ ------ ------	
 	/**
 	 * Reading and parsing a PDF file.
 	 * @throws IOException
 	 */
 	public PDDocument process_pdf() throws IOException {
 		PDDocument document = null;
         FileInputStream file = null;
         try
         {
             file = new FileInputStream(fileName);
             PDFParser parser = new PDFParser( file );
             parser.parse();
             document = parser.getPDDocument();
             if( document.isEncrypted() )
             {
                 try
                 {
                     document.decrypt( "" );
                 }
                 catch( InvalidPasswordException e )
                 {
                     logger.warning( "Error: Document is encrypted with a password." );
                     System.exit( 1 );
                 } catch (CryptographyException e) {
 					e.printStackTrace();
 				}
             }
             // get full text
             
             setFullText(getTextFromPDF(document));
             return document;
         }
         finally
         {
             if( file != null )
             {
                 file.close();
             }
             if( document != null )
             {
                 document.close();
             }
         }
 	}
 	
 	
     /**
      * This method reads a PDF file as plain text.
      * @param document The PDDocument to get the data from.
      * @throws IOException If there is an error getting the page count.
      */
     protected static String getTextFromPDF( PDDocument document ) throws IOException
     {
     	PDFTextStripper stripper = new PDFTextStripper();
 		return stripper.getText(document);
     }
  
  // ------ ------ ------   plain text preprocessing ------ ------ ------    
     /**
      * Method to read plain text files.
      * @throws IOException 
      */
     protected void process_plainTextFile() throws IOException {
     	BufferedReader reader = new BufferedReader(new FileReader(fileName));
     	String zeile = reader.readLine();
         StringBuffer buffer = new StringBuffer("");
     	while (zeile != null) {
    		buffer.append(zeile+System.getProperty("line.separator"));
     		zeile = reader.readLine();
     	}
     	fullText=buffer.toString();
     }
     
     /**
 	 * Method returns fields of a loaded Document.
 	 * @param name Name of the field.
 	 * @return Corresponding text.
 	 */
 	public String get(String name) {
 		if(name.equals(HEAD)) {
 			return head;
 		}
 		else if(name.equalsIgnoreCase(BODY)) {
 			return body;
 		}
 		else if(name.equalsIgnoreCase(REFERENCES)) {
 			return references;
 		}
 		return "";
 	}
 	/**
 	 * Set the path to file which to process.
 	 * @param fileName
 	 */
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 	
 	/**
 	 * Get the path to the file to process.
 	 */
 	public String getFileName() {
 		return fileName;
 	}
 
     
 	public void setFullText(String fullText) {
 		this.fullText = fullText;
 	}
 	
 	public String getFullText() {
 		return fullText;
 	}	
 	
 	/**
 	 * Method to split a paper.
 	 */
 	public void splitFullText() {
 		Divider div = new Divider(fullText);
 		int bruteForceCertainty = div.determineBruteForceMethod();
 		if(debug)
 			logger.info("BruteForceValue = "+bruteForceCertainty);
 		if(bruteForceCertainty >= 1) {
 			if(debug)
 				logger.info("Applying brute force algorithm.");
 			div.splitByBruteForce();
 			head = div.head;
 			body = div.body;
 			references = div.tail;
 		}
 		else {
 			if(debug)
 				logger.warning("No splitting performed");
 		}
 		
 	}	
 
 	public static void main(String args[]) throws IOException, CryptographyException {
 		String filePath = "examples/journal.pone.0027856.pdf";
 		filePath = "examples/Ngonga Ermilov - Complex Linking in a Nutshel.pdf";
 		filePath = "examples/text.txt";
 		filePath = "examples/Lit Linguist Computing-2010-Craig-37-52.pdf";
 		filePath = "examples/Lit Linguist Computing-2008-Windram-443-63.pdf";
 		filePath = "examples/Lit/2011/323.full.pdf";
 		//filePath = "examples/Lit/2011/Lit Linguist Computing-2011-Sainte-Marie-329-34.pdf";
 		filePath = "examples/Lit/2009/Lit Linguist Computing-2009-Fraistat-9-18.pdf";
 		// Books need to be split.
 		//	filePath = "C:/Users/Lyko/Desktop/Textmining datasets/Publikationsdaten/Digital Humanities Conference/2007/dh2007abstractsrevised.pdf";
 		
 		BaseDoc doc = new BaseDoc(filePath);
 		try {
 			doc.process();
 			System.out.println(doc.get(HEAD));
 			System.out.println("=======================");
 		//	System.out.println(doc.get(BODY));
 			System.out.println("=======================");
 			System.out.println(doc.get(REFERENCES));
 		
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void setField(String name, String value) {
 		if(name.equalsIgnoreCase(HEAD))
 			head = value;
 		else if (name.equalsIgnoreCase(BODY))
 			body = value;
 		else if (name.equalsIgnoreCase(REFERENCES))
 			references=value;
 		else 
 			if(debug)
 				logger.warning("Trying to set unknown field "+name);
 		
 	}
 	
 }
