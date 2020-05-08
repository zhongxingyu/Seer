 package cryptocast.comm;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 import static cryptocast.comm.TestUtils.*;
 
 public class TestDecoratingMessageOutChannel {
     @Test
     public void decoratesCorrectly() throws Exception {
         MessageOutChannel inner = mock(MessageOutChannel.class);
         DecoratingMessageOutChannel sut = new DecoratingMessageOutChannel(
                  inner, str2bytes("abc"), str2bytes("hi"));
         sut.sendMessage(str2bytes("defg"));
         verify(inner).sendMessage(str2bytes("abcdefghi"));
        verifyNoMoreInteractions(inner);
     }
 }
