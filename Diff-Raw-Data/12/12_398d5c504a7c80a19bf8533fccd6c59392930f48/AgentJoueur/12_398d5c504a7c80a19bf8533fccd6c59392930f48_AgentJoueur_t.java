 package agent;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.UnreadableException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Vector;
 
 import util.Constantes;
 import util.Constantes.Couleur;
 import util.Constantes.Pion;
 import util.Logger;
 import view.Case;
 import view.CaseAchetable;
 import view.CaseTerrain;
 import view.Infos;
 import behaviour.RecupInitialCapital;
 
 public class AgentJoueur extends Agent{
 	private static final long serialVersionUID = 1L;
 	
 	private String 	nomJoueur;
 	private AID 	seed;
 	private AID	 	monopoly;
 	private Pion 	pion;
 	private Case 	caseCourante;
 	private int		capitalJoueur;
 	private boolean	enFaillite;
 	private boolean regles;
 	private int 	probaDemandeLoyer;
 	private ACLMessage propagateMessage = null;
 	private Vector<CaseAchetable> proprietesDujoueur;
 	private Infos myInfos;
 	private int myID;
 
 	private void fetchSeedAgent() {
 		DFAgentDescription template = new DFAgentDescription();
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType("seed"); 
 		template.addServices(sd);
 		DFAgentDescription[] result;
 		
 		try {
 			do{ 
 				result = DFService.search(this, template);
 			}while (result.length == 0);
 			
 			seed = result[0].getName();
 			//System.out.println("L'agent " + getLocalName() + " est connecte a l'agent SEED");
 		}
 		catch(FIPAException fe) { Logger.err("Exception  la recuperation du seedagent par le joueur "); fe.printStackTrace(); }
 	} 
 	
 	private void registerPlayer() 
 	{
 		DFAgentDescription agentDescription = new DFAgentDescription();
         agentDescription.setName(getAID());
         ServiceDescription serviceDescription  = new ServiceDescription();
         serviceDescription.setType("joueur");
         serviceDescription.setName(getLocalName());
         agentDescription.addServices(serviceDescription);
         try {
             DFService.register(this, agentDescription);
         } 
         catch (FIPAException e) { Logger.err("Enregistrement de l'agent joueur au service echoue - Cause : " + e); }  
 	}
 	
 	protected void setup() {
 		fetchSeedAgent(); 
 		Object[] params = this.getArguments();	
 		proprietesDujoueur = new Vector<CaseAchetable>();
 		pion = ((Pion)params[0]);
 		nomJoueur = ((String)params[2]);
 		capitalJoueur = 0;
 		enFaillite = false;
 		regles = true;
 		myInfos = ((Infos)params[3]);
 		myID = ((Integer)params[4]);
 		registerPlayer(); 
 		
 		myInfos.addInfo("nomJoueur", this.nomJoueur+" ("+this.getLocalName()+")", myID);
 		myInfos.addInfo("pion", this.pion.toString(), myID);
 		
 		// Comportement initial
 		addBehaviour(new RecupInitialCapital(this, params));
 	}
 	
 	public void setRegles(boolean regles) {this.regles = regles;}
 	public void addComportement(String value){myInfos.addInfo("strategie", value, myID);}
 	
 	/**
 	 * Regarde si le joueur peux acheter des Maisons
 	 * Condition : Avoir tous les terrains de la couleur ou que les regles sont abolies
 	 */
 	public ArrayList<Couleur> possedeLaCouleur(){
 		HashMap<Couleur, Integer> m = new HashMap<Couleur, Integer>();
 		m.put(Couleur.ROUGE, 0); m.put(Couleur.JAUNE, 0); m.put(Couleur.VERT, 0); m.put(Couleur.BLEU_FONCE, 0); m.put(Couleur.MAGENTA, 0); 
 		m.put(Couleur.BLEU_CIEL, 0); m.put(Couleur.VIOLET, 0); m.put(Couleur.ORANGE, 0);
 		
 		for(CaseAchetable c : proprietesDujoueur){
 			Integer i = m.get(c.getCouleur());
 			if(i != null){
 				if(c instanceof CaseTerrain){
 					if(((CaseTerrain)c).getNbMaisons() < Constantes.NB_MAX_MAISONS_PAR_CASE){
 						i++;
 						m.put(c.getCouleur(), i);
 					}
 				}
 				else{
 					i++;
 					m.put(c.getCouleur(), i);
 				}
 			}
 		}
 		//System.out.println(m);
 		
 		ArrayList<Couleur> res = new ArrayList<Couleur>();
 		
 		if(regles){
 			if(m.get(Couleur.ROUGE) == 3) res.add(Couleur.ROUGE);
 			if(m.get(Couleur.JAUNE) == 3) res.add(Couleur.JAUNE);
 			if(m.get(Couleur.VERT) == 3) res.add(Couleur.VERT);
 			if(m.get(Couleur.BLEU_FONCE) == 2) res.add(Couleur.BLEU_FONCE);
 			if(m.get(Couleur.MAGENTA) == 2) res.add(Couleur.MAGENTA);
 			if(m.get(Couleur.BLEU_CIEL) == 3) res.add(Couleur.BLEU_CIEL);
 			if(m.get(Couleur.VIOLET) == 3) res.add(Couleur.VIOLET);
 			if(m.get(Couleur.ORANGE) == 3) res.add(Couleur.ORANGE);
 		}
 		else{
 			if(m.get(Couleur.ROUGE) > 0) res.add(Couleur.ROUGE);
 			if(m.get(Couleur.JAUNE) > 0) res.add(Couleur.JAUNE);
 			if(m.get(Couleur.VERT) > 0) res.add(Couleur.VERT);
 			if(m.get(Couleur.BLEU_FONCE) > 0) res.add(Couleur.BLEU_FONCE);
 			if(m.get(Couleur.MAGENTA) > 0) res.add(Couleur.MAGENTA);
 			if(m.get(Couleur.BLEU_CIEL) > 0) res.add(Couleur.BLEU_CIEL);
 			if(m.get(Couleur.VIOLET) > 0) res.add(Couleur.VIOLET);
 			if(m.get(Couleur.ORANGE) > 0) res.add(Couleur.ORANGE);
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * Recherche le prix des maisons pour une couleur particulire
 	 * @param coul : la couleur des cases
 	 */
 	public int[] getPrixMaison(Couleur coul) {
 		int prix[] = {0,0};
 		
 		for(CaseAchetable c : proprietesDujoueur){
 			if(c.getCouleur() == coul){
 				prix[0] = ((CaseTerrain) c).getValeurMaison();
 				prix[1]++;
 			}
 				
 		}
 		return prix;
 	}
 	
 	/**
 	 * Calcule une valeur entre 1 et 100. Si celle-ci correspond  la probabilit de demande,
 	 * on envoi un message au joueur concern lui demandant de payer le loyer qu'il doit  this
 	 * Mthode utilise par les comportements du joueur
 	 * @param msgReceived message de requte reu
 	 */
 	public boolean demanderLoyer(ACLMessage msgReceived){
 		Random rand = new Random();
 		int value = rand.nextInt(100)+1;
 		if (value <= probaDemandeLoyer){
 			Case caseJoueur = null;
 			try{
 				caseJoueur = (Case) msgReceived.getContentObject();
 			}
 			catch (UnreadableException e1){e1.printStackTrace();}
 
 			ACLMessage demandeLoyer = new ACLMessage(ACLMessage.REQUEST);
 			int montantLoyer = 0;
 			if (caseJoueur instanceof CaseTerrain){
 				int nbMaisons = ((CaseTerrain)caseJoueur).getNbMaisons(); 
 				montantLoyer = ((CaseAchetable)caseJoueur).getLoyers().get(nbMaisons);
 			}
 			else if (caseJoueur instanceof CaseAchetable){  
 				montantLoyer = ((CaseAchetable)caseJoueur).computeLoyer();
 			}
 			demandeLoyer.setContent(String.valueOf(montantLoyer));			
 			demandeLoyer.addReceiver(caseJoueur.getJoueurQuiVientdArriver());
 				
 			send(demandeLoyer);
 			
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Envoi un message contenant le montant demand 
 	 *  l'agent destinataire de cette somme (un joueur ou la banque)
 	 * @param msgReceived le message de demande d'argent reu par le joueur
 	 */
 	public void payerMontantDu(ACLMessage msgReceived){
 		ACLMessage response = msgReceived.createReply();
 		int montantDu = Integer.parseInt(msgReceived.getContent().trim());
 		setCapitalJoueur(capitalJoueur-montantDu);
 		response.setContent(String.valueOf(montantDu));
 		response.setPerformative(ACLMessage.AGREE);
 		send(response);
 		System.out.println("Mouvement d'argent : " + getLocalName() + " -> -" + montantDu);
 	}
 	
 	/**
 	 * Envoi un message  l'agent monopoly pour lui signaler que this est en faillite
 	 * Le joueur n'a plus d'argent, il a perdu
 	 */
 	public void faillite(){
 		ACLMessage msgFaillite = new ACLMessage(ACLMessage.INFORM_REF);
 		msgFaillite.setContent(getNom()+ " a perdu");
 		msgFaillite.addReceiver(monopoly);
 		send(msgFaillite);
 	}
 	
 	public AID getSeed() { return seed;	}
 	public AID getMonopoly() { return monopoly; }
 	public Pion getPion() { return pion; }
 	public String getNom() { return nomJoueur; }
 	public Case getCaseCourante() { return caseCourante; }
 	public int getCapitalJoueur() { return capitalJoueur; }
 	public boolean isEnFaillite() {return enFaillite;}
 
 	public boolean aRecuPropagate() {return propagateMessage != null;}
 	public ACLMessage getPropagateMessage() {return propagateMessage;}
 	public void setPropagateMessage(ACLMessage propagateMessage) {this.propagateMessage = propagateMessage;}
 	
 	public Vector<CaseAchetable> getProprietesDujoueur() {return proprietesDujoueur;}
 	public void setProprietesDujoueur(Vector<CaseAchetable> proprietesDujoueur) {this.proprietesDujoueur = proprietesDujoueur;}
 	public void addProprieteToJoueur(CaseAchetable propriete) {this.proprietesDujoueur.add(propriete);}
 	
 	public void setPion(Pion pion) { this.pion = pion; }
 	public void setNom(String nom) { nomJoueur = nom; }
	public void setCaseCourante(Case caseCourante) { 
		this.caseCourante = caseCourante;
		myInfos.addInfo("case", this.caseCourante.getNom(), myID);
	}
 	public void setMonopoly(AID m) { monopoly = m; }
 	public void setEnFaillite(boolean enFaillite) {this.enFaillite = enFaillite;}
 	
 	public void setCapitalJoueur(int capitalJoueur) {
 		if (capitalJoueur <= 0){
 			this.capitalJoueur = 0;
 			faillite(); // Joueur en faillite
 		}
 		else{
 			this.capitalJoueur = capitalJoueur;
 		}
 		
 		myInfos.addInfo("argent", Integer.toString(this.capitalJoueur), myID);
 	}
 
 	public int getProbaDemandeLoyer() {return probaDemandeLoyer;}
 	public void setProbaDemandeLoyer(int probaDemandeLoyer) {this.probaDemandeLoyer = probaDemandeLoyer;}
 }
