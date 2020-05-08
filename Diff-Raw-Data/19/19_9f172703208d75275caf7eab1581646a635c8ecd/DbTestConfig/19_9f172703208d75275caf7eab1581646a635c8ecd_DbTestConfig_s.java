 package com.adaptiweb.tools.dbtest;
 
 import static java.lang.String.format;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import com.adaptiweb.tools.JndiUtils;
 
 
 /**
  * Utility for runs multiple executions with provided {@link EntityManager}. All executions used same
  * {@link EntityManagerFactory}, each one has own {@link EntityManager} and can be run in transaction (default).
  */
 public class DbTestConfig {
 
 	public interface Execution {
 
 		void execute(EntityManager em);
 	}
 
 	private final static class Record implements Execution, Comparable<Record> {
 
 		private final Execution execution;
 		private final boolean useTransaction;
 		private final int order;
 
 		Record(Execution execution, boolean useTransaction, int order) {
 			this.execution = execution;
 			this.useTransaction = useTransaction;
 			this.order = order;
 		}
 
 		public Record(final Object obj, final Method method) {
 			this(new Execution() {
 
 				public void execute(EntityManager em) {
 					invokeMethod(obj, method, em);
 				}
 
 			}, method.getAnnotation(DbTestExecution.class).useTransaction(), method.getAnnotation(DbTestExecution.class).order());
 		}
 
 		public void execute(EntityManager em) {
 			execution.execute(em);
 		}
 
 		public boolean useTransaction() {
 			return useTransaction;
 		}
 
 		public int compareTo(Record other) {
 			return this.order > other.order ? 1 : this.order < other.order ? -1 : 0;
 		}
 	}
 
 	private final String persistenceUnit;
 	private final SortedSet<Record> records = new TreeSet<Record>();
 
 	private DbTestConfig(String persistenceUnit) {
 		this.persistenceUnit = persistenceUnit;
 	}
 
 	public static DbTestConfig create(String persistenceUnit) {
 		return new DbTestConfig(persistenceUnit);
 	}
 
 	public DbTestConfig add(Execution execution, boolean withTransaction) {
 		records.add(new Record(execution, withTransaction, records.size()));
 		return this;
 	}
 
 	public void execute() {
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit);
 		try {
 			for (Record record : records)
 				execute(record, emf);
 		} finally {
 			emf.close();
 		}
 	}
 
 	private void execute(Record record, EntityManagerFactory emf) {
 		EntityManager em = emf.createEntityManager();
 		boolean useTransaction = record.useTransaction();
 		try {
 			if (useTransaction) em.getTransaction().begin();
 			record.execute(em);
 			if (useTransaction) em.getTransaction().commit();
 		} catch (RuntimeException e) {
 			if (useTransaction && em.getTransaction().isActive()) em.getTransaction().rollback();
			throw e;
 		} finally {
 			em.close();
 		}
 	}
 
 	/**
 	 * Create instance from object defined by annotated class.
 	 * 
 	 * @param dbTestInstance instance defined database test by using annotations {@link DbTestConnection} and
 	 * {@link DbTestExecution}.
 	 * @return executable instance.
 	 */
 	public static DbTestConfig create(Object dbTestInstance) {
 		Class<? extends Object> type = dbTestInstance.getClass();
 
 		if (type.isAnnotationPresent(DbTestJNDIDataSource.class)) JndiUtils.setUpJNDIDataSource(type);
 
 		//TODO DbTestConnection can be used for method which create EntityManagerFactory
 		String unit = type.getAnnotation(DbTestConnection.class).persistenceUnit();
 		DbTestConfig result = create(unit);
 
 		for (Method method : type.getDeclaredMethods())
 			if (isDbTestExecution(method)) {
 				Record record = new Record(dbTestInstance, method);
 				if (!result.records.contains(record)) result.records.add(record);
 				else throw new IllegalArgumentException(duplicateOrderMsg(method));
 			}
 		return result;
 	}
 
 	private static boolean isDbTestExecution(Method method) {
 		if (!method.isAnnotationPresent(DbTestExecution.class)) return false;
 		Class<?>[] parameterTypes = method.getParameterTypes();
 		if (parameterTypes.length != 1 || !EntityManager.class.isAssignableFrom(parameterTypes[0])) throw new IllegalArgumentException(wrongArgumentMsg(method));
 		return true;
 	}
 
 	private static String wrongArgumentMsg(Method method) {
 		return format("Method %s must have one parameter of type %s!", method.getName(), EntityManager.class.getName());
 	}
 
 	private static String duplicateOrderMsg(Method method) {
 		int order = method.getAnnotation(DbTestExecution.class).order();
 
 		return format("Duplicate order (%d) for %s and %s!", order, method);
 	}
 
 	private static void invokeMethod(Object obj, Method method, EntityManager em) {
 		try {
 			method.invoke(obj, em);
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (InvocationTargetException e) {
 			throw e.getCause() instanceof RuntimeException ?
 				(RuntimeException) e.getCause() :
 				new RuntimeException(e.getCause());
 		}
 	}
 }
