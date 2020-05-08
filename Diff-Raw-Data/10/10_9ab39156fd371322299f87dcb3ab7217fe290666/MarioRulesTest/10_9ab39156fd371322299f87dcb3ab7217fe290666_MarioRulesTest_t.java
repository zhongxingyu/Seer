 package edu.ucsc.eis.mario;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyFloat;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.doNothing;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import javax.imageio.ImageIO;
 import javax.jms.*;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.conf.EventProcessingOption;
 import org.drools.runtime.KnowledgeSessionConfiguration;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.conf.ClockTypeOption;
 import org.drools.runtime.rule.FactHandle;
 import org.drools.time.SessionPseudoClock;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 
 import com.mojang.sonar.FakeSoundEngine;
 import com.mojang.sonar.SonarSoundEngine;
 import com.mojang.sonar.SoundSource;
 import com.mojang.sonar.sample.SonarSample;
 
 import edu.ucsc.eis.mario.level.LevelGenerator;
 import edu.ucsc.eis.mario.rules.KnowledgeFactory;
 import edu.ucsc.eis.mario.sprites.Mario;
 
 @Ignore
 public class MarioRulesTest {
 	protected LevelScene scene;
 	protected Mario mario;
 	protected StatefulKnowledgeSession ksession;
 	protected TrackingAgendaEventListener trackingAgendaEventListener;
 	protected boolean rulesEnabled;
     protected MessageProducer producer;
     protected Session session;
 	
 	public MarioRulesTest() {
 		rulesEnabled = true;
 	}
 	
 	@Before
 	public void setUp() throws IOException {
 		GraphicsConfiguration graphicsConfiguration = mock(GraphicsConfiguration.class);
 		BufferedImage image = mock(BufferedImage.class);
 		when(image.getWidth()).thenReturn(100);
 		when(image.getHeight()).thenReturn(100);
 		when(graphicsConfiguration.createCompatibleImage(anyInt(), anyInt(), anyInt())).thenReturn(image);
 
 		Graphics2D g = mock(Graphics2D.class);
 		when(g.drawImage(any(Image.class), anyInt(), anyInt(), any(ImageObserver.class))).thenReturn(true);
 		when(image.getGraphics()).thenReturn(g);
 		
 		Art.init(null, new FakeSoundEngine());
 		initArt();
 
 		MarioComponent marioComponent = mock(MarioComponent.class);
 
 		scene = spy(new LevelScene(graphicsConfiguration, marioComponent, -8821502137513047579l, 1, 
 				LevelGenerator.TYPE_OVERGROUND));
 		SonarSoundEngine sound = mock(SonarSoundEngine.class);
 		scene.sound = sound;
 		doNothing().when(sound).play(any(SonarSample.class), any(SoundSource.class), anyFloat(), anyFloat(), anyFloat());
 		scene.init();
 		Art.stopMusic();
 		scene.paused = false;		
 		
 		mario = scene.mario;
 		Mario.resetStatic();
 		mario.deathTime = 0;
 		
 		try {
 			// load up the knowledge base
 			KnowledgeBase kbase = KnowledgeFactory.newKnowledgeBase("Mario.drl", "Mario.rf");
 			KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
 			config.setOption(ClockTypeOption.get("pseudo"));
 			ksession = kbase.newStatefulKnowledgeSession(config, null);
 			//KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
 			trackingAgendaEventListener = new TrackingAgendaEventListener();
 			ksession.addEventListener(trackingAgendaEventListener);
 			MarioComponent.ksession = ksession;
 		} catch (Throwable t) {
 			t.printStackTrace();
 			System.exit(1);
 		}
 
         try {
             ConnectionFactory factory =
                     new ActiveMQConnectionFactory("tcp://localhost:61616");
             Connection connection = factory.createConnection();
             connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("mariotest");
            producer = session.createProducer(destination);
            ksession.setGlobal("producer", producer);
            ksession.setGlobal("session", session);
            MarioComponent.session = session;
            MarioComponent.producer = producer;
         } catch (Exception e) {
             System.err.println("Couldn't connect to broker");
             e.printStackTrace();
         }
 		
 		mario.keys[Mario.KEY_JUMP] = false;
 		tickScene(500);
 		assertTrue(mario.deathTime == 0);
 		assertTrue(mario.getJumpTime() == 0);
 		assertTrue(mario.isOnGround());
 	}
 	
 	@After
 	public void tearDown() {
 		// Reset the knowledge base
 		ksession.dispose();
 	}
 	
 	
 	protected void tickScene(int ticks) {
 		for (int i = 0; i < ticks; i++) {
 			FactHandle marioFact = ksession.insert(mario);
 
 			if (rulesEnabled) {
 				ksession.startProcess("Mario");
 				ksession.fireAllRules();
 			}
 			
 			scene.tick();
 			SessionPseudoClock clock = ksession.getSessionClock();
 			clock.advanceTime(42, TimeUnit.MILLISECONDS);
 			ksession.retract(marioFact);
 		}
 	}
 	
 	protected long getClockTime() {
     	long time = System.currentTimeMillis();
     	if (ksession != null) {
     		SessionPseudoClock clock = ksession.getSessionClock();
     		time = clock.getCurrentTime();
     	}
     	
     	System.out.println("Time is " + time);
     	return time;
 	}
 	
 	protected void assertFired(String ruleName) {
 		//ksession.fireAllRules(new RuleNameEqualsAgendaFilter(ruleName));
 		tickScene(1);
 		ksession.startProcess("Mario");
 		ksession.fireAllRules();
 		assertTrue(trackingAgendaEventListener.isRuleFired(ruleName));
 	}
 	
 	protected void assertNotFired(String ruleName) {
 		ksession.startProcess("Mario");
 		ksession.fireAllRules();
 		assertFalse(trackingAgendaEventListener.isRuleFired(ruleName));
 	}
 	
 	protected void initArt() throws IOException {
 		Image sheet;
 		
 		sheet = ImageIO.read(Art.class.getResourceAsStream("/smallmariosheet.png"));
 		Art.smallMario = new Image[sheet.getWidth(null)][sheet.getHeight(null)];
 		Art.smallMario[0][0] = sheet;
 		
 		sheet = ImageIO.read(Art.class.getResourceAsStream("/mariosheet.png"));
 		Art.mario = new Image[sheet.getWidth(null)][sheet.getHeight(null)];
 		Art.mario[0][0] = sheet;
 		
 		sheet = ImageIO.read(Art.class.getResourceAsStream("/firemariosheet.png"));
 		Art.fireMario = new Image[sheet.getWidth(null)][sheet.getHeight(null)];
 		Art.fireMario[0][0] = sheet;
 	}
 }
