 import org.javascool.Pml;
 
 public class PmlTst {
   public static void main(String argv[]) {
     tst("{test coco=titi toto}", "pml");
     tst("{test coco=titi toto=} ", "pml");
     tst("{hello you=\"Philou & Guillaume\" me=vthierry 1 2 3 hi=  } ", "pml");
     tst("<root><toto>ABCD</toto></root>", "pml");
     tst("<root><toto>ABCD</toto><toto>ADER</toto></root>", "xml");
   }
  public static void tst(String string, String format) {
     System.out.println("in>"+string);
     Pml pml = new Pml().reset(string, format);
     System.out.println("out>"+pml);
   }
 }
