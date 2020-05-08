 /*******************************************************************************
  * Copyright (c) 2006-2013
  * Software Technology Group, Dresden University of Technology
  * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany;
  *   DevBoost GmbH - Berlin, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package de.devboost.codecomposers.java;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import de.devboost.codecomposers.Component;
 import de.devboost.codecomposers.StringComposite;
 import de.devboost.codecomposers.util.Pair;
 import de.devboost.codecomposers.util.StringUtil;
 
 /**
  * A custom StringComposite that is configured with the Java-specific line break
  * characters and indentation starter and stoppers.
  */
 public class JavaComposite extends StringComposite {
 
 	private Map<String, String> fields = new LinkedHashMap<String, String>();
 	private Map<String, String[]> fieldDocs = new LinkedHashMap<String, String[]>();
 	private Map<String, String> getters = new LinkedHashMap<String, String>();
 	private Map<String, String[]> getterDocs = new LinkedHashMap<String, String[]>();
 	private Map<String, String> setters = new LinkedHashMap<String, String>();
 	private ImportsPlaceholder importsPlaceholder;
 	private boolean interfaceMode;
 
 	public JavaComposite() {
 		super(true);
 		addIndentationStarter("{");
 		addIndentationStopper("}");
 		addLineBreaker("{");
 		addLineBreaker(";");
 		addLineBreaker(",");
 		addLineBreaker("}");
 		addLineBreaker("*/");
 	}
 
 	@Override
 	protected boolean isLineBreaker(Component component) {
 		boolean superResult = super.isLineBreaker(component);
 		if (superResult) {
 			return true;
 		}
 		// add line breaks after single line comments
 		String componentText = component.toString();
 		boolean isSingleLineComment = componentText.contains("//");
 		if (isSingleLineComment) {
 			return true;
 		}
 		boolean isMultiLineComment = 
 			"/*".equals(componentText) ||      // start of multi-line comment
 			"/**".equals(componentText) ||     // start of Javadoc comment
 			componentText.startsWith(" * ") || // intermediate line
 			" */".equals(componentText);       // end of multi-line comment
 		if (isMultiLineComment) {
 			return true;
 		}
 		
 		boolean isAnnotation = componentText.startsWith("@");
 		return isAnnotation;
 	}
 
 	public void addComment(String... paragraphs) {
 		for (String paragraph : paragraphs) {
 			String[] chunks = paragraph.split("\n");
 			for (String chunk : chunks) {
 				// split chunk into lines of 80 characters (split at space)
 				List<String> lines = split(chunk, 80);
 				for (String line : lines) {
 					add("// " + line);
 				}
 			}
 		}
 	}
 	
 	public void addJavadoc(String... paragraphs) {
 		addDocInternal("/**", paragraphs);
 	}
 
 	public void addDoc(String... paragraphs) {
 		addDocInternal("/*", paragraphs);
 	}
 
 	private void addDocInternal(String prefix, String... paragraphs) {
 		// skip empty documentation
 		if (paragraphs == null || paragraphs.length == 0) {
 			return;
 		}
 		
 		add(prefix);
 		boolean wasParameterParagraph = false;
 		for (String paragraph : paragraphs) {
 			boolean addEmptyLine = false;
 			if (paragraph.startsWith("@param")) {
 				if (!wasParameterParagraph) {
 					addEmptyLine = true;
 				}
 				wasParameterParagraph = true;
 			}
 			if (paragraph.startsWith("@return")) {
 				addEmptyLine = true;
 			}
 			if (paragraph.startsWith("@throws")) {
 				addEmptyLine = true;
 			}
 			if (paragraph.startsWith("@see")) {
 				addEmptyLine = true;
 			}
 			if (paragraph.startsWith("@since")) {
 				addEmptyLine = true;
 			}
 			if (addEmptyLine) {
 				add(" * ");
 			}
 			String[] chunks = paragraph.split("\n");
 			for (String chunk : chunks) {
 				// split chunk into lines of 80 characters (split at space)
 				List<String> lines = split(chunk, 80);
 				for (String line : lines) {
 					add(" * " + line);
 				}
 			}
 		}
 		add(" */");
 	}
 	
 	/**
 	 * Splits the given text into lines where each line does
 	 * contain at most 'maxLength' characters. The text is split
 	 * at space characters (i.e., words are not split).
 	 * 
 	 * @param text the string to split
 	 * @param maxLength the maximum length of the lines returned
 	 * @return
 	 */
 	public List<String> split(String text, int maxLength) {
 		String tail = text;
 		List<String> result = new ArrayList<String>();
 		int length = Integer.MAX_VALUE;
 		while (length > maxLength) {
 			length = tail.length();
 			if (length <= maxLength) {
 				result.add(tail);
 			} else {
 				String head = tail.substring(0, maxLength);
 				int indexOfLastSpace = head.lastIndexOf(" ");
 				if (indexOfLastSpace >= 0) {
 					head = tail.substring(0, indexOfLastSpace);
 				} else {
 					indexOfLastSpace = head.length() - 1;
 				}
 				result.add(head);
 				tail = tail.substring(indexOfLastSpace + 1).trim();
 			}
 		}
 		return result;
 	}
 
 	public String declareArrayList(String name, String type) {
 		return IClassNameConstants.LIST + "<" + type + "> " + name  + " = new " + IClassNameConstants.ARRAY_LIST + "<" + type + ">();";
 	}
 
 	public String declareLinkedHashMap(String name, String keyType, String valueType) {
 		return IClassNameConstants.MAP + "<" + keyType + ", " + valueType + "> " + name  + " = new " + IClassNameConstants.LINKED_HASH_MAP + "<" + keyType + ", " + valueType + ">();";
 	}
 
 	public String declareLinkedHashSet(String name, String type) {
 		return IClassNameConstants.SET + "<" + type + "> " + name + " = new " + IClassNameConstants.LINKED_HASH_SET + "<" + type + ">();";
 	}
 
 	/**
 	 * Adds a field with the given name and type and the respective get and set
 	 * methods.
 	 * 
 	 * @param fieldName the name of the field to add
 	 * @param type the type of the field
 	 */
 	public void addFieldGetSet(String fieldName, String type) {
 		addFieldGetSet(fieldName, type, (String[]) null);
 	}
 
 	public void addFieldGetSet(String fieldName, String type, String... javadoc) {
 		addFieldGetSet(fieldName, type, javadoc, null);
 	}
 
 	public void addFieldGetSet(String fieldName, String type, String[] fieldDoc, String[] getterDoc) {
 		addFieldGet(fieldName, type, fieldDoc, getterDoc);
 		setters.put(fieldName, type);
 	}
 
 	/**
 	 * Adds a field with the given name and type and the respective get 
 	 * method.
 	 * 
 	 * @param fieldName the name of the field to add
 	 * @param type the type of the field
 	 */
 	public void addFieldGet(String fieldName, String type, String... javadoc) {
 		addFieldGet(fieldName, type, javadoc, null);
 	}
 	
 	public void addFieldGet(String fieldName, String type, String[] fieldDoc, String[] getterDoc) {
 		fields.put(fieldName, type);
 		fieldDocs.put(fieldName, fieldDoc);
 		getters.put(fieldName, type);
 		getterDocs.put(fieldName, getterDoc);
 	}
 	
 	public void addGettersSetters() {
 		addGetters();
 		addSetters();
 	}
 
 	private void addSetters() {
 		for (String fieldName : setters.keySet()) {
 			String type = setters.get(fieldName);
 			add("public void set" + StringUtil.capitalize(fieldName) + "(" + type + " " + fieldName + ") {");
 			add("this." + fieldName + " = " + fieldName + ";");
 			add("}");
 			addLineBreak();
 		}
 	}
 
 	private void addGetters() {
 		for (String fieldName : getters.keySet()) {
 			String type = getters.get(fieldName);
 			String[] doc = getterDocs.get(fieldName);
 			addJavadoc(doc);
 			String methodPrefix = "get";
 			if ("boolean".equals(type)) {
 				methodPrefix = "is";
 			}
 			add("public " + type + " " + methodPrefix + StringUtil.capitalize(fieldName) + "() {");
 			add("return " + fieldName + ";");
 			add("}");
 			addLineBreak();
 		}
 	}
 
 	public void addFields() {
 		for (String fieldName : fields.keySet()) {
 			String type = fields.get(fieldName);
 			String[] doc = fieldDocs.get(fieldName);
 			addJavadoc(doc);
 			add("private " + type + " " + fieldName + ";");
 			addLineBreak();
 		}
 	}
 	
 	/**
 	 * Creates a set of methods that contain the given statements. Each 
 	 * statement is represented by its content (the code) and the number of
 	 * bytes that this code requires. The method is automatically split if
 	 * the code exceeds the 64k limit.
 	 * 
 	 * @param name a prefix for the method names
 	 * @param statements the statements to add to the method body
 	 */
 	public void addLargeMethod(String name, List<Pair<String, Integer>> statements) {
 		int bytesUsedInCurrentMethod = 0;
 		boolean methodIsFull = true;
 		int i = 0;
 		int numberOfMethods = 0;
 		// create multiple nameX() methods
 		for (Pair<String, Integer> statement : statements) {
 			if (methodIsFull) {
 				add("public static void " + name + numberOfMethods + "() {");
 				numberOfMethods++;
 				methodIsFull = false;
 			}
 			add(statement.getLeft());
 			bytesUsedInCurrentMethod += statement.getRight();
 			
 			if (bytesUsedInCurrentMethod >= 63 * 1024) {
 				methodIsFull = true;
 				bytesUsedInCurrentMethod = 0;
 			}
 			if (methodIsFull || i == statements.size() - 1) {
 				add("}");
 				addLineBreak();
 			}
 			i++;
 		}
 		
 		// create a name() method that calls all nameX() methods
 		add("public static void " + name + "() {");
 		for (int c = 0; c < numberOfMethods; c++) {
 			add(name + c + "();");
 		}
 		add("}");
 		addLineBreak();
 	}
 
 	public void addImportsPlaceholder() {
 		if (this.importsPlaceholder != null) {
 			throw new IllegalArgumentException("Can't add placeholder for imports twice.");
 		}
 		this.importsPlaceholder = new ImportsPlaceholder(getLineBreak());
 		add(this.importsPlaceholder);
 	}
 	
 	public String getClassName(Class<?> clazz) {
 		return getClassName(clazz.getName());
 	}
 
 	public String getClassName(String qualifiedClassName) {
 		if (this.importsPlaceholder == null) {
 			throw new IllegalArgumentException("No placeholder for imports found.");
 		}
 		return this.importsPlaceholder.getClassName(qualifiedClassName);
 	}
 
 	public void addImplicitImport(String simpleClassName) {
 		if (this.importsPlaceholder == null) {
 			throw new IllegalArgumentException("No placeholder for imports found.");
 		}
 		this.importsPlaceholder.addImplicitImport(simpleClassName);
 	}
 	
 	public String STRING() {
 		return getClassName(String.class);
 	}
 	
 	public String SET() {
 		return getClassName(Set.class);
 	}
 	
 	public String LIST() {
 		return getClassName(List.class);
 	}
 	
 	public String ARRAY_LIST() {
 		return getClassName(ArrayList.class);
 	}
 	
 	public String LINKED_HASH_SET() {
 		return getClassName(LinkedHashSet.class);
 	}
 	
 	@Override
 	public StringComposite add(String text) {
 		StringComposite result = super.add(text);
		if (interfaceMode && text.endsWith(";") && !text.startsWith("package ")) {
 			result = result.addLineBreak();
 		}
 		return result;
 	}
 
 	/**
 	 * Set the interface mode (i.e., whether to insert two line breaks after 
 	 * line that end with a semicolon).
 	 */
 	public void setInterfaceMode(boolean interfaceMode) {
 		this.interfaceMode = interfaceMode;
 	}
 }
