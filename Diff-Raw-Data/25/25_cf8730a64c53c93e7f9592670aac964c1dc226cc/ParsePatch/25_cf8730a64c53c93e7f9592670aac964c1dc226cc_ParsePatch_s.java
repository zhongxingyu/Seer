 package importpatch.parser;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class ParsePatch {
 
 	private static final String XML_FILE = "patchData.xml";
 	private static String WORKSPACE_PATH = ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString();
 
 	public static void parse(String pathName, String fileName, IProject proj) throws IOException {
 		int index = pathName.indexOf(fileName);
 		File xmlFile = new File(pathName.substring(0, index) + XML_FILE);
 		if(!xmlFile.exists()){
 			xmlFile.createNewFile();
 			
 			BufferedWriter output = new BufferedWriter(new FileWriter(xmlFile));
 			output.write("<globalPatchData></globalPatchData>");
 			output.close();
 		}
 		File patchFile = new File(pathName);
 		parseToXML(patchFile, xmlFile, proj);
 	}
 
 	private static void parseToXML(File patchFile, File xmlFile, IProject proj) {
 
 		try {
 			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
             Document doc = docBuilder.parse (xmlFile);
             Element root = doc.getDocumentElement();
             
             BufferedReader input = new BufferedReader(new FileReader(patchFile));
             String line;
 			String fileName = null;
			int addLineCount = 0;
 			int lineCount = 0;
 			
 			int index = patchFile.getName().indexOf('.');
 			String patchName = patchFile.getName().substring(0, index);
 			Element patchEl = doc.createElement("patch");
 			patchEl.setAttribute("name", patchName);
 			patchEl.setAttribute("applied", "false");
 			root.appendChild(patchEl);
 			Element diffStart = null;
 			Element off = null;
 			
             while((line = input.readLine()) != null){
             	if (line.startsWith("+++")) {
             		//contains filename
 					String[] array = line.split("\\s");
 					fileName = array[1];
 					diffStart = doc.createElement("file");
 					
 					// need to get length of file
 					int length = getFileLength(fileName, proj);
 					String[] results = splitPath(fileName);
 					String packageName = results[0];
 					fileName = results[1];
 
 					diffStart.setAttribute("package", packageName);
 					diffStart.setAttribute("name", fileName);
 					diffStart.setAttribute("length", Integer.toString(length));
 					patchEl.appendChild(diffStart);
 				} else if (line.startsWith("@@")) {
 					// contains starting line and number of lines changed				
 					index = line.indexOf(',');
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
					int lengthBefore = Integer.parseInt(halfLine2.substring(index2 + 1));
					int lengthAfter = Integer.parseInt(halfLine.substring(index + 1));
 					
 					off = doc.createElement("offset");
 					off.setAttribute("start", Integer.toString(startAt));
 					off.setAttribute("length", Integer.toString(Math.abs(lengthAfter - lengthBefore)));
 					diffStart.appendChild(off);
 				} else if (line.startsWith("-") && !line.startsWith("---")) {
 					Element min = doc.createElement("remline");
 					min.setAttribute("at", Integer.toString(addLineCount++));
 					min.setAttribute("content", line.substring(1));
 					lineCount++;
 					off.appendChild(min);
 				} else if (line.startsWith("+")) {
 					Element add = doc.createElement("addline");
 					add.setAttribute("at", Integer.toString(addLineCount++ - lineCount));
 					add.setAttribute("content", line.substring(1));
 					//lineCount++;
 					off.appendChild(add);
 				} else {
 					addLineCount++;
 				}
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
 		index = file.indexOf('.');
 		file = file.substring(0, index);
 		packageName = packageName.replace(File.separatorChar, '.');
 
 		array[0] = packageName;
 		array[1] = file;
 
 		return array;
 	}
 	
 	public static void markAsPatched(File patchFile, IProject proj, boolean patched){
 		File xmlFile = new File(WORKSPACE_PATH + File.separator + proj.getName() + File.separator + XML_FILE);
 		
 		//mark as patched in xml
 		try{
 			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 	        Document doc = docBuilder.parse (xmlFile);
 	        
 	        int index = patchFile.getName().indexOf('.');
 	        String patchName = patchFile.getName().substring(0, index);
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
 
 	private static int getFileLength(String fileName, IProject proj) {
 		String path = WORKSPACE_PATH + File.separator + proj.getName() + File.separator + fileName;
 		File countFile = new File(path);
 		if(!countFile.exists()){
 			//try under src directory
 			String newpath = WORKSPACE_PATH + File.separator + proj.getName() + File.separator + "src" + File.separator + fileName;
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
