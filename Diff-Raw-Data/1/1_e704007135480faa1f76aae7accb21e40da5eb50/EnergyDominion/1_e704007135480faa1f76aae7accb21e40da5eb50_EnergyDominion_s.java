 package org.hopto.energy.energydominion;
 
 import net.xeoh.plugins.base.PluginManager;
 import net.xeoh.plugins.base.impl.PluginManagerFactory;
 import net.xeoh.plugins.base.util.PluginManagerUtil;
 import net.xeoh.plugins.base.util.uri.ClassURI;
 import org.hopto.energy.energydominion.api.Expansion;
 import org.hopto.energy.energydominion.api.Game;
 import org.hopto.energy.energydominion.api.Player;
 import org.hopto.energy.energydominion.api.core.LocalGame;
 import org.hopto.energy.energydominion.api.core.LocalPlayer;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 
 public class EnergyDominion {
     private Game game = new LocalGame(new ArrayList<Player>(Arrays.<Player>asList(new LocalPlayer("Energy"), new LocalPlayer("Star"))));
 
     public static void main(String[] args) {
 
         PluginManager pm = PluginManagerFactory.createPluginManager();
 
         try {
            pm.addPluginsFrom(ClassURI.CLASSPATH);
            pm.addPluginsFrom(new File("expansions/").toURI());
         } catch (Exception e) {
             e.printStackTrace();
         }
 
 
 
             test(pm);
 
 
 
 
 
     }
 
     private static void test(PluginManager pm) {
 
 
         PluginManagerUtil pmUtil = new PluginManagerUtil(pm);
         Collection<Expansion> expansions = pmUtil.getPlugins(Expansion.class);
         for (Expansion expansion : expansions) {
             System.out.println("--------------------------------------");
             System.out.println(expansion.getClass().toString());
             System.out.println(expansion.toString());
             System.out.println("Name: " + expansion.getName());
             if (expansion instanceof Expansion) {
                 System.out.println("IsExpansion: True");
             }
             ;
             System.out.println("CardList:");
             System.out.println(expansion.getClassList().toString());
             for (Class card : expansion.getClassList()) {
                 System.out.println("**********");
                 System.out.println(card.getSimpleName());
                 try {
                     System.out.println(card.newInstance().toString());
                 } catch (InstantiationException e) {
                     e.printStackTrace();
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 }
                 System.out.println("**********");
             }
             System.out.println("--------------------------------------");
         }
     }
 
     public Game getGame() {
         return game;
     }
 
 
 }
