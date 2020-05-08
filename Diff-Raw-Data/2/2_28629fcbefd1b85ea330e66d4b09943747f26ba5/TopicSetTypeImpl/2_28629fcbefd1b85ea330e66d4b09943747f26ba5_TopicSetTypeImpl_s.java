 package com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import org.w3c.dom.Element;
 
 import com.ebmwebsourcing.wsstar.topics.datatypes.api.abstraction.TopicSetType;
 import com.ebmwebsourcing.wsstar.topics.datatypes.impl.WstopJAXBContext;
 
 public class TopicSetTypeImpl implements TopicSetType {
 	
 	private com.ebmwebsourcing.wsstar.jaxb.notification.topics.TopicSetType jaxbTypeObj;
 //	private static Logger logger  = Logger.getLogger(TopicSetTypeImpl.class.getSimpleName());
 	
 	/**
 	 * Default constructor
 	 */
 	protected TopicSetTypeImpl() {		
 		this.jaxbTypeObj = WstopJAXBContext.WSTOP_JAXB_FACTORY.createTopicSetType();		
 	}
 
 	public TopicSetTypeImpl(com.ebmwebsourcing.wsstar.jaxb.notification.topics.TopicSetType jaxbTypeObj){
 		this.jaxbTypeObj = jaxbTypeObj;
 	}
 	
 	protected final com.ebmwebsourcing.wsstar.jaxb.notification.topics.TopicSetType getJaxbTypeObj() {
 		return jaxbTypeObj;
 	}
 	
 	@Override
 	public List<Element> getTopicsTrees() {
 		List<Element> listToReturn = new ArrayList<Element>();
 	
 		List<Object> objFromModel = this.jaxbTypeObj.getAny();
 		for (Object item : objFromModel) {
 			if (item instanceof Element){
 				listToReturn.add((Element)item);
 			}
 		}		
 		return listToReturn;
 	}
 	
 	@Override
 	public void addTopicsTree(Element topicTree) {
 		List<Object> objFromModel = this.jaxbTypeObj.getAny();
 		int index = -1;
 		QName rootTopicToAddQName = null;
 		if(topicTree.getNamespaceURI() != null) {
 			rootTopicToAddQName = new QName(topicTree.getNamespaceURI(),topicTree.getLocalName());
 		} else {
 			rootTopicToAddQName = new QName(topicTree.getNodeName());
 		}
 		Element currentRootTopic = null;
 		for (Object item : objFromModel) {
 			if (item instanceof Element){
 				currentRootTopic = (Element)item;
 				if (currentRootTopic != null && currentRootTopic.getNamespaceURI() != null && currentRootTopic.getNamespaceURI().equals(rootTopicToAddQName.getNamespaceURI()) &&
						currentRootTopic.getLocalName().equals(currentRootTopic.getLocalName())){
 					index = objFromModel.indexOf(item);
 					break;
 				}				
 			}
 		}
 		if (index >=0 ){
 			objFromModel.remove(index);			
 		}		
 		objFromModel.add(topicTree);		
 	}
 
 
 	
 	
 	/**
 	 * A way to create a  {@link com.ebmwebsourcing.wsstar.notification.topics.TopicSetType}
 	 *  "Jaxb model type" object from a {@link TopicSetType} "api type" one  
 	 *    
 	 * @param apiTypeObj
 	 */
 	public static com.ebmwebsourcing.wsstar.jaxb.notification.topics.TopicSetType toJaxbModel(TopicSetType apiTypeObj) {
 
 		com.ebmwebsourcing.wsstar.jaxb.notification.topics.TopicSetType jaxbTypeObj = null;
 		
 		if (apiTypeObj instanceof TopicSetTypeImpl){
 			jaxbTypeObj = ((TopicSetTypeImpl)apiTypeObj).getJaxbTypeObj();
 		} else { 
 			jaxbTypeObj = WstopJAXBContext.WSTOP_JAXB_FACTORY.createTopicSetType();
 		
 			// ----- /!\ Must be changed to use right Type ! ------
 			jaxbTypeObj.getAny().addAll(apiTypeObj.getTopicsTrees());
 		}
 
 		return jaxbTypeObj;
 	}
 }
