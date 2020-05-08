 package org.chai.kevin.fct;
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import org.chai.kevin.Exportable;
 import org.chai.kevin.data.Sum;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.reports.ReportEntity;
 import org.chai.kevin.util.Utils;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Entity(name = "FctTargetOption")
 @Table(name = "dhsst_fct_target_option", uniqueConstraints={@UniqueConstraint(columnNames="code")})
 @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 public class FctTargetOption extends ReportEntity implements Exportable {
 
 	private Long id;	
 	private Sum sum;
 	private FctTarget target;	
	private String numberFormat = "#";
	private String percentageFormat = "#%";
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@Transient
 	public Type getType() {
 		return getSum().getType();
 	}
 	
 	@ManyToOne(targetEntity=Sum.class)
 	public Sum getSum() {
 		return sum;		
 	}
 	
 	public void setSum(Sum sum){
 		this.sum = sum;
 	}
 	
 	@ManyToOne(targetEntity=FctTarget.class)
 	public FctTarget getTarget() {
 		return target;
 	}
 	
 	public void setTarget(FctTarget target) {
 		this.target = target;
 	}
 	
 	@Transient
	public String getNumberFormat() {
		return numberFormat;
 	}
 
	@Transient
	public String getPercentageFormat() {
		return percentageFormat;
	}
	
 	@Override
 	public String toString() {
 		return "FctTargetOption[getId()=" + getId() + ", getCode()=" + getCode() + "]";
 	}
 
 	@Override
 	public String toExportString() {
 		return "[" + Utils.formatExportCode(getCode()) + "]";
 	}
 }
