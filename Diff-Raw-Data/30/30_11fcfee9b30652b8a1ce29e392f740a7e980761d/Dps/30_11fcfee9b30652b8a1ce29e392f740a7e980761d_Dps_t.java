 package com.warfactory.dpscalc.model;
 
 /**
  * Model for calculating total DPS
  */
 public class Dps {
 
 	/**
 	 * Weapon dps, as displayed in weapons's property
 	 */
 	protected double weaponDps;
 
 	/**
 	 * Character main attribute
 	 */
 	private int primaryAttribute;
 
 	/**
 	 * Increased attack speed percentage, from items other than weapon. 1 means
 	 * +1% attack speed.
 	 * 
 	 */
 	private double iasPercent;
 
 	/**
 	 * Critical hit chance. between 0 and 1. 1 means 100% critical hit rate
 	 */
 	private double critChance;
 
 	/**
 	 * Critical hit damage increase percentage. 1 means +100% critical damage.
 	 */
 	private double critDamage;
 
 	public Dps() {
		// default ctor
 	}
 
 	/**
 	 * Copy ctor
 	 * 
 	 * @param that
 	 */
 	public Dps(Dps that) {
 		this.setWeaponDps(that.getWeaponDps());
 		this.setPrimaryAttribute(that.getPrimaryAttribute());
 		this.setIasPercent(that.getIasPercent());
 		this.setCritChance(that.getCritChance());
 		this.setCritDamage(that.getCritDamage());
 	}
 
 	public double getDps() {
 		double primaryAttributeFactor = 1 + primaryAttribute / 100.0;
 		double iasFactor = 1 + iasPercent / 100.0;
 		double critHitFactor = 1 + critChance * critDamage;
 		return weaponDps * primaryAttributeFactor * iasFactor * critHitFactor;
 	}
 
 	public double getWeaponDps() {
 		return weaponDps;
 	}
 
 	public void setWeaponDps(double weaponDps) {
 		this.weaponDps = weaponDps;
 	}
 
 	public double getIasPercent() {
 		return iasPercent;
 	}
 
 	public void setIasPercent(double iasPercent) {
 		this.iasPercent = iasPercent;
 	}
 
 	public double getCritChance() {
 		return critChance;
 	}
 
 	public void setCritChance(double critChance) {
 		if (critChance > 1) {
			this.critChance = 1;
		} else {
			this.critChance = critChance;
 		}
 	}
 
 	public double getCritDamage() {
 		return critDamage;
 	}
 
 	public void setCritDamage(double critDamage) {
 		this.critDamage = critDamage;
 	}
 
 	public int getPrimaryAttribute() {
 		return primaryAttribute;
 	}
 
 	public void setPrimaryAttribute(int mainAttribute) {
 		this.primaryAttribute = mainAttribute;
 	}
 
 }
