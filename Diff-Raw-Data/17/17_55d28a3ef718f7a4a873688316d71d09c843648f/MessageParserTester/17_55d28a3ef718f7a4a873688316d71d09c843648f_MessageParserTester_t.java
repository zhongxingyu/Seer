 import bitter.*;
 import java.util.*;
 
 public class MessageParserTester {
 
     public static void main(String[] args) {
         String msg1 = "@bobby@tessie @griswold.  I hate writing tests " +
                 "#latenightcoding#java #topic!";
         String msg2 = "@philipmorris@cigshops @tobacconist?  Why aren't " +
                 "English Ovals more available? #englishovals#tasty #end~";
         String msg3 = "@me test test test";
         String msg4 = "#this is crazy @me";
         String msg5 = "no topics no addressees";
        String msg6 = "Redundant topics and users @bob@bob@mary #repeat" +
                " #repeat #petrol";
        String[] msgs = { msg1, msg2, msg3, msg4, msg5, msg6 };
 
         LinkedList<String> extracts = new LinkedList<String>();
         for (String str : msgs) {
            System.out.printf(">>%s\n", str);
             extracts = MessageParser.extractTopics(str);
             System.out.print("Topics: ");
             printList(extracts);
             extracts = MessageParser.extractAddressees(str);
             System.out.print("Addressees: ");
             printList(extracts);
         }
 
     }
 
     private static void printList(LinkedList<String> list) {
         for (String str : list) {
             System.out.printf("%s, ", str);
         }
         System.out.print("\n");
     }
 
 }
