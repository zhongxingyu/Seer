 package models;
 
 import java.util.Set;
 import java.util.TreeSet;
 
 import models.interfaces.Listener;
 import Controller.Simulator;
 import Enum.EventType;
 
 public class Receiver implements Listener {
 	private Server server;
 	private PackageModel nextPackage;
 	private Simulator simulator;
 	private Set<PackageModel> receivedPackages;
 	
 	public Receiver(Server server) {
 		super();
 		this.server = server;
 		nextPackage = new PackageModel(0);
 		
 		receivedPackages = new TreeSet<PackageModel>();
 		
 		simulator = Simulator.getInstance();
 		simulator.registerListener(this, EventType.PACKAGE_DELIVERED);
 	}
 
 	@Override
 	public void Listen(Event event) {
 		if (event.getSender().equals(server)) {
 			PackageModel eventPackage = event.getPackageModel();
						
 			if (eventPackage.equals(nextPackage)) {
 				
 				//Procura próximo pacote ainda não recebido
 				nextPackage = new PackageModel(nextPackage.getValue() + simulator.getMss());
 				Set<PackageModel> packagesToRemove = new TreeSet<PackageModel>();
 				
 				while (receivedPackages.contains(nextPackage)) {
 					packagesToRemove.add(nextPackage);
 					nextPackage = new PackageModel(nextPackage.getValue() + simulator.getMss());
 				}
 				
 				//Limpa pacotes da primeira sequencia completa da lista de recebidos.
 				receivedPackages.removeAll(packagesToRemove);
 				sendAck(event);
 			} else if (eventPackage.compareTo(nextPackage) == 1){ //Se o pacote recebido for posterior ao esperado. Se for anterior ignora, pois já foi recebido
 				receivedPackages.add(eventPackage);
 				sendAck(event);
 			}
 			
 		}				
 	}
 
 	private void sendAck(Event event) {
		Set<PackageModel> newReceivedPackages = new TreeSet<PackageModel>();
		for (PackageModel packageModel : receivedPackages) {
			newReceivedPackages.add(packageModel);
		}
		
		nextPackage.setSackOption(newReceivedPackages);
 		
 		long initialTime = event.getTime();
 		simulator.shotEvent(this, initialTime + server.getGroup().getDelay(), event.leaveServerTime(), EventType.ACK, nextPackage);
 	}	
 }
