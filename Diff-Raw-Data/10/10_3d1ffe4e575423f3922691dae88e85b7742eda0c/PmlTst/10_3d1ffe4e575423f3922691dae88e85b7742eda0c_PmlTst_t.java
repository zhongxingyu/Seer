 import org.javascool.Pml;
 
/** Read/write test of Pml data structures. */
 public class PmlTst {
  /** Runs the test.
     * * Usage: <tt>java org.javascool.test.PmlTst</tt>
     * @param argv Not used.
     */
   public static void main(String argv[]) {
     tst("{test coco=titi toto}", "pml");
     tst("{test coco=titi toto=} ", "pml");
     tst("{hello you=\"Philou & Guillaume\" me=vthierry 1 2 3 hi=  } ", "pml");
     tst("<root><toto>ABCD</toto></root>", "pml");
     tst("<root><toto>ABCD</toto><toto>ADER</toto></root>", "xml");
   }
  // Read and then write in normalized format
  private static void tst(String string, String format) {
     System.out.println("in>"+string);
     Pml pml = new Pml().reset(string, format);
     System.out.println("out>"+pml);
   }
 }
