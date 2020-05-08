 /**
  * 
  */
 package uk.org.vacuumtube.hibernate;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.FetchMode;
 import org.hibernate.Hibernate;
 import org.hibernate.HibernateException;
 import org.hibernate.LockMode;
 import org.hibernate.LockOptions;
 import org.hibernate.Session.LockRequest;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.context.ApplicationContext;
 
 import uk.org.vacuumtube.dao.Notes;
 import uk.org.vacuumtube.dao.Persistable;
 import uk.org.vacuumtube.dao.Stats;
 import uk.org.vacuumtube.dao.StatsDao;
 import uk.org.vacuumtube.exception.InfrastructureException;
 
 /**
  * @author clivem
  *
  */
 public class StatsDaoImpl extends HibernateDaoImpl implements StatsDao {
 
 	private final static Logger LOGGER = Logger.getLogger(StatsDaoImpl.class);
 	
 	/**
 	 * 
 	 */
	public StatsDaoImpl() throws InfrastructureException {
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.dao.StatsDao#addNote(uk.org.vacuumtube.dao.Notes)
 	 *
 	@Override
 	public void addNote(Notes note) throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("addNote(note=" + note + ")");
 		}
 		super.makePersistent(note);
 	}
 	*/
 
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.dao.StatsDao#entityToString(uk.org.vacuumtube.dao.Persistable)
 	 */
 	@Override
 	public String entityToString(Persistable persistable) throws InfrastructureException {
 		try {
 			/*
 			 * Create a dummy non-locking lock request to re-attach object to the session
 			 */
 			LockRequest lockReq = getSession().buildLockRequest(new LockOptions(LockMode.NONE));
 			lockReq.lock(persistable);
 			return persistable.toString();
 		} catch (HibernateException he) {
 			throw new InfrastructureException(he);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.dao.StatsDao#addNote(uk.org.vacuumtube.dao.Stats, uk.org.vacuumtube.dao.Notes)
 	 */
 	@Override
 	public Notes addNoteToStat(Stats stats, String note) throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("addNote(stats=" + stats.shortDescription() + ", note=" + note + ")");
 		}
 
 		Notes notes = stats.addNote(note);
 		makePersistent(notes);
 		return notes;
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.routeros.spring.dao.StatsDao#add(uk.org.vacuumtube.routeros.spring.dao.Stats)
 	 */
 	@Override
 	public Long add(Stats stats) throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("add(stats=" + stats.shortDescription() + ")");
 		}
 		/*
 		try {
 			Long id = (Long) getSession().save(stats);
 			return id;
 		} catch (HibernateException he) {
 			throw new InfrastructureException(he);
 		}
 		*/
 		return (Long) super.save(stats);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.routeros.spring.dao.StatsDao#delete(uk.org.vacuumtube.routeros.Stats)
 	 */
 	@Override
 	public void delete(Stats stats) throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("delete(stats=" + stats.shortDescription() + ")");
 		}
 		
 		super.makeTransient(stats);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.routeros.spring.dao.StatsDao#update(uk.org.vacuumtube.routeros.Stats)
 	 */
 	@Override
 	public void update(Stats stats) throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("update(stats=" + stats.shortDescription() + ")");
 		}
 		
 		super.update(stats);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.dao.StatsDao#merge(uk.org.vacuumtube.dao.Stats)
 	 */
 	@Override
 	public Stats merge(Stats stats) throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("merge(stats=" + stats.shortDescription() + ")");
 		}
 		
 		return (Stats) super.merge(stats);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.routeros.spring.dao.StatsDao#getStats(long)
 	 */
 	@Override
 	public Stats getStatsById(long id) throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("getStats(id=" + id + ")");
 		}
 		
 		return getStatsById(id, false);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.dao.StatsDao#getStatsById(long, boolean)
 	 */
 	@Override
 	public Stats getStatsById(long id, boolean lazy)
 			throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("getStats(id=" + id + ", lazy=" + lazy + ")");
 		}
 
 		Stats stats = null;
 		try {
 			/*
 			if (!lazy) {
 				getSession().enableFetchProfile("stats-with-notes");
 			}
 			stats = (Stats) getSession().get(Stats.class, id);
 			*/
 			
 			Criteria criteria = getSession().createCriteria(Stats.class)
 					.add(Restrictions.idEq(id));
 			
 			if (!lazy) {
 				criteria = criteria.setFetchMode("notes", FetchMode.JOIN);
 			}
 			stats = (Stats) criteria.uniqueResult();
 		} catch (HibernateException he) {
 			throw new InfrastructureException(he);
 		}
 		return stats;
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.routeros.spring.dao.StatsDao#getCount()
 	 */
 	@Override
 	public int getCount() throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("getCount()");
 		}
 		
 		try {
 			Long count = (Long) getSession()
 					.createQuery("select count(*) from Stats")
 					.uniqueResult();
 			return count.intValue();
 		} catch (HibernateException he) {
 			throw new InfrastructureException(he);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.routeros.spring.dao.StatsDao#getStatsList()
 	 */
 	@Override
 	public List<Stats> getStatsList() throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("getStatsList()");
 		}
 
 		return getStatsList(false);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.org.vacuumtube.dao.StatsDao#getStatsList(boolean)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Stats> getStatsList(boolean lazy)
 			throws InfrastructureException {
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("getStatsList(lazy=" + lazy + ")");
 		}
 		
 		List<Stats> statsList = null;
 		try {
 			/*
 			Criteria criteria = getSession().createCriteria(Stats.class)
 					.addOrder(Order.asc("id"));
 			
 			if (!lazy) {
 				criteria = criteria.setFetchMode("notes", FetchMode.JOIN)
 						.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			}
 
 			statsList = criteria.list();
 			*/
 			String hql = null;
 			if (!lazy) {
 				hql = "select DISTINCT stats from Stats stats left join fetch stats.notes ORDER BY stats.id";
 			} else {
 				hql = "from Stats stats ORDER BY stats.id";
 			}
 			statsList = getSession().createQuery(hql).list();
 		} catch (HibernateException he) {
 			throw new InfrastructureException(he);
 		}
 		return statsList;
 	}
 
 	/**
 	 * @param stats
 	 */
 	private void eagerLoadNotesCollection(Stats stats) {
 		if (stats != null) {
 			Hibernate.initialize(stats.getNotes());
 		}
 	}
 	
 	/**
 	 * @param statsList
 	 */
 	@SuppressWarnings("unused")
 	private void eagerLoadNotesCollection(Collection<Stats> statsList) {
 		for (Stats stats : statsList) {
 			eagerLoadNotesCollection(stats);
 		}
 	}
 	
 	/**
 	 * @param ctx
 	 * @return
 	 */
 	public final static StatsDao getStatsDao(ApplicationContext ctx) {
 		return ctx.getBean("hibernateStatsDao", StatsDaoImpl.class);
 	}
 }
