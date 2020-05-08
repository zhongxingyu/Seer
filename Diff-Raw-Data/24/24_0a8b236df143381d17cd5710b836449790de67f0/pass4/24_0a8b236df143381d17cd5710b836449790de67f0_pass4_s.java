 class A
 {
 	A a;
 	static int i;
 	public static void main (String[] args)
 	{
 		A a = new A();
 		if (a.test(null))
 			System.out.println(4);
 	
 		A[] b = new A[2];
 		b[0] = a.retFunc();
 
 	}
 	public boolean test (A a)
 	{
 
 		boolean flag = false;
 
 		if (a == null)
 			flag = true;
 	
 		return (flag);		
 	}
 
 	public A retFunc ()
 	{
 		return null;
 	}
 
 }
