 package com.mckesson.bo.impl;
 
 import com.mckesson.bo.HelloWorldBo;
 
 public class HelloWorldBoImpl implements HelloWorldBo {
 	
 	public String getHelloWorld(String name) {
		if (name == null)
 			return "Hello World!";
 		return "Hello " + name + "!";
 	}
 }
