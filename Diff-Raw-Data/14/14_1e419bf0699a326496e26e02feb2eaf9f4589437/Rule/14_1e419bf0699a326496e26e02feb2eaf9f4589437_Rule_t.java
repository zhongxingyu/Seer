 package org.hack;
 
 import java.util.Map;
 
 public interface Rule {
    void evaluate(Map<String,Object> asset, Map<String,Object> globals);
 }
