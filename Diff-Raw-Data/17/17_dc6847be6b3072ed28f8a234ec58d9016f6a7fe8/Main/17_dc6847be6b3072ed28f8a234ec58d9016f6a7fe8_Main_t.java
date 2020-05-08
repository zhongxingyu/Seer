 package main;
 
 import java.util.Collection;
 
 import database.Database;
 import database.Scope;
 
 import utils.PathAndFileNames;
 import workers.Counter;
 import workers.CountNestednessOfSynchronizedBlocks;
 
 /**
  * Parse a java file or directory of java files using the generated parser
  * ANTclassOrInterfaceDeclarationLR builds from java.g
  */
 class Main {
 	public static void main(String[] args) {
 		Database database = new Database(PathAndFileNames.DATA_BASE_PATH);
 		
 		WorkerQueue workerQueue = new WorkerQueue(database);
 		
 		workerQueue.addWorker(new Counter("public_methods", Scope.METHOD, ".*METHOD_DECL", "MODIFIER_LIST", "public"));
 		workerQueue.addWorker(new Counter("private_methods", Scope.METHOD, ".*METHOD_DECL", "MODIFIER_LIST", "private"));
 		workerQueue.addWorker(new Counter("synchronized_methods", Scope.METHOD, ".*METHOD_DECL", "MODIFIER_LIST", "synchronized"));
 		workerQueue.addWorker(new Counter("synchronized_blocks", Scope.METHOD, "BLOCK_SCOPE", "synchronized"));
 		workerQueue.addWorker(new Counter("lock_calls", Scope.METHOD, "METHOD_CALL", "\\.", "lock"));
 		workerQueue.addWorker(new Counter("unlock_calls", Scope.METHOD, "METHOD_CALL", "\\.", "unlock"));
 		workerQueue.addWorker(new Counter("wait_calls", Scope.METHOD, "METHOD_CALL", "\\.", "wait"));
 		workerQueue.addWorker(new Counter("notify_calls", Scope.METHOD, "METHOD_CALL", "\\.", "notify"));
 		workerQueue.addWorker(new Counter("notifyAll_calls", Scope.METHOD, "METHOD_CALL", "\\.", "notifyAll"));
 		workerQueue.addWorker(new Counter("sleep_calls", Scope.METHOD, "METHOD_CALL", "\\.", "sleep"));
 		workerQueue.addWorker(new Counter("yield_calls", Scope.METHOD, "METHOD_CALL", "\\.", "yield"));
 		workerQueue.addWorker(new Counter("join_calls", Scope.METHOD, "METHOD_CALL", "\\.", "join"));
 		workerQueue.addWorker(new Counter("volatile_variables", Scope.CLASS, "VAR_DECLARATION", "MODIFIER_LIST", "volatile"));
 		workerQueue.addWorker(new Counter("public_variables", Scope.CLASS, "VAR_DECLARATION", "MODIFIER_LIST", "public"));
 		workerQueue.addWorker(new Counter("private_variables", Scope.CLASS, "VAR_DECLARATION", "MODIFIER_LIST", "private"));
 		workerQueue.addWorker(new Counter("executor_objects", Scope.CLASS, "VAR_DECLARATION", "TYPE", "QUALIFIED_TYPE_IDENT", "Executor"));
 		workerQueue.addWorker(new Counter("concurrentMap_objects", Scope.CLASS, "VAR_DECLARATION", "TYPE", "QUALIFIED_TYPE_IDENT", "ConcurrentMap"));
 		workerQueue.addWorker(new Counter("atomicInteger_objects", Scope.CLASS, "VAR_DECLARATION", "TYPE", "QUALIFIED_TYPE_IDENT", "AtomicInteger"));
 		workerQueue.addWorker(new Counter("blockingQueue_objects", Scope.CLASS, "VAR_DECLARATION", "TYPE", "QUALIFIED_TYPE_IDENT", "BlockingQueue"));
 		workerQueue.addWorker(new Counter("concurrentLinkedQueue_objects", Scope.CLASS, "VAR_DECLARATION", "TYPE", "QUALIFIED_TYPE_IDENT", "ConcurrentLinkedQueue"));
 		workerQueue.addWorker(new Counter("copyOnWriteArrayList_objects", Scope.CLASS, "VAR_DECLARATION", "TYPE", "QUALIFIED_TYPE_IDENT", "CopyOnWriteArrayList"));
 		workerQueue.addWorker(new Counter("deleyQueue_objects", Scope.CLASS, "VAR_DECLARATION", "TYPE", "QUALIFIED_TYPE_IDENT", "DeleyQueue"));
 		workerQueue.addWorker(new Counter("executorService_objects", Scope.CLASS, "VAR_DECLARATION", "TYPE", "QUALIFIED_TYPE_IDENT", "ExecutorService"));
 		workerQueue.addWorker(new Counter("future_objects", Scope.CLASS, "VAR_DECLARATION", "TYPE", "QUALIFIED_TYPE_IDENT", "Future"));
 		workerQueue.addWorker(new Counter("lock_objects", Scope.CLASS, "VAR_DECLARATION", "TYPE", "QUALIFIED_TYPE_IDENT", "Lock"));
 		workerQueue.addWorker(new CountNestednessOfSynchronizedBlocks());
 		// TODO: add more workers here
 		
 		workerQueue.createTables();
 		
 		Collection<TreePackage> projectPackages = (new TreePackageGenerator()).generateProjectPackages();
 		for(TreePackage projectPackage: projectPackages) {
 			workerQueue.doWork(projectPackage);
 		}
 		
 		workerQueue.createViews();
 
 		database.shutdown();
 		
 		System.out.println("Done!");
 	}
 }
