 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Aug 25, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package org.eclipse.jst.common.internal.annotations.registry;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.eclipse.jem.util.logger.proxy.Logger;
import org.eclipse.wst.common.internal.emf.annotations.core.AnnotationsCoreResources;
 
 /**
  * @author kelleyp Information on a single parameter for a tag. Parameters have names, and can be
  *         marked as being required. (ie, not optional)
  */
 public class TagAttribSpec {
 	private String attribName;
 	private int flags;
 	private String helpKey;
 	private int type = Type.TEXT;
 	private static final int FLG_REQUIRED = 1;
 	private String[] validValues;
 	private TagSpec tagSpec;
 
 	/* Enum for type */
 	public interface Type {
 		public static final int TEXT = 0;
 		public static final int BOOLEAN = 1;
 		public static final int JAVATYPE = 2;
 		public static final int ENUM = 3;
 	}
 
 	public class Unique {
 		public static final int MODULE = 0;
 		public static final int FILE = 1;
 		public static final int TYPE = 2;
 		public static final int METHOD = 3;
 		public static final int FIELD = 4;
 
 		private int scope = MODULE;
 
 		public int getScope() {
 			return scope;
 		}
 
 		public void setScope(int in) {
 			scope = in;
 		}
 	}
 
 	private Unique unique;
 
 	public Unique getUnique() {
 		return unique;
 	}
 
 	public boolean isUnique() {
 		return unique != null;
 	}
 
 	public void setUnique() {
 		unique = new Unique();
 	}
 
 	public void clearUnique() {
 		unique = null;
 	}
 
 	/**
 	 * Converts a string representation of a tag attribute type to the integer representation.
 	 * 
 	 * @param name
 	 * @return Integer type, defaults to TEXT if the type name is not recognized.
 	 */
 	public static int typeNameToType(String name) {
 		//TODO add enum
 		if (name != null) {
 			if (name.equalsIgnoreCase("text") || name.equalsIgnoreCase("string")) { //$NON-NLS-1$ //$NON-NLS-2$
 				return Type.TEXT;
 			} else if (name.equalsIgnoreCase("boolean") || name.equalsIgnoreCase("bool")) { //$NON-NLS-1$ //$NON-NLS-2$
 				return Type.BOOLEAN;
 			} else if (name.equalsIgnoreCase("javaType")) { //$NON-NLS-1$
 				return Type.JAVATYPE;
 			}
 		}
 		return Type.TEXT;
 	}
 
 	/**
 	 * Converts a type enum to a type string.
 	 * 
 	 * @param ty
 	 * @return
 	 */
 	public static String typeToTypeName(int ty) {
 		switch (ty) {
 			case Type.TEXT :
 				return "string"; //$NON-NLS-1$
 			case Type.BOOLEAN :
 				return "bool"; //$NON-NLS-1$
 			case Type.JAVATYPE :
 				return "javaType"; //$NON-NLS-1$
 			default :
 				return "string"; //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Constructs a TagAttribSpec with <code>name</code> as the attribute name.
 	 * 
 	 * @param name
 	 *            Name for the attribute. Must not be null.
 	 * @throws IllegalArgumentException
 	 *             if name is null.
 	 */
 	public TagAttribSpec(String name) throws IllegalArgumentException {
 		this(name, null);
 	}
 
 	public TagAttribSpec(String name, String hlpKey) {
 		setAttribName(name);
 		setHelpKey(hlpKey);
 	}
 
 	/**
 	 * Sets the type of this attribute.
 	 * 
 	 * @param t
 	 *            TEXT | BOOLEAN
 	 */
 	public void setType(int t) {
 		type = t;
 	}
 
 	/**
 	 * Sets the type of this attribute.
 	 * 
 	 * @param typename
 	 *            String representation, should be text or boolean.
 	 */
 	public void setType(String typename) {
 		type = typeNameToType(typename);
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public boolean valueIsJavaType() {
 		return type == Type.JAVATYPE;
 	}
 
 	public boolean valueIsText() {
 		return type == Type.TEXT;
 	}
 
 	public boolean valueIsBool() {
 		return type == Type.BOOLEAN;
 	}
 
 	/**
 	 * @return Name of the attribute.
 	 */
 	public String getAttribName() {
 		return attribName;
 	}
 
 	/**
 	 * Sets the attribute name. This can not be null.
 	 * 
 	 * @param name
 	 *            New name for the attribute.
 	 * @throws IllegalArgumentException
 	 *             if the name is null.
 	 */
 	public void setAttribName(String name) throws IllegalArgumentException {
 		if (name == null) {
 			throw new IllegalArgumentException(AnnotationsCoreResources.TagAttribSpec_6); 
 		}
 		attribName = name;
 	}
 
 	/**
 	 * 
 	 * @return true if this is a required attribute.
 	 */
 	public boolean isRequired() {
 		return (flags & FLG_REQUIRED) != 0;
 	}
 
 	/**
 	 * Sets the required flag for this attribute.
 	 */
 	public void setRequired() {
 		flags |= FLG_REQUIRED;
 	}
 
 	/**
 	 * Clears the required flag for this attribute.
 	 *  
 	 */
 	public void clearRequired() {
 		flags &= (~FLG_REQUIRED);
 	}
 
 	/**
 	 * 
 	 * @return The help key for this tag attribute. Should never return null.
 	 */
 	public String getTextKey(int aType) {
 		if (aType != TagSpec.HELP_TEXT) {
 			return null;
 		}
 
 		if (helpKey == null) {
 			helpKey = defaultHelpKey();
 		}
 		return helpKey;
 	}
 
 	/**
 	 * Formats the help text so it includes type and use information.
 	 */
 	public String transformLocalizedText(String txt) {
 		if (txt == null)
 			return txt;
 		StringBuffer buf = new StringBuffer(txt.length() + 50);
 
 		buf.append("<b>Type: "); //$NON-NLS-1$
 		buf.append(typeToTypeName(type));
 		buf.append(", Use: "); //$NON-NLS-1$
 		if (this.isRequired()) {
 			buf.append("required"); //$NON-NLS-1$
 		} else {
 			buf.append("optional"); //$NON-NLS-1$
 		}
 		if (this.isUnique()) {
 			buf.append(", unique:scope: "); //$NON-NLS-1$
 			buf.append(TagAttribSpec.uniqueScopeToString(this.getUnique().getScope())); //$NON-NLS-1$
 		}
 		buf.append("</b><p>"); //$NON-NLS-1$
 		buf.append(txt);
 		buf.append("</p>"); //$NON-NLS-1$
 		return buf.toString();
 
 	}
 
 	/**
 	 * 
 	 * @return The help key for this tag attribute. Should never return null.
 	 */
 	public String getHelpKey() {
 		return getTextKey(TagSpec.HELP_TEXT);
 	}
 
 	/**
 	 * Sets the help key. Setting this to null resets the help key to the default help key.
 	 * 
 	 * @param key
 	 */
 	public void setHelpKey(String key) {
 		helpKey = key;
 	}
 
 	/**
 	 * @return the default help key name for this tag.
 	 *  
 	 */
 	private String defaultHelpKey() {
 		return "ath." + attribName; //$NON-NLS-1$
 	}
 
 	public static int uniqueScopeFromString(String scopeStr) {
 		if (scopeStr != null) {
 			if (scopeStr.equalsIgnoreCase("module"))return TagAttribSpec.Unique.MODULE; //$NON-NLS-1$
 			if (scopeStr.equalsIgnoreCase("file"))return TagAttribSpec.Unique.FILE; //$NON-NLS-1$
 			if (scopeStr.equalsIgnoreCase("type"))return TagAttribSpec.Unique.TYPE; //$NON-NLS-1$
 			if (scopeStr.equalsIgnoreCase("method"))return TagAttribSpec.Unique.METHOD; //$NON-NLS-1$
 			if (scopeStr.equalsIgnoreCase("field"))return TagAttribSpec.Unique.FIELD; //$NON-NLS-1$
 		}
 		Logger.getLogger().logError(AnnotationsControllerResources.TagAttribSpec_1 + scopeStr); //$NON-NLS-1$
 		return TagAttribSpec.Unique.MODULE;
 	}
 
 	public static String uniqueScopeToString(int scope) {
 		switch (scope) {
 			case TagAttribSpec.Unique.MODULE :
 				return "module"; //$NON-NLS-1$
 			case TagAttribSpec.Unique.FILE :
 				return "file"; //$NON-NLS-1$
 			case TagAttribSpec.Unique.TYPE :
 				return "type"; //$NON-NLS-1$
 			case TagAttribSpec.Unique.METHOD :
 				return "method"; //$NON-NLS-1$
 			case TagAttribSpec.Unique.FIELD :
 				return "field"; //$NON-NLS-1$
 			default :
 				Logger.getLogger().logError(AnnotationsControllerResources.TagAttribSpec_1 + scope); //$NON-NLS-1$
 				return "unknown value"; //$NON-NLS-1$
 
 		}
 	}
 
 	/**
 	 * @return Returns the validValues.
 	 */
 	public String[] getValidValues() {
 		return validValues;
 	}
 
 	/**
 	 * @param validValues
 	 *            The validValues to set.
 	 */
 	public void setValidValues(String[] validValues) {
 		this.validValues = validValues;
 	}
 
 	public TagSpec getTagSpec() {
 		return tagSpec;
 	}
 
 	void setTagSpec(TagSpec tagSpec) {
 		this.tagSpec = tagSpec;
 	}
 
 	public String lookupTagHelp() throws MissingResourceException {
 		ResourceBundle b = getTagSpec().getResourceBundle();
 		if (b == null)
 			return null;
 		String key = getHelpKey();
 		String val = b.getString(getHelpKey());
 		if (val == key)
 			val = null;
 		return transformLocalizedText(val);
 	}
 }
