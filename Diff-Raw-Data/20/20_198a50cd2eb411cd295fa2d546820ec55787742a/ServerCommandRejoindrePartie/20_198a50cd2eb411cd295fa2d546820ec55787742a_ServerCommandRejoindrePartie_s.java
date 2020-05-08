 package server.command;
 
 import server.ClientListener;
 import server.Server;
 
 import common.Constante;
 import common.Etat;
 import common.Joueur;
 import common.Partie;
 import common.Translator;
 
 /**
  * @author omar
  */
 public class ServerCommandRejoindrePartie extends ServerCommand{
 	
 	@Override
 	public void execute(ClientListener _client){
 		int idPartie = Integer.parseInt(arguments[0]);
 		String password = arguments[1];
 		
 		Joueur joueur = Server.getJoueur(_client.getSocket());
 		Partie partie = Server.getParte(idPartie);
 		
 		
 		boolean rejoint = false;
 		String message_erreur = "";
 		
 		Etat etat = partie.getEtatDeLaPartie();
 		if (etat == Etat.EN_ATTENTE_JOUEUR || etat == Etat.SAUVEGARDEE){
 			if (partie.getPassword().equals(password)){
 				if (partie.placeDisponible()){
 					partie.addJoueur(joueur);
 					rejoint = true;
 				}else{
 					// erreur deja trop de joueur
 					message_erreur = Translator.translate("nbJoueurMaximumDejaAtteint");
 				}
 			}else{
 				// erreur mauvais mot de passe
 				message_erreur = Translator.translate("motDePasseIncorrect");
 			}
 		}else{
 			// erreur partie commencee
 			message_erreur = Translator.translate("partieDejaCommencee");
 		}
 		
 		
 		String[] args = { Constante.COMMANDE_REJOINDRE_PARTIE,Boolean.toString(rejoint),message_erreur };
 		_client.send(args);
 		
 		if (rejoint && !partie.placeDisponible()){
 			// dans le cas ou tout les sjoueurs ont rejoint la partie, on demarre le jeu
 			partie.notifierDebutJeu();
 		}
 	}
 	
 }
