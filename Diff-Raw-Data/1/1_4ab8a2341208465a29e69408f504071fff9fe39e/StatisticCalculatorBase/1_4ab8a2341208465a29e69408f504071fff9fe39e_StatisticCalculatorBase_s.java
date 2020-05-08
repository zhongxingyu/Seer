 package swag49.statistics;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.transaction.annotation.Transactional;
 import swag49.dao.DataAccessObject;
 import swag49.model.Player;
 import swag49.model.Statistic;
 import swag49.model.StatisticEntry;
 import swag49.util.Log;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import java.util.Collection;
 
 public abstract class StatisticCalculatorBase implements StatisticCalculator {
 
     @Log
     private Logger log;
 
     @PersistenceContext
     private EntityManager em;
 
     @Autowired
     @Qualifier("statisticDAO")
     private DataAccessObject<Statistic, Long> statisticDAO;
 
     @Autowired
     @Qualifier("statisticEntryDAO")
     private DataAccessObject<StatisticEntry, StatisticEntry.Id> statisticEntryDAO;
 
 //    @Value("$processing{statistic.limit}")
     private Integer limit = 5;
 
     @Override
     @Transactional("swag49.map")
     public void calculate() {
         log.info("Calculation of {} started (limit: {}).", getStatisticName(), limit);
 
         Statistic statistic = new Statistic();
         statistic.setName(getStatisticName());
        statistic.setEntries(null);
         Collection<Statistic> statistics = statisticDAO.queryByExample(statistic);
         assert statistics.size() <= 1;
         if (statistics.size() == 1)
             statistic = statistics.toArray(new Statistic[statistics.size()])[0];
         else
             statistic = statisticDAO.create(statistic);
 
         String queryString = getRankedPlayersQuery();
         TypedQuery<Player> query = em.createQuery(queryString, Player.class);
         query.setMaxResults(limit);
 
         statistic.getEntries().clear();
         int ranking = 1;
         for (Player player : query.getResultList()) {
             StatisticEntry entry = new StatisticEntry(statistic, ranking);
             entry.setPlayer(player);
             entry.setScore(getScore(player));
             statisticEntryDAO.create(entry);
             ranking++;
         }
 
         statisticDAO.update(statistic);
 
         log.info("Calculation of {} finished (limit: {}).", getStatisticName(), limit);
     }
 
     protected abstract String getRankedPlayersQuery();
 
     protected abstract int getScore(Player player);
 
     protected abstract String getStatisticName();
 }
