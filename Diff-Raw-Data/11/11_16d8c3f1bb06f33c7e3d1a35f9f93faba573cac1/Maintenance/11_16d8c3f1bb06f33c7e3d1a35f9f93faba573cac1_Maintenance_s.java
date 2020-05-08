 package sce.finalprojects.sceprojectbackend.algorithms;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ls.DOMImplementationLS;
 
 
 
 import sce.finalprojects.sceprojectbackend.database.DatabaseOperations;
 import sce.finalprojects.sceprojectbackend.datatypes.ArrayOfCommentsDO;
 import sce.finalprojects.sceprojectbackend.datatypes.Cluster;
 import sce.finalprojects.sceprojectbackend.datatypes.Comment;
 import sce.finalprojects.sceprojectbackend.datatypes.CommentEntityDS;
 import sce.finalprojects.sceprojectbackend.datatypes.DocDO;
 import sce.finalprojects.sceprojectbackend.datatypes.MapCell;
 import sce.finalprojects.sceprojectbackend.datatypes.XmlElement;
 import sce.finalprojects.sceprojectbackend.factories.ArrayOfCommentsFactory;
 import sce.finalprojects.sceprojectbackend.factories.DocFactory;
 /**
  * this class is responsible of creating a mapping from XML and for adding a new element to the HAC
  * @author the source
  *
  */
 public class Maintenance {
 	
 	public Maintenance() {}
 	
 	/**
 	 * Convert the HAC XML representation into a mapping array to reduce
 	 * the cost of scanning the xml tree 
 	 * @param articleId
 	 * @throws Exception
 	 */
 	public void mapXmlHacToClusters(String articleId) throws Exception {
 		
 		DocFactory DocumentFactory = new DocFactory();
 		DocDO document = DocumentFactory.get(articleId);
 		ArrayOfCommentsFactory commentsFactory = new ArrayOfCommentsFactory();
 		ArrayOfCommentsDO arrayOfComments = commentsFactory.get(articleId);
 
 		ArrayList<Cluster> clustersArray = Cluster.makeClustersArray(arrayOfComments.arrayOfComment);
 		
 		ArrayList<MapCell> mapping = new ArrayList<MapCell>();
 		//Initialize necessary elements 
 		Element fatherElement;
 		NodeList childNodes;
 		Element tempChild;
 		Cluster childcluster;
 		Cluster fatherCluster;
 		
 		//Evaluate XPath against Document itself
 		XPath xPath = XPathFactory.newInstance().newXPath();
 		
 		//defining the root
 		Element root = document.doc.getDocumentElement();
 		int currentLevel = getMaxLevel(root);
 		int lengthOfNodeList;
 		
 		///with the root level
 		while(currentLevel > 0) {
 			
 			currentLevel--;
 			//Retrieve the nodes of the level that before the last level
 			NodeList nodesList = (NodeList)xPath.evaluate("//"+XmlElement.ELEMENT_CLUSTER+"[@"+XmlElement.ELEMENT_LEVEL+" = "+currentLevel+" ]",
 					document.doc.getDocumentElement(), XPathConstants.NODESET);
 			lengthOfNodeList = nodesList.getLength();
 			//take every element from the current level and add his children's comments to the mapping
 			for(int i = 0 ; i < lengthOfNodeList ; i++) {
 
 				fatherElement = (Element) nodesList.item(i);
 				fatherCluster = new Cluster(Cluster.findClusterByIdFromArray(clustersArray, fatherElement.getAttribute(XmlElement.ELEMENT_ID)));
 				if(fatherCluster.cluster_id == null)
 					throw new Exception("cluster didnt found");
 				///get the elements children
 				childNodes = fatherElement.getChildNodes();
 
 				//for each child get the inner comments of its matching cluster and add a new entry in mapping
 				for(int j = 0 ; j < childNodes.getLength() ; j++) {
 
 					tempChild = (Element) childNodes.item(j);
 					childcluster  = Cluster.findClusterByIdFromArray(clustersArray, tempChild.getAttribute(XmlElement.ELEMENT_ID));
 					if(childcluster == null)
 						throw new Exception("cluster didnt found");
 					//add the entry to the map for each comment that belongs to the child cluster
 
 					for (Comment comment : childcluster.innerComments) {
 						mapping.add(new MapCell(articleId , comment.comment_id, fatherElement.getAttribute(XmlElement.ELEMENT_ID)+"_"+currentLevel,isDirectSun(comment.comment_id, tempChild)));
 					}
 					//merge the children into the father cluster
 					fatherCluster.mergeWithCluster(childcluster);
 				}	
 				clustersArray = Cluster.removeUnavailableClustersFromArray(clustersArray);
 				clustersArray.add(fatherCluster);
 			}	
 		}
 		DatabaseOperations.setArticleMapping(articleId,mapping);
 	}
 	
 	private int isDirectSun(String id , Element father) {
 		
 		if(father.getAttribute(XmlElement.ELEMENT_ID).equals(id))
 			return 1;
 		return 0;
 		
 	}
 	
 	/**
 	 * Traverse the tree until finding the last level
 	 * @param root the root of the tree
 	 * @return the max level of the tree
 	 */
 	private int getMaxLevel(Element root) {
 		Element last = root;
 		do {
 			 last = (Element) last.getLastChild();
 		}
 		while(last.getLastChild() != null);
 		return Integer.parseInt(last.getAttribute(XmlElement.ELEMENT_LEVEL));
 	}
 	
 	/**
 	 * Receives an Array of comments and add them one by one to the HAC using the maintenance algorithm
 	 * Save the new DOC in the cache 
 	 * Save the new xmlRepresentation in the DB
 	 * Save the mapping array to DB
 	 * save the new comments to DB (save it to cache one by one and finally save it to DB)
 	 * @param neArray
 	 * @param articleId
 	 * @param vector
 	 * @throws Exception
 	 */
 	public void addNewElementsToHAC(ArrayList<CommentEntityDS> neCommentDSArray, String articleId) throws Exception {
 	
 		//docFactory
 		DocFactory documentFactory = new DocFactory();
 		DocDO document = documentFactory.get(articleId);
 		//mapping
 		ArrayList<MapCell> mappingArray = DatabaseOperations.getArticleMapping(articleId);
 		
 		//set the cached comments array into the new cached DO
 		ArrayOfCommentsFactory commentsArrayFactory = new ArrayOfCommentsFactory();
		ArrayOfCommentsDO arrayOfCommentsDO = new ArrayOfCommentsDO(articleId, null);
 		arrayOfCommentsDO = commentsArrayFactory.get(articleId);
		
		//copy the vector
		double[] vector = arrayOfCommentsDO.vect;
		
 		ArrayList<Comment> neArray = Comment.convertCommentsDStoCommentsArrayList(neCommentDSArray);
 		
 		for (Comment ne : neArray) {
 			Comment.nomalizeCommentVector(ne);
 			addNewElementToHAC(ne, articleId, vector, document, mappingArray, arrayOfCommentsDO.arrayOfComment);
 			//save the comments array with the newElement to the cache (to start a new add element with the last added element)
 			arrayOfCommentsDO.arrayOfComment.add(ne);
 			commentsArrayFactory.save(arrayOfCommentsDO);			
 		}
 		
 		DOMImplementationLS domImplementation = (DOMImplementationLS) document.doc.getImplementation();
 		DatabaseOperations.setXmlRepresentation(articleId,domImplementation.createLSSerializer().writeToString(document.doc));
 		DatabaseOperations.setArticleMapping(articleId,mappingArray);
 		documentFactory.save(document);
 		DatabaseOperations.setComments(articleId, neCommentDSArray);
 	}
 	
 	
 	/**
 	 * set the new comment ne into the right place in the HAC
 	 * <br> updates the XML representation
 	 * <br> update the mapping
 	 * @param ne
 	 * @param articleId
 	 * @param vector
 	 * @throws Exception 
 	 */
 	public void addNewElementToHAC(Comment ne, String articleId, double[] vector,DocDO document, ArrayList<MapCell> mappingArray , ArrayList<Comment> arrayOfComments ) throws Exception {
 
 		ArrayList<Cluster> clustersArray = Cluster.makeClustersArray(arrayOfComments);
 		Cluster newElement = new Cluster(ne);
 		Cluster childcluster;
 		Cluster fatherCluster;
 		
 		NodeList childNodes;
 		NodeList nodesList;
 		
 		Element fatherElement;
 		Element tempChild;
 		Element newXMLElement;
 		Element root = document.doc.getDocumentElement();
 		
 		//Evaluate XPath against Document itself
 		XPath xPath = XPathFactory.newInstance().newXPath();
 		int lastLevel = getMaxLevel(root);
 		int maxlevel = lastLevel;
 		double sim = 0;
 		double maxSim = 0;
 		int maxSimIndex = -1;
 		int lengthOfNodeList;
 		
 		while(lastLevel > 0)
 		{
 			lastLevel--;
 			//if the New element isn't merged with none of the levels until the root, merge it to the root
 			if(lastLevel == 0) {
 				fatherElement = root;
 				//add the NE to the XML concatenate it until the last level
 				do{
 					newXMLElement = document.doc.createElement(XmlElement.ELEMENT_CLUSTER);
 					newXMLElement.setAttribute(XmlElement.ELEMENT_ID, newElement.cluster_id);
 					newXMLElement.setAttribute(XmlElement.ELEMENT_LEVEL, ""+ (Integer.parseInt(fatherElement.getAttribute(XmlElement.ELEMENT_LEVEL))+1) );
 					newXMLElement.setAttribute(XmlElement.ELEMENT_MERG_SIM, ""+1);
 					//adding to XML
 					fatherElement.appendChild(newXMLElement); 
 					//adding to mapping array
 					mappingArray.add(new MapCell(articleId, newElement.cluster_id,""+fatherElement.getAttribute(XmlElement.ELEMENT_ID)+"_"+fatherElement.getAttribute(XmlElement.ELEMENT_LEVEL),isDirectSun(newElement.cluster_id, fatherElement)));
 					fatherElement = newXMLElement;
 
 				}
 				while(maxlevel > Integer.parseInt(fatherElement.getAttribute(XmlElement.ELEMENT_LEVEL)));
 				break;
 			}
 
 			//Retrieve the nodes of the level that before the last level (search from the top = root to the requested level)
 			nodesList = (NodeList)xPath.evaluate("//"+XmlElement.ELEMENT_CLUSTER+"[@"+XmlElement.ELEMENT_LEVEL+" = "+lastLevel+" ]",
 					root, XPathConstants.NODESET);
 
 			lengthOfNodeList = nodesList.getLength();
 			double minSim = 2;
 
 			//take every element from the current level and add his children's comments to the mapping
 			for(int i = 0 ; i < lengthOfNodeList ; i++) {
 
 				fatherElement = (Element) nodesList.item(i);
 				//find the maximum similarity of the level
 				if(Double.parseDouble(fatherElement.getAttribute(XmlElement.ELEMENT_MERG_SIM)) < minSim)
 					minSim = Double.parseDouble(fatherElement.getAttribute(XmlElement.ELEMENT_MERG_SIM));
 				fatherCluster = new Cluster(Cluster.findClusterByIdFromArray(clustersArray, fatherElement.getAttribute(XmlElement.ELEMENT_ID)));
 				if(fatherCluster.cluster_id == null)
 					throw new Exception("cluster didnt found");
 				///get the elements children
 				childNodes = fatherElement.getChildNodes();
 
 				//for each child get the inner comments of its matching cluster and add a new entry in mapping
 				for(int j = 0 ; j < childNodes.getLength() ; j++) {
 
 					tempChild = (Element) childNodes.item(j);
 					childcluster  = Cluster.findClusterByIdFromArray(clustersArray, tempChild.getAttribute(XmlElement.ELEMENT_ID));
 					if(childcluster == null)
 						throw new Exception("cluster didnt found");
 					//merge the children into the father cluster
 					fatherCluster.mergeWithCluster(childcluster);
 				}
 
 				clustersArray = Cluster.removeUnavailableClustersFromArray(clustersArray);
 				clustersArray.add(fatherCluster);
 			}
 			sim = 0;
 			maxSim = 0;
 			maxSimIndex = -1;
 			///finding the maximum similarity element 
 			for(int i = 0 ; i < lengthOfNodeList ; i++) {
 				tempChild = (Element) nodesList.item(i);
 				Cluster element = Cluster.findClusterByIdFromArray(clustersArray, tempChild.getAttribute(XmlElement.ELEMENT_ID));
 				sim = element.GAAC(element,newElement, vector);
 				if(sim > maxSim) {
 					maxSim = sim;
 					maxSimIndex = i;
 				}	
 			}
 
 			//adding new element as sun of the max sim element
 			if((maxSim > minSim) && maxSimIndex != -1) {
 				tempChild = (Element) nodesList.item(maxSimIndex);
 				//replacing the similarity of the root to be the similarity with the new elements
 				tempChild.removeAttribute(XmlElement.ELEMENT_MERG_SIM);
 				tempChild.setAttribute(XmlElement.ELEMENT_MERG_SIM, ""+maxSim);
 				fatherElement = tempChild;
 				//add the NE to the XML concatenate it until the last level
 				do{
 					newXMLElement = document.doc.createElement(XmlElement.ELEMENT_CLUSTER);
 					newXMLElement.setAttribute(XmlElement.ELEMENT_ID, newElement.cluster_id);
 					newXMLElement.setAttribute(XmlElement.ELEMENT_LEVEL, ""+ (Integer.parseInt(fatherElement.getAttribute(XmlElement.ELEMENT_LEVEL))+1) );
 					newXMLElement.setAttribute(XmlElement.ELEMENT_MERG_SIM, ""+1);
 					//adding to XML
 					fatherElement.appendChild(newXMLElement); 
 					//adding to mapping array
 					mappingArray.add(new MapCell(articleId, newElement.cluster_id,""+fatherElement.getAttribute(XmlElement.ELEMENT_ID)+"_"+fatherElement.getAttribute(XmlElement.ELEMENT_LEVEL),isDirectSun(newElement.cluster_id, fatherElement)));
 					fatherElement = newXMLElement;
 				}
 				while(maxlevel > Integer.parseInt(fatherElement.getAttribute(XmlElement.ELEMENT_LEVEL))); 
 				lastLevel = 0;
 			}
 		}
 			
 		// write the content into xml file
 		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 		Transformer transformer = transformerFactory.newTransformer();
 		DOMSource source = new DOMSource(document.doc);
 		StreamResult result = new StreamResult(new File("C:\\file.xml"));
 		transformer.transform(source, result);
 
 		//TODO: Check why if i'm adding a new element the father of the element after running the algorithm is getting wrong merge sim number
 	}
 
 	
 }
