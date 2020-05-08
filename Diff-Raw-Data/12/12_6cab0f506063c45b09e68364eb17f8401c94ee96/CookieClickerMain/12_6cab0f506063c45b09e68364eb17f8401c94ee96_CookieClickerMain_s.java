 package ch.jmnetwork.cookieclicker;
 
 import java.io.File;
 
 import ch.jmnetwork.cookieclicker.helper.Helper;
 import ch.jmnetwork.cookieclicker.threading.AchievementThread;
 import ch.jmnetwork.cookieclicker.threading.SaveThread;
 import ch.jmnetwork.cookieclicker.ui.CCUserInterface;
 import ch.jmnetwork.cookieclicker.util.CryptedSLHandler;
 import ch.jmnetwork.cookieclicker.util.SaveLoadHandler;
 import ch.jmnetwork.vapi.VersionApi;
 import ch.jmnetwork.vapi.events.VapiListener;
 
 public class CookieClickerMain implements VapiListener
 {
     public static CookieClickerMain INSTANCE;
     public static int TICKS_PER_SECOND = 25;
     public static int SECOND_TASK_EACH_SECONDS = 10;
     private static long lastTime_1 = 0;
     private static long thisTime_1 = 0;
     private static long lastTime_2 = 0;
     private static long thisTime_2 = 0;
     private static long lastTime_3 = 0;
     private static long thisTime_3 = 0;
     private static long lastHandmadeCookies = 0;
     private static long thisHandmadeCookies = 0;
     public static long handmadePerSec = 0;
     private static CCUserInterface ccui;
     private static CookieManager cookiemanager = new CookieManager();
     public static CryptedSLHandler cslhandler;
     private static SaveLoadHandler slhandler = new SaveLoadHandler(cookiemanager);
     private static VersionApi vapi = new VersionApi();
     private static boolean doUselessLoop = true;
     
     public static void main(String[] args)
     {
         INSTANCE = new CookieClickerMain();
         vapi.setVersioningURL("http://www.jmnetwork.ch/public/jm-software/ccassets/");
         vapi.reload();
         vapi.addListener(INSTANCE);
         
         if (!downloadIcon())
         {
             start();
         }
         else
         {
             while (doUselessLoop)
             {
                 // Loop so the main program doesn't shutdown while we are downloading files
             }
         }
     }
     
     private static void start()
     {
         cslhandler = new CryptedSLHandler(cookiemanager);
         ccui = new CCUserInterface(cookiemanager, slhandler, cslhandler);
         downloadIcon();
         
         cslhandler.load();
         
         System.out.println("Starting game...");
         while (true)
         {
             if (lastTime_1 == 0) lastTime_1 = System.nanoTime();
             if (lastTime_2 == 0) lastTime_2 = System.nanoTime();
             if (lastTime_3 == 0) lastTime_3 = System.nanoTime();
             if (lastHandmadeCookies == 0) lastHandmadeCookies = cookiemanager.getHandmadeCookies();
             
             thisTime_1 = System.nanoTime();
             thisTime_2 = System.nanoTime();
             thisTime_3 = System.nanoTime();
             
             // only run this TICKS_PER_SECOND times per second
             if ((thisTime_1 - lastTime_1) / 1000000 >= 1000 / TICKS_PER_SECOND)
             {
                 // ======================================//
                 // STUFF TO RUN EACH TICK
                 // ======================================//
                 
                 INSTANCE.handleTick();
                 ccui.updateUI();
                 lastTime_1 = System.nanoTime();
             }
             
             if ((thisTime_3 - lastTime_3) / 1000000 >= 2 * 1000)
             {
                 INSTANCE.handleAchievements();
                 thisHandmadeCookies = cookiemanager.getHandmadeCookies();
                 
                 handmadePerSec = (thisHandmadeCookies - lastHandmadeCookies) / 2;
                 
                 lastHandmadeCookies = cookiemanager.getHandmadeCookies();
                 lastTime_3 = System.nanoTime();
             }
             
             if ((thisTime_2 - lastTime_2) / 1000000 >= SECOND_TASK_EACH_SECONDS * 1000)
             {
                 // ===================================================//
                 // STUFF TO RUN EVERY SECOND_TASK_EACH_SECONDS SECONDS
                 // ===================================================//
                 
                 // Save game state
                 new Thread(new SaveThread(ccui, cslhandler)).start();
                 
                 lastTime_2 = System.nanoTime();
             }
         }
     }
     
     private void handleTick()
     {
         cookiemanager.decimalValue += Helper.getCookieRate() / TICKS_PER_SECOND;
         
         if (cookiemanager.decimalValue >= 1.0F)
         {
             float rem = cookiemanager.decimalValue - (cookiemanager.decimalValue % 1);
             
             cookiemanager.addCookies((int) rem);
             cookiemanager.decimalValue -= rem;
             
         }
     }
     
     private void handleAchievements()
     {
         new Thread(new AchievementThread(cookiemanager)).start();
     }
     
     public CookieClickerMain()
     {
         Helper.registerHelpers();
     }
     
     private static boolean downloadIcon()
     {
         File cookie = new File("cookie.png");
         File cookieSmall = new File("cookie_small.png");
         File achievementBack = new File("AchievementBackground.png");
         File mainBack = new File("BackgroundNew.png");
         
         if (!cookie.exists() || !cookieSmall.exists() || !achievementBack.exists() || !mainBack.exists())
         {
             vapi.downloadNewVersion(vapi.getNewestVersion(), "./");
             return true;
         }
         else
         {
             return false;
         }
     }
     
     @Override
     public void contentLengthAvialable(Long arg0)
     {
     }
     
     @Override
     public void downloadComplete()
     {
         start();
         doUselessLoop = false;
     }
 }
