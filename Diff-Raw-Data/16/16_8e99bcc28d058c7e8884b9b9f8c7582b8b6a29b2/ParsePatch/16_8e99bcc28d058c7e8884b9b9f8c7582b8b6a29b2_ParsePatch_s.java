 package importpatch.parser;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.DateFormat;
 import java.util.Date;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class ParsePatch {
 
 	private static final String XML_FILE = "patch.cfg";
 	private static String WORKSPACE_PATH = ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString();
 
 	/**
 	 * Transforms contents of a patch into XML and stores it into patch.cfg in the root of the IProject passed in
 	 * 
 	 * @param patchFile the patch file, can be null if altContents is provided
 	 * @param proj project into which the patch configuration (patch.cfg) file will be saved
 	 * @param altContents the contents of a patch file passed in as a string, will be used if the patchFile is null
 	 * @param patchTitle name of the patching functionality if patchFile is not supplied
 	 * @throws IOException
 	 */
 	public static void parse(IFile patchFile, IProject proj, String altContents, String patchTitle, boolean applied, String pathPrefix) throws IOException {		
 		File xmlFile = new File(proj.getLocation() + File.separator + pathPrefix + File.separator + XML_FILE);
 		if(!xmlFile.exists()){
 			xmlFile.createNewFile();
 			
 			BufferedWriter output = new BufferedWriter(new FileWriter(xmlFile));
 			output.write("<patchdata></patchdata>");
 			output.close();
 			
 			//TODO create ifile for this and set it to hidden, visible for now since need to debug
 		}
 		if(patchFile != null){
 			parseToXML(new File(proj.getLocation() + File.separator + patchFile.getName()), xmlFile, proj, altContents, patchTitle, applied);
 		} else {
 			parseToXML(null, xmlFile, proj, altContents, patchTitle, applied);
 		}
 	}
 
 	private static void parseToXML(File patchFile, File xmlFile, IProject proj, String altContents, String patchTitle, boolean applied) {
 
 		try {
 			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
             Document doc = docBuilder.parse (xmlFile);
             Element root = doc.getDocumentElement();
             
             String line;
 			String fileName = null;
 			int addLineCount = 0;
 			int lineCount = 0;
 			String patchName = "";
 			BufferedReader input;
 			
 			if(patchFile != null){
 				input = new BufferedReader(new FileReader(patchFile));
 				int index = patchFile.getName().indexOf('.');
 				patchName = patchFile.getName().substring(0, index);
 			} else {
 				input =  new BufferedReader(new StringReader(altContents));
 				patchName = patchTitle;
 			}
 			Element patchEl = doc.createElement("patch");
 			patchEl.setAttribute("name", patchName);
 			patchEl.setAttribute("applied", Boolean.toString(applied));
 			patchEl.setAttribute("date", new Date().toLocaleString());
 			root.appendChild(patchEl);
 			Element diffStart = null;
 			Element off = null;
 			int lengthBefore = 0;
 			int lengthAfter = 0;
 			int lineOfPatch = 1;
 			
             while((line = input.readLine()) != null){
             	if (line.startsWith("+++")) {
             		//contains filename
 					String[] array = line.split("\\s");
 					fileName = array[1];
 					
 					//changes for git patches with /a and /b
 					if(fileName.startsWith("a/") || fileName.startsWith("b/")){
 						//TODO extra check that it was made with git just in case they have an "a" or "b" package
						fileName = fileName.replace("a/", "/");
						fileName = fileName.replace("b/", "/");
 					}
 					
 					// need to get length of file
 					int length = getFileLength(fileName, proj);
 					String[] results = splitPath(fileName);
 					String packageName = results[0];
 					if(packageName.startsWith(".")){
 						packageName = packageName.substring(1);
 					}
 					fileName = results[1];
 					
 					diffStart.setAttribute("package", packageName);
 					diffStart.setAttribute("name", fileName);
 					diffStart.setAttribute("length", Integer.toString(length));
 					patchEl.appendChild(diffStart);
 				} else if(line.startsWith("---")){
 					diffStart = doc.createElement("file");
 					
 					if(line.contains("/dev/null"))
 						diffStart.setAttribute("added", "true");
 					else
 						diffStart.setAttribute("added", "false");
 					
 					addLineCount++;
 				} else if (line.startsWith("@@")) {
 					// contains starting line and number of lines changed				
 					int index = line.indexOf(',');
 					int startAt = Integer.parseInt(line.substring(4, index));
 					index = line.lastIndexOf('+');
 					int index2 = line.lastIndexOf(',');
 					int startAtSecondFile = Integer.parseInt(line.substring(index + 1, index2));
 					addLineCount = startAtSecondFile;
 					lineCount = 0;
 					String[] array = line.split("\\s");
 					String halfLine = array[2];
 					index = halfLine.indexOf(',');
 					String halfLine2 = array[1];
 					index2 = halfLine2.indexOf(',');
 					lengthBefore = Integer.parseInt(halfLine2.substring(index2 + 1));
 					lengthAfter = Integer.parseInt(halfLine.substring(index + 1));
 					
 					off = doc.createElement("offset");
 					off.setAttribute("start", Integer.toString(startAt));
 					off.setAttribute("startApplied", Integer.toString(startAtSecondFile));
 					off.setAttribute("length", Integer.toString(Math.abs(lengthAfter - lengthBefore)));
 					off.setAttribute("patchLine", Integer.toString(lineOfPatch));
 					diffStart.appendChild(off);
 				} else if (line.startsWith("-") && !line.startsWith("---")) {
 					Element min = doc.createElement("remline");
 					min.setAttribute("at", Integer.toString(addLineCount++));
 					min.setAttribute("content", line.substring(1));
 					min.setAttribute("patchLine", Integer.toString(lineOfPatch));
 					lineCount++;
 					off.appendChild(min);
 				} else if (line.startsWith("+")) {
 					Element add = doc.createElement("addline");
 					add.setAttribute("at", Integer.toString(addLineCount++ - lineCount));
 					add.setAttribute("content", line.substring(1));
 					add.setAttribute("patchLine", Integer.toString(lineOfPatch));
 					//lineCount++;
 					off.appendChild(add);
 				} else {
 					addLineCount++;
 				}
             	lineOfPatch++;
             }
             
             //write out new xml
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer();
             transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
             
             DOMSource source = new DOMSource(doc);
             StreamResult result = new StreamResult(new FileOutputStream(xmlFile));
             transformer.transform(source, result); 
             input.close();
             
             //TODO cause visualiser to refresh by doing selectionchanged event
             
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static String[] splitPath(String fileName) {
 		String[] array = new String[2];
 		
 		//Patch format for separator
 		int index = fileName.lastIndexOf("/");
 		String packageName = fileName.substring(0, index);
 		String file = fileName.substring(index + 1);
 		//removing file extension
 		//index = file.indexOf('.');
 		//file = file.substring(0, index);
 		packageName = packageName.replace(File.separatorChar, '.');
 
 		array[0] = packageName;
 		array[1] = file;
 
 		return array;
 	}
 	
 	/**
 	 * Alters the patch.cfg file to mark a patch as applied
 	 * 
 	 * @param patchFile the patch file, can be null if patchTitle is provided
 	 * @param patchTitle name of the patching functionality, used if patchFile is not supplied
 	 * @param proj the project whose patch.cfg file should be altered
 	 * @param patched true if the patch is applied, and false if the patch is not applied
 	 */
 	public static void markAsPatched(File patchFile, String patchTitle, IProject proj, boolean patched, String pathPrefix){
 		File xmlFile = new File(proj.getLocation() + pathPrefix + File.separator + XML_FILE);
 		
 		//mark as patched in xml
 		try{
 			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 	        Document doc = docBuilder.parse (xmlFile);
 	        
 	        String patchName = patchTitle;
 	        
 	        if(patchFile != null){
 	        	int index = patchFile.getName().indexOf('.');
 	        	patchName = patchFile.getName().substring(0, index);
 	        } 
 	        
 	        NodeList els = doc.getElementsByTagName("patch");
 	        Element el = null;
 	        for (int i = 0; i < els.getLength(); i++) {
 				if(((Element)els.item(i)).getAttribute("name").equals(patchName)){
 					el = (Element)els.item(i);
 					break;
 				}
 			}
 	        el.setAttribute("applied", new Boolean(patched).toString());
 	        
 	        //write out new xml
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer();
             transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
             
             DOMSource source = new DOMSource(doc);
             StreamResult result = new StreamResult(new FileOutputStream(xmlFile));
             transformer.transform(source, result); 
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void removePatch(String patchName, IProject proj, String pathPrefix){
 		File xmlFile = new File(proj.getLocation() + File.separator + pathPrefix + File.separator + XML_FILE);
 		
 		//remove element from xml
 		try{
 			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 	        Document doc = docBuilder.parse (xmlFile);
 	        
 	        patchName = patchName.replace(".patch", "");
 	        
 	        NodeList els = doc.getElementsByTagName("patch");
 	        Element el = null;
 	        for (int i = 0; i < els.getLength(); i++) {
 				if(((Element)els.item(i)).getAttribute("name").equals(patchName)){
 					el = (Element)els.item(i);
 					break;
 				}
 			}
 	        
 	        if(el != null){
 		        doc.getDocumentElement().removeChild(el);
 		        
 		        //write out new xml
 	            TransformerFactory tFactory = TransformerFactory.newInstance();
 	            Transformer transformer = tFactory.newTransformer();
 	            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
 	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 	            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 	            
 	            DOMSource source = new DOMSource(doc);
 	            StreamResult result = new StreamResult(new FileOutputStream(xmlFile));
 	            transformer.transform(source, result); 
 	        }
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	        
 	}
 
 	private static int getFileLength(String fileName, IProject proj) {
		String path = proj.getLocation() + fileName;
 		File countFile = new File(path);
 		if(!countFile.exists()){
 			//try under src directory
 			String newpath = proj.getLocation() + "src" + File.separator + fileName;
 			countFile = new File(newpath);
 			
 			if(!countFile.exists()){
 				//try without src directory
 				newpath = path.replaceFirst("src", ".");
 				countFile = new File(newpath);
 			}
 		}
 		int count = 0;
 
 		BufferedReader read;
 		try {
 			read = new BufferedReader(new FileReader(countFile));
 
 			while ((read.readLine()) != null) {
 				count++;
 			}
 			count++;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return count;
 	}
 }
