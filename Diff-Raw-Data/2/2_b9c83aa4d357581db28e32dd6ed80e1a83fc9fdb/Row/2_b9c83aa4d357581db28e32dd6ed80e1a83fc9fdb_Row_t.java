 package de.devboost.natspec.library.tables;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class Row {
 
 	private List<Field> fields = new ArrayList<Field>();
 	private Table table;
 
 	public Row(Table parent) {
 		this.table = parent;
 	}
 	
 	public List<Field> getFields() {
 		return fields;
 	}
 	
 	public Field getField(String columnName) {
 		List<Field> headers = table.getHeaders();
 		for (Field headerField : headers) {
 			String headerName = headerField.getText();
 			if (headerName.trim().equals(columnName.trim())) {
 				int columnIndex = headers.indexOf(headerField);
 				return getField(columnIndex);
 			}
 		}
		throw new IllegalArgumentException("Unknown column name: " + columnName);
 	}
 
 	private Field getField(int columnIndex) {
 		return fields.get(columnIndex);
 	}
 
 	public void seal() {
 		this.fields = Collections.unmodifiableList(fields);
 	}
 	
 	@Override
 	public String toString() {
 		return getClass().getSimpleName() + " " + fields.toString();
 	}
 
 	public String getText(int columnIndex) {
 		Field field = getFields().get(columnIndex);
 		return field.getText();
 	}
 }
