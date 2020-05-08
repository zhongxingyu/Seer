 package jp.gr.java_conf.afterthesunrise.commons.object;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 
 import javax.sound.sampled.Clip;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.io.ByteStreams;
 import com.google.common.io.Resources;
 
 /**
  * @author takanori.takase
  */
 public class SoundsTest {
 
 	private byte[] sample;
 
 	@Before
 	public void setUp() throws Exception {
 
 		URL url = Resources.getResource("sample.wav");
 
 		InputStream in = url.openStream();
 
 		try {
 			sample = ByteStreams.toByteArray(in);
 		} finally {
 			in.close();
 		}
 
 	}
 
 	@Test(expected = IllegalAccessError.class)
 	public void testConstructor() throws Throwable {
 
 		Class<?> clazz = Sounds.class;
 
 		Constructor<?> c = clazz.getDeclaredConstructor();
 
 		assertTrue(Modifier.isPrivate(c.getModifiers()));
 
 		c.setAccessible(true);
 
 		try {
 			c.newInstance();
 		} catch (InvocationTargetException e) {
 			throw e.getCause();
 		}
 
 	}
 
 	@Test
 	public void testGetClip() throws IOException {
 
 		try {
 
 			Clip clip = Sounds.getClip(sample);
 
 			try {
 				assertNotNull(clip);
 			} finally {
 				clip.close();
 			}
 
		} catch (IOException e) {
 			// Ignore as some platform may not like ".wav"
 		}
 
 		assertNull(Sounds.getClip(new byte[0]));
 
 		assertNull(Sounds.getClip(null));
 
 	}
 
 	@Test(expected = IOException.class)
 	public void testGetClip_Invalid() throws IOException {
 		Sounds.getClip(new byte[] { 0 });
 	}
 
 	@Test
 	public void testCloseQuietly() throws IOException {
 
 		Clip clip = mock(Clip.class);
 
 		Sounds.closeQuietly(clip);
 
 		verify(clip).close();
 
 		Sounds.closeQuietly(null);
 
 	}
 
 }
