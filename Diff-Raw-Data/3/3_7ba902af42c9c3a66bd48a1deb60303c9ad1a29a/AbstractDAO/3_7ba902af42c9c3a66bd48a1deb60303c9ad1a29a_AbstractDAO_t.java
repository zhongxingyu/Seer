 package org.myftp.gattserver.grass3.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBException;
 
 import org.h2.Driver;
 import org.hibernate.Criteria;
 import org.hibernate.Hibernate;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.SimpleExpression;
 import org.hibernate.dialect.H2Dialect;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.service.ServiceRegistryBuilder;
 import org.myftp.gattserver.grass3.config.ConfigurationUtils;
 import org.myftp.gattserver.grass3.model.config.ModelConfiguration;
 import org.myftp.gattserver.grass3.model.service.IEntityServiceListener;
 import org.myftp.gattserver.grass3.model.service.IEntityService;
 
 /**
  * Abstraktní třída DAO tříd na získávání Entit z databáze nebo přes existující
  * entity.
  * 
  * @author gatt
  * 
  * @param <E>
  *            Entity class
  */
 public abstract class AbstractDAO<E> {
 
 	/**
 	 * Připojení na dynamické získávání definic objektů odtud získám vždycky
 	 * aktuálně přehled tříd, které mají být persistovány
 	 */
 	public static IEntityServiceListener serviceListener;
 
 	/**
 	 * Verze "sestavení" entit. Došlo k nějaké změně ? Má se přegenerovat
 	 * SessionFactory ?
 	 */
 	private Long version = 0L;
 
 	/**
 	 * Hibernate {@link Configuration}
 	 */
 	private static Configuration configuration;
 
 	/**
 	 * Hibernate {@link SessionFactory}
 	 */
 	private static SessionFactory sessionFactory;
 
 	/**
 	 * Aktuálně používaná {@link Session}
 	 */
 	protected Session session;
 
 	/**
 	 * Třída entity - tedy doménového objektu
 	 */
 	protected Class<?> entityClass;
 
 	/**
 	 * Získá aktuální konfiguraci ze souboru konfigurace
 	 * 
 	 * @return soubor konfigurace FM
 	 * @throws JAXBException
 	 */
 	private ModelConfiguration loadConfiguration() throws JAXBException {
 		return new ConfigurationUtils<ModelConfiguration>(
 				new ModelConfiguration(), ModelConfiguration.CONFIG_PATH)
 				.loadExistingOrCreateNewConfiguration();
 	}
 
 	// TODO
 	private void log(String msg) {
 		System.out.println(msg);
 	}
 
 	private synchronized boolean isNewVersionOfDomainModel() {
 		if (serviceListener.getVersion() == version)
 			return false;
 		else {
 			version++;
 			return true;
 		}
 	}
 
 	/**
 	 * Tato metoda by měla být volána při každém vytváření jakéhokoliv DAO -
 	 * získá pro něj informace od Hibernatu pro připojení a práci se session a
 	 * zařídí zavedení mappingu do Dozer
 	 * 
 	 * Je jasné, že když začnu pracovat s DB, tak potřebuji, abych měl aktuální
 	 * nastavení ... to sice nemusím ověřovat při každém vytvoření DAO, ale
 	 * určitě to musím udělat při každém zaregistrování nové třídy
 	 */
 	private void createConnection() {
 
 		/**
 		 * Pokud nebyly nahlášeny ani odhlášeny žádné třídy, tak neměň staré
 		 * hodnoty ať ušetříš čas vytváření SessionFactory apod.
 		 */
 		if (!isNewVersionOfDomainModel())
 			return;
 
 		/**
 		 * Získej konfigurace
 		 */
 		ModelConfiguration modelConfiguration = null;
 		try {
 			modelConfiguration = loadConfiguration();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 
 		/**
 		 * Budeš updatovat hodnoty, pokud už jsou nějaké staré hodonoty, tak je
 		 * nejdříve uzavři - například sessionFactory se musí zavolat close()
 		 */
 		if (sessionFactory != null && sessionFactory.isClosed() == false)
 			sessionFactory.close();
 
 		configuration = new Configuration();
 
 		configuration.setProperty("hibernate.connection.driver_class",
 				Driver.class.getName());
 		configuration.setProperty("hibernate.connection.url",
 				modelConfiguration.getURL());
 		configuration.setProperty("hibernate.connection.username",
 				modelConfiguration.getUsername());
 		configuration.setProperty("hibernate.connection.password",
 				modelConfiguration.getPassword());
 		configuration.setProperty("hibernate.connection.pool_size", "1");
 		configuration.setProperty("hibernate.dialect",
 				H2Dialect.class.getName());
 		configuration.setProperty("hibernate.current_session_context_class",
 				"thread");
 		// configuration.setProperty("cache.provider_class",
 		// org.hibernate.cache.internaNl.NoCacheProvider.);
 		configuration.setProperty("hibernate.show_sql", "false");
 		configuration.setProperty("hibernate.hbm2ddl.auto", "update");
 
 		// snad tohle pomůže
 		// UPDATE: v čem ?
 		// configuration.setProperty("hibernate.generate_statistics", "false");
 
 		/**
 		 * Zde se přidávají třídy, které budou persistovány
 		 */
 		List<IEntityService> services = serviceListener.getServices();
 		synchronized (services) {
 			for (IEntityService service : services) {
 				for (Class<?> entityClass : service.getDomainClasses()) {
 					configuration.addAnnotatedClass(entityClass);
 					// log("addAnnotatedClass " + entityClass.getName()
 					// + " from classloader "
 					// + entityClass.getClassLoader() + " registred");
 				}
 			}
 		}
 
 		ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder();
 		serviceRegistryBuilder.applySettings(configuration.getProperties());
 		ServiceRegistry serviceRegistry = serviceRegistryBuilder
 				.buildServiceRegistry();
 		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
 
 	}
 
 	public AbstractDAO(Class<?> entityClass) {
 		this.entityClass = entityClass;
 
 		/**
 		 * při každém vytvoření se musí tohle provést aby se zaktualizovali
 		 * údaje
 		 */
 		createConnection();
 	}
 
 	protected final void openSession() {
 		session = sessionFactory.openSession();
 	}
 
 	protected final void closeSession() {
 		session.close();
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<E> findAllAndCast(Class<?> entityClass) {
 		return (List<E>) session.createCriteria(entityClass)
 				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
 	}
 
 	/**
 	 * Najde všechny objekty a vrátí jejich list
 	 * 
 	 * @return list všech nalezených objektů
 	 */
 	public final List<E> findAll() {
 		Transaction tx = null;
 		List<E> list = null;
 		openSession();
 		try {
 			tx = session.beginTransaction();
 			list = findAllAndCast(entityClass);
 			tx.commit();
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (tx != null) {
 				tx.rollback();
 			}
 			return null;
 		} finally {
 			closeSession();
 		}
 		return list;
 	}
 
 	@SuppressWarnings("unchecked")
 	private E findByIdAndCast(Class<?> entityClass, Serializable id) {
 		return (E) session.load(entityClass, id);
 	}
 
 	/**
 	 * Získá objekt dle jeho ID
 	 * 
 	 * @param id
 	 *            identifikátor, dle kterého je možné jednoznačně objekt určit
 	 * @return hledaný objekt
 	 */
 	public final E findByID(Serializable id) {
 		Transaction tx = null;
 		E entity = null;
 		openSession();
 		try {
 			tx = session.beginTransaction();
 			entity = findByIdAndCast(entityClass, id);
 			Hibernate.initialize(entity);
 			tx.commit();
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (tx != null) {
 				tx.rollback();
 			}
 			return null;
 		} finally {
 			closeSession();
 		}
 		return entity;
 	}
 
 	/**
 	 * Získá objekt dle jeho zadaných pravidel
 	 * 
 	 * @param expression
 	 *            {@link SimpleExpression} výraz dle kterého se bude hledat
 	 * 
 	 * @return list hledaných objektů
 	 */
 	@SuppressWarnings("unchecked")
 	public final List<E> findByRestriction(Criterion criterion, Order order,
 			Integer maxResults) {
 		Transaction tx = null;
 		List<E> list = null;
 		openSession();
 		try {
 			tx = session.beginTransaction();
 			Criteria criteria = session.createCriteria(entityClass);
 			if (maxResults != null)
 				criteria.setMaxResults(maxResults);
 			if (criterion != null)
 				criteria.add(criterion);
 			if (order != null)
 				criteria.addOrder(order);
			list = (List<E>) criteria.setResultTransformer(
					Criteria.DISTINCT_ROOT_ENTITY).list();
 			tx.commit();
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (tx != null) {
 				tx.rollback();
 			}
 			return null;
 		} finally {
 			closeSession();
 		}
 		return list;
 	}
 
 	/**
 	 * Uloží objekt
 	 * 
 	 * @param entity
 	 *            objekt, který bude uložen
 	 * @return id objekt, kterým je uložený objekt identifikován
 	 */
 	public final Object save(E entity) {
 		Transaction tx = null;
 		Object id = null;
 		openSession();
 		try {
 			tx = session.beginTransaction();
 			id = session.save(entity);
 			tx.commit();
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (tx != null) {
 				tx.rollback();
 			}
 			return null;
 		} finally {
 			closeSession();
 		}
 		return id;
 	}
 
 	/**
 	 * Uloží skupinu objektů
 	 * 
 	 * @param entityList
 	 *            list objektů, které budou uloženy
 	 * @return ids list objektů, kterými jsou uložené objekty identifikovány
 	 */
 	public final List<Object> save(List<E> entityList) {
 		List<Object> ids = new ArrayList<Object>();
 		Transaction tx = null;
 		openSession();
 		try {
 			tx = session.beginTransaction();
 			for (E entity : entityList) {
 				ids.add(session.save(entity));
 			}
 			tx.commit();
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (tx != null) {
 				tx.rollback();
 			}
 			return null;
 		} finally {
 			closeSession();
 		}
 		return ids;
 	}
 
 	/**
 	 * Updatuje objekt
 	 * 
 	 * @param entity
 	 *            objekt, který bude uložen
 	 */
 	public final boolean merge(E entity) {
 		Transaction tx = null;
 		openSession();
 		try {
 			tx = session.beginTransaction();
 			session.merge(entity);
 			tx.commit();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (tx != null) {
 				tx.rollback();
 			}
 			return false;
 		} finally {
 			closeSession();
 		}
 	}
 
 	/**
 	 * Updatuje skupinu objektů
 	 * 
 	 * @param entityList
 	 *            list objektů, které budou updatovány
 	 */
 	public final boolean merge(List<E> entityList) {
 		Transaction tx = null;
 		openSession();
 		try {
 			tx = session.beginTransaction();
 			for (E entity : entityList) {
 				session.merge(entity);
 			}
 			tx.commit();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (tx != null) {
 				tx.rollback();
 			}
 			return false;
 		} finally {
 			closeSession();
 		}
 	}
 
 	/**
 	 * Smaže objekt
 	 * 
 	 * @param entity
 	 *            objekt, který bude smazán
 	 */
 	public final boolean delete(E entity) {
 		Transaction tx = null;
 		openSession();
 		try {
 			tx = session.beginTransaction();
 			session.delete(entity);
 			tx.commit();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (tx != null) {
 				tx.rollback();
 			}
 			return false;
 		} finally {
 			closeSession();
 		}
 	}
 
 }
