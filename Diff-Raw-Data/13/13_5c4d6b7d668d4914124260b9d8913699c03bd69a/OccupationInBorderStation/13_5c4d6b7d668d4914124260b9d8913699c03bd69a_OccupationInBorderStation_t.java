 package ee.itcollege.borderproject.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 
 import ee.itcollege.borderproject.common.BaseJoinEntity;
 
 @Entity
 @Table(name = "AmetPiiripunktis")
 @NamedQueries({
	@NamedQuery(
			name = "OccupationInBorderStation.findAll",
			query = "SELECT o FROM OccupationInBorderStation o WHERE o.removed IS null AND o.occupation.removed IS null AND o.borderStation.removed IS null")
 })
 public class OccupationInBorderStation extends BaseJoinEntity implements Serializable{
 	
 	private static final long serialVersionUID = 1L;
 	
 	@ManyToOne
 	@JoinColumn(name = "Amet_Id")
 	@NotNull
 	private Occupation occupation;
 	
 	@ManyToOne
 	@JoinColumn(name = "Piiripunkt_Id")
 	@NotNull
 	private BorderStation borderStation;
 	
 	public Occupation getOccupation() {
 		return occupation;
 	}
 	
 	public BorderStation getBorderStation() {
 		return borderStation;
 	}
 	
 	public void setOccupation(Occupation occupation) {
 		this.occupation = occupation;
 	}
 	
 	public void setBorderStation(BorderStation borderStation) {
 		this.borderStation = borderStation;
 	}
 }
