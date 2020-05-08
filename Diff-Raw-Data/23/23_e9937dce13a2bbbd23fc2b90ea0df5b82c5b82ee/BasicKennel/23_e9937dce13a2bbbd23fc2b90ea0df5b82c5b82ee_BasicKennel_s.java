 /**
 *A container class for storing all the pets into kennels. I
 *Is dependant on the Pet class in order to get the pets.
 *Determines, inserts, removes, and  if a pet is in the kennel.
 */
 public class BasicKennel{
 
 	private Pet[] pens;
 	
 	/**A constructor that creates a kennel with a set
 	*Amount of pens within the whole facility.*/
 	public BasicKennel(int numSpots)
 	{
 		pens = new Pet[numSpots];	
 	}
 	
 	
 	/**If the pen is empty, it will return true, if not it will return false
 	*I'm using this as it is easier to do this check than rewrite it each
 	*time I am doing a check.*/
 	public boolean isEmpty(int spot)
 	{
 		if (pens[spot] == null){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	/**Inserts a pet in the kennel, in a specific pen, and 
 	*Performs a check to see if there is viable space in the
 	*kennel to place the pet.*/
 	public void insert(int spot, Pet myPet)
 	{
 		if(spot<0 || spot>=pens.length)
 		{
 			throw new RuntimeException("Illegal index value: "
 					+spot);
 		}
 		if(!isEmpty(spot))
 		{
 			throw new RuntimeException("Cannot move pet to kennel "+
 					"that is currently in use");
 		}
 		else
 		{
 			pens[spot] = myPet;
 		}
 		}
 		
 	/**If the pet does not have a name within any of the kennels 
 	*it will return false at the end of the loop.
 	*Checks if the pet with a specific name is in the kennel.*/
 	public boolean hasPet(String name)
 	{
 		for(int i = 0; i < pens.length; i++)
 		{
 			if(pens[i].getName().equals(name) && pens[i].getName() != null)
 			{
 			return true;
 			}
 		}
 		return false;
 	}
 	
 	
 	/**If pet is not found, the function will return -1.
 	*Returns negative one because it is out of boundaries
 	*Of the array, so it does not get confused with
 	0, or n amount of pets.*/
 	public int penNumberOf(String pet)
 	{
 		
 		int petNotFound = -1;
 		for(int i = 0; i < pens.length; i++)
 		{
 			if(pens[i].getName().equals(pet))
 			{
				return i;
 			}
 			
 		}
 		return petNotFound;
 	}
 	
 	/**Removes the pet out of the kennel and clears the pen.
 	*However, if there is no pet to begin with, that is already
 	*null, it will return a runtime exception stating that 
 	*it is either an illegal index value (say it's an array of 5
 	*And you put 6) or if the kennel is empty (if 0 is null).*/
 	public void remove(int spot)
 	{
 		if(spot<0 || spot>=pens.length)
 		{
 			throw new RuntimeException("Illegal index value: "
 					+spot);
 		}
 		if(isEmpty(spot))
 		{
 			throw new RuntimeException("Cannot retrieve pet from" +
 										"an empty kennel");
 		}
 		else
 		{
 			pens[spot] = null;
 		}
 		
 	
 	}
 	
 	
 	/**toString is to output all the information to the console.
 	*It will tell what pet is in the kennel, and the pen they
 	*are located in. It builds the string, then adds to it at the
 	*end if the else condition is met.*/
 	public String toString(){
 		String temp = "";
 		for(int i = 0; i< pens.length; i++){
			String current_pen = "Pen number "+i+":\n";
 			temp = temp+current_pen;
 			if (isEmpty(i)){
 				temp = temp+"empty\n";
 			}
 			else{
 				temp = temp+pens[i].toString();
 			}
 			
 		}
 		return temp;
 	}
 	
 	/**Main function to test all the above functions, with
 	*text output to test to make sure everything works.*/
 	public static void main(String[] args)
 	{
 		BasicKennel myKennel = new BasicKennel(4);
 		
 		//test insertion
 		Pet pet1 = new Pet("Shibe", "Evan");
 		Pet pet2 = new Pet("Dober", "Kayle");
 		Pet pet3 = new Pet("Kitten", "Rianne");
 		Pet pet4 = new Pet("Fluffy", "Taylor");
 		myKennel.insert(0, pet1);
 		myKennel.insert(1, pet2);
		System.out.println("Now inserting Evan's Shibe into kennel 0, and Kayle's Dober" + 
							" into kennel 1...\n");
 		System.out.println(myKennel.toString());
 		
 		//test deletion
 		myKennel.remove(0);
		System.out.println("Now removing the pet from kennel 0...\n");
 		System.out.println(myKennel.toString());
 		myKennel.remove(1);
		System.out.println("Now removing the pet out of kennel 1...\n");
 		System.out.println(myKennel.toString());
 		
 		//test for has pet
 		myKennel.insert(3, pet3);
 		myKennel.insert(0, pet1);
 		myKennel.insert(1, pet2);
 		myKennel.insert(2, pet4);
		System.out.println("Now adding a Kitten to spot 3 in the kennel, " +
 							"and filling the kennel... \nchecking for Kitten...\n");
 		System.out.println(myKennel.hasPet("Kitten"));
 		System.out.println(myKennel.toString());
 		
 		//test for penNumberOf
 		System.out.println("Now testing to find where Fluffy is contained...\n");
 		if(myKennel.penNumberOf("Fluffy") == -1)
 		{
 			System.out.println("Fluffy is not in the kennel!\n");
 		}	
 		else
 		{
 			System.out.println("Fluffy is in pen # " + myKennel.penNumberOf("Fluffy") +
 								"\n");
 		}
 		
 		System.out.println("Now removing everyone out of the kennel..\n");
 		myKennel.remove(0);
 		myKennel.remove(1);
 		myKennel.remove(2);
 		myKennel.remove(3);
 		System.out.println(myKennel.toString());
 	}
}
