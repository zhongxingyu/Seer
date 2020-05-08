 public class Flavors {
 	private static String[] flavors={"Mint Chocolate Chip", "Chocolate", "Vanilla", "Strawberry", "Cookies N' Cream", "Strawberry"};
 	public Flavors(int flavors)
 		{
 		int favorite=flavors;
 		if (flavors>=1 && flavors<=5)
 		{
 			System.out.println("Your favorite ice cream flavor is " +this.flavors[flavors]);
 		}
 		else
 			{
 				System.out.println("We don't offer "+favorite);
 			}
 		
 
 		}
 	public static void ListFlavors()
 	{
 	    for (int i=1; i<flavors.length; i++)
 	    {
 	      System.out.println(i+": "+flavors[i]);
 	    } 
 	}
 	public String getFlavors()
 	{
		return "Your flavor is" +flavors[Flavors];
 		
 	}
 	}
 	
