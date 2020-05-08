 /*
  * DISASTEROIDS
  * ObjectManager.java
  */
 package disasteroids;
 
 import disasteroids.networking.Constants;
 import disasteroids.networking.Server;
 import disasteroids.networking.ServerCommands;
 import java.awt.Graphics;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * The game's manager that tracks the adding and removal of all objects.
  * @author Phillip Cohen
  */
 public class ObjectManager implements GameElement
 {
     /**
      * The mother map of all game objects. Objects are mapped to their IDs.
      */
     private ConcurrentHashMap<Integer, GameObject> gameObjects = new ConcurrentHashMap<Integer, GameObject>( 500 );
 
     private ConcurrentLinkedQueue<Asteroid> asteroids = new ConcurrentLinkedQueue<Asteroid>();
 
     /**
      * Reference list of all black holes.
      */
     private ConcurrentLinkedQueue<BlackHole> blackHoles = new ConcurrentLinkedQueue<BlackHole>();
 
     /**
      * Reference list of players.
      */
     private ConcurrentLinkedQueue<Ship> players = new ConcurrentLinkedQueue<Ship>();
 
     /**
      * Reference list of all objects that shoot.
      */
     private ConcurrentLinkedQueue<ShootingObject> shootingObjects = new ConcurrentLinkedQueue<ShootingObject>();
 
     /**
      * Reference list all of living objects on the level that the player must destroy (aliens, stations, and so on).
      */
     private ConcurrentLinkedQueue<GameObject> baddies = new ConcurrentLinkedQueue<GameObject>();
 
     public ObjectManager()
     {
     }
 
     public void act()
     {
         for ( int id : gameObjects.keySet() )
             if ( gameObjects.get( id ) != null )
                 gameObjects.get( id ).act();
     }
 
     public void draw( Graphics g )
     {
         for ( int id : gameObjects.keySet() )
         {
             if ( !GameLoop.isRunning() )
                 return;
 
             if ( gameObjects.get( id ) != null && gameObjects.get( id ) instanceof BlackHole == false )
                 gameObjects.get( id ).draw( g );
         }
 
         for ( BlackHole b : blackHoles )
             b.draw( g );
     }
 
     public void addObject( GameObject go )
     {
         gameObjects.put( go.getId(), go );
         if ( go instanceof Asteroid )
             asteroids.add( (Asteroid) go );
         if ( go instanceof BlackHole )
             blackHoles.add( (BlackHole) go );
         if ( go instanceof ShootingObject )
             shootingObjects.add( (ShootingObject) go );
         if ( go instanceof Ship )
             players.add( (Ship) go );
 
         if ( Server.is() )
             ServerCommands.objectCreatedOrDestroyed( go, true );
     }
 
     public void removeObject( GameObject go )
     {
         gameObjects.remove( go.getId() );
         if ( go instanceof Asteroid )
             asteroids.remove( (Asteroid) go );
         if ( go instanceof BlackHole )
             blackHoles.remove( (BlackHole) go );
         if ( go instanceof ShootingObject )
             shootingObjects.remove( (ShootingObject) go );
         if ( go instanceof Ship )
             players.remove( (Ship) go );
 
         if ( Server.is() )
             ServerCommands.objectCreatedOrDestroyed( go, false );
     }
 
     public void clear()
     {
         gameObjects.clear();
         asteroids.clear();
         blackHoles.clear();
         shootingObjects.clear();
         players.clear();
     }
 
     /**
      * Writes <code>this</code> to a stream for client/server transmission.
      */
     public void flatten( DataOutputStream stream ) throws IOException
     {
         stream.writeInt( gameObjects.size() );
         for ( int id : gameObjects.keySet() )
         {
             stream.writeInt( Constants.parseGameObject( gameObjects.get( id ) ) );
             gameObjects.get( id ).flatten( stream );
         }
     }
 
     public void addObjectFromStream( DataInputStream stream ) throws IOException
     {
         switch ( Constants.GameObjectTIDs.values()[stream.readInt()] )
         {
             case ALIEN:
                 addObject( new Alien( stream ) );
                 break;
             case ASTEROID:
                 addObject( new Asteroid(stream));
                 break;
             case BLACK_HOLE:
                 addObject( new BlackHole( stream ) );
                 break;
             case BONUS:
                 addObject( new Bonus( stream ) );
                 break;
             case SHIP:
                 addObject( new Ship( stream ) );
                 break;
             case STATION:
                 addObject( new Station( stream ) );
                 break;
         }
     }
 
     /**
      * Creates <code>this</code> from a stream for client/server transmission.
      */
     public ObjectManager( DataInputStream stream ) throws IOException
     {
         System.out.println( "" );
         int size = stream.readInt();
         for ( int i = 0; i < size; i++ )
             addObjectFromStream( stream );
     }
 
     public void clearObstacles()
     {
         for ( int id : gameObjects.keySet() )
         {
             if ( gameObjects.get( id ) instanceof Alien || gameObjects.get( id ) instanceof BlackHole ||
                     gameObjects.get( id ) instanceof Asteroid || gameObjects.get( id ) instanceof Bonus ||
                     gameObjects.get( id ) instanceof Station )
                 removeObject( gameObjects.get( id ) );
         }
     }
 
     public ConcurrentLinkedQueue<Asteroid> getAsteroids()
     {
         return asteroids;
     }
 
     public ConcurrentLinkedQueue<GameObject> getBaddies()
     {
         return baddies;
     }
 
     public ConcurrentLinkedQueue<BlackHole> getBlackHoles()
     {
         return blackHoles;
     }
 
     public Set<Integer> getAllIds()
     {
         return gameObjects.keySet();
     }
 
     public ConcurrentLinkedQueue<Ship> getPlayers()
     {
         return players;
     }
 
     public ConcurrentLinkedQueue<ShootingObject> getShootingObjects()
     {
         return shootingObjects;
     }
 
     public GameObject getObject( int id )
     {
         return gameObjects.get( id );
     }
 
     public boolean contains( int id )
     {
         return gameObjects.containsKey( id );
     }
 }
