 package com.github.kpacha.jkata.anagram;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class Anagram {
 
     public static Set<String> generate(String source) {
 	Set<String> result = new HashSet<String>();
 	result.add(source);
 	return result;
     }
 }
