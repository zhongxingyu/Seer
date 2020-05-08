 public class Main
 {
     private Infested instance;
     
     public Main()
     {
         instance = new Infested();       
     }
     
    public void getInstance()
     {
         return instance;
     }
 
     public static void main(String[] args)
     {
         new Main();
     }
 }
