 package de.cubenation.plugins.utils.commandapi;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.junit.Assert;
 import org.junit.Test;
 
 import de.cubenation.plugins.utils.commandapi.exception.CommandWarmUpException;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.TestInvalidCommandEmptyMain;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.TestInvalidCommandOtherException;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.TestInvalidCommandWrongConstructor;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.TestInvalidCommandWrongMethodParameterFirst;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.TestInvalidCommandWrongMethodParameterNumber;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.TestInvalidCommandWrongMethodParameterSecond;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.TestInvalidCommandWrongMinMax;
 
 public class CommandExceptionTest {
     @Test
     public void testCommandNull() {
         CommandsManager commandsManager = new CommandsManager(new JavaPlugin() {
         });
 
         try {
             commandsManager.add(null);
             Assert.fail("expected null command object raise exception");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("command class could not be null", e.getMessage());
         }
     }
 
     @Test
     public void testCommandMainNull() {
         CommandsManager commandsManager = new CommandsManager(new JavaPlugin() {
         });
 
         try {
             commandsManager.add(TestInvalidCommandEmptyMain.class);
             Assert.fail("expected null main string raise exception");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandEmptyMain.class.getName() + "] main attribute could not be empty", e.getMessage());
         }
     }
 
     @Test
     public void testCommandWrongConstructor() {
         CommandsManager commandsManager = new CommandsManager(new JavaPlugin() {
         });
 
         try {
             commandsManager.add(TestInvalidCommandWrongConstructor.class);
             Assert.fail("expected wrong constructor");
         } catch (CommandWarmUpException e) {
            Assert.assertEquals("[" + TestInvalidCommandWrongConstructor.class.getName()
                    + "] no matching constructor found, matches are empty constructors and constructors with JavaPlugin as parameter", e.getMessage());
         }
     }
 
     @Test
     public void testCommandWrongNumberOfMethodParameter() {
         CommandsManager commandsManager = new CommandsManager(new JavaPlugin() {
         });
 
         try {
             commandsManager.add(TestInvalidCommandWrongMethodParameterNumber.class);
             Assert.fail("expected wrong number of parameter");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandWrongMethodParameterNumber.class.getName()
                     + "] wrong number of paramter in method wrongCommad, expected 2 but was 1", e.getMessage());
         }
     }
 
     @Test
     public void testCommandWrongFirstMethodParameter() {
         CommandsManager commandsManager = new CommandsManager(new JavaPlugin() {
         });
 
         try {
             commandsManager.add(TestInvalidCommandWrongMethodParameterFirst.class);
             Assert.fail("expected wrong first parameter");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandWrongMethodParameterFirst.class.getName()
                     + "] first parameter in method wrongCommad must be Player or ConsoleCommandSender but was java.lang.Integer", e.getMessage());
         }
     }
 
     @Test
     public void testCommandWrongSecondMethodParameter() {
         CommandsManager commandsManager = new CommandsManager(new JavaPlugin() {
         });
 
         try {
             commandsManager.add(TestInvalidCommandWrongMethodParameterSecond.class);
             Assert.fail("expected wrong second parameter");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandWrongMethodParameterSecond.class.getName()
                     + "] second parameter in method wrongCommad must be String[] but was java.lang.Integer", e.getMessage());
         }
     }
 
     @Test
     public void testCommandOtherException() {
         CommandsManager commandsManager = new CommandsManager(new JavaPlugin() {
         });
 
         try {
             commandsManager.add(TestInvalidCommandOtherException.class);
             Assert.fail("expected other exception");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandOtherException.class.getName() + "] error on command warmup", e.getMessage());
         }
     }
 
     @Test
     public void testCommandWrongMinMax() {
         CommandsManager commandsManager = new CommandsManager(new JavaPlugin() {
         });
 
         try {
             commandsManager.add(TestInvalidCommandWrongMinMax.class);
             Assert.fail("expected other exception");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandWrongMinMax.class.getName() + "] min(2) attribute could not be greater than max(1) attribute",
                     e.getMessage());
         }
     }
 }
