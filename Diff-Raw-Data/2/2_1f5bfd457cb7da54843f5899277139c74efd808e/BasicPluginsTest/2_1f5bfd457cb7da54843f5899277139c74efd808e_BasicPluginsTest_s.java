 package org.kt3k.straw.plugin;
 
 import org.junit.runner.RunWith;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import org.robolectric.RobolectricTestRunner;
 
 @RunWith(RobolectricTestRunner.class)
 public class BasicPluginsTest {
 
 	@Test
 	public void testNames() {
 		assertArrayEquals(new String[]{
 			"org.kt3k.straw.plugin.ActivityPlugin",
 			"org.kt3k.straw.plugin.HttpPlugin",
 			"org.kt3k.straw.plugin.LogPlugin",
 			"org.kt3k.straw.plugin.OptionsMenuPlugin",
			"org.kt3k.straw.plugin.SharedPreferencePlugin",
 			"org.kt3k.straw.plugin.UIPlugin",
 		}, BasicPlugins.names);
 	}
 
 	@Test
 	public void testConstructor() {
 		assertNotNull(new BasicPlugins());
 	}
 
 }
