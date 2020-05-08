 package simulation.beefs.replication;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import manelsim.EventScheduler;
 import simulation.beefs.model.DataServer;
 import simulation.beefs.model.FileReplica;
 import simulation.beefs.model.ReplicatedFile;
 
 /**
  * 
  * Keep machines sleeping if some other machine can get the replica
  *
  */
 public class MigrateReplicas extends Replicator {
 	
 	private final List<DataServer> dataServers;
 	
 	public MigrateReplicas(Set<DataServer> dataServers) {
 		if(dataServers != null) {
 			this.dataServers = new ArrayList<DataServer>(dataServers);
 		} else {
 			this.dataServers = new ArrayList<DataServer>();
 		}
 	}
 
 	@Override
 	public void updateReplicas(ReplicatedFile file) {
 		Collections.shuffle(dataServers);
 		if(!file.primary().host().isReachable()) {
 			file.primary().host().wakeOnLan(EventScheduler.now());
 		}
 		
 		Set<DataServer> exceptions = new HashSet<DataServer>();
 		Set<FileReplica> newReplicas = new HashSet<FileReplica>();
 
 		DataServer newDataServer = null;
 		for(FileReplica replica : file.replicas()) {
 			exceptions.add(replica.dataServer());
 			replica.delete();
 			
 			if(replica.dataServer().host().isReachable() && replica.dataServer().freeSpace() > file.size()) {
 				newDataServer = replica.dataServer();
 			} else {
 				newDataServer = giveMeOneAwakeDataServer(exceptions, file.size());
 			}
 			
 			if(newDataServer != null) {
 				exceptions.add(newDataServer);
 				newReplicas.add(new FileReplica(newDataServer, file.size()));
 			} else {
 				break;
 			}
 		}
 		
 		if(newReplicas.size() < file.expectedReplicationLevel() && newDataServer != null) {
			for(int i = 0; i < (file.expectedReplicationLevel() - newReplicas.size()); i++) {
 				newDataServer = giveMeOneAwakeDataServer(exceptions, file.size());
 
 				if(newDataServer != null) {
 					exceptions.add(newDataServer);
 					newReplicas.add(new FileReplica(newDataServer, file.size()));
 				} else {
 					break;
 				}
 			}
 		}
 		
 		file.updateReplicas(newReplicas);
 	}
 
 	private DataServer giveMeOneAwakeDataServer(Set<DataServer> exceptions, long fileSize) {
 		for(DataServer ds : dataServers) {
 			if(ds.host().isReachable() && !exceptions.contains(ds) && ds.freeSpace() >= fileSize) {
 				return ds;
 			}
 		}
 		return wakeUpWhoIsSleepingForLonger(fileSize);
 	}
 
 	private DataServer wakeUpWhoIsSleepingForLonger(long fileSize) {
 		DataServer unfortunateDataServer = null;
 		
 		for(DataServer ds : dataServers) {
 			if(!ds.host().isReachable()) {
 				if(ds.freeSpace() >= fileSize) {
 					if(unfortunateDataServer == null) {
 						unfortunateDataServer = ds;
 					} else if(ds.host().lastTransitionTime().
 							isEarlierThan(unfortunateDataServer.host().lastTransitionTime())) {
 						unfortunateDataServer = ds;
 					}					
 				}
 			}
 		}
 		if( unfortunateDataServer != null) {
 			unfortunateDataServer.host().wakeOnLan(EventScheduler.now());
 		} else {
 			System.out.println(String.format("@all disks full: could not replicate %d bytes - %s", fileSize, EventScheduler.now()));
 		}
 		return unfortunateDataServer;
 	}
 
 }
