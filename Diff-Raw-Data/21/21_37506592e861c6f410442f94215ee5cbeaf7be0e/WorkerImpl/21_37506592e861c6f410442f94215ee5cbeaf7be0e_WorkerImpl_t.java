 package alpv.mwp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 public class WorkerImpl implements Worker, Runnable, Serializable {
 
 	private static final long serialVersionUID = -4483191034408758974L;
 	private final Master _master;
 	private boolean _isRunning = true;
 
 	public WorkerImpl(String host, int port) throws RemoteException,
 			NotBoundException {
 		System.out.println("Worker: looking for server " + host + ":" + port);
 		Registry registry = LocateRegistry.getRegistry(host, port);
 		_master = (Master) (registry.lookup("mwp"));
 		_master.registerWorker(this);
 	}
 
 	public void run() {
 		System.out.print("Worker: running");
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		while (_isRunning) {
 			try {
 				String line = br.readLine();
				if (line != null && line.startsWith("q")) {
 					_master.unregisterWorker(this);
 					_isRunning = false;
 					br.close();
 					System.out.println("Worker: bye");
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Starts a WorkerThread and returns instantly.
 	 */
 	public <Argument, Result> void start(Task<Argument, Result> t,
 			Pool<Argument> argpool, Pool<Result> respool)
 			throws RemoteException {
 		Thread workerTread = new Thread(new WorkerThread<Argument, Result>(t,
 				argpool, respool));
 		workerTread.start();
 	}
 
 	private class WorkerThread<Argument, Result> implements Runnable {
 		private Task<Argument, Result> _task;
 		private Pool<Argument> _argumentPool;
 		private Pool<Result> _resultPool;
 
 		public WorkerThread(Task<Argument, Result> task,
 				Pool<Argument> argumentPool, Pool<Result> resultPool) {
 			_task = task;
 			_argumentPool = argumentPool;
 			_resultPool = resultPool;
 		}
 
 		/**
 		 * Executes the tasks and writes the result to the resource pool as soon
 		 * as there are arguments in the argument pool.
 		 */
 		public void run() {
 			try {
 				for (Argument argument = _argumentPool.get(); argument != null
 						&& _isRunning; argument = _argumentPool.get()) {
 					_resultPool.put(_task.exec(argument));
 				}
 			} catch (RemoteException e) {
 				System.err.println(e.getMessage());
 			}
 		}
 	}
 }
