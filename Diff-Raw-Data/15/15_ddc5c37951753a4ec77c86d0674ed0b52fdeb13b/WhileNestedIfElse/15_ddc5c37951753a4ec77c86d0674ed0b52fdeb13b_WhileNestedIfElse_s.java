 public class WhileNestedIfElse {
     public WhileNestedIfElse() {}
     public int m() {
         while(false) if (true) return 0;
         while(false) if (true) return 0; else return 1;
 
         while(true){
             return 0;
         }

        do if (true) return 0; else return 1; while(false);
        do if (true) return 0; while(false);

        do {
            if (true) return 0;
            else return 1;
        }while(false);
     }
 }
