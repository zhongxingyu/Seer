 package org.iucn.sis.shared.api.models;
 
 /**
  * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
  * 
  * This is an automatic generated file. It will be regenerated every time 
  * you generate persistence class.
  * 
  * Modifying its content may cause the program not work, or your work may lost.
  */
 
 /**
  * Licensee: 
  * License Type: Evaluation
  */
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.interfaces.ForeignObject;
 
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.portable.XMLWritingUtils;
 
 @SuppressWarnings("serial")
 public class Notes implements Serializable, ForeignObject<Notes> {
 
 	public static String ROOT_TAG = "note";
 
 	public static Notes fromXML(NativeElement current) {
 		final Notes note = new Notes();
 		try {
 			note.setId(Integer.valueOf(current.getAttribute("id")));
 		} catch (NullPointerException e) {
 			Debug.println(e);
 		} catch (NumberFormatException e) {
 			Debug.println(e);
 		}
 		
 		final NativeNodeList children = current.getChildNodes();
 		for (int i = 0; i < children.getLength(); i++) {
 			final NativeNode child = children.item(i);
 			if ("value".equals(child.getNodeName()))
 				note.setValue(child.getTextContent());
 			else if ("offlineStatus".equals(child.getNodeName()))
 				note.setOfflineStatus(Boolean.valueOf(child.getTextContent()));
 			else if (Edit.ROOT_TAG.equals(child.getNodeName()))
 				note.getEdits().add(Edit.fromXML((NativeElement)child));	
 		}
 		
 		return note;
 	}
 
 	public static List<Notes> notesFromXML(NativeElement element) {
 		List<Notes> notes = new ArrayList<Notes>();
 		NativeNodeList list = element.getElementsByTagName(Notes.ROOT_TAG);
 		for (int i = 0; i < list.getLength(); i++) {
 			notes.add(Notes.fromXML(list.elementAt(i)));
 		}
 		return notes;
 	}
 
 	private int id;
 	private String value;
 	private boolean offlineStatus;
 	private Set<Synonym> synonyms;
 	private Set<CommonName> commonNames;
 	private Set<Taxon> taxa;
 	private Set<Edit> edits;
 	private Set<Field> fields;
 	
 	
 	public Notes() {
 		synonyms = new HashSet<Synonym>();
 		commonNames = new HashSet<CommonName>();
 		taxa = new HashSet<Taxon>();
 		edits = new HashSet<Edit>();
 		fields = new HashSet<Field>();
 	}
 	
 	public Notes deepCopy() {
 		Notes note = new Notes();
 		note.setId(getId());
 		note.setValue(getValue());
		note.setEdit(getEdit().deepCopy());
 		return note;
 	}
 	
 	public CommonName getCommonName() {
 		return commonNames.iterator().hasNext() ? commonNames.iterator().next() : null;
 	}
 
 	public Set<CommonName> getCommonNames() {
 		return commonNames;
 	}
 
 	public Edit getEdit() {
 		return edits.iterator().hasNext() ? edits.iterator().next() : null;
 	}
 
 	public Set<Edit> getEdits() {
 		return edits;
 	}
 
 	public Field getField() {
 		return fields.iterator().hasNext() ? fields.iterator().next() : null;
 	}
 
 	public Set<Field> getFields() {
 		return fields;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public int getORMID() {
 		return getId();
 	}
 	
 	@Override
 	public boolean getOfflineStatus() {
 		return offlineStatus;
 	}
 	
 	@Override
 	public Notes getOfflineCopy() {
 		Notes copy = deepCopy();
 		copy.setId(0);
 		copy.getEdit().setId(0);
 		
 		return copy;
 	}
 
 	public Synonym getSynonym() {
 		return synonyms.iterator().hasNext() ? synonyms.iterator().next() : null;
 	}
 
 	public Set<Synonym> getSynonyms() {
 		return synonyms;
 	}
 
 	public Set<Taxon> getTaxa() {
 		return taxa;
 	}
 
 	public Taxon getTaxon() {
 		return taxa.iterator().hasNext() ? taxa.iterator().next() : null;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setCommonName(CommonName value) {
 		commonNames.clear();
 		commonNames.add(value);
 	}
 
 	public void setCommonNames(Set<CommonName> commonNames) {
 		this.commonNames = commonNames;
 	}
 
 	public void setEdit(Edit value) {
 		edits.clear();
 		edits.add(value);
 	}
 
 	public void setEdits(Set<Edit> edits) {
 		this.edits = edits;
 	}
 
 	public void setField(Field value) {
 		this.fields.clear();
 		fields.add(value);
 	}
 
 	public void setFields(Set<Field> fields) {
 		this.fields = fields;
 	}
 
 	public void setId(int value) {
 		this.id = value;
 	}
 	
 	@Override
 	public void setOfflineStatus(boolean offlineStatus) {
 		this.offlineStatus = offlineStatus;
 	}
 
 	public void setSynonym(Synonym value) {
 		synonyms.clear();
 		synonyms.add(value);
 	}
 
 	public void setSynonyms(Set<Synonym> synonyms) {
 		this.synonyms = synonyms;
 	}
 
 	public void setTaxa(Set<Taxon> taxa) {
 		this.taxa = taxa;
 	}
 
 	public void setTaxon(Taxon value) {
 		taxa.clear();
 		taxa.add(value);
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public String toString() {
 		return String.valueOf(getId());
 	}
 
 	public String toXML() {
 		StringBuilder xml =  new StringBuilder("<" + ROOT_TAG + " id=\"" + getId() + "\">");
 		xml.append(XMLWritingUtils.writeCDATATag("value", getValue()));
 		xml.append(XMLWritingUtils.writeTag("offlineStatus", Boolean.toString(getOfflineStatus())));
 		for (Edit edit : getEdits())
 			xml.append(edit.toXML());
 		xml.append("</" + ROOT_TAG + ">");
 		return xml.toString();
 	}
 
 }
