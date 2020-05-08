 package com.stephenn.problems.knapsack.genetic;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import junit.framework.*;
 
 import static org.mockito.Mockito.*;
 
 public class EntityTest extends TestCase {
 	
 	float[] prices = {1,1,1};
 	float target = 30;
 	
 	Entity a = new Entity(new int[] {8,8,8}, prices, target);
 	Entity b = new Entity(new int[] {12,12,12}, prices, target);
 	
 	public void testCompair(){
 		List<Entity> list = Arrays.asList(a,b);
 		
 		Collections.sort(list);
 		
 		assertTrue(Arrays.equals(list.get(1).items, new int[]{8,8,8}));
 	}
 	
 	public void testFitness(){
		assertEquals(a.fitness(), 24f);
		assertEquals(b.fitness(), -1f);
 	}
 	
 	public void testMate(){
 		Random mockedRandom = mock(Random.class);
 		when(mockedRandom.nextFloat()).thenReturn(0.5f);
 		when(mockedRandom.nextInt(10)).thenReturn(9);
 		
 		Entity child = a.mate(b, mockedRandom);
 		
 		assertTrue(Arrays.equals(child.items, new int[]{10,10,10}));
 	}
 	
 	public void testMate2(){
 		Random mockedRandom = mock(Random.class);
 		when(mockedRandom.nextFloat()).thenReturn(0.5f);
 		when(mockedRandom.nextInt(10)).thenReturn(0);
 		
 		Entity child = a.mate(b, mockedRandom);
 		
 		assertTrue(Arrays.equals(child.items, new int[]{11,11,11}));
 	}
 	
 	public void testMate3(){
 		Random mockedRandom = mock(Random.class);
 		when(mockedRandom.nextFloat()).thenReturn(0.5f);
 		when(mockedRandom.nextInt(10)).thenReturn(1);
 		
 		Entity child = a.mate(b, mockedRandom);
 		
 		assertTrue(Arrays.equals(child.items, new int[]{9,9,9}));
 	}
 }
