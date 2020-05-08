 import junit.framework.Assert;
 import junit.framework.TestCase;
 import main.TicTacToe;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.PrintStream;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fannar
  * Date: 11/23/12
  * Time: 16:20
  * To change this template use File | Settings | File Templates.
  */
 public class PrintTableTest extends TestCase
 {
     TicTacToe capture;
 
     protected void setUp() throws Exception
     {
         super.setUp();
         capture = new TicTacToe();
     }
 
     public final void testPrintTableTest()
     {
         PrintStream originalOut = System.out;
         OutputStream os = new ByteArrayOutputStream();
         PrintStream ps = new PrintStream(os);
         System.setOut(ps);
 
         String separator = System.getProperty("line.separator");
         capture.printTable();
 
         assertEquals("["+ capture.arr[0][0] + "]"+ "" + "["+ capture.arr[0][1] + "]"+"" + "["+capture.arr[0][2] +"]"+ "\n" +
                 "["+ capture.arr[1][0] + "]"+ "" + "["+ capture.arr[1][1] + "]"+"" + "["+capture.arr[1][2] +"]"+ "\n" +
                 "["+ capture.arr[2][0] + "]"+ "" + "["+ capture.arr[2][1] + "]"+"" + "["+capture.arr[2][2] +"]" +separator, os.toString());
 
        System.setOut(originalOut);
 
     }
 }
