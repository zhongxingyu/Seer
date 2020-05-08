 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 
 /**
  *
  * @author chappljd.
  *         Created Mar 23, 2013.
  */
 public class TestFilter {
 
 	public static void main(String[] args) throws IOException {
 		FilterApplier command = new FilterApplier("", "2013-03-25_09:51", "FilterTest.txt" );
 		command.execute();
 		
 		//compare files
		BufferedReader testBuffer = new BufferedReader(new FileReader("TestFile.txt"));
 		BufferedReader goalBuffer = new BufferedReader(new FileReader("FilterPass.txt"));
 		
 		String goal = goalBuffer.readLine();
 		String toTest = testBuffer.readLine();
 		while ( goal != null && toTest != null )
 		{
 			if ( !goal.equals(toTest) )
 			{
 				break;
 			}
 			goal = goalBuffer.readLine();
 			toTest = testBuffer.readLine();
 		}
 		
 		if ( goal == null && toTest == null )
 		{
 			System.out.println("PASS: Filter test");
 		}
 		else
 		{
 			System.out.println(String.format( "FAIL: Filter test\n\tgoal: %s\n\tfile: %s", goal, toTest) );
 		}
 		
 		testBuffer.close();
 		goalBuffer.close();
 
 	}
 
 }
