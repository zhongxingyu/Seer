 package com.tree.combatcalculator;
 
 import java.io.Serializable;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.List;
 
 
 
 public class AttackResult implements Serializable{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4969741298965801915L;
 	public float hitDamage;
 	public float hitChance;
 	public String hitChancePer;
 	//crit damage stores CMA leftovers for combined attacks
 	public boolean hasCrit;
 	public float critChance;
 	public String critChancePer;
 	public float critDamage;
 	public Weapon weapon;
 	public boolean isCMA = false;
 	public int cmaGroup = 0;
 	public int cmaTotal = 0;
 	private static NumberFormat f = new DecimalFormat("###,###,###,###,##0.##");
 	public boolean boughtAttackBoost = false;
 	public boolean boughtDamageBoost = false;
 	public boolean boughtAttack = false;
 	public boolean isCharge = false;
 	
 	
 	public AttackResult(float hitDamage, float hitChance, boolean hasCrit, float critChance, float critDamage, Weapon weapon){
 		this.hitDamage = hitDamage;
 		this.hitChance = hitChance;
 		this.hasCrit = hasCrit;
 		this.critChance = critChance;
 		this.critDamage = critDamage;
 		this.weapon = weapon;
 		this.hitChancePer = f.format(hitChance*100);
 		this.critChancePer = f.format(critChance*100);
 	}
 	
 	public AttackResult(float hitDamage, float hitChance, boolean hasCrit, float critChance, float critDamage, Weapon weapon,
 			boolean isCMA, int cmaNum, int cmaTotal){
 		this.hitDamage = hitDamage;
 		this.hitChance = hitChance;
 		this.hasCrit = hasCrit;
 		this.critChance = critChance;
 		this.critDamage = critDamage;
 		this.weapon = weapon;
 		this.hitChancePer = f.format(hitChance*100);
 		this.critChancePer = f.format(critChance*100);
 		this.isCMA = isCMA;
 		this.cmaGroup = cmaNum;
 		this.cmaTotal = cmaTotal;
 	}
 	
 	public String getTruncExpectedHitDamage(){
 		return f.format(hitDamage*hitChance);
 	}
 	
 	public String getTruncExpectedCritDamage(){
 		return f.format(critDamage*critChance);
 	}
 	
 	public float getTotalExpectedDamageWithHitChance(){
 		return hitChance*hitDamage + critChance*critDamage;
 	}
 	
 	public float getTotalExpectedDamageAssumeHit(){
 		return hitDamage*hitChance/(hitChance+critChance)+critDamage*critChance/(hitChance+critChance);
 	}
 
 	static public String formatResult(float result){
 		return f.format(result);
 	}
 	
 	
 	static public String totalHits(List<AttackResult> attackResults, boolean usingCrits){
 		
 		float total = 0;
 		
 		if (usingCrits)
 			for (AttackResult a : attackResults)
 				total += a.hitChance;
 		else
 			for (AttackResult a : attackResults)
 				total += a.hitChance + a.critChance;
 		
 		return f.format(total);
 	}
 	
 	static public String expDamage(List<AttackResult> attackResults, boolean usingCrits){
 		
 		float total = 0;
 		
 		if (usingCrits)
 			for (AttackResult a : attackResults)
 				total += a.getTotalExpectedDamageWithHitChance();
 		else
 			for (AttackResult a : attackResults)
 				total += (a.hitChance + a.critChance) * a.hitDamage;
 		
 		return f.format(total);
 	}
 	
 	static public String expDamageAllHit(List<AttackResult> attackResults, boolean usingCrits){
 		
 		float total = 0;
 		
 		if (usingCrits)
 			for (AttackResult a : attackResults)
 				total += a.getTotalExpectedDamageAssumeHit();
 		else
 			for (AttackResult a : attackResults)
 				total += a.hitDamage;
 		
 		return f.format(total);
 	}
 	
 	static public String totalCrits(List<AttackResult> attackResults, boolean usingCrits){
 		
 		float total = 0;
 		
 		if (usingCrits)
 			for (AttackResult a : attackResults)
 				total += a.critChance;
 
 		
 		return f.format(total);
 	}
 
 	public String constructAttackValues() {
 		String values = "";
 		
 		if (isCharge)
			values += "C=> ";
 		
 		if (boughtAttackBoost)
			values += "+A ";
 		
 		if (boughtDamageBoost)
			values += "+D ";
 		
 		if (boughtAttack)
			values += "$B ";
 
 			
 		return values;
 	}
 
 
 
 	
 
 
 }
