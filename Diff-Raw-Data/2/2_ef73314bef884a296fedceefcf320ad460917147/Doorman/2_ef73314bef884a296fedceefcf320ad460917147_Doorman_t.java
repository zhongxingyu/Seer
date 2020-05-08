 /*
  * $Id$
  */
 package org.xins.util.threads;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import org.apache.log4j.Logger;
 
 /**
  * Monitor that acts like a doorman. It implements a variation of the
  * <em>Alternating Reader Writer</em> algorithm.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.66
  */
 public final class Doorman extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private final static Logger LOG = Logger.getLogger(Doorman.class.getName());
 
    /**
     * The type for readers in the queue.
     */
    private static final Queue.EntryType READ_QUEUE_ENTRY_TYPE = new Queue.EntryType();
 
    /**
     * The type for writers in the queue.
     */
    private static final Queue.EntryType WRITE_QUEUE_ENTRY_TYPE = new Queue.EntryType();
 
    /**
     * The number of instances of this class.
     */
    private static int INSTANCE_COUNT;
 
    /**
     * The lock object for <code>INSTANCE_COUNT</code>.
     */
    private static final Object INSTANCE_COUNT_LOCK = new Object();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Doorman</code> with the specified initial queue
     * size.
     *
     * @param queueSize
     *    the initial queue size, must be &gt;= 0.
     *
     * @param maxQueueWaitTime
     *    the maximum number of milliseconds an entry should be allowed to wait
     *    in the queue, must be &gt;= 0.
     *
     * @throws IllegalArgumentException
     *    if <code>queueSize &lt; 0 || maxQueueWaitTime &lt; 0L</code>.
     */
    public Doorman(int queueSize, long maxQueueWaitTime)
    throws IllegalArgumentException {
 
       // Check preconditions
       if (queueSize < 0 || maxQueueWaitTime <= 0L) {
          if (queueSize < 0 && maxQueueWaitTime <= 0L) {
             throw new IllegalArgumentException("queueSize (" + queueSize + ") < 0 && maxQueueWaitTime (" + maxQueueWaitTime + ") <= 0L");
          } else if (queueSize < 0) {
             throw new IllegalArgumentException("queueSize (" + queueSize + ") < 0");
          } else {
             throw new IllegalArgumentException("maxQueueWaitTime (" + maxQueueWaitTime + ") <= 0L");
          }
       }
 
       // Determine instance number
       synchronized (INSTANCE_COUNT_LOCK) {
          _instanceID = INSTANCE_COUNT++;
       }
 
       // Initialize other fields
       _currentActorLock = new Object();
       _currentReaders   = new HashSet();
       _queue            = new Queue(queueSize);
       _maxQueueWaitTime = maxQueueWaitTime;
 
       if (LOG.isDebugEnabled()) {
          LOG.debug("Constructed Doorman #" + _instanceID + ", initial queue size is " + queueSize + ", maximum queue wait time is " + maxQueueWaitTime + " ms.");
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Maximum wait time in the queue. After reaching this period of time, a
     * {@link QueueTimeOutException} is thrown.
     */
    private final long _maxQueueWaitTime;
 
    /**
     * Lock object for reading and writing the set of current readers and the
     * current writer.
     */
    private final Object _currentActorLock;
 
    /**
     * The set of currently active readers. All elements in the set are
     * {@link Thread} instances.
     */
    private final Set _currentReaders;
 
    /**
     * The currently active writer, if any.
     */
    private Thread _currentWriter;
 
    /**
     * The queue that contains the waiting readers and/or writers.
     */
    private final Queue _queue;
 
    /**
     * Unique numeric identifier for this instance.
     */
    private final int _instanceID;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the maximum time to wait in the queue.
     *
     * @return
     *    the maximum wait time, always &gt; 0.
     */
    public long getMaxQueueWaitTime() {
       return _maxQueueWaitTime;
    }
 
    /**
     * Enters the 'protected area' as a reader. If necessary, this method will
     * wait until the area can be entered.
     *
     * @throws QueueTimeOutException
     *    if this thread was waiting in the queue for too long.
     */
    public void enterAsReader()
    throws QueueTimeOutException {
 
       Thread reader = Thread.currentThread();
 
       if (LOG.isDebugEnabled()) {
          LOG.debug("Doorman #" + _instanceID + ", enterAsReader() called for " + reader.toString() + '.');
       }
 
       synchronized (_currentActorLock) {
 
          // Check preconditions
          if (_currentWriter == reader) {
             throw new IllegalStateException(reader.toString() + " cannot enter as a reader if it is already the active writer.");
          } else if (_currentReaders.contains(reader)) {
             throw new IllegalStateException(reader.toString() + " cannot enter as a reader if it is already an active reader.");
          }
 
          // If there is a current writer, then we need to wait in the queue
          boolean enterQueue = _currentWriter != null;
          synchronized (_queue) {
 
             // If there is no current writer, but there is already a queue,
             // then we also need to join it
             enterQueue = enterQueue ? true : !_queue.isEmpty();
 
             // Join the queue if necessary
             if (enterQueue) {
                _queue.add(reader, READ_QUEUE_ENTRY_TYPE);
             }
          }
 
          // If we don't have to join the queue, join the set of current
          // readers and go ahead
          if (!enterQueue) {
             _currentReaders.add(reader);
             return;
          }
       }
 
       // Wait for read access
       try {
          Thread.sleep(_maxQueueWaitTime);
          throw new QueueTimeOutException();
       } catch (InterruptedException exception) {
          // fall through
       }
 
       synchronized (_currentActorLock) {
          if (! _currentReaders.contains(reader)) {
             throw new IllegalStateException(reader.toString() + " was interrupted in enterAsReader(), but not in the set of current readers.");
          }
       }
    }
 
    /**
     * Enters the 'protected area' as a writer. If necessary, this method will
     * wait until the area can be entered.
     *
     * @throws QueueTimeOutException
     *    if this thread was waiting in the queue for too long.
     */
    public void enterAsWriter()
    throws QueueTimeOutException {
 
       Thread writer = Thread.currentThread();
 
       if (LOG.isDebugEnabled()) {
          LOG.debug("Doorman #" + _instanceID + ", enterAsWriter() called for " + writer.toString() + '.');
       }
 
       synchronized (_currentActorLock) {
 
          // Check preconditions
          if (_currentWriter == writer) {
             throw new IllegalStateException(writer.toString() + " cannot enter as a writer if it is already the active writer.");
          } else if (_currentReaders.contains(writer)) {
             throw new IllegalStateException(writer.toString() + " cannot enter as a writer if it is already an active reader.");
          }
 
          // If there is a current writer or one or more current readers, then
          // we need to wait in the queue
          boolean enterQueue = ! (_currentWriter == null && _currentReaders.isEmpty());
 
          // Join the queue if necessary
          if (enterQueue) {
             synchronized (_queue) {
                _queue.add(writer, WRITE_QUEUE_ENTRY_TYPE);
             }
 
          // If we don't have to join the queue, become the current writer and
          // return
          } else {
             _currentWriter = writer;
             return;
          }
       }
 
       // Wait for write access
       try {
          Thread.sleep(_maxQueueWaitTime);
          throw new QueueTimeOutException();
       } catch (InterruptedException exception) {
          // fall through
       }
 
       synchronized (_currentActorLock) {
          if (_currentWriter != writer) {
             throw new IllegalStateException("Thread was interrupted in enterAsWriter(), but is not set as the current writer.");
          }
       }
    }
 
    /**
     * Leaves the 'protected area' as a reader.
     */
    public void leaveAsReader() {
 
       Thread reader = Thread.currentThread();
 
       if (LOG.isDebugEnabled()) {
          LOG.debug("Doorman #" + _instanceID + ", leaveAsReader() called for " + reader.toString() + '.');
       }
 
       synchronized (_currentActorLock) {
          boolean readerRemoved = _currentReaders.remove(reader);
 
          if (!readerRemoved) {
             throw new IllegalStateException(reader.toString() + " cannot leave protected area as reader, because it has not entered as a reader.");
          }
 
          if (_currentReaders.isEmpty()) {
 
             synchronized (_queue) {
 
                // Determine if the queue has a writer atop, a reader atop or is
                // empty
                Queue.EntryType type = _queue.getTypeOfFirst();
 
                if (type == WRITE_QUEUE_ENTRY_TYPE) {
 
                   // If a writer is waiting, activate it
                   _currentWriter = _queue.pop();
                   _currentWriter.interrupt();
                } else if (type == READ_QUEUE_ENTRY_TYPE) {
 
                   // If a reader leaves, the queue cannot contain a reader at the
                   // top, it must be either empty or have a writer at the top
                   throw new IllegalStateException("Found writer at top of queue while a reader is leaving the protected area.");
                }
             }
          }
       }
    }
 
    /**
     * Leaves the 'protected area' as a writer.
     */
    public void leaveAsWriter() {
 
       Thread writer = Thread.currentThread();
 
       if (LOG.isDebugEnabled()) {
          LOG.debug("Doorman #" + _instanceID + ", leaveAsWriter() called for " + writer.toString() + '.');
       }
 
       synchronized (_currentActorLock) {
 
          if (_currentWriter != writer) {
             throw new IllegalStateException(writer.toString() + " cannot leave protected area as writer, because it has not entered as a writer.");
          }
 
          synchronized (_queue) {
 
             // Determine if the queue has a writer atop, a reader atop or is
             // empty
             Queue.EntryType type = _queue.getTypeOfFirst();
 
             // If a writer is waiting, activate it alone
             if (type == WRITE_QUEUE_ENTRY_TYPE) {
 
                _currentWriter = _queue.pop();
                _currentWriter.interrupt();
 
             // If readers are on top, active all readers atop
             } else if (type == READ_QUEUE_ENTRY_TYPE) {
                do {
                   Thread reader = _queue.pop();
                   _currentReaders.add(reader);
                   reader.interrupt();
                } while (_queue.getTypeOfFirst() == READ_QUEUE_ENTRY_TYPE);
 
             // Otherwise there is no active thread, make sure to reset the
             // current writer
             } else {
                _currentWriter = null;
             }
          }
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Inner class
    //-------------------------------------------------------------------------
 
    /**
     * Queue of waiting reader and writer threads.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     *
     * @since XINS 0.66
     */
    private static final class Queue
    extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new queue with the specified initial capacity.
        *
        * @param capacity
        *    the initial capacity, must be &gt;= 0.
        *
        * @throws IllegalArgumentException
        *    if <code>capacity &lt; 0</code>.
        */
       public Queue(int capacity)
       throws IllegalArgumentException {
 
          // Check preconditions
          if (capacity < 0) {
             throw new IllegalArgumentException("capacity (" + capacity + ") < 0");
          }
 
          // Initialize fields
          _entries    = new LinkedList();
          _entryTypes = new HashMap(capacity);
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The list of entries.
        */
       private final LinkedList _entries;
 
       /**
        * The entry types, by entry. This map has the {@link Thread threads} as
        * keys and their {@link EntryType types} as values.
        */
       private final Map _entryTypes;
 
       /**
        * Cached link to the first entry. This field is either
        * <code>null</code> or an instance of class {@link Thread}.
        */
       private Thread _first;
 
       /**
        * Cached type of the first entry. This field is either
        * <code>null</code> (if {@link #_entries} is empty), or
       * <code>(EntryType) </code>{@link #_entries}<code>.</code>{@link LinkedList#get() get}<code>(0)</code>
        * (if {@link #_entries} is not empty).
        */
       private EntryType _typeOfFirst;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Determines if this queue is empty.
        *
        * @return
        *    <code>true</code> if this queue is empty, <code>false</code> if it
        *    is not.
        */
       public boolean isEmpty() {
          return (_first == null);
       }
 
       /**
        * Gets the type of the first waiting thread in this queue. If this
        * queue is empty, then <code>null</code> is returned.
        *
        * @return
        *    <code>null</code> if this queue is empty;
        *    {@link #READ_QUEUE_ENTRY_TYPE} is the first thread in this queue
        *    is waiting for read access;
        *    {@link #WRITE_QUEUE_ENTRY_TYPE} is the first thread in this queue
        *    is waiting for write access;
        */
       public EntryType getTypeOfFirst() {
          return _typeOfFirst;
       }
 
       /**
        * Adds the specified thread to the queue of waiting threads.
        *
        * @param thread
        *    the thread to be added, should not be <code>null</code>.
        *
        * @param type
        *    the type of thread, should be either
        *    {@link #READ_QUEUE_ENTRY_TYPE} or {@link #WRITE_QUEUE_ENTRY_TYPE}.
        *
        * @throws IllegalStateException
        *    if the specified thread is already in this queue.
        */
       public void add(Thread thread, EntryType type)
       throws IllegalStateException {
 
          // Check preconditions
          if (_entryTypes.containsKey(thread)) {
             throw new IllegalStateException(thread.toString() + " is already in this queue.");
          }
 
          // If the queue is empty, then store the new waiter as the first
          if (_first == null) {
             _first       = thread;
             _typeOfFirst = type;
          }
 
          // Store the waiter thread and its type
          _entryTypes.put(thread, type);
          _entries.addLast(thread);
       }
 
       /**
        * Pops the first waiting thread from this queue, removes it and then
        * returns it.
        *
        * @return
        *    the top waiting thread, never <code>null</code>.
        *
        * @throws IllegalStateException
        *    if this queue is empty.
        */
       public Thread pop() throws IllegalStateException {
 
          // Check preconditions
          if (_first == null) {
             throw new IllegalStateException("This queue is empty.");
          }
 
          Thread oldFirst = _first;
 
          // Remove the current first
          _entries.removeFirst();
          _entryTypes.remove(oldFirst);
 
          // Get the new first, now that the other one is removed
          Object newFirst = _entries.getFirst();
          _first          = newFirst == null ? null : (Thread) _entries.getFirst();
          _typeOfFirst    = newFirst == null ? null : (EntryType) _entryTypes.get(_first);
 
          return oldFirst;
       }
 
       /**
        * Removes the specified thread from this queue.
        *
        * @param thread
        *    the thread to be removed from this queue, should not be
        *    <code>null</code>.
        *
        * @throws IllegalStateException
        *    if this queue does not contain the specified thread.
        */
       public void remove(Thread thread)
       throws IllegalStateException {
 
          if (thread == _first) {
 
             // Remove the current first
             _entries.removeFirst();
 
             // Get the new first, now that the other one is removed
             Object newFirst = _entries.getFirst();
             _first       = newFirst == null ? null : (Thread) _entries.getFirst();
             _typeOfFirst = newFirst == null ? null : (EntryType) _entryTypes.get(_first);
          } else {
 
             // Remove the thread from the list
             if (! _entries.remove(thread)) {
                throw new IllegalStateException(thread.toString() + " is not in this queue.");
             }
          }
 
          _entryTypes.remove(thread);
       }
 
 
       //----------------------------------------------------------------------
       // Inner classes
       //----------------------------------------------------------------------
 
       /**
        * Type of an entry in a queue for a doorman.
        *
        * @version $Revision$ $Date$
        * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
        *
        * @since XINS 0.66
        */
       public static final class EntryType
       extends Object {
          // empty
       }
    }
 }
