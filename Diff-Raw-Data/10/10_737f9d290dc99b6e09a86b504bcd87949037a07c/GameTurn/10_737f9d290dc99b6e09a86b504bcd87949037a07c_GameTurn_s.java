 package org.hackermongo.gameofkrowns.access.domain.events;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.hackermongo.gameofkrowns.access.domain.Event;
 import org.joda.time.DateTime;
 
 /**
  * Event representing one turn complete of a game, IE all players have registered a move.
  * @author dansun
  *
  */
 @Entity
 @NamedQueries({
 	@NamedQuery(
 			name="findAllTimedOutEvents",
			query="SELECT gameturn.game FROM GameTurn gameturn WHERE gameturn.timeout < SYSDATE")
 })
 @Table(name = "GAME_TURN_EVENT")
 @Inheritance(strategy=InheritanceType.JOINED)
 @XmlRootElement(name="started")
 public class GameTurn extends Event {
 
 	private static final long serialVersionUID = 1L;
 
 	public GameTurn() {
 		this.eventType = EventType.GAME_TURN;
 		this.timeout = new DateTime().plusHours(1).toDate();
 	}
 	
 	@Column(name="TIMEOUT")
 	private Date timeout;
 	
 	public Date getTimeout() {
 		return timeout;
 	}
 	
 }
