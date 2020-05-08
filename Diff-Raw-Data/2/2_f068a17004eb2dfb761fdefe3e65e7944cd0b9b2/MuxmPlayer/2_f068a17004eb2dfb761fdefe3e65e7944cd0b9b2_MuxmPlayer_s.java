 package javax.microedition.media;
 
 import net.intensicode.runme.util.Log;
 
 import java.io.*;
 
 public final class MuxmPlayer implements Player
     {
     public MuxmPlayer( final InputStream aInputStream ) throws MediaException
         {
         try
             {
             myBackend = new MuxmBackend( aInputStream );
             }
         catch ( final IOException e )
             {
             throw new MediaException( e );
             }
         }
 
     // From Controllable
 
     public final Control getControl( final String aString )
         {
         failIfClosedOr( UNREALIZED );
         if ( aString.equals( "VolumeControl" ) ) return myBackend.volumeControl;
         return null;
         }
 
     public final Control[] getControls()
         {
         failIfClosedOr( UNREALIZED );
         return new Control[]{ myBackend.volumeControl };
         }
 
     // From Player
 
     public final void setLoopCount( final int aLoopCount )
         {
         failIfClosedOr( STARTED );
         myLoopCount = aLoopCount;
         }
 
     public final long setMediaTime( final long aMediaTime ) throws MediaException
         {
         failIfClosedOr( UNREALIZED );
         return 0;
         }
 
     public final void realize() throws MediaException
         {
         failIfClosed();
         if ( isUnrealized() ) tryToRealizePlayer();
         }
 
     public final void prefetch() throws MediaException
         {
         failIfClosed();
         if ( isUnrealized() ) realize();
         if ( isRealized() ) tryToPrefetchPlayer();
         }
 
     public final void start() throws MediaException
         {
         failIfClosed();
         if ( isUnrealized() || isRealized() ) prefetch();
         if ( isPrefetched() ) tryToStartPlayer();
         }
 
     public final void stop() throws MediaException
         {
         failIfClosed();
         if ( isStarted() ) tryToStopPlayback();
         }
 
     public final void deallocate()
         {
         //failIfClosed();
         if ( isStarted() ) forceStop();
         if ( isPrefetched() ) forceUnprefetch();
         if ( isRealized() ) forceUnrealize();
         }
 
     public final void close()
         {
         deallocate();
         forceClose();
         markClosed();
         }
 
     public final int getState()
         {
         return myPlayerState;
         }
 
     public final void addPlayerListener( final PlayerListener aPlayerListener )
         {
         throw new RuntimeException( "nyi" );
         }
 
     public final void removePlayerListener( final PlayerListener aPlayerListener )
         {
         throw new RuntimeException( "nyi" );
         }
 
     // Implementation
 
     private void failIfClosedOr( final int aPlayerState )
         {
         failIfClosed();
         if ( myPlayerState == aPlayerState ) throw new IllegalStateException( getPlayerStateString() );
         }
 
     private void failIfClosed()
         {
         if ( myPlayerState == CLOSED ) throw new IllegalStateException( getPlayerStateString() );
         }
 
     private String getPlayerStateString()
         {
         if ( myPlayerState == CLOSED ) return "closed";
         if ( myPlayerState == REALIZED ) return "realized";
         if ( myPlayerState == PREFETCHED ) return "prefetched";
         if ( myPlayerState == STARTED ) return "started";
         return "unknown";
         }
 
     private boolean isUnrealized()
         {
         return myPlayerState == UNREALIZED;
         }
 
     private boolean isRealized()
         {
         return myPlayerState == REALIZED;
         }
 
     private boolean isPrefetched()
         {
         return myPlayerState == PREFETCHED;
         }
 
     private boolean isStarted()
         {
        return myPlayerState != STARTED;
         }
 
     private void tryToRealizePlayer() throws MediaException
         {
         try
             {
             myBackend.createModuleDataStream();
             myBackend.determinePlaybackFormat();
 
             // The next two should happen in tryToPrefetchPlayer. But then we need a VolumeControlProxy that can be
             // available after tryToRealizePlayer, but will be attached to a real SourceDataLine only during
             // tryToPrefetchPlayer. It should then set the volume to whatever it has been called with meanwhile.
             myBackend.openPlaybackLine();
             myBackend.createVolumeControl();
 
             markRealized();
             }
         catch ( final Exception e )
             {
             throw new MediaException( e );
             }
         }
 
     private void tryToPrefetchPlayer() throws MediaException
         {
         try
             {
             myBackend.createPlaybackStream();
             markPrefetched();
             }
         catch ( final Exception e )
             {
             throw new MediaException( e );
             }
         }
 
     private void tryToStartPlayer() throws MediaException
         {
         myBackend.startPlayback();
         markStarted();
         }
 
     private void tryToStopPlayback() throws MediaException
         {
         try
             {
             myBackend.pausePlayback();
             markPrefetched();
             }
         catch ( final InterruptedException e )
             {
             throw new MediaException( e );
             }
         }
 
     private void forceStop()
         {
         try
             {
             myBackend.pausePlayback();
             }
         catch ( final InterruptedException e )
             {
             Log.error( e );
             }
         }
 
     private void forceUnprefetch()
         {
         myBackend.disposePlaybackStream();
         }
 
     private void forceUnrealize()
         {
         myBackend.dispose();
         markUnrealized();
         }
 
     private void forceClose()
         {
         // Nothing to be done here..
         }
 
     private void markRealized()
         {
         myPlayerState = REALIZED;
         }
 
     private void markPrefetched()
         {
         myPlayerState = PREFETCHED;
         }
 
     private void markStarted()
         {
         myPlayerState = STARTED;
         }
 
     private void markUnrealized()
         {
         myPlayerState = UNREALIZED;
         }
 
     private void markClosed()
         {
         myPlayerState = CLOSED;
         }
 
 
     private int myLoopCount;
 
     private int myPlayerState = UNREALIZED;
 
     private final MuxmBackend myBackend;
     }
