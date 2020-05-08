 package models;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 
 import play.db.jpa.Model;
 
 /**
  * 
  * @author sven
  *
  */
 @Entity
 public class ProductionPlan extends Model {
 	
 	@OneToMany
	public List<DispositionManufacture> dispositionManufactureP1;
	@OneToMany
	public List<DispositionManufacture> dispositionManufactureP2;
	@OneToMany
	public List<DispositionManufacture> dispositionManufactureP3;
 	
 }
