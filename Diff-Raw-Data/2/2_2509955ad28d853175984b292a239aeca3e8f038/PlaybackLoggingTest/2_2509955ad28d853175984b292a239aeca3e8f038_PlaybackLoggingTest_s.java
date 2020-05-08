 package org.concord.datagraph.test;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import org.concord.data.state.OTTimerDataStoreDataProducer;
 import org.concord.datagraph.state.OTDataCollector;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.logging.OTModelEvent;
 import org.concord.otrunk.logging.OTModelEvent.EventType;
 import org.concord.otrunk.view.OTViewer;
 import org.concord.testing.gui.TestHelper;
 import org.concord.testing.gui.junit.Order;
 import org.fest.assertions.Assertions;
 import org.fest.swing.annotation.GUITest;
 import org.fest.swing.core.matcher.JButtonMatcher;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.fixture.JButtonFixture;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 @GUITest
 public class PlaybackLoggingTest {
     private static final Logger logger = Logger.getLogger(PlaybackLoggingTest.class.getName());
     
     private static FrameFixture mainFrame;
     private static OTrunkImpl otrunk;
     private static OTDataCollector model;
 
     private static JButtonFixture playButton;
     private static JButtonFixture pauseButton;
     private static JButtonFixture resetButton;
     
     @BeforeClass
     public static void setup() throws Exception {
         logger.info("Running test setup");
 
         System.setProperty("sailotrunk.otmlurl", PlaybackLoggingTest.class.getResource("playback-logging.otml").toExternalForm());
         System.setProperty("otrunk.view.author", "false");
         System.setProperty("otrunk.view.no_user","true");
         System.setProperty("otrunk.view.mode", "student");
         System.setProperty("otrunk.view.status", "true");
         
         mainFrame = TestHelper.getFrameFixture();
         otrunk = (OTrunkImpl) ((OTViewer)(mainFrame.component())).getOTrunk();
         model = (OTDataCollector) otrunk.getRoot();
         
         playButton = mainFrame.button(JButtonMatcher.withText("Play"));
         pauseButton = mainFrame.button(JButtonMatcher.withText("Pause"));
         resetButton = mainFrame.button(JButtonMatcher.withText("Reset"));
     }
     
     @AfterClass
     public static void cleanup() throws Exception {
         mainFrame.cleanUp();
         mainFrame = null;
         otrunk = null;
     }
     
     @Test
     @Order(0)
     public void nonDetailedLoggingInteraction() {
         interactWithModel();
     }
     
     @Test
     @Order(10)
     public void startEvents() {
         ArrayList<OTModelEvent> events = findAllEvents(EventType.PLAYBACK_START);
         Assertions.assertThat(events.size()).as("Start events").isEqualTo(3);
         for (OTModelEvent event : events) {
             Assertions.assertThat(event.getInfo().getObject("graph")).isNull();
         }
     }
     
     @Test
     @Order(20)
     public void stopEvents() {
         ArrayList<OTModelEvent> events = findAllEvents(EventType.PLAYBACK_STOP);
         Assertions.assertThat(events.size()).as("Stop events").isEqualTo(3);
         for (OTModelEvent event : events) {
             Assertions.assertThat(event.getInfo().getObject("graph")).isNull();
         }
     }
     
     @Test
     @Order(30)
     public void resetEvents() {
         ArrayList<OTModelEvent> events = findAllEvents(EventType.PLAYBACK_RESET);
         Assertions.assertThat(events.size()).as("Reset events").isEqualTo(2);
         for (OTModelEvent event : events) {
             Assertions.assertThat(event.getInfo().getObject("graph")).isNull();
         }
     }
     
     @Test
     @Order(40)
     public void detailedLoggingInteraction() {
         model.getLog().clear();
        model.setLogGraphOnStart(true);
         interactWithModel();
     }
     
     @Test
     @Order(50)
     public void detailedStartEvents() {
         ArrayList<OTModelEvent> events = findAllEvents(EventType.PLAYBACK_START);
         Assertions.assertThat(events.size()).as("Detailed start events").isEqualTo(3);
         for (OTModelEvent event : events) {
             OTObject graphObject = event.getInfo().getObject("graph");
             Assertions.assertThat(graphObject).isNotNull();
             Assertions.assertThat(graphObject).isInstanceOf(OTTimerDataStoreDataProducer.class);
         }
         
         OTTimerDataStoreDataProducer firstEventGraph = (OTTimerDataStoreDataProducer) events.get(0).getInfo().getObject("graph");
         OTTimerDataStoreDataProducer secondEventGraph = (OTTimerDataStoreDataProducer) events.get(1).getInfo().getObject("graph");
         OTTimerDataStoreDataProducer thirdEventGraph = (OTTimerDataStoreDataProducer) events.get(2).getInfo().getObject("graph");
         
         // the first and second graphs should be the same object, since we didn't change the graph in between those play presses
         Assertions.assertThat(firstEventGraph).as("The graph object shouldn't be copied twice if the graph doesn't change between play presses").isSameAs(secondEventGraph);
         
         // the third object should be different, since we changed the graph before hitting play the third time
         Assertions.assertThat(thirdEventGraph).as("The graph object should be copied again when the graph changes between play presses").isNotSameAs(firstEventGraph);
     }
     
     @Test
     @Order(60)
     public void detailedStopEvents() {
         ArrayList<OTModelEvent> events = findAllEvents(EventType.PLAYBACK_STOP);
         Assertions.assertThat(events.size()).as("Detailed stop events").isEqualTo(3);
         for (OTModelEvent event : events) {
             Assertions.assertThat(event.getInfo().getObject("graph")).isNull();
         }
     }
     
     @Test
     @Order(70)
     public void detailedResetEvents() {
         ArrayList<OTModelEvent> events = findAllEvents(EventType.PLAYBACK_RESET);
         Assertions.assertThat(events.size()).as("Detailed reset events").isEqualTo(2);
         for (OTModelEvent event : events) {
             Assertions.assertThat(event.getInfo().getObject("graph")).isNull();
         }
     }
     
     private void interactWithModel() {
         // 3 play
         // 3 pause
         // 2 reset
         
         // draw stuff
         drawGraph();
         
         playButton.click();
         TestHelper.sleep(200);
         
         pauseButton.click();
         TestHelper.sleep(200);
         
         playButton.click();
         TestHelper.sleep(200);
         
         pauseButton.click();
         TestHelper.sleep(200);
         
         resetButton.click();
         TestHelper.sleep(200);
         
         // draw new stuff
         drawGraph();
         
         playButton.click();
         TestHelper.sleep(200);
         
         pauseButton.click();
         TestHelper.sleep(200);
         
         resetButton.click();
         TestHelper.sleep(200);
     }
     
     private void drawGraph() {
         int y = 0;
         for (int x = 225; x <= 750; x += 75) {
             y = (int)((Math.random() * 300) + 100);
             mainFrame.robot.click(mainFrame.component(), new Point(x,y));
         }
     }
     
     private ArrayList<OTModelEvent> findAllEvents(EventType type) {
         ArrayList<OTModelEvent> events = new ArrayList<OTModelEvent>();
         for (OTObject o : model.getLog()) {
             if (o instanceof OTModelEvent) {
                 OTModelEvent evt = (OTModelEvent) o;
                 if (evt.getType().equals(type)) {
                     events.add(evt);
                 }
             }
         }
         return events;
     }
 }
