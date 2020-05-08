 package pt.ist.expenditureTrackingSystem.domain;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import module.organization.domain.Accountability;
 import module.organization.domain.Party;
 import module.organizationIst.domain.IstAccountabilityType;
 import module.organizationIst.domain.IstPartyType;
 import myorg.domain.VirtualHost;
 import myorg.domain.util.Money;
 import pt.ist.dbUtils.ExternalDbOperation;
 import pt.ist.dbUtils.ExternalDbQuery;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.pstm.Transaction;
 import pt.utl.ist.fenix.tools.util.FileUtils;
 
 public abstract class SyncProjectsAux {
 
     public static class MgpSubProject {
 	private String institution;
 	private String institutionDescription;
 
 	MgpSubProject(final String institution, final String institutionDescription) {
 	    this.institution = institution;
 	    this.institutionDescription = institutionDescription;
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 	    if (obj instanceof MgpSubProject) {
 		final MgpSubProject mgpSubProject = (MgpSubProject) obj;
 		return institution.equals(mgpSubProject.institution);
 	    }
 	    return false;
 	}
 
 	@Override
 	public int hashCode() {
 	    return institution.hashCode();
 	}
 
 	public String getInstitution() {
 	    return institution;
 	}
 
 	public void setInstitution(String institution) {
 	    this.institution = institution;
 	}
 
 	public String getInstitutionDescription() {
 	    return institutionDescription;
 	}
 
 	public void setInstitutionDescription(String institutionDescription) {
 	    this.institutionDescription = institutionDescription;
 	}
 
     }
 
     public static class MgpProject {
 	private String projectCode;
 	private String unidExploracao;
 	private String title;
 	private Set<String> idCoord = new HashSet<String>();
 	private String costCenter;
 	private String inicio;
 	private String duracao;
 	private String status;
 	private String type;
 	private String accountManager;
 	private String subAccountingUnit;
 	private Set<MgpSubProject> subProjects = new HashSet<MgpSubProject>();
 
 	MgpProject(final ResultSet resultSet) throws SQLException {
 	    unidExploracao = resultSet.getString(1);
 	    projectCode = resultSet.getString(2);
 	    title = resultSet.getString(3);
 	    final String coord = resultSet.getString(4);
 	    if (coord != null && !coord.isEmpty()) {
 		idCoord.add(coord);
 	    }
 	    costCenter = resultSet.getString(5);
 	    inicio = resultSet.getString(6);
 	    duracao = resultSet.getString(7);
 	    status = resultSet.getString(8);
 	    type = resultSet.getString(9);
 	    accountManager = resultSet.getString(10);
 	    subAccountingUnit = resultSet.getString(11);
 	}
 
 	public String getProjectCode() {
 	    return projectCode;
 	}
 
 	public String getUnidExploracao() {
 	    return unidExploracao;
 	}
 
 	public String getTitle() {
 	    return title;
 	}
 
 	public Set<String> getIdCoord() {
 	    return idCoord;
 	}
 
 	public String getCostCenter() {
 	    return costCenter;
 	}
 
 	public String getInicio() {
 	    return inicio;
 	}
 
 	public String getDuracao() {
 	    return duracao;
 	}
 
 	public String getStatus() {
 	    return status;
 	}
 
 	public String getType() {
 	    return type;
 	}
 
 	public Set<MgpSubProject> getSubProjects() {
 	    return subProjects;
 	}
 
 	public String getAccountManager() {
 	    return accountManager;
 	}
 
 	public String getSubAccountingUnit() {
 	    return subAccountingUnit;
 	}
 
 	public void registerSubProject(final String institution, final String institutionDescription) {
 	    subProjects.add(new MgpSubProject(institution, institutionDescription));
 	}
 
 	public MgpSubProject findSubProject(final String name) {
 	    final int i = name.indexOf(" - ");
 	    final String prefix = name.substring(0, i);
 	    for (final MgpSubProject mgpSubProject : subProjects) {
 		if (mgpSubProject.institution.equals(prefix)) {
 		    return mgpSubProject;
 		}
 	    }
 	    return null;
 	}
 
     }
 
     public static class ProjectQuery implements ExternalDbQuery {
 
 	private final Set<MgpProject> mgpProjects = new HashSet<MgpProject>();
 
 	@Override
 	public String getQueryString() {
 	    return "SELECT v_projectos.unid_exploracao, v_projectos.projectcode, v_projectos.title,"
 		    + " v_projectos.idcoord, v_projectos.costcenter, v_projectos.inicio, v_projectos.duracao,"
 		    + " v_projectos.status, v_projectos.tipo, v_projectos.gestor, v_projectos.grupo" + " FROM v_projectos";
 	}
 
 	@Override
 	public void processResultSet(final ResultSet resultSet) throws SQLException {
 	    while (resultSet.next()) {
 		final MgpProject mgpProject = new MgpProject(resultSet);
 		mgpProjects.add(mgpProject);
 	    }
 	}
 
 	Set<MgpProject> getMgpProjects() {
 	    return mgpProjects;
 	}
 
     }
 
     public static class OtherProjectCoordinatorsQuery implements ExternalDbQuery {
 
 	private final Set<MgpProject> mgpProjects;
 
 	public OtherProjectCoordinatorsQuery(final Set<MgpProject> mgpProjects) {
 	    this.mgpProjects = mgpProjects;
 	}
 
 	@Override
 	public String getQueryString() {
 	    return "SELECT a.idproj, a.idautorizado, a.inicio, a.fim FROM autorizacoes a";
 	}
 
 	@Override
 	public void processResultSet(final ResultSet resultSet) throws SQLException {
 	    while (resultSet.next()) {
 		final String idproj = resultSet.getString(1);
 		final String idautorizado = resultSet.getString(2);
 		// final String inicio = resultSet.getString(3);
 		// final String fim = resultSet.getString(4);
 
 		final MgpProject mgpProject = findMgpProject(idproj);
 		mgpProject.getIdCoord().add(idautorizado);
 	    }
 	}
 
 	private MgpProject findMgpProject(String idproj) {
 	    for (final MgpProject mgpProject : mgpProjects) {
 		if (mgpProject.projectCode.equals(idproj)) {
 		    return mgpProject;
 		}
 	    }
 	    return null;
 	}
 
 	Set<MgpProject> getMgpProjects() {
 	    return mgpProjects;
 	}
 
     }
 
     public static class ProjectReader extends ExternalDbOperation {
 
 	private Set<MgpProject> mgpProjects = null;
 
 	private final String dbPrefix;
 
 	public ProjectReader(final String dbPrefix) {
 	    this.dbPrefix = dbPrefix;
 	}
 
 	@Override
 	protected String getDbPropertyPrefix() {
 	    return dbPrefix;
 	}
 
 	@Override
 	protected void doOperation() throws SQLException {
 	    final ProjectQuery projectQuery = new ProjectQuery();
 	    executeQuery(projectQuery);
 	    mgpProjects = projectQuery.getMgpProjects();
 	    final SubProjectQuery subProjectQuery = new SubProjectQuery(mgpProjects);
 	    executeQuery(subProjectQuery);
 	    final OtherProjectCoordinatorsQuery otherProjectCoordinatorsQuery = new OtherProjectCoordinatorsQuery(mgpProjects);
 	    executeQuery(otherProjectCoordinatorsQuery);
 	}
 
 	public Set<MgpProject> getMgpProjects() {
 	    return mgpProjects;
 	}
     }
 
     public static class SubProjectQuery implements ExternalDbQuery {
 
 	private final Set<MgpProject> mgpProjects;
 
 	public SubProjectQuery(final Set<MgpProject> mgpProjects) {
 	    this.mgpProjects = mgpProjects;
 	}
 
 	@Override
 	public String getQueryString() {
 	    return "SELECT a.projecto, a.instituicao, a.descricaoinstituicao FROM v_membros_consorcio a";
 	}
 
 	@Override
 	public void processResultSet(final ResultSet resultSet) throws SQLException {
 	    while (resultSet.next()) {
 		final String projectCode = resultSet.getString(1);
 		final String institution = resultSet.getString(2);
 		final String institutionDescription = resultSet.getString(3);
 
 		final MgpProject mgpProject = findMgpProject(projectCode);
 		if (mgpProject == null) {
 		    // nothing to be done.
 		} else {
 		    mgpProject.registerSubProject(institution, institutionDescription);
 		}
 	    }
 	}
 
 	private MgpProject findMgpProject(final String projectCode) {
 	    for (final MgpProject mgpProject : mgpProjects) {
 		if (mgpProject.getProjectCode().equals(projectCode)) {
 		    return mgpProject;
 		}
 	    }
 	    return null;
 	}
 
 	Set<MgpProject> getMgpProjects() {
 	    return mgpProjects;
 	}
 
     }
 
     int createdProjects = 0;
     int updatedProjects = 0;
     int createdSubProjects = 0;
     int updatedSubProjects = 0;
     int disconnectedSubProjects = 0;
     private Map<String, String> employees = new HashMap<String, String>();
     protected Set<Integer> projectResponsibles = new HashSet<Integer>();
 
     protected abstract String getDbPropertyPrefix();
 
     protected abstract String getVirtualHost();
 
     @Service
     public void syncData() throws IOException, SQLException {
 	try {
 	    System.out.println("Running sync for: " + getVirtualHost());
 	    VirtualHost.setVirtualHostForThread(getVirtualHost());
 
 	    final ProjectReader projectReader = new ProjectReader(getDbPropertyPrefix());
 	    projectReader.execute();
 	    final Set<MgpProject> mgpProjects = projectReader.getMgpProjects();
 
 	    System.out.println("Read " + mgpProjects.size() + " projects from mgp.");
 
 	    loadEmployees();
 	    loadTeachers();
 	    loadProjectResponsiblesSet();
 
 	    for (final MgpProject mgpProject : mgpProjects) {
 		Project project = Project.findProjectByCode(mgpProject.projectCode);
 		if (project == null) {
 		    createdProjects++;
 		    createProject(mgpProject);
 		} else {
 		    updatedProjects++;
 		    updateProject(mgpProject, project);
 		}
 	    }
 
 	    System.out.println("Created " + createdProjects + " projects.");
 	    System.out.println("Created " + createdSubProjects + " sub-projects.");
 	    System.out.println("Updated " + updatedProjects + " projects.");
 	    System.out.println("Updated " + updatedSubProjects + " sub-projects.");
 	    System.out.println("Found   " + disconnectedSubProjects + " diconnected sub-projects.");
 	    System.out.println("Did not find " + notFoundCostCenters.size() + " cost centers.");
 	} finally {
 	    VirtualHost.releaseVirtualHostFromThread();
 	}
     }
 
     private void loadTeachers() throws SQLException {
 	final Connection connection = Transaction.getCurrentJdbcConnection();
 
 	Statement statementQuery = null;
 	ResultSet resultSetQuery = null;
 	try {
 	    statementQuery = connection.createStatement();
 	    resultSetQuery = statementQuery
 		    .executeQuery("select fenix.USER.USER_U_ID, fenix.TEACHER.TEACHER_NUMBER from fenix.TEACHER inner join fenix.USER on fenix.USER.OID_PERSON = fenix.TEACHER.OID_PERSON;");
 	    int c = 0;
 	    while (resultSetQuery.next()) {
 		final String username = resultSetQuery.getString(1);
 		final String teacherNumber = resultSetQuery.getString(2);
 		employees.put(teacherNumber, username);
 		c++;
 	    }
 	    System.out.println("Processed: " + c + " teachers.");
 	} finally {
 	    if (resultSetQuery != null) {
 		resultSetQuery.close();
 	    }
 	    if (statementQuery != null) {
 		statementQuery.close();
 	    }
 	}
     }
 
     private void loadEmployees() throws SQLException {
 	final Connection connection = Transaction.getCurrentJdbcConnection();
 
 	Statement statementQuery = null;
 	ResultSet resultSetQuery = null;
 	try {
 	    statementQuery = connection.createStatement();
 	    resultSetQuery = statementQuery
 		    .executeQuery("select fenix.USER.USER_U_ID, fenix.EMPLOYEE.EMPLOYEE_NUMBER from fenix.EMPLOYEE inner join fenix.USER on fenix.USER.OID_PERSON = fenix.EMPLOYEE.OID_PERSON;");
 	    int c = 0;
 	    while (resultSetQuery.next()) {
 		final String username = resultSetQuery.getString(1);
 		final String employeeNumber = resultSetQuery.getString(2);
 		employees.put(employeeNumber, username);
 		c++;
 	    }
 	    System.out.println("Processed: " + c + " employees.");
 	} finally {
 	    if (resultSetQuery != null) {
 		resultSetQuery.close();
 	    }
 	    if (statementQuery != null) {
 		statementQuery.close();
 	    }
 	}
     }
 
     private void loadProjectResponsiblesSet() throws IOException {
 	final InputStream inputStream = getClass().getResourceAsStream("/responsaveisProjectos.csv");
 	String contents = FileUtils.readFile(inputStream);
 	for (String line : contents.split("\n")) {
 	    String[] split = line.split("\t");
 	    projectResponsibles.add(Integer.valueOf(split[0]));
 	}
     }
 
     private void createProject(/* final Map<String, SubAccountingUnit> subAccountingUnits, */ final MgpProject mgpProject) {
 	String projectCodeString = mgpProject.projectCode;
 	String costCenterString = mgpProject.costCenter.replace("\"", "");
 	Set<String> responsibleStrings = mgpProject.idCoord;
 	String acronym = mgpProject.title.replace("\"", "");
 	String accountingUnitString = mgpProject.unidExploracao.replace("\"", "");
 	String type = mgpProject.type.replace("\"", "");
 	String accountManager = mgpProject.accountManager;
 	String subAccountingUnitString = mgpProject.subAccountingUnit == null ? null : mgpProject.subAccountingUnit.replace("\"", "");
 
 	final Unit costCenter = findCostCenter(costCenterString);
 	if (costCenter != null) {
 
 	    final CreateUnitBean createUnitBean = new CreateUnitBean(costCenter);
 	    createUnitBean.setProjectCode(projectCodeString);
 	    createUnitBean.setName(acronym);
 	    final Unit unit = Unit.createNewUnit(createUnitBean);
 	    unit.setDefaultRegeimIsCCP(!type.equalsIgnoreCase("i"));
 
 	    final AccountingUnit accountingUnit = AccountingUnit.readAccountingUnitByUnitName(accountingUnitString);
 	    if (accountingUnit != null) {
 		unit.setAccountingUnit(accountingUnit);
 	    } else {
 		System.out.println("No accounting unit found for project: " + projectCodeString);
 	    }
 
 	    for (final String responsibleString : responsibleStrings) {
 		final Person responsible = findPerson(responsibleString);
 		if (responsible != null) {
 		    if (isResponsabilityAllowed(responsibleString)) {
 			final Authorization authorization = new Authorization(responsible, unit, "Imported from MGP");
 			authorization.setMaxAmount(getAuthorizationValue());
 		    } else {
 			// System.out.println("[" + responsibleString +
 			// "] for project [" + acronym
 			// + "] is not in project responsibles list");
 		    }
 		}
 	    }
 
 	    final Person accountManagerPerson = findPersonByUsername(accountManager);
 	    final Project project = (Project) unit;
 	    project.setAccountManager(accountManagerPerson);
 
 	    if (subAccountingUnitString != null
 		    && !subAccountingUnitString.isEmpty()) {
 		final AccountingUnit subAccountingUnit = AccountingUnit.readAccountingUnitByUnitName("20 - " + subAccountingUnitString);
 		if (subAccountingUnit != null) {
 		    unit.setAccountingUnit(accountingUnit);
 		} else {
 		    System.out.println("Unable to find accounting unit with name: 20 - " + subAccountingUnitString);
 		}
 	    }
 
 	}
     }
 
     protected abstract Money getAuthorizationValue();
 
     protected boolean isResponsabilityAllowed(final String responsibleString) {
 	return true;
     }
 
     //final static Money AUTHORIZED_VALUE = new Money("75000");
 
     private void updateProject(/* final Map<String, SubAccountingUnit> subAccountingUnits, */ final MgpProject mgpProject,
 	    final Project project) {
 	String projectCodeString = mgpProject.projectCode;
 	String costCenterString = mgpProject.costCenter.replace("\"", "");
 	Set<String> responsibleStrings = mgpProject.idCoord;
 	String acronym = mgpProject.title.replace("\"", "");
 	String accountingUnitString = mgpProject.unidExploracao.replace("\"", "");
 	String type = mgpProject.type.replace("\"", "");
 	String accountManager = mgpProject.accountManager;
 	String subAccountingUnitString = mgpProject.subAccountingUnit == null ? null : mgpProject.subAccountingUnit.replace("\"", "");
 	Person accountManagerPerson = findPersonByUsername(accountManager);
 
 	if (!acronym.equals(project.getName())) {
 	    project.setName(acronym);
 	}
 
 	// V 3 I A F E f
 	if (mgpProject.getStatus() != null && !"V".equals(mgpProject.getStatus())) {
 	    if (project.isOpen()) {
 		project.close();
 	    }
	} else if (!project.isOpen()) {
	    project.open();
 	}
 
 	project.setAccountManager(accountManagerPerson);
 
 	AccountingUnit accountingUnit = AccountingUnit.readAccountingUnitByUnitName(accountingUnitString);
 	if (subAccountingUnitString != null && !subAccountingUnitString.isEmpty()) {
 	    final AccountingUnit subAccountingUnit = AccountingUnit.readAccountingUnitByUnitName("20 - " + subAccountingUnitString);
 	    if (subAccountingUnit != null) {
 		accountingUnit = subAccountingUnit;
 	    } else {
 		System.out.println("Unable to find accounting unit with name: 20 - " + subAccountingUnitString);
 	    }
 	}
 
 	boolean changeAccountingUnit = accountingUnit != project.getAccountingUnit();
 	if (changeAccountingUnit) {
 	    changeAccountingUnit(project, accountingUnit);
 	}
 
 	for (final String responsibleString : responsibleStrings) {
 	    final Person responsible = findPerson(responsibleString);
 	    if (responsible != null) {
 		if (projectResponsibles.contains(Integer.valueOf(responsibleString))) {
 		    if (!hasAuthorization(project, responsible)) {
 			final Authorization authorization = new Authorization(responsible, project, "Imported from MGP");
 			authorization.setMaxAmount(getAuthorizationValue());
 		    }
 		} else {
 		    // System.out.println("[" + responsibleString +
 		    // "] for project [" + acronym +
 		    // "] is not in project responsibles list");
 		    if (!hasAprovalAuthorization(project, responsible)) {
 			new Authorization(responsible, project, "Imported from MGP");
 		    }
 		}
 	    }
 	}
 
 	final Unit costCenter = findCostCenter(costCenterString);
 	if (project.getCostCenterUnit() != costCenter) {
 	    System.out.println("Project: " + projectCodeString + " changed cost center to " + costCenterString + "!");
 	    System.out.println("   current cost center is "
 		    + (project.getCostCenterUnit() == null ? null : project.getCostCenterUnit().getCostCenter()));
 	    project.setParentUnit(costCenter);
 	}
 
 	for (final MgpSubProject mgpSubProject : mgpProject.getSubProjects()) {
 	    final SubProject subProject = project.findSubProjectByNamePrefix(mgpSubProject.institution);
 	    if (subProject == null) {
 		createdSubProjects++;
 		createSubProject(project, mgpSubProject);
 	    } else {
 		updatedSubProjects++;
 		updateSubProject(subProject, mgpSubProject);
 		if (changeAccountingUnit) {
 		    changeAccountingUnit(subProject, accountingUnit);
 		}
 	    }
 	}
 
 	for (final Accountability accountability : project.getUnit().getChildAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		final Party party = accountability.getChild();
 		if (party.isUnit()) {
 		    final module.organization.domain.Unit child = (module.organization.domain.Unit) party;
 		    if (child.hasExpenditureUnit()) {
 			final Unit unit = child.getExpenditureUnit();
 			if (unit instanceof SubProject) {
 			    final SubProject subProject = (SubProject) unit;
 			    final MgpSubProject mgpSubProject = mgpProject.findSubProject(subProject.getName());
 			    if (mgpProject == null) {
 				disconnectedSubProjects++;
 				System.out.println("Project: " + project.getPresentationName() + " no longer has subproject: "
 					+ mgpSubProject.getInstitution() + " - " + mgpSubProject.getInstitutionDescription());
 			    }
 			}
 		    }
 		}
 	    }
 	}
     }
 
     private void changeAccountingUnit(final Unit unit, final AccountingUnit accountingUnit) {
 	if (accountingUnit != unit.getAccountingUnit()) {
 	    unit.setAccountingUnit(accountingUnit);
 	}
 	for (Financer financer : unit.getFinancedItems()) {
 	    financer.setAccountingUnit(accountingUnit);
 	}
 	for (final Unit subUnit : unit.getSubUnitsSet()) {
 	    changeAccountingUnit(subUnit, accountingUnit);
 	}
     }
 
     private Person findPersonByUsername(final String username) {
 	if (username == null || username.trim().isEmpty()) {
 	    return null;
 	}
 	return Person.findByUsername(username.trim().toLowerCase());
     }
 
     private void createSubProject(final Project project, final MgpSubProject mgpSubProject) {
 	final String subProjectName = project.getName() + " - " + mgpSubProject.getInstitution() + " - "
 		+ mgpSubProject.getInstitutionDescription();
 	final Unit unit = Unit.createRealUnit(project, IstPartyType.SUB_PROJECT, "", subProjectName);
 	unit.setDefaultRegeimIsCCP(project.getDefaultRegeimIsCCP());
 	final SubProject subProject = (SubProject) unit;
 	subProject.setParentUnit(project);
     }
 
     private void updateSubProject(final SubProject subProject, final MgpSubProject mgpSubProject) {
 	final String subProjectName = subProject.getParentUnit().getName() + " - " + mgpSubProject.getInstitution() + " - "
 		+ mgpSubProject.getInstitutionDescription();
 	subProject.setName(subProjectName);
     }
 
     private boolean hasAuthorization(final Project project, final Person responsible) {
 	for (final Authorization authorization : project.getAuthorizationsSet()) {
 	    if (authorization.getPerson() == responsible && authorization.getMaxAmount().isGreaterThanOrEqual(getAuthorizationValue())) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     private boolean hasAprovalAuthorization(final Project project, final Person responsible) {
 	for (final Authorization authorization : project.getAuthorizationsSet()) {
 	    if (authorization.getPerson() == responsible && authorization.getMaxAmount().isGreaterThanOrEqual(Money.ZERO)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     private static final Set<String> cdCostCenters = new HashSet<String>();
     static {
 	cdCostCenters.add("9999");
 	cdCostCenters.add("0003");
     }
 
     private final Set<String> notFoundCostCenters = new HashSet<String>();
 
     protected Unit findCostCenter(final String costCenterString) {
 	final String costCenter = cdCostCenters.contains(costCenterString) ? "0003" : costCenterString;
 	final Integer cc = Integer.valueOf(costCenter);
 	final String ccString = cc.toString();
 	// final Unit unit = Unit.findUnitByCostCenter(cc.toString());
 	Unit unit = null;
 	for (final Unit ounit : ExpenditureTrackingSystem.getInstance().getUnitsSet()) {
 	    if (ounit instanceof CostCenter) {
 		final CostCenter ccUnit = (CostCenter) ounit;
 		if (Integer.parseInt(ccString) == Integer.parseInt(ccUnit.getCostCenter())) {
 		    unit = ounit;
 		}
 	    }
 	}
 	if (unit == null) {
 	    if (!notFoundCostCenters.contains(costCenter)) {
 		System.out.println("Not found cost center: " + costCenterString);
 		notFoundCostCenters.add(costCenterString);
 	    }
 	}
 	return unit;
     }
 
     private Person findPerson(final String responsibleString) {
 	if (!responsibleString.isEmpty()) {
 	    String user = findISTUsername(responsibleString);
 	    return user == null ? null : Person.findByUsername(user);
 	}
 	return null;
     }
 
     private String findISTUsername(String nMec) {
 	if (nMec.length() == 66 && nMec.indexOf("99") == 0) {
 	    return null;
 	}
 	String username = employees.get(nMec);
 	if (username == null) {
 	    // System.out.println("Can't find username for " + nMec);
 	}
 	return username;
     }
 
 }
