 package net.croxis.plugins.civilmineation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 @Entity()
 @Table(name = "civ_civilization")
 public class CivilizationComponent{
 	@Id
 	private int id;
 	@OneToOne(fetch = FetchType.LAZY)
 	private Ent entityID;
 	private String name;
	@OneToMany(mappedBy="civAssistant")
    private List<ResidentComponent> assistants = new ArrayList<ResidentComponent>();
 	//@OneToMany(mappedBy="civilization")
     //private List<CityComponent> cities = new ArrayList<CityComponent>();
 	/*@OneToMany(cascade=CascadeType.ALL)
 	@JoinTable(
 			name="CIV_ALLIES",
 			joinColumns = @JoinColumn(name="CIV_ID", referencedColumnName="ID"),
 			inverseJoinColumns = @JoinColumn(name="ALLY_ID", referencedColumnName="ID") 
 			)*/
 	@ManyToMany(targetEntity=CivilizationComponent.class)
 	@JoinTable(
 			name="civ_civ_ally_map",
 			joinColumns=@JoinColumn(name="civ_id", referencedColumnName="ID"),
 			inverseJoinColumns=@JoinColumn(name="ally_id", referencedColumnName="ID")
 			)
 	    private List<CivilizationComponent> allies = new ArrayList<CivilizationComponent>();
 	/*@OneToMany(cascade=CascadeType.ALL)
 	@JoinTable(
 			name="CIV_ENEMIES",
 			joinColumns = @JoinColumn(name="CIV_ID", referencedColumnName="ID"),
 			inverseJoinColumns = @JoinColumn(name="ENEMY_ID", referencedColumnName="ID") 
 			)*/
 	@ManyToMany(targetEntity=CivilizationComponent.class)
 	@JoinTable(
 			name="civ_civ_enemy_map",
 			joinColumns=@JoinColumn(name="civ_id", referencedColumnName="ID"),
 			inverseJoinColumns=@JoinColumn(name="enemy_id", referencedColumnName="ID")
 			)
     private List<CivilizationComponent> enemies = new ArrayList<CivilizationComponent>();
 	//@OneToOne
 	//@JoinColumn(name="CITY_ID")
     //private CityComponent capital;
     private double taxes;
     private boolean neutral = false;
     private String tag;
     
     private long registered;
     
     private String texturePackNode;
 	private String capeNode;    	
 	private String musicNode;
	public List<ResidentComponent> getAssistants() {
		return assistants;
	}
	public void setAssistants(List<ResidentComponent> assistants) {
		this.assistants = assistants;
	}
 	//public List<CityComponent> getCities() {
 	//	return cities;
 	//}
 	//public void setCities(List<CityComponent> cities) {
 	//	this.cities = cities;
 	//}
 	public List<CivilizationComponent> getAllies() {
 		return allies;
 	}
 	public void setAllies(List<CivilizationComponent> allies) {
 		this.allies = allies;
 	}
 	public List<CivilizationComponent> getEnemies() {
 		return enemies;
 	}
 	public void setEnemies(List<CivilizationComponent> enemies) {
 		this.enemies = enemies;
 	}
 	//public CityComponent getCapital() {
 	//	return capital;
 	//}
 	//public void setCapital(CityComponent capital) {
 	//	this.capital = capital;
 	//}
 	public double getTaxes() {
 		return taxes;
 	}
 	public void setTaxes(double taxes) {
 		this.taxes = taxes;
 	}
 	public boolean isNeutral() {
 		return neutral;
 	}
 	public void setNeutral(boolean neutral) {
 		this.neutral = neutral;
 	}
 	public String getTag() {
 		return tag;
 	}
 	public void setTag(String tag) {
 		this.tag = tag;
 	}
 	public long getRegistered() {
 		return registered;
 	}
 	public void setRegistered(long registered) {
 		this.registered = registered;
 	}
 	public String getTexturePackNode() {
 		return texturePackNode;
 	}
 	public void setTexturePackNode(String texturePackNode) {
 		this.texturePackNode = texturePackNode;
 	}
 	public String getCapeNode() {
 		return capeNode;
 	}
 	public void setCapeNode(String capeNode) {
 		this.capeNode = capeNode;
 	}
 	public String getMusicNode() {
 		return musicNode;
 	}
 	public void setMusicNode(String musicNode) {
 		this.musicNode = musicNode;
 	}
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public Ent getEntityID() {
 		return entityID;
 	}
 	public void setEntityID(Ent entityID) {
 		this.entityID = entityID;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 
 }
