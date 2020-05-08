 package ch.andefgassm.adventuregame.combat;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 public class CombatSystem {
     
     private Map<String, Skill> skills = new HashMap<String, Skill>();
         
     public void registerSkill(String name, Skill skill) {
         skills.put(name, skill);
     }
 
 	public Skill getSkill(String skill) {
         return skills.get(skill);
     }
 }
