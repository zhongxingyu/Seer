 package buildingMain;
 
 import java.util.PriorityQueue;
 
 import people.Person;
 
 import building.Building;
 
 public class Main
 {
 	public static void main(String[] args)
 	{
 		int numPeople=10;
 		Building building = new Building();
		int nextFloor=1;
 		for (int i=0;i<numPeople;i++)
 		{
 			PriorityQueue<Integer> q = new PriorityQueue<Integer>();
 			q.add(nextFloor);
 			Person p = new Person(0,q,building,i);
 			Thread t = new Thread(p);
 			t.start();
 			if (i%2==0)
 			{
 				nextFloor++;
 			}
 		}
 		
 	}
 }
