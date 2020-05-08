 package fixbugs.test;
 
 public class Simple {
 
 	public static void main(String[] args) {
 		int x = 2;
 		int y = x + 3;
		for(int i = 0;i<10;i++) {
			System.out.println(i);
		}
        if(x < y) {
             System.out.println("Foo");
         } else {
             System.out.println("bar");
        }
 	}
 	
 }
