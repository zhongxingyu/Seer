 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.danielkvasnicka.cloudscrobbler.engine;
 
 import de.umass.lastfm.Session;
 import de.umass.lastfm.scrobble.ScrobbleData;
 import de.umass.lastfm.scrobble.ScrobbleResult;
 import java.util.List;
 import java.util.Properties;
 import javax.enterprise.context.Dependent;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 import net.danielkvasnicka.cloudscrobbler.engine.api.NewTracks;
 import net.danielkvasnicka.cloudscrobbler.engine.api.ScrobbleBatch;
 import net.danielkvasnicka.cloudscrobbler.utils.Utils;
 import org.jboss.solder.logging.Category;
 import org.jboss.solder.logging.Logger;
 import org.jboss.solder.resourceLoader.Resource;
 
 /**
  *
  * @author daniel
  */
 @Dependent
 public class Engine {
 
     @Inject
     @Resource("META-INF/lastfm.properties")
     private Properties lastFmCredentials;
 
     @Inject @Category("Engine")
     private Logger logger;
 
     public void scrobble(@Observes NewTracks event) {
         for (ScrobbleBatch batch : event.getBatches()) {
 
             String lastFmSessionKey = batch.getLastFmSessionKey();
            Session session = Session.createSession(
                     this.lastFmCredentials.getProperty("lastfm.apikey"),
                    this.lastFmCredentials.getProperty("lastfm.secret"),
                    lastFmSessionKey);
 
             List<ScrobbleData> scrobbleData = (List<ScrobbleData>) Utils.transformTracksToScrobbleData(batch.getNewTracks());
             List<ScrobbleResult> result = de.umass.lastfm.Track.scrobble(scrobbleData, session);
            // TODO: use the logger
             Utils.logFailedScrobbleAttempts(result, session.getUsername());
         }
     }
 }
