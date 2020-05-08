 package cz.muni.fi.pa165.pujcovnastroju.entity;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 
 /**
  * 
  * @author Ondřej Güttner
  */
 @Entity
 public class Loan implements Serializable {
 
 	private static final long serialVersionUID = -517093082752147127L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Long id;
 
 	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE,
 			CascadeType.REFRESH })
 	@JoinColumn(name = "SYSTEMUSER_ID")
 	private SystemUser customer;
 
 	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE,
 			CascadeType.REFRESH })
 	@JoinTable(name="LOAN_MACHINES", joinColumns={@JoinColumn(name="LOAN_ID", referencedColumnName="id")},
 			inverseJoinColumns={@JoinColumn(name="MACHINE_ID", referencedColumnName="id")})
 	private List<Machine> machines;
 
 	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
 	private Date loanTime;
 
 	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
 	private Date returnTime;
 
 	@Column(nullable = false)
 	private LoanStateEnum loanState;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public SystemUser getCustomer() {
 		return customer;
 	}
 
 	public void setCustomer(SystemUser customer) {
 		this.customer = customer;
 	}
 
 	public Date getLoanTime() {
 		return loanTime;
 	}
 
 	public void setLoanTime(Date loanTime) {
 		this.loanTime = loanTime;
 	}
 
 	public List<Machine> getMachines() {
 		return machines;
 	}
 
 	public void setMachines(List<Machine> machines) {
 		this.machines = machines;
 	}
 
 	public Date getReturnTime() {
 		return returnTime;
 	}
 
 	public void setReturnTime(Date returnTime) {
 		this.returnTime = returnTime;
 	}
 
 	public LoanStateEnum getLoanState() {
 		return loanState;
 	}
 
 	public void setLoanState(LoanStateEnum loanState) {
 		this.loanState = loanState;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 0;
 		hash += (id != null ? id.hashCode() : 0);
 		return hash;
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		if (!(object instanceof Loan)) {
 			return false;
 		}
 		Loan other = (Loan) object;
 		if ((this.id == null && other.id != null)
 				|| (this.id != null && !this.id.equals(other.id))) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Loan [id=" + id + ", customer=" + customer + ", machines="
 				+ machines + ", loanTime=" + loanTime + ", returnTime="
 				+ returnTime + ", loanState=" + loanState + "]";
 	}
 
 	
 
 }
