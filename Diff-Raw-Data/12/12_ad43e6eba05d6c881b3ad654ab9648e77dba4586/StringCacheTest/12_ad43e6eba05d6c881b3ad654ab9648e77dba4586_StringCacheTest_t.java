 package com.byond.dm.model.dmb;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 public class StringCacheTest extends DMBTest {
 	@Test
 	public void initializeCache() throws IOException {
 		if (stream.skip(51) < 51) {
 			throw new IOException("Could not skip to position, DMB is corrupt.");
 		}
 		StringCache cache = new StringCache(stream);
 	}
 }
