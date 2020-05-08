 //@author: DK
 package edu.cmu.square.server.dao.model;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import edu.cmu.square.client.model.GwtQualityAttribute;
 import edu.cmu.square.client.model.GwtSoftwarePackage;
 
 /**
 * DK
 */
 @Entity
 @Table(name = "quality_attribute")
 public class QualityAttribute implements java.io.Serializable
 {	 
 	private static final long serialVersionUID = -2551908241213631843L;
 	private Integer id;
 	private String name;
 	private String description;
 	private Date dateCreated;
 	private Date dateModified;
	private Project project;
 	
 	public QualityAttribute() {
 	}
 
 
 	public QualityAttribute(GwtQualityAttribute gwtAttribute) {
 		if(gwtAttribute == null)
 		{
 			return;
 		}
 		this.id = gwtAttribute.getId();
 		this.name = gwtAttribute.getName();
 		this.description = gwtAttribute.getDescription();
 	}
	
 	public QualityAttribute(String name, String description, Project project, Date dateCreated, Date dateModified)
 	{
 		this.name = name;
 		this.description = description;
 		this.project = project;
 		this.dateCreated = dateCreated;
 		this.dateModified = dateModified;
 	}

 
 	@Id
 	@GeneratedValue(strategy = IDENTITY)
 	@Column(name = "id", unique = true, nullable = false)
 	public Integer getId() {
 		return this.id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	@Column(name = "name", nullable = false, length = 50)
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Column(name = "description", nullable = false, length = 250)
 	public String getDescription() {
 		return this.description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
	public void setProject(Project project) {
		this.project = project;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pid", nullable = false)
	public Project getProject() {
		return this.project;
	}

 	
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "date_created", nullable = false, length = 19)
 	public Date getDateCreated() {
 		return this.dateCreated;
 	}
 
 	public void setDateCreated(Date dateCreated) {
 		this.dateCreated = dateCreated;
 	}
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "date_modified", nullable = false, length = 19)
 	public Date getDateModified() {
 		return this.dateModified;
 	}
 
 	public void setDateModified(Date dateModified) {
 		this.dateModified = dateModified;
 	}
 	
 	
 	public GwtQualityAttribute createGwtQualityAttribute() {
 		GwtQualityAttribute gwtPackage = new GwtQualityAttribute();
 		gwtPackage.setDescription(this.description);
 		gwtPackage.setName(this.name);
 		gwtPackage.setId(this.id);
 		return gwtPackage;
 	}
 	
 	public String toString() {
 		return this.name;
 	}	
 }
 
 
 
 
