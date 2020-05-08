 package com.blastedstudios.crittercaptors.creature;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.blastedstudios.crittercaptors.CritterCaptors;
 import com.blastedstudios.crittercaptors.util.ExperienceManager;
 import com.blastedstudios.crittercaptors.util.XMLUtil;
 
 /**
  * Defines creature. Has base stats, rest are calculated
  */
 public class Creature {
 	private static final int BASE_HAPPINESS = 70;
 	private String name;
 	private List<AffinityEnum> affinities;
 	private HashMap<Ability,Integer> abilities;
 	private int active, experience, hpCurrent, happiness;
 	private Stats baseStats, ivStats, evStats, evYield;
 	public Camera camera = new PerspectiveCamera(67, 1.33f, 2f);
 	public float timeSinceDirectionChange = 0;
 	private float catchRate = 0f;
 	private List<Ability> activeAbilities;
 	
 	public Creature(String name, Stats baseStats, Stats ivStats, Stats evStats, 
 			Stats evYield, int experience, List<AffinityEnum> affinities, 
 			HashMap<Ability,Integer> abilities, int active, float catchRate,
 			int happiness){
 		this.name = name;
 		this.baseStats = baseStats;
 		this.ivStats = ivStats;
 		this.evStats = evStats;
 		this.evYield = evYield;
 		this.experience = experience;
 		this.affinities = affinities;
 		this.abilities = abilities;
 		this.active = active;
 		this.catchRate = catchRate;
 		this.hpCurrent = getHPMax();
 		this.happiness = happiness;
 		activeAbilities = new ArrayList<Ability>();
 		for(Ability ability : abilities.keySet())
 			if(abilities.get(ability) <= ExperienceManager.getLevel(experience) && activeAbilities.size() <= 4)
 				activeAbilities.add(ability);
 	}
 
 	private int attackPhysical(Creature enemy, Ability ability){
		return (int) (((((( (ExperienceManager.getLevel(experience) * 2f / 5f) + 2f) * 
				getAttack() * ability.power / enemy.getDefense())) / 50f) * 
                /*CH ×*/ getR() / 100f) * AffinityDamage.getDamageMultiplier(affinities, enemy.affinities));
 	}
 	
 	private int attackSpecial(Creature enemy, Ability ability){
 		return (int) ((((((((ExperienceManager.getLevel(experience) * 2f / 5f) + 2f) * 
 				ability.power * getSpecialAttack() / 50f) / enemy.getSpecialDefense())) / 2f) * 
                /*CH ×*/ getR() / 100f) * AffinityDamage.getDamageMultiplier(ability.affinity, enemy.affinities));
 	}
 	
 	public int attack(Creature enemy, String abilityName){
 		for(Ability activeAbility : activeAbilities)
 			if(activeAbility.name.equals(abilityName))
 				if(activeAbility.affinity == AffinityEnum.physical)
 					return attackPhysical(enemy, activeAbility);
 				else
 					return attackSpecial(enemy, activeAbility);
 		return 0;
 	}
 	
 	private static int getR(){
 		return 100 - CritterCaptors.random.nextInt(15) ;
 	}
 	
 	public int getAttack(){
 		return ( ((ivStats.attack + (2 * baseStats.attack) + 
 				(evStats.attack / 4)) * getLevel()) / 100) + 5;
 	}
 	
 	public int getSpecialAttack(){
 		return ( ((ivStats.specialAttack + (2 * baseStats.specialAttack) + 
 				(evStats.specialAttack / 4)) * getLevel()) / 100) + 5;
 	}
 
 	public int getDefense(){
 		return ( ((ivStats.defense + (2 * baseStats.defense) + 
 				(evStats.defense / 4)) * getLevel()) / 100) + 5;
 	}
 	
 	public int getSpecialDefense(){
 		return ( ((ivStats.specialDefense + (2 * baseStats.specialDefense) + 
 				(evStats.specialDefense / 4)) * getLevel()) / 100) + 5;
 	}
 	
 	public int getHPMax(){
 		return (((ivStats.hpMax + (2 * baseStats.hpMax) + (evStats.hpMax / 4) + 100) * getLevel()) / 100) + 10;
 	}
 	
 	public int getHPCurrent(){
 		return hpCurrent;
 	}
 	
 	public float getCatchRate(){
 		return (((3*getHPMax() - 2*getHPCurrent())*catchRate/* *ball bonus*/) / (3*getHPMax()))/* *status bonus*/;
 	}
 	
 	public void addHappiness(int happiness){
 		this.happiness += happiness;
 	}
 
 	public void setHappiness(int happiness) {
 		this.happiness = happiness;
 	}
 	
 	/**
 	 * @param damage received by creature
 	 * @return if the creature is dead
 	 */
 	public boolean receiveDamage(int damage){
 		hpCurrent = Math.max(0, hpCurrent - damage);
 		return getHPCurrent() <= 0;
 	}
 	
 	public List<AffinityEnum> getAffinities(){
 		return affinities;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public int getLevel(){
 		return ExperienceManager.getLevel(experience);
 	}
 	
 	public void setIV(Stats iv){
 		ivStats = iv;
 	}
 	
 	public void setEV(Stats ev){
 		evStats = ev;
 	}
 	
 	public int getPercentLevelComplete(){
 		int currentLevelXP = ExperienceManager.getExperience(getLevel()+1) - 
 			ExperienceManager.getExperience(getLevel());
 		int amountCompleted = experience - ExperienceManager.getExperience(getLevel());
 		return amountCompleted * 100 / currentLevelXP;
 	}
 	
 	public int getExperience(){
 		return experience;
 	}
 	
 	public void setExperience(int experience){
 		this.experience = experience;
 	}
 	
 	public void addExperience(int experience){
 		this.experience += experience;
 	}
 
 	public void heal(){
 		this.hpCurrent = getHPMax();
 	}
 	
 	public Creature clone(){
 		Random rand = CritterCaptors.random;
 		Stats ivStats = new Stats(rand.nextInt(31), rand.nextInt(31),
 				rand.nextInt(31), rand.nextInt(31), rand.nextInt(31),
 				rand.nextInt(31));
 		Creature creature = new Creature(name, baseStats, ivStats, evStats,
 				evYield, experience, affinities, abilities, active, catchRate,
 				happiness);
 		return creature;
 	}
 	
 	public List<Ability> getActiveAbilities(){
 		return activeAbilities;
 	}
 	
 	public Element asXML(Document doc){
 		Element node = doc.createElement("creature");
 		node.setAttribute("name", name);
 		node.setAttribute("experience", Integer.toString(experience));
 		node.setAttribute("happiness", Integer.toString(happiness));
 		if(active != -1)
 			node.setAttribute("active", Integer.toString(active));
 		return node;
 	}
 	
 	public static Creature fromXML(Element ele){
 		List<AffinityEnum> affinities = new ArrayList<AffinityEnum>();
 		for(Element affinityEle : XMLUtil.iterableElementList(ele.getElementsByTagName("affinity")))
 			affinities.add(AffinityEnum.valueOf(affinityEle.getFirstChild().getNodeValue()));
 		int active = -1;
 		float catchRate = Float.parseFloat(ele.getAttribute("catchRate"));
 		HashMap<Ability,Integer> abilities = new HashMap<Ability,Integer>();
 		for(Element abilityEle : XMLUtil.iterableElementList(ele.getElementsByTagName("ability")))
 			abilities.put(Ability.abilities.get(abilityEle.getAttribute("name")), 
 					Integer.parseInt(abilityEle.getAttribute("level")));
 		Stats baseStats = new Stats(), ivStats = new Stats(), evStats = new Stats();
 		if(ele.getElementsByTagName("baseStats").getLength() > 0)
 			baseStats = Stats.fromXML((Element)ele.getElementsByTagName("baseStats").item(0));
 		if(ele.getElementsByTagName("ivStats").getLength() > 0)
 			ivStats = Stats.fromXML((Element)ele.getElementsByTagName("ivStats").item(0));
 		if(ele.getElementsByTagName("evStats").getLength() > 0)
 			evStats = Stats.fromXML((Element)ele.getElementsByTagName("evStats").item(0));
 		Stats evYield = Stats.fromXML((Element)ele.getElementsByTagName("evYield").item(0));
 		int happiness = BASE_HAPPINESS;
 		if(ele.hasAttribute("happiness"))
 			happiness = Integer.parseInt(ele.getAttribute("happiness"));
 		return new Creature(ele.getAttribute("name"), baseStats, ivStats, 
 				evStats, evYield, Integer.parseInt(ele.getAttribute("experience")), 
 				affinities, abilities, active, catchRate, happiness);
 	}
 
 	public void setActive(int active) {
 		this.active = active;
 	}
 
 	public int getActive() {
 		return active;
 	}
 
 	public Stats getIV() {
 		return ivStats;
 	}
 
 	public Stats getEV() {
 		return evStats;
 	}
 	
 	public Stats getEVYield() {
 		return evYield;
 	}
 	
 	public int getWorth(){
 		return (int)((3*getLevel() + evStats.sum() + ivStats.sum()) / getCatchRate()) / 100;
 	}
 }
