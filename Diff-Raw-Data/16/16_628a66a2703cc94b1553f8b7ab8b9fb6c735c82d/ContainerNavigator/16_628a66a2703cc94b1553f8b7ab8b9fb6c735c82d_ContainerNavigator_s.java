 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcatarmy.utils;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import longcatarmy.src.AuctionCatalogue;
 import longcatarmy.src.AuctionObject;
 
 /**
  *
 * @author lissal
  * 
  * 
  * OBS! Tveksam till om denna funkar som den ska, men borde kunna ses som ett vettigt utkast.
  * Finns en risk att vi måste ändra en del i AuctionCatalogue.
  */
 public class ContainerNavigator {
     private int first;  
     private int nItems;  
     private AuctionCatalogue cat;
     
     public ContainerNavigator(int first, int nItems, AuctionCatalogue cat) {
         this.first = first;
         this.nItems = nItems;
         this.cat = cat;
     }
     
     public List<AuctionObject> next() {
         int count = cat.auctionList.size();
         first = (first + nItems < count) ? first + nItems : first;
         int n = adjustNItems(count);
         
         return getRange(); //funkar detta? känns som att vi behöver modifiera getRange så den tar in nåt, men vad? hur? var?
     }
     
     public List<AuctionObject> previous() {
         int count = cat.auctionList.size();
         first = (first - nItems > 0) ? first - nItems : 0;
         int n = adjustNItems(count);
         
         return getRange(); //funkar detta?
     }
     
     private int adjustNItems(int count) {
         return (first + nItems > count) ? count - first : nItems;
     }
     
     //kan behöva modifieras med inparametrar, tror inte next och prev funkar annars
     public List<AuctionObject> getRange() {
         int count = cat.auctionList.size(); 
         int n = adjustNItems(count);
         Iterator it = cat.auctionList.entrySet().iterator();
         List<AuctionObject> xs = new ArrayList<AuctionObject>();
         for(int i = 0; i < n; i++) {
             while(it.hasNext()) {
                 xs.add(cat.auctionList.get(i));
             }
         }
         //returnerar rätt range??
         return xs;
     }
     
     public int getFst() {
         return first;
     }
 
     public void setFst(int first) {
         this.first = first;
     }
     
     public int getnItems() {
         return nItems;
     }
 
     public void setnItems(int nItems) {
         this.nItems = nItems;
     }
 }
