 package org.adorsys.xlseasy.boot;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * The Class WorkBookSheet.
  * 
  * @param <T>
  *            the generic type
  * 
  * @author Sandro Sonntag, Francis Pouatcha, Marius Guede
  */
 public class WorkBookSheet<T> {
 
 	public WorkBookSheet(Field field, List<Field> fieldOrder,
 			Collection<String> excludedFields, Class<T> sheeKlass,
 			Field keyField, Map<String, String> fieldDateStyles) {
 		super();
 		this.field = field;
 		this.sheetKlass = sheeKlass;
 		this.keyField = keyField;
 		this.fieldOrder.addAll(fieldOrder);
 		excludedFields.addAll(excludedFields);
 		this.fieldDateStyles = fieldDateStyles;
 	}
 
 	private final Class<T> sheetKlass;
 
 	public Class<T> getSheetKlass() {
 		return sheetKlass;
 	}
 
 	private final Field field;
 
 	public Field getField() {
 		return field;
 	}
 
 	private final List<Field> fieldOrder = new ArrayList<Field>();
 
 	public List<Field> getFieldOrder() {
 		return fieldOrder;
 	}
 
 	private final Collection<String> excludedFields = new ArrayList<String>();
 
 	public Collection<String> getExcludedFields() {
 		return excludedFields;
 	}
 
 	private final Field keyField;
 
 	public Field getKeyField() {
 		return keyField;
 	}
 
 	private final Map<String, String> fieldDateStyles;
 
 	public Map<String, String> getFieldDateStyles() {
 		return fieldDateStyles;
 	}
 
 	private WorkbookCbe workbookCbe;
 
 	public WorkbookCbe getWorkbookCbe() {
 		return workbookCbe;
 	}
 
 	public void setWorkbookCbe(WorkbookCbe workbookCbe) {
 		this.workbookCbe = workbookCbe;
 	}
 
 	/**
 	 * We make the method case insensitive. Must be helpful to compare case
 	 * classes fields and them from workbook sheet.
 	 */
 	public Field getField(String fieldName) {
 		List<Field> fieldOrder2 = fieldOrder;
		if (fieldName != null) {
			for (Field field : fieldOrder2) {
				// if (fieldName.equals(field.getName()))
				if (fieldName.equalsIgnoreCase(field.getName()))
					return field;
			}
 		}
 		return null;
 	}
 }
