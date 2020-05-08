 package com.sern.rpg.core.character;
 
 import java.util.Set;
 
 import com.sern.rpg.core.character.modifier.Modifier;
 import com.sern.rpg.core.character.special.Special;
 
 public class BaseCharacter implements Character {
     private String name;
     private int health = 0;
     private int bonus = 0;
     private Set<Modifier> modifiers;
     private Special special;
     
     public BaseCharacter(String name, int health) {
         setName(name);
         setHealth(health);
     }
     
     @Override
     public void setName(String name) {
         this.name = name;
     }
     
     @Override
     public String getName() {
         return name;
     }
     
     @Override
     public void addModifier(Modifier modifier) {
         modifiers.add(modifier);
     }
     
     @Override
     public Set<Modifier> getModifiers() {
         return modifiers;
     }
     
     @Override
     public void setHealth(int health) {
         this.health = health;
     }
     
     @Override
     public int getHealth() {
         return health;
     }
     
     @Override
     public void adjustHealth(int amount) {
         health += amount;
     }
 
     @Override
     public void setBonus(int bonus) {
         this.bonus = bonus;
     }
 
     @Override
     public int getBonus() {
         return bonus;
     }
 
     @Override
     public void setSpecial(Special special) {
         this.special = special;
     }
 
     @Override
     public Special getSpecial() {
         return special;
     }
 
     @Override
     public boolean hasSpecial() {
        return special != null;
     }
 }
