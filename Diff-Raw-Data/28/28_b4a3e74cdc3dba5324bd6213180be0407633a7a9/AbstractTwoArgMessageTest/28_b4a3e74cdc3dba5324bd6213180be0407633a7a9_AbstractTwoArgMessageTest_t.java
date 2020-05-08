 package balle.bluetooth.messages;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 
 import org.junit.Test;
 
 class AbstractTwoArgMessageConcrete extends AbstractTwoArgMessage {
 
     public AbstractTwoArgMessageConcrete(int arg1, int arg2) throws InvalidArgumentException {
         super(arg1, arg2);
     }
 
     @Override
     public int getOpcode() {
         // this will be stubbed by Mockito
         return 0;
     }
 
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

 }
 
 public class AbstractTwoArgMessageTest {
 
     /**
      * Given a message with valid opcode and valid arguments,
      * AbstractTwoArgMessage should hash both the opcode and arguments
      * appropriatelly.
      * 
      * @throws InvalidArgumentException
      * @throws InvalidOpcodeException
      */
     @Test
     public void testHash() throws InvalidArgumentException, InvalidOpcodeException {
         AbstractTwoArgMessage message1 = spy(new AbstractTwoArgMessageConcrete(0, 32767));
         when(message1.getOpcode()).thenReturn(3);
         assertEquals(0xC0007FFF, message1.hash());
 
         AbstractTwoArgMessage message2 = spy(new AbstractTwoArgMessageConcrete(5, 6));
         when(message2.getOpcode()).thenReturn(1);
         assertEquals(0x40028006, message2.hash());
     }
 
     /**
      * Given two correct arguments, the AbstractTwoArgMessage should process
      * them in constructor and store in a correct place so .getArgument(index)
      * will return the elements correctly
      * 
      * @throws InvalidArgumentException
      */
     @Test
     public void testAbstractTwoArgMessageConstructor() throws InvalidArgumentException {
         AbstractTwoArgMessageConcrete message = new AbstractTwoArgMessageConcrete(777, 222);
         assertEquals(777, message.getArgument(0));
         assertEquals(222, message.getArgument(1));
     }
 
     /**
      * Given that the first elements value is too high to store in specified
      * number of bits, AbstractTwoArgMessage should throw an
      * InvalidArgumentException
      * 
      * @throws InvalidArgumentException
      *             the invalid argument exception
      */
     @Test(expected = InvalidArgumentException.class)
     public void testAbstractTwoArgMessageConstructorAgainstHugeFirstValue()
             throws InvalidArgumentException {
         AbstractTwoArgMessageConcrete message = new AbstractTwoArgMessageConcrete(32768, 5);
     }
 
     /**
      * Given that the first elements value is too high to store in specified
      * number of bits, AbstractTwoArgMessage should throw an
      * InvalidArgumentException
      * 
      * @throws InvalidArgumentException
      *             the invalid argument exception
      */
     @Test(expected = InvalidArgumentException.class)
     public void testAbstractTwoArgMessageConstructorAgainstHugeSecondValue()
             throws InvalidArgumentException {
         AbstractTwoArgMessageConcrete message = new AbstractTwoArgMessageConcrete(5, 32768);
     }
 
     /**
      * Given that the first elements value is negative, AbstractTwoArgMessage
      * should throw an InvalidArgumentException
      * 
      * @throws InvalidArgumentException
      *             the invalid argument exception
      */
     @Test(expected = InvalidArgumentException.class)
     public void testAbstractTwoArgMessageConstructorAgainstNegativeFirstValue()
             throws InvalidArgumentException {
         AbstractTwoArgMessageConcrete message = new AbstractTwoArgMessageConcrete(-1, 5);
     }
 
     /**
      * Given that the first elements value is negative, AbstractTwoArgMessage
      * should throw an InvalidArgumentException
      * 
      * @throws InvalidArgumentException
      *             the invalid argument exception
      */
     @Test(expected = InvalidArgumentException.class)
     public void testAbstractTwoArgMessageConstructorAgainstNegativeSecondValue()
             throws InvalidArgumentException {
         AbstractTwoArgMessageConcrete message = new AbstractTwoArgMessageConcrete(5, -1);
     }
 
     /**
      * Given correctly formed argument hash, AbstractTwoArgMessage should be
      * able to decode the arguments correctly.
      */
     @Test
     public void testDecodeArgumentsFromHash() {
         int[] result1 = { 500, 300 };
         assertArrayEquals(result1, AbstractTwoArgMessage.decodeArgumentsFromHash(0xFA012C));
 
         int[] result2 = { 0, 32767 };
         assertArrayEquals(result2, AbstractTwoArgMessage.decodeArgumentsFromHash(0x7FFF));
     }
 
     /**
      * Given an AbstractTwoArgMessage, and two valid arguments, the abstract
      * class should be able hash the arguments into correct places.
      * 
      * @throws InvalidArgumentException
      */
     @Test
     public void testHashArguments() throws InvalidArgumentException {
         AbstractTwoArgMessageConcrete message1 = new AbstractTwoArgMessageConcrete(500, 300);
         assertEquals(0xFA012C, message1.hashArguments());
 
         AbstractTwoArgMessageConcrete message2 = new AbstractTwoArgMessageConcrete(0, 32767);
         assertEquals(0x7FFF, message2.hashArguments());
 
     }
 }
