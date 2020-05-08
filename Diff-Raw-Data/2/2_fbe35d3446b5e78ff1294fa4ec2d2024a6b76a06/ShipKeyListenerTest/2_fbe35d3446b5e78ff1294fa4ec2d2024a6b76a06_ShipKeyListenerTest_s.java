 package net.sf.adastra.sopwith.model.ship;
 
 import static java.awt.event.KeyEvent.VK_DOWN;
 import static java.awt.event.KeyEvent.VK_LEFT;
 import static java.awt.event.KeyEvent.VK_RIGHT;
 import static java.awt.event.KeyEvent.VK_UP;
 
 import java.awt.Component;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.jmock.lib.legacy.ClassImposteriser;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(JMock.class)
 public class ShipKeyListenerTest {
 
 	private final Mockery context = new JUnit4Mockery() {
 		{
 			setImposteriser(ClassImposteriser.INSTANCE);
 		}
 	};
 
 	private final Ship ship = context.mock(Ship.class);
 
 	private final KeyListener listener = new ShipKeyListener(ship);
 
 	@Test
 	public void keyPressed() {
 
 		context.checking(new Expectations() {
 			{
 				one(ship).setMainThrottle(1);
 				one(ship).setMainThrottle(-1);
 				one(ship).setRotateThrottle(-1);
 				one(ship).setRotateThrottle(1);
 			}
 		});
 
 		listener.keyPressed(keyEvent(VK_UP));
 		listener.keyPressed(keyEvent(VK_DOWN));
 		listener.keyPressed(keyEvent(VK_LEFT));
 		listener.keyPressed(keyEvent(VK_RIGHT));
 
 	}
 
 	@Test
 	public void keyReleased() {
 
 		context.checking(new Expectations() {
 			{
 				one(ship).setMainThrottle(0);
 				one(ship).setMainThrottle(0);
 				one(ship).setRotateThrottle(0);
 				one(ship).setRotateThrottle(0);
 			}
 		});
 
 		listener.keyReleased(keyEvent(VK_UP));
 		listener.keyReleased(keyEvent(VK_DOWN));
 		listener.keyReleased(keyEvent(VK_LEFT));
 		listener.keyReleased(keyEvent(VK_RIGHT));
 
 	}
 
 	private KeyEvent keyEvent(int keyCode) {
 		Component source = new Component() {
			// empty component
 		};
 
 		return new KeyEvent(source, 0, 0, 0, keyCode, KeyEvent.CHAR_UNDEFINED);
 	}
 }
