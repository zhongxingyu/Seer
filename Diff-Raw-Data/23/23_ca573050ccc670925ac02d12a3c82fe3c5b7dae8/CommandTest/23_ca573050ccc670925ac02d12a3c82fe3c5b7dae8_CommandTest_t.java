 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.p000ison.dev.commandlib.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Represents a CommandTest
  */
 @RunWith(JUnit4.class)
 public class CommandTest {
     private static final String HELP_TEXT = "| This is a help text! |";
     private static final String COMMAND_NOT_FOUND = "| Command not found! |";
     private static final String TEST_TEXT = "You executed the test command!";
     private static final String TEST_TEXT_SUB = "You executed the sub test command!";
     private static final String TEST_TEXT_SUB_SUB = "You executed the sub sub test command!";
 
     private CommandExecutor executor;
     private ConsoleCommandSender consoleSender;
     private ByteArrayOutputStream outContent;
 
     @Before
     public void createCommands() {
         outContent = new ByteArrayOutputStream();
         System.setOut(new PrintStream(outContent));
         consoleSender = new ConsoleCommandSender();
 
         executor = new CommandExecutor() {
             @Override
             public void onDisplayCommandHelp(CommandSender sender, Command command) {
                 sender.sendMessage(HELP_TEXT);
             }
 
             @Override
             public void onCommandNotFound(CommandSender sender) {
                 sender.sendMessage(COMMAND_NOT_FOUND);
             }

            @Override
            public void onPermissionFailed(CommandSender sender, Command command) {
            }
         };
 
         Command subCommand = executor.register(this, "SubTest");
         Command subsubCommand = executor.register(this, "SubSubTest");
         executor.register(this, "Test").addSubCommand(subCommand);
         subCommand.addSubCommand(subsubCommand);
     }
 
     @Test
     public void testMainCommand() {
         executor.executeAll(consoleSender, "test hey");
         assertEquals(TEST_TEXT, outContent.toString().trim());
     }
 
     @Test
     public void testSubCommand() {
         executor.executeAll(consoleSender, "test sub");
         assertEquals(TEST_TEXT_SUB + '\n' + TEST_TEXT, outContent.toString().trim());
 
     }
 
     @Test
     public void testSubSubCommand() {
         executor.executeAll(consoleSender, "test sub subsub");
         assertEquals(TEST_TEXT_SUB_SUB + '\n' + TEST_TEXT_SUB + '\n' + TEST_TEXT, outContent.toString().trim());
     }
 
     @After
     public void clean() {
         outContent.reset();
     }
 
     @Test
     public void testFuzzyStringMatching() {
         assertEquals("\"test\" does not equal \"test\" to 100%!", 1.0, CommandExecutor.fuzzyEqualsString("test", "test"), 0.0);
     }
 
     @CommandAnnotation(name = "Test", usage = "None", identifiers = {"test"}, arguments = "test", minArguments = 1, maxArguments = 1)
     public void testCommand(CommandSender sender, CallInformation info) {
         info.reply(TEST_TEXT);
     }
 
     @CommandAnnotation(name = "SubTest", usage = "None", identifiers = {"sub"}, minArguments = 0, maxArguments = 0)
     public void testSubCommand(CommandSender sender, CallInformation info) {
         info.reply(TEST_TEXT_SUB);
     }
 
     @CommandAnnotation(name = "SubSubTest", usage = "None", identifiers = {"subsub"}, minArguments = 0, maxArguments = 0)
     public void testSubSubCommand(CommandSender sender, CallInformation info) {
         info.reply(TEST_TEXT_SUB_SUB);
     }
 
     @After
     public void cleanUpStreams() throws IOException {
         outContent.close();
         System.setOut(null);
     }
 }
