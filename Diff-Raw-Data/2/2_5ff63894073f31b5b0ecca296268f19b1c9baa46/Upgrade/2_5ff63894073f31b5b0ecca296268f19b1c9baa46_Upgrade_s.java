 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hopkins.rocknrollracing.state;
 
 /**
  *
  * @author ihopkins
  */
 public class Upgrade extends NamedModel {
     
     public static final Upgrade[] Armor = new Upgrade[] {
         new Upgrade("Defender", 0),
         new Upgrade("Rhino Skin", 24000),
         new Upgrade("Saber Tooth", 48000),
         new Upgrade("Atlas Powerplate", 64000)
         };
     public static final Upgrade[] Engine = new Upgrade[] {
         new Upgrade("Cobra Mark VII", 0),
         new Upgrade("War Hammer", 40000),
         new Upgrade("Super Charger", 70000),
         new Upgrade("Atlas Power Boss", 110000)
         };
     public static final Upgrade[] Shocks = new Upgrade[] {
         new Upgrade("Grasshoppers", 0),
         new Upgrade("Hydrosprings", 20000),
         new Upgrade("Hydro Twinpacks", 40000),
         new Upgrade("Atlas Power Lifts", 60000)
         };
     public static final Upgrade[] Tires = new Upgrade[] {
         new Upgrade("Track Masters", 0),
         new Upgrade("Road Warriors", 30000),
         new Upgrade("Super Mudwhumpers", 50000),
         new Upgrade("Atlas Power Claws", 70000)
         };
     
     public static final Upgrade PlasmaRifle = new Upgrade("VK Plasma Rifle", 11000, "1 Pulse Round");
     public static final Upgrade RogueMissile = new Upgrade("Rogue Missile", 20000, "1 Rogue Missle");
     public static final Upgrade SundogBeam = new Upgrade("Sundog Beams", 20000, "1 Sundog Beam");
     
     public static final Upgrade[] Weapon = new Upgrade[] {
         PlasmaRifle, RogueMissile, SundogBeam,
     };
     
     public static final Upgrade Slipsauce = new Upgrade("BF's Slipsauce", 11000, "1 Barrel");
     public static final Upgrade Mine = new Upgrade("Bear Claw Mine", 20000, "1 Mine");
     public static final Upgrade Scatterpack = new Upgrade("KO Scatterpacks", 24000, "1 Scatterpack");
     
     public static final Upgrade[] Drop = new Upgrade[] {
         Slipsauce, Mine, Scatterpack
     };
     
     public static final Upgrade JumpJet = new Upgrade("Locust Jump Jets", 11000, "1 Gas Charge");
    public static final Upgrade Nitro = new Upgrade("Lightning NItro", 24000, "1 Bottle");
     
     public static final Upgrade[] Boost = new Upgrade[] {
         JumpJet, Nitro
     };
     
     public static final Upgrade[][] All = new Upgrade[][] {
         Armor, Engine, Shocks, Tires,
         Weapon, Boost, Drop
     };
     
     protected int price;
     protected String single;
     
     public int getPrice() {
         return price;
     }
     
     public String getSingle() {
         return single;
     }
     
     
     public Upgrade(String name, int price) {
         super(name);
         this.price = price;
         this.single = "";
     }
     public Upgrade(String name, int price, String single) {
         super(name);
         this.price = price;
         this.single = single;
     }
 }
