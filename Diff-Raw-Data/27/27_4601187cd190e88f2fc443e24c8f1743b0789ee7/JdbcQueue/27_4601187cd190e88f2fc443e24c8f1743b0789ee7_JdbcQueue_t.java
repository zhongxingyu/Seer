 /*

  License:
 
  blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
  (http://www.eclipse.org/legal/epl-v10.html)
 
 
  Distribution:
 
  Repository - https://github.com/lempel/blueprint-sdk.git
  Blog - http://lempel.egloos.com
  */
 
 package blueprint.sdk.util.queue;
 
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 import java.util.UUID;
 
 import javax.sql.DataSource;
 
 /**
  * Prototype for JDBC based Queue.<br>
  * <br>
  * Must call {@link JdbcQueue#init()} before use.<br>
  * 
  * @author Sangmin Lee
  * @since 2013. 8. 26.
  */
 public abstract class JdbcQueue extends MessageQueue {
 	/** actual queue */
 	protected LinkedList<JdbcElement> queue = new LinkedList<JdbcElement>();
 
 	/** DataSource for persistence */
 	protected DataSource datasrc;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param dsrc
 	 *            DataSource for persistence
 	 */
 	public JdbcQueue(DataSource datasrc) {
 		super();
 
 		this.datasrc = datasrc;
 
 		if (datasrc == null) {
 			throw new NullPointerException("DataSource is null");
 		}
 	}
 
 	/**
 	 * create table & load queue contents from DataSource
 	 */
 	public void init() throws SQLException {
 		synchronized (queue) {
 			createTable();
 			load();
 		}
 	}
 
 	@Override
 	public void clear() throws JdbcQueueException {
 		synchronized (queue) {
 			try {
 				emptyTable();
 				queue.clear();
 			} catch (SQLException e) {
 				throw new JdbcQueueException(e);
 			}
 		}
 	}
 
 	/**
 	 * Push an persistent element to queue.
 	 * 
 	 * @param element
 	 *            Element to push
 	 */
 	@Override
 	public void push(String element) {
 		push(element, true);
 	}
 
 	/**
 	 * Push an element to queue.
 	 * 
 	 * @param element
 	 *            Element to push
 	 * @param persistent
 	 *            true: store to Database, false: don't store
 	 */
 	public void push(String element, boolean persistent) {
 		if (element == null) {
 			throw new NullPointerException("Can't push null");
 		}
 
 		JdbcElement item = new JdbcElement();
 		item.uuid = UUID.randomUUID().toString();
 		item.content = element;
 		item.persistent = persistent;
 
 		try {
 			if (persistent) {
 				insert(item);
 			}
 			synchronized (queue) {
 				queue.push(item);
 			}
 			notifyWaiter();
 		} catch (SQLException e) {
 			throw new JdbcQueueException(e);
 		}
 	}
 
 	@Override
 	public String pop() throws JdbcQueueException {
 		String result = null;
 
 		try {
 			JdbcElement element;
 			synchronized (queue) {
 				element = queue.pop();
 			}
 
 			if (element != null) {
 				try {
 					if (element.persistent) {
 						delete(element);
 					}
 
 					result = element.content;
 				} catch (SQLException e) {
 					synchronized (queue) {
 						queue.push(element);
 					}
 					notifyWaiter();
 					throw new JdbcQueueException(e);
 				}
 			}
 		} catch (NoSuchElementException ignored) {
 			// just return null
 		}
 
 		return result;
 	}
 
 	/**
 	 * Load queue contents from DBMS
 	 * 
 	 * @throws SQLException
 	 *             Can't load
 	 */
 	protected abstract void load() throws SQLException;
 
 	/**
 	 * Create table for queue (if not exits)
 	 * 
 	 * @throws SQLException
 	 *             Can't create
 	 */
 	protected abstract void createTable() throws SQLException;
 
 	/**
 	 * Empty table for queue
 	 * 
 	 * @throws SQLException
 	 *             Can't delete
 	 */
 	protected abstract void emptyTable() throws SQLException;
 
 	/**
 	 * Insert queue element to table
 	 * 
 	 * @param element
 	 * @throws SQLException
 	 *             Can't insert
 	 */
 	protected abstract void insert(Element element) throws SQLException;
 
 	/**
 	 * Delete queue element from table
 	 * 
 	 * @param element
 	 * @throws SQLException
 	 *             Can't delete
 	 */
 	protected abstract void delete(Element element) throws SQLException;
 }
 
 /**
  * Internal element of JdbcQueue
  * 
  * @author Sangmin Lee
  * @since 2013. 10. 25.
  */
 class JdbcElement extends Element {
 	/** true: store to Database, false: don't store */
 	// default true for load()
 	public boolean persistent = true;
 }
