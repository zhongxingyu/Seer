 package yuuki.action;
 
 import yuuki.entity.Character;
 import java.util.Random;
 /**
  *
  * @author Caleb Smith
  * @version 10/11/12
  */
 public class Flee extends Action {
     
     public Flee()
     {
        super("Flee", 0.0, 0.0, null, null);
     }
     
     public Flee createInstance(String[] args) {
     	return new Flee();
     }
     
     protected void applyBuffs()
     {
         
     }
     protected boolean applyCost()
     {
      return calculateFlee();   
     }
     protected void applyEffect()
     {
         
     }
     protected void setCostStat(Character c)
     {
         
     }
     protected void setEffectStat(Character c)
     {
         
     }
     /**
      * Using character and monster points, this method determines what the chance
      * that the character will be able to flee is.  It also returns whether the 
      * character manages to flee of not.
      * 
      * @author Caleb Smith
      * @param monsterLevel
      * @param playerLevel 
      * @return Boolean variable if character can escape.
      */
    public boolean calculateFlee()
     {
         boolean flee = false;
         Random rand;
         int playerLevel = origin.getLevel();
         int monsterLevel = 0;
         for(Character c: targets)
         {
             monsterLevel += c.getLevel();
         }
         double advantageLevel = (double) playerLevel / monsterLevel;
         int randomNumber;
         rand = new Random();  
       
         //Get Character agility level.
         int agility = origin.getAgility();
         advantageLevel += (agility * 0.2);
         if(advantageLevel == 1)
         {
             randomNumber = rand.nextInt(2) + 1;
             if(randomNumber == 1)
             {
                 flee = true;
             }
             else
             {
                 flee = false;
             }
         }
         
         else if(advantageLevel > 1)
         {
             flee = true;
         }
         else if(advantageLevel < 0.1)
         {
             flee = false;
         }
         else
         {
             double escapeChance = rand.nextDouble();
             flee = (escapeChance <= advantageLevel);
         }
         
         return flee;
     }
 }
