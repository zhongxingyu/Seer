 import java.io.File;
 import java.util.LinkedList;
 
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
		LinkedListString hello = new LinkedListString("");
 		System.out.println(hello);
//		System.out.println(hello.indexOf("l"));
//		System.out.println(hello.indexOf("l", 3));
//		System.out.println(hello.subString(1));
//		System.out.println(hello.subString(2, 3));
//		hello.insert("i", 0);
//		System.out.println(hello);
//		System.out.println(hello.size());
 		
 		File f = new File("src/testfile.txt");
 		LinkedListString hello1 = new LinkedListString(f);
 		System.out.println(hello1);
 		System.out.println(hello1.indexOf("l"));
 		System.out.println(hello1.indexOf("l", 3));
 		System.out.println(hello1.subString(1));
 		System.out.println(hello1.subString(2, 3));
 		System.out.println(hello1.size());
 		hello1.insert("i", 9);
 		System.out.println(hello1);
 
 		Number n1 = new Number(14);
 		Number n2 = new Number(8);
 		Number n3 = Number.addNumber(n1, n2);
 		System.out.println(n1);
 		System.out.println(n2);
 		System.out.println(n3);
 		
 		n3 = Number.subtractNumber(n1, n2);
 		System.out.println(n1);
 		System.out.println(n2);
 		System.out.println(n3);
 		
 	}
 
}

/*TODO:
	ask about design of LinkedListtring, specifically subString and search methods
	Number class
	main for testing
*/
