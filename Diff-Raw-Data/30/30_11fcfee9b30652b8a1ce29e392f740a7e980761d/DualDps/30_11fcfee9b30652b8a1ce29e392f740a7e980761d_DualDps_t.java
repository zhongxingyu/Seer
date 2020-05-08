 package com.warfactory.dpscalc.model;
 
 /**
  * Model to calculate dual wield DPS
  */
 public class DualDps extends Dps {
 
 	/**
 	 * DPS of secondary
 	 */
 	private double weapon2Dps;
 
 	public DualDps() {
		super();
 	}
 
 	/**
 	 * Copy ctor
 	 * 
 	 * @param that
 	 */
 	public DualDps(DualDps that) {
 		this.setWeaponDps(that.getWeaponDps());
 		this.setWeapon2Dps(that.getWeapon2Dps());
 		this.setPrimaryAttribute(that.getPrimaryAttribute());
 		this.setIasPercent(that.getIasPercent());
 		this.setCritChance(that.getCritChance());
 		this.setCritDamage(that.getCritDamage());
 	}
 
 	@Override
 	public double getDps() {
 		double primaryAttributeFactor = 1 + getPrimaryAttribute() / 100.0;
 		// IAS is nerfed by 50% in v1.0.3
 		// dual wield has +15% attack speed bonus
 		double iasFactor = 1 + getIasPercent() / 100.0 + 0.15;
 		double critHitFactor = 1 + getCritChance() * getCritDamage();
 		return getAverageWeaponDps() * primaryAttributeFactor * iasFactor * critHitFactor;
 	}
 
 	/**
 	 * Calculate the combined dps of both hands
 	 */
 	private double getAverageWeaponDps() {
 		return (weaponDps + weapon2Dps) * 0.5;
 	}
 
 	public double getWeapon2Dps() {
 		return weapon2Dps;
 	}
 
 	public void setWeapon2Dps(double weapon2Dps) {
 		this.weapon2Dps = weapon2Dps;
 	}
 
 }
