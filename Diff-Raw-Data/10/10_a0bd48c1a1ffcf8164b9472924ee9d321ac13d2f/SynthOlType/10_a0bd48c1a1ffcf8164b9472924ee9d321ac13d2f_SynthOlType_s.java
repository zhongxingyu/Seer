 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jst.declaration;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.ISynthesized;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.term.NV;
 import org.eclipse.vjet.dsf.jst.term.ObjLiteral;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 
 public class SynthOlType extends JstType implements ISynthesized {
 
 	private static final long serialVersionUID = 1L;
 	public static final String TYPE_NAME = "(obj literal)";
 
 	private final ObjLiteral m_ol;
 	private boolean m_init = false;
 	
 	private List<IJstType> m_resolvedOType;
 	
 	public SynthOlType(ObjLiteral ol) {
 		m_ol = ol;
 		getModifiers().setDynamic();
 		this.addExtend(JstCache.getInstance().getType("ObjLiteral"));
 	}
 	
 	public ObjLiteral getObjectLiteral(){
 		return m_ol;
 	}
 	
 	@Override
 	public JstType getOwnerType() {
 		return m_ol.getOwnerType();
 	}
 	
 	private synchronized void init() {
 		if (!m_init) {
 			m_init = true;
			IJstType defaultType = JstCache.getInstance().getType("ObjLiteral");
 			for (NV nv: m_ol.getNVs()) {
 				JstIdentifier id = nv.getIdentifier();
 				IJstNode binding = id.getJstBinding();
 				if (binding instanceof IJstMethod) {
 					this.addMethod((IJstMethod)binding);
 				}
 				else {
					IJstType type = id.getResultType();
 					if (type == null || type == defaultType) {
 						IExpr expr = nv.getValue();
 						if (expr != null) {
 							type = expr.getResultType();
 						}
 						if (type == null) {
 							type = defaultType;
 						}
 					}
 					JstName name = new JstName(nv.getName());
 					name.setSource(nv.getIdentifier().getSource());
 					JstProperty pty = new JstProperty(type, name, (JstIdentifier) null, new JstModifiers().setPublic());
 					
 					this.addProperty(pty);
 				}
			}			
 		}
 	}
 	
 	@Override
 	public String getAlias() {
 		return TYPE_NAME;
 	}
 
 	@Override
 	public List<IJstProperty> getAllPossibleProperties(boolean isStatic, boolean recursive) {
 		init();
 		return super.getAllPossibleProperties(isStatic, recursive);
 	}
 
 
 	@Override
 	public List<IJstProperty> getInstanceProperties() {
 		init();
 		return super.getInstanceProperties();
 	}
 
 	@Override
 	public IJstProperty getInstanceProperty(String name) {
 		init();
 		return super.getInstanceProperty(name);
 	}
 
 	@Override
 	public IJstProperty getInstanceProperty(String name, boolean recursive) {
 		init();
 		return super.getInstanceProperty(name, recursive);
 	}
 
 	@Override
 	public String getName() {
 		return TYPE_NAME;
 	}
 
 	@Override
 	public List<IJstProperty> getProperties() {
 		init();
 		return super.getProperties();
 	}
 
 	@Override
 	public List<IJstProperty> getProperties(boolean isStatic) {
 		init();
 		return super.getProperties(isStatic);
 	}
 
 	@Override
 	public IJstProperty getProperty(String name) {
 		init();
 		return super.getProperty(name);
 	}
 
 	@Override
 	public IJstProperty getProperty(String name, boolean isStatic) {
 		init();
 		return super.getProperty(name, isStatic);
 	}
 
 	@Override
 	public IJstProperty getProperty(String name, boolean isStatic, boolean recursive) {
 		init();
 		return super.getProperty(name, isStatic, recursive);
 	}
 
 	@Override
 	public String getSimpleName() {
 		return TYPE_NAME;
 	}
 
 
 	@Override
 	public boolean hasInstanceProperties() {
 		init();
 		return super.hasInstanceProperties();
 	}
 
 	@Override
 	public boolean hasInstanceProperty(String name, boolean recursive) {
 		init();
 		return super.hasInstanceProperty(name, recursive);
 	}
 
 	@Override
 	public JstSource getSource() {
 		return m_ol.getSource();
 	}
 	
 	@Override
 	public IJstMethod getMethod(String name) {
 		init();
 		return super.getMethod(name);
 	}
 	
 	@Override
 	public IJstMethod getMethod(String name, boolean isStatic) {
 		init();
 		return super.getMethod(name, isStatic);
 	}
 	
 	@Override
 	public IJstMethod getMethod(String name, boolean isStatic, boolean recursive) {
 		init();
 		return super.getMethod(name, isStatic, recursive);
 	}
 	
 	@Override
 	public List<IJstMethod> getMethods() {
 		init();
 		return super.getMethods();
 	}
 	
 	@Override
 	public List<IJstMethod> getMethods(boolean isStatic) {
 		init();
 		return super.getMethods(isStatic);
 	}
 	
 	@Override
 	public List<IJstMethod> getMethods(boolean isStatic, boolean recursive) {
 		init();
 		return super.getMethods(isStatic, recursive);
 	}
 	
 	@Override
 	public boolean hasInstanceMethods() {
 		init();
 		return super.hasInstanceMethods();
 	}
 	
 	@Override
 	public boolean hasInstanceMethod(String mtdName, boolean recursive) {
 		init();
 		return super.hasInstanceMethod(mtdName, recursive);
 	}
 	
 	@Override
 	public List<IJstMethod> getInstanceMethods() {
 		init();
 		return super.getInstanceMethods();
 	}
 	
 	@Override
 	public IJstMethod getInstanceMethod(String name) {
 		init();
 		return super.getInstanceMethod(name);
 	}
 
 	@Override
 	public IJstMethod getInstanceMethod(final String name, boolean recursive) {
 		init();
 		return super.getInstanceMethod(name, recursive);
 	}
 
 //	public void setResolvedOType(IJstType m_resolvedOType) {
 //		this.m_resolvedOType = m_resolvedOType;
 //	}
 
 	public List<IJstType> getResolvedOTypes() {
 		return m_resolvedOType;
 	}
 
 	public void addResolvedOType(IJstType otype) {
 		if(m_resolvedOType==null){
 			m_resolvedOType = new ArrayList<IJstType>();
 		}
 		if(!m_resolvedOType.contains(otype)){
 			m_resolvedOType.add(otype);
 		}
 		
 	}
 	
 	
 }
