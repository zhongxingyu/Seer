 /**
 * Created by IntelliJ IDEA.
 * User: Gifflen
 * Date: 10/18/11
 * Time: 10:33 PM
 * To change this template use File | Settings | File Templates.
 */
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
import LinkedBag;
 
 public class Driver {
 
private static LinkedBag get_Input(int count, String type){
    LinkedBag lb = new LinkedBag();
     String inpString;
   InputStreamReader inp = new InputStreamReader(System.in);
     BufferedReader in = new BufferedReader(inp);
     while (lb.size()<count){
        System.out.println("Input at least " + count + " "+type+"s delimited by spaces");
 
         try {
             inpString = in.readLine();
             String[] split = inpString.split(" ");
             for (String word : split){
                 lb.add(word);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
     return lb;
 }
 
 
 public static void main(String[] args){
     int nounCount = 10;
     int verbCount = 10;
    LinkedBag lbNouns = get_Input(nounCount,"Noun");
    LinkedBag lbVerbs = get_Input(verbCount,"Verb");
 
     for (int i =0; i<10;i++){
         System.out.println("The " + lbNouns.grab() + " " + lbVerbs.grab() +"s the " + lbNouns.grab() );
     }
 
 }
 }
