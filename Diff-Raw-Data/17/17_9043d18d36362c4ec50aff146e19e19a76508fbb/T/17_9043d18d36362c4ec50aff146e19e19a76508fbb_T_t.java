 template T1 {
 
     class TA {
         TA(){
             System.out.println("TA");
         }
     }
 
     class TB extends TA {
         TB(){
             System.out.println("TB");
         }
     }
     
 }
 
 template T2 {
     inst T1 with
         TA => TX,
        TB => TY;
     
     class TX adds {
         TX(){
             System.out.println("TX");            
         }
     }
 
     class TY adds {
         TY(){
             System.out.println("TY");
         }
     }
 }
 
