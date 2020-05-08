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
 import myorg.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.persistenceTier.ExternalDbOperation;
 import pt.ist.expenditureTrackingSystem.persistenceTier.ExternalDbQuery;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.pstm.Transaction;
 import pt.utl.ist.fenix.tools.util.FileUtils;
 
 public class SyncProjectsAux {
 
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
 	private String idCoord;
 	private String costCenter;
 	private String inicio;
 	private String duracao;
 	private String status;
 	private Set<MgpSubProject> subProjects = new HashSet<MgpSubProject>();
 
 	MgpProject(final ResultSet resultSet) throws SQLException {
 	    unidExploracao = resultSet.getString(1);
 	    projectCode = resultSet.getString(2);
 	    title = resultSet.getString(3);
 	    idCoord = resultSet.getString(4);
 	    costCenter = resultSet.getString(5);
 	    inicio = resultSet.getString(6);
 	    duracao = resultSet.getString(7);
 	    status = resultSet.getString(8);
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
 
 	public String getIdCoord() {
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
 
 	public Set<MgpSubProject> getSubProjects() {
 	    return subProjects;
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
 
     private static class ProjectQuery extends ExternalDbQuery {
 
 	private final Set<MgpProject> mgpProjects = new HashSet<MgpProject>();
 
 	@Override
 	protected String getQueryString() {
 	    return "SELECT v_projectos.unid_exploracao, v_projectos.projectcode, v_projectos.title,"
 	    		+ " v_projectos.idcoord, v_projectos.costcenter, v_projectos.inicio, v_projectos.duracao, v_projectos.status"
 	    		+ " FROM v_projectos";
 	}
 
 	@Override
 	protected void processResultSet(final ResultSet resultSet) throws SQLException {
 	    while (resultSet.next()) {
 		final MgpProject mgpProject = new MgpProject(resultSet);
 		mgpProjects.add(mgpProject);
 	    }
 	}
 
 	Set<MgpProject> getMgpProjects() {
 	    return mgpProjects;
 	}
     }
 
     private static class SubProjectQuery extends ExternalDbQuery {
 
 	private final Set<MgpProject> mgpProjects;
 
 	public SubProjectQuery(final Set<MgpProject> mgpProjects) {
 	    this.mgpProjects = mgpProjects;
 	}
 
 	@Override
 	protected String getQueryString() {
 	    return "SELECT a.projecto, a.instituicao, a.descricaoinstituicao FROM v_membros_consorcio a";
 	}
 
 	@Override
 	protected void processResultSet(final ResultSet resultSet) throws SQLException {
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
 
     public static class ProjectReader extends ExternalDbOperation {
 
 	private Set<MgpProject> mgpProjects = null;
 
 	@Override
 	protected String getDbPropertyPrefix() {
 	    return "db.mgp";
 	}
 
 	@Override
 	protected void doOperation() throws SQLException {
 	    final ProjectQuery projectQuery = new ProjectQuery();
 	    executeQuery(projectQuery);
 	    mgpProjects = projectQuery.getMgpProjects();
 	    final SubProjectQuery subProjectQuery = new SubProjectQuery(mgpProjects);
 	    executeQuery(subProjectQuery);
 	}	
 
 	public Set<MgpProject> getMgpProjects() {
 	    return mgpProjects;
 	}
     }
 
     int createdProjects = 0;
     int updatedProjects = 0;
     int createdSubProjects = 0;
     int updatedSubProjects = 0;
     int disconnectedSubProjects = 0;
     private Map<String, String> employees = new HashMap<String, String>();
     private Set<Integer> projectResponsibles = new HashSet<Integer>();
 
     @Service
     public void syncData() throws IOException, SQLException {
 	final ProjectReader projectReader = new ProjectReader();
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
     }
 
     private void loadTeachers() throws SQLException {
 	final Connection connection = Transaction.getCurrentJdbcConnection();
 
 	Statement statementQuery = null;
 	ResultSet resultSetQuery = null;
 	try {
 	    statementQuery = connection.createStatement();
 	    resultSetQuery = statementQuery.executeQuery("select fenix.USER.USER_U_ID, fenix.TEACHER.TEACHER_NUMBER from fenix.TEACHER inner join fenix.USER on fenix.USER.KEY_PERSON = fenix.TEACHER.KEY_PERSON;");
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
 	    resultSetQuery = statementQuery.executeQuery("select fenix.USER.USER_U_ID, fenix.EMPLOYEE.EMPLOYEE_NUMBER from fenix.EMPLOYEE inner join fenix.USER on fenix.USER.KEY_PERSON = fenix.EMPLOYEE.KEY_PERSON;");
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
 
     private void createProject(final MgpProject mgpProject) {
 	String projectCodeString = mgpProject.projectCode;
 	String costCenterString = mgpProject.costCenter.replace("\"", "");
 	String responsibleString = mgpProject.idCoord;
 	String acronym = mgpProject.title.replace("\"", "");
 	String accountingUnitString = mgpProject.unidExploracao.replace("\"", "");
 
 	final Unit costCenter = findCostCenter(costCenterString);
 	if (costCenter != null) {
 	    final Person responsible = findPerson(responsibleString);
 
 	    final CreateUnitBean createUnitBean = new CreateUnitBean(costCenter);
 	    createUnitBean.setProjectCode(projectCodeString);
 	    createUnitBean.setName(acronym);
 	    final Unit unit = Unit.createNewUnit(createUnitBean);
 
 	    final AccountingUnit accountingUnit = AccountingUnit.readAccountingUnitByUnitName(accountingUnitString);
 	    if (accountingUnit != null) {
 		unit.setAccountingUnit(accountingUnit);
 	    } else {
 		System.out.println("No accounting unit found for project: " + projectCodeString);
 	    }
 
 	    if (responsible != null) {
 		if (projectResponsibles.contains(Integer.valueOf(responsibleString))) {
 		    final Authorization authorization = new Authorization(responsible, unit);
 		    authorization.setMaxAmount(new Money("12470"));
 		} else {
 //		    System.out.println("[" + responsibleString + "] for project [" + acronym
 //			    + "] is not in project responsibles list");
 		}
 	    }
 	}
     }
 
     final static Money AUTHORIZED_VALUE = new Money("12470");
 
     private void updateProject(final MgpProject mgpProject, final Project project) {
 	String projectCodeString = mgpProject.projectCode;
 	String costCenterString = mgpProject.costCenter.replace("\"", "");
 	String responsibleString = mgpProject.idCoord;
 	String acronym = mgpProject.title.replace("\"", "");
 	String accountingUnitString = mgpProject.unidExploracao.replace("\"", "");
 
 	if (!acronym.equals(project.getName())) {
 	    project.setName(acronym);
 	}
 
 	final AccountingUnit accountingUnit = AccountingUnit.readAccountingUnitByUnitName(accountingUnitString);
 	if (accountingUnit != project.getAccountingUnit()) {
 	    project.setAccountingUnit(accountingUnit);
 	}
 
 	final Person responsible = findPerson(responsibleString);
 	    if (responsibleString.equals("3568")) {
 		System.out.println("   resp string 3568 has resopnsible: " + responsible);
 	    }
 	if (responsible != null) {
 	    if (responsibleString.equals("3568")) {
 		System.out.println("Found person.");
 	    }
 	    if (projectResponsibles.contains(Integer.valueOf(responsibleString))) {
 		if (!hasAuthorization(project, responsible)) {
 		    final Authorization authorization = new Authorization(responsible, project);
 		    authorization.setMaxAmount(AUTHORIZED_VALUE);
 		}
 	    } else {
 		// System.out.println("[" + responsibleString + "] for project [" + acronym + "] is not in project responsibles list");
 		if (!hasAprovalAuthorization(project, responsible)) {
 		    new Authorization(responsible, project);
 		}
 	    }
 	}	    
 
 	final Unit costCenter = findCostCenter(costCenterString);
 	if (project.getCostCenterUnit() != costCenter) {
 	    System.out.println("Project: " + projectCodeString + " changed cost center to " + costCenterString + "!");
 	    System.out.println("   current cost center is " + (project.getCostCenterUnit() == null ? null : project.getCostCenterUnit().getCostCenter()));
 	    project.setParentUnit(costCenter);
 	}
 
 	for (final MgpSubProject mgpSubProject : mgpProject.getSubProjects()) {
 	    final SubProject subProject = project.findSubProjectByNamePrefix(mgpSubProject.institution);
 	    if (subProject == null) {
 		createdSubProjects++;
 //		System.out.println("Creating subproject: " + mgpSubProject.getInstitution() + " - " + mgpSubProject.getInstitutionDescription()
 //			+ " for project: " + mgpProject.getProjectCode() + " - " + mgpProject.getTitle());
 		createSubProject(project, mgpSubProject);
 	    } else {
 		updatedSubProjects++;
 		updateSubProject(subProject, mgpSubProject);
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
 				System.out.println("Project: " + project.getPresentationName() + " no longer has subproject: " + mgpSubProject.getInstitution() + " - " + mgpSubProject.getInstitutionDescription());
 			    }
 			}
 		    }
 		}
 	    }
 	}
     }
 
     private void createSubProject(final Project project, final MgpSubProject mgpSubProject) {
 	final String subProjectName = project.getName() + " - " + mgpSubProject.getInstitution() + " - " + mgpSubProject.getInstitutionDescription();
 	final Unit unit = Unit.createRealUnit(project, IstPartyType.SUB_PROJECT, "", subProjectName);
 	final SubProject subProject = (SubProject) unit;
 	subProject.setParentUnit(project);
     }
 
     private void updateSubProject(final SubProject subProject, final MgpSubProject mgpSubProject) {
	final String subProjectName = subProject.getParentUnit().getName() + " - " + mgpSubProject.getInstitution() + " - " + mgpSubProject.getInstitutionDescription();
	subProject.setName(subProjectName);
     }
 
     private boolean hasAuthorization(final Project project, final Person responsible) {
 	for (final Authorization authorization : project.getAuthorizationsSet()) {
 	    if (authorization.getPerson() == responsible && authorization.getMaxAmount().isGreaterThanOrEqual(AUTHORIZED_VALUE)) {
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
 
     private Unit findCostCenter(final String costCenterString) {
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
 //	    System.out.println("Can't find username for " + nMec);
 	}
 	return username;
     }
 
 }
