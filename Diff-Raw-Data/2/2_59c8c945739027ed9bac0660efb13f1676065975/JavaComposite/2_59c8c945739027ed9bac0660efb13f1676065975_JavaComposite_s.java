 /*******************************************************************************
  * Copyright (c) 2006-2014
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
 import java.util.Collection;
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
  * A {@link JavaComposite} is custom {@link StringComposite} that is configured
 * with the Java-specific line break characters and indentation starter and
  * stoppers. It is also capable of adding required imports automatically (if
  * class names are acquired using {@link #getClassName(Class)} or
  * {@link #getClassName(String)}.
  */
 public class JavaComposite extends StringComposite {
 
 	private final Map<String, String> fields = new LinkedHashMap<String, String>();
 	private final Map<String, String[]> fieldDocs = new LinkedHashMap<String, String[]>();
 	private final Map<String, String> getters = new LinkedHashMap<String, String>();
 	private final Map<String, String[]> getterDocs = new LinkedHashMap<String, String[]>();
 	private final Map<String, String> setters = new LinkedHashMap<String, String>();
 	
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
 	
 	/**
 	 * Adds a Javadoc comment containing the given paragraphs to the generated
 	 * code.
 	 * 
 	 * @param paragraphs the text of the comment
 	 */
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
 	 * Splits the given text into lines where each line does contain at most
 	 * 'maxLength' characters. The text is split at space characters (i.e.,
 	 * words are not split).
 	 * 
 	 * @param text
 	 *            the string to split
 	 * @param maxLength
 	 *            the maximum length of the lines returned
 	 * @return the lines the text was split into
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
 
 	/**
 	 * Returns a declaration of an {@link ArrayList} with the given name and
 	 * element type.
 	 * 
 	 * @param name the name of the list
 	 * @param type the type of elements contained in the list
 	 * @return a local variable declaration for the list
 	 */
 	public String declareArrayList(String name, String type) {
 		return IClassNameConstants.LIST + "<" + type + "> " + name  + " = new " + IClassNameConstants.ARRAY_LIST + "<" + type + ">();";
 	}
 
 	/**
 	 * Returns a declaration of an {@link LinkedHashMap} with the given name and
 	 * key/value types.
 	 * 
 	 * @param name the name of the map
 	 * @param keyType the type of elements used as keys
 	 * @param valueType the type of elements used as values
 	 * @return a local variable declaration for the map
 	 */
 	public String declareLinkedHashMap(String name, String keyType, String valueType) {
 		return IClassNameConstants.MAP + "<" + keyType + ", " + valueType + "> " + name  + " = new " + IClassNameConstants.LINKED_HASH_MAP + "<" + keyType + ", " + valueType + ">();";
 	}
 
 	/**
 	 * Returns a declaration of an {@link LinkedHashSet} with the given name and
 	 * element type.
 	 * 
 	 * @param name the name of the set
 	 * @param type the type of elements contained in the set
 	 * @return a local variable declaration for the set
 	 */
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
 
 	/**
 	 * Adds a field with the given name and type and the respective get and set
 	 * methods.
 	 * 
 	 * @param fieldName the name of the field to add
 	 * @param type the type of the field
 	 * @param fieldDoc the documentation for the field
 	 */
 	public void addFieldGetSet(String fieldName, String type, String... fieldDoc) {
 		addFieldGetSet(fieldName, type, fieldDoc, null);
 	}
 
 	/**
 	 * Adds a field with the given name and type and the respective get and set
 	 * methods.
 	 * 
 	 * @param fieldName
 	 *            the name of the field to add
 	 * @param type
 	 *            the type of the field
 	 * @param fieldDoc
 	 *            the documentation for the field
 	 * @param getterDoc
 	 *            the documentation for the get method
 	 */
 	public void addFieldGetSet(String fieldName, String type, String[] fieldDoc, String[] getterDoc) {
 		addFieldGet(fieldName, type, fieldDoc, getterDoc);
 		setters.put(fieldName, type);
 	}
 
 	/**
 	 * Adds a field with the given name and type and the respective get method.
 	 * No set method is added.
 	 * 
 	 * @param fieldName
 	 *            the name of the field to add
 	 * @param type
 	 *            the type of the field
 	 * @param fieldDoc
 	 *            the documentation for the field
 	 */
 	public void addFieldGet(String fieldName, String type, String... fieldDoc) {
 		addFieldGet(fieldName, type, fieldDoc, null);
 	}
 	
 	/**
 	 * Adds a field with the given name and type and the respective get method.
 	 * No set method is added.
 	 * 
 	 * @param fieldName
 	 *            the name of the field to add
 	 * @param type
 	 *            the type of the field
 	 * @param fieldDoc
 	 *            the documentation for the field
 	 * @param getterDoc
 	 *            the documentation for the get method
 	 */
 	public void addFieldGet(String fieldName, String type, String[] fieldDoc, String[] getterDoc) {
 		fields.put(fieldName, type);
 		fieldDocs.put(fieldName, fieldDoc);
 		getters.put(fieldName, type);
 		getterDocs.put(fieldName, getterDoc);
 	}
 	
 	/**
 	 * Inserts all get and set methods for fields that were created by previous
 	 * calls to {@link #addFieldGet()} or {@link #addFieldGetSet()}.
 	 */
 	public void addGettersSetters() {
 		addGetters();
 		addSetters();
 	}
 
 	/**
 	 * Inserts all set methods for fields that were created by previous calls to
 	 * {@link #addFieldGetSet()}.
 	 */
 	private void addSetters() {
 		for (String fieldName : setters.keySet()) {
 			String type = setters.get(fieldName);
 			add("public void set" + StringUtil.capitalize(fieldName) + "(" + type + " " + fieldName + ") {");
 			add("this." + fieldName + " = " + fieldName + ";");
 			add("}");
 			addLineBreak();
 		}
 	}
 
 	/**
 	 * Inserts all get methods for fields that were created by previous calls to
 	 * {@link #addFieldGet()} or {@link #addFieldGetSet()}.
 	 */
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
 
 	/**
 	 * Inserts all fields that were created by previous calls to
 	 * {@link #addFieldGet()} or {@link #addFieldGetSet()}.
 	 */
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
 	 * bytes that this code requires. The method is automatically split if the
 	 * code exceeds the 64k limit.
 	 * 
 	 * @param name
 	 *            a prefix for the method names
 	 * @param statements
 	 *            the statements to add to the method body
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
 
 	/**
 	 * Adds a placeholder where the required import statements will be inserted
 	 * later on. This must must be called at most once.
 	 * 
 	 * @throws IllegalStateException if method is called more than once.
 	 */
 	public void addImportsPlaceholder() {
 		if (this.importsPlaceholder != null) {
 			throw new IllegalStateException("Can't add placeholder for imports twice.");
 		}
 		this.importsPlaceholder = new ImportsPlaceholder(getLineBreak());
 		add(this.importsPlaceholder);
 	}
 	
 	/**
 	 * Returns the simple name for the given class and adds an import (if
 	 * required). If a class with the same simple name, but a different
 	 * qualified name is already imported, the qualified class name is returned.
 	 * Before this method is called, {@link #addImportsPlaceholder()} must be
 	 * called.
 	 * 
 	 * @param clazz
 	 *            the class the determine the name for
 	 * @return either a simple or qualified name
 	 * @throws IllegalStateException
 	 *             if {@link #addImportsPlaceholder()} was not called before
 	 */
 	public String getClassName(Class<?> clazz) {
 		return getClassName(clazz.getCanonicalName());
 	}
 
 	/**
 	 * Returns the simple name for the given qualified class name and adds an
 	 * import (if required). If a class with the same simple name, but a
 	 * different qualified name is already imported, the qualified class name is
 	 * returned as it is. Before this method is called,
 	 * {@link #addImportsPlaceholder()} must be called.
 	 * 
 	 * @param clazz
 	 *            the class to determine the name for
 	 * @return either a simple or qualified name
 	 * @throws IllegalStateException
 	 *             if {@link #addImportsPlaceholder()} was not called before
 	 */
 	public String getClassName(String qualifiedClassName) {
 		if (this.importsPlaceholder == null) {
 			throw new IllegalStateException("No placeholder for imports found.");
 		}
 		return this.importsPlaceholder.getClassName(qualifiedClassName);
 	}
 
 	/**
 	 * Returns the simple name for the given qualified member and adds a static
 	 * import (if required). If a class or member with the same simple name, but
 	 * a different qualified name is already imported, the qualified member name 
 	 * is returned as it is.
 	 * 
 	 * @param qualifiedMemberName
 	 *            the member to determine the name for
 	 * @return either a simple or qualified name
 	 */
 	public String getStaticMemberName(String qualifiedMemberName) {
 		return this.importsPlaceholder.getStaticMemberName(qualifiedMemberName);
 	}
 	
 	/**
 	 * Adds the simple name of a class that is implicitly imported (e.g.,
 	 * because it resides in the same package as the class that is currently
 	 * generated). Before this method is called,
 	 * {@link #addImportsPlaceholder()} must be called.
 	 * 
 	 * @throws IllegalStateException
 	 *             if {@link #addImportsPlaceholder()} was not called before
 	 */
 	public void addImplicitImport(String simpleClassName) {
 		if (this.importsPlaceholder == null) {
 			throw new IllegalArgumentException("No placeholder for imports found.");
 		}
 		this.importsPlaceholder.addImplicitImport(simpleClassName);
 	}
 
 	public ImportsPlaceholder getImportsPlaceholder() {
 		return importsPlaceholder;
 	}
 
 	/**
 	 * Sets the import placeholder. This can be useful in rare cases where the
 	 * imports that are generated by this {@link JavaComposite} must be added to
 	 * another {@link JavaComposite}. Before this method is called,
 	 * {@link #addImportsPlaceholder()} must not be called.
 	 * 
 	 * @throws IllegalStateException
 	 *             if {@link #addImportsPlaceholder()} or
 	 *             {@link #setImportsPlaceholder(ImportsPlaceholder)} was called
 	 *             before.
 	 */
 	public void setImportsPlaceholder(ImportsPlaceholder importsPlaceholder) {
 		if (this.importsPlaceholder != null) {
 			throw new IllegalStateException("Placeholder for imports is already set.");
 		}
 		this.importsPlaceholder = importsPlaceholder;
 	}
 	
 	public String STRING() {
 		return getClassName(String.class);
 	}
 	
 	public String SET() {
 		return getClassName(Set.class);
 	}
 	
 	public String MAP() {
 		return getClassName(Map.class);
 	}
 	
 	public String LINKED_HASH_MAP() {
 		return getClassName(LinkedHashMap.class);
 	}
 	
 	public String LIST() {
 		return getClassName(List.class);
 	}
 	
 	public String COLLECTION() {
 		return getClassName(Collection.class);
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
 	 * lines that end with a semicolon).
 	 */
 	public void setInterfaceMode(boolean interfaceMode) {
 		this.interfaceMode = interfaceMode;
 	}
 }
