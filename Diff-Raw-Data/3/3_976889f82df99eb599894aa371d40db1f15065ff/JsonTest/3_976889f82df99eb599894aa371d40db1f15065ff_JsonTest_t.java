 package org.umlg.tinker.json;
 
import com.fasterxml.jackson.databind.ObjectMapper;
 import junit.framework.Assert;
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.umlg.concretetest.God;
 import org.umlg.concretetest.Universe.UniverseRuntimePropertyEnum;
 import org.umlg.embeddedtest.REASON;
 import org.umlg.embeddedtest.TestEmbedded;
 import org.umlg.inheritencetest.Biped;
 import org.umlg.inheritencetest.Mamal;
 import org.umlg.inheritencetest.Quadped;
 import org.umlg.runtime.test.BaseLocalDbTest;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class JsonTest extends BaseLocalDbTest {
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testEmbeddedManiesToJson() throws IOException {
 		God god = new God(true);
 		god.addToEmbeddedString("embeddedString1");
 		god.addToEmbeddedString("embeddedString2");
 		god.addToEmbeddedString("embeddedString3");
         db.commit();
 		Assert.assertEquals(4, countVertices());
 
 		ObjectMapper mapper = new ObjectMapper();
 		System.out.println(god.toJson());
 		Map<String, Object> jsonObject = mapper.readValue(god.toJson(), Map.class);
 		Assert.assertFalse(jsonObject.isEmpty());
 		Assert.assertSame(ArrayList.class, jsonObject.get("embeddedString").getClass());
 		boolean foundembeddedString1 = false;
 		boolean foundembeddedString2 = false;
 		boolean foundembeddedString3 = false;
 		List<String> embeddeds = (List<String>) jsonObject.get("embeddedString");
 		for (String s : embeddeds) {
 			if (s.equals("embeddedString1")) {
 				foundembeddedString1 = true;
 			}
 			if (s.equals("embeddedString2")) {
 				foundembeddedString2 = true;
 			}
 			if (s.equals("embeddedString3")) {
 				foundembeddedString3 = true;
 			}
 		}
 		Assert.assertTrue(foundembeddedString1 && foundembeddedString2 && foundembeddedString3);
 	}
 
 	@Test
 	public void testEmbeddedManiesFromJson() throws IOException {
 		God god = new God(true);
 		god.addToEmbeddedString("embeddedString1");
 		god.addToEmbeddedString("embeddedString2");
 		god.addToEmbeddedString("embeddedString3");
 		god.addToEmbeddedInteger(1);
 		god.addToEmbeddedInteger(2);
 		god.addToEmbeddedInteger(3);
         db.commit();
 		Assert.assertEquals(7, countVertices());
 
 		String json = god.toJson();
 		God godtest = new God(true);
 		godtest.fromJson(json);
 		Assert.assertEquals(god.getEmbeddedString().size(), godtest.getEmbeddedString().size());
 		Assert.assertEquals(god.getEmbeddedInteger().size(), godtest.getEmbeddedInteger().size());
 		boolean foundembeddedString1 = false;
 		boolean foundembeddedString2 = false;
 		boolean foundembeddedString3 = false;
 		for (String s : godtest.getEmbeddedString()) {
 			if (s.equals("embeddedString1")) {
 				foundembeddedString1 = true;
 			}
 			if (s.equals("embeddedString2")) {
 				foundembeddedString2 = true;
 			}
 			if (s.equals("embeddedString3")) {
 				foundembeddedString3 = true;
 			}
 		}
 		Assert.assertTrue(foundembeddedString1 && foundembeddedString2 && foundembeddedString3);
 		boolean foundembeddedInteger1 = false;
 		boolean foundembeddedInteger2 = false;
 		boolean foundembeddedInteger3 = false;
 		for (Integer i : godtest.getEmbeddedInteger()) {
 			if (i.equals(1)) {
 				foundembeddedInteger1 = true;
 			}
 			if (i.equals(2)) {
 				foundembeddedInteger2 = true;
 			}
 			if (i.equals(3)) {
 				foundembeddedInteger3 = true;
 			}
 		}
 		Assert.assertTrue(foundembeddedInteger1 && foundembeddedInteger2 && foundembeddedInteger3);
 
 	}
 
     @Test
     public void testEmbeddedManyBooleans() throws IOException {
         God god = new God(true);
         god.setName("god");
         TestEmbedded testEmbedded = new TestEmbedded(god);
         testEmbedded.setName("testEmbedded");
         testEmbedded.addToManyBoolean(true);
         testEmbedded.addToManyBoolean(true);
         testEmbedded.addToManyBoolean(true);
         testEmbedded.addToManyBoolean(false);
         testEmbedded.addToManyBoolean(false);
         testEmbedded.addToManyBoolean(false);
         db.commit();
         Assert.assertEquals(6, testEmbedded.getManyBoolean().size());
         String json = testEmbedded.toJson();
 
         TestEmbedded testTestEmbedded = new TestEmbedded(true);
         testTestEmbedded.addToManyBoolean(true);
         testTestEmbedded.addToManyBoolean(true);
         testTestEmbedded.addToManyBoolean(true);
         testTestEmbedded.addToManyBoolean(true);
         testTestEmbedded.addToManyBoolean(true);
         testTestEmbedded.addToManyBoolean(true);
         testTestEmbedded.addToManyBoolean(true);
 
         testTestEmbedded.fromJson(json);
         Assert.assertEquals(6, testTestEmbedded.getManyBoolean().size());
 
         List<Boolean> trueList = new ArrayList<Boolean>();
         List<Boolean> falseList = new ArrayList<Boolean>();
         for (Boolean b : testTestEmbedded.getManyBoolean()) {
             if (b) {
                 trueList.add(b);
             } else {
                 falseList.add(b);
             }
         }
         Assert.assertEquals(3, trueList.size());
         Assert.assertEquals(3, falseList.size());
         Assert.assertEquals("testEmbedded", testTestEmbedded.getName());
         Assert.assertNull(testTestEmbedded.getGod());
     }
 
     @Test
     public void testEmbeddedManyInteger() throws IOException {
         God god = new God(true);
         god.setName("god");
         TestEmbedded testEmbedded = new TestEmbedded(god);
         testEmbedded.setName("testEmbedded");
         //Has default value of 1,2,3
         testEmbedded.addToManyOrderedRequiredInteger(1);
         testEmbedded.addToManyOrderedRequiredInteger(2);
         testEmbedded.addToManyOrderedRequiredInteger(3);
         testEmbedded.addToManyOrderedRequiredInteger(4);
         testEmbedded.addToManyOrderedRequiredInteger(5);
         testEmbedded.addToManyOrderedRequiredInteger(6);
         db.commit();
         Assert.assertEquals(9, testEmbedded.getManyOrderedRequiredInteger().size());
         String json = testEmbedded.toJson();
 
         TestEmbedded testTestEmbedded = new TestEmbedded(true);
         testTestEmbedded.addToManyOrderedRequiredInteger(9);
         testTestEmbedded.addToManyOrderedRequiredInteger(8);
         testTestEmbedded.addToManyOrderedRequiredInteger(7);
         testTestEmbedded.addToManyOrderedRequiredInteger(6);
         testTestEmbedded.addToManyOrderedRequiredInteger(5);
         testTestEmbedded.addToManyOrderedRequiredInteger(4);
 
         testTestEmbedded.fromJson(json);
         Assert.assertEquals(9, testTestEmbedded.getManyOrderedRequiredInteger().size());
 
         Assert.assertEquals(new Integer(1), testTestEmbedded.getManyOrderedRequiredInteger().get(0));
         Assert.assertEquals(new Integer(2), testTestEmbedded.getManyOrderedRequiredInteger().get(1));
         Assert.assertEquals(new Integer(3), testTestEmbedded.getManyOrderedRequiredInteger().get(2));
         Assert.assertEquals(new Integer(1), testTestEmbedded.getManyOrderedRequiredInteger().get(3));
         Assert.assertEquals(new Integer(2), testTestEmbedded.getManyOrderedRequiredInteger().get(4));
         Assert.assertEquals(new Integer(3), testTestEmbedded.getManyOrderedRequiredInteger().get(5));
         Assert.assertEquals(new Integer(4), testTestEmbedded.getManyOrderedRequiredInteger().get(6));
         Assert.assertEquals(new Integer(5), testTestEmbedded.getManyOrderedRequiredInteger().get(7));
         Assert.assertEquals(new Integer(6), testTestEmbedded.getManyOrderedRequiredInteger().get(8));
 
         Assert.assertEquals("testEmbedded", testTestEmbedded.getName());
         Assert.assertNull(testTestEmbedded.getGod());
     }
 
     @Test
     public void testEmbeddedManyString() throws IOException {
         God god = new God(true);
         god.setName("god");
         TestEmbedded testEmbedded = new TestEmbedded(god);
         testEmbedded.setName("testEmbedded");
         //Has default value of 1,2,3
         testEmbedded.addToManyOrderedString("a");
         testEmbedded.addToManyOrderedString("b");
         testEmbedded.addToManyOrderedString("c");
         testEmbedded.addToManyOrderedString("d");
         testEmbedded.addToManyOrderedString("e");
         testEmbedded.addToManyOrderedString("f");
         db.commit();
         Assert.assertEquals(6, testEmbedded.getManyOrderedString().size());
         String json = testEmbedded.toJson();
 
         TestEmbedded testTestEmbedded = new TestEmbedded(true);
         testTestEmbedded.addToManyOrderedString("");
         testTestEmbedded.addToManyOrderedString("");
         testTestEmbedded.addToManyOrderedString("");
         testTestEmbedded.addToManyOrderedString("");
         testTestEmbedded.addToManyOrderedString("");
         testTestEmbedded.addToManyOrderedString("");
 
         testTestEmbedded.fromJson(json);
         Assert.assertEquals(6, testTestEmbedded.getManyOrderedString().size());
 
         Assert.assertEquals("a", testTestEmbedded.getManyOrderedString().get(0));
         Assert.assertEquals("b", testTestEmbedded.getManyOrderedString().get(1));
         Assert.assertEquals("c", testTestEmbedded.getManyOrderedString().get(2));
         Assert.assertEquals("d", testTestEmbedded.getManyOrderedString().get(3));
         Assert.assertEquals("e", testTestEmbedded.getManyOrderedString().get(4));
         Assert.assertEquals("f", testTestEmbedded.getManyOrderedString().get(5));
 
         Assert.assertEquals("testEmbedded", testTestEmbedded.getName());
         Assert.assertNull(testTestEmbedded.getGod());
     }
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testJsonToFromWithNulls() throws IOException {
 		God g1 = new God(true);
 		g1.setName("g1");
 		g1.setReason(REASON.BAD);
 		God g2 = new God(true);
 		g2.setName("g2");
 		g2.setReason(null);
         db.commit();
 
 		ObjectMapper objectMapper = new ObjectMapper();
 		Map<String, Object> jsonMap = objectMapper.readValue(g1.toJson(), Map.class);
 		Assert.assertEquals("BAD", jsonMap.get("reason"));
 
 		jsonMap = objectMapper.readValue(g2.toJson(), Map.class);
 		Assert.assertEquals(null, jsonMap.get("reason"));
 
 		God god1FromJson = new God(true);
 		god1FromJson.fromJson(g1.toJson());
 		God god2FromJson = new God(true);
 		god2FromJson.fromJson(g2.toJson());
         db.commit();
 
 		Assert.assertEquals(REASON.BAD, god1FromJson.getReason());
 		Assert.assertEquals("g1", god1FromJson.getName());
 		Assert.assertNull(god2FromJson.getReason());
 		Assert.assertEquals("g2", god2FromJson.getName());
 		Assert.assertNull(god1FromJson.getBeginning());
 		Assert.assertNull(god2FromJson.getBeginning());
 		Assert.assertNull(god1FromJson.getPet());
 		Assert.assertNull(god2FromJson.getPet());
 	}
 
 	@Test
 	public void testDates() throws IOException {
 		God g1 = new God(true);
 		g1.setName("g1");
 		DateTime beginning = new DateTime();
 		g1.setBeginning(beginning);
         db.commit();
 
 		God testG = new God(g1.getVertex());
 		Assert.assertEquals(beginning, testG.getBeginning());
 
 		ObjectMapper objectMapper = new ObjectMapper();
 		@SuppressWarnings("unchecked")
 		Map<String, Object> jsonMap = objectMapper.readValue(testG.toJson(), Map.class);
 
 		Assert.assertEquals(beginning.toString(), jsonMap.get("beginning"));
 	}
 
 	@Test
 	public void testValidation() throws IOException {
 		ObjectMapper objectMapper = new ObjectMapper();
 		String json = UniverseRuntimePropertyEnum.asJson();
 		@SuppressWarnings("unchecked")
 		Map<String, ArrayList<Map<String, Object>>> jsonMap = objectMapper.readValue(json, Map.class);
 		ArrayList<Map<String, Object>> o = jsonMap.get("properties");
 		boolean foundValidations = false;
 		for (Map<String, Object> map : o) {
 			foundValidations = map.containsKey("validations");
 		}
 		Assert.assertTrue(foundValidations);
 	}
 
 	@Test
 	public void testWithInheritence() throws IOException {
 		God g1 = new God(true);
 		g1.setName("g1");
 		DateTime beginning = new DateTime();
 		g1.setBeginning(beginning);
 		Mamal mamal1 = new Mamal(g1);
 		mamal1.setName("mamal1");
 		Biped biped1 = new Biped(g1);
 		biped1.setName("biped1");
 		Quadped quadped1 = new Quadped(g1);
 		quadped1.setName("quadped1");
         db.commit();
 
 		System.out.println(quadped1.toJson());
 		ObjectMapper objectMapper = new ObjectMapper();
 		@SuppressWarnings("unchecked")
 		Map<String, Object> jsonMap = objectMapper.readValue(quadped1.toJson(), Map.class);
 		Assert.assertEquals(8, jsonMap.size());
 		Assert.assertEquals(jsonMap.get("name"), "quadped1");
 	}
 
     @Test
     public void testManyEnum() throws IOException {
         God god = new God(true);
         god.setName("god");
         god.addToREASON(REASON.BAD);
         god.addToREASON(REASON.GOOD);
         db.commit();
 
         ObjectMapper objectMapper = new ObjectMapper();
         objectMapper.readValue(god.toJson(), Map.class);
 
         God godTest = new God(true);
         godTest.fromJson(god.toJson());
 
         Assert.assertEquals(2, godTest.getREASON().size());
     }
 
 }
