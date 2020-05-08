 package platform;
 
 import jade.core.Profile;
 import jade.core.ProfileImpl;
 import jade.core.Runtime;
 import jade.wrapper.AgentContainer;
 import jade.wrapper.AgentController;
 
 import java.util.Vector;
 
 import util.Constantes;
 import util.Constantes.Pion;
 import util.Helper;
 import view.Infos;
 
 public class MainContainer {
	private static final int NB_JOUEURS = 6; //Peut-tre chang
 	private static Vector<String> lesNomsDeJoueur;
 	private static AgentContainer mc;
 	
 	public static void main(String[] args){
 		Infos infos = new Infos();
 		
 		
 		Runtime rt = Runtime.instance();
 		Profile p = null;
 		lesNomsDeJoueur = Helper.computePlayersName();
 		try {
 			p = new ProfileImpl();
 			p.setParameter(Profile.GUI, "true");
 			mc = rt.createMainContainer(p);
 			AgentController bc;
 			
 			bc = mc.createNewAgent("PRISON", "agent.AgentPrison", null);
 			bc.start();
 			
 			bc = mc.createNewAgent("SEED", "agent.AgentSeed", null);
 			bc.start();
 			
 			// On lance les joueurs une fois que le plateau est prt 
			for(int i = 1; i <= NB_JOUEURS; ++i){
 				Object[] params = new Object[5];
 				params[0] = (Pion)Constantes.lesPions[i-1];
 				params[1] = (Integer) i-1;
 				params[2] = lesNomsDeJoueur.get(i);
 				params[3] = infos;
 				params[4] = i;
 				bc = mc.createNewAgent("JOUEUR" + i, "agent.AgentJoueur", params);
 				bc.start();
 			}
 			Object[] param = new Object[1];
 			param[0] = infos;
 			bc = mc.createNewAgent("MONOPOLY", "agent.AgentMonopoly", param);
 			bc.start();			
 		}
 		catch(Exception ex){ex.printStackTrace();}
 	}
 
 	public static AgentContainer getMc() {return mc;}
 	public static void setMc(AgentContainer mc) {MainContainer.mc = mc;}
 }
