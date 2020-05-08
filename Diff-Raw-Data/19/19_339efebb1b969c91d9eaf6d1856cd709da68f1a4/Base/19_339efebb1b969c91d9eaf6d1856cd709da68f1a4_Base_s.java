 package game;
 
 import javax.swing.JButton;
 import javax.vecmath.Vector2f;
 
 import exceptions.RealPlayerException;
 
 import time.TimerGame;
 import time.Timerable;
 
 public class Base extends JButton implements Situable, Timerable{
 	
 	private int might;
 	private Player player;
 	private Vector2f position;
 	private int nbAgents;
 	private float momentOfTheLastGeneration;
 	
 	public int getMight() {
 		return might;
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 		
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 
 	public boolean hasPlayer() {
 		if (player != null) {
 			return true;
 		}
 		else return false;
 	}
 	
 	@Override
 	public Vector2f getPosition() {
 		return position;
 	}
 
 	@Override
 	public void setPosition(float x, float y) {
 		position.x = x;
 		position.y = y;
 	}
 	
 	public void setNbAgents(int agents) {
 		agents = (agents <= 0) ? 0 : agents;
 		this.nbAgents = agents;
 	}
 	
 	public int getNbAgents() {
 		return this.nbAgents;
 	}
 	
 	public float getMomentOfTheLastGeneration() {
 		return momentOfTheLastGeneration;
 	}
 
 	public void setMomentOfTheLastGeneration(float momentOfTheLastGeneration) {
 		this.momentOfTheLastGeneration = momentOfTheLastGeneration;
 	}
 
 	public void attackBase(Base enemy){
 		System.out.println("Attack !\n");
 		
 		int maxAgent = (int) Math.max(this.getNbAgents()*0.5f, enemy.getNbAgents());
 		for(int i = 0; i < maxAgent; ++i) {
 			// whoever had a LinkedList<Agent> should use attackAgent() here.
 			
 		}
 		return;
 	}
 	
 	public void generateAgent() {
 		this.setNbAgents(this.getNbAgents() + 1);
 	}
 	
 	public void deleteAgent() {
 		if(this.getNbAgents() <= 0) return;
 		this.setNbAgents(this.getNbAgents() - 1);
 	}
 	
 	/**
 	 * Deletes a specific number of agents in the base.
 	 * @param nbSentAgents	The number of agents to delete
 	 */
 	public void deleteAgents(int nbComingAgents) {
 		if(nbComingAgents >= this.nbAgents) {
 			this.nbAgents = 0;
 			// It will need to be improved : when the coming agents are superior than the current agents, then the base change of player (and some agents stay).
 		}
 		else {
 			this.nbAgents -= nbComingAgents; 
 		}
 	}
 	
 	/**
 	 * Add a specific number of agents in the base.
 	 * @param nbSentAgents The number of agents to add to the base
 	 */
 	public void addAgents(int nbComingAgents) {
 		this.nbAgents += nbComingAgents; 
 	}
 	
 	/**
 	 *  Manages the click on a Base.
 	 *  A click is obviously an action from the RealPlayer. 
 	 *  So we manage what to do according to the data of this unique realPlayer : if he has a selected base or not.
 	 * @throws RealPlayerException  
 	 */
 	public void clicked() throws RealPlayerException {
 		Player realPlayer = Game.getInstance().getPlayerManager().getRealPlayer();
 		Base selectedBases = realPlayer.getSelectedBases();
 		
 		// 1st case : the player doesn't have any selected base, so this one become his selected base !
 		if(selectedBases == null) {
 			realPlayer.setSelectedBases(this);
 			System.out.println("Base selected !");
 		}
 		// 2nd case : this base is already selected by the realPlayer : nothing is done
 		else if (selectedBases.equals(this)) {
 			System.out.println("Base already selected.");
 		}
 		
 		// 3rd case : the realPlayer has an other base selected : agents can go from the selected base to this one ! (and we deselect the base)
 		else {
 			System.out.println("Go go !!");
 			
 			int nbSentAgents = selectedBases.getNbAgents() / 2;
 			// the selected base send the agents
 			
 			if(this.getPlayer() != selectedBases.getPlayer()) {
 				// It's an attack !
 				/*
 				 * Will be managed by Engine (FIFO of commands) 
 				 */
				selectedBases.deleteAgents(nbSentAgents);
 				if(this.getNbAgents() == 0) {
 					this.setPlayer(selectedBases.getPlayer());
 				}
 			} else {
 				// It's only a move !
 				/*
 				 * Will be managed by Engine (FIFO of commands) 
 				 */
				selectedBases.addAgents(nbSentAgents);
 			}
 			// and the agents of this base are killed !
			this.deleteAgents(nbSentAgents);
 			realPlayer.setSelectedBases(null);
 		}
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder("Base :\n");
 		sb.append("might ");
 		sb.append(this.might);
 		sb.append("\n");
 		sb.append("player: ");
 		sb.append(this.player);
 		sb.append("\n");
 		sb.append("position: ");
 		sb.append(this.position);
 		sb.append("\n");
 		sb.append("nbAgents: ");
 		sb.append(this.getNbAgents());
 		sb.append("\n");
 		return sb.toString();
 	}
 	// -------------------------------------------------- Timerable ---------------
 	
 	@Override
 	public void runTimer() {
 		System.out.println(this.getNbAgents());
 		this.generateAgent();
 	}
 
 	@Override
 	public void runTimer(Timerable t) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void runTimer(Vector2f v) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void runTimer(boolean b) {
 		System.out.println(this.getNbAgents());
 		if(b == true)
 			this.generateAgent();
 		else
 			this.deleteAgent();
 	}
 
 	
 	// ----------------------------------------------------constructor-------------
 	
 	public Base() {
 		this(0);
 	}
 	
 	public Base(int might){
 		this(might, null);
 	}
 	
 	public Base(int might, Player player){
 		this(might, player, new Vector2f(0, 0));
 	}
 	
 	public Base(int might, Player player, Vector2f position){
 		this(might, player, position, 0);
 	}
 	
 	public Base(int might, Player player, Vector2f position, int nbAgents){
 		super();
 		this.might = might;
 		this.player = player;
 		this.position = position;
 		this.nbAgents = nbAgents;
 		this.momentOfTheLastGeneration = 0.0f;
 	}
 	
 	@SuppressWarnings("unused")
 	public static void main(String[] args) {
 		
 		//Agent strongAgent = new Agent(true, 10, 10, 10, 10, null, null);
 		//Agent weakAgent = new Agent(true, 5, 7, 5, 5, null, null);
 		
 		Base base1 = new Base(5, null, new Vector2f(0.5f, 0.5f), 5);
 		//System.out.println(base1);
 		
 		Base base2 = new Base(5, null, new Vector2f(-0.5f, -0.5f), 5);
 		//System.out.println(base2);
 		
 		
 		// If one attacks, the other defenses also !
 		base2.attackBase(base1);
 		
 
 		TimerGame tg = new TimerGame(1000/base1.getMight(), 0, 0, 0, base1, false);
 
 		
 	}
 
 
 
 
 }
