 package com.openhr.data;
 
 import java.io.Serializable;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedNativeQuery;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "department", catalog = "payhumrepo", schema = "")
 @NamedQueries({
     @NamedQuery(name = "Department.findAll", query = "FROM Department db"),
    @NamedQuery(name = "Department.findByBrachId", query = "FROM Department db WHERE branchId = ?"),
     @NamedQuery(name = "Department.findById", query = "FROM Department db WHERE id = ?"),
     @NamedQuery(name = "Department.findByName", query = "SELECT db FROM Department db WHERE db.deptname = ?")})
     @NamedNativeQuery(name = "Department.findLastId", query = "SELECT * FROM Department WHERE Department.id = (SELECT max(Department.id) FROM Department)",
 	resultClass=Department.class)
 
 public class Department implements Serializable {
 	private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id", nullable = false)
     private Integer id;
     
     @Basic(optional = false)
     @Column(name = "deptname", nullable = false, length = 45)
     private String deptname;
   
 
     @JoinColumn(name = "branchId", referencedColumnName = "id", nullable = false)
     @ManyToOne(optional = false)
     private Branch branchId;
      
     
     public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getDeptname() {
 		return deptname;
 	}
 
 	public void setDeptname(String deptname) {
 		this.deptname = deptname;
 	}
 
 	public Branch getBranchId() {
 		return branchId;
 	}
 
 	public void setBranchId(Branch branchId) {
 		this.branchId = branchId;
 	}
 
 	@Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Department)) {
             return false;
         }
         Department other = (Department) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.openhr.data.Department[id=" + id + "]";
     }
 }
