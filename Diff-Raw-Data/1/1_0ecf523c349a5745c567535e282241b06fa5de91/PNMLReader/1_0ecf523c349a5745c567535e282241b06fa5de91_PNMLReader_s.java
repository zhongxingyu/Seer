 package de.freiburg.uni.iig.sisi.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import de.freiburg.uni.iig.sisi.model.ProcessModel;
 import de.freiburg.uni.iig.sisi.model.net.Arc;
 import de.freiburg.uni.iig.sisi.model.net.Place;
 import de.freiburg.uni.iig.sisi.model.net.Transition;
 import de.freiburg.uni.iig.sisi.model.resource.Role;
 import de.freiburg.uni.iig.sisi.model.resource.Subject;
 import de.freiburg.uni.iig.sisi.model.resource.WorkObject;
 import de.freiburg.uni.iig.sisi.model.safetyrequirements.Policy;
 import de.freiburg.uni.iig.sisi.model.safetyrequirements.UsageControl;
 
 public class PNMLReader {
 
 	ProcessModel sm;
 
 	public ProcessModel createModelFromPNML(ProcessModel sm, String uri) throws ParserConfigurationException, SAXException,
 			IOException {
 		this.sm = sm;
 
 		File fXmlFile = new File(uri);
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 		Document doc = dBuilder.parse(fXmlFile);
 		doc.getDocumentElement().normalize();
 
 		// check for correct type
 		if (!((Element) doc.getElementsByTagName("pnml").item(0)).getAttribute("type").equals("de.freiburg.uni.iig.sisi"))
 			throw new IOException("Can not read. Wrong PNML type. Can only read 'de.freiburg.uni.iig.sisi'.");
 
 		setNetAttributes((Element) doc.getElementsByTagName("net").item(0));
 
 		// set up control flow
 		setPlaces(doc.getElementsByTagName("place"));
 		setTransitions(doc.getElementsByTagName("transition"));
 		setArcs(doc.getElementsByTagName("arc"));
 
 		// set up resource model
 		setRoles(doc.getElementsByTagName("role"));
 		setSubjects(doc.getElementsByTagName("subject"));
 		setWorkObjects(doc.getElementsByTagName("object"));
 
 		// set up safety requirements
 		setDelegations(doc.getElementsByTagName("delegation"));
 		setPolicies(doc.getElementsByTagName("policy"));
 		setUsageControl(doc.getElementsByTagName("usageControl"));
 
 		return sm;
 	}
 
 	private void setUsageControl(NodeList nodeList) {
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Element e = (Element) nodeList.item(i);
 			UsageControl usageControl = new UsageControl(e.getAttribute("id"), "", e.getAttribute("type"), (Transition) sm.getNet().getNode(
 					e.getAttribute("objective")), (Transition) sm.getNet().getNode(e.getAttribute("eventually")));
 			sm.getSafetyRequirements().addUsageControl(usageControl);
 		}
 	}
 
 	private void setPolicies(NodeList nodeList) {
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Element e = (Element) nodeList.item(i);
 			Policy policy = new Policy(e.getAttribute("id"), "", e.getAttribute("type"), (Transition) sm.getNet().getNode(
 					e.getAttribute("objective")), (Transition) sm.getNet().getNode(e.getAttribute("eventually")));
 			sm.getSafetyRequirements().addPolicy(policy);
 		}
 	}
 
 	private void setDelegations(NodeList nodeList) {
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Element e = (Element) nodeList.item(i);
 			sm.getSafetyRequirements().addDelegation((Transition) sm.getNet().getNode(e.getAttribute("transRef")),
 					sm.getResourceModel().getRole(e.getAttribute("roleRef")));
 		}
 	}
 
 	private void setWorkObjects(NodeList nodeList) {
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Element e = (Element) nodeList.item(i);
 			NodeList usedByList = e.getElementsByTagName("usedBy");
 			HashSet<Transition> usedBySet = new HashSet<Transition>();
 			for (int j = 0; j < usedByList.getLength(); j++) {
 				Element usedBy = (Element) usedByList.item(j);
 				usedBySet.add((Transition) sm.getNet().getNode(usedBy.getAttribute("transRef")));
 			}
 			WorkObject workObject = new WorkObject(e.getAttribute("id"), e.getElementsByTagName("name").item(0).getTextContent().trim(),
 					usedBySet);
 			sm.getResourceModel().addWorkObject(workObject);
 		}
 	}
 
 	private void setSubjects(NodeList nodeList) {
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Element e = (Element) nodeList.item(i);
 			Subject subject = new Subject(e.getAttribute("id"), e.getElementsByTagName("name").item(0).getTextContent().trim());
 			NodeList assignments = e.getElementsByTagName("assigned");
 			for (int j = 0; j < assignments.getLength(); j++) {
 				Element role = (Element) assignments.item(j);
 				subject.addRole(sm.getResourceModel().getRole(role.getAttribute("roleRef")));
 				sm.getResourceModel().getRole(role.getAttribute("roleRef")).addMember(subject);
 			}
 		}
 	}
 
 	private void setRoles(NodeList nodeList) {
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Element e = (Element) nodeList.item(i);
 			Role role = new Role(e.getAttribute("id"), e.getElementsByTagName("name").item(0).getTextContent().trim());
 			sm.getResourceModel().addRole(role);
 			NodeList domains = e.getElementsByTagName("domain");
 			for (int j = 0; j < domains.getLength(); j++) {
 				Element domain = (Element) domains.item(j);
 				sm.getResourceModel().getRole(e.getAttribute("id"))
 						.addDomain((Transition) sm.getNet().getNode(domain.getAttribute("transRef")));
 			}
 		}
 	}
 
 	private void setNetAttributes(Element e) {
 		sm.getNet().setId(e.getAttribute("id"));
 		NodeList nodeList = e.getChildNodes();
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node childNode = nodeList.item(i);
 			if ((childNode.getNodeType() == 1) && (childNode.getNodeName().equals("name"))) {
 				sm.getNet().setName(childNode.getTextContent().trim());
 				break;
 			}
 		}
 	}
 
 	private void setPlaces(NodeList nodeList) {
 		HashMap<Place, Integer> initialMarking = new HashMap<Place, Integer>();
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Element node = (Element) nodeList.item(i);
 			int marking = 0;
 			if (node.getElementsByTagName("initialMarking").getLength() != 0)
 				marking = Integer.valueOf(node.getElementsByTagName("initialMarking").item(0).getTextContent().trim());
 			Place place = new Place(node.getAttribute("id"), node.getElementsByTagName("name").item(0).getTextContent().trim(), marking);
 			initialMarking.put(place, marking);
 			sm.getNet().addPlace(place);
 		}
 		sm.getNet().setInitialMarking(initialMarking);
 	}
 
 	private void setTransitions(NodeList nodeList) {
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Element node = (Element) nodeList.item(i);
 			Transition transition = new Transition(node.getAttribute("id"), node.getElementsByTagName("name").item(0).getTextContent()
 					.trim());
 			sm.getNet().addTransition(transition);
 		}
 	}
 
 	private void setArcs(NodeList nodeList) {
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Element node = (Element) nodeList.item(i);
 			Arc arc = new Arc(node.getAttribute("id"), sm.getNet().getNode(node.getAttribute("source")), sm.getNet().getNode(
 					node.getAttribute("target")));
 			sm.getNet().addArc(arc);
 			sm.getNet().getNode(node.getAttribute("source")).addOutgoingArc(arc);
 			sm.getNet().getNode(node.getAttribute("target")).addIncomingArc(arc);
 		}
 	}
 
 }
