 package elxris.Useless.Objects;
 
 public abstract class Savable implements Runnable{
    private static boolean save = false;
     public void save(){
         if(!save){
             setSave(true);
             new Thread(this).start();
         }
     }
     @Override
     public void run() {
         try {
             Thread.sleep(60*1000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         setSave(false);
     }
    public static boolean getSave() {
         return save;
     }
    public static void setSave(boolean save) {
        Savable.save = save;
     }
 }
