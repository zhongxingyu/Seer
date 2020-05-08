 package com.dc2f.publish.files;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.dc2f.contentrepository.BranchAccess;
 import com.dc2f.contentrepository.Node;
 import com.dc2f.nodetype.BinaryNodeType;
 import com.dc2f.renderer.url.URLMapper;
 
 /**
  * FIXME unite this class with ServletURLMapper
  */
 public class FilePublisherURLMapper implements URLMapper {
 	static Logger logger = Logger.getLogger(FilePublisherURLMapper.class.getName());
 
 	private Node project;
 	private BranchAccess craccess;
 	private Node[] path;
 	
 	private String pathPrefix = "";
 
 	public FilePublisherURLMapper(BranchAccess craccess, Node project, Node renderedNode) {
 		this.craccess = craccess;
 		this.project = project;
 		
 		if (renderedNode != null) {
 			// find the root path for this project given the currently rendered node.
 			String path = this.getRenderPath(renderedNode);
 			StringBuffer prefix = new StringBuffer();
 			int lastpos = 0;
 			while ((lastpos = path.indexOf('/', lastpos+1)) > -1) {
 				if (prefix.length() > 0) {
 					prefix.append('/');
 				}
 				prefix.append("..");
 			}
 			this.pathPrefix = prefix.toString();
 		}
 //		this.path = craccess.getNodesInPath(renderedNode.getPath());
 	}
 
 	public String getRenderURL(Node node) {
 		/*
 		StringBuffer url = new StringBuffer();
 		Node[] targetPath = craccess.getNodesInPath(node.getPath());
 		
 		int startPath = 0;
 		for (int i = path.length - 1 ; i >= 0 ; i--) {
 			if (targetPath.length > i) {
 				if (targetPath[i].equals(path[i])) {
 					startPath = i;
 					break;
 				}
 			}
 			url.append("../");
 		}
 		for (int i = startPath ; i < targetPath.length - 1 ; i++) {
 			url.append(targetPath[i].getName()).append('/');
 		}
 		
 		url.append(node.getName());
 		String fileExtension = ".html";
 		if (node.getNodeType() instanceof BinaryNodeType) {
 			String mimeType = ((BinaryNodeType)node.getNodeType()).getMimeType(node);
 			fileExtension = FilePublisherRenderResponse.getFileExtensionForMimeType(mimeType);
 		}
 		url.append(fileExtension);
 		return url.toString();
 		*/
 		return pathPrefix + getRenderPath(node);
 	}
 
 	public String getRenderPath(Node node) {
 		
 		
 		// get all render configurations from the project
 		@SuppressWarnings("unchecked")
 		List<Node> renderConfigurations = (List<Node>) project.get("renderconfiguration");
 		
 		// look for all url mappings
 		for (Node renderConfig : renderConfigurations) {
 			
 			@SuppressWarnings("unchecked")
 			List<Node> urlMappingList = (List<Node>) renderConfig.get("urlmapping");
 			
 			if (urlMappingList != null && urlMappingList.size() > 0) {
 				for (Node urlmapping : urlMappingList) {
 					logger.info("We have a render configuration with a url mapper. " + urlmapping.get("url"));
 					// render the url mapping
 					Node rootNode = (Node) renderConfig.get("rootnode");
 					String path = findPathForNode(node, urlmapping, rootNode, "");
 					if (path != null) {
 						return path;
 					}
 				}
 			}
 		}

 		
 		return null;
 	}
 
 	private String findPathForNode(Node node, Node urlmapping, Node checkBaseNode, String prefix) {
 		Node[] children = craccess.getChildren(checkBaseNode);
 		for(Node child : children) {
 			String tmp = findPathForNode(node, urlmapping, child, prefix + "/" + child.getName());
 			if (tmp != null) {
 				return tmp;
 			}
 			if (child.equals(node)) {
 				// we found it!
 				String fileExtension = ".html";
 				if (node.getNodeType() instanceof BinaryNodeType) {
 					String mimeType = ((BinaryNodeType)node.getNodeType()).getMimeType(node);
 					fileExtension = FilePublisherRenderResponse.getFileExtensionForMimeType(mimeType);
 				}
 				return urlmapping.get("url") + child.getName() + fileExtension;
 			}
 		}
 		return null;
 	}
 
 }
