 package commandpattern.receivers;
 
 /**
  * This abstract class is used to define the methods that
  * will be used for each of our devices. These devices
 * are the "Receivers" in the commandpattern.Commandommands.Command Pattern.
  */
 public abstract class Device {
 
   protected int volume = 0;
 
   public abstract void on();
 
   public abstract void off();
 
   public abstract void volumeUp();
 
   public abstract void volumeDown();
 
 }
