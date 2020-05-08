 package elxris.Useless.Objects;
 
 public abstract class Savable implements Runnable{
    private boolean save = false;
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
    public boolean getSave() {
         return save;
     }
    public void setSave(boolean save) {
        this.save = save;
     }
 }
