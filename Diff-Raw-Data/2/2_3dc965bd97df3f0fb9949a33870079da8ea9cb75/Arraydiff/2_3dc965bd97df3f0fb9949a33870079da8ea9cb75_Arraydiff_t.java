 
 public class Arraydiff
 {
 	public static void main (String [] args)
 	{
 		int [] array = new int [10];
 		//for(int index = 0; index )
 		for(int index = 0; index < array.length; index++)
 		{
 			System.out.println(array[index]);
 		}
 		put24s(array);
 		System.out.println("after calling put24s");
 		for(int index = 0; index < array.length; index++)
 		{
 			System.out.println(array[index]);
 		}
 		
 		
 	}
 	/* Array Parameters 
 	 * when a parameter is an array, then the parameter is initialized to refer to the same array that 
 	 * the actual argument refers to. therefore, if the method changes the components of the array,
	 * the changes do affect argument.    is this equal to call(pass)by reference? 
 	 */
 	
 	public static void put24s(int[] data)
 	{
 		int i; 
 		for(i = 0; i < data.length; i ++)
 		{
 			data[i] = 24;
 		}
 		
 	}
 }//class 
