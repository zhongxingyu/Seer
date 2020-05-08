 /*
   END-USER LICENSE AGREEMENT FOR pns
 
   Copyright (c) 2013 Huray Positive, Inc. All rights reserved.
 
   THIS SOFTWARE IS PROVIDED BY HURAY POSITIVE INC. ``AS IS'' AND ANY EXPRESS
   OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
   NON-INFRINGEMENT, ARE DISCLAIMED.  IN NO EVENT SHALL HURAY POSITIVE INC.
   BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
   INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
   THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package blueprint.sdk.util.jdbc;
 
 import java.sql.SQLException;
 import java.util.UUID;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.sql.DataSource;
 
 /**
  * Prototype for JDBC based Queue.
  * 
  * @author Simon Lee
  * @since 2013. 8. 26.
  */
 public abstract class AbstractJdbcQueue {
 	/** actual queue */
 	protected LinkedBlockingQueue<Element> queue = new LinkedBlockingQueue<Element>();
 
 	/** DataSource for persistence */
 	protected DataSource datasrc;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param dsrc
 	 *            DataSource for persistence
 	 */
 	public AbstractJdbcQueue(DataSource datasrc) {
 		this.datasrc = datasrc;
 	}
 
 	/**
 	 * queue를 초기화 (table 생성, 복원 등등)
 	 */
 	public void init() throws SQLException {
 		synchronized (this) {
 			createTable();
 			load();
 		}
 	}
 
 	/**
 	 * Load queue contents from DBMS
 	 * 
 	 * @throws SQLException
 	 *             Can't load
 	 */
 	protected abstract void load() throws SQLException;
 
 	/**
 	 * Create tables for queue (if not exits)
 	 * 
 	 * @throws SQLException
 	 *             Can't create
 	 */
 	protected abstract void createTable() throws SQLException;
 
 	/**
 	 * Clears queue
 	 * 
 	 * @throws SQLException
 	 *             Can't delete
 	 */
 	public void clear() throws SQLException {
 		synchronized (this) {
 			while (size() > 0) {
 				try {
 					pop();
 				} catch (InterruptedException ignored) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * Push an element to queue
 	 * 
 	 * @param element
 	 * @throws SQLException
 	 *             Can't insert
 	 */
 	public void push(String element) throws SQLException {
 		Element item = new Element();
 		item.uuid = UUID.randomUUID().toString();
 		item.content = element;
 
 		insert(item);
 		queue.add(item);
 	}
 
 	/**
 	 * Insert queue element to tables
 	 * 
 	 * @param element
 	 * @throws SQLException
 	 *             Can't insert
 	 */
 	protected abstract void insert(Element element) throws SQLException;
 
 	/**
 	 * Delete queue element from tables
 	 * 
 	 * @param element
 	 * @throws SQLException
 	 *             Can't delete
 	 */
 	protected abstract void delete(Element element) throws SQLException;
 
 	/**
 	 * Pop an element from queue
 	 * 
 	 * @return queue element
 	 * @throws SQLException
 	 *             Can't delete
 	 * @throws InterruptedException
 	 *             interrupted from blocked status
 	 */
 	public String pop() throws SQLException, InterruptedException {
 		Element item = queue.take();
 		delete(item);
 
 		return item.content;
 	}
 
 	/**
 	 * @return queue size
 	 */
 	public int size() {
 		return queue.size();
 	}
 }
 
 /**
  * Internal element of AbstractJdbcQueue
  * 
  * @author Simon Lee
  * @since 2013. 8. 27.
  */
 class Element {
 	/** UUID of content */
 	public String uuid;
 	/** actual queue content */
 	public String content;
 }
