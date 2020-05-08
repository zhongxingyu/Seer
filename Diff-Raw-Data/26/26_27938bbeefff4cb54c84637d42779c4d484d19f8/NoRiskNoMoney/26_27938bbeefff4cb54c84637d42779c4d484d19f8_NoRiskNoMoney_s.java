 import java.util.Random;
 
 public class NoRiskNoMoney{
     private boolean[] field = new boolean[6];
    private boolean diceAllowed = true;
 
     public NoRiskNoMoney(){
         for(boolean b: field)
             b = false;
     }
 
    public void dice(boolean allowed){
         if(allowed){
            int r = new Random().nextInt(7);
 
             if(!field[r])
                 field[r] = true;
             else
                 allowed = false;
         }
 
     }
 
     public void printField(){
         System.out.println("=====================================================================");
         System.out.println("+                         No Risk No Money!");
         System.out.println("=====================================================================\n");
        System.out.println("+---+---+---+");
        for(int i=0; i<field.length; i+=3){
            for(int j=0; j<2; i++){
                System.out.println("| " + field[i] + " | " + field[i++] + " |\n");
            }
         }
        System.out.println("+---+---+---+");
     }
 }
