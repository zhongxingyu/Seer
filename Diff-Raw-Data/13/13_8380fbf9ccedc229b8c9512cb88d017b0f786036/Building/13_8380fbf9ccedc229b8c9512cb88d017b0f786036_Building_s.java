 package dk.itu.gsd.lms.model;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @SuppressWarnings("serial")
 @Entity
 @Table(name = "building")
 public class Building extends HibernateModel implements Serializable{
	@Column(name = "security_mode")
	@Enumerated(EnumType.STRING)
 	private SecurityMode securityMode;
 	
 	@Column(name = "energy_state")
 	@Enumerated(EnumType.STRING)
 	private EnergyState energyState;
 	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "recourse_case_id")
	private List<Floor> floors;
 	
	@Id
 	@Column(name = "foreign_building_id", unique = true, nullable = false)
 	private Long foreignBuildingID;
 	
 }
