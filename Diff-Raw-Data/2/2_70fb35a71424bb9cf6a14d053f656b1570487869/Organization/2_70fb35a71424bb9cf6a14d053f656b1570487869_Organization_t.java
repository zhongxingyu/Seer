 package no.f12.jzx.weboo.domain;
 
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 public class Organization {
 
 	@NotEmpty
 	private String name;
 
 	@NotNull
 	@Valid
 	private OrganizationNumber organizationNumber;
 	private Long id;
 
 	public Organization(){
 		
 	}
 
 	public Organization(OrganizationNumber organizationNumber, String organizationName) {
 		this.organizationNumber = organizationNumber;
 		this.name = organizationName;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public OrganizationNumber getOrganizationNumber() {
 		return this.organizationNumber;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setOrganizationNumber(OrganizationNumber number) {
 		this.organizationNumber = number;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Long getId() {
 		return this.id;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((organizationNumber == null) ? 0 : organizationNumber.hashCode());
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
 		Organization other = (Organization) obj;
 		if (organizationNumber == null) {
 			if (other.organizationNumber != null)
 				return false;
 		} else if (!organizationNumber.equals(other.organizationNumber))
 			return false;
 		return true;
 	}
 
 }
