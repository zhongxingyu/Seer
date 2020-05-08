 package org.tothought.entities;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "DEGREE")
 public class Degree {
 
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	@Column(name="DEGREE_ID")
 	private Integer degreeId;
 
 	@Column(name="INSTITUTION")
 	private String institution;
 	
 	@Column(name="PROGRAM")
 	private String program;
 	
 	@Column(name="EMPHASIS")
	private String empahsis;
 	
 	@Column(name="DEGREE_TYPE")
 	private String degreeType;
 	
 	@Column(name="GPA")
 	private String gpa;
 	
 	@Column(name="START_DATE")
 	private Date startDate;
 	
 	@Column(name="END_DATE")
 	private Date endDate;
 	
 	@Column(name="IS_PRESENT", columnDefinition="tinyint")
 	private boolean isPresent;
 
 	public Integer getDegreeId() {
 		return degreeId;
 	}
 
 	public void setDegreeId(Integer degreeId) {
 		this.degreeId = degreeId;
 	}
 
 	public String getInstitution() {
 		return institution;
 	}
 
 	public void setInstitution(String institution) {
 		this.institution = institution;
 	}
 
 	public String getProgram() {
 		return program;
 	}
 
 	public void setProgram(String program) {
 		this.program = program;
 	}
 
	public String getEmpahsis() {
		return empahsis;
 	}
 
	public void setEmpahsis(String empahsis) {
		this.empahsis = empahsis;
 	}
 
 	public String getDegreeType() {
 		return degreeType;
 	}
 
 	public void setDegreeType(String degreeType) {
 		this.degreeType = degreeType;
 	}
 
 	public String getGpa() {
 		return gpa;
 	}
 
 	public void setGpa(String gpa) {
 		this.gpa = gpa;
 	}
 
 	public Date getStartDate() {
 		return startDate;
 	}
 
 	public void setStartDate(Date startDate) {
 		this.startDate = startDate;
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(Date endDate) {
 		this.endDate = endDate;
 	}
 
	public boolean isPresent() {
 		return isPresent;
 	}
 
	public void setPresent(boolean isPresent) {
 		this.isPresent = isPresent;
 	}
 
 }
