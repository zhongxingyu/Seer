 package com.github.kpacha.jkata.anagram;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class Anagram {
 
     public static Set<String> generate(String source) {
 	Set<String> result = new HashSet<String>();
	if (source.length() == 2) {
	    result.add(source.substring(1) + source.substring(0, 1));
	}
 	result.add(source);
 	return result;
     }
 }
