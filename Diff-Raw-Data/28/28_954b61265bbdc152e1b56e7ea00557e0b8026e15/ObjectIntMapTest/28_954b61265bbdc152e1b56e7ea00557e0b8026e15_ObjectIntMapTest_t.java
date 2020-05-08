 package com.badlogic.gdx.utils;
 
 import java.util.Random;
 
import com.badlogic.gdx.math.MathUtils;

 public class ObjectIntMapTest {
 
 	public static class Tuple {
		public Integer key;
 		public int value;
 	}
 
 	public static final int NOT_PRESENT = -1;
 
 	public static void main(String[] args) throws Exception {
 
 		ObjectIntMap<Object> map = new ObjectIntMap<Object>();
 		boolean comments = true;
 		Array<Tuple> addedObjects = new Array<Tuple>();
 
 		long seed = 8686956005147532L;
		MathUtils.random = new Random(seed);
		int freeKey = 1;
 		for (int i = 0; i < 1000000000; i++) {
 			try {
 				if (i % 1000000 == 0)
 					System.out.println("Processing iteration " + i);
 
 				// float f = MathUtils.random();
				float f = MathUtils.random.nextFloat();
 
 				if (f > 0.6f) {
 
 					Tuple tuple = new Tuple();
					tuple.key = new Integer(freeKey);
					freeKey++;
					tuple.value = MathUtils.random.nextInt(10000);
 					addedObjects.add(tuple);
 					map.put(tuple.key, tuple.value);
 					int obtainedValue = map.get(tuple.key, NOT_PRESENT);
 					if (obtainedValue != tuple.value) {
 						throw new RuntimeException("The obtained value: " + obtainedValue + " is not the same as " + tuple.value);
 					}
 
 					if (addedObjects.size != map.size && comments)
 						System.out.println("Error " + i + " when adding: teoricSize: " + addedObjects.size + " - sizeSet: " + map.size);
 				} else if (f > 0.3) {
 					if (map.size <= 0)
 						continue;
 
 					// int index = MathUtils.random(0, addedObjects.size - 1);
					int index = MathUtils.random.nextInt(addedObjects.size);
 
 					Tuple tupleToRemove = addedObjects.removeIndex(index);
 					int removedValue = map.remove(tupleToRemove.key, NOT_PRESENT);
 					if (removedValue != tupleToRemove.value)
 						throw new RuntimeException("The removed value: " + removedValue + " is not the same as " + tupleToRemove.value);
 
 					if (map.containsKey(tupleToRemove.key))
 						throw new RuntimeException("The removed object is still present according to map");
 
 					if (addedObjects.size != map.size && comments)
 						System.out.println("Error " + i + " when removing: theoricSize: " + addedObjects.size + " - sizeSet: " + map.size);
 				} else {
 					if (map.size <= 0)
 						continue;
 
 					// int index = MathUtils.random(0, addedObjects.size - 1);
					int index = MathUtils.random.nextInt(addedObjects.size);
 
 					Tuple tupleToModify = addedObjects.get(index);
					tupleToModify.value = MathUtils.random.nextInt(10000);
 					map.put(tupleToModify.key, tupleToModify.value);
 					int obtainedValue = map.get(tupleToModify.key, NOT_PRESENT);
 					if (obtainedValue != tupleToModify.value) {
 						throw new RuntimeException("The obtained value: " + obtainedValue + " is not the same as " + tupleToModify.value);
 					}
 
 					if (addedObjects.size != map.size && comments)
 						System.out.println("Error " + i + " when modifying: theoricSize: " + addedObjects.size + " - sizeSet: " + map.size);
 				}
 			} catch (Exception e) {
 				System.out.println("Exception in iteration " + i);
 				e.printStackTrace();
 				throw e;
 			}
 		}
 
 	}
 }
