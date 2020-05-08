 package me.Whatshiywl.heroesskilltree.commands;
 
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.skill.Skill;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Multitallented
  */
 public class SkillListCommand {
     public static void skillList(HeroesSkillTree hst, CommandSender sender, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
             return;
         }
         Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player) sender);
        
         int j=0;
         HashMap<String, Skill> skills = new HashMap<>();
         ArrayList<String> alphabeticalSkills = new ArrayList<>();
         if (hero.getHeroClass() != null) {
             for (String skillName : hero.getHeroClass().getSkillNames()) {
                 Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(skillName);
                 if (hst.isLocked(hero, skill) || !hero.canUseSkill(skill)) {
                     continue;
                 }
                 skills.put(skillName, skill);
                 alphabeticalSkills.add(skillName);
             }
         }
         /*if (hero.getSecondClass() != null) {
             for (String skillName : hero.getSecondClass().getSkillNames()) {
                 Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(skillName);
                 if (hst.isLocked(hero, skill) || !hero.canUseSkill(skill)) {
                     continue;
                 }
                 skills.put(skillName, skill);
                 alphabeticalSkills.add(skillName);
             }
         }*/
         Collections.sort(alphabeticalSkills);
         int k = 0;
         int t = 0;
         if (args.length > 0 && !args[0].equalsIgnoreCase("1")) {
             try {
                 t = Integer.parseInt(args[0]);
                 t = t < 2 ? 1 : t;
                 k = (t - 1) * 10;
             } catch (NumberFormatException nfe) {
                 k=0;
             }
         } else {
             t = 1;
         }
         sender.sendMessage(ChatColor.GOLD + "[HST] Unlocked skills list page " + t + "/" + (Math.round(((double) alphabeticalSkills.size()) / 10)));
         for (int i = k; j < 10 && i < alphabeticalSkills.size(); i++) {
             if (j > 9) {
                 break;
             }
             String name = alphabeticalSkills.get(i);
            sender.sendMessage(ChatColor.GREEN + name + ": " + ChatColor.GRAY + skills.get(name).getDescription(hero));
             j++;
         }
     }
 }
