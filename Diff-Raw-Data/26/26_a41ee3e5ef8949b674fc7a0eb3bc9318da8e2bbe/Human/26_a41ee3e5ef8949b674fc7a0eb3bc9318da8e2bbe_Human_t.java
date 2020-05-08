 package edu.ucsb.cs56.S13.lab04.mastergberry;
 
 public class Human {
  
     private String name;
     private int age;
  
 	/** Default constructor to create a Human with no name or age */
     Human() {
         this.name = "";
         this.age = 0;
     }
  
 	/**
 	@param n name of a human
 	@param a age of a human
 	*/
     Human(String n, int a) {
        this.name = n;
        this.age = a;
     }
  
 	/**
 		@return name (e.g. Adam, Conrad)
 	*/
    public String getName() { return name; }
 
 	/**
 		@param name human's name
 		@return void (is a setter for name)
 	*/
    public void setName(String n) { this.name = n; }
 
 	/**
 		@return age (20, 33) 	
 	*/
    public int getAge() { return age; }
 
 	/**
 		@param age human's age
 		@return void (is a setter for age)
 	*/
    public void setAge(int a) { this.age = a; }
 
 	/**
 		Print out a name with associated age, e.g ("Adam is 20 years old.")
 		@return String representation of human.
 	*/
 	public String toString() { 
		return name + " is " + age + " years old.";
 	}
 
 	/**
 		Checks to see if two humans are equal
 		@param h another human object to compare
 		@return true if this object is the same as the other human object 
 	*/
 	public boolean equals(Human h) {
		if (h.age != this.age)
			return false;
		if (h.name != this.name)
			return false;
		return true;
 	}
 
 	public static void main(String [] args) {
 		Human adam = new Human("Adam", 20);
 		Human dummy = new Human();
 		System.out.println(adam);
 	}
 
 } // class Human.java
