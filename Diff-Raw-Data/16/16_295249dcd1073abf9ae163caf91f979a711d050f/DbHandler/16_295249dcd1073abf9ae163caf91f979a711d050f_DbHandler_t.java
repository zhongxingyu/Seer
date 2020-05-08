 package ch.cern.atlas.apvs.obsolete;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gwt.user.client.rpc.SerializationException;
 
 import ch.cern.atlas.apvs.db.Scale;
 import ch.cern.atlas.apvs.db.SensorMap;
 import ch.cern.atlas.apvs.domain.Data;
 import ch.cern.atlas.apvs.domain.Device;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.History;
 import ch.cern.atlas.apvs.domain.Intervention;
 import ch.cern.atlas.apvs.domain.InterventionMap;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.domain.Sensor;
 import ch.cern.atlas.apvs.domain.SortOrder;
 import ch.cern.atlas.apvs.domain.Ternary;
 import ch.cern.atlas.apvs.domain.User;
 import ch.cern.atlas.apvs.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.event.ConnectionStatusChangedRemoteEvent.ConnectionType;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 import ch.cern.atlas.apvs.server.APVSServerFactory;
 import ch.cern.atlas.apvs.util.StringUtils;
 
 public class DbHandler extends DbCallback {
 
 	private static DbHandler handler;
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	private final RemoteEventBus eventBus;
 
 	private InterventionMap interventions = new InterventionMap();
 
 	private static final boolean DEBUG = true;
 
 	private Ternary updated = Ternary.Unknown;
 
 	private long time;
 
 	private DbHandler(final RemoteEventBus eventBus) {
 		super(eventBus);
 		this.eventBus = eventBus;
 
 		time = new Date().getTime();
 
 		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				String type = event.getRequestedClassName();
 
 				if (type.equals(InterventionMapChangedRemoteEvent.class
 						.getName())) {
 					InterventionMapChangedRemoteEvent.fire(eventBus,
 							interventions);
 				} else if (type.equals(ConnectionStatusChangedRemoteEvent.class
 						.getName())) {
 					ConnectionStatusChangedRemoteEvent.fire(eventBus,
 							ConnectionType.databaseConnect, isConnected(), "");
 					ConnectionStatusChangedRemoteEvent.fire(eventBus,
 							ConnectionType.databaseUpdate, updated, "");
 				}
 			}
 		});
 
 		ScheduledExecutorService executor = Executors
 				.newSingleThreadScheduledExecutor();
 		executor.scheduleWithFixedDelay(new Runnable() {
 
 			ScheduledFuture<?> watchdog;
 
 			@Override
 			public void run() {
 				try {
 					if (isConnected()) {
 						if (!checkConnection()) {
 							log.warn("DB no longer reachable");
 						}
 						try {
 							rereadInterventions();
 							if (!checkUpdate()) {
 								log.warn("DB no longer updated");
 							}
 
 						} catch (HibernateException e) {
 							log.warn(
 									"Could not regularly-update intervention list: ",
 									e);
 						}
 
 						if (watchdog != null) {
 							watchdog.cancel(false);
 						}
 						watchdog = scheduleWatchDog();
 					}
 				} catch (Exception e) {
 					log.warn(e.getMessage());
 				}
 			}
 		}, 0, 30, TimeUnit.SECONDS);
 	}
 
 	public static DbHandler getInstance() {
 		if (handler == null) {
 			handler = new DbHandler(APVSServerFactory.getInstance()
 					.getEventBus());
 		}
 		return handler;
 	}
 
 	private ScheduledFuture<?> scheduleWatchDog() {
 		ScheduledExecutorService executor = Executors
 				.newSingleThreadScheduledExecutor();
 		return executor.schedule(new Runnable() {
 
 			@Override
 			public void run() {
 				Date now = new Date();
 				log.error("Failed to reset watchdog, terminating server at "
 						+ now + " after " + (now.getTime() - time) / 1000
 						+ " seconds.");
 				System.exit(1);
 			}
 		}, 45, TimeUnit.SECONDS);
 	}
 
 	public History getHistoryMap(List<Device> ptuList, Date from)
 			throws HibernateException {
 
 		// FIXME could be part of the SQL
 		SensorMap sensorMap = getSensorMap();
 
 		String sql = "from Measurement"
 				+ " where Measurement.device.name = :name"
 				+ (from != null ? " and Measurement.date > :date" : "")
 				+ " order by Measurement.date asc";
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			History map = new History();
 			Query query = session.createQuery(sql);
 
 			for (Device ptu : ptuList) {
 				// at most from fromTime or startTime
 				long startTime = interventions.get(ptu).getStartTime()
 						.getTime();
 				long fromTime = from.getTime();
 
 				query.setString("name", ptu.getName());
 
 				if (from != null) {
 					query.setTimestamp("date",
 							new Timestamp(Math.max(startTime, fromTime)));
 				}
 
 				long now = new Date().getTime();
 				int total = 0;
 
 				for (@SuppressWarnings("unchecked")
 				Iterator<Measurement> i = query.iterate(); i.hasNext();) {
 					Measurement m = i.next();
 					long time = m.getDate().getTime();
 					if (time > now + 60000) {
 						break;
 					}
 
 					Long id = m.getId();
 					String sensor = m.getSensor();
 					Double value = m.getValue();
 					String unit = m.getUnit();
 
 					// Fix for #488, invalid db entry
 					if ((sensor == null) || (value == null) || (unit == null)) {
 						log.warn("MeasurementTable ID " + id
 								+ " contains <null> sensor, value or unit ("
 								+ sensor + ", " + value + ", " + unit
 								+ ") for ptu: " + ptu.getName());
 						continue;
 					}
 
 					Double low = m.getLowLimit();
 					Double high = m.getHighLimit();
 
 					Integer samplingRate = m.getSamplingRate();
 
 					// Scale down to microSievert
 					value = Scale.getValue(value, unit);
 					low = Scale.getLowLimit(low, unit);
 					high = Scale.getHighLimit(high, unit);
 					unit = Scale.getUnit(sensor, unit);
 
 					if (!sensorMap.isEnabled(ptu, sensor)) {
 						continue;
 					}
 
 					Data history = map.get(ptu, sensor);
 					if (history == null) {
 
 						if ((sensor.equals("Temperature") || sensor
 								.equals("BodyTemperature")) && unit.equals("C")) {
 							unit = "&deg;C";
 						}
 
 						history = new Data(ptu, sensor, unit, 2000);
 						map.put(history);
 					}
 
 					if (history.addEntry(time, value, low, high, samplingRate)) {
 						total++;
 					}
 				}
 				if (DEBUG) {
 					System.err.println("Total entries in history for "
 							+ ptu.getName() + " " + total);
 				}
 
 			}
 
 			tx.commit();
 
 			return map;
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
 
 	@Override
 	public void dbConnected(Session session) {
 		super.dbConnected(session);
 
 		log.info("DB connected");
 
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.databaseConnect, true, "");
 
 		rereadInterventions();
 	}
 
 	@Override
 	public void dbDisconnected() {
 		super.dbDisconnected();
 
 		log.warn("DB disconnected");
 
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.databaseConnect, false, "");
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.databaseUpdate, false, "");
 
 		interventions.clear();
 		InterventionMapChangedRemoteEvent.fire(eventBus, interventions);
 	}
 
 	private boolean checkUpdate() throws HibernateException {
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 
 			String sql = "select DATETIME from Measurement order by DATETIME DESC";
 
 			long now = new Date().getTime();
 			Date lastUpdate = (Date) session.createQuery(sql).uniqueResult();
 			if (lastUpdate != null) {
 				long time = lastUpdate.getTime();
 				updated = (time > now - (3 * 60000)) ? Ternary.True
 						: Ternary.False;
 			} else {
 				updated = Ternary.False;
 			}
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
 
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.databaseUpdate, updated, "");
 
 		return !updated.isFalse();
 	}
 
 	private String getSql(String sql, SortOrder[] order) {
 		StringBuffer s = new StringBuffer(sql);
 		for (int i = 0; i < order.length; i++) {
 			if (i == 0) {
 				s.append(" order by ");
 			}
 			s.append(order[i].getName());
 			s.append(" ");
 			s.append(order[i].isAscending() ? "ASC" : "DESC");
 			if (i + 1 < order.length) {
 				s.append(", ");
 			}
 		}
 		return s.toString();
 	}
 
 	public int getInterventionCount() throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			Integer count = (Integer) session.createQuery(
 					"select count(*) from Intervention").uniqueResult();
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
 
 	@SuppressWarnings("unchecked")
 	public List<Intervention> getInterventions(int start, int length,
 			SortOrder[] order) throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			List<Intervention> interventions = session
 					.createQuery(getSql("from Intervention", order))
 					.setFirstResult(start).setMaxResults(length).list();
 			tx.commit();
 			return interventions;
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
 	
 	public int getEventCount(String ptuId, String measurementName)
 			throws HibernateException {
 		String sql = "select count(*) from Event";
 		if ((ptuId != null) || (measurementName != null)) {
 			sql += " where";
 			if (ptuId != null) {
 				sql += " Event.device.name = ?";
 			}
 			if (measurementName != null) {
 				if (ptuId != null) {
 					sql += " and";
 				}
 				sql += " Event.name = ?";
 			}
 		}
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			Query query = session.createQuery(sql);
 			int param = 0;
 			if (ptuId != null) {
 				query.setString(param, ptuId);
 				param++;
 			}
 			if (measurementName != null) {
 				query.setString(param, measurementName);
 				param++;
 			}
 
 			Integer count = (Integer) query.uniqueResult();
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
 
 	@SuppressWarnings("unchecked")
 	public List<Event> getEvents(int start, int length, SortOrder[] order,
 			String ptuId, String measurementName) throws HibernateException {
 
 		String sql = "from Event";
 		if ((ptuId != null) || (measurementName != null)) {
 			sql += " where";
 			if (ptuId != null) {
 				sql += " tbl_devices.name = ?";
 			}
 			if (measurementName != null) {
 				if (ptuId != null) {
 					sql += " and";
 				}
 				sql += " tbl_events.sensor = ?";
 			}
 		}
 
 		String s = getSql(sql, order);
 		// log.info("SQL: "+s);
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			Query query = session.createQuery(s);
 			int param = 0;
 			if (ptuId != null) {
 				query.setString(param, ptuId);
 				param++;
 			}
 			if (measurementName != null) {
 				query.setString(param, measurementName);
 				param++;
 			}
 			query.setFirstResult(start);
 			query.setMaxResults(length);
 			List<Event> events = query.list();
 			tx.commit();
 			return events;
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
 
 	public synchronized void addUser(User user) throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			session.save(user);
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
 
 	public synchronized void addDevice(Device device) throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			session.save(device);
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
 
 	public synchronized void addIntervention(Intervention intervention)
 			throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			// CHECK... timestamp used to be set to current time on server
 			session.save(intervention);
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
 
 		rereadInterventions();
 	}
 
 	public synchronized void updateIntervention(Intervention intervention)
 			throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			// CHECK... endtime used to be set to current time on server
 			session.update(intervention);
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
 
 		rereadInterventions();
 	}
 
 	public List<User> getUsers(boolean notBusy) throws HibernateException {
 		return notBusy ? getNotBusyUsers() : getAllUsers();
 	}
 
 	@SuppressWarnings("unchecked")
 	private synchronized List<User> getNotBusyUsers() throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			List<User> users = session
 					.createQuery(
 							"from User u where u.id not in (select user.id from Intervention i where i.endTime is null) order by u.lastName, u.firstName")
 					.list();
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
 
 	@SuppressWarnings("unchecked")
 	private synchronized List<User> getAllUsers() throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			List<User> users = session.createQuery(
 					"from User u order by u.lastName, u.firstName").list();
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
 
 	public List<Device> getDevices(boolean notBusy) throws HibernateException {
 		return notBusy ? getNotBusyDevices() : getDevices();
 	}
 
 	@SuppressWarnings("unchecked")
 	private synchronized List<Device> getNotBusyDevices()
 			throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			List<Device> devices = session
 					.createQuery(
 							"from Device d where d.id not in (select device.id from Intervention i where i.endTime is null) order by d.name")
 					.list();
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
 
 	@SuppressWarnings("unchecked")
 	private List<Device> getDevices() throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			List<Device> devices = session.createQuery(
 					"from Device d order by d.name").list();
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
 
 	public synchronized Intervention getIntervention(String ptuId)
 			throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			Intervention intervention = (Intervention) session
 					.createQuery(
 							"from Intervention where ENDTIME is null and Device.NAME = :device order by Intervention.startTime DESC")
 					.setString("device", ptuId).uniqueResult();
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
 
 	@SuppressWarnings("unchecked")
 	private synchronized void rereadInterventions() {
 		InterventionMap newMap = new InterventionMap();
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			for (Intervention intervention : (List<Intervention>) session
 					.createQuery(
 							"from Intervention intervention where intervention.endTime is null")
 					.list()) {
 				newMap.put(intervention.getDevice(), intervention);
 			}
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
 
 		if (!interventions.equals(newMap)) {
 			interventions = newMap;
 			InterventionMapChangedRemoteEvent.fire(eventBus, interventions);
 		}
 	}
 
 	public List<Measurement> getMeasurements(String ptuId, String name)
 			throws HibernateException {
 		List<String> ptuIdList = null;
 		if (ptuId != null) {
 			ptuIdList = new ArrayList<String>();
 			ptuIdList.add(ptuId);
 		}
 		return getMeasurements(ptuIdList, name);
 	}
 
 	/**
 	 * 
 	 * @param ptuId
 	 *            can be null
 	 * @param name
 	 *            can be null
 	 * @return
 	 * @throws HibernateException
 	 */
 	public List<Measurement> getMeasurements(List<String> ptuIdList, String name)
 			throws HibernateException {
 
 		// FIXME could be part of the SQL
 		SensorMap sensorMap = getSensorMap();
 
 		String sql = "from Measurement, view_last_measurements_date "
 				+ "where view_last_measurements_date.datetime = Measurement.date "
 				+ "and view_last_measurements_date.sensor = Measurement.sensor "
 				+ "and view_last_measurements_date.device_id = Measuerement.device.id";
 
 		if (ptuIdList != null) {
 			sql += " and NAME in ("
 					+ StringUtils.join(ptuIdList.toArray(), ',', '\'') + ")";
 		}
 		if (name != null) {
 			sql += " and view_last_measurements_date.SENSOR = :sensor";
 		}
 
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = getSession();
 			tx = session.beginTransaction();
 			Query query = session.createQuery(sql);
 			if (name != null) {
 				query.setString("sensor", name);
 			}
 
 			List<Measurement> list = new ArrayList<Measurement>();
 
 			for (@SuppressWarnings("unchecked")
 			Iterator<Measurement> i = query.iterate(); i.hasNext();) {
 				Measurement m = i.next();
 
 				if (!sensorMap.isEnabled(m.getDevice(), m.getSensor())) {
 					continue;
 				}
 
 				String unit = m.getUnit();
 				Double value = m.getValue();
 				Double low = m.getLowLimit();
 				Double high = m.getHighLimit();
 
 				// if equal of low higher than high, no limits to be shown
 				if (low != null && high != null
 						&& low.doubleValue() >= high.doubleValue()) {
 					low = null;
 					high = null;
 				}
 
 				// Scale down to microSievert
 				value = Scale.getValue(value, unit);
 				low = Scale.getLowLimit(low, unit);
 				high = Scale.getHighLimit(high, unit);
 				unit = Scale.getUnit(m.getSensor(), unit);
 
 				list.add(new Measurement(m.getDevice(), m.getSensor(), value,
						low, high, unit, m.getSamplingRate(), "OneShoot", m.getDate()));
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
 
 	public SensorMap getSensorMap() throws HibernateException {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			SensorMap sensorMap = new SensorMap();
 
 			session = getSession();
 			tx = session.beginTransaction();
 			@SuppressWarnings("unchecked")
 			List<Sensor> list = session.createQuery("from Sensor").list();
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
 }
