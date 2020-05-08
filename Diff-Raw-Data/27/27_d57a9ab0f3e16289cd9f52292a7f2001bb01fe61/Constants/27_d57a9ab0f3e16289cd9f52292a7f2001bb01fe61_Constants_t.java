 /*
  * DISASTEROIDS | Networking
  * Constants.java
  */
 package disasteroids.networking;
 
 import disasteroids.Alien;
 import disasteroids.Asteroid;
 import disasteroids.BlackHole;
 import disasteroids.Bonus;
import disasteroids.BonusAsteroid;
 import disasteroids.Deathmatch;
 import disasteroids.GameMode;
 import disasteroids.GameObject;
 import disasteroids.LinearGameplay;
 import disasteroids.Ship;
 import disasteroids.Station;
 import disasteroids.WaveGameplay;
 import java.io.DataInputStream;
 import java.io.IOException;
 
 /**
  * Storage class for network constants.
  * @author Phillip Cohen
  * @since January 13, 2008
  */
 public class Constants
 {
 
     /**
      * The version of our net code protocol. Bump to ensure older clients don't connect and cause havoc.
      * @since April 11, 2008
      */
     public static final int NETCODE_VERSION = 4;
 
     /**
      * The default port that the server runs on.
      * @since December 29, 2007
      */
     public static final int DEFAULT_PORT = 1024;
 
     /**
      * Time (in seconds) that must elapse before a machine is considered to have timed out.
      * @since January 13, 2008
      */
     public static final int TIMEOUT_TIME = 15;
 
     /**
      * Time (in seconds) between intervalLogic() calls.
      * @since January 13, 2008
      */
     public static final int INTERVAL_TIME = 5;
 
     /**
      * The max size of a packet (bytes).
      * @since January 22, 2008
      */
     public static final int MAX_PACKET_SIZE = 1024;
 
     /**
      * Size of the overhead for a packet in a series.
      * @since January 22, 2008
      */
     public static final int MULTIPACKET_HEADER_SIZE = ( 4 * Integer.SIZE / 8 );
 
     /**
      * Remaining size for data for packet in a series.
      * @since January 22, 2008
      */
     public static final int MULTIPACKET_DATA_SIZE = MAX_PACKET_SIZE - MULTIPACKET_HEADER_SIZE;
 
     public static enum GameModeTIDs
     {
 
         LINEAR( LinearGameplay.class ), WAVE( WaveGameplay.class ), DEATHMATCH( Deathmatch.class );
 
         final Class myClass;
 
         private GameModeTIDs( Class myClass )
         {
             this.myClass = myClass;
         }
     }
 
     public static enum GameObjectTIDs
     {
 
        ALIEN, ASTEROID, BONUS_ASTEROID, BLACK_HOLE, BONUS, SHIP, STATION;
 
     }
 
     public static int parseGameMode( GameMode m )
     {
         if ( m instanceof LinearGameplay )
             return GameModeTIDs.LINEAR.ordinal();
         else if ( m instanceof WaveGameplay )
             return GameModeTIDs.WAVE.ordinal();
         else if ( m instanceof Deathmatch )
             return GameModeTIDs.DEATHMATCH.ordinal();
         else
             throw new IllegalArgumentException( "Unknown game mode: " + m + "." );
     }
 
     public static GameMode parseGameMode( int type, DataInputStream stream ) throws IOException
     {
         switch ( GameModeTIDs.values()[type] )
         {
             case LINEAR:
                 return new LinearGameplay( stream );
             case WAVE:
                 return new WaveGameplay( stream );
             case DEATHMATCH:
                 return new Deathmatch( stream );
             default:
                 throw new IllegalArgumentException( "Unknown game mode: " + type + "." );
         }
     }
 
     public static int parseGameObject( GameObject o )
     {
         if ( o instanceof Alien )
             return GameObjectTIDs.ALIEN.ordinal();
        else if ( o instanceof BonusAsteroid )
            return GameObjectTIDs.BONUS_ASTEROID.ordinal();
        else if ( o instanceof Asteroid )
             return GameObjectTIDs.ASTEROID.ordinal();
         else if ( o instanceof Bonus )
             return GameObjectTIDs.BONUS.ordinal();
         else if ( o instanceof Station )
             return GameObjectTIDs.STATION.ordinal();
         else if ( o instanceof Ship )
             return GameObjectTIDs.SHIP.ordinal();
         else if ( o instanceof BlackHole )
             return GameObjectTIDs.BLACK_HOLE.ordinal();
         else
             throw new IllegalArgumentException( "Unknown game object: " + o + "." );
     }
 }
