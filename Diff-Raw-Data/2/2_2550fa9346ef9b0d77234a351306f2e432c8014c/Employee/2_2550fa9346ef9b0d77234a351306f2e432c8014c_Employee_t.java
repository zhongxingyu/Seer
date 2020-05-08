 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.openhr.data;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedNativeQuery;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
import com.openhr.common.PayhumConstants;

 /**
  *
  * 
  */
 @Entity
 @Table(name = "employee", catalog = PayhumConstants.DATABASE_NAME, schema = "")
 @NamedQueries({
 		@NamedQuery(name = "Employee.findAllByComp", query = "SELECT e from Employee e, Department d, Branch b, Company c where e.deptId=d.id and d.branchId=b.id and b.companyId=c.id and c.id= ?"),
 		@NamedQuery(name = "Employee.findAll", query = "SELECT e from Employee e"),
 		@NamedQuery(name = "Employee.findById", query = "SELECT e FROM Employee e WHERE e.id = ?"),
 		@NamedQuery(name = "Employee.findByEmployeeId", query = "SELECT e FROM Employee e WHERE e.employeeId = ?"),
 		@NamedQuery(name = "Employee.findByFirstname", query = "SELECT e FROM Employee e WHERE e.firstname = ?"),
 		@NamedQuery(name = "Employee.findByMiddlename", query = "SELECT e FROM Employee e WHERE e.middlename = ?"),
 		@NamedQuery(name = "Employee.findByLastname", query = "SELECT e FROM Employee e WHERE e.lastname = ?"),
 		@NamedQuery(name = "Employee.findByBirthdate", query = "SELECT e FROM Employee e WHERE e.birthdate = ?"),
 		@NamedQuery(name = "Employee.findByDeptID", query = "SELECT e FROM Employee e WHERE e.deptId = ?"),
 		@NamedQuery(name = "Employee.findByHiredate", query = "SELECT e FROM Employee e WHERE e.hiredate = ?"),
 		@NamedQuery(name = "Employee.findActiveByDeptID", query = "SELECT e FROM Employee e WHERE e.status = 'ACTIVE' AND e.deptId = ?"),
 		@NamedQuery(name = "Employee.findAllActive", query = "SELECT e FROM Employee e, Department d, Branch b, Company c WHERE e.status = 'ACTIVE' AND e.deptId=d.id and d.branchId=b.id and b.companyId=c.id and c.id= ?"),
 		@NamedQuery(name = "Employee.findAllActiveByBranch", query = "SELECT e FROM Employee e, Department d WHERE e.status = 'ACTIVE' AND d.branchId = ? "
 				+ " AND e.deptId = d.id"),
 		@NamedQuery(name = "Employee.findInActiveByDeptIDAndDate", query = "SELECT e FROM Employee e WHERE e.status = 'IN ACTIVE' AND e.deptId = ? AND"
 				+ " MONTH(e.inactiveDate) = MONTH(?) AND YEAR(e.inactiveDate) = YEAR(?)"),
 		@NamedQuery(name = "Employee.findInActiveByDate", query = "SELECT e FROM Employee e, Department d, Branch b, Company c  WHERE e.status = 'IN ACTIVE' AND"
 				+ " MONTH(e.inactiveDate) = MONTH(?) AND YEAR(e.inactiveDate) = YEAR(?) AND e.deptId=d.id and d.branchId=b.id and b.companyId=c.id and c.id= ?"),
 		@NamedQuery(name = "Employee.findInActiveByDateAndBranch", query = "SELECT e FROM Employee e, Department d WHERE e.status = 'IN ACTIVE' AND"
 				+ " MONTH(e.inactiveDate) = MONTH(?) AND YEAR(e.inactiveDate) = YEAR(?)"
 				+ " AND d.branchId = ? AND e.deptId = d.id") })
 @NamedNativeQuery(name = "Employee.findLastId", query = "SELECT * FROM Employee WHERE Employee.id = (SELECT max(Employee.id) FROM EMPLOYEE)", resultClass = Employee.class)
 public class Employee implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Basic(optional = false)
 	@Column(name = "id", nullable = false)
 	private Integer id;
 	@Basic(optional = false)
 	@Column(name = "employeeId", nullable = false, length = 45)
 	private String employeeId;
 	@Basic(optional = false)
 	@Column(name = "firstname", nullable = false, length = 45)
 	private String firstname;
 	@Basic(optional = false)
 	@Column(name = "middlename", nullable = false, length = 45)
 	private String middlename;
 	@Basic(optional = false)
 	@Column(name = "lastname", nullable = false, length = 45)
 	private String lastname;
 	@Basic(optional = false)
 	@Column(name = "sex", nullable = false, length = 6)
 	private String sex;
 
 	@Basic(optional = false)
 	@Column(name = "photo_path", nullable = false, length = 55)
 	private String photo;
 
 	@Basic(optional = false)
 	@Column(name = "status", nullable = false, length = 20)
 	private String status;
 
 	@Basic(optional = false)
 	@Column(name = "emerContactName", nullable = false, length = 20)
 	private String emerContactName;
 
 	@Basic(optional = false)
 	@Column(name = "emerContactNo", nullable = false, length = 20)
 	private String emerContactNo;
 
 	@Basic(optional = false)
 	@Column(name = "birthdate", nullable = false)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date birthdate;
 	@Basic(optional = false)
 	@Column(name = "hiredate", nullable = false)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date hiredate;
 
 	@Basic(optional = false)
 	@Column(name = "inactiveDate")
 	private Date inactiveDate;
 
 	@JoinColumn(name = "positionId", referencedColumnName = "id", nullable = false)
 	@ManyToOne(optional = false)
 	private Position positionId;
 
 	@Basic(optional = false)
 	@Column(name = "married", nullable = false, length = 20)
 	private String married;
 
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "employeeId", fetch = FetchType.EAGER)
 	private List<EmpDependents> dependents;
 
 	@JoinColumn(name = "residentType", referencedColumnName = "id", nullable = false)
 	@ManyToOne(optional = false)
 	private TypesData residentType;
 
 	@JoinColumn(name = "deptId", referencedColumnName = "id", nullable = false)
 	@ManyToOne(optional = false)
 	private Department deptId;
 
 	@Basic(optional = false)
 	@Column(name = "empNationalID", nullable = false, length = 45)
 	private String empNationalID;
 
 	@Basic(optional = false)
 	@Column(name = "address", nullable = false, length = 90)
 	private String address;
 
 	@Basic(optional = false)
 	@Column(name = "phoneNo", nullable = false, length = 15)
 	private String phoneNo;
 
 	@Basic(optional = true)
 	@Column(name = "nationality", nullable = true, length = 55)
 	private String nationality;
 	
 	@Basic(optional = true)
 	@Column(name = "ppNumber", nullable = true, length = 15)
 	private String ppNumber;
 	
 	@Basic(optional = true)
 	@Column(name = "ppExpDate", nullable = true)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date ppExpDate;
 	
 	@Basic(optional = true)
 	@Column(name = "ppIssuePlace", nullable = true, length = 45)
 	private String ppIssuePlace;
 	
 	@JoinColumn(name = "currency", referencedColumnName = "id", nullable = false)
 	@ManyToOne(optional = false)
 	private TypesData currency;
 	
 	public String getEmpNationalID() {
 		return empNationalID;
 	}
 
 	public void setEmpNationalID(String empNationalID) {
 		this.empNationalID = empNationalID;
 	}
 
 	public Employee() {
 	}
 
 	public Employee(Integer id) {
 		this.id = id;
 	}
 
 	public Employee(Integer id, String employeeId, String firstname,
 			String middlename, String lastname, String sex, Date birthdate,
 			Date hiredate) {
 		this.id = id;
 		this.employeeId = employeeId;
 		this.firstname = firstname;
 		this.middlename = middlename;
 		this.lastname = lastname;
 		this.sex = sex;
 		this.birthdate = birthdate;
 		this.hiredate = hiredate;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getEmployeeId() {
 		return employeeId;
 	}
 
 	public void setEmployeeId(String employeeId) {
 		this.employeeId = employeeId;
 	}
 
 	public String getFirstname() {
 		return firstname;
 	}
 
 	public void setFirstname(String firstname) {
 		this.firstname = firstname;
 	}
 
 	public String getMiddlename() {
 		return middlename;
 	}
 
 	public void setMiddlename(String middlename) {
 		this.middlename = middlename;
 	}
 
 	public String getLastname() {
 		return lastname;
 	}
 
 	public void setLastname(String lastname) {
 		this.lastname = lastname;
 	}
 
 	public String getSex() {
 		return sex;
 	}
 
 	public void setSex(String sex) {
 		this.sex = sex;
 	}
 
 	public Date getBirthdate() {
 		return birthdate;
 	}
 
 	public void setBirthdate(Date birthdate) {
 		this.birthdate = birthdate;
 	}
 
 	public Date getHiredate() {
 		return hiredate;
 	}
 
 	public void setHiredate(Date hiredate) {
 		this.hiredate = hiredate;
 	}
 
 	public Position getPositionId() {
 		return positionId;
 	}
 
 	public void setPositionId(Position positionId) {
 		this.positionId = positionId;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 0;
 		hash += (id != null ? id.hashCode() : 0);
 		return hash;
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		// TODO: Warning - this method won't work in the case the id fields are
 		// not set
 		if (!(object instanceof Employee)) {
 			return false;
 		}
 		Employee other = (Employee) object;
 		if ((this.id == null && other.id != null)
 				|| (this.id != null && !this.id.equals(other.id))) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "com.openhr.data.Employee[id=" + id + "]";
 	}
 
 	public String getPhoto() {
 		return photo;
 	}
 
 	public void setPhoto(String photo) {
 		this.photo = photo;
 	}
 
 	public String getStatus() {
 		return status;
 	}
 
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	public TypesData getResidentType() {
 		return residentType;
 	}
 
 	public List<EmpDependents> getDependents() {
 		return this.dependents;
 	}
 
 	public boolean isMarried() {
 		return Boolean.parseBoolean(married);
 	}
 
 	public void setMarried(String married) {
 		this.married = married;
 	}
 
 	public void setDependents(List<EmpDependents> deps) {
 		this.dependents = deps;
 	}
 
 	public void setResidentType(TypesData resType) {
 		this.residentType = resType;
 	}
 
 	public Department getDeptId() {
 		return deptId;
 	}
 
 	public void setDeptId(Department did) {
 		this.deptId = did;
 	}
 
 	public Date getInactiveDate() {
 		return inactiveDate;
 	}
 
 	public void setInactiveDate(Date inactiveDate) {
 		this.inactiveDate = inactiveDate;
 	}
 
 	public String getEmerContactName() {
 		return emerContactName;
 	}
 
 	public void setEmerContactName(String emerContactName) {
 		this.emerContactName = emerContactName;
 	}
 
 	public String getEmerContactNo() {
 		return emerContactNo;
 	}
 
 	public void setEmerContactNo(String emerContactNo) {
 		this.emerContactNo = emerContactNo;
 	}
 	
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public String getPhoneNo() {
 		return phoneNo;
 	}
 
 	public void setPhoneNo(String phoneNo) {
 		this.phoneNo = phoneNo;
 	}
 
 	public String getNationality() {
 		return nationality;
 	}
 
 	public void setNationality(String nationality) {
 		this.nationality = nationality;
 	}
 
 	public String getPpNumber() {
 		return ppNumber;
 	}
 
 	public void setPpNumber(String ppNumber) {
 		this.ppNumber = ppNumber;
 	}
 
 	public Date getPpExpDate() {
 		return ppExpDate;
 	}
 
 	public void setPpExpDate(Date ppExpDate) {
 		this.ppExpDate = ppExpDate;
 	}
 
 	public String getPpIssuePlace() {
 		return ppIssuePlace;
 	}
 
 	public void setPpIssuePlace(String ppIssuePlace) {
 		this.ppIssuePlace = ppIssuePlace;
 	}
 
 	public TypesData getCurrency() {
 		return currency;
 	}
 
 	public void setCurrency(TypesData currency) {
 		this.currency = currency;
 	}
 	
 }
