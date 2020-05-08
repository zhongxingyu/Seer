 package org.hackermongo.gameofkrowns.access.domain.events;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 import org.hackermongo.gameofkrowns.access.domain.Event;
 import org.hackermongo.gameofkrowns.access.domain.EventEntity;
 import org.hackermongo.gameofkrowns.access.domain.EventType;
 import org.hackermongo.gameofkrowns.access.domain.GameEntity;
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
			query="SELECT gameturn.game FROM GameTurn gameturn WHERE gameturn.timeout < :currentdate")
 })
 @Table(name = "GAME_TURN_EVENT")
 public class GameTurnEntity extends EventEntity implements GameTurn<GameEntity> {
 
 	private static final long serialVersionUID = 1L;
 
 	public GameTurnEntity() {
 		this.eventType = EventType.GAME_TURN;
 		this.timeout = new DateTime().plusHours(1).toDate();
 	}
 	
 	@Column(name="TIMEOUT")
 	private Date timeout;
 	
 	public Date getTimeout() {
 		return timeout;
 	}
 
 	@Override
 	public int compareTo(Event<GameEntity> o) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
