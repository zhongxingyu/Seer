 package net.krinsoft.jobsuite.commands;
 
 import net.krinsoft.jobsuite.Job;
 import net.krinsoft.jobsuite.JobCore;
 import net.krinsoft.jobsuite.JobItem;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.util.List;
 
 /**
  * @author krinsdeath
  */
 public class JobAddEnchantmentCommand extends JobCommand {
 
     public JobAddEnchantmentCommand(JobCore instance) {
         super(instance);
         setName("JobSuite: Add Enchantment");
         setCommandUsage("/job addenchant [item num] [enchantment] [power]");
         setArgRange(2, 3);
         addKey("jobsuite addenchant");
         addKey("job addenchant");
         addKey("js addenchant");
         addKey("jobsuite ae");
         addKey("job ae");
         addKey("js ae");
         setPermission("jobsuite.addenchant", "Adds an enchantment to a job item.", PermissionDefault.TRUE);
     }
 
     @Override
     public void runCommand(CommandSender sender, List<String> args) {
         Job job = manager.getQueuedJob(sender.getName());
         if (job != null) {
             JobItem jItem;
             Enchantment ench;
             int level = 1;
             try {
                 jItem = job.getItem(Integer.parseInt(args.get(0)));
                 if (args.size() == 3) {
                     level = Integer.parseInt(args.get(2));
                 }
             } catch (NumberFormatException e) {
                 error(sender, "Error parsing argument: expected number");
                 return;
             }
             try {
                 ench = Enchantment.getById(Integer.parseInt(args.get(1)));
             } catch (NumberFormatException e) {
                 ench = Enchantment.getByName(args.get(1));
             }
             if (jItem == null) {
                 error(sender, "Couldn't find an item at that index.");
                 return;
             }
             if (ench == null) {
                 error(sender, "No such enchantment.");
             } else if (!ench.canEnchantItem(jItem.getItem())) {
                 error(sender, "You can't attach that enchantment (" + ench.getName() + ") to the specified item.");
            } else if (ench.getMaxLevel() > level) {
                 error(sender, "That enchantment (" + ench.getName() + ") can't reach that level.");
             } else {
                 jItem.addEnchant(ench, level);
                 message(sender, "Enchantment added to item at index '" + jItem.getId() + "'.");
                 message(sender, "View item info: " + ChatColor.DARK_AQUA + "/job info this " + jItem.getId());
                 message(sender, "Remove the enchantment: " + ChatColor.DARK_AQUA + "/job remenchant " + jItem.getId() + " " + ench.getName());
                 message(sender, "Add another enchant: " + ChatColor.DARK_AQUA + "/job addenchant " + jItem.getId() + " [enchantment]");
                 message(sender, "List current items: " + ChatColor.DARK_AQUA + "/job listitems");
             }
 
         }
     }
 }
