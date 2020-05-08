 package com.practice.thenewboston;
 
 import java.util.Scanner;
 
 class Practiceit {
 	public static void main (String[] args) {
 		Scanner input = new Scanner(System.in);
 		review reviewObject = new review();
 		System.out.println("Enter name of first friend: ");
		String friend = input.nextLine();
  		reviewObject.setName(friend);
  		reviewObject.saying();
 	}
 }
