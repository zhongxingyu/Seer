 package ee.itcollege.borderproject.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.Range;
 
 import ee.itcollege.borderproject.common.BaseJoinEntity;
 
 @Entity
 @Table(name = "Piirivalvurpiiripunktis")
 @NamedQueries({
	@NamedQuery(name = "GuardInBorderStation.findAll", query = "SELECT g FROM GuardInBorderStation g WHERE g.removed IS null")
 })
 public class GuardInBorderStation extends BaseJoinEntity implements Serializable {
 	
 	private static final long serialVersionUID = 5376653039415829163L;
 	
 	@Column(name = "Koormus")
 	@Range(min = 0, max = 1)
 	private double workTime;
 	
 	@ManyToOne
 	@JoinColumn(name = "Piirivalvur_Id")
 	@NotNull
 	private Guard guard;
 	
 	@ManyToOne
 	@JoinColumn(name = "Piiripunkt_Id")
 	@NotNull
 	private BorderStation borderStation;
 	
 	public double getWorkTime() {
 		return workTime;
 	}
 	
 	public BorderStation getBorderStation() {
 		return borderStation;
 	}
 
 	public Guard getGuard() {
 		return guard;
 	}
 	
 	public void setWorkTime(double workTime) {
 		this.workTime = workTime;
 	}
 	
 	public void setGuard(Guard guard) {
 		this.guard = guard;
 	}
 
 	public void setBorderStation(BorderStation borderStation) {
 		this.borderStation = borderStation;
 	}
 }
