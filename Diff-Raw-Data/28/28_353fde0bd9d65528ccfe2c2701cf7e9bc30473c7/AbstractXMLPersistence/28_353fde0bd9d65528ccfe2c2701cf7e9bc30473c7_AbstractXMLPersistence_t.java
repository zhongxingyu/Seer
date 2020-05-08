 package edu.uci.lighthouse.model.io;
 
 import java.text.ParseException;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Element;
 
 import edu.uci.lighthouse.model.LighthouseAuthor;
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseExternalClass;
 import edu.uci.lighthouse.model.LighthouseField;
 import edu.uci.lighthouse.model.LighthouseInterface;
 import edu.uci.lighthouse.model.LighthouseMethod;
 import edu.uci.lighthouse.model.LighthouseModifier;
 import edu.uci.lighthouse.model.LighthouseRelationship;
import edu.uci.lighthouse.model.util.LHStringUtil;
 
 public class AbstractXMLPersistence {
 
 	private static Logger logger = Logger.getLogger(AbstractXMLPersistence.class);
 	
 	protected void writeEvent(LighthouseEvent event, Element root) {
 		Element node = root.addElement("event");		
 		node.addAttribute("type", event.getType().toString());
 		writeAuthor(event.getAuthor(),node);
 		Object artifact = event.getArtifact();
 		if (artifact  instanceof LighthouseEntity) {
 			LighthouseEntity entity = (LighthouseEntity) artifact;
 			writeEntity(entity,node);
 		} else if (artifact instanceof LighthouseRelationship) {
 			LighthouseRelationship rel = (LighthouseRelationship) artifact;
 			writeRelationship(rel,node);
 		}
 		if (event.getTimestamp()!=null) {
 			node.addElement("timestamp").addText(event.getTimestamp().toString());	
		}
		node.addElement("isCommitted").addText(new Boolean(event.isCommitted()).toString());
		node.addElement("committedTime").addText(event.getCommittedTime().toString());
 	}
 	
 	protected void writeAuthor(LighthouseAuthor author, Element root) {
 		root.addElement("author").addText(author.getName());
 	}
 	
 	protected void writeEntity(LighthouseEntity entity, Element root) {
 		Element node = root.addElement("entity");
 		node.addAttribute("type", entity.getClass().getSimpleName());
 		node.addElement("fqn").addText(entity.getFullyQualifiedName());
 	}
 	
 	protected void writeRelationship(LighthouseRelationship relationship, Element root) {
 		Element node = root.addElement("relationship");
 		node.addAttribute("type", relationship.getType().toString());
 		writeEntity(relationship.getFromEntity(), node.addElement("from"));
 		writeEntity(relationship.getToEntity(), node.addElement("to"));
 	}
 	
 	
 	//
 	protected LighthouseEvent readEvent(Element root) {
 		LighthouseEvent event = null;
 		String type = root.attributeValue("type");
 		LighthouseAuthor author = readAuthor(root.element("author"));
 		Element eleEntity = root.element("entity");
 		if (eleEntity != null) {
 			LighthouseEntity entity = readEntity(eleEntity);
 			event = new LighthouseEvent(LighthouseEvent.TYPE.valueOf(type), author, entity);
 		} else {
 			Element eleRel = root.element("relationship");
 			if (eleRel != null) {
 				LighthouseRelationship rel = readRelationship(eleRel);
 				 event = new LighthouseEvent(LighthouseEvent.TYPE.valueOf(type), author, rel);
 			}
 		}
 		String strTimestamp = root.elementText("timestamp");
 		Date timestamp = null;
 		if (strTimestamp==null) {
 			timestamp = new Date(); 
 		} else {			
 			try {
				timestamp = LHStringUtil.simpleDateFormat.parse(strTimestamp);
 			} catch (ParseException e) {
 				logger.warn("Trying to parse the Timestamp: " + timestamp);
 			}
 		}
 		event.setTimestamp(timestamp);
		String strIsCommitted = root.elementText("isCommitted");
		if (strIsCommitted!=null) {
			event.setCommitted(new Boolean(strIsCommitted).booleanValue());
		}
		String strCommittedTime = root.elementText("committedTime");
		if (strCommittedTime!=null) {
			try {
				event.setCommittedTime(LHStringUtil.simpleDateFormat.parse(strCommittedTime));
			} catch (ParseException e) {
				logger.warn("Trying to parse the CommittedTime: " + strCommittedTime);
			}
		}
 		return event; 
 	}
 	
 	protected LighthouseAuthor readAuthor(Element root) {
 		return new LighthouseAuthor(root.getText());
 	}
 	
 	protected LighthouseEntity readEntity(Element root) {
 		String fqn = root.elementText("fqn");
 		LighthouseEntity entity = null;
 		String type = root.attributeValue("type");
 		if (type.equals(LighthouseClass.class.getSimpleName())) {
 			entity = new LighthouseClass(fqn);
 		} else if (type.equals(LighthouseExternalClass.class.getSimpleName())) {
 			entity = new LighthouseExternalClass(fqn);
 		} else if (type.equals(LighthouseField.class.getSimpleName())) {
 			entity = new LighthouseField(fqn);
 		} else if (type.equals(LighthouseMethod.class.getSimpleName())) {
 			entity = new LighthouseMethod(fqn);
 		} else if (type.equals(LighthouseInterface.class.getSimpleName())) {
 			entity = new LighthouseInterface(fqn);
 		} else if (type.equals(LighthouseModifier.class.getSimpleName())) {
 			entity = new LighthouseModifier(fqn);
 		}
 		return entity;
 	}
 	
 	protected LighthouseRelationship readRelationship(Element root) {
 		LighthouseRelationship.TYPE type = LighthouseRelationship.TYPE
 				.valueOf(root.attributeValue("type"));
 		LighthouseEntity from = readEntity(root.element("from").element(
 				"entity"));
 		LighthouseEntity to = readEntity(root.element("to").element(
 				"entity"));
 		LighthouseRelationship relationship = new LighthouseRelationship(from,
 				to, type);
 		return relationship;
 	}
 	
 }
