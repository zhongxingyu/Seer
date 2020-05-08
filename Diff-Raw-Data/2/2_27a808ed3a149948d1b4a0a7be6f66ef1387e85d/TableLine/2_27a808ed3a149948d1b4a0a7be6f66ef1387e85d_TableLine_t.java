 /**
  * Copyright (c) 2008 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.conn.tools;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A line of a table read using RFC_READ_TABLE.
  * @author vwegert
  *
  */
 public class TableLine implements ITableLine {
 
 	private ITableStructure structure;
 	private Map<String, String> fieldContents = new HashMap<String, String>();
 
 	/**
 	 * Default constructor
 	 * @param structure the structure of the results
 	 * @param rawData the result as a single string
 	 */
 	public TableLine(ITableStructure structure, String rawData) {
 		super();
 		this.structure = structure;
 		for (ITableField field: structure.getFieldList()) {
 			final int startIndex = field.getOffset();
			final int endIndex = field.getOffset() + field.getLength();
 			String value;
 			if (startIndex >= rawData.length()) {
 				value = "";
 			} else {
 				if (endIndex <= rawData.length()) {
 					value = rawData.substring(startIndex, endIndex).trim();
 				} else {
 					value = rawData.substring(startIndex, rawData.length()).trim();
 				}
 			}
 			fieldContents.put(field.getFieldName(), value);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.rcer.conn.tools.ITableLine#getValue(java.lang.String)
 	 */
 	public String getValue(String fieldName) throws FieldNotFoundException {
 		if (fieldContents.containsKey(fieldName)) {
 			return fieldContents.get(fieldName);
 		}
 		throw new FieldNotFoundException(structure.getTableName(), fieldName);
 	}
 	
 }
