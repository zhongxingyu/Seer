 package com.aminpy.phonebook.model.person;
 
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 
 @Entity
 @NamedQueries({
 		@NamedQuery(name = "MarriageStatus.findAll", query = "SELECT ms From MarriageStatus ms"),
 		@NamedQuery(name = "MarriageStatus.findByMarriageStatus", query = "SELECT ms FROM MarriageStatus ms WHERE ms.marriageStatus = :marriageStatus") })
 public class MarriageStatus {
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private long marriageStatusID;
 
 	private String marriageStatus;
 
 	@OneToMany(mappedBy = "marriageStatus")
 	private List<Person> personList;
 
 	public long getMarriageStatusID() {
 		return marriageStatusID;
 	}
 
 	public void setMarriageStatusID(long marriageStatusID) {
 		this.marriageStatusID = marriageStatusID;
 	}
 
 	public String getMarriageStatus() {
 		return marriageStatus;
 	}
 
 	public void setMarriageStatus(String marriageStatus) {
 		this.marriageStatus = marriageStatus;
 	}
 
 	public List<Person> getPersonList() {
 		return personList;
 	}
 
 	public void setPersonList(List<Person> personList) {
 		this.personList = personList;
 	}
 
 	@Override
 	public String toString() {
 		return this.marriageStatus;
 	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MarriageStatus))
			return false;
		else {
			MarriageStatus marriageStatus = (MarriageStatus) obj;
			return (this.marriageStatusID == marriageStatus.marriageStatusID);
		}
	}
 }
