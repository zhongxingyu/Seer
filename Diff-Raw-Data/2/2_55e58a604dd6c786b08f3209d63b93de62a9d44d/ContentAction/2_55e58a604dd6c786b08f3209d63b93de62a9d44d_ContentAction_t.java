 /*
  * @(#)ContentAction.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: João Figueiredo, Luis Cruz, Paulo Abrantes, Susana Fernandes
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
 
 package module.contents.presentationTier.actions;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import module.contents.domain.Page;
 import module.contents.domain.PageNode;
 import module.contents.domain.Section;
 import module.contents.domain.Page.PageBean;
 import module.contents.domain.Section.SectionBean;
 import myorg.domain.VirtualHost;
 import myorg.domain.contents.INode;
 import myorg.domain.contents.Node;
 import myorg.presentationTier.Context;
 import myorg.presentationTier.LayoutContext;
 import myorg.presentationTier.actions.ContextBaseAction;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.fenixWebFramework.servlets.functionalities.CreateNodeAction;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping( path="/content" )
 public class ContentAction extends ContextBaseAction {
 
     @Override
     public Context createContext(final String contextPathString) {
 	final LayoutContext layoutContext = new LayoutContext(contextPathString);
 	layoutContext.setPageOperations("/pageOperations.jsp");
 	return layoutContext;
     }
 
     public final ActionForward viewPage(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws Exception {
 	final Context context = getContext(request);
 	if (context.getElements().isEmpty()) {
	    final Node node = Node.getFirstAvailableTopLevelNode();
 	    context.push(node);
 	}
 	return context.forward("/contents/page.jsp");
     }
 
     @CreateNodeAction( bundle="CONTENT_RESOURCES", key="option.create.new.page", groupKey="label.module.contents" )
     public final ActionForward prepareCreateNewPage(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final VirtualHost virtualHost = getDomainObject(request, "virtualHostToManageId");
 	final Node node = getDomainObject(request, "parentOfNodesToManageId");
 	final PageBean pageBean = new PageBean(virtualHost, node);
 	request.setAttribute("pageBean", pageBean);
 
 	final Context context = getContext(request);
 	return context.forward("/contents/newPage.jsp");
     }
 
     public final ActionForward createNewPage(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final PageBean pageBean = getRenderedObject();
 	Page.createNewPage(pageBean);
 	final VirtualHost virtualHost = pageBean.getVirtualHost();
 	final Node node = pageBean.getParentNode();
 	return forwardToMuneConfiguration(request, virtualHost, node);
     }
 
     public final ActionForward deletePage(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final Context context = getContext(request);
 	final INode node = context.getSelectedNode();
 	context.pop(node);
 	((Node) node).deleteService();
 	return viewPage(mapping, form, request, response);
     }
 
     public final ActionForward prepareEditPage(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final Context context = getContext(request);
 	final Node node = getDomainObject(request, "nodeOid");
 	request.setAttribute("selectedNode", node);
 	return context.forward("/contents/editPage.jsp");
     }
 
     public final ActionForward prepareAddSection(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final Context context = getContext(request);
 	final PageNode pageNode = (PageNode) context.getSelectedNode();
 	final Page page = pageNode.getPage();
 	final SectionBean sectionBean = new SectionBean(page);
 	request.setAttribute("sectionBean", sectionBean);
 	return context.forward("/contents/newSection.jsp");
     }
 
     public final ActionForward addSection(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final Context context = getContext(request);
 	final SectionBean sectionBean = getRenderedObject();
 	Section.createNewSection(sectionBean);
 	return context.forward("/contents/page.jsp");
     }
 
     public final ActionForward deleteSection(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final Section section = getDomainObject(request, "sectionOid");
 	section.delete();
 	return viewPage(mapping, form, request, response);
     }
 
     public final ActionForward prepareEditSection(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final Context context = getContext(request);
 	final Section section = getDomainObject(request, "sectionOid");
 	request.setAttribute("section", section);
 	return context.forward("/contents/editSection.jsp");
     }
 
     public final ActionForward saveSectionOrders(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final PageNode pageNode = getDomainObject(request, "nodeOid");
 	final Page page = pageNode.getPage();
 
 	final String[] sectionOrders = request.getParameter("articleOrders").split(";");
 	final String[] originalSectionIds = request.getParameter("originalArticleIds").split(";");
 
 	if (sectionOrders.length == originalSectionIds.length) {
 	    final ArrayList<Section> originalSections = new ArrayList<Section>(sectionOrders.length);
 	    final ArrayList<Section> sections = new ArrayList<Section>(sectionOrders.length);
 	    int i = 0;
 	    for (final Section section : page.getOrderedSections() ) {
 		if (Long.toString(section.getOID()).equals(originalSectionIds[i])) {
 		    originalSections.add(section);
 		} else {
 		    return viewPage(mapping, form, request, response);
 		}
 		i++;
 	    }
 	    for (final String sectionOrder : sectionOrders) {
 		final int so = Integer.parseInt(sectionOrder.substring(7));
 		sections.add(originalSections.get(so));
 	    }
 	    page.reorderSections(sections);
 	}
 
 	return viewPage(mapping, form, request, response);
     }
 
     public final ActionForward reorderSections(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	request.setAttribute("reorderSections", Boolean.TRUE);
 	return viewPage(mapping, form, request, response);
     }
 
     public final ActionForward savePageOrders(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final Context context = getContext(request);
 	final Collection<INode> menuNodes = context.getMenuElements();
 
 	final String[] nodeOrders = request.getParameter("articleOrders").split(";");
 	final String[] originalNodeIds = request.getParameter("originalArticleIds").split(";");
 
 	if (nodeOrders.length == originalNodeIds.length) {
 	    final ArrayList<INode> originalNodes = new ArrayList<INode>(nodeOrders.length);
 	    final ArrayList<Node> nodes = new ArrayList<Node>(nodeOrders.length);
 	    int i = 0;
 	    for (final INode node : menuNodes) {
 		if (node.asString().equals(originalNodeIds[i])) {
 		    originalNodes.add(node);
 		} else {
 		    return viewPage(mapping, form, request, response);
 		}
 		i++;
 	    }
 	    for (final String nodeOrder : nodeOrders) {
 		final int no = Integer.parseInt(nodeOrder.substring(11));
 		nodes.add((Node) originalNodes.get(no));
 	    }
 	    final Node parentNode = (Node) context.getParentNode();
 	    if (parentNode == null) {
 		Node.reorderTopLevelNodes(nodes);
 	    } else {
 		parentNode.reorderNodes(nodes);
 	    }
 	}
 
 	final VirtualHost virtualHost = getDomainObject(request, "virtualHostToManageId");
 	final Node node = getDomainObject(request, "parentOfNodesToManageId");
 	return forwardToMuneConfiguration(request, virtualHost, node);
     }
 
     public final ActionForward reorderPages(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	request.setAttribute("reorderPages", Boolean.TRUE);
 	return viewPage(mapping, form, request, response);
     }
 
 }
