 public class VarargsTest {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		passMeVarArgs(1, 2, 3, 4, 5);
 
 		System.out.println("\r\n+++++++++++++++++++");
 
 		passMeVarArgs();
 
		System.out.println("+++++++++++++++++++");
 
 		passMeVarArgs(1, 3, 5);
 		
 		System.out.println("\r\n+++++++++++++++++++");
 
 		try {
 			passMeVarArgs(null);
 		} catch (IllegalArgumentException e) {
 			System.out.println("[null]");
 		}
 	}
 
 	private static void passMeVarArgs(int... args) {
 		if(args == null){
 			throw new IllegalArgumentException("Argument 'args' must not be null");
 		}
 		
 		if(args.length == 0){
 			System.out.println("[empty]");
 			return;
 		}
 		
 		for (int i : args) {
 			System.out.print(i);
 		}
 	}
 
 }
