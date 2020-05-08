 package no.nxc.bank.atm.client;
 
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.HashMap;
 
 @XmlRootElement
 public class AtmClient {
 
     private String name;
     private HashMap<String, Cash> cash = new HashMap<String, Cash>();
 
     public AtmClient(){}
 
     public AtmClient(String name){
         this.name = name;
     }
 
     public void setCash(String name){
         this.cash.put(name, new Cash(100, 30));
     }
 
    public HashMap<String, Cash> getCash(){
         return cash;
     }
 
 }
