 /*
  * @(#)Page.java
  *
 * Copyright 2009 Instituto Superior Tecnico, João Figueiredo, Luis Cruz, Paulo Abrantes, Susana Fernandes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Contents Module for the MyOrg web application.
  *
  *   The Contents Module is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published
  *   by the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.*
  *
  *   The Contents Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Contents Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package module.contents.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.TreeSet;
 
 import myorg.domain.MyOrg;
 import myorg.domain.VirtualHost;
 import myorg.domain.contents.INode;
 import myorg.domain.contents.Node;
 import myorg.domain.groups.PersistentGroup;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixWebFramework.util.DomainReference;
 import pt.ist.fenixframework.pstm.Transaction;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class Page extends Page_Base {
 
     public static class PageBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	MultiLanguageString title;
 	MultiLanguageString link;
 	DomainReference<Node> parentNode;
 	DomainReference<VirtualHost> virtualHost;
 	DomainReference<PersistentGroup> group;
 
 	public PageBean() {
 	}
 
 	public PageBean(final VirtualHost virtualHost, final Node parentNode) {
 	    this();
 	    setParentNode(parentNode);
 	    setVirtualHost(virtualHost);
 	}
 
 	public PageBean(final VirtualHost virtualHost, final Node parentNode, PersistentGroup group) {
 	    this();
 	    setParentNode(parentNode);
 	    setVirtualHost(virtualHost);
 	    setPersistentGroup(group);
 	}
 
 	public MultiLanguageString getTitle() {
 	    return title;
 	}
 
 	public void setTitle(MultiLanguageString title) {
 	    this.title = title;
 	}
 
 	public MultiLanguageString getLink() {
 	    return link;
 	}
 
 	public void setLink(MultiLanguageString link) {
 	    this.link = link;
 	}
 
 	public Node getParentNode() {
 	    return parentNode == null ? null : parentNode.getObject();
 	}
 
 	public void setParentNode(final Node parentNode) {
 	    this.parentNode = parentNode == null ? null : new DomainReference<Node>(parentNode);
 	}
 
 	public VirtualHost getVirtualHost() {
 	    return virtualHost == null ? null : virtualHost.getObject();
 	}
 
 	public void setVirtualHost(final VirtualHost virtualHost) {
 	    this.virtualHost = virtualHost == null ? null : new DomainReference<VirtualHost>(virtualHost);
 	}
 
 	public PersistentGroup getPersistentGroup() {
 	    return group == null ? null : group.getObject();
 	}
 
 	public void setPersistentGroup(final PersistentGroup group) {
 	    this.group = group == null ? null : new DomainReference<PersistentGroup>(group);
 	}
     }
 
     public Page() {
 	super();
 	setMyOrg(MyOrg.getInstance());
     }
 
     public Page(final PageBean pageBean) {
 	this();
 	setTitle(pageBean.getTitle());
 	setLink(pageBean.getLink());
 	new PageNode(pageBean.getVirtualHost(), pageBean.getParentNode(), this, null);
     }
 
     @Service
     public static INode createNewPage(final PageBean pageBean) {
 	final Page page = new Page(pageBean);
 	return page.getNodesIterator().next();
     }
 
     public void deleteIfDisconnected() {
 	if (!hasAnyNodes()) {
 	    for (final Section section : getSectionsSet()) {
 		section.delete();
 	    }
 	    removeMyOrg();
 	    Transaction.deleteObject(this);
 	}
     }
 
     public Set<Section> getOrderedSections() {
 	final Set<Section> sections = new TreeSet<Section>(Section.COMPARATOR_BY_ORDER);
 	sections.addAll(getSectionsSet());
 	return sections;
     }
 
     @Service
     public void reorderSections(final ArrayList<Section> sections) {
 	for (final Section section : getSectionsSet()) {
 	    if (!sections.contains(section)) {
 		throw new Error("Sections changed!");
 	    }
 	}
 
 	int i = 0;
 	for (final Section section : sections) {
 	    if (!hasSections(section)) {
 		throw new Error("Sections changed!");
 	    }
 	    section.setSectionOrder(Integer.valueOf(i++));
 	}
     }
 
 }
