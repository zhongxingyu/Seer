 package com.o2xml.core;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import static com.o2xml.core.EngineUtils.*;
 
 import com.o2xml.ano.XMLNode;
 import com.o2xml.ano.XMLRoot;
 import com.o2xml.core.exception.EngineException;
 import com.o2xml.xml.XmlBuilder;
 import com.o2xml.xml.XmlBuilderFactory;
 import com.o2xml.xml.XmlElement;
 
 public class Object2XmlEngine {
 
 	public static void transformObject2XML(Object o) throws EngineException{
 
 		final String root = getRootDocument(o);
 		final List<NodeItem> l = getNodeInformations(o);
 
 		XmlBuilder dxb = XmlBuilderFactory.createXmlBuilder(root);
 		
 		for (NodeItem ni : l) {
 			String nodeParent = root;
 			String n = ni.getParentNode();
 			
 			if(n!= null){
 				nodeParent=n;
 			}
 			XmlElement parent = dxb.findElementById(nodeParent);
 			if(!parent.isValidXmlElement()){
 				parent = dxb.findElementById(root).addChild(nodeParent, null);
 			}
 			parent.addChild(ni.getNodeName(), ni.getValue());
 		}
 
 		System.out.println(dxb.getXml());
 	}
 
 	private static List<NodeItem> getNodeInformations(Object o)
 			throws EngineException {
 		List<NodeItem> nodeItems = new ArrayList<NodeItem>();
 
 		try {
 			Method[] m = o.getClass().getMethods();
 			for (Method me : m) {
 
 				Annotation an = getXmlNodeAnnotationFromMethod(me);
 				if (an != null) {
 
 					nodeItems.addAll(methodTreatment(o, me, an));
 
 				}
 
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return nodeItems;
 	}
 
 	
 
 	private static String getRootDocument(Object o) {
 		XMLRoot xr = o.getClass().getAnnotation(XMLRoot.class);
 		String racineValue = null;
 		if (xr != null) {
 			racineValue = xr.rootName();
 		} else {
 			racineValue = o.getClass().getSimpleName();
 		}
 
 		return racineValue;
 	}
 
 	private static Annotation getXmlNodeAnnotationFromMethod(Method m) {
 
 		Annotation[] a = m.getAnnotations();
 		for (Annotation an : a) {
 			if (an.annotationType().equals(XMLNode.class)) {
 				return an;
 			}
 		}
 
 		return null;
 
 	}
 
 	private static List<NodeItem> methodTreatment(Object o, Method m,
 			Annotation an) throws EngineException {
 		Object returnObject = null;
 
 		List<NodeItem> nodeItems = new ArrayList<NodeItem>();
 
 		try {
 			returnObject = m.invoke(o, new Object[0]);
			if (EngineUtils.isArrayType(returnObject)) {fabrice
 				String nodeParent = extractNodeNameFromMethodSignature(m,an);
 				for (Object currentobject : (Object[]) returnObject) {
 					nodeItems.add(NodeItem.buildNodeItem(compileValue(currentobject,an), an,nodeParent));
 				}
 			} else if (isCollectionType(returnObject)) {
 				String nodeParent = extractNodeNameFromMethodSignature(m,an);
 				for (Object currentobject : (Collection<?>) returnObject) {
 					nodeItems.add(NodeItem.buildNodeItem(compileValue(currentobject,an), an,nodeParent));
 				} 
 			} else {
 				String nodeParent = ((XMLNode)an).nodeParent();
 				nodeItems.add(NodeItem.buildNodeItem(compileValue(returnObject,an), an,(nodeParent.isEmpty()?null:nodeParent)));
 			}
 
 			return nodeItems;
 
 		} catch (Exception e) {
 			throw new EngineException(
 					"Exception throws during method theatment : "
 							+ m, e);
 		}
 	}
 
 }
 
 class NodeItem {
 
 	private String nodeName = null;
 	private String xpathDef = null;
 	private String value = null;
 	private String parentNode = null;
 
 	private NodeItem(String _nodeName, String _xpathDef, String _value) {
 		this.nodeName = _nodeName;
 		this.xpathDef = _xpathDef;
 		this.value = _value;
 	}
 
 	private NodeItem(String _nodeName, String _xpathDef, String _value, String _nodeParent) {
 		this.nodeName = _nodeName;
 		this.xpathDef = _xpathDef;
 		this.value = _value;
 		this.parentNode=_nodeParent;
 	}
 	
 	protected static NodeItem buildNodeItem(String value, Annotation an, String nodeParent) {
 		return new NodeItem(((XMLNode) an).name(), ((XMLNode) an).xpath(),value,nodeParent);
 
 	}
 	protected static NodeItem buildNodeItem(String value, Annotation an) {
 		return new NodeItem(((XMLNode) an).name(), ((XMLNode) an).xpath(),value,null);
 
 	}
 	
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public String getNodeName() {
 		return nodeName;
 	}
 
 	public void setNodeName(String nodeName) {
 		this.nodeName = nodeName;
 	}
 
 	public String getXpathDef() {
 		return xpathDef;
 	}
 
 	public void setXpathDef(String xpathDef) {
 		this.xpathDef = xpathDef;
 	}
 
 	public String getParentNode() {
 		return parentNode;
 	}
 
 	public void setParentNode(String parentNode) {
 		this.parentNode = parentNode;
 	}
 	
 }
