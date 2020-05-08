 package com.game.jetpack;
 
 public class Weapon {
 	private int bulletsRemaining;
 	private float damage;
 	private float fireRate;
 	private int type;
 	private float bulletSpeed;
 	
 	public static int WEAPON_PISTOL  = 0;
 	public static int WEAPON_SHOTGUN = 1;
 	public static int WEAPON_RIFLE   = 2;
 	public static int WEAPON_ROCKET  = 3;
 	
 	public Weapon(int type) {
 		super();
 		this.type = type;
 		
 		assignWeaponProperties(type);
 	}
 
 	private void assignWeaponProperties(int type)
 	{
 		if(type == WEAPON_PISTOL)
 		{
 			this.bulletsRemaining = 1;
 			this.damage = 10;
 			this.fireRate = 0.5f;
 			this.bulletSpeed = 30;
 		}
 		else if(type == WEAPON_SHOTGUN)
 		{
 			this.bulletsRemaining = 25;
 			this.damage = 15;
 			this.fireRate = 2.0f;
 			this.bulletSpeed = 17;
 		}
 		else if(type == WEAPON_RIFLE)
 		{
			this.bulletsRemaining = 50; 
			this.damage = 8.0f;
			this.fireRate = 0.1f;
 			this.bulletSpeed = 30;
 		}
 		else if(type == WEAPON_ROCKET)
 		{
 			this.bulletsRemaining = 15;
 			this.damage = 25f;
 			this.fireRate = 2.5f;
 			this.bulletSpeed = 17;
 		}
 	}
 	
 	public void fire() {
 		if(type != WEAPON_PISTOL) 
 		{
 			bulletsRemaining--;
 		}
 		
 		if(bulletsRemaining <= 0)
 		{
 			setType(WEAPON_PISTOL);
 		}
 	}
 
 	public float getDamage() {
 		return damage;
 	}
 
 	public void setDamage(float damage) {
 		this.damage = damage;
 	}
 
 	public float getFireRate() {
 		return fireRate;
 	}
 
 	public void setFireRate(float fireRate) {
 		this.fireRate = fireRate;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 		
 		assignWeaponProperties(type);
 	}
 
 	public float getBulletSpeed() {
 		return bulletSpeed;
 	}
 
 	public void setBulletSpeed(float bulletSpeed) {
 		this.bulletSpeed = bulletSpeed;
 	}
 	
 }
