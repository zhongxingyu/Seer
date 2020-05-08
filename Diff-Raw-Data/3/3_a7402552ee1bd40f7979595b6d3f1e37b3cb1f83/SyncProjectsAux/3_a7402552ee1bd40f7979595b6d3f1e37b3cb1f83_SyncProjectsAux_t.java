 package pt.ist.expenditureTrackingSystem.domain;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import myorg.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.persistenceTier.ExternalDbOperation;
 import pt.ist.expenditureTrackingSystem.persistenceTier.ExternalDbQuery;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.utl.ist.fenix.tools.util.FileUtils;
 
 public class SyncProjectsAux {
 
     static class MgpProject {
 	private String projectCode;
 	private String unidExploracao;
 	private String title;
 	private String idCoord;
 	private String costCenter;
 	private String inicio;
 	private String duracao;
 	private String status;
 
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
 
     private static class ProjectReader extends ExternalDbOperation {
 
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
 	}	
 
 	Set<MgpProject> getMgpProjects() {
 	    return mgpProjects;
 	}
     }
 
     int createdProjects = 0;
     int updatedProjects = 0;
     private Map<String, String> teachers = new HashMap<String, String>();
     private Set<Integer> projectResponsibles = new HashSet<Integer>();
 
     @Service
     public void syncData() throws IOException, SQLException {
 	final ProjectReader projectReader = new ProjectReader();
 	projectReader.execute();
 	final Set<MgpProject> mgpProjects = projectReader.getMgpProjects();
 
 	System.out.println("Read " + mgpProjects.size() + " projects from mgp.");
 
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
 	System.out.println("Did not find " + notFoundCostCenters.size() + " cost centers.");
     }
 
     private void loadTeachers() throws IOException {
 	final InputStream inputStream = getClass().getResourceAsStream("/teacher.csv");
 	final String contents = FileUtils.readFile(inputStream);
 	for (String line : contents.split("\n")) {
 	    String[] split = line.split("\t");
 	    if (split.length == 2 && split[1] != null && !split[1].isEmpty()) {
 		teachers.put(split[0], split[1]);
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
 
 	project.setName(acronym);
 
 	final AccountingUnit accountingUnit = AccountingUnit.readAccountingUnitByUnitName(accountingUnitString);
 	project.setAccountingUnit(accountingUnit);
 
 	final Person responsible = findPerson(responsibleString);
 	if (responsible != null) {
 	    if (projectResponsibles.contains(Integer.valueOf(responsibleString))) {
 		if (!hasAuthorization(project, responsible)) {
 		    final Authorization authorization = new Authorization(responsible, project);
 		    authorization.setMaxAmount(AUTHORIZED_VALUE);
 		}
 	    } else {
 		// System.out.println("[" + responsibleString + "] for project [" + acronym + "] is not in project responsibles list");
 	    }
 	}	    
 
 	final Unit costCenter = findCostCenter(costCenterString);
 	if (project.getCostCenterUnit() != costCenter) {
 	    System.out.println("Project: " + projectCodeString + " changed cost center to " + costCenterString + "!");
 	    System.out.println("   current cost center is " + (project.getCostCenterUnit() == null ? null : project.getCostCenterUnit().getCostCenter()));
 	    project.setParentUnit(costCenter);
 	}
     }
 
     private boolean hasAuthorization(final Project project, final Person responsible) {
 	for (final Authorization authorization : project.getAuthorizationsSet()) {
 	    if (authorization.getPerson() == responsible && authorization.getMaxAmount().isGreaterThanOrEqual(AUTHORIZED_VALUE)) {
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
 	String username = teachers.get(nMec);
 	if (username == null) {
 	    // System.out.println("Can't find username for " + nMec);
 	}
 	return username;
     }
 
 }
