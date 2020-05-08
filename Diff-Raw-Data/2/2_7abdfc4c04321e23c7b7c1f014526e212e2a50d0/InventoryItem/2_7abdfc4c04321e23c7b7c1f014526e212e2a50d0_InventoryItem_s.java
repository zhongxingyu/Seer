 package org.jeffklein.tw.calc;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jklein
  * Date: 9/28/12
  * Time: 9:16 PM
  * To change this template use File | Settings | File Templates.
  */
 public enum InventoryItem {
     SHANK(InventoryItemClass.MELEE, 1, 0, 400, 0, false),
     SATURDAY_NIGHT_SPECIAL(InventoryItemClass.WEAPON, 2, 1, 1000, 0, false),
     GARROTE(InventoryItemClass.MELEE, 3, 1, 5000,0, true),
     NINE_MM_HANDGUN(InventoryItemClass.WEAPON, 3, 2, 2500, 0, false),
     RIOT_SHIELD(InventoryItemClass.ARMOR, 3, 6, 16000, 0, true),
     MOLOTOV_COCKTAIL(InventoryItemClass.WEAPON, 7, 3, 4000, 0, false),
     GALESI_MODEL_503(InventoryItemClass.WEAPON, 8, 9, 6500, 0, false),
     KEVLAR_VEST(InventoryItemClass.ARMOR, 1, 4, 8000, 0, false),
    BRASS_KNUCKLES(InventoryItemClass.WEAPON, 3, 2, 4000, 0, true),
     FIFTY_SEVEN_MAGNUM(InventoryItemClass.WEAPON, 5, 3, 10000, 0, false),
     MEAT_CLEAVER(InventoryItemClass.MELEE, 2, 1, 2000, 0, false),
     GRENADE(InventoryItemClass.WEAPON, 10, 8, 10000, 0, false),
     AK47(InventoryItemClass.WEAPON, 15, 12, 20000,100, false),
     GERMAN_STILLETTO_KNIFE(InventoryItemClass.MELEE, 7, 3, 35000, 0, true),
     POTATO_MASHER(InventoryItemClass.WEAPON, 13, 9, 0, 0, true),
     SAWED_OFF_SHOTGUN(InventoryItemClass.WEAPON, 13, 14, 25000, 0, true),
     GLOCK_31(InventoryItemClass.WEAPON, 14, 10, 30000, 0, true),
     XM400_MINIGUN(InventoryItemClass.WEAPON, 18, 16, 100000, 250, false),
     SLUGGER(InventoryItemClass.MELEE, 9, 5, 75000, 0, true),
     STEEL_TOED_SHOES(InventoryItemClass.MELEE, 5, 4, 60000, 60, false),
     RPG(InventoryItemClass.WEAPON, 22, 14, 200000, 500, false),
     BODY_ARMOR(InventoryItemClass.ARMOR, 2, 15, 25000, 200, false),
     LUPARA(InventoryItemClass.WEAPON, 20, 20, 80000, 0, true),
     MACHETE(InventoryItemClass.MELEE, 12, 4, 110000, 0, true),
     TOMMY_GUN(InventoryItemClass.WEAPON, 24, 12, 300000, 750, false),
     CHAINSAW(InventoryItemClass.MELEE, 10, 5, 30000, 0, true),
     THREE_THIRTY_EIGHT_LAPUA_RIFLE(InventoryItemClass.WEAPON, 20, 17, 350000, 350, false),
     KEVLAR_LINED_SUIT(InventoryItemClass.ARMOR, 4, 23, 50000, 0, true),
     AR15_ASSAULT_RIFLE(InventoryItemClass.WEAPON, 30, 14, 500000, 0, true),
     BERETTA_MODELO_38A(InventoryItemClass.WEAPON, 28, 20, 450000, 0, true),
     BAZOOKA(InventoryItemClass.WEAPON, 40, 0, 400000, 0, true),
     BREN_GUN(InventoryItemClass.WEAPON, 50, 29, 700000, 0, true)
     ;
 
     private InventoryItemClass itemClass;
     private Integer attack, defense, cost, upkeep;
     private Boolean isLoot;
 
     private InventoryItem(InventoryItemClass itemClass, int attack, int def, int cost, int upkeep, boolean isLoot) {
         this.itemClass = itemClass;
         this.attack = attack;
         this.defense = def;
         this.cost = cost;
         this.upkeep = upkeep;
         this.isLoot = isLoot;
     }
 
     public InventoryItemClass getInventoryItemClass() {
         return itemClass;
     }
 
     public Integer getAttack() {
         return attack;
     }
 
     public Integer getDefense() {
         return defense;
     }
 
     public Integer getCost() {
         return cost;
     }
 
     public Integer getUpkeep() {
         return upkeep;
     }
 }
