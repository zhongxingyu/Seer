 public class ForNestedIfElse {
     public ForNestedIfElse() {}
     public int m() {
         for(;;) if (true) return 0;
         for(;;) if (true) return 0; else return 1;
 
        for(int i = 0; i < 10; i = i + 1){
             if (true) return 0; else return 1;
         }
     }
 }
