 package com.herocraftonline.dev.heroes.menus;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 import me.desht.scrollingmenusign.SMSException;
 import me.desht.scrollingmenusign.SMSHandler;
 import me.desht.scrollingmenusign.SMSMenu;
 import me.desht.scrollingmenusign.ScrollingMenuSign;
 import me.desht.scrollingmenusign.enums.SMSMenuAction;
 import me.desht.scrollingmenusign.views.SMSMapView;
 
 public class MenuHandler {
 
     public static SMSHandler smsHandler;
     public static Map<HeroClass, SMSMenu> menus = new HashMap<HeroClass, SMSMenu>();
     public static Map<HeroClass, SMSMapView> views = new HashMap<HeroClass, SMSMapView>();
 
     public static void setup(Plugin p) {
         if (p != null && p instanceof ScrollingMenuSign) {
             ScrollingMenuSign sms = (ScrollingMenuSign) p;
             smsHandler = sms.getHandler();
             Heroes.useSMS = true;
             Heroes.log(Level.INFO, "ScrollingMenuSign integration is enabled");
         }
     }
 
     public static void setupMenu(HeroClass hc, Heroes plugin) {
         String name = hc.getName();
         SMSMenu menu = null;
 
         if (smsHandler == null) {
             return;
         }
         try {
             menu = smsHandler.getMenu(name + " menu");
         } catch (SMSException e) {
             menu = smsHandler.createMenu(name + " menu", name + " Skills", name);
         }
         if (menu == null) {
             menu = smsHandler.createMenu(name + " menu", name + " Skills", name);
         }
         menu.getItems().clear();
         menu.setAutosave(true);
         menu.setAutosort(true);
         // Dump the menu into the map
         menus.put(hc, menu);
         for (String sn : hc.getSkillNames()) {
             Skill skill = plugin.getSkillManager().getSkill(sn);
             if (skill instanceof ActiveSkill) {
                 menu.addItem(skill.getName(), "/" + skill.getIdentifiers()[0], "");
             }
         }
         SMSMapView view = null;
         try {
             view = (SMSMapView) SMSMapView.getView(name + " view");
         } catch (SMSException e) {
             short id = Bukkit.getServer().createMap(Bukkit.getWorlds().get(0)).getId();
             view = new SMSMapView(name + " view", menu);
             view.register();
             view.setMapId(id);
             view.update(menu, SMSMenuAction.REPAINT);
         }
         views.put(hc, view);
         view.setAutosave(true);
     }
 }
