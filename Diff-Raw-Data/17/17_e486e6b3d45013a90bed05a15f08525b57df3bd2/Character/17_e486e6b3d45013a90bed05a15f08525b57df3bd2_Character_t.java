 
 import javax.servlet.http.HttpServletRequest;
 
 /*******************************************************
 * Abstract character class
 *   the requestAction function needs to be implemented 
 *   by child classes
 *******************************************************/
 public abstract class Character {
     String name, bio;
     int level, health, maxHealth, strength, agility, magic;
     int timesAttacked, timesSwitchedToStrength, timesSwitchedToAgility, timesSwitchedToMagic;
     Item[] itemsHeld;
     Item weapon, armor;
     
     Character(String name, String bio, int level, int health, int strength, int agility, int magic, Item[] itemsHeld, Item weapon, Item armor, int timesAttacked, int timesSwitchedToStrength, int timesSwitchedToAgility, int timesSwitchedToMagic)
     {
         this.name = name;
         this.bio = bio;
         this.level = level; //should be 1 for logged in uers
        this.maxHealth = health;
         this.health = health;
         this.strength = strength;
         this.agility = agility;
         this.magic = magic;
         this.itemsHeld = itemsHeld;
         this.weapon = weapon;
         this.armor = armor;
         this.timesAttacked = timesAttacked;
         this.timesSwitchedToStrength = timesSwitchedToStrength;
         this.timesSwitchedToAgility = timesSwitchedToAgility;
         this.timesSwitchedToMagic = timesSwitchedToMagic;
     }
     
     /****************************************************
      * Abstracted function for getting the action
      * @return an actionEnum for the next move
      ***************************************************/
     abstract actionEnum requestAction(HttpServletRequest request);
     
     
     /***************************************************
      * Equip an item to the character
      * @param newItem the item to be equipped
      **************************************************/
     void equipItem(Item newItem)
     {
         if (newItem.getType() == 1)
         {
             weapon = newItem;
         }
         else if (newItem.getType() == 2)
         {
             armor = newItem;
         }
         else //item is a consumable
         {
             //do nothing
         }
     }
     
     /***************************************************
      * Use a one time use item
      * @param itemToUse the item to be used
      **************************************************/
     void useItem(Item itemToUse)
     {
         if (itemToUse.getType() == 3)
         {
             health = health + itemToUse.getHeal();
             if (health > maxHealth)
             {
                 health = maxHealth;
             }
         }
         else //item is not a consumable
         {
             //do nothing
         }
     }
     
     String getName()
     {
         return name;
     }
     
     String getBio()
     {
         return bio;
     }
     
     void setLevel(int level)
     {
         this.level = level;
     }
     int getLevel()
     {
         return level;
     }
     
     void setHealth(int health)
     {
         this.health = health;
     }
     int getHealth()
     {
         return health;
     }
     
     void setMaxHealth(int maxHealth)
     {
         this.maxHealth = maxHealth;
     }
     int getMaxHealth()
     {
         return maxHealth;
     }
     
     void setStrength(int strength)
     {
         this.strength = strength;
     }
     int getStrength()
     {
         return strength;
     }
     
     void setAgility(int agility)
     {
         this.agility = agility;
     }
     int getAgility()
     {
         return agility;
     }
     
     void setMagic(int magic)
     {
         this.magic = magic;
     }
     int getMagic()
     {
         return magic;
     }
     
 }
