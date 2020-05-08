 package edu.cmu.square.client.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A class that represents a requirements a requirement
  */
 public class GwtRequirement implements Serializable, Comparable<GwtRequirement>
 {
 	private static final long serialVersionUID = 7563508736259459013L;
 	private Integer id;             
 	private String description;             
 	private String title;                
 	private boolean privacy =false;
 	private boolean security=true;
 	private List<GwtSubGoal> subGoals= new ArrayList<GwtSubGoal>();
 	private List<GwtRisk> risks = new ArrayList<GwtRisk>();
 	private List<GwtArtifact> artifacts = new ArrayList<GwtArtifact>();
 	private List<GwtCategory> categories = new ArrayList<GwtCategory>();
 	private int priority=0;              
	private int projectID;
		
	private String status="";
 	
 	
 	public Integer getId() 
 	{
 		return id;
 	}
 	
 	public void setId(Integer id)
 	{
 		this.id = id;
 	}
 	
 	public String getDescription()
 	{
 		return description;
 	}
 	
 	
 	public void setDescription(String description) 
 	{
 		this.description = description;
 	}
 	
 	
 	public String getTitle()
 	{
 		return title;
 	}
 	
 	
 	public void setTitle(String title)
 	{
 		this.title = title;
 	}
 	
 	
 	public boolean isPrivacy() 
 	{
 		return privacy;
 	}
 	
 	
 	public void setPrivacy(boolean privacy) 
 	{
 		this.privacy = privacy;
 	}
 	
 	
 	public boolean isSecurity()
 	{
 		return security;
 	}
 	
 	
 	public void setSecurity(boolean security) 
 	{
 		this.security = security;
 	}
 	
 	
 	public int getPriority()
 	{
 		return priority;
 	}
 	
 	
 	public void setPriority(int priority)
 	{
 		this.priority = priority;
 	}
 	
 	public String getStatus()
 	{
 		return status;
 	}
 	public void setStatus(String status)
 	{
 		this.status = status;
 	}
 	
 	public int getProjectID()
 	{
 		return projectID;
 	}
 	
 	
 	public void setProjectID(int projectID) 
 	{
 		this.projectID = projectID;
 	}
 
 	public void setSubGoals(List<GwtSubGoal> subGoals)
 	{
 		this.subGoals = subGoals;
 	}
 
 	public List<GwtSubGoal> getSubGoals()
 	{
 		return subGoals;
 	}
 
 	public void setRisks(List<GwtRisk> risks)
 	{
 		this.risks = risks;
 	}
 	
	
 
 	public List<GwtRisk> getRisks()
 	{
 		return risks;
 	}
 
 	
 	public int compareTo(GwtRequirement otherRequirement) {
 		if (this.id <= 0 || otherRequirement.id <= 0) {
 			return 0;
 		}
 		return this.id.compareTo(otherRequirement.id);
 	}
 	   public boolean equals(Object obj) {
 	       if (this == obj)
 	           return true;
 	       if (obj == null)
 	           return false;
 	       if (getClass() != obj.getClass())
 	           return false;
 	       final GwtRequirement other = ( GwtRequirement) obj;
 	       if (this.id.intValue() != other.getId().intValue())
 	           return false;
 	       return true;
 	   }
 	public void setCategories(List<GwtCategory> categories)
 	{
 		this.categories = categories;
 	}
 
 	public List<GwtCategory> getCategories()
 	{
 		return categories;
 	}
 
 	public void setArtifacts(List<GwtArtifact> artifacts)
 	{
 		this.artifacts = artifacts;
 	}
 
 	public List<GwtArtifact> getArtifacts()
 	{
 		return artifacts;
 	} 
 
 
 }
