 package swag49.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
 public abstract class LevelBase {
 	
 	@Column(nullable = false)
 	private Long upgradeDuration;
 	
 	public abstract Integer getLevel();
 
 	public void setUpgradeDuration(Long upgradeDuration) {
 		this.upgradeDuration = upgradeDuration;
 	}
 
 	public Long getUpgradeDuration() {
 		return upgradeDuration;
 	}
 
 }
