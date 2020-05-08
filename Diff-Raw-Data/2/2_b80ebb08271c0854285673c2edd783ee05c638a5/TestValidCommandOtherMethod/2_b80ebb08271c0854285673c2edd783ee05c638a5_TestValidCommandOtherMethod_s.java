 package de.cubenation.plugins.utils.commandapi.testutils.testcommands;
 
import junit.framework.Assert;
 
 import org.bukkit.entity.Player;
 
 import de.cubenation.plugins.utils.commandapi.annotation.Command;
 import de.cubenation.plugins.utils.commandapi.testutils.TestPlugin;
 
 public class TestValidCommandOtherMethod {
     private TestPlugin plugin;
 
     public TestValidCommandOtherMethod(TestPlugin plugin) {
         this.plugin = plugin;
     }
 
     @Command(main = "test")
     public void testOtherCommand(Player player, String[] args) {
         plugin.doSomeThing("testOtherCommand");
     }
 
     public void failCommand(Player player, String[] args) {
         Assert.fail("command should not be called");
     }
 }
