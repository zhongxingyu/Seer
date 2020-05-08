 import java.util.ArrayList;
 
 public class Scholar implements Comparable<Scholar>{
 	
 	private Name name = new Name();
 	
 	/** publications published by this scholar */
 	private ArrayList<Publication> publishedPapers = new ArrayList<Publication>();
 	
 	/** institutional affiliation(s) */
 	private ArrayList<String> affiliations = new ArrayList<String>();
 	
 	/** areas of research */
 	private ArrayList<String> researchAreas  = new ArrayList<String>();
 	
 	/** conferences organized by this scholar */
 	private ArrayList<Conference> conferencesOrganized  = new ArrayList<Conference>();
 	
 	/**
 	 * default constructor
 	 * 
 	 * @param nameWhole name of author
 	 */
 	public Scholar(String nameWhole) {
 		name.setName(nameWhole);
 	}
 	
 	/**
 	 * constructor with fields
 	 * 
 	 * @param nameWhole name of author
 	 * @param aff author affiliation
 	 * @param res research area
 	 */
 	public Scholar(String nameWhole, String aff, String res) {
 		name.setName(nameWhole);
 		addAffiliation(aff);
 		addResearchArea(res);
 	}
 	
	@Override
 	public String toString() {
 		return name.getNameFull();
 	}
 	
 	@Override
 	public int compareTo(Scholar arg0) {
 		return this.name.getNameFull().compareTo(arg0.name.getNameFull());
 	}
 	
 	public Name getName() {
 		return this.name;
 	}
 	
 	public void setName(Name name) {
 		this.name = name;
 	}
 	
 	public ArrayList<Publication> getPublishedPapers()
 	{
 		return publishedPapers;
 	}
 	
 	public void addPublishedPaper(Publication paper)
 	{
 		publishedPapers.add(paper);
 	}
 
 	public ArrayList<String> getAffiliations() {
 		return affiliations;
 	}
 
 	public void setAffiliations(ArrayList<String> affiliations) {
 		this.affiliations = affiliations;
 	}
 	
 	public void addAffiliation(String affiliation) {
 		this.affiliations.add(affiliation);
 	}
 	
 	public void removeAffiliation(String affiliation) {
 		this.affiliations.remove(affiliation);
 	}
 
 	public ArrayList<String> getResearchAreas() {
 		return researchAreas;
 	}
 
 	public void setResearchAreas(ArrayList<String> researchAreas) {
 		this.researchAreas = researchAreas;
 	}
 	
 	public void addResearchArea(String area) {
 		this.researchAreas.add(area);
 	}
 	
 	public void removeResearchArea(String area) {
 		this.researchAreas.remove(area);
 	}
 
 	public ArrayList<Conference> getConferencesOrganized() {
 		return conferencesOrganized;
 	}
 
 	public void setConferencesOrganized(ArrayList<Conference> conferencesOrganized) {
 		this.conferencesOrganized = conferencesOrganized;
 	}
 	
 	public void addConferencesOrganized(Conference conference) {
 		this.conferencesOrganized.add(conference);
 	}
 	
 	public void removeConferencesOrganized(Conference conference) {
 		this.conferencesOrganized.remove(conference);
 	}
 	
 }
