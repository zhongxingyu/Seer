 package org.gsoft.openserv.domain.amortization;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Transient;
 
 import org.gsoft.openserv.domain.OpenServDomainObject;
 
 @Entity
 public class AmortizationSchedule extends OpenServDomainObject {
 	private static final long serialVersionUID = -591660710764385964L;
 	private Long amortizationScheduleID;
 	private Date creationDate;
	private Date effectiveDate;
 	private List<LoanAmortizationSchedule> loanAmortizations;
 
 	public void addLoanAmortizationSchedule(LoanAmortizationSchedule loanAmortization){
 		if(this.loanAmortizations==null){
 			loanAmortizations = new ArrayList<LoanAmortizationSchedule>();
 		}
 		loanAmortizations.add(loanAmortization);
 		loanAmortization.setAmortizationSchedule(this);
 	}
 	
 	@Id
 	@GeneratedValue( strategy=GenerationType.AUTO)
 	public Long getAmortizationScheduleID() {
 		return amortizationScheduleID;
 	}
 
 	public void setAmortizationScheduleID(Long amortizationScheduleID) {
 		this.amortizationScheduleID = amortizationScheduleID;
 	}
 
 	public Date getCreationDate() {
		return (creationDate==null)?null:(Date)creationDate.clone();
 	}
 	
 	public void setCreationDate(Date creationDate) {
		this.creationDate = (creationDate==null)?null:(Date)creationDate.clone();
 	}
 	
 	public Date getEffectiveDate() {
		return (effectiveDate==null)?null:(Date)effectiveDate.clone();
 	}
 	
 	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = (effectiveDate==null)?null:(Date)effectiveDate.clone();
 	}
 	
 	@OneToMany(mappedBy="amortizationSchedule", cascade=CascadeType.ALL)
 	public List<LoanAmortizationSchedule> getLoanAmortizationSchedules() {
 		return loanAmortizations;
 	}
 
 	public void setLoanAmortizationSchedules(List<LoanAmortizationSchedule> loanAmortizations) {
 		this.loanAmortizations = loanAmortizations;
 	}
 
 	@Override
 	@Transient
 	public Long getID() {
 		return this.getAmortizationScheduleID();
 	}
 }
