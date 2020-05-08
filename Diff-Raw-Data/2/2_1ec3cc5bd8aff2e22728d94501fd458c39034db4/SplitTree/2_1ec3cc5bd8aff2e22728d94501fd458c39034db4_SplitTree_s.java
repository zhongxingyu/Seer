 package ml;
 
 // this seems to work at least with the test tree: "((a:1,b:1):0.5,(d:1,e:1):0.5,c:1);"
 
 import java.io.File;
 
 public class SplitTree {
 	public static void main( String args[] ) {
 		
 		
 		File inFile = new File(args[0]);
 		
 		LN n = TreeParser.parse(inFile);
 		
 		LN[] nl = LN.getAsList(n);
 		String[] rl = { "b", "d" };
 		
 		for( String r : rl ) {
 			for( LN cn : nl ) {
 				if( cn.data.isTip(r)) {
 					LN cnt = LN.getTowardsTree(cn);
 					
 					n = LN.removeBranch(cnt.back)[0];
					
 				}
 			
 			}
 		
 		}
 		
 		TreePrinter.printRaw(n, System.out);
 		
 	}
 }
