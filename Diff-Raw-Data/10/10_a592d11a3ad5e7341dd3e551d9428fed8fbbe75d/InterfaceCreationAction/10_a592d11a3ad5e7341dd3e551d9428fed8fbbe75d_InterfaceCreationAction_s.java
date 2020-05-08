 package pt.ist.expenditureTrackingSystem.presentationTier.actions;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import module.contents.domain.Page;
 import module.contents.domain.Page.PageBean;
 import myorg.domain.VirtualHost;
 import myorg.domain.contents.ActionNode;
import myorg.domain.contents.JumpToFirstAccessibleChildNode;
 import myorg.domain.contents.Node;
 import myorg.domain.groups.AnyoneGroup;
 import myorg.domain.groups.PersistentGroup;
 import myorg.domain.groups.UserGroup;
 import myorg.presentationTier.actions.ContextBaseAction;
 import myorg.util.BundleUtil;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.fenixWebFramework.servlets.functionalities.CreateNodeAction;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 @Mapping(path = "/expendituresInterfaceCreationAction")
 public class InterfaceCreationAction extends ContextBaseAction {
 
     @CreateNodeAction(bundle = "EXPENDITURE_RESOURCES", key = "add.node.expenditure-tracking.interface", groupKey = "label.module.expenditure-tracking")
     public final ActionForward createExpenditureNodes(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	final VirtualHost virtualHost = getDomainObject(request, "virtualHostToManageId");
 	final Node node = getDomainObject(request, "parentOfNodesToManageId");
 
	final Node homeNode = JumpToFirstAccessibleChildNode.createJumpToFirstAccessibleChildNode(virtualHost, node);
	createNodeForPage(virtualHost, homeNode, "resources.ExpenditureResources", "link.topBar.home", UserGroup.getInstance());
 //	ActionNode.createActionNode(virtualHost, homeNode, "/expendituresHome", "showActiveRequestsForProposal",
 //		"resources.ExpenditureResources", "link.sideBar.home.publicRequestsForProposal", Role.getRole(RoleType.MANAGER));
 	ActionNode.createActionNode(virtualHost, homeNode, "/expendituresHome", "showAcquisitionAnnouncements",
 		"resources.ExpenditureResources", "link.sideBar.home.publicAnnouncements", AnyoneGroup.getInstance());
 
 //	final Node requestsForProposalNode = createNodeForPage(virtualHost, node, "resources.ExpenditureResources",
 //		"link.topBar.requestForProposal", Role.getRole(RoleType.MANAGER));
 //	ActionNode.createActionNode(virtualHost, requestsForProposalNode, "/requestForProposalProcess",
 //		"prepareCreateRequestForProposalProcess", "resources.ExpenditureResources",
 //		"link.sideBar.requestForProposal.create", Role.getRole(RoleType.MANAGER));
 //	ActionNode.createActionNode(virtualHost, requestsForProposalNode, "/requestForProposalProcess",
 //		"searchRequestProposalProcess", "resources.ExpenditureResources", "link.sideBar.requestForProposal.search", Role
 //			.getRole(RoleType.MANAGER));
 //	ActionNode.createActionNode(virtualHost, requestsForProposalNode, "/requestForProposalProcess", "showPendingRequests",
 //		"resources.ExpenditureResources", "link.sideBar.requestForProposal.pendingProcesses", Role
 //			.getRole(RoleType.MANAGER));
 //
 //	final Node announcementsnNode = createNodeForPage(virtualHost, node, "resources.ExpenditureResources",
 //		"link.topBar.announcements", Role.getRole(RoleType.MANAGER));
 //	ActionNode.createActionNode(virtualHost, announcementsnNode, "/announcementProcess", "prepareCreateAnnouncement",
 //		"resources.ExpenditureResources", "link.sideBar.announcementProcess.createAnnouncement", Role
 //			.getRole(RoleType.MANAGER));
 //	ActionNode.createActionNode(virtualHost, announcementsnNode, "/announcementProcess", "searchAnnouncementProcess",
 //		"resources.ExpenditureResources", "link.sideBar.announcementProcess.searchProcesses", Role
 //			.getRole(RoleType.MANAGER));
 //	ActionNode.createActionNode(virtualHost, announcementsnNode, "/announcementProcess", "showMyProcesses",
 //		"resources.ExpenditureResources", "link.sideBar.announcementProcess.myProcesses", Role.getRole(RoleType.MANAGER));
 //	ActionNode.createActionNode(virtualHost, announcementsnNode, "/announcementProcess", "showPendingProcesses",
 //		"resources.ExpenditureResources", "link.sideBar.announcementProcess.pendingProcesses", Role
 //			.getRole(RoleType.MANAGER));
 
 	final Node aquisitionProcessNode = ActionNode.createActionNode(
 		virtualHost, node, "/search", "search", "resources.ExpenditureResources",
 		"link.topBar.acquisitionProcesses", UserGroup.getInstance());
 	ActionNode.createActionNode(virtualHost, aquisitionProcessNode, "/wizard", "newAcquisitionWizard",
 		"resources.ExpenditureResources", "link.sideBar.process.create", UserGroup.getInstance());
 	ActionNode.createActionNode(virtualHost, aquisitionProcessNode, "/search", "search", "resources.ExpenditureResources",
 		"link.sideBar.acquisitionProcess.search", UserGroup.getInstance());
 	ActionNode.createActionNode(virtualHost, aquisitionProcessNode, "/wizard", "afterTheFactOperationsWizard",
 		"resources.ExpenditureResources", "link.register",
 		pt.ist.expenditureTrackingSystem.domain.Role.getRole(pt.ist.expenditureTrackingSystem.domain.RoleType.ACQUISITION_CENTRAL)
 				.getSystemRole());
 	ActionNode.createActionNode(virtualHost, aquisitionProcessNode, "/acquisitionProcess", "checkFundAllocations",
 		"resources.ExpenditureResources", "link.fundAllocations", 
 		pt.ist.expenditureTrackingSystem.domain.Role.getRole(pt.ist.expenditureTrackingSystem.domain.RoleType.ACCOUNTING_MANAGER)
 				.getSystemRole());
 
 	final Node organizationNode = ActionNode.createActionNode(
 		virtualHost, node, "/expenditureTrackingOrganization", "viewLoggedPerson",
 		"resources.ExpenditureResources", "link.topBar.organization", UserGroup.getInstance());
 	ActionNode.createActionNode(virtualHost, organizationNode, "/expenditureTrackingOrganization", "viewOrganization",
 		"resources.ExpenditureResources", "link.viewOrganization", UserGroup.getInstance());
 	ActionNode.createActionNode(virtualHost, organizationNode, "/expenditureTrackingOrganization", "searchUsers",
 		"resources.ExpenditureResources", "search.link.users", UserGroup.getInstance());
 	ActionNode.createActionNode(virtualHost, organizationNode, "/expenditureTrackingOrganization", "manageSuppliers",
 		"resources.ExpenditureOrganizationResources", "supplier.link.manage", UserGroup.getInstance());
 
 	final Node statisticsNode = ActionNode.createActionNode(
 		virtualHost, node, "/statistics", "showSimplifiedProcessStatistics",
 		"resources.ExpenditureResources", "link.topBar.statistics",
 		pt.ist.expenditureTrackingSystem.domain.Role.getRole(pt.ist.expenditureTrackingSystem.domain.RoleType.STATISTICS_VIEWER)
 				.getSystemRole());
 	ActionNode.createActionNode(virtualHost, statisticsNode, "/statistics", "showSimplifiedProcessStatistics",
 		"resources.StatisticsResources", "label.statistics.process.simplified",
 		pt.ist.expenditureTrackingSystem.domain.Role.getRole(pt.ist.expenditureTrackingSystem.domain.RoleType.STATISTICS_VIEWER)
 				.getSystemRole());
 	ActionNode.createActionNode(virtualHost, statisticsNode, "/statistics", "showRefundProcessStatistics",
 		"resources.StatisticsResources", "label.statistics.process.refund",
 		pt.ist.expenditureTrackingSystem.domain.Role.getRole(pt.ist.expenditureTrackingSystem.domain.RoleType.STATISTICS_VIEWER)
 				.getSystemRole());
 
 	return forwardToMuneConfiguration(request, virtualHost, node);
     }
 
     protected Node createNodeForPage(final VirtualHost virtualHost, final Node node, final String bundle, final String key,
 	    PersistentGroup userGroup) {
 	final PageBean pageBean = new PageBean(virtualHost, node, userGroup);
 	final MultiLanguageString statisticsLabel = BundleUtil.getMultilanguageString(bundle, key);
 	pageBean.setLink(statisticsLabel);
 	pageBean.setTitle(statisticsLabel);
 	return (Node) Page.createNewPage(pageBean);
     }
 
 }
