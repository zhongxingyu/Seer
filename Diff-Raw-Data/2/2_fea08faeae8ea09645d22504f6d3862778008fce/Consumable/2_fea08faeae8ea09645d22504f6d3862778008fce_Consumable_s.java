 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package antquest.items;
 
 /**
  *
  * @author Kevin
  */
 public abstract class Consumable extends Item{
    protected boolean destroyme;
            
    public abstract void onUse(Entity user, Entity target);
    public boolean isDestroyable()
    {
        return destroyme;
    }
 }
