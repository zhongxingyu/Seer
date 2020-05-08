 package p3;
 
 import fr.irisa.diversify.annotations.MemoryLeak;
 import fr.irisa.diversify.annotations.Perforable;
 import fr.irisa.diversify.annotations.WhileTrueThread;
 
 public class A {
 	public static void main(String[] args) {
 		new A().foo();
         new A().foo1();
 	}
 
 	@Perforable(step = 5)
	@MemoryLeak
	@WhileTrueThread
 	public void foo() {
 		int j = 0;
 		for (int i = 0; i < 100; i++) {
 			j++;
 		}
 		System.err.println(j);
 
 	}
 
 	@MemoryLeak
 	@WhileTrueThread
 	public void foo1() {
 		int j = 0;
 		while (true) {
 			for (int i = 0; i < 100; i++) {
 				j++;
 			}
 			System.err.println(j);
 		}
 
 	}
 
 	
 }
