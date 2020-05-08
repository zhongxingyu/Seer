import java.util.*;
 
 
 public class Assignment2 {
 
 	public static void main(String[] args) {
 		vehicleTest();
 		hashSetTest();
 		linkedHashSetTest();
 		queueTest();
 		linkedListTest();
 		arrayListTest();
 		hashMapTest();
 		treeMapTest();
 	}
 	
 	private static void vehicleTest()
 	{
 		Sedan sedan = new Sedan("white", 100.0, "wind");
 		Motorcycle motorcycle = new Motorcycle("black", 120.0, "flash");
 		Truck truck = new Truck("yellow", 88.0, "grandma");
 		Vehicle currentVehicle;
 		
 		TreeSet<Vehicle> compareSpeed = new TreeSet<Vehicle>(new VehicleComparator_Speed());
 		TreeSet<Vehicle> compareColor = new TreeSet<Vehicle>(new VehicleComparator_Color());
 		TreeSet<Vehicle> compareWheels = new TreeSet<Vehicle>(new VehicleComparator_Wheels());
 		
 		compareSpeed.add(sedan);
 		compareColor.add(sedan);
 		compareWheels.add(sedan);
 		compareSpeed.add(motorcycle);
 		compareColor.add(motorcycle);
 		compareWheels.add(motorcycle);
 		compareSpeed.add(truck);
 		compareColor.add(truck);
 		compareWheels.add(truck);
 		
 		System.out.println("Order of speed:");
 		while((currentVehicle = compareSpeed.pollFirst()) != null)
 		{
 			System.out.print(currentVehicle.toString().substring(0, currentVehicle.toString().indexOf("@")));
 			System.out.print(", " + currentVehicle.color);
 			System.out.println(", " + currentVehicle.nickname);
 		}
 		
 		System.out.println("Order of color:");
 		while((currentVehicle = compareColor.pollFirst()) != null)
 		{
 			System.out.print(currentVehicle.toString().substring(0, currentVehicle.toString().indexOf("@")));
 			System.out.print(", " + currentVehicle.color);
 			System.out.println(", " + currentVehicle.nickname);
 		}
 		
 		System.out.println("Order of number of wheels:");
 		while((currentVehicle = compareWheels.pollFirst()) != null)
 		{
 			System.out.print(currentVehicle.toString().substring(0, currentVehicle.toString().indexOf("@")));
 			System.out.print(", " + currentVehicle.color);
 			System.out.println(", " + currentVehicle.nickname);
 		}
 	}
 	
 	private static void hashSetTest()
 	{
 		HashSet<Double> hs = new HashSet<Double>();
 		hs.add(1.3);
 		hs.add(4.2);
 		hs.add(77.0);
 		hs.add(100.43);
 		hs.add(2.5);
 		System.out.println(hs);
 	}
 	
 	private static void linkedHashSetTest()
 	{
 		Set<String> lhs = new LinkedHashSet<String>();
 		lhs.add("Monday");
 		lhs.add("Tuesday");
 		lhs.add("Wednesday");
 		lhs.add("Thursday");
 		lhs.add("Friday");
 		lhs.add("Saturday");
 		lhs.add("Sunday");
 		System.out.println(lhs);
 	}
 	
 	private static void queueTest()
 	{
 		Queue<Double> q = new LinkedList<Double>();
 		q.add(1.2);
 		q.add(1.3);
 		q.add(2.5);
 		q.add(5.5);
 		q.add(110.0);
 		System.out.println(q);
 		while (q.poll() != null); // Removes all items from queue
 	}
 	
 	private static void linkedListTest()
 	{
 		LinkedList<String> ll = new LinkedList<String>();
 		ll.add("red");
 		ll.add("yellow");
 		ll.add("black");
 		ll.add("blue");
 		ll.add("green");
 		System.out.println(ll);
 		Iterator<String> backwardLL = ll.descendingIterator();
 		System.out.print("[");
 		while (backwardLL.hasNext())
 		{
 			System.out.print(backwardLL.next());
 			if (backwardLL.hasNext())
 				System.out.print(", ");
 		}
 		System.out.println("]");
 		
 	}
 	
 	private static void arrayListTest()
 	{
 		ArrayList<Integer> al = new ArrayList<Integer>();
 		al.add(2);
 		al.add(3);
 		al.add(5);
 		al.add(6);
 		al.add(1);
 		al.add(2);
 		Iterator<Integer> x = al.iterator();
 		System.out.print("[");
 		while (x.hasNext())
 		{
 			System.out.print(x.next());
 			if (x.hasNext())
 				System.out.print(", ");
 		}
 		System.out.println("]");
 		al.remove(0);
 		x = al.iterator();
 		System.out.print("[");
 		while (x.hasNext())
 		{
 			System.out.print(x.next());
 			if (x.hasNext())
 				System.out.print(", ");
 		}
 		System.out.println("]");
 	}
 	
 	private static void hashMapTest()
 	{
 		HashMap<Integer, String> hm = new HashMap<Integer, String>();
 		hm.put(2, "Smith");
 		hm.put(1, "Anderson");
 		hm.put(3, "Johnson");
 		hm.put(4, "White");
 		System.out.println(hm.get(3));
 	}
 	
 	private static void treeMapTest()
 	{
 		TreeMap<Integer, String> tm = new TreeMap<Integer, String>();
 		tm.put(2, "Smith");
 		tm.put(1, "Anderson");
 		tm.put(3, "Johnson");
 		tm.put(4, "White");
 		System.out.println(tm);
 	}
 
 }
