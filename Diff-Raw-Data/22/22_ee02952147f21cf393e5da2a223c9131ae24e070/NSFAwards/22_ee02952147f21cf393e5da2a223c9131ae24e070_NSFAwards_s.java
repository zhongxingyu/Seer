 package edu.utep.cybershare.vlc.sources.nsf;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import edu.utep.cybershare.vlc.ontology.Discipline;
 import edu.utep.cybershare.vlc.ontology.Institution;
 import edu.utep.cybershare.vlc.ontology.Person;
 import edu.utep.cybershare.vlc.sources.ProjectSource;
 
 public class NSFAwards extends ProjectSource {
 	
 	private static final String CACHED_AWARD_DATA = "./cached-award-data/";
 	private static final String NSF = CACHED_AWARD_DATA + "NSF/";
 
 	private static final File[] AWARDS_NSF = new File[]{
 		new File(NSF + "biodiversity-forecasting.xml"),
 		new File(NSF + "data-curation.xml"),
 		new File(NSF + "data-integration.xml"),
 		new File(NSF + "data-management.xml"),
 		new File(NSF + "geoepidemiology.xml"),
 		new File(NSF + "knowledge-representation.xml"),
 		new File(NSF + "ontology.xml"),
 		new File(NSF + "semantic-web.xml"),
 		new File(NSF + "sensor-data.xml"),
 		new File(NSF + "sensor-networks.xml"),
 		new File(NSF + "spatial-epidemiology.xml"),
 		new File(NSF + "sustainability-science.xml"),
 		new File(NSF + "visual-analytics.xml"),
 		new File(NSF + "visualization.xml"),
 		new File(NSF + "water-models.xml")};
 	
 	private int counter = 0;
 
 	
 	public NSFAwards() {
 		DocumentBuilderFactory dbFactory;
 		DocumentBuilder dBuilder;
 		Document awardsDoc;
 		try{
 			for(File awardFile : AWARDS_NSF){
 				dbFactory = DocumentBuilderFactory.newInstance();
 				dBuilder = dbFactory.newDocumentBuilder();
 				awardsDoc = dBuilder.parse(awardFile);	 
 				awardsDoc.getDocumentElement().normalize();
 				populateProjects(awardsDoc);
 				dBuilder.reset();
 			}
 		}catch(Exception e){e.printStackTrace();}
 		
 		System.out.println("Total Projects Processed: " + counter);
 	}
 	
 	private List<String> getCoInvestigators(Element awardElement){
		String coiName = ProjectSource.NULL_PERSON_PROPERNAME;
 		NodeList pis = awardElement.getElementsByTagName("Co-PIName");
 		List<String> pisList = NSFAwardsUtils.nodeListConverter(pis);
 		
 		if(pisList.size() == 0){
 			pisList = new ArrayList<String>();
 			pisList.add(coiName);
 		}
 		
 		return pisList;
 	}
 	
 	private String getProgram(Element awardElement){
 		String program = ProjectSource.NULL_DISCIPLINE;
 		NodeList programs = awardElement.getElementsByTagName("Program");
 		if(programs.getLength() > 0)
 			program = programs.item(0).getTextContent();
 		
 		return program;
 	}
 	
 	private String getPI(Element awardElement){
		String pi = ProjectSource.NULL_PERSON_PROPERNAME;
 		NodeList pis = awardElement.getElementsByTagName("PrincipalInvestigator");
 		if(pis.getLength() > 0)
 			pi = pis.item(0).getTextContent();
 		
 		return pi;
 	}
 	
 	private void populateProjects(Document awardsDoc){
 		NodeList awardsList = awardsDoc.getElementsByTagName("Award");
 			
 		for (int i = 0; i < awardsList.getLength(); i++) {
 			
 			counter ++;
 			
 			Node awardNode = awardsList.item(i);
 			
 			if (awardNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element awardElement = (Element) awardNode;
 				
 				String piName = getPI(awardElement);
 				String disciplineName = getProgram(awardElement);
 				String title = awardElement.getElementsByTagName("Title").item(0).getTextContent();
 				String abstractText = awardElement.getElementsByTagName("Abstract").item(0).getTextContent();				
 				String startDateString = awardElement.getElementsByTagName("StartDate").item(0).getTextContent();
 				String endDateString = awardElement.getElementsByTagName("ExpirationDate").item(0).getTextContent();
 				
 				GregorianCalendar startDate = NSFAwardsUtils.getDate(startDateString);
 				GregorianCalendar endDate = NSFAwardsUtils.getDate(endDateString);
 				
 				Discipline discipline = this.getDiscipline(disciplineName);
 				Institution institution = this.getInstitution(awardElement);
 				List<Person> coPrincipalInvestigators = this.getCoPrincipalInvestigators(awardElement, discipline, institution);				
 				Person hasPrincipalInvestigator = getPerson(NSFAwardsUtils.getFirstName(piName), NSFAwardsUtils.getLastName(piName), discipline, institution);
 
 				addProject(hasPrincipalInvestigator, coPrincipalInvestigators, title, abstractText, startDate, endDate);
 			}
 		}
 	}
 	
 	private List<Person> getCoPrincipalInvestigators(Element awardElement, Discipline discipline, Institution institution){
 		List<String> coiNames = getCoInvestigators(awardElement);
 
 		ArrayList<Person> coPrincipalInvestigators = new ArrayList<Person>();
 		Person hasCoPrincipalInvestigator;
 		for(String coiName : coiNames){
 			hasCoPrincipalInvestigator = getPerson(NSFAwardsUtils.getFirstName(coiName), NSFAwardsUtils.getLastName(coiName), discipline, institution);
 			coPrincipalInvestigators.add(hasCoPrincipalInvestigator);				
 		}
 		
 		return coPrincipalInvestigators;
 	}
 	
 	private Institution getInstitution(Element awardElement){
 		String institutionName = awardElement.getElementsByTagName("Organization").item(0).getTextContent();		
 		String city = awardElement.getElementsByTagName("OrganizationCity").item(0).getTextContent();		
 		String state = awardElement.getElementsByTagName("OrganizationState").item(0).getTextContent();				
 		String zipCode = awardElement.getElementsByTagName("OrganizationZip").item(0).getTextContent();
 		String address = awardElement.getElementsByTagName("OrganizationStreet").item(0).getTextContent();		
 
 		return getInstitution(institutionName, address, city, state, zipCode);
 	}
 }
