 package xml2labbook;
 
 import org.concord.LabBook.*;
 import org.concord.CCProbe.*;
 import org.concord.waba.extra.probware.probs.*;
 import waba.fx.*;
 import graph.*;
 
 import javax.xml.parsers.*;
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 import org.w3c.dom.*;
 
 import java.io.*;
 import java.net.*;
 import java.awt.*;
 import java.awt.image.*;
 
 
 
 public class XML2LabBook{
 public static int newIndex = 0;
 
 
 private  static int indent = 0;
 
 public final static int FOLDER_TAG 				= 0;
 public final static int NOTES_TAG 				= 1;
 public final static int DATACOLLECTOR_TAG 		= 2;
 public final static int DRAWING_TAG 			= 3;
 public final static int UNITCONV_TAG 			= 4;
 public final static int IMAGE_TAG 				= 5;
 public final static int EXPOBJECT_TAG 			= 6;
 public final static int SUPERNOTES_TAG 			= 7;
 
 public final static int UNKNOWN_PROTOCOL 		= 0;
 public final static int FILE_PROTOCOL 			= 1;
 public final static int HTTP_PROTOCOL 			= 2;
 public final static int FTP_PROTOCOL 			= 3;
 
 
 public static String []labBookObjectTAGs = {"FOLDER","NOTES","DATACOLLECTOR","DRAWING","UNITCONV","IMAGE","EXPOBJECT","SUPERNOTES"};
 public static String []labBookObjectNames = {"Folder","Notes","Data Collector","Drawing","UnitConvertor","Image",null,"SuperNotes"};
 
 
 
 
 public static Document	currentDocument;
 
 public static java.util.Hashtable	docObjects;
 
 public static boolean qtInstalled = false;
 
 public static QTManager qtManager = null;
 
 	public static void exit(int code){
 			if(qtManager != null) qtManager.closeQTSession();
 			System.exit(code);
 	}
 
 	public static void main(String []args) throws Exception {
 		try{
 			qtManager = new QTManager();
 			qtManager.openQTSession();
 			qtInstalled = qtManager.qtInstalled;
 		}catch(Throwable t){
 			qtManager = null;
 			qtInstalled = false;
 			System.out.println("QuickTime is not installed");
 		}
 
 
 
 		String xmlFile = "labbook.xml";
 		if(args != null && args.length > 0){
 			xmlFile = args[0];
 		}
 		
 	
         // Step 1: create a DocumentBuilderFactory and configure it
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         // Optional: set various configuration options
         dbf.setValidating(true);
         dbf.setIgnoringComments(true);
         dbf.setIgnoringElementContentWhitespace(true);//???
         dbf.setCoalescing(true);
         // The opposite of creating entity ref nodes is expanding them inline
 //        dbf.setExpandEntityReferences(!createEntityRefs);
 
         DocumentBuilder db = null;
         try {
             db = dbf.newDocumentBuilder();
         } catch (ParserConfigurationException pce) {
             System.err.println(pce);
 			exit(1);
         }
 
 
         // Set an ErrorHandler before parsing
         OutputStreamWriter errorWriter =
             new OutputStreamWriter(System.err, "UTF-8");
         db.setErrorHandler(
             new MyErrorHandler(new PrintWriter(errorWriter, true)));
 
         // Step 3: parse the input file
         Document doc = null;
         try {
             doc = db.parse(new File(xmlFile));
         } catch (SAXException se) {
             System.err.println(se.getMessage());
 			exit(1);
         } catch (IOException ioe) {
             System.err.println(ioe);
 			exit(1);
         }
         
         String rootName = doc.getDocumentElement().getTagName();
        if(rootName == null || !rootName.equals("LABBOOK")){
             System.err.println("LabBook description wasn't found");
 			exit(1);
         }
         
         LabBook labBook = createLabBook(doc.getDocumentElement());
    		if(labBook == null){
    			System.out.println("Error creating LabBook");
 			exit(1);
    		}
  		LObjDictionary loDict = initRootDictionary(labBook);
    		boolean pagingView = doc.getDocumentElement().getAttribute("view").equals("paging");
    		if(pagingView){
 			loDict.viewType = LObjDictionary.PAGING_VIEW;
 		} else {
 			loDict.viewType = LObjDictionary.TREE_VIEW;
    		}
 
 
 
        
 		NodeList nodeList = doc.getDocumentElement().getChildNodes();
 		currentDocument = doc;
 		docObjects = new java.util.Hashtable();
 		for(int i = 0; i < nodeList.getLength(); i++){
 			Node node = nodeList.item(i);
 			int type = node.getNodeType();
 			if(type == Node.ELEMENT_NODE) addObjectToLabBook(labBook,loDict,loDict,(Element)node);
    		}
    		
    		
 		labBook.commit();
 		labBook.close();
    		
 		exit(0);
 
 
 	}
 	
 	
 	public static LabBook createLabBook(Node labBookNode){
 		LabBook 	retBook = null;
 		if(labBookNode == null) return retBook;
 		LabBookDB 	lbDB = null;
 		labBookInit();
 		try{
 			File labBookFile = new File("LabBook.PDB");
 			if(labBookFile.exists()){
 				labBookFile.delete();
 			}
 			lbDB = new LabBookCatalog("LabBook");
 			if(lbDB.getError()){
 				retBook = null;
 			}else{
 				retBook = new LabBook();
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 			retBook = null;
 		}
 		if(retBook != null){
 			LabObject.lBook = retBook;
 			retBook.open(lbDB);
 		}
 		return retBook;
 	}
 	public static void labBookInit(){
 		LabBook.init();
 		LabBook.registerFactory(new DataObjFactory());
 	}
 	
 	public static LabObject addObjectToLabBook(LabBook labBook,LObjDictionary mainDict,LObjDictionary dict,Element element){
 		if(labBook == null || dict == null) return null;
 		if(!isElementLabBookObject(element)) return null;
 		System.out.println("Adding Object to LabBook " + element.getTagName() + " ID = "+element.getAttribute("ID"));
 		LabObject labObject = null;
 		if(element.getTagName().equals(labBookObjectTAGs[FOLDER_TAG])){
 			labObject = addFolderToDictionary(labBook,mainDict,dict,element);
 		}else if(element.getTagName().equals(labBookObjectTAGs[NOTES_TAG])){
 			labObject = addNotesToDictionary(labBook,dict,element);
 		}else if(element.getTagName().equals(labBookObjectTAGs[DATACOLLECTOR_TAG])){
 			labObject = addDataCollectorToDictionary(labBook,dict,element);
 		}else if(element.getTagName().equals(labBookObjectTAGs[DRAWING_TAG])){
 			labObject = addDrawingToDictionary(labBook,dict,element);
 		}else if(element.getTagName().equals(labBookObjectTAGs[UNITCONV_TAG])){
 			labObject = addUnitConvertorToDictionary(labBook,dict,element);
 		}else if(element.getTagName().equals(labBookObjectTAGs[IMAGE_TAG])){
 			labObject = addImageToDictionary(labBook,dict,element);
 		}else if(element.getTagName().equals(labBookObjectTAGs[EXPOBJECT_TAG])){
 			labObject = exportObjectToDictionary(labBook,dict,element);
 		}else if(element.getTagName().equals(labBookObjectTAGs[SUPERNOTES_TAG])){
 			labObject = addSuperNotesToDictionary(labBook,mainDict,dict,element);
 		}
 		if(mainDict != null) labBook.store(mainDict);
 		return labObject;
 	}
 
 	public static int getIdentifierFromElement(Element element){
 		int retValue = -1;
 		if(element.getTagName().equals(labBookObjectTAGs[FOLDER_TAG])){
 			retValue = FOLDER_TAG;
 		}else if(element.getTagName().equals(labBookObjectTAGs[NOTES_TAG])){
 			retValue = NOTES_TAG;
 		}else if(element.getTagName().equals(labBookObjectTAGs[DATACOLLECTOR_TAG])){
 			retValue = DATACOLLECTOR_TAG;
 		}else if(element.getTagName().equals(labBookObjectTAGs[DRAWING_TAG])){
 			retValue = DRAWING_TAG;
 		}else if(element.getTagName().equals(labBookObjectTAGs[UNITCONV_TAG])){
 			retValue = UNITCONV_TAG;
 		}else if(element.getTagName().equals(labBookObjectTAGs[IMAGE_TAG])){
 			retValue = IMAGE_TAG;
 		}else if(element.getTagName().equals(labBookObjectTAGs[EXPOBJECT_TAG])){
 			retValue = EXPOBJECT_TAG;
 		}else if(element.getTagName().equals(labBookObjectTAGs[SUPERNOTES_TAG])){
 			retValue = SUPERNOTES_TAG;
 		}
 		return retValue;
 
 	}
 
 	
 	public static boolean isElementLabBookObject(Element element){
 		if(element == null) return false;
 		String elementName = element.getTagName();
 		if(elementName == null) return false;
 		for(int i = 0; i < labBookObjectTAGs.length; i++){
 			if(elementName.equals(labBookObjectTAGs[i])) return true;
 		}
 		return true;
 	}
 
 	public static LObjDictionary initRootDictionary(LabBook labBook){
 		if(labBook == null) return null;
  		LObjDictionary dict = DefaultFactory.createDictionary();
 		dict.name = "Root";
 		labBook.store(dict);
 		return dict;
 	}
 	
 	
 	public static int getProtocolFromURL(String url){
 		int retValue = UNKNOWN_PROTOCOL;
 		if(url == null) return retValue;
 		if(url.startsWith("http://")) 	return 	HTTP_PROTOCOL;
 		if(url.startsWith("ftp://")) 	return 	FTP_PROTOCOL;
 		if(url.startsWith("file://")) 	return 	FILE_PROTOCOL;
 		if(url.indexOf("://") < 0)  	return 	FILE_PROTOCOL;
 		return retValue;
 	}
 	
 	public static String convertURLStringToPath(String url){
 		if(getProtocolFromURL(url) != FILE_PROTOCOL) return null;
 		if(url.startsWith("file://")){
 			return url.substring(7);
 		}
 		return url;
 	}
 	
 	
 	public static LabObject createRegularObject(LabBook labBook,Element element,String str,String objName){
 		if(element == null) return null;
 		String ID = element.getAttribute("ID");
 		if(ID == null) return  null;
 		if(docObjects.containsKey(ID)){
 			return (LabObject)docObjects.get(ID);
 		}
 		
 		if(!element.getTagName().equals(str)) return null;
 		LabObject labObject = null;
 		if(str.equals(labBookObjectTAGs[EXPOBJECT_TAG])){//embed object
 			if(labBook != null){
 				String url = convertURLStringToPath(element.getAttribute("url"));
 				if(url != null){
 					LabBookFile imFile = new LabBookFile(url);
 					labObject = labBook.importDB(imFile);
 					imFile.close();
 				}
 			}
 		}else{
 			labObject = createObj(objName);
 		}
 		if(labObject == null) return null;
 		String nameObject = element.getAttribute("name");
 		if(nameObject != null){
 			labObject.name = nameObject;
 		}else{
 			labObject.name = "";
 		}
 		docObjects.put(ID,labObject);
 		return labObject;
 	}
 	public static LabObject createRegularObject(LabBook labBook,Element element){
 		int id = getIdentifierFromElement(element);
 		LabObject labObject = null;
 		if(id >= 0){
 			labObject = createRegularObject(labBook,element,labBookObjectTAGs[id],labBookObjectNames[id]);
 		}
 		return labObject;
 	}
 
 	
 	public static LabObject exportObjectToDictionary(LabBook labBook,LObjDictionary dict,Element element){
 		if(labBook == null || dict == null || element == null) return null;
 		if(!element.getTagName().equals(labBookObjectTAGs[EXPOBJECT_TAG])) return null;
 		String url = convertURLStringToPath(element.getAttribute("url"));
 
 
 
 		if(url == null) return null;
 		LabBookFile imFile = new LabBookFile(url);
 		LabObject labObject = labBook.importDB(imFile);
 		imFile.close();
 		if(labObject != null){
 			dict.add(labObject);
 		}
 		return labObject;
 	}
 
 	public static LabObject addFolderToDictionary(LabBook labBook,LObjDictionary mainDict,LObjDictionary dict,Element element){
 		if(labBook == null || dict == null) return null;
 		LabObject labObject = createRegularObject(labBook,element,labBookObjectTAGs[FOLDER_TAG],labBookObjectNames[FOLDER_TAG]);
 		if(labObject == null) return null;
 		LObjDictionary folder = (LObjDictionary)labObject;
 		dict.add(folder);
 		if(mainDict != null) labBook.store(mainDict);
 		
 		String	urlStr = convertURLStringToPath(element.getAttribute("url"));
 		boolean autoRecursion = ((urlStr != null) && urlStr.length() > 0);
 		File file = null;
 		if(autoRecursion){
 			file = new File(urlStr);
 			autoRecursion = file.exists() && file.isDirectory();
 			if(autoRecursion && (labObject.name == null || labObject.name.length() < 1)){
 				labObject.name = file.getName();
 			}
 		}
 		if(autoRecursion){
 			addFileSystemIntoFolder(labBook,mainDict,folder,file);
 		}else{		
 			NodeList nodeList = element.getChildNodes();
 			for(int i = 0; i < nodeList.getLength(); i++){
 				Node node = nodeList.item(i);
 				int type = node.getNodeType();
 				if(type == Node.ELEMENT_NODE) addObjectToLabBook(labBook,mainDict,folder,(Element)node);
 	   		}
 	   		
 	   		boolean pagingView = element.getAttribute("view").equals("paging");
 	   		if(pagingView){
 				folder.viewType = LObjDictionary.PAGING_VIEW;
 			} else {
 				folder.viewType = LObjDictionary.TREE_VIEW;
 	   		}
 	   	}
    		return labObject;
 	}
 	
 	public static void addFileSystemIntoFolder(LabBook labBook,LObjDictionary mainDict,LObjDictionary dict,File file){
 		if(file == null || !file.exists() || !file.isDirectory()) return;
 		String []files = file.list();
 		if(files == null || files.length < 1) return;
 		for(int i = 0; i < files.length; i++){
 			File fileChild = new File(file,files[i]);
 			if(!fileChild.exists()) continue;
 			if(fileChild.isDirectory()){
 				String objName = labBookObjectNames[FOLDER_TAG];
 				LObjDictionary folderObject = (LObjDictionary)createObj(objName);
 				folderObject.name = fileChild.getName();
 				dict.add(folderObject);
 				if(mainDict != null) labBook.store(mainDict);
 				addFileSystemIntoFolder(labBook,mainDict,folderObject,fileChild);
 			}else if(fileChild.isFile()){
 				if(files[i].endsWith(".gif")  || files[i].endsWith(".GIF")  ||
 				   files[i].endsWith(".bmp")  || files[i].endsWith(".BMP")  ||
 				   files[i].endsWith(".png")  || files[i].endsWith(".PNG")  ||
 				   files[i].endsWith(".tiff") || files[i].endsWith(".TIFF") ||
 				   files[i].endsWith(".jpg")  || files[i].endsWith(".JPG")  ||
 				   files[i].endsWith(".jpeg") || files[i].endsWith(".JPEG")){
 						addImageIntoFolder(fileChild,labBook,mainDict,dict);
 				}else if(files[i].endsWith(".export")){
 					LabBookFile imFile = new LabBookFile(fileChild.getAbsolutePath());
 					LabObject exportObject = labBook.importDB(imFile);
 					imFile.close();
 					if(exportObject != null){
 						dict.add(exportObject);
 						exportObject.store();
 					}
 				}
 			}
 		}
 	}
 	public static void addImageIntoFolder(File file,LabBook labBook,LObjDictionary mainDict,LObjDictionary dict){
 		if(dict == null || file == null || !file.exists() || file.isDirectory()) return;
 		String fileName = file.getName();
 		String objName = labBookObjectNames[IMAGE_TAG];
 		LabObject imageObject = createObj(objName);
 		LObjImageView view = null;
 		if(imageObject != null) view = (LObjImageView)imageObject.getView(null,false,null);
 		if(view == null) return;
 		imageObject.name = file.getName();
 		if(fileName.endsWith(".bmp") || fileName.endsWith(".BMP")){
 			view.loadImage(file.getAbsolutePath());
 		}else if(fileName.endsWith(".gif") || fileName.endsWith(".GIF") ||
 				 (qtInstalled && (fileName.endsWith(".png") || fileName.endsWith(".PNG") ||
 				  fileName.endsWith(".tiff") || fileName.endsWith(".TIFF"))) ||
 				  fileName.endsWith(".jpg") || fileName.endsWith(".JPG") ||
 				  fileName.endsWith(".jpeg") || fileName.endsWith(".JPEG")){
 					exportImage(file.getAbsolutePath(),view);
 		}
 		dict.add(imageObject);
 		imageObject.store();
 	}
 	
 	public static LabObject addSuperNotesToDictionary(LabBook labBook,LObjDictionary mainDict,LObjDictionary dict,Element element){
 		if(labBook == null || dict == null) return null;
 		LabObject labObject = createRegularObject(labBook,element,labBookObjectTAGs[SUPERNOTES_TAG],labBookObjectNames[SUPERNOTES_TAG]);
 		if(labObject == null) return null;
 		LObjCCTextArea superNotes = (LObjCCTextArea)labObject;
 		
 		LObjCCTextAreaView view = (LObjCCTextAreaView)labObject.getView(null,false,dict);
 
 		if(view == null) return null;
 		waba.util.Vector lines = new waba.util.Vector();
 
 		NodeList children = element.getChildNodes();
 		if(children != null){
 			int currParagraph = 0;
 			waba.util.Vector components = new waba.util.Vector();
 			waba.util.Vector linkComponents = new waba.util.Vector();
 			for(int i = 0; i < children.getLength(); i++){
 				Element child = (Element)children.item(i);
 				if(child == null) continue;
 				if(child.getTagName().equals("SNPARAGRAPH")){
 					int nSpaces = getIntFromString(child.getAttribute("indent"));
 					Node textNode = child.getFirstChild();
 					String tData = "";
 					if(textNode instanceof CharacterData){
 						tData = ((CharacterData)textNode).getData().trim();
 			        	if(tData.length() > 0){
 							if(nSpaces > 0){
 								for(int p=0; p < nSpaces; p++) tData = "\t"+tData;
 							}
 							tData = tData.replace('\n',' ');
 							tData = tData.replace('\r',' ');
 			        	}
 					}
 					String linkStr = child.getAttribute("link");
 					boolean link = (linkStr == null)?false:linkStr.equals("true");
 					String idref = child.getAttribute("object");
 					if(link && idref == null) link = false;
 					if(!link && idref != null) link = true;
 					if(link){
 						Element linkElement =  (Element)currentDocument.getElementById(idref);
 						if(linkElement == null){
 							link = false;
 						}else{      
 							LabObject linkObject = createRegularObject(labBook,linkElement);
 							if(linkObject == null){
 								link = false;
 							}else{
 								linkComponents.add(linkObject);
 							}
 						}
 					}
 					CCStringWrapper wrapper = CCTextArea.createCCStringWrapper(tData,child.getAttribute("linkcolor"),link,null);
 					lines.add(wrapper);
 					currParagraph++;
 				}else if(child.getTagName().equals("EMBOBJ")){
 					String idref = child.getAttribute("object");
 					LabObject embObject = null;
 					Element embElement = null;
 					if(idref == null || idref.trim().length() < 1){
 						embElement = (Element)child.getFirstChild();
 						if(embElement != null){
 							int id = getIdentifierFromElement(embElement);
 							if(id >= 0){
 								if(superNotes.getDict() != null){
 									embObject = addObjectToLabBook(labBook,mainDict,superNotes.getDict(),embElement);
 								}
 							}
 						}
 					}else{
 						embElement =  (Element)currentDocument.getElementById(idref);        
 						if(embElement != null) embObject = createRegularObject(labBook,embElement);
 					}
 					if(embObject != null){
 						String tempStr = child.getAttribute("link");
 						boolean link = (tempStr != null && tempStr.equals("true"));
 						String linkColor = child.getAttribute("linkcolor");
 						if(linkColor == null || linkColor.length() < 1) linkColor = "0000FF";
 					
 						LabObjectView embObjView = (link)?embObject.getMinimizedView():embObject.getView(null,false,null);
 						int	prefWidth = -1;
 						int	prefHeight = -1;
 						if(embObjView != null){
 							extra.ui.Dimension dimPref = null;
 							try{
 								dimPref = embObjView.getPreferredSize();
 							}catch(Exception e){
 								dimPref = new extra.ui.Dimension((int)((double)embObject.name.length()*5.5+0.5),12) ;
 							}
 							if(dimPref != null){
 								prefWidth = dimPref.width;
 								prefHeight = dimPref.height;
 							}
 						}
 						String widthStr = child.getAttribute("w");
 						int w = (widthStr == null || widthStr.length() < 1)?prefWidth:getIntFromString(widthStr);
 						if(w < 10) w = 10;
 						String heightStr = child.getAttribute("h");
 						int h = (heightStr == null || heightStr.length() < 1)?prefHeight:getIntFromString(heightStr);
 						if(h < 10) h = 10;
 						int alignment = LBCompDesc.ALIGNMENT_LEFT;
 						tempStr = child.getAttribute("alignment");
 						if(tempStr != null && tempStr.equals("right")){
 							alignment = LBCompDesc.ALIGNMENT_RIGHT;
 						}
 						tempStr = child.getAttribute("wrapping");
 						boolean wrapping = (tempStr != null && tempStr.equals("true"));
 						LBCompDesc compDesc = new LBCompDesc(currParagraph,w, h,alignment, wrapping,link);
 						compDesc.linkColor = getIntColorFromStringColor(linkColor);
 						compDesc.setObject(embObject);
 						components.add(compDesc);
 					}					
 				}
 			}
 			view.createTArea(lines,linkComponents,components);
 
 
 		}
 
 
 
 		superNotes.store();
 		dict.add(superNotes);
 		return labObject;
 	
 	}	
 	
 	public static LabObject addDataCollectorToDictionary(LabBook labBook,LObjDictionary dict,Element element){
 		if(dict == null) return null;
 		LabObject labObject = createRegularObject(labBook,element,labBookObjectTAGs[DATACOLLECTOR_TAG],labBookObjectNames[DATACOLLECTOR_TAG]);
 		if(labObject == null) return null;
 		String probStr = element.getAttribute("probe");
 		if(probStr == null || probStr.length() < 1){
 			probStr = ProbFactory.probeNames[ProbFactory.Prob_ThermalCouple];
 		}
 		String addIntegralStr = element.getAttribute("addintegral");
 		boolean addIntegral = (addIntegralStr != null && addIntegralStr.equals("true"));
 		
 		int probeId = ProbFactory.getIndex(probStr);
 		LObjDataCollector dc = (LObjDataCollector)labObject;
 		waba.util.Vector dataSources = new waba.util.Vector(1);
 		LObjProbeDataSource newDS = 
			LObjProbeDataSource.getProbeDataSource(probeId, dc.getInterfaceID());
 		dataSources.add(newDS);
 
 		LObjCalculusTrans trans = null;
 		if(addIntegral){
 			trans = (LObjCalculusTrans)DataObjFactory.create(DataObjFactory.CALCULUS_TRANS);
 			trans.setDataSource(newDS);
 			trans.name = "Integral";
 			trans.store();
 			dataSources.add(trans);
 		}
 		dc.setDataSources(dataSources);
 		LObjGraph graph = dc.getGraph();
 		if(graph != null){
 			graph.clear();
 			graph.createDefaultAxis();
 
 			Axis xaxis = graph.getXAxis(0);
 			float xmin = xaxis.getDispMin();
 			float xmax = xaxis.getDispMax();
 			float tempFloat = Float.NaN;
 			String xminStr = element.getAttribute("xmin");
 			if(xminStr != null && xminStr.length() > 0){
 				tempFloat = getFloatFromString(xminStr);
 				if(tempFloat != Float.NaN) xmin = tempFloat;
 			}
 			String xmaxStr = element.getAttribute("xmax");
 			if(xmaxStr != null && xmaxStr.length() > 0){
 				tempFloat = getFloatFromString(xmaxStr);
 				if(tempFloat != Float.NaN) xmax = tempFloat;
 			}
 			xaxis.setRange(xmin, xmax-xmin);
 			
 			Axis yaxis = graph.getYAxis(0);
 			float ymin = yaxis.getDispMin();
 			float ymax = yaxis.getDispMax();
 			String yminStr = element.getAttribute("ymin");
 			if(yminStr != null && yminStr.length() > 0){
 				tempFloat = getFloatFromString(yminStr);
 				if(tempFloat != Float.NaN) ymin = tempFloat;
 			}
 			String ymaxStr = element.getAttribute("ymax");
 			if(ymaxStr != null && ymaxStr.length() > 0){
 				tempFloat = getFloatFromString(ymaxStr);
 				if(tempFloat != Float.NaN) ymax = tempFloat;
 			}
 			System.out.println("XML2LabBook: setting yaxisRange: " + ymin + ", " + (ymax-ymin));
 			yaxis.setRange(ymin, ymax-ymin);					   			
 			
 			graph.addDataSource(newDS,true,0,0);
 			if(trans != null){
 				graph.addYAxis();
 				graph.addDataSource(trans, true, 0, 1);
 			}
 			graph.store();
 		}
 
 		dict.add(labObject);
 		return labObject;
 	}
 
 	public static LabObject addDrawingToDictionary(LabBook labBook,LObjDictionary dict,Element element){
 		if(dict == null) return null;
 		LabObject labObject = createRegularObject(labBook,element,labBookObjectTAGs[DRAWING_TAG],labBookObjectNames[DRAWING_TAG]);
 		if(labObject == null) return null;
 		dict.add(labObject);
 		return labObject;
 
 	}
 
 	public static LabObject addUnitConvertorToDictionary(LabBook labBook,LObjDictionary dict,Element element){
 		if(dict == null) return null;
 		LabObject labObject = createRegularObject(labBook,element,labBookObjectTAGs[UNITCONV_TAG],labBookObjectNames[UNITCONV_TAG]);
 		if(labObject == null) return null;
 		dict.add(labObject);
 		return labObject;
 
 	}
 
 	
 	public static LabObject addNotesToDictionary(LabBook labBook,LObjDictionary dict,Element element){
 		if(dict == null) return null;
 		LabObject labObject = createRegularObject(labBook,element,labBookObjectTAGs[NOTES_TAG],labBookObjectNames[NOTES_TAG]);
 		if(labObject == null) return null;
 		int nSpaces = getIntFromString(element.getAttribute("indent"));
 		Node textNode = element.getFirstChild();
 		String tData = "";
 		if(textNode instanceof CharacterData){
 			tData = ((CharacterData)textNode).getData().trim();
         	if(tData.length() > 0){
 				if(nSpaces > 0){
 					for(int p=0; p < nSpaces; p++) tData = "\t"+tData;
 				}
         	}
 		}
 		((LObjDocument)labObject).setText(tData);
 		dict.add(labObject);
 		return labObject;
 	}
 	
 	
 	
 	public static LabObject addImageToDictionary(LabBook labBook,LObjDictionary dict,Element element){
 		if(dict == null) return null;
 		LabObject labObject = createRegularObject(labBook,element,labBookObjectTAGs[IMAGE_TAG],labBookObjectNames[IMAGE_TAG]);
 		if(labObject == null) return null;
 		String imageURL = convertURLStringToPath(element.getAttribute("url"));
 		if(imageURL != null){
 			LObjImageView view = (LObjImageView)labObject.getView(null,false,null);
 			if(view != null){
 				if(imageURL.endsWith(".gif") || imageURL.endsWith(".GIF") ||
 				   (qtInstalled && (imageURL.endsWith(".png") || imageURL.endsWith(".PNG") ||
 				   imageURL.endsWith(".tiff") || imageURL.endsWith(".TIFF"))) ||
 				   imageURL.endsWith(".jpg") || imageURL.endsWith(".JPG") ||
 				   imageURL.endsWith(".jpeg") || imageURL.endsWith(".JPEG")){
 						exportImage(imageURL,view);
 				}else{
 					view.loadImage(imageURL);
 				}
 			}
 		}
 		
 		
 		dict.add(labObject);
 		return labObject;
 	}
 	
 
 	public static LabObject createObj(String objType)
 	{
 		LabObject newObj = null;
 
 		for(int f = 0; f < LabBook.objFactories.length; f++){
 			if(LabBook.objFactories[f] == null) continue;
 			LabObjDescriptor []desc = LabBook.objFactories[f].getLabBookObjDesc();
 			if(desc == null) continue;
 			boolean doExit = false;
 			for(int d = 0; d < desc.length; d++){
 				if(desc[d] == null) continue;
 				if(objType.equals(desc[d].name)){
 					newObj = LabBook.objFactories[f].makeNewObj(desc[d].objType);
 					doExit = true;
 					break;
 				}
 			}
 			if(doExit) break;
 		}
 
 		if(newObj != null){
 			if(newIndex == 0){
 				newObj.name = objType;		    
 			} else {
 				newObj.name = objType + " " + newIndex;		    
 			}
 			newIndex++;
 		}
 		return newObj;
 	}
 	
 	public static int getIntColorFromStringColor(String linkColor){
 		if(linkColor == null) return 0;
 		String str = linkColor;
 		if(linkColor.length() > 6){
 			str = linkColor.substring(0,6);
 		}else if(linkColor.length() < 6){
 			int addZero = 6 - linkColor.length();
 			for(int i = 0; i < addZero; i++){
 				str += "0";
 			}
 		}
 		int color = 0;
 		int rColor = getIntFromHexString(str.substring(0,2));
 		rColor <<= 16;
 		color |= rColor;
 		int gColor = getIntFromHexString(str.substring(2,4));
 		gColor <<= 8;
 		color |= gColor;
 		int bColor = getIntFromHexString(str.substring(4,6));
 		color |= bColor;
 		return color;
 	}
 	
 	public static float getFloatFromString(String str){
 		float retValue = 0;
 		try{
 			retValue = Float.parseFloat(str);
 		}catch(Exception te){
 			retValue = Float.NaN;
 		}
 		return retValue;		
 	}
 	public static int getIntFromString(String str){
 		int retValue = 0;
 		try{
 			retValue = Integer.parseInt(str);
 		}catch(Exception te){
 			retValue = 0;
 		}
 		return retValue;
 	}
 	public static int getIntFromHexString(String str){
 		int retValue = 0;
 		if(str == null || str.length() < 1) return retValue;
 		int multiplayer = 1;
 		for(int i = str.length() - 1; i >= 0; i--){
 			retValue += (multiplayer*getIntFromHexChar(str.charAt(i)));
 			multiplayer <<= 4;
 		}
 		return retValue;
 	}
 	
 	public static int getIntFromHexChar(char c){
 		if(c >= '0' && c <= '9') return (int)(c - '0');
 		if(c >= 'a' && c <= 'f') return (10 + (int)(c - 'a'));
 		if(c >= 'A' && c <= 'F') return (10 + (int)(c - 'A'));
 		return 0;
 	}
 	public static void exportImage(String str,LObjImageView view){
 		if(!qtInstalled){
 			exportImageWithoutQT(str,view);
 			return;
 		}else if(qtManager != null){
 			qtManager.exportImage(str,view);
 		}
 	}
 
 
 
 	public static void exportImageWithoutQT(String str,LObjImageView view){
 		try{
 			FileInputStream fis = new FileInputStream(str);
 			byte []buffer = new byte[4096];
 			int rb = 0;
 			int currIndex = 0;
 			boolean doExit = false;
 			while(!doExit){
 				rb = fis.read(buffer,currIndex,buffer.length - currIndex);
 				if(rb < 1){
 					doExit = true;
 				}else{
 					currIndex += rb;
 					if(currIndex >= buffer.length){
 						byte []newBuffer = new byte[buffer.length*2];
 						System.arraycopy(buffer,0,newBuffer,0,buffer.length);
 						buffer = newBuffer;
 					}
 				}
 			}
 			java.awt.Image  awtImage = Toolkit.getDefaultToolkit().createImage(buffer);
 			PixelGrabber pg = new PixelGrabber(awtImage,0,0,-1,-1,true);
 			pg.grabPixels();
 			if((pg.status() & ImageObserver.ABORT) != 0) return;
 			int			imageHeight 				= pg.getHeight();
 			int			imageWidth					= pg.getWidth();
 			if(imageHeight <= 0 || imageWidth <= 0) return;
 			Object		pixels 						= pg.getPixels();
 			int			imageBPP 					= 8;
 			int			[]imageCMAP					= org.concord.waba.extra.ui.CCPalette.getPaletteAsInt();
 			int			imageScanlen 				= imageWidth;
 			byte		[]imagePixels				= null;
 			ColorModel cm = pg.getColorModel();
 			if(pixels instanceof byte[]){
 				imagePixels = (byte [])pixels;
 				if(cm instanceof DirectColorModel) return;
 				return;
 			}else if(pixels instanceof int[]){
 				if(cm instanceof IndexColorModel) return;
 				imagePixels				= new byte[imageHeight * imageWidth];
 				int []pgPixel = (int[])pixels;
 				if(pgPixel.length != imageHeight * imageWidth) return;
 				DirectColorModel dcm = (DirectColorModel)cm;
 				for(int p = 0; p < pgPixel.length; p++){
 					int r = dcm.getRed(pgPixel[p]);
 					int g = dcm.getGreen(pgPixel[p]);
 					int b = dcm.getBlue(pgPixel[p]);
 					int pIndex = org.concord.waba.extra.ui.CCPalette.findNearestColor(r,g,b);
 					if(pIndex < 0) pIndex += 256;
 					imagePixels[p] = (byte)pIndex;
 				}
 				if(view != null){
 					view.loadImage(imageWidth,imageHeight,imageBPP,imageCMAP,imageScanlen,imagePixels);
 				}
 				
 			}else{
 				return;
 			}
 		}catch(Exception e){
 		}
 		return;
 	}
 
     // Error handler to report errors and warnings
     private static class MyErrorHandler implements ErrorHandler {
         /** Error handler output goes here */
         private PrintWriter out;
 
         MyErrorHandler(PrintWriter out) {
             this.out = out;
         }
 
         /**
          * Returns a string describing parse exception details
          */
         private String getParseExceptionInfo(SAXParseException spe) {
             String systemId = spe.getSystemId();
             if (systemId == null) {
                 systemId = "null";
             }
             String info = "URI=" + systemId +
                 " Line=" + spe.getLineNumber() +
                 ": " + spe.getMessage();
             return info;
         }
 
         // The following methods are standard SAX ErrorHandler methods.
         // See SAX documentation for more info.
 
         public void warning(SAXParseException spe) throws SAXException {
             out.println("Warning: " + getParseExceptionInfo(spe));
         }
         
         public void error(SAXParseException spe) throws SAXException {
             String message = "Error: " + getParseExceptionInfo(spe);
             throw new SAXException(message);
         }
 
         public void fatalError(SAXParseException spe) throws SAXException {
             String message = "Fatal Error: " + getParseExceptionInfo(spe);
             throw new SAXException(message);
         }
     }
 
 }
