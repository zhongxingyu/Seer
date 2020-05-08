 /**
  * 
  */
 package module.protocols.presentationTier.actions;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import module.fileManagement.domain.ContextPath;
 import module.fileManagement.domain.FileNode;
 import module.organization.domain.Person;
 import module.organization.domain.Unit;
 import module.protocols.domain.Protocol;
 import module.protocols.domain.ProtocolAuthorizationGroup;
 import module.protocols.domain.ProtocolManager;
 import module.protocols.domain.ProtocolResponsible;
 import module.protocols.domain.util.ProtocolResponsibleType;
 import module.protocols.dto.AuthorizationGroupBean;
 import module.protocols.dto.ProtocolCreationBean;
 import module.protocols.dto.ProtocolCreationBean.ProtocolResponsibleBean;
 import module.protocols.dto.ProtocolFileBean;
 import module.protocols.dto.ProtocolFileUploadBean;
 import module.protocols.dto.ProtocolHistoryBean;
 import module.protocols.dto.ProtocolSearchBean;
 import module.protocols.dto.ProtocolSystemConfigurationBean;
 import myorg.applicationTier.Authenticate;
 import myorg.domain.User;
 import myorg.domain.VirtualHost;
 import myorg.domain.contents.ActionNode;
 import myorg.domain.contents.Node;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.groups.AnyoneGroup;
 import myorg.domain.groups.PersistentGroup;
 import myorg.presentationTier.actions.ContextBaseAction;
 import myorg.util.InputStreamUtil;
 import myorg.util.VariantBean;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.joda.time.LocalDate;
 
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.servlets.functionalities.CreateNodeAction;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 
 /**
  * @author Joao Carvalho (joao.pedro.carvalho@ist.utl.pt)
  * 
  */
 @Mapping(path = "/protocols")
 public class ProtocolsDispatchAction extends ContextBaseAction {
 
     @CreateNodeAction(bundle = "PROTOCOLS_RESOURCES", key = "add.node.manage.protocols", groupKey = "label.module.protocols")
     public final ActionForward createProtocolsNode(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 
 	final VirtualHost virtualHost = getDomainObject(request, "virtualHostToManageId");
 
 	final Node parentOfNodes = getDomainObject(request, "parentOfNodesToManageId");
 
 	final ActionNode topActionNode = ActionNode.createActionNode(virtualHost, parentOfNodes, "/protocols", "firstPage",
 		"resources.ProtocolsResources", "label.module.protocols", AnyoneGroup.getInstance());
 
 	ActionNode.createActionNode(virtualHost, topActionNode, "/protocols", "showProtocols", "resources.ProtocolsResources",
 		"label.protocols.show", AnyoneGroup.getInstance());
 
 	ActionNode.createActionNode(virtualHost, topActionNode, "/protocols", "searchProtocols", "resources.ProtocolsResources",
 		"label.protocols.search", AnyoneGroup.getInstance());
 
 	ActionNode.createActionNode(virtualHost, topActionNode, "/protocols", "prepareCreateProtocolData",
 		"resources.ProtocolsResources", "label.protocols.create", ProtocolManager.getInstance().getCreatorsGroup());
 
 	ActionNode.createActionNode(virtualHost, topActionNode, "/protocols", "showAlerts", "resources.ProtocolsResources",
 		"label.protocols.alerts", ProtocolManager.getInstance().getAdministrativeGroup());
 
 	// System configuration. Should only be accessed by the administrative
 	// group
 	ActionNode.createActionNode(virtualHost, topActionNode, "/protocols", "protocolSystemConfiguration",
 		"resources.ProtocolsResources", "label.protocolSystem.configure", ProtocolManager.getInstance()
 			.getAdministrativeGroup());
 
 	ActionNode.createActionNode(virtualHost, topActionNode, "/protocols", "authorizationGroupsConfiguration",
 		"resources.ProtocolsResources", "label.protocolSystem.configureAuthorizationGroups", ProtocolManager
 			.getInstance().getAdministrativeGroup());
 
 	return forwardToMuneConfiguration(request, virtualHost, topActionNode);
     }
 
     public ActionForward firstPage(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	return forward(request, "/protocols/firstPage.jsp");
     }
 
     public ActionForward showProtocols(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	final User currentUser = Authenticate.getCurrentUser();
 
 	Collection<Protocol> protocols = Collections2.filter(ProtocolManager.getInstance().getProtocols(),
 		new Predicate<Protocol>() {
 
 		    @Override
 		    public boolean apply(Protocol protocol) {
 			return protocol.canBeReadByUser(currentUser) && protocol.isActive();
 		    }
 		});
 
 	request.setAttribute("protocols", protocols);
 
 	return forward(request, "/protocols/showProtocols.jsp");
     }
 
     public ActionForward showAlerts(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	setOrganizationalModels(request);
 
 	List<Protocol> allProtocols = ProtocolManager.getInstance().getProtocols();
 
 	Collection<Protocol> almostExpiredProtocols = Collections2.filter(allProtocols, new Predicate<Protocol>() {
 
 	    @Override
 	    public boolean apply(Protocol protocol) {
 		if (!protocol.isActive())
 		    return false;
 		LocalDate endDate = protocol.getLastProtocolHistory().getEndDate();
		return endDate == null ? false : endDate.isBefore(new LocalDate().plusMonths(2).withDayOfMonth(1).minusDays(1));
 	    }
 	});
 
 	Collection<Protocol> nullEndDateProtocols = Collections2.filter(allProtocols, new Predicate<Protocol>() {
 
 	    @Override
 	    public boolean apply(Protocol protocol) {
 		return protocol.isActive() && protocol.getCurrentProtocolHistory().getEndDate() == null;
 	    }
 	});
 
 	request.setAttribute("almostExpiredProtocols", almostExpiredProtocols);
 	request.setAttribute("nullEndDateProtocols", nullEndDateProtocols);
 
 	return forward(request, "/protocols/showAlerts.jsp");
     }
 
     public ActionForward protocolSystemConfiguration(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	checkAdministrative();
 
 	ProtocolSystemConfigurationBean bean = getRenderedObject();
 
 	if (bean == null)
 	    bean = new ProtocolSystemConfigurationBean();
 	else
 	    ProtocolManager.getInstance().updateFromBean(bean);
 
 	request.setAttribute("configurationBean", bean);
 
 	return forward(request, "/protocols/protocolSystemConfiguration.jsp");
     }
 
     public ActionForward authorizationGroupsConfiguration(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	checkAdministrative();
 
 	Collection<ProtocolAuthorizationGroup> groups = ProtocolManager.getInstance().getProtocolAuthorizationGroups();
 
 	request.setAttribute("authorizationGroups", groups);
 
 	VariantBean newGroupBean = new VariantBean();
 
 	request.setAttribute("newGroupBean", newGroupBean);
 
 	return forward(request, "/protocols/authorizationGroupsConfiguration.jsp");
 
     }
 
     public ActionForward createNewAuthorizationGroup(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	VariantBean bean = getRenderedObject();
 
 	PersistentGroup group = bean.getDomainObject();
 
 	if (group != null)
 	    if (!ProtocolAuthorizationGroup.createGroupWithWriter(group))
 		setMessage(request, "errorMessage", new ActionMessage("label.protocolSystem.group.alredy.exists"));
 
 	bean.setDomainObject(null);
 	RenderUtils.invalidateViewState();
 
 	return authorizationGroupsConfiguration(mapping, form, request, response);
     }
 
     public ActionForward removeAuthorizationGroup(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	checkAdministrative();
 
 	ProtocolAuthorizationGroup group = ProtocolAuthorizationGroup.fromExternalId(request.getParameter("OID"));
 
 	group.delete();
 
 	return authorizationGroupsConfiguration(mapping, form, request, response);
     }
 
     public ActionForward configureAuthorizationGroup(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	checkAdministrative();
 
 	AuthorizationGroupBean bean = getRenderedObject();
 
 	if (bean == null) {
 	    ProtocolAuthorizationGroup group = ProtocolAuthorizationGroup.fromExternalId(request.getParameter("OID"));
 	    bean = new AuthorizationGroupBean(group);
 	    request.setAttribute("bean", bean);
 	    return forward(request, "/protocols/configureAuthorizationGroup.jsp");
 	} else {
 	    bean.getGroup().updateReaders(bean.getAuthorizedGroups());
 	    return authorizationGroupsConfiguration(mapping, form, request, response);
 	}
 
     }
 
     public ActionForward viewProtocolDetails(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	String OID = request.getParameter("OID");
 
 	return viewProtocolDetailsFromOID(request, OID);
     }
 
     private ActionForward viewProtocolDetailsFromOID(final HttpServletRequest request, String OID) {
 
 	User currentUser = Authenticate.getCurrentUser();
 
 	Protocol protocol = getDomainObject(OID);
 
 	if (!protocol.canBeReadByUser(currentUser))
 	    return showProtocols(null, null, request, null);
 
 	request.setAttribute("protocol", protocol);
 
 	List<ProtocolResponsible> responsibles = protocol.getProtocolResponsible();
 
 	Collection<ProtocolResponsible> internalResponsibles = Collections2.filter(responsibles,
 		new Predicate<ProtocolResponsible>() {
 
 		    @Override
 		    public boolean apply(ProtocolResponsible responsible) {
 			return responsible.getType() == ProtocolResponsibleType.INTERNAL;
 		    }
 
 		});
 
 	Collection<ProtocolResponsible> externalResponsibles = Collections2.filter(responsibles,
 		new Predicate<ProtocolResponsible>() {
 
 		    @Override
 		    public boolean apply(ProtocolResponsible responsible) {
 			return responsible.getType() == ProtocolResponsibleType.EXTERNAL;
 		    }
 
 		});
 
 	request.setAttribute("internalResponsibles", internalResponsibles);
 	request.setAttribute("externalResponsibles", externalResponsibles);
 
 	if (protocol.canFilesBeReadByUser(currentUser))
 	    request.setAttribute("protocolFiles",
 		    Collections2.transform(protocol.getFiles(), new Function<FileNode, ProtocolFileBean>() {
 
 			@Override
 			public ProtocolFileBean apply(FileNode file) {
 			    return new ProtocolFileBean(file, request);
 			}
 		    }));
 
 	request.setAttribute("canBeWritten", protocol.canBeWrittenByUser(currentUser));
 
 	return forward(request, "/protocols/viewProtocolDetails.jsp");
     }
 
     /*
      * Searching
      */
 
     public ActionForward searchProtocols(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	ProtocolSearchBean bean = getRenderedObject();
 
 	if (bean != null && request.getParameter("search") != null) {
 
 	    Collection<Protocol> filteredProtocols = Collections2.filter(ProtocolManager.getInstance().getProtocols(), bean);
 
 	    request.setAttribute("searchResults", filteredProtocols);
 
 	} else if (bean == null) {
 	    bean = new ProtocolSearchBean();
 	}
 
 	request.setAttribute("protocolSearch", bean);
 
 	return forward(request, "/protocols/searchProtocols.jsp");
     }
 
     /*
      * Protocol History edition
      */
 
     public ActionForward prepareEditProtocolHistory(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	Protocol protocol = Protocol.fromExternalId(request.getParameter("OID"));
 
 	request.setAttribute("protocolHistory", new ProtocolHistoryBean(protocol.getCurrentProtocolHistory()));
 
 	return forward(request, "/protocols/editProtocolHistory.jsp");
     }
 
     public ActionForward editProtocolHistory(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	ProtocolHistoryBean bean = getRenderedObject();
 
 	if (!validateDates(bean.getBeginDate(), bean.getEndDate(), request)) {
 	    return forward(request, "/protocols/editProtocolHistory.jsp");
 	}
 
 	bean.getProtocolHistory().editProtocolHistory(bean.getBeginDate(), bean.getEndDate());
 
 	setMessage(request, "success", new ActionMessage("label.protocolHistory.editSuccessful"));
 
 	return showAlerts(mapping, form, request, response);
     }
 
     /*
      * Renewal Process
      */
 
     public ActionForward prepareRenewProtocol(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	String protocolOID = request.getParameter("protocolOID");
 
 	Protocol protocol = Protocol.fromExternalId(protocolOID);
 
 	ProtocolHistoryBean bean = new ProtocolHistoryBean(protocol);
 
 	request.setAttribute("protocol", bean);
 
 	return forward(request, "/protocols/renewProtocol.jsp");
     }
 
     public ActionForward renewProtocol(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	ProtocolHistoryBean bean = getRenderedObject();
 
 	bean.getProtocol().renewFor(bean.getDuration(), bean.getRenewTime());
 
 	setMessage(request, "success", new ActionMessage("label.procotols.renew.success"));
 
 	return showAlerts(mapping, form, request, response);
     }
 
     /*
      * Protocol Edition
      */
 
     public ActionForward uploadProtocolFile(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	ProtocolFileUploadBean bean = getRenderedObject();
 
 	if (bean == null) {
 
 	    Protocol protocol = getDomainObject(request, "OID");
 
 	    if (!protocol.canBeWrittenByUser(Authenticate.getCurrentUser()))
 		throw new DomainException("error.unauthorized");
 
 	    bean = new ProtocolFileUploadBean(protocol);
 
 	    request.setAttribute("file", bean);
 
 	    return forward(request, "/protocols/uploadProtocolFile.jsp");
 
 	} else {
 	    if (bean.getInputStream() != null) {
 		bean.getProtocol().uploadFile(bean.getFilename(), InputStreamUtil.consumeInputStream(bean.getInputStream()));
 		setMessage(request, "success", new ActionMessage("label.protocols.fileUpload.success"));
 	    }
 	    return viewProtocolDetailsFromOID(request, bean.getProtocol().getExternalId());
 	}
 
     }
 
     public ActionForward removeProtocolFile(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	Protocol protocol = getDomainObject(request, "protocol");
 
 	if (!protocol.canBeWrittenByUser(Authenticate.getCurrentUser()))
 	    throw new DomainException("error.unauthorized");
 
 	FileNode file = getDomainObject(request, "file");
 
 	file.trash(new ContextPath(file.getParent()));
 
 	setMessage(request, "success", new ActionMessage("label.protocols.fileRemoval.success"));
 
 	return viewProtocolDetailsFromOID(request, protocol.getExternalId());
 
     }
 
     public ActionForward prepareEditProtocolData(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	Protocol protocol = getDomainObject(request, "OID");
 
 	if (!protocol.canBeWrittenByUser(Authenticate.getCurrentUser()))
 	    throw new DomainException("error.unauthorized");
 
 	request.setAttribute("protocolBean", new ProtocolCreationBean(protocol));
 
 	return forward(request, "/protocols/prepareEditProtocolData.jsp");
     }
 
     public ActionForward prepareEditInternalResponsibles(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) throws Exception {
 
 	ProtocolCreationBean protocolBean = getRenderedObject();
 	RenderUtils.invalidateViewState();
 
 	request.setAttribute("protocolBean", protocolBean);
 
 	if (protocolBean.isProtocolNumberValid()) {
 	    if (validateDates(protocolBean.getBeginDate(), protocolBean.getEndDate(), request)) {
 		return forward(request, "/protocols/prepareEditInternalResponsibles.jsp");
 	    } else {
 		return forward(request, "/protocols/prepareEditProtocolData.jsp");
 	    }
 	} else {
 	    validateDates(protocolBean.getBeginDate(), protocolBean.getEndDate(), request);
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocol.number.alreadyExists"));
 	    return forward(request, "/protocols/prepareEditProtocolData.jsp");
 	}
     }
 
     public ActionForward prepareEditExternalResponsibles(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) throws Exception {
 
 	ProtocolCreationBean protocolBean = getRenderedObject();
 	RenderUtils.invalidateViewState();
 
 	request.setAttribute("protocolBean", protocolBean);
 
 	if (request.getParameter("back") != null) {
 	    return forward(request, "/protocols/prepareEditProtocolData.jsp");
 	}
 
 	if (!protocolBean.internalResponsiblesCorrect()) {
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocols.invalidInternalResponsibles"));
 	    return forward(request, "/protocols/prepareEditInternalResponsibles.jsp");
 	}
 
 	return forward(request, "/protocols/prepareEditExternalResponsibles.jsp");
     }
 
     public ActionForward prepareEditProtocolPermissions(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) throws Exception {
 
 	ProtocolCreationBean protocolBean = getRenderedObject();
 	RenderUtils.invalidateViewState();
 
 	request.setAttribute("protocolBean", protocolBean);
 
 	if (request.getParameter("back") != null) {
 	    return forward(request, "/protocols/prepareEditInternalResponsibles.jsp");
 	}
 
 	if (!protocolBean.externalResponsiblesCorrect()) {
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocols.invalidExternalResponsibles"));
 	    return forward(request, "/protocols/prepareEditExternalResponsibles.jsp");
 	}
 
 	return forward(request, "/protocols/prepareEditProtocolPermissions.jsp");
     }
 
     public ActionForward editProtocol(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) throws Exception {
 
 	ProtocolCreationBean protocolBean = getRenderedObject();
 	RenderUtils.invalidateViewState();
 
 	request.setAttribute("protocolBean", protocolBean);
 
 	if (request.getParameter("back") != null) {
 	    return forward(request, "/protocols/prepareEditExternalResponsibles.jsp");
 	}
 
 	if (!protocolBean.permissionsCorrectlyDefined()) {
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocols.invalidProtocolPermissions"));
 	    return forward(request, "/protocols/prepareEditProtocolPermissions.jsp");
 	}
 
 	try {
 	    protocolBean.getProtocol().updateFromBean(protocolBean);
 
 	    setMessage(request, "success", new ActionMessage("label.protocols.successEdit"));
 
 	    return viewProtocolDetailsFromOID(request, protocolBean.getProtocol().getExternalId());
 	} catch (DomainException e) {
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocol.number.alreadyExists"));
 	    return forward(request, "/protocols/prepareEditProtocolPermissions.jsp");
 	}
     }
 
     public ActionForward updateEditionBean(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	ProtocolCreationBean protocolBean = getRenderedObject();
 	RenderUtils.invalidateViewState();
 	request.setAttribute("protocolBean", protocolBean);
 
 	boolean internal = beanUpdateLogic(request, protocolBean);
 
 	return forward(request, internal ? "/protocols/prepareEditInternalResponsibles.jsp"
 		: "/protocols/prepareEditExternalResponsibles.jsp");
     }
 
     /*
      * Protocol Creation
      */
 
     public ActionForward prepareCreateProtocolData(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
 	    HttpServletResponse response) throws Exception {
 
 	if (!ProtocolManager.getInstance().getCreatorsGroup().isMember(Authenticate.getCurrentUser()))
 	    throw new DomainException("error.unauthorized");
 
 	request.setAttribute("protocolBean", new ProtocolCreationBean());
 
 	return forward(request, "/protocols/prepareCreateProtocolData.jsp");
     }
 
     // Protocol data inserted, proceed to create internal responsibles
     public ActionForward prepareCreateInternalResponsibles(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) throws Exception {
 
 	ProtocolCreationBean protocolBean = getRenderedObject();
 	RenderUtils.invalidateViewState();
 
 	request.setAttribute("protocolBean", protocolBean);
 
 	if (isCancelled(request)) {
 	    return showProtocols(mapping, form, request, response);
 	}
 
 	if (protocolBean.isProtocolNumberValid()) {
 	    if (validateDates(protocolBean.getBeginDate(), protocolBean.getEndDate(), request)) {
 		return forward(request, "/protocols/prepareCreateInternalResponsibles.jsp");
 	    } else {
 		return forward(request, "/protocols/prepareCreateProtocolData.jsp");
 	    }
 	} else {
 	    validateDates(protocolBean.getBeginDate(), protocolBean.getEndDate(), request);
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocol.number.alreadyExists"));
 	    return forward(request, "/protocols/prepareCreateProtocolData.jsp");
 	}
     }
 
     // Internal data inserted, proceed to permission setting
     public ActionForward prepareCreateExternalResponsibles(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) throws Exception {
 
 	ProtocolCreationBean protocolBean = getRenderedObject();
 	RenderUtils.invalidateViewState();
 
 	request.setAttribute("protocolBean", protocolBean);
 
 	if (request.getParameter("back") != null) {
 	    return forward(request, "/protocols/prepareCreateProtocolData.jsp");
 	}
 
 	if (!protocolBean.internalResponsiblesCorrect()) {
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocols.invalidInternalResponsibles"));
 	    return forward(request, "/protocols/prepareCreateInternalResponsibles.jsp");
 	}
 
 	return forward(request, "/protocols/prepareCreateExternalResponsibles.jsp");
     }
 
     // External data inserted, proceed to permission setting
     public ActionForward prepareDefineProtocolPermissions(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) throws Exception {
 
 	ProtocolCreationBean protocolBean = getRenderedObject();
 	RenderUtils.invalidateViewState();
 
 	request.setAttribute("protocolBean", protocolBean);
 
 	if (request.getParameter("back") != null) {
 	    return forward(request, "/protocols/prepareCreateInternalResponsibles.jsp");
 	}
 
 	if (!protocolBean.externalResponsiblesCorrect()) {
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocols.invalidExternalResponsibles"));
 	    return forward(request, "/protocols/prepareCreateExternalResponsibles.jsp");
 	}
 
 	return forward(request, "/protocols/prepareDefineProtocolPermissions.jsp");
     }
 
     // All data inserted, create protocol
     public ActionForward createProtocol(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) throws Exception {
 
 	ProtocolCreationBean protocolBean = getRenderedObject();
 	RenderUtils.invalidateViewState();
 
 	request.setAttribute("protocolBean", protocolBean);
 
 	if (request.getParameter("back") != null) {
 	    return forward(request, "/protocols/prepareCreateExternalResponsibles.jsp");
 	}
 
 	if (!protocolBean.permissionsCorrectlyDefined()) {
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocols.invalidProtocolPermissions"));
 	    return forward(request, "/protocols/prepareDefineProtocolPermissions.jsp");
 	}
 
 	try {
 	    Protocol protocol = Protocol.createProtocol(protocolBean);
 
 	    setMessage(request, "success", new ActionMessage("label.protocols.successCreate"));
 
 	    return viewProtocolDetailsFromOID(request, protocol.getExternalId());
 	} catch (DomainException e) {
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocol.number.alreadyExists"));
 	    return forward(request, "/protocols/prepareDefineProtocolPermissions.jsp");
 	}
     }
 
     public ActionForward updateBean(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	ProtocolCreationBean protocolBean = getRenderedObject();
 	RenderUtils.invalidateViewState();
 	request.setAttribute("protocolBean", protocolBean);
 
 	boolean internal = beanUpdateLogic(request, protocolBean);
 
 	return forward(request, internal ? "/protocols/prepareCreateInternalResponsibles.jsp"
 		: "/protocols/prepareCreateExternalResponsibles.jsp");
     }
 
     /*
      * Helper methods
      */
 
     // Terrible hack
     private boolean beanUpdateLogic(final HttpServletRequest request, ProtocolCreationBean protocolBean) {
 
 	boolean internal = true;
 
 	Unit unit = null;
 
 	if (request.getParameter("unitOID") != null)
 	    unit = getDomainObject(request, "unitOID");
 
 	if (request.getParameter("insertInternalUnit") != null && protocolBean.getNewUnit() != null) {
 	    protocolBean.addInternalResponsible(new ProtocolResponsibleBean(protocolBean.getNewUnit()));
 	    protocolBean.setNewUnit(null);
 	} else if (request.getParameter("insertPersonInUnit") != null && protocolBean.getNewPerson() != null) {
 	    protocolBean.getBeanForUnit(unit).addResponsible(protocolBean.getNewPerson());
 	    protocolBean.setNewPerson(null);
 	} else if (request.getParameter("removePersonInUnit") != null) {
 	    Person person = getDomainObject(request, "personOID");
 	    protocolBean.getBeanForUnit(unit).removeResponsible(person);
 	    protocolBean.setNewPerson(null);
 	} else if (request.getParameter("removePersonInExternalUnit") != null) {
 	    Person person = getDomainObject(request, "personOID");
 	    protocolBean.getBeanForUnit(unit).removeResponsible(person);
 	    protocolBean.setNewPerson(null);
 	    internal = false;
 	} else if (request.getParameter("removeUnit") != null) {
 	    protocolBean.removeUnit(unit);
 	} else if (request.getParameter("removeExternalUnit") != null) {
 	    protocolBean.removeUnit(unit);
 	    internal = false;
 	} else if (request.getParameter("insertExternalUnit") != null) {
 	    protocolBean.addExternalResponsible(new ProtocolResponsibleBean(protocolBean.getNewUnit()));
 	    protocolBean.setNewUnit(null);
 	    internal = false;
 	} else if (request.getParameter("insertPersonInExternalUnit") != null && protocolBean.getNewPerson() != null) {
 	    protocolBean.getBeanForUnit(unit).addResponsible(protocolBean.getNewPerson());
 	    protocolBean.setNewPerson(null);
 	    internal = false;
 	} else if (request.getParameter("removePosition") != null) {
 	    protocolBean.getBeanForUnit(unit).removePosition(request.getParameter("position"));
 	} else if (request.getParameter("removePositionExternal") != null) {
 	    protocolBean.getBeanForUnit(unit).removePosition(request.getParameter("position"));
 	    internal = false;
 	} else if (request.getParameter("removePositions") != null) {
 	    protocolBean.getBeanForUnit(unit).removePositions();
 	} else if (request.getParameter("removePositionsExternal") != null) {
 	    protocolBean.getBeanForUnit(unit).removePositions();
 	    internal = false;
 	} else if (request.getParameter("insertPosition") != null && protocolBean.getNewPosition() != null
 		&& !protocolBean.getNewPosition().trim().isEmpty()) {
 	    protocolBean.getBeanForUnit(unit).addPosition(protocolBean.getNewPosition());
 	    protocolBean.setNewPosition(null);
 	} else if (request.getParameter("insertExternalPosition") != null && protocolBean.getNewPosition() != null
 		&& !protocolBean.getNewPosition().trim().isEmpty()) {
 	    protocolBean.getBeanForUnit(unit).addPosition(protocolBean.getNewPosition());
 	    protocolBean.setNewPosition(null);
 	    internal = false;
 	}
 	return internal;
     }
 
     private boolean validateDates(LocalDate beginDate, LocalDate endDate, HttpServletRequest request) {
 	if (beginDate != null && endDate != null && endDate.isBefore(beginDate)) {
 	    setMessage(request, "errorMessage", new ActionMessage("error.protocols.dates.notContinuous"));
 	    return false;
 	}
 	return true;
     }
 
     private void setMessage(HttpServletRequest request, String error, ActionMessage actionMessage) {
 	ActionMessages actionMessages = getMessages(request);
 	actionMessages.add(error, actionMessage);
 	saveMessages(request, actionMessages);
     }
 
     private void setOrganizationalModels(HttpServletRequest request) {
 	request.setAttribute("internalOrganizationalModel", ProtocolManager.getInstance().getInternalOrganizationalModel());
 	request.setAttribute("externalOrganizationalModel", ProtocolManager.getInstance().getExternalOrganizationalModel());
     }
 
     private void checkAdministrative() {
 	if (!ProtocolManager.getInstance().getAdministrativeGroup().isMember(Authenticate.getCurrentUser()))
 	    throw new DomainException("error.unauthorized");
     }
 
 }
