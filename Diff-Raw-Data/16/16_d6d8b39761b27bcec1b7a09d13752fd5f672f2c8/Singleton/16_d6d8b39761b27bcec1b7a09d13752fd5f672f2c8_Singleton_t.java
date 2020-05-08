 public class Singleton
 {
   private volatile static Singleton uniqueIntance;
  private Singleton()
  {
    
  }
  
   public static Singleton getInstance()
   {
     if (uniqueIntance == null)
     {
       synchronized(Singleton.class)
       {
         if (uniqueIntance == null)
         {
           uniqueIntance = new Singleton();          
         }
       }
     }
 
     return uniqueIntance;
     
   }
   
 }
