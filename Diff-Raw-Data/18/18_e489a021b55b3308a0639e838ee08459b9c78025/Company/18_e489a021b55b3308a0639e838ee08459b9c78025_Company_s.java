 package businessLayer;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 @Entity
 @Table(
         uniqueConstraints=
             @UniqueConstraint(columnNames={"socialNumber"})
     )
 @NamedQueries({
 		@NamedQuery(name = "Company.findAll", query = "SELECT c FROM Company c ORDER BY c.name"),
 		@NamedQuery(name = "Company.findBySocialNumber", query = "SELECT c FROM Company c WHERE c.socialNumber =:number") })
 public class Company {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private int id;
 
 	@NotNull
 	@Size(min = 1, max = 1000)
 	private String name;
 
 	private String businessName;
 	private String legalResidence;
 	private String socialNumber;
	private int VatNumber;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public String getBusinessName() {
 		return businessName;
 	}
 
 	public void setBusinessName(String businessName) {
 		this.businessName = businessName;
 	}
 
 	public String getLegalResidence() {
 		return legalResidence;
 	}
 
 	public void setLegalResidence(String legalResidence) {
 		this.legalResidence = legalResidence;
 	}
 
 	public String getSocialNumber() {
 		return socialNumber;
 	}
 
 	public void setSocialNumber(String socialNumber) {
 		this.socialNumber = socialNumber;
 	}
 
	public int getVatNumber() {
 		return VatNumber;
 	}
 
	public void setVatNumber(int vatNumber) {
 		VatNumber = vatNumber;
 	}
 
 	@Override
 	public String toString() {
 		return "Company [name=" + name + ", businessName=" + businessName
 				+ ", legalResidence=" + legalResidence + ", socialNumber="
 				+ socialNumber + ", VatNumber=" + VatNumber + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
		result = prime * result + VatNumber;
 		result = prime * result
 				+ ((socialNumber == null) ? 0 : socialNumber.hashCode());
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
 		Company other = (Company) obj;
		if (VatNumber != other.VatNumber)
 			return false;
 		if (socialNumber == null) {
 			if (other.socialNumber != null)
 				return false;
 		} else if (!socialNumber.equals(other.socialNumber))
 			return false;
 		return true;
 	}
 
 }
