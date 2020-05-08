 package tagEditor;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Path;
 import java.nio.file.PathMatcher;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeModel;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 public class MP3_FileVisitor extends SimpleFileVisitor<Path> {
 	
 	private PathMatcher pathMatcher;
 	private TreeModel tree;
 	private Element currentDirectory;
 	private Element root;
 	private Element mp3File;
 	private MP3File mp3;
 	private MP3Parser parser =  new MP3Parser();;
 	private Directory directory;
 	private View GUI;
 	private long lastTimeCacheChanged = Long.MAX_VALUE;
 	private boolean cacheExists = false;
 	private boolean cacheTooOld = false;
 	private Document doc;
 	private Document cacheDoc;
 	private DocumentBuilder dBuilder;
 	
 	public MP3_FileVisitor(View GUI){
 		this.pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + "*.mp3*");
 		this.GUI = GUI;
 			try {
 				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 				dbFactory.setValidating(true);
 				dBuilder = dbFactory.newDocumentBuilder();
 				
 			} catch (ParserConfigurationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			this.doc = dBuilder.newDocument();
 	}
 	
 	@Override
 	public FileVisitResult visitFile(Path filePath,BasicFileAttributes basicFileAttributes) {
 		if (filePath.getFileName() != null && pathMatcher.matches(filePath.getFileName())) {
 			// Everything worked fine, so let's parse the file
 			if(cacheExists){ 
 				File f = new File(filePath.toString());
				long lastmod = f.lastModified();
 				if(cacheDoc.getElementById(filePath.toString().replace("/", ":").replace(" ", "_")) == null){ // Is the file in the cache?
 					System.out.println("CACHE MISS: FILE:" + filePath);
 					cacheTooOld = true;
 				}
				else if( lastmod> lastTimeCacheChanged){ // Was the file modified?
 					System.out.println("MODIFIED: FILE:" + filePath);
 				
 					cacheTooOld = true;
 				}
 			}
 			if(cacheTooOld || !cacheExists){
 				mp3 = new MP3File(); 
 				try {
 					mp3 = parser.readMP3(filePath);
 					mp3.setPath(filePath.toString());
 				} catch (IOException e) {
 					// Maybe an ID3v24 tag, but nonetheless just continue
 					return FileVisitResult.CONTINUE;
 				}
 				mp3File = XMLControl.MP3ToXML(mp3, this.doc);
 				currentDirectory.appendChild(mp3File);
 			} else {
 				System.out.println("CACHE HIT: FILE:" + filePath.getFileName());
 				XMLControl.filesReadFromCache++;
 				// This is how one transfer the node of one document to another, wtf
 				Element temp = cacheDoc.getElementById(filePath.toString().replace("/", ":").replace(" ", "_"));
 				currentDirectory.appendChild(doc.adoptNode(temp.cloneNode(true)));
 			}
 			cacheTooOld = false;
 		}
 		return FileVisitResult.CONTINUE;
 	}
 
 	@Override
 	public FileVisitResult preVisitDirectory(Path directoryPath,BasicFileAttributes attrs) {
 		if (directoryPath.getFileName() != null) {
 			directory = new Directory(directoryPath.toString());
 			Element tempDirectory = XMLControl.DirectoryToXML(directory, doc);
 			String myPath = directoryPath.toString() + "/cache.xml";
 			File f = new File(myPath);
 			//Does the cache exists and is it the first cache?
 			if(f.exists() && !cacheExists) {
 				lastTimeCacheChanged = f.lastModified();
 				cacheExists = true;
 				try {
 					this.cacheDoc = dBuilder.parse(f);
 				} catch (SAXException | IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}		
 			}
 			if (currentDirectory != null) { // root set? If so, add the working directory onto the current parent directory
 				currentDirectory.appendChild(tempDirectory); 
 				currentDirectory = tempDirectory;
 			} else { // root is not set
 				root = tempDirectory;
 				currentDirectory = tempDirectory;
 			}
 			System.out.println("DIR: " + directoryPath);
 		}
 		return FileVisitResult.CONTINUE;
 	}
 
 	@Override
 	public FileVisitResult visitFileFailed(Path filePath, IOException exc) {
 		return FileVisitResult.CONTINUE;
 	}
 
 	@Override
 	public FileVisitResult postVisitDirectory(Path directoryPath, IOException exc) {
 		Element tempDirectory = currentDirectory;
 		currentDirectory = (Element) currentDirectory.getParentNode();
 		if (tempDirectory.getChildNodes().getLength() == 0 && currentDirectory != null) {
 			currentDirectory.removeChild(tempDirectory);
 		} 
 		return FileVisitResult.CONTINUE;
 	}
 	
 	public TreeModel getTree() {
 		doc.appendChild(root);
 		tree = new DefaultTreeModel(XMLControl.build(doc.getDocumentElement()));
 		GUI.getTree().setModel(tree);
 		return tree;
 	}
 	
 	public boolean getCacheTooOld() {
 		return this.cacheTooOld;
 	}
 	
 	public String getRootDirectory(){
 		return root.getAttribute("filename");
 	}
 
 }
