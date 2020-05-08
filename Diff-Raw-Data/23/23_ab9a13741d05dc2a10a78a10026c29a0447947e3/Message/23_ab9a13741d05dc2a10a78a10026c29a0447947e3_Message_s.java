 package models;
 
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import play.api.Play;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Query;
 import com.avaje.ebean.validation.NotNull;
 
 @Entity
 @Table(name="messages")
 public class Message extends Model {
 	
 	private static final SimpleDateFormat datetimeFr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 	
 	@Id
 	public long id;
 	
 	@Constraints.Required
 	@Constraints.MaxLength(141)	// betten than Twitter !
 	@Column(length=141)
 	@NotNull
 	public String text;
 	
 	@NotNull
 	@ManyToOne
 	public User author;
 	
 	@ManyToOne
 	public Message reference;
 	
 	@NotNull
 	public Date date;
 	
 	public static Finder<Long,Message> find = new Finder<Long,Message>(
 			Long.class, Message.class
 	);
 
 	/**
 	 * Retourne les derniers messages à partir d'un ID donné
 	 * @param lastId Si null, retourne les derniers messages
 	 * @return
 	 */
 	public static List<Message> findGlobalLastMessages(final Long lastId) {
 		Query<Message> finder = find;
 		
 		if (lastId != null) {
 			finder = finder.where().gt("id", lastId).query();
 		}
 		
 		List<Message> messages = finder
 				.fetch("reference")
 				.fetch("reference.author")
 				.fetch("author")
 				.orderBy("id ASC")
 				.setMaxRows(getPaginationFirstCall())
 				.findList();
 		
 		Collections.reverse(messages);
 		return messages;
 	}
 
 
 	private static int getPaginationFirstCall() {
 		// TODO : Trouver comment utiliser GetOrElse
 		final scala.Option<Object> paginationConfig = Play.current().configuration().getInt("pagination.first");
 		return (Integer) (paginationConfig.isDefined() ? paginationConfig.get() : 20);
 	}
 	
 	public String getDateFr() {
 		return datetimeFr.format(date);
 	}
 
 }
