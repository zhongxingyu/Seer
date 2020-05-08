 package businessLayer;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.Size;
 
 @Entity
 @Table(
         uniqueConstraints=
             @UniqueConstraint(columnNames={"serialNumber"})
     )
 @NamedQueries({
 @NamedQuery(name="ChiefScientist.findAll",query="SELECT c FROM ChiefScientist c ORDER BY c.surname"),
 @NamedQuery(name="ChiefScientist.getBySerial", query="SELECT c FROM ChiefScientist c WHERE LOWER(c.serialNumber)= :number"),
@NamedQuery(name="ChiefScientist.getByDeptSerials", query="SELECT c FROM ChiefScientist c WHERE c.department IN :serials")
 
 })
 public class ChiefScientist {
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	private int id;
 	
 	@Size(min=1, max=100) 
 	private String name;
 	@Size(min=1, max=100) 
 	private String surname;
 	
 	
 	private String serialNumber;
 	
 	@ManyToOne
 	private Department department;
 	
 	
 	
 	public Department getDepartment() {
 		return department;
 	}
 
 	public void setDepartment(Department department) {
 		this.department = department;
 	}
 
 	public String getSerialNumber() {
 		return serialNumber;
 	}
 
 	public void setSerialNumber(String serialNumber) {
 		this.serialNumber = serialNumber;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getSurname() {
 		return surname;
 	}
 	public void setSurname(String surname) {
 		this.surname = surname;
 	}
 	public int getId() {
 		return id;
 	}
 	
 	public String getCompleteName(){
 		return this.surname + " " + this.name;
 	}
 	
 	
 
 	public void copy (ChiefScientist c) {
 		this.name = c.getName();
 		this.surname = c.getSurname();
 		this.serialNumber = c.getSerialNumber();
 		this.department = c.getDepartment();
 	}
 
 	@Override
 	public String toString() {
 		return "ChiefScientist [id=" + id + ", name=" + name + ", surname="
 				+ surname + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((serialNumber == null) ? 0 : serialNumber.hashCode());
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
 		ChiefScientist other = (ChiefScientist) obj;
 		if (serialNumber == null) {
 			if (other.serialNumber != null)
 				return false;
 		} else if (!serialNumber.equals(other.serialNumber))
 			return false;
 		return true;
 	}
 	
 	
 
 	
 	
 	
 	
 
 }
