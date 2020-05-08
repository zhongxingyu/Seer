 package map;
 
 public class Main {
 	
 	public static void main(String[] args) {
 		SimpleHashMap<Integer, Integer> map = new SimpleHashMap<Integer, Integer>();
 		map.put(1, 5);
 		map.put(7, 2);
 		map.put(-15, 3);
 		map.put(3, 14);
 		map.put(10, 10);
 		map.put(8, 9);
 		map.put(16, 7);
 		map.put(-13, 3);
 		map.put(0, 12);
 		map.put(-5, 13);
 		map.put(11, 16);
		System.out.println(map.show());
 	}
 }
