 package com.shockk.SALabs.unit1;
 
 public class IntArrayTest
 {
 	public static void main(String[] args)
 	{
 		int[] array1 = {10, 20, 30, 40, 50};
 		int[] array2 = array1;
 		int[] array3 = new int[array1.length];
 
 		System.out.println("array1");
 		display_array(array1);
 		System.out.println("array2");
 		display_array(array2);
 		System.out.println("array3");
 		display_array(array3);
 		
 		copy_array(array1, array3);
 		reverse_array(array2);
 
 		System.out.println();
 		System.out.println("array1");
 		display_array(array1);
 		System.out.println("array2");
 		display_array(array2);
 		System.out.println("array3");
 		display_array(array3);
 	}
 	
 	public static void display_array(int[] a)
 	{
 		for(int v : a) System.out.print(v + " ");
 		System.out.println();
 	}
 	
 	public static void copy_array(int[] a1, int[] a2)
 	{
 		for(int i=0; i<a1.length; ++i)
 		{
 			a2[i] = a1[i];
 		}
 	}
 	
 	public static void reverse_array(int[] a)
 	{
 		for(int tail=a.length-1; tail>0; --tail)
 		{
 			for(int head=0; head<tail; ++head)
 			{
 				// swap values with XOR logic
 				a[head]     = a[head] ^ a[head + 1];
 				a[head + 1] = a[head] ^ a[head + 1];
 				a[head]     = a[head] ^ a[head + 1];
 			}
 		}
 	}
}
