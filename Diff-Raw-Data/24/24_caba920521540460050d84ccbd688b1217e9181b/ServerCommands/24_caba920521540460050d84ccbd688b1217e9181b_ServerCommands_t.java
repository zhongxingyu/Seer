 /*
  * DISASTEROIDS
  * ServerCommaands.java
  */
 package disasteroids.networking;
 
 import disasteroids.*;
 import disasteroids.networking.DatagramListener.ByteOutputStream;
 import disasteroids.weapons.Unit;
 import java.io.IOException;
 
 /**
  * All of the updating methods that the game calls to update clients.
  * @author Phillip Cohen
  */
 public class ServerCommands
 {

     /**
      * Headers of the messages that we send to the client.
      */
     public enum Message
     {
         //====================
         // GENERAL NETWORKING
         //====================

         MULTI_PACKET,
         CONNECT_ERROR_OLDNETCODE,
         PONG,
         FULL_UPDATE,
         //======
         // GAME
         //======
         PAUSE,
         SERVER_QUITTING,
         //=================
         // PLAYER COMMANDS
         //=================
         PLAYER_JOINED,
         PLAYER_QUIT,
         PLAYER_STRAFE,
         PLAYER_BERSERK,
         //======
         // MISC
         //======
         WEAPON_UNIT_CREATED,
         OBJECT_UPDATE_VELOCITY,
         OBJECT_CREATED,
         OBJECT_REMOVED;
 
     }
 
     //=========================//=====================================//=========================//
     //                         //      DANGER: COPYPASTA              //                         //
     //=========================//=====================================//=========================//
     /**
      * Notifies all players as to whether the game is paused.     
      */
     public static void updatePause( boolean paused )
     {
         try
         {
             ByteOutputStream out = new ByteOutputStream();
             out.writeInt( ServerCommands.Message.PAUSE.ordinal() );
             out.writeBoolean( paused );
             Server.getInstance().sendPacketToAllPlayers( out );
         }
         catch ( IOException ex )
         {
             Main.warning( "Network stream failure, updatePause", ex );
         }
     }
 
     /**
      * Notifies clients that the <code>Ship</code> with given id has just berserked.
      */
     public static void berserk( long id )
     {
         try
         {
             ByteOutputStream out = new ByteOutputStream();
             out.writeInt( ServerCommands.Message.PLAYER_BERSERK.ordinal() );
             out.writeLong( id );
             Server.getInstance().sendPacketToAllPlayers( out );
         }
         catch ( IOException ex )
         {
             Main.warning( "Network stream failure, berkserk", ex );
         }
     }
 
     /**
      * Notifies clients that the <code>Ship</code> with given id has just strafed.
      */
     public static void strafe( long id, boolean toRight )
     {
         try
         {
             ByteOutputStream out = new ByteOutputStream();
             out.writeInt( ServerCommands.Message.PLAYER_STRAFE.ordinal() );
             out.writeLong( id );
             out.writeBoolean( toRight );
             Server.getInstance().sendPacketToAllPlayers( out );
         }
         catch ( IOException ex )
         {
             Main.warning( "Network stream failure, strafe", ex );
         }
     }
 
     /**
      * Notifies clients that the <code>Ship</code> with given id has just strafed.
      */
     public static void weaponUnitCreated( Unit u, long id, int indexOfWeapon )
     {
         try
         {
             ByteOutputStream out = new ByteOutputStream();
             out.writeInt( ServerCommands.Message.WEAPON_UNIT_CREATED.ordinal() );
             out.writeLong( id );
             out.writeInt( indexOfWeapon );
             out.writeInt( Constants.parseWeaponUnit( u ).ordinal() );
             u.flatten( out );
             Server.getInstance().sendPacketToAllPlayers( out );
         }
         catch ( IOException ex )
         {
         }
     }
 
     /**
      * Updates clients about a object's position and speed.
      */
     public static void updateObjectVelocity( GameObject go )
     {
         try
         {
             ByteOutputStream out = new ByteOutputStream();
             out.writeInt( ServerCommands.Message.OBJECT_UPDATE_VELOCITY.ordinal() );
             out.writeLong( go.getId() );
             go.flattenPosition( out );
             Server.getInstance().sendPacketToAllPlayers( out );
         }
         catch ( IOException ex )
         {
             Main.warning( "Network stream failure, changeBonusVelocity", ex );
         }
     }
 
     /**
      * Notifies clients that an object was created.
      */
     public static void objectCreatedOrDestroyed( GameObject go, boolean created )
     {
        if ( go instanceof Ship )
            return;

         try
         {
             ByteOutputStream out = new ByteOutputStream();
             if ( created )
             {
                 out.writeInt( ServerCommands.Message.OBJECT_CREATED.ordinal() );
                 out.writeInt( Constants.parseGameObject( go ) );
                 go.flatten( out );
             }
             else
             {
                 out.writeInt( ServerCommands.Message.OBJECT_REMOVED.ordinal() );
                 out.writeLong( go.getId() );
             }
             Server.getInstance().sendPacketToAllPlayers( out );
         }
         catch ( IOException ex )
         {
             Main.warning( "Network stream failure, objectCreatedOrDestroyed", ex );
         }
     }
 }
