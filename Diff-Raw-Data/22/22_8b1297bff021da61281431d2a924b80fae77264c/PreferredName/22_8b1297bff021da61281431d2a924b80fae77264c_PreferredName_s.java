 package edu.wisc.portlet.preferred.form;
 
 import java.io.Serializable;
 
 import javax.validation.constraints.Max;
 import javax.validation.constraints.NotNull;
 
 public class PreferredName implements Serializable {
     private static final long serialVersionUID = 1L;
 
     private String firstName;
     
     private String middleName;
     
     private String pvi;
     
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getMiddleName() {
 		return middleName;
 	}
 
 	public void setMiddleName(String middleName) {
 		this.middleName = middleName;
 	}
 	
 	public void setPvi(String pvi) {
 		this.pvi = pvi;
 	}
 	
 	public String getPvi() {
 		return this.pvi;
 	}
 
 
 
 	public PreferredName() {
 		
 	}
 	
 	public PreferredName(String first, String middle) {
 		this.firstName = first;
 		this.middleName = middle;
 	}
 	
 	public PreferredName(String first, String middle, String pvi) {
 		this.firstName = first;
 		this.middleName = middle;
 		this.pvi = pvi;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((firstName == null) ? 0 : firstName.hashCode());
 		result = prime * result
 				+ ((middleName == null) ? 0 : middleName.hashCode());
		result = prime * result + ((pvi == null) ? 0 : pvi.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		PreferredName other = (PreferredName) obj;
 		if (firstName == null) {
 			if (other.firstName != null)
 				return false;
		} else if (!firstName.equals(other.firstName))
 			return false;
 		if (middleName == null) {
 			if (other.middleName != null)
 				return false;
		} else if (!middleName.equals(other.middleName))
			return false;
		if (pvi == null) {
			if (other.pvi != null)
				return false;
		} else if (!pvi.equals(other.pvi))
 			return false;
 		return true;
 	}
 	
 	

 }
