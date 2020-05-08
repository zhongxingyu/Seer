 /**
  * 
  */
 package fr.ultimate.breakfast.web.services;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.sql.DataSource;
 
 import org.apache.tapestry5.SymbolConstants;
 import org.apache.tapestry5.ioc.annotations.EagerLoad;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.ioc.annotations.Symbol;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 
 import fr.ultimate.breakfast.common.exception.BusinessException;
 import fr.ultimate.breakfast.domain.business.BreakfastManager;
 import fr.ultimate.breakfast.domain.model.Eater;
 import fr.ultimate.breakfast.domain.model.Team;
 
 /**
  * Classe chargée automatiquement au démarrage de l'application.
  * 
  * @author lguerin
  */
 @EagerLoad
 public class ApplicationListener
 {
     /**
      * Logger de la classe
      */
     private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationListener.class);
 
     /**
      * Le formatteur de date / heure
      */
     private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
 
     /**
      * Constructeur.
      * 
      * @param context Contexte Spring
      * @param productionMode Mode Tapestry (PRODUCTION ou DEVELOPPEMENT)
      * @throws SQLException
      */
     public ApplicationListener(ApplicationContext context, @Inject
     @Symbol(SymbolConstants.PRODUCTION_MODE)
     boolean productionMode) throws SQLException
     {
         if (productionMode)
         {
             LOGGER.info("Demarrage de l'application en mode PRODUCTION.");
             return;
         }
 
         /**
          * En mode DEVELOPPEMENT, permet d'initialiser un jeu de données par défaut.
          */
         LOGGER.info("Demarrage de l'application en mode DEVELOPPEMENT.");
 
         cleanUpAll(context);
         try
         {
             loadUser(context);
         }
         catch (BusinessException e)
         {
             LOGGER.error(">> Erreur fatale : impossible de charger la liste d'utilisateurs: " + e);
             System.exit(1);
         }
         catch (ParseException e)
         {
             LOGGER.error(">> Erreur fatale : impossible de parser les dates: " + e);
             System.exit(1);
         }
     }
 
     private void cleanUpAll(ApplicationContext context) throws SQLException
     {
         LOGGER.info(">>> Nettoyage USER / ROLE / AUTHORITY...");
 
         Connection c = pullConnection(context);
         Statement statement = null;
         try
         {
             statement = c.createStatement();
             statement.addBatch("DELETE FROM Eater WHERE id >= 0");
             statement.addBatch("DELETE FROM Team WHERE id >= 0");
             statement.executeBatch();
             c.commit();
         }
         finally
         {
             statement.close();
             c.close();
         }
         LOGGER.info("<<< Nettoyage : traitement termine");
     }
 
     private void loadUser(ApplicationContext context) throws SQLException, BusinessException, ParseException
     {
         LOGGER.info(">>> Users : Chargement du jeu de donnees par defaut...");
 
         BreakfastManager breakfastManager = (BreakfastManager) context.getBean(BreakfastManager.class);
         Team team = new Team();
         team.setName("Team");
         // enable to test
         // team.setEmail("email@ultimate-breakfast.fr");
         team.setPassword("te.am");
         team.setLastNotification(new Date());
 
         team = breakfastManager.create(team);
 
         Eater eat = new Eater();
         eat.setName("eater1");
         eat.setPosition(3);
         eat.setTeam(team);
         eat.setEmail("eater1@ultimate-breakfast.fr");
         eat.setLastUpdate(new SimpleDateFormat("dd/MM/yyyy").parse("20/01/2014"));
 
         Eater eat2 = new Eater();
         eat2.setName("eater2");
         eat2.setPosition(4);
         eat2.setTeam(team);
         eat2.setLastUpdate(new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2013"));
 
         Eater eat3 = new Eater();
         eat3.setName("eater3");
         eat3.setPosition(5);
         eat3.setTeam(team);
         eat3.setLastUpdate(new SimpleDateFormat("dd/MM/yyyy").parse("19/01/2012"));
 
         team.getEaters().add(eat);
         team.getEaters().add(eat2);
         team.getEaters().add(eat3);
 
         breakfastManager.update(team);
 
         // team "vide"
         Team team2 = new Team();
         team2.setName("Team2");
         // enable to test
         // team2.setEmail("email@@ultimate-breakfast.fr");
         team2.setPassword("te.am2");
 
         team = breakfastManager.create(team2);
 
         Team admin = new Team();
         admin.setName("admin");
         // enable to test
        // admin.setEmail("email@@ultimate-breakfast.fr");
         admin.setPassword("adminmempass");
 
         team = breakfastManager.create(admin);
     }
 
     private Connection pullConnection(ApplicationContext context) throws SQLException
     {
         return pullDataSource(context).getConnection();
     }
 
     private DataSource pullDataSource(ApplicationContext context)
     {
         return (DataSource) context.getBean("dataSource");
     }
 }
