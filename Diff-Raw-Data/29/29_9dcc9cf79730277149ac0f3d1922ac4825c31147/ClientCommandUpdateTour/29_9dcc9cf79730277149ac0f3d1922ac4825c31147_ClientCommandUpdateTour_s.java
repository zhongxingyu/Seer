 package client.command;
 
 import client.JeuPanel;
 import client.ServerListener;
 import client.view.jeu.EcranJeu;
 
 import common.Joueur;
 import common.Partie;
 
 /**
  * @author omar
  */
 public class ClientCommandUpdateTour extends ClientCommand{
 	
 	@Override
 	public void execute(ServerListener _server){
 		// recuperer la partie (position des QG, ...) et de
 		Partie partie = (Partie)arguments[0];
 		Joueur joueur = (Joueur)arguments[1];
 		boolean isMonTour = (boolean)arguments[2];
 		
 		JeuPanel jeu = _server.getJeu();
 		jeu.getClient().setPartie(partie);
 		jeu.getClient().setJoueur(joueur);
 		jeu.getClient().update();
 		
 		EcranJeu ej = JeuPanel.getEcranJeu();
 		if (isMonTour){
 			ej.cacherEcranAttente();
 		}else{
 			ej.afficherEcranAttente();
 		}
		System.out.println(joueur.getLogin() + " : " + isMonTour);
 		ej.update();
 	}
 	
 }
