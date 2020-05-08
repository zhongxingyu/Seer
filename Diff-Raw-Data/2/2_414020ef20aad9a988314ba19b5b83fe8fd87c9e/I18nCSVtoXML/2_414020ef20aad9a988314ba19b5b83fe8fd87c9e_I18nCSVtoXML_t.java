 /*!
  * Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
  * wet-boew.github.com/wet-boew/License-eng.txt / wet-boew.github.com/wet-boew/Licence-fra.txt
  */
 
 package org.wet_boew.wet_boew.ant;
 
 import au.com.bytecode.opencsv.CSVReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.apache.tools.ant.BuildException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class I18nCSVtoXML extends I18nBaseTask {
 	protected File outFile = null;
 	
 	public void setOutFile (File f){
 		outFile = f;
 	}
 	
 	@Override
 	public void execute (){
 		validateAttributes();
 		
 		CSVReader iReader = null;
 		
 		try {
 			iReader = new CSVReader(new FileReader(i18nFile));
 			List<String []> i18n = iReader.readAll();
 			String[] languages = i18n.get(startAtRow);
 			String[] r;
 			
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("strings");
 			doc.appendChild(rootElement);
 			
 			for (int s = startAtRow + 1; s < i18n.size(); s++){
 				r = i18n.get(s);
 				
 				Element string = doc.createElement("string");
 				string.setAttribute("id",  r[startAtCol]);
 				rootElement.appendChild(string);
 				
 				for (int c = startAtCol + 1; c < r.length; c++){
 					String v = "";
 					
 					if (r.length <= c){
 						v = r[startAtCol + 1]; //defaults to the default language when string is missing
 					}else if (r[c].isEmpty()) {
 						v = r[startAtCol + 1]; //defaults to the default language when a string is empty 
 					} else {
 						v = r[c];
 					}
 					
 					Element value = doc.createElement("value");
					value.setAttribute("xml:lang", languages[c]);
 					value.appendChild(doc.createTextNode(v));
 					string.appendChild(value);
 				}
 			}
 
 			new File(outFile.getParent()).mkdirs();
 			
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 			StreamResult result = new StreamResult(outFile);
 			transformer.transform(source, result);
 		} catch (FileNotFoundException e) {
 			throw new BuildException (e.getMessage());
 		} catch (IOException e) {
 			throw new BuildException (e.getMessage());
 		}catch (Exception e){
 			throw new BuildException (e.getMessage());
 		} finally {
 			try{
 				if (iReader != null){
 					iReader.close();
 				}
 			} catch (IOException e){
 
 			}
 		}
 	}
 	
 	@Override
 	protected void validateAttributes() throws BuildException {
 		super.validateAttributes();
 		
 		if (outFile == null){
 			throw new BuildException ("Specify the output file");
 		}
 		
 		if (i18nFile.compareTo(outFile) == 0){
 			throw new BuildException ("The output file cannot be the same as the i18n file");
 		}
 	}
 }
