 
 /**
  *
  * @author chappljd.
  *         Created Mar 23, 2013.
  */
public class vcfTester {
 
 	public static void main(String[] args) {
 		FilterApplier command = new FilterApplier("", "2013-03-25_09:51", "filteredData.txt" );
 		System.out.println( command.execute() );
 	}
 
 }
