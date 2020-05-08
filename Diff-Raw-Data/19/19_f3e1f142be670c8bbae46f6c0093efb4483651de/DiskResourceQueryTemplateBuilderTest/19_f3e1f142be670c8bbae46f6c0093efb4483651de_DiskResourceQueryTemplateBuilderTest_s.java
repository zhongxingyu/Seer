 package org.iplantc.core.uicommons.client.services.impl;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.gwt.thirdparty.guava.common.base.Splitter;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 public class DiskResourceQueryTemplateBuilderTest {
 
     @Before public void setUp() {
         
     }
 
     @Test public void testBuild() {
         String testString = "ONE_FIELD: derpy derpy doo OTHER_FIELD: gorpy groovy gravy YET_ANOTHER_FIELD: torpy";
 
         final HashMap<String, List<String>> newHashMap = Maps.newHashMap();
         final Iterable<String> split = Splitter.on(" ").split(testString);
         String lastTerm = null;
         for (String term : split) {
             if (term.matches("^\\b\\w+:$")) {
                 newHashMap.put(term, Lists.<String> newArrayList());
                 lastTerm = term;
             } else if (lastTerm != null) {
                 newHashMap.get(lastTerm).add(term);
             }
         }
 
         final Set<Entry<String, List<String>>> entrySet = newHashMap.entrySet();
         for (Entry<String, List<String>> entry : entrySet) {
            System.out.println("Key : " + entry.getKey() + " -> " + entry.getValue());
         }
         assertTrue(true);
     }
 
 }
