 package istic.sapfor.client.command.impl;
 
 import java.util.List;
 
 import istic.sapfor.api.dto.AgentDTO;
 import istic.sapfor.api.service.ServiceAgent;
 import istic.sapfor.client.command.ICommand;
 import istic.sapfor.client.command.ICommandContext;
 import istic.sapfor.client.command.ICommandContextKey;
 
 //Commande permettant la cration d'un agent
 //Un agent est cre avec un nom, prnom, login ainsi qu'un type (directeur ou non)
 public class AddAgent implements ICommand {
 
 	private ServiceAgent client;
 
 	@Override
 	public Boolean execute(ICommandContext ctx) {
 		List<String> infoAddAgent = ctx.getList(ICommandContextKey.Key_Add);
 		String nom = infoAddAgent.get(0);
 		String prenom = infoAddAgent.get(1);
 		String login = infoAddAgent.get(2);
 		String type = infoAddAgent.get(3);
 		System.out.println("c'est quoi ton type" + type);
 		AgentDTO a = new AgentDTO();
 		a.setName(nom);
 		a.setFirstName(prenom);
 		a.setLogin(login);
 		a.setIdTypeAgent(Integer.parseInt(type));
 		boolean effectif = client.addAgent(a);
 		if (effectif) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public ServiceAgent getClient() {
 		return client;
 	}
 
 	public void setClient(ServiceAgent client) {
 		this.client = client;
 	}
 
 }
