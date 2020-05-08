 package behaviour;
 
 import jade.core.AID;
 import jade.core.behaviours.Behaviour;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.UnreadableException;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Vector;
 
 import util.Constantes;
 import util.Constantes.Pion;
 import view.Carte;
 import view.Plateau;
 import agent.AgentMonopoly;
 
 public class OrdonnanceurBehaviour extends Behaviour {
 
 	private static final long serialVersionUID = 1L;
 	private Vector<DFAgentDescription> lesJoueurs;
 	private HashMap<DFAgentDescription, Integer> lesPositionsDesJoueurs;
 	private HashMap<DFAgentDescription, Boolean> canPlayerBeReleasedFromJail;
 	HashMap<Pion, Integer> lesJoueursEtLesPions;
 	private int currentTour;
 	private AID prison;
 	private AID banque;
 	private Plateau plateau;
 	
 	public OrdonnanceurBehaviour(AgentMonopoly agentMonopoly, Plateau pl, Vector<DFAgentDescription> j, AID p) {
 		super(agentMonopoly);
 		lesJoueurs = j;
 		prison = p;
 		plateau = pl;
 		banque = new AID("BANQUE", AID.ISLOCALNAME);
 		currentTour = 0; 
 		
 		lesPositionsDesJoueurs = new HashMap<DFAgentDescription, Integer>();
 		lesJoueursEtLesPions = new HashMap<Pion, Integer>();
 		canPlayerBeReleasedFromJail = new HashMap<DFAgentDescription, Boolean>();
 		
 		for ( DFAgentDescription joueur : lesJoueurs ) {
 			lesPositionsDesJoueurs.put(joueur, 0);
 			int num = Integer.parseInt(joueur.getName().getLocalName().substring(6));
 			lesJoueursEtLesPions.put(Constantes.lesPions[num-1], 0);
 		}
 		plateau.setPositionJoueurs(lesJoueursEtLesPions);
 		
 		// Tous les joueurs doivent-tre sur la case depart
 		myAgent.blockingReceive(); 
 	}
 	
 	public void sendToJail(AID player) {
 		System.out.println("Envoi du joueur " + player + " en prison");
 		ACLMessage tick = new ACLMessage(ACLMessage.CONFIRM);
 		tick.addReceiver(prison);
 		try {
 			tick.setContentObject(player);
 			myAgent.send(tick);
 		} 
 		catch (IOException e) { System.out.println(e.getMessage()); }
 	}
 	
 	public void libererJoueur(AID player) {
 		System.out.println("Liberation du  joueur " + player + " emprisonne");
 		ACLMessage tick = new ACLMessage(ACLMessage.DISCONFIRM);
 		tick.addReceiver(prison);
 		try {
 			tick.setContentObject(player);
 			myAgent.send(tick);
 		} 
 		catch (IOException e) { System.out.println(e.getMessage()); }
 	}
 
 
 	@Override
 	public void action() {  
 		DFAgentDescription joueur = lesJoueurs.get(currentTour); 
 		
 		//System.out.println("Envoi d'un message a " + joueur.getName().getLocalName() + " pour qu'il lance les des");
 		throwDice(joueur);
 		
 		//System.out.println("Reception du score du joueur " + messageReceived.getSender().getLocalName() + " : " + message.getContent());
 		ACLMessage messageReceived = myAgent.blockingReceive(); 
 		
 		// Deplacement du pion du joueur
 		if ( messageReceived != null ) { 
 			if ( messageReceived.getPerformative() == ACLMessage.INFORM ) { 
 				// Cas classique : le joueur n'est pas en faillite
 				String playerName = messageReceived.getSender().getLocalName();
 				Integer oldPosition = lesPositionsDesJoueurs.get(joueur);
 				Integer diceValue = Integer.parseInt(messageReceived.getContent());
 				int newPos;
 				boolean canPlayerPlay = true;
 				
 				// Calcul de la nouvelle position
 				newPos = oldPosition + diceValue;
 				
 				if ( wasPlayerInJail(joueur) ) {
 					// Le joueur etait en prison >> 
 					if ( ! playerCanBeRealeased(diceValue, joueur, playerName) ) {
 						newPos = Constantes.CASE_PRISON; // S'il n'a pas fait 12, il reste sur sa case
 						canPlayerPlay = false;
 					}
 					else {
 						libererJoueur(joueur.getName());
 					}
 					
 				}
 				if ( canPlayerPlay ) {
 					// Libre de se deplacer
 					if ( newPos == Constantes.CASE_GOTOPRISON ) {
 						// Le joueur tombe sur la case prison, il faut l'y envoyer
 						newPos = Constantes.CASE_PRISON;
 						sendToJail(joueur.getName() );
 					}
 					if ( newPos > Constantes.CASE_FIN ) {
 						newPos -= Constantes.CASE_FIN;
 						System.out.println(playerName + " a fini un tour");
 						giveMoneyToPlayer(playerName, 20000); // Le joueur a fait un tour complet
 					}
 					switch ( newPos ) {
 					case Constantes.CASE_IMPOTS :
 						System.out.println(playerName + " est tombe sur la case IMPOTS");
 						makePlayerPay(playerName, 20000); 
 						break;
 					case  Constantes.CASE_TAXE :
 						System.out.println(playerName + " est tombe sur la case TAXE");
 						makePlayerPay(playerName, 10000); 
 						break;	
 					}
 					if ( plateau.isCaseChance(newPos)) {
 						Carte c = plateau.tirageChance();
 						System.out.println(playerName + " tire une carte Chance :\n" + c.getMsg());
 						executeActionCarte(joueur, playerName, c);
 						
 					}
 					if ( plateau.isCaseCommunaute(newPos)) {
 						Carte c = plateau.tirageCommunaute();
 						System.out.println(playerName + " tire une carte Communaute :\n" + c.getMsg());
 						executeActionCarte(joueur, playerName, c);
 					} 
 				}
 			 
 				lesPositionsDesJoueurs.put(joueur, newPos);
 				int num = Integer.parseInt(joueur.getName().getLocalName().substring(6));
 				lesJoueursEtLesPions.put(Constantes.lesPions[num-1], newPos);
 				plateau.setPositionJoueurs(lesJoueursEtLesPions);
 				//System.out.println(lesJoueursEtLesPions);
 				
 				// L'agent Monopoly envoie au joueur la case
 				ACLMessage caseCourante = new ACLMessage(ACLMessage.INFORM_REF);
 				try {
 					caseCourante.setContentObject(plateau.getCase(newPos));
 					caseCourante.addReceiver(joueur.getName());
 					myAgent.send(caseCourante);
 				} 
 				catch (IOException e1) { e1.printStackTrace(); }
 				try {
 					Thread.sleep(Constantes.DUREE_ANIMATION);
 				} 
				catch (InterruptedException e) {  e.printStackTrace(); }
 			}
 			else if (messageReceived.getPerformative() == ACLMessage.INFORM_REF)
 			{
 				// Joueur en faillite
 			}
 		}  
 		// On passe au joueur suivant
 		plateau.redrawFrame();
 		tourSuivant();
 	}
 
 	private boolean wasPlayerInJail(DFAgentDescription joueur) {
 		try {
 			ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
 			req.setContentObject(joueur.getName());
 			req.addReceiver(prison);
 			myAgent.send(req);
 			ACLMessage reply = myAgent.blockingReceive();
 			if ( reply != null ) {
 				if ( reply.getPerformative() == ACLMessage.CONFIRM ) { 
 					return (Boolean) reply.getContentObject();
 				}
 			}
 		} 
 		catch (IOException e) { 
 			e.printStackTrace();
 		} catch (UnreadableException e) { 
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Retourne si un joueur peut sortir ou non de prison
 	 * @param diceValue
 	 * @param joueur
 	 * @return
 	 */
 	private boolean playerCanBeRealeased(Integer diceValue,
 			DFAgentDescription joueur, String playerName) {
 		// Sur les valeurs du des
 		if ( diceValue == 12 ) {
 			// S'il fait 12, il sort
 			return true;
 		}
 		if ( diceValue % 2 == 0 && diceValue > 6 ) {
 			return true;
 		}
 		int nbTours = getTimePassedInJail(joueur.getName());
 		if ( nbTours == 3 ) {
 			makePlayerPay(playerName, 5000);
 			return true;
 		}
 		else {
 			if ( canPlayerBeReleasedFromJail.containsKey(joueur) ) {
 				// Le joueur dcide de se servir de sa carte
 				canPlayerBeReleasedFromJail.remove(joueur);
 				System.err.println("Le joueur " + playerName + " utilise sa carte et sort de prison!");
 				return true;
 			}
 			else {
 				// Le joueur dcide ou non de payer pour sortir TODO le faire dpendre du behaviour ?
 				boolean pay = new Random().nextBoolean();
 				if ( pay ) {
 					makePlayerPay(playerName, 5000);
 					System.err.println("Le joueur " + playerName + " decide de payer 5000F et de sortir de prison!");
 					return true;
 				}
 				System.err.println("Le joueur " + playerName + " prefere rester en prison");
 				incrementTimePassedInJail(joueur.getName());
 				return false;
 			}
 		}
 	}
 
 	/**
 	 * Demande  la prison, le nombre de tours passes par un joueur en prison
 	 * @param name le nom du joueur
 	 * @return le nombre de tours passes
 	 */
 	private void incrementTimePassedInJail(AID name) {
 		try {
 			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHENEVER);
 			msg.addReceiver(prison);
 			msg.setContentObject(name);
 			myAgent.send(msg); 
 		} 
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 	
 	/**
 	 * Demande  la prison, le nombre de tours passes par un joueur en prison
 	 * @param name le nom du joueur
 	 * @return le nombre de tours passes
 	 */
 	private int getTimePassedInJail(AID name) {
 		try {
 			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
 			msg.addReceiver(prison);
 			msg.setContentObject(name);
 			myAgent.send(msg);
 			ACLMessage reply = myAgent.blockingReceive();
 		} 
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return 0;
 	}
 
 	/**
 	 * Joueur execute action en fonction de la carte tiree
 	 * @param joueur le nom du joueur qui a tire la carte
 	 * @param messageToTheBank ACLMessage a envoyer a l'agent banque
 	 * @param playerName le nom du joueur
 	 * @param c la carte tiree
 	 */
 	private void executeActionCarte(DFAgentDescription joueur, String playerName, Carte c) {
 		if (c.getValeur() != 0 ) {
 			ACLMessage messageToTheBank = new ACLMessage(ACLMessage.SUBSCRIBE);
 			messageToTheBank.addReceiver(banque);
 			messageToTheBank.setContent(playerName + "#" + c.getValeur());
 			myAgent.send(messageToTheBank);
 		}
 		else {
 			if ( c.goToJail() ) {
 				// Carte qui envoie le joueur en prison
 				sendToJail(joueur.getName());
 			}					
 			if ( c.canSetFreeFromJail() ) {
 				// Carte qui permet de librer de prison
 				canPlayerBeReleasedFromJail.put(joueur, true);
 			}
 		}
 	}
 
 	/**
 	 * Faire payer un joueur
 	 * @param playerName le nom du joueur
 	 * @param money la somme  prelever
 	 */
 	private void makePlayerPay(String playerName, int money) {
 		ACLMessage messageToTheBank = new ACLMessage(ACLMessage.SUBSCRIBE);
 		messageToTheBank.addReceiver(banque);
 		messageToTheBank.setContent(playerName + "#" + "-" + money);
 		myAgent.send(messageToTheBank);
 	}
 	 
 	/**
 	 * Donner de l'argent a un joueur
 	 * @param playerName le nom du joueur
 	 * @param money la somme  donner
 	 */
 	private void giveMoneyToPlayer(String playerName, int money) {
 		ACLMessage messageToTheBank = new ACLMessage(ACLMessage.SUBSCRIBE);
 		messageToTheBank.addReceiver(banque);
 		messageToTheBank.setContent(playerName + "#" + "+" + money);
 		myAgent.send(messageToTheBank);
 	}
 
 	private void throwDice(DFAgentDescription joueur) {
 		ACLMessage tick = new ACLMessage(ACLMessage.PROPAGATE);
 		tick.addReceiver(joueur.getName());
 		myAgent.send(tick);
 	}
 
 	private void tourSuivant() {
 		currentTour ++;
 		if ( currentTour >= Constantes.NB_JOUEURS ) {
 			currentTour = 0;
 		}
 	}
 
 	@Override
 	public boolean done() {
 		// A voir
 		return false;
 	}
 
 }
