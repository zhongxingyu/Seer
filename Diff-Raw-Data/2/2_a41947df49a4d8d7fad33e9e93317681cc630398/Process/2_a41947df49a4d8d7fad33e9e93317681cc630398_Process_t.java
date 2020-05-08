 /* Process Class
  * 
  */
 
 /**
  *
  * @author amabeli
  */
 public class Process {
     
     int allocation, max, need;
     
     public Process(int allocation, int max, int need){
         this.allocation = allocation;
         this.max = max;
         this.need = need;
     }
     
     public String addedZeros(int num){
         String temp = ""+num;
         if(num<10){
             temp = "00"+num;
         }
         else if(num<100){
             temp = "0"+num;
         }
             return temp;
         }
     
     public String toString(){
        return "     "+addedZeros(allocation)+"           "+addedZeros(max)+"      "+addedZeros(need)+"\n";
     }
     
 }
