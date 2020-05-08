 package lad.data;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import lad.db.EXPManager;
 import lad.db.MySQLDB;
 
 /**
  * Data handler for modifiers
  *
  * @author msflowers
  */
 public class Modifier
 {
     /**
      * Target for this modifier
      */
     private ModifierTarget target;
 
     /**
      * Rarity of this modifier (1-9)
      */
     private int rarity;
 
     /**
      * The number of battles this modifier will survive for
      */
     private int battles;
 
     /**
      * The initial multiplier for battles this modifier was good for
      */
     private int initialMultiplier;
 
     /**
      * Owner of the modifier
      */
     private int owner;
 
     /**
      * ID of the modifier
      */
     private int ID;
 
     /**
      * Trainer that has this modifier equipped (no DB)
      */
     private Trainer equipped = null;
 
     /**
      * Prepared statement used to insert a new modifier
      */
     private static PreparedStatement insertStmt = null;
 
     /**
      * Prepared statement used to delete a used modifier
      */
     private static PreparedStatement deleteStmt = null;
 
     /**
      * Prepared statement used to update a modifier's battles
      */
     private static PreparedStatement commitStmt = null;
 
     /**
      * Ctor (from db)
      *
      * @param n_ID       ID of the modifier
      * @param n_target   Target for the modifier
      * @param n_rarity   Rarity of the modifier
      * @param n_owner    Owner of the modifier
      * @param n_battles  Number of battles left
      * @param n_initial  Initial multiplier for battles
      */
     public Modifier( int n_ID, int n_target, int n_rarity, int n_owner,
                      int n_battles, int n_initial )
     {
         ID = n_ID;
         target = ModifierTarget.fromInt( n_target );
         rarity = n_rarity;
         owner = n_owner;
         battles = n_battles;
         initialMultiplier = n_initial;
     }
 
     /**
      * Prepares all of the prepared statements
      *
      * @throws SQLException If an error occurs when preparing the statements
      */
     public static void prepareStatements() throws SQLException
     {
         Connection conn = MySQLDB.getConn();
 
         deleteStmt = conn.prepareStatement( "DELETE FROM MODIFIERS WHERE ID " +
                                             "= ?" );
         insertStmt = conn.prepareStatement(
                         "INSERT INTO MODIFIERS VALUES( NULL, ?, ?, ?, ?, ? )",
                         Statement.RETURN_GENERATED_KEYS );
         commitStmt = conn.prepareStatement( "UPDATE MODIFIERS SET BATTLES = " +
                                             "?, TARGET = ?, RARITY = ?, " +
                                             "OWNER = ? WHERE ID = ?" );
     }
 
     /**
      * Returns the target of this modifier
      *
      * @return Target
      */
     public ModifierTarget getTarget()
     {
         return target;
     }
 
     /**
      * Sets the target of this modifier
      *
      * @param target Target to set
      */
     public void setTarget( ModifierTarget target )
     {
         this.target = target;
         commit();
     }
 
     /**
      * Returns the rarity of this modifier
      *
      * @return Rarity
      */
     public int getRarity()
     {
         return rarity;
     }
 
     /**
      * Sets the rarity of this modifier
      *
      * @param rarity Rarity to set
      */
     public void setRarity( int rarity )
     {
         this.rarity = rarity;
         commit();
     }
 
     /**
      * Returns the owner of this modifier
      *
      * @return Owner
      */
     public int getOwner()
     {
         return owner;
     }
 
     /**
      * Sets the owner of this modifier
      *
      * @param owner Owner to set
      */
     public void setOwner( int owner )
     {
         this.owner = owner;
         commit();
     }
 
     /**
      * Returns the trainer that has this modifier equipped
      *
      * @return Trainer that has this modifier equipped
      */
     public Trainer getEquipped()
     {
         return equipped;
     }
 
     /**
      * Sets the trainer that has this modifier equipped.
      *
      * Null is an acceptable parameter which indicates that no trainer has the
      * modifier equipped.
      *
      * @param equipped Trainer that has this modifier, or null
      */
     public void setEquipped( Trainer equipped )
     {
         this.equipped = equipped;
     }
 
     /**
      * Returns the number of battles this modifier will survive through.
      *
      * @return Battles
      */
     public int getBattles()
     {
         return battles;
     }
 
     /**
      * Sets the remaining number of battles this modifier has.
      * 
      * If the parameter is below 0 it is automatically set to 0.
      *
      * @param battles Remaining battles to set
      */
     public void setBattles( int battles )
     {
         int acceptedBattles = battles < 0 ? 0 : battles;
         if( this.battles == acceptedBattles )
         {
             return;
         }
         this.battles = acceptedBattles;
         commit();
     }
 
     /**
      * @return The initial multiplier of battles
      */
     public int getInitialMultiplier()
     {
         return initialMultiplier;
     }
 
     /**
      * @return the ID
      */
     public int getID()
     {
         return ID;
     }
 
     /**
      * Reduces battles by one.
      *
      * Warning: This function will not commit the data to the DB.  This must
      * be performed by calling commit.
      *
      * @see lad.java.Modifier#commit()
      */
     public void reduceBattles()
     {
         this.battles--;
     }
 
     /**
      * Commits battles for this modifier.
      *
      * Called whenever this modifier is done being constantly modified in order
      * to commit the changes to the DB.
      *
      * @see lad.java.Modifier#reduceBattles()
      */
     public void commit()
     {
         try
         {
             commitStmt.setInt( 1, battles );
             commitStmt.setInt( 2, target.getValue() );
             commitStmt.setInt( 3, rarity );
             commitStmt.setInt( 4, owner );
             commitStmt.setInt( 5, ID );
 
             MySQLDB.delaySQL( commitStmt );
         }
         catch( SQLException x )
         {
             System.err.println( "Error while setting minion level." +
                                 x.toString() );
             System.exit( -1 );
         }
     }
 
     /**
      * Creates a new modifier from the given parameters
      *
      * @param targ Target of the modifier
      * @param rare Rarity of the modifier
      * @param owner Owner of the modifier
      * @param battleMult Multiplier for the number of battles
      * @return The created modifier
      */
     public static Modifier create( int targ, int rare, int owner,
                                    int battleMult )
     {
         float preMultBattles = calculateSurvival( rare );
         float multiplier = multiplierFromInt( battleMult );
         int battles = Math.round( preMultBattles * multiplier );
 
         Modifier adder = new Modifier( 0, targ, rare, owner,
                                       battles, battles );
 
         ResultSet generatedKeys;
         try
         {
             // Set statement values
             insertStmt.setInt( 1, owner );
             insertStmt.setInt( 2, targ );
             insertStmt.setInt( 3, rare );
             insertStmt.setInt( 4, battles );
             insertStmt.setInt( 5, battleMult );
 
             // Validate it works/run it
             int affectedRows = insertStmt.executeUpdate();
             if( affectedRows == 0 )
             {
                 throw new SQLException(
                         "Creating modifier failed, no rows affected." );
             }
 
             // Get the ID
             generatedKeys = insertStmt.getGeneratedKeys();
             if( generatedKeys.next() )
             {
                 adder.ID = generatedKeys.getInt( 1 );
             }
             else
             {
                 throw new SQLException(
                         "Creating trainer failed, no key returned." );
             }
         }
         catch( SQLException e )
         {
             System.err.println( "Error while creating minion: " + e.toString() );
             System.exit( -1 );
         }
         return adder;
     }
 
     /**
      * Destroys the trainer in the database
      */
     void destroy()
     {
         // Ensure the delete statement is prepared
         try
         {
             deleteStmt.setInt( 1, ID );
 
             // Run the delete
             int affectedRows = deleteStmt.executeUpdate();
 
             if( affectedRows == 0 )
             {
                 throw new SQLException( "No rows were deleted." );
             }
         }
         catch( SQLException e )
         {
             System.err.println( "Error while deleting modifier: " +
                                 e.toString() );
             System.exit( -1 );
         }
 
         owner = ID = initialMultiplier = battles = rarity = 0;
         target = ModifierTarget.InvalidTarget;
     }
 
     /**
      * Calculates the number of battles a given rarity will last.
      *
      * @param rare The rarity to calculate for
      * @return # of battles the modifier will survive for.
      */
     public static float calculateSurvival( int rare )
     {
         switch( rare )
         {
             case 1:
                 return 10;
             case 2:
                 return 25;
             case 3:
                 return 40;
             case 4:
                 return 60;
             case 5:
                 return 100;
             case 6:
                 return 200;
             case 7:
                 return 500;
             case 8:
                 return 1000;
             case 9:
                 return 10000;
         }
 
         throw new IndexOutOfBoundsException( "Invalid rarity: " + rare );
     }
 
     /**
      * Returns a string for a given rarity
      *
      * @param rare The rarity to calculate for
      * @return Human readable string of the given rarity.
      */
     public static String rarityString( int rare )
     {
         switch( rare )
         {
             case 1:
                 return "Slave's ";
             case 2:
                 return "Weak ";
             case 3:
                 return "Minion's ";
             case 4:
                 return "Standard ";
             case 5:
                 return "Trainer's ";
             case 6:
                 return "Powerful ";
             case 7:
                 return "User's ";
             case 8:
                 return "Glorious ";
             case 9:
                 return "Master's ";
         }
 
         throw new IndexOutOfBoundsException( "Invalid rarity: " + rare );
     }
     
     /**
      * Converts an integer multiplier to a float
      * 
      * @param mult Multiplier to convert ( 0 - 12 )
      * @return A float multiplier from 1.0 to 10.0
      */
     public static float multiplierFromInt( int mult )
     {
         switch( mult )
         {
             case 0:
                 return 1.0f;
             case 1:
                 return 1.1f;
             case 2:
                 return 1.3f;
             case 3:
                 return 1.5f;
             case 4:
                 return 1.7f;
             case 5:
                 return 2.0f;
             case 6:
                 return 2.5f;
             case 7:
                 return 3.3f;
             case 8:
                 return 4.2f;
             case 9:
                 return 5.4f;
             case 10:
                 return 6.8f;
             case 11:
                 return 8.3f;
             case 12:
                 return 10.0f;
         }
         throw new IndexOutOfBoundsException( "Invalid multiplier: " + mult );
     }
 
     /**
      * Returns a string for a given multiplier
      *
      * @param mult The multiplier to calculate for
      * @return Human readable string of the given rarity.
      */
     public static String multiplierString( int mult )
     {
         switch( mult )
         {
             case 0:
                 return "";
             case 1:
                 return "Renovated ";
             case 2:
                 return "Hardened ";
             case 3:
                 return "Improved ";
             case 4:
                 return "Enhanced ";
             case 5:
                 return "Augmented ";
             case 6:
                 return "Refined ";
             case 7:
                 return "Upgraded ";
             case 8:
                 return "Advanced ";
             case 9:
                 return "Fine-Tuned ";
             case 10:
                 return "Sturdy ";
             case 11:
                 return "Perfected ";
             case 12:
                 return "Neverending ";
         }
         throw new IndexOutOfBoundsException( "Invalid multiplier: " + mult );
     }
 
     /**
      * Generates a string representing the modifier.
      *
      * Concatenates the following:
      * Rarity String,
      * Multiplier String,
      * Mod of,
      * Modifier Target String
      *
      * @see lad.java.Modifier#rarityString(int)
      * @see lad.java.Modifier#multiplierString(int)
      * @see lad.java.ModifierTarget#toString()
      *
      * @param target Target the modifier affects
      * @param mult Multiplier the modifier initially had
      * @param rare Rarity of the modifier
      * @return Generated string
      */
     public static String generateString( ModifierTarget target, int mult,
                                                                 int rare )
     {
         String ret = target == ModifierTarget.Proficiency ?
                                "" : rarityString( rare );
         ret += multiplierString( mult );
         ret += "Mod of ";
         ret += target.toString();
         return ret;
     }
 
     /**
      * Returns a string representation of this modifier.
      *
      * @see lad.java.Modifier#generateString(lad.java.ModifierTarget, int, int)
      * @return Generated string
      */
     @Override
     public String toString()
     {
         return generateString( target, initialMultiplier, rarity );
     }
 
     /**
      * Calculates how much experience will be awarded to a trainer
      *
      * @param won Whether the trainer won the battle or not
      * @return EXP to award
      */
     public int calculateBattleEXP( boolean won )
     {
         if( target == ModifierTarget.Proficiency )
         {
             return 2;
         }
         switch( rarity )
         {
             // 1-3 require a win and yield 1 XP
             case 1:
             case 2:
             case 3:
                 return won ? 1 : 0;
             case 4: // Awards 1 for win, 50/50 for loss
                 if( won )
                 {
                     return 1;
                 }
                 return Math.random() > 0.5 ? 0 : 1;
             case 5: // Awards 1 XP
                 return 1;
             case 6: // Awards 50/50 1/2 for win, 1 for loss
                 if( !won )
                 {
                     return 1;
                 }
                 return Math.random() > 0.5 ? 2 : 1;
             case 7: // Awards 2 for win, 1 for loss
                 return won ? 2 : 1;
             case 8: // Awards 2 for win, 50/50 1/2 for loss
                 if( won )
                 {
                     return 2;
                 }
                 return Math.random() > 0.5 ? 2 : 1;
             case 9: // Always rewards double XP
                 return 2;
         }
 
         return 0;
     }
 
     /**
      * Called after a battle with this modifier equipped
      *
      * @param won Set to true if the owner won the battle
      * @param weapon Weapon that equipped this modifier
      */
     public void battled( boolean won, Weapon weapon )
     {
         // Abort if the modifier has no more uses
         if( getBattles() == 0 )
         {
             return;
         }
 
         // Update the battle count
         setBattles( getBattles() - 1 );
         
         // Abort if the battle yielded no exp
         int exp = calculateBattleEXP( won );
         if( exp == 0 )
         {
             return;
         }
 
         // Grant the EXP
         int user = getOwner();
         UserExpTarget generalTarget = UserExpTarget.generalFromWeapon( weapon );
         UserExpTarget specificTarget = UserExpTarget.specificFromWeapon(
                                        weapon );
         EXPManager.grantUserEXP( user, generalTarget, target, exp );
         EXPManager.grantUserEXP( user, specificTarget, target, exp * 2 );
 
     }
 }
