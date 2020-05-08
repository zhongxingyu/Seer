class OperatorsTest
 {
     public static void main(String[] s) 
     {
 	System.out.println(1);
     }
 }
 
 
 class A { 
     int z;    
     public int f(int x, int y, boolean b1, boolean b2) 
     {
 	x = 1-2-3;
 	x = 1-(2-3);
 	x = 1*2+3;
 	x = 1*(2+3);
 	x = 1+2*3;
 	y = new A().f(1,2,true,false);	
 	b2 = 1 < 2 && !(x < y) && !b2 && (1 < x);
 	b2 = 1+2<3+4 && 5-6<7-8;	
 	return x*x+y;	
     }
     
 }
 
 
