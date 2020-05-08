 package lad.data;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import lad.db.MySQLDB;
 
 /**
  * Data handler for trainers
  */
 public class Trainer
 {
     /**
      * Current amount of experience the trainer has
      */
     private int exp = 0;
 
     /**
      * Current level the trainer is
      */
     private int level = 0;
 
     /**
      * ID of the trainer
      */
     private int ID;
 
     /**
      * Owner of the trainer. Referenced from the user ID from the
      * PHP server.
      */
     private int owner;
 
     /**
      * Set to true when this trainer is actually an NPC.
      */
     private boolean isNPC;
 
     /**
      * Current state (arena battling) the trainer is in
      */
     private BattleState battleState = BattleState.NoBattle;
 
     /**
      * List of minions that this trainer owns
      */
     private List< Minion > minionList = new LinkedList<>();
 
     /**
      * Prepared statement for pulling all the minions for a trainer
      */
     private static PreparedStatement minionStmt = null;
 
     /**
      * Statement for inserting a new trainer
      */
     private static PreparedStatement insertStmt = null;
 
     /**
      * Statement for deleting a trainer
      */
     private static PreparedStatement deleteStmt = null;
 
     /**
      * Statement for deleting a trainer's minions
      */
     private static PreparedStatement deleteMinionStmt = null;
 
     /**
      * Statement for updating Level
      */
     private static PreparedStatement updateLevelStmt = null;
 
     /**
      * Statement for updating Exp
      */
     private static PreparedStatement updateExpStmt = null;
 
     /**
      * Statement for updating Owner
      */
     private static PreparedStatement updateOwnerStmt = null;
 
     /**
      * Statement for adjusting Exp
      */
     private static PreparedStatement adjustExpStmt = null;
 
     /**
      * Prepares all of the prepared statements
      *
      * @throws SQLException If an error occurs when preparing the statements
      */
     public static void prepareStatements() throws SQLException
     {
         Connection conn = MySQLDB.getConn();
 
         final String pre = "UPDATE TRAINERS SET ";
         final String post = " WHERE ID = ?";
 
         updateLevelStmt = conn.prepareStatement( pre + "LEVEL = ?" + post );
         updateExpStmt = conn.prepareStatement( pre + "EXP = ?" + post );
         updateOwnerStmt = conn.prepareStatement( pre + "OWNER = ?" + post );
         adjustExpStmt = conn.prepareStatement( pre + "LEVEL = ?, EXP = ?" +
                                                post );
         deleteStmt = conn.prepareStatement( "DELETE FROM TRAINERS" + post );
         deleteMinionStmt = conn.prepareStatement( "DELETE FROM MINIONS WHERE " +
                                                   "OWNER = ?" );
 
         minionStmt = conn.prepareStatement( "SELECT * FROM MINIONS WHERE " +
                                             "OWNER = ?" );
         insertStmt = conn.prepareStatement(
                         "INSERT INTO TRAINERS VALUES( NULL, ?, 0, 0 )",
                         Statement.RETURN_GENERATED_KEYS );
     }
 
     /**
      * Ctor (from DB)
      *
      * @param n_exp   Experience of the trainer
      * @param n_level Level of the trainer
      * @param n_ID    ID of the trainer
      * @param n_owner Owner of the trainer
      */
     public Trainer( int n_ID, int n_owner, int n_exp, int n_level )
     {
         exp = n_exp;
         level = n_level;
         ID = n_ID;
         owner = n_owner;
         isNPC = false;
     }
 
     /**
      * Ctor (add to DB)
      *
      * @param n_owner Owner of the trainer to add
      */
     private Trainer( int n_owner )
     {
         owner = n_owner;
         isNPC = false;
     }
 
     /**
      * Ctor (for NPCs)
      *
      * @param npc Throws an exception if this is not true
      * @throws IndexOutOfBoundsException Thrown if npc is not true
      */
     public Trainer( boolean npc ) throws IndexOutOfBoundsException
     {
         if( !npc )
         {
             throw new IndexOutOfBoundsException( "Making NPC trainer false." );
         }
 
         isNPC = true;
         exp = level = ID = owner = 0;
     }
 
     /**
      * Loads minions from DB
      */
     public void load()
     {
         ResultSet result;
         try
         {
             // Set up the statement
            minionStmt.setLong( 1, owner );
 
             // Run the statement
             result = minionStmt.executeQuery();
 
             while( result.next() )
             {
                 // Pull the values
                 int minionid = result.getInt( "id" );
                 int minionexp = result.getInt( "exp" );
                 int minionlevel = result.getInt( "level" );
 
                 // Add the minion to our list
                 minionList.add( new Minion( minionid, minionexp,
                                             minionlevel, ID ) );
             }
         }
         catch( SQLException e )
         {
             System.err.println( "Error while getting trainer minions:" +
                                 e.toString() );
             System.exit( -1 );
         }
     }
 
     /**
      * Get experience
      *
      * @return exp
      */
     public int getExp()
     {
         return exp;
     }
 
     /**
      * Get level
      *
      * @return level
      */
     public int getLevel()
     {
         return level;
     }
 
     /**
      * Get ID
      *
      * @return ID
      */
     public int getID()
     {
         return ID;
     }
 
     /**
      * Get owner
      *
      * @return owner
      */
     public int getOwner()
     {
         return owner;
     }
 
     /**
      * Get whether this trainer is an NPC or not
      *
      * @return True if it is, false otherwise
      */
     public boolean isNPC()
     {
         return this.isNPC;
     }
 
     /**
      * Gets the state of battling the trainer is in.
      *
      * @return Current battle state
      */
     public BattleState getBattleState()
     {
         return battleState;
     }
 
     /**
      * Sets the state of battling this trainer is in
      *
      * @param battleState New state to set
      */
     public void setBattleState( BattleState battleState )
     {
         this.battleState = battleState;
     }
 
     /**
      * Set exp
      *
      * @param value New value of the exp
      */
     public void setExp( int value )
     {
         exp = value;
 
         try
         {
             updateExpStmt.setInt( 1, value );
             updateExpStmt.setInt( 2, ID );
 
             MySQLDB.delaySQL( updateExpStmt );
         }
         catch( SQLException x )
         {
             System.err.println( "Error while setting minion level." +
                                 x.toString() );
             System.exit( -1 );
         }
     }
 
     /**
      * Set level
      *
      * @param value New value of the level
      */
     public void setLevel( int value )
     {
         level = value;
 
         try
         {
             updateLevelStmt.setInt( 1, value );
             updateLevelStmt.setInt( 2, ID );
 
             MySQLDB.delaySQL( updateLevelStmt );
         }
         catch( SQLException e )
         {
             System.err.println( "Error while setting minion level." +
                                 e.toString() );
             System.exit( -1 );
         }
     }
 
     /**
      * Set owner
      *
      * @param value New value of the owner
      */
     public void setOwner( int value )
     {
         owner = value;
 
         try
         {
             updateOwnerStmt.setInt( 1, value );
             updateOwnerStmt.setInt( 2, ID );
 
             MySQLDB.delaySQL( updateOwnerStmt );
         }
         catch( SQLException e )
         {
             System.err.println( "Error while setting minion level." +
                                 e.toString() );
             System.exit( -1 );
         }
     }
 
     /**
      * Gets the list of minions
      *
      * @return minionList
      */
     public List< Minion > getMinions()
     {
         return Collections.unmodifiableList( minionList );
     }
 
     /**
      * Adds a minion to the list
      *
      * @param minion The minion to add to this trainer's list
      */
     public void addMinion( Minion minion )
     {
         minionList.add( minion );
     }
 
     /**
      * Adds exp and updates the level accordingly
      *
      * @param xp The exp to add
      */
     public void adjustExp( int xp )
     {
         exp += xp;
         while( exp >= 10 && level < 130 )
         {
             exp -= 10;
             level++;
         }
 
         if( level == 130 )
         {
             exp = 0;
         }
 
         try
         {
             adjustExpStmt.setInt( 1, level );
             adjustExpStmt.setInt( 2, exp );
             adjustExpStmt.setInt( 3, ID );
 
             MySQLDB.delaySQL( adjustExpStmt );
         }
         catch( SQLException e )
         {
             System.err.println( "Error while setting minion level." +
                                 e.toString() );
             System.exit( -1 );
         }
     }
 
     /**
      * Battles two minions.
      * The losing minion will be destroyed and removed from this trainer.
      *
      * @param minion1 The first minion to battle
      * @param minion2 The second minion to battle
      * @return The minion that lost (is destroyed).
      */
     public Minion battle( Minion minion1, Minion minion2 )
     {
         // Fancy formula to decide a winner
         int level1 = minion1.getLevel();
         int level2 = minion2.getLevel();
         Minion winner, loser, loserCopy;
         int random = (int)( Math.random() * ( level1 + level2 ) );
         if( random <= level1 )
         {
             winner = minion1;
             loser = minion2;
         }
         else
         {
             winner = minion2;
             loser = minion1;
         }
 
         // The amount of exp gained
         int loserXp = loser.getExp();
         int loserLvl = loser.getLevel();
         int totalLoserXp = loser.getTotalEXP();
         int gainedExp = (int)( totalLoserXp * 0.2 );
 
         // Remove the loser from our list
         ListIterator< Minion> iter = minionList.listIterator();
         while( iter.hasNext() )
         {
             Minion minionTester = iter.next();
             if( minionTester.getID() == loser.getID() )
             {
                 iter.remove();
                 break;
             }
         }
 
         // Copy the loser so it can be returned
         loserCopy = new Minion( loser );
 
         // Kill the loser
         loser.destroy();
 
         // Update the winner
         winner.adjustExp( gainedExp );
 
         // Update the trainer
         adjustExp( gainedExp );
 
         return loserCopy;
     }
 
     /**
      * Creates a trainer
      *
      * @param n_owner Owner of the trainer
      * @return The created trainer
      */
     public static Trainer create( int n_owner )
     {
         Trainer trainer = new Trainer( n_owner );
         ResultSet generatedKeys;
         try
         {
             // Set statement values
             insertStmt.setInt( 1, n_owner );
 
             // Validate it works/run it
             int affectedRows = insertStmt.executeUpdate();
             if( affectedRows == 0 )
             {
                 throw new SQLException( 
                         "Creating trainer failed, no rows affected." );
             }
 
             // Get the ID
             generatedKeys = insertStmt.getGeneratedKeys();
             if( generatedKeys.next() )
             {
                 trainer.ID = generatedKeys.getInt( 1 );
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
         return trainer;
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
             deleteMinionStmt.setInt( 1, ID );
 
             // Run the delete
             int affectedRows = deleteStmt.executeUpdate();
 
             if( affectedRows == 0 )
             {
                 throw new SQLException( "No rows were deleted." );
             }
 
             // Minion statement does not need to be checked
             deleteMinionStmt.executeUpdate();
         }
         catch( SQLException e )
         {
             System.err.println( "Error while deleting trainer: " + e.toString() );
             System.exit( -1 );
         }
 
         owner = ID = exp = level = 0;
     }
 
     /**
      * Possible states the trainer can be in for battling
      */
     public enum BattleState
     {
        /**
         * Trainer is not interacting with the arena battles.
         */
         NoBattle,
         /**
          * Trainer is in a queue to join an arena battle.
          */
         InBattleQueue,
         /**
          * Trainer is actively looking for a battle.
          */
         LookingForBattle,
         /**
          * Trainer is in an arena battle.
          */
         InBattle;
 
         /**
          * String representation of the state.
          *
          * Mostly just adds spaces.  Deletes a word in a case.
          *
          * @return String representation
          */
         @Override
         public String toString()
         {
             if( this == NoBattle )
             {
                 return "Not battling";
             }
             if( this == InBattleQueue )
             {
                 return "In queue to battle";
             }
             if( this == LookingForBattle )
             {
                 return "Looking for battle";
             }
             if( this == InBattle )
             {
                 return "In battle";
             }
 
             throw new IndexOutOfBoundsException( "Battle state out of range." );
         }
     };
 }
