 package balle.bluetooth.messages;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 
 import org.junit.Test;
 
 /**
  * Just a stub to go from Abstract class to a concrete one so compiler is happy.
  * 
  * The getOpcode() method will be mocked
  * 
  * @author s0909773
  */
 class AbstractMessageConcrete extends AbstractMessage {
 
     @Override
     public int getOpcode() {
         // This will be mocked
         return -1;
     }
 
     @Override
     public int hash() throws InvalidOpcodeException {
         // TODO Auto-generated method stub
         return 0;
     }
 
 }
 
 public class AbstractMessageTest {
 
     /**
      * Given a hashed message, the static method extactOpcodeFromEncodedMessage
      * should return the message's opcode.
      */
     @Test
     public void testExtactOpcodeFromEncodedMessage() {
 
         // 11 000000000000000000000000000000
         assertEquals(3, AbstractMessage.extactOpcodeFromEncodedMessage(0xC0000000));
 
         // 01 000000000011010001010000010101
         assertEquals(1, AbstractMessage.extactOpcodeFromEncodedMessage(0x400D1415));
 
         // 00 111111111111111111111111111111
         assertEquals(0, AbstractMessage.extactOpcodeFromEncodedMessage(0x3FFFFFFF));
 
     }
 
     /**
      * Given an opcode for the message, AbstractMessage should encode the
      * message so the opcode is first three bits of the message, and the rest of
      * the message is zeros.
      * 
      * @throws InvalidOpcodeException
      */
     @Test
     public void test_abstract_message_hashes_opcode_correctly() throws InvalidOpcodeException {
         AbstractMessage message1 = spy(new AbstractMessageConcrete());
         when(message1.getOpcode()).thenReturn(3);
 
         assertEquals(0xC0000000, message1.hashOpcode());
 
         AbstractMessage message2 = spy(new AbstractMessageConcrete());
         when(message2.getOpcode()).thenReturn(0);
 
         assertEquals(0x00000000, message2.hashOpcode());
 
         AbstractMessage message3 = spy(new AbstractMessageConcrete());
         when(message3.getOpcode()).thenReturn(1);
 
         assertEquals(0x40000000, message3.hashOpcode());
     }
 
     /**
      * Given that our bits-per-opcode is set to three and the provided opcode is
      * greater than 7 (so it does not fit in these bits) the hash function
      * should fail with InvalidOpcodeException
      * 
      * @throws InvalidOpcodeException
      */
     @Test(expected = InvalidOpcodeException.class)
     public void test_abstract_message_fails_to_hash_when_opcode_too_big()
             throws InvalidOpcodeException {
         AbstractMessage message1 = spy(new AbstractMessageConcrete());
         when(message1.getOpcode()).thenReturn(8);
 
         message1.hashOpcode();
 
     }
 
     /**
      * Given that our opcode is assumed to be unsigned integer and the opcode
      * provided is less than zero, the hash function should fail with
      * InvalidOpcodeException
      * 
      * @throws InvalidOpcodeException
      */
     @Test(expected = InvalidOpcodeException.class)
     public void test_abstract_message_fails_to_hash_when_opcode_too_small()
             throws InvalidOpcodeException {
         AbstractMessage message1 = spy(new AbstractMessageConcrete());
         when(message1.getOpcode()).thenReturn(-1);
 
         message1.hashOpcode();
 
     }
 
 }
