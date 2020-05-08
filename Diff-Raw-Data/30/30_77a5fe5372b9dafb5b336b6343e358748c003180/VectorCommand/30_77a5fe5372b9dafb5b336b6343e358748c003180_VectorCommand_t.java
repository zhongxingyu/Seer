 package net.ant.rc.serial;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ant
  * Date: 05.02.13
  * Time: 0:09
  * To change this template use File | Settings | File Templates.
  */
 public class VectorCommand extends Command {
     final public int x;
     final public int y;
 
     public VectorCommand(String commandType, int x, int y, long timeMillis) {
         super(commandType, timeMillis);
         this.x = x;
         this.y = y;
     }
 
    public static VectorCommand STOP(long timeMillis) {
        return new VectorCommand("Digital", 0, 0, timeMillis);
     }
 
     @Override
     public boolean equals(Object o){
         if (!(o instanceof VectorCommand))return false;
         VectorCommand v = (VectorCommand)o;
         return (v.x == this.x && v.y == this.y);
     }
 
 }
