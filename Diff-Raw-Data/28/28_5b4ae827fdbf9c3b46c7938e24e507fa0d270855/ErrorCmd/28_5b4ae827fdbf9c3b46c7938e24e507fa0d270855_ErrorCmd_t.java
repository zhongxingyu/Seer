 package risk.protocol.command;
 
 import risk.protocol.CommandVisitor;
 
 public class ErrorCmd extends Command {
     private static final long serialVersionUID = Command.serialVersionUID;
     
     public static final int ILLEGAL_ARGUMENT = 1;
     public static final int INVALID_PHASE = 2;
    public static final int INVALID_ATTACK_PHASE = 3;
    public static final int NAME_ALREADY_USED = 4;
 
     private int errorCode;
     
     public ErrorCmd(int errorCode) {
         super();
         this.errorCode = errorCode;
     }
 
     public int getErrorCode() {
         return errorCode;
     }
 
     @Override
     public void accept(CommandVisitor cv) {
         cv.visit(this);
     }
 
 }
