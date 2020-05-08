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
  * Created on Aug 22, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package org.eclipse.jst.common.internal.annotations.registry;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.osgi.framework.Bundle;
 
 /**
  * @author kelleyp
  * 
  * Singleton that parses the annotation tag information from the annotation-taghandler extension
  * point, and provides an interface for accessing it for other classes. Largely taken from the
  * AnnotationProcessor builder.
  */
 
 public class AnnotationTagRegistry {
 
 	/**
 	 * Set to true once we've read in the annotation tag information from the plugin registry.
 	 */
 	private static boolean initialized = false;
 	private static final String ANNOTATION_TAG_INFO = "org.eclipse.jst.common.annotations.controller.AnnotationTagInfo"; //$NON-NLS-1$
 
 	/**
 	 * List of tag specs for all of the tags.
 	 */
 	private static ArrayList allTagSpecs = new ArrayList() {
 		final private static long serialVersionUID = 8683452581122892190L;
 
 		private void scopeAll(Collection c, boolean forAdd) {
 			Iterator iter = c.iterator();
 			while (iter.hasNext()) {
 				TagSpec ts = (TagSpec) iter.next();
 				if (forAdd)
 					addScope(ts);
 				else
 					removeScope(ts);
 			}
 		}
 
 		private void addScope(TagSpec ts) {
 			if (ts == null)
 				return;
 			switch (ts.getScope()) {
 				case TagSpec.FIELD :
 					fieldTags.put(ts.getTagName(), ts);
 					break;
 				case TagSpec.METHOD :
 					methodTags.put(ts.getTagName(), ts);
 					break;
 				case TagSpec.TYPE :
 					typeTags.put(ts.getTagName(), ts);
 					break;
 			}
 		}
 
 		private void removeScope(TagSpec ts) {
 			if (ts == null)
 				return;
 			switch (ts.getScope()) {
 				case TagSpec.FIELD :
 					fieldTags.remove(ts.getTagName());
 					break;
 				case TagSpec.METHOD :
 					methodTags.remove(ts.getTagName());
 					break;
 				case TagSpec.TYPE :
 					typeTags.remove(ts.getTagName());
 					break;
 			}
 		}
 
 		public void add(int index, Object element) {
 			super.add(index, element);
			addScope((TagSpec)element);
 		}
 
 		public boolean add(Object o) {
			TagSpec newTagSpec = (TagSpec)o;
			// search for already existing tag spec with same name and same tag set name
			for (int i=0; i<this.size(); i++) {
				TagSpec tagSpec = (TagSpec) get(i);
				if (tagSpec.getTagName().equals(newTagSpec.getTagName())) {
					remove(tagSpec);
					removeScope(tagSpec);
				}	
			}
			// add the new tag spec
			addScope(newTagSpec);
			return super.add(newTagSpec);
 		}
 
 		public boolean addAll(Collection c) {
 			scopeAll(c, true);
 			return super.addAll(c);
 		}
 
 		public boolean addAll(int index, Collection c) {
 			scopeAll(c, true);
 			return super.addAll(index, c);
 		}
 
 		public Object remove(int index) {
 			Object result = super.remove(index);
 			removeScope((TagSpec) result);
 			return result;
 		}
 
 		public boolean remove(Object o) {
 			removeScope((TagSpec) o);
 			return super.remove(o);
 		}
 
 		public boolean removeAll(Collection c) {
 			scopeAll(c, false);
 			return super.removeAll(c);
 		}
 
 		public boolean retainAll(Collection c) {
 			Iterator iter = this.iterator();
 			while (iter.hasNext()) {
 				TagSpec ts = (TagSpec) iter.next();
 				if (!c.contains(ts))
 					removeScope(ts);
 			}
 			return super.retainAll(c);
 		}
 	};
 
 	/**
 	 * Map from a tag name to a InitTagInfo. Only live during up to the end of the init() method.
 	 */
 	private static Hashtable tagAttribs = new Hashtable();
 
 	/**
 	 * Division of tag names between allowed scopes.
 	 */
 	private static Map methodTags = new HashMap();
 
 	private static Map typeTags = new HashMap();
 
 	private static Map fieldTags = new HashMap();
 	
 	private static final String CLASS_PROP = "class"; //$NON-NLS-1$
 	private static final String DYNAMIC_INITIALIZER_EX_PT = "annotationTagDynamicInitializer"; //$NON-NLS-1$
 	private static final String ANNOTATIONS_CONTROLLER_NAMESPACE = "org.eclipse.jst.common.annotations.controller"; //$NON-NLS-1$
 
 	/**
 	 * Helper for init, parse the tag attributes for a AnnotationTagInfo tag.
 	 * 
 	 * @param elems
 	 *            Array of "attrib" configuration elements.
 	 * @param tagName
 	 *            Lowercased name of the tag these attributes are associated with.
 	 */
 	private static InitTagInfo parseTagAttribs(IConfigurationElement[] elems, String tagName, String scope) {
 		int i;
 		ArrayList attribList = new ArrayList();
 
 		InitTagInfo tagInf = new InitTagInfo(tagName, scope, attribList);
 
 		for (i = 0; i < elems.length; i++) {
 			IConfigurationElement elem = elems[i];
 
 			if (elem.getName().equalsIgnoreCase("attrib")) { //$NON-NLS-1$
 				TagAttribSpec tas = new TagAttribSpec(elem.getAttribute("name"), elem.getAttribute("description")); //$NON-NLS-1$ //$NON-NLS-2$
 				String use = elem.getAttribute("use"); //$NON-NLS-1$
 
 				tas.setType(elem.getAttribute("type")); //$NON-NLS-1$
 
 				// add valid values
 				if ("enum".equals(elem.getAttribute("type"))) { //$NON-NLS-1$ //$NON-NLS-2$
 					IConfigurationElement[] validValues = elem.getChildren("enumValues"); //$NON-NLS-1$
 					List valuesList = new ArrayList();
 					for (int j = 0; j < validValues.length; j++) {
 						String value = validValues[j].getAttribute("value"); //$NON-NLS-1$
 						valuesList.add(value);
 					}
 					String[] validValuesArray = new String[valuesList.size()];
 					validValuesArray = (String[]) valuesList.toArray(validValuesArray);
 
 					tas.setValidValues(validValuesArray);
 				}
 
 				if (use == null) {
 					tas.clearRequired();
 				} else if (use.equalsIgnoreCase("required")) { //$NON-NLS-1$
 					tas.setRequired();
 				} else if (use.equalsIgnoreCase("optional")) { //$NON-NLS-1$
 					tas.clearRequired();
 				} else {
 					// Unlikely, unless annotation extension spec changes
 					// without changes here.
 					System.err.println(AnnotationsControllerResources.getString("AnnotationTagRegistry.9") + use); //$NON-NLS-1$
 					return null;
 				}
 
 				IConfigurationElement[] elemUniqueArray = elem.getChildren("unique"); //$NON-NLS-1$
 				if (elemUniqueArray.length > 0) {
 					tas.setUnique();
 					if (elemUniqueArray[0].getAttribute("scope") != null) //$NON-NLS-1$
 						tas.getUnique().setScope(TagAttribSpec.uniqueScopeFromString(elemUniqueArray[0].getAttribute("scope"))); //$NON-NLS-1$
 					if (elemUniqueArray.length > 1) {
 						Logger.getLogger().logError(AnnotationsControllerResources.getString("TagAttribSpec.2") + elemUniqueArray.length); //$NON-NLS-1$
 					}
 				} else {
 					tas.clearUnique();
 				}
 
 				attribList.add(tas);
 			}
 		}
 		return tagInf;
 	}
 
 	/**
 	 * Return the tag set name from a full tag name.
 	 * 
 	 * @param name
 	 *            Full tag name (without the '@' at the beginning)
 	 * @return
 	 */
 	public static String tagSetFromTagName(String name) {
 		if (name == null)
 			return null;
 		int idx = name.lastIndexOf('.');
 
 		if (idx != -1)
 			return name.substring(0, idx);
 		return ""; //$NON-NLS-1$
 	}
 
 	/**
 	 * Return the short name from a full tag name.
 	 * 
 	 * @param name
 	 *            Full tag name (without the '@' at the beginning)
 	 * @return
 	 */
 	public static String tagFromTagName(String name) {
 		if (name == null)
 			return null;
 		int idx = name.indexOf('.');
 
 		if (idx != -1) {
 			return name.substring(idx + 1);
 		}
 		// Default to the whole name being the tagset.
 		return name;
 	}
 
 	/**
 	 * Reads in all of the tag attribute information from all annotation-tag-info extensions defined
 	 * in the system, and initializes the tagAttribs hashtable with them.
 	 * 
 	 * @param registry
 	 */
 	private static void readAllAttributeInfo(IExtensionPoint xp) {
 
 		if (xp == null) {
 			return;
 		}
 
 		IExtension[] exts = xp.getExtensions();
 		Bundle bundle = null;
 		for (int i = 0; i < exts.length; i++) {
 			IConfigurationElement[] elems = exts[i].getConfigurationElements();
 			bundle = Platform.getBundle(exts[i].getNamespace());
 			String identifier = exts[i].getUniqueIdentifier();
 
 			IConfigurationElement elem = null;
 			String tagName = null;
 			String scope = null;
 			String tagSet = null;
 			String fullTagName = null;
 			for (int j = 0; j < elems.length; j++) {
 				elem = elems[j];
 				if (!elem.getName().equalsIgnoreCase("AnnotationTagInfo")) { //$NON-NLS-1$
 					continue;
 				}
 				tagSet = elem.getAttribute("tagSet"); //$NON-NLS-1$
 				tagName = elem.getAttribute("tagName"); //$NON-NLS-1$
 				scope = elem.getAttribute("scope"); //$NON-NLS-1$
 				if (isNullOrEmpty(tagSet) || isNullOrEmpty(tagName) || isNullOrEmpty(scope)) {
 					Logger.getLogger().log(AnnotationsControllerResources.getString("AnnotationTagRegistry.10", new Object[]{identifier})); //$NON-NLS-1$ //$NON-NLS-2$
 					continue;
 				}
 				fullTagName = tagSet + "." + tagName; //$NON-NLS-1$
 
 				InitTagInfo tagInf = parseTagAttribs(elem.getChildren(), fullTagName.toLowerCase(), scope); //$NON-NLS-1$
 				String key = (fullTagName + "#" + scope).toLowerCase(); //$NON-NLS-1$
 				/*
 				 * There should only ever be one AnnotationTagInfo tag for any one annotation tag.
 				 */
 				if (tagAttribs.containsKey(key)) {
 					Logger.getLogger().log(AnnotationsControllerResources.getString("AnnotationTagRegistry.0") + tagName + "'."); //$NON-NLS-1$ //$NON-NLS-2$
 				} else {
 					tagInf.bundle = bundle;
 					tagAttribs.put(key, tagInf);
 				}
 			}
 		}
 	}
 
 	private static boolean isNullOrEmpty(String aString) {
 		return aString == null || aString.length() == 0;
 	}
 
 	/**
 	 * Reads tagSpec information in from the plugin registry. Taken from AnnotationProcessor.
 	 * 
 	 * @return True if initialization completed successfully.
 	 * @throws CoreException
 	 *             If there were problems reading the registry.
 	 */
 	private static/* synchronized */boolean init() throws CoreException {
 
 		 /* Prevent multiple initialization */
        if (initialized) {
            return true;
        }
       initializeStaticTagDefinitions();
       initiaizeDynamicTagDefinitions();
        initialized = true;
 
        /* Don't need this anymore */
        tagAttribs = null;
 
        return true;
    }
 	
 	private static void initializeStaticTagDefinitions() throws CoreException {
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 
 		// TODO: Not even checking the tagset extension point yet.
 		IExtensionPoint xp = registry.getExtensionPoint(ANNOTATION_TAG_INFO);
 
         if (xp == null)
             return;
 
         IExtension[] x = xp.getExtensions();
 
         /* Get all tag attribute information */
         readAllAttributeInfo(xp);
 		for (int j = 0; j < x.length; j++) {
 			IConfigurationElement[] tagSpecs = x[j].getConfigurationElements();
 			for (int i = 0; i < tagSpecs.length; i++) {
 				IConfigurationElement tagSpec = tagSpecs[i];
 				String tagName = tagSpec.getAttribute("tagSet") + "." + tagSpec.getAttribute("tagName"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				String scope = tagSpec.getAttribute("scope"); //$NON-NLS-1$
 				String multiplicity = tagSpec.getAttribute("multiplicity"); //$NON-NLS-1$
 				TagSpec ts = null;
 				if (multiplicity != null)
 					ts = new TagSpec(tagName, TagSpec.scopeFromString(scope), TagSpec.multiplicityFromString(multiplicity));
 				else
 					ts = new TagSpec(tagName, TagSpec.scopeFromString(scope), TagSpec.Multiplicity.ONE);
 				String key = (tagName + "#" + scope).toLowerCase(); //$NON-NLS-1$
 				InitTagInfo tagInf = (InitTagInfo) tagAttribs.get(key);
 
 				allTagSpecs.add(ts);
 
 				if (tagInf != null) {
 					ts.setAttributes(tagInf.attributes);
 					ts.setBundle(tagInf.bundle);
 				}
 			}
 		}
     }
 
 	private static void initiaizeDynamicTagDefinitions() {
 		IExtensionPoint xp = Platform.getExtensionRegistry().getExtensionPoint(ANNOTATIONS_CONTROLLER_NAMESPACE, DYNAMIC_INITIALIZER_EX_PT);
 		if (xp == null)
 			return;
 		IExtension[] extensions = xp.getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			IExtension extension = extensions[i];
 			IConfigurationElement[] elements = extension.getConfigurationElements();
 			for (int j = 0; j < elements.length; j++) {
 				try {
 					AnnotationTagDynamicInitializer initializer = (AnnotationTagDynamicInitializer) elements[j].createExecutableExtension(CLASS_PROP);
 					initializer.registerTags();
 				} catch (CoreException e) {
 					Logger.getLogger().logError(e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @return List of AnnotationTagRegistry.TagSpecs for all tags.
 	 * @throws CoreException
 	 *             If there were problems reading the initialization data from the plugin registry.
 	 */
 	public static synchronized List getAllTagSpecs() {
 		return allTagSpecs;
 	}
 
 	public static synchronized boolean isMethodTag(String tagName) {
 		return methodTags.containsKey(tagName);
 	}
 
 	public static synchronized boolean isFieldTag(String tagName) {
 		return fieldTags.containsKey(tagName);
 	}
 
 	public static synchronized boolean isTypeTag(String tagName) {
 		return typeTags.containsKey(tagName);
 	}
 
 	/**
 	 * Answers the tagspec for the specified method tag name.
 	 * 
 	 * @param tagName
 	 *            Full name for a tag.
 	 * @return a TagSpec for the tag name, or null if no tag with that name is registered.
 	 */
 	public static synchronized TagSpec getMethodTag(String tagName) {
 		return (TagSpec) methodTags.get(tagName);
 	}
 
 	/**
 	 * Answers the tagspec for the specified field tag name.
 	 * 
 	 * @param tagName
 	 *            Full name for a tag.
 	 * @return a TagSpec for the tag name, or null if no tag with that name is registered.
 	 */
 	public static synchronized TagSpec getFieldTag(String tagName) {
 		return (TagSpec) fieldTags.get(tagName);
 	}
 
 	/**
 	 * Answers the tagspec for the specified type tag name.
 	 * 
 	 * @param tagName
 	 *            Full name for a tag.
 	 * @return a TagSpec for the tag name, or null if no tag with that name is registered.
 	 */
 	public static synchronized TagSpec getTypeTag(String tagName) {
 		return (TagSpec) typeTags.get(tagName);
 	}
 
 	private static class InitTagInfo {
 		private String name;
 
 		private List attributes;
 
 		private Bundle bundle;
 
 		private String scope;
 
 		public InitTagInfo(String name, String scope, List att) {
 			attributes = att;
 			this.name = name;
 			this.scope = scope;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			else if (!(obj instanceof InitTagInfo))
 				return false;
 
 			return name.equals(((InitTagInfo) obj).name) || (scope.equals(((InitTagInfo) obj).name));
 
 		}
 	}
 
 	static {
 		try {
 			AnnotationTagRegistry.init();
 		} catch (CoreException e) {
 			Logger.getLogger().logError(AnnotationsControllerResources.getString("AnnotationTagRegistry_ERROR_1")); //$NON-NLS-1$
 			Logger.getLogger().logError(e);
 		}
 	}
 }
