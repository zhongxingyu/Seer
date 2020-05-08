 import java.util.Random;
 
 public class NoRiskNoMoney{
     private boolean[] field = new boolean[6];
    private boolean allowed= true;
 
     public NoRiskNoMoney(){
         for(boolean b: field)
             b = false;
     }
 
    public void dice(){
         if(allowed){
            int r = new Random().nextInt(6);
 
             if(!field[r])
                 field[r] = true;
             else
                 allowed = false;
         }
 
     }
 
     public void printField(){
        System.out.println("\f");
         System.out.println("=====================================================================");
         System.out.println("+                         No Risk No Money!");
         System.out.println("=====================================================================\n");
        System.out.println("+---+---+");
        for(int i=0; i<5;){
            int j=i;
            System.out.println("| " + ((!field[j] ? ++j : "X") + " | " + ((!field[j]) ?  ++j: "X") + " |"));
            i+=2;
         }
        System.out.println("+---+---+");
        if(!allowed)
            System.out.println("Game Over!\n Please restart...");
     }
 }
