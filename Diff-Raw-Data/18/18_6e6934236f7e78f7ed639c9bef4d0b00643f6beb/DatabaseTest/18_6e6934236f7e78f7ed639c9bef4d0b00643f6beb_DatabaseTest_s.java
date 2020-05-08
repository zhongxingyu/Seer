 package com.github.davidmoten.et;
 
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.Map;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 
 import com.google.appengine.api.datastore.PropertyContainer;
 import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
 
 public class DatabaseTest {
 
 	@Test
 	public void test() {
 		Map<String, Object> map = Maps.newHashMap();
 		map.put("time", new Date());
 		map.put("lat", -15.7);
 		map.put("lon", 135.5);
 		PropertyContainer pc = EasyMock.createMock(PropertyContainer.class);
 		EasyMock.expect(pc.getProperties()).andReturn(map).atLeastOnce();
 		EasyMock.replay(pc);
		Database.writeReportsAsJson(Sets.newHashSet(pc), new PrintWriter(
				System.out));
 		EasyMock.verify(pc);
 	}
 
 }
