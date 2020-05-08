 package Entities;
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.InputStream;
 
 import java.io.FileInputStream;
 
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.xml.parsers.ParserConfigurationException;
 
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 /**
  * document for editing and saving
  * @author Roseline Okpara
  */
 public class TheDocument {
 	
 	private boolean isWrapped;
 	private boolean isIndented;
 	private boolean isSaved;
 	private boolean isWellFormed = true;
 	private File file;
 	private String filepath;
 	private Document domDoc;
 	private DefaultMutableTreeNode tree;
 	private String xml;
     private DocumentReader reader;
     private DocMomento momento;
     private DocCaretaker care;
 	
 	/**
 	 * creates the document for the given string
 	 * @param htmlFile
 	 */
 	public TheDocument(String htmlFile) {
 	    
 		    file = new File(htmlFile);
     		isWrapped = false;
     		isIndented = true;
     		isSaved = true;
     		setFilepath(htmlFile);
     		reader = new DocumentReader();
     		care = new DocCaretaker();
     		
     		
     		if(!file.exists()){
                 try {
                     domDoc = reader.newDocument(file);
                 } catch (IOException e) {
                     System.out.println("Cannot create new file.");
                 } catch (ParserConfigurationException e) {
                     // TODO Auto-generated catch block
                     isWellFormed = false;
                     e.printStackTrace();
                 }
             }
     		else{
     		//Testing stream
         		try{
             		domDoc = reader.buildDocument(file);
             		tree = reader.buildTree(domDoc);
         		}catch(Exception e){
         		    this.isWellFormed = false;
         		}
     		}
     		
     		FileInputStream stream = null;
     		MappedByteBuffer bb = null;
     		try {
     		     stream = new FileInputStream(file);
     		  
     		    FileChannel fc = stream.getChannel();
     		    bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
    		    if(this.isWellFormed()){
    		        xml = reader.parseAndPretty(Charset.defaultCharset().decode(bb).toString(),"2");
    		    }
    		    else{
    		        xml = Charset.defaultCharset().decode(bb).toString();
    		    }
     		  } catch (IOException e) {
                 e.printStackTrace();
     		  } 
     		  catch (Exception e){
                 isWellFormed = false;
     		  }
     		  finally {
     		    try {
                     stream.close();
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
     		  }		
     		  
     		  this.createMomento();
     		  care.storeState(momento);
 	}
 	
 
 	public DefaultMutableTreeNode buildTree(String html) throws ParserConfigurationException, SAXException, IOException{
 	    InputStream stream = new ByteArrayInputStream(html.getBytes("UTF-8"));
 	    HtmlTree tree = new HtmlTree(reader.buildDocument(stream));
 	    return tree.createTree();
 	}
 	
 	public void setWellFormed(boolean bool) {
 	    this.isWellFormed = bool;
 	}
 	
 	public DocumentReader getReader(){
 	    return reader;
 	}
 
 	public void insert(String tagName){
 	    
 	}
 	
 
 
 	public void cut() {
 		
 		
 	}
 
 
 	
 	public void paste() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * saves the given text to the file
 	 * @param writeText
 	 */
 	public void save(String writeText) {
 		try {
 			FileWriter fileWrite = new FileWriter(file);
 			BufferedWriter out = new BufferedWriter(fileWrite);
 			out.write(writeText);
 			out.close();
 		} catch (IOException e) {
 			System.out.println("File is in use somewhere else");		}
 	}
 
 	/**
 	 * saves the text
 	 * @param writeText
 	 */
 	public void saveAs(String writeText) {
 		save(writeText);
 	}
 
 	
 	public void close() {
 		
 	}
 	
 	public void indent() {
 		if (isIndented) {
 			//To Add
 		}
 		else {
 			return;
 		}
 	}
 	
 	/**
 	 * changes the word wrap for the document
 	 */
 	public void toggleWrap() {
 		if (isWrapped == true){
 			isWrapped = false;
 		}else{
 			isWrapped = true;
 		}
 	}
 	
 	/**
 	 * changes the indent for the document
 	 */
 	public void toggleIndent() {
 		if (isIndented == true){
 			isIndented = false;
 		}else{
 			isIndented = true;
 		}
 	}
 	
 	public boolean getWrap() {
 		return this.isWrapped;
 	}
 	
 	public boolean getIndent() {
 		return this.isIndented;
 	}
 	
 	public boolean getSaved() {
 		return this.isSaved;
 	}
 	public String getName() {
 		return this.file.getName();	
 	}
 	
 	public DefaultMutableTreeNode getNode(){
 	   return this.tree;
 	}
 	
 	public String getXml(){
 	    return this.xml;
 	}
 	
 	public void setXml(String xml){
 	    this.xml = xml;
 	}
 	/**
 	 * 
 	 * @return boolean of well formed
 	 */
 	public boolean isWellFormed(){
 	    return this.isWellFormed;
 	}
 
     public void setFilepath(String filepath) {
         this.filepath = filepath;
     }
 
     public String getFilepath() {
         return filepath;
     }
     
     public DocCaretaker getCareTaker(){
         return care;
     }
     
     public DocMomento createMomento(){
         momento = new DocMomento();
         momento.setState(this.xml);
         return momento;
     }
     
     public void setMomento(){
         momento.setState(xml);
     }
     
     public DocMomento getMomento(){
         return momento;
     }
 }
