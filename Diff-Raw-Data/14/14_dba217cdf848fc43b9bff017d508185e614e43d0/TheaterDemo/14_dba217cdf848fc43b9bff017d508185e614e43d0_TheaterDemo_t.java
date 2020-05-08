 package net.geertvos.theater.demo;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicLong;
 
 import net.geertvos.gossip.api.cluster.ClusterMember;
 import net.geertvos.gossip.core.GossipCluster;
 import net.geertvos.gossip.core.GossipClusterMember;
 import net.geertvos.gossip.core.network.GossipServer;
 import net.geertvos.theater.api.actors.Actor;
 import net.geertvos.theater.api.actors.ActorId;
 import net.geertvos.theater.api.actorstore.ActorStore;
 import net.geertvos.theater.api.durability.MessageLog;
 import net.geertvos.theater.api.factory.ActorFactory;
 import net.geertvos.theater.api.messaging.Message;
 import net.geertvos.theater.core.actor.ActorIdImpl;
 import net.geertvos.theater.core.durability.NoopPartitionMessageLog;
 import net.geertvos.theater.core.messaging.PartitionMessageSender;
 import net.geertvos.theater.core.networking.PartitionMessage;
 import net.geertvos.theater.core.networking.PartitionServer;
 import net.geertvos.theater.core.partitioning.ClusteredPartitionManager;
 import net.geertvos.theater.core.partitioning.LocalPartition;
 import net.geertvos.theater.core.partitioning.LocalPartitionFactory;
 import net.geertvos.theater.core.partitioning.RemotePartition;
 import net.geertvos.theater.core.partitioning.RemotePartitionFactory;
 
 import org.apache.log4j.BasicConfigurator;
 
 public class TheaterDemo {
 
 	private static final String CLUSTER = "demoCluster";
 	
 	public static void main(String[] args) {
 		BasicConfigurator.configure();
 		int number = 1;
 		int knows = 1;
 		if(args.length == 2 ) {
 			number = Integer.parseInt(args[0]);
 			knows = Integer.parseInt(args[1]);
 		}
 		
 		GossipClusterMember member = new GossipClusterMember("Member-"+knows, "localhost", 8000+knows, System.currentTimeMillis(),"");
 		Map<String,String> meta = new HashMap<String,String>();
 		meta.put("partitionServer.port", "500"+number);
 		GossipCluster cluster = new GossipCluster(CLUSTER, "Member-"+number, "localhost", 8000+number, meta, member );
 		GossipServer server = new GossipServer(cluster);
 		server.start();
 
 		final ActorFactory factory = new ActorFactory() {
 			
 			public Actor createActor(final Message input) {
 					System.out.println("New actor created: "+input.getTo().getId());
 					return new TheaterActor(input.getTo());
 			}
 		};
 
 		final ActorStore store = new ActorStore() {
 			
 			private Map<ActorId,Actor> actors = new HashMap<ActorId, Actor>();
 			
 			public void writeActor(Actor actor) {
 				actors.put(actor.getId(), actor);
 			}
 			
 			public Actor readActor(ActorId actorId) {
 				return actors.get(actorId);
 			}
 		};
 
 		LocalPartitionFactory local = new LocalPartitionFactory() {
 
 			public LocalPartition createPartition(int id) {
 				return new LocalPartition(id, factory, store);
 			}
 		};
 		RemotePartitionFactory remote = new RemotePartitionFactory() {
 
 			public RemotePartition createPartition(int id, ClusterMember host) {
 				MessageLog log = new NoopPartitionMessageLog();
 				return new RemotePartition(id, host, log);
 			}
 		};
 		ClusteredPartitionManager partitionManager = new ClusteredPartitionManager(8, cluster, local, remote);
 		PartitionServer partitionServer = new PartitionServer("localhost", 5000+number, partitionManager);
 		partitionServer.start();
 		
 		final PartitionMessageSender sender = new PartitionMessageSender(partitionManager);
 		final ActorId to = new ActorIdImpl(UUID.randomUUID(), CLUSTER);
 		final ActorId from = new ActorIdImpl(UUID.randomUUID(), CLUSTER);
 
 		Timer t = new Timer();
 		t.scheduleAtFixedRate(new TimerTask() {
 			
 			@Override
 			public void run() {
				final PartitionMessage message = new PartitionMessage(1, UUID.randomUUID(), from, to);
 				message.setParameter("test", "value");
 				sender.sendMessage(message);
 			}
 		}, 600, 1000);
 
 	}
 	
 }
