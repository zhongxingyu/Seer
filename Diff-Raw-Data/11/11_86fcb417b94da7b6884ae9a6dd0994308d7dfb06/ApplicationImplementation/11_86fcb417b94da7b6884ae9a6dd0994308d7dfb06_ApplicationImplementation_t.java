 package ljas.application;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import ljas.application.annotations.AttachToEverySession;
 import ljas.exception.ApplicationException;
 import ljas.session.Session;
 import ljas.tasking.executors.TaskThread;
 import ljas.tasking.step.ExecutingContext;
 
 import org.apache.log4j.Logger;
 
 /**
  * Provides helper methods for the application implementation of a server
  * 
  * @author jonashansen
  * 
  */
 public abstract class ApplicationImplementation implements Application {
 
 	private Map<Session, Map<Class<?>, Object>> sessionObjects;
 	private Class<? extends Application> applicationClass;
 
 	public ApplicationImplementation(
 			Class<? extends Application> applicationClass) {
 		this.applicationClass = applicationClass;
 		sessionObjects = new HashMap<>();
 	}
 
 	@Override
 	public void onSessionConnect(Session session, LoginParameters parameters)
 			throws ApplicationException {
 		if (ApplicationAnalyzer
 				.getAttachToEverySessionAnnotation(applicationClass) != null) {
 			addSessionObjects(session);
 		}
 	}
 
 	@Override
 	public void onSessionDisconnect(Session session)
 			throws ApplicationException {
 		sessionObjects.remove(session);
 	}
 
 	/**
 	 * 
 	 * @return The current {@link ExecutingContext} of the active thread. Fetch
 	 *         the ExecutingContext to gather information about the surrounding
 	 *         environment in an implementation of an application method.<br/>
 	 *         Examples:
 	 *         <ul>
 	 *         <li>Session of client which is currently executing the method</li>
 	 *         <li>TaskSystem of the Server</li>
 	 *         </ul>
 	 */
 	protected ExecutingContext getExecutingContext() {
 		TaskThread thread = (TaskThread) Thread.currentThread();
 		return thread.getExecutingContext();
 	}
 
	protected <V> V getSessionObject(Class<V> clazz)
 			throws ApplicationException {
 		Session session = getExecutingContext().getSenderSession();
 		return getSessionObject(session, clazz);
 	}
 
 	protected <V> V getSessionObject(Session session, Class<V> clazz)
 			throws ApplicationException {
 		if (!ApplicationAnalyzer.hasSessionObject(applicationClass, clazz)) {
 			throw new ApplicationException("The object type " + clazz
 					+ " has not been annotated and attached do a session");
 		}
 
 		return (V) sessionObjects.get(session).get(clazz);
 	}
 
 	protected <V> Collection<V> getAllSessionObjects(Class<V> clazz)
 			throws ApplicationException {
 		Collection<V> objects = new ArrayList<>();
 
 		for (Session session : sessionObjects.keySet()) {
 			V sessionObject = getSessionObject(session, clazz);
 			objects.add(sessionObject);
 		}
 
 		return objects;
 	}
 
 	protected Collection<Session> getConnectedSessions() {
 		return sessionObjects.keySet();
 	}
 
 	protected void log(Object message) {
 		getLogger().info(message);
 	}
 
 	private Logger getLogger() {
 		return Logger.getLogger(applicationClass);
 	}
 
 	private void addSessionObjects(Session session) throws ApplicationException {
 		AttachToEverySession objToAttach = ApplicationAnalyzer
 				.getAttachToEverySessionAnnotation(applicationClass);
 
 		for (Class<?> clazz : objToAttach.classes()) {
 			try {
 				Object obj = clazz.getConstructor().newInstance();
 
 				if (!sessionObjects.containsKey(session)) {
 					sessionObjects
 							.put(session, new HashMap<Class<?>, Object>());
 				}
 
 				Map<Class<?>, Object> objects = sessionObjects.get(session);
 				objects.put(clazz, obj);
 			} catch (Exception e) {
 				throw new ApplicationException(e);
 			}
 		}
 	}
 
 }
