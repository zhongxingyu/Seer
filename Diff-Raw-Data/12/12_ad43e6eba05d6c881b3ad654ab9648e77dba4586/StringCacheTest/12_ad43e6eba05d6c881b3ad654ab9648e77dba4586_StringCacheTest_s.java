 package com.byond.dm.model.dmb;
 
 import java.io.IOException;
 
import org.junit.Ignore;
 import org.junit.Test;
 
 public class StringCacheTest extends DMBTest {
 	@Test
	//@Ignore // FIXME: Need to implement a few other things before the cache.
 	public void initializeCache() throws IOException {
 		if (stream.skip(51) < 51) {
 			throw new IOException("Could not skip to position, DMB is corrupt.");
 		}
 		StringCache cache = new StringCache(stream);
 	}
 }
