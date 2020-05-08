package temp;

import java.io.File;

public class Test {

	
	public static void main(String[] args) {
		File fred = new File("d:/temp");
		System.out.println(fred.getParent());
		System.out.println(fred.getName());
	}
}
