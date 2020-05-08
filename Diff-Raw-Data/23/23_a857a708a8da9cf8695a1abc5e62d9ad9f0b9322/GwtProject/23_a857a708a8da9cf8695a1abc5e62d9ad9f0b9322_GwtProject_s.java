 /**
  * 
  */
 package edu.cmu.square.client.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author kaalpurush
  * 
  */
 public class GwtProject implements Serializable {
 	
 	
 	private static final long serialVersionUID = -1722355937492472171L;
 	private Integer id=-1;
 	private String name;
 	private boolean lite;
 	private boolean privacy;
 	private boolean security;
 	private InspectionStatus inspectionStatus = null;
 	private int securityTechniqueID=-1;//Default value used in select security technique
 	private String securityRational;
 	private GwtInspectionTechnique inspectionTechniqueID;//Placeholder for inspection technique
 	private int inspectionCompletionStatus = 0; // Default inspection is not complete
 	private List<GwtStep> steps = new ArrayList<GwtStep>();
 	private GwtRole currentRole;
 	private GwtUser leadRequirementEngineer;
 	private GwtAsquareCase cases;
 	
 
 	public GwtProject(Integer id)
 	{
		System.out.println("Cons1");
 		this.id=id;
 	}
 	public GwtProject(Integer id, String projectName, int leadRequirementEngineer)
 	{
		System.out.println("Cons2");
 		this.id=id;
 		this.name= projectName;
 		this.leadRequirementEngineer= new GwtUser(leadRequirementEngineer);
 	}
 	public GwtProject(Integer id, String projectName, int leadRequirementEngineer, int cases)
 	{
		System.out.println("Cons3");
 		this.id=id;
 		this.name= projectName;
 		this.leadRequirementEngineer= new GwtUser(leadRequirementEngineer);
 		this.cases = new GwtAsquareCase(cases);
 	}
 	public GwtProject()
	{System.out.println("Cons4");
 		
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public boolean isLite() {
 		return lite;
 	}
 
 	public void setLite(boolean lite) {
 		this.lite = lite;
 	}
 
 	public boolean isPrivacy() {
 		return privacy;
 	}
 
 	public void setPrivacy(boolean privacy) {
 		this.privacy = privacy;
 	}
 
 	public boolean isSecurity() {
 		return security;
 	}
 
 	public void setSecurity(boolean security) {
 		this.security = security;
 	}
 
 
 	public void setSecurityTechniqueID(int securityTechniqueID) {
 		this.securityTechniqueID = securityTechniqueID;
 	}
 
 	public int getSecurityTechniqueID() {
 		return securityTechniqueID;
 	}
 
 	public void setSecurityRational(String securityRational) {
 		this.securityRational = securityRational;
 	}
 
 	public String getSecurityRational() {
 		return securityRational;
 	}
 
 	public void setInspectionTechniqueID(GwtInspectionTechnique inspectionTechniqueID)
 	{
 		this.inspectionTechniqueID = inspectionTechniqueID;
 	}
 
 	public GwtInspectionTechnique getInspectionTechniqueID()
 	{
 		return inspectionTechniqueID;
 	}
 
 	public void setInspectionCompletionStatus(int inspectionCompletionStatus)
 	{
 		this.inspectionCompletionStatus = inspectionCompletionStatus;
 	}
 
 	public int getInspectionCompletionStatus()
 	{
 		return inspectionCompletionStatus;
 	}
 	/**
 	 * @param inspectionStatus the inspectionStatus to set
 	 */
 	public void setInspectionStatus(InspectionStatus inspectionStatus)
 	{
 		this.inspectionStatus = inspectionStatus;
 	}
 	/**
 	 * @return the inspectionStatus
 	 */
 	public InspectionStatus getInspectionStatus()
 	{
 		return inspectionStatus;
 	}
 
 	public List<GwtStep> getSteps()
 	{
 		return steps;
 	}
 	public void setSteps(List<GwtStep> steps)
 	{
 		this.steps = steps;
 	}
 	public GwtRole getCurrentRole()
 	{
 		return currentRole;
 	}
 	public void setCurrentRole(GwtRole currentRole)
 	{
 		this.currentRole = currentRole;
 	}
 	public void setLeadRequirementEngineer(GwtUser leadRequirementEngineer)
 	{
 		this.leadRequirementEngineer = leadRequirementEngineer;
 	}
 	public GwtUser getLeadRequirementEngineer()
 	{
 		return leadRequirementEngineer;
 	}
 	
 	public void setCases(GwtAsquareCase cases)
 	{
 		this.cases = cases;
 	}
 	public GwtAsquareCase getCases()
 	{
 		return cases;
 	}
 	
 	
 	public boolean isInDatabase()
 	{
 		return this.id != -1;
 	}
 
 }
