 package master;
 
 import java.io.File;
 import java.util.List;
 import java.util.Set;
 
 import model.BSScrimmageSet;
 import networking.Packet;
 
 import common.NetworkMatch;
 
 /**
  * Provides static methods to perform asynchronous calls to the master
  * @author stevearc
  *
  */
 public abstract class AbstractMaster {
 	protected static AbstractMaster singleton;
 	
 	protected AbstractMaster() {
 		singleton = this;
 	}
 	
 	public static AbstractMaster getMaster() {
 		return singleton;
 	}
 
 	/**
 	 * Update the battlecode source files
 	 * @param battlecode_server
 	 * @param idata
 	 */
 	public static void kickoffUpdateBattlecodeFiles(final File battlecode_server, final File idata) {
 		new Thread(new Runnable(){
 
 			@Override
 			public void run() {
 				singleton.updateBattlecodeFiles(battlecode_server, idata);
 			}
 
 		}).start();
 	}
 	protected abstract void updateBattlecodeFiles(final File battlecode_server, final File idata);
 		
 	/**
 	 * Queues a run
 	 * @param run
 	 * @param seeds
 	 * @param maps
 	 */
 	public static void kickoffQueueRun(final Long teamAId, final Long teamBId, final List<Long> seeds, final List<Long> mapIds) {
 		new Thread(new Runnable(){
 
 			@Override
 			public void run() {
 				singleton.queueRun(teamAId, teamBId, seeds, mapIds);
 			}
 
 		}).start();
 	}
 	protected abstract void queueRun(final Long teamAId, final Long teamBId, final List<Long> seeds, final List<Long> mapIds);
 	
 	/**
 	 * Delete run data
 	 * @param run_id
 	 */
 	public static void kickoffDeleteRun(final Long run_id) {
 		new Thread(new Runnable(){
 
 			@Override
 			public void run() {
 				singleton.deleteRun(run_id);
 			}
 
 		}).start();
 	}
 	protected abstract void deleteRun(final Long run_id);
 	
 	public abstract void deleteScrimmage(Long scrimId);
 
 	/**
 	 * Send the finished match data to the master
 	 * @param worker
 	 * @param p
 	 */
 	public static void kickoffMatchFinished(final WorkerRepr worker, final Packet p) {
 		new Thread(new Runnable(){
 
 			@Override
 			public void run() {
 				singleton.matchFinished(worker, p);
 			}
 
 		}).start();
 	}
 	public abstract void matchFinished(final WorkerRepr worker, final Packet p);
 
 	/**
 	 * Send the analyzed scrimmage match data to the master
 	 * @param worker
 	 * @param p
 	 */
 	public static void kickoffMatchAnalyzed(final WorkerRepr worker, final Packet p) {
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				singleton.matchAnalyzed(worker, p);
 			}
 		});
 	}
 	public abstract void matchAnalyzed(final WorkerRepr worker, final Packet p);
 	
 	/**
 	 * Tell the master to send matches to a worker
 	 * @param worker
 	 */
 	public static void kickoffSendWorkerMatches(final WorkerRepr worker) {
 		new Thread(new Runnable(){
 
 			@Override
 			public void run() {
 				singleton.sendWorkerMatches(worker);
 			}
 
 		}).start();
 	}
 	protected abstract void sendWorkerMatches(final WorkerRepr worker);
 	
 	/**
 	 * Tell the master to send player/map data to the worker
 	 * @param worker
 	 * @param match
 	 * @param needMap
 	 * @param needTeamA
 	 * @param needTeamB
 	 */
 	public static void kickoffSendWorkerDependencies(final WorkerRepr worker, final NetworkMatch match, final boolean needUpdate, 
 			final boolean needMap, final boolean needTeamA, final boolean needTeamB) {
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				singleton.sendWorkerDependencies(worker, match, needUpdate, needMap, needTeamA, needTeamB);
 			}
 			
 		}).start();
 	}
 	public abstract void sendWorkerDependencies(final WorkerRepr worker, final NetworkMatch match, final boolean needUpdate, 
 			final boolean needMap, final boolean needTeamA, final boolean needTeamB);
 	
 	public static void kickoffUpdateMaps() {
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				singleton.updateMaps();
 			}
 			
 		}).start();
 	}
 	protected abstract void updateMaps();
 	
 	static void kickoffWorkerDisconnect(final WorkerRepr worker) {
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				singleton.workerDisconnect(worker);
 			}
 			
 		}).start();
 	}
 	abstract void workerDisconnect(WorkerRepr worker);
 	
 	static void kickoffWorkerConnect(final WorkerRepr worker) {
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				singleton.workerConnect(worker);
 			}
 			
 		}).start();
 	}
 	abstract void workerConnect(WorkerRepr worker);
 	
 	public static Set<WorkerRepr> getConnectionsStatic() {
 		return singleton.getConnections();
 	}
 	protected abstract Set<WorkerRepr> getConnections();
 	
 	
 	public static void kickoffAnalyzeScrimmageMatch(final BSScrimmageSet scrim) {
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				singleton.analyzeScrimmageMatch(scrim);
 			}
 		}).start();
 	}
	protected abstract void analyzeScrimmageMatch(BSScrimmageSet scrim);
 }
