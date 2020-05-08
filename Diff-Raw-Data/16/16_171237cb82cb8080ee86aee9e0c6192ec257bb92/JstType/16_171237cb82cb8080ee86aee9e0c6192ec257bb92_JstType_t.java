 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jst.declaration;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectStreamException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.vjet.dsf.jst.BaseJstNode;
 import org.eclipse.vjet.dsf.jst.IJstDoc;
 import org.eclipse.vjet.dsf.jst.IJstGlobalVar;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstOType;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.ISynthesized;
 import org.eclipse.vjet.dsf.jst.token.IStmt;
 import org.eclipse.vjet.dsf.jst.traversal.IJstNodeVisitor;
 import org.eclipse.vjet.dsf.common.Z;
 
 /**
  * Implementation of IJstType
  * Please note:
  * 1. JstType supports following types:
  * <li>Class</li>
  * <li>Abstract Class</li>
  * <li>Interface</li>
  * <li>Enum</li>
  * <li>Module</li>
  * 2. JstType is mainly a data structure that support a superset of characteristics needed by individual types.
  * It doesn't provide validation on whether a certain characteristic is supported or not by a specific type.
  * Validation should be provided externally.
  * 
  * TODO: review synchronization on lists and maps for safe and efficient operations
 
  * 
  */
 public class JstType extends BaseJstNode implements IJstType {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static final String EMPTY = "";
 	
 	private Category m_category = Category.CLASS;
 	private JstPackage m_pkg;
 	private String m_simpleName;
 	private String m_alias;
 	private String m_aliasTypeName;
 	List<IJstOType> m_otypes = new ArrayList<IJstOType>();
 
 	// using JstTypeReference not JstType to prevent owneriship (getParent() === this) of the imported type
 	private Map<String, IJstTypeReference> m_imports = null; // new LinkedHashMap<String, IJstTypeReference>();
 	private Map<String, IJstTypeReference> m_inactiveImports = null; // new LinkedHashMap<String, IJstTypeReference>(); 
 	private Map<String, IJstTypeReference> m_fullyQualifiedImports = null; //  new LinkedHashMap<String, IJstTypeReference>();
 	private List<IJstTypeReference> m_extends;
 	private List<IJstTypeReference> m_satisfies;	
 	private List<IJstTypeReference> m_expects;
 	private List<IJstTypeReference> m_mixins;
 	private Map<String, Object> m_options = null; // new LinkedHashMap<String, Object>();
 	
 	private JstModifiers m_modifiers = new JstModifiers();
 
 	private List<IJstProperty> m_ptys = null; // new ArrayList<IJstProperty>();
 	private List<IJstGlobalVar> m_gvars = null; // new ArrayList<IJstGlobalVar>();
 	private List<IJstMethod> m_mtds = null; // new ArrayList<IJstMethod>();
 	private JstMethod m_constructor;
 	
 	private List<IStmt> m_staticInits;
 	private List<IStmt> m_instanceInits;
 		
 	private List<JstType> m_innerTypes;
 	private JstType m_outerType;
 	
 	private List<JstType> m_siblingTypes;
 	private JstType m_containingType;
 	
 	private boolean m_isLocalType = false;
 	
 	private List<IJstProperty> m_enumValues;
 
 	private VarTable m_varTable;
 	
 	private Map<String,JstParamType> m_paramTypes;
 	
 	transient private Info m_info = new Info();
 	
 	private IJstDoc m_doc;
 
 	private JstBlock m_initBlock;
 
 	private boolean m_impliedImport;
 	
 	private boolean m_isMetaType;// = false;
 	
 	private boolean m_isFakeType = false;
 
 	private List<IJstType> m_secondaryTypes;
 
 	private boolean m_singleton;
 
 	//
 	// Constructor
 	//
 	JstType() {
 		getModifiers().setStatic(true).setPublic();
 	}
 
 	JstType(final String name) {
 		this();
 		if (name == null){
 			return;
 		}
 
 		setName(name);
 	}
 
 	/**
 	 * Sets the given name
 	 * 
 	 * @param name
 	 */
 	public void setName(final String name) {
 		int index = name.lastIndexOf(".");
 		if (index != -1) {
 			setPackage(new JstPackage(name.substring(0, index)));
 			setSimpleName(name.substring(index+1));
 		} else {
 			setPackage(new JstPackage());
 			setSimpleName(name);
 		}
 	}
 
 	JstType(final JstPackage pkg, final String simpleName) {
 		this();
 		m_pkg = pkg;
 		setSimpleName(simpleName);
 	}
 
 	JstType(final JstType parent) {
 		this();
 		setOuterType(parent);
 	}
 
 	//
 	// Satisfy IJstType
 	//
 	/**
 	 * @see IJstType#getPackage()
 	 */
 	public JstPackage getPackage(){
 		if (m_pkg != null){
 			return m_pkg;
 		}
 		if (m_outerType != null) {
 			JstPackage pkg = m_outerType.getPackage();
 			JstPackage pkg4Inner = new JstPackage(m_outerType.getName());
 			if (pkg != null) {
 				pkg4Inner.setGroupName(pkg.getGroupName());
 			}
 			return pkg4Inner;
 		}
 		return null;
 	}
 	
 	/**
 	 * @see IJstType#getSimpleName()
 	 */
 	public String getSimpleName(){
 		return m_simpleName;
 	}
 	
 	/**
 	 * @see IJstType#getName()
 	 */
 	public String getName(){
 		JstPackage pkg = getPackage();
 		if (pkg != null && pkg.getName() != null && pkg.getName().length() > 0){
 			return pkg.getName() + "." + getSimpleName();
 //			return pkg.getName() + ( (m_outerType!=null)?"$":".") + getSimpleName();
 		}
 		else {
 			return getSimpleName();
 		}
 	}
 	
 	/**
 	 * @see IJstType#getAlias()
 	 */
 	public String getAlias() {
 		if (m_alias == null) {
 			return getName();
 		}
 		return m_alias;
 	}
 	
 	/**
 	 * @see IJstType#getAliasTypeName()
 	 */
 	public String getAliasTypeName() {
 		return m_aliasTypeName;
 	}
 
 	public Category getCategory(){
 		return m_category;
 	}
 	
 	/**
 	 * @see IJstType#isClass()
 	 */
 	public boolean isClass(){
 		return m_category == Category.CLASS;
 	}
 
 	/**
 	 * @see IJstType#isInterface()
 	 */
 	public boolean isInterface(){
 		return m_category == Category.INTERFACE;
 	}
 
 	/**
 	 * @see IJstType#isEnum()
 	 */
 	public boolean isEnum(){
 		return m_category == Category.ENUM;
 	}
 	
 	
 	/**
 	 * @see IJstType#isMixin()
 	 */
 	public boolean isMixin() {
 		return m_category == Category.MODULE;
 	}
 	
 	/**
 	 * @see IJstType#isOType()
 	 */
 	public boolean isOType() {
 		return m_category == Category.OTYPE;
 	}
 	
 	/**
 	 * @see IJstType#isFType()
 	 */
 	public boolean isFType() {
 		return m_category == Category.FTYPE;
 	}
 	
 	@Override
 	public boolean isMetaType() {
 		boolean isMeta = m_isMetaType || isOType();
 		if (isMeta) {
 			return isMeta;
 		}
 		IJstType outerType = getOuterType();
 		return outerType == null ? false : outerType.isMetaType();
 	}
 	
 	public void setMetaType(boolean set) {
 		m_isMetaType = set;
 	}
 	
 	@Override
 	public boolean isFakeType() {
 		return m_isFakeType;
 	}
 	
 	public void setFakeType(boolean set) {
 		m_isFakeType = set;
 	}
 	
 	/**
 	 * @see IJstType#isAnonymous()
 	 */
 	public boolean isAnonymous(){
 		return getParentNode() != null 
 		&& !(getParentNode() instanceof IJstType)
 		&& getName() == null;
 	}
 	
 	/**
 	 * @see IJstType#hasImport(String)
 	 */
 	public boolean hasImport(final String typeName){
 		return getImportRef(typeName) != null;
 	}
 	
 	/**
 	 * @see IJstType#getImport(String)
 	 */
 	public IJstType getImport(final String typeName) {
 		IJstTypeReference t = getImportRef(typeName);
 		return (t != null)? t.getReferencedType() : null;
 	}
 	
 	
 	public IJstTypeReference getImportRef(final String typeName) {
 		if (typeName == null || m_imports == null || m_imports.isEmpty()) {
 			return null;
 		}
 		synchronized (this){
 			IJstTypeReference t = m_imports.get(typeName);
 			return (t != null)? t : null;
 		}
 	}
 	
 	
 	/**
 	 * @see IJstType#getImports()
 	 */
 	public List<IJstType> getImports() {
 		List<IJstType> list = new ArrayList<IJstType>();
 		synchronized(this){			
 			if (m_imports==null || m_imports.size()== 0) {
 				return Collections.emptyList();
 			}
 			
 			for (IJstTypeReference tr: m_imports.values()){
 				IJstType type = tr.getReferencedType();
 				boolean isDup = false;
 				for (IJstType t: list){
 					if (type.getName().equals(t.getName())){
 						isDup = true;
 						break;
 					}
 				}
 				if (!isDup){
 					list.add(type);
 				}		
 			}
 		}
 		
 		return list;
 	}
 
 	public IJstType getInactiveImport(String typeName) {
 		if (typeName == null || m_inactiveImports==null || m_inactiveImports.isEmpty()) {
 			return null;
 		}
 		synchronized (this){
 			IJstTypeReference t = m_inactiveImports.get(typeName);
 			return (t != null)? t.getReferencedType() : null;
 		}
 	}
 
 	
 	public IJstTypeReference getInactiveImportRef(final String typeName) {
 		if (typeName == null ||  m_inactiveImports==null || m_inactiveImports.isEmpty()) {
 			return null;
 		}
 		synchronized (this){
 			IJstTypeReference t = m_inactiveImports.get(typeName);
 			return (t != null)? t : null;
 		}
 	}
 
 	public List<? extends IJstType> getInactiveImports() {
 		List<IJstType> list = new ArrayList<IJstType>();
 		synchronized(this){			
 			if ( m_inactiveImports==null || m_inactiveImports.isEmpty()) {
 				return Collections.emptyList();
 			}
 			
 			for (IJstTypeReference tr: m_inactiveImports.values()){
 				IJstType type = tr.getReferencedType();
 				boolean isDup = false;
 				for (IJstType t: list){
 					if (type.getName().equals(t.getName())){
 						isDup = true;
 						break;
 					}
 				}
 				if (!isDup){
 					list.add(type);
 				}		
 			}
 		}
 		
 		return list;
 	}
 	
 	
 	public void addOType(IJstOType otype) {
 		assert isOType();
 		//TODO: add otype to tree
 		if (otype instanceof JstType) {
 			((JstType)otype).setParent(this);
 		}
 		m_otypes.add(otype);
 	}
 	public List<IJstOType> getOTypes() {
 		return m_otypes;
 	}
 	
 	public IJstOType getOType(String name) {
 		if (name==null) {
 			return null;
 		}
 		for (IJstOType otype : m_otypes) {
 			if (otype.getSimpleName().equals(name)) {
 				return otype;
 			}
 		}
 		return null;
 	}
 	public List<IJstTypeReference> getImportsRef() {
 		List<IJstTypeReference> list = new ArrayList<IJstTypeReference>();
 		synchronized(this){			
 			if (m_imports == null || m_imports.isEmpty()) {
 				return Collections.emptyList();
 			}
 			
 			for (IJstTypeReference tr: m_imports.values()){
 				if (!list.contains(tr)) {
 					list.add(tr);
 				}		
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * @see IJstType#getExtend()
 	 */
 	public IJstType getExtend() {
 		IJstTypeReference t = getExtendRef();
 		return (t != null)? t.getReferencedType():null;
 	}
 	
 	public IJstTypeReference getExtendRef() {
 		if (m_extends == null) {
 			return null;
 		}
 		synchronized(this){
 			if (!m_extends.isEmpty()){
 				return m_extends.get(0);
 			}
 		}
 		return null;
 	}
 
 	static List<IJstType> unwrap(List<IJstTypeReference> l) {
 		if (l == null || l.isEmpty()) {
 			return Collections.emptyList();
 		}
 		List<IJstType> ret = new ArrayList<IJstType>(l.size());
 		for (IJstTypeReference t: l) {
 			ret.add(t.getReferencedType());
 		}
 		return ret;
 	}
 	
 	static boolean contains(IJstType t, List<IJstTypeReference> l) {
 		for (IJstTypeReference tr: l) {
 			if (tr.getReferencedType() == t)
 				return true;
 		}
 		return false;
 	}
 	
 	public List<IJstTypeReference> getExtendsRef() {
 		if (m_extends == null){
 			return Collections.emptyList();
 		}
 		return Collections.unmodifiableList(m_extends);
 	}
 	
 	/**
 	 * @see IJstType#getExtends()
 	 */
 	public List<IJstType> getExtends() {
 		if (m_extends == null){
 			return Collections.emptyList();
 		}
 		synchronized(this){
 			return unwrap(m_extends);
 		}
 	}
 
 	/**
 	 * @see IJstType#getSatisfies()
 	 */
 	public List<IJstType> getSatisfies() {
 		if (m_satisfies == null &&( m_mixins==null || this.getMixinsRef().size() == 0)) {
 			return Collections.emptyList();
 		}
 		List<IJstTypeReference> list = m_satisfies;
 		if (this.getMixinsRef().size() > 0) {
 			if (m_satisfies == null)
 				list = new ArrayList<IJstTypeReference>();
 			else
 				list = new ArrayList<IJstTypeReference>(m_satisfies);
 			getMergedMixinSatisfies(list);
 		} else if (m_satisfies == null) {
 			return Collections.emptyList();
 		}
 		return unwrap(list);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void getMergedMixinSatisfies(List<IJstTypeReference> satisfies) {
 		for (IJstTypeReference mixin : this.getMixinsRef()) {
 			List<IJstTypeReference> mixinSatisfies = 
 				(List<IJstTypeReference>)((IJstType)mixin.getReferencedType()).getSatisfiesRef();
 			if (mixinSatisfies == null) continue;
 			
 			for (IJstTypeReference sat : mixinSatisfies) {
 				satisfies.add(sat);
 			}
 		}
 	}
 
 	public List<IJstTypeReference> getSatisfiesRef() {
 		if (m_satisfies == null) {
 			return Collections.emptyList();
 		}
 		return Collections.unmodifiableList(m_satisfies);
 	}
 	
 	/**
 	 * @see IJstType#getExpects()
 	 */
 	public List<IJstType> getExpects() {
 		
 		if (m_expects == null || m_expects.size() == 0) {
 			return Collections.emptyList();
 		}
 		List<IJstTypeReference> list = m_expects;
 		if (this.getExpectsRef().size() > 0) {
 			if (m_expects == null)
 				list = new ArrayList<IJstTypeReference>();
 			else
 				list = new ArrayList<IJstTypeReference>(m_expects);
 		} 
 		return unwrap(list);
 		
 	}
 
 	/**
 	 * @see IJstType#getModifiers()
 	 */
 	public JstModifiers getModifiers() {
 		return m_modifiers;
 	}
 
 	/**
 	 * @see IJstType#getProperty(String)
 	 */
 	public IJstProperty getProperty(final String name) {
 		IJstProperty pty = getProperty(name, true);
 		if (pty != null){
 			return pty;
 		}
 		return getProperty(name, false);
 	}
 
 	/**
 	 * @see IJstType#getProperty(String,boolean)
 	 */
 	public IJstProperty getProperty(final String name, boolean isStatic) {
 		return getProperty(name, isStatic, false);
 	}
 
 	/**
 	 * @see IJstType#getProperty(String,boolean,boolean)
 	 */
 	public IJstProperty getProperty(final String name, boolean isStatic, boolean recursive) {
 		if (name == null){
 			return null;
 		}
 		for (IJstProperty pty: getProperties()){
 			if (pty.isStatic() == isStatic && name.equals(pty.getName().getName())){
 				return pty;
 			}
 		}
 		if (recursive && !getExtends().isEmpty()){
 			IJstProperty pty;
 			for (IJstType t: getExtends()){
 				if (t == this) continue; // prevent recursive on itself
 				
 				pty = t.getProperty(name, isStatic, recursive);
 				if (pty != null){
 					return pty;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * @see IJstType#getProperties()
 	 */
 	public List<IJstProperty> getProperties(){
 		if (m_mixins != null &&  this.getMixinsRef().size() > 0) {
 			return getMixinMergedProperties();
 		}
		if(m_ptys==null ){
			return Collections.EMPTY_LIST;
		}
 		return Collections.unmodifiableList(m_ptys);
 	}
 
 	public List<IJstProperty> getMixinMergedProperties() {
 		if(m_ptys==null){
 			m_ptys = new ArrayList<IJstProperty>();
 		}
 		List<IJstProperty> list = new ArrayList<IJstProperty>(m_ptys);
 		
 		
 //		if(this.getMixinsRef().equals(this) || this.getMixinsRef().get(0).getReferencedType()==null ||  this.getMixinsRef().get(0).getReferencedType().getName().equals("")){
 //			return list;
 //		}
 		
 		for (IJstTypeReference module : this.getMixinsRef()) {
 			
 			if(this.equals(module.getReferencedType())){
 				continue;
 			}
 			
 			//Create proxy properties for mixin properties
 			for (IJstProperty prop : module.getReferencedType().getProperties()) {
 				if (prop instanceof ISynthesized || prop instanceof JstProxyProperty) {
 					continue;
 				}
 				list.add(new JstProxyProperty(prop));
 			}
 		}
 		return list;
 		
 	}
 	/**
 	 * @see IJstType#getProperties(boolean)
 	 */
 	public List<IJstProperty> getProperties(boolean isStatic) {
 		if (isStatic) {
 			return getStaticProperties();
 		} else {
 			return getInstanceProperties();
 		}
 	}
 
 	/**
 	 * @see IJstType#getAllPossibleProperties(boolean,boolean)
 	 * Getting all possible properties from inherited types
 	 * Will not include private properties of inherited types
 	 * Will include properties from interfaces that are satisfies. 
 	 * This API is not to be used to verify access correctness
 	 * 
 	 */
 	public List<IJstProperty> getAllPossibleProperties(boolean isStatic, boolean recursive) {
 		List<IJstProperty> list = new ArrayList<IJstProperty>();
 		list.addAll(getProperties(isStatic));
 		//Jack: Memory overflow when "A extends B extends A" -- this should be han
 		if (recursive) {
 			List<IJstType> types = new ArrayList<IJstType>();
 			gatherExtendTypes(types, this);
 			gatherSatisfiesTypes(types,this);
 			if (!types.isEmpty()){
 				for (IJstType t: types){
 					for (IJstProperty p: t.getProperties(isStatic)){
 						if (list.contains(p)){
 							continue;
 						}
 						if(!p.getModifiers().isPrivate()){
 							list.add(p);
 						}
 					}
 				}
 			}
 			
 		}
 		return list;
 	}
 	
 	public List<IJstType> getAllDerivedTypes(){
 		List<IJstType> types = new ArrayList<IJstType>();
 		for(IJstType mixin: getMixins()){
 			if(mixin instanceof JstProxyType){
 				JstProxyType pt = (JstProxyType)mixin;
 				types.add(pt.getType());
 			}else if (mixin instanceof JstType){
 				types.add(mixin);
 			}
 				
 			
 			//types.addAll(getMixins());
 		}
 		gatherExtendTypes(types, this);
 		gatherSatisfiesTypes(types,this);
 		return types;
 	}
 	
 
 	/**
 	 * @see IJstType#getEnumValue(String)
 	 */
 	public IJstProperty getEnumValue(final String name) {
 		if (null == name) {
 			return null;
 		}
 		for (IJstProperty prop : this.getEnumValues()) {
 			if (name.equals(prop.getName().getName())) {
 				return prop;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see IJstType#hasStaticProperties()
 	 */
 	public boolean hasStaticProperties() {
 		return getStaticProperties().size() > 0;
 	}
 
 	/**
 	 * @see IJstType#hasStaticProperty(String,boolean)
 	 */
 	public boolean hasStaticProperty(final String varName, boolean recursive) {
 		if (varName == null){
 			return false;
 		}
 		boolean isStatic =  getStaticProperty(varName) != null;
 		if (isStatic || !recursive || getExtends().isEmpty()){
 			return isStatic;
 		}
 		for (IJstType base: getExtends()){
 			if (base.hasStaticProperty(varName, recursive)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @see IJstType#getStaticProperty(String)
 	 */
 	public IJstProperty getStaticProperty(final String name) {
 		if (name == null){
 			return null;
 		}
 		for (IJstProperty pty: getStaticProperties()){
 			if (name.equals(pty.getName().getName())){
 				return pty;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see IJstType#getStaticProperty(String,boolean)
 	 */
 	public IJstProperty getStaticProperty(final String name, boolean recursive) {
 		if (name == null){
 			return null;
 		}
 		for (IJstProperty pty: getStaticProperties()){
 			if (name.equals(pty.getName().getName())){
 				return pty;
 			}
 		}
 		IJstProperty pty = null;
 		if (recursive && !getExtends().isEmpty()){
 			for (IJstType base: getExtends()){
 				pty = base.getStaticProperty(name, recursive);
 				if (pty != null){
 					return pty;
 				}
 			}
 		}
 		if (recursive && !getSatisfies().isEmpty()){
 			for (IJstType base: getSatisfies()){
 				pty = base.getStaticProperty(name, recursive);
 				if (pty != null){
 					return pty;
 				}
 			}
 		}		
 		return null;
 	}
 
 	/**
 	 * @see IJstType#getStaticProperties()
 	 */
 	public List<IJstProperty> getStaticProperties() {
 		List<IJstProperty> list = new ArrayList<IJstProperty>();
 		for(IJstProperty pty: getProperties()){
 			if (pty.isStatic()){
 				list.add(pty);
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * @see IJstType#hasInstanceProperties()
 	 */
 	public boolean hasInstanceProperties() {
 		return getInstanceProperties().size() > 0;
 	}
 
 	/**
 	 * @see IJstType#hasInstanceProperty(String,boolean)
 	 */
 	public boolean hasInstanceProperty(String varName, boolean recursive) {
 		if (varName == null){
 			return false;
 		}
 		boolean isInstance =  getInstanceProperty(varName) != null;
 		if (isInstance || !recursive || getExtends().isEmpty()){
 			return isInstance;
 		}
 		for (IJstType base: getExtends()){
 			if (base.hasInstanceProperty(varName, recursive)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @see IJstType#getInstanceProperty(String)
 	 */
 	public IJstProperty getInstanceProperty(final String name) {
 		if (name == null){
 			return null;
 		}
 		for (IJstProperty pty: getInstanceProperties()){
 			if (name.equals(pty.getName().getName())){
 				return pty;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see IJstType#getInstanceProperty(String,boolean)
 	 */
 	public IJstProperty getInstanceProperty(final String name, boolean recursive) {
 		if (name == null){
 			return null;
 		}
 		for (IJstProperty pty: getInstanceProperties()){
 			if (name.equals(pty.getName().getName())){
 				return pty;
 			}
 		}
 		IJstProperty pty = null;
 		if (recursive && !getExtends().isEmpty()){
 			for (IJstType base: getExtends()){
 				pty = base.getInstanceProperty(name, recursive);
 				if (pty != null){
 					return pty;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see IJstType#getInstanceProperties()
 	 */
 	public List<IJstProperty> getInstanceProperties() {
 		List<IJstProperty> list = new ArrayList<IJstProperty>();
 		for(IJstProperty pty: getProperties()){
 			if (!pty.isStatic()){
 				list.add(pty);
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * @see IJstType#getConstructor()
 	 */
 	public JstMethod getConstructor() {
 		return m_constructor;
 	}
 
 	/**
 	 * @see IJstType#getMethod(String)
 	 */
 	public IJstMethod getMethod(final String name) {
 		IJstMethod mtd = getMethod(name, true, false);
 		if (mtd != null){
 			return mtd;
 		}
 		return getMethod(name, false, false);
 	}
 
 	/**
 	 * @see IJstType#getMethod(String,boolean)
 	 */
 	public IJstMethod getMethod(final String name, boolean isStatic) {
 		return getMethod(name, isStatic, false);
 	}
 
 	/**
 	 * @see IJstType#getMethod(String,boolean,boolean)
 	 */
 	public IJstMethod getMethod(final String name, boolean isStatic, boolean recursive) {
 		if (name == null){
 			return null;
 		}
 		for (IJstMethod mtd: getMethods()){
 			if (mtd.isStatic() == isStatic && name.equals(mtd.getName().getName())){
 				return mtd;
 			}
 		}
 		if (recursive && !getExtends().isEmpty()){
 			IJstMethod mtd;
 			for (IJstType t: getExtends()){
 				if (t == this) continue; // prevent recursive on itself
 				
 				mtd = t.getMethod(name, isStatic, recursive);
 				if (mtd != null){
 					return mtd;
 				}
 			}
 		}
 		
 		if (!isStatic && getModifiers().isAbstract()) {
 			return getMethodFromSatisfies(name, this);
 		}
 		
 		return null;
 	}
 	
 	private IJstMethod getMethodFromSatisfies(final String name, IJstType type) {
 		
 		for (IJstType t: type.getSatisfies()){
 			IJstMethod mtd = t.getMethod(name, false, true);
 			
 			if (mtd != null) {
 				return mtd;
 			}			
 		}	
 		
 		IJstType baseType = type.getExtend();
 		
 		if (baseType == null) {
 			return null;
 		}
 		
 		if (baseType.getModifiers().isAbstract()) {
 			return getMethodFromSatisfies(name, baseType);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * @see IJstType#getMethods()
 	 */
 	public List<IJstMethod> getMethods(){
	
 		if (m_mixins != null && this.getMixinsRef().size() > 0) {
 			return getMixinMergedMethods();
 		}
		if(m_mtds==null){
			m_mtds = new ArrayList<IJstMethod>();
		}
 		return Collections.unmodifiableList(m_mtds);
 	}
 
 	private List<IJstMethod> getMixinMergedMethods() {
 		if(m_mtds==null){
 			m_mtds = new ArrayList<IJstMethod>();
 		}
 		List<IJstMethod> list = new ArrayList<IJstMethod>(m_mtds);
 		
 		for (IJstTypeReference module : this.getMixinsRef()) {
 			//Create proxy methods for mixin methods
 			
 			if(this.equals(module.getReferencedType())){
 				continue;
 			}
 			
 			for (IJstMethod mtd : module.getReferencedType().getMethods()) {
 				if (mtd instanceof ISynthesized || mtd instanceof JstProxyMethod) {
 					continue;
 				}
 				list.add(new JstProxyMethod(mtd));
 			}
 		}
 		return list;
 	}
 	/**
 	 * @see IJstType#getMethods(boolean)
 	 */
 	public List<IJstMethod> getMethods(boolean isStatic) {
 		if (isStatic) {
 			return getStaticMethods();
 		} else {
 			return getInstanceMethods();
 		}
 	}
 
 	/**
 	 * @see IJstType#getMethods(boolean,boolean)
 	 */
 	public List<IJstMethod> getMethods(boolean isStatic, boolean recursive) {
 		Map<String, IJstMethod> mtdMap = new LinkedHashMap<String, IJstMethod>();
 	
 		for (IJstMethod mtd : getMethods(isStatic)) {
 			String mtdName = mtd.getName().getName();
 			if (!mtdMap.containsKey(mtdName)){
 				mtdMap.put(mtdName, mtd);
 			}
 		}
 		
 		//Jack: Memory overflow when "A extends B extends A"
 		if (recursive) {
 			List<IJstType> types = new ArrayList<IJstType>();
 			gatherExtendTypes(types, this);
 			
 			if (!isStatic && getModifiers().isAbstract()) {
 				gatherSatisfiesForAbstractTypes(types, this);
 			}
 			
 			if (!types.isEmpty()){
 				for (IJstType t: types){
 					for (IJstMethod mtd: t.getMethods(isStatic)){
 						String mtdName = mtd.getName().getName();
 						if (!mtdMap.containsKey(mtdName)){
 							mtdMap.put(mtdName, mtd);
 						}						
 					}
 				}
 			}			
 		}
 		
 		List<IJstMethod> mtdList = new ArrayList<IJstMethod>(mtdMap.size());
 		
 		for (IJstMethod mtd : mtdMap.values()) {
 			mtdList.add(mtd);
 		}
 		
 		return mtdList;
 	}
 	
 	private static void gatherSatisfiesForAbstractTypes(List<IJstType> types, IJstType type) {
 		
 		// collect all satisfies and their extends for abstract type
 		for (IJstType t : type.getSatisfies()) {
 			if (!types.contains(t)) {
 				types.add(t);
 			}
 		}
 		
 		// collect all satisfies on abstract base types
 		IJstType baseType = type.getExtend();
 		if (baseType == null) {
 			return;
 		}
 		
 		if (baseType.getModifiers().isAbstract()) {
 			gatherSatisfiesForAbstractTypes(types, baseType);
 		}		
 	}
 	
 	private void gatherExtendTypes(List<IJstType> types, IJstType type) {
 		for(IJstType t : type.getExtends()) {
 			if (types.contains(t) || getName().equals(t.getName())) {
 				break;
 			} else {
 				types.add(t);
 				gatherExtendTypes(types, t);
 			}
 		}
 	}
 	private void gatherSatisfiesTypes(List<IJstType> types, IJstType type) {
 		for(IJstType t : type.getSatisfies()) {
 			if (types.contains(t) || getName().equals(t.getName())) {
 				break;
 			} else {
 				types.add(t);
 				gatherSatisfiesTypes(types, t);
 			}
 		}
 	}
 
 	/**
 	 * @see IJstType#hasStaticMethods()
 	 */
 	public boolean hasStaticMethods() {
 		return getStaticMethods().size() > 0;
 	}
 	
 	/**
 	 * @see IJstType#hasStaticMethod(String,boolean)
 	 */
 	public boolean hasStaticMethod(String mtdName, boolean recursive) {
 		if (mtdName == null){
 			return false;
 		}
 		IJstMethod mtd = getMethod(mtdName, true);
 		boolean hasStatic = mtd != null;
 		if (hasStatic || !recursive || getExtends().isEmpty()){
 			return hasStatic;
 		}
 		for (IJstType base: getExtends()){
 			if (base.hasStaticMethod(mtdName, recursive)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @see IJstType#getStaticMethods()
 	 */
 	public List<IJstMethod> getStaticMethods() {
 		List<IJstMethod> list = new ArrayList<IJstMethod>();
 		for(IJstMethod mtd: getMethods()){
 			if (mtd.isStatic()){
 				list.add(mtd);
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * @see IJstType#getStaticMethod(String)
 	 */
 	public IJstMethod getStaticMethod(final String name) {
 		if (name == null){
 			return null;
 		}
 		for (IJstMethod mtd: getStaticMethods()){
 			if (name.equals(mtd.getName().getName())){
 				return mtd;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see IJstType#getStaticMethod(String,boolean)
 	 */
 	public IJstMethod getStaticMethod(final String name, boolean recursive) {
 		if (name == null){
 			return null;
 		}
 		for (IJstMethod mtd: getStaticMethods()){
 			if (name.equals(mtd.getName().getName())){
 				return mtd;
 			}
 		}
 		IJstMethod mtd = null;
 		if (recursive && !getExtends().isEmpty()){
 			for (IJstType base: getExtends()){
 				mtd = base.getStaticMethod(name, recursive);
 				if (mtd != null){
 					return mtd;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see IJstType#hasInstanceMethods()
 	 */
 	public boolean hasInstanceMethods() {
 		return getInstanceMethods().size() > 0;
 	}
 	
 	/**
 	 * @see IJstType#hasInstanceMethod(String,boolean)
 	 */
 	public boolean hasInstanceMethod(String mtdName, boolean recursive) {
 		if (mtdName == null){
 			return false;
 		}
 		IJstMethod mtd = getMethod(mtdName, false);
 		boolean hasInstance = mtd != null;
 		if (hasInstance || !recursive || getExtends().isEmpty()){
 			return hasInstance;
 		}
 		for (IJstType base: getExtends()){
 			if (base.hasInstanceMethod(mtdName, recursive)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @see IJstType#getInstanceMethods()
 	 */
 	public List<IJstMethod> getInstanceMethods() {
 		List<IJstMethod> list = new ArrayList<IJstMethod>();
 		for(IJstMethod mtd: getMethods()){
 			if (!mtd.isStatic()){
 				list.add(mtd);
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * @see IJstType#getInstanceMethod(String)
 	 */
 	public IJstMethod getInstanceMethod(final String name) {
 		if (name == null){
 			return null;
 		}
 		for (IJstMethod mtd: getInstanceMethods()){
 			if (name.equals(mtd.getName().getName())){
 				return mtd;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see IJstType#getInstanceMethod(String,boolean)
 	 */
 	public IJstMethod getInstanceMethod(final String name, boolean recursive) {
 		if (name == null){
 			return null;
 		}
 		for (IJstMethod mtd: getInstanceMethods()){
 			if (name.equals(mtd.getName().getName())){
 				return mtd;
 			}
 		}
 		IJstMethod mtd = null;
 		if (recursive && !getExtends().isEmpty()){
 			for (IJstType base: getExtends()){
 				mtd = base.getInstanceMethod(name, recursive);
 				if (mtd != null){
 					return mtd;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see IJstType#getInitializers(boolean)
 	 */
 	public List<IStmt> getInitializers(boolean isStatic) {
 		if (isInterface() || isStatic) {
 			return getStaticInitializers();
 		} else {
 			return getInstanceInitializers();
 		}
 	}
 
 	/**
 	 * @see IJstType#getStaticInitializers()
 	 */
 	public List<IStmt> getStaticInitializers() {
 		if (m_staticInits == null){
 			return Collections.emptyList();
 		}
 		return Collections.unmodifiableList(m_staticInits);
 	}
 
 	/**
 	 * @see IJstType#getInstanceInitializers()
 	 */
 	public List<IStmt> getInstanceInitializers() {
 		if (m_instanceInits == null){
 			return Collections.emptyList();
 		}
 		return Collections.unmodifiableList(m_instanceInits);
 	}
 
 	/**
 	 * @see IJstType#hasInstanceMixins()
 	 */
 	public boolean hasMixins() {
 		return getMixinsRef().size() > 0;
 	}
 
 	
 	public List<IJstType> getMixins() {
 		
 		if (m_mixins == null || m_mixins.size() == 0) {
 			return Collections.emptyList();
 		}
 		List<IJstTypeReference> list = m_mixins;
 		if (this.getMixinsRef().size() > 0) {
 			if (m_mixins == null)
 				list = new ArrayList<IJstTypeReference>();
 			else
 				list = new ArrayList<IJstTypeReference>(m_mixins);
 		} else if (m_mixins == null) {
 			return Collections.emptyList();
 		}
 		return unwrap(list);
 		
 	}
 	
 	/**
 	 * @see IJstType#getInstanceMixins()
 	 */
 	public List<IJstTypeReference> getMixinsRef() {
 		if (m_mixins == null){
 			return Collections.emptyList();
 		}
 		synchronized(this){
 			return Collections.unmodifiableList(m_mixins);
 		}
 	}
 	
 	/**
 	 * @see IJstType#getEmbededTypes()
 	 */
 	public synchronized List<JstType> getEmbededTypes(){
 		if (m_innerTypes == null){
 			return Collections.emptyList();
 		}
 		return Collections.unmodifiableList(m_innerTypes);
 	}
 
 	/**
 	 * @see IJstType#getEmbededType(String)
 	 */
 	public JstType getEmbededType(final String shortName) {
 		if (shortName == null){
 			return null;
 		}
 		for (JstType embeded : getEmbededTypes()) {
 			if (shortName.equals(embeded.getSimpleName())) {
 				return embeded;
 			}
 		}
 		return null;
 	}
 	
 	public void setEmbeddedTypes(List<JstType> embeddedTypes){
 		for (JstType iJstType : embeddedTypes) {
 			addInnerType(iJstType);
 		}
 	}
 	
 	/**
 	 * @see IJstType#getStaticEmbededTypes()
 	 */
 	public List<JstType> getStaticEmbededTypes() {
 		List<JstType> list = new ArrayList<JstType>();
 		for (JstType embeded : getEmbededTypes()) {
 			if (embeded.getModifiers().isStatic()) {
 				list.add(embeded);
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * @see IJstType#getInstanceEmbededTypes()
 	 */
 	public List<JstType> getInstanceEmbededTypes() {
 		List<JstType> list = new ArrayList<JstType>();
 		for (JstType embeded : getEmbededTypes()) {
 			if (!embeded.getModifiers().isStatic()) {
 				list.add(embeded);
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * @see IJstType#isEmbededType()
 	 */
 	public boolean isEmbededType(){
 		return m_outerType != null;
 	}
 
 	/**
 	 * @see IJstType#getOuterType()
 	 */
 	public JstType getOuterType() {
 		return m_outerType;
 	}
 	
 	/**
 	 * @see IJstType#getSiblingType(String)
 	 */
 	public JstType getSiblingType(final String shortName) {
 		if (shortName == null || m_siblingTypes == null){
 			return null;
 		}
 		synchronized(this){
 			for (JstType embeded : m_siblingTypes) {
 				if (embeded.getSimpleName().equals(shortName)) {
 					return embeded;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * @see IJstType#getSiblingTypes()
 	 */
 	public List<JstType> getSiblingTypes() {
 		if (m_siblingTypes == null){
 			return Collections.emptyList();
 		}
 		synchronized(this){	
 			return Collections.unmodifiableList(m_siblingTypes);
 		}
 	}
 
 	/**
 	 * @see IJstType#isEmbededType()
 	 */
 	public boolean isSiblingType(){
 		return m_containingType != null;
 	}
 
 	/**
 	 * @see IJstType#getContainingType()
 	 */
 	public JstType getContainingType() {
 		return m_containingType;
 	}
 
 	/**
 	 * @see IJstType#isParamName(String)
 	 */
 	public boolean isParamName(String name){
 		return name != null && getParamNames().contains(name); 
 	}
 	
 	/**
 	 * @see IJstType#getParamNames()
 	 */
 	public synchronized List<String> getParamNames(){
 		if (m_paramTypes == null){
 			return Collections.emptyList();
 		}
 		else {
 			List<String> list = new ArrayList<String>();
 			list.addAll(m_paramTypes.keySet());
 			return list;
 		}
 	}
 	
 	/**
 	 * @see IJstType#getParamTypes()
 	 */
 	public synchronized List<JstParamType> getParamTypes(){
 		if (m_paramTypes == null){
 			return Collections.emptyList();
 		}
 		else {
 			List<JstParamType> list = new ArrayList<JstParamType>();
 			list.addAll(m_paramTypes.values());
 			return list;
 		}
 	}
 	
 	
 	/**
 	 * {@link IJstType#getDoc()}
 	 */
 	public IJstDoc getDoc() {
 		return m_doc;
 	}
 	
 	//
 	// API
 	//
 	/**
 	 * Set JstDoc for this type
 	 * @param jstDoc IJstDoc
 	 */
 	public void setDoc(IJstDoc jstDoc) {
 		m_doc = jstDoc;
 	}
 	
 	/**
 	 * Set the category of the type
 	 * @param category Category
 	 * @return JstType
 	 */
 	public JstType setCategory(final Category category){
 		assert category != null : "category cannot be null";
 		m_category = category;
 		
 		if(Category.FTYPE == category){
 			//ftype is a Function
 			addExtend(JstCache.getInstance().getType("Function"));
 		}
 		return this;
 	}
 
 	/**
 	 * Set the package of the type
 	 * @param pkg JstPackage
 	 * @return JstType
 	 */
 	public JstType setPackage(final JstPackage pkg){
 		m_pkg = pkg;
 		return this;
 	}
 	
 	/**
 	 * Set the simple name of the type
 	 * @param simpleName String
 	 * @return JstType
 	 */
 	public JstType setSimpleName(final String simpleName){
 		assert simpleName != null : "simpleName cannot be null";
 		m_simpleName = simpleName;
 		if (m_simpleName != null){
 			m_info.setHasName();
 		}
 		return this;
 	}
 	
 	/**
 	 * Set alias of the type
 	 * @param alias String
 	 * @return JstType
 	 */
 	public JstType setAlias(final String alias) {
 		m_alias = alias;
 		return this;
 	}
 	
 	/**
 	 * Set the alias type name (a short-hand to refer the type) of the type
 	 * @param aliasTypeName String
 	 * @return JstType
 	 */
 	public JstType setAliasTypeName(final String aliasTypeName) {
 		m_aliasTypeName = aliasTypeName;
 		return this;
 	}
 
 	/**
 	 * Add given type to the dependency map, using simple name
 	 * of the given type as map key
 	 * @param importType JstType
 	 */
 	public void addImport(final IJstType importType) {
 		assert importType != null : "importType cannot be null";
 		assert importType.getSimpleName() != null : "importType.getSimpleName() cannot be null";
 		if(importType!=null){
 			addImport(importType.getSimpleName(), importType);
 		}
 	}
 
 	/**
 	 * Add given type to the dependency map, using simple name
 	 * of the given type as map key
 	 * @param importType IJstTypeReference
 	 */
 	public void addImport(final IJstTypeReference importType) {
 		assert importType != null : "importType cannot be null";
 		assert importType.getReferencedType().getSimpleName() != null : "importType.getSimpleName() cannot be null";
 		if (importType != null) {
 			addImport(importType.getReferencedType().getSimpleName(), importType);
 		}
 	}
 	
 	/**
 	 * Add given type to the dependency map using given key as map key
 	 * @param key String 
 	 * @param importType IJstTypeReference
 	 */
 	public void addImport(String key, IJstTypeReference importType) {
 		if (key == null || importType == null
 				|| m_outerType != null
 				|| importType.getReferencedType() == this) {
 				return;
 			}
 		
 		IJstType type = importType.getReferencedType();
 		//If the alias is blank string then use full name
 		if (key.length() == 0) {
 			key = type.getName();
 		}
 		//TODO - Don't dedup, validation should deal with this
 		if (getImport(type.getSimpleName()) == type
 			|| getImport(type.getName()) == type) {
 			return;
 		}
 		synchronized (this){
 			if(m_imports==null){
 				m_imports = new LinkedHashMap<String, IJstTypeReference>();
 			}
 			if (!m_imports.containsKey(key)){
 				m_imports.put(key, importType);
 				addChild(importType);
 			}
 		}
 	}
 
 	/**
 	 * Add given type to the dependency map using given key as map key
 	 * @param key String 
 	 * @param importType JstType
 	 */
 	public void addImport(final String key, final IJstType importType) {
 		if (key == null || importType == null
 			|| m_outerType != null
 			|| importType == this) {
 			return;
 		}
 		addImport(key, new JstTypeReference(importType));
 	}
 	
 
 
 	
 	/**
 	 * Add given type to the dependency map, using simple name
 	 * of the given type as map key
 	 * @param importType JstType
 	 */
 	public void addInactiveImport(final IJstType importType) {
 		assert importType != null : "importType cannot be null";
 		assert importType.getSimpleName() != null : "importType.getSimpleName() cannot be null";
 		addInactiveImport(importType.getSimpleName(), importType);
 	}
 
 	/**
 	 * Add given type to the dependency map, using simple name
 	 * of the given type as map key
 	 * @param importType IJstTypeReference
 	 */
 	public void addInactiveImport(final IJstTypeReference importType) {
 		assert importType != null : "importType cannot be null";
 		assert importType.getReferencedType().getSimpleName() != null : "importType.getSimpleName() cannot be null";
 		addInactiveImport(importType.getReferencedType().getSimpleName(), importType);
 	}
 	
 	/**
 	 * Add given type to the dependency map using given key as map key
 	 * @param key String 
 	 * @param importType IJstTypeReference
 	 */
 	public void addInactiveImport(String key, IJstTypeReference importType) {
 		if (key == null || importType == null
 				|| m_outerType != null
 				|| importType.getReferencedType() == this) {
 				return;
 			}
 		IJstType type = importType.getReferencedType();
 		//If the alias is blank string then use full name
 		if (key.length() == 0) {
 			key = type.getName();
 		}
 		if (getInactiveImport(type.getSimpleName()) == type
 			|| getInactiveImport(type.getName()) == type) {
 			return;
 		}
 		synchronized (this){
 			if(m_inactiveImports==null){
 				m_inactiveImports = new LinkedHashMap<String, IJstTypeReference>();
 			}
 			if (!m_inactiveImports.containsKey(key)){
 				m_inactiveImports.put(key, importType);
 				addChild(importType);
 			}
 		}
 	}
 
 	/**
 	 * Add given type to the dependency map using given key as map key
 	 * @param key String 
 	 * @param importType JstType
 	 */
 	public void addInactiveImport(final String key, final IJstType importType) {
 		if (key == null || importType == null
 			|| m_outerType != null
 			|| importType == this) {
 			return;
 		}
 		addInactiveImport(key, new JstTypeReference(importType));
 	}
 	
 
 	/**
 	 * Add given type to the dependency map, using simple name
 	 * of the given type as map key
 	 * added by huzhou@ebay.com to support fully qualified types' linkages
 	 * @param importType JstType
 	 */
 	public void addFullyQualifiedImport(final IJstType importType) {
 		assert importType != null : "importType cannot be null";
 		assert importType.getSimpleName() != null : "importType.getSimpleName() cannot be null";
 		addFullyQualifiedImport(importType.getSimpleName(), importType);
 	}
 
 	/**
 	 * Add given type to the dependency map, using simple name
 	 * of the given type as map key
 	 * added by huzhou@ebay.com to support fully qualified types' linkages
 	 * @param importType IJstTypeReference
 	 */
 	public void addFullyQualifiedImport(final IJstTypeReference importType) {
 		assert importType != null : "importType cannot be null";
 		assert importType.getReferencedType().getSimpleName() != null : "importType.getSimpleName() cannot be null";
 		addFullyQualifiedImport(importType.getReferencedType().getSimpleName(), importType);
 	}
 	
 	/**
 	 * Add given type to the dependency map using given key as map key
 	 * added by huzhou@ebay.com to support fully qualified types' linkages
 	 * @param key String
 	 * @param importType IJstTypeReference
 	 */
 	public void addFullyQualifiedImport(String key, IJstTypeReference importType) {
 		if (key == null || importType == null
 				|| m_outerType != null
 				|| importType.getReferencedType() == this) {
 				return;
 			}
 		IJstType type = importType.getReferencedType();
 		//If the alias is blank string then use full name
 		if (key.length() == 0) {
 			key = type.getName();
 		}
 		if (getFullyQualifiedImport(type.getSimpleName()) == type
 			|| getFullyQualifiedImport(type.getName()) == type) {
 			return;
 		}
 		synchronized (this){
 			if(m_fullyQualifiedImports==null){
 				m_fullyQualifiedImports = new LinkedHashMap<String, IJstTypeReference>();
 			}
 			if (!m_fullyQualifiedImports.containsKey(key)){
 				m_fullyQualifiedImports.put(key, importType);
 				addChild(importType);
 			}
 		}
 	}
 
 	/**
 	 * Add given type to the dependency map using given key as map key
 	 * added by huzhou@ebay.com to support fully qualified types' linkages
 	 * @param key String 
 	 * @param importType JstType
 	 */
 	public void addFullyQualifiedImport(final String key, final IJstType importType) {
 		if (key == null || importType == null
 			|| m_outerType != null
 			|| importType == this) {
 			return;
 		}
 		addFullyQualifiedImport(key, new JstTypeReference(importType));
 	}
 	
 	public void removeImport(final IJstType type){
 		if (m_imports == null || m_imports.isEmpty()){
 			return;
 		}		
 		synchronized (this){
 			for (Entry<String, IJstTypeReference> t: m_imports.entrySet()){			
 				if (t.getValue().getReferencedType() == type){
 					IJstTypeReference tr = m_imports.remove(t.getKey());
 					removeChild(tr);
 					return;
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * Remove given type from dependency map
 	 * @param type IJstTypeReference
 	 */
 	public void removeImport(final IJstTypeReference type){
 		if (m_imports == null || m_imports.isEmpty()){
 			return;
 		}		
 		synchronized (this){
 			for (Entry<String, IJstTypeReference> t: m_imports.entrySet()){			
 				if (t.getValue().equals(type)){
 					IJstTypeReference tr = m_imports.remove(t.getKey());
 					removeChild(tr);
 					return;
 				}
 			}
 		}
 	}
 
 	
 	
 	public void removeInactiveImport(final IJstType type){
 		if (m_inactiveImports==null && m_inactiveImports.isEmpty()){
 			return;
 		}		
 		synchronized (this){
 			for (Entry<String, IJstTypeReference> t: m_inactiveImports.entrySet()){			
 				if (t.getValue().getReferencedType() == type){
 					IJstTypeReference tr = m_inactiveImports.remove(t.getKey());
 					removeChild(tr);
 					return;
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * Remove given type from dependency map
 	 * @param type IJstTypeReference
 	 */
 	public void removeInactiveImport(final IJstTypeReference type){
 		if (m_inactiveImports == null || m_inactiveImports.isEmpty()){
 			return;
 		}		
 		synchronized (this){
 			for (Entry<String, IJstTypeReference> t: m_inactiveImports.entrySet()){			
 				if (t.getValue().equals(type)){
 					IJstTypeReference tr = m_inactiveImports.remove(t.getKey());
 					removeChild(tr);
 					return;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * added by huzhou@ebay.com to support fully qualified types' linkages
 	 * @param type
 	 */
 	public void removeFullyQualifiedImport(final IJstType type){
 		if (m_fullyQualifiedImports==null || m_fullyQualifiedImports.isEmpty()){
 			return;
 		}		
 		synchronized (this){
 			for (Entry<String, IJstTypeReference> t: m_fullyQualifiedImports.entrySet()){			
 				if (t.getValue().getReferencedType() == type){
 					IJstTypeReference tr = m_fullyQualifiedImports.remove(t.getKey());
 					removeChild(tr);
 					return;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * added by huzhou@ebay.com to support fully qualified types' linkages
 	 * @param type IJstTypeReference
 	 */
 	public void removeFullyQualifiedImport(final IJstTypeReference type){
 		if (m_fullyQualifiedImports == null || m_fullyQualifiedImports.isEmpty()){
 			return;
 		}		
 		synchronized (this){
 			for (Entry<String, IJstTypeReference> t: m_fullyQualifiedImports.entrySet()){			
 				if (t.getValue().equals(type)){
 					IJstTypeReference tr = m_fullyQualifiedImports.remove(t.getKey());
 					removeChild(tr);
 					return;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Add given type as base type
 	 * @param extend IJstType
 	 * @exception Runtime exception if the type is not an interface but
 	 * the type already has a base type
 	 */
 	public void addExtend(final IJstType extend) {
 		if (extend == null){
 			return;
 		}
 		addExtend(new JstTypeReference(extend));
 	}
 	
 	/**
 	 * Add given type as base type
 	 * @param extend IJstTypeReference
 	 * @exception Runtime exception if the type is not an interface but
 	 * the type already has a base type
 	 */
 	public void addExtend(final IJstTypeReference extend) {
 		if (extend == null){
 			return;
 		}
 		synchronized(this){
 			if (m_extends != null && contains(extend.getReferencedType(), m_extends)){
 				return;
 			}
 	
 			if (m_extends == null) {
 				m_extends = new ArrayList<IJstTypeReference>(1);
 			}
 			
 			if (m_extends.size() == 1) {
 				if ("vjo.Object".equals(m_extends.get(0).getReferencedType().getName())) {
 					m_extends = new ArrayList<IJstTypeReference>(1);
 				}
 			}
 
 			// Remove the runtime exception replace. Semantic validator can now see these and correctly validate
 			// We can't generate a model if this happens.
 //			if (!isInterface() && m_extends.size() >= 1){
 //				throw new RuntimeException("non-interface type cannot have more than one base type.");
 //			}
 			m_extends.add(extend);
 			addChild(extend);
 		}
 	}
 	
 	/**
 	 * Remove the extend with given name
 	 * @param extend IJstType
 	 * @return JstType the removed extended type
 	 */
 	public IJstType removeExtend(final IJstType extend) {
 		if (extend == null) {
 			return null;
 		}
 		synchronized(this){
 			for (IJstTypeReference ref: getExtendsRef()){
 				if (extend == ref.getReferencedType()){
 					m_extends.remove(ref);
 					removeChild(ref);
 					return extend;
 				}
 			}
 			return null;
 		}
 	}
 	
 	/**
 	 * Remove the satisfy with given name
 	 * @param satisfy IJstType
 	 * @return JstType the removed satisfy type
 	 */
 	public IJstType removeSatisfy(final IJstType satisfy) {
 		if (satisfy == null) {
 			return null;
 		}
 		synchronized(this){
 			for (IJstTypeReference ref: getSatisfiesRef()){
 				if (satisfy == ref.getReferencedType()){
 					m_satisfies.remove(ref);
 					removeChild(ref);
 					return satisfy;
 				}
 			}
 			return null;
 		}
 	}
 	
 	
 	/**
 	 * Remove alias from the type
 	 */
 	public void clearAlias() {
 		synchronized (this) {
 			m_alias = null;
 		}
 	}
 	
 	/**
 	 * Remove all extends from the type
 	 */
 	public void clearExtends() {
 		synchronized(this){
 			if (m_extends != null) {	
 				removeChildren(m_extends);
 				m_extends = null;
 			}
 		}
 	}
 	
 	/**
 	 * Remove all satisfies from the type
 	 */
 	public void clearSatisfies() {
 		synchronized(this){
 			if (m_satisfies != null) {
 				removeChildren(m_satisfies);
 				m_satisfies = null;
 			}
 		}
 	}
 
 	/**
 	 * Remove all embedded types from the type
 	 */
 	public void clearEmbeddedTypes() {
 		synchronized(this){
 			if (m_innerTypes != null) {
 				removeChildren(m_innerTypes);
 				m_innerTypes = null;
 			}
 		}
 	}
 	
 	/**
 	 * Remove all outer type from the type
 	 */
 	public void clearOuterType() {
 		synchronized(this){
 			if (m_outerType != null) {
 				//removeChildren(m_outerType);
 				m_outerType = null;
 			}
 		}
 	}
 	
 	/**
 	 * Remove all embedded types from the type
 	 */
 	public void clearSecondaryTypes() {
 		synchronized(this){
 			if (m_secondaryTypes != null) {
 				removeChildren(m_secondaryTypes);
 				m_secondaryTypes = null;
 			}
 		}
 	}
 	
 	/**
 	 * Remove all static initialization statements from the type
 	 */
 	public void clearStaticInits() {
 		synchronized(this){
 			if (m_staticInits != null) {
 				removeChildren(m_staticInits);
 				m_staticInits = null;
 			}
 			if (m_initBlock != null) {
 				removeChild(m_initBlock);
 				m_initBlock=null;
 			}
 		}
 	}
 	/**
 	 * Remove all static initialization statements from the type
 	 */
 	public void clearInstanceInits() {
 		synchronized(this){
 			if (m_instanceInits != null) {
 				removeChildren(m_instanceInits);
 				m_instanceInits = null;
 			}
 			
 		}
 	}
 
 	
 	/**
 	 * Remove all imports from the type
 	 */
 	public void clearImports() {
 		synchronized(this){
 			if(m_imports!=null){
 				removeChildren(m_imports.values());
 				m_imports = null;
 			}
 		}
 	}
 	
 	/**
 	 * Remove all inactive imports from the type
 	 */
 	public void clearInactiveImports() {
 		synchronized(this){
 			if(m_inactiveImports!=null){
 				removeChildren(m_inactiveImports.values());
 				m_inactiveImports = null; // new LinkedHashMap<String, IJstTypeReference>();	
 			}
 		}
 	}
 
 
 	/**
 	 * Remove all otypes from the type
 	 */
 	public void clearOTypes() {
 		synchronized(this){
 			m_otypes =  new ArrayList<IJstOType>();	
 		}
 	}
 	
 	/**
 	 * Remove name from the type
 	 */
 	public void clearName() {
 		synchronized (this) {
 			m_simpleName = null;
 			m_pkg = null;
 		}
 	}
 	
 	public void clearModifiers() {
 		synchronized (this) {
 			m_modifiers = new JstModifiers();;
 		}
 	}
 	
 	/**
 	 * Remove all expects from the type
 	 */
 	public void clearExpects() {
 		synchronized(this){
 			m_expects = null;
 		}
 	}
 	
 	public void clearOptions() {
 		synchronized(this){
 			m_options =  null;
 			m_isMetaType = false;
 		}
 	}
 	
 	/**
 	 * Remove all mixins from the type
 	 */
 	public void clearMixins() {
 		synchronized(this){
 			if (m_mixins != null) {
 				for (IJstTypeReference m:m_mixins) {
 					for (IJstProperty prop : m.getReferencedType().getProperties()) {
 						removeProperty(prop.getName().getName(), prop.isStatic());
 					}
 					for (IJstMethod mtd : m.getReferencedType().getMethods()) {
 						removeMethod(mtd.getName().getName(), mtd.isStatic());
 					}
 
 				}
 				m_mixins = null;
 			}
 //			if (m_staticMixins != null) {
 //				m_staticMixins .clear();
 //			}
 		}
 	}
 
 	public void addSatisfy(final IJstTypeReference satisfier) {
 		if (satisfier == null) {
 			return;
 		}
 
 		synchronized(this){
 			if (m_satisfies != null && contains(satisfier.getReferencedType(), m_satisfies)){
 				return;
 			}
 			if (m_satisfies == null){
 				m_satisfies = new ArrayList<IJstTypeReference>(1);
 			}
 			m_satisfies.add(satisfier);
 			addChild(satisfier);
 		}
 	}
 	
 	/**
 	 * Add given type as interface this type satisfies.
 	 * @param satisfier IJstType
 	 */
 	public void addSatisfy(final IJstType satisfier) {
 		if (satisfier == null) {
 			return;
 		}
 		addSatisfy(new JstTypeReference(satisfier));
 	}
 
 	public void addExpects(final IJstTypeReference expects) {
 		if (expects == null) {
 			return;
 		}
 
 		synchronized(this){
 			if (m_expects != null && contains(expects.getReferencedType(), m_expects)){
 				return;
 			}
 			if (m_expects == null){
 				m_expects = new ArrayList<IJstTypeReference>(1);
 			}
 			m_expects.add(expects);
 			addChild(expects);
 		}
 	}
 	
 	public void addExpects(final IJstType expects) {
 		
 		if (expects == null) {
 			return;
 		}
 		synchronized(this){
 			if (m_expects != null && contains(expects, m_expects)){
 				return;
 			}
 			if (m_expects == null){
 				m_expects = new ArrayList<IJstTypeReference>();
 			}
 			
 			JstTypeReference ref = new JstTypeReference(expects);
 			addChild(ref);
 			m_expects.add(ref);
 		}
 	}
 
 	/**
 	 * Add given property to the type
 	 * @param pty JstProperty
 	 */
 	public void addProperty(final IJstProperty pty) {
 		if (pty == null || getProperties().contains(pty)) {
 			return;
 		}
 		synchronized(this){
 			if(m_ptys==null){
 				m_ptys = new ArrayList<IJstProperty>();
 			}
 			m_ptys.add(pty);
 			addChild(pty);
 		}
 	}
 	
 	/**
 	 * Remove property with given name from the type
 	 * @param ptyName String
 	 * @param isStatic boolean
 	 * @return IJstProperty the removed property
 	 */
 	public IJstProperty removeProperty(final String ptyName, boolean isStatic) {
 		if (ptyName == null || m_ptys==null) {
 			return null;
 		}
 		synchronized(this){
 			IJstProperty pty = getProperty(ptyName, isStatic);
 			m_ptys.remove(pty);
 			if (pty instanceof BaseJstNode){
 				removeChild((BaseJstNode)pty);
 			}
 			return pty;
 		}
 	}
 
 	/**
 	 * Set given method as constructor of the type
 	 * @param constructor JstMethod
 	 */
 	public JstType setConstructor(final JstMethod constructor) {
 		removeChild(m_constructor);
 		addChild(constructor);
 		m_constructor = constructor;
 		return this;
 	}
 
 	/**
 	 * Add given method to the type
 	 * @param mtd JstMethod
 	 */
 	public JstType addMethod(final IJstMethod mtd) {
 		if (mtd == null || getMethods().contains(mtd)) {
 			return this;
 		}
 		synchronized(this){
 			if(m_mtds==null){
 				m_mtds = new ArrayList<IJstMethod>();
 			}
 			m_mtds.add(mtd);
 			addChild(mtd);
 		}
 		return this;
 	}
 
 	/**
 	 * Remove the method with given from the type
 	 * @param mtdName String
 	 * @param isStatic boolean
 	 * @return JstMethod the removed method
 	 */
 	public IJstMethod removeMethod(final String mtdName, boolean isStatic) {
 		if (mtdName == null || m_mtds==null) {
 			return null;
 		}
 		synchronized(this){
 			IJstMethod mtd = getMethod(mtdName, isStatic);
 			m_mtds.remove(mtd);
 			if (mtd instanceof BaseJstNode){
 				removeChild((BaseJstNode)mtd);
 			}
 			return mtd;
 		}
 	}
 	
 	/**
 	 * Removes all methods from the type
 	 */
 	public void clearMethods() {
 		for (IJstMethod mtd: getMethods()){
 			removeChild(mtd);
 			mtd = null;
 		}
 		synchronized(this){
 			m_mtds = null; // new ArrayList<IJstMethod>();
 			
 			// clear constructor
 			setConstructor(null);
 		}
 	}
 	
 	
 	/**
 	 * Removes all properties from the type
 	 */
 	public void clearProperties() {
 		for (IJstProperty pty: getProperties()){
 			if (pty instanceof BaseJstNode){
 				removeChild((BaseJstNode)pty);
 				pty = null;
 			}
 		}
 		synchronized(this){
 			m_ptys = null; // new ArrayList<IJstProperty>();
 		}
 	}
 
 	/**
 	 * Add init statement to the type
 	 * @param stmt IStmt
 	 * @param isStatic boolean
 	 */
 	public void addInit(final IStmt stmt, boolean isStatic) {
 		if (stmt == null){
 			return;
 		}
 
 		if (isStatic) {
 			if (m_staticInits == null) {
 				m_staticInits = new ArrayList<IStmt>();
 			}
 			m_staticInits.add(stmt);
 		} 
 		else {
 			if (m_instanceInits == null) {
 				m_instanceInits = new ArrayList<IStmt>();
 			}
 			m_instanceInits.add(stmt);
 		}
 				
 		getInitBlock().addStmt(stmt);		
 	}
 	
 	public JstBlock getInitBlock(){
 		if(m_initBlock==null){
 			m_initBlock = new JstBlock();
 			addChild(m_initBlock);
 		}
 		return m_initBlock;
 	}
 	
 	/**
 	 * This internal API is for setting special init block.
 	 * Don't expose it via API - IJstType
 	 */
 	public void setInitBlock(JstBlock block) {
 		if (m_initBlock != null) {
 			removeChild(m_initBlock);
 		}
 		m_initBlock = block;
 		addChild(m_initBlock);
 	}	
 	
 	/**
 	 * Answer the variable table at the type level
 	 * @return VarTable
 	 */
 	public VarTable getVarTable(){
 		if (m_varTable == null){
 			m_varTable = new VarTable();
 		}
 		return m_varTable;
 	}
 	
 	private void clearVarTable() {
 		m_varTable = null;
 	}
 	
 	/**
 	 * Add given type as static mixin of this type
 	 * @param module JstType
 	 */
 /*	public void addStaticMixin(final IJstTypeReference module) {
 		if (module == null){
 			return;
 		}
 		synchronized(this){
 			if (m_staticMixins != null && m_staticMixins.contains(module)){
 				return;
 			}
 		
 			if (m_staticMixins == null) {
 				m_staticMixins = new ArrayList<IJstTypeReference>(1);
 			}
 			m_staticMixins.add(module);
 			addChild(module);
 		}
 	}
 */	
 	/**
 	 * Add given type as instance mixin of this type
 	 * @param module JstType
 	 */
 	public void addMixin(final IJstTypeReference module) {
 		if (module == null){
 			return;
 		}
 		synchronized(this){
 			if (m_mixins != null && m_mixins.contains(module)){
 				return;
 			}
 		
 			if (m_mixins == null) {
 				m_mixins = new ArrayList<IJstTypeReference>(1);
 			}
 			m_mixins.add(module);			
 			addChild(module);
 			
 		}
 	}
 	
 	/**
 	 * Add given type as inner type of this type
 	 * @param innerType JstType
 	 */
 	public void addInnerType(final JstType innerType) {
 		if (innerType == null){
 			return;
 		}
 		synchronized(this){
 			if (m_innerTypes != null && m_innerTypes.contains(innerType)){
 				return;
 			}
 		
 			if (m_innerTypes == null) {
 				m_innerTypes = new ArrayList<JstType>(1);
 			}
 			m_innerTypes.add(innerType);
 			addChild(innerType);
 		}
 		innerType.m_outerType = this;
 		innerType.setParent(this);
 	}
 	
 	/**
 	 * Remove the method with given from the type
 	 * @param shortName String
 	 * @return JstType the removed inner type
 	 */
 	public JstType removeInnerType(final String shortName) {
 		if (shortName == null) {
 			return null;
 		}
 		synchronized(this){
 			JstType innerType = this.getEmbededType(shortName);
 			m_innerTypes.remove(innerType);
 			removeChild(innerType);
 			return innerType;
 		}
 	}
 	
 	/**
 	 * Answer whether the type has inner type with given short name
 	 * @param shortName String
 	 * @return boolean
 	 */
 	public boolean hasInnerType(final String shortName){
 		return getEmbededType(shortName) != null;
 	}
 
 	/**
 	 * Set given type as outer type of this type
 	 * @param outerType JstType
 	 */
 	public void setOuterType(JstType outerType) {
 		m_outerType = outerType;
 		setParent(m_outerType);
 	}
 
 	/**
 	 * Add given type as sibling type of this type
 	 * @param siblingType JstType
 	 */
 	public void addSiblingType(final JstType siblingType) {
 		if (siblingType == null){
 			return;
 		}
 		synchronized(this){
 			if (m_siblingTypes != null && m_siblingTypes.contains(siblingType)){
 				return;
 			}
 		
 			if (m_siblingTypes == null) {
 				m_siblingTypes = new ArrayList<JstType>(1);
 			}
 			m_siblingTypes.add(siblingType);
 			addChild(siblingType);
 		}
 		siblingType.m_containingType = this;
 		siblingType.setParent(this);
 	}
 	
 	/**
 	 * Answer whether the type has sibling type with given short name
 	 * @param shortName String
 	 * @return boolean
 	 */
 	public boolean hasSiblingType(final String shortName){
 		return getSiblingType(shortName) != null;
 	}
 
 	public JstParamType addParam(final String paramName){
 		if (paramName == null){
 			return null;
 		}
 		
 		String trimmedParamName = paramName.trim(); // remove white spaces
 		
 		synchronized (this){
 			if (m_paramTypes == null){
 				m_paramTypes = new LinkedHashMap<String,JstParamType>();
 			}
 			JstParamType pType = m_paramTypes.get(trimmedParamName);
 			if (pType != null){
 				return pType;
 			}
 			pType = new JstParamType(trimmedParamName);
 			m_paramTypes.put(trimmedParamName,pType);
 			return pType;
 		}
 	}
 	
 	public synchronized JstParamType getParamType(String name){
 		
 		JstParamType paramType = null;
 		
 		if (m_paramTypes != null){
 			paramType = m_paramTypes.get(name);
 		}
 		
 		if (paramType == null && isEmbededType()) {
 			return m_outerType.getParamType(name);
 		}
 		else {		
 			return paramType;
 		}
 	}
 	
 	public synchronized void clearParams(){
 		if (m_paramTypes != null){
 			m_paramTypes = null;
 		}
 	}
 	
 
 	public String getParamsDecoration(){
 		if (getParamNames().isEmpty()){
 			return EMPTY;
 		}
 		StringBuilder sb = new StringBuilder("<");
 		int i=0;
 		IJstType bType;
 		for (JstParamType p: getParamTypes()){
 			if (i++ > 0){
 				sb.append(",");
 			}
 			sb.append(p.getSimpleName());
 			if (!p.getBounds().isEmpty()){
 				bType = p.getBounds().get(0);
 				sb.append(" extends ").append(bType.getSimpleName());
 				if (bType instanceof JstTypeWithArgs){
 					sb.append(((JstTypeWithArgs)bType).getArgsDecoration());
 				}
 			}
 		}
 		sb.append(">");
 		
 		return sb.toString();
 	}
 
 	
 	public List<IJstProperty> getEnumValues() {
 		if (m_enumValues == null) {
 			m_enumValues = new ArrayList<IJstProperty>();
 		}
 		return m_enumValues;
 	}
 	
 	public void setEnumValues(List<IJstProperty> enumValues) {
 		for(IJstProperty enumV : enumValues){
 			addEnumValue(enumV);
 		}
 		
 	}
 	
 	public synchronized void clearEnumValues(){
 		m_enumValues = null;
 	}
 	
 	public void addEnumValue(IJstProperty enumVal) {
 		synchronized(this){
 			if (m_enumValues == null){
 				m_enumValues = new ArrayList<IJstProperty>();
 			}
 			//DO NOT DEDUP at the translation level
 //			if (hasEnumValue(enumVal)) {
 //				return;
 //			}
 			m_enumValues.add(enumVal);
 		}
 	}
 	
 	/**
 	 * After the input mixinType is modified, with property or method changes,
 	 * need to re-create all the proxy methods and properties in current type
 	 * using mixin
 	 * @param jstType
 	 */
 	public void fixMixin(IJstType mixinType) {
 		
 		if (!hasMixins() || this == mixinType) { // has to have mixin
 			return;
 		}
 		
 		List<? extends IJstTypeReference> mixinDependencies = getMixinsRef();
 		boolean isMixIn = false;
 		
 		for (IJstTypeReference typeRef : mixinDependencies) {
 			if (typeRef.getReferencedType() == mixinType) {
 				isMixIn = true;
 				break;
 			}
 		}
 		
 		if (isMixIn) {
 			// remove old proxy properties belonging to mixin jstType
 			List<IJstProperty> removePropList = new ArrayList<IJstProperty>();
 			for (IJstProperty prop : getProperties()) {
 				if (prop.getOwnerType() == mixinType) {
 					removePropList.add(prop);
 				}
 			}
 			
 			for (IJstProperty removeProp : removePropList) {
 				removeProperty(removeProp.getName().getName(), removeProp.isStatic());
 			}
 			
 			// remove old proxy methods belonging to mixin jstType
 			List<IJstMethod> removeMtdList = new ArrayList<IJstMethod>();
 			for (IJstMethod mtd : getMethods()) {
 				if (mtd.getOwnerType() == mixinType) {
 					removeMtdList.add(mtd);
 				}
 			}
 			
 			for (IJstMethod removeMtd : removeMtdList) {
 				removeMethod(removeMtd.getName().getName(), removeMtd.isStatic());
 			}
 			
 			// add new proxy methods and properties from new mixin jstType
 			for (IJstMethod mtd : mixinType.getMethods()) {
 				addMethod(new JstProxyMethod(mtd));
 			}
 			// add proxy properties for mixin properties
 			for (IJstProperty prop : mixinType.getProperties()) {
 				addProperty(new JstProxyProperty(prop));
 			}
 		}			
 	}
 	
 	/*
 	private boolean hasEnumValue(IJstProperty enumVal) {
 		for (IJstProperty val : m_enumValues) {
 			if (val.getName().getName().equals(enumVal.getName().getName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	 */
 	/**
 	 * Answer the status of this type
 	 * @return Info
 	 */
 	public Info getStatus(){
 		return m_info;
 	}
 
 	@Override
 	public synchronized void accept(IJstNodeVisitor visitor){
 		visitor.visit(this);
 	}
 	
 	@Override
 	public String toString() {
 		Z z = new Z();
 		z.format("m_name", m_simpleName);
 		z.format("m_alias", m_alias);
 		z.format("m_pkg", getPackage());
 		
 		synchronized(this){
 			if (m_imports!=null && !m_imports.isEmpty()) {
 				//for (JstTypeReference n : m_imports.values()) {
 				//	z.format("import", n.getReferencedType().getName());
 				//}
 			}
 			if (m_extends != null) {
 				for (IJstTypeReference n : m_extends) {
 					z.format("extend", n.getReferencedType().getName());
 				}
 			}
 			if (m_enumValues != null) {
 				int i = 0;
 				for (IJstProperty n : m_enumValues) {
 					z.format("m_enumValues[" + i + "]", n.getName());
 					i++;
 				}
 			}
 		}
 		return z.toString();
 	}
 
 	public void dump() {
 		Z z = new Z();
 		z.format("type", getName());
 		z.format("package", getPackage());
 		z.format("constructor", getConstructor());
 		if (m_extends != null) {
 			for (IJstTypeReference n : m_extends) {
 				z.format("extend", n.getReferencedType().getName());
 			}
 		}
 		z.format("static props", getStaticProperties());
 		z.format("static methods", getStaticMethods());
 		z.format("instance props", getInstanceProperties());
 		z.format("instance methods", getInstanceMethods());
 
 		System.out.println(z);
 
 	}
 	
 	public Map<String, ? extends IJstType> getImportsMap() {
 		Map<String, IJstType> map = new LinkedHashMap<String, IJstType>();
 		synchronized (this) {
 			if(m_imports!=null){
 				for (Map.Entry<String, IJstTypeReference> me: m_imports.entrySet()) {
 					map.put(me.getKey(), me.getValue().getReferencedType());
 				}
 			}
 		}
 		return Collections.unmodifiableMap(map);
 	}
 	
 	public Map<String, ? extends IJstType> getInactiveImportsMap() {
 		Map<String, IJstType> map = new LinkedHashMap<String, IJstType>();
 		synchronized (this) {
 			if(m_inactiveImports!=null){
 				for (Map.Entry<String, IJstTypeReference> me: m_inactiveImports.entrySet()) {
 					map.put(me.getKey(), me.getValue().getReferencedType());
 				}
 			}
 		}
 		return Collections.unmodifiableMap(map);
 	}
 
 	//
 	// Inner
 	//
 	public static enum Category {
 		CLASS,
 		INTERFACE,
 		ENUM, 
 		MODULE, 
 		OTYPE,
 		FTYPE,
 		LTYPE
 	}
 
 	public static class Info {
 		private static final int HAS_NAME = 1;
 		private static final int HAS_DECL = 2;
 		private static final int HAS_IMPL = 4;
 		private static final int HAS_RESOLUTION = 5;
 		private static final int IS_PHANTOM = 8;
 		private static final int IS_PROMOTED= 9;
 		private static final int IS_ALIAS_PROMOTED= 10;
 		
 		private int m_status;
 		private boolean m_isValid;
 		
 		public void setHasName(){
 			m_status |= HAS_NAME;
 		}
 		
 		public boolean hasName(){
 			return (m_status & HAS_NAME) == HAS_NAME;
 		}
 		
 		public void setHasDecl(){
 			m_status |= HAS_DECL;
 		}
 		
 		public boolean hasImpl(){
 			return (m_status & HAS_IMPL) == HAS_IMPL;
 		}
 		
 		public void setHasResolution(){
 			m_status |= HAS_RESOLUTION;
 		}
 		
 		public boolean hasResolution(){
 			return (m_status & HAS_RESOLUTION) == HAS_RESOLUTION;
 		}
 		
 		public void setHasImpl(){
 			m_status |= HAS_IMPL;
 		}
 		
 		public boolean hasDecl(){
 			return (m_status & HAS_DECL) == HAS_DECL;
 		}
 		
 		public void setIsValid(boolean isValid){
 			m_isValid = isValid;
 		}
 		
 		public boolean isValid(){
 			return m_isValid;
 		}
 		
 		public void setIsPhantom() {
 			m_status |= IS_PHANTOM;
 		}
 		
 		public boolean isPhantom() {
 			return (m_status & IS_PHANTOM) == IS_PHANTOM;
 		}
 		
 		public void setGlobalsPromoted() {
 			m_status |= IS_PROMOTED;
 		}
 		
 		public boolean areGlobalsPromoted() {
 			return (m_status & IS_PROMOTED) == IS_PROMOTED;
 		}
 		
 		public boolean isAliasTypeNamePromoted() {
 			return (m_status & IS_ALIAS_PROMOTED) == IS_ALIAS_PROMOTED;
 		}
 		
 		
 		@Override
 		public String toString(){
 			Z z = new Z();
 			z.format("m_isValid", m_isValid);
 			z.format("hasName", hasName());
 			z.format("hasDecl", hasDecl());
 			z.format("hasImpl", hasImpl());
 			z.format("isPhantom", isPhantom());
 			z.format("isPromoted", areGlobalsPromoted());
 			z.format("isAliasTypeNamePromoted", isAliasTypeNamePromoted());
 			return z.toString();
 		}
 
 
 	}
 
 	public void addInitWithoutChild(final IStmt stmt, boolean isStatic) {
 		if (stmt == null){
 			return;
 		}
 		if (isStatic) {
 			if (m_staticInits == null) {
 				m_staticInits = new ArrayList<IStmt>();
 			}
 			m_staticInits.add(stmt);
 		} 
 		else {
 			if (m_instanceInits == null) {
 				m_instanceInits = new ArrayList<IStmt>();
 			}
 			m_instanceInits.add(stmt);
 		}
 	}
 	
 	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		// Re-instantiate the transient objects
 		m_info = new Info();
 		m_info.setHasResolution();
 		m_info.setHasName();
 	}
 	
 	
 	private Object readResolve() throws ObjectStreamException{
 		IJstType type = JstCache.getInstance().getType(getName());
 		if(type!=null){
 			return type;
 		}
 		return this;
 	}
 
 
 	public boolean isLocalType() {
 		return m_isLocalType;
 	}
 
 	public void setLocalType(boolean localType) {
 		m_isLocalType = localType;
 	}
 
 	public List<? extends IJstTypeReference> getExpectsRef() {
 
 		if (m_expects == null) {
 			return Collections.emptyList();
 		}
 		return Collections.unmodifiableList(m_expects);
 	}
 
 	public List<IJstTypeReference> getInactiveImportsRef() {
 		List<IJstTypeReference> list = new ArrayList<IJstTypeReference>();
 		synchronized(this){			
 			if (m_inactiveImports == null || m_inactiveImports.isEmpty()) {
 				return Collections.emptyList();
 			}
 			for (IJstTypeReference tr: m_inactiveImports.values()){
 					list.add(tr);
 				
 			}
 		}
 		
 		return list;
 	}
 	
 
 
 	@Override
 	/**
 	 * added by huzhou@ebay.com to support fully qualified types' linkages
 	 */
 	public IJstType getFullyQualifiedImport(String typeName) {
 		if (typeName == null || m_fullyQualifiedImports==null || m_fullyQualifiedImports.isEmpty()) {
 			return null;
 		}
 		synchronized (this){
 			IJstTypeReference t = m_fullyQualifiedImports.get(typeName);
 			return (t != null)? t.getReferencedType() : null;
 		}
 	}
 
 	@Override
 	/**
 	 * added by huzhou@ebay.com to support fully qualified types' linkages
 	 */
 	public List<? extends IJstType> getFullyQualifiedImports() {
 		List<IJstType> list = new ArrayList<IJstType>();
 		synchronized(this){			
 			if (m_fullyQualifiedImports == null || m_fullyQualifiedImports.isEmpty()) {
 				return Collections.emptyList();
 			}
 			
 			for (IJstTypeReference tr: m_fullyQualifiedImports.values()){
 				IJstType type = tr.getReferencedType();
 				boolean isDup = false;
 				for (IJstType t: list){
 					if (type.getName().equals(t.getName())){
 						isDup = true;
 						break;
 					}
 				}
 				if (!isDup){
 					list.add(type);
 				}		
 			}
 		}
 		
 		return list;
 	}
 
 	@Override
 	/**
 	 * added by huzhou@ebay.com to support fully qualified types' linkages
 	 */
 	public List<? extends IJstTypeReference> getFullyQualifiedImportsRef() {
 		List<IJstTypeReference> list = new ArrayList<IJstTypeReference>();
 		synchronized(this){			
 			if (m_fullyQualifiedImports == null || m_fullyQualifiedImports.isEmpty()) {
 				return Collections.emptyList();
 			}
 			for (IJstTypeReference tr: m_fullyQualifiedImports.values()){
 					list.add(tr);
 				
 			}
 		}
 		
 		return list;
 	}
 	
 	/**
 	 * added by huzhou@ebay.com to support fully qualified types' linkages
 	 */
 	public Map<String, ? extends IJstType> getFullyQualifiedImportsMap() {
 		Map<String, IJstType> map = new LinkedHashMap<String, IJstType>();
 		synchronized (this) {
 			for (Map.Entry<String, IJstTypeReference> me: m_fullyQualifiedImports.entrySet()) {
 				map.put(me.getKey(), me.getValue().getReferencedType());
 			}
 		}
 		return Collections.unmodifiableMap(map);
 	}
 
 	public void clearAll() {
 		clearChildren(); // from base
 		clearAnnotations(); // from base
 		clearModifiers();
 		clearParams();
 		clearName();
 		clearAlias();
 		clearExpects();
 		clearMixins();
 		clearExtends();
 		clearImports();
 		clearMethods();
 		clearProperties();
 		clearSatisfies();
 		clearEmbeddedTypes();
 		clearEnumValues();
 		clearStaticInits();
 		clearInstanceInits();
 		clearInactiveImports();
 		clearGlobalVars();
 		clearOTypes();
 		clearOptions();
 		clearVarTable();
 		clearSecondaryTypes();
 		clearOuterType();
 	}
 
 	private void clearGlobalVars() {
 		for (IJstGlobalVar gbl: getGlobalVars()){
 			if (gbl instanceof BaseJstNode){
 				removeChild((BaseJstNode)gbl);
 			}
 		}
 		synchronized(this){
 			m_gvars = null; // new ArrayList<IJstGlobalVar>();
 		}
 	}
 	
 	public boolean hasGlobalVars(){
 		return m_gvars!=null && !m_gvars.isEmpty();
 	}
 
 	@Override
 	public IJstGlobalVar getGlobalVar(String name) {
 		return getGlobalVar(name, false);
 	}
 
 	@Override
 	public IJstGlobalVar getGlobalVar(String name, boolean recursive) {
 		if (name == null){
 			return null;
 		}
 		for (IJstGlobalVar glb: getGlobalVars()){
 			if (name.equals(glb.getName().getName())){
 				return glb;
 			}
 		}
 		if (recursive && !getExtends().isEmpty()){
 			IJstGlobalVar glbl;
 			for (IJstType t: getExtends()){
 				if (t == this) continue; // prevent recursive on itself
 				
 				glbl = t.getGlobalVar(name, recursive);
 				if (glbl != null){
 					return glbl;
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public List<? extends IJstGlobalVar> getGlobalVars() {
 		if(m_gvars==null || m_gvars.isEmpty()){
 			return Collections.emptyList();
 		}
 		return Collections.unmodifiableList(m_gvars);
 	}
 
 	/**
 	 * Add given global variable to the type
 	 * @param global IJstGlobalVar
 	 */
 	public void addGlobalVar(final IJstGlobalVar global) {
 		if (global == null || getGlobalVars().contains(global)) {
 			return;
 		}
 		synchronized(this){
 			if(m_gvars==null){
 				m_gvars = new ArrayList<IJstGlobalVar>();
 			}
 			m_gvars.add(global);
 			addChild(global);
 		}
 	}
 	
 	/**
 	 * Remove global var with given name from the type
 	 * @param global String
 	 * @return IJstGlobalVar the removed global
 	 */
 	public IJstGlobalVar removeGlobalVar(final String global) {
 		if (global == null || m_gvars==null) {
 			return null;
 		}
 		synchronized(this){
 			IJstGlobalVar glb = getGlobalVar(global);
 			m_gvars.remove(glb);
 			if (glb instanceof BaseJstNode){
 				removeChild((BaseJstNode)glb);
 			}
 			return glb;
 		}
 	}
 
 	
 	@Override
 	public boolean isImpliedImport() {
 		return m_impliedImport;
 	}
 	
 	public void setImpliedImport(boolean impliedImport){
 		m_impliedImport = impliedImport;
 	}
 	
 	@Override
 	public Map<String, Object> getOptions() {
 		if(m_options==null){
 			return Collections.EMPTY_MAP;
 		}
 		return m_options;
 	}
 	
 	public void addOption(String name, Object value){
 		if(m_options==null){
 			m_options = new LinkedHashMap<String, Object>();
 		}
 		m_options.put(name, value);
 	}
 
 	public void addSecondaryType(IJstType secondaryType){
 		if(m_secondaryTypes==null){
 			m_secondaryTypes = new ArrayList<IJstType>();
 		}
 		m_secondaryTypes.add(secondaryType);
 	}
 	
 	public void addSecondaryType(final JstType secondaryType) {
 		if (secondaryType == null){
 			return;
 		}
 		synchronized(this){
 			if (m_secondaryTypes != null && m_secondaryTypes.contains(secondaryType)){
 				return;
 			}
 		
 			if (m_secondaryTypes == null) {
 				m_secondaryTypes = new ArrayList<IJstType>(1);
 			}
 			m_secondaryTypes.add(secondaryType);
 //			addChild(secondaryType);
 		}
 //		secondaryType.m_outerType = this;
 //		secondaryType.setParent(this);
 	}
 	
 	@Override
 	public List<IJstType> getSecondaryTypes() {
 		if(m_secondaryTypes==null){
 			return Collections.EMPTY_LIST;
 		}
 		return Collections.unmodifiableList(m_secondaryTypes);
 	}
 
 	public void setSingleton(boolean b) {
 		m_singleton = b;
 		
 	}
 	
 	public boolean isSingleton() {
 		return m_singleton;
 		
 	}
 
 	public void setExpects(List<IJstType> expects) {
 		for (IJstType expect : expects) {
 			addExpects(expect);
 		}
 		
 	}
 
 	public void setExtends(List<IJstType> extends1) {
 		// TODO should this be in reverse order?
 		for (IJstType extend : extends1) {
 			addExtend(extend);
 		}
 		
 	}
 
 	public void setImports(List<IJstType> imports) {
 		for (IJstType iJstType : imports) {
 			addImport(iJstType);
 		}
 		
 	}
 
 	public void setInactiveImports(List<? extends IJstType> inactiveImports) {
 		for (IJstType iJstType : inactiveImports) {
 			addInactiveImport(iJstType);
 		}
 		
 	}
 
 	public void setMethods(List<IJstMethod> methods) {
 		for (IJstMethod iJstMethod : methods) {
 			addMethod(iJstMethod);
 		}
 		
 	}
 
 	public void setProperties(List<IJstProperty> properties) {
 		for (IJstProperty iJstProperty : properties) {
 			addProperty(iJstProperty);
 		}
 		
 	}
 
 	public void setInstanceInitializers(List<IStmt> instanceInitializers) {
 		for (IStmt iStmt : instanceInitializers) {
 			addInit(iStmt, false);
 		}
 		
 	}
 
 	public void setStaticInitializers(List<IStmt> staticInitializers) {
 		for (IStmt iStmt : staticInitializers) {
 			addInit(iStmt, true);
 		}
 		
 	}
 
 	public void setGlobalVars(List<? extends IJstGlobalVar> globalVars) {
 		for (IJstGlobalVar iJstGlobalVar : globalVars) {
 			addGlobalVar(iJstGlobalVar);
 		}
 		
 	}
 
 	public void setMixins(List<IJstType> mixins) {
 		for (IJstType iJstType : mixins) {
 			addMixin(new JstTypeReference(iJstType));
 		}
 		
 	}
 
 	public void setModifers(JstModifiers modifiers) {
 		m_modifiers =modifiers;
 		
 	}
 
 	public void setOptions(Map<String, Object> options) {
 		m_options = options;
 		
 	}
 
 	public void setOTypes(List<IJstOType> oTypes) {
 		for (IJstOType iJstOType : oTypes) {
 			addOType(iJstOType);
 		}
 		
 	}
 
 	public void setParam(Map<String,JstParamType> paramTypes) {
 		m_paramTypes = paramTypes;
 		
 	}
 
 	public void setSatisfies(List<IJstType> satisfies) {
 		for (IJstType iJstType : satisfies) {
 			addSatisfy(iJstType);
 		}
 		
 	}
 
 	public void setSecondaryTypes(List<IJstType> secondaryTypes) {
 		m_secondaryTypes = secondaryTypes;
 		
 	}
 
 	public void setSiblingTypes(List<JstType> siblingTypes) {
 		for (JstType jstType : siblingTypes) {
 			addSiblingType(jstType);
 		}
 		
 	}
 
 	public void setStatus(Info status) {
 		m_info = status;
 		
 	}
 
 
 
 
 }
