 package fr.vinsnet.compteurtarot.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import fr.vinsnet.compteurtarot.model.future.bid.OnBidLoaded;
 import fr.vinsnet.compteurtarot.model.future.player.OnPlayerLoaded;
 import fr.vinsnet.utils.ObjectWithId;
 
 
 public class Round implements ObjectWithId,OnPlayerLoaded,OnBidLoaded {
 
 	private static final int NB_BOUTS = 3;
 	public static final int MAX_SCORE= 91;
 	
 	private long id;
 	private long creationTimestamp;
 	private long updateTimestamp;
 	private List<Player> takers;
 	private List<Player> defenders;
 	private int nbBoutsTakers;
 	private float scoreTakers;
 	private PetitAuBout petitAuBout;
 	private List<Poignee> poignees;
 	private List<Bonus> bonus;
 	private Bid bidding;
 	private Game game;
 	
 	
 	public Round() {
 		takers = new ArrayList<Player>();
 		defenders = new ArrayList<Player>();
 		poignees = new ArrayList<Poignee>();
 		bonus = new ArrayList<Bonus>();
 		petitAuBout = null;
 		bidding=null;
 		nbBoutsTakers=-1;
 		scoreTakers=-1;
 		updateTimestamp=-1;
 		creationTimestamp=-1;
 		game=null;
 	}
 
 	public long getId() {
 		return id;
 	}
 	
 	public void setId(long id){
 		this.id=id;
 	}
 	
 	public float getScoreTakers() {
 		//if(scoreTakers==-1)return -1;
 		return scoreTakers;
 	}
 
 	public void setScoreTakers(float scoreTakers) {
 		this.scoreTakers = scoreTakers;
 	}
 	
 	public float getScoreDefenders() {
 		if(scoreTakers==-1)return -1;
 		return MAX_SCORE-scoreTakers;
 	}
 
 	public void setScoreDefenders(float scoreDefenders) {
 		this.scoreTakers = MAX_SCORE-scoreDefenders;
 	}
 
 	public List<Player> getTakers() {
 		return takers;
 	}
 
 	public List<Poignee> getPoignees() {
 		return poignees;
 	}
 
 	public int getNbBoutsTakers() {
 		if(nbBoutsTakers==-1)return -1;
 		return nbBoutsTakers;
 	}
 
 	public void setNbBoutsTakers(int nbBoutsTakers) {
 		this.nbBoutsTakers = nbBoutsTakers;
 	}
 	public int getNbBoutsDefenders() {
 		if(nbBoutsTakers==-1)return -1;
 		return NB_BOUTS-nbBoutsTakers;
 	}
 
 	public void setNbBoutsDefenders(int nbBoutsDefenders) {
 		this.nbBoutsTakers = NB_BOUTS-nbBoutsTakers;
 	}
 
 	public PetitAuBout getPetitAuBout() {
 		return petitAuBout;
 	}
 
 	public void setPetitAuBout(PetitAuBout petitAuBout) {
 		this.petitAuBout = petitAuBout;
 	}
 
 	public List<Player> getDefenders() {
 		return defenders;
 	}
 
 	public List<Bonus> getBonus() {
 		return bonus;
 	}
 
 	public void setbidding(Bid bidding) {
 		this.bidding = bidding;
 		
 	}
 	public Bid getBidding( ) {
 		return bidding;
 		
 	}
 
 	public void clearTeams() {
 		takers = new ArrayList<Player>();
 		defenders = new ArrayList<Player>();
 		
 	}
 
 	public boolean isUnsetScore() {
 		return getScoreTakers()==-1;
 	}
 
 	public boolean isUnsetNbBouts() {
 		return getNbBoutsTakers()==-1;
 	}
 
 	public boolean isPetitAuBout() {
 		return petitAuBout!=null;
 	}
 
 	public boolean isUnsetBidding() {
 		return bidding==null;
 	}
 
 
 	public void loadWithPlayers(List<Player> players) {
 		loadTakerAndDefenders(players);
 		loadPoigneesPlayer(players);
 		loadBonusPlayer(players);
 	}
 	public void loadWithBids(List<Bid> bids) {
 		bidding.loadWithBids(bids);
 	}
 
 	protected void loadPoigneesPlayer(List<Player> players) {
 		for( Poignee p : getPoignees()){
 			p.getPlayer().loadWithPlayers(players);
 		}
 	}
 	protected void loadBonusPlayer(List<Player> players) {
 		for( Bonus b : getBonus()){
 			b.getPlayer().loadWithPlayers(players);
 		}
 	}
 	
 	
 	
 	protected void loadTakerAndDefenders(List<Player> players) {
 		if(this.takers.isEmpty())return;
 
 
 		updateDefenderList(players);
 		
 		List<Player> roundPlayers = new ArrayList<Player>(this.getTakers());
 		roundPlayers.addAll(this.getDefenders());
 		
 		for(Player p :roundPlayers){
 			p.loadWithPlayers(players);
 		}
 		
 	}
 
 	private List<Player> updateDefenderList(List<Player> players) {
 		
 		defenders.clear();
 		
 		for(Player p : players){
 			boolean isCurrentPlayerIsTaker = false;
 			for(Player t : takers){
 				if(t.getId()==p.getId()){
 					isCurrentPlayerIsTaker = true;
 				}
 			}
 			if(!isCurrentPlayerIsTaker){
 				defenders.add(p);
 			}
 		}
 		
 		return defenders;
 		
 	}
 
 	public Game getGame() {
 		return game;
 	}
 
 	public void setGame(Game game) {
 		this.game = game;
 	}
 
 	public long getCreationTimestamp() {
 		return creationTimestamp;
 	}
 
 	public void setCreationTimestamp(long creationTimestamp) {
 		this.creationTimestamp = creationTimestamp;
 	}
 
 	public long getUpdateTimestamp() {
 		return updateTimestamp;
 	}
 
 	public void setUpdateTimestamp(long updateTimestamp) {
 		this.updateTimestamp = updateTimestamp;
 	}
 
 
 	
 	
 }
