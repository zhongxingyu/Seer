 /******************************************************************************* 
  * Copyright (c) 2007 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb.internal.scanner;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelException;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.filesystems.impl.FolderImpl;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.project.ext.store.XMLStoreConstants;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.jst.web.kb.IKbProject;
 import org.jboss.tools.jst.web.kb.internal.taglib.AbstractAttribute;
 import org.jboss.tools.jst.web.kb.internal.taglib.AbstractComponent;
 import org.jboss.tools.jst.web.kb.internal.taglib.AbstractTagLib;
 import org.jboss.tools.jst.web.kb.internal.taglib.ELFunction;
 import org.jboss.tools.jst.web.kb.internal.taglib.FaceletAttribute;
 import org.jboss.tools.jst.web.kb.internal.taglib.FaceletTag;
 import org.jboss.tools.jst.web.kb.internal.taglib.FaceletTagLibrary;
 import org.jboss.tools.jst.web.kb.internal.taglib.FacesConfigAttribute;
 import org.jboss.tools.jst.web.kb.internal.taglib.FacesConfigComponent;
 import org.jboss.tools.jst.web.kb.internal.taglib.FacesConfigTagLibrary;
 import org.jboss.tools.jst.web.kb.internal.taglib.TLDAttribute;
 import org.jboss.tools.jst.web.kb.internal.taglib.TLDLibrary;
 import org.jboss.tools.jst.web.kb.internal.taglib.TLDTag;
 import org.jboss.tools.jst.web.kb.taglib.Facet;
 import org.jboss.tools.jst.web.model.helpers.InnerModelHelper;
 import org.jboss.tools.jst.web.model.project.ext.store.XMLValueInfo;
 
 /**
  * @author Viacheslav Kabanovich
  */
 public class XMLScanner implements IFileScanner {
 	public static final String ATTR_SHORTNAME = "shortname"; //$NON-NLS-1$
 	public static final String ATTR_TAGCLASS = "tagclass"; //$NON-NLS-1$
 	public static final String ATTR_TAG_NAME = "tag-name"; //$NON-NLS-1$
 	public static final String ATTR_BODY_CONTENT = "bodycontent"; //$NON-NLS-1$
 	public static final String ATTR_FACET_NAME = "facet-name"; //$NON-NLS-1$
 	public static final String ATTR_ATTRIBUTE_NAME = "attribute-name"; //$NON-NLS-1$
 	public static final String ATTR_FUNC_SIGN = "function-signature"; //$NON-NLS-1$
 	public static final String ATTR_FUNC_NAME = "function-name"; //$NON-NLS-1$
 	public static final String ATTR_COMPONENT_TYPE = "component-type"; //$NON-NLS-1$
 	
 	public static final String XML_SUFFIX = ".xml";
 	public static final String TLD_SUFFIX = ".tld";	
 	
 	public XMLScanner() {}
 
 	/**
 	 * Returns true if file is probable component source - 
 	 * has components.xml name or *.component.xml mask.
 	 * @param resource
 	 * @return
 	 */	
 	public boolean isRelevant(IFile resource) {
 		String name = resource.getName().toLowerCase();
 		return name.endsWith(XML_SUFFIX) || name.endsWith(TLD_SUFFIX); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 	
 	/**
 	 * This method should be called only if isRelevant returns true;
 	 * Makes simple check if this java file contains annotation Name. 
 	 * @param resource
 	 * @return
 	 */
 	public boolean isLikelyComponentSource(IFile f) {
 		if(!f.isSynchronized(IFile.DEPTH_ZERO) || !f.exists()) return false;
 		XModel model = InnerModelHelper.createXModel(f.getProject());
 		if(model == null) return false;
 		XModelObject o = EclipseResourceUtil.getObjectByResource(model, f);
 		if(o == null) return false;
 		if(LibraryScanner.isTLDFile(o) 
 				|| LibraryScanner.isFaceletTaglibFile(o)
 				|| LibraryScanner.isFacesConfigFile(o)) return true;
 		return false;
 	}
 
 	/**
 	 * Returns list of components
 	 * @param f
 	 * @return
 	 * @throws ScannerException
 	 */
 	public LoadedDeclarations parse(IFile f, IKbProject sp) throws ScannerException {
 		XModel model = InnerModelHelper.createXModel(f.getProject());
 		if(model == null) return null;
 		XModelObject o = EclipseResourceUtil.getObjectByResource(model, f);
 		return parse(o, f.getFullPath(), sp);
 	}
 	
 	public LoadedDeclarations parse(XModelObject o, IPath source, IKbProject sp) {
 		if(o == null) return null;
 
 		if(o.getParent() instanceof FolderImpl) {
 			IFile f = ResourcesPlugin.getWorkspace().getRoot().getFile(source);
 			if(f != null && f.exists()) {
 				try {
 					((FolderImpl)o.getParent()).updateChildFile(o, f.getLocation().toFile());
 				} catch (XModelException e) {
 					ModelPlugin.getPluginLog().logError(e);
 				}
 				if(o.getParent() == null) {
 					boolean b = isLikelyComponentSource(f);
 					if(!b) return null;
 					o = EclipseResourceUtil.getObjectByResource(o.getModel(), f);
 					if(o == null) return null;
 				}
 			}
 		}
 		
 		LoadedDeclarations ds = new LoadedDeclarations();
 		if(LibraryScanner.isTLDFile(o)) {
 			parseTLD(o, source, sp, ds);
 		} else if(LibraryScanner.isFaceletTaglibFile(o)) {
 			parseFaceletTaglib(o, source, sp, ds);
 		} else if(LibraryScanner.isFacesConfigFile(o)) {
 			parseFacesConfig(o, source, sp, ds);
 		}
 		return ds;
 	}
 
 	private void parseTLD(XModelObject o, IPath source, IKbProject sp, LoadedDeclarations ds) {
 		TLDLibrary library = new TLDLibrary();
 		library.setId(o);
 		library.setURI(new XMLValueInfo(o, AbstractTagLib.URI));
 		library.setDisplayName(new XMLValueInfo(o, TLDLibrary.DISPLAY_NAME));
 		library.setShortName(new XMLValueInfo(o, ATTR_SHORTNAME));
 		String version = o.getAttributeValue(TLDLibrary.VERSION);
 		if(version == null) {
 			if("FileTLD_1_2".equals(o.getModelEntity().getName())) { //$NON-NLS-1$
 				version = "1.2"; //$NON-NLS-1$
 			} else {
 				version = "1.1"; //$NON-NLS-1$
 			}
 			library.setVersion(version);
 		} else {
 			library.setVersion(new XMLValueInfo(o, TLDLibrary.VERSION));
 		}
 
 		ds.getLibraries().add(library);
 
 		XModelObject[] ts = o.getChildren();
 		for (XModelObject t: ts) {
 			if(t.getModelEntity().getName().startsWith("TLDTag")) { //$NON-NLS-1$
 				AbstractComponent tag = new TLDTag();
 				tag.setId(t);
 
 				tag.setName(new XMLValueInfo(t, XMLStoreConstants.ATTR_NAME));
 				tag.setDescription(new XMLValueInfo(t, AbstractComponent.DESCRIPTION));
 				tag.setComponentClass(new XMLValueInfo(t, ATTR_TAGCLASS));
 				tag.setCanHaveBody(new XMLValueInfo(t, ATTR_BODY_CONTENT));
 				
 				XModelObject[] as = t.getChildren();
 				for(XModelObject a: as) {
 					if(a.getModelEntity().getName().startsWith("TLDAttribute")) { //$NON-NLS-1$
 						AbstractAttribute attr = new TLDAttribute();
 						attr.setId(a);
 						attr.setName(new XMLValueInfo(a, XMLStoreConstants.ATTR_NAME));
 						attr.setDescription(new XMLValueInfo(a, AbstractComponent.DESCRIPTION));
 						attr.setRequired(new XMLValueInfo(a, AbstractAttribute.REQUIRED));
 						
 						tag.addAttribute(attr);
 					}
 				}
 				
 				library.addComponent(tag);
 			}
 		}
 		
 	}
 
 	private void parseFaceletTaglib(XModelObject o, IPath source, IKbProject sp, LoadedDeclarations ds) {
 		FaceletTagLibrary library = new FaceletTagLibrary();
 		library.setId(o);
 		library.setURI(new XMLValueInfo(o, AbstractTagLib.URI));
 
 		ds.getLibraries().add(library);
 
 		XModelObject[] os = o.getChildren();
 		for (XModelObject t: os) {
 			String entity = t.getModelEntity().getName();
 			if(entity.startsWith("FaceletTaglibTag")) { //$NON-NLS-1$
 				FaceletTag tag = new FaceletTag();
 				tag.setId(t);
 				tag.setName(new XMLValueInfo(t, ATTR_TAG_NAME));
				tag.setDescription(new XMLValueInfo(t, AbstractComponent.DESCRIPTION));
 				XModelObject d = t.getChildByPath("declaration"); //$NON-NLS-1$
 				if(d != null && d.getModelEntity().getName().startsWith("FaceletTaglibComponent")) { //$NON-NLS-1$
 					String componentType = d.getAttributeValue(ATTR_COMPONENT_TYPE); //$NON-NLS-1$
 					if(componentType != null && componentType.length() > 0) {
 						tag.setComponentType(new XMLValueInfo(d, ATTR_COMPONENT_TYPE)); //$NON-NLS-1$
 					}
 				}
 				XModelObject[] as = t.getChildren();
 				for (XModelObject a: as) {
 					String entity2 = a.getModelEntity().getName();
 					if(entity2.startsWith("FaceletTaglibAttribute")) { //$NON-NLS-1$
 						FaceletAttribute attr = new FaceletAttribute();
 						attr.setId(a);
 						attr.setName(new XMLValueInfo(a, XMLStoreConstants.ATTR_NAME));
 						attr.setDescription(new XMLValueInfo(a, AbstractComponent.DESCRIPTION));
 						attr.setRequired(new XMLValueInfo(a, AbstractAttribute.REQUIRED));
 						
 						tag.addAttribute(attr);
 					}
 				}
 				library.addComponent(tag);
 			} else if(entity.startsWith("FaceletTaglibFunction")) { //$NON-NLS-1$
 				ELFunction f = new ELFunction();
 				f.setId(t);
 				f.setName(new XMLValueInfo(t, ATTR_FUNC_NAME));
 				f.setSignature(new XMLValueInfo(t, ATTR_FUNC_SIGN));
 				library.addFunction(f);
 			}
 		}
 	}
 
 	private void parseFacesConfig(XModelObject o, IPath source, IKbProject sp, LoadedDeclarations ds) {
 		FacesConfigTagLibrary library = new FacesConfigTagLibrary();
 		library.setId(o);
 		library.setURI("TODO"); //TODO what is the URI? //$NON-NLS-1$
 		
 		ds.getLibraries().add(library);
 
 		XModelObject componentFolder = o.getChildByPath("Components"); //$NON-NLS-1$
 		if(componentFolder == null) return;
 		XModelObject[] os = componentFolder.getChildren();
 		for (XModelObject c: os) {
 			FacesConfigComponent component = new FacesConfigComponent();
 			component.setId(c);
 			//what else can we take for the name? only attribute 'component-type' is available
 			component.setName(new XMLValueInfo(c, AbstractComponent.COMPONENT_TYPE));
 			
 			component.setComponentClass(new XMLValueInfo(c, AbstractComponent.COMPONENT_CLASS));
 			component.setComponentType(new XMLValueInfo(c, AbstractComponent.COMPONENT_TYPE));
 			component.setDescription(new XMLValueInfo(c, AbstractComponent.DESCRIPTION));
 			
 			XModelObject[] as = c.getChildren();
 			for (XModelObject child: as) {
 				String entity = child.getModelEntity().getName();
 				if(entity.startsWith("JSFAttribute")) { //$NON-NLS-1$
 					FacesConfigAttribute attr = new FacesConfigAttribute();
 					attr.setId(child);
 					attr.setName(new XMLValueInfo(child, ATTR_ATTRIBUTE_NAME));					
 					component.addAttribute(attr);
 				} else if(entity.startsWith("JSFFacet")) { //$NON-NLS-1$
 					Facet f = new Facet();
 					f.setId(child);
 					f.setName(new XMLValueInfo(child, ATTR_FACET_NAME));
 					f.setDescription(new XMLValueInfo(child, AbstractComponent.DESCRIPTION));
 					component.addFacet(f);
 				}
 			}
 			library.addComponent(component);
 		}
 		
 	}
 
 }
