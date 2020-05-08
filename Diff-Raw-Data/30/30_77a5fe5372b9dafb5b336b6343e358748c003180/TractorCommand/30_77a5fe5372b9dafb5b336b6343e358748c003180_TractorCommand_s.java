 package net.ant.rc.serial;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ant
  * Date: 17.02.13
  * Time: 6:27
  * To change this template use File | Settings | File Templates.
  */
 public class TractorCommand extends Command {
     final public int left;
     final public int right;
 
     public TractorCommand(String commandType, int left, int right, long timeMillis) {
         super(commandType, timeMillis);
         this.left = left;
         this.right = right;
     }
 
    public static TractorCommand STOP() {
        return new TractorCommand("Digital", 0, 0, 0);
     }
 
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof TractorCommand))return false;
         TractorCommand c = (TractorCommand)o;
         return (c.left == this.left && c.right == this.right);
     }
 }
