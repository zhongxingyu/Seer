 /*
  * Copyright 2013 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.parser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.ObjectComparator;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryCommandPipe;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.query.command.Search;
 import org.araqne.logdb.query.expr.Expression;
 import org.junit.Test;
 
 public class SearchParserTest {
 	@Test
 	public void testWildSearch() {
 		SearchParser p = new SearchParser();
 		Search search = (Search) p.parse(null, "search sip == \"10.1.*\" ");
 		Expression expr = search.getExpression();
 
 		Row map = new Row();
 		map.put("sip", "10.1.2.2");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "10.2.2.2");
 		assertFalse((Boolean) expr.eval(map));
 
 		assertEquals("search (sip == \"10.1.*\")", search.toString());
 	}
 
 	// iisidx.i1 == "74.86.*" or iisidx.i1 == "211.*" or iisidx.i1 ==
 	// "110.221.*"
 	@Test
 	public void testWhitespace() {
 		SearchParser p = new SearchParser();
 		Search search = (Search) p.parse(null,
 				"search sip == \"74.86.*\"  \tor     sip  ==   \"211.*\"      or\tsip == \"110.221.*\"");
 		Expression expr = search.getExpression();
 
 		Row map = new Row();
 		map.put("sip", "74.86.1.2");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "211.123.1.1");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "110.221.3.4");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "10.2.2.2");
 		assertFalse((Boolean) expr.eval(map));
 
 		assertEquals("search (((sip == \"74.86.*\") or (sip == \"211.*\")) or (sip == \"110.221.*\"))", search.toString());
 	}
 
 	@Test
 	public void testInExact() {
 		SearchParser p = new SearchParser();
 		Search search = (Search) p.parse(null, "search in(sip, \"192.168.0.1\", \"211.123.1.1\", \"10.2.2.2\")");
 		Expression expr = search.getExpression();
 
 		Row map = new Row();
 		map.put("sip", "74.86.1.2");
 		assertFalse((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "211.123.1.1");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "192.168.0.1");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "10.2.2.2");
 		assertTrue((Boolean) expr.eval(map));
 
 		assertEquals("search in(sip, \"192.168.0.1\", \"211.123.1.1\", \"10.2.2.2\")", search.toString());
 	}
 
 	@Test
 	public void testInPattern() {
 		SearchParser p = new SearchParser();
 		Search search = (Search) p.parse(null, "search in(sip, \"192.*.1\", \"*123*\", \"10*\", \"*255\")");
 		Expression expr = search.getExpression();
 
 		Row map = new Row();
 		map.put("sip", "74.86.1.2");
 		assertFalse((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "211.123.1.1");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "192.168.0.1");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "10.2.2.2");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "44.2.2.255");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "127.10.2.1");
 		assertFalse((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "127.255.2.1");
 		assertFalse((Boolean) expr.eval(map));
 	}
 
 	@Test
 	public void testInMixed() {
 		SearchParser p = new SearchParser();
 		Search search = (Search) p.parse(null,
 				"search in(sip, \"127.255.2.1\", \"192.*.1\", \"*123*\", \"127.10.2.1\", \"10*\", \"*255\")");
 		Expression expr = search.getExpression();
 
 		Row map = new Row();
 		map.put("sip", "74.86.1.2");
 		assertFalse((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "211.123.1.1");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "192.168.0.1");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "10.2.2.2");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "44.2.2.255");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "127.10.2.1");
 		assertTrue((Boolean) expr.eval(map));
 
 		map = new Row();
 		map.put("sip", "127.255.2.2");
 		assertFalse((Boolean) expr.eval(map));
 	}
 
 	@Test
 	public void testLimit() {
 		SearchParser p = new SearchParser();
 		Search search = (Search) p.parse(null, "search limit=10 port > 1024");
 		Expression expr = search.getExpression();
 
 		Row map = new Row();
 		map.put("port", 1024);
 		assertFalse((Boolean) expr.eval(map));
 
 		map.put("port", 1025);
 		assertTrue((Boolean) expr.eval(map));
 
 		assertEquals(10L, (long) search.getLimit());
 		assertEquals("search limit=10 (port > 1024)", search.toString());
 	}
 
 	@Test
 	public void testLimitOnly() {
 		SearchParser p = new SearchParser();
 		Search search = (Search) p.parse(null, "search limit=10");
 		assertEquals(10, (long) search.getLimit());
 		assertEquals("search limit=10", search.toString());
 	}
 
 	@Test
 	public void testMissingEscape() {
 		try {
 			String query = "search limit=10 category == \"E002\" and ((method == \"<iframe src=\"http://www.w3schools.com\">,,,,,,\"\",,\"<\"<<<\"<,,</iframe>\"))";
 			SearchParser p = new SearchParser();
 			p.parse(null, query);
 			fail();
 		} catch (QueryParseException e) {
 			assertEquals("quote-mismatch", e.getType());
 		}
 	}
 
 	@Test
 	public void testEscape() {
 		String query = "search limit=10 category == \"E002\" and ((method == \"<iframe src=\\\"http://www.w3schools.com\\\">,,,,,,\\\"\\\",,\\\"<\\\"<<<\\\"<,,</iframe>\"))";
 		SearchParser p = new SearchParser();
 		Search search = (Search) p.parse(null, query);
 		Output output = new Output();
 		search.setOutput(new QueryCommandPipe(output));
 
 		Row m = new Row();
 		m.put("category", "E002");
 		m.put("method", "<iframe src=\"http://www.w3schools.com\">,,,,,,\"\",,\"<\"<<<\"<,,</iframe>");
 		search.onPush(m);
 		assertEquals(m, output.m);
 
 	}
 
 	@Test
 	public void testEscape2() {
 		String query = "search in (method,\"<iframe src=\\\"http://www.w3schools.com\\\">,,,,,,\\\"\\\",,\\\"<\\\"<<<\\\"<,,</iframe>\") and d_port == \"21\"";
 		SearchParser p = new SearchParser();
 		Search search = (Search) p.parse(null, query);
 		Output output = new Output();
 		search.setOutput(new QueryCommandPipe(output));
 
 		Row m = new Row();
 		m.put("d_port", "21");
 		m.put("method", "<iframe src=\"http://www.w3schools.com\">,,,,,,\"\",,\"<\"<<<\"<,,</iframe>");
 		search.onPush(m);
 		assertEquals(m, output.m);
 	}
 
 	/**
 	 * test for araqne/logpresso#120 issue
 	 */
 	@Test
 	public void testNumberComparisons() {
 		assertEquals((short) 10, compare("search value > 5", (short) 10));
 		assertEquals(6L, compare("search value > 5", 6L));
 		assertEquals(5.5, compare("search value > 5", 5.5));
 		assertNull(compare("search value > 5", 4.5));
 		assertEquals(5, compare("search value >= 5.0", 5));
 		assertEquals(1, compare("search value >= 6 - 5.0", 1));
 		assertNull(compare("search value >= 6 - 5.0", 0.5));
 	}
 
 	@Test
 	public void testIpComparison() throws UnknownHostException {
 		ObjectComparator cmp = new ObjectComparator();
 		InetAddress ip1 = InetAddress.getByName("1.2.3.4");
 		InetAddress ip2 = InetAddress.getByName("1.2.3.5");
 		InetAddress ip3 = InetAddress.getByName("1.2.3.6");
 
		assertTrue(cmp.compare(ip1, null) > 0);
 		assertTrue(cmp.compare(ip1, ip1) == 0);
 		assertTrue(cmp.compare(ip1, ip2) < 0);
 		assertTrue(cmp.compare(ip2, ip1) > 0);
 		assertTrue(cmp.compare(ip1, ip3) < 0);
 
 		assertEquals(ip1, compare("search value >= ip(\"1.2.3.4\")", ip1));
 		assertNull(compare("search value > ip(\"1.2.3.4\")", ip1));
 		assertEquals(ip1, compare("search value > ip(\"1.2.3.3\") and value < ip(\"1.2.3.5\")", ip1));
 		assertNull(compare("search value > ip(\"1.2.3.3\") and value < ip(\"1.2.3.5\")", ip3));
 		assertNull(compare("search value > ip(\"1.2.3.3\")", null));
 	}
 
 	private Object compare(String query, Object value) {
 		SearchParser p = new SearchParser();
 		Search search = (Search) p.parse(null, query);
 		Output output = new Output();
 		search.setOutput(new QueryCommandPipe(output));
 
 		Row m = new Row();
 		m.put("value", value);
 		search.onPush(m);
 
 		if (output.m == null)
 			return null;
 		return output.m.get("value");
 	}
 
 	private class Output extends QueryCommand {
 		private Row m;
 
 		@Override
 		public void onPush(Row m) {
 			this.m = m;
 		}
 
 		@Override
 		public boolean isReducer() {
 			return false;
 		}
 
 	}
 }
