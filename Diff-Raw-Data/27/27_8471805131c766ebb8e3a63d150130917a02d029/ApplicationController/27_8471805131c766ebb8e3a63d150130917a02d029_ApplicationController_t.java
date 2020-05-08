 package us.northdeck.ui;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.JTextArea;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import com.sun.xml.xsom.XSSchemaSet;
 import us.northdeck.xsomtest.XsomTest;
 
 
 public class ApplicationController {
 	private XSSchemaSet sset;
 	private String[] nsList = new String[1];
 	protected Console c;
 	
 	ApplicationController(){
 		c = new SystemConsole();
 		
 		
 	}
 	
 	class SystemConsole implements Console {
 		@Override
 		public void println(String s) {
 			System.out.println(s);			
 		}
 
 		@Override
 		public void printStackTrace(Exception e) {
 			for (StackTraceElement ste : e.getStackTrace()) {
 				this.println(ste.toString());
 			}
 			
 		}		
 	}
 	
 	void setConsole (Console c) {
 		this.c = c;
 	}
 	
 	boolean loadSchemaSet(File f) {
 		boolean result = false;
 		try {
 			sset = XsomTest.getSchemaSet(f);
 			result = true;
 			nsList = XsomTest.getNamespaceArray(sset);
 		} catch (Exception e) {
 			c.printStackTrace(e);
 			c.println(e.toString());
 		}
 		
 		return result;
 	}
 
 	
 
 	public String[] getNsList() {
 		
 		return nsList;
 	}
 
 	class SchemaNotFoundException extends Exception{
 		private static final long serialVersionUID = -991408886252026578L;}
 	
 	protected void handleFlattenButton(SchemaFlattenerPanel schemaFlatternerPanel) throws SchemaNotFoundException, ParserConfigurationException, TransformerException, IOException {
 		if (sset == null) {
			schemaFlatternerPanel.getBtnFlatten().setOpaque(true);
 			startSchemaLoad(schemaFlatternerPanel);
			schemaFlatternerPanel.getBtnFlatten().setOpaque(false);
 		} else {
			schemaFlatternerPanel.getBtnFlatten().setOpaque(true);
 			String ns = (String) schemaFlatternerPanel.nsBox.getSelectedItem();
 			c.println(ns);
 			File inFile = new File(schemaFlatternerPanel.getFileName());
 			if (!inFile.exists()) throw new SchemaNotFoundException();
 			File outFile = new File (inFile.getAbsolutePath() + ".1");
 			c.println("Writing: "+outFile.toString());
 			XsomTest.writeFlatSchemaFile(sset, ns, outFile);
			schemaFlatternerPanel.getBtnFlatten().setOpaque(false);
			sset = null;
			schemaFlatternerPanel.nsBox.removeAllItems();
 		}
 		
 	}
 	
 	public void startSchemaLoad(SchemaFlattenerPanel schemaFlatternerPanel) {
 		String fileName = schemaFlatternerPanel.getFileName();
 		c.println("parsing: " + fileName);
 		File f = new File(fileName);
 		loadSchemaSet(f);
 		List<String> nsList1 = Arrays.asList(nsList);
 		c.println("loaded namespaces");
 		for (String s : nsList1) {
 			schemaFlatternerPanel.nsBox.addItem(s);
 		}
 		
 		//schemaFlatternerPanel.nsVector.addAll(nsList1);
 		
 		
 	}
 
 	class TextAreaConsole implements Console {
 		JTextArea consoleTextArea;
 		private final static String newline = "\n";
 		
 		TextAreaConsole (JTextArea consoleTextArea) {
 			this.consoleTextArea = consoleTextArea;
 		}
 		@Override
 		public void println(String s) {
 			consoleTextArea.append(s + newline);
 			
 		}
 		@Override
 		public void printStackTrace(Exception e) {
 			for (StackTraceElement ste : e.getStackTrace()) {
 				this.println(ste.toString());
 			}
 			
 		}
 	}
 	
 	
 	
 	public void setConsoleTextArea(JTextArea consoleTextArea) {
 		c = new TextAreaConsole(consoleTextArea);
 		
 	}
 
 	
 	
 	
 	
 	
 	
 }
