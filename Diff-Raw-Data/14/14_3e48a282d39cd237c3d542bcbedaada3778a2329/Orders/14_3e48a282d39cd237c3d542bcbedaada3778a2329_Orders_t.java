 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package logikk;
 
 import DB.Database;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 /**
  *
  * @author Frode
  */
 public class Orders implements Serializable {
     private ArrayList<Order> list = new ArrayList<Order>();
     private Database database = new Database(); 
     
     public Orders(){
         this.list = database.getOrderOverview();
     }
 
     public ArrayList<Order> getList() {
         return list;
     }
     public ArrayList<Order>getDriversList(){ //Sjåførene skal kun ha ordrene som er merket "under way"
         return database.getDriversList();
     }
     public ArrayList<Order> getOrdersMonth(int month, int year){
         ArrayList<Order> Month = new ArrayList<Order>();
         for(int i = 0;i<list.size();i++){
            if(list.get(i).getTimeOfDelivery() !=null){
                    if(month == 13){
                     Month.add(list.get(i));
                 }
                if (list.get(i).getTimeOfDelivery().getMonth() + 1 == month && list.get(i).getTimeOfDelivery().getYear() + 1900 == year) {
                     Month.add(list.get(i));
                 }
             }
          
         }
         return Month;
     }
     public static void main(String[]args){
         Orders orders = new Orders();
         ArrayList<Order>list = orders.getList();
         for(int i = 0;i<list.size();i++){
             System.out.println(list.get(i).getOrderId());
         }
     }  
 }
