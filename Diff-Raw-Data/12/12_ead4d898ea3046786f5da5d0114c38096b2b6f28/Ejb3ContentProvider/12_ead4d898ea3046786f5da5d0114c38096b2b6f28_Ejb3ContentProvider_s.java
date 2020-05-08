 /***********************************************************************
  * Copyright (c) 2008 by SAP AG, Walldorf. 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SAP AG - initial API and implementation
  ***********************************************************************/
 package org.eclipse.jst.jee.ui.internal.navigator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
 import org.eclipse.jst.javaee.ejb.EJBJar;
 import org.eclipse.jst.javaee.ejb.EntityBean;
 import org.eclipse.jst.javaee.ejb.MessageDrivenBean;
 import org.eclipse.jst.javaee.ejb.SessionBean;
 import org.eclipse.jst.jee.ui.internal.navigator.JndiRefNode.KINDS;
 import org.eclipse.jst.jee.ui.internal.navigator.ejb.ActivationConfigProperties;
 import org.eclipse.jst.jee.ui.internal.navigator.ejb.BeanInterfaceNode;
 import org.eclipse.jst.jee.ui.internal.navigator.ejb.BeanNode;
 import org.eclipse.jst.jee.ui.internal.navigator.ejb.GroupEJBProvider;
 import org.eclipse.jst.jee.ui.plugin.JEEUIPlugin;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 /**
  * Ejb 3.0 Content provider is Deployment Descriptor content provider, 
  * used for constructing of the descriptor tree in project explorer. 
  * 
  * @author Dimitar Giormov
  */
 public class Ejb3ContentProvider extends JEE5ContentProvider {
 
 
 	public Object[] getChildren(Object aParentElement) {
 		List<Object> children = new ArrayList<Object>();
 		IProject project = null;
 
 		if (aParentElement instanceof AbstractGroupProvider) {
 			List lst = ((AbstractGroupProvider) aParentElement).getChildren();
 			children.addAll(lst);
 		} else if (aParentElement instanceof AbstractDDNode) {
 			List lst = ((AbstractDDNode) aParentElement).getChildren();
 			children.addAll(lst);
 		} else if (aParentElement instanceof SessionBean) {
 			SessionBean sb = ((SessionBean)aParentElement);
 			addSessionBeanSubNodes(sb, children);
 
 		} else if (aParentElement instanceof EntityBean) {
 			EntityBean eb = ((EntityBean)aParentElement);
 
 			addEntityBeanSubNodes(eb, children);
 
 		} else if (aParentElement instanceof MessageDrivenBean) {
 			Object msgBean = new BeanNode((MessageDrivenBean) aParentElement);
 			addActivationConfigProperties((MessageDrivenBean) aParentElement, children);
 			children.add(msgBean);
 		} else 
 			if (aParentElement instanceof IAdaptable) {
 				project = (IProject) ((IAdaptable) aParentElement)
 				.getAdapter(IPROJECT_CLASS);
 				if (project != null) {
 					if (isEjbModuleProject(project)) {
 						IModelProvider modelProvider = getCachedModelProvider(project);
 						GroupEJBProvider element = new GroupEJBProvider((EJBJar) modelProvider.getModelObject());
 						element.setProjectName(project.getName());
 						children.add(element);
 					}
 				}
 			}
 		return children.toArray();
 	}
 
 	private void addActivationConfigProperties(MessageDrivenBean parentElement,
 			List<Object> children) {
 		if (parentElement.getActivationConfig() != null && parentElement.getActivationConfig().getActivationConfigProperties() != null && !parentElement.getActivationConfig().getActivationConfigProperties().isEmpty()){
 			children.add(new ActivationConfigProperties(parentElement.getActivationConfig().getActivationConfigProperties()));
 		}
 	}
 
 	private void addEntityBeanSubNodes(EntityBean eb, List<Object> children) {
 		if (eb.getLocal() != null) {
 			children.add(new BeanInterfaceNode(eb, (String)eb.getLocal(), BeanInterfaceNode.KINDS.LOCAL));
 		}
 		if (eb.getLocalHome() != null) {
 			children.add(new BeanInterfaceNode(eb, (String)eb.getLocalHome(), BeanInterfaceNode.KINDS.LOCAL_HOME));
 		}
 
 		if (eb.getRemote() != null) {
 			children.add(new BeanInterfaceNode(eb, (String)eb.getRemote(), BeanInterfaceNode.KINDS.REMOTE));
 		}
 		if (eb.getHome() != null) {
 			children.add(new BeanInterfaceNode(eb, (String)eb.getHome(), BeanInterfaceNode.KINDS.REMOTE_HOME));
 		}
 
 
 		children.add(new BeanNode((EntityBean) eb));
 
 	}
 
 	private void addSessionBeanSubNodes(SessionBean sb, List children) {
 		addClassRelatedInfo(sb, children);
 		addSessionJNDIRefInfo(sb, children);
 	}
 
 	private void addSessionJNDIRefInfo(SessionBean sb, List children) {
 		if (sb.getEjbLocalRefs() != null && !sb.getEjbLocalRefs().isEmpty()){
 			children.add(new JndiRefNode(sb.getEjbLocalRefs(), KINDS.EJBLOCALREF));
 		}
 
 		if (sb.getEjbRefs() != null && !sb.getEjbRefs().isEmpty()){
 			children.add(new JndiRefNode(sb.getEjbRefs(), KINDS.EJBREF));
 		}
 
 		if (sb.getResourceEnvRefs() != null && !sb.getResourceEnvRefs().isEmpty()){
 			children.add(new JndiRefNode(sb.getResourceEnvRefs(), KINDS.RESENVENTY));
 		}
 		if (sb.getEnvEntries() != null && !sb.getEnvEntries().isEmpty()){
 			children.add(new JndiRefNode(sb.getEnvEntries(), KINDS.ENVENTRY));
 		}
 
 		if (sb.getResourceRefs() != null && !sb.getResourceRefs().isEmpty()){
 			children.add(new JndiRefNode(sb.getResourceRefs(), KINDS.RESREF));
 		}
 
 		if (sb.getServiceRefs() != null && !sb.getServiceRefs().isEmpty()){
 			children.add(new JndiRefNode(sb.getServiceRefs(), KINDS.SERVICEREF));
 		}
 	}
 
 	private void addSessionJNDIRefInfo(EntityBean eb, List children) {
 		if (eb.getEjbLocalRefs() != null && !eb.getEjbLocalRefs().isEmpty()){
 			children.add(new JndiRefNode(eb.getEjbLocalRefs(), KINDS.EJBLOCALREF));
 		}
 
 		if (eb.getEjbRefs() != null && !eb.getEjbRefs().isEmpty()){
 			children.add(new JndiRefNode(eb.getEjbRefs(), KINDS.EJBREF));
 		}
 
 		if (eb.getResourceEnvRefs() != null && !eb.getResourceEnvRefs().isEmpty()){
 			children.add(new JndiRefNode(eb.getResourceEnvRefs(), KINDS.RESENVENTY));
 		}
 		if (eb.getEnvEntries() != null && !eb.getEnvEntries().isEmpty()){
 			children.add(new JndiRefNode(eb.getEnvEntries(), KINDS.ENVENTRY));
 		}
 
 		if (eb.getResourceRefs() != null && !eb.getResourceRefs().isEmpty()){
 			children.add(new JndiRefNode(eb.getResourceRefs(), KINDS.RESREF));
 		}
 
 		if (eb.getServiceRefs() != null && !eb.getServiceRefs().isEmpty()){
 			children.add(new JndiRefNode(eb.getServiceRefs(), KINDS.SERVICEREF));
 		}
 	}
 
 	private void addClassRelatedInfo(SessionBean sb, List children) {
 		if (sb.getLocal() != null) {
 			children.add(new BeanInterfaceNode(sb, (String)sb.getLocal(), BeanInterfaceNode.KINDS.LOCAL));
 		}
 		if (sb.getLocalHome() != null) {
 			children.add(new BeanInterfaceNode(sb, (String)sb.getLocalHome(), BeanInterfaceNode.KINDS.LOCAL_HOME));
 		}
 
 		if (sb.getRemote() != null) {
 			children.add(new BeanInterfaceNode(sb, (String)sb.getRemote(), BeanInterfaceNode.KINDS.REMOTE));
 		}
 		if (sb.getHome() != null) {
 			children.add(new BeanInterfaceNode(sb, (String)sb.getHome(), BeanInterfaceNode.KINDS.REMOTE_HOME));
 		}
 

		children.add(new BeanNode((SessionBean) sb));
 		List r = sb.getBusinessLocals();
 		for (Object locals : r) {
 			children.add(new BeanInterfaceNode(sb, (String)locals, BeanInterfaceNode.KINDS.BUSSINESS_LOCAL));
 		}
 		r = sb.getBusinessRemotes();
 		for (Object locals : r) {
 			children.add(new BeanInterfaceNode(sb, (String)locals, BeanInterfaceNode.KINDS.BUSSINESS_REMOTE));
 		}
 	}
 
 	private boolean isEjbModuleProject(IProject project) {
 		try {
 			IFacetedProject facetedProject = ProjectFacetsManager.create(project);
 			IProjectFacetVersion installedVersion = facetedProject.getInstalledVersion(ProjectFacetsManager.getProjectFacet(IJ2EEFacetConstants.EJB).getVersion(IJ2EEFacetConstants.EJB_30.getVersionString()).getProjectFacet());
 			return installedVersion != null;
 		} catch (CoreException e) {
 			JEEUIPlugin.logError("Can not acces project", e); //$NON-NLS-1$
 		}
 		return false;
 	}
 
 	public boolean hasChildren(Object element) {
 		if (element instanceof AbstractGroupProvider) {
 			return ((AbstractGroupProvider) element).hasChildren();
 		} else if (element instanceof AbstractDDNode) {
 			return ((AbstractDDNode) element).hasChildren();
 		} else if (element instanceof SessionBean) {
 			return true;
 		} else if (element instanceof EntityBean) {
 			return true;
 		}else if (element instanceof MessageDrivenBean) {
 			return true;
 		} else
 			return false;
 	}
 
 	public Object getParent(Object element) {
 		return null;
 	}
 
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 }
