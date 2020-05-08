 package org.chai.kevin.fct;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Exportable;
import org.chai.kevin.data.Calculation;
 import org.chai.kevin.data.Sum;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.reports.AbstractReportTarget;
 import org.chai.kevin.util.Utils;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.proxy.HibernateProxy;
 
 @Entity(name = "FctTargetOption")
 @Table(name = "dhsst_fct_target_option")
 @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 public class FctTargetOption extends AbstractReportTarget implements Exportable {
 
 	private FctTarget target;	
 	private String numberFormat = "#";
 	private String percentageFormat = "#%";
 	
 	@Transient
 	public Type getType() {
 		return getSum().getType();
 	}
 	
 	@Transient
 	public Sum getSum() {
		if (getData() instanceof HibernateProxy) {
			return Sum.class.cast(((HibernateProxy) getData()).getHibernateLazyInitializer().getImplementation());  
		}
		else {
			return Sum.class.cast(getData());
		}
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
