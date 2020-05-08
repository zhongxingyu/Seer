 /**
  * Copyright (C) 2009 Universidade Federal de Campina Grande
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ddg.emulator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import ddg.kernel.JEEventScheduler;
 import ddg.model.Aggregator;
 import ddg.model.DDGClient;
 import ddg.model.Machine;
 import ddg.model.MetadataServer;
 import ddg.model.data.DataServer;
 import ddg.model.loginAlgorithm.HomeLessLoginAlgorithm;
 import ddg.model.loginAlgorithm.LoginAlgorithm;
 import ddg.model.loginAlgorithm.SweetHomeLoginAlgorithm;
 import ddg.model.placement.CoLocatedWithSecondariesLoadBalance;
 import ddg.model.placement.CoLocatedWithSecondaryRandomPlacement;
 import ddg.model.placement.DataPlacementAlgorithm;
 import ddg.model.placement.RandomDataPlacementAlgorithm;
 import ddg.model.populateAlgorithm.NOPAlgorithm;
 import ddg.util.FileSizeDistribution;
 
 /**
  * TODO make doc
  * 
  * @author thiago - thiago@lsd.ufcg.edu.br
  */
 public class SeerTraceMain {
 
 	public static void main(String[] args) throws IOException {
 
 		/**
 		 * Arguments
 		 * 
 		 * 0. trace file 
 		 * 1. Data placement police [random, co-random, co-balance] 
 		 * 2. number of machines 
 		 * 3. homeless login [true, false] 
 		 * 4. migration probability [0, 1) 
 		 * 5. data_migration [true, false] 
 		 * 6. replication delay secs
 		 */
 
 		System.out.println(Arrays.toString(args));
 
 		final JEEventScheduler scheduler = new JEEventScheduler();
 
 		String traceFile = args[0];
 		String placement_police = args[1];
 		String num_machines = args[2];
 		String homeless = args[3];
 		String migration_prob = args[4];
 		String enableMigration = args[5];
 		long replicationDelayMillis = Long.parseLong(args[6]) * 1000;
 
 		DataPlacementAlgorithm placement = createPlacementPolice(placement_police);
 
 		Integer numberOfMachines = new Integer(num_machines);
 
 		// 1 GiBytes
 		long diskSize = 1024 * 1024 * 1024 * 1L;
 		FileSizeDistribution fileSizeDistribution = new FileSizeDistribution(
 				8.46, 2.38, diskSize);
 
 		// building network
 		List<Machine> machines = createMachines(scheduler, numberOfMachines);
 		List<DataServer> dataServers = createDataServers(scheduler,
 				numberOfMachines, diskSize, machines);
 
 		MetadataServer metadataServer = new MetadataServer(scheduler,
 				dataServers, placement, fileSizeDistribution,
 				new NOPAlgorithm());
 		List<DDGClient> clients = createClients(scheduler, numberOfMachines,
 				machines, metadataServer);
 
 		// login algorithm
		LoginAlgorithm loginAlgorithm = createLoginAlgorithm(new Boolean(
 				homeless), new Double(migration_prob), MetadataServer.ONE_DAY,
 				clients);
 		SeerParserAndEventInjector injector = new SeerParserAndEventInjector(
 				new File(traceFile), loginAlgorithm);
 		EmulatorControl control = EmulatorControl.build(scheduler, injector,
				metadataServer, new Boolean(enableMigration),
 				replicationDelayMillis);
 
 		metadataServer.populateNamespace(0, 2, dataServers);
 
 		control.scheduleNext();
 		scheduler.start();
 
 		System.out.println(Aggregator.getInstance());
 	}
 
 	/**
 	 * @param homeless
 	 * @param migrationProb
 	 * @param loginDuration
 	 * @param clients
 	 * @return
 	 */
 	private static LoginAlgorithm createLoginAlgorithm(Boolean homeless,
 			double migrationProb, int loginDuration, List<DDGClient> clients) {
 
 		// always the first machines is full of data, otherwise in the case of
 		// machines are empty
 
 		DDGClient firstClient = clients.get(0);
 
 		for (DDGClient ddgClient : clients) {
 
 			if (ddgClient.getMachine().getDeployedDataServers().get(0).isFull()) {
 				firstClient = ddgClient;
 				break;
 			}
 		}
 
 		if (homeless) {
 			return new HomeLessLoginAlgorithm(migrationProb, loginDuration,
 					firstClient, clients);
 		} else {
 			clients.remove(firstClient);
 			return new SweetHomeLoginAlgorithm(migrationProb, loginDuration,
 					firstClient, clients);
 		}
 
 	}
 
 	private static DataPlacementAlgorithm createPlacementPolice(String police) {
 
 		if (police.equals("random")) {
 			return new RandomDataPlacementAlgorithm();
 		} else if (police.equals("co-random")) {
 			return new CoLocatedWithSecondaryRandomPlacement();
 		} else if (police.equals("co-balance")) {
 			return new CoLocatedWithSecondariesLoadBalance();
 		}
 
 		return null;
 	}
 
 	/**
 	 * It create all clients.
 	 * 
 	 * @param scheduler
 	 * @param numberOfClients
 	 * @param herald
 	 * @param aggregator
 	 * @param machines2
 	 * @return
 	 */
 	private static List<DDGClient> createClients(JEEventScheduler scheduler,
 			int numberOfClients, List<Machine> machines, MetadataServer herald) {
 
 		Iterator<Machine> iterator = machines.iterator();
 
 		List<DDGClient> newClients = new LinkedList<DDGClient>();
 
 		for (int i = 0; i < numberOfClients; i++) {
 
 			if (!iterator.hasNext()) {
 				iterator = machines.iterator();
 			}
 
 			Machine machine = iterator.next();
 			DDGClient newClient = new DDGClient(scheduler, i, machine, herald);
 			newClients.add(newClient);
 		}
 
 		return newClients;
 	}
 
 	private static List<Machine> createMachines(JEEventScheduler scheduler,
 			int numberOfMachines) {
 
 		List<Machine> machines = new ArrayList<Machine>(numberOfMachines);
 
 		for (int i = 0; i < numberOfMachines; i++) {
 			machines.add(new Machine(scheduler, i));
 		}
 
 		return machines;
 	}
 
 	/**
 	 * Create Data Servers.
 	 * 
 	 * @param scheduler
 	 * @param numberOfDataServers
 	 * @param diskSize
 	 * @param machines
 	 * @return
 	 */
 	private static List<DataServer> createDataServers(
 			JEEventScheduler scheduler, int numberOfDataServers,
 			double diskSize, List<Machine> machines) {
 
 		List<DataServer> dataServers = new ArrayList<DataServer>();
 
 		for (Machine machine : machines) {
 			DataServer newDataServer = new DataServer(scheduler, machine,
 					diskSize);
 			dataServers.add(newDataServer);
 		}
 
 		return dataServers;
 	}
 }
