 package org.muis.core;
 
 /** Facilitates instantiating MUIS classes via their namespace/tagname mappings */
 public class MuisClassView {
 	private final MuisEnvironment theEnvironment;
 
 	private final MuisClassView theParent;
 
 	private final MuisToolkit theMemberToolkit;
 
 	private java.util.HashMap<String, MuisToolkit> theNamespaces;
 
 	private boolean isSealed;
 
 	/**
 	 * @param env The environment to create the class view in
 	 * @param parent The parent for this class view
 	 * @param memberToolkit The toolkit that the owner of class view member belongs to
 	 */
 	public MuisClassView(MuisEnvironment env, MuisClassView parent, MuisToolkit memberToolkit) {
 		if(env == null)
 			throw new NullPointerException("environment is null");
 		theEnvironment=env;
 		theParent = parent;
 		theMemberToolkit = memberToolkit;
 		theNamespaces = new java.util.HashMap<>();
 	}
 
 	/** @return The environment that this class view is in */
 	public MuisEnvironment getEnvironment() {
 		return theEnvironment;
 	}
 
 	/** @return This class view's parent */
 	public MuisClassView getParent() {
 		return theParent;
 	}
 
 	/** @return Whether this class view has been sealed or not */
 	public final boolean isSealed() {
 		return isSealed;
 	}
 
 	/** Seals this class view such that it cannot be modified */
 	public final void seal() {
 		isSealed = true;
 	}
 
 	/**
 	 * Maps a namespace to a toolkit under this view
 	 *
 	 * @param namespace The namespace to map
 	 * @param toolkit The toolkit to map the namespace to
 	 * @throws MuisException If this class view is sealed
 	 */
 	public void addNamespace(String namespace, MuisToolkit toolkit) throws MuisException {
 		if(isSealed)
 			throw new MuisException("Cannot modify a sealed class view");
 		theNamespaces.put(namespace, toolkit);
 	}
 
 	/**
 	 * Gets the toolkit mapped to a given namespace
 	 *
 	 * @param namespace The namespace to get the toolkit for
 	 * @return The toolkit mapped to the given namespace under this view, or null if the namespace is not mapped in this view
 	 */
 	public MuisToolkit getToolkit(String namespace) {
 		MuisToolkit ret = theNamespaces.get(namespace);
 		if(ret == null) {
 			if(theParent != null)
 				ret = theParent.getToolkit(namespace);
 			else if(namespace == null)
 				ret = theEnvironment.getCoreToolkit();
 		}
 		return ret;
 	}
 
 	/** @return The toolkits that may be used without specifying a namespace */
 	public MuisToolkit [] getScopedToolkits() {
 		java.util.LinkedHashSet<MuisToolkit> ret = new java.util.LinkedHashSet<>();
 		if(theParent != null) {
 			for(MuisToolkit tk : theParent.getScopedToolkits())
 				ret.add(tk);
 		} else
 			ret.add(theEnvironment.getCoreToolkit());
 		if(theMemberToolkit != null)
 			ret.add(theMemberToolkit);
 		return ret.toArray(new MuisToolkit[ret.size()]);
 	}
 
 	/**
 	 * Gets the toolkit that would load a class for a qualified name
 	 *
 	 * @param qName The qualified name to get the toolkit for
 	 * @return The toolkit that can load the type with the given qualified name, or null if no such tag has been mapped in this view
 	 */
 	public MuisToolkit getToolkitForQName(String qName) {
 		int idx = qName.indexOf(":");
 		if(idx < 0) {
 			for(MuisToolkit toolkit : getScopedToolkits()) {
 				if(toolkit.getMappedClass(qName) != null)
 					return toolkit;
 			}
 			return null;
 		}
 		else
 			return getToolkit(qName.substring(0, idx));
 	}
 
 	/** @return All namespaces that have been mapped to toolkits in this class view */
 	public String [] getMappedNamespaces() {
 		return theNamespaces.keySet().toArray(new String[theNamespaces.size()]);
 	}
 
 	/**
 	 * Gets the fully-qualified class name mapped to a qualified tag name (namespace:tagName)
 	 *
 	 * @param qName The qualified tag name of the class to get
 	 * @return The fully-qualified class name mapped to the qualified tag name, or null if no such class has been mapped in this view
 	 */
 	public String getMappedClass(String qName) {
 		int idx = qName.indexOf(':');
 		if(idx < 0)
 			return getMappedClass(null, qName);
 		else
 			return getMappedClass(qName.substring(0, idx), qName.substring(idx + 1));
 	}
 
 	/**
 	 * Gets the fully-qualified class name mapped to a tag name in this class view's domain
 	 *
 	 * @param namespace The namespace of the tag
 	 * @param tag The tag name
 	 * @return The fully-qualified class name mapped to the tag name, or null if no such class has been mapped in this domain
 	 */
 	public String getMappedClass(String namespace, String tag) {
 		if(namespace == null) {
 			for(MuisToolkit toolkit : getScopedToolkits()) {
 				if(toolkit.getMappedClass(tag) != null)
 					return toolkit.getMappedClass(tag);
 			}
 			return null;
 		} else {
 			MuisToolkit toolkit = getToolkit(namespace);
 			if(toolkit == null)
 				return null;
 			return toolkit.getMappedClass(tag);
 		}
 	}
 
 	/**
 	 * A combination of {@link #getMappedClass(String)} and {@link MuisToolkit#loadClass(String, Class)} for simpler code.
 	 *
 	 * @param <T> The type of interface or superclass to return the class as
 	 * @param qName The qualified tag name
 	 * @param superClass The superclass or interface class to cast the class as an subclass of
 	 * @return The loaded class, as an implementation or subclass of the interface or super class; or null if no such class has been mapped
 	 *         in this domain
 	 * @throws MuisException If the class cannot be found, cannot be loaded, or is not an subclass/implementation of the given class or
 	 *             interface
 	 */
 	public <T> Class<? extends T> loadMappedClass(String qName, Class<T> superClass) throws MuisException {
 		int idx = qName.indexOf(':');
 		String ns, tag;
 		if(idx < 0) {
 			ns = null;
 			tag = qName;
 		} else {
 			ns = qName.substring(0, idx);
 			tag = qName.substring(idx + 1);
 		}
 		return loadMappedClass(ns, tag, superClass);
 	}
 
 	/**
 	 * A combination of {@link #getMappedClass(String, String)} and {@link MuisToolkit#loadClass(String, Class)} for simpler code.
 	 *
 	 * @param <T> The type of interface or superclass to return the class as
 	 * @param namespace The namespace of the tag
 	 * @param tag The tag name
 	 * @param superClass The superclass or interface class to cast the class as an subclass of
 	 * @return The loaded class, as an implementation or subclass of the interface or super class; or null if no such class has been mapped
 	 *         in this domain
 	 * @throws MuisException If the class cannot be found, cannot be loaded, or is not an subclass/implementation of the given class or
 	 *             interface
 	 */
 	public <T> Class<? extends T> loadMappedClass(String namespace, String tag, Class<T> superClass) throws MuisException {
 		if(namespace != null) {
 			MuisToolkit toolkit = getToolkit(namespace);
 			if(toolkit == null)
 				throw new MuisException("No toolkit mapped to namespace " + namespace);
 			String className = toolkit.getMappedClass(tag);
 			if(className == null)
 				throw new MuisException("No class mapped to " + tag + " for namespace " + namespace + " (toolkit " + toolkit.getName()
 					+ ")");
 			return toolkit.loadClass(className, superClass);
 		} else {
 			for(MuisToolkit toolkit : getScopedToolkits()) {
 				String className = toolkit.getMappedClass(tag);
 				if(className != null)
 					return toolkit.loadClass(className, superClass);
 			}
 			throw new MuisException("No class mapped to " + tag + " in scoped namespaces");
 		}
 	}
 }
