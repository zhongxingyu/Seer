 package lad.db;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import lad.data.GameException;
 import lad.data.Minion;
 import lad.data.Trainer;
 
 /**
  * Manages all of the trainers (and consequently their minions)
  *
  * @author msflowers
  */
 public class TrainerManager extends DBManager
 {
     /**
      * The internal list of trainers.
      */
     private LinkedList< Trainer > trainers = new LinkedList<>();
 
     /**
      * Private ctor
      */
     private TrainerManager()
     {
     }
 
     /**
      * Returns the table profiles to load
      *
      * @return A list with the minion and trainer tables to load
      */
     @Override
     public TableProfile[] profiles()
     {
         return new TableProfile[]{
             new TableProfile(){
                 @Override
                 public String tableName()
                 {
                     return "MINIONS";
                 }
                 @Override
                 public String createString()
                 {
                     return
                         "CREATE TABLE `MINIONS` (" +
                         "`ID` int(10) unsigned NOT NULL AUTO_INCREMENT," +
                         "`owner` int(10) unsigned NOT NULL," +
                         "`exp` int(10) unsigned NOT NULL," +
                         "`level` int(10) unsigned NOT NULL," +
                         "PRIMARY KEY (`ID`)" +
                         ") ENGINE = MyISAM DEFAULT CHARSET=latin1";
                 }
                 @Override
                 public String[] tableHeaders()
                 {
                     return new String[] { "ID", "owner", "exp", "level" };
                 }
                 @Override
                 public void loadRow( ResultSet rs ) throws SQLException
                 {
                     // Not run because loadData is false
                 }
                 @Override
                 public void postinit() throws SQLException
                 {
                     Minion.prepareStatements();
                 }
                 @Override
                 public boolean loadData()
                 {
                     return false;
                 }
             },
             new TableProfile(){
                 @Override
                 public String tableName()
                 {
                     return "TRAINERS";
                 }
                 @Override
                 public String createString()
                 {
                     return
                         "CREATE TABLE `TRAINERS` (" +
                         "`ID` int(10) unsigned NOT NULL AUTO_INCREMENT," +
                         "`owner` int(10) unsigned NOT NULL," +
                         "`exp` int(10) unsigned NOT NULL," +
                         "`level` int(10) unsigned NOT NULL," +
                         "PRIMARY KEY (`ID`)" +
                         ") ENGINE = MyISAM DEFAULT CHARSET=latin1";
                 }
                 @Override
                 public String[] tableHeaders()
                 {
                     return new String[] { "ID", "owner", "exp", "level" };
                 }
                 @Override
                 public void loadRow( ResultSet rs ) throws SQLException
                 {
                     int ID = rs.getInt( 1 );
                     int owner = rs.getInt( 2 );
                     int exp = rs.getInt( 3 );
                     int level = rs.getInt( 4 );
 
                     Trainer trainer = new Trainer( ID, owner, exp, level );
                     trainers.add( trainer );
                     trainer.load();
                 }
                 @Override
                 public void postinit() throws SQLException
                 {
                     Trainer.prepareStatements();
                 }
                 @Override
                 public boolean loadData()
                 {
                     return true;
                 }
             }
         };
     }
 
     /**
      * Returns a list of trainers belonging to a user
      *
      * @param userid The ID of the user to get trainers for
      * @return List of trainers (whether empty or populated)
      */
     public LinkedList< Trainer > getTrainersByUser( int userid )
     {
         LinkedList< Trainer > ret = new LinkedList<>();
         ListIterator< Trainer > iter = trainers.listIterator();
 
         while( iter.hasNext() )
         {
             Trainer current = iter.next();
             if( current.getOwner() == userid )
             {
                 ret.add( current );
             }
         }
 
         return ret;
     }
 
     /**
      * Returns a specific trainer by its ID
      *
      * @param id The ID of the trainer to search for
     * @return Either the trainer if it is found
      * @throws GameException Thrown if the given ID is not found
      */
     public Trainer getTrainerByID( int id )
     {
         ListIterator< Trainer > iter = trainers.listIterator();
         while( iter.hasNext() )
         {
             Trainer current = iter.next();
             if( current.getID() == id )
             {
                 return current;
             }
         }
         throw new GameException( 1, "Trainer not found:" + id );
     }
 
     /**
      * Creates a trainer for the specified user
      *
      * @param userid The ID of the user to create the trainer for
      */
     public void addTrainer( int userid )
     {
         Trainer creation = Trainer.create( userid );
         trainers.add( creation );
     }
 
     /**
      * Returns the singleton
      *
      * @return Singleton
      */
     public static TrainerManager getInstance()
     {
         return TrainerManagerHolder.INSTANCE;
     }
 
     private static class TrainerManagerHolder
     {
         private static final TrainerManager INSTANCE = new TrainerManager();
     }
 
 }
