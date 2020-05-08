 package fr.cpcgifts.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
import java.util.logging.Logger;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.google.appengine.api.datastore.Key;
 
 import fr.cpcgifts.persistance.CpcUserPersistance;
 import fr.cpcgifts.utils.TextTools;
 
 @PersistenceCapable(detachable="true")
 public class Giveaway implements Serializable {
 	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(Giveaway.class.getSimpleName());
 
 	@PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 	
 	@Persistent
 	private Key author;
 	
 	@Persistent
 	private String title = "";
 	
 	@Persistent
 	private String description = "";
 	
 	@Persistent
 	private String imgUrl = "";
 	
 	@Persistent
 	private List<Key> entrants;
 	
 	@Persistent
 	private List<Key> comments;
 	
 	/* TimeZone en UTC */
 	@Persistent
 	private Date endDate;
 	
 	@Persistent
 	private Key winner;
 	
 	@Persistent
 	private boolean open = true;
 	
 	
 	
 	public Giveaway(Key author, String title, String description, String imgUrl, Date endDate) {
 		this.author = author;		
 		setTitle(title);
 		setDescription(description);
 		setImgUrl(imgUrl);
 		
 		this.entrants = new ArrayList<Key>();
 		this.comments = new ArrayList<Key>();
 		
 		this.endDate = endDate;
 		
 	}
 
 	public Key getKey() {
 		return key;
 	}
 
 	public void setKey(Key key) {
 		this.key = key;
 	}
 
 	public Key getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(Key author) {
 		this.author = author;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = TextTools.escapeHtml(title);
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = TextTools.escapeHtml(description);
 	}
 
 	public String getImgUrl() {
 		return imgUrl;
 	}
 
 	public void setImgUrl(String imgUrl) {
 		if(imgUrl == "")
 			imgUrl = "img/game.png";
 		
 		this.imgUrl = imgUrl;
 	}
 
 	public List<Key> getEntrants() {
 		if(entrants == null)
 			entrants = new ArrayList<Key>();
 		
 		return entrants;
 	}
 	
 	public void addEntrant(Key cpcuser) {
 		getEntrants();
 		
 		entrants.add(cpcuser);
 	}
 	
 	public boolean removeEntrant(Key cpcuser) {
 		getEntrants();
 		
 		return entrants.remove(cpcuser);
 	}
 
 	public List<Key> getComments() {
 		if(comments == null)
 			comments = new ArrayList<Key>();
 		
 		return comments;
 	}
 
 	public void setComments(List<Key> comments) {
 		this.comments = comments;
 	}
 	
 	public void addComment(Key k) {
 		getComments();
 		
 		comments.add(k);
 		
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(Date endDate) {
 		this.endDate = endDate;
 	}
 
 	public Key getWinner() {
 		return winner;
 	}
 
 	public void setWinner(Key winner) {
 		this.winner = winner;
 	}
 
 	public boolean isOpen() {
 		return open;
 	}
 
 	public void setOpen(boolean open) {
 		this.open = open;
 	}
 
 	public void drawWinner() {
 		open = false;
		
		log.info("Giveaway " + getKey().getId() + ":" + title + " ended.");
		
 		if(entrants.size() == 0)
 			return;
 		
 		Random rand = new Random();
 		
 		int winnerIndex = rand.nextInt(entrants.size());
 		
 		winner = entrants.get(winnerIndex);
 		
 		CpcUser winnerEntity = CpcUserPersistance.getCpcUserUndetached(winner);
 		winnerEntity.addWon(this.key);
		log.info("Winner is " + winnerEntity.getCpcNickname() + " !");
 		CpcUserPersistance.closePm();
 	}
 
 	@Override
 	public String toString() {
 		return "Giveaway [key=" + key + ", author=" + author + ", title="
 				+ title + ", description=" + description + ", endDate="
 				+ endDate + ", winner=" + winner + ", open=" + open + "]";
 	}
 
 	
 	
 }
