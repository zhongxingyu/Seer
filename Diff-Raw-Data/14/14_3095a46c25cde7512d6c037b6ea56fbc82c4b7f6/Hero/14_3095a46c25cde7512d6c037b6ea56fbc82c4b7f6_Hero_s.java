 package com.herocraftonline.dev.heroes.persistance;
 
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 
 public class Hero {
 
     private Player player;
     private HeroClass playerClass;
     private HeroClass playerSubClass;
     private int experience;
     private int mana;
 
     // Constructor
     public Hero(Player p, HeroClass pClass, HeroClass pSubClass, int exp, int mana) {
         this.player = p;
         this.playerClass = pClass;
         this.playerSubClass = pSubClass;
         this.experience = exp;
         this.mana = mana;
     }
 
     public Player getPlayer() {
         return player;
     }
 
     public HeroClass getPlayerClass() {
         return playerClass;
     }
 
     public HeroClass getPlayerSubClass() {
         return playerSubClass;
     }
 
     public int getExp() {
         return experience;
     }
 
     public int getMana() {
         return mana;
     }
 
     public void setPlayerClass(HeroClass heroclass) {
         playerClass = heroclass;
     }
 
     public void setPlayerSubClass(HeroClass heroclass) {
         playerSubClass = heroclass;
     }
 
     public void setExperience(int value) {
         experience = value;
     }
 
     public void setMana(int value) {
         mana = value;
     }
 
    public boolean equals(Player p){
        if(p.getName().equalsIgnoreCase(player.getName())){
            return true;
         } else {
             return false;
         }
     }
 }
