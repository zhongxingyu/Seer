import org.investovator.core.data.types.HistoryOrderData;
 import org.investovator.dataPlayBackEngine.DataPlayer;
 
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public class Tester {
     static Observer observer=new Obs();
 
     public Tester(Observer observer) {
 //        this.observer = new Obs();
     }
 
     public static void main(String[] args) {
         DataPlayer player=new DataPlayer("GOOG",observer);
         player.runPlayback(1);
     }
 
     private static class Obs implements Observer{
 
         @Override
         public void update(Observable o, Object arg) {
 
             HistoryOrderData d= (HistoryOrderData)arg;
 
             System.out.println(d.getDate()+"-"+d.getStockId());
         }
     }
 
 }
