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
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 
 import org.iucn.sis.shared.api.models.parsers.FieldV1Parser;
 import org.iucn.sis.shared.api.models.parsers.FieldV2Parser;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 
 import com.solertium.lwxml.shared.NativeElement;
 
 public class Field implements Serializable {
 
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 	//public static final String ROOT_TAG = "field";
 	
 	private static final String VERSION = "2";
 
 	public static Field fromXML(NativeElement element) {
 		String version = element.getAttribute("version");
 		if (VERSION.equals(version))
 			return FieldV2Parser.parse(element); 
 		else
 			return FieldV1Parser.parse(element);
 	}
 
 	private int id;
 
 	private Assessment assessment;
 
 	private String name;
 	
 	private Field parent;
 	
 	private java.util.Set<Notes> notes;	
 	
 	private java.util.Set<Field> fields;
 	
 	private java.util.Set<Reference> reference;
 
 	private java.util.Set<PrimitiveField> primitiveField;
 	
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 
 	public Field() {
 		notes = new java.util.HashSet<Notes>();
 		fields = new java.util.HashSet<Field>();
 		reference = new java.util.HashSet<Reference>();
 		primitiveField = new java.util.HashSet<PrimitiveField>();
 	}
 
 	public Field(String canonicalName, Assessment assessment) {
 		this();
 		this.name = canonicalName;
 		this.assessment = assessment;
 	}
 
 	/**
 	 * 
 	 *  Copies all information from this field into the 
 	 * parameter field.  
 	 * 
 	 * @param field -- all information is copied into this
 	 * @param append -- if information should be appended if field is a narrative field
 	 * @param overwrite -- if data is not null, should it be overwritten
 	 * @return boolean if data has been changed in field
 	 */
 	public boolean copyInto(Field field, boolean append, boolean overwrite) {
 		
 		//QUIT IF THE FIELDS ARE EQUAL IF DATA IS THERE AND WE ARE NOT APPENDING AND NOT OVERWRITING
 		if (this.equals(field) || (field != null && !overwrite && !field.isNarrativeField()) || (field != null && field.isNarrativeField() && (!append || !overwrite)))
 			return false;
 			
 		field.setName(getName());
 		if (overwrite) {
 			field.setFields(new HashSet<Field>());
 			field.setPrimitiveField(new HashSet<PrimitiveField>());
 		}
 		
 		Map<String,PrimitiveField> keyToPF = field.getKeyToPrimitiveFields();
 		for (PrimitiveField pf : getPrimitiveField()) {			
 			if (keyToPF.containsKey(pf.getName())) {
 				if (overwrite) {
 					pf.copyInto(keyToPF.get(pf.getName()));
 				} else if (append && field.isNarrativeField()) {
 					keyToPF.get(pf.getName()).appendValue(pf.getValue());
 				}
 			} else {
 				field.getPrimitiveField().add(pf.deepCopy());
 			}
 		}
 		
 		
 		Map<String, Field> keyToField = field.getKeyToFields();
 		for (Field f : getFields()) {
 			if (!keyToField.containsKey(f.getName())) {
 				field.getFields().add(f.deepCopy(false));
 			} 
 		}
 		return true;
 	}
 
 	/**
 	 * Returns a deep copy of all things associated with the field.  
 	 * IDs are not copied over
 	 * 
 	 * ONLY START AT ROOT
 	 * 
 	 * @return
 	 */
 	public Field deepCopy(boolean copyReferenceToAssessment) {
 		return deepCopy(copyReferenceToAssessment, false);
 	}
 	
 	public Field deepCopy(boolean copyReferenceToAssessment, boolean copyFieldReferences) {
 		Field field = new Field(getName(), null);
 		
 		if (copyReferenceToAssessment && this.getAssessment() != null) {
 			field.assessment = assessment.deepCopy();
 			field.assessment.setId(assessment.getId());
 		}
 		
 		if (this.getNotes() != null) {
 			field.setNotes(new HashSet<Notes>());
 			for (Notes note : getNotes()) {
 				Notes copy = note.deepCopy();
 				copy.getFields().add(field);
 				field.getNotes().add(copy);
 			}
 		}
 		
 		if (copyFieldReferences && this.getReference() != null) {
 			field.setReference(new HashSet<Reference>());
 			for (Reference reference : getReference()) {
 				Reference copy = reference.deepCopy();
 				copy.getField().add(field);
 				field.getReference().add(copy);
 			}
 		}
 		
 		if (this.getPrimitiveField() != null) {
 			field.setPrimitiveField(new HashSet<PrimitiveField>());
 			for (PrimitiveField pf : getPrimitiveField()) {
 				PrimitiveField copy = pf.deepCopy(false);
 				copy.setField(field);
 				field.getPrimitiveField().add(copy);
 			}
 		}
 		
 		if (this.getFields() != null) {
 			field.setFields(new HashSet<Field>());
 			for (Field f : getFields()) {
 				Field copy = f.deepCopy(copyReferenceToAssessment);
 				copy.setParent(field);
 				field.getFields().add(copy);
 			}
 		}
 		return field;	
 	}
 
 	
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Field other = (Field) obj;
 		
 		if (getId() > 0)
 			return getId() == other.getId();
 		else
 			return false;
 		
 		/*if (generationCode == null) {
 			if (other.generationCode != null)
 				return false;
 		} else if (!generationCode.equals(other.generationCode))
 			return false;
 		return true;*/
 	}
 
 	public Assessment getAssessment() {
 		return assessment;
 	}
 
 	public java.util.Set<Field> getFields() {
 		return fields;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public Map<String,Field> getKeyToFields() {
 		HashMap<String, Field> keyToFields = new HashMap<String, Field>();
 		for (Field f : getFields())
 			keyToFields.put(f.getName(), f);
 
 		return keyToFields;
 	}
 
 	public Map<String, PrimitiveField> getKeyToPrimitiveFields() {
 		Map<String, PrimitiveField> keyToPrimitiveFields = new HashMap<String, PrimitiveField>();
 		for (PrimitiveField pf : getPrimitiveField())
 			keyToPrimitiveFields.put(pf.getName(), pf);
 		
 		return keyToPrimitiveFields;
 		
 	}
 	
 	public PrimitiveField<?> getPrimitiveField(String fieldName) {
 		if (primitiveField == null)
 			primitiveField = new HashSet<PrimitiveField>();
 		
 		return getKeyToPrimitiveFields().get(fieldName);
 	}
 	
 	public void addPrimitiveField(PrimitiveField<?> field) {
 		if (primitiveField == null)
 			primitiveField = new HashSet<PrimitiveField>();
 		
 		PrimitiveField<?> existing = getPrimitiveField(field.getName());
		if (existing == null)
 			primitiveField.remove(existing);
 		
 		primitiveField.add(field);
 	}
 	
 	public Field getField(String fieldName) {
 		if (fields == null)
 			fields = new HashSet<Field>();
 		
 		return getKeyToFields().get(fieldName);
 	}
 	
 	public void addField(Field field) {
 		if (fields == null)
 			fields = new HashSet<Field>();
 		
 		Field existing = getField(field.getName());
 		if (existing != null)
 			fields.remove(existing);
 		
 		fields.add(field);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public java.util.Set<Notes> getNotes() {
 		return notes;
 	}
 
 	public int getORMID() {
 		return getId();
 	}
 
 	public Field getParent() {
 		return parent;
 	}
 
 	public java.util.Set<PrimitiveField> getPrimitiveField() {
 		return primitiveField;
 	}
 
 	public java.util.Set<Reference> getReference() {
 		return reference;
 	}
 	
 	public boolean hasData() {
 		boolean hasData = false;
 		if (primitiveField != null) {
 			for (PrimitiveField current : primitiveField) {
 				String value = current.getRawValue();
 				if ("".equals(value))
 					value = null;
 				
 				if (value != null) {
 					hasData = true;
 					break;
 				}
 			}
 		}
 		
 		if (hasData)
 			return true;
 		
 		if (fields != null) {
 			for (Field current : fields)
 				if (hasData = current.hasData())
 					break;
 		}
 		
 		return hasData;
 	}
 
 	public boolean isNarrativeField() {
 		for (String name : CanonicalNames.narrativeFields)
 			if (name.equals(getName()))
 				return true;
 		
 		return getName().endsWith("Documentation");
 	}
 	
 	public boolean isClassificationScheme() {
 		for (String name : CanonicalNames.classificationSchemes)
 			if (name.equals(getName()))
 				return true;
 		return false;
 	}
 
 	public void setAssessment(Assessment value) {
 		setAssessment(value, false);
 	}
 	
 	public void setAssessment(Assessment value, boolean cascade) {
 		this.assessment = value;
 		/*
 		 * FIXME: is this necessary?
 		 */
 		if (cascade && fields != null)
 			for (Field field : fields)
 				field.setAssessment(value);
 	}
 
 	public void setFields(java.util.Set<Field> value) {
 		this.fields = value;
 	}
 
 	public void setId(int value) {
 		this.id = value;
 	}
 
 	public void setName(String value) {
 		this.name = value;
 	}
 
 	public void setNotes(java.util.Set<Notes> value) {
 		this.notes = value;
 	}
 
 	public void setParent(Field value) {
 		this.parent = value;
 	}
 
 	public void setPrimitiveField(java.util.Set<PrimitiveField> value) {
 		this.primitiveField = value;
 	}
 
 	public void setReference(java.util.Set<Reference> value) {
 		this.reference = value;
 	}
 
 	public String toString() {
 		return "FIELD " + String.valueOf(getId());
 	}
 
 	public String toXML() {
 		return toXML(getName());
 	}
 	
 	public String toXML(String rootTag) {
 		StringBuilder str = new StringBuilder("<");
 		str.append(rootTag);
 		str.append(" version=\"");
 		str.append(VERSION);
 		str.append("\"");
 		str.append(" id=\"");
 		str.append(getId());
 		str.append("\">\n");
 		
 		if (!getFields().isEmpty()) {
 			str.append("<subfields>\n");
 			
 			final List<Field> sorted = new ArrayList<Field>(getFields());
 			Collections.sort(sorted, new FieldNameComparator());
 			
 			for (Field subfield : sorted)
 				str.append(subfield.toXML());
 			str.append("</subfields>\n");
 		}
 		
 		if (!getPrimitiveField().isEmpty()) {
 			TreeSet<PrimitiveField> sorted = new TreeSet<PrimitiveField>(
 				new PrimitiveFieldNameComparator()	
 			);
 			sorted.addAll(getPrimitiveField());
 				
 			for (PrimitiveField cur : sorted) {
 				str.append(cur.toXML());
 				str.append("\n");
 			}
 		}
 		
 		if (getReference() != null) {
 			for (Reference ref : getReference())
 				str.append(ref.toXML()+"\n");
 		}
 		
 		if (getNotes() != null) {
 			for (Notes note : getNotes())
 				str.append(note.toXML()+"\n");
 		}
 		
 		str.append("</" + rootTag + ">\n");
 		
 		return str.toString();
 	}
 
 	private static class PrimitiveFieldNameComparator implements Comparator<PrimitiveField> {
 		
 		/*
 		 * I left this here to show that I *don't* want to use 
 		 * our Alphanumeric comparator, because it won't match 
 		 * up on the server because the database isn't smart 
 		 * enough to sort like this.  I don't need the sorting 
 		 * to be correct, I just need it to be consistent.
 		 */
 		/*private final PortableAlphanumericComparator comparator = 
 			new PortableAlphanumericComparator();*/
 		
 		public int compare(PrimitiveField o1, PrimitiveField o2) {
 			if (o1.getName() == null || o2.getName() == null)
 				return o1.getName() == null ? o2.getName() == null ? 0 : 1 : -1;
 			else
 				return o1.getName().compareTo(o2.getName());
 		}
 		
 	}
 	
 	public static class FieldNameComparator implements Comparator<Field> {
 		
 		/*
 		 * I left this here to show that I *don't* want to use 
 		 * our Alphanumeric comparator, because it won't match 
 		 * up on the server because the database isn't smart 
 		 * enough to sort like this.  I don't need the sorting 
 		 * to be correct, I just need it to be consistent.
 		 */
 		/*private final PortableAlphanumericComparator comparator = 
 			new PortableAlphanumericComparator();*/
 		
 		@Override
 		public int compare(Field o1, Field o2) {
 			int result;
 			if (o1.getName() == null || o2.getName() == null)
 				result = o1.getName() == null ? o2.getName() == null ? 0 : 1 : -1;
 			else
 				result = o1.getName().compareTo(o2.getName());
 			/*FIXME: if (result == 0)
 				result =  o1.generationCode.compareTo(o2.generationCode);*/
 			
 			return result;
 		}
 		
 	}
 }
