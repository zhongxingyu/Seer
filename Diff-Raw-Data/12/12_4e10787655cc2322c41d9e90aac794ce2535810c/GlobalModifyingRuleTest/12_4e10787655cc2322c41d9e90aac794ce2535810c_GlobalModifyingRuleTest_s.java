 package org.hack;
 
 import com.google.common.base.Predicate;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static junit.framework.Assert.assertEquals;
 
 
 public class GlobalModifyingRuleTest {
     private Map<String, Object> asset;
     private Map<String, Object> globals;
 
     @Before
     public void setUp() throws Exception {
         asset = new HashMap<String,Object>();
         globals = new HashMap<String,Object>();
     }
 
     @Test
     public void testFalseCondition() throws Exception {
         Predicate<Map<String,Object>> condition = new JexlCondition("false");
         Action action = new JexlAction("key = 'value'");
         Rule sut = new GlobalModifyingRule(condition, action);
         sut.evaluate(asset, globals);
         assertEquals(null, asset.get("key"));
         assertEquals(null, globals.get("key"));
     }
 
     @Test
     public void testTrueCondition() throws Exception {
         Predicate<Map<String,Object>> condition = new JexlCondition("true");
         Action action = new JexlAction("key = 'value'");
         Rule sut = new GlobalModifyingRule(condition, action);
         sut.evaluate(asset, globals);
         assertEquals(null, asset.get("key"));
         assertEquals("value", globals.get("key"));
     }
 }
