 package org.iucn.sis.shared.api.models.parsers;
 
 import java.util.HashSet;
 
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldFactory;
 
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 
 public class FieldV2Parser {
 
 	public static Field parse(final NativeElement element) {
 		String id = element.getAttribute("id");
 		String name = element.getNodeName();
 		
 		Field field = new Field();
 		field.setName(name);
 		field.setFields(new HashSet<Field>());
 		field.setPrimitiveField(new HashSet<PrimitiveField>());
 		field.setNotes(new HashSet<Notes>());
 		field.setReference(new HashSet<Reference>());
 		
 		try {
 			field.setId(Integer.valueOf(id).intValue());
 		} catch (NumberFormatException e) {
 			Debug.println("ERROR - FIELD " + name + " DOES NOT HAVE AN ID!!! trying to parse " + id + " with name " + name);
 		}
 		
 		final NativeNodeList children = element.getChildNodes();
 		for (int i = 0; i < children.getLength(); i++) {
 			NativeNode current = children.item(i);
 			if ("subfields".equals(current.getNodeName())) {
 				NativeNodeList subfields = current.getChildNodes();
 				for (int k = 0; k < subfields.getLength(); k++) {
 					NativeNode subfield = subfields.item(k);
 					Field cur;
 					try {
 						cur = Field.fromXML((NativeElement)subfield);
 					} catch (ClassCastException e) {
 						continue;
 					} catch (Throwable e) {
 						e.printStackTrace();
 						continue;
 					}
 					
 					cur.setParent(field);
 					field.getFields().add(cur);
 				}
 			}
			else if (Notes.ROOT_TAG.equals(current.getNodeName()) && !hasTypeAttribute(current)) {
 				Notes cur = Notes.fromXML((NativeElement)current);
 				cur.setField(field);
 				
 				field.getNotes().add(cur);
 			}
			else if (Reference.ROOT_TAG.equals(current.getNodeName()) && !hasTypeAttribute(current)) {
 				Reference cur = Reference.fromXML((NativeElement)current);
 				if (cur.getField() == null)
 					cur.setField(new HashSet<Field>());
 				cur.getField().add(field);
 				
 				field.getReference().add(cur);
 			}
 			else if (current instanceof NativeElement) {
 				NativeElement el = (NativeElement)current;
 				String type = el.getAttribute("type");
 				if (type != null && type.endsWith("PrimitiveField")) {
 					PrimitiveField cur = PrimitiveFieldFactory.generatePrimitiveField(type);
 					cur.fromXML((NativeElement)current);
 					cur.setField(field);
 					field.getPrimitiveField().add(cur);
 				}
 			}
 		}
 		
 		return field;
 	}
 	
	private static boolean hasTypeAttribute(NativeNode node) {
		return getAttribute(node, "type") != null;
	}
	
	private static String getAttribute(NativeNode node, String attribute) {
		if (!(node instanceof NativeElement))
			return null;
		
		return ((NativeElement)node).getAttribute(attribute);
	}
 }
