 import java.util.ArrayList;
 public class TreeTest
 {
 	public static void main(String [] args)
 	{
 		IntTreeBag bag1 = new IntTreeBag(50);
 		bag1.add(45);
 		bag1.add(90);
 		bag1.add(100);
 		bag1.add(6);
 		bag1.add(87);
 		bag1.add(45);
 		//bag1.add(88);
 		bag1.print(6);
 		
 		
 		
 	//	ArrayList<Integer> ir = IntTreeBag.getAlldata();
 		//System.out.println(ir);
 		int allData [] = bag1.getAllDatawihtArray();
 		//System.out.println(bag1.getAllDatawihtArray());
		System.out.println("for - loop");
		for(int i = 1; i < allData.length; i++)
 		{
 			System.out.println("for - loop");
 			System.out.println("i =" + allData[i]);
 		}
 		/*for(int i = 0; i < 10; i++)
 		{
 			System.out.println(bag1.al.get(i));
 		}
 		*/
 	}
 }
