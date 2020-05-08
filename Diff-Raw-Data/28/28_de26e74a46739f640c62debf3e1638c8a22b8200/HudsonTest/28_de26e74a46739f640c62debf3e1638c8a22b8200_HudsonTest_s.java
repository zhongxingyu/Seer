 package com.jsmadja.wall.hudsonclient.it;
 
 import org.junit.Test;
 
 import com.jsmadja.wall.hudsonclient.Hudson;
 import com.jsmadja.wall.hudsonclient.HudsonBuildNotFoundException;
 import com.jsmadja.wall.hudsonclient.builder.HudsonUrlBuilder;
 
 public class HudsonTest {
 
     private static final String HUDSON_URL = "http://fluxx.fr.cr:8080/hudson";
 
     HudsonUrlBuilder hudsonUrlBuilder = new HudsonUrlBuilder(HUDSON_URL);
 
     private Hudson hudson = new Hudson(HUDSON_URL);
 
     @Test(expected = HudsonBuildNotFoundException.class)
     public void should_throw_an_exception_when_searching_an_inexistant_build() throws HudsonBuildNotFoundException {
         hudson.findBuild("dev-radar", -1);
     }
 }
