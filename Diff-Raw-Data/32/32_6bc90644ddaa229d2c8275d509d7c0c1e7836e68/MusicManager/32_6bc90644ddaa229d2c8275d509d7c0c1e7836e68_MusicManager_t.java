 package ch.zhaw.arsphema.services;
 
 
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.utils.Disposable;
 
 /**
  * A service that manages the background music.
  * <p>
  * Only one music may be playing at a given time.
  */
 public class MusicManager
     implements
         Disposable
 {
     
     /**
      * Holds the music currently being played, if any.
      */
     private Music musicBeingPlayed;
 
     /**
      * The volume to be set on the music.
      */
     private float volume = 1f;
 
     /**
      * Whether the music is enabled.
      */
     private boolean enabled = true;
 
     /**
      * Creates the music manager.
      */
     public MusicManager()
     {
     }
 
     /**
      * Plays the given music (starts the streaming).
      * <p>
      * If there is already a music being played it is stopped automatically.
      */
     public void play(
     		Music music )
     {
         // check if the music is enabled
         if( ! enabled ) return;
 
         // stop any music being played
         
         stop();
 
         // start streaming the new music
         musicBeingPlayed = music;
         musicBeingPlayed.setVolume( volume );
         musicBeingPlayed.setLooping( true );
         musicBeingPlayed.play();
     }
 
     /**
      * Stops and disposes the current music being played, if any.
      */
     public void stop()
     {
         if( musicBeingPlayed != null ) {
             
             musicBeingPlayed.stop();
             musicBeingPlayed.dispose();
         }
     }
 
     /**
      * Sets the music volume which must be inside the range [0,1].
      */
     public void setVolume(
         float volume )
     {
         
 
         // check and set the new volume
         if( volume < 0 || volume > 1f ) {
             throw new IllegalArgumentException( "The volume must be inside the range: [0,1]" );
         }
         this.volume = volume;
 
         // if there is a music being played, change its volume
         if( musicBeingPlayed != null ) {
             musicBeingPlayed.setVolume( volume );
         }
     }
 
     /**
      * Enables or disabled the music.
      */
     public void setEnabled(
         boolean enabled )
     {
         this.enabled = enabled;
 
         // if the music is being deactivated, stop any music being played
         if( ! enabled ) {
             stop();
         }
     }
 
     /**
      * Disposes the music manager.
      */
     public void dispose()
     {
         stop();
     }
 
 	public void stopMusic() {
 		musicBeingPlayed.stop();
 	}
 }
