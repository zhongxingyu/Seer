 package org.jggug.kobo.gircbot.core;
 
 import java.util.Arrays;
 import java.util.List;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 public class PrimaryMonitorTest {
 
     private PrimaryMonitor primaryMonitor;
     private String defaultChannel;
     private List<String> orderedPrimaryNicks;
     private IrcControl ircControl;
 
     @Before
     public void before() throws Exception {
         // Setup arguments
         defaultChannel = "#default";
         orderedPrimaryNicks = Arrays.asList("bot1", "bot2", "bot3");
         // Setup mock
         ircControl = mock(IrcControl.class);
         when(ircControl.getNick()).thenReturn("bot2");
         // Setup SUT
         primaryMonitor = spy(new PrimaryMonitor(defaultChannel, orderedPrimaryNicks, ircControl));
     }
 
     @After
     public void after() throws Exception {
         verify(ircControl).getNick();
     }
 
     @Test
     public void isPrimary_existingPriorerNickThanMe_toBeFalse() throws Exception {
         // Setup
         doReturn(Arrays.asList("bot1", "bot2", "bot3")).when(primaryMonitor).getJoinedNicks("#test");
         // Exercise
         boolean actual = primaryMonitor.isPrimary("#test");
         // Verify
         assertFalse(actual);
         verify(primaryMonitor).getJoinedNicks("#test");
         verify(ircControl, never()).sendMessage(anyString(), anyString());
     }
 
     @Test
     public void isPrimary_notExistingPriorerNickThanMe_toBeTrue() throws Exception {
         // Setup
         doReturn(Arrays.asList("bot2", "bot3")).when(primaryMonitor).getJoinedNicks("#test");
         // Exercise
         boolean actual = primaryMonitor.isPrimary("#test");
         // Verify
         assertTrue(actual);
         verify(primaryMonitor).getJoinedNicks("#test");
         verify(ircControl).sendMessage(eq("#test"), anyString());
     }
 
     @Test
     public void isPrimary_notExistingMe_toBeFalse() throws Exception {
         // Setup
         doReturn(Arrays.asList("bot3")).when(primaryMonitor).getJoinedNicks("#test");
         // Exercise
         boolean actual = primaryMonitor.isPrimary("#test");
         // Verify
         assertFalse(actual);
         verify(primaryMonitor).getJoinedNicks("#test");
         verify(ircControl, never()).sendMessage(anyString(), anyString());
     }
 
     @Test
     public void isPrimary_notExistingAnyone_toBeFalse() throws Exception {
         // Setup
         doReturn(Arrays.asList()).when(primaryMonitor).getJoinedNicks("#test");
         // Exercise
         boolean actual = primaryMonitor.isPrimary("#test");
         // Verify
         assertFalse(actual);
         verify(primaryMonitor).getJoinedNicks("#test");
         verify(ircControl, never()).sendMessage(anyString(), anyString());
     }
 
     @Test
     public void isPrimary_emptyOrderedPrimaryNicks_toThrowException() throws Exception {
         // Setup
         doReturn(Arrays.asList("bot1", "bot2", "bot3")).when(primaryMonitor).getJoinedNicks("#test");
         primaryMonitor.orderedPrimaryNicks = Arrays.<String>asList();
         // Exercise & Verify
         try {
             primaryMonitor.isPrimary("#test");
             fail();
         } catch (IllegalStateException e) {
             assertEquals("Why isn't there this bot in orderedPrimaryNicks? : bot2 []", e.getMessage());
         }
     }
 
     @Test
     public void isPrimaryGlobally_existingPriorerNickThanMe_toBeFalse() throws Exception {
         // Setup
         doReturn(Arrays.asList("bot1", "bot2", "bot3")).when(primaryMonitor).getJoinedNicks("#default");
         // Exercise
         boolean actual = primaryMonitor.isPrimaryGlobally();
         // Verify
         assertFalse(actual);
         verify(primaryMonitor).getJoinedNicks("#default");
         verify(ircControl, never()).sendMessage(anyString(), anyString());
     }
 
     @Test
     public void isPrimaryGlobally_notExistingPriorerNickThanMe_toBeTrue() throws Exception {
         // Setup
         doReturn(Arrays.asList("bot2", "bot3")).when(primaryMonitor).getJoinedNicks("#default");
         // Exercise
         boolean actual = primaryMonitor.isPrimaryGlobally();
         // Verify
         assertTrue(actual);
         verify(primaryMonitor).getJoinedNicks("#default");
         verify(ircControl).sendMessage(eq("#default"), anyString());
     }
 
     @Test
    public void isPrimary_notPrimaryToPrimary_atFirstPrimaryMonitorIsNull() throws Exception {
        // Setup
        doReturn(Arrays.asList("bot2", "bot3")).when(primaryMonitor).getJoinedNicks("#test");
        primaryMonitor.primaryStatus.remove("#test");
        // Exercise
        boolean actual = primaryMonitor.isPrimary("#test");
        // Verify
        assertTrue(actual);
        assertTrue(primaryMonitor.primaryStatus.get("#test"));
        verify(primaryMonitor).getJoinedNicks("#test");
        verify(ircControl).sendMessage(eq("#test"), anyString());
    }

    @Test
     public void isPrimary_notPrimaryToPrimary() throws Exception {
         // Setup
         doReturn(Arrays.asList("bot2", "bot3")).when(primaryMonitor).getJoinedNicks("#test");
        primaryMonitor.primaryStatus.put("#test", false);
         // Exercise
         boolean actual = primaryMonitor.isPrimary("#test");
         // Verify
         assertTrue(actual);
        assertTrue(primaryMonitor.primaryStatus.get("#test"));
         verify(primaryMonitor).getJoinedNicks("#test");
         verify(ircControl).sendMessage(eq("#test"), anyString());
     }
 
     @Test
     public void isPrimary_primaryToNotPrimary() throws Exception {
         // Setup
         doReturn(Arrays.asList("bot1", "bot2", "bot3")).when(primaryMonitor).getJoinedNicks("#test");
        primaryMonitor.primaryStatus.put("#test", true);
         // Exercise
         boolean actual = primaryMonitor.isPrimary("#test");
         // Verify
         assertFalse(actual);
        assertFalse(primaryMonitor.primaryStatus.get("#test"));
         verify(primaryMonitor).getJoinedNicks("#test");
         verify(ircControl).sendMessage(eq("#test"), anyString());
     }
 
 }
