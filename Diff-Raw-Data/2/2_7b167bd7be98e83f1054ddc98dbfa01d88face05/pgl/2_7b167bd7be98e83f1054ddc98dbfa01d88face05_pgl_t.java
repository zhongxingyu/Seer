 /*
 * Author: John Bernier
 * Created: 1/2013
 * pgl.java
 *		this is the main class for using the pgl language interpreter as a stand
 *		alone application
 */
 public class pgl
 {
 	public static void main(String[] args)
 	{
 		if(args.length <= 0)
 		{
 			System.out.println("invalid arguments");
 			return;
 		}
 		if(!args[0].endsWith(".pgl"))
 		{
 			System.out.println("wrong file type: .pgl file expected");	
 			return;
 		}
 		
		parse p = new parse(args[0],null);
 	}
 }
