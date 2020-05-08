 
 package scalr.misc;
 
 public class DegreeGenerator
 {
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		System.out.println("public enum Degree {");
 		String[] letter = {"C", "D", "E", "F", "G", "A", "B"};
 		String[] mid = {"s", "b"};
 		int count = 0;
		for (int i = 0; i <= 10; i++)
 			for (int j = 0; j < letter.length; j++) {
 				if (count == 127)
 					break;
 				if (j == 0) {
 					System.out.print(letter[j] + i + "(" + (count++) + "), ");
 					System.out.print(letter[j] + mid[0] + i + "(" + (count) + "), ");
 				}
 				else if (j == 1) {
 					System.out.print(letter[j] + mid[1] + i + "(" + (count++) + "), ");
 					System.out.print(letter[j] + i + "(" + (count++) + "), ");
 					System.out.print(letter[j] + mid[0] + i + "(" + (count) + "), ");
 				}
 				else if (j == 2) {
 					System.out.print(letter[j] + mid[1] + i + "(" + (count++) + "), ");
 					System.out.print(letter[j] + i + "(" + (count++) + "), ");
 				}
 				else if (j == 3) {
 					System.out.print(letter[j] + i + "(" + (count++) + "), ");
 					System.out.print(letter[j] + mid[0] + i + "(" + (count) + "), ");
 				}
 				else if (j == 4) {
 					System.out.print(letter[j] + mid[1] + i + "(" + (count++) + "), ");
 					System.out.print(letter[j] + i + "(" + (count++) + "), ");
 					System.out.print(letter[j] + mid[0] + i + "(" + (count) + "), ");
 				}
 				else if (j == 5) {
 					System.out.print(letter[j] + mid[1] + i + "(" + (count++) + "), ");
 					System.out.print(letter[j] + i + "(" + (count++) + "), ");
 					System.out.print(letter[j] + mid[0] + i + "(" + (count) + "), ");
 				}
 				else if (j == 6) {
 					System.out.print(letter[j] + mid[1] + i + "(" + (count++) + "), ");
 					System.out.print(letter[j] + i + "(" + (count++) + "), ");
 				}
 				else
 					System.out.print(letter[j] + i + "(" + (count++) + "), ");
 			}
 		System.out.println("}");
 	}
 	
 }
