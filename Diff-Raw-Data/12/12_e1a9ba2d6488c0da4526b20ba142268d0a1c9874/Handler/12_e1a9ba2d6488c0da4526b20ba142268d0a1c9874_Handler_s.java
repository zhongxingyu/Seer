 /**
  * 
  */
 package net.sourceforge.gjtapi.protocols.playback;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLStreamHandler;
 
import net.sourceforge.gjtapi.protocols.PlaybackURLConnection;

 /**
  * Protocol handler for the javasound API.
  * @author Dirk Schnelle-Walka
  *
  */
 public final class Handler extends URLStreamHandler {
 
     /**
      * Constructs a new object.
      */
     public Handler() {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected URLConnection openConnection(final URL url) throws IOException {
         return new PlaybackURLConnection(url);
     }
 
 }
