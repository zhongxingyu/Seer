 package balle.bluetooth.messages;
 
 public abstract class AbstractSingleArgMessage extends AbstractMessage {
 
     private final int        arg1;
     private static final int BITS_PER_ARGUMENT = AbstractMessage.BITS_PER_INT
                                                        - AbstractMessage.BITS_FOR_OPCODE;
 
     public AbstractSingleArgMessage(int arg1) throws InvalidArgumentException {
         this.arg1 = arg1;
 
         if (arg1 < 0) {
             throw new InvalidArgumentException("Provided argument " + arg1
                     + " is < 0. Arguments should be unsigned. "
                     + " If you need a negative valued one, consider offsetting it "
                     + " so it is always positive.");
 
         } else if (arg1 > (int) Math.pow(2, AbstractSingleArgMessage.BITS_PER_ARGUMENT) - 1) {
             throw new InvalidArgumentException("Provided argument " + arg1
                     + " exceeds number of bits per argument");
         }
 
     }
 
     protected int hashArguments() {
         // No shifting needed in this case
         return this.arg1;
     }
 
     @Override
     public int hash() throws InvalidOpcodeException {
         return hashOpcode() | hashArguments();
     }
 
     public static int decodeArgumentsFromHash(int hash) {
        return hash & 0x1FFFFFFF;
     }
 
     public int getArgument() {
         return this.arg1;
     }
 
 }
