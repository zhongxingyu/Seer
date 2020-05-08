 package biz.thaicom.eBudgeting.models.bgt;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import biz.thaicom.eBudgeting.models.hrx.Organization;
 import biz.thaicom.eBudgeting.models.pln.Objective;
 
 import com.fasterxml.jackson.annotation.JsonIdentityInfo;
 import com.fasterxml.jackson.annotation.ObjectIdGenerators;
 
 @Entity
 @Table(name="BGT_BUDGETPROPOSAL")
 @SequenceGenerator(name="BGT_BUDGETPROPOSAL_SEQ", sequenceName="BGT_BUDGETPROPOSAL_SEQ", allocationSize=1)
 @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
 public class BudgetProposal implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6691460847982541290L;
 
 	
 	// Field
 	@Id
 	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="BGT_BUDGETPROPOSAL_SEQ")
 	private Long id;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="BUDGETTYPE_BGT_BUDGETTYPE_ID")
 	private BudgetType budgetType;
 	
 	
 	@Basic
 	private String name;
 	
 	@Basic
 	private Long amountRequest;
 	
 	@Basic
 	private Long amountRequestNext1Year;
 	
 	@Basic
 	private Long amountRequestNext2Year;
 	
 	@Basic
 	private Long amountRequestNext3Year;
 	
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="ORGANIZATION_ID")
 	private Organization owner;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="OBJECTIVE_ID")
 	private Objective forObjective;
 	
 	@OneToMany(mappedBy="proposal", fetch=FetchType.LAZY)
 	private List<AllocationRecord> allocationRecords;	
 
 	@OneToMany(mappedBy="proposal", fetch=FetchType.LAZY)
 	private List<ProposalStrategy> proposalStrategies;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public BudgetType getBudgetType() {
 		return budgetType;
 	}
 
 	public void setBudgetType(BudgetType budgetType) {
 		this.budgetType = budgetType;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Long getAmountRequest() {
 		return amountRequest;
 	}
 
 	public void setAmountRequest(Long amountRequest) {
 		this.amountRequest = amountRequest;
 	}
 
 	public Organization getOwner() {
 		return owner;
 	}
 
 	public void setOwner(Organization owner) {
 		this.owner = owner;
 	}
 
 	public Objective getForObjective() {
 		return forObjective;
 	}
 
 	public void setForObjective(Objective forObjective) {
 		this.forObjective = forObjective;
 	}
 
 	public List<AllocationRecord> getAllocationRecords() {
 		return allocationRecords;
 	}
 
 	public void setAllocationRecords(List<AllocationRecord> allocationRecords) {
 		this.allocationRecords = allocationRecords;
 	}
 
 	public List<ProposalStrategy> getProposalStrategies() {
 		return proposalStrategies;
 	}
 
 	public void setProposalStrategies(List<ProposalStrategy> proposalStrategies) {
 		this.proposalStrategies = proposalStrategies;
 	}
 
 	public void addAmountRequest(Long amount) {
 		if(this.getAmountRequest() == null) {
 			this.amountRequest = amount;
 		} else {
 			this.amountRequest += amount;
 		}
 	}
 
 	public Long getAmountRequestNext1Year() {
 		return amountRequestNext1Year;
 	}
 
 	public void setAmountRequestNext1Year(Long amountRequestNext1Year) {
 		this.amountRequestNext1Year = amountRequestNext1Year;
 	}
 
 	public Long getAmountRequestNext2Year() {
 		return amountRequestNext2Year;
 	}
 
 	public void setAmountRequestNext2Year(Long amountRequestNext2Year) {
 		this.amountRequestNext2Year = amountRequestNext2Year;
 	}
 
 	public Long getAmountRequestNext3Year() {
 		return amountRequestNext3Year;
 	}
 
 	public void setAmountRequestNext3Year(Long amountRequestNext3Year) {
 		this.amountRequestNext3Year = amountRequestNext3Year;
 	}
 
 	public void addAmountRequestNext1Year(Long amountRequestNext1Year) {
 		if(this.getAmountRequestNext1Year() == null) {
 			this.amountRequestNext1Year = amountRequestNext1Year;
 		} else {
 			this.amountRequestNext1Year += amountRequestNext1Year;
 		}
 	}
 	
 	public void addAmountRequestNext2Year(Long amountRequestNext2Year) {
		if(this.getAmountRequestNext1Year() == null) {
 			this.amountRequestNext2Year = amountRequestNext2Year;
 		} else {
 			this.amountRequestNext2Year += amountRequestNext2Year;
 		}
 	}	
 	public void addAmountRequestNext3Year(Long amountRequestNext3Year) {
		if(this.getAmountRequestNext1Year() == null) {
 			this.amountRequestNext3Year = amountRequestNext1Year;
 		} else {
 			this.amountRequestNext3Year += amountRequestNext3Year;
 		}
 	}
 }
