 public class Main
 {
     private Infested instance;
     
     public Main()
     {
         instance = new Infested();       
     }
     
    public Infested getInstance()
     {
         return instance;
     }
 
     public static void main(String[] args)
     {
         new Main();
     }
 }
