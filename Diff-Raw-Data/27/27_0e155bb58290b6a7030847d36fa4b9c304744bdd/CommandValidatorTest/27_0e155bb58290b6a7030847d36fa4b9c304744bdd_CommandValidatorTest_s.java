 package de.cubenation.plugins.utils.commandapi;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import de.cubenation.plugins.utils.commandapi.exception.CommandWarmUpException;
 import de.cubenation.plugins.utils.commandapi.testutils.AbstractTest;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandDuplicateMain1;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandDuplicateMain2;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandPermission;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandPermissionBoth;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandPermissionBothWithout;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandPermissionBothWithoutMulti;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandPermissionMulti;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandPermissionMultiPart;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandPermissionNotWithout;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandPermissionNotWithoutMulti;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandPermissionWithout;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.exception.TestInvalidCommandPermissionWithoutMulti;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.minmax.TestInvalidCommandMinMax1;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.minmax.TestInvalidCommandMinMax2;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.minmax.TestInvalidCommandMinMaxOk;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.parameter.TestInvalidCommandMainSub1;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.parameter.TestInvalidCommandMainSub2;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.world.TestInvalidCommandWorld1;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.world.TestInvalidCommandWorld2;
 import de.cubenation.plugins.utils.commandapi.testutils.testcommands.world.TestValidCommandMultiWorld2;
 
 public class CommandValidatorTest extends AbstractTest {
     @Test
     public void testCommandValidator() throws CommandWarmUpException {
         CommandValidator commandValidator = new CommandValidator();
 
         commandValidator.checkEqual(null, null);
     }
 
     @Test
     public void testValidateDuplicateMainCommand() throws CommandWarmUpException {
         commandsManager.add(TestInvalidCommandDuplicateMain1.class);
         try {
             commandsManager.add(TestInvalidCommandDuplicateMain2.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandDuplicateMain2.class.getName() + "] similar command found for testThreeMainCommand in class "
                     + TestInvalidCommandDuplicateMain1.class.getName() + " method testOneMainCommand", e.getMessage());
         }
     }
 
     @Test
     public void testValidateDuplicateMinMaxCommandOk() throws CommandWarmUpException {
         commandsManager.add(TestInvalidCommandMinMaxOk.class);
     }
 
     @Test
     public void testValidateDuplicateMinMaxCommand1() throws CommandWarmUpException {
         commandsManager.add(TestInvalidCommandMinMax1.class);
         try {
             commandsManager.add(TestInvalidCommandMinMax2.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandMinMax2.class.getName() + "] similar command found for testMaxCommand in class "
                     + TestInvalidCommandMinMax1.class.getName() + " method testMinCommand", e.getMessage());
         }
     }
 
     @Test
     public void testValidateDuplicateMinMaxCommand2() throws CommandWarmUpException {
         commandsManager.add(TestInvalidCommandMinMax2.class);
         try {
             commandsManager.add(TestInvalidCommandMinMax1.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandMinMax1.class.getName() + "] similar command found for testMinCommand in class "
                     + TestInvalidCommandMinMax2.class.getName() + " method testMaxCommand", e.getMessage());
         }
     }
 
     @Test
     public void testValidateDuplicateWorld1Command() throws CommandWarmUpException {
         commandsManager.add(TestInvalidCommandWorld1.class);
         try {
             commandsManager.add(TestInvalidCommandWorld2.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandWorld2.class.getName() + "] similar command found for testFiveWorldCommand in class "
                     + TestInvalidCommandWorld1.class.getName() + " method testOneWorldCommand", e.getMessage());
         }
     }
 
     @Test
     public void testValidateDuplicateWorld2Command() throws CommandWarmUpException {
         commandsManager.add(TestValidCommandMultiWorld2.class);
     }
 
     @Test
     public void testValidateDuplicateSubCommand() throws CommandWarmUpException {
         commandsManager.add(TestInvalidCommandMainSub1.class);
         try {
             commandsManager.add(TestInvalidCommandMainSub2.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertEquals("[" + TestInvalidCommandMainSub2.class.getName() + "] similar command found for testThreeMainOneSubCommand in class "
                     + TestInvalidCommandMainSub1.class.getName() + " method testTwoMainOneSubCommand", e.getMessage());
         }
     }
 
     @Test
     public void testValidatePermissionCommand() throws CommandWarmUpException {
         try {
             commandsManager.add(TestInvalidCommandPermission.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
            Assert.assertEquals("[" + TestInvalidCommandPermission.class.getName() + "] similar command found for testPermissionCommandNot in class "
                    + TestInvalidCommandPermission.class.getName() + " method testPermissionCommand", e.getMessage());
         }
     }
 
     @Test
     public void testValidatePermissionBothCommand() throws CommandWarmUpException {
         try {
             commandsManager.add(TestInvalidCommandPermissionBoth.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
            Assert.assertEquals("[" + TestInvalidCommandPermissionBoth.class.getName() + "] similar command found for testPermissionCommandNot in class "
                    + TestInvalidCommandPermissionBoth.class.getName() + " method testPermissionCommand", e.getMessage());
         }
     }
 
     @Test
     public void testValidatePermissionBothWithoutCommand() throws CommandWarmUpException {
         try {
             commandsManager.add(TestInvalidCommandPermissionBothWithout.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertTrue(e.getMessage().contains(TestInvalidCommandPermissionBothWithout.class.getName()));
             Assert.assertTrue(e.getMessage().contains("testPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("testWithoutPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("similar command found for"));
         }
     }
 
     @Test
     public void testValidatePermissionBothWithoutMultiCommand() throws CommandWarmUpException {
         try {
             commandsManager.add(TestInvalidCommandPermissionBothWithoutMulti.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertTrue(e.getMessage().contains(TestInvalidCommandPermissionBothWithoutMulti.class.getName()));
             Assert.assertTrue(e.getMessage().contains("testPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("testWithoutPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("similar command found for"));
         }
     }
 
     @Test
     public void testValidatePermissionMultiCommand() throws CommandWarmUpException {
         try {
             commandsManager.add(TestInvalidCommandPermissionMulti.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
            Assert.assertEquals("[" + TestInvalidCommandPermissionMulti.class.getName() + "] similar command found for testPermissionCommandNot in class "
                    + TestInvalidCommandPermissionMulti.class.getName() + " method testPermissionCommand", e.getMessage());
         }
     }
 
     @Test
     public void testValidatePermissionMultiPartCommand() throws CommandWarmUpException {
         try {
             commandsManager.add(TestInvalidCommandPermissionMultiPart.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
            Assert.assertEquals("[" + TestInvalidCommandPermissionMultiPart.class.getName() + "] similar command found for testPermissionCommandNot in class "
                    + TestInvalidCommandPermissionMultiPart.class.getName() + " method testPermissionCommand", e.getMessage());
         }
     }
 
     @Test
     public void testValidatePermissionNotWithoutCommand() throws CommandWarmUpException {
         try {
             commandsManager.add(TestInvalidCommandPermissionNotWithout.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertTrue(e.getMessage().contains(TestInvalidCommandPermissionNotWithout.class.getName()));
             Assert.assertTrue(e.getMessage().contains("testPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("testWithoutPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("similar command found for"));
         }
     }
 
     @Test
     public void testValidatePermissionNotWithoutMultiCommand() throws CommandWarmUpException {
         try {
             commandsManager.add(TestInvalidCommandPermissionNotWithoutMulti.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertTrue(e.getMessage().contains(TestInvalidCommandPermissionNotWithoutMulti.class.getName()));
             Assert.assertTrue(e.getMessage().contains("testPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("testWithoutPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("similar command found for"));
         }
     }
 
     @Test
     public void testValidatePermissionWithoutCommand() throws CommandWarmUpException {
         try {
             commandsManager.add(TestInvalidCommandPermissionWithout.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertTrue(e.getMessage().contains(TestInvalidCommandPermissionWithout.class.getName()));
             Assert.assertTrue(e.getMessage().contains("testPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("testWithoutPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("similar command found for"));
         }
     }
 
     @Test
     public void testValidatePermissionWithoutMultiCommand() throws CommandWarmUpException {
         try {
             commandsManager.add(TestInvalidCommandPermissionWithoutMulti.class);
             Assert.fail("expected exception");
         } catch (CommandWarmUpException e) {
             Assert.assertTrue(e.getMessage().contains(TestInvalidCommandPermissionWithoutMulti.class.getName()));
             Assert.assertTrue(e.getMessage().contains("testPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("testWithoutPermissionCommand"));
             Assert.assertTrue(e.getMessage().contains("similar command found for"));
         }
     }
 }
