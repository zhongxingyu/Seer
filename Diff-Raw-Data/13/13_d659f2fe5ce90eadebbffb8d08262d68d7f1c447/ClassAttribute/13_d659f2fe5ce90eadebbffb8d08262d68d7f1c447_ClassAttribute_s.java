 package attributes;
 
 import database.Scope;
 
 public class ClassAttribute {
 	public static final Attribute COMBINED_CLASS_NAME = new Attribute(Scope.CLASS,
 			"CONCAT(PROJECT_NAME,'->',CLASS_NAME," +
 			"'(',CAST(RAND()*1000000 AS INT),')') AS NAME", false);
 	
 	public static final Attribute PROJECT_NAME = Attribute.PROJECT_NAME;
 	public static final Attribute CLASS_NAME = Attribute.CLASS_NAME;
 	
 	// method attributes
 	public static final Attribute PUBLIC_METHODS = Attribute.PUBLIC_METHODS;
 	public static final Attribute PRIVATE_METHODS = Attribute.PRIVATE_METHODS;
 	public static final Attribute SYNCHRONIZED_METHODS = Attribute.SYNCHRONIZED_METHODS;
 	public static final Attribute METHOD_CALLS = Attribute.METHOD_CALLS;
 	public static final Attribute SYNCHRONIZED_BLOCKS = Attribute.SYNCHRONIZED_BLOCKS;
 	public static final Attribute LOCK_CALLS = Attribute.LOCK_CALLS;
 	public static final Attribute UNLOCK_CALLS= Attribute.UNLOCK_CALLS;
 	public static final Attribute WAIT_CALLS = Attribute.WAIT_CALLS;
 	public static final Attribute NOTIFY_CALLS = Attribute.NOTIFY_CALLS;
 	public static final Attribute NOTIFYALL_CALLS = Attribute.NOTIFYALL_CALLS;
 	public static final Attribute SLEEP_CALLS = Attribute.SLEEP_CALLS;
 	public static final Attribute YIELD_CALLS = Attribute.YIELD_CALLS;
 	public static final Attribute JOIN_CALLS = Attribute.JOIN_CALLS;
 	
 	public static final Attribute LOCK_OBJECTS = Attribute.LOCK_OBJECTS;
 	public static final Attribute COUNT_DOWN_OBJECTS = Attribute.COUNT_DOWN_OBJECTS;
 	public static final Attribute CONDITION_OBJECTS = Attribute.CONDITION_OBJECTS;
 	public static final Attribute SEMAPHORE_OBJECTS = Attribute.SEMAPHORE_OBJECTS;
 	public static final Attribute CYCLICBARRIER_OBJECTS = Attribute.CYCLICBARRIER_OBJECTS;
 	
 	public static final Attribute NESTEDNESS_LOCKS = Attribute.NESTEDNESS_LOCKS;
 	public static final Attribute NESTEDNESS_SYNCHRONIZED = Attribute.NESTEDNESS_SYNCHRONIZED;
 	public static final Attribute NESTEDNESS_CONDITIONALS = Attribute.NESTEDNESS_CONDITIONALS;
 	public static final Attribute NESTEDNESS_LOOPS = Attribute.NESTEDNESS_LOOPS;
 	
 	// class attributes
 	public static final Attribute NUMBER_OF_METHODS = Attribute.NUMBER_OF_METHODS;
 	public static final Attribute VOLATILE_VARIABLES = Attribute.VOLATILE_VARIABLES;
 	public static final Attribute PUBLIC_VARIABLES = Attribute.PUBLIC_VARIABLES;
 	public static final Attribute PRIVATE_VARIABLES = Attribute.PRIVATE_VARIABLES;
 	public static final Attribute LOCK_OBJECT_FIELDS = Attribute.LOCK_OBJECT_FIELDS;
 	public static final Attribute CONDITION_OBJECT_FIELDS = Attribute.CONDITION_OBJECT_FIELDS;
 	public static final Attribute EXECUTOR_OBJECT_FIELDS = Attribute.EXECUTOR_OBJECT_FIELDS;
 	public static final Attribute CONCURRENTMAP_OBJECT_FIELDS = Attribute.CONCURRENTMAP_OBJECT_FIELDS;
 	public static final Attribute ATOMICINTEGER_OBJECT_FIELDS = Attribute.ATOMICINTEGER_OBJECT_FIELDS;
 	public static final Attribute BLOCKINGQUEUE_OBJECT_FIELDS = Attribute.BLOCKINGQUEUE_OBJECT_FIELDS;
 	public static final Attribute CONCURRENTLINKEDQUEUE_OBJECT_FIELDS = Attribute.CONCURRENTLINKEDQUEUE_OBJECT_FIELDS;
 	public static final Attribute COPYONWRITEARRAYLIST_OBJECT_FIELDS = Attribute.COPYONWRITEARRAYLIST_OBJECT_FIELDS;
 	public static final Attribute EXECUTORSERVICE_OBJECT_FIELDS = Attribute.EXECUTORSERVICE_OBJECT_FIELDS;
 	public static final Attribute FUTURE_OBJECT_FIELDS = Attribute.FUTURE_OBJECT_FIELDS;
 	public static final Attribute THREADPOOLEXECUTOR_OBJECT_FIELDS = Attribute.THREADPOOLEXECUTOR_OBJECT_FIELDS;
 	public static final Attribute COUNTDOWNLATCH_OBJECT_FIELDS = Attribute.COUNTDOWNLATCH_OBJECT_FIELDS;
 	public static final Attribute CYCLICBARRIER_OBJECT_FIELDS = Attribute.CYCLICBARRIER_OBJECT_FIELDS;
 	public static final Attribute EXCHANGER_OBJECT_FIELDS = Attribute.EXCHANGER_OBJECT_FIELDS;
 	public static final Attribute SEMAPHORE_OBJECT_FIELDS = Attribute.SEMAPHORE_OBJECT_FIELDS;
 	public static final Attribute THREADFACTORY_OBJECT_FIELDS = Attribute.THREADFACTORY_OBJECT_FIELDS;
 	
 	public static final Attribute CALLABLE_INTERFACE = Attribute.CALLABLE_INTERFACE;
 	public static final Attribute DELAYED_INTERFACE = Attribute.DELAYED_INTERFACE;
 	public static final Attribute THREADFACTORY_INTERFACE = Attribute.THREADFACTORY_INTERFACE;
 	public static final Attribute BLOCKINGQUEUE_INTERFACE = Attribute.BLOCKINGQUEUE_INTERFACE;
 	
 	public static final Attribute CONCURRENTMAP_OBJECTS_EXTENDS = Attribute.CONCURRENTMAP_OBJECTS_EXTENDS;
 	public static final Attribute EXECUTORSERVICE_OBJECTS_EXTENDS = Attribute.EXECUTORSERVICE_OBJECTS_EXTENDS;
 	public static final Attribute THREADPOOLEXECUTOR_OBJECTS_EXTENDS = Attribute.THREADPOOLEXECUTOR_OBJECTS_EXTENDS;
 	public static final Attribute CYCLICBARRIER_OBJECTS_EXTENDS = Attribute.CYCLICBARRIER_OBJECTS_EXTENDS;
 	public static final Attribute EXCHANGER_OBJECTS_EXTENDS = Attribute.EXCHANGER_OBJECTS_EXTENDS;
 	public static final Attribute SEMAPHORE_OBJECTS_EXTENDS = Attribute.SEMAPHORE_OBJECTS_EXTENDS;
 	public static final Attribute LOCK_OBJECTS_EXTENDS = Attribute.LOCK_OBJECTS_EXTENDS;
 	public static final Attribute EXECUTOR_OBJECTS_EXTENDS = Attribute.EXECUTOR_OBJECTS_EXTENDS;
 	public static final Attribute ATOMICINTEGER_OBJECTS_EXTENDS = Attribute.ATOMICINTEGER_OBJECTS_EXTENDS;
 	public static final Attribute CONCURRENTLINKEDQUEUE_OBJECTS_EXTENDS = Attribute.CONCURRENTLINKEDQUEUE_OBJECTS_EXTENDS;
 	public static final Attribute COPYONWRITEARRAYLIST_OBJECTS_EXTENDS = Attribute.COPYONWRITEARRAYLIST_OBJECTS_EXTENDS;
 	public static final Attribute FUTURE_OBJECTS_EXTENDS = Attribute.FUTURE_OBJECTS_EXTENDS;
 	public static final Attribute COUNTDOWNLATCH_OBJECTS_EXTENDS = Attribute.COUNTDOWNLATCH_OBJECTS_EXTENDS;
 }
