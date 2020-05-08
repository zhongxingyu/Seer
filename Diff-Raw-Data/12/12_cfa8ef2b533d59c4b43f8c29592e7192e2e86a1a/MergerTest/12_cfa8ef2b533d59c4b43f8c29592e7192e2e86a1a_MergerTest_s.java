 package com.sm;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class MergerTest {
 
 	@Test
 	public void coffeeMergerTest(){
 		Merger merger = new Merger();
		merger.setModulesLocation("/modules/");
 		String val = null;
 		try{
 			val = merger.getModule("main");
 		}catch(Exception e){
 			Assert.fail(e.getMessage());
 		}
 		Assert.assertTrue(val.length() > 0);
 	}
 	
 	@Test
 	public void coffeeMergerDepsTest(){
 		Merger merger = new Merger();
		merger.setModulesLocation("/modules/");
 		String val = null;
 		try{
 			val = merger.getModule("main2");
 		}catch(Exception e){
 			Assert.fail(e.getMessage());
 		}
 		Assert.assertTrue(val.length() > 0);
 	}
 	
 }
