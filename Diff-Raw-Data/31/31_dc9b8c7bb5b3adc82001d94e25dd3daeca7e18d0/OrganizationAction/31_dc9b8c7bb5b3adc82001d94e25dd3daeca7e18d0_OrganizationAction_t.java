 package pt.ist.expenditureTrackingSystem.presentationTier.actions.organization;
 
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.Role;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.DelegatedAuthorization;
 import pt.ist.expenditureTrackingSystem.domain.dto.AuthorizationBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreatePersonBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateSupplierBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.SupplierBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.UnitBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.SearchUsers;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.presentationTier.Context;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.BaseAction;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.struts.annotations.Forward;
 import pt.ist.fenixWebFramework.struts.annotations.Forwards;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/organization")
 @Forwards( { @Forward(name = "view.organization", path = "/organization/viewOrganization.jsp"),
 	@Forward(name = "create.unit", path = "/organization/createUnit.jsp"),
 	@Forward(name = "edit.unit", path = "/organization/editUnit.jsp"),
 	@Forward(name = "search.users", path = "/organization/searchUsers.jsp"),
 	@Forward(name = "manage.suppliers", path = "/organization/manageSuppliers.jsp"),
 	@Forward(name = "view.person", path = "/organization/viewPerson.jsp"),
 	@Forward(name = "view.authorization", path = "/organization/viewAuthorization.jsp"),
 	@Forward(name = "edit.authorization", path = "/organization/editAuthorization.jsp"),
 	@Forward(name = "delegate.authorization", path = "/organization/delegateAuthorization.jsp"),
 	@Forward(name = "create.person", path = "/organization/createPerson.jsp"),
 	@Forward(name = "edit.person", path = "/organization/editPerson.jsp"),
 	@Forward(name = "view.supplier", path = "/organization/viewSupplier.jsp"),
 	@Forward(name = "create.supplier", path = "/organization/createSupplier.jsp"),
 	@Forward(name = "edit.supplier", path = "/organization/editSupplier.jsp"),
 	@Forward(name = "change.authorization.unit", path = "/organization/changeAuthorizationUnit.jsp") })
 public class OrganizationAction extends BaseAction {
 
     private static final Context CONTEXT = new Context("organization");
 
     @Override
     protected Context getContextModule() {
 	return CONTEXT;
     }
 
     public final ActionForward viewOrganization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	UnitBean unitBean = getRenderedObject();
 	if (unitBean == null) {
 	    unitBean = new UnitBean();
 	}
 	final Unit unit = unitBean.getUnit() == null ? (Unit) getDomainObject(request, "unitOid") : unitBean.getUnit();
 	request.setAttribute("unitBean", unitBean);
 	return viewOrganization(mapping, request, unit);
     }
 
     public final ActionForward viewOrganization(final ActionMapping mapping, final HttpServletRequest request, final Unit unit) {
 	request.setAttribute("unit", unit);
 	final Set<Unit> units = unit == null ? ExpenditureTrackingSystem.getInstance().getTopLevelUnitsSet() : unit
 		.getSubUnitsSet();
 	request.setAttribute("units", units);
 	return mapping.findForward("view.organization");
     }
 
     public final ActionForward prepareCreateUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitOid");
 	request.setAttribute("bean", new CreateUnitBean(unit));
 	return mapping.findForward("create.unit");
     }
 
     public final ActionForward createNewUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	CreateUnitBean createUnitBean = getRenderedObject();
 	final Unit newUnit = Unit.createNewUnit(createUnitBean);
 	return viewOrganization(mapping, request, newUnit);
     }
 
     private ActionForward editUnit(ActionMapping mapping, HttpServletRequest request, Unit unit) {
 	request.setAttribute("unit", unit);
 	return mapping.findForward("edit.unit");
     }
 
     public final ActionForward editUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitOid");
 	return editUnit(mapping, request, unit);
     }
 
     public final ActionForward deleteUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitOid");
 	final Unit parentUnit = unit.getParentUnit();
 	unit.delete();
 	return viewOrganization(mapping, request, parentUnit);
     }
 
     public final ActionForward searchUsers(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	SearchUsers searchUsers = getRenderedObject("searchUsers");
 	if (searchUsers == null) {
 	    searchUsers = new SearchUsers();
 	}
 	request.setAttribute("searchUsers", searchUsers);
 	return mapping.findForward("search.users");
     }
 
     public final ActionForward prepareCreatePerson(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final CreatePersonBean createPersonBean = new CreatePersonBean();
 	request.setAttribute("bean", createPersonBean);
 	return mapping.findForward("create.person");
     }
 
     public final ActionForward createPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final CreatePersonBean createPersonBean = getRenderedObject();
 	Person person = Person.createPerson(createPersonBean);
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward editPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	return editPerson(mapping, request, person);
     }
 
     public final ActionForward editPerson(final ActionMapping mapping, final HttpServletRequest request, final Person person) {
 	request.setAttribute("person", person);
 	return mapping.findForward("edit.person");
     }
 
     public final ActionForward deletePerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	person.delete();
 	return searchUsers(mapping, form, request, response);
     }
 
     public final ActionForward addRole(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final Role role = getRole(request);
 	person.addRoles(role);
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward removeRole(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final Role role = getRole(request);
 	person.removeRoles(role);
 	return viewPerson(mapping, request, person);
     }
 
     private Role getRole(HttpServletRequest request) {
 	String role = request.getParameter("role");
 	return role == null ? null : Role.getRole(RoleType.valueOf(role));
     }
 
     public final ActionForward viewPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward viewPerson(final ActionMapping mapping, final HttpServletRequest request, final Person person) {
 	request.setAttribute("person", person);
 	request.setAttribute("availableRoles", RoleType.values());
 	return mapping.findForward("view.person");
     }
 
     public final ActionForward editAuthorization(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Authorization authorization = getDomainObject(request, "authorizationOid");
 	request.setAttribute("authorization", authorization);
 	return mapping.findForward("edit.authorization");
     }
 
     public final ActionForward viewAuthorization(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Authorization authorization = getDomainObject(request, "authorizationOid");
 	request.setAttribute("authorization", authorization);
 	return mapping.findForward("view.authorization");
     }
 
     public final ActionForward attributeAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	request.setAttribute("person", person);
 	RenderUtils.invalidateViewState();
 	final UnitBean unitBean = new UnitBean();
 	request.setAttribute("unitBean", unitBean);
 	return expandAuthorizationUnit(mapping, request, person, null);
     }
 
     public final ActionForward expandAuthorizationUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final UnitBean unitBean = getRenderedObject();
 	final Person person = getDomainObject(request, "personOid");
 	if (unitBean == null || unitBean.getUnit() == null) {
 	    final Unit unit = getDomainObject(request, "unitOid");
 	    return expandAuthorizationUnit(mapping, request, person, unit);
 	} else {
 	    return expandAuthorizationUnit(mapping, request, person, unitBean.getUnit());
 	}
     }
 
     public final ActionForward expandAuthorizationUnit(final ActionMapping mapping, final HttpServletRequest request,
 	    final Person person, final Unit unit) {
 	request.setAttribute("person", person);
 	request.setAttribute("unit", unit);
 	final Set<Unit> units = unit == null ? ExpenditureTrackingSystem.getInstance().getTopLevelUnitsSet() : unit
 		.getSubUnitsSet();
 	request.setAttribute("units", units);
 	return mapping.findForward("change.authorization.unit");
     }
 
     public final ActionForward changeAuthorizationUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final Unit unit = getDomainObject(request, "unitOid");
 	person.createAuthorization(unit);
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward deleteAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Authorization authorization = getDomainObject(request, "authorizationOid");
 	final Person person = authorization.getPerson();
	authorization.delete();
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward manageSuppliers(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	SupplierBean supplierBean = getRenderedObject();
 	if (supplierBean == null) {
 	    supplierBean = new SupplierBean();
 	}
 
 	request.setAttribute("supplierBean", supplierBean);
 	return mapping.findForward("manage.suppliers");
     }
 
     public final ActionForward manageSuppliers(final ActionMapping mapping, final HttpServletRequest request, 
 	    final SupplierBean supplierBean) {
 	request.setAttribute("supplierBean", supplierBean);
 	return mapping.findForward("manage.suppliers");
     }
 
     public final ActionForward viewSupplier(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Supplier supplier = getDomainObject(request, "supplierOid");
 
 	return viewSupplier(mapping, form, request, response, supplier);
     }
 
     private final ActionForward viewSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response, final Supplier supplier) {
 	request.setAttribute("supplier", supplier);
 	return mapping.findForward("view.supplier");
     }
 
     public final ActionForward prepareCreateSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final CreateSupplierBean createSupplierBean = new CreateSupplierBean();
 
 	request.setAttribute("bean", createSupplierBean);
 	return mapping.findForward("create.supplier");
     }
 
     public final ActionForward createSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final CreateSupplierBean createSupplierBean = getRenderedObject();
 	final Supplier supplier = Supplier.createNewSupplier(createSupplierBean);
 	final SupplierBean supplierBean = new SupplierBean(supplier);
 	return manageSuppliers(mapping, request, supplierBean);
     }
 
     public final ActionForward prepareEditSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Supplier supplier = getDomainObject(request, "supplierOid");
 
 	request.setAttribute("supplier", supplier);
 	return mapping.findForward("edit.supplier");
     }
 
     public final ActionForward editSupplier(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Supplier supplier = getRenderedObject();
 	final SupplierBean supplierBean = new SupplierBean(supplier);
 	return manageSuppliers(mapping, request, supplierBean);
     }
 
     public final ActionForward deleteSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Supplier supplier = getDomainObject(request, "supplierOid");
 	supplier.delete();
 	return manageSuppliers(mapping, form, request, response);
     }
 
     public ActionForward revokeAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final Authorization authorization = getDomainObject(request, "authorizationOid");
 	
 	try {
 	    authorization.revoke();
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), "EXPENDITURE_RESOURCES");
 	}
 
 	return viewAuthorization(mapping, form, request, response);
     }
 
     public ActionForward delegateAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	Authorization authorization = getDomainObject(request, "authorizationOid");
 	request.setAttribute("authorization", authorization);
 	Person person = getLoggedPerson();
 
 	if (authorization.getPerson() == person && authorization.getCanDelegate()) {
 	    AuthorizationBean bean = new AuthorizationBean(authorization);
 	    request.setAttribute("bean", bean);
 	} else {
 	    addErrorMessage("label.unable.to.delegate.that.action", "EXPENDITURE_RESOURCES");
 	}
 
 	return mapping.findForward("delegate.authorization");
     }
 
     public ActionForward createDelegation(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	final AuthorizationBean bean = getRenderedObject("bean");
 	try {
 	    DelegatedAuthorization.delegate(bean.getAuthorization(), bean.getPerson(), bean.getCanDelegate(), bean.getEndDate());
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), "EXPENDITURE_RESOURCES");
 	    request.setAttribute("bean", bean);
 	    return mapping.findForward("delegate.authorization");
 	}
 	RenderUtils.invalidateViewState();
 	request.setAttribute("authorization", bean.getAuthorization());
 	return mapping.findForward("view.authorization");
     }
 
 }
