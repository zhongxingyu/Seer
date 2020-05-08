 package Tippspiel;
 
 
 import javax.xml.bind.JAXBException;
 import java.io.IOException;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: Oli
  * Date: 10.05.13
  * Time: 17:28
  * To change this template use File | Settings | File Templates.
  */
 public class Testing {
     public static void main(String[] args) throws JAXBException, IOException {
         TippspielService test = new TippspielService();
         //test.getTippspiel("Oli");
         //test.getErgebnis("0:4");
        //test.deleteTipp("Peter");
         test.newTipp("Troll", "Bayern", "HSV", "7:6");
     }
 }
