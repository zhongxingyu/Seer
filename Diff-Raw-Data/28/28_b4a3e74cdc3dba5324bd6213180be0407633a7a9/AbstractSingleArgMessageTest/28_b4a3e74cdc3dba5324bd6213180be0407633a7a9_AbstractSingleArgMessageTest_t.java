 package balle.bluetooth.messages;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 
 import org.junit.Test;
 
 class AbstractSingleArgMessageConcrete extends AbstractSingleArgMessage {
 
     public AbstractSingleArgMessageConcrete(int arg1) throws InvalidArgumentException {
         super(arg1);
         // TODO Auto-generated constructor stub
     }
 
     @Override
     public int getOpcode() {
         // TODO Auto-generated method stub
         return 0;
     }
 
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

 }
 
 public class AbstractSingleArgMessageTest {
 
     /**
      * Given a valid argument and a valid opcode, AbstractsignleArgMessage
      * should be able to hash both into a single integer
      * 
      * @throws InvalidArgumentException
      * @throws InvalidOpcodeException
      */
     @Test
     public void testHash() throws InvalidArgumentException, InvalidOpcodeException {
         AbstractSingleArgMessage message1 = spy(new AbstractSingleArgMessageConcrete(12));
         when(message1.getOpcode()).thenReturn(3);
 
         assertEquals(0xC000000C, message1.hash());
     }
 
     /**
      * Given a negative valued argument the constructor should raise
      * InvalidArgumentException. Arguments with negative numbers should be
      * implemented with offset shifting to ensure they're positive.
      */
     @Test(expected = InvalidArgumentException.class)
     public void test_constructor_fails_when_negative_argument_is_passed()
             throws InvalidArgumentException {
         AbstractSingleArgMessage message = new AbstractSingleArgMessageConcrete(-1);
     }
 
     /**
      * Given a very high valued argument the constructor should raise
      * InvalidArgumentException if it does not fit into the bits allocated.
      */
     @Test(expected = InvalidArgumentException.class)
     public void test_constructor_fails_when_high_valued_argument_is_passed()
             throws InvalidArgumentException {
         // Currently it should have 29 bits allowing to store integers as big as
         // 2^30-1
         AbstractSingleArgMessage message = new AbstractSingleArgMessageConcrete((int) Math.pow(2,
                 30));
     }
 
     /**
      * Given a valid argument, AbstractSingleArgMessage should hash the argument
      * to a correct place.
      * 
      * @throws InvalidArgumentException
      */
     @Test
     public void testHashArguments() throws InvalidArgumentException {
         AbstractSingleArgMessage message1 = new AbstractSingleArgMessageConcrete(1234);
         assertEquals(1234, message1.hashArguments());
 
         AbstractSingleArgMessage message2 = new AbstractSingleArgMessageConcrete(7);
         assertEquals(7, message2.hashArguments());
     }
 
     /**
      * Given a valid hash, AbstractSingleArgMessage should be able to decode the
      * argument from it and not be fooled by the opcode bits.
      */
     @Test
     public void testDecodeArgumentsFromHash() {
         assertEquals(0, AbstractSingleArgMessage.decodeArgumentsFromHash(0xC0000000));
         assertEquals(536871136, AbstractSingleArgMessage.decodeArgumentsFromHash(0xA00000E0));
 
     }
 
 }
