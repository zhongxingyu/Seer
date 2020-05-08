 package ch.cern.atlas.apvs.db;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.NullPrecedence;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Property;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.service.ServiceRegistryBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.domain.Data;
 import ch.cern.atlas.apvs.domain.Device;
 import ch.cern.atlas.apvs.domain.DeviceData;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.GeneralConfiguration;
 import ch.cern.atlas.apvs.domain.History;
 import ch.cern.atlas.apvs.domain.Intervention;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.domain.MeasurementConfiguration;
 import ch.cern.atlas.apvs.domain.Sensor;
 import ch.cern.atlas.apvs.domain.SortOrder;
 import ch.cern.atlas.apvs.domain.User;
 import ch.cern.atlas.apvs.hibernate.types.DoubleStringType;
 import ch.cern.atlas.apvs.hibernate.types.InetAddressType;
 import ch.cern.atlas.apvs.hibernate.types.IntegerStringType;
 import ch.cern.atlas.apvs.hibernate.types.MacAddressType;
 import ch.cern.atlas.apvs.util.CircularList;
 import ch.cern.atlas.apvs.util.StringUtils;
 
 public class Database {
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private static Database instance;
 
 	private Configuration configuration;
 	private ServiceRegistry serviceRegistry;
 	private SessionFactory sessionFactory;
 
 	protected Database(Configuration configuration) {
 		this.configuration = configuration;
 
 		// mapped classes
 		configuration.addAnnotatedClass(Device.class);
 		configuration.addAnnotatedClass(Event.class);
 		configuration.addAnnotatedClass(Intervention.class);
 		configuration.addAnnotatedClass(GeneralConfiguration.class);
 		configuration.addAnnotatedClass(Measurement.class);
 		configuration.addAnnotatedClass(MeasurementConfiguration.class);
 		configuration.addAnnotatedClass(Sensor.class);
 		configuration.addAnnotatedClass(User.class);
 
 		// mapped types
 		configuration.registerTypeOverride(new DoubleStringType());
 		configuration.registerTypeOverride(new IntegerStringType());
 		configuration.registerTypeOverride(new MacAddressType());
 		configuration.registerTypeOverride(new InetAddressType());
 
 		serviceRegistry = new ServiceRegistryBuilder().applySettings(
 				configuration.getProperties()).buildServiceRegistry();
 		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
 
 		// new SchemaExport(configuration).create(true, false);
 	}
 
 	public static Database getInstance() {
 		if (instance == null) {
 			instance = new Database(new Configuration().configure(new File(
 					"hibernate.cfg.xml")));
 		}
 		return instance;
 	}
 
 	public void close() {
 		sessionFactory.close();
 	}
 
 	public Configuration getConfiguration() {
 		return configuration;
 	}
 
 	public SessionFactory getSessionFactory() {
 		return sessionFactory;
 	}
 
 	public long getCount(Class<?> clazz) {
 		return getCount(clazz, null);
 	}
 
 	public long getCount(Class<?> clazz, List<Criterion> criterion) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Criteria criteria = session.createCriteria(clazz);
 			criteria.setProjection(Projections.rowCount());
 
 			if (criterion != null) {
 				for (Criterion c : criterion) {
 					criteria.add(c);
 				}
 			}
 
 			Long count = (Long) criteria.uniqueResult();
 
 			tx.commit();
 			return count;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	// Keep for iterating
 	public Query getQuery(Session session, Class<?> clazz, Integer start,
 			Integer length, List<SortOrder> order) {
 		Query query = session.createQuery(getSql("from " + clazz.getName()
 				+ " t", order));
 		if (start != null) {
 			query.setFirstResult(start);
 		}
 		if (length != null) {
 			query.setMaxResults(length);
 		}
 		return query;
 	}
 
 	private String getSql(String sql, List<SortOrder> order) {
 		StringBuffer s = new StringBuffer(sql);
 		if (order != null) {
 			for (int i = 0; i < order.size(); i++) {
 				if (i == 0) {
 					s.append(" order by ");
 				}
 
 				s.append(order.get(i).getName());
 				s.append(" ");
 				s.append(order.get(i).isAscending() ? "ASC" : "DESC");
 				// FIX for #710
 				if (order.get(i).isNullsFirst()) {
 					s.append(" NULLS FIRST");
 				}
 				if (i + 1 < order.size()) {
 					s.append(", ");
 				}
 			}
 		}
 		return s.toString();
 	}
 
 	public <T> List<T> getList(Class<T> clazz, Integer start, Integer length,
 			List<Order> order) {
 		return getList(clazz, start, length, order, null, null);
 	}
 
 	public <T> List<T> getList(Class<T> clazz, Integer start, Integer length,
 			List<Order> order, List<Criterion> criterion, List<String> alias) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Criteria criteria = session.createCriteria(clazz);
 			if (alias != null) {
 				for (String a : alias) {
 					criteria.createAlias(a, a);
 				}
 			}
 
 			if (start != null) {
 				criteria.setFirstResult(start);
 			}
 
 			if (length != null) {
 				criteria.setMaxResults(length);
 			}
 
 			if (order != null) {
 				for (Order o : order) {
 					criteria.addOrder(o);
 				}
 			}
 
 			if (criterion != null) {
 				for (Criterion c : criterion) {
 					criteria.add(c);
 				}
 			}
 
 			@SuppressWarnings("unchecked")
 			List<T> list = criteria.list();
 
 			tx.commit();
 			return list;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public List<Device> getDevices(boolean available) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Criteria c = session.createCriteria(Device.class);
 			c.addOrder(Order.asc("name"));
 
 			if (available) {
 				// device not in Active Interventions
 				DetachedCriteria dc = DetachedCriteria
 						.forClass(Intervention.class);
 				dc.add(Restrictions.isNull("endTime"));
 				dc.setProjection(Projections.property("device.id"));
 
 				c.add(Property.forName("id").notIn(dc));
 				
				// device not virtual
//				c.add(Restrictions.ne("virtual", true));
 			}
 
 			@SuppressWarnings("unchecked")
 			List<Device> devices = c.list();
 
 			tx.commit();
 			return devices;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public Map<String, Device> getDeviceMap() {
 		Map<String, Device> devices = new HashMap<String, Device>();
 		for (Device device : getDevices(false)) {
 			devices.put(device.getName(), device);
 		}
 		return devices;
 	}
 
 	public List<String> getSensorNames(Device device) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Criteria c = session.createCriteria(Measurement.class);
 			c.add(Restrictions.eq("device", device));
 			c.setProjection(Projections.distinct(Projections.property("sensor")));
 
 			@SuppressWarnings("unchecked")
 			List<String> sensorNames = c.list();
 			log.info("Found " + sensorNames.size() + " sensor names for "
 					+ device.getName());
 
 			tx.commit();
 
 			return sensorNames;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 
 	}
 
 	public void saveOrUpdate(Object object) {
 		if (object == null) {
 			return;
 		}
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			session.saveOrUpdate(object);
 			tx.commit();
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<User> getUsers(boolean available) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Criteria c = session.createCriteria(User.class);
 			c.addOrder(Order.asc("lastName"));
 			c.addOrder(Order.asc("firstName"));
 
 			if (available) {
 				DetachedCriteria dc = DetachedCriteria
 						.forClass(Intervention.class);
 				dc.add(Restrictions.isNull("endTime"));
 				dc.setProjection(Projections.property("user.id"));
 
 				c.add(Property.forName("id").notIn(dc));
 			}
 
 			List<User> users = c.list();
 
 			tx.commit();
 
 			return users;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public SensorMap getSensorMap() throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			SensorMap sensorMap = new SensorMap();
 
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			@SuppressWarnings("unchecked")
 			List<Sensor> list = session.createCriteria(Sensor.class).list();
 			for (Iterator<Sensor> i = list.iterator(); i.hasNext();) {
 				Sensor sensor = i.next();
 				Boolean enabled = sensor.isEnabled() == null
 						|| sensor.isEnabled();
 				sensorMap.setEnabled(sensor.getDevice(), sensor.getName(),
 						enabled);
 			}
 			tx.commit();
 
 			return sensorMap;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public Intervention getIntervention(Device device)
 			throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			Intervention intervention = (Intervention) session
 					.createCriteria(Intervention.class)
 					.add(Restrictions.isNull("endTime"))
 					.add(Restrictions.eq("device", device))
 					.addOrder(Order.desc("startTime")).uniqueResult();
 			tx.commit();
 			return intervention;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public Date getLastMeasurementUpdateTime() {
 		Date time = null;
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			time = (Date) session.createCriteria(Measurement.class)
 					.setProjection(Projections.property("time"))
 					.addOrder(Order.desc("time")).setMaxResults(1)
 					.uniqueResult();
 			tx.commit();
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 		return time;
 	}
 
 	public List<Measurement> getMeasurements(Device ptu, String name)
 			throws HibernateException {
 		List<Device> ptuList = null;
 		if (ptu != null) {
 			ptuList = new ArrayList<Device>();
 			ptuList.add(ptu);
 		}
 		return getMeasurements(ptuList, name);
 	}
 
 	public List<Measurement> getMeasurements(List<Device> ptuList, String name)
 			throws HibernateException {
 
 		// FIXME could be part of the SQL
 		SensorMap sensorMap = getSensorMap();
 
 		String sql = "from Measurement m, view_last_measurements_date d "
 				+ "where d.datetime = m.time " + "and d.sensor = m.sensor "
 				+ "and d.device_id = m.device.id";
 
 		if (ptuList != null) {
 			sql += " and m.sensor in ("
 					+ StringUtils.join(ptuList.toArray(), ',', '\'') + ")";
 		}
 		if (name != null) {
 			sql += " and d.sensor = :sensor";
 		}
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Query query = session.createQuery(sql);
 			if (name != null) {
 				query.setString("sensor", name);
 			}
 
 			List<Measurement> list = new ArrayList<Measurement>();
 
 			for (@SuppressWarnings("unchecked")
 			Iterator<Measurement> i = query.iterate(); i.hasNext();) {
 				Measurement m = i.next();
 				Device device = m.getDevice();
 				String sensor = m.getSensor();
 
 				if (!sensorMap.isEnabled(device, sensor)) {
 					continue;
 				}
 
 				String unit = m.getUnit();
 				Double value = m.getValue();
 				Double low = m.getDownThreshold();
 				Double high = m.getUpThreshold();
 
 				// if equal of low higher than high, no limits to be shown
 				if (low != null && high != null
 						&& low.doubleValue() >= high.doubleValue()) {
 					low = null;
 					high = null;
 				}
 
 				// Scale down to microSievert
 				value = Scale.getValue(value, unit);
 				low = Scale.getDownThreshold(low, unit);
 				high = Scale.getUpThreshold(high, unit);
 				unit = Scale.getUnit(sensor, unit);
 
 				list.add(new Measurement(device, sensor, value, low, high,
 						unit, m.getSamplingRate(), m.getMethod(), m.getTime()));
 			}
 			tx.commit();
 			return list;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public List<Measurement> getLastMeasurements(Device device, String sensor,
 			int maxEntries) {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Criteria c = session.createCriteria(Measurement.class);
 			c.add(Restrictions.eq("device", device));
 			c.add(Restrictions.eq("sensor", sensor));
 			c.addOrder(Order.desc("time"));
 			c.setMaxResults(maxEntries);
 
 			List<Measurement> lastMeasurements = new CircularList<Measurement>(
 					maxEntries);
 			for (Object m : c.list()) {
 				lastMeasurements.add((Measurement) m);
 			}
 			log.info("Found " + lastMeasurements.size()
 					+ " last measurements for device " + device.getName()
 					+ " and sensor " + sensor);
 
 			tx.commit();
 
 			return lastMeasurements;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public Map<Device, Map<String, List<Measurement>>> getLastMeasurements(
 			int maxEntries) {
 		Map<Device, Map<String, List<Measurement>>> result = new HashMap<Device, Map<String, List<Measurement>>>();
 		for (Device device : getDevices(false)) {
 			Map<String, List<Measurement>> sensors = new HashMap<String, List<Measurement>>();
 			result.put(device, sensors);
 
 			for (String sensor : getSensorNames(device)) {
 				sensors.put(sensor,
 						getLastMeasurements(device, sensor, maxEntries));
 			}
 		}
 		return result;
 	}
 
 	public History getHistory(List<Device> devices, Date from,
 			Integer maxEntries) {
 		History history = new History();
 
 		for (Device device : devices) {
 			history.put(getDeviceData(device, from, maxEntries));
 		}
 
 		return history;
 	}
 
 	public DeviceData getDeviceData(Device device, Date from, Integer maxEntries) {
 		DeviceData deviceData = new DeviceData(device);
 
 		String sql = "from Measurement m" + " where m.device = :device";
 		if (from != null) {
 			sql += " and m.time > :time";
 		}
 		sql += " order by m.time asc";
 
 		Date now = new Date();
 
 		from = getAdjustedDate(sql, device, from, now, maxEntries);
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Query query = session.createQuery(sql);
 
 			query.setEntity("device", device);
 
 			if (from != null) {
 				query.setTimestamp("time", from);
 			}
 
 			for (@SuppressWarnings("unchecked")
 			Iterator<Measurement> i = query.list().iterator(); i.hasNext();) {
 				Measurement measurement = i.next();
 				long time = measurement.getTime().getTime();
 				if (time > now.getTime() + 60000) {
 					break;
 				}
 
 				Long id = measurement.getId();
 				String sensor = measurement.getSensor();
 				Double value = measurement.getValue();
 				String unit = measurement.getUnit();
 
 				// Fix for #488, invalid db entry
 				if ((sensor == null) || (value == null) || (unit == null)) {
 					log.warn("MeasurementTable ID " + id
 							+ " contains <null> sensor, value or unit ("
 							+ sensor + ", " + value + ", " + unit
 							+ ") for ptu: " + device.getName());
 					continue;
 				}
 
 				Double low = measurement.getDownThreshold();
 				Double high = measurement.getUpThreshold();
 
 				Integer samplingRate = measurement.getSamplingRate();
 
 				// Scale down to microSievert
 				value = Scale.getValue(value, unit);
 				low = Scale.getDownThreshold(low, unit);
 				high = Scale.getUpThreshold(high, unit);
 				unit = Scale.getUnit(sensor, unit);
 
 				// if (!sensorMap.isEnabled(ptu, sensor)) {
 				// continue;
 				// }
 
 				Data data = deviceData.get(sensor);
 				if (data == null) {
 					data = new Data(device, sensor, unit, maxEntries);
 					deviceData.put(data);
 				}
 
 				if (data.addEntry(time, value, low, high, samplingRate)) {
 					// total++;
 				}
 			}
 
 			tx.commit();
 
 			return deviceData;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<GeneralConfiguration> getGeneralConfigurationList() {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Query query = session.createQuery("from GeneralConfiguration"
 					+ " where (device,time) in"
 					+ " (select device,max(time)"
 					+ " from GeneralConfiguration"
 					+ " group by device)");
 			return query.list();
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 
 	@SuppressWarnings("unchecked")
 	public List<MeasurementConfiguration> getMeasurementConfigurationList() {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 
 			Query query = session.createQuery("from MeasurementConfiguration"
 					+ " where (device,sensor,time) in"
 					+ " (select device,sensor,max(time)"
 					+ " from MeasurementConfiguration"
 					+ " group by device, sensor)");
 			return query.list();
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	// NOTE #705: we could improve by binary search... rather then just half and
 	// see if we get fewer entries.
 	private Date getAdjustedDate(String sql, Device device, Date from,
 			Date until, int maxEntries) {
 		if (from == null) {
 			return null;
 		}
 
 		Session session = null;
 		Transaction tx = null;
 
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			Query count = session.createQuery("select count(*) " + sql);
 
 			count.setEntity("device", device);
 
 			long entries;
 			do {
 				count.setTimestamp("time", from);
 
 				entries = (Long) count.uniqueResult();
 				// System.err.println("Entries " + entries+" "+from);
 
 				if (entries > maxEntries) {
 					from = new Date((from.getTime() + until.getTime()) / 2);
 				}
 
 			} while (entries > maxEntries);
 			tx.commit();
 
 			return from;
 		} catch (HibernateException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			throw e;
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	public static List<Order> getOrder(List<SortOrder> sortOrder) {
 		if (sortOrder == null)
 			return null;
 
 		List<Order> order = new ArrayList<Order>(sortOrder.size());
 		for (SortOrder o : sortOrder) {
 			String name = o.getName();
 			NullPrecedence precedence = o.isNullsFirst() ? NullPrecedence.FIRST
 					: NullPrecedence.LAST;
 			order.add(o.isAscending() ? Order.asc(name).nulls(precedence)
 					: Order.desc(name).nulls(precedence));
 		}
 		return order;
 	}
 
 }
