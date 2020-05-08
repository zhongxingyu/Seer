 package ch.jmnetwork.cookieclicker.util;
 
 import ch.jmnetwork.cookieclicker.CookieManager;
 import ch.jmnetwork.cookieclicker.exceptions.CCLoadFromDiskException;
 import ch.jmnetwork.cookieclicker.helper.Helper;
 import ch.jmnetwork.cookieclicker.helper.achievement.AchievementEventHandler;
 
 public class SaveLoadHandler
 {
     
     private static CookieManager cookiemanager;
     private static PropertiesHandler ph = new PropertiesHandler("CCSave.xml");
     
     public SaveLoadHandler(CookieManager cm)
     {
         cookiemanager = cm;
     }
     
     public void saveToDisk()
     {
         ph.setProperty("CURRENT_COOKIES", cookiemanager.getCurrentCookies() + "");
         ph.setProperty("TOTAL_COOKIES", cookiemanager.getTotalCookies() + "");
         ph.setProperty("ACHIEVEMENTS", AchievementEventHandler.save());
         ph.setProperty("POINTERS_OWNED", Helper.owned[0] + "");
         ph.setProperty("GRANDMAS_OWNED", Helper.owned[1] + "");
         ph.setProperty("FARMS_OWNED", Helper.owned[2] + "");
         ph.setProperty("FACTORYS_OWNED", Helper.owned[3] + "");
         ph.setProperty("MINES_OWNED", Helper.owned[4] + "");
         ph.setProperty("SHIPMENTS_OWNED", Helper.owned[5] + "");
         ph.setProperty("ALCHEMYLABSS_OWNED", Helper.owned[6] + "");
         ph.setProperty("PORTALS_OWNED", Helper.owned[7] + "");
         ph.setProperty("TIMEMACHINES_OWNED", Helper.owned[8] + "");
         
         ph.saveProperties();
     }
     
     public void loadFromDisk() throws CCLoadFromDiskException
     {
         try
         {
             cookiemanager.setTotalCookies(Long.parseLong(ph.getProperty("TOTAL_COOKIES", "0")));
             cookiemanager.setCurrentCookies(Long.parseLong(ph.getProperty("CURRENT_COOKIES", "0")));
             Helper.owned[0] = Integer.parseInt(ph.getProperty("POINTERS_OWNED", "0"));
             Helper.owned[1] = Integer.parseInt(ph.getProperty("GRANDMAS_OWNED", "0"));
             Helper.owned[2] = Integer.parseInt(ph.getProperty("FARMS_OWNED", "0"));
            Helper.owned[3] = Integer.parseInt(ph.getProperty("FACTORYS_OWNED"));
             Helper.owned[4] = Integer.parseInt(ph.getProperty("MINES_OWNED", "0"));
             Helper.owned[5] = Integer.parseInt(ph.getProperty("SHIPMENTS_OWNED", "0"));
             Helper.owned[6] = Integer.parseInt(ph.getProperty("ALCHEMYLABS_OWNED", "0"));
             Helper.owned[7] = Integer.parseInt(ph.getProperty("PORTALS_OWNED", "0"));
             Helper.owned[8] = Integer.parseInt(ph.getProperty("TIMEMACHINES_OWNED", "0"));
         }
         catch (Exception e)
         {
             throw new CCLoadFromDiskException(e);
         }
         
     }
 }
