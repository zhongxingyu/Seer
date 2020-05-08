 package cryptocast.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.PrintStream;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static cryptocast.util.FileUtils.expandPath;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 
 import cryptocast.crypto.naorpinkas.*;
 import cryptocast.util.InteractiveCommandLineInterface.CommandError;
 
 public class TestShell {
     private Shell sut;
     SchnorrNPServer npServer = 
             (SchnorrNPServer) new SchnorrNPServerFactory().construct(10);
     @Mock private BufferedReader in;
     @Mock private PrintStream out;
     @Mock private PrintStream err;
     @Mock private Controller control;
     @Mock private NaorPinkasServerData model;
 
     @Before
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         sut = new Shell(in, out, err, control);
         when(control.getModel()).thenReturn(model);
     }
 
     @Test(expected=CommandError.class)
     public void invalidCommand() throws Throwable {
         String[] args = {""};
         sut.performCommand("42", args);
     }
 
     @Test
    public void HelpAdd() throws Throwable {
        String[] args = {"add"};
        sut.performCommand("help", args);
        verify(out).printf("Usage: %s %s\n", "add", "<name>");
     }
 
     @Test
    public void showHelp() throws Throwable {
         String[] args = {};
         sut.performCommand("help", args);
         verify(out).println("Use `help <cmd>' to get more information about a specific command");
     }
 
     @Test
     public void reInit() throws Throwable {
         String[] args = {"100"};
         when(in.readLine()).thenReturn("YES");
         sut.performCommand("init", args);
         sut.performCommand("init", args);
         verify(control, times(2)).reinitializeCrypto(100);
     }
     
     @Test(expected=CommandError.class)
     public void initInvalid() throws Throwable {
         String[] args = {"-1"};
         sut.performCommand("init", args);
     }
     
     @Test
     public void mp3Stream() throws Throwable {
         String[] args = {"C://test.mp3"};
         sut.performCommand("stream-mp3", args);
         File file = expandPath(args[0]);
         verify(control).streamAudio(file);
     }
     
     @Test
     public void revokeSingle() throws Throwable {
         String name = "bob";
         String[] args = {name};
         User<NPIdentity> bob = new User<NPIdentity>(name, npServer.getIdentity(3));
         when(model.getUserByName(name)).thenReturn(Optional.fromNullable(bob));
         sut.performCommand("revoke", args);
         verify(model).revoke(ImmutableSet.of(bob));
     }
     
     @Test
     public void revokeSeveral() throws Throwable {
         String[] names = { "bob", "alice", "james" };
         List<User<NPIdentity>> users = ImmutableList.of(
             new User<NPIdentity>(names[0], npServer.getIdentity(3)),
             new User<NPIdentity>(names[1], npServer.getIdentity(5)),
             new User<NPIdentity>(names[2], npServer.getIdentity(6))
         );
         when(model.getUserByName(names[0])).thenReturn(Optional.fromNullable(users.get(0)));
         when(model.getUserByName(names[1])).thenReturn(Optional.fromNullable(users.get(1)));
         when(model.getUserByName(names[2])).thenReturn(Optional.fromNullable(users.get(2)));
         sut.performCommand("revoke", names);
         verify(model).revoke(ImmutableSet.copyOf(users));
     }
     
     @Test
     public void unrevoke() throws Throwable {
         String name = "alice";
         String[] args = {name};
         User<NPIdentity> myAlice = new User<NPIdentity>(name, npServer.getIdentity(12));
         when(model.getUserByName(name)).thenReturn(Optional.fromNullable(myAlice));
         sut.performCommand("unrevoke", args);
         verify(model).unrevoke(myAlice);
     }
     
     @Test
     public void parseNotAnInt() {
         Optional<Integer> result = Shell.parseInt("?!/($%?");
         assertFalse(result.isPresent());
     }
     
     @Test
     public void parseRealInts() {
         Optional<Integer> result = Shell.parseInt("-12");
         assertTrue(result.isPresent());
         int expected = -12;
         assertTrue(expected == result.get());
     }
 
 }
