 package pt.ist.expenditureTrackingSystem;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
import java.util.Stack;
 
 import pt.ist.expenditureTrackingSystem._development.PropertiesManager;
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.CPVReference;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.FenixWebFramework;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.utl.ist.fenix.tools.util.FileUtils;
 
 public class LoadTestData {
 
     public static void init() {
 	String domainModelPath = "web/WEB-INF/classes/domain_model.dml";
 	FenixWebFramework.initialize(PropertiesManager.getFenixFrameworkConfig(domainModelPath));
 	ExpenditureTrackingSystem.initialize(FenixWebFramework.getConfig());
     }
 
     public static void main(String[] args) {
 	init();
 	try {
 	    loadData();
 	} catch (final IOException e) {
 	    throw new Error(e);
 	}
 	System.out.println("Done.");
     }
 
     private static class FenixUnit {
 	private Long oid;
 	private Long parent;
 	private String costCenterCode;
 	private String name;
 	private Unit unit;
 
 	private FenixUnit(final String line) {
 	    final int iTab1 = line.indexOf('\t');
 	    final int iTab2 = line.indexOf('\t', iTab1 + 1);
 	    final int iTab3 = line.indexOf('\t', iTab2 + 1);
 
 	    oid = Long.valueOf(line.substring(0, iTab1));
 
 	    final String parentString = line.substring(iTab1 + 1, iTab2);
 	    parent = parentString.equals("null") ? null : Long.valueOf(parentString);
 
 	    final String costCenterString = line.substring(iTab2 + 1, iTab3);
 	    costCenterCode = costCenterString.equals("null") ? null : costCenterString;
 
 	    name = line.substring(iTab3 + 1);
 	}
 
 	public Unit getParentUnit(final FenixUnitMap fenixUnitMap) {
 	    final FenixUnit fenixUnit = parent == null ? null : fenixUnitMap.get(parent);
 	    return fenixUnit == null ? null : fenixUnit.unit;
 	}
 
     }
 
     private static class FenixUnitMap extends ArrayList<FenixUnit> {
 
 	private FenixUnitMap(final String contents) {
 	    for (final String line : contents.split("\n")) {
 		add(line);
 	    }
 	}
 
 	public FenixUnit get(final Long oid) {
 	    if (oid == null) {
 		return null;
 	    }
 	    for (final FenixUnit fenixUnit : this) {
 		if (oid.equals(fenixUnit.oid)) {
 		    return fenixUnit;
 		}
 	    }
 	    return null;
 	}
 
 	private void add(final String line) {
 	    final FenixUnit fenixUnit = new FenixUnit(line);
 	    add(fenixUnit);
 	}
 
     }
 
     private static class CdUnit {
 	private CdUnit parentUnit;
 	private final String costCenterCode;
 	private final String name;
 	private Unit unit;
 
 	private CdUnit(final CdUnit parent, final String line) {
 	    final String[] parts = line.split("\t");
 	    final String ccPart = parts[0].trim();
 	    costCenterCode = ccPart.isEmpty() ? null : ccPart;
 	    name = parts[1].trim();
 	    parentUnit = parent;
 	}
     }
 
     private static class CdUnitMap extends ArrayList<CdUnit> {
 
 	private CdUnitMap(final String contents) {
	    final Stack<CdUnit> stack = new Stack<CdUnit>();
	    stack.push(add(null, "\tInstituto Superior Técnico"));

	    boolean previousIsCC = false;
 	    for (final String line : contents.split("\n")) {
		final CdUnit cdUnit = add(stack.peek(), line);
 		if (cdUnit.costCenterCode == null) {
		    if (previousIsCC && !stack.peek().name.equals("Taguspark")) {
			stack.pop();
			if (cdUnit.name.startsWith("COORDENA") && cdUnit.name.endsWith("O DE LICENCIATURA")) {
			    stack.pop();
			}
 		    }
		    cdUnit.parentUnit = stack.peek();
		    stack.push(cdUnit);
		    previousIsCC = false;
 		} else {
		    previousIsCC = true;
 		}
 	    }
 	}
 
 	private CdUnit add(final CdUnit parent, final String line) {
 	    final CdUnit cdUnit = new CdUnit(parent, line);
 	    add(cdUnit);
 	    return cdUnit;
 	}
 
     }
 
     private static class FenixPerson {
 
 	private String username;
 	private String name;
 	private Set<Long> unitOids = new HashSet<Long>();
 	private Person person;
 
 	public FenixPerson(final String line) {
 	    final String[] parts = line.split("\t");
 	    username = parts[0];
 	    name = parts[1];
 	    for (int i = 2; i < parts.length; i++) {
 		final String unitOid = parts[i];
 		unitOids.add(Long.valueOf(unitOid));
 	    }
 	}
 
     }
 
     private static class FenixPeopleSet extends ArrayList<FenixPerson> {
 
 	public FenixPeopleSet(final String contents) {
 	    for (final String line : contents.split("\n")) {
 		final FenixPerson fenixPerson = new FenixPerson(line);
 		add(fenixPerson);
 	    }
 	}
 
     }
 
     private static void loadData() throws IOException {
 //	final String unitContents = FileUtils.readFile("units.txt");
 //	final FenixUnitMap fenixUnitMap = new FenixUnitMap(unitContents);
 //	createUnits(fenixUnitMap);
 
 	final String activeCostCenterContents = FileUtils.readFile("activeCostCenters.csv");
 	final CdUnitMap cdUnitMap = new CdUnitMap(activeCostCenterContents);
 	createUnits(cdUnitMap);
 
 	final String peopleContents = FileUtils.readFile("people.txt");
 	final FenixPeopleSet fenixPeopleSet = new FenixPeopleSet(peopleContents);
 	createPeople(fenixPeopleSet /*, fenixUnitMap */);
 
 	final String projectContents = FileUtils.readFile("projects.txt");
 	createProjects(projectContents, fenixPeopleSet);
 
 	LoadSuppliersData.loadData();
 
 	final String subProjectContents = FileUtils.readFile("subProjects.txt");
 	createSubProjects(subProjectContents);
 
 	final String cpvReferences = FileUtils.readFile("cpv.csv");
 	createCPVCodes(cpvReferences);
     }
 
 
     @Service
     private static void createCPVCodes(String cpvReferences) {
 	for (final String line : cpvReferences.split("\n")) {
 	    String[] lineArgs = line.split("\\|");
 	    try {
 		new CPVReference(lineArgs[0].replace("\"", ""), lineArgs[1].replace("\"", ""));
 	    } catch (DomainException e) {
 		// do not worry
 	    }
 	}
     }
 
     @Service
     private static void createUnits(final FenixUnitMap fenixUnitMap) {
 	for (final FenixUnit fenixUnit : fenixUnitMap) {
 	    createUnit(fenixUnit, fenixUnitMap);
 	}
     }
 
     @Service
     private static void createUnits(final CdUnitMap cdUnitMap) {
 	for (final CdUnit cdUnit : cdUnitMap) {
 	    createUnit(cdUnit);
 	}
     }
 
 
     @Service
     private static void createPeople(final FenixPeopleSet fenixPeopleSet /*, final FenixUnitMap fenixUnitMap*/) {
 	for (final FenixPerson fenixPerson : fenixPeopleSet) {
 	    final Person person = Person.createEmptyPerson();
 	    person.setUsername(fenixPerson.username);
 	    person.setName(fenixPerson.name);
 	    if (!fenixPerson.unitOids.isEmpty()) {
 		person.getOptions().setDisplayAuthorizationPending(Boolean.TRUE);
 		person.getOptions().setRecurseAuthorizationPendingUnits(Boolean.TRUE);
 	    }
 	    fenixPerson.person = person;
 	    /*
 	     * for (final Long oid : fenixPerson.unitOids) { final FenixUnit
 	     * fenixUnit = fenixUnitMap.get(oid); final Authorization
 	     * authorization = new Authorization(person, fenixUnit.unit);
 	     * authorization.setCanDelegate(Boolean.TRUE); }
 	     */
 	}
     }
 
     private static void createUnit(final FenixUnit fenixUnit, final FenixUnitMap fenixUnitMap) {
 	final CreateUnitBean createUnitBean = new CreateUnitBean(fenixUnit.getParentUnit(fenixUnitMap));
 	createUnitBean.setCostCenter(fenixUnit.costCenterCode);
 	createUnitBean.setName(fenixUnit.name);
 	fenixUnit.unit = Unit.createNewUnit(createUnitBean);
     }
 
     private static void createUnit(final CdUnit cdUnit) {
 	final Unit parentUnit = cdUnit.parentUnit == null ? null : cdUnit.parentUnit.unit;
 	final CreateUnitBean createUnitBean = new CreateUnitBean(parentUnit);
 	createUnitBean.setCostCenter(cdUnit.costCenterCode);
 	createUnitBean.setName(cdUnit.name);
 	cdUnit.unit = Unit.createNewUnit(createUnitBean);
     }
 
     @Service
     private static void createProjects(final String contents, final FenixPeopleSet fenixPeopleSet) {
 	for (final String line : contents.split("\n")) {
 	    final String[] parts = line.split("\t");
 	    final String projectCodeString = parts[0];
 	    final String costCenterString = parts[1];
 	    final String responsibleString = parts[2].trim();
 	    final String acronimo = parts[3].trim();
 
 	    final Unit costCenter = findCostCenter(costCenterString);
 	    if (costCenter != null) {
 		final Person responsible = findPerson(responsibleString, fenixPeopleSet);
 
 		final CreateUnitBean createUnitBean = new CreateUnitBean(costCenter);
 		createUnitBean.setProjectCode(projectCodeString);
 		createUnitBean.setName(acronimo);
 		final Unit unit = Unit.createNewUnit(createUnitBean);
 
 		if (responsible != null) {
 //		    final Authorization authorization = new Authorization(responsible, unit);
 //		    authorization.setCanDelegate(Boolean.FALSE);
 		}
 	    }
 	}
 	for (final String costCenterString : notFoundCostCenters) {
 	    System.out.println("Unable to find unit with cost center: [" + costCenterString + "]");
 	}
     }
 
     @Service
     private static void createSubProjects(final String contents) {
 	for (final String line : contents.split("\n")) {
 	    final String[] parts = line.split("\t");
 	    final String projectCode = parts[0];
 	    final String institution = parts[1].trim();
 	    final String description = parts[2].trim();
 
 	    final Project project = findProject(projectCode);
 	    if (project == null) {
 		System.out.println("No project found for: " + projectCode);
 	    } else {
 		new SubProject(project, institution + " - " + description);
 	    }
 	}
     }
 
     private static Project findProject(final String projectCode) {
 	for (final Unit unit : ExpenditureTrackingSystem.getInstance().getUnitsSet()) {
 	    if (unit instanceof Project) {
 		final Project project = (Project) unit;
 		if (projectCode.equals(project.getProjectCode())) {
 		    return project;
 		}
 	    }
 	}
 	return null;
     }
 
     private static final Set<String> cdCostCenters = new HashSet<String>();
     static {
 	cdCostCenters.add("9999");
 	cdCostCenters.add("0003");
     }
 
     private static final Set<String> notFoundCostCenters = new HashSet<String>();
 
     private static Unit findCostCenter(final String costCenterString) {
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
 	    notFoundCostCenters.add(costCenterString);
 	}
 	return unit;
     }
 
     private static Person findPerson(final String responsibleString, final FenixPeopleSet fenixPeopleSet) {
 	if (!responsibleString.isEmpty()) {
 	    for (final FenixPerson fenixPerson : fenixPeopleSet) {
 		final String username = fenixPerson.username;
 		if (username.startsWith("ist") && responsibleString.equals(username.substring(4))) {
 		    return fenixPerson.person;
 		}
 	    }
 	}
 	return null;
     }
 
 }
