 package roguelike.rpg.sisyphean;
 
 import java.util.ArrayList;
 import sofia.app.ShapeScreen;
 import sofia.graphics.ImageShape;
 
 /**
  *  The abstract class for every character in the game.
  *  This could be a player, an enemy and etc.
  *
  *  TODO: how to loop through an animation sprite using sofia?!
  *
  *  @author Artur
  *  @version Nov 3, 2012
  */
 abstract public class Character
 {
     /////FIELDS-----------------------------------------------------------------
 
     // Name
     private String name;
 
     // Level
     private int level;
 
     // Health
     private float health;
 
     // Mana
     private float mana;
 
     // Stamina
     private float stamina;
 
     // Statuses
     private float maxHealth;
     private float maxMana;
     private float maxStamina;
     private float strength;
     private float defense;
     private float dexterity;
     private float intelligence;
 
     // Type
     protected enum PlayerType { WARRIOR, WIZARD, ARCHER };
     protected enum EnemyType { ZOMBIE, HARPY, RAT };
 
     //Skills.
     // TODO: is an ArrayList the best data structure for this?
     private ArrayList<Skill> skills;
 
 
     // Sprites
 
     /** The sprite to use when not in battle */
     private ImageShape mazeSprite;
 
     /** The sprite to use while in battle */
     private ImageShape battleSprite;
 
 
     /////METHODS----------------------------------------------------------------
 
     // Abstract
 
     /**
      * The method called to draw this character on the screen.
      * TODO: Do we need this if we're using sophia?
      *
      * @param screen The screen to draw on.
      */
     abstract public void drawMe(ShapeScreen screen);
 
 
     // Getters and setters
 
     /**
      * Gets the name of the character.
      * @return String The name of the character
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * Sets the name of the character.
      * @param name The new name of the character
      */
     public void setName(String name)
     {
         this.name = name;
     }
 
     /**
      * Retrieves the characters level.
      * @return int The integer value of the level that the character is
      */
     public int getLevel()
     {
         return level;
     }
 
     /**
      * Sets the level of the character.
      * @param level The new level of the character
      */
     public void setLevel(int level)
     {
         this.level = level;
     }
 
     /**
      * Retrieves the max health of the character.
      * @return float The value returned for your health
      */
     public float getMaxHealth()
     {
         return maxHealth;
     }
 
     /**
      * Sets the max health of the character.
      * @param maxHealth the new max health for the character
      */
     public void setMaxHealth(float maxHealth)
     {
         float percentage;
 
         if (getMaxHealth() > 0)
         {
             percentage = getHealth() / getMaxHealth();
         }
         else
         {
             percentage = 1.0f;
         }
 
         this.maxHealth = maxHealth;
 
         // Update the health to continue as the same percentage of the max.
         setHealth( getMaxHealth() * percentage );
     }
 
     /**
      * Retrieves the current health of the character.
      * @return float The current health value
      */
     public float getHealth()
     {
         return health;
     }
 
     /**
      * Sets the current health of the character.
      * @param health The new current health
      */
     public void setHealth(float health)
     {
        if ( health > getMaxHealth() )
        {
            this.health = getMaxHealth();
        }
        else
        {
            this.health = health;
        }

     }
 
     /**
      * Retrieves the max mana.
      * @return float The max mana of the character
      */
     public float getMaxMana()
     {
         return maxMana;
     }
 
     /**
      * Sets the max mana of the character.
      * @param maxMana The new max mana
      */
     public void setMaxMana(float maxMana)
     {
         float percentage;
 
         if (getMaxMana() > 0)
         {
             percentage = getMana() / getMaxMana();
         }
         else
         {
             percentage = 1.0f;
         }
 
         this.maxMana = maxMana;
 
         // Update the mana to continue as the same percentage of the max.
         setMana( getMaxMana() * percentage );
     }
 
     /**
      * The current mana of the character.
      * @return float The characters current mana
      */
     public float getMana()
     {
         return mana;
     }
 
     /**
      * Sets the current mana of the character.
      * @param mana The new current mana
      */
     public void setMana(float mana)
     {
        if ( mana > getMaxMana() )
        {
            this.mana = getMaxMana();
        }
        else
        {
            this.mana = mana;
        }
     }
 
     /**
      * Retrieves the max stamina of the character.
      * @return float The stamina value of the character
      */
     public float getMaxStamina()
     {
         return maxStamina;
     }
 
     /**
      * Sets the max stamina of the character.
      * @param maxStamina The new max stamina of the character
      */
     public void setMaxStamina(float maxStamina)
     {
         float percentage;
 
         if (getMaxStamina() > 0)
         {
             percentage = getStamina() / getMaxStamina();
         }
         else
         {
             percentage = 1.0f;
         }
 
         this.maxStamina = maxStamina;
 
         // Update the stamina to continue as the same percentage of the max.
         setStamina( getMaxStamina() * percentage );
     }
 
     /**
      * Retrieves the current stamina of the character.
      * @return float The current stamina of the character
      */
     public float getStamina()
     {
         return stamina;
     }
 
     /**
      * Sets the current stamina of the character.
      * @param stamina The new current stamina
      */
     public void setStamina(float stamina)
     {
        if ( stamina > getMaxStamina() )
        {
            this.stamina = getMaxStamina();
        }
        else
        {
            this.stamina = stamina;
        }
     }
 
     /**
      * Returns the current strength of the character.
      * @return float The current strength of the character
      */
     public float getStrength()
     {
         return strength;
     }
 
     /**
      * Sets the current strength of the character.
      * @param strength The new current strength of the character
      */
     public void setStrength(float strength)
     {
         this.strength = strength;
     }
 
     /**
      * Returns the current defense value of the character.
      * @return float The current defense value
      */
     public float getDefense()
     {
         return defense;
     }
 
     /**
      * Sets the current defense value of the character
      * @param defense The new current defense value for the character
      */
     public void setDefense(float defense)
     {
         this.defense = defense;
     }
 
     /**
      * Returns the current dexterity of the character.
      * @return float The current dexterity of the character
      */
     public float getDexterity()
     {
         return dexterity;
     }
 
     /**
      * Sets the current dexterity of the character.
      * @param dexterity The new current dexterity of the character
      */
     public void setDexterity(float dexterity)
     {
         this.dexterity = dexterity;
     }
 
     /**
      * Returns the intelligence value of the character.
      * @return float The current intelligence value
      */
     public float getIntelligence()
     {
         return intelligence;
     }
 
     /**
      * Sets the current intelligence value of the character.
      * @param intelligence The new current intelligence of the character
      */
     public void setIntelligence(float intelligence)
     {
         this.intelligence = intelligence;
     }
 
     /**
      * Returns the skills the character has.
      * @return ArrayList<Skill> Returns all the skills of the character
      */
     public ArrayList<Skill> getSkills()
     {
         return skills;
     }
 
     /**
      * Returns an image of the sprite.
      * @return ImageShape The image of the sprite
      */
     public ImageShape getMazeSprite()
     {
         return mazeSprite;
     }
 
     /**
      * Sets the image of the sprite.
      * @param mazeSprite Sets the maze sprite image
      */
     public void setMazeSprite(ImageShape mazeSprite)
     {
         this.mazeSprite = mazeSprite;
     }
 
     /**
      * Returns the battle sprite image.
      * @return ImageShape the battle sprite image
      */
     public ImageShape getBattleSprite()
     {
         return battleSprite;
     }
 
     /**
      * Sets the battle sprite image.
      * @param battleSprite The battle sprite image
      */
     public void setBattleSprite(ImageShape battleSprite)
     {
         this.battleSprite = battleSprite;
     }
 
 }
