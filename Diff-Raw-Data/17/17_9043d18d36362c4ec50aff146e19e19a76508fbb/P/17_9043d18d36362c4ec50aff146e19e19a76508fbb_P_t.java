 package minit_simple {
     inst T2 with
        TX => PSuper,
        TY => PChild;
 
     class PSuper adds {
         PSuper() {
             System.out.println("PSuper");
         }
     }
 
     class PChild adds {
         PChild() {
             System.out.println("PChild");
         }
     }
     
     class InstPChild {
         public static void main(String[] args) {
             new PChild();
         }
     }
 
     class InstPSuper {
         public static void main(String[] args) {
             new PSuper();
         }
     }        
 }
