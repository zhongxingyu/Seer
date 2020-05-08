 /*
  * Copyright 2011 Tomas Schlosser
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.anadix.factories;
 
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 
 import org.anadix.ConditionSet;
 import org.anadix.Parser;
 import org.anadix.ReportFormatter;
 import org.anadix.mock.MockConditionSet;
 import org.anadix.mock.MockParser;
 import org.mockito.Mockito;
 import org.testng.annotations.Test;
 
 @Test
 public class ObjectFactoryTest {
 	@Test(groups = "parser", expectedExceptions = NullPointerException.class)
 	public void testNewParser1() throws Exception {
 		ObjectFactory.newParser((String)null);
 	}
 
 	@Test(groups = "parser", expectedExceptions = NullPointerException.class)
 	public void testNewParser2() throws Exception {
 		ObjectFactory.newParser("");
 	}
 
 	@Test(groups = "parser", expectedExceptions = InstantiationException.class)
 	public void testNewParser3() throws Exception {
 		ObjectFactory.newParser("non.existent.Class");
 	}
 
 	@Test(groups = "parser", expectedExceptions = IllegalArgumentException.class)
 	public void testNewParser4() throws Exception {
 		ObjectFactory.newParser("java.lang.Object");
 	}
 
 	@Test(groups = "parser")
 	public void testNewParser5() throws Exception {
 		Parser expected = mock(Parser.class);
 		Parser p = ObjectFactory.newParser(expected.getClass().getCanonicalName());
 
 		assertNotNull(p);
 		assertEquals(p.getClass(), expected.getClass());
 	}
 
 	@Test(groups = "parser", expectedExceptions = NullPointerException.class)
 	public void testNewParser6() throws Exception {
 		ObjectFactory.newParser((Class<? extends Parser>)null);
 	}
 
 	@Test(groups = "parser")
 	public void testNewParser7() throws Exception {
 		Parser expected = mock(Parser.class);
 		Parser p = ObjectFactory.newParser(expected.getClass());
 
 		assertNotNull(p);
 		assertEquals(p.getClass(), expected.getClass());
 	}
 
 	@Test(groups = "conditions", expectedExceptions = NullPointerException.class)
 	public void testNewConditionSet1() throws Exception {
 		ObjectFactory.newConditionSet((String)null);
 	}
 
 	@Test(groups = "conditions", expectedExceptions = NullPointerException.class)
 	public void testNewConditionSet2() throws Exception {
 		ObjectFactory.newConditionSet("");
 	}
 
 	@Test(groups = "conditions", expectedExceptions = InstantiationException.class)
 	public void testNewConditionSet3() throws Exception {
 		ObjectFactory.newConditionSet("non.existent.Class");
 	}
 
 	@Test(groups = "conditions", expectedExceptions = IllegalArgumentException.class)
 	public void testNewConditionSet4() throws Exception {
 		ObjectFactory.newConditionSet("java.lang.Object");
 	}
 
 	@Test(groups = "conditions")
 	public void testNewConditionSet5() throws Exception {
 		ConditionSet expected = mock(ConditionSet.class);
 		ConditionSet cs = ObjectFactory.newConditionSet(expected.getClass().getCanonicalName());
 
 		assertNotNull(cs);
 		assertEquals(cs.getClass(), expected.getClass());
 	}
 
 	@Test(groups = "conditions", expectedExceptions = NullPointerException.class)
 	public void testNewConditionSet6() throws Exception {
 		ObjectFactory.newConditionSet((Class<? extends ConditionSet>)null);
 	}
 
 	@Test(groups = "conditions")
 	public void testNewConditionSet7() throws Exception {
 		ConditionSet expected = mock(ConditionSet.class);
 		ConditionSet cs = ObjectFactory.newConditionSet(expected.getClass());
 
 		assertNotNull(cs);
 		assertEquals(cs.getClass(), expected.getClass());
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" })
 	public void testNewAnalyzer1() throws Exception {
 		ObjectFactory.newAnalyzer();
 	}
 
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewAnalyzer2() throws Exception {
 		ObjectFactory.newAnalyzer(null);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" })
 	public void testNewAnalyzer3() throws Exception {
 		ObjectFactory.newAnalyzer(MockConditionSet.class);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewAnalyzer4() throws Exception {
 		ObjectFactory.newAnalyzer((Class<Parser>)null, null);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewAnalyzer5() throws Exception {
 		ObjectFactory.newAnalyzer(null, MockConditionSet.class);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewAnalyzer6() throws Exception {
 		ObjectFactory.newAnalyzer(MockParser.class, null);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewAnalyzer7() throws Exception {
 		ObjectFactory.newAnalyzer(MockParser.class, null);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" })
 	public void testNewAnalyzer8() throws Exception {
 		ObjectFactory.newAnalyzer(MockParser.class, MockConditionSet.class);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewAnalyzer9() throws Exception {
 		ObjectFactory.newAnalyzer((Parser)null, null);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewAnalyzer10() throws Exception {
 		ObjectFactory.newAnalyzer(new MockParser(), null);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewAnalyzer11() throws Exception {
 		ObjectFactory.newAnalyzer(null, new MockConditionSet());
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" })
 	public void testNewAnalyzer12() throws Exception {
 		ObjectFactory.newAnalyzer(new MockParser(), new MockConditionSet());
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" })
 	public void testNewFormatter1() throws Exception {
 		ObjectFactory.newFormatter();
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewFormatter2() throws Exception {
 		ObjectFactory.newFormatter((String)null);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewFormatter3() throws Exception {
 		ObjectFactory.newFormatter("");
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = InstantiationException.class)
 	public void testNewFormatter4() throws Exception {
 		ObjectFactory.newFormatter("non.existing.ClassReportFormatter");
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = IllegalArgumentException.class)
 	public void testNewFormatter5() throws Exception {
 		ObjectFactory.newFormatter("java.lang.Object");
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" })
 	public void testNewFormatter6() throws Exception {
 		ReportFormatter rf = Mockito.mock(ReportFormatter.class);
 		ObjectFactory.newFormatter(rf.getClass().getName());
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" }, expectedExceptions = NullPointerException.class)
 	public void testNewFormatter7() throws Exception {
 		ObjectFactory.newFormatter((Class<ReportFormatter>)null);
 	}
 
 	@Test(dependsOnGroups = { "parser", "conditions" })
 	public void testNewFormatter8() throws Exception {
 		ReportFormatter rf = Mockito.mock(ReportFormatter.class);
 
 		ObjectFactory.newFormatter(rf.getClass());
 	}
 }
