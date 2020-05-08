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

 
 public class Driver {
 
private static LinkedBag<String> get_Input(int count, String type){
    LinkedBag<String> lb = new LinkedBag<String>();
     String inpString;
    InputStreamReader inp = new InputStreamReader(System.in);
     BufferedReader in = new BufferedReader(inp);
     while (lb.size()<count){
        System.out.println("Input at least " + (count-lb.size()) + " "+type+" delimited by spaces");
 
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
    LinkedBag<String> lbNouns = get_Input(nounCount, "Noun");
    LinkedBag<String> lbVerbs = get_Input(verbCount, "Verb");
 
     for (int i =0; i<10;i++){
         System.out.println("The " + lbNouns.grab() + " " + lbVerbs.grab() +"s the " + lbNouns.grab() );
     }
 
 }
 }
