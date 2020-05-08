 package org.lh.dmlj.schema.editor.property;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.lh.dmlj.schema.Procedure;
 import org.lh.dmlj.schema.SchemaArea;
 import org.lh.dmlj.schema.SchemaPackage;
 import org.lh.dmlj.schema.SchemaRecord;
 import org.lh.dmlj.schema.Set;
 
 public class SchemaGeneralPropertiesSection 
 	extends AbstractSchemaPropertiesSection {
 
 	private static final DateFormat MEMO_DATE_FORMAT = 
 		new SimpleDateFormat("MM/dd/yy");
 	
 	public SchemaGeneralPropertiesSection() {
 		super();
 	}
 	
 	@Override
 	protected String getDescription(EAttribute attribute) {
 		if (attribute == SchemaPackage.eINSTANCE.getSchema_Name()) {
 			return "Identifies the schema";
 		} else if (attribute == SchemaPackage.eINSTANCE.getSchema_Version()) {
 			return "Qualifies the schema with a version number, which " +
 				   "distinguishes this schema from others that have the same " +
 				   "name";
 		} else if (attribute == SchemaPackage.eINSTANCE.getSchema_Description()) {
 			return "Optionally specifies a name that is more descriptive " +
 				   "than the 8-character schema name required by CA IDMS/DB, " +
 				   "but can be used to store any type of information";
 		} else if (attribute == SchemaPackage.eINSTANCE.getSchema_MemoDate()) {
 			return "Specifies any date the user wishes to supply";
 		}
 		return super.getDescription(attribute);
 	}
 	
 	@Override
 	protected EObject getEditableObject(EAttribute attribute) {
 		if (attribute == SchemaPackage.eINSTANCE.getSchema_Name() ||
 			attribute == SchemaPackage.eINSTANCE.getSchema_Version() ||
 			attribute == SchemaPackage.eINSTANCE.getSchema_Description() ||
 			attribute == SchemaPackage.eINSTANCE.getSchema_MemoDate()) {
 			
 			return target;
 		}
 		return super.getEditableObject(attribute);
 	}
 	
 	@Override
 	protected IEditHandler getEditHandler(EAttribute attribute, Object newValue) {
 		if (attribute == SchemaPackage.eINSTANCE.getSchema_Name()) {
 			// get the new schema name
 			String newSchemaName = (String) newValue;
 			// newSchemaName must be a 1- to 8-character value; it must not be 
 			// the same (case insensitive) as any components or synonyms within 
 			// the schema
 			if (newSchemaName == null || newSchemaName.length() < 1 || 
 				newSchemaName.length() > 8) {
 				
 				String message = "must be a 1- to 8-character value";
 				return new ErrorEditHandler(message);
 			}
 			for (SchemaArea area : target.getAreas()){
 				if (area.getName().equalsIgnoreCase(newSchemaName)) {
 					String message = "same as area '" + area.getName() + "'";
 					return new ErrorEditHandler(message);
 				}
 			}
 			for (Procedure procedure : target.getProcedures()){
 				if (procedure.getName().equalsIgnoreCase(newSchemaName)) {
 					String message = 
 						"same as procedure '" + procedure.getName() + "'";
 					return new ErrorEditHandler(message);
 				}
 			}
 			for (SchemaRecord record : target.getRecords()){
 				if (record.getName().equalsIgnoreCase(newSchemaName)) {
 					String message = 
 						"same as record '" + record.getName() + "'";
 					return new ErrorEditHandler(message);
 				}
 			}
 			for (Set set : target.getSets()){
 				if (set.getName().equalsIgnoreCase(newSchemaName)) {
 					String message = "same as set '" + set.getName() + "'";
 					return new ErrorEditHandler(message);
 				}
 			}
 			// convert the new schema name to uppercase before passing it to the 
 			// set attribute command
 			return super.getEditHandler(attribute, newSchemaName.toUpperCase());
 		} else if (attribute == SchemaPackage.eINSTANCE.getSchema_Version()) {
 			// get the new version
 			short newVersion = (short) newValue;
 			// version must be an unsigned integer in the range 1 through 9999
 			if (newVersion < 1 || newVersion > 9999) {
 				String message = 
 					"must be an unsigned integer in the range 1 through 9999";
 				return new ErrorEditHandler(message);
 			}
 		} else if (attribute == SchemaPackage.eINSTANCE.getSchema_Description()) {
 			// get the new description
 			String newDescription = (String) newValue;
 			// description is a 1- to 40-character alphanumeric field; it is 
 			// optional
 			if (newDescription != null) {
 				if (newDescription.length() < 1 || newDescription.length() > 40) {				
 					String message = "not a 1- to 40-character alphanumeric field";
 					return new ErrorEditHandler(message);
 				} else {
 					return super.getEditHandler(attribute, 
 												newDescription.toUpperCase());
 				}
 			} else {
 				return super.getEditHandler(attribute, null);
 			}
 		} else if (attribute == SchemaPackage.eINSTANCE.getSchema_MemoDate()) {
 			// get the new memo date (format: mm/dd/yy)
 			String newMemoDate = (String) newValue;
			String message = 
				"Unparsable date: \"" + newMemoDate + "\" - please enter a " +
				"valid date in the format \"mm/dd/yy\"";
			try {
				Date date = MEMO_DATE_FORMAT.parse(newMemoDate);
				if (!MEMO_DATE_FORMAT.format(date).equals(newMemoDate)) {
 					return new ErrorEditHandler(message);
 				}
			} catch (ParseException e) {
				return new ErrorEditHandler(message);
 			}
 			return super.getEditHandler(attribute, newMemoDate);
 		}
 		return super.getEditHandler(attribute, newValue);
 	}
 	
 	
 	@Override
 	protected List<EAttribute> getAttributes() {
 		List<EAttribute> attributes = new ArrayList<>();
 		attributes.add(SchemaPackage.eINSTANCE.getSchema_Name());
 		attributes.add(SchemaPackage.eINSTANCE.getSchema_Version());
 		attributes.add(SchemaPackage.eINSTANCE.getSchema_Description());
 		attributes.add(SchemaPackage.eINSTANCE.getSchema_MemoDate());
 		return attributes;
 	}
 	
 	@Override
 	protected String getLabel(EAttribute attribute) {
 		if (attribute == SchemaPackage.eINSTANCE.getSchema_Name()) {
 			return "Name";
 		} else if (attribute == SchemaPackage.eINSTANCE.getSchema_Version()) {
 			return "Version";
 		} else if (attribute == SchemaPackage.eINSTANCE.getSchema_Description()) {
 			return "Description";
 		} else if (attribute == SchemaPackage.eINSTANCE.getSchema_MemoDate()) {
 			return "Memo date";
 		}
 		return super.getLabel(attribute);
 	}	
 
 }
