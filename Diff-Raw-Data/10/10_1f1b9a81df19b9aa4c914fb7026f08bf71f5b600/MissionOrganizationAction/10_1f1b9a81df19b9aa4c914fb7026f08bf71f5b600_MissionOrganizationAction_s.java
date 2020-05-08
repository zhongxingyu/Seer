 package module.mission.presentationTier.action;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import module.mission.domain.MissionSystem;
 import module.mission.domain.util.FunctionDelegationBean;
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.FunctionDelegation;
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 import module.organization.domain.Unit;
 import module.organization.presentationTier.actions.OrganizationModelAction;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.presentationTier.actions.ContextBaseAction;
 import myorg.presentationTier.component.OrganizationChart;
 
 import org.apache.commons.collections.ComparatorUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.joda.time.LocalDate;
 
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/missionOrganization")
 public class MissionOrganizationAction extends ContextBaseAction {
 
     @Override
     public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws Exception {
 	final ActionForward forward = super.execute(mapping, form, request, response);
 	OrganizationModelAction.addHeadToLayoutContext(request);
 	return forward;
     }
 
     public ActionForward showPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final User currentUser = UserView.getCurrentUser();
 	final Person person = currentUser.getPerson();
 	return showPerson(person, request);
     }
 
     public ActionForward showPersonById(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personId");
 	return showPerson(person, request);
     }
 
     public ActionForward showPerson(final Person person, final HttpServletRequest request) {
 	request.setAttribute("person", person);
 
 	final MissionSystem missionSystem = MissionSystem.getInstance();
 	final Collection<Accountability> workingPlaceAccountabilities = person.getParentAccountabilities(missionSystem
 		.getAccountabilityTypesRequireingAuthorization());
 	request.setAttribute("workingPlaceAccountabilities", workingPlaceAccountabilities);
 
 	final Collection<Accountability> authorityAccountabilities = person.getParentAccountabilities(missionSystem
 		.getAccountabilityTypesThatAuthorize());
 	request.setAttribute("authorityAccountabilities", authorityAccountabilities);
 
 	return forward(request, "/mission/showPerson.jsp");
     }
 
     public ActionForward showUnitById(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitId");
 	return showUnit(unit, request);
     }
 
     public ActionForward showUnit(final Unit unit, final HttpServletRequest request) {
 	request.setAttribute("unit", unit);
 
 	final MissionSystem missionSystem = MissionSystem.getInstance();
 	final Collection<AccountabilityType> accountabilityTypes = missionSystem.getAccountabilityTypesForUnits();
 	final Collection<Unit> parents = sortUnit(unit.getParents(accountabilityTypes));
 	final Collection<Unit> children = sortUnit(unit.getChildren(accountabilityTypes));
 	OrganizationChart<Unit> chart = new OrganizationChart<Unit>(unit, parents, children, 3);
 	request.setAttribute("chart", chart);
 
 	final Collection<Accountability> authorityAccountabilities = sortChildren(unit.getChildrenAccountabilities(missionSystem
 		.getAccountabilityTypesThatAuthorize()));
 	request.setAttribute("authorityAccountabilities", authorityAccountabilities);
 
 	final Collection<Accountability> workerAccountabilities = sortChildren(unit.getChildrenAccountabilities(missionSystem
 		.getAccountabilityTypesRequireingAuthorization()));
 	request.setAttribute("workerAccountabilities", workerAccountabilities);
 
 	return forward(request, "/mission/showUnit.jsp");
     }
 
     private Collection<Unit> sortUnit(final Collection<Party> parties) {
 	final SortedSet<Unit> result = new TreeSet<Unit>(Unit.COMPARATOR_BY_NAME);
 	result.addAll((Collection) parties);
 	return result;
     }
 
     private Collection<Accountability> sortChildren(final Collection<Accountability> accountabilities) {
 	final SortedSet<Accountability> result = new TreeSet<Accountability>(Accountability.COMPARATOR_BY_CHILD_PARTY_NAMES);
 	result.addAll(accountabilities);
 	return result;
     }
 
     public ActionForward addUnitWithResumedAuthorizations(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitId");
 	MissionSystem.getInstance().addUnitWithResumedAuthorizations(unit);
 	return showUnit(unit, request);
     }
 
     public ActionForward removeUnitWithResumedAuthorizations(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitId");
 	MissionSystem.getInstance().removeUnitWithResumedAuthorizations(unit);
 	return showUnit(unit, request);
     }
 
     public ActionForward showDelegationsForAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Accountability accountability = getDomainObject(request, "authorizationId");
 	return showDelegationsForAuthorization(request, accountability);
     }
 
     public ActionForward showDelegationsForAuthorization(final HttpServletRequest request, final Accountability accountability) {
 	String sortBy = request.getParameter("sortBy");
 	String order = request.getParameter("order");
 	request.setAttribute("sortBy", (sortBy == null) ? "parentUnitName" : sortBy);
 	request.setAttribute("order", (order == null) ? "asc" : order);
 	request.setAttribute("accountability", accountability);
 
 	Comparator<FunctionDelegation> comparator;
 	if (StringUtils.equals(sortBy, "childPartyName")) {
 	    comparator = FunctionDelegation.COMPARATOR_BY_DELEGATEE_CHILD_PARTY_NAME;
 	} else {
 	    comparator = FunctionDelegation.COMPARATOR_BY_DELEGATEE_PARENT_UNIT_NAME;
 	}
 	if (StringUtils.equals(order, "desc")) {
 	    comparator = ComparatorUtils.reversedComparator(comparator);
 	}
 	TreeSet<FunctionDelegation> functionDelegationDelegated = new TreeSet<FunctionDelegation>(comparator);
 	functionDelegationDelegated.addAll(accountability.getFunctionDelegationDelegated());
 	request.setAttribute("functionDelegationDelegated", functionDelegationDelegated);
 
 	return forward(request, "/mission/showDelegationForAuthorization.jsp");
     }
 
     public ActionForward prepareAddDelegationsForAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Accountability accountability = getDomainObject(request, "authorizationId");
 	final FunctionDelegationBean functionDelegationBean = new FunctionDelegationBean(accountability);
 	request.setAttribute("functionDelegationBean", functionDelegationBean);
 	return forward(request, "/mission/addDelegationForAuthorization.jsp");
     }
 
     public ActionForward addDelegationsForAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final FunctionDelegationBean functionDelegationBean = getRenderedObject();
 	final Accountability accountability = functionDelegationBean.getAccountability();
 	final Unit unit = functionDelegationBean.getUnit();
 	final Person person = functionDelegationBean.getPerson();
 	final LocalDate beginDate = functionDelegationBean.getBeginDate();
 	final LocalDate endDate = functionDelegationBean.getEndDate();
	FunctionDelegation.create(accountability, unit, person, beginDate, endDate);
 	return showDelegationsForAuthorization(request, accountability);
     }
 
 }
