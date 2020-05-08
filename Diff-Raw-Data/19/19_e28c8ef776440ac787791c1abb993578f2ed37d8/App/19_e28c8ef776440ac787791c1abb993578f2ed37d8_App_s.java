 package cdiddy.fantasyfootballapp;
 
 
 import cdiddy.gui.FantasyFootballFrame;
 import cdiddy.utils.application.PlayerService;
 import cdiddy.utils.application.StatsService;
 import cdiddy.utils.system.OAuthConnection;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Hello world!
  *
  */
 public class App 
 {
       private static final ApplicationContext applicationContext = 
         new ClassPathXmlApplicationContext("app-config.xml");
         private static OAuthConnection conn = applicationContext.getBean(OAuthConnection.class);
         private static PlayerService playerUtil = applicationContext.getBean(PlayerService.class);
        private static StatsService statsService = applicationContext.getBean(StatsService.class); 
     
        
        public static void main( String[] args )
     {
         if(args.length > 0 && args[0].equals("load"))
         {
            loadFreshData();
         }
         else if(args.length > 0 && args[0].equals("loadWeek") && args[1] !=null)
         {
             loadWeekData(Integer.parseInt(args[1]));
         }
         else
         {
             FantasyFootballFrame.main(args);
         }
          
     }
     public static void loadFreshData()
     {
         conn.connect();
         playerUtil.primePlayersDatabase();
         statsService.primeStatCategories();
         statsService.primeStats();
         playerUtil.loadPlayers();
         statsService.loadStatCategories();
     }
         public static void loadWeekData(int week)
     {
         conn.connect();
         playerUtil.yahooWeeklyStatsLoad(week);
     }
 }
