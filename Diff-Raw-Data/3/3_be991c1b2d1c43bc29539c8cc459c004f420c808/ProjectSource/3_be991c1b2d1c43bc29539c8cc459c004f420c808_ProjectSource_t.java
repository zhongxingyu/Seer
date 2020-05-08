 package edu.utep.cybershare.vlc.sources;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Hashtable;
 import java.util.List;
 
 import edu.utep.cybershare.vlc.ontology.Discipline;
 import edu.utep.cybershare.vlc.ontology.Institution;
 import edu.utep.cybershare.vlc.ontology.Person;
 import edu.utep.cybershare.vlc.ontology.Project;
 public abstract class ProjectSource {
 		
 	private Hashtable<String,Person> people;
 	private Hashtable<String,Project> projects;
 	private Hashtable<String,Institution> institutions;
 	private Hashtable<String,Discipline> disciplines;
 	
 	public static final String NULL_INSTITUTION = "null-institution";
 	public static final String NULL_DISCIPLINE = "null-discipline";
 	public static final String NULL_NAME = "null-name";
 	public static final String NULL_PERSON_PROPERNAME = NULL_NAME + " " + NULL_NAME;
 	
 	public ProjectSource(){
 		projects = new Hashtable<String,Project>();	
 		people = new Hashtable<String,Person>();
 		institutions = new Hashtable<String,Institution>();
 		disciplines = new Hashtable<String,Discipline>();
 		
 		addNullDiscipline();
 		addNullPerson();
 		addNullInstitution();
 	}
 		
 	private void addNullPerson(){
 		Person nullPerson = new Person();
 		nullPerson.setHasFirstName(NULL_NAME);
 		nullPerson.setHasLastName(NULL_NAME);
 		people.put(nullPerson.getProperName(), nullPerson);
 	}
 	
 	private void addNullDiscipline(){
 		Discipline nullDiscipline = new Discipline();
 		nullDiscipline.setHasName(NULL_NAME);
 		disciplines.put(NULL_DISCIPLINE, nullDiscipline);
 	}
 
 	private void addNullInstitution(){
 		Institution nullInstitution = new Institution();
 		nullInstitution.setHasName(NULL_NAME);
 		institutions.put(NULL_INSTITUTION, nullInstitution);
 	}
 	
 	public List<Project> getProjects(){
 		return new ArrayList<Project>(projects.values());
 	}
 	
 	public List<Person> getPeople(){
 		return new ArrayList<Person>(people.values());
 	}
 	
 	public List<Discipline> getDisciplines(){
 		return new ArrayList<Discipline>(disciplines.values());
 	}
 	
 	public List<Institution> getInstitutions(){
 		return new ArrayList<Institution>(institutions.values());
 	}
 
 	protected void addProject(
 			Person hasPrincipalInvestigator,
 			List<Person> hasCoPrincipalInvestigators,
 			String hasTitle,
 			String hasAbstract,
 			GregorianCalendar hasStartDate_Funding,
 			GregorianCalendar hasEndDate_Funding){
 		
 		Project project = projects.get(hasTitle);
 		
 		if(project == null){
 			project = new Project();
 			project.setHasPrincipalInvestigator(hasPrincipalInvestigator);
 			
 			for(Person coPI : hasCoPrincipalInvestigators)
 				project.addHasCoPrincipalInvestigator(coPI);
 			
 			project.setHasTitle(hasTitle);
 			project.setHasAbstract(hasAbstract);
 			project.setHasStartDate_Funding(hasStartDate_Funding);
 			project.setHasEndDate_Funding(hasEndDate_Funding);
 			projects.put(hasTitle, project);
 		}
		else
			for(Person coPI : hasCoPrincipalInvestigators)
				project.addHasCoPrincipalInvestigator(coPI);
 	}
 
 	protected Discipline getDiscipline(String disciplineName){
 		Discipline discipline = disciplines.get(disciplineName);
 		if(discipline == null){
 			discipline = new Discipline();
 			discipline.setHasName(disciplineName);
 			disciplines.put(disciplineName, discipline);
 		}
 		return discipline;
 	}
 	
 	protected Institution getInstitution(
 			String institutionName,
 			String address,
 			String city,
 			String state,
 			String zipCode){
 		Institution institution = institutions.get(institutionName);
 		if(institution == null){
 			institution = new Institution();
 			institution.setHasName(institutionName);
 			institution.setHasAddress(address);
 			institution.setHasCity(city);
 			institution.setHasState(state);
 			institution.setHasZipCode(zipCode);
 			institutions.put(institutionName, institution);
 		}
 		return institution;
 	}
 	
 	protected Person getPerson(String firstName, String lastName, Discipline discipline, Institution institution){
 		Person person = people.get(lastName + ", " + firstName);
 		if(person == null){
 			person = new Person();
 			person.setHasFirstName(firstName);
 			person.setHasLastName(lastName);
 			person.addHasDiscipline(discipline);
 			person.addAffiliatedWithInstitution(institution);
 			people.put(person.getProperName(), person);
 		}
 		return person;
 	}
 	
 	@Override
 	public String toString(){
 		int i = 0;
 		List<Project> projects = getProjects();
 		String projectListing = "--- Projects ---\n";
 		for(Project project : projects){
 			System.out.println(i ++);
 			projectListing += "project: " + project + "\n\n";
 		}
 		
 		return projectListing;
 	}
 }
