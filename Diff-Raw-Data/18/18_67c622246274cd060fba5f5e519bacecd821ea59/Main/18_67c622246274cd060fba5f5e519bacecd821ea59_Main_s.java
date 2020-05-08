 class Main {
 	final int DEVIDE_BY = 64;
 	// Array to be sorted.
 	int tall [] = new int [Integer.MAX_VALUE / DEVIDE_BY];
 	long time;
 	public Main() {
 		initialSetup();
 		System.out.println("The current time in milliseconds before we start"
			+ " to devide the array: ");
 		System.out.println(time = System.currentTimeMillis());
 		new FindLeast (tall);
		System.out.println("Milliseconds after we have devided all the"
			+ " arrays");
 		System.out.println(System.currentTimeMillis() - time);
 		System.out.println("The current time in milliseconds before we start"
 			+ " to find the least number linear: ");
 		System.out.println(time = System.currentTimeMillis());
 		new FindLeastLinear(tall);
 		System.out.println("Milliseconds after we have found the least" 
 			+ " number");
 		System.out.println(System.currentTimeMillis() - time);
 		
 	}
 	public static void main (String [] args) {
 		new Main();
 	}
 	void initialSetup() {
 		// Fill array with all sorts of numbers.
 		System.out.print("Number of places in the array to be sorted: ");
 		System.out.println(Integer.MAX_VALUE / DEVIDE_BY);
 		System.out.println("The current time in milliseconds before we start"
 			+ " to fill the array: ");
 		System.out.println(time = System.currentTimeMillis());
 		for (int i = 0; i < Integer.MAX_VALUE / DEVIDE_BY; i++)
 			tall[i] = (int) (Math.random() * Integer.MAX_VALUE);
 		System.out.println("Milliseconds after we have filled the array");
 		System.out.println(System.currentTimeMillis() - time);
 		System.out.println("Finished filling the array, let's move on.");
 	}
 }
