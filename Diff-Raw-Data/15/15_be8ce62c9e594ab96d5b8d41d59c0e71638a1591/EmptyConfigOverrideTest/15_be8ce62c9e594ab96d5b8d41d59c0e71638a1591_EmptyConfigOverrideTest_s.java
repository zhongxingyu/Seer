 package com.nesscomputing.config;
 
 import java.util.Collections;
 
 import org.apache.commons.configuration.MapConfiguration;
 import org.junit.Test;
 
 public class EmptyConfigOverrideTest
 {
     @Test
     public void testEmptyConfigOverride()
     {
         // Fails in ness-cache <= 2.1.0
         Config.getOverriddenConfig(Config.getEmptyConfig(), new MapConfiguration(Collections.EMPTY_MAP));
     }
 }
