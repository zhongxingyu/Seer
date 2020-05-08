 /*
  * Copyright 2013 Eediom Inc.
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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryCommandPipe;
 import org.araqne.logdb.QueryParserService;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.impl.FunctionRegistryImpl;
 import org.araqne.logdb.query.command.ParseKv;
 import org.araqne.logdb.query.engine.QueryParserServiceImpl;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ParseKvParserTest {
 	private QueryParserService queryParserService;
 
 	@Before
 	public void setup() {
 		QueryParserServiceImpl p = new QueryParserServiceImpl();
 		p.setFunctionRegistry(new FunctionRegistryImpl());
 		queryParserService = p;
 	}
 	
 	@Test
 	public void testQuery() {
 		ParseKvParser parser = new ParseKvParser();
 		parser.setQueryParserService(queryParserService);
 		
 		ParseKv kv = (ParseKv) parser.parse(null, "parsekv");
 		DummyOutput out = new DummyOutput();
 		kv.setOutput(new QueryCommandPipe(out));
 
 		assertEquals("line", kv.getField());
 		assertFalse(kv.isOverlay());
 		assertEquals(" ", kv.getPairDelim());
 		assertEquals("=", kv.getKvDelim());
		assertEquals("parsekv", kv.toString());
 
 		// test field extraction
 		String line = "a=1 b=2 c=3";
 		Map<String, Object> m = new HashMap<String, Object>();
 		m.put("line", line);
 		kv.onPush(new Row(m));
 		Row o = out.output;
 
 		assertEquals(3, o.map().size());
 		assertEquals("1", o.get("a"));
 		assertEquals("2", o.get("b"));
 		assertEquals("3", o.get("c"));
 	}
 
 	private class DummyOutput extends QueryCommand {
 		private Row output;
 
 		@Override
 		public String getName() {
 			return "output";
 		}
 
 		@Override
 		public void onPush(Row m) {
 			output = m;
 		}
 	}
 
 }
