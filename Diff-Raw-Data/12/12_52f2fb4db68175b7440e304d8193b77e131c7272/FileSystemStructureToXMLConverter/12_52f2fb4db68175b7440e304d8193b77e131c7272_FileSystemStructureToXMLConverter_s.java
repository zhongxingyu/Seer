 
 
 package org.vpac.grisu.fs.control.utils;
 
 import java.io.FileNotFoundException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileType;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.control.exceptions.VomsException;
 import org.vpac.grisu.fs.model.MountPoint;
 import org.vpac.grisu.fs.model.User;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * This one is used to browse the remote filesystem (user-accessable "grid-space") and return a xml document that
  * contains all information about it to be transfered over the web service. At the moment it only contains the filesystem
  * structure, the file type and file name as information. This could be extended to also include file size and maybe permission.
  * 
  * @author Markus Binsteiner
  *
  */
 public class FileSystemStructureToXMLConverter {
 	
 	static final Logger myLogger = Logger
 	.getLogger(FileSystemStructureToXMLConverter.class.getName());
 
 	private static DocumentBuilder docBuilder = null;
 	
 	Document output = null;
 	private User user = null;
 
 	public static DocumentBuilder getDocBuilder() {
 
 		if (docBuilder == null) {
 			try {
 				DocumentBuilderFactory docFactory = DocumentBuilderFactory
 						.newInstance();
 				docBuilder = docFactory.newDocumentBuilder();
 			} catch (ParserConfigurationException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 				return null;
 			}
 		}
 		return docBuilder;
 	}
 	
 	/**
 	 * Build an instance of a FileSystemStructureToXMLConverter and connect it to a user so we know which filesystems
 	 * are mounted and therefore accessible.
 	 * @param user the user
 	 */
 	public FileSystemStructureToXMLConverter(User user) {
 		this.user = user;
 	}
 
 	/**
 	 * Parses the remote filesystem(s) and returns the structure of it/them in a xml file.
 	 * 
 	 * @param folder the root folder
 	 * @param recursion_level the recursion_level (usually 1 because otherwise the browsing can take a long time)
 	 * @param absolutePath whether you want the path information within the xml file absolute (like gsiftp://ngdata.../file.txt)
 	 * 				or not (/home.vpac.NGAdmin/file.txt)
 	 * @return the directory structure
 	 * @throws FileNotFoundException if the root folder does not exist
 	 * @throws DOMException if there is a xml problem
 	 * @throws RemoteFileSystemException if the filesystem can't be accessed
 	 * @throws VomsException if there is a problem with the vo / voms proxy
 	 */
 	public Document getDirectoryStructure(String folder,
 			int recursion_level, boolean absolutePath)
 			throws FileNotFoundException, DOMException, RemoteFileSystemException, VomsException {
 
 		output = getDocBuilder().newDocument();
 		Element root = output.createElement("Files");
 		
 		if (absolutePath) {
 			root.setAttribute("absolutePath", "true");
 		} else {
 			root.setAttribute("absolutePath", "false");
 		}
 		root.setAttribute("name", "fs_root");
 		
 		output.appendChild(root);
 		
 		if ("/".equals(folder)) {
 			for (MountPoint mp : user.getAllMountPoints()) {
 				Element mp_element = output.createElement("MountPoint");
 				if (absolutePath) {
 					mp_element.setAttribute("path", mp.getRootUrl());
 					mp_element.setAttribute("name", mp.getRootUrl());
 				} else {
 					mp_element.setAttribute("path", mp.getMountpoint());
 					mp_element.setAttribute("name", mp.getMountpoint().substring(1));
 				}
 				if ( recursion_level > 1 ) 
 					buildDirectoryStructure(mp_element, mp, absolutePath, 1, recursion_level);
 				root.appendChild(mp_element);
 			}
 		} else {
 			MountPoint mp = null;
 			if ( folder.startsWith("/") ) {
 				mp = user.getResponsibleMountpointForUserSpaceFile(folder);
 			} else {
 				mp = user.getResponsibleMountpointForAbsoluteFile(folder);
 			}
 			Element root_element = null;
 //			if ( mp != null ) {
 //				root_element = output.createElement("MountPoint");
 //				if (absolutePath) {
 //					root_element.setAttribute("path", mp.getRootUrl());
 //					root_element.setAttribute("name", mp.getRootUrl());
 //				} else {
 //					root_element.setAttribute("path", mp.getMountpoint());
 //					root_element.setAttribute("name", mp.getMountpoint().substring(1));
 //				
 //				}
 //				
 //				buildDirectoryStructure(root_element, mp, absolutePath, 0, recursion_level);
 //			} else {
 				try {
 					root_element = createElement(user.aquireFile(folder), absolutePath, mp);
 				} catch (FileSystemException e) {
 					throw new RemoteFileSystemException("Could not aquire file: "+folder+": "+e.getMessage());
 				}
 				buildDirectoryStructure(root_element, mp, absolutePath, 1, recursion_level);
 //			}
 			root.appendChild(root_element);
 				
 		}
 
 		return output;
 	}
 
 	private void buildDirectoryStructure(Element parentElement, MountPoint mp, boolean absolutePath, int currentRecursion, int maxRecursion) throws RemoteFileSystemException, VomsException  {
 		
 		FileObject[] filesAndDirs;
 		String parentFolder = parentElement.getAttribute("path");
 		if ( parentFolder == null || "".equals(parentFolder) ) {
 			// means no folder
 			return;
 		}
 		FileObject folder = user.aquireFile(parentFolder);
 		try {
 			myLogger.debug("Accessing folder: "+parentFolder);
 			filesAndDirs = folder.getChildren();
 		} catch (FileSystemException e) {
 			//TODO improve that
 			e.printStackTrace();
 			myLogger.error("Can't access folder: "+parentFolder);
 			return;
 		}
 		
 		try {
 			for (FileObject fo : filesAndDirs) {
 				if (fo.getType().equals(FileType.FOLDER)) {
 					if ( currentRecursion < maxRecursion ) {
 						Element element = createElement(fo, absolutePath, mp);
 						parentElement.appendChild(element);
 						buildDirectoryStructure(element, mp, absolutePath, currentRecursion+1, maxRecursion);
 					}	else { 
 						Element element = createElement(fo, absolutePath, mp);
 						parentElement.appendChild(element);
 					}
 				} else {
 					Element element = createElement(fo, absolutePath, mp);
 					parentElement.appendChild(element);
 				}
 			}
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException("Could not access file/folder under: "+parentFolder+": "+e.getMessage());
 		} 
 	}
 
 	private Element createElement(FileObject fo, boolean absolute_path, MountPoint mp) throws FileSystemException, DOMException {
 	
 		Element element = null;
 		
 		if (fo.getType().equals(FileType.FOLDER)) {
 			element = output.createElement("Directory");
 			if ( absolute_path )
 				element.setAttribute("path", fo.getName().getURI());
 			else {
 				myLogger.debug("Setting user-space path to: "+mp.replaceAbsoluteRootUrlWithMountPoint(fo.getName().getURI()));
 				element.setAttribute("path", mp.replaceAbsoluteRootUrlWithMountPoint(fo.getName().getURI()));
 			}
 		} else {
 			element = output.createElement("File");
 		}
 		
 		element.setAttribute("name", fo.getName().getBaseName());
 		if ( fo.getType().equals(FileType.FILE) ) 
 			element.setAttribute("size", new Long(fo.getContent().getSize()).toString());
 
 		return element;
 	}
 
 }
