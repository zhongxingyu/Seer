 package com.pathfindersdk.creatures;
 
 import java.util.Hashtable;
 
 import com.pathfindersdk.enums.AbilityType;
 import com.pathfindersdk.enums.AlignmentType;
 import com.pathfindersdk.enums.SaveType;
 import com.pathfindersdk.enums.SizeType;
 import com.pathfindersdk.enums.SpeedType;
 import com.pathfindersdk.stats.AbilityScore;
 import com.pathfindersdk.stats.SavingThrow;
 import com.pathfindersdk.stats.Size;
import com.pathfindersdk.stats.Stat;
 
 
 /**
  * This base class contains stats common to every creatures (PCs, NPCs and Monsters).
  */
 public abstract class Creature
 {
   protected String name;
   protected AlignmentType alignment;
   protected Size size;
   protected Boolean biped;
  protected Hashtable<SpeedType, Stat> speeds = new Hashtable<SpeedType, Stat>();
   protected Hashtable<AbilityType, AbilityScore> abilityScores = new Hashtable<AbilityType, AbilityScore>(); 
   protected Hashtable<SaveType, SavingThrow> savingThrows = new Hashtable<SaveType, SavingThrow>(); 
   
   public Creature(String name)
   {
     this.name = name;
     
     biped = true;
     
     abilityScores.put(AbilityType.STR, new AbilityScore(10));
     abilityScores.put(AbilityType.DEX, new AbilityScore(10));
     abilityScores.put(AbilityType.CON, new AbilityScore(10));
     abilityScores.put(AbilityType.INT, new AbilityScore(10));
     abilityScores.put(AbilityType.WIS, new AbilityScore(10));
     abilityScores.put(AbilityType.CHA, new AbilityScore(10));
 
     savingThrows.put(SaveType.FORT, new SavingThrow(getAbilityScore(AbilityType.CON)));
     savingThrows.put(SaveType.REF,  new SavingThrow(getAbilityScore(AbilityType.DEX)));
     savingThrows.put(SaveType.WILL, new SavingThrow(getAbilityScore(AbilityType.WIS)));
   }
   
   public void setName(String name)
   {
     this.name = name;
   }
   
   public AlignmentType getAlignment()
   {
     return alignment;
   }
   
   public void setAlignment(AlignmentType alignment)
   {
     this.alignment = alignment;
   }
   
   public boolean isBiped()
   {
     return biped;
   }
   
   public void setBiped(boolean biped)
   {
     this.biped = biped;
   }
   
   public AbilityScore getAbilityScore(AbilityType type)
   {
     AbilityScore score = abilityScores.get(type);
     if(score == null)
       System.out.println("Ability Score is null!");
     
     return score; 
   }
   
   public AbilityScore getStrenght()
   {
     return getAbilityScore(AbilityType.STR);
   }
   
   public AbilityScore getDexterity()
   {
     return getAbilityScore(AbilityType.DEX);
   }
   
   public AbilityScore getConstitution()
   {
     return getAbilityScore(AbilityType.CON);
   }
   
   public AbilityScore getIntelligence()
   {
     return getAbilityScore(AbilityType.INT);
   }
   
   public AbilityScore getWisdom()
   {
     return getAbilityScore(AbilityType.WIS);
   }
   
   public AbilityScore getCharisma()
   {
     return getAbilityScore(AbilityType.CHA);
   }
   
   public SavingThrow getSavingThrow(SaveType type)
   {
     SavingThrow savingThrow = savingThrows.get(type);
     if(savingThrow == null)
       System.out.println("SavingThrow is null!");
     
     return savingThrow;
   }
   
   public SavingThrow getFortitude()
   {
     return getSavingThrow(SaveType.FORT);
   }
   
   public SavingThrow getReflex()
   {
     return getSavingThrow(SaveType.REF);
   }
   
   public SavingThrow getWill()
   {
     return getSavingThrow(SaveType.WILL);
   }
   
   public void setSize(SizeType size)
   {
     if(size == null)
       this.size = new Size(size);
     else
       this.size.setBaseSize(size);
   }
 
   public SizeType getSize()
   {
     return size.getSize(biped);
   }
   
  public Stat getSpeed(SpeedType type)
   {
    Stat speed = speeds.get(type);
     if(speed == null)
       System.out.println("Speed is null!");
     
     return speed;
   }
   
  public void addSpeed(SpeedType type, Stat speed)
   {
    speeds.put(type, speed);
   }
 
  public void removeSpeed(SpeedType speed)
   {
    speeds.remove(speed);
   }
 }
