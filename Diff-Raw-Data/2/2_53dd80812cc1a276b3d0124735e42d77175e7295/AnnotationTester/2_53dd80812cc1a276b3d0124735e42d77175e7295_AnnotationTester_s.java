 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 /**
  * prints the annotations for all classes and their methods
  * 
  * @author Thomas
  * 
  */
 @Creator(name = "Thomas", lastUpdate = "08.12.2012")
 public class AnnotationTester implements Tester{
 	
 	@Override
 	@Creator(name = "Thomas", lastUpdate = "08.12.2012")
 	public void runTests() {
 		System.out.println("=== " + Bauernhof.class.getName() + " ===");
 		displayAnnotations(Bauernhof.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(Bauernhof.class.getDeclaredMethods());
 		
 		
 		
 		System.out.println("\n\n=== " + Traktor.class.getName() + " ===");
 		displayAnnotations(Traktor.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(Traktor.class.getDeclaredMethods());
 		
 		System.out.println("\n\n=== " + BiogasTraktor.class.getName() + " ===");
 		displayAnnotations(BiogasTraktor.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(BiogasTraktor.class.getDeclaredMethods());
 		
 		System.out.println("\n\n=== " + DieselTraktor.class.getName() + " ===");
 		displayAnnotations(DieselTraktor.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(DieselTraktor.class.getDeclaredMethods());
 		
 		
 		
 		System.out.println("\n\n=== " + TraktorGeraet.class.getName() + " ===");
 		displayAnnotations(TraktorGeraet.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(TraktorGeraet.class.getDeclaredMethods());
 		
 		System.out.println("\n\n=== " + Drillmaschine.class.getName() + " ===");
 		displayAnnotations(Drillmaschine.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(Drillmaschine.class.getDeclaredMethods());
 		
 		System.out.println("\n\n=== " + Duengerstreuer.class.getName() + " ===");
 		displayAnnotations(Duengerstreuer.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(Duengerstreuer.class.getDeclaredMethods());
 		
 		
 		
 		System.out.println("\n\n=== " + MyIterator.class.getName() + " ===");
 		displayAnnotations(MyIterator.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(MyIterator.class.getDeclaredMethods());
 		
 		System.out.println("\n\n=== " + Liste.class.getName() + " ===");
 		displayAnnotations(Liste.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(Liste.class.getDeclaredMethods());
 		
 		System.out.println("\n\n=== " + Liste.class.getDeclaredClasses()[0].getName() + " ===");
 		displayAnnotations(Liste.class.getDeclaredClasses()[0].getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(Liste.class.getDeclaredClasses()[0].getDeclaredMethods()); // nested iterator
 		
 		
 		
 		System.out.println("\n\n=== " + Tester.class.getName() + " ===");
 		displayAnnotations(Tester.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(Tester.class.getDeclaredMethods());
 		
 		System.out.println("\n\n=== " + AnnotationTester.class.getName() + " ===");
 		displayAnnotations(AnnotationTester.class.getAnnotations());
 		System.out.println();
 		displayMethodAnnotations(AnnotationTester.class.getDeclaredMethods());
 
 		
 		
 	}
 
 	/**
 	 * prints the annotations in a pretty manner
 	 * 
 	 * @param a
 	 *            annotation-array (ALLOWED!)
 	 */
 	@Creator(name = "Thomas", lastUpdate = "08.12.2012")
 	private void displayAnnotations(Annotation[] a) {
 		if (a != null) {
 			for (int i = 0; i < a.length; i++) {
 				System.out.println(a[i]);
 			}
 		}
 	}
 
 	/**
 	 * prints the annotations of all methods passed as arg
 	 * 
 	 * @param m
 	 *            method-array (ALLOWED!)
 	 */
 	@Creator(name = "Thomas", lastUpdate = "08.12.2012")
 	private void displayMethodAnnotations(Method[] m) {
 		if (m != null) {
 			for (int i = 0; i < m.length; i++) {
 				if (m[i].getAnnotations().length != 0) {
 					System.out.print("Method: " + m[i].getName() + "(");
 					for (int k = 0; k < m[i].getParameterTypes().length; k++) {
 						System.out.print(m[i].getParameterTypes()[k].getName());
 						if (k != m[i].getParameterTypes().length - 1) {
							System.out.print(" ");
 						}
 					}
 					System.out.print("):\n");
 					displayAnnotations(m[i].getAnnotations());
 					System.out.println();
 				}
 			}
 		}
 	}
 }
