 package com.badlogic.gdx.tests.utils;
 
 import static org.junit.Assert.*;
 
 import java.util.Random;
 
 import org.hamcrest.core.IsEqual;
 import org.junit.Test;
 
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.utils.IntMap;
 
 public class IntMapTest {
 
 	@Test
 	public void bugSizeShouldNotBeChangedIfElementReplaced() {
 		IntMap<Object> intMap = new IntMap<Object>();
 
 		assertThat(intMap.size, IsEqual.equalTo(0));
 		intMap.put(0, new Object());
 		assertThat(intMap.size, IsEqual.equalTo(1));
 
 		intMap.put(0, new Object());
 		assertThat(intMap.size, IsEqual.equalTo(1));
 	}
 	
 	@Test
 	public void bugWhenStashContainsKey() {
 		IntMap<Object> intMap = new IntMap<Object>();
 
 		MathUtils.random = new Random(10);
 		
 		int[] keys = {30314, 71946, 113578, 155210};
 
 		for (int i = 0; i < keys.length; i++) 
 			intMap.put(keys[i], new Object());
 		
 		intMap.put(keys[3], new Object());
 		assertThat(intMap.size, IsEqual.equalTo(keys.length));
 	}
 
 }
