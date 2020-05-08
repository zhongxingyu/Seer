 /**
  * 
  */
 package com.ssparrow.projecteuler;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /**
  * @author Gao, Fei
  *
  */
 public class Problem101To125Test {
 
 	@Test
 	public void testP107GetMaximumSaving() {
 		int [][] matrix=new int [][]{
 				{0,16,12,21,0,0,0},
 				{16,0,0,17,20,0,0},
 				{12,0,0,28,0,31,0},
 				{21,17,28,0,18,19,23},
 				{0,20,0,18,0,0,11},
 				{0,0,31,19,0,0,27},
 				{0,0,0,23,11,27,0}
 		};
 		
		assertEquals(150, Problem101To125.p107GetMaximumSaving(matrix));
 	}
 
 }
