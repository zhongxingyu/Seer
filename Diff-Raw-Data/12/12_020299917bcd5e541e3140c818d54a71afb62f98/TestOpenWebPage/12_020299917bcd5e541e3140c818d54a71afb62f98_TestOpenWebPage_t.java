 package test.org.malai.action;
 
 import java.awt.Desktop;
 import java.lang.reflect.Field;
 import java.net.URI;
 
 import org.malai.action.library.OpenWebPage;
 
 import test.org.malai.HelperTest;
 
 public class TestOpenWebPage extends TestAbstractAction<OpenWebPage> {
 	@Override
 	protected OpenWebPage createAction() {
 		return new OpenWebPage();
 	}
 
 	@SuppressWarnings("unused")
 	@Override
 	public void testConstructor() throws SecurityException,
 			NoSuchFieldException, IllegalArgumentException,
 			IllegalAccessException {
 		new OpenWebPage();
 	}
 
 	@Override
 	public void testFlush() throws Exception {
 		action.setUri(new URI("foo"));
 		action.flush();
 		final Field act = HelperTest.getField(OpenWebPage.class, "uri");
 		assertNull(act.get(action));
 	}
 
 	@Override
 	public void testDo() throws Exception {
 		action.setUri(new URI("http://www.google.com"));
 		action.doIt();
 	}
 
 
 	public void testDoBadURI() throws Exception {
 		action.setUri(new URI("foo"));
 		action.doIt();
 	}
 
 
 	@Override
 	public void testCanDo() throws Exception {
		if(HelperTest.isX11Set()) {
			assertFalse(action.canDo());
			action.setUri(new URI("foo"));
			assertEquals(action.canDo(), Desktop.getDesktop().isSupported(Desktop.Action.BROWSE));
			action.setUri(null);
			assertFalse(action.canDo());
		}
 	}
 
 	@Override
 	public void testIsRegisterable() throws SecurityException,
 			NoSuchFieldException, IllegalArgumentException,
 			IllegalAccessException {
 		assertFalse(action.isRegisterable());
 	}
 
 	@Override
 	public void testHadEffect() throws Exception {
 		if(HelperTest.isX11Set()) {
 			assertFalse(action.hadEffect());
 			action.setUri(new URI("http://localhost"));
 			assertFalse(action.hadEffect());
 			action.doIt();
 			action.done();
 			assertTrue(action.hadEffect());
 		}
 	}
 
 
 	public void testHadEffectBadURI() throws Exception {
 		assertFalse(action.hadEffect());
 		action.setUri(new URI("foo"));
 		assertFalse(action.hadEffect());
 		action.doIt();
 		action.done();
 		assertFalse(action.hadEffect());
 	}
 }
