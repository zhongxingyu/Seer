 package org.unividuell.upnp.renderer.statemachine;
 
 import java.io.*;
 import java.net.*;
 
 import org.fourthline.cling.support.avtransport.impl.state.*;
 import org.fourthline.cling.support.avtransport.lastchange.*;
 import org.fourthline.cling.support.model.*;
 import org.slf4j.*;
 import org.unividuell.upnp.renderer.*;
 
 public class MyRendererPlaying extends Playing<AVTransport> {
     
     final Logger logger = LoggerFactory.getLogger(MyRendererPlaying.class);
    
    /** signals that we left the playing state into pause mode. */
    private boolean leftStateIntoPauseMode = false;
 
     public MyRendererPlaying(AVTransport transport) {
         super(transport);
     }
     
     @Override
     public void onEntry() {
         super.onEntry();
         // Start playing now!
         String currentURI = getTransport().getMediaInfo().getCurrentURI();
        if (! leftStateIntoPauseMode) {
            try {
                PlayerBeanHolder.getInstance().getPlayer().loadFile(currentURI, false);
            } catch (IOException e) {
                logger.error("couldn't load file '{}'.", currentURI, e);
            }
        } else {
            PlayerBeanHolder.getInstance().getPlayer().pause();
         }
     }
 
     @Override
     public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
         // Your choice of action here, and what the next state is going to be!
         logger.info("setTransportURI in Playing with uri '{}'", uri.toString());
         logger.info("MetaData: {}", metaData);
 
         getTransport().setMediaInfo(new MediaInfo(uri.toString(), metaData));
 
         // If you can, you should find and set the duration of the track here!
         getTransport().setPositionInfo(new PositionInfo(1, metaData, uri.toString()));
 
         // It's up to you what "last changes" you want to announce to event
         // listeners
         getTransport()
             .getLastChange()
             .setEventedValue(
                     getTransport().getInstanceId(),
                     new AVTransportVariable.AVTransportURI(uri), 
                     new AVTransportVariable.CurrentTrackURI(uri));
         
         return MyRendererPlaying.class;
     }
 
     @Override
     public Class<? extends AbstractState> stop() {
         // Stop playing!
 //        logger.info("STOP in Playing");
        leftStateIntoPauseMode = false;
         return MyRendererStopped.class;
     }
 
     @Override
     public Class play(String speed) {
        logger.info("PLAY in Playing");
         return MyRendererPlaying.class;
     }
 
     @Override
     public Class pause() {
        logger.info("PAUSE in Playing");
        leftStateIntoPauseMode = true;
         return MyRendererPausing.class;
     }
 
     @Override
     public Class next() {
         logger.info("NEXT in Playing");
         return null;
     }
 
     @Override
     public Class previous() {
         logger.info("PREV in Playing");
         return null;
     }
 
     @Override
     public Class seek(SeekMode unit, String target) {
         logger.info("unit '{}', target '{}', ", unit, target);
         return MyRendererPlaying.class;
     }
 
 }
