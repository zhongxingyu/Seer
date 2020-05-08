 public class CommunicateTest
 {
 
     private static String testString = "This is a test string";
 
     public static void main(String[] args)
     {
         PrintingObject po = new PrintingObject();
         Communicate.rebind("printing", po);
         System.out.println("obj bound on registry");
         PrintingObjectInterface obj = (PrintingObjectInterface)Communicate.lookup("printing");
        obj.printThis(testString, 5);
     }
 }
