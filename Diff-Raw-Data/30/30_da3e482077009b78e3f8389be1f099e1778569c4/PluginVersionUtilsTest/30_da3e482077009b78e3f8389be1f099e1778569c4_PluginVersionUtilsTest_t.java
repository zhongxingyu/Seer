 package cytoscape.plugin;
 
 
 import junit.framework.TestCase;
 
 
 public class PluginVersionUtilsTest extends TestCase {
 	public void testGoodCyVersion1() {
 		assertTrue(PluginVersionUtils.versionOk("2.8", false));
 	}
 
 	public void testGoodCyVersion2() {
 		assertTrue(PluginVersionUtils.versionOk("2.8.1", false));
 	}
 
 	public void testGoodCyVersion3() {
 		assertTrue(PluginVersionUtils.versionOk("2.8.1-lower", false));
 	}
 
 	public void testGoodCyVersion4() {
 		assertTrue(PluginVersionUtils.versionOk("2.8.1-UPPER", false));
 	}
 
/*
 * This is no longer bad.  Version "2" get's interpreted as version 2.0
 *
 	public void testBadCyVersion1() {
 		assertFalse(PluginVersionUtils.versionOk("2", false));
 	}
 */
 
 	public void testBadCyVersion2() {
 		assertFalse(PluginVersionUtils.versionOk("X.Y.Z", false));
 	}
 
 	public void testBadCyVersion3() {
 		assertFalse(PluginVersionUtils.versionOk("2.8.1-133t", false));
 	}
 
 	public void testBadCyVersion4() {
 		assertFalse(PluginVersionUtils.versionOk("2.8.1.4", false));
 	}
 
 	public void testGoodPluginVersion1() {
 		assertTrue(PluginVersionUtils.versionOk("2.8", true));
 	}
 
/*
 * This is no longer bad.  Version "2" get's interpreted as version 2.0
 *
 	public void testBadPluginVersion1() {
 		assertFalse(PluginVersionUtils.versionOk("2", true));
 	}
 */
 
 	public void testBadPluginVersion2() {
 		assertFalse(PluginVersionUtils.versionOk("X.Y", true));
 	}
 
 	public void testBadPluginVersion3() {
 		assertFalse(PluginVersionUtils.versionOk("2.8.1", true));
 	}
 
 	public void testBadPluginVersion4() {
 		assertFalse(PluginVersionUtils.versionOk("2-8", true));
 	}
 
 	public void testBadPluginVersion5() {
 		assertFalse(PluginVersionUtils.versionOk("2.8-SNAPSHOT", true));
 	}
 }
 
